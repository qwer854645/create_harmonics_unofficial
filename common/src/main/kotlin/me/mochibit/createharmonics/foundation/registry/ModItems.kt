package me.mochibit.createharmonics.foundation.registry

import com.tterrag.registrate.util.entry.ItemEntry
import me.mochibit.createharmonics.ModRegistrate
import me.mochibit.createharmonics.content.records.BaseRecordItem
import me.mochibit.createharmonics.content.records.EtherealRecordItem
import me.mochibit.createharmonics.content.records.RecordType
import me.mochibit.createharmonics.foundation.info
import net.minecraft.core.Registry
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import java.util.EnumMap

object ModItems : CommonRegistry {
    override val registrationOrder = 3

    val BASE_RECORD: ItemEntry<BaseRecordItem> =
        ModRegistrate
            .item("webdisc_blank") { BaseRecordItem(Item.Properties().stacksTo(16)) }
            .model { ctx, prov ->
                prov.generated(ctx, prov.modLoc("item/webdisc_blank/base"))
            }.register()

    val BROKEN_WEBDISCS =
        EnumMap<RecordType, ItemEntry<EtherealRecordItem>>(RecordType::class.java).apply {
            RecordType.entries
                .filter { it != RecordType.CREATIVE }
                .forEach { this[it] = registerBrokenWebdiscVariant(it) }
        }

    val WEBDISCS =
        EnumMap<RecordType, ItemEntry<EtherealRecordItem>>(RecordType::class.java).apply {
            RecordType.entries.forEach { this[it] = registerWebdiscVariant(it) }
        }

    private fun registerBrokenWebdiscVariant(recordType: RecordType): ItemEntry<EtherealRecordItem> {
        val typeName = recordType.name.lowercase()
        return ModRegistrate
            .item("broken_${typeName}_webdisc") {
                EtherealRecordItem(recordType, Item.Properties().stacksTo(1), true)
            }.apply {
                recordType.properties.materialDisplayName?.let { lang("Broken $it Webdisc") }
            }.model { ctx, prov ->
                prov.generated(ctx, prov.modLoc("item/webdisc/${typeName}_broken"))
            }.register()
    }

    private fun registerWebdiscVariant(recordType: RecordType): ItemEntry<EtherealRecordItem> {
        val typeName = recordType.name.lowercase()
        return ModRegistrate
            .item("${typeName}_webdisc") {
                val properties = Item.Properties().stacksTo(1)
                if (recordType == RecordType.CREATIVE) properties.rarity(Rarity.EPIC)
                EtherealRecordItem(recordType, properties)
            }.apply {
                recordType.properties.materialDisplayName?.let { lang("$it Webdisc") }
            }.model { ctx, prov ->
                prov.generated(ctx, prov.modLoc("item/webdisc/$typeName"))
            }.register()
    }

    fun getWebdiscItem(recordType: RecordType): ItemEntry<EtherealRecordItem> = WEBDISCS.getValue(recordType)

    fun getBrokenWebdiscItem(recordType: RecordType): ItemEntry<EtherealRecordItem>? = BROKEN_WEBDISCS[recordType]

    fun brokenVariantOf(recordType: RecordType): Item? = BROKEN_WEBDISCS[recordType]?.get()

    override fun register(registry: Registry<*>?) {
        "Registering items".info()
    }

    infix fun ModItems.webdisc(recordType: RecordType): EtherealRecordItem = getWebdiscItem(recordType).get()
}
