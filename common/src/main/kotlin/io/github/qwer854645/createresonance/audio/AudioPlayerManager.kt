package io.github.qwer854645.createresonance.audio

import kotlinx.coroutines.launch
import io.github.qwer854645.createresonance.audio.effect.EffectChain
import io.github.qwer854645.createresonance.audio.player.AudioPlayer
import io.github.qwer854645.createresonance.audio.player.SoundInstanceFactory
import io.github.qwer854645.createresonance.foundation.async.ModCoroutineScope
import io.github.qwer854645.createresonance.foundation.err
import io.github.qwer854645.createresonance.foundation.eventbus.CommonEvents
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.ProxyEvent
import io.github.qwer854645.createresonance.foundation.info
import java.util.concurrent.ConcurrentHashMap

object AudioPlayerManager {
    private val players = ConcurrentHashMap<String, AudioPlayer>()

    fun getOrCreate(
        id: String,
        provider: SoundInstanceFactory,
        effectChainConfiguration: EffectChain.(player: AudioPlayer) -> Unit,
    ): AudioPlayer {
        require(id.isNotBlank()) { "Player ID cannot be blank" }

        val player =
            players.computeIfAbsent(id) { key ->
                AudioPlayer(
                    playerId = key,
                    soundInstanceFactory = provider,
                ).also { newPlayer ->
                    newPlayer.effectChain.effectChainConfiguration(newPlayer)
                }
            }

        // Ensure state machine is running in case it was a re-retrieved existing player
        // that somehow had its state machine cancelled, though usually not expected unless stopped.
        player.startStateMachine()

        return player
    }

    fun get(id: String): AudioPlayer? = players[id]

    fun release(
        id: String,
        cancelTail: Boolean = false,
    ) {
        players.remove(id)?.close(cancelTail)
    }

    fun closeAll(cancelTails: Boolean = false) {
        val snapshot = players.values.toList().also { players.clear() }
        snapshot.forEach { player ->
            runCatching { player.close(cancelTails) }
                .onFailure { "Error disposing ${player.playerId}: ${it.message}".err() }
        }
    }


    fun exists(id: String): Boolean = players.containsKey(id)
}
