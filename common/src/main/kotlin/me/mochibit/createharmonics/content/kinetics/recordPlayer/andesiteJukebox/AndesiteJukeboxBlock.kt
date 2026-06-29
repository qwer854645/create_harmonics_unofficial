package me.mochibit.createharmonics.content.kinetics.recordPlayer.andesiteJukebox

import me.mochibit.createharmonics.content.kinetics.recordPlayer.AllDirectionRecordPlayerBlock
import me.mochibit.createharmonics.foundation.registry.ModBlockEntities
import net.minecraft.core.Direction
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class AndesiteJukeboxBlock(
    properties: Properties,
) : AllDirectionRecordPlayerBlock(properties) {
    override fun getBlockEntityType(): BlockEntityType<out AndesiteJukeboxBlockEntity> = ModBlockEntities.ANDESITE_JUKEBOX.get()

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val placed = super.getStateForPlacement(context) ?: return null
        return placed.setValue(FACING, Direction.UP)
    }
}
