package io.github.sirschrolli.kingdomsatwar.loot;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/** Schlüssel für eigene Loot-Tables, die nicht zu einem Block gehören. */
public final class ModLootTables {
    /** Samen, die beim Abbauen von Gras zusätzlich fallen können. */
    public static final ResourceKey<LootTable> GRASS_SEEDS =
            ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("gameplay/grass_seeds"));

    private ModLootTables() {}
}
