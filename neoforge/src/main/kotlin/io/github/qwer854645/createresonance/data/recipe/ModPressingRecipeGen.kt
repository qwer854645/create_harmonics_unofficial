package io.github.qwer854645.createresonance.data.recipe

import com.simibubi.create.api.data.recipe.PressingRecipeGen
import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import io.github.qwer854645.createresonance.foundation.registry.ModItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import java.util.concurrent.CompletableFuture

class ModPressingRecipeGen(
    output: PackOutput,
    registries: CompletableFuture<HolderLookup.Provider>,
) : PressingRecipeGen(output, registries, MOD_ID) {
    /**
     * Identity press recipe so Create's mechanical press will start on a resonance disc.
     * Actual URL imprint is applied by [RecordPressBaseBehaviour] when the press sits on a resonance press.
     */
    val discPressingRecipes: List<GeneratedRecipe> =
        listOf(
            create("resonance_disc") { builder ->
                builder
                    .require { ModItems.WEBDISC.get() }
                    .output { ModItems.WEBDISC.get() }
            },
        )
}
