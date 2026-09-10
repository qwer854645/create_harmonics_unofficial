package io.github.qwer854645.createresonance.audio.midi

import io.github.qwer854645.createresonance.CreateResonanceMod
import io.github.qwer854645.createresonance.content.midi.MidiLimits
import io.github.qwer854645.createresonance.foundation.info
import io.github.qwer854645.createresonance.foundation.warn
import net.minecraft.client.Minecraft
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.midi.MetaMessage
import javax.sound.midi.MidiChannel
import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
import javax.sound.midi.Sequence
import javax.sound.midi.Sequencer
import javax.sound.midi.ShortMessage
import javax.sound.midi.Soundbank
import javax.sound.midi.Synthesizer
import kotlin.math.abs

/**
 * Client-side MIDI playback with channel filtering, forced GM instrument, and SoundFont loading.
 */
object MidiEngine {
    const val MAX_MIDI_BYTES = MidiLimits.MAX_MIDI_BYTES

    /** Melodic sink channel used when a single instrument is forced from the GUI. */
    const val FORCED_SINK_CHANNEL = 0

    /** GM percussion channel — must not receive melodic program changes. */
    const val DRUM_CHANNEL = 9

    private val players = ConcurrentHashMap<String, MidiPlayerInstance>()
    private var sharedSoundbank: Soundbank? = null
    private val soundbankTried = AtomicBoolean(false)

    fun soundfontDir(): File {
        val gameDir = Minecraft.getInstance().gameDirectory
        return File(gameDir, "config/${CreateResonanceMod.MOD_ID}/soundfonts").also { it.mkdirs() }
    }

    /** Same layout as Create's `schematics/` — folder at the game root. */
    fun importDir(): File {
        val gameDir = Minecraft.getInstance().gameDirectory
        return File(gameDir, "midi").also { it.mkdirs() }
    }

    fun ensureSoundbank() {
        if (!soundbankTried.compareAndSet(false, true)) return
        val dir = soundfontDir()
        val sf2 =
            dir.listFiles()?.firstOrNull {
                it.isFile && (it.name.endsWith(".sf2", true) || it.name.endsWith(".sf3", true))
            }
        if (sf2 != null) {
            try {
                sharedSoundbank = MidiSystem.getSoundbank(sf2)
            } catch (e: Exception) {
                "Failed to load SoundFont ${sf2.name}: ${e.message}".warn()
            }
        }
    }

    fun getOrCreate(id: String): MidiPlayerInstance =
        players.getOrPut(id) {
            ensureSoundbank()
            MidiPlayerInstance(id, sharedSoundbank)
        }

    fun release(id: String) {
        players.remove(id)?.close()
    }

    /** Silence without creating a player — used when a paused sync packet arrives. */
    fun pauseIfPresent(id: String) {
        players[id]?.pause()
    }

    /** True while any music-box / conductor MIDI instance is emitting audio. */
    fun anyAudiblyPlaying(): Boolean =
        players.values.any { it.playing && !it.muted }

    fun listImportFiles(): List<File> =
        importDir()
            .listFiles()
            ?.filter { it.isFile && (it.extension.equals("mid", true) || it.extension.equals("midi", true)) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
}

class MidiPlayerInstance(
    val id: String,
    private val soundbank: Soundbank?,
) {
    private var synthesizer: Synthesizer? = null
    /** Extra SoftSynth banks when a single device cannot raise polyphony past Gervill's 64. */
    private var auxiliarySynthesizers: List<Synthesizer> = emptyList()
    private var sequencer: Sequencer? = null
    private var sequence: Sequence? = null

    private fun allSynthesizers(): List<Synthesizer> = listOfNotNull(synthesizer) + auxiliarySynthesizers

    @Volatile
    var allowedChannels: Set<Int>? = null // null = all channels
        private set

    /** When set, all melodic notes play with this GM program on [MidiEngine.FORCED_SINK_CHANNEL]. */
    @Volatile
    var forcedProgram: Int? = null
        private set

    @Volatile
    var playing: Boolean = false
        private set

    /** When true, sequencer advances for UI progress but no audio is emitted. */
    @Volatile
    var muted: Boolean = false
        private set

    @Volatile
    var durationSeconds: Double = 0.0
        private set

    fun load(midiBytes: ByteArray): Boolean {
        stop()
        return try {
            val seq = MidiSystem.getSequence(ByteArrayInputStream(midiBytes))
            sequence = seq
            durationSeconds = seq.microsecondLength / 1_000_000.0
            true
        } catch (e: Exception) {
            "Invalid MIDI for $id: ${e.message}".warn()
            sequence = null
            durationSeconds = 0.0
            false
        }
    }

    fun setChannelFilter(channels: Set<Int>?) {
        val next = channels?.let { HashSet(it) }
        // Content equality — callers often allocate a fresh set each tick.
        if (allowedChannels == next) return
        // Do not stop/restart the sequencer: that desyncs ensemble sections from the conductor.
        // Clearing sounding voices is enough when the part filter changes mid-play.
        allowedChannels = next
        softAllNotesOff()
    }

    fun setForcedProgram(program: Int?) {
        val next = program?.let { GeneralMidiInstruments.clamp(it) }
        if (forcedProgram == next) return
        forcedProgram = next
        softAllNotesOff()
        next?.let { applyForcedProgram(it) }
    }

    fun play(fromSeconds: Double = 0.0) {
        val seq = sequence ?: return
        try {
            ensureDevices()
            val sequ = sequencer ?: return
            sequ.sequence = seq
            val micros = (fromSeconds.coerceAtLeast(0.0) * 1_000_000).toLong().coerceAtMost(seq.microsecondLength)
            sequ.microsecondPosition = micros
            hardSilence()
            forcedProgram?.let { applyForcedProgram(it) }
            sequ.start()
            playing = true
        } catch (e: Exception) {
            "MIDI play failed: ${e.message}".warn()
            playing = false
        }
    }

    fun pause() {
        try {
            sequencer?.stop()
        } catch (_: Exception) {
        }
        playing = false
        hardSilence()
    }

    fun stop() {
        try {
            sequencer?.stop()
            sequencer?.microsecondPosition = 0
        } catch (_: Exception) {
        }
        playing = false
        hardSilence()
    }

    fun seek(seconds: Double) {
        val seq = sequence ?: return
        val wasPlaying = playing
        try {
            ensureDevices()
            val sequ = sequencer ?: return
            if (sequ.sequence !== seq) sequ.sequence = seq
            val micros = (seconds.coerceAtLeast(0.0) * 1_000_000).toLong().coerceAtMost(seq.microsecondLength)
            if (wasPlaying) {
                try {
                    sequ.stop()
                } catch (_: Exception) {
                }
            }
            hardSilence()
            sequ.microsecondPosition = micros
            forcedProgram?.let { applyForcedProgram(it) }
            if (wasPlaying) {
                sequ.start()
                playing = true
            }
        } catch (e: Exception) {
            "MIDI seek failed: ${e.message}".warn()
        }
    }

    /**
     * Lightweight position align for ensemble soft-sync.
     * Avoids stop/start so complex MIDIs do not glitch or drift apart while correcting.
     */
    fun syncPosition(seconds: Double) {
        val seq = sequence ?: return
        try {
            ensureDevices()
            val sequ = sequencer ?: return
            if (sequ.sequence !== seq) {
                sequ.sequence = seq
            }
            val micros = (seconds.coerceAtLeast(0.0) * 1_000_000).toLong().coerceAtMost(seq.microsecondLength)
            val current =
                try {
                    sequ.microsecondPosition
                } catch (_: Exception) {
                    Long.MIN_VALUE / 2
                }
            // Sub-frame nudges only skip MIDI events (lost note-offs → swallowed notes / stuck voices).
            if (abs(micros - current) < 40_000L) return // <40ms
            // Clear sounding voices before the jump so stolen polyphony and hangs do not accumulate.
            softAllNotesOff()
            sequ.microsecondPosition = micros
        } catch (e: Exception) {
            // Fallback to full seek if the sequencer rejects live position updates.
            seek(seconds)
        }
    }

    /** All-notes-off without stopping the sequencer (used before soft position jumps). */
    private fun softAllNotesOff() {
        try {
            allSynthesizers().forEach { synth ->
                synth.channels?.forEach { ch ->
                    try {
                        ch.controlChange(123, 0)
                        ch.allNotesOff()
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun setTempoFactor(factor: Float) {
        try {
            sequencer?.tempoFactor = factor.coerceIn(0.25f, 4.0f)
        } catch (_: Exception) {
        }
    }

    fun setMuted(value: Boolean) {
        if (muted == value) return
        muted = value
        if (value) hardSilence()
    }

    fun currentSeconds(): Double =
        try {
            (sequencer?.microsecondPosition ?: 0L) / 1_000_000.0
        } catch (_: Exception) {
            0.0
        }

    fun isSequencerRunning(): Boolean =
        try {
            sequencer?.isRunning == true
        } catch (_: Exception) {
            false
        }

    /**
     * True when playback was active but the sequencer has finished the sequence.
     * Also clears the stale [playing] flag so callers can stop the block entity.
     */
    fun pollNaturalEnd(): Boolean {
        if (!playing) return false
        // Unknown / unloaded length must never count as "finished".
        if (durationSeconds < 0.5) return false
        val sequ = sequencer ?: return false
        return try {
            if (sequ.isRunning) return false
            // Require actually reaching the end — not a sequencer that never started.
            val pos = currentSeconds()
            if (pos < durationSeconds * 0.9 && pos < durationSeconds - 0.25) return false
            playing = false
            hardSilence()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun close() {
        stop()
        try {
            sequencer?.close()
        } catch (_: Exception) {
        }
        try {
            // Silence again after closing the sequencer — SoftSynth can emit a pop if
            // voices are still decaying when the audio line is torn down mid-note.
            hardSilence()
            for (synth in allSynthesizers()) {
                try {
                    synth.close()
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }
        sequencer = null
        synthesizer = null
        auxiliarySynthesizers = emptyList()
    }

    private fun ensureDevices() {
        if (synthesizer?.isOpen == true && sequencer?.isOpen == true) return
        close()
        val synth = MidiSystem.getSynthesizer()
        SoftSynthPolyphony.openPrimary(synth, MAX_POLYPHONY)
        soundbank?.let {
            try {
                synth.loadAllInstruments(it)
            } catch (_: Exception) {
            }
        }

        val sinkReceiver: Receiver =
            if (synth.maxPolyphony < SoftSynthPolyphony.MIN_ACCEPTABLE_POLYPHONY) {
                warnPolyphonyFallback(synth.maxPolyphony)
                val (banks, fanout) = SoftSynthPolyphony.openPolyphonyBanks(synth, soundbank, MAX_POLYPHONY)
                auxiliarySynthesizers = banks.drop(1)
                fanout
            } else {
                auxiliarySynthesizers = emptyList()
                "MIDI SoftSynth ready polyphony=${synth.maxPolyphony}".info()
                synth.receiver
            }

        val sequ = MidiSystem.getSequencer(false)
        sequ.open()
        sequ.transmitter.receiver =
            FilteringReceiver(
                sinkReceiver,
                { allowedChannels },
                { forcedProgram },
                { muted },
            )
        synthesizer = synth
        sequencer = sequ
    }

    companion object {
        /** SoftSynthesizer default is 64; 1024 covers dense / multi-box MIDI with headroom. */
        const val MAX_POLYPHONY = SoftSynthPolyphony.TARGET_POLYPHONY
    }

    /** Apply the forced GM program only on the melodic sink channel. */
    private fun applyForcedProgram(program: Int) {
        val ch = MidiEngine.FORCED_SINK_CHANNEL
        for (synth in allSynthesizers()) {
            try {
                val midiCh = synth.channels.getOrNull(ch) ?: continue
                midiCh.controlChange(64, 0)
                midiCh.controlChange(121, 0) // reset all controllers
                midiCh.pitchBend = 8192
                midiCh.programChange(program)
            } catch (_: Exception) {
            }
        }
    }

    private fun hardSilence() {
        try {
            allSynthesizers().forEach { synth ->
                synth.channels?.forEach { ch: MidiChannel ->
                    try {
                        ch.controlChange(64, 0) // sustain off
                        ch.controlChange(123, 0) // all notes off CC
                        ch.controlChange(120, 0) // all sound off CC
                        ch.controlChange(121, 0) // reset all controllers
                        ch.pitchBend = 8192
                    } catch (_: Exception) {
                    }
                    try {
                        ch.allNotesOff()
                        ch.allSoundOff()
                    } catch (_: Exception) {
                    }
                    // SoftSynthesizer often keeps voices after CC all-sound-off when stopped mid-note.
                    try {
                        for (note in 0..127) {
                            ch.noteOff(note)
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
        // Also push through the sequencer receiver path (respects mute/filter / fan-out wiring).
        try {
            val receiver = sequencer?.transmitter?.receiver ?: synthesizer?.receiver ?: return
            for (ch in 0..15) {
                receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 64, 0), -1)
                receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 123, 0), -1)
                receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 120, 0), -1)
                receiver.send(ShortMessage(ShortMessage.CONTROL_CHANGE, ch, 121, 0), -1)
            }
        } catch (_: Exception) {
        }
    }
}

/**
 * When [forcedProgram] is set:
 * - Drop percussion (channel 9) — drum keys as melodic notes sound wrong.
 * - Remap every other channel onto [MidiEngine.FORCED_SINK_CHANNEL].
 * - Ignore file Program Change / bank select.
 */
private class FilteringReceiver(
    private val delegate: Receiver,
    private val allowed: () -> Set<Int>?,
    private val forcedProgram: () -> Int?,
    private val muted: () -> Boolean,
) : Receiver {
    override fun send(
        message: javax.sound.midi.MidiMessage,
        timeStamp: Long,
    ) {
        if (message is MetaMessage) {
            // Keep meta (tempo/end-of-track) flowing so the sequencer stays consistent.
            delegate.send(message, timeStamp)
            return
        }
        if (muted()) return
        if (message !is ShortMessage) {
            delegate.send(message, timeStamp)
            return
        }

        val filter = allowed()
        val cmd = message.command
        val channel = message.channel

        if (filter != null &&
            cmd != ShortMessage.SYSTEM_RESET &&
            cmd != ShortMessage.TIMING_CLOCK &&
            channel !in filter
        ) {
            return
        }

        val program = forcedProgram()
        if (program != null) {
            // System realtime / reset — pass through unchanged.
            if (cmd >= 0xF0) {
                delegate.send(message, timeStamp)
                return
            }

            // Percussion channel has no melodic program — skipping avoids kit glitches.
            if (channel == MidiEngine.DRUM_CHANNEL) return

            // File-driven bank/program changes fight the GUI instrument.
            if (cmd == ShortMessage.PROGRAM_CHANGE) return
            if (cmd == ShortMessage.CONTROL_CHANGE && (message.data1 == 0 || message.data1 == 32)) return

            val remapped =
                if (channel == MidiEngine.FORCED_SINK_CHANNEL) {
                    message
                } else {
                    try {
                        ShortMessage(cmd, MidiEngine.FORCED_SINK_CHANNEL, message.data1, message.data2)
                    } catch (_: Exception) {
                        return
                    }
                }
            delegate.send(remapped, timeStamp)
            return
        }

        delegate.send(message, timeStamp)
    }

    override fun close() {
        delegate.close()
    }
}

fun rpmToTempoFactor(rpm: Float): Float {
    val absRpm = abs(rpm)
    // Solo / conductor: 64 RPM = 1.0x tempo. Section boxes use the conductor RPM instead.
    return (absRpm / 64f).coerceIn(0.25f, 4.0f)
}
