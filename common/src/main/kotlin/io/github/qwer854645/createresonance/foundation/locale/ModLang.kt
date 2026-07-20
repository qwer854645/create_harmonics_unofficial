package io.github.qwer854645.createresonance.foundation.locale

import com.simibubi.create.foundation.utility.CreateLang
import io.github.qwer854645.createresonance.CreateResonanceMod
import net.createmod.catnip.lang.Lang
import net.createmod.catnip.lang.LangBuilder
import net.createmod.catnip.lang.LangNumberFormat
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

object ModLang : Lang() {
    fun builder(): LangBuilder = LangBuilder(CreateResonanceMod.MOD_ID)

    fun translatedOptions(
        prefix: String,
        vararg keys: String,
    ): MutableList<Component> {
        val result: MutableList<Component> = ArrayList(keys.size)

        for (key in keys) {
            result.add(translate("$prefix.$key").component())
        }

        return result
    }

    fun blockName(state: BlockState): LangBuilder =
        builder().add(
            state
                .block
                .name,
        )

    fun itemName(stack: ItemStack): LangBuilder =
        builder().add(
            stack
                .hoverName
                .copy(),
        )

    fun number(d: Double): LangBuilder = builder().text(LangNumberFormat.format(d))

    fun translate(
        langKey: String,
        vararg args: Any?,
    ): LangBuilder = builder().translate(langKey, *args)

    fun text(text: String): LangBuilder = builder().text(text)
}

interface LangProvider {
    fun provideLang(keyValueConsumer: (String, String) -> Unit)

    fun String.withModNamespace(): String = "${CreateResonanceMod.MOD_ID.lowercase()}.$this"
}
