package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions
import com.simibubi.create.foundation.gui.AllIcons
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerValueBoxTransform
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState

/** Solo / Section — front value box above the frequency item slots. */
enum class MusicBoxEnsembleMode : INamedIconOptions {
    SOLO(AllIcons.I_PLAY),
    SECTION(AllIcons.I_WHITELIST),
    ;

    private val icon: AllIcons
    private val translationKey: String

    constructor(icon: AllIcons) {
        this.icon = icon
        this.translationKey = "create_resonance.music_box.ensemble_mode." + name.lowercase()
    }

    override fun getIcon(): AllIcons = icon

    override fun getTranslationKey(): String = translationKey

    fun toRole(): MusicBoxRole =
        when (this) {
            SOLO -> MusicBoxRole.SOLO
            SECTION -> MusicBoxRole.SECTION
        }

    companion object {
        fun fromRole(role: MusicBoxRole): MusicBoxEnsembleMode =
            when (role) {
                MusicBoxRole.SECTION -> SECTION
                else -> SOLO
            }

        /** Only on the front face (same face as frequency slots). */
        fun sideSlot(): RecordPlayerValueBoxTransform =
            RecordPlayerValueBoxTransform { blockState: BlockState, direction: Direction ->
                direction == MusicBoxFrequencySlot.frontFace(blockState)
            }
    }
}
