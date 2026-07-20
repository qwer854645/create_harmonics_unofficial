package io.github.qwer854645.createresonance.foundation.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag

object BlockPosSerializer : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("net.minecraft.core.BlockPos", PrimitiveKind.LONG)

    override fun serialize(
        encoder: Encoder,
        value: BlockPos,
    ) {
        if (encoder is FriendlyByteBufEncoder) {
            encoder.buf.writeBlockPos(value)
        } else {
            encoder.encodeLong(value.asLong())
        }
    }

    override fun deserialize(decoder: Decoder): BlockPos =
        if (decoder is FriendlyByteBufDecoder) {
            decoder.buf.readBlockPos()
        } else {
            BlockPos.of(decoder.decodeLong())
        }
}

object CompoundTagSerializer : KSerializer<CompoundTag> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("net.minecraft.nbt.CompoundTag", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: CompoundTag,
    ) {
        if (encoder is FriendlyByteBufEncoder) {
            encoder.buf.writeNbt(value)
        } else {
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): CompoundTag =
        if (decoder is FriendlyByteBufDecoder) {
            decoder.buf.readNbt() ?: CompoundTag()
        } else {
            CompoundTag()
        }
}

/** Raw bytes via FriendlyByteBuf — avoids UTF string 32767 limit (MIDI payloads). */
object ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("create_resonance.network.ByteArray")

    override fun serialize(
        encoder: Encoder,
        value: ByteArray,
    ) {
        require(encoder is FriendlyByteBufEncoder) { "ByteArraySerializer requires FriendlyByteBufEncoder" }
        encoder.buf.writeByteArray(value)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        require(decoder is FriendlyByteBufDecoder) { "ByteArraySerializer requires FriendlyByteBufDecoder" }
        return decoder.buf.readByteArray()
    }
}

val MinecraftSerializersModule =
    SerializersModule {
        contextual(BlockPosSerializer)
        contextual(CompoundTagSerializer)
        contextual(ByteArraySerializer)
    }
