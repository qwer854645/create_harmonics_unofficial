package io.github.qwer854645.createresonance.content.midi

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseRenderer
import io.github.qwer854645.createresonance.foundation.registry.ModPartialModels
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.render.CachedBuffers
import net.createmod.catnip.render.SuperByteBuffer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.util.Mth
import kotlin.math.abs
import kotlin.math.sin

class MidiTableRenderer(
    context: BlockEntityRendererProvider.Context,
) : SafeBlockEntityRenderer<MidiTableBlockEntity>() {
    override fun renderSafe(
        be: MidiTableBlockEntity,
        partialTicks: Float,
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        val state = be.blockState
        val shaftFacing = state.getValue(DirectionalKineticBlock.FACING).opposite
        val vb = buffer.getBuffer(RenderType.cutoutMipped())
        val shaft =
            CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF,
                state,
                shaftFacing,
            )
        val shaftLight =
            be.level?.let { LevelRenderer.getLightColor(it, be.blockPos.relative(shaftFacing)) } ?: light
        KineticBlockEntityRenderer.standardKineticRotationTransform(shaft, be, shaftLight).renderInto(ms, vb)

        renderScorePad(be, ms, buffer, light)
        renderStylus(be, partialTicks, ms, buffer, light)
        RecordPressBaseRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.behaviour)
    }

    /**
     * Green pad lights whenever the table is kinetically powered — independent of score /
     * configured MIDI (user request: working state alone).
     */
    private fun renderScorePad(
        be: MidiTableBlockEntity,
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
    ) {
        val working = abs(be.speed) >= 1f
        var pad = CachedBuffers.partial(ModPartialModels.MIDI_TABLE_SCORE_PAD, be.blockState)
        if (working) {
            pad =
                pad
                    .light<SuperByteBuffer>(LightTexture.pack(15, 15))
                    .disableDiffuse<SuperByteBuffer>()
                    .color<SuperByteBuffer>(180, 255, 190, 255)
            pad.renderInto(ms, buffer.getBuffer(RenderType.translucent()))
        } else {
            pad.light<SuperByteBuffer>(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()))
        }
    }

    private fun renderStylus(
        be: MidiTableBlockEntity,
        partialTicks: Float,
        ms: PoseStack,
        buffer: MultiBufferSource,
        light: Int,
    ) {
        val vb = buffer.getBuffer(RenderType.solid())
        val stylus = CachedBuffers.partial(ModPartialModels.MIDI_TABLE_STYLUS, be.blockState)

        // Hinge at the front of the brass pedestal (block-space).
        val pivotX = 8.0 / 16.0
        val pivotY = 14.05 / 16.0
        val pivotZ = 11.35 / 16.0

        val yawDegrees =
            if (be.behaviour.imprinting || shouldClientPredictImprint(be)) {
                val time = AnimationTickHolder.getRenderTime(be.level) + partialTicks
                val rpmFactor = (abs(be.speed) / 32f).coerceIn(0.4f, 2.2f)
                // Envelope: amplitude breathes between small and large swings.
                val envelope = (sin(time * 0.12 * rpmFactor) + 1.0) * 0.5
                val amplitude = Mth.lerp(envelope.toFloat(), MIN_SWING_DEG, MAX_SWING_DEG)
                // Horizontal scribble across the pad (Y axis).
                (sin(time * 0.65 * rpmFactor) * amplitude).toFloat()
            } else {
                0f
            }

        ms.pushPose()
        ms.translate(pivotX, pivotY, pivotZ)
        ms.mulPose(Axis.YP.rotationDegrees(yawDegrees))
        ms.translate(-pivotX, -pivotY, -pivotZ)
        stylus.light<SuperByteBuffer>(light).renderInto(ms, vb)
        ms.popPose()
    }

    /** Smooth client motion if sync packet lags a few ticks behind. */
    private fun shouldClientPredictImprint(be: MidiTableBlockEntity): Boolean {
        if (abs(be.speed) < 1f || !be.hasConfiguredMidi()) return false
        val held = be.behaviour.heldItem ?: return false
        if (!MidiRollUtilities.isMidiRoll(held.stack) || MidiRollUtilities.hasMidi(held.stack)) return false
        return 0.5f - held.beltPosition <= 1 / 16f
    }

    companion object {
        private const val MIN_SWING_DEG = 4f
        private const val MAX_SWING_DEG = 28f
    }
}
