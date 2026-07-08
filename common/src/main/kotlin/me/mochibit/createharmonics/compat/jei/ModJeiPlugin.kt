package me.mochibit.createharmonics.compat.jei

import me.mochibit.createharmonics.foundation.extension.asResource
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import net.minecraft.resources.ResourceLocation

@JeiPlugin
class ModJeiPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation = "jei_plugin".asResource()
}
