package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import com.simibubi.create.content.contraptions.ControlledContraptionEntity
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import com.simibubi.create.content.contraptions.render.ActorVisual
import com.simibubi.create.content.contraptions.render.ContraptionMatrices
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld
import com.simibubi.create.infrastructure.config.AllConfigs
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.api.visualization.VisualizationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import io.github.qwer854645.createresonance.audio.AudioPlayerManager
import io.github.qwer854645.createresonance.audio.effect.AudioEffect
import io.github.qwer854645.createresonance.audio.effect.PitchShiftEffect
import io.github.qwer854645.createresonance.audio.instance.StreamingSoundInstance
import io.github.qwer854645.createresonance.audio.player.*
import io.github.qwer854645.createresonance.config.ModConfigs
import io.github.qwer854645.createresonance.config.ServerConfig
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour.Companion.Suppliers.pitchSupplierFactory
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour.Companion.Suppliers.radiusSupplierFactory
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour.Companion.Suppliers.volumeSupplierFactory
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour.Companion.Utils.isPauseModeWithRedstone
import io.github.qwer854645.createresonance.content.records.RecordUtilities
import io.github.qwer854645.createresonance.content.records.RecordUtilities.playFromRecord
import io.github.qwer854645.createresonance.foundation.async.every
import io.github.qwer854645.createresonance.foundation.async.thenLaunch
import io.github.qwer854645.createresonance.foundation.behaviour.movement.SmartMovementBehaviour
import io.github.qwer854645.createresonance.foundation.behaviour.movement.Stainable
import io.github.qwer854645.createresonance.foundation.behaviour.movement.StopAwareData
import io.github.qwer854645.createresonance.foundation.extension.*
import io.github.qwer854645.createresonance.foundation.network.packet.AudioPlayerContextStopPacket
import io.github.qwer854645.createresonance.foundation.registry.ModPackets
import io.github.qwer854645.createresonance.foundation.signals.SignalBox
import io.github.qwer854645.createresonance.foundation.supplier.values.FloatSupplier
import net.createmod.catnip.nbt.NBTHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.ShriekParticleOption
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import io.github.qwer854645.createresonance.extension.getRecordSlotDirection
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.items.ItemHandlerHelper
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper
import kotlin.math.abs
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

data class RecordPlayerContextData(
    val pitchSupplier: FloatSupplier,
    val volumeSupplier: FloatSupplier,
    val radiusSupplier: FloatSupplier,
    var particleJob: Job? = null,
    var movementPlayerInitialized: Boolean = false,
    var heldItemStack: ItemStack = ItemStack.EMPTY,
    val playtimeClock: PlaytimeClock,
    var playbackState: PlaybackState = PlaybackState.STOPPED,
    var gracefulStopJob: Job? = null,
    var ticksSinceLastClockSave: Int = 0,
    override var isDirty: Boolean = false,
    override var stopMovingCalled: Boolean = false,
) : Stainable,
    StopAwareData

object GlobalRecordPlayerMovementBehaviourTracker {
    val canRestart = SignalBox<String>()
}

class RecordPlayerMovementBehaviour : SmartMovementBehaviour<RecordPlayerContextData>() {
    companion object {
        const val PLAYER_UUID_KEY = "RecordPlayerUUID"
        private const val SPEED_THRESHOLD = 0.01f

        object Suppliers {
            private fun calculateControlledContraptionPitch(context: MovementContext): Float {
                // Get min and max pitch from config
                val minPitch =
                    ModConfigs.client.minPitch
                        .get()
                        .toFloat()
                val maxPitch =
                    ModConfigs.client.maxPitch
                        .get()
                        .toFloat()

                // Use animation speed as a proxy for rotation speed
                val rotationSpeed = abs(context.animationSpeed)

                // Define speed thresholds based on actual train animation speed values
                // Observed values: min ~100, half max ~600, max ~1100
                val maxSpeed = 1200f // Slightly higher than max to leave headroom
                val relativeSpeed = (rotationSpeed / maxSpeed).coerceIn(0f, 1f)

                // Define the curve: rise from 0-25%, plateau 25-75%, rise 75-100%
                val lowerThreshold = 0.25f // Reach pitch 1.0 at 25% speed (~300 animation speed)
                val upperThreshold = 0.75f // Start rising to maxPitch at 75% speed (~900 animation speed)

                return when {
                    // Rising phase: 0-25% speed -> minPitch to 1.0
                    relativeSpeed < lowerThreshold -> {
                        val t = (relativeSpeed / lowerThreshold).coerceIn(0f, 1f)
                        minPitch + ((1.0f - minPitch) * t)
                    }

                    // Plateau phase: 25-75% speed -> stay at 1.0
                    relativeSpeed in lowerThreshold..upperThreshold -> {
                        1.0f
                    }

                    // Rising phase: 75-100% speed -> 1.0 to maxPitch
                    relativeSpeed > upperThreshold -> {
                        val t = ((relativeSpeed - upperThreshold) / (1.0f - upperThreshold)).coerceIn(0f, 1f)
                        1.0f + ((maxPitch - 1.0f) * t)
                    }

                    else -> {
                        1.0f
                    }
                }
            }

            private fun calculateDefaultPitch(animationSpeed: Float): Float {
                // Get min and max pitch from config
                val minPitch =
                    ModConfigs.client.minPitch
                        .get()
                        .toFloat()
                val maxPitch =
                    ModConfigs.client.maxPitch
                        .get()
                        .toFloat()

                val speed = abs(animationSpeed)

                // Define speed thresholds based on actual animation speed values
                // Observed values for trains: min ~100, half max ~600, max ~1100
                val maxSpeed = 1200f // Slightly higher than max to leave headroom
                val relativeSpeed = (speed / maxSpeed).coerceIn(0f, 1f)

                // Define the curve: rise from 0-25%, plateau 25-75%, rise 75-100%
                val lowerThreshold = 0.25f // Reach pitch 1.0 at 25% speed (~300 animation speed)
                val upperThreshold = 0.65f // Start rising to maxPitch at 75% speed (~900 animation speed)

                return when {
                    // Rising phase: 0-25% speed -> minPitch to 1.0
                    relativeSpeed < lowerThreshold -> {
                        val t = (relativeSpeed / lowerThreshold).coerceIn(0f, 1f)
                        minPitch + ((1.0f - minPitch) * t)
                    }

                    // Plateau phase: 25-75% speed -> stay at 1.0
                    relativeSpeed in lowerThreshold..upperThreshold -> {
                        1.0f
                    }

                    // Rising phase: 75-100% speed -> 1.0 to maxPitch
                    relativeSpeed > upperThreshold -> {
                        val t = ((relativeSpeed - upperThreshold) / (1.0f - upperThreshold)).coerceIn(0f, 1f)
                        1.0f + ((maxPitch - 1.0f) * t)
                    }

                    else -> {
                        1.0f
                    }
                }
            }

            fun pitchSupplierFactory(context: MovementContext): FloatSupplier {
                val playbackMode =
                    RecordPlayerBlockEntity.PlaybackMode.entries[context.blockEntityData.getInt("ScrollValue")]

                if (playbackMode == RecordPlayerBlockEntity.PlaybackMode.PLAY_STATIC_PITCH ||
                    playbackMode == RecordPlayerBlockEntity.PlaybackMode.PAUSE_STATIC_PITCH
                ) {
                    return FloatSupplier { 1.0f }
                }

                return FloatSupplier {
                    when (context.contraption.entity) {
                        is ControlledContraptionEntity -> {
                            calculateControlledContraptionPitch(context)
                        }

                        else -> {
                            // Fallback to animation speed for other contraption types
                            calculateDefaultPitch(context.animationSpeed)
                        }
                    }
                }
            }

            fun volumeSupplierFactory(context: MovementContext): FloatSupplier {
                val redstonePower = context.blockEntityData.getInt("RedstonePower")
                return FloatSupplier {
                    if (redstonePower <= 0) return@FloatSupplier RecordPlayerBehaviour.BASE_VOLUME
                    redstonePower.toFloat().remapTo(1f, 15f, 0.15f, RecordPlayerBehaviour.BASE_VOLUME)
                }
            }

            fun radiusSupplierFactory(context: MovementContext): FloatSupplier {
                val redstonePower = context.blockEntityData.getInt("RedstonePower")
                return FloatSupplier {
                    if (redstonePower <= 0) return@FloatSupplier 16f
                    redstonePower.remapTo(0, 15, 4, ServerConfig.maxJukeboxSoundRange.get()).toFloat()
                }
            }
        }

        object Utils {
            fun isPauseModeWithRedstone(context: MovementContext): Boolean {
                val playbackMode =
                    RecordPlayerBlockEntity.PlaybackMode.entries[context.blockEntityData.getInt("ScrollValue")]
                val isPauseMode =
                    playbackMode == RecordPlayerBlockEntity.PlaybackMode.PAUSE ||
                        playbackMode == RecordPlayerBlockEntity.PlaybackMode.PAUSE_STATIC_PITCH

                val isPowered = context.blockEntityData.getInt("RedstonePower") > 0
                return isPauseMode && isPowered
            }
        }
    }

    override fun contextDataFactory(context: MovementContext): RecordPlayerContextData =
        RecordPlayerContextData(
            pitchSupplier = pitchSupplierFactory(context),
            volumeSupplier = volumeSupplierFactory(context),
            radiusSupplier = radiusSupplierFactory(context),
            playbackState =
                if (context.blockEntityData.contains("PlaybackState")) {
                    val state = NBTHelper.readEnum(context.blockEntityData, "PlaybackState", PlaybackState::class.java)
                    state
                } else {
                    PlaybackState.STOPPED
                },
            playtimeClock = PlaytimeClock(context.blockEntityData),
        ).apply {
            context.world.onClient { level, virtual ->
                this.particleJob =
                    10.ticks().every {
                        if (!level.isClientSide) return@every
                        if (Minecraft.getInstance().isPaused) return@every
                        val playerUID = getPlayerUUID(context)
                        val audioPlayer = AudioPlayerManager.get(playerUID) ?: return@every
                        val displacement = Random.nextInt(4) / 24f

                        val position = context.position ?: return@every
                        val entity = context.contraption?.entity ?: return@every
                        if (this@apply.playbackState != PlaybackState.PLAYING) {
                            return@every
                        }

                        if (!entity.isAlive) {
                            cancel()
                            return@every
                        }

                        val facing = context.state.getRecordSlotDirection()
                        val localDirection =
                            Vec3(facing.stepX.toDouble(), facing.stepY.toDouble(), facing.stepZ.toDouble())
                        val worldDirection = context.rotation.apply(localDirection).normalize()
                        val velocity = entity.deltaMovement

                        val spawnPos =
                            position
                                .add(worldDirection.scale(0.7 + displacement))
                                .add(velocity)

                        when (audioPlayer.state.value) {
                            PlayerState.LOADING -> {
                                context.world.addParticle(
                                    ShriekParticleOption(2),
                                    false,
                                    spawnPos.x,
                                    spawnPos.y,
                                    spawnPos.z,
                                    0.0,
                                    12.5,
                                    0.0,
                                )
                            }

                            PlayerState.PLAYING -> {
                                context.world.addParticle(
                                    ParticleTypes.NOTE,
                                    spawnPos.x + velocity.x,
                                    spawnPos.y + velocity.y,
                                    spawnPos.z + velocity.z,
                                    worldDirection.x * 0.5,
                                    worldDirection.y * 0.5,
                                    worldDirection.z * 0.5,
                                )
                            }

                            else -> {}
                        }
                    }
            }
        }

    override fun onStopTracking(blockEntityData: CompoundTag) {
        if (!blockEntityData.contains(PLAYER_UUID_KEY)) return
        val playerId = blockEntityData.getUUID(PLAYER_UUID_KEY).toString()
        ModPackets.broadcast(AudioPlayerContextStopPacket(playerId))
    }

    override fun write(
        target: CompoundTag,
        contextData: RecordPlayerContextData,
        context: MovementContext,
        syncType: SyncType,
        provider: HolderLookup.Provider,
    ) {
        target.apply {
            putClock(contextData.playtimeClock)
            put("HeldRecordItem", contextData.heldItemStack.toNBT(provider))
            if (syncType != SyncType.DISK) {
                // PlayState syncs only over network
                writeEnum("PlaybackStateMoving", contextData.playbackState)
            }
        }
    }

    override fun read(
        context: MovementContext,
        from: CompoundTag,
        target: RecordPlayerContextData,
        syncType: SyncType,
        provider: HolderLookup.Provider,
    ) {
        target.apply {
            if (from.contains("ClockOffset")) {
                from.updateClock(this.playtimeClock)
            }

            if (from.contains("HeldRecordItem")) {
                heldItemStack = ItemStack.parseOptional(provider, from.getCompound("HeldRecordItem"))
            }

            if (syncType != SyncType.DISK && from.contains("PlaybackStateMoving")) {
                playbackState = from.readEnum("PlaybackStateMoving")
            }
        }
    }

    override fun mustTickWhileDisabled(): Boolean = true

    override fun disableBlockEntityRendering(): Boolean = true

    override fun createVisual(
        visualizationContext: VisualizationContext,
        simulationWorld: VirtualRenderWorld,
        movementContext: MovementContext,
    ): ActorVisual = RecordPlayerActorVisual(visualizationContext, simulationWorld, movementContext)

    override fun renderInContraption(
        context: MovementContext,
        renderWorld: VirtualRenderWorld,
        matrices: ContraptionMatrices,
        buffer: MultiBufferSource,
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) return
        RecordPlayerRenderer.renderInContraption(context, renderWorld, matrices, buffer)
    }

    override fun tick(context: MovementContext) {
        context.world.onServer {
            val data = getContextData(context)
            val currentRecord = getRecordItem(context)
            if (currentRecord != data.heldItemStack) {
                data.heldItemStack = currentRecord
                data.markDirty()
            }
            data.playtimeClock.tick()

            val contraptionEntity = context.contraption?.entity
            val isDisassembled = contraptionEntity == null || !contraptionEntity.isAlive

            val newState: PlaybackState =
                if (isDisassembled || currentRecord == ItemStack.EMPTY) {
                    PlaybackState.STOPPED
                } else if (!context.disabled &&
                    (
                        abs(context.animationSpeed) >= SPEED_THRESHOLD ||
                            context.stall ||
                            isPauseModeWithRedstone(context)
                    )
                ) {
                    PlaybackState.PLAYING
                } else {
                    PlaybackState.PAUSED
                }

            if (GlobalRecordPlayerMovementBehaviourTracker.canRestart.consume(getPlayerUUID(context)) &&
                newState == PlaybackState.PLAYING
            ) {
                data.playtimeClock.stop()
                data.playbackState = PlaybackState.STOPPED
                resyncData(context, true)
                return@onServer
            }

            if (data.playbackState != newState) {
                if (newState == PlaybackState.PLAYING) {
                    data.playbackState = newState
                }
            }

            when (newState) {
                PlaybackState.PLAYING -> {
                    data.gracefulStopJob?.cancel()
                    data.gracefulStopJob = null
                    if (!data.playtimeClock.isPlaying) {
                        data.playtimeClock.play()
                        data.markDirty()
                    }
                }

                PlaybackState.PAUSED -> {
                    // Only schedule if not already pending and not already paused
                    if (data.gracefulStopJob == null && data.playbackState != PlaybackState.PAUSED) {
                        data.gracefulStopJob =
                            1.seconds.thenLaunch {
                                data.playtimeClock.pause()
                                data.playbackState = PlaybackState.PAUSED
                                resyncData(context, false)
                                data.gracefulStopJob = null
                            }
                    }
                }

                PlaybackState.STOPPED -> {
                    if (data.gracefulStopJob == null && data.playbackState != PlaybackState.STOPPED) {
                        if (data.heldItemStack.isEmpty) {
                            // No record — stop immediately, no grace period needed
                            data.playtimeClock.stop()
                            data.playbackState = PlaybackState.STOPPED
                            data.markDirty()
                        } else {
                            data.gracefulStopJob =
                                1.seconds.thenLaunch {
                                    data.playtimeClock.stop()
                                    data.playbackState = PlaybackState.STOPPED
                                    resyncData(context, false)
                                    data.gracefulStopJob = null
                                }
                        }
                    }
                }
            }

            data.ticksSinceLastClockSave++
            if (data.ticksSinceLastClockSave >= 100) {
                data.ticksSinceLastClockSave = 0
                data.markDirty()
            }

            resyncData(context, true)
        }

        context.world.onClient { _, _ ->
            val data = getContextData(context)
            val player = getAudioPlayer(context)

            player.tick()

            if (!data.isDirty) return
            data.clean()

            player.syncWith(data.playtimeClock)

            val newState = data.playbackState
            when (newState) {
                PlaybackState.PLAYING -> {
                    if (player.state.value == PlayerState.PLAYING) return

                    when (player.state.value) {
                        PlayerState.PAUSED -> {
                            player.play()
                        }

                        PlayerState.STOPPED,
                        PlayerState.TAILING,
                        -> {
                            val record = getRecordItem(context)
                            player.playFromRecord(
                                record,
                                data.playtimeClock.currentPlaytime,
                                context.world,
                            )
                        }

                        else -> {}
                    }
                }

                PlaybackState.PAUSED -> {
                    if (player.state.value == PlayerState.PAUSED) {
                        return
                    }

                    player.pause()
                }

                PlaybackState.STOPPED -> {
                    if (player.state.value == PlayerState.STOPPED || player.state.value == PlayerState.TAILING) {
                        return
                    }
                    player.stop()
                }
            }
        }
    }

    override fun stopMoving(context: MovementContext) {
        context.world.onServer {
            val data = getContextData(context)
            data.stopMovingCalled = true
        }
    }

    fun getAudioPlayer(context: MovementContext): AudioPlayer {
        val playerId = getPlayerUUID(context)
        val data = getContextData(context)

        if (!data.movementPlayerInitialized) {
            data.movementPlayerInitialized = true
        }

        val player =
            AudioPlayerManager.getOrCreate(
                playerId,
                provider = { streamId, stream ->
                    StreamingSoundInstance.simpleFactory(
                        this,
                        stream,
                        streamId,
                        SoundEvents.EMPTY,
                    )
                },
                effectChainConfiguration = { player ->
                    val effects = this.getEffects()
                    if (effects.none { it is PitchShiftEffect }) {
                        this.addEffectAt(
                            0,
                            PitchShiftEffect(player.masterPitchInterpolator, scope = AudioEffect.Scope.MACHINE_CONTROLLED_PITCH),
                        )
                    }
                },
            )

        if (player.contextKey !== context) {
            player.contextKey = context
            player.context =
                ContraptionAudioContext(
                    context,
                    data.volumeSupplier,
                    data.pitchSupplier,
                    data.radiusSupplier,
                )
        }

        return player
    }

    fun getPlayerUUID(context: MovementContext): String {
        require(
            context.blockEntityData.contains(PLAYER_UUID_KEY),
        ) {
            "Player UUID not found, something is very wrong here! Try replacing the record player block on the contraption ${context.localPos}"
        }
        return context.blockEntityData.getUUID(PLAYER_UUID_KEY).toString()
    }

    private fun getRecordItem(context: MovementContext): ItemStack {
        if (context.world.isClientSide) {
            val contextData = getContextData(context)
            return contextData.heldItemStack
        }
        val handler =
            context.contraption.storage.allItemStorages[context.localPos] as? RecordPlayerMountedStorage
                ?: return ItemStack.EMPTY
        return handler.getRecord()
    }
}
