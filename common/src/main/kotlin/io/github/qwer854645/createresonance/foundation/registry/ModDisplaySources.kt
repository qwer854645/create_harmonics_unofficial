package io.github.qwer854645.createresonance.foundation.registry

import com.simibubi.create.api.behaviour.display.DisplaySource
import com.tterrag.registrate.util.entry.RegistryEntry
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.displaySource.AudioNameDisplaySource
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.displaySource.PlayerStatusDisplaySource
import io.github.qwer854645.createresonance.foundation.info
import net.minecraft.core.Registry

object ModDisplaySources : CommonRegistry {
    override val registrationOrder = 1

    val AUDIO_NAME: RegistryEntry<DisplaySource, AudioNameDisplaySource> =
        ModRegistrate
            .displaySource("audio_name", ::AudioNameDisplaySource)
            .register()

    val PLAYER_STATUS: RegistryEntry<DisplaySource, PlayerStatusDisplaySource> =
        ModRegistrate
            .displaySource("record_player_status", ::PlayerStatusDisplaySource)
            .register()

    override fun register(registry: Registry<*>?) {
        "Registering display sources".info()
    }
}
