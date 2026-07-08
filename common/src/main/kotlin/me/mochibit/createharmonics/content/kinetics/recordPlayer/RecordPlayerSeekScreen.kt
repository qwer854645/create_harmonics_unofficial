package me.mochibit.createharmonics.content.kinetics.recordPlayer

import me.mochibit.createharmonics.foundation.locale.ModLang
import me.mochibit.createharmonics.foundation.network.packet.SeekRecordPlayerPacket
import me.mochibit.createharmonics.foundation.registry.ModPackets
import net.createmod.catnip.gui.AbstractSimiScreen
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import kotlin.math.max

class RecordPlayerSeekScreen(
    private val be: RecordPlayerBlockEntity,
) : AbstractSimiScreen(ModLang.translate("gui.record_player_seek.title").component()) {
    companion object {
        private const val WINDOW_WIDTH = 220
        private const val WINDOW_HEIGHT = 96
        private const val FALLBACK_DURATION = 300.0
    }

    private var durationSeconds = FALLBACK_DURATION
    private var targetSeconds = 0.0
    private var syncingFromSlider = false
    private var syncingFromInput = false
    private lateinit var progressSlider: SeekSlider
    private lateinit var timeInput: EditBox

    private inner class SeekSlider(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        initialSeconds: Double,
    ) : AbstractSliderButton(
            x,
            y,
            width,
            height,
            formatTime(initialSeconds),
            (initialSeconds / durationSeconds).coerceIn(0.0, 1.0),
        ) {
        override fun updateMessage() {
            message = formatTime(targetSeconds)
        }

        override fun applyValue() {
            targetSeconds = this.value * durationSeconds
            updateMessage()
            if (!syncingFromInput) {
                syncingFromSlider = true
                timeInput.value = formatTimeString(targetSeconds)
                syncingFromSlider = false
            }
        }

        fun setSeconds(seconds: Double) {
            targetSeconds = seconds.coerceIn(0.0, durationSeconds)
            value = (targetSeconds / durationSeconds).coerceIn(0.0, 1.0)
            updateMessage()
        }
    }

    override fun init() {
        setWindowSize(WINDOW_WIDTH, WINDOW_HEIGHT)
        super.init()

        val behaviour = be.playerBehaviour
        durationSeconds = resolveDuration(behaviour)
        val currentSeconds = RecordPlayerClientHandler.resolveCurrentSeconds(behaviour).coerceIn(0.0, durationSeconds)
        targetSeconds = currentSeconds

        progressSlider =
            SeekSlider(
                guiLeft + 12,
                guiTop + 24,
                WINDOW_WIDTH - 24,
                16,
                currentSeconds,
            )
        addRenderableWidget(progressSlider)

        timeInput =
            EditBox(
                font,
                guiLeft + 12,
                guiTop + 46,
                72,
                18,
                ModLang.translate("gui.record_player_seek.time_input").component(),
            )
        timeInput.value = formatTimeString(targetSeconds)
        timeInput.setMaxLength(16)
        timeInput.setResponder { text ->
            if (syncingFromSlider) return@setResponder
            syncingFromInput = true
            parseTime(text)?.let { parsed ->
                targetSeconds = parsed.coerceIn(0.0, durationSeconds)
                progressSlider.setSeconds(targetSeconds)
            }
            syncingFromInput = false
        }
        addRenderableWidget(timeInput)

        addRenderableWidget(
            Button
                .builder(ModLang.translate("gui.record_player_seek.apply").component()) {
                    val parsed = parseTime(timeInput.value)
                    if (parsed != null) {
                        targetSeconds = parsed.coerceIn(0.0, durationSeconds)
                    }
                    sendSeek(targetSeconds)
                    onClose()
                }.bounds(guiLeft + 90, guiTop + 46, 52, 18)
                .build(),
        )

        addRenderableWidget(
            Button
                .builder(ModLang.translate("gui.record_player_seek.restart").component()) {
                    sendRestart()
                    onClose()
                }.bounds(guiLeft + WINDOW_WIDTH - 72, guiTop + 46, 60, 18)
                .build(),
        )
    }

    override fun tick() {
        super.tick()

        val behaviour = be.playerBehaviour
        val resolvedDuration = resolveDuration(behaviour)
        if (resolvedDuration > durationSeconds) {
            durationSeconds = resolvedDuration
        }

        val liveCurrent = RecordPlayerClientHandler.resolveCurrentSeconds(behaviour).coerceIn(0.0, durationSeconds)
        if (!progressSlider.isFocused && !timeInput.isFocused) {
            targetSeconds = liveCurrent
            progressSlider.setSeconds(liveCurrent)
            if (!syncingFromSlider) {
                syncingFromInput = true
                timeInput.value = formatTimeString(liveCurrent)
                syncingFromInput = false
            }
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

        graphics.fill(x, y, x + WINDOW_WIDTH, y + WINDOW_HEIGHT, 0xC0101010.toInt())
        graphics.fill(x + 1, y + 1, x + WINDOW_WIDTH - 1, y + WINDOW_HEIGHT - 1, 0xF0202020.toInt())
        graphics.renderOutline(x, y, WINDOW_WIDTH, WINDOW_HEIGHT, 0xFF555555.toInt())

        graphics.drawCenteredString(font, title, x + WINDOW_WIDTH / 2, y + 7, 0xFFFFFF)
        graphics.drawString(
            font,
            ModLang.translate("gui.record_player_seek.time_label").component(),
            x + 12,
            y + 38,
            0xA0A0A0,
            false,
        )

        val current = RecordPlayerClientHandler.resolveCurrentSeconds(be.playerBehaviour)
        graphics.drawCenteredString(
            font,
            Component.literal("${formatTimeString(current)} / ${formatTimeString(durationSeconds)}"),
            x + WINDOW_WIDTH / 2,
            y + WINDOW_HEIGHT - 10,
            0xA0A0A0,
        )
    }

    private fun resolveDuration(behaviour: RecordPlayerBehaviour): Double =
        max(30.0, RecordPlayerClientHandler.resolveDurationSeconds(behaviour) ?: FALLBACK_DURATION)

    private fun sendSeek(positionSeconds: Double) {
        ModPackets.sendToServer(SeekRecordPlayerPacket(be.blockPos, positionSeconds))
    }

    private fun sendRestart() {
        ModPackets.sendToServer(SeekRecordPlayerPacket(be.blockPos, 0.0, restart = true))
    }

    private fun formatTime(seconds: Double): Component = Component.literal(formatTimeString(seconds))

    private fun formatTimeString(seconds: Double): String {
        val total = Mth.floor(seconds).toInt().coerceAtLeast(0)
        val minutes = total / 60
        val secs = total % 60
        return "%d:%02d".format(minutes, secs)
    }

    private fun parseTime(text: String): Double? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.contains(':')) {
            val parts = trimmed.split(':')
            if (parts.size != 2) return null
            val minutes = parts[0].toIntOrNull() ?: return null
            val seconds = parts[1].toIntOrNull() ?: return null
            if (seconds !in 0..59 || minutes < 0) return null
            return minutes * 60.0 + seconds
        }

        return trimmed.toDoubleOrNull()?.coerceAtLeast(0.0)
    }
}
