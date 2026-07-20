package io.github.qwer854645.createresonance.content.midi

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
import io.github.qwer854645.createresonance.foundation.registry.ModArmInteractionPoints
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class MidiTableArmInteractionPoint(
    type: ModArmInteractionPoints.MidiTableType,
    level: Level,
    pos: BlockPos,
    state: BlockState,
) : ArmInteractionPoint(type, level, pos, state) {
    override fun extract(
        armBlockEntity: ArmBlockEntity,
        slot: Int,
        amount: Int,
        simulate: Boolean,
    ): ItemStack {
        // Slot 0 is the held blank/in-progress roll — only pull finished output slots.
        if (slot == 0) return ItemStack.EMPTY
        return super.extract(armBlockEntity, slot, amount, simulate)
    }

    override fun getInteractionPositionVector(): Vec3 =
        Vec3.atLowerCornerOf(pos).add(.5, (14 / 16f).toDouble(), .5)
}
