package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import io.github.qwer854645.createresonance.audio.AudioPlayerManager
import net.createmod.catnip.gui.ScreenOpener

object RecordPlayerClientHandler {
    fun openSeekScreen(be: RecordPlayerBlockEntity) {
        ScreenOpener.open(RecordPlayerSeekScreen(be))
    }

    fun resolveDurationSeconds(behaviour: RecordPlayerBehaviour): Double? {
        val cached = behaviour.cachedDurationSeconds?.toDouble()?.takeIf { it > 0 }
        val uuid = behaviour.recordPlayerUUID ?: return cached
        val player = AudioPlayerManager.get(uuid.toString()) ?: return cached
        return player.durationSeconds?.toDouble()?.takeIf { it > 0 } ?: cached
    }

    fun resolveCurrentSeconds(behaviour: RecordPlayerBehaviour): Double {
        val uuid = behaviour.recordPlayerUUID
        if (uuid != null) {
            AudioPlayerManager.get(uuid.toString())?.clock?.currentPlaytime?.let { return it }
        }
        return behaviour.playtimeClock.currentPlaytime
    }
}
