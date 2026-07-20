package io.github.qwer854645.createresonance.audio.player

import io.github.qwer854645.createresonance.audio.info.AudioInfo
import io.github.qwer854645.createresonance.audio.stream.AudioEffectInputStream
import net.minecraft.client.resources.sounds.SoundInstance

sealed interface PlayerIntent {
    data class Play(
        val initialPosition: Double,
    ) : PlayerIntent

    data object Pause : PlayerIntent

    data object Stop : PlayerIntent

    data object AudioHanged : PlayerIntent

    data object AudioFinished : PlayerIntent

    data class Shutdown(
        val cancelTrail: Boolean,
    ) : PlayerIntent

    data class Seek(
        val position: Double,
    ) : PlayerIntent

    data class NewRequest(
        val req: AudioRequest,
    ) : PlayerIntent

    object TailFinished : PlayerIntent

    data class StreamReady(
        val stream: AudioEffectInputStream,
        val soundInstance: SoundInstance,
        val audioInfo: AudioInfo,
        val atPos: Double,
        val streamGeneration: Int,
    ) : PlayerIntent

    data class StreamFailed(
        val shouldDisableSeek: Boolean = false,
        val shouldRetry: Boolean = false,
    ) : PlayerIntent
}
