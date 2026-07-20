package io.github.qwer854645.createresonance.foundation.registry

import io.github.qwer854645.createresonance.audio.AudioPlayerManager
import io.github.qwer854645.createresonance.audio.process.ProcessLifecycleManager
import io.github.qwer854645.createresonance.foundation.async.ClientCoroutineScope
import io.github.qwer854645.createresonance.foundation.eventbus.ClientEvents
import io.github.qwer854645.createresonance.foundation.eventbus.CommonEvents
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.LogicalSide
import io.github.qwer854645.createresonance.foundation.services.PlatformService

/**
 * Client-only lifecycle. Loaded only when [ModLifecycleHandlers] detects a client environment,
 * so dedicated servers never initialize SoftSynth / Minecraft client classes through this path.
 */
@RegistrableEnv(PlatformService.Environment.CLIENT)
object ModClientLifecycleHandlers {
    fun register() {
        EventBus.onSync<ClientEvents.ClientDisconnectedEvent> { _ ->
            AudioPlayerManager.closeAll(true)
        }

        EventBus.on<ClientEvents.ClientDisconnectedEvent> { _ ->
            ProcessLifecycleManager.shutdownAll()
            ClientCoroutineScope.reset()
        }

        EventBus.onSync<CommonEvents.LevelUnloadEvent> { event ->
            if (event.side == LogicalSide.CLIENT) {
                AudioPlayerManager.closeAll(true)
                ClientCoroutineScope.reset()
            }
        }

        EventBus.onSync<CommonEvents.GameShuttingDownEvent> { event ->
            if (event.side == LogicalSide.CLIENT) {
                AudioPlayerManager.closeAll(true)
                ProcessLifecycleManager.shutdownAll()
                ClientCoroutineScope.reset()
            }
        }
    }
}
