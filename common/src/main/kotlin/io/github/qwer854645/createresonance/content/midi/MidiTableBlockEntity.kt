package io.github.qwer854645.createresonance.content.midi

import com.simibubi.create.content.kinetics.base.KineticBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import io.github.qwer854645.createresonance.foundation.goggles.ResonanceGoggles
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.Clearable
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.abs

class MidiTableBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : KineticBlockEntity(type, pos, state),
    Clearable {
    lateinit var behaviour: MidiTableBehaviour
        private set

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        behaviour = MidiTableBehaviour(this)
        behaviours.add(behaviour)
        behaviour.addSubBehaviours(behaviours)
    }

    fun configureMidi(
        bytes: ByteArray,
        name: String,
    ): Boolean = behaviour.configureMidi(bytes, name)

    fun hasConfiguredMidi(): Boolean = behaviour.hasConfiguredMidi()

    val configuredMidiName: String
        get() = behaviour.midiName

    override fun addToGoggleTooltip(
        tooltip: MutableList<Component>,
        isPlayerSneaking: Boolean,
    ): Boolean {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking)
        ResonanceGoggles.header(tooltip, "gui.goggles.midi_table.header")

        if (behaviour.hasConfiguredMidi()) {
            ResonanceGoggles.line(
                tooltip,
                "gui.goggles.midi_table.configured",
                ResonanceGoggles.truncate(behaviour.midiName.ifBlank { "MIDI" }),
            )
        } else {
            ResonanceGoggles.line(
                tooltip,
                "gui.goggles.midi_table.not_configured",
                style = ChatFormatting.DARK_GRAY,
            )
        }

        when {
            behaviour.imprinting ->
                ResonanceGoggles.line(tooltip, "gui.goggles.midi_table.imprinting", style = ChatFormatting.GREEN)
            abs(speed) >= 1f && behaviour.hasConfiguredMidi() ->
                ResonanceGoggles.line(tooltip, "gui.goggles.midi_table.ready", style = ChatFormatting.YELLOW)
            else ->
                ResonanceGoggles.line(tooltip, "gui.goggles.midi_table.idle", style = ChatFormatting.DARK_GRAY)
        }

        val held = behaviour.heldItem?.stack
        when {
            held != null && !held.isEmpty && MidiRollUtilities.isMidiRoll(held) && !MidiRollUtilities.hasMidi(held) ->
                ResonanceGoggles.line(tooltip, "gui.goggles.midi_table.blank_roll")
            held != null && !held.isEmpty ->
                ResonanceGoggles.plain(tooltip, held.hoverName.string)
            else ->
                ResonanceGoggles.line(tooltip, "gui.goggles.midi_table.empty", style = ChatFormatting.DARK_GRAY)
        }
        return true
    }

    override fun canPlayerUse(player: Player): Boolean {
        val level = level ?: return false
        if (level.getBlockEntity(worldPosition) !== this) return false
        return player.distanceToSqr(
            worldPosition.x + 0.5,
            worldPosition.y + 0.5,
            worldPosition.z + 0.5,
        ) <= 64.0
    }

    override fun clearContent() {
        val handler = behaviour.itemHandler
        for (i in 0 until handler.slots) {
            handler.extractItem(i, Int.MAX_VALUE, false)
        }
        notifyUpdate()
    }
}
