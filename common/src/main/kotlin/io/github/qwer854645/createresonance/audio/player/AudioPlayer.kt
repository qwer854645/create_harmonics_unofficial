package io.github.qwer854645.createresonance.audio.player

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.qwer854645.createresonance.audio.comp.SoundEventComposition
import io.github.qwer854645.createresonance.audio.info.AudioInfo
import io.github.qwer854645.createresonance.audio.effect.EffectChain
import io.github.qwer854645.createresonance.audio.effect.EffectPreset
import io.github.qwer854645.createresonance.audio.effect.MixerEffect
import io.github.qwer854645.createresonance.audio.effect.PitchShiftEffect
import io.github.qwer854645.createresonance.audio.instance.SampleRatedInstance
import io.github.qwer854645.createresonance.audio.stream.AudioEffectInputStream
import io.github.qwer854645.createresonance.audio.utils.pause
import io.github.qwer854645.createresonance.audio.utils.unpause
import io.github.qwer854645.createresonance.config.ClientConfig
import io.github.qwer854645.createresonance.foundation.async.ClientCoroutineScope
import io.github.qwer854645.createresonance.foundation.async.modLaunch
import io.github.qwer854645.createresonance.foundation.async.withMainContext
import io.github.qwer854645.createresonance.foundation.debug
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.network.packet.AudioPlayerStreamEndPacket
import io.github.qwer854645.createresonance.foundation.network.packet.UpdateAudioNamePacket
import io.github.qwer854645.createresonance.foundation.registry.ModPackets
import io.github.qwer854645.createresonance.foundation.supplier.values.FloatInterpolator
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

typealias SoundInstanceFactory = AudioPlayer.(streamId: String, stream: InputStream) -> SoundInstance

// TODO: This class is very big now and needs some cleanup ASAP
// TODO: Split tail jobs with a proper manager
// TODO: Preload next stream so loop works without gaps (loop can set true or false externally)

/**
 * Audio player with the following features:
 * - Play audio from a stream or a url
 * - Idempotent commands
 * - Internal state machine with TAILING state for effect decay
 * - Supports synchronization
 */
class AudioPlayer(
    val playerId: String,
    val soundInstanceFactory: SoundInstanceFactory,
) {
    private val playerScope =
        CoroutineScope(
            ClientCoroutineScope.coroutineContext + SupervisorJob(ClientCoroutineScope.coroutineContext[Job]),
        )

    private val _state = MutableStateFlow(PlayerState.STOPPED)

    @Volatile
    var activeAudioInfo: AudioInfo? = null
        private set

    val durationSeconds: Int?
        get() = activeAudioInfo?.durationSeconds?.takeIf { it > 0 }

    @Volatile
    private var currentAudioRequest: AudioRequest? = null

    @Volatile
    private var currentAudioEffectInputStream: AudioEffectInputStream? = null

    @Volatile
    private var currentSoundInstance: SoundInstance? = null

    private val intents = Channel<PlayerIntent>(Channel.UNLIMITED)

    val playerTerminated = AtomicBoolean(false)

    @Volatile
    private var stateMachineJob: Job? = null

    @Volatile
    private var tailJob: Job? = null

    @Volatile
    private var startPlaybackJob: Job? = null

    private val streamResolutionStartMillis = AtomicLong(0)

    val effectChain = EffectChain()
    val soundEventComposition = SoundEventComposition(soundEffectChain = effectChain)
    val state: StateFlow<PlayerState> = _state.asStateFlow()
    val clock = PlaytimeClock()
    val isSeekingDisabled = AtomicBoolean(false)

    private val loadingGeneration = AtomicInteger(0)

    private val soundManager get() = Minecraft.getInstance().soundManager

    @Volatile
    private var lastResyncAt: Long = -1L
    private val resyncCooldown = 10.seconds

    @Volatile
    var context: AudioSpatialContext? = null

    @Volatile
    var contextKey: Any? = null

    val masterVolumeInterpolator = FloatInterpolator(1f, 4.0.seconds)
    val masterPitchInterpolator = FloatInterpolator(1f, 4.0.seconds)
    val masterRadiusInterpolator = FloatInterpolator(1f, 4.0.seconds)

    val underwaterFilter = EffectPreset.UnderwaterFilter()
    val reverberator = EffectPreset.Reverberator()

    init {
        startStateMachine()
    }

    fun startStateMachine() {
        val currentSm = stateMachineJob
        if (currentSm == null || !currentSm.isActive) {
            stateMachineJob =
                playerScope.launch(Dispatchers.IO) {
                    var shouldCancelTails = false
                    try {
                        for (intent in intents) {
                            try {
                                if (intent is PlayerIntent.Shutdown) {
                                    shouldCancelTails = intent.cancelTrail
                                    break
                                }
                                handleIntent(intent)
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                e.printStackTrace()
                            }
                        }
                    } finally {
                        withContext(NonCancellable) {
                            intents.close()
                            doStopPlayback(ignoreTail = shouldCancelTails)
                            if (shouldCancelTails) {
                                cancelTail()
                            }
                            playerScope.cancel()
                        }
                    }
                }
            playerScope.launch(Dispatchers.Default) {
                while (isActive) {
                    delay(1.seconds)
                    val instance = currentSoundInstance
                    val currentState = _state.value
                    if (currentState == PlayerState.PLAYING && instance != null) {
                        if (!soundManager.isActive(instance)) {
                            intents.trySend(PlayerIntent.AudioHanged)
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.Play -> {
                when (_state.value) {
                    PlayerState.PLAYING,
                    PlayerState.LOADING,
                    -> {
                        return
                    }

                    PlayerState.PAUSED -> {
                        // If there's a live sound instance, resume it.
                        // If not (we were paused mid-load), restart from the clock position.
                        if (currentSoundInstance != null) {
                            resumePlayback()
                        } else {
                            // Was paused before loading finished — treat as a fresh start
                            // from wherever the clock was when we paused.
                            doStopPlayback() // clean any partial state
                            startPlayback(clock.currentPlaytime)
                        }
                    }

                    PlayerState.STOPPED -> {
                        startPlayback(intent.initialPosition)
                    }

                    PlayerState.TAILING -> {
                        startPlayback(intent.initialPosition)
                    }
                }
            }

            is PlayerIntent.Pause -> {
                when (_state.value) {
                    PlayerState.PAUSED,
                    PlayerState.TAILING,
                    -> {
                        return
                    }

                    PlayerState.LOADING -> {
                        startPlaybackJob?.cancelAndJoin()
                        pausePlayback()
                    }

                    PlayerState.PLAYING -> {
                        pausePlayback()
                    }

                    else -> {
                        return
                    }
                }
            }

            is PlayerIntent.Stop -> {
                when (_state.value) {
                    PlayerState.STOPPED -> return
                    else -> doStopPlayback()
                }
            }

            is PlayerIntent.Seek -> {
                startPlaybackJob?.cancelAndJoin()
                if (isSeekingDisabled.get()) return
                when (_state.value) {
                    PlayerState.PLAYING,
                    PlayerState.PAUSED,
                    PlayerState.LOADING,
                    PlayerState.STOPPED,
                    -> {
                        if (_state.value == PlayerState.PLAYING ||
                            _state.value == PlayerState.PAUSED ||
                            _state.value == PlayerState.LOADING
                        ) {
                            doStopPlayback(ignoreTail = true, isSeek = true)
                        }
                        startPlayback(intent.position)
                    }

                    else -> return
                }
            }

            is PlayerIntent.StreamReady -> {
                if (_state.value != PlayerState.LOADING || intent.streamGeneration != loadingGeneration.get()) {
                    intent.stream.close()
                    return
                }

                currentAudioEffectInputStream = intent.stream
                currentSoundInstance = intent.soundInstance

                if (intent.audioInfo.isLive) {
                    effectChain
                        .getEffects()
                        .filterIsInstance<PitchShiftEffect>()
                        .forEach { it.preserveTiming() }
                    isSeekingDisabled.set(true)
                }

                activeAudioInfo = intent.audioInfo
                val resolutionElapsed = (System.currentTimeMillis() - streamResolutionStartMillis.get()) / 1000.0
                val adjustedPos = if (intent.audioInfo.isLive) 0.0 else intent.atPos + resolutionElapsed
                clock.play(adjustedPos)
                lastResyncAt = System.currentTimeMillis()
                withMainContext { soundManager.play(intent.soundInstance) }
                soundEventComposition.makeComposition(intent.soundInstance)
                notifyAudioMetadata(intent.audioInfo)

                transition(PlayerState.PLAYING)
            }

            is PlayerIntent.StreamFailed -> {
                doStopPlayback(ignoreTail = true)
                isSeekingDisabled.set(intent.shouldDisableSeek)
                if (intent.shouldRetry) {
                    intents.trySend(PlayerIntent.Play(0.0))
                }
            }

            is PlayerIntent.AudioFinished -> {
                startPlaybackJob?.cancelAndJoin()
                notifyStreamEnd()
                doStopPlayback()
            }

            is PlayerIntent.AudioHanged -> {
                startPlaybackJob?.cancelAndJoin()
                when (_state.value) {
                    PlayerState.PAUSED -> {
                        startPlaybackJob?.cancelAndJoin()
                        resumePlayback(freezeOnly = true)
                        pausePlayback()
                    }

                    PlayerState.PLAYING -> {
                        startPlaybackJob?.cancelAndJoin()
                        unstuckPlayback()
                    }

                    else -> {
                        return
                    }
                }
            }

            is PlayerIntent.TailFinished -> {
                if (_state.value == PlayerState.TAILING) {
                    transition(PlayerState.STOPPED)
                }
            }

            is PlayerIntent.NewRequest -> {
                if (currentAudioRequest?.isSameSource(intent.req) == true) return

                // Cancel in-flight resolution before changing the request
                startPlaybackJob?.cancelAndJoin()

                if (_state.value == PlayerState.PLAYING || _state.value == PlayerState.LOADING) return
                doStopPlayback()
                currentAudioRequest = intent.req
            }

            else -> {
                "Invalid intent sent to the audio player!".info()
            }
        }
    }

    private suspend fun startPlayback(pos: Double = 0.0) {
        cancelTail()
        val request = currentAudioRequest ?: return

        startPlaybackJob?.cancelAndJoin()
        loadingGeneration.incrementAndGet()
        transition(PlayerState.LOADING)

        launchStreamResolution(request, pos)
    }

    private fun launchStreamResolution(
        request: AudioRequest,
        pos: Double,
    ) {
        val currentGeneration = loadingGeneration.get()
        startPlaybackJob =
            playerScope.launch(Dispatchers.IO) {
                streamResolutionStartMillis.set(System.currentTimeMillis())
                val source = AudioSourceResolver.resolve(request)
                val resolvedStream =
                    try {
                        SourceStreamResolver.resolveInputStream(source, pos)
                    } catch (e: CancellationException) {
                        if (ClientConfig.debugAudioPlayer.get()) {
                            "Audio player cancellation was requested!".debug()
                        }
                        throw e
                    } catch (e: Exception) {
                        if (ClientConfig.debugAudioPlayer.get()) {
                            "[AUDIO PLAYER FAIL] The player failed, and a retry will be requested\n cause:${e.message ?: "no explicit cause.. this is bad"}"
                                .debug()
                        }
                        if (isActive) intents.trySend(PlayerIntent.StreamFailed())
                        return@launch
                    }
                if (!isActive) {
                    resolvedStream.inputStream?.close()
                    return@launch
                }

                if (resolvedStream.status == SourceStreamResolver.Result.StreamStatus.FINISHED) {
                    resolvedStream.inputStream?.close()
                    intents.trySend(PlayerIntent.AudioFinished)
                    return@launch
                }

                if (resolvedStream.inputStream == null || resolvedStream.status == SourceStreamResolver.Result.StreamStatus.FAILED) {
                    resolvedStream.inputStream?.close()
                    if (pos > 0) {
                        // Fall back to the start once — do not permanently disable seeking.
                        // Remote seek often fails transiently; disabling made the GUI appear broken.
                        "Seek to ${"%.1f".format(pos)}s failed; restarting from beginning".info()
                        if (isActive) {
                            intents.trySend(PlayerIntent.StreamFailed(shouldDisableSeek = false, shouldRetry = true))
                        }
                    } else if (isActive) {
                        intents.trySend(PlayerIntent.StreamFailed(shouldDisableSeek = false, shouldRetry = false))
                    }
                    return@launch
                }

                val stream =
                    AudioEffectInputStream(
                        resolvedStream.inputStream,
                        effectChain,
                        resolvedStream.audioInfo.sampleRate.toInt(),
                        onStreamEnd = { handleStreamEnd() },
                        onStreamHang = { handleStreamHang() },
                    )

                val soundInstance = soundInstanceFactory(this@AudioPlayer, playerId, stream)
                if (soundInstance is SampleRatedInstance) {
                    soundInstance.sampleRate = resolvedStream.audioInfo.sampleRate.toInt()
                }

                if (!isActive) {
                    stream.close()
                    return@launch
                }

                intents.trySend(
                    PlayerIntent.StreamReady(
                        stream,
                        soundInstance,
                        resolvedStream.audioInfo,
                        pos,
                        currentGeneration,
                    ),
                )
            }
    }

    private suspend fun pausePlayback() {
        val capturedInstance = currentSoundInstance ?: return handleStreamFailure()
        val capturedStream = currentAudioEffectInputStream

        clock.pause()
        transition(PlayerState.PAUSED)
        if (capturedStream?.hasTail == true) {
            capturedStream.resetTailSignal()
            capturedStream.isFrozen = true
            freezeMixerSources(true)
            tailJob =
                modLaunch(Dispatchers.IO) {
                    try {
                        withTimeoutOrNull(15.seconds) {
                            capturedStream.tailFinished.await()
                        }
                    } finally {
                        withContext(NonCancellable) {
                            withMainContext {
                                if (!capturedInstance.pause()) soundManager.stop(capturedInstance)
                            }
                        }
                    }
                }
        } else {
            withContext(NonCancellable) {
                capturedStream?.isFrozen = true
                freezeMixerSources(true)
                withMainContext {
                    if (!capturedInstance.pause()) soundManager.stop(capturedInstance)
                }
            }
        }
    }

    private suspend fun resumePlayback(freezeOnly: Boolean = false) {
        cancelTail()
        val capturedInstance = currentSoundInstance ?: return handleStreamFailure()

        effectChain.setFrozen(false)
        currentAudioEffectInputStream?.isFrozen = false

        if (freezeOnly) {
            currentAudioEffectInputStream?.isFrozen = true
            effectChain.setFrozen(true)
        }

        withMainContext {
            if (!capturedInstance.unpause()) soundManager.play(capturedInstance)
        }
        clock.play()
        transition(PlayerState.PLAYING)
    }

    private suspend fun unstuckPlayback() {
        cancelTail()
        val capturedInstance = currentSoundInstance ?: return handleStreamFailure()
        withMainContext {
            if (Minecraft.getInstance().level == null) return@withMainContext
            if (Minecraft.getInstance().isSingleplayer && Minecraft.getInstance().isPaused) return@withMainContext
            if (soundManager.isActive(capturedInstance)) return@withMainContext
            soundManager.play(capturedInstance)
        }
    }

    private suspend fun doStopPlayback(
        ignoreTail: Boolean = false,
        isSeek: Boolean = false,
    ) {
        startPlaybackJob?.cancelAndJoin()
        startPlaybackJob = null

        val capturedStream = currentAudioEffectInputStream
        val capturedInstance = currentSoundInstance
        currentAudioEffectInputStream = null
        currentSoundInstance = null
        activeAudioInfo = null

        soundEventComposition.stopComposition()
        clock.stop()
        isSeekingDisabled.set(false)
        if (!isSeek) lastResyncAt = -1L

        if (capturedStream?.hasTail == true && !ignoreTail) {
            _state.value = PlayerState.TAILING
            capturedStream.isFrozen = true
            tailJob =
                modLaunch(Dispatchers.IO) {
                    try {
                        withTimeoutOrNull(15.seconds) {
                            capturedStream.tailFinished.await()
                        }
                    } finally {
                        withContext(NonCancellable) {
                            withMainContext { capturedInstance?.let { soundManager.stop(it) } }
                            capturedStream.close()
                            effectChain.reset()
                        }
                    }
                    intents.trySend(PlayerIntent.TailFinished)
                }
        } else {
            withContext(NonCancellable) {
                withMainContext { capturedInstance?.let { soundManager.stop(it) } }
                capturedStream?.close()
                effectChain.reset()
                _state.value = PlayerState.STOPPED
            }
        }
    }

    private suspend fun handleStreamFailure(
        shouldDisableSeek: Boolean = false,
        shouldRetry: Boolean = false,
    ) {
        startPlaybackJob?.cancelAndJoin()
        startPlaybackJob = null
        doStopPlayback(ignoreTail = true)
        isSeekingDisabled.set(shouldDisableSeek)
        if (shouldRetry) {
            intents.trySend(PlayerIntent.Play(0.0))
        }
    }

    private suspend fun cancelTail() {
        tailJob?.cancelAndJoin()
        tailJob = null
    }

    private fun transition(next: PlayerState) {
        val current = _state.value
        check(current.canTransitionTo(next)) { "Invalid transition: $current → $next" }
        _state.value = next
    }

    private fun freezeMixerSources(frozen: Boolean) {
        effectChain
            .getEffects()
            .filterIsInstance<MixerEffect>()
            .forEach { it.setFrozen(frozen) }
    }

    private fun handleStreamEnd() = intents.trySend(PlayerIntent.AudioFinished)

    private fun handleStreamHang() = intents.trySend(PlayerIntent.AudioHanged)

    private fun notifyAudioMetadata(info: AudioInfo) =
        ModPackets.sendToServer(UpdateAudioNamePacket(playerId, info.title, info.durationSeconds))

    private fun notifyStreamEnd() = ModPackets.sendToServer(AudioPlayerStreamEndPacket(playerId))

    fun play(initialPosition: Double = 0.0) = intents.trySend(PlayerIntent.Play(initialPosition))

    fun pause() {
        intents.trySend(PlayerIntent.Pause)
    }

    fun stop() {
        intents.trySend(PlayerIntent.Stop)
    }

    fun seek(position: Double) {
        intents.trySend(PlayerIntent.Seek(position))
    }

    fun request(req: AudioRequest) = intents.trySend(PlayerIntent.NewRequest(req))

    fun syncWith(other: PlaytimeClock) {
        if (!clock.isPlaying || !other.isPlaying) return
        val now = System.currentTimeMillis()
        if (lastResyncAt != -1L && now - lastResyncAt < resyncCooldown.inWholeMilliseconds) return
        val drift = other.currentPlaytime - clock.currentPlaytime
        if (abs(drift) > 2.0) {
            lastResyncAt = now
            seek(other.currentPlaytime)
        }
    }

    fun tick() {
        clock.tick()

        val ctx = context ?: return

        masterPitchInterpolator.setTarget(ctx.targetPitch())
        masterVolumeInterpolator.setTarget(ctx.targetVolume())
        masterRadiusInterpolator.setTarget(ctx.targetRadius())

        masterPitchInterpolator.tick()
        masterVolumeInterpolator.tick()
        masterRadiusInterpolator.tick()

        underwaterFilter.update(this)
        reverberator.update(this)
    }

    fun close(shouldCancelTails: Boolean = false) {
        playerTerminated.set(true)
        intents.trySend(PlayerIntent.Shutdown(shouldCancelTails))
        intents.close()
    }
}
