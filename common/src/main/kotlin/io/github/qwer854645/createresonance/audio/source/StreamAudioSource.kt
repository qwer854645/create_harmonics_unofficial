package io.github.qwer854645.createresonance.audio.source

import io.github.qwer854645.createresonance.audio.info.AudioInfo
import java.io.InputStream

class StreamAudioSource(
    val streamRetriever: () -> InputStream,
    val audioInfo: AudioInfo,
) : AudioSource {
    override suspend fun resolveAudioInfo(): AudioInfo = audioInfo
}
