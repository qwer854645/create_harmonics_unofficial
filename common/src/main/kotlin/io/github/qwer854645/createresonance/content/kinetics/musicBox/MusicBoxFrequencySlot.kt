package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import dev.engine_room.flywheel.lib.transform.TransformStack
import net.createmod.catnip.math.AngleHelper
import net.createmod.catnip.math.VecHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

/**
 * Dual frequency slots on the **front** face, below the ensemble-mode scroll.
 *
 * Front = [Direction.SOUTH] when the shaft faces up/down (default placement);
 * otherwise the block's [DirectionalKineticBlock.FACING] face.
 */
class MusicBoxFrequencySlot(
    first: Boolean,
) : ValueBoxTransform.Dual(first) {
    override fun getLocalOffset(
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
    ): Vec3 {
        val front = frontFace(state)
        // South-local coords, then yaw to the front. Y sits below the mode scroll
        // (mode is biased toward the shaft tip ≈ upper half of the face).
        var o = VecHelper.voxelSpace(if (isFirst) 5.5 else 10.5, 5.0, 15.5)
        o = VecHelper.rotateCentered(o, AngleHelper.horizontalAngle(front).toDouble(), Direction.Axis.Y)
        return o
    }

    override fun rotate(
        level: LevelAccessor?,
        pos: BlockPos,
        state: BlockState,
        ms: PoseStack,
    ) {
        // Same convention as CenteredSideValueBoxTransform so outlines face outward.
        val front = frontFace(state)
        val yRot = AngleHelper.horizontalAngle(front) + 180f
        val xRot =
            when (front) {
                Direction.UP -> 90f
                Direction.DOWN -> -90f
                else -> 0f
            }
        TransformStack
            .of(ms)
            .rotateYDegrees(yRot)
            .rotateXDegrees(xRot)
    }

    override fun getScale(): Float = 0.5f

    /** Larger than Dual default (scale/3.5) so front-face clicks register reliably. */
    override fun testHit(
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        localHit: Vec3,
    ): Boolean {
        val offset = getLocalOffset(level, pos, state) ?: return false
        return localHit.distanceTo(offset) < scale / 2.0
    }

    companion object {
        /** Front face shared with the ensemble-mode value box. */
        fun frontFace(state: BlockState): Direction {
            val facing = state.getValue(DirectionalKineticBlock.FACING)
            return if (facing.axis.isVertical) Direction.SOUTH else facing
        }
    }
}
