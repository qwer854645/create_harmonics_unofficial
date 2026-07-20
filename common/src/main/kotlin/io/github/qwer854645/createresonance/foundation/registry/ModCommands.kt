package io.github.qwer854645.createresonance.foundation.registry

import io.github.qwer854645.createresonance.command.CommandEntry
import io.github.qwer854645.createresonance.foundation.eventbus.CommonEvents
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.ProxyEvent
import io.github.qwer854645.createresonance.foundation.info
import net.minecraft.core.Registry

object ModCommands : CommonRegistry {
    override fun register(registry: Registry<*>?) {
        "Pointing mod commands for registration".info()
        EventBus.on<CommonEvents.RegisterCommandsEvent> { event ->
            CommandEntry.registerAll(event.dispatcher, event.env, event.context)
        }
    }
}
