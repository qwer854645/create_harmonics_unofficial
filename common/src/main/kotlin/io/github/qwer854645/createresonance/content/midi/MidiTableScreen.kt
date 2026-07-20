package io.github.qwer854645.createresonance.content.midi

import com.simibubi.create.foundation.gui.AllIcons
import com.simibubi.create.foundation.gui.widget.IconButton
import com.simibubi.create.foundation.gui.widget.Label
import com.simibubi.create.foundation.gui.widget.ScrollInput
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput
import io.github.qwer854645.createresonance.audio.midi.MidiEngine
import io.github.qwer854645.createresonance.foundation.network.packet.ConfigureMidiTablePacket
import io.github.qwer854645.createresonance.foundation.registry.ModBlocks
import io.github.qwer854645.createresonance.foundation.registry.ModPackets
import io.github.qwer854645.createresonance.gui.ResonanceGuiStyle
import net.createmod.catnip.gui.AbstractSimiScreen
import net.createmod.catnip.gui.element.GuiGameElement
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.io.File

/**
 * Side-config screen: pick a .mid once; the kinetic depot stamps blank rolls automatically.
 * Visual language matches [MusicBoxScreen] parchment panels.
 */
class MidiTableScreen(
    private val be: MidiTableBlockEntity,
) : AbstractSimiScreen(Component.translatable("create_resonance.gui.midi_table.title")) {
    companion object {
        private const val GUI_WIDTH = 176
        private const val GUI_HEIGHT = 92
        private const val FILE_ROW_X = 8
        private const val FILE_ROW_Y = 36
        private const val FILE_ROW_W = 140
        private const val FILE_ROW_H = 18
        private const val LABEL_MAX_W = 118
    }

    private val renderedItem = ItemStack(ModBlocks.MIDI_TABLE.get())

    private val noFiles = Component.translatable("create_resonance.gui.midi_table.no_files")
    private val availableTitle = Component.translatable("create_resonance.gui.midi_table.available")
    private val folderTooltip = Component.translatable("create_resonance.gui.midi_table.open_folder")
    private val refreshTooltip = Component.translatable("create_resonance.gui.midi_table.refresh")
    private val confirmTooltip = Component.translatable("create_resonance.gui.midi_table.confirm_config")
    private val hint = Component.translatable("create_resonance.gui.midi_table.hint")

    private var filesArea: ScrollInput? = null
    private lateinit var filesLabel: Label
    private lateinit var confirmButton: IconButton
    private lateinit var folderButton: IconButton
    private lateinit var refreshButton: IconButton
    /** Full paths kept for upload; scroll options use truncated display names. */
    private var midiFiles: List<File> = emptyList()
    private var displayNames: List<Component> = emptyList()

    override fun init() {
        setWindowSize(GUI_WIDTH, GUI_HEIGHT)
        super.init()
        clearWidgets()
        refreshFileList()
        buildFileWidgets()

        val x = guiLeft
        val y = guiTop

        folderButton = IconButton(x + 8, y + 66, AllIcons.I_OPEN_FOLDER)
        folderButton.withCallback<IconButton> {
            Util.getPlatform().openFile(MidiEngine.importDir())
        }
        folderButton.setToolTip(folderTooltip)
        addRenderableWidget(folderButton)

        refreshButton = IconButton(x + 30, y + 66, AllIcons.I_REFRESH)
        refreshButton.withCallback<IconButton> { rebuildFileWidgets() }
        refreshButton.setToolTip(refreshTooltip)
        addRenderableWidget(refreshButton)

        confirmButton = IconButton(x + GUI_WIDTH - 28, y + 66, AllIcons.I_CONFIRM)
        confirmButton.withCallback<IconButton> { uploadSelectedFile() }
        confirmButton.setToolTip(confirmTooltip)
        addRenderableWidget(confirmButton)
    }

    private fun refreshFileList() {
        midiFiles = MidiEngine.listImportFiles()
        displayNames =
            midiFiles.map { file ->
                Component.literal(ResonanceGuiStyle.ellipsize(file.name, LABEL_MAX_W))
            }
    }

    private fun buildFileWidgets() {
        filesArea?.let { removeWidget(it) }
        if (::filesLabel.isInitialized) removeWidget(filesLabel)
        filesArea = null

        val x = guiLeft
        val y = guiTop
        filesLabel =
            Label(x + FILE_ROW_X + 6, y + FILE_ROW_Y + 5, CommonComponents.EMPTY)
                .colored(ResonanceGuiStyle.COL_BODY)
        filesLabel.text = CommonComponents.EMPTY

        if (displayNames.isNotEmpty()) {
            filesArea =
                SelectionScrollInput(x + FILE_ROW_X, y + FILE_ROW_Y, FILE_ROW_W, FILE_ROW_H)
                    .forOptions(displayNames)
                    .titled(availableTitle.plainCopy())
                    .writingTo(filesLabel) as ScrollInput
            addRenderableWidget(filesArea!!)
            addRenderableWidget(filesLabel)
            // Keep label clipped even if SelectionScrollInput writes a long string later.
            filesLabel.setTextAndTrim(displayNames[0], true, LABEL_MAX_W)
        }
    }

    private fun rebuildFileWidgets() {
        refreshFileList()
        buildFileWidgets()
    }

    private fun uploadSelectedFile() {
        if (filesArea == null || midiFiles.isEmpty()) return
        val index = filesArea!!.state.coerceIn(0, midiFiles.lastIndex)
        val file = midiFiles[index]
        val bytes = file.readBytes()
        if (bytes.size > MidiEngine.MAX_MIDI_BYTES) return
        ModPackets.sendToServer(
            ConfigureMidiTablePacket(
                blockPos = be.blockPos,
                midiBytes = bytes,
                displayName = file.nameWithoutExtension,
            ),
        )
        onClose()
    }

    override fun tick() {
        super.tick()
        if (::confirmButton.isInitialized) {
            confirmButton.active = filesArea != null && midiFiles.isNotEmpty()
        }
        // Re-trim every tick in case the scroll input rewrote the label with a long option.
        if (::filesLabel.isInitialized && filesArea != null && displayNames.isNotEmpty()) {
            val idx = filesArea!!.state.coerceIn(0, displayNames.lastIndex)
            filesLabel.setTextAndTrim(displayNames[idx], true, LABEL_MAX_W)
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
        val w = GUI_WIDTH
        val h = GUI_HEIGHT

        ResonanceGuiStyle.drawPanel(graphics, x, y, w, h)
        graphics.drawString(font, title, x + 8, y + 6, ResonanceGuiStyle.COL_TITLE, false)

        if (be.hasConfiguredMidi()) {
            val current =
                Component.translatable(
                    "create_resonance.gui.midi_table.current",
                    be.configuredMidiName.ifBlank { "MIDI" },
                )
            graphics.drawString(
                font,
                ResonanceGuiStyle.ellipsize(current.string, w - 16),
                x + 8,
                y + 18,
                ResonanceGuiStyle.COL_BODY,
                false,
            )
        } else {
            graphics.drawString(
                font,
                ResonanceGuiStyle.ellipsize(hint.string, w - 16),
                x + 8,
                y + 18,
                ResonanceGuiStyle.COL_HINT,
                false,
            )
        }

        if (filesArea == null) {
            graphics.drawString(font, noFiles, x + 8, y + 40, ResonanceGuiStyle.COL_HINT, false)
        }

        GuiGameElement
            .of(renderedItem)
            .at<GuiGameElement.GuiRenderBuilder>(x + w + 8f, y + h - 40f, -200f)
            .scale(3.0)
            .render(graphics)
    }
}
