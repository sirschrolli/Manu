package io.github.sirschrolli.kingdomsatwar.registry;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Hallen der Tiefe (seit 1.15): vier Bauwerke tief unter der Oberwelt – die Gruft der Vergessenen, die Brutgrotte, die
 * Zwergenhalle und die Trollhöhle – mit je einem Mini-Boss. Hier stehen ihre Spawn-Eier (im Kreativ-Tab „Legenden“); die
 * Wesen selbst stehen in {@link ModEntities}, die Bauwerke in {@code worldgen.site}.
 */
public final class DeepLairs {
    private DeepLairs() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    public static final DeferredItem<SpawnEggItem> BROOD_MOTHER_SPAWN_EGG = egg("brood_mother", () -> ModEntities.BROOD_MOTHER.get());
    public static final DeferredItem<SpawnEggItem> CRYPT_KING_SPAWN_EGG = egg("crypt_king", () -> ModEntities.CRYPT_KING.get());
    public static final DeferredItem<SpawnEggItem> RUNE_WARDEN_SPAWN_EGG = egg("rune_warden", () -> ModEntities.RUNE_WARDEN.get());
    public static final DeferredItem<SpawnEggItem> TROLL_FATHER_SPAWN_EGG = egg("troll_father", () -> ModEntities.TROLL_FATHER.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(BROOD_MOTHER_SPAWN_EGG, CRYPT_KING_SPAWN_EGG, RUNE_WARDEN_SPAWN_EGG, TROLL_FATHER_SPAWN_EGG);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        DeferredItem<SpawnEggItem> item = ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
        ForgeBlocks.TAB.add(item);
        return item;
    }
}
