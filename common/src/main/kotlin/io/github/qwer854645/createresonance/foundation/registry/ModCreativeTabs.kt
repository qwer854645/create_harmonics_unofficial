package io.github.qwer854645.createresonance.foundation.registry

import com.tterrag.registrate.util.entry.RegistryEntry
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.locale.ModLang
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.function.Predicate

object ModCreativeTabs : CommonRegistry {
    val MAIN_TAB: RegistryEntry<CreativeModeTab, CreativeModeTab> =
        creativeTab(
            "main",
            CreativeModeTab
                .builder()
                .title(ModLang.translate("item_group").component())
                .icon { ItemStack(ModItems.WEBDISC.get()) }
                .displayItems(DisplayItemsGenerator())
                .build(),
        )

    override fun register(registry: Registry<*>?) {
        "Registering creative tabs".info()
    }

    fun creativeTab(
        name: String,
        tab: CreativeModeTab,
    ): RegistryEntry<CreativeModeTab, CreativeModeTab> =
        ModRegistrate.simple(name, Registries.CREATIVE_MODE_TAB) {
            tab
        }

    private class DisplayItemsGenerator : CreativeModeTab.DisplayItemsGenerator {
        override fun accept(
            params: CreativeModeTab.ItemDisplayParameters,
            output: CreativeModeTab.Output,
        ) {
            val exclusionPredicate = makeExclusionPredicate()
            val orderings = makeOrderings()
            val stackFunc = makeStackFunc()
            val visibilityFunc = makeVisibilityFunc()

            val items = mutableListOf<Item>()

            items += collectItems(exclusionPredicate.or(IS_ITEM_3D.negate()))
            items += collectBlocks(exclusionPredicate)
            items += collectItems(exclusionPredicate.or(IS_ITEM_3D))

            applyOrderings(items, orderings)
            outputAll(output, items, stackFunc, visibilityFunc)
        }

        private fun collectBlocks(exclusionPredicate: Predicate<Item>): List<Item> =
            ModRegistrate
                .getAll(Registries.BLOCK)
                .mapNotNull { entry -> entry.get().asItem().takeIf { it != Items.AIR } }
                .filter { !exclusionPredicate.test(it) }
                .distinct()

        private fun collectItems(exclusionPredicate: Predicate<Item>): List<Item> =
            ModRegistrate
                .getAll(Registries.ITEM)
                .map { entry -> entry.get() }
                .filter { it !is BlockItem && !exclusionPredicate.test(it) }

        private fun applyOrderings(
            items: MutableList<Item>,
            orderings: List<ItemOrdering>,
        ) {
            for (ordering in orderings) {
                val anchorIndex = items.indexOf(ordering.anchor).takeIf { it != -1 } ?: continue
                val itemIndex = items.indexOf(ordering.item).takeIf { it != -1 } ?: continue

                items.removeAt(itemIndex)
                val adjustedAnchor = if (itemIndex < anchorIndex) anchorIndex - 1 else anchorIndex

                when (ordering.type) {
                    ItemOrdering.Type.BEFORE -> items.add(adjustedAnchor, ordering.item)
                    ItemOrdering.Type.AFTER -> items.add(adjustedAnchor + 1, ordering.item)
                }
            }
        }

        private fun outputAll(
            output: CreativeModeTab.Output,
            items: List<Item>,
            stackFunc: (Item) -> ItemStack,
            visibilityFunc: (Item) -> CreativeModeTab.TabVisibility,
        ) {
            for (item in items) {
                output.accept(stackFunc(item), visibilityFunc(item))
            }
        }
    }

    private fun makeExclusionPredicate(): Predicate<Item> {
        val exclusions = mutableSetOf<Item>()
        return Predicate { it in exclusions }
    }

    private fun makeOrderings(): List<ItemOrdering> = emptyList()

    private fun makeStackFunc(): (Item) -> ItemStack {
        val resonance_disc = ModItems.WEBDISC.get()
        return { item ->
            if (item == resonance_disc) {
                item.defaultInstance
            } else {
                ItemStack(item)
            }
        }
    }

    private fun makeVisibilityFunc(): (Item) -> CreativeModeTab.TabVisibility =
        { CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS }

    private val IS_ITEM_3D: Predicate<Item> = Predicate { false }

    private data class ItemOrdering(
        val item: Item,
        val anchor: Item,
        val type: Type,
    ) {
        enum class Type { BEFORE, AFTER }

        companion object {
            fun before(
                item: Item,
                anchor: Item,
            ) = ItemOrdering(item, anchor, Type.BEFORE)

            fun after(
                item: Item,
                anchor: Item,
            ) = ItemOrdering(item, anchor, Type.AFTER)
        }
    }
}
