package io.github.qwer854645.createresonance.event.crafting

import io.github.qwer854645.createresonance.foundation.eventbus.ModEvent
import net.minecraft.world.item.ItemStack

data class RecipeAssembledEvent(
    val ingredients: List<ItemStack>,
    val result: List<ItemStack>,
) : ModEvent
