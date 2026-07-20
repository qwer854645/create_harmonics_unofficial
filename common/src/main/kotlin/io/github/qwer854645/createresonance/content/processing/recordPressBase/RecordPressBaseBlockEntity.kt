package io.github.qwer854645.createresonance.content.processing.recordPressBase

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import io.github.qwer854645.createresonance.foundation.goggles.ResonanceGoggles
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.Clearable
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class RecordPressBaseBlockEntity(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
) : SmartBlockEntity(type, pos, state),
    Clearable,
    IHaveGoggleInformation {
    lateinit var behaviour: RecordPressBaseBehaviour
        private set

    override fun addBehaviours(behaviours: MutableList<BlockEntityBehaviour>) {
        behaviour = RecordPressBaseBehaviour(this)
        behaviours.add(behaviour)
        behaviour.addSubBehaviours(behaviours)
    }

    var currentUrlIndex: Int
        get() = behaviour.currentUrlIndex
        set(value) {
            behaviour.currentUrlIndex = value
            notifyUpdate()
        }

    var urlTemplate: String
        get() = behaviour.audioUrls.firstOrNull() ?: ""
        set(value) {
            if (value.isNotEmpty()) {
                behaviour.audioUrls.clear()
                behaviour.audioUrls.add(value)
            }
            notifyUpdate()
        }

    var audioUrls: MutableList<String>
        get() = behaviour.audioUrls
        set(value) {
            behaviour.audioUrls = value
            notifyUpdate()
        }

    var urlWeights: MutableList<Float>
        get() = behaviour.urlWeights
        set(value) {
            behaviour.urlWeights = value
            notifyUpdate()
        }

    var randomMode: Boolean
        get() = behaviour.randomMode
        set(value) {
            behaviour.randomMode = value
            notifyUpdate()
        }

    override fun addToGoggleTooltip(
        tooltip: MutableList<Component>,
        isPlayerSneaking: Boolean,
    ): Boolean {
        ResonanceGoggles.header(tooltip, "gui.goggles.record_press.header")

        val urlCount = behaviour.audioUrls.count { it.isNotBlank() }
        if (urlCount > 0) {
            ResonanceGoggles.line(tooltip, "gui.goggles.record_press.urls", urlCount)
            ResonanceGoggles.line(
                tooltip,
                if (behaviour.randomMode) {
                    "gui.goggles.record_press.mode.random"
                } else {
                    "gui.goggles.record_press.mode.sequential"
                },
            )
            if (isPlayerSneaking) {
                val active =
                    behaviour.audioUrls
                        .getOrNull(behaviour.currentUrlIndex.coerceIn(0, behaviour.audioUrls.lastIndex.coerceAtLeast(0)))
                        ?.takeIf { it.isNotBlank() }
                if (active != null) {
                    ResonanceGoggles.line(
                        tooltip,
                        "gui.goggles.record_press.active_url",
                        ResonanceGoggles.truncate(active, 36),
                        style = ChatFormatting.GRAY,
                    )
                }
            }
        } else {
            ResonanceGoggles.line(tooltip, "gui.goggles.record_press.no_urls", style = ChatFormatting.DARK_GRAY)
        }

        val held = behaviour.heldItemStack
        if (!held.isEmpty) {
            ResonanceGoggles.line(tooltip, "gui.goggles.record_press.holding", held.hoverName.string)
        } else {
            ResonanceGoggles.line(tooltip, "gui.goggles.record_press.empty", style = ChatFormatting.DARK_GRAY)
        }
        return true
    }

    override fun clearContent() {
        val handler = this.behaviour.itemHandler
        for (i in 0 until handler.slots) {
            handler.extractItem(i, Int.MAX_VALUE, false)
        }

        notifyUpdate()
    }
}
