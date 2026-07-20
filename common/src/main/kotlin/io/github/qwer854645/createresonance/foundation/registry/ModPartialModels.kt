package io.github.qwer854645.createresonance.foundation.registry

import dev.engine_room.flywheel.lib.model.baked.PartialModel
import io.github.qwer854645.createresonance.foundation.extension.asResource
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.services.PlatformService
import net.minecraft.core.Registry

@RegistrableEnv(PlatformService.Environment.CLIENT)
object ModPartialModels : CommonRegistry {
    override val targetEnvironment: PlatformService.Environment
        get() = PlatformService.Environment.CLIENT
    private val resonance_discVisual: PartialModel = block("resonance_disc_visual/resonance_disc")
    val MUSIC_BOX_TAPE_BELT: PartialModel = block("kinetic_music_box/tape_belt")
    val MUSIC_BOX_ENSEMBLE_LINK: PartialModel = block("kinetic_music_box/ensemble_link")
    val CONDUCTOR_ENSEMBLE_LINK: PartialModel = block("music_conductor/ensemble_link")
    val MIDI_TABLE_STYLUS: PartialModel = block("midi_table/stylus")
    val MIDI_TABLE_SCORE_PAD: PartialModel = block("midi_table/score_pad")

    fun getRecordModel(): PartialModel = resonance_discVisual

    private fun block(path: String): PartialModel = PartialModel.of("block/$path".asResource())

    override fun register(registry: Registry<*>?) {
        "Lazily loading partial models".info()
    }
}
