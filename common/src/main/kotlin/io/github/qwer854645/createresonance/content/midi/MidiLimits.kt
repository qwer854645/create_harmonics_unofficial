package io.github.qwer854645.createresonance.content.midi

/**
 * Shared MIDI size limits — kept free of client-only types so dedicated servers can reference them.
 */
object MidiLimits {
    /** Hard cap for stored / networked MIDI payloads (512 KiB). */
    const val MAX_MIDI_BYTES = 512 * 1024
}
