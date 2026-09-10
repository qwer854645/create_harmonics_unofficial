package io.github.qwer854645.createresonance.gui

import io.github.qwer854645.createresonance.config.ModConfigs
import io.github.qwer854645.createresonance.foundation.eventbus.ClientEvents
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.registry.ModItems
import io.github.qwer854645.createresonance.gui.OpenLibMenuButton.MenuRows
import io.github.qwer854645.createresonance.gui.OpenLibMenuButton.MenuRows.leftTextKeys
import io.github.qwer854645.createresonance.gui.OpenLibMenuButton.MenuRows.rightTextKeys
import net.createmod.catnip.gui.ScreenOpener
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.PauseScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import org.apache.commons.lang3.mutable.MutableObject
import java.util.function.Consumer

/**
 * Title / pause menu entry for Create: Resonance.
 *
 * FancyMenu:
 * - Stable locator message (language-independent): `create_resonance:menu_open`
 * - Opens screen id: `io.github.qwer854645.createresonance.gui.ResonanceMenuScreen`
 * - Disable injection: set `mainMenuLibButtonRow` / `ingameMenuLibButtonRow` to 0, then add your own
 *   FancyMenu button with Open Screen / Mimic on this widget if desired.
 */
class OpenLibMenuButton(
    x: Int,
    y: Int,
) : Button(
        x,
        y,
        20,
        20,
        // Stable id for FancyMenu across languages; icon is drawn instead of this text.
        Component.literal(WIDGET_ID),
        OnPress { click(it) },
        DEFAULT_NARRATION,
    ) {
    init {
        tooltip = Tooltip.create(Component.translatable("create_resonance.gui.main_menu.open_btn"))
    }

    override fun renderString(
        graphics: GuiGraphics,
        pFont: Font,
        pColor: Int,
    ) {
        graphics.renderItem(ModItems.WEBDISC.asStack(), x + 2, y + 2)
    }

    data object MenuRows {
        data class SingleMenuRow(
            val leftTextKey: String,
            val rightTextKey: String = "",
        )

        val MAIN_MENU =
            listOf(
                SingleMenuRow("menu.singleplayer"),
                SingleMenuRow("menu.multiplayer"),
                SingleMenuRow("fml.menu.mods", "menu.online"),
                SingleMenuRow("narrator.button.language", "narrator.button.accessibility"),
            )

        val INGAME_MENU =
            listOf(
                SingleMenuRow("menu.returnToGame"),
                SingleMenuRow("gui.advancements", "gui.stats"),
                SingleMenuRow("menu.sendFeedback", "menu.reportBugs"),
                SingleMenuRow("menu.options", "menu.shareToLan"),
                SingleMenuRow("menu.returnToMenu"),
            )

        fun List<SingleMenuRow>.leftTextKeys(): List<String> = map { it.leftTextKey }

        fun List<SingleMenuRow>.rightTextKeys(): List<String> = map { it.rightTextKey }
    }

    companion object {
        const val WIDGET_ID = "create_resonance:menu_open"

        fun click(b: Button?) {
            ScreenOpener.open(ResonanceMenuScreen(Minecraft.getInstance().screen))
        }
    }
}

object MainMenuHandler : CommonGuiEventHandler {
    override fun setupEvents() {
        EventBus.onMcMain<ClientEvents.ScreenEvent.Init> { event ->
            val screen = event.screen

            val menu: List<MenuRows.SingleMenuRow>
            val rowIdx: Int
            val offsetX: Int
            when (screen) {
                is TitleScreen -> {
                    menu = MenuRows.MAIN_MENU
                    rowIdx = ModConfigs.client.mainMenuLibButtonRow.get()
                    offsetX = ModConfigs.client.mainMenuLibButtonOffsetX.get()
                }

                is PauseScreen -> {
                    menu = MenuRows.INGAME_MENU
                    rowIdx = ModConfigs.client.ingameMenuLibButtonRow.get()
                    offsetX = ModConfigs.client.ingameMenuLibButtonOffsetX.get()
                }

                else -> return@onMcMain
            }

            // Row 0 = disabled so FancyMenu can own the entire title/pause layout.
            if (rowIdx == 0) return@onMcMain

            val onLeft = offsetX < 0
            val keys = if (onLeft) menu.leftTextKeys() else menu.rightTextKeys()
            if (rowIdx < 1 || rowIdx > keys.size) return@onMcMain
            val targetMessage = I18n.get(keys[rowIdx - 1])
            if (targetMessage.isBlank()) return@onMcMain

            val toAdd = MutableObject<GuiEventListener>(null)
            event.listenerList
                .stream()
                .filter { w -> w is AbstractWidget }
                .map { w -> w as AbstractWidget }
                .filter { w -> w.message.string == targetMessage }
                .findFirst()
                .ifPresent(
                    Consumer { w ->
                        toAdd.value =
                            OpenLibMenuButton(
                                w.x + offsetX + (if (onLeft) -20 else w.width),
                                w.y,
                            )
                    },
                )
            toAdd.value?.let { event.addListener(it) }
        }
    }
}
