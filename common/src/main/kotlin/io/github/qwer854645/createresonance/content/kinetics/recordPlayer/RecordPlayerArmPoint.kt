package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
import io.github.qwer854645.createresonance.content.records.ResonanceDiscItem
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.JukeboxBlock
import net.minecraft.world.level.block.state.BlockState

class RecordPlayerArmPoint(
    type: ArmInteractionPointType,
    level: Level,
    pos: BlockPos,
    state: BlockState,
) : AllArmInteractionPointTypes.JukeboxPoint(type, level, pos, state) {
    override fun insert(
        armBlockEntity: ArmBlockEntity,
        stack: ItemStack,
        simulate: Boolean,
    ): ItemStack {
        if (stack.item !is ResonanceDiscItem) return stack
        if (cachedState.getOptionalValue(RecordPlayerTrait.HAS_RESONANCE_DISC).orElse(true)) return stack
        val be =
            level.getBlockEntity(pos) as? RecordPlayerBlockEntity
                ?: return stack

        val remainder = stack.copy()
        val toInsert = remainder.split(1)
        val isPowered = be.playerBehaviour.redstonePower == 15
        if (!simulate) {
            be.playerBehaviour.apply {
                insertRecord(toInsert)
                if (!isPowered) {
                    startPlayer()
                }
            }
        }

        return remainder
    }

    override fun extract(
        armBlockEntity: ArmBlockEntity,
        slot: Int,
        amount: Int,
        simulate: Boolean,
    ): ItemStack {
        if (!cachedState.getOptionalValue(RecordPlayerTrait.HAS_RESONANCE_DISC).orElse(false)) return ItemStack.EMPTY
        val be =
            level.getBlockEntity(pos) as? RecordPlayerBlockEntity
                ?: return ItemStack.EMPTY

        val isFullyPowered = be.playerBehaviour.redstonePower == 15
        if (isFullyPowered) return ItemStack.EMPTY
        val playbackState = be.playerBehaviour.playbackState
        if (playbackState != PlaybackState.STOPPED && playbackState != PlaybackState.PAUSED) return ItemStack.EMPTY

        if (!simulate) {
            val record = be.playerBehaviour.popRecord()
            return record ?: ItemStack.EMPTY
        }

        val recordInBe = be.playerBehaviour.getRecord()
        return recordInBe
    }
}
