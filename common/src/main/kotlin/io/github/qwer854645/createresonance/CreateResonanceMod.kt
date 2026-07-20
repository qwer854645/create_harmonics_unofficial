package io.github.qwer854645.createresonance

import com.simibubi.create.foundation.data.CreateRegistrate
import com.simibubi.create.foundation.item.ItemDescription
import com.simibubi.create.foundation.item.KineticStats
import com.simibubi.create.foundation.item.TooltipModifier
import io.github.qwer854645.createresonance.foundation.async.ModDispatchers
import io.github.qwer854645.createresonance.foundation.err
import io.github.qwer854645.createresonance.foundation.eventbus.autoHandler
import io.github.qwer854645.createresonance.foundation.registry.CommonRegistry
import io.github.qwer854645.createresonance.foundation.registry.PreFreezeCommonRegistry
import io.github.qwer854645.createresonance.foundation.registry.autoRegister
import io.github.qwer854645.createresonance.foundation.services.PlatformService
import io.github.qwer854645.createresonance.foundation.services.platformService
import io.github.qwer854645.createresonance.gui.CommonGuiEventHandler
import io.github.qwer854645.createresonance.handler.CommonEventHandler
import net.createmod.catnip.lang.FontHelper
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab

object CreateResonanceMod {
    const val MOD_ID = "create_resonance"
    private var initialized = false

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private val _registrate: CreateRegistrate =
        CreateRegistrate
            .create(MOD_ID)
            .defaultCreativeTab(null as ResourceKey<CreativeModeTab>?)
            .setTooltipModifierFactory { item ->
                ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                    .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            }

    val registrate: CreateRegistrate get() {
        if (!initialized) {
            throw IllegalStateException("Create registrate was not initialized!")
        }
        return _registrate
    }

    fun commonPreFreezeSetup(registry: Registry<*>) {
        autoRegister<PreFreezeCommonRegistry>(registry)
    }

    fun commonSetup(registrateConfiguration: CreateRegistrate.() -> Unit) {
        if (initialized) {
            return "Common was already initialized".err()
        }
        initialized = true
        _registrate.registrateConfiguration()
        ModDispatchers.setupEvents()
        autoRegister<CommonRegistry>()
        // GUI handlers reference Screen / Minecraft — client only.
        if (platformService isEnvironment PlatformService.Environment.CLIENT) {
            autoHandler<CommonGuiEventHandler>()
        }
        autoHandler<CommonEventHandler>()
    }
}

val ModRegistrate: CreateRegistrate = CreateResonanceMod.registrate
