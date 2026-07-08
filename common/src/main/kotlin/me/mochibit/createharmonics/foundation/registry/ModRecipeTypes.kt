package me.mochibit.createharmonics.foundation.registry

import me.mochibit.createharmonics.foundation.info
import net.minecraft.core.Registry

object ModRecipeTypes : CommonRegistry {
    override fun register(registry: Registry<*>?) {
        "Registering recipe types".info()
    }
}
