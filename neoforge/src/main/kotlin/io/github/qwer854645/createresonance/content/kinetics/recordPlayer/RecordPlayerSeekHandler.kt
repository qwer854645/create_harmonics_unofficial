package io.github.qwer854645.createresonance.content.kinetics.recordPlayer

import com.simibubi.create.AllItems
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock
import io.github.qwer854645.createresonance.CreateResonanceMod.MOD_ID
import io.github.qwer854645.createresonance.content.kinetics.recordPlayer.kineticNetworkJukebox.KineticNetworkJukeboxBlock
import io.github.qwer854645.createresonance.foundation.registry.ModBlocks
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.LogicalSide
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent

/**
 * Opens the seek GUI on side faces, but never when the playback-mode value box is hit —
 * otherwise empty-hand clicks steal Create's scroll / ValueSettings interaction.
 *
 * Holding a non-wrench item on the mode box still goes to Create (this handler returns early).
 */
@EventBusSubscriber(modid = MOD_ID)
object RecordPlayerSeekHandler {
    @JvmStatic
    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onBlockActivated(event: PlayerInteractEvent.RightClickBlock) {
        val player = event.entity
        if (player.isSpectator) return

        val world = event.level
        val pos = event.pos
        val state = world.getBlockState(pos)
        if (!state.`is`(ModBlocks.KINETIC_NETWORK_JUKEBOX.get())) return
        if (state.block !is KineticNetworkJukeboxBlock) return

        val held = player.getItemInHand(event.hand)
        if (AllItems.WRENCH.isIn(held)) return

        val facing = state.getValue(DirectionalKineticBlock.FACING)
        val hit = event.hitVec ?: return
        val hitFace = event.face ?: hit.direction
        if (!isSideSeekFace(hitFace, facing)) return

        // Mode scroll / hold-to-configure must win over seek UI.
        val be = world.getBlockEntity(pos) as? RecordPlayerBlockEntity ?: return
        if (be.hitsPlaybackModeSlot(hit)) return

        // Let Create's mode ValueBox handle item clicks; empty hand / sneak opens seek.
        if (!held.isEmpty && !player.isShiftKeyDown) return

        if (event.side == LogicalSide.CLIENT) {
            RecordPlayerClientHandler.openSeekScreen(be)
        }

        event.isCanceled = true
        event.cancellationResult = InteractionResult.SUCCESS
    }

    private fun isSideSeekFace(
        hitFace: Direction,
        facing: Direction,
    ): Boolean = hitFace != facing && hitFace != facing.opposite
}
