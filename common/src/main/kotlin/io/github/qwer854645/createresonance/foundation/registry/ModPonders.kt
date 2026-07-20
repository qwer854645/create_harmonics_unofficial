package io.github.qwer854645.createresonance.foundation.registry

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags
import com.tterrag.registrate.util.entry.ItemProviderEntry
import com.tterrag.registrate.util.entry.RegistryEntry
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.services.PlatformService
import io.github.qwer854645.createresonance.ponder.PonderScenes
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

@RegistrableEnv(PlatformService.Environment.CLIENT)
object ModPonders : CommonRegistry {
    override val targetEnvironment: PlatformService.Environment
        get() = PlatformService.Environment.CLIENT

    override val registrationOrder = 5

    fun addTags(rawHelper: PonderTagRegistrationHelper<ResourceLocation>) {
        val helper = rawHelper.withKeyFunction(RegistryEntry<*, *>::getId)

        helper
            .addToTag(
                AllCreatePonderTags.ARM_TARGETS,
                AllCreatePonderTags.DISPLAY_SOURCES,
                AllCreatePonderTags.KINETIC_APPLIANCES,
            ).add(ModBlocks.KINETIC_NETWORK_JUKEBOX)

        helper
            .addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
            .add(ModBlocks.RECORD_PRESS_BASE)
            .add(ModBlocks.MIDI_TABLE)
            .add(ModBlocks.KINETIC_MUSIC_BOX)
            .add(ModBlocks.MUSIC_CONDUCTOR)

        helper
            .addToTag(AllCreatePonderTags.ARM_TARGETS)
            .add(ModBlocks.MIDI_TABLE)
            .add(ModBlocks.KINETIC_MUSIC_BOX)
            .add(ModBlocks.MUSIC_CONDUCTOR)
    }

    fun addScenes(rawHelper: PonderSceneRegistrationHelper<ResourceLocation>) {
        val helper: PonderSceneRegistrationHelper<ItemProviderEntry<*, *>?> =
            rawHelper.withKeyFunction(RegistryEntry<*, *>::getId)

        helper.addStoryBoard(
            ModBlocks.KINETIC_NETWORK_JUKEBOX,
            "kinetic_network_jukebox",
            PonderScenes::kineticNetworkJukebox,
            AllCreatePonderTags.KINETIC_APPLIANCES,
        )

        helper.addStoryBoard(
            ModBlocks.RECORD_PRESS_BASE,
            "resonance_press",
            PonderScenes::recordPressBase,
            AllCreatePonderTags.KINETIC_APPLIANCES,
        )

        helper.addStoryBoard(
            ModBlocks.MIDI_TABLE,
            "midi_table",
            PonderScenes::midiTable,
            AllCreatePonderTags.KINETIC_APPLIANCES,
        )

        helper.addStoryBoard(
            ModBlocks.KINETIC_MUSIC_BOX,
            "kinetic_music_box",
            PonderScenes::kineticMusicBox,
            AllCreatePonderTags.KINETIC_APPLIANCES,
        )

        helper.addStoryBoard(
            ModBlocks.MUSIC_CONDUCTOR,
            "music_conductor",
            PonderScenes::musicConductor,
            AllCreatePonderTags.KINETIC_APPLIANCES,
        )
    }

    override fun register(registry: Registry<*>?) {
        "Lazily loading ponders".info()
    }
}
