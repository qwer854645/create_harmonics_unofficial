package io.github.qwer854645.createresonance.content.midi

import net.createmod.catnip.gui.ScreenOpener

object MidiTableClientHandler {
    fun openConfigScreen(be: MidiTableBlockEntity) {
        ScreenOpener.open(MidiTableScreen(be))
    }
}
