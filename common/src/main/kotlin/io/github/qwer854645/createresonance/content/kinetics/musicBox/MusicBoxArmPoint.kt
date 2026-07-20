package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
import io.github.qwer854645.createresonance.content.midi.MidiRollUtilities
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * Mechanical arm target for kinetic music boxes (solo) and the conductor podium.
 * Scores may only be extracted after the track has finished playing naturally.
 */
class MusicBoxArmPoint(
    type: ArmInteractionPointType,
    level: Level,
    pos: BlockPos,
    state: BlockState,
) : ArmInteractionPoint(type, level, pos, state) {
    override fun insert(
        armBlockEntity: ArmBlockEntity,
        stack: ItemStack,
        simulate: Boolean,
    ): ItemStack {
        if (!MidiRollUtilities.isMidiRoll(stack) || !MidiRollUtilities.hasMidi(stack)) return stack
        val be = level.getBlockEntity(pos) as? MusicBoxBlockEntity ?: return stack
        if (!be.allowsArmScoreAccess()) return stack
        if (be.hasScore()) return stack

        val remainder = stack.copy()
        val toInsert = remainder.split(1)
        if (!simulate) {
            be.insertScore(toInsert)
        }
        return remainder
    }

    override fun extract(
        armBlockEntity: ArmBlockEntity,
        slot: Int,
        amount: Int,
        simulate: Boolean,
    ): ItemStack {
        val be = level.getBlockEntity(pos) as? MusicBoxBlockEntity ?: return ItemStack.EMPTY
        if (!be.canArmExtractScore()) return ItemStack.EMPTY

        if (!simulate) {
            return be.popScore() ?: ItemStack.EMPTY
        }
        return be.getScore()
    }
}