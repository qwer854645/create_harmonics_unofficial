package io.github.qwer854645.createresonance.ponder

import com.simibubi.create.AllBlocks
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity
import com.simibubi.create.content.kinetics.press.PressingBehaviour
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlock
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity
import com.simibubi.create.foundation.ponder.CreateSceneBuilder
import io.github.qwer854645.createresonance.audio.effect.EffectPreset
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxBlockEntity
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxEnsembleMode
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.kineticNetworkJukebox.KineticNetworkJukeboxBlockEntity
import io.github.qwer854645.createresonance.content.midi.MidiRollUtilities
import io.github.qwer854645.createresonance.content.midi.MidiTableBlockEntity
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseBlockEntity
import io.github.qwer854645.createresonance.foundation.registry.ModItems
import net.createmod.catnip.math.Pointing
import net.createmod.ponder.api.PonderPalette
import net.createmod.ponder.api.scene.SceneBuilder
import net.createmod.ponder.api.scene.SceneBuildingUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeverBlock
import net.minecraft.world.phys.AABB
import kotlin.jvm.java

object PonderScenes {
    fun recordPressBase(
        builder: SceneBuilder,
        util: SceneBuildingUtil,
    ) {
        // Ponder scene size 9 x 6 x 9
        val scene = CreateSceneBuilder(builder)
        scene.title(
            "resonance_press",
            "Using Record Press Bases",
        )

        val plateSize = 7
        val pressBase = util.grid().at(4, 1, 4)
        val pressTop = util.vector().topOf(pressBase)

        val plateOffsetX = pressBase.x - plateSize / 2
        val plateOffsetZ = pressBase.z - plateSize / 2

        scene.configureBasePlate(plateOffsetX, plateOffsetZ, plateSize)
        scene.addKeyframe()
        scene.showBasePlate()
        scene.idle(5)
        scene.world().showSection(util.select().position(pressBase), Direction.DOWN)
        scene
            .overlay()
            .showText(60)
            .text("Record Press Bases are depot-like blocks used for setting urls on Webdiscs")
            .placeNearTarget()
            .pointAt(pressTop)
        scene.idle(70)

        scene.addKeyframe()
        val fallingItem = ItemStack(ModItems.WEBDISC.get())
        val fallingItemLink =
            scene.world().createItemEntity(util.vector().centerOf(4, 4, 4), util.vector().of(.0, -0.1, .0), fallingItem)
        scene.idle(9)
        scene.world().modifyEntity(fallingItemLink, Entity::discard)
        scene.world().createItemOnBeltLike(pressBase, Direction.UP, fallingItem)
        scene.idle(20)
        scene
            .overlay()
            .showText(40)
            .text("Webdiscs can be dropped on top of the press base")
            .placeNearTarget()
            .pointAt(pressTop)
        scene.idle(50)
        val beltSelection1 = util.select().fromTo(2, 1, 4, 3, 1, 4)
        val beltSelection2 = util.select().fromTo(5, 1, 4, 8, 1, 4)
        scene.world().showSection(beltSelection1, Direction.DOWN)
        scene.idle(5)
        scene.world().showSection(beltSelection2, Direction.DOWN)
        scene
            .overlay()
            .showText(40)
            .text("Or be sledded in using conveyor belts")
            .placeNearTarget()
            .pointAt(pressTop)
        scene.idle(15)
        scene.addKeyframe()
        scene.world().modifyBlockEntity(pressBase, RecordPressBaseBlockEntity::class.java) { be ->
            be.behaviour.heldItem?.let { be.behaviour.tryEjectOutputToBelts() }
        }
        scene.idle(7)
        val beltNearPress = util.grid().at(5, 1, 4)
        scene.world().createItemOnBeltLike(beltNearPress, Direction.WEST, fallingItem)
        scene.idle(50)
        scene.addKeyframe()
        scene
            .overlay()
            .showControls(pressTop, Pointing.DOWN, 20)
            .rightClick()
        scene
            .overlay()
            .showText(50)
            .text("Right-Click to open the configuration interface")
            .placeNearTarget()
            .pointAt(pressTop)
        scene.idle(60)
        scene
            .overlay()
            .showText(50)
            .text("You can create a list of urls to be set, and choose the mode of selection")
            .placeNearTarget()
            .pointAt(pressTop)
        scene.idle(60)
        val pressPos = util.grid().at(4, 3, 4)
        scene.addKeyframe()
        scene.world().showSection(util.select().position(pressPos), Direction.DOWN)
        scene.idle(10)
        scene.world().modifyBlockEntity(pressPos, MechanicalPressBlockEntity::class.java) { be ->
            be.pressingBehaviour.start(PressingBehaviour.Mode.BELT)
        }
        scene.idle(10)
        scene.world().modifyBlockEntity(
            pressPos,
            MechanicalPressBlockEntity::class.java,
        ) { pte: MechanicalPressBlockEntity ->
            pte
                .getPressingBehaviour()
                .makePressingParticleEffect(
                    util.vector().centerOf(pressBase).add(0.0, (8 / 16f).toDouble(), 0.0),
                    ItemStack(ModItems.WEBDISC.get()),
                )
        }
        scene
            .overlay()
            .showText(70)
            .text("When a record is present, a mechanical press must be used to inprint the url onto it")
            .placeNearTarget()
            .pointAt(pressTop)
        scene.idle(80)

        scene.markAsFinished()
    }

    fun kineticNetworkJukebox(
        builder: SceneBuilder,
        util: SceneBuildingUtil,
    ) {
        val scene = CreateSceneBuilder(builder)
        scene.title(
            "kinetic_network_jukebox",
            "Using Kinetic Network Jukeboxes",
        )

        val recordPlayer = util.grid().at(16, 2, 16)
        val recordLever = util.grid().at(16, 2, 15)

        val topOfPlayer = util.vector().topOf(recordPlayer)
        val plateSize = 8
        scene.configureBasePlate(recordPlayer.x - (plateSize / 2), recordPlayer.z - (plateSize / 2), plateSize)
        scene.addKeyframe()
        scene.showBasePlate()
        scene.idle(5)

        scene.world().showSection(util.select().fromTo(16, 1, 15, 16, 1, 21), Direction.DOWN)
        scene.world().showSection(util.select().position(16, 2, 16), Direction.DOWN)
        scene
            .overlay()
            .showText(60)
            .text("Andesite jukeboxes can play audio from Webdiscs")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.idle(70)

        // Record insertion feature
        scene.addKeyframe()
        val resonance_disc = ItemStack(ModItems.WEBDISC.get())
        scene
            .overlay()
            .showControls(topOfPlayer, Pointing.DOWN, 20)
            .rightClick()
            .withItem(resonance_disc)
        scene.idle(7)

        scene.world().modifyBlockEntity(recordPlayer, KineticNetworkJukeboxBlockEntity::class.java) { be ->
            be.playerBehaviour.insertRecord(resonance_disc)
        }

        scene.idle(10)
        scene
            .overlay()
            .showText(60)
            .text("Right-Click to manually insert or pop Webdiscs from it")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.idle(50)

        // Pitch change feature
        scene.addKeyframe()
        scene.world().showSection(util.select().fromTo(16, 2, 17, 16, 2, 20), Direction.DOWN)
        scene.idle(10)
        scene
            .overlay()
            .showText(90)
            .text("Based on the rotational speed supplied, the pitch of the audio will change")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.rotateCameraY(-90f)
        scene.idle(40)
        scene.world().multiplyKineticSpeed(util.select().everywhere(), 1 / 4f)
        scene.idle(70)

        // Jukebox modes
        scene.addKeyframe()
        scene.rotateCameraY(90f)
        scene.world().hideSection(util.select().fromTo(16, 2, 17, 16, 2, 20), Direction.UP)
        scene.idle(5)
        scene.world().multiplyKineticSpeed(util.select().everywhere(), 2f)
        scene
            .overlay()
            .showText(60)
            .text("Andesite jukeboxes have 2 modes, play and pause")
            .pointAt(topOfPlayer)
        scene.idle(70)

        scene
            .overlay()
            .showText(60)
            .colored(PonderPalette.GREEN)
            .text("When in play mode, the audio will play once and stop when finished")
            .pointAt(topOfPlayer)

        scene.idle(80)

        scene
            .overlay()
            .showText(60)
            .colored(PonderPalette.BLUE)
            .text("In pause mode the playback will be simply manually paused, and can be resumed resetting to play mode")
            .pointAt(topOfPlayer)
        scene.idle(80)

        // Redstone loop mode
        scene.addKeyframe()
        val redstoneBB =
            AABB(recordPlayer)
                .inflate((-1 / 32f).toDouble(), (-1 / 32f).toDouble(), (-1 / 32f).toDouble())

        scene.world().showSection(util.select().position(recordLever), Direction.DOWN)
        scene.idle(10)
        scene.world().modifyBlock(recordLever, { s -> s.cycle(LeverBlock.POWERED) }, false)
        scene.effects().indicateRedstone(recordLever)
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, recordPlayer, redstoneBB, 80)
        scene
            .overlay()
            .showText(60)
            .colored(PonderPalette.RED)
            .text("When powered they behave differently depending on the mode")
            .pointAt(topOfPlayer)
        scene.idle(70)

        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.GREEN)
            .text("In play mode, they will loop the playback until unpowered")
            .pointAt(topOfPlayer)
        scene.idle(80)

        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.BLUE)
            .text("In pause mode, redstone controls the playback, so power to play, unpower to pause")
            .pointAt(topOfPlayer)
        scene.idle(80)
        scene.addKeyframe()
        scene.world().hideSection(util.select().position(recordLever), Direction.UP)
        scene.idle(20)
        scene.world().setBlock(
            recordLever,
            AllBlocks.ANALOG_LEVER.get().defaultBlockState().setValue(
                AnalogLeverBlock.FACING,
                Direction.WEST,
            ),
            false,
        )
        scene.world().showSection(util.select().position(recordLever), Direction.DOWN)
        scene
            .overlay()
            .showText(70)
            .text("When the redstone signal is analog, volume and play mode can be controlled")
            .pointAt(topOfPlayer)
        scene.idle(40)

        scene.idle(7)
        for (i in 0..6) {
            scene.idle(2)
            val state = i + 1
            scene.world().modifyBlockEntityNBT(
                util.select().position(recordLever),
                AnalogLeverBlockEntity::class.java,
            ) { nbt: CompoundTag -> nbt.putInt("State", state) }
        }
        scene.idle(40)

        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.GREEN)
            .text("In play mode, it will only loop when fully powered, scaling volume with power level")
            .pointAt(topOfPlayer)
        scene.idle(80)

        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.BLUE)
            .text("In pause mode, audio will always loop, with volume scaled by power level, pausing only when fully unpowered")
            .pointAt(topOfPlayer)
        scene.idle(80)
        // External effects modulation
        scene.addKeyframe()
        scene
            .overlay()
            .showText(30)
            .text("Environment also affects audio")
            .pointAt(topOfPlayer)
            .placeNearTarget()
        scene.idle(35)

        val reverberatorBlockTypes =
            listOf(
                EffectPreset.Reverberator.ROOM_INCREASERS_BLOCKS,
                EffectPreset.Reverberator.DAMPING_INCREASERS_BLOCKS,
                EffectPreset.Reverberator.WET_INCREASERS_BLOCKS,
            )

        val typeMaxRow =
            reverberatorBlockTypes
                .maxOfOrNull { it.size }
                ?.coerceAtMost(6) ?: 0

        val reverberatorWallStartX = 13
        val reverberatorWallStartY = 1
        val reverberatorWallStartZ = 15

        val fullSelection =
            util.select().fromTo(
                reverberatorWallStartX,
                reverberatorWallStartY,
                reverberatorWallStartZ,
                reverberatorWallStartX,
                reverberatorWallStartY + reverberatorBlockTypes.size,
                reverberatorWallStartZ + 6,
            )

        scene.world().showSection(
            fullSelection,
            Direction.DOWN,
        )

        reverberatorBlockTypes.forEachIndexed { index, typeList ->
            for (row in 0 until typeMaxRow) {
                val block = typeList.getOrNull(row)
                if (block != null) {
                    val placePos =
                        BlockPos(reverberatorWallStartX, reverberatorWallStartY + index, reverberatorWallStartZ + row)
                    scene.world().setBlock(placePos, block.defaultBlockState(), true)
                    scene.idle(3)
                }
            }
        }

        scene
            .overlay()
            .showText(50)
            .text("With specific blocks placed around the jukebox, a reverberator will form.")
            .placeNearTarget()
        scene.idle(60)

        scene
            .overlay()
            .showText(70)
            .text("The blocks in this row control the room size; more blocks mean a bigger room.")
            .pointAt(util.vector().topOf(reverberatorWallStartX, 0, reverberatorWallStartZ))
            .placeNearTarget()
        scene.idle(80)

        scene
            .overlay()
            .showText(70)
            .text("These blocks control damping, which determines how long the reverb lasts; more blocks mean shorter reverb.")
            .pointAt(util.vector().topOf(reverberatorWallStartX, reverberatorWallStartY, reverberatorWallStartZ))
            .placeNearTarget()
        scene.idle(80)

        scene
            .overlay()
            .showText(70)
            .text("These blocks control wetness; more blocks mean more intense reverb.")
            .pointAt(util.vector().topOf(reverberatorWallStartX, reverberatorWallStartY + 1, reverberatorWallStartZ))
            .placeNearTarget()
        scene.idle(80)

        scene.world().hideSection(
            fullSelection,
            Direction.UP,
        )

        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.BLUE)
            .text("Liquids affect audio based on their viscosity, muffling the sound.")
            .placeNearTarget()
        scene.idle(80)

        // Mechanical Arm Feature
        val extractingArm = util.grid().at(18, 2, 15)
        val insertingArm = util.grid().at(18, 2, 17)
        val depot = util.grid().at(18, 2, 16)

        scene.addKeyframe()
        scene.world().showSection(util.select().fromTo(18, 1, 15, 18, 2, 17), Direction.DOWN)
        scene
            .overlay()
            .showText(50)
            .text("Mechanical arms works on Andesite jukeboxes and it has special behaviours based on the mode")
            .pointAt(topOfPlayer)
            .placeNearTarget()
        scene.idle(60)

        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.GREEN)
            .text("In play mode, when unpowered or not fully powered, arms will extract the record only if the playback ends")
            .placeNearTarget()
        scene.idle(24)
        scene.world().instructArm(extractingArm, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0)
        scene.idle(30)
        scene.world().modifyBlockEntity(recordPlayer, KineticNetworkJukeboxBlockEntity::class.java) { be ->
            be.playerBehaviour.popRecord()
        }
        scene.world().instructArm(extractingArm, ArmBlockEntity.Phase.SEARCH_OUTPUTS, resonance_disc, -1)
        scene.idle(30)
        scene.world().instructArm(extractingArm, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, resonance_disc, 0)
        scene.idle(24)
        scene.world().createItemOnBeltLike(depot, Direction.UP, resonance_disc)
        scene.world().instructArm(extractingArm, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1)
        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.GREEN)
            .text("In play mode if powered fully, arms can't extract the record")
            .placeNearTarget()

        scene.idle(80)

        scene.idle(15)
        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.BLUE)
            .text("In pause mode, arms will extract the record when fully unpowered")
            .placeNearTarget()

        scene.idle(24)
        scene.world().instructArm(insertingArm, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0)
        scene.idle(24)
        scene.world().removeItemsFromBelt(depot)
        scene.world().instructArm(insertingArm, ArmBlockEntity.Phase.SEARCH_OUTPUTS, resonance_disc, -1)
        scene.idle(5)
        scene.world().instructArm(insertingArm, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, resonance_disc, 0)
        scene.idle(24)
        scene.world().modifyBlockEntity(recordPlayer, KineticNetworkJukeboxBlockEntity::class.java) { be ->
            be.playerBehaviour.insertRecord(resonance_disc)
        }
        scene.world().instructArm(insertingArm, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1)
        scene.idle(5)
        scene.idle(80)
        scene.world().hideSection(util.select().position(recordLever), Direction.UP)

        // Contraption scene
        val bearingPos = util.grid().at(16, 3, 16)
        val jukeboxContraption = util.grid().at(15, 3, 14)
        scene.addKeyframe()
        scene.world().hideSection(util.select().fromTo(18, 1, 14, 18, 2, 17), Direction.UP)
        scene.world().hideSection(util.select().fromTo(16, 1, 15, 16, 1, 21), Direction.UP)
        scene.world().hideSection(util.select().position(16, 2, 16), Direction.UP)
        scene.idle(30)
        scene.world().showSection(util.select().position(bearingPos), Direction.WEST)

        val rotatingStructure =
            scene.world().showIndependentSection(
                util.select().fromTo(15, 3, 14, 15, 4, 16),
                Direction.WEST,
            )
        scene
            .world()
            .configureCenterOfRotation(rotatingStructure, util.vector().blockSurface(bearingPos, Direction.EAST))
        scene.world().setKineticSpeed(util.select().position(jukeboxContraption), 32f)
        scene.world().rotateBearing(bearingPos, 360f, 80)
        scene.world().rotateSection(rotatingStructure, 360.0, 0.0, 0.0, 80)
        scene
            .overlay()
            .showText(80)
            .text("Resonance players work on contraptions too")
            .placeNearTarget()
            .pointAt(topOfPlayer)

        scene.idle(90)
        scene
            .overlay()
            .showText(60)
            .text("The Andesite Jukebox pauses audio when the contraption stops, by default")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.world().setKineticSpeed(util.select().position(jukeboxContraption), 0f)
        scene.idle(70)

        scene
            .overlay()
            .showText(80)
            .text("Playback is resumed when the contraption starts moving again")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.world().setKineticSpeed(util.select().position(jukeboxContraption), 32f)
        scene.world().rotateBearing(bearingPos, 360f, 80)
        scene.world().rotateSection(rotatingStructure, 360.0, 0.0, 0.0, 80)
        scene.idle(90)

        scene
            .overlay()
            .showText(80)
            .text("Jukeboxes affects audio pitch relative to its position speed")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.world().setKineticSpeed(util.select().position(jukeboxContraption), 16f)
        scene.world().rotateBearing(bearingPos, 360f, 120)
        scene.world().rotateSection(rotatingStructure, 360.0, 0.0, 0.0, 120)
        scene.idle(130)

        scene.addKeyframe()
        val rotation: Float = 360 * 3f
        val duration = 120 * 3
        val contraptionLeverPosition = util.grid().at(15, 3, 15)
        val contraptionLeverSel = util.select().position(15, 3, 15)

        scene
            .overlay()
            .showText(80)
            .text("Modes set before assembly works on contraptions, allowing some special cases and locking the pitch")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.world().rotateBearing(bearingPos, rotation, duration)
        scene.world().rotateSection(rotatingStructure, rotation.toDouble(), 0.0, 0.0, duration)
        scene.idle(90)

        scene
            .overlay()
            .showText(80)
            .text("Redstone level is also preserved, allowing for volume regulation both on play and pause modes")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.world().setBlock(
            contraptionLeverPosition,
            Blocks.REDSTONE_BLOCK.defaultBlockState(),
            true,
        )

        scene.idle(7)
        for (i in 0..9) {
            scene.idle(2)
            val state = i + 1
            scene.world().modifyBlockEntityNBT(
                contraptionLeverSel,
                AnalogLeverBlockEntity::class.java,
            ) { nbt: CompoundTag -> nbt.putInt("State", state) }
        }

        scene.effects().indicateRedstone(contraptionLeverPosition)
        scene.idle(90)

        scene
            .overlay()
            .showText(80)
            .text("If the jukebox is on pause mode, and powered, it will play audio even if the contraption is stopped")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.idle(90)

        scene
            .overlay()
            .showText(80)
            .text("If in play mode, redstone will only control the volume, but playback will stop when the contraption stops")
            .placeNearTarget()
            .pointAt(topOfPlayer)
        scene.idle(90)

        scene.markAsFinished()
    }

    fun midiTable(
        builder: SceneBuilder,
        util: SceneBuildingUtil,
    ) {
        val scene = CreateSceneBuilder(builder)
        scene.title("midi_table", "Using MIDI Tables")

        val table = util.grid().at(3, 2, 2)
        val tableTop = util.vector().topOf(table)
        val plateSize = 5

        scene.configureBasePlate(table.x - plateSize / 2, table.z - plateSize / 2, plateSize)
        scene.showBasePlate()
        scene.idle(5)

        scene.world().showSection(util.select().position(3, 1, 2), Direction.DOWN)
        scene.idle(5)
        scene.world().showSection(util.select().position(table), Direction.DOWN)
        scene
            .overlay()
            .showText(60)
            .text("MIDI Tables imprint blank Scores with a configured MIDI file while spinning")
            .placeNearTarget()
            .pointAt(tableTop)
        scene.idle(70)

        scene.addKeyframe()
        scene
            .overlay()
            .showControls(util.vector().blockSurface(table, Direction.WEST), Pointing.RIGHT, 40)
            .rightClick()
        scene
            .overlay()
            .showText(60)
            .text("Right-Click a side to open the configuration screen and load a .mid file once")
            .placeNearTarget()
            .pointAt(tableTop)
        scene.idle(20)
        scene.world().modifyBlockEntity(table, MidiTableBlockEntity::class.java) { be ->
            be.configureMidi(DEMO_MIDI, "Demo Track")
        }
        scene.idle(50)

        scene.addKeyframe()
        val blankRoll = ItemStack(ModItems.MIDI_ROLL.get())
        scene.world().createItemOnBeltLike(table, Direction.UP, blankRoll)
        scene
            .overlay()
            .showText(50)
            .text("Place blank Scores on top of the pad — or feed them with belts, funnels, or arms")
            .placeNearTarget()
            .pointAt(tableTop)
        scene.idle(20)
        scene.world().showSection(util.select().fromTo(1, 2, 2, 2, 2, 2), Direction.DOWN)
        scene.idle(5)
        scene.world().showSection(util.select().fromTo(4, 2, 2, 5, 2, 2), Direction.DOWN)
        scene.idle(40)

        scene.addKeyframe()
        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.GREEN)
            .text("While powered (|RPM| ≥ 1), the table writes the MIDI onto the blank Score — no mechanical press needed")
            .placeNearTarget()
            .pointAt(tableTop)
        scene.idle(40)
        scene.world().modifyBlockEntity(table, MidiTableBlockEntity::class.java) { be ->
            be.behaviour.removeHeldItem()
            val filled = MidiRollUtilities.createFilledRoll(DEMO_MIDI, "Demo Track") ?: return@modifyBlockEntity
            be.behaviour.processingOutputBuffer.setStackInSlot(0, filled)
        }
        scene.idle(40)
        scene
            .overlay()
            .showText(60)
            .text("Finished Scores leave via the output buffer — automation cannot pull mid-imprint pads")
            .placeNearTarget()
            .pointAt(tableTop)
        scene.idle(70)

        scene.markAsFinished()
    }

    fun kineticMusicBox(
        builder: SceneBuilder,
        util: SceneBuildingUtil,
    ) {
        val scene = CreateSceneBuilder(builder)
        scene.title("kinetic_music_box", "Using Kinetic Music Boxes")

        val box = util.grid().at(3, 2, 2)
        val boxTop = util.vector().topOf(box)
        val plateSize = 5

        scene.configureBasePlate(box.x - plateSize / 2, box.z - plateSize / 2, plateSize)
        scene.showBasePlate()
        scene.idle(5)

        scene.world().showSection(util.select().position(3, 1, 2), Direction.DOWN)
        scene.idle(5)
        scene.world().showSection(util.select().fromTo(3, 2, 2, 3, 2, 3), Direction.DOWN)
        scene
            .overlay()
            .showText(60)
            .text("Kinetic Music Boxes play filled Scores using rotational power")
            .placeNearTarget()
            .pointAt(boxTop)
        scene.idle(70)

        scene.addKeyframe()
        val score = MidiRollUtilities.createFilledRoll(DEMO_MIDI, "Demo Track")!!
        scene
            .overlay()
            .showControls(boxTop, Pointing.DOWN, 30)
            .rightClick()
            .withItem(score)
        scene.idle(10)
        scene.world().modifyBlockEntity(box, MusicBoxBlockEntity::class.java) { be ->
            be.insertScore(score)
        }
        scene
            .overlay()
            .showText(60)
            .text("Insert a filled Score — playback starts automatically in Solo mode")
            .placeNearTarget()
            .pointAt(boxTop)
        scene.idle(70)

        scene.addKeyframe()
        scene
            .overlay()
            .showText(70)
            .text("RPM controls tempo: 64 RPM is normal speed, slower or faster stretches the music")
            .placeNearTarget()
            .pointAt(boxTop)
        scene.idle(40)
        scene.world().multiplyKineticSpeed(util.select().everywhere(), 0.5f)
        scene.idle(50)
        scene.world().multiplyKineticSpeed(util.select().everywhere(), 2f)
        scene.idle(30)

        scene.addKeyframe()
        scene
            .overlay()
            .showText(70)
            .text("Right-Click the body to open the menu for play, pause, seek, instrument, and MIDI parts")
            .placeNearTarget()
            .pointAt(boxTop)
        scene
            .overlay()
            .showControls(boxTop, Pointing.DOWN, 30)
            .rightClick()
        scene.idle(80)

        scene.addKeyframe()
        scene
            .overlay()
            .showText(80)
            .colored(PonderPalette.BLUE)
            .text("Use the front value box to switch Solo ↔ Section, and set frequency items for ensemble sync with a Music Conductor")
            .placeNearTarget()
            .pointAt(util.vector().blockSurface(box, Direction.SOUTH))
        scene.idle(90)

        scene.markAsFinished()
    }

    fun musicConductor(
        builder: SceneBuilder,
        util: SceneBuildingUtil,
    ) {
        val scene = CreateSceneBuilder(builder)
        scene.title("music_conductor", "Conducting an Ensemble")

        val conductor = util.grid().at(4, 2, 3)
        val leftBox = util.grid().at(2, 2, 3)
        val rightBox = util.grid().at(6, 2, 3)
        val conductorTop = util.vector().topOf(conductor)
        val plateSize = 7

        scene.configureBasePlate(conductor.x - plateSize / 2, conductor.z - plateSize / 2, plateSize)
        scene.showBasePlate()
        scene.idle(5)

        scene.world().showSection(util.select().position(4, 1, 3), Direction.DOWN)
        scene.idle(5)
        scene.world().showSection(util.select().position(conductor), Direction.DOWN)
        scene
            .overlay()
            .showText(60)
            .text("The Music Conductor holds the band Score and silently drives Section music boxes")
            .placeNearTarget()
            .pointAt(conductorTop)
        scene.idle(70)

        scene.addKeyframe()
        val score = MidiRollUtilities.createFilledRoll(DEMO_MIDI, "Band Chart")!!
        scene
            .overlay()
            .showControls(conductorTop, Pointing.DOWN, 30)
            .rightClick()
            .withItem(score)
        scene.idle(10)
        scene.world().modifyBlockEntity(conductor, MusicBoxBlockEntity::class.java) { be ->
            be.insertScore(score)
            be.frequency.setFrequency(true, ItemStack(Items.REDSTONE))
            be.frequency.setFrequency(false, ItemStack(Items.NOTE_BLOCK))
        }
        scene
            .overlay()
            .showText(60)
            .text("Load a Score on the Conductor and set matching frequency items on the front slots")
            .placeNearTarget()
            .pointAt(conductorTop)
        scene.idle(70)

        scene.addKeyframe()
        scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 2, 3), Direction.DOWN)
        scene.idle(5)
        scene.world().showSection(util.select().fromTo(6, 1, 3, 6, 2, 3), Direction.DOWN)
        scene.idle(10)
        scene.world().modifyBlockEntity(leftBox, MusicBoxBlockEntity::class.java) { be ->
            be.ensembleMode?.setValue(MusicBoxEnsembleMode.SECTION.ordinal)
            be.frequency.setFrequency(true, ItemStack(Items.REDSTONE))
            be.frequency.setFrequency(false, ItemStack(Items.NOTE_BLOCK))
        }
        scene.world().modifyBlockEntity(rightBox, MusicBoxBlockEntity::class.java) { be ->
            be.ensembleMode?.setValue(MusicBoxEnsembleMode.SECTION.ordinal)
            be.frequency.setFrequency(true, ItemStack(Items.REDSTONE))
            be.frequency.setFrequency(false, ItemStack(Items.NOTE_BLOCK))
        }
        scene
            .overlay()
            .showText(80)
            .colored(PonderPalette.BLUE)
            .text("Section music boxes must share the Conductor's frequency and spin so their accept range reaches it")
            .placeNearTarget()
            .pointAt(util.vector().topOf(leftBox))
        scene.idle(90)

        scene.addKeyframe()
        scene.world().modifyBlockEntity(conductor, MusicBoxBlockEntity::class.java) { be ->
            be.behaviour.setPlaying(true)
        }
        scene
            .overlay()
            .showText(70)
            .colored(PonderPalette.GREEN)
            .text("Play, pause, seek, and restart on the Conductor sync to every matching Section in range")
            .placeNearTarget()
            .pointAt(conductorTop)
        scene.idle(80)
        scene
            .overlay()
            .showText(70)
            .text("Conductor RPM sets shared tempo; each Section's RPM only sets how far it listens (up to 128 blocks)")
            .placeNearTarget()
            .pointAt(conductorTop)
        scene.idle(80)

        scene.markAsFinished()
    }

    /** Tiny valid Type-0 MIDI (one middle-C) for score demos in ponder scenes. */
    private val DEMO_MIDI: ByteArray =
        byteArrayOf(
            0x4D, 0x54, 0x68, 0x64, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, 0x01, 0x00, 0x60,
            0x4D, 0x54, 0x72, 0x6B, 0x00, 0x00, 0x00, 0x0B,
            0x00, 0x90.toByte(), 0x3C, 0x40, 0x60, 0x80.toByte(), 0x3C, 0x40, 0x00, 0xFF.toByte(), 0x2F, 0x00,
        )
}
