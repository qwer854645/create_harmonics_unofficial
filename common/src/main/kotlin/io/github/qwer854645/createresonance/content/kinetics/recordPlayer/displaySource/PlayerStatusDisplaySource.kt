package io.github.qwer854645.createresonance.content.kinetics.recordPlayer.displaySource

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.PlaybackState
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerBehaviour
import io.github.qwer854645.createresonance.foundation.locale.ModLang
import net.minecraft.network.chat.MutableComponent

class PlayerStatusDisplaySource : SingleLineDisplaySource() {
    override fun provideLine(
        context: DisplayLinkContext,
        stats: DisplayTargetStats,
    ): MutableComponent {
        val smartBe = context.sourceBlockEntity as? SmartBlockEntity ?: return EMPTY_LINE
        val audioPlayerBehaviour = smartBe.getBehaviour(RecordPlayerBehaviour.BEHAVIOUR_TYPE) ?: return EMPTY_LINE

        val stateComponent =
            when (audioPlayerBehaviour.playbackState) {
                PlaybackState.PLAYING -> {
                    ModLang.translate("display_source.record_player.playing").component()
                }

                PlaybackState.PAUSED -> {
                    ModLang.translate("display_source.record_player.paused").component()
                }

                PlaybackState.STOPPED -> {
                    ModLang.translate("display_source.record_player.stopped").component()
                }
            }

        if (audioPlayerBehaviour.currentlyLooping) {
            stateComponent.append(" \uD83D\uDD01")
        }

        return stateComponent
    }

    override fun getTranslationKey(): String = "read_record_player_state"

    override fun allowsLabeling(context: DisplayLinkContext): Boolean = true
}
