package me.mochibit.createharmonics.mixin.content.record;

import me.mochibit.createharmonics.content.records.EtherealRecordItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EtherealRecordItem.class)
public abstract class EtherealRecordItemMixin implements IItemExtension {

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        return 0;
    }
}
