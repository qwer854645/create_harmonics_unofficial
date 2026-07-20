package io.github.qwer854645.createresonance.foundation.services

import com.simibubi.create.content.contraptions.AbstractContraptionEntity
import com.simibubi.create.foundation.utility.AdventureUtil
import io.github.qwer854645.createresonance.audio.instance.SimpleStreamSoundInstance
import io.github.qwer854645.createresonance.audio.instance.StreamingSoundInstance
import io.github.qwer854645.createresonance.content.processing.recordPressBase.RecordPressBaseBlockEntity
import io.github.qwer854645.createresonance.foundation.behaviour.movement.handleBlockDataChange
import io.github.qwer854645.createresonance.foundation.registry.ModPonders
import io.github.qwer854645.createresonance.foundation.supplier.values.FloatSupplier
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.level.material.FluidState
import java.io.InputStream

class NeoforgeContentService : ContentService {
    override fun getViscosity(fluidState: FluidState): Int = fluidState.fluidType.viscosity
}
