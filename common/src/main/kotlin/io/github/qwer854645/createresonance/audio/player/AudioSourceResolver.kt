package io.github.qwer854645.createresonance.audio.player

import io.github.qwer854645.createresonance.audio.info.AudioInfo
import io.github.qwer854645.createresonance.audio.source.AudioSource
import io.github.qwer854645.createresonance.audio.source.HttpAudioSource
import io.github.qwer854645.createresonance.audio.source.StreamAudioSource

object AudioSourceResolver {
    fun resolve(request: AudioRequest): AudioSource =
        when (request) {
            is AudioRequest.Stream -> {
                StreamAudioSource(request.streamRetriever, request.streamInfo)
            }

            is AudioRequest.Url -> {
                HttpAudioSource(request.url)
            }
        }
}
