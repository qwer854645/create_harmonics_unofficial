package io.github.qwer854645.createresonance.gui

import com.mojang.blaze3d.systems.RenderSystem
import dev.engine_room.flywheel.lib.model.baked.PartialModel
import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import io.github.qwer854645.createresonance.config.ModConfigs
import io.github.qwer854645.createresonance.foundation.locale.ModLang
import io.github.qwer854645.createresonance.foundation.registry.ModPartialModels
import net.createmod.catnip.config.ui.BaseConfigScreen
import net.createmod.catnip.data.Iterate
import net.createmod.catnip.gui.AbstractSimiScreen
import net.createmod.catnip.gui.ILightingSettings
import net.createmod.catnip.gui.element.BoxElement
import net.createmod.catnip.gui.element.GuiGameElement
import net.createmod.catnip.theme.Color
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.renderer.PanoramaRenderer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

/**
 * Create: Resonance hub screen (library + mod settings).
 *
 * FancyMenu:
 * - Screen id: `io.github.qwer854645.createresonance.gui.ResonanceMenuScreen`
 * - No-arg constructible for FancyMenu "Open Screen" actions
 * - Blank canvas: set client `renderMainMenuDecorations=false` (no panorama / logo / discs / title)
 * - Widget messages (stable translation keys):
 *   - create_resonance.gui.main_menu.library_management_btn
 *   - create_resonance.gui.main_menu.mod_settings_btn
 *   - create_resonance.gui.main_menu.go_back_btn
 */
class ResonanceMenuScreen
    @JvmOverloads
    constructor(
        private val parent: Screen? = Minecraft.getInstance().screen,
    ) : AbstractSimiScreen() {
        private val buttonWidth = 200
        private val buttonHeight = 20

        private val panoramaOverlay =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/title/background/panorama_overlay.png")
        private var vanillaPanorama: PanoramaRenderer = PanoramaRenderer(CUBE_MAP)
        private var firstRenderTime: Long = 0L
        private var returnOnClose = true

        val logoTexture = ModGuiTexture("logo_small", 0, 0, 256, 256)
        val recordModel: PartialModel = ModPartialModels.getRecordModel()

        override fun init() {
            super.init()
            returnOnClose = true
            addButtons()
        }

        private fun addButtons() {
            val yStart = height / 4 + 40
            val center = width / 2

            addRenderableWidget(
                Button
                    .builder(
                        ModLang
                            .translate("gui.main_menu.library_management_btn")
                            .component()
                            .withStyle(ChatFormatting.AQUA),
                    ) {
                        linkTo(LibraryDisclaimerScreen(this))
                    }.bounds(center - buttonWidth / 2, yStart + 24, buttonWidth, buttonHeight)
                    .build(),
            )

            addRenderableWidget(
                Button
                    .builder(
                        ModLang.translate("gui.main_menu.mod_settings_btn").component().withStyle(ChatFormatting.YELLOW),
                    ) {
                        linkTo(BaseConfigScreen(this, MOD_ID))
                    }.bounds(center - buttonWidth / 2, yStart + 48, buttonWidth, buttonHeight)
                    .build(),
            )

            addRenderableWidget(
                Button
                    .builder(ModLang.translate("gui.main_menu.go_back_btn").component()) {
                        linkTo(resolveParent())
                    }.bounds(center - buttonWidth / 2, yStart + 72, buttonWidth, buttonHeight)
                    .build(),
            )
        }

        override fun render(
            graphics: GuiGraphics,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float,
        ) {
            if (firstRenderTime == 0L) {
                firstRenderTime = Util.getMillis()
            }
            super.render(graphics, mouseX, mouseY, partialTicks)
        }

        override fun renderWindow(
            graphics: GuiGraphics,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float,
        ) {
            // Blank canvas for FancyMenu layouts / Custom GUI overrides.
            if (!ModConfigs.client.renderMainMenuDecorations.get()) {
                return
            }

            val f = (Util.getMillis() - firstRenderTime) / 1000.0f
            val alpha = Mth.clamp(f, 0.0f, 1.0f)

            vanillaPanorama.render(graphics, width, height, alpha, partialTicks)
            RenderSystem.enableBlend()
            graphics.blit(panoramaOverlay, 0, 0, width, height, 0.0f, 0.0f, 16, 128, 16, 128)

            RenderSystem.enableDepthTest()
            val ms = graphics.pose()

            for (side in Iterate.positiveAndNegative) {
                ms.pushPose()
                ms.translate(width / 2.0, 60.0, 200.0)
                ms.scale(2 * 24f * side, 2 * 24f * side, 32f)
                ms.translate(-1.25 * ((alpha * alpha) / 2f + .5f), 0.25, 0.0)
                GuiGameElement
                    .of(recordModel)
                    .rotateBlock(0.0, 0.0, Util.getMillis() / 32.0)
                    .lighting(ILightingSettings.DEFAULT_FLAT)
                    .render(graphics)
                ms.translate(-.7, 0.0, -1.0)
                GuiGameElement
                    .of(recordModel)
                    .rotateBlock(0.0, 0.0, Util.getMillis() / -16.0)
                    .lighting(ILightingSettings.DEFAULT_FLAT)
                    .render(graphics)
                ms.popPose()
            }

            RenderSystem.enableBlend()
            ms.pushPose()
            ms.translate(width / 2.0 - 32, 32.0, -10.0)
            ms.pushPose()
            ms.scale(0.25f, 0.25f, 0.25f)
            logoTexture.render(graphics, 0, 0)
            ms.popPose()
            BoxElement()
                .apply {
                    withBackground<BoxElement>(-0x78000000)
                    flatBorder<BoxElement>(Color(0x01000000))
                    at<BoxElement>(-32f, 56f, 100f)
                    withBounds<BoxElement>(128, 11)
                }.render(graphics)
            ms.popPose()

            ms.pushPose()
            ms.translate(0.0, 0.0, 500.0)
            graphics.drawCenteredString(
                font,
                Component
                    .literal("Create: ")
                    .withStyle(ChatFormatting.BOLD)
                    .append(
                        Component.literal("Resonance").withStyle { it.withColor(TextColor.fromRgb(0xFE4597)) },
                    ),
                width / 2,
                89,
                -0x1b4499,
            )
            graphics.drawCenteredString(
                font,
                ModLang.translate("mod_fork_subtitle").component().withStyle(ChatFormatting.GRAY),
                width / 2,
                102,
                -0x1b4499,
            )
            ms.popPose()
            RenderSystem.disableDepthTest()
        }

        override fun renderWindowForeground(
            graphics: GuiGraphics,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float,
        ) {
            super.renderWindowForeground(graphics, mouseX, mouseY, partialTicks)
            // Do not re-render widgets here — FancyMenu overlays / layout transforms own that pass.
        }

        private fun resolveParent(): Screen = parent ?: TitleScreen()

        private fun linkTo(screen: Screen?) {
            returnOnClose = false
            minecraft?.setScreen(screen)
        }

        override fun onClose() {
            if (returnOnClose) {
                minecraft?.setScreen(resolveParent())
            }
        }

        override fun isPauseScreen(): Boolean = true
    }
