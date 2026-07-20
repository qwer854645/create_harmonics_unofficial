package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.datafixers.util.Pair
import com.simibubi.create.AllPartialModels
import com.simibubi.create.CreateClient
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer
import com.simibubi.create.foundation.utility.CreateLang
import com.simibubi.create.infrastructure.config.AllConfigs
import dev.engine_room.flywheel.lib.model.baked.PartialModel
import io.github.qwer854645.createresonance.foundation.registry.ModPartialModels
import io.github.qwer854645.createresonance.foundation.registry.ModSpriteShifts
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.data.Iterate
import net.createmod.catnip.math.VecHelper
import net.createmod.catnip.outliner.Outliner
import net.createmod.catnip.render.CachedBuffers
import net.createmod.catnip.render.SuperByteBuffer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

object MusicBoxFrequencyRenderer {
    fun tick() {
        val mc = Minecraft.getInstance()
        val target = mc.hitResult
        if (target == null || target !is BlockHitResult) return
        val world = mc.level ?: return
        val pos = target.blockPos
        val state = world.getBlockState(pos)
        val front = MusicBoxFrequencySlot.frontFace(state)
        // Only when looking at the front — same face the slots sit on.
        if (target.direction != front) return
        val behaviour =
            BlockEntityBehaviour.get(world, pos, MusicBoxFrequencyBehaviour.TYPE) ?: return

        val freq1: Component = CreateLang.translateDirect("logistics.firstFrequency")
        val freq2: Component = CreateLang.translateDirect("logistics.secondFrequency")

        for (first in Iterate.trueAndFalse) {
            val bb = AABB(Vec3.ZERO, Vec3.ZERO).inflate(0.25)
            val label = if (first) freq1 else freq2
            val hit = behaviour.testHit(first, target.location)
            val transform = if (first) behaviour.firstSlot else behaviour.secondSlot
            val box = ValueBox(label, bb, pos).passive(!hit)
            val empty = behaviour.getNetworkKey().get(first).stack.isEmpty
            if (!empty) box.wideOutline()
            Outliner.getInstance().showOutline(Pair.of(first, pos), box.transform(transform))
                .highlightFace(front)
            if (!hit) continue
            val tip = ArrayList<net.minecraft.network.chat.MutableComponent>()
            tip.add(label.copy())
            tip.add(
                CreateLang.translateDirect(
                    if (empty) "logistics.filter.click_to_set" else "logistics.filter.click_to_replace",
                ),
            )
            CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip)
        }
    }

    /** Opaque cubes report light 0 at their own pos — sample the front face neighbor. */
    fun lightForFrequencySlots(be: MusicBoxBlockEntity): Int {
        val level = be.level ?: return 0
        val front = MusicBoxFrequencySlot.frontFace(be.blockState)
        return LevelRenderer.getLightColor(level, be.blockPos.relative(front))
    }

    fun renderOnBlockEntity(
        be: MusicBoxBlockEntity,
        ms: PoseStack,
        buffer: MultiBufferSource,
        overlay: Int,
    ) {
        if (be.isRemoved) return
        val camera = Minecraft.getInstance().cameraEntity
        val max = AllConfigs.client().filterItemRenderDistance.getF()
        if (!be.isVirtual &&
            camera != null &&
            camera.position().distanceToSqr(VecHelper.getCenterOf(be.blockPos)) > (max * max).toDouble()
        ) {
            return
        }
        val behaviour = be.getBehaviour(MusicBoxFrequencyBehaviour.TYPE) ?: return
        val light = lightForFrequencySlots(be)
        for (first in Iterate.trueAndFalse) {
            val transform = if (first) behaviour.firstSlot else behaviour.secondSlot
            val stack = if (first) behaviour.frequencyFirst.stack else behaviour.frequencyLast.stack
            if (stack.isEmpty) continue
            ms.pushPose()
            transform.transform(be.level, be.blockPos, be.blockState, ms)
            ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay)
            ms.popPose()
        }
    }
}

/** Kinetic input half-shaft, two scaled horizontal interior shafts, and frequency items. */
class MusicBoxRenderer(
    context: BlockEntityRendererProvider.Context,
) : SafeBlockEntityRenderer<MusicBoxBlockEntity>() {
    override fun renderSafe(
        be: MusicBoxBlockEntity,
        partialTicks: Float,
        ms: PoseStack,
        bufferSource: MultiBufferSource,
        light: Int,
        overlay: Int,
    ) {
        val state = be.blockState
        val shaftFacing = state.getValue(DirectionalKineticBlock.FACING).opposite
        val vb = bufferSource.getBuffer(RenderType.cutoutMipped())
        val shaft =
            CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF,
                state,
                shaftFacing,
            )
        // Opaque full cubes report light 0 at their own pos — sample the shaft face neighbor.
        val shaftLight =
            be.level?.let { LevelRenderer.getLightColor(it, be.blockPos.relative(shaftFacing)) } ?: light
        KineticBlockEntityRenderer.standardKineticRotationTransform(shaft, be, shaftLight).renderInto(ms, vb)

        val interiorLight =
            be.level?.let { LevelRenderer.getLightColor(it, be.blockPos.above()) } ?: light
        val isConductor = be.behaviour.isConductorBlock
        val sectionMode = be.behaviour.role == MusicBoxRole.SECTION && !isConductor

        if (isConductor) {
            // Opaque first — translucent bulb must come last or cutout buffers end ("Not building!").
            renderMiniHorizontalShaft(be, ms, vb, interiorLight, offsetY = 3.0 / 16.0, offsetZ = -4.0 / 16.0)
            renderMiniHorizontalShaft(be, ms, vb, interiorLight, offsetY = 3.0 / 16.0, offsetZ = 4.0 / 16.0)
            renderTapeBelt(be, state, ms, vb, interiorLight)
            renderEnsembleLink(
                ModPartialModels.CONDUCTOR_ENSEMBLE_LINK,
                be,
                state,
                ms,
                bufferSource,
                interiorLight,
            )
        } else if (sectionMode) {
            renderEnsembleLink(
                ModPartialModels.MUSIC_BOX_ENSEMBLE_LINK,
                be,
                state,
                ms,
                bufferSource,
                interiorLight,
            )
        } else {
            // Two miniature horizontal shafts (east-west), front/back inside the casing.
            // y=11 is the brass wall / glass shell boundary in the block model.
            renderMiniHorizontalShaft(be, ms, vb, interiorLight, offsetY = 3.0 / 16.0, offsetZ = -4.0 / 16.0)
            renderMiniHorizontalShaft(be, ms, vb, interiorLight, offsetY = 3.0 / 16.0, offsetZ = 4.0 / 16.0)
            renderTapeBelt(be, state, ms, vb, interiorLight)
        }

        MusicBoxFrequencyRenderer.renderOnBlockEntity(be, ms, bufferSource, overlay)
    }

    private fun renderTapeBelt(
        be: MusicBoxBlockEntity,
        state: BlockState,
        ms: PoseStack,
        vb: VertexConsumer,
        interiorLight: Int,
    ) {
        if (!be.hasScore()) return
        // Scroll like Create belts; negate so UV travel matches shaft spin direction.
        val tape = CachedBuffers.partial(ModPartialModels.MUSIC_BOX_TAPE_BELT, state)
        val speed = be.speed
        if (abs(speed) > 0.01f) {
            val shift = ModSpriteShifts.MUSIC_BOX_TAPE
            val spriteSize = shift.target.v1 - shift.target.v0
            val time = AnimationTickHolder.getRenderTime(be.level)
            var scroll = -speed * time / (31.5f * 16f)
            scroll -= Mth.floor(scroll)
            scroll *= spriteSize * 0.5f
            tape.shiftUVScrolling<SuperByteBuffer>(shift, scroll)
        }
        tape.light<SuperByteBuffer>(interiorLight).renderInto(ms, vb)
    }

    private fun renderEnsembleLink(
        model: PartialModel,
        be: MusicBoxBlockEntity,
        state: BlockState,
        ms: PoseStack,
        bufferSource: MultiBufferSource,
        interiorLight: Int,
    ) {
        val link = CachedBuffers.partial(model, state)
        val playing = be.behaviour.playing
        val linked = be.behaviour.midiBytes != null
        val linkLight =
            when {
                playing -> LightTexture.pack(15, 15)
                linked -> interiorLight
                else ->
                    LightTexture.pack(
                        (LightTexture.block(interiorLight) * 0.35f).toInt().coerceIn(0, 15),
                        (LightTexture.sky(interiorLight) * 0.35f).toInt().coerceIn(0, 15),
                    )
            }
        var buf = link.light<SuperByteBuffer>(linkLight)
        if (playing) {
            buf = buf.disableDiffuse<SuperByteBuffer>()
            buf = buf.color<SuperByteBuffer>(200, 255, 200, 255)
        }
        buf.renderInto(ms, bufferSource.getBuffer(RenderType.translucent()))
    }

    private fun renderMiniHorizontalShaft(
        be: MusicBoxBlockEntity,
        ms: PoseStack,
        vb: VertexConsumer,
        light: Int,
        offsetY: Double,
        offsetZ: Double,
    ) {
        ms.pushPose()
        ms.translate(0.5, 0.5, 0.5)
        ms.translate(0.0, offsetY, offsetZ)
        ms.scale(INTERNAL_SHAFT_SCALE, INTERNAL_SHAFT_SCALE, INTERNAL_SHAFT_SCALE)
        ms.translate(-0.5, -0.5, -0.5)

        // Use shaft blockstate with AXIS — partialFacing(SHAFT) stays vertical and tumbles wrongly.
        val shaftState = KineticBlockEntityRenderer.shaft(Direction.Axis.X)
        val shaft =
            CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, shaftState)
        val angle =
            KineticBlockEntityRenderer.getAngleForBe(be, be.blockPos, Direction.Axis.X)
        KineticBlockEntityRenderer
            .kineticRotationTransform(shaft, be, Direction.Axis.X, angle, light)
            .renderInto(ms, vb)
        ms.popPose()
    }

    companion object {
        /** Half-size Create shaft so a pair fits in the hollow casing. */
        private const val INTERNAL_SHAFT_SCALE = 0.5f
    }
}
