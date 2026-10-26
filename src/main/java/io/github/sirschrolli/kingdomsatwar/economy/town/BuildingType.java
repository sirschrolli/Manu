package io.github.sirschrolli.kingdomsatwar.economy.town;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Die Bauten, die der Baumeister einer Siedlung errichten kann. Jeder Bau hat eine Stufe, ab der er erlaubt ist, einen
 * Preis (in Bronze), Betten für Bewohner und Arbeitsplätze für einen Beruf; den Bauplan liefert {@link Blueprints}.
 */
public enum BuildingType implements StringRepresentable {
    HOUSE(TownTier.CAMP, 60, 2, 0),
    FIELD(TownTier.CAMP, 30, 0, 1),
    LUMBER_HUT(TownTier.CAMP, 50, 1, 1),
    WELL(TownTier.HAMLET, 40, 0, 0),
    SMITHY(TownTier.HAMLET, 120, 1, 1),
    MINE(TownTier.VILLAGE, 120, 0, 2),
    MARKET(TownTier.VILLAGE, 150, 0, 2),
    TAVERN(TownTier.VILLAGE, 150, 2, 1),
    TOWNHOUSE(TownTier.MARKET_TOWN, 150, 4, 0),
    BARRACKS(TownTier.MARKET_TOWN, 250, 0, 0),
    MAP_HOUSE(TownTier.MARKET_TOWN, 200, 1, 1),
    THRONE_HALL(TownTier.CITY, 600, 0, 0);

    public static final Codec<BuildingType> CODEC = StringRepresentable.fromEnum(BuildingType::values);
    private static final Map<BuildingType, Blueprint> PLANS = new EnumMap<>(BuildingType.class);

    private final TownTier tier;
    private final int price;
    private final int beds;
    private final int jobs;

    BuildingType(TownTier tier, int price, int beds, int jobs) {
        this.tier = tier;
        this.price = price;
        this.beds = beds;
        this.jobs = jobs;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    public String translationKey() {
        return "building.kingdomsatwar." + this.id();
    }

    public String descriptionKey() {
        return "building.kingdomsatwar." + this.id() + ".description";
    }

    /** Ab welcher Stufe der Siedlung der Bau erlaubt ist. */
    public TownTier tier() {
        return this.tier;
    }

    /** Preis in Bronze. */
    public int price() {
        return this.price;
    }

    /** Betten für Bewohner. */
    public int beds() {
        return this.beds;
    }

    /** Arbeitsplätze für den Beruf, der hier arbeitet. */
    public int jobs() {
        return this.jobs;
    }

    /** Bild im Stadtfenster. */
    public Item icon() {
        return switch (this) {
            case HOUSE -> Items.OAK_DOOR;
            case FIELD -> Items.WHEAT;
            case LUMBER_HUT -> Items.OAK_LOG;
            case WELL -> Items.WATER_BUCKET;
            case SMITHY -> Items.ANVIL;
            case MINE -> Items.IRON_PICKAXE;
            case MARKET -> Items.BARREL;
            case TAVERN -> io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.FEAST.get().asItem();
            case TOWNHOUSE -> ModBlocks.TIMBER_FRAME.get().asItem();
            case BARRACKS -> Items.IRON_SWORD;
            case MAP_HOUSE -> Items.CARTOGRAPHY_TABLE;
            case THRONE_HALL -> ModBlocks.THRONE.get().asItem();
        };
    }

    /** Der Bauplan (einmal erzeugt, dann zwischengespeichert). */
    public Blueprint blueprint() {
        synchronized (PLANS) {
            return PLANS.computeIfAbsent(this, type -> Blueprints.create(type));
        }
    }

    /** Für Tests: Baupläne neu erzeugen lassen. */
    static void clearCache() {
        synchronized (PLANS) {
            PLANS.clear();
        }
    }

    static Supplier<Blueprint> plan(BuildingType type) {
        return type::blueprint;
    }
}
