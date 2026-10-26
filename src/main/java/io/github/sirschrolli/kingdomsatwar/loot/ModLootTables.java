package io.github.sirschrolli.kingdomsatwar.loot;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/** Schlüssel für eigene Loot-Tables, die nicht zu einem Block gehören. */
public final class ModLootTables {
    /** Samen, die beim Abbauen von Gras zusätzlich fallen können. */
    public static final ResourceKey<LootTable> GRASS_SEEDS =
            ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("gameplay/grass_seeds"));
    /** Truhe in den Burgruinen. */
    public static final ResourceKey<LootTable> CASTLE_RUIN_CHEST =
            ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("chests/castle_ruin"));
    /** Truhe in der verfallenen Sturmwarte auf Voltarions Sturmgipfel. */
    public static final ResourceKey<LootTable> STORM_WATCHTOWER_CHEST =
            ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("chests/storm_watchtower"));
    /** Truhen am Thron der Tiefe in Thalassas Versunkener Grotte. */
    public static final ResourceKey<LootTable> SUNKEN_GROTTO_CHEST =
            ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("chests/sunken_grotto"));
    /** Was man mit dem Pinsel aus dem Kies eines Drachennests ausgräbt (je Art). */
    public static final Map<DragonType, ResourceKey<LootTable>> DRAGON_NEST_ARCHAEOLOGY;

    static {
        Map<DragonType, ResourceKey<LootTable>> nests = new EnumMap<>(DragonType.class);
        for (DragonType type : DragonType.values()) {
            nests.put(type, ResourceKey.create(Registries.LOOT_TABLE, KingdomsAtWar.id("archaeology/" + type.id() + "_dragon_nest")));
        }
        DRAGON_NEST_ARCHAEOLOGY = Collections.unmodifiableMap(nests);
    }

    private ModLootTables() {}
}
