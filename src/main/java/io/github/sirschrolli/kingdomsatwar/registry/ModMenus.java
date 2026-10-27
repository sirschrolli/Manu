package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.magic.MagicTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Fenster mit Inventar (Menüs) der Mod. */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, KingdomsAtWar.MODID);

    /** Magietisch: Zaubertafeln in den Aetherit-Stab einschreiben. */
    public static final DeferredHolder<MenuType<?>, MenuType<MagicTableMenu>> MAGIC_TABLE =
            MENUS.register("magic_table", () -> IMenuTypeExtension.create(MagicTableMenu::new));

    private ModMenus() {}
}
