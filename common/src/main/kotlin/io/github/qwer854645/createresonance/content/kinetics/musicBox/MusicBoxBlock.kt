package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.simibubi.create.AllItems
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.foundation.block.IBE
import com.simibubi.create.foundation.item.ItemHelper
import io.github.qwer854645.createresonance.foundation.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.phys.BlockHitResult

enum class MusicBoxKind {
    KINETIC,
    /** Dedicated conductor podium — always leads a band frequency. */
    CONDUCTOR,
}

open class MusicBoxBlock(
    properties: Properties,
    val kind: MusicBoxKind,
) : DirectionalKineticBlock(properties),
    IBE<MusicBoxBlockEntity> {
    companion object {
        /**
         * Horizontal front used while the kinetic [FACING] is vertical (shaft from above/below).
         * Must not reuse [BlockStateProperties.HORIZONTAL_FACING] — that property is also named
         * `"facing"` and collides with [DirectionalKineticBlock.FACING].
         */
        val FRONT: DirectionProperty = DirectionProperty.create("front", Direction.Plane.HORIZONTAL)
    }

    val isConductor: Boolean get() = kind == MusicBoxKind.CONDUCTOR
    val hasAdvancedControls: Boolean get() = kind == MusicBoxKind.KINETIC || kind == MusicBoxKind.CONDUCTOR

    init {
        registerDefaultState(
            defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(FRONT, Direction.SOUTH),
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(FRONT)
    }

    override fun getBlockEntityClass(): Class<MusicBoxBlockEntity> = MusicBoxBlockEntity::class.java

    override fun getBlockEntityType(): BlockEntityType<out MusicBoxBlockEntity> =
        when (kind) {
            MusicBoxKind.KINETIC -> ModBlockEntities.KINETIC_MUSIC_BOX.get()
            MusicBoxKind.CONDUCTOR -> ModBlockEntities.MUSIC_CONDUCTOR.get()
        }

    override fun hasShaftTowards(
        world: LevelReader?,
        pos: BlockPos?,
        state: BlockState,
        face: Direction?,
    ): Boolean = face == state.getValue(FACING).opposite

    override fun getRotationAxis(state: BlockState): Direction.Axis = state.getValue(FACING).axis

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val placed = super.getStateForPlacement(context) ?: return null
        // Shaft from below by default; yaw the front toward the player.
        return placed
            .setValue(FACING, Direction.UP)
            .setValue(FRONT, context.horizontalDirection.opposite)
    }

    private fun trySetFrequency(
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult,
        stack: ItemStack,
    ): Boolean {
        // Frequency clicks are handled by MusicBoxFrequencyHandler (LinkHandler-style)
        // so they cancel before the GUI opens. Keep a fallback here for non-event paths.
        val be = level.getBlockEntity(pos) as? MusicBoxBlockEntity ?: return false
        val freq = be.frequency
        for (first in listOf(false, true)) {
            if (freq.testHit(first, hit.location)) {
                if (!level.isClientSide) {
                    freq.setFrequency(first, stack)
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_FRAME_ADD_ITEM,
                        SoundSource.BLOCKS,
                        0.25f,
                        0.1f,
                    )
                }
                return true
            }
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): ItemInteractionResult {
        if (player.isShiftKeyDown || player.isSpectator) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }
        if (AllItems.WRENCH.isIn(stack) || AllItems.LINKED_CONTROLLER.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
        }
        if (trySetFrequency(level, pos, player, hit, stack)) {
            return ItemInteractionResult.SUCCESS
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
    }

    @Deprecated("Deprecated in Java")
    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult,
    ): InteractionResult {
        if (player.isShiftKeyDown || player.isSpectator) {
            return InteractionResult.PASS
        }
        // Empty-hand click on a frequency slot clears it (Create redstone-link style).
        if (trySetFrequency(level, pos, player, hit, ItemStack.EMPTY)) {
            return InteractionResult.SUCCESS
        }
        if (level.isClientSide) return InteractionResult.SUCCESS
        withBlockEntityDo(level, pos) { be ->
            player.openMenu(be, be::sendToMenu)
        }
        return InteractionResult.SUCCESS
    }

    @Deprecated("Deprecated in Java")
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        isMoving: Boolean,
    ) {
        if (state.hasBlockEntity() && (!newState.hasBlockEntity() || state.block != newState.block)) {
            withBlockEntityDo(level, pos) { be ->
                ItemHelper.dropContents(level, pos, be.inventory)
            }
        }
        super.onRemove(state, level, pos, newState, isMoving)
    }
}

class KineticMusicBoxBlock(
    properties: Properties,
) : MusicBoxBlock(properties, MusicBoxKind.KINETIC)

/** Dedicated block that conducts a band frequency and syncs MIDI to section music boxes within range (section RPM). */
class MusicConductorBlock(
    properties: Properties,
) : MusicBoxBlock(properties, MusicBoxKind.CONDUCTOR)
