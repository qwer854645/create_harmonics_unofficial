package io.github.qwer854645.createresonance.compat

import io.github.qwer854645.createresonance.compat.sable.SableCompat
import io.github.qwer854645.createresonance.compat.sable.SableCompatImpl
import io.github.qwer854645.createresonance.foundation.services.platformService

internal object ModCompats {
    val sableCompat: SableCompat? by lazy {
        if (platformService.isModLoaded("sable")) {
            SableCompatImpl
        } else {
            null
        }
    }
}
