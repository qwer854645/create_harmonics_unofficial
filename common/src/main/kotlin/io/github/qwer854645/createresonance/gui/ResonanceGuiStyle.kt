package io.github.qwer854645.createresonance.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

/**
 * Shared Create: Resonance GUI look — soft parchment like [MusicBoxScreen].
 * Text colours stay dark enough to read on cream panels and Create schedule fields.
 */
object ResonanceGuiStyle {
    /** Default MM/SS seek field plate size. */
    const val TIME_FIELD_W = 28
    const val TIME_FIELD_H = 16

    /** Vanilla EditBox text insets when bordered (we keep these without the dark border). */
    private const val EDIT_PAD_X = 4
    private const val EDIT_TEXT_H = 8
    /** Near-ink brown for titles. */
    const val COL_TITLE = 0x2A1810

    /** Primary body / EditBox text. */
    const val COL_BODY = 0x1E140E

    /** Secondary captions. */
    const val COL_HINT = 0x5C4638

    /** Soft warning (needs power). */
    const val COL_WARN = 0x8A5528

    private const val PANEL_OUTER = 0xFFC8B090.toInt()
    private const val PANEL_INNER = 0xFFF6EFE3.toInt()
    private const val PANEL_OUTLINE = 0xFF8B7355.toInt()
    private const val INSET_OUTER = 0xFFB9A588.toInt()
    private const val INSET_INNER = 0xFFF0E6D4.toInt()
    private const val FIELD_BG = 0xFFFBF6EC.toInt()

    fun drawPanel(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
    ) {
        graphics.fill(x, y, x + w, y + h, PANEL_OUTER)
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_INNER)
        graphics.renderOutline(x, y, w, h, PANEL_OUTLINE)
    }

    fun drawInset(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
    ) {
        graphics.fill(x, y, x + w, y + h, INSET_OUTER)
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, INSET_INNER)
        graphics.renderOutline(x, y, w, h, PANEL_OUTLINE)
    }

    /** Light field plate behind Create schedule input chrome / EditBoxes. */
    fun drawFieldPlate(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
    ) {
        graphics.fill(x, y, x + w, y + h, FIELD_BG)
        graphics.renderOutline(x, y, w, h, PANEL_OUTLINE)
    }

    /** Dark ink on cream plates — readable; no shadow (shadow washes out on parchment). */
    fun styleParchmentEditBox(box: EditBox) {
        box.setTextColor(COL_BODY)
        box.setTextColorUneditable(COL_HINT)
        box.setTextShadow(false)
        // Borderless text is drawn at (x, y) instead of the bordered inset —
        // use [parchmentEditBox] so digits stay centred on [drawFieldPlate].
        box.setBordered(false)
    }

    /**
     * EditBox laid out on a parchment field plate: same horizontal/vertical
     * insets as a bordered vanilla field, without the dark border chrome.
     */
    fun parchmentEditBox(
        font: Font,
        plateX: Int,
        plateY: Int,
        message: Component,
        plateW: Int = TIME_FIELD_W,
        plateH: Int = TIME_FIELD_H,
    ): EditBox {
        val textX = plateX + EDIT_PAD_X
        val textY = plateY + (plateH - EDIT_TEXT_H) / 2
        val textW = (plateW - EDIT_PAD_X * 2).coerceAtLeast(8)
        return EditBox(font, textX, textY, textW, EDIT_TEXT_H, message).also {
            styleParchmentEditBox(it)
        }
    }

    fun ellipsize(
        text: String,
        maxWidthPx: Int,
    ): String {
        val font = Minecraft.getInstance().font
        if (font.width(text) <= maxWidthPx) return text
        var truncated = text
        while (truncated.isNotEmpty() && font.width("$truncated…") > maxWidthPx) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isEmpty()) "…" else "$truncated…"
    }
}
