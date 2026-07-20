package io.github.qwer854645.createresonance.content.kinetics.recordPlayer.kineticNetworkJukebox

import com.simibubi.create.AllItems
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.AllDirectionRecordPlayerBlock
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBlockEntity
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerClientHandler
import io.github.qwer854645.createresonance.foundation.extension.onClient
import io.github.qwer854645.createresonance.foundation.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class KineticNetworkJukeboxBlock(
    properties: Properties,
) : AllDirectionRecordPlayerBlock(properties) {
    override fun getBlockEntityType(): BlockEntityType<out KineticNetworkJukeboxBlockEntity> =
        ModBlockEntities.KINETIC_NETWORK_JUKEBOX.get()

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val placed = super.getStateForPlacement(context) ?: return null
        return placed.setValue(FACING, Direction.UP)
    }

    @Deprecated("Deprecated in Java")
    override fun useItemOn(
        stack: ItemStack,
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHand: InteractionHand,
        pHit: BlockHitResult,
    ): ItemInteractionResult {
        if (AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }

        val facing = pState.getValue(FACING)
        if (isSideSeekInteraction(pHit.direction, facing)) {
            pLevel.onClient { _, _ ->
                withBlockEntityDo(pLevel, pPos) { be: RecordPlayerBlockEntity ->
                    RecordPlayerClientHandler.openSeekScreen(be)
                }
            }
            return ItemInteractionResult.SUCCESS
        }

        return handleRecordUse(pState, pLevel, pPos, pPlayer, pHand, pHit, facing)
    }

    @Deprecated("Deprecated in Java")
    override fun useWithoutItem(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHit: BlockHitResult,
    ): InteractionResult {
        val facing = pState.getValue(FACING)
        if (isSideSeekInteraction(pHit.direction, facing)) {
            pLevel.onClient { _, _ ->
                withBlockEntityDo(pLevel, pPos) { be: RecordPlayerBlockEntity ->
                    RecordPlayerClientHandler.openSeekScreen(be)
                }
            }
            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }

    private fun isSideSeekInteraction(
        hitFace: Direction,
        facing: Direction,
    ): Boolean = hitFace != facing && hitFace != facing.opposite
}
