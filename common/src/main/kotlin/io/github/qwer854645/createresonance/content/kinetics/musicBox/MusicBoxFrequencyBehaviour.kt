package io.github.qwer854645.createresonance.content.kinetics.musicBox

import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import net.createmod.catnip.data.Couple
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.tuple.Pair

/**
 * Item-based frequency pair (Create wireless redstone style), without joining the
 * global redstone-link network. Ensemble matching is spatial (section RPM = accept range, max 128) + frequency.
 */
class MusicBoxFrequencyBehaviour(
    be: SmartBlockEntity,
    slots: Pair<ValueBoxTransform, ValueBoxTransform>,
) : BlockEntityBehaviour(be) {
    companion object {
        val TYPE = BehaviourType<MusicBoxFrequencyBehaviour>()

        fun create(be: SmartBlockEntity): MusicBoxFrequencyBehaviour {
            val slots = ValueBoxTransform.Dual.makeSlots(::MusicBoxFrequencySlot)
            return MusicBoxFrequencyBehaviour(be, slots)
        }
    }

    var frequencyFirst: Frequency = Frequency.EMPTY
        private set
    var frequencyLast: Frequency = Frequency.EMPTY
        private set

    val firstSlot: ValueBoxTransform = slots.left
    val secondSlot: ValueBoxTransform = slots.right

    fun getNetworkKey(): Couple<Frequency> = Couple.create(frequencyFirst, frequencyLast)

    fun matches(other: MusicBoxFrequencyBehaviour): Boolean = getNetworkKey() == other.getNetworkKey()

    fun setFrequency(
        first: Boolean,
        stack: ItemStack,
    ) {
        val copy = stack.copy()
        copy.count = 1
        val previous = if (first) frequencyFirst.stack else frequencyLast.stack
        if (ItemStack.isSameItemSameComponents(copy, previous)) return
        if (first) {
            frequencyFirst = Frequency.of(copy)
        } else {
            frequencyLast = Frequency.of(copy)
        }
        blockEntity.sendData()
        blockEntity.setChanged()
    }

    fun testHit(
        first: Boolean,
        hit: Vec3,
    ): Boolean {
        val state = blockEntity.blockState
        val localHit = hit.subtract(Vec3.atLowerCornerOf(blockEntity.blockPos))
        return (if (first) firstSlot else secondSlot).testHit(world, pos, state, localHit)
    }

    override fun getType(): BehaviourType<*> = TYPE

    override fun isSafeNBT(): Boolean = true

    override fun destroy() {
        val level = world
        if (!level.isClientSide) {
            for (stack in listOf(frequencyFirst.stack, frequencyLast.stack)) {
                if (!stack.isEmpty) {
                    Containers.dropItemStack(
                        level,
                        pos.x + 0.5,
                        pos.y + 0.5,
                        pos.z + 0.5,
                        stack.copy(),
                    )
                }
            }
        }
        frequencyFirst = Frequency.EMPTY
        frequencyLast = Frequency.EMPTY
        super.destroy()
    }

    override fun write(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        nbt.put("FrequencyFirst", frequencyFirst.stack.saveOptional(registries))
        nbt.put("FrequencyLast", frequencyLast.stack.saveOptional(registries))
    }

    override fun read(
        nbt: CompoundTag,
        registries: HolderLookup.Provider,
        clientPacket: Boolean,
    ) {
        frequencyFirst = Frequency.of(ItemStack.parseOptional(registries, nbt.getCompound("FrequencyFirst")))
        frequencyLast = Frequency.of(ItemStack.parseOptional(registries, nbt.getCompound("FrequencyLast")))
    }
}
