package io.github.qwer854645.createresonance.foundation.extension

import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import net.minecraft.resources.ResourceLocation

fun String.asResource(): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, this)

infix fun String.resPath(other: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(this, other)
