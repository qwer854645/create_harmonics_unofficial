package io.github.qwer854645.createresonance.foundation.registry

import io.github.qwer854645.createresonance.foundation.eventbus.PlatformEventBridge
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.services.PlatformService
import io.github.qwer854645.createresonance.foundation.services.platformService
import net.minecraft.core.Registry

object ModEventProxy : CommonRegistry {
    override fun register(registry: Registry<*>?) {
        "Registering mod event proxies...".info()
        val isClient = platformService isEnvironment PlatformService.Environment.CLIENT
        platformService.setupEventBridge()
        if (isClient) {
            platformService.setupClientEventBridge()
        }
        PlatformEventBridge.validateAll(isClient)
    }
}
