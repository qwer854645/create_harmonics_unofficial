package io.github.qwer854645.createresonance.content.kinetics.recordPlayer.kineticNetworkJukebox

import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class KineticNetworkJukeboxBlockEntity(
    type: BlockEntityType<KineticNetworkJukeboxBlockEntity>,
    pos: BlockPos,
    state: BlockState,
) : RecordPlayerBlockEntity(type, pos, state)
