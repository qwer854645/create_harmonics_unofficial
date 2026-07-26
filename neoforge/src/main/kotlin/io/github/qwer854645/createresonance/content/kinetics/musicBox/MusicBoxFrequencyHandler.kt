package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.simibubi.create.AllItems
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import com.simibubi.create.foundation.utility.RaycastHelper
import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.LogicalSide
import net.neoforged.neoforge.common.util.FakePlayer
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

/**
 * Create LinkHandler-style intercept so frequency slots win over opening the GUI.
 *
 * Runs after Create's [com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler]
 * (LOW) so the Solo/Section value box is not stolen by frequency hit-tests.
 */
@EventBusSubscriber(modid = MOD_ID)
object MusicBoxFrequencyHandler {
    @JvmStatic
    @SubscribeEvent(priority = EventPriority.LOW)
    fun onBlockActivated(event: PlayerInteractEvent.RightClickBlock) {
        val world = event.level
        val pos = event.pos
        val player = event.entity
        val hand = event.hand

        if (player.isShiftKeyDown || player.isSpectator) return
        if (event.isCanceled) return

        val behaviour =
            BlockEntityBehaviour.get(world, pos, MusicBoxFrequencyBehaviour.TYPE) ?: return

        val heldItem = player.getItemInHand(hand)
        if (AllItems.LINKED_CONTROLLER.isIn(heldItem)) return
        if (AllItems.WRENCH.isIn(heldItem)) return

        val ray = RaycastHelper.rayTraceRange(world, player, 10.0) ?: return
        if (ray.blockPos != pos) return

        // Never claim clicks on the ensemble-mode scroll box.
        val be = world.getBlockEntity(pos) as? MusicBoxBlockEntity
        val mode = be?.ensembleMode
        if (mode != null && mode.isActive) {
            val slot = mode.slotPositioning
            if (slot is ValueBoxTransform.Sided) {
                slot.fromSide(ray.direction)
            }
            if (mode.testHit(ray.location)) return
        }

        var fakePlayerChoice = false
        if (player is FakePlayer) {
            val blockState = world.getBlockState(pos)
            val localHit =
                ray.location
                    .subtract(Vec3.atLowerCornerOf(pos))
                    .add(Vec3.atLowerCornerOf(ray.direction.normal).scale(0.25))
            fakePlayerChoice =
                localHit.distanceToSqr(behaviour.firstSlot.getLocalOffset(world, pos, blockState)) >
                    localHit.distanceToSqr(behaviour.secondSlot.getLocalOffset(world, pos, blockState))
        }

        for (first in listOf(false, true)) {
            if (behaviour.testHit(first, ray.location) || (player is FakePlayer && fakePlayerChoice == first)) {
                if (event.side != LogicalSide.CLIENT) {
                    behaviour.setFrequency(first, heldItem)
                    world.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_FRAME_ADD_ITEM,
                        SoundSource.BLOCKS,
                        0.25f,
                        0.1f,
                    )
                }
                event.isCanceled = true
                event.cancellationResult = InteractionResult.SUCCESS
                return
            }
        }
    }
}
