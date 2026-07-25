package io.github.qwer854645.createresonance

import kotlinx.coroutines.runBlocking
import io.github.qwer854645.createresonance.audio.BackgroundMusicGuard
import io.github.qwer854645.createresonance.audio.bin.BinStatusManager
import io.github.qwer854645.createresonance.audio.process.ProcessLifecycleManager
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxFrequencyRenderer
import io.github.qwer854645.createresonance.foundation.async.launchOnClient
import io.github.qwer854645.createresonance.foundation.err
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.TickEvents
import io.github.qwer854645.createresonance.ponder.ModPonderPlugin
import net.createmod.ponder.foundation.PonderIndex

object CreateResonanceClientMod {
    private var initialized = false

    fun setup() {
        if (initialized) {
            return "Client side was already initialized".err()
        }
        initialized = true

        PonderIndex.addPlugin(ModPonderPlugin())

        EventBus.onMcMain<TickEvents.ClientTickEvent> { event ->
            if (event.phase == TickEvents.Phase.END) {
                MusicBoxFrequencyRenderer.tick()
                BackgroundMusicGuard.tick()
            }
        }

        launchOnClient {
            BinStatusManager.initialize()
        }

        Runtime.getRuntime().addShutdownHook(
            Thread {
                try {
                    runBlocking {
                        ProcessLifecycleManager.shutdownAll()
                    }
                } catch (e: Exception) {
                    "Error shutting down processes: ${e.message}".err()
                }
            },
        )
    }
}
