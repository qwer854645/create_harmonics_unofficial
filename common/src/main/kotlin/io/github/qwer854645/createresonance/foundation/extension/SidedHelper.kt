package io.github.qwer854645.createresonance.foundation.extension

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

inline fun BlockEntity.onClient(block: () -> Unit) {
    level?.takeIf { it.isClientSide }?.let { block() }
}

inline fun BlockEntity.onServer(block: () -> Unit) {
    level?.takeIf { !it.isClientSide }?.let { block() }
}

inline fun Level.onServer(block: (level: ServerLevel) -> Unit) {
    if (!isClientSide) {
        block(this as ServerLevel)
    }
}

/**
 * Client-side callback. Uses [Level] (not ClientLevel/Minecraft) so common classes stay
 * dedicated-server safe. [virtual] is true for ponder / virtual render worlds.
 */
inline fun Level.onClient(block: (level: Level, virtual: Boolean) -> Unit) {
    if (!isClientSide) return
    val name = this::class.java.name
    val virtual = name.contains("PonderLevel") || name.contains("VirtualRenderWorld")
    block(this, virtual)
}
