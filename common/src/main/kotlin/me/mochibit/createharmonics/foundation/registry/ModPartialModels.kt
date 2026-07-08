package me.mochibit.createharmonics.foundation.registry

import dev.engine_room.flywheel.lib.model.baked.PartialModel
import me.mochibit.createharmonics.foundation.extension.asResource
import me.mochibit.createharmonics.foundation.info
import net.minecraft.core.Registry

object ModPartialModels : CommonRegistry {
    private val webdiscVisual: PartialModel = block("webdisc_visual/webdisc")

    fun getRecordModel(): PartialModel = webdiscVisual

    private fun block(path: String): PartialModel = PartialModel.of("block/$path".asResource())

    override fun register(registry: Registry<*>?) {
        "Lazily loading partial models".info()
    }
}
