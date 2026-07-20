package io.github.qwer854645.createresonance.content.midi

import io.github.qwer854645.createresonance.foundation.registry.ModDataComponents
import io.github.qwer854645.createresonance.foundation.registry.ModItems
import net.minecraft.world.item.ItemStack

object MidiRollUtilities {
    fun isMidiRoll(stack: ItemStack): Boolean = stack.item is MidiRollItem

    fun hasMidi(stack: ItemStack): Boolean =
        isMidiRoll(stack) && getMidiBytes(stack) != null

    fun getDisplayName(stack: ItemStack): String =
        stack.get(ModDataComponents.MIDI_NAME)?.takeIf { it.isNotBlank() } ?: ""

    fun getMidiBytes(stack: ItemStack): ByteArray? {
        if (!isMidiRoll(stack)) return null
        return stack.get(ModDataComponents.MIDI_BYTES)?.toByteArray()
    }

    fun writeMidi(
        stack: ItemStack,
        bytes: ByteArray,
        name: String,
    ): Boolean {
        if (!isMidiRoll(stack) || bytes.isEmpty() || bytes.size > MidiLimits.MAX_MIDI_BYTES) return false
        stack.set(ModDataComponents.MIDI_BYTES, MidiData.of(bytes))
        stack.set(ModDataComponents.MIDI_NAME, name.take(64).ifBlank { "MIDI" })
        return true
    }

    fun clearMidi(stack: ItemStack) {
        if (!isMidiRoll(stack)) return
        stack.remove(ModDataComponents.MIDI_BYTES)
        stack.remove(ModDataComponents.MIDI_NAME)
    }

    fun createFilledRoll(
        bytes: ByteArray,
        name: String,
    ): ItemStack? {
        if (bytes.isEmpty() || bytes.size > MidiLimits.MAX_MIDI_BYTES) return null
        val stack = ItemStack(ModItems.MIDI_ROLL.get())
        return if (writeMidi(stack, bytes, name)) stack else null
    }
}
