package io.github.qwer854645.createresonance.foundation.registry

import io.github.qwer854645.createresonance.foundation.async.ServerCoroutineScope
import io.github.qwer854645.createresonance.foundation.eventbus.CommonEvents
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.LogicalSide
import io.github.qwer854645.createresonance.foundation.eventbus.ServerEvents
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.services.PlatformService
import io.github.qwer854645.createresonance.foundation.services.platformService
import net.minecraft.core.Registry

/**
 * Lifecycle hooks that are safe to register on dedicated servers.
 * Client-only audio teardown is registered from [ModClientLifecycleHandlers].
 */
object ModLifecycleHandlers : CommonRegistry {
    override fun register(registry: Registry<*>?) {
        EventBus.on<ServerEvents.ServerStoppedEvent> { _ ->
            ServerCoroutineScope.reset()
        }

        EventBus.onSync<CommonEvents.GameShuttingDownEvent> { _ ->
            ServerCoroutineScope.reset()
        }

        EventBus.onSync<CommonEvents.LevelUnloadEvent> { _ ->
            // Client audio cleanup is handled by ModClientLifecycleHandlers.
        }

        if (platformService isEnvironment PlatformService.Environment.CLIENT) {
            ModClientLifecycleHandlers.register()
        }

        "Registering lifecycle handlers".info()
    }
}
