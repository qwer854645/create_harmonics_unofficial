package io.github.qwer854645.createresonance.gametest

import com.simibubi.create.content.logistics.depot.DepotBlockEntity
import io.github.qwer854645.createresonance.CreateResonanceMod
import io.github.qwer854645.createresonance.content.records.ResonanceDiscItem
import io.github.qwer854645.createresonance.foundation.registry.ModItems
import io.github.qwer854645.createresonance.handler.RecordCraftingHandler
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.gametest.GameTestHolder

@GameTestHolder(CreateResonanceMod.MOD_ID)
class CraftingTest {
    @GameTest(template = "complete_setup", setupTicks = 100, timeoutTicks = 1000)
    fun `crafting chain and original disc check`(helper: GameTestHelper) {
        val discDepotPos = fromOrigin(4, 1, 4)

        val referenceBaseStack = ModItems.WEBDISC.get().defaultInstance

        val defaultedDiscName = RecordCraftingHandler.getCraftedWithDisc(referenceBaseStack).displayName
        val uniqueDisc = ResonanceDiscItem.JUKEBOX_DISCS.filter { it.defaultInstance.displayName != defaultedDiscName }.random().defaultInstance

        val discDepotBe = helper.getBlockEntity(discDepotPos) as? DepotBlockEntity ?: return helper.fail("Disc depot was not found")
        discDepotBe.heldItem = uniqueDisc
        helper.runAfterDelay(800) {
            val resultDepotPos = fromOrigin(4, 1, 0)
            val resultDepotBe = helper.getBlockEntity(resultDepotPos) as? DepotBlockEntity ?: return@runAfterDelay helper.fail("Result depot was not found")
            val resultStack = resultDepotBe.heldItem
            val resultItem = resultStack.item

            if (resultItem !is ResonanceDiscItem) {
                return@runAfterDelay helper.fail("Expected an Webdisc Item, got ${resultItem}")
            }

            val craftedWithDisc = RecordCraftingHandler.getCraftedWithDisc(resultStack)
            if (craftedWithDisc.isEmpty) {
                return@runAfterDelay helper.fail("Result expected to source with ${uniqueDisc.displayName}, but empty!")
            }

            if (craftedWithDisc.displayName != uniqueDisc.displayName) {
                return@runAfterDelay helper.fail("Result expected to source with ${uniqueDisc.displayName}, but instead got ${craftedWithDisc.displayName}")
            }

            helper.succeed()
        }
    }
}
