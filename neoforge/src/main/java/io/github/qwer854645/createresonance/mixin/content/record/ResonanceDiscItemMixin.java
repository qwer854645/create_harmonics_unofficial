package io.github.qwer854645.createresonance.mixin.content.record;

import io.github.qwer854645.createresonance.content.records.ResonanceDiscItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ResonanceDiscItem.class)
public abstract class ResonanceDiscItemMixin implements IItemExtension {

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        return 0;
    }
}
