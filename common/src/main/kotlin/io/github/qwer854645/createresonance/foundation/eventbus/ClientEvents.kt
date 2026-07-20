package io.github.qwer854645.createresonance.foundation.eventbus

/**
 * Client-only proxy events. Field types intentionally avoid net.minecraft.client.* so that
 * walking [ClientProxyEvent] sealed leaves on a dedicated server does not crash classloading.
 * Call sites cast as needed on the client.
 */
object ClientEvents {
    data class ClientDisconnectedEvent(
        val controller: Any?,
        val localPlayer: Any?,
        val networkManager: Any?,
    ) : ClientProxyEvent {
        override val side: LogicalSide = LogicalSide.CLIENT
    }

    object ScreenEvent {
        data class Init(
            val screen: Any,
            val listenerList: List<Any>,
            val addListener: (Any) -> Unit,
            val removeListener: (Any) -> Unit,
        ) : ClientProxyEvent {
            override val side: LogicalSide = LogicalSide.CLIENT
        }
    }
}
