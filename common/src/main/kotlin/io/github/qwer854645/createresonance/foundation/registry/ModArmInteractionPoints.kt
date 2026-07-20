package io.github.qwer854645.createresonance.foundation.registry

import com.simibubi.create.api.registry.CreateRegistries
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
import com.tterrag.registrate.util.entry.RegistryEntry
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxArmPoint
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerArmPoint
import io.github.qwer854645.createresonance.content.midi.MidiTableArmInteractionPoint
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseArmInteractionPoint
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object ModArmInteractionPoints : CommonRegistry {
    override val registrationOrder = 3

    val RECORD_PLAYER_TYPE: RegistryEntry<ArmInteractionPointType, RecordPlayerType> =
        ModRegistrate
            .generic("record_player", CreateRegistries.ARM_INTERACTION_POINT_TYPE) {
                RecordPlayerType()
            }.register()

    val RECORD_PRESS_BASE_TYPE: RegistryEntry<ArmInteractionPointType, RecordPressBaseType> =
        ModRegistrate
            .generic("resonance_press", CreateRegistries.ARM_INTERACTION_POINT_TYPE) {
                RecordPressBaseType()
            }.register()

    val MUSIC_BOX_TYPE: RegistryEntry<ArmInteractionPointType, MusicBoxType> =
        ModRegistrate
            .generic("music_box", CreateRegistries.ARM_INTERACTION_POINT_TYPE) {
                MusicBoxType()
            }.register()

    val MIDI_TABLE_TYPE: RegistryEntry<ArmInteractionPointType, MidiTableType> =
        ModRegistrate
            .generic("midi_table", CreateRegistries.ARM_INTERACTION_POINT_TYPE) {
                MidiTableType()
            }.register()

    override fun register(registry: Registry<*>?) {
    }

    class RecordPressBaseType : ArmInteractionPointType() {
        override fun canCreatePoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): Boolean = state.`is`(ModBlocks.RECORD_PRESS_BASE.get())

        override fun createPoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): ArmInteractionPoint = RecordPressBaseArmInteractionPoint(this, level, pos, state)
    }

    class MidiTableType : ArmInteractionPointType() {
        override fun canCreatePoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): Boolean = state.`is`(ModBlocks.MIDI_TABLE.get())

        override fun createPoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): ArmInteractionPoint = MidiTableArmInteractionPoint(this, level, pos, state)
    }

    class RecordPlayerType : AllArmInteractionPointTypes.JukeboxType() {
        override fun canCreatePoint(
            level: Level?,
            pos: BlockPos?,
            state: BlockState,
        ): Boolean = state.`is`(ModBlocks.KINETIC_NETWORK_JUKEBOX.get())

        override fun createPoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): ArmInteractionPoint = RecordPlayerArmPoint(this, level, pos, state)
    }

    /** Solo music box + conductor — swap MIDI score rolls like a jukebox disc. */
    class MusicBoxType : ArmInteractionPointType() {
        override fun canCreatePoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): Boolean =
            state.`is`(ModBlocks.KINETIC_MUSIC_BOX.get()) ||
                state.`is`(ModBlocks.MUSIC_CONDUCTOR.get())

        override fun createPoint(
            level: Level,
            pos: BlockPos,
            state: BlockState,
        ): ArmInteractionPoint = MusicBoxArmPoint(this, level, pos, state)
    }
}
