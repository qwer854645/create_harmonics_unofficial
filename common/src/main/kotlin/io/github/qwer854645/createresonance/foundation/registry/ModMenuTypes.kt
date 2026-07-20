package io.github.qwer854645.createresonance.foundation.registry

import com.tterrag.registrate.builders.MenuBuilder
import com.tterrag.registrate.util.entry.MenuEntry
import com.tterrag.registrate.util.nullness.NonNullSupplier
import io.github.qwer854645.createresonance.ModRegistrate
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxMenu
import io.github.qwer854645.createresonance.content.kinetics.musicBox.MusicBoxScreen
import io.github.qwer854645.createresonance.foundation.info
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.core.Registry
import net.minecraft.world.inventory.AbstractContainerMenu

/**
 * Menu types register on both sides (Create does the same with Screen factories).
 * Screen classes are only constructed on the client when a menu is opened.
 */
object ModMenuTypes : CommonRegistry {
    override val registrationOrder = 4

    val MUSIC_BOX: MenuEntry<MusicBoxMenu> =
        register(
            "music_box",
            { type, id, inv, buf -> MusicBoxMenu(type, id, inv, buf!!) },
            NonNullSupplier {
                MenuBuilder.ScreenFactory { menu, inv, title -> MusicBoxScreen(menu, inv, title) }
            },
        )

    private fun <C : AbstractContainerMenu, S> register(
        name: String,
        factory: MenuBuilder.ForgeMenuFactory<C>,
        screenFactory: NonNullSupplier<MenuBuilder.ScreenFactory<C, S>>,
    ): MenuEntry<C> where S : Screen, S : MenuAccess<C> =
        ModRegistrate
            .menu(name, factory, screenFactory)
            .register()

    override fun register(registry: Registry<*>?) {
        "Registering menu types".info()
    }
}
