package me.mochibit.createharmonics.content.records

import com.simibubi.create.content.equipment.goggles.GogglesItem
import me.mochibit.createharmonics.handler.RecordCraftingHandler
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class EtherealRecordItem(
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

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        isAdvanced: TooltipFlag,
    ) {
        val player = Minecraft.getInstance().player
        val wearingGoggles = player != null && GogglesItem.isWearingGoggles(player)

        val url = RecordUtilities.getAudioUrl(stack)
        if (wearingGoggles && !url.isNullOrBlank()) {
            tooltipComponents.add(
                Component
                    .translatable("create_webdisc.tooltips.item.webdisc.url_bound")
                    .withStyle(ChatFormatting.GRAY),
            )
        }

        super.appendHoverText(stack, context, tooltipComponents, isAdvanced)
    }
}
