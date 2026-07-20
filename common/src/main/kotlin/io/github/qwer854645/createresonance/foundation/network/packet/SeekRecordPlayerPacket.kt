package io.github.qwer854645.createresonance.foundation.network.packet

import com.simibubi.create.foundation.utility.AdventureUtil
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBlockEntity
import net.minecraft.core.BlockPos

@Serializable
class SeekRecordPlayerPacket(
    @Contextual val blockPos: BlockPos,
    val positionSeconds: Double,
    val restart: Boolean = false,
) : ModPacket,
    C2SPacket {
    override fun handle(context: ModPacket.Context): Boolean {
        val sender = context.sender ?: return false
        if (sender.isSpectator || AdventureUtil.isAdventure(sender)) return false
        val world = sender.level() ?: return false
        if (!world.isLoaded(blockPos) || !sender.canInteractWithBlock(blockPos, 20.0)) return false

        val blockEntity = world.getBlockEntity(blockPos) as? RecordPlayerBlockEntity ?: return false
        if (restart) {
            blockEntity.playerBehaviour.restartFromBeginning()
        } else {
            blockEntity.playerBehaviour.seekTo(positionSeconds.coerceAtLeast(0.0))
        }
        blockEntity.setChanged()
        return true
    }
}
