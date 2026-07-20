package io.github.qwer854645.createresonance.content.midi

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

/**
 * Blank MIDI roll (Create schematic analogue). Imprint at the MIDI Table, then insert into a music box.
 */
class MidiRollItem(
    props: Properties,
) : Item(props) {
    override fun isFoil(stack: ItemStack): Boolean = MidiRollUtilities.hasMidi(stack)

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
        if (MidiRollUtilities.hasMidi(stack)) {
            val name = MidiRollUtilities.getDisplayName(stack).ifBlank { "MIDI" }
            tooltipComponents.add(
                Component
                    .translatable("create_resonance.tooltips.item.midi_roll.bound", name)
                    .withStyle(ChatFormatting.AQUA),
            )
        } else {
            tooltipComponents.add(
                Component
                    .translatable("create_resonance.tooltips.item.midi_roll.blank")
                    .withStyle(ChatFormatting.DARK_GRAY),
            )
        }
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced)
    }
}
