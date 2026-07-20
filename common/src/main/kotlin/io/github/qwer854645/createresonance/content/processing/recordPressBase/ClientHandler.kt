package io.github.qwer854645.createresonance.content.processing.recordPressBase

import net.createmod.catnip.gui.ScreenOpener

object ClientHandler {
    fun openRecordPressScreen(be: RecordPressBaseBlockEntity) {
        ScreenOpener.open(RecordPressBaseScreen(be))
    }
}
