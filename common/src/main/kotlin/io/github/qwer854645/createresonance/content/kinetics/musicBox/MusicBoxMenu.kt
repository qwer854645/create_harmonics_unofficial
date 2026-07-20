package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.simibubi.create.foundation.gui.menu.MenuBase
import io.github.qwer854645.createresonance.content.midi.MidiRollUtilities
import io.github.qwer854645.createresonance.foundation.registry.ModMenuTypes
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.neoforged.neoforge.items.SlotItemHandler

/**
 * Slot layout must stay in sync with [MusicBoxScreen] panel metrics.
 */
class MusicBoxMenu : MenuBase<MusicBoxBlockEntity> {
    lateinit var scoreSlot: Slot
        private set

    constructor(
        type: MenuType<*>,
        id: Int,
        inv: Inventory,
        extraData: RegistryFriendlyByteBuf,
    ) : super(type, id, inv, extraData)

    constructor(
        type: MenuType<*>,
        id: Int,
        inv: Inventory,
        be: MusicBoxBlockEntity,
    ) : super(type, id, inv, be)

    companion object {
        /** Matches [MusicBoxScreen] window width / inventory. */
        const val GUI_WIDTH = 176
        const val SCORE_SLOT_X = 79
        const val SCORE_SLOT_Y = 36

        fun panelHeight(be: MusicBoxBlockEntity): Int {
            val b = be.behaviour
            b.enforceRoleFromBlock()
            b.syncRoleFromModeSlot()
            val conductor = b.isConductorBlock
            val section = !conductor && b.role == MusicBoxRole.SECTION

            // Title + status + score slot region
            var h = 58
            if (!section) {
                h += 22 // transport
                if (b.hasAdvancedControls) h += 20 // seek
            }
            if (!conductor) {
                h += 20 // instrument
                h += 20 // part
            }
            return h
        }

        fun create(
            id: Int,
            inv: Inventory,
            be: MusicBoxBlockEntity,
        ): MusicBoxMenu = MusicBoxMenu(ModMenuTypes.MUSIC_BOX.get(), id, inv, be)
    }

    override fun createOnClient(extraData: RegistryFriendlyByteBuf): MusicBoxBlockEntity? {
        // Reflective access keeps Minecraft out of this class's constant pool (dedicated-server safe).
        val minecraftClass = Class.forName("net.minecraft.client.Minecraft")
        val minecraft = minecraftClass.getMethod("getInstance").invoke(null) ?: return null
        val level = minecraftClass.getField("level").get(minecraft) as? Level ?: return null
        val pos = extraData.readBlockPos()
        val be = level.getBlockEntity(pos) as? MusicBoxBlockEntity ?: return null
        val nbt = extraData.readNbt()
        if (nbt != null) {
            be.readClient(nbt, extraData.registryAccess())
        }
        return be
    }

    override fun initAndReadInventory(contentHolder: MusicBoxBlockEntity) {}

    override fun addSlots() {
        val inv = contentHolder.inventory
        scoreSlot =
            object : SlotItemHandler(inv, 0, SCORE_SLOT_X, SCORE_SLOT_Y) {
                override fun mayPlace(stack: ItemStack): Boolean =
                    MidiRollUtilities.isMidiRoll(stack) && MidiRollUtilities.hasMidi(stack)

                // Always active — section mode grays the slot but still allows insert/extract.
                override fun isActive(): Boolean = true
            }
        addSlot(scoreSlot)

        val invTop = panelHeight(contentHolder) + 4
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, invTop + 18 + row * 18))
            }
        }
        for (col in 0 until 9) {
            addSlot(Slot(playerInventory, col, 8 + col * 18, invTop + 76))
        }
    }

    override fun saveData(contentHolder: MusicBoxBlockEntity) {}

    override fun quickMoveStack(
        player: Player,
        index: Int,
    ): ItemStack {
        val slot = getSlot(index)
        if (!slot.hasItem()) return ItemStack.EMPTY
        val stack = slot.item
        if (index == 0) {
            moveItemStackTo(stack, 1, slots.size, true)
        } else {
            moveItemStackTo(stack, 0, 1, false)
        }
        return ItemStack.EMPTY
    }
}
