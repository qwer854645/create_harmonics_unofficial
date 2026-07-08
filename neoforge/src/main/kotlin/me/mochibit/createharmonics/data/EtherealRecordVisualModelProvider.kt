package me.mochibit.createharmonics.data

import me.mochibit.createharmonics.CreateHarmonicsMod.MOD_ID
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.BlockModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper

class EtherealRecordVisualModelProvider(
    output: PackOutput,
    existingFileHelper: ExistingFileHelper,
) : BlockModelProvider(output, MOD_ID, existingFileHelper) {
    override fun registerModels() {
        generateRecordVisualModel("webdisc")
    }

    private fun generateRecordVisualModel(modelName: String) {
        withExistingParent(
            "block/webdisc_visual/$modelName",
            modLoc("block/webdisc_visual/visual"),
        ).texture("0", modLoc("block/webdisc_visual/$modelName"))
            .texture("particle", modLoc("block/webdisc_visual/$modelName"))
    }

    override fun getName(): String = "Webdisc Visual Models"
}
