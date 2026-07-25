package io.github.qwer854645.createresonance.foundation.registry

import com.simibubi.create.AllTags
import com.simibubi.create.api.behaviour.display.DisplaySource.displaySource
import com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType.mountedItemStorage
import com.simibubi.create.foundation.data.BlockStateGen
import com.simibubi.create.foundation.data.ModelGen.customItemModel
import com.simibubi.create.foundation.data.SharedProperties
import com.tterrag.registrate.util.entry.BlockEntry
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.config.ModStressConfig
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.RecordPlayerMovementBehaviour
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.kineticNetworkJukebox.KineticNetworkJukeboxBlock
import io.github.qwer854645.createresonance.content.kinetics.musicBox.KineticMusicBoxBlock
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxBlock
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicConductorBlock
import io.github.qwer854645.createresonance.content.midi.MidiTableBlock
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseBlock
import io.github.qwer854645.createresonance.foundation.info
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.world.level.block.SoundType
import net.neoforged.neoforge.client.model.generators.ConfiguredModel
import net.neoforged.neoforge.client.model.generators.ModelFile
import com.tterrag.registrate.providers.DataGenContext
import com.tterrag.registrate.providers.RegistrateBlockstateProvider
import net.minecraft.world.level.block.Block
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock

object ModBlocks : CommonRegistry {
    override val registrationOrder = 2

    val KINETIC_NETWORK_JUKEBOX: BlockEntry<KineticNetworkJukeboxBlock> =
        ModRegistrate
            .block("kinetic_network_jukebox") { properties ->
                KineticNetworkJukeboxBlock(properties)
            }.initialProperties { SharedProperties.wooden() }
            .properties { p ->
                p
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.WOOD)
            }.onRegister(movementBehaviour(RecordPlayerMovementBehaviour()))
            .lang("Kinetic Network Jukebox")
            .tag(
                AllTags.AllBlockTags.SAFE_NBT.tag,
            ).tag(AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.tag)
            .transform(mountedItemStorage(ModMountedStorages.SIMPLE_RECORD_PLAYER_STORAGE))
            .transform(displaySource(ModDisplaySources.AUDIO_NAME))
            .transform(displaySource(ModDisplaySources.PLAYER_STATUS))
            .transform(ModStressConfig.setImpact(1.0))
            .blockstate { ctx, prov ->
                val model = prov.models().getExistingFile(prov.modLoc("block/${ctx.name}/block"))
                prov.getVariantBuilder(ctx.entry).forAllStates {
                    ConfiguredModel.builder().modelFile(model).build()
                }
            }.item()
            .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
            .transform(customItemModel())
            .register()

    val RECORD_PRESS_BASE: BlockEntry<RecordPressBaseBlock> =
        ModRegistrate
            .block("resonance_press", ::RecordPressBaseBlock)
            .lang("Resonance Press")
            .properties { p ->
                p
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.COPPER)
            }.tag(
                AllTags.AllBlockTags.SAFE_NBT.tag,
            ).blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel())
            .register()


    val KINETIC_MUSIC_BOX: BlockEntry<KineticMusicBoxBlock> =
        ModRegistrate
            .block("kinetic_music_box") { KineticMusicBoxBlock(it) }
            .initialProperties { SharedProperties.stone() }
            .properties { p ->
                p.strength(2.0f, 6.0f).sound(SoundType.METAL).noOcclusion()
            }
            .lang("Kinetic Music Box")
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(ModStressConfig.setImpact(1.0))
            .blockstate { ctx, prov -> musicBoxBlockstate(ctx, prov) }
            .item()
            .transform(customItemModel())
            .register()

    val MUSIC_CONDUCTOR: BlockEntry<MusicConductorBlock> =
        ModRegistrate
            .block("music_conductor") { MusicConductorBlock(it) }
            .initialProperties { SharedProperties.stone() }
            .properties { p ->
                p.strength(2.0f, 6.0f).sound(SoundType.METAL).noOcclusion()
            }
            .lang("Music Conductor")
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(ModStressConfig.setImpact(1.0))
            .blockstate { ctx, prov -> musicBoxBlockstate(ctx, prov) }
            .item()
            .transform(customItemModel())
            .register()

    /** Kinetic MIDI imprint depot: configure MIDI once, stamp blank rolls while powered. */
    val MIDI_TABLE: BlockEntry<MidiTableBlock> =
        ModRegistrate
            .block("midi_table") { MidiTableBlock(it) }
            .initialProperties { SharedProperties.stone() }
            .properties { p ->
                p.strength(3.0f, 6.0f).sound(SoundType.WOOD).noOcclusion()
            }
            .transform(ModStressConfig.setImpact(2.0))
            .lang("MIDI Table")
            .blockstate { ctx, prov ->
                val model = prov.models().getExistingFile(prov.modLoc("block/${ctx.name}/block"))
                prov.getVariantBuilder(ctx.entry).forAllStates {
                    ConfiguredModel.builder().modelFile(model).build()
                }
            }.item()
            .transform(customItemModel())
            .register()

    override fun register(registry: Registry<*>?) {
        "Registering blocks".info()
    }

    /** Default model faces south; FRONT rotates yaw while shaft [FACING] is vertical. */
    private fun yRotFromFront(front: Direction): Int =
        when (front) {
            Direction.SOUTH -> 0
            Direction.WEST -> 90
            Direction.NORTH -> 180
            Direction.EAST -> 270
            else -> 0
        }

    private fun musicBoxBlockstate(
        ctx: DataGenContext<Block, out Block>,
        prov: RegistrateBlockstateProvider,
    ) {
        val model: ModelFile = prov.models().getExistingFile(prov.modLoc("block/${ctx.name}/block"))
        prov.getVariantBuilder(ctx.entry).forAllStates { state ->
            val facing = state.getValue(DirectionalKineticBlock.FACING)
            val front = state.getValue(MusicBoxBlock.FRONT)
            var x = 0
            var y = 0
            when (facing) {
                Direction.UP -> y = yRotFromFront(front)
                Direction.DOWN -> {
                    x = 180
                    y = yRotFromFront(front)
                }
                Direction.NORTH -> x = 90
                Direction.SOUTH -> {
                    x = 90
                    y = 180
                }
                Direction.WEST -> {
                    x = 90
                    y = 270
                }
                Direction.EAST -> {
                    x = 90
                    y = 90
                }
            }
            ConfiguredModel
                .builder()
                .modelFile(model)
                .rotationX(x)
                .rotationY(y)
                .build()
        }
    }
}
