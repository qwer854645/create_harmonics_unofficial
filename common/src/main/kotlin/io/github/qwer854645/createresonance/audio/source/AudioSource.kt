package io.github.qwer854645.createresonance.audio.source

import io.github.qwer854645.createresonance.audio.info.AudioInfo

/**
 * Interface representing an audio source that can provide raw audio data.
 * Implementations can include YouTube, local files, HTTP streams, etc.
 */
sealed interface AudioSource {
    suspend fun resolveAudioInfo(): AudioInfo
}
