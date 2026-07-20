package io.github.qwer854645.createresonance.foundation.goggles

import io.github.qwer854645.createresonance.foundation.locale.ModLang
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

/** Shared Create Engineer's Goggles helpers for Resonance machines. */
object ResonanceGoggles {
    fun header(
        tooltip: MutableList<Component>,
        key: String,
    ) {
        ModLang
            .translate(key)
            .style(ChatFormatting.GRAY)
            .forGoggles(tooltip)
    }

    fun line(
        tooltip: MutableList<Component>,
        key: String,
        vararg args: Any?,
        style: ChatFormatting = ChatFormatting.AQUA,
    ) {
        ModLang
            .translate(key, *args)
            .style(style)
            .forGoggles(tooltip)
    }

    fun plain(
        tooltip: MutableList<Component>,
        text: String,
        style: ChatFormatting = ChatFormatting.AQUA,
    ) {
        ModLang
            .text(text)
            .style(style)
            .forGoggles(tooltip)
    }

    fun formatTime(seconds: Double): String {
        val total = Mth.floor(seconds).coerceAtLeast(0)
        return "%d:%02d".format(total / 60, total % 60)
    }

    fun truncate(
        text: String,
        max: Int = 28,
    ): String = if (text.length <= max) text else text.take(max - 1) + "…"
}
