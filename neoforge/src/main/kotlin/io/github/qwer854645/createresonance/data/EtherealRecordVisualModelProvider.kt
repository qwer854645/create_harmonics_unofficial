package io.github.qwer854645.createresonance.data

import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.BlockModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class ResonanceDiscVisualModelProvider(
    output: PackOutput,
    existingFileHelper: ExistingFileHelper,
) : BlockModelProvider(output, MOD_ID, existingFileHelper) {
    override fun registerModels() {
        generateRecordVisualModel("resonance_disc")
    }

    private fun generateRecordVisualModel(modelName: String) {
        withExistingParent(
            "block/resonance_disc_visual/$modelName",
            modLoc("block/resonance_disc_visual/visual"),
        ).texture("0", modLoc("block/resonance_disc_visual/$modelName"))
            .texture("particle", modLoc("block/resonance_disc_visual/$modelName"))
    }

    override fun getName(): String = "Webdisc Visual Models"
}
