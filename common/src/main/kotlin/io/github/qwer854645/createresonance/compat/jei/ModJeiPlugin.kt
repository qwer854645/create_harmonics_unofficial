package io.github.qwer854645.createresonance.compat.jei

import io.github.qwer854645.createresonance.foundation.extension.asResource
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import net.minecraft.resources.ResourceLocation

@JeiPlugin
class ModJeiPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation = "jei_plugin".asResource()
}
