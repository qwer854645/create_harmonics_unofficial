package io.github.qwer854645.createresonance.extension

import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.Property

fun BlockState.hasProperty(prop: Property<*>): Boolean =
    this.properties.contains(prop)

fun BlockState.getFacingDirection(): Direction =
    when {
        this.hasProperty(BlockStateProperties.FACING) ->
            this.getValue(BlockStateProperties.FACING)

        this.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ->
            this.getValue(BlockStateProperties.HORIZONTAL_FACING)

        else ->
            Direction.NORTH
    }

fun BlockState.getRecordSlotDirection(): Direction = getFacingDirection()

fun BlockState.getShaftFacing(): Direction = getFacingDirection().opposite
