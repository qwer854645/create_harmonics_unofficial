package io.github.qwer854645.createresonance.foundation.registry

import com.tterrag.registrate.util.entry.BlockEntityEntry
import com.tterrag.registrate.util.nullness.NonNullFunction
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerRenderer
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerVisual
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.kineticNetworkJukebox.KineticNetworkJukeboxBlockEntity
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxBlockEntity
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxRenderer
import io.github.qwer854645.createresonance.content.midi.MidiTableBlockEntity
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseBlockEntity
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseRenderer
import io.github.qwer854645.createresonance.foundation.info
import net.minecraft.core.Registry

object ModBlockEntities : CommonRegistry {
    override val registrationOrder = 3

    val KINETIC_NETWORK_JUKEBOX: BlockEntityEntry<KineticNetworkJukeboxBlockEntity> =
        ModRegistrate
            .blockEntity("kinetic_network_jukebox", ::KineticNetworkJukeboxBlockEntity)
            .visual({
                SimpleBlockEntityVisualizer.Factory { ctx, be, pt ->
                    RecordPlayerVisual(ctx, be, pt)
                }
            }, false)
            .validBlocks(ModBlocks.KINETIC_NETWORK_JUKEBOX)
            .renderer {
                NonNullFunction { ctx ->
                    RecordPlayerRenderer(ctx)
                }
            }.register()

    val RECORD_PRESS_BASE: BlockEntityEntry<RecordPressBaseBlockEntity> =
        ModRegistrate
            .blockEntity("resonance_press", ::RecordPressBaseBlockEntity)
            .validBlocks(ModBlocks.RECORD_PRESS_BASE)
            .renderer {
                NonNullFunction { ctx ->
                    RecordPressBaseRenderer(ctx)
                }
            }.register()


    val KINETIC_MUSIC_BOX: BlockEntityEntry<MusicBoxBlockEntity> =
        ModRegistrate
            .blockEntity("kinetic_music_box", ::MusicBoxBlockEntity)
            .validBlocks(ModBlocks.KINETIC_MUSIC_BOX)
            .renderer {
                NonNullFunction { ctx ->
                    MusicBoxRenderer(ctx)
                }
            }.register()

    val MUSIC_CONDUCTOR: BlockEntityEntry<MusicBoxBlockEntity> =
        ModRegistrate
            .blockEntity("music_conductor", ::MusicBoxBlockEntity)
            .validBlocks(ModBlocks.MUSIC_CONDUCTOR)
            .renderer {
                NonNullFunction { ctx ->
                    MusicBoxRenderer(ctx)
                }
            }.register()

    val MIDI_TABLE: BlockEntityEntry<MidiTableBlockEntity> =
        ModRegistrate
            .blockEntity("midi_table", ::MidiTableBlockEntity)
            .validBlocks(ModBlocks.MIDI_TABLE)
            .renderer {
                NonNullFunction { ctx ->
                    io.github.qwer854645.createresonance.content.midi.MidiTableRenderer(ctx)
                }
            }.register()

    override fun register(registry: Registry<*>?) {
        "Registering block entities".info()
    }
}
