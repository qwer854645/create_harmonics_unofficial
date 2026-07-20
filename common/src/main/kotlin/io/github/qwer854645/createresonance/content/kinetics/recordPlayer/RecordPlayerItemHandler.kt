package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import io.github.qwer854645.createresonance.content.records.ResonanceDiscItem
import io.github.qwer854645.createresonance.foundation.extension.onServer
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.ItemStackHandler

class RecordPlayerItemHandler(
    val behaviour: RecordPlayerBehaviour,
) : ItemStackHandler(1) {
    companion object {
        const val MAIN_RECORD_SLOT = 0
    }

    override fun onLoad() {
        behaviour.be.onServer {
            behaviour.blockEntity.notifyUpdate()
        }
    }

    override fun onContentsChanged(slot: Int) {
        behaviour.be.onServer {
            val hasDisc = !getStackInSlot(MAIN_RECORD_SLOT).isEmpty
            val be = behaviour.blockEntity
            if (!hasDisc) {
                behaviour.onAudioTitleUpdate("")
            }
            be.level?.setBlockAndUpdate(
                be.blockPos,
                be.blockState.setValue(RecordPlayerTrait.HAS_RESONANCE_DISC, hasDisc),
            )
            be.notifyUpdate()
        }
    }

    override fun isItemValid(
        slot: Int,
        stack: ItemStack,
    ): Boolean {
        if (stack.isEmpty) return true
        return stack.item is ResonanceDiscItem
    }

    override fun deserializeNBT(
        provider: HolderLookup.Provider,
        nbt: CompoundTag,
    ) {
        super.deserializeNBT(provider, nbt)
        if (stacks.size < 1) {
            val expanded = NonNullList.withSize(1, ItemStack.EMPTY)
            for (i in stacks.indices) expanded[i] = stacks[i]
            stacks = expanded
        }
    }
}
