package io.github.qwer854645.createresonance.content.records

import io.github.qwer854645.createresonance.handler.RecordCraftingHandler
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class ResonanceDiscItem(
    props: Properties,
) : Item(props) {
    companion object {
        val JUKEBOX_DISCS: List<Item> by lazy {
            BuiltInRegistries.ITEM
                .stream()
                .filter { item ->
                    val stack = ItemStack(item)
                    stack.has(DataComponents.JUKEBOX_PLAYABLE)
                }.toList()
        }

        private fun truncateUrl(url: String, max: Int = 48): String =
            if (url.length <= max) url else url.take(max - 1) + "…"
    }

    override fun getDefaultInstance(): ItemStack {
        val default = super.getDefaultInstance()

        if (JUKEBOX_DISCS.isNotEmpty()) {
            val randomDisc = JUKEBOX_DISCS.random()
            RecordCraftingHandler.setCraftedWithDisc(
                default,
                ItemStack(randomDisc),
            )
        }

        return default
    }

    /** Enchantment glint when a URL is stored — visible at a glance in inventory. */
    override fun isFoil(stack: ItemStack): Boolean = stack.hasAssignedUrl()

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
        val url = RecordUtilities.getAudioUrl(stack)
        if (!url.isNullOrBlank()) {
            tooltipComponents.add(
                Component
                    .translatable("create_resonance.tooltips.item.resonance_disc.url_bound")
                    .withStyle(ChatFormatting.AQUA),
            )
            // Keep this method free of net.minecraft.client.* so the item class can load on
            // dedicated servers. Full URL is shown with advanced tooltips (F3+H).
            if (isAdvanced.isAdvanced) {
                tooltipComponents.add(
                    Component
                        .literal(truncateUrl(url))
                        .withStyle(ChatFormatting.DARK_GRAY),
                )
            } else {
                tooltipComponents.add(
                    Component
                        .translatable("create_resonance.tooltips.item.resonance_disc.url_hint")
                        .withStyle(ChatFormatting.DARK_GRAY),
                )
            }
        } else {
            tooltipComponents.add(
                Component
                    .translatable("create_resonance.tooltips.item.resonance_disc.url_empty")
                    .withStyle(ChatFormatting.DARK_GRAY),
            )
        }

        super.appendHoverText(stack, context, tooltipComponents, isAdvanced)
    }
}
