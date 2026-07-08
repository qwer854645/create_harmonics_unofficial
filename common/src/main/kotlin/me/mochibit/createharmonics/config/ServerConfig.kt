package me.mochibit.createharmonics.config

import net.createmod.catnip.config.ConfigBase
import net.neoforged.neoforge.common.ModConfigSpec

/**
 * Server-side configuration for CreateHarmonics.
 */
object ServerConfig : ConfigBase() {
    val modStress = nested(0, { ModStressConfig }, "Mechanical stress impacts and capacities for Webdisc blocks")

    lateinit var maxJukeboxSoundRange: ConfigInt
        private set

    private fun jukeboxesGroup() {
        group(1, "jukeboxes", "Configuration for the Andesite Jukebox block")

        maxJukeboxSoundRange =
            i(
                32,
                5,
                4095,
                "maxSoundRange",
                "Maximum sound range (in blocks) for the Andesite Jukebox.",
            )
    }

    override fun registerAll(builder: ModConfigSpec.Builder) {
        jukeboxesGroup()
        super.registerAll(builder)
    }

    override fun getName(): String = "server"
}
