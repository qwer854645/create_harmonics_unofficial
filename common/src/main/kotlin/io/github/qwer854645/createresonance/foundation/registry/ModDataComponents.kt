package io.github.qwer854645.createresonance.foundation.registry

import com.mojang.serialization.Codec
import com.simibubi.create.foundation.data.CreateRegistrate
import com.tterrag.registrate.util.entry.RegistryEntry
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.content.midi.MidiData
import io.github.qwer854645.createresonance.foundation.info
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries

object ModDataComponents : CommonRegistry {
    val RECORD_URL = ModRegistrate.dataComponent("record_url", Codec.STRING)
    val CRAFTED_WITH = ModRegistrate.dataComponent("crafted_with", Codec.STRING)

    /** Immutable MIDI payload on score items (avoids UTF limits + ByteArray component ban). */
    val MIDI_BYTES: RegistryEntry<DataComponentType<*>, DataComponentType<MidiData>> =
        ModRegistrate.simple("midi_bytes", Registries.DATA_COMPONENT_TYPE) {
            DataComponentType
                .builder<MidiData>()
                .persistent(MidiData.CODEC)
                .networkSynchronized(MidiData.STREAM_CODEC)
                .build()
        }

    val MIDI_NAME = ModRegistrate.dataComponent("midi_name", Codec.STRING)

    override fun register(registry: Registry<*>?) {
        "Registering data components..".info()
    }

    inline fun <reified T> CreateRegistrate.dataComponent(
        name: String,
        codec: Codec<T>,
    ): RegistryEntry<DataComponentType<*>, DataComponentType<T>> =
        this.simple(name, Registries.DATA_COMPONENT_TYPE) {
            DataComponentType
                .builder<T>()
                .persistent(codec)
                .build()
        }
}
