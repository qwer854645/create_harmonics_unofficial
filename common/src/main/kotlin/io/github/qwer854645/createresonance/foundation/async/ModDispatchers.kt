package io.github.qwer854645.createresonance.foundation.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import io.github.qwer854645.createresonance.foundation.err
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.ModEventHandler
import io.github.qwer854645.createresonance.foundation.eventbus.ProxyEvent
import io.github.qwer854645.createresonance.foundation.eventbus.ServerEvents
import io.github.qwer854645.createresonance.handler.CommonEventHandler
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import kotlin.coroutines.CoroutineContext

object ModDispatchers : ModEventHandler {
    private var currentServer: MinecraftServer? = null

    override fun setupEvents() {
        EventBus.on<ServerEvents.ServerStartedEvent> { event ->
            currentServer = event.server
        }
        EventBus.on<ServerEvents.ServerStoppedEvent> { event ->
            currentServer = null
        }
    }

    class Client : CoroutineDispatcher() {
        override fun dispatch(
            context: CoroutineContext,
            block: Runnable,
        ) {
            try {
                Minecraft.getInstance().execute(block)
            } catch (e: Exception) {
                "Error dispatching to Minecraft main thread".err()
            }
        }
    }

    class Server : CoroutineDispatcher() {
        override fun dispatch(
            context: CoroutineContext,
            block: Runnable,
        ) {
            try {
                currentServer?.execute(block)
            } catch (e: Exception) {
                "Error dispatching to Minecraft main thread".err()
            }
        }
    }
}
