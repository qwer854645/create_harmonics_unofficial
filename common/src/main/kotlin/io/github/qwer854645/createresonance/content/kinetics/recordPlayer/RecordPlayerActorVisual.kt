package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.contraptions.behaviour.MovementContext
import com.simibubi.create.content.contraptions.render.ActorVisual
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual
import com.simibubi.create.content.kinetics.base.RotatingInstance
import com.simibubi.create.foundation.render.AllInstanceTypes
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.flywheel.lib.model.Models
import dev.engine_room.flywheel.lib.model.baked.PartialModel
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour.Companion.Utils.isPauseModeWithRedstone
import io.github.qwer854645.createresonance.content.records.ResonanceDiscItem
import io.github.qwer854645.createresonance.foundation.behaviour.movement.getContextData
import io.github.qwer854645.createresonance.foundation.extension.lerpTo
import io.github.qwer854645.createresonance.foundation.registry.ModPartialModels
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.math.AngleHelper
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import io.github.qwer854645.createresonance.extension.getRecordSlotDirection
import io.github.qwer854645.createresonance.extension.getShaftFacing

class RecordPlayerActorVisual(
    vCtx: VisualizationContext,
    vRw: VirtualRenderWorld,
    mCtx: MovementContext,
) : ActorVisual(
        vCtx,
        vRw,
        mCtx,
    ) {
    private val discFacing = context.state.getRecordSlotDirection()
    private val axis: Direction.Axis = KineticBlockEntityVisual.rotationAxis(context.state)
    private val blockState: BlockState = context.state

    private var rotation: Double = 0.0
    private var previousRotation: Double = 0.0

    private var currentSpeed = 0.0f
    private val speedSmoothingFactor = 0.1f

    private var currentModel: PartialModel = ModPartialModels.getRecordModel()

    private var hasRecord = false

    val disc: TransformedInstance =
        instancerProvider
            .instancer(InstanceTypes.TRANSFORMED, Models.partial(currentModel))
            .createInstance()
            .apply {
                light(localBlockLight(), 0)
                setVisible(false)
                setChanged()
            }

    val shaft: RotatingInstance =
        instancerProvider
            .instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF))
            .createInstance()
            .apply {
                setRotationAxis(axis)
                setRotationOffset(KineticBlockEntityVisual.rotationOffset(blockState, axis, context.localPos))
                setPosition(context.localPos)
                rotateToFace(Direction.SOUTH, blockState.getShaftFacing())
                light(localBlockLight(), 0)
                setChanged()
            }

    private fun getRecord(): ResonanceDiscItem? {
        val contextData = context.getContextData<RecordPlayerContextData>() ?: return null
        val heldItemStack = contextData.heldItemStack
        if (heldItemStack.isEmpty || heldItemStack.item !is ResonanceDiscItem) return null
        return heldItemStack.item as ResonanceDiscItem
    }

    override fun tick() {
        val recordPresent = getRecord() != null

        if (recordPresent != hasRecord) {
            hasRecord = recordPresent
            updateRecordVisibility(recordPresent)
        }
        val speed =
            when {
                context.disabled -> 0f
                context.getAnimationSpeed() != 0f -> context.getAnimationSpeed()
                isPauseModeWithRedstone(context) -> -60f
                else -> 0f
            }

        previousRotation = rotation

        currentSpeed = currentSpeed.lerpTo(speed / 20, speedSmoothingFactor)

        val deg: Float = currentSpeed * 5

        rotation += (deg / 20).toDouble()

        rotation %= 360.0
    }

    private fun updateRecordVisibility(visible: Boolean) {
        disc.setVisible(visible)
    }

    override fun beginFrame() {
        disc
            .setIdentityTransform()
            .translate(context.localPos)
            .translate(
                discFacing.normal.x * .95f,
                discFacing.normal.y * .95f,
                discFacing.normal.z * .95f,
            ).center()
            .rotateToFace(discFacing)
            .rotateZDegrees(getRotation())
            .uncenter()
            .setChanged()
    }

    private fun getRotation(): Float = AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks().toDouble(), previousRotation, rotation)

    override fun _delete() {
        disc.delete()
        shaft.delete()
    }
}
