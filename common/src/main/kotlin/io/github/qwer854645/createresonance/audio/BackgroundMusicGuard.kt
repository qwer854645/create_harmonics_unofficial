package io.github.qwer854645.createresonance.audio

import io.github.qwer854645.createresonance.audio.midi.MidiEngine
import io.github.qwer854645.createresonance.config.ClientConfig
import net.minecraft.client.Minecraft

/**
 * Keeps vanilla situational / creative / biome music from stacking on top of Resonance playback.
 * Our streaming audio uses [net.minecraft.sounds.SoundSource.RECORDS]; MIDI uses SoftSynthesizer —
 * neither goes through the MUSIC bus, so stopping [MusicManager] does not mute our own sound.
 */
object BackgroundMusicGuard {
    fun tick() {
        if (!ClientConfig.muteBackgroundMusicWhilePlaying.get()) return
        if (!AudioPlayerManager.anyActivelyPlaying() && !MidiEngine.anyAudiblyPlaying()) return
        val mc = Minecraft.getInstance()
        if (mc.level == null) return
        mc.musicManager.stopPlaying()
    }
}
