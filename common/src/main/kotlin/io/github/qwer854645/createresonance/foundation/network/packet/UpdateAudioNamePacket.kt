package io.github.qwer854645.createresonance.foundation.network.packet

import kotlinx.serialization.Serializable
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBlockEntity
import io.github.qwer854645.createresonance.foundation.services.contentService

@Serializable
class UpdateAudioNamePacket(
    val audioPlayerId: String,
    val audioName: String,
    val durationSeconds: Int = -1,
) : ModPacket,
    C2SPacket {
    override fun handle(context: ModPacket.Context): Boolean {
        RecordPlayerBlockEntity.handleAudioTitleChange(audioPlayerId, audioName, durationSeconds)
        return true
    }
}
