package io.github.qwer854645.createresonance.config

import net.createmod.catnip.config.ConfigBase
import net.neoforged.neoforge.common.ModConfigSpec

/**
 * Server-side configuration for Create: Resonance.
 */
object ServerConfig : ConfigBase() {
    val modStress = nested(0, { ModStressConfig }, "Mechanical stress impacts and capacities for Webdisc blocks")

    lateinit var maxJukeboxSoundRange: ConfigInt
        private set

    private fun jukeboxesGroup() {
        group(1, "jukeboxes", "Configuration for the Andesite Jukebox block")

        maxJukeboxSoundRange =
            i(
                64,
                5,
                4095,
                "maxSoundRange",
                "Maximum sound range (in blocks) for the record player / Andesite Jukebox.",
            )
    }

    override fun registerAll(builder: ModConfigSpec.Builder) {
        jukeboxesGroup()
        super.registerAll(builder)
    }

    override fun getName(): String = "server"
}
