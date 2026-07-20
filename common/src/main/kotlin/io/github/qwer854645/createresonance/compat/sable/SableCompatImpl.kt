package io.github.qwer854645.createresonance.compat.sable

import dev.ryanhcode.sable.companion.SableCompanion
import io.github.qwer854645.createresonance.foundation.services.platformService
import net.minecraft.world.level.Level
import org.joml.Vector3d

internal object SableCompatImpl : SableCompat {
    override fun projectOutOfSubLevel(
        level: Level,
        currentPosition: Vector3d,
    ) {
        SableCompanion.INSTANCE.projectOutOfSubLevel(
            level,
            currentPosition,
        )
    }

    override fun isInPlotGrid(
        level: Level,
        pos: Vector3d,
    ): Boolean =
        SableCompanion.INSTANCE.isInPlotGrid(
            level,
            pos,
        )
}
