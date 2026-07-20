package io.github.qwer854645.createresonance.content.midi

import com.mojang.serialization.Codec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import java.nio.ByteBuffer

/**
 * Immutable MIDI payload for item data components.
 * Plain [ByteArray] is rejected by Minecraft (no content equals/hashCode).
 */
class MidiData private constructor(
    private val data: ByteArray,
) {
    fun toByteArray(): ByteArray = data.copyOf()

    val size: Int get() = data.size

    fun isEmpty(): Boolean = data.isEmpty()

    override fun equals(other: Any?): Boolean =
        other is MidiData && data.contentEquals(other.data)

    override fun hashCode(): Int = data.contentHashCode()

    override fun toString(): String = "MidiData(${data.size} bytes)"

    companion object {
        val CODEC: Codec<MidiData> =
            Codec.BYTE_BUFFER.xmap(
                { buf ->
                    val copy = ByteArray(buf.remaining())
                    buf.duplicate().get(copy)
                    of(copy)
                },
                { midi -> ByteBuffer.wrap(midi.toByteArray()) },
            )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, MidiData> =
            StreamCodec.of(
                { buf, value ->
                    buf.writeVarInt(value.size)
                    buf.writeBytes(value.toByteArray())
                },
                { buf ->
                    val size = buf.readVarInt()
                    val bytes = ByteArray(size)
                    buf.readBytes(bytes)
                    of(bytes)
                },
            )

        fun of(bytes: ByteArray): MidiData {
            require(bytes.size <= MidiLimits.MAX_MIDI_BYTES) { "MIDI too large" }
            return MidiData(bytes.copyOf())
        }

        fun ofNullable(bytes: ByteArray?): MidiData? =
            bytes?.takeIf { it.isNotEmpty() && it.size <= MidiLimits.MAX_MIDI_BYTES }?.let { of(it) }
    }
}
