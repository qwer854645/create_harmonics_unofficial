package me.mochibit.createharmonics.foundation.registry

import com.tterrag.registrate.util.entry.ItemEntry
import me.mochibit.createharmonics.ModRegistrate
import me.mochibit.createharmonics.content.records.EtherealRecordItem
import me.mochibit.createharmonics.foundation.info
import net.minecraft.core.Registry
import net.minecraft.world.item.Item

object ModItems : CommonRegistry {
    override val registrationOrder = 3

    val WEBDISC: ItemEntry<EtherealRecordItem> =
        ModRegistrate
            .item("webdisc") { EtherealRecordItem(Item.Properties().stacksTo(1)) }
            .lang("Webdisc")
            .model { ctx, prov ->
                prov.generated(ctx, prov.modLoc("item/webdisc_blank/base"))
            }.register()

    override fun register(registry: Registry<*>?) {
        "Registering items".info()
    }
}
