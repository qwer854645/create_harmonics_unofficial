package io.github.qwer854645.createresonance.audio.instance

import io.github.qwer854645.createresonance.audio.player.AudioPlayer
import io.github.qwer854645.createresonance.foundation.supplier.values.FloatSupplier
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import org.joml.Vector3d

class SimpleTickableSoundInstance(
    audioPlayer: AudioPlayer,
    soundEvent: SoundEvent,
    soundSource: SoundSource,
    randomSource: RandomSource,
    looping: Boolean,
    delay: Int,
    attenuation: SoundInstance.Attenuation,
    relative: Boolean,
    needStream: Boolean,
) : AudioPlayerSoundInstance(
        audioPlayer,
        soundEvent,
        soundSource,
        randomSource,
        needStream,
    ) {
    init {
        this.looping = looping
        this.delay = delay
        this.attenuation = attenuation
        this.relative = relative
    }
}
