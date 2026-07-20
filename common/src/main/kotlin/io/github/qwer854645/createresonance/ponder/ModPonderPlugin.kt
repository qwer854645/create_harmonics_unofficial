package io.github.qwer854645.createresonance.ponder

import com.simibubi.create.foundation.ponder.PonderWorldBlockEntityFix
import io.github.qwer854645.createresonance.CreateResonanceMod
import io.github.qwer854645.createresonance.foundation.registry.ModPonders
import io.github.qwer854645.createresonance.foundation.services.contentService
import net.createmod.ponder.api.level.PonderLevel
import net.createmod.ponder.api.registration.PonderPlugin
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper
import net.minecraft.resources.ResourceLocation

class ModPonderPlugin : PonderPlugin {
    override fun getModId(): String = CreateResonanceMod.MOD_ID

    override fun registerScenes(helper: PonderSceneRegistrationHelper<ResourceLocation>) {
        ModPonders.addScenes(helper)
    }

    override fun registerTags(helper: PonderTagRegistrationHelper<ResourceLocation>) {
        ModPonders.addTags(helper)
    }

    override fun onPonderLevelRestore(ponderLevel: PonderLevel) {
        PonderWorldBlockEntityFix.fixControllerBlockEntities(ponderLevel)
    }
}
