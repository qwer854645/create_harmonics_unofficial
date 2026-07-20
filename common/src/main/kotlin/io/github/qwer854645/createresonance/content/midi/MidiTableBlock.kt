package io.github.qwer854645.createresonance.content.midi

import com.simibubi.create.AllBlocks
import com.simibubi.create.AllItems
import com.simibubi.create.AllShapes
import com.simibubi.create.AllSoundEvents
import com.simibubi.create.content.equipment.wrench.IWrenchable
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.logistics.depot.SharedDepotBlockMethods
import com.simibubi.create.foundation.block.IBE
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import io.github.qwer854645.createresonance.foundation.extension.onClient
import io.github.qwer854645.createresonance.foundation.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Kinetic MIDI imprint depot: shaft from below, blank rolls in via funnel/arm/drop,
 * imprint while powered, take written rolls from top / funnel / arm.
 */
class MidiTableBlock(
    properties: Properties,
) : DirectionalKineticBlock(properties),
    IBE<MidiTableBlockEntity>,
    IWrenchable {
    companion object {
        private val RANDOM = RandomSource.create()
    }

    override fun getBlockEntityClass(): Class<MidiTableBlockEntity> = MidiTableBlockEntity::class.java

    override fun getBlockEntityType(): BlockEntityType<out MidiTableBlockEntity> = ModBlockEntities.MIDI_TABLE.get()

    override fun hasShaftTowards(
        world: LevelReader?,
        pos: BlockPos?,
        state: BlockState,
        face: Direction?,
    ): Boolean = face == state.getValue(FACING).opposite

    override fun getRotationAxis(state: BlockState): Direction.Axis = state.getValue(FACING).axis

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val placed = super.getStateForPlacement(context) ?: return null
        return placed.setValue(FACING, Direction.UP)
    }

    @Deprecated("Deprecated in Java")
    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape = AllShapes.CASING_13PX.get(Direction.UP)

    @Deprecated("Deprecated in Java")
    override fun useShapeForLightOcclusion(state: BlockState): Boolean = true

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): ItemInteractionResult {
        if (AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }

        if (hit.direction != Direction.UP) {
            level.onClient { _, _ ->
                withBlockEntityDo(level, pos) { be ->
                    MidiTableClientHandler.openConfigScreen(be)
                }
            }
            return ItemInteractionResult.SUCCESS
        }

        if (level.isClientSide) return ItemInteractionResult.SUCCESS

        val behaviour =
            BlockEntityBehaviour.get(level, pos, MidiTableBehaviour.BEHAVIOUR_TYPE)
                ?: return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        if (!behaviour.canAcceptItems()) return ItemInteractionResult.SUCCESS

        pickupBehaviourItems(behaviour, level, pos, player)

        val cantPlaceItem = AllBlocks.MECHANICAL_ARM.isIn(stack)
        if (!stack.isEmpty && !cantPlaceItem && behaviour.isItemValid(stack)) {
            val transported = TransportedItemStack(stack)
            transported.insertedFrom = player.direction
            transported.prevBeltPosition = .25f
            transported.beltPosition = .25f
            behaviour.heldItem = transported
            player.setItemInHand(hand, ItemStack.EMPTY)
            AllSoundEvents.DEPOT_SLIDE.playOnServer(level, pos)
        }

        behaviour.blockEntity.notifyUpdate()
        return ItemInteractionResult.SUCCESS
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult,
    ): InteractionResult {
        if (hit.direction != Direction.UP) {
            level.onClient { _, _ ->
                withBlockEntityDo(level, pos) { be ->
                    MidiTableClientHandler.openConfigScreen(be)
                }
            }
            return InteractionResult.SUCCESS
        }

        if (level.isClientSide) return InteractionResult.SUCCESS

        val behaviour =
            BlockEntityBehaviour.get(level, pos, MidiTableBehaviour.BEHAVIOUR_TYPE)
                ?: return InteractionResult.PASS
        if (!behaviour.canAcceptItems()) return InteractionResult.SUCCESS

        pickupBehaviourItems(behaviour, level, pos, player)
        behaviour.blockEntity.notifyUpdate()
        return InteractionResult.SUCCESS
    }

    private fun pickupBehaviourItems(
        behaviour: MidiTableBehaviour,
        level: Level,
        pos: BlockPos,
        player: Player,
    ) {
        var playedSoundOnce = false
        val mainItemStack = behaviour.heldItemStack

        if (!mainItemStack.isEmpty) {
            player.inventory.placeItemBackInInventory(mainItemStack)
            behaviour.removeHeldItem()
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f, 1f + RANDOM.nextFloat())
            playedSoundOnce = true
        }

        val outputs = behaviour.processingOutputBuffer
        for (i in 0..<outputs.slots) {
            val extracted = outputs.extractItem(i, 64, false)
            if (!extracted.isEmpty && !playedSoundOnce) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f, 1f + RANDOM.nextFloat())
            }
            player.inventory.placeItemBackInInventory(extracted)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        isMoving: Boolean,
    ) {
        IBE.onRemove(state, level, pos, newState)
    }

    override fun updateEntityAfterFallOn(
        level: BlockGetter,
        entity: Entity,
    ) {
        super.updateEntityAfterFallOn(level, entity)
        SharedDepotBlockMethods.onLanded(level, entity)
    }
}
