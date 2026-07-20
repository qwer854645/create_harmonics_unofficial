package io.github.qwer854645.createresonance.foundation.network.packet

import kotlinx.serialization.Serializable
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.GlobalRecordPlayerMovementBehaviourTracker
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBlockEntity
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour

@Serializable
class AudioPlayerStreamEndPacket(
    val audioPlayerId: String,
    val failure: Boolean = false,
) : ModPacket,
    C2SPacket {
    override fun handle(context: ModPacket.Context): Boolean {
        RecordPlayerBlockEntity.handlePlaybackEnd(audioPlayerId, failure)
        GlobalRecordPlayerMovementBehaviourTracker.canRestart += audioPlayerId
        return true
    }
}
