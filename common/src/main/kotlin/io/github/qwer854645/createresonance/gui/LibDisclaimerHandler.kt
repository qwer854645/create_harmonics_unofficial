package io.github.qwer854645.createresonance.gui

import io.github.qwer854645.createresonance.audio.bin.FFMPEGProvider
import io.github.qwer854645.createresonance.audio.bin.YTDLProvider
import io.github.qwer854645.createresonance.config.ModConfigs
import io.github.qwer854645.createresonance.foundation.eventbus.EventBus
import io.github.qwer854645.createresonance.foundation.eventbus.ProxyEvent
import io.github.qwer854645.createresonance.foundation.eventbus.TickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen

object LibDisclaimerHandler : CommonGuiEventHandler {
    private var hasShownDisclaimer = false
    private var hasChecked = false

    override fun setupEvents() {
        EventBus.onMcMain<TickEvents.ClientTickEvent> { event ->
            if (event.phase != TickEvents.Phase.END) return@onMcMain
            if (hasChecked) return@onMcMain

            val minecraft = Minecraft.getInstance()
            val currentScreen = minecraft.screen

            if (currentScreen is TitleScreen) {
                hasChecked = true

                // Only show once per game session
                if (hasShownDisclaimer) {
                    return@onMcMain
                }

                // Check if user has disabled the disclaimer
                if (ModConfigs.client.neverShowLibraryDisclaimer.get()) {
                    hasShownDisclaimer = true
                    return@onMcMain
                }

                // Check if libraries are already installed
                val ytdlInstalled = YTDLProvider.isAvailable()
                val ffmpegInstalled = FFMPEGProvider.isAvailable()

                if (ytdlInstalled && ffmpegInstalled) {
                    hasShownDisclaimer = true
                    return@onMcMain
                }

                hasShownDisclaimer = true

                minecraft.setScreen(LibraryDisclaimerScreen(currentScreen))
            }
        }
    }
}
