package io.github.qwer854645.createresonance.foundation.services

import io.github.qwer854645.createresonance.foundation.network.packet.ModPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

interface NetworkService {
    fun sendToServer(packet: ModPacket)

    fun sendToPlayer(
        player: ServerPlayer,
        packet: ModPacket,
    )

    fun sendToTrackingEntity(
        packet: ModPacket,
        entity: Entity,
    )

    fun broadcast(packet: ModPacket)
}

val networkService: NetworkService by lazy {
    loadService<NetworkService>()
}
