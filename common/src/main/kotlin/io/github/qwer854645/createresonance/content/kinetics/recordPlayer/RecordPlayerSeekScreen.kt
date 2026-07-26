package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import com.simibubi.create.foundation.gui.AllIcons
import com.simibubi.create.foundation.gui.widget.IconButton
import io.github.qwer854645.createresonance.foundation.network.packet.SeekRecordPlayerPacket
import io.github.qwer854645.createresonance.foundation.registry.ModPackets
import io.github.qwer854645.createresonance.gui.ResonanceGuiStyle
import net.createmod.catnip.gui.AbstractSimiScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import kotlin.math.max

/**
 * Record-player seek / transport UI — parchment style matching [MusicBoxScreen].
 */
class RecordPlayerSeekScreen(
    private val be: RecordPlayerBlockEntity,
) : AbstractSimiScreen(Component.translatable("create_resonance.gui.record_player_seek.title")) {
    companion object {
        private const val WINDOW_WIDTH = 176
        private const val WINDOW_HEIGHT = 100
        private const val FALLBACK_DURATION = 300.0
        private const val COL_TITLE = ResonanceGuiStyle.COL_TITLE
        private const val COL_HINT = ResonanceGuiStyle.COL_HINT
    }

    private var durationSeconds = FALLBACK_DURATION
    private var minuteInput: EditBox? = null
    private var secondInput: EditBox? = null
    private var seekButton: Button? = null

    override fun init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        super.init()

        val behaviour = be.playerBehaviour
        durationSeconds = resolveDuration(behaviour)
        val current = RecordPlayerClientHandler.resolveCurrentSeconds(behaviour).coerceIn(0.0, durationSeconds)
        val total = Mth.floor(current).coerceAtLeast(0)

        val x = guiLeft
        val y = guiTop

        // Transport: play / pause / restart — nudged left of center for balance with seek row.
        val transportY = y + 34
        addRenderableWidget(
            IconButton(x + 46, transportY, AllIcons.I_PLAY).also {
                it.withCallback<IconButton> {
                    ModPackets.sendToServer(SeekRecordPlayerPacket(be.blockPos, action = "play"))
                }
                it.setToolTip(Component.translatable("create_resonance.gui.record_player_seek.play"))
            },
        )
        addRenderableWidget(
            IconButton(x + 68, transportY, AllIcons.I_PAUSE).also {
                it.withCallback<IconButton> {
                    ModPackets.sendToServer(SeekRecordPlayerPacket(be.blockPos, action = "pause"))
                }
                it.setToolTip(Component.translatable("create_resonance.gui.record_player_seek.pause"))
            },
        )
        addRenderableWidget(
            IconButton(x + 90, transportY, AllIcons.I_STOP).also {
                it.withCallback<IconButton> {
                    sendRestart()
                }
                it.setToolTip(Component.translatable("create_resonance.gui.record_player_seek.restart"))
            },
        )

        val rowY = y + 58
        val minutes =
            ResonanceGuiStyle
                .parchmentEditBox(
                    font,
                    x + 8,
                    rowY,
                    Component.translatable("create_resonance.gui.music_box.minutes"),
                ).also {
                    it.value = (total / 60).toString()
                    it.setMaxLength(4)
                    it.setFilter { text -> text.isEmpty() || text.all(Char::isDigit) }
                }
        val seconds =
            ResonanceGuiStyle
                .parchmentEditBox(
                    font,
                    x + 48,
                    rowY,
                    Component.translatable("create_resonance.gui.music_box.seconds"),
                ).also {
                    it.value = "%02d".format(total % 60)
                    it.setMaxLength(2)
                    it.setFilter { text -> text.isEmpty() || text.all(Char::isDigit) }
                }
        minuteInput = minutes
        secondInput = seconds
        addRenderableWidget(minutes)
        addRenderableWidget(seconds)

        val seek =
            Button
                .builder(Component.translatable("create_resonance.gui.music_box.seek")) {
                    parseSeekSeconds()?.let { sec ->
                        sendSeek(sec.coerceIn(0.0, durationSeconds))
                        onClose()
                    }
                }.bounds(x + 84, rowY, 48, 16)
                .build()
        seekButton = seek
        seek.active = parseSeekSeconds() != null
        addRenderableWidget(seek)
    }

    override fun tick() {
        super.tick()
        val behaviour = be.playerBehaviour
        val resolved = resolveDuration(behaviour)
        if (resolved > durationSeconds) {
            durationSeconds = resolved
        }
        seekButton?.active = parseSeekSeconds() != null

        val minutes = minuteInput ?: return
        val seconds = secondInput ?: return
        if (!minutes.isFocused && !seconds.isFocused) {
            val live = RecordPlayerClientHandler.resolveCurrentSeconds(behaviour).coerceIn(0.0, durationSeconds)
            val total = Mth.floor(live).coerceAtLeast(0)
            minutes.value = (total / 60).toString()
            seconds.value = "%02d".format(total % 60)
        }
    }

    override fun renderWindow(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val x = guiLeft
        val y = guiTop
        val w = WINDOW_WIDTH
        val h = WINDOW_HEIGHT

        ResonanceGuiStyle.drawPanel(graphics, x, y, w, h)
        graphics.drawString(font, title, x + 8, y + 6, COL_TITLE, false)

        val behaviour = be.playerBehaviour
        val current = RecordPlayerClientHandler.resolveCurrentSeconds(behaviour)
        val progress =
            Component
                .translatable(
                    "create_resonance.gui.music_box.progress",
                    formatTime(current),
                    formatTime(durationSeconds),
                ).string
        graphics.drawString(font, progress, x + 8, y + 20, COL_HINT, false)

        val rowY = y + 58
        ResonanceGuiStyle.drawFieldPlate(graphics, x + 8, rowY, 28, 16)
        ResonanceGuiStyle.drawFieldPlate(graphics, x + 48, rowY, 28, 16)
        graphics.drawString(font, ":", x + 40, rowY + 4, COL_HINT, false)
    }

    private fun resolveDuration(behaviour: RecordPlayerBehaviour): Double =
        max(30.0, RecordPlayerClientHandler.resolveDurationSeconds(behaviour) ?: FALLBACK_DURATION)

    private fun sendSeek(positionSeconds: Double) {
        ModPackets.sendToServer(SeekRecordPlayerPacket(be.blockPos, positionSeconds))
    }

    private fun sendRestart() {
        ModPackets.sendToServer(SeekRecordPlayerPacket(be.blockPos, 0.0, restart = true))
    }

    private fun formatTime(seconds: Double): String {
        val total = Mth.floor(seconds).coerceAtLeast(0)
        return "%d:%02d".format(total / 60, total % 60)
    }

    private fun parseSeekSeconds(): Double? {
        val minutesText = minuteInput?.value?.trim() ?: return null
        val secondsText = secondInput?.value?.trim() ?: return null
        if (minutesText.isEmpty() || secondsText.isEmpty()) return null
        if (!minutesText.all(Char::isDigit) || !secondsText.all(Char::isDigit)) return null
        val minutes = minutesText.toIntOrNull() ?: return null
        val seconds = secondsText.toIntOrNull() ?: return null
        if (minutes < 0 || seconds !in 0..59) return null
        return minutes * 60.0 + seconds
    }
}
