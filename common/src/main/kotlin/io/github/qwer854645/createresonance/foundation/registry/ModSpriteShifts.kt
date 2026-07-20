package io.github.qwer854645.createresonance.foundation.registry

import io.github.qwer854645.createresonance.foundation.extension.asResource
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.services.PlatformService
import net.createmod.catnip.render.SpriteShiftEntry
import net.createmod.catnip.render.SpriteShifter
import net.minecraft.core.Registry

@RegistrableEnv(PlatformService.Environment.CLIENT)
object ModSpriteShifts : CommonRegistry {
    override val targetEnvironment: PlatformService.Environment
        get() = PlatformService.Environment.CLIENT
    val MUSIC_BOX_TAPE: SpriteShiftEntry =
        SpriteShifter.get(
            "block/kinetic_music_box/tape".asResource(),
            "block/kinetic_music_box/tape_scroll".asResource(),
        )

    override fun register(registry: Registry<*>?) {
        "Registering sprite shifts".info()
    }
}
