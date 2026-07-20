package io.github.qwer854645.createresonance

import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import io.github.qwer854645.createresonance.config.ModConfigs
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBlockEntity
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseBlockEntity
import io.github.qwer854645.createresonance.data.DataGenerators.provideLang
import io.github.qwer854645.createresonance.foundation.registry.ModBlockEntities
import io.github.qwer854645.createresonance.foundation.registry.NeoforgeModPackets
import io.github.qwer854645.createresonance.foundation.registry.NeoforgeRegistry
import io.github.qwer854645.createresonance.foundation.registry.autoRegister
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.RegisterEvent

@Mod(MOD_ID)
class NeoforgeModEntryPoint(
    val modEventBus: IEventBus,
) {
    companion object {
        @JvmStatic
        lateinit var instance: NeoforgeModEntryPoint
            private set

        fun onRegister(event: RegisterEvent) {
            CreateResonanceMod.commonPreFreezeSetup(event.registry)
        }
    }

    init {
        instance = this
        initialize()
    }

    @EventBusSubscriber(modid = MOD_ID)
    object ModSetup {
        @JvmStatic
        @SubscribeEvent
        fun registerCapabilities(event: RegisterCapabilitiesEvent) {
            event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.KINETIC_NETWORK_JUKEBOX.get(),
            ) { be: RecordPlayerBlockEntity, _ -> be.itemHandler }

            event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.RECORD_PRESS_BASE.get(),
            ) { be: RecordPressBaseBlockEntity, _ -> be.behaviour.itemHandler }

            event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MIDI_TABLE.get(),
            ) { be: io.github.qwer854645.createresonance.content.midi.MidiTableBlockEntity, _ ->
                be.behaviour.itemHandler
            }

            event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.KINETIC_MUSIC_BOX.get(),
            ) { be: io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxBlockEntity, _ ->
                be.inventory
            }
            event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MUSIC_CONDUCTOR.get(),
            ) { be: io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxBlockEntity, _ ->
                be.inventory
            }
        }
    }

    private fun initialize() {
        ModEventBus.addListener(NeoforgeModEntryPoint::onRegister)

        CreateResonanceMod.commonSetup {
            registerEventListeners(this@NeoforgeModEntryPoint.modEventBus)
        }

        provideLang()

        autoRegister<NeoforgeRegistry>()
        ModEventBus.addListener(NeoforgeModPackets::registerPayloads)
    }
}

internal val ModEventBus: IEventBus = NeoforgeModEntryPoint.instance.modEventBus
