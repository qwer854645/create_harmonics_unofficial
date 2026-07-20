package io.github.qwer854645.createresonance.event.audio

import io.github.qwer854645.createresonance.foundation.eventbus.ModEvent
import java.util.UUID

data class AudioFinishedEvent(
    val playerUUID: UUID,
) : ModEvent

data class AudioSyncEndedEvent(
    val playerUUID: UUID,
) : ModEvent
