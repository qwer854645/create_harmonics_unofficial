package io.github.qwer854645.createresonance

import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import io.github.qwer854645.createresonance.config.ModConfigs
import net.createmod.catnip.config.ui.BaseConfigScreen
import net.minecraft.client.gui.screens.Screen
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory

@Mod(CreateResonanceMod.MOD_ID, dist = [Dist.CLIENT])
class NeoforgeModClientEntryPoint(
    val modEventBus: IEventBus,
    val container: ModContainer,
) {
    companion object {
        @JvmStatic
        lateinit var instance: NeoforgeModClientEntryPoint
            private set
    }

    @EventBusSubscriber(value = [Dist.CLIENT], modid = MOD_ID)
    object ClientModSetup {
        @JvmStatic
        @SubscribeEvent
        fun onClientSetup(event: FMLClientSetupEvent) {
            BaseConfigScreen.setDefaultActionFor(MOD_ID) { base ->
                base.withSpecs(
                    ModConfigs.client.specification,
                    ModConfigs.common.specification,
                    ModConfigs.server.specification,
                )
            }
        }

        @JvmStatic
        @SubscribeEvent
        fun onLoadComplete(event: FMLLoadCompleteEvent) {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
                IConfigScreenFactory { _: ModContainer, previousScreen: Screen ->
                    BaseConfigScreen(previousScreen, MOD_ID)
                }
            }
        }
    }

    init {
        instance = this
        initialize()
    }

    private fun initialize() {
        CreateResonanceClientMod.setup()
    }
}
