package io.github.qwer854645.createresonance.foundation.network.packet

import kotlinx.serialization.Serializable
import io.github.qwer854645.createresonance.audio.AudioPlayerManager

@Serializable
class AudioPlayerContextStopPacket(
    val audioPlayerId: String,
) : ModPacket,
    S2CPacket {
    override fun handle(context: ModPacket.Context): Boolean {
        AudioPlayerManager.release(audioPlayerId)
        return true
    }
}
