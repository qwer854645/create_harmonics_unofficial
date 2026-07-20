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
 * Opens the seek GUI on side faces before Create's ValueSettings handler
 * can claim the click for the playback-mode scroll box.
 *
 * Empty hand or sneak + side face → seek. Mode scroll still works when
 * holding an item (non-wrench) and clicking the value box.
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
        val hitFace = event.face ?: return
        if (!isSideSeekFace(hitFace, facing)) return

        // Let Create's mode ValueBox handle item clicks; empty hand / sneak opens seek.
        if (!held.isEmpty && !player.isShiftKeyDown) return

        if (event.side == LogicalSide.CLIENT) {
            val be = world.getBlockEntity(pos) as? RecordPlayerBlockEntity ?: return
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
