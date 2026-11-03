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
    HOUSE(TownTier.CAMP, 54, 2, 0),
    FIELD(TownTier.CAMP, 27, 0, 1),
    LUMBER_HUT(TownTier.CAMP, 45, 1, 1),
    STOREHOUSE(TownTier.CAMP, 27, 0, 0),
    WELL(TownTier.HAMLET, 36, 0, 0),
    SMITHY(TownTier.HAMLET, 108, 1, 1),
    MINE(TownTier.VILLAGE, 108, 0, 2),
    MARKET(TownTier.VILLAGE, 135, 0, 2),
    TAVERN(TownTier.VILLAGE, 135, 2, 1),
    TOWNHOUSE(TownTier.MARKET_TOWN, 135, 4, 0),
    BARRACKS(TownTier.MARKET_TOWN, 243, 0, 0),
    MAP_HOUSE(TownTier.MARKET_TOWN, 189, 1, 1),
    THRONE_HALL(TownTier.CITY, 567, 0, 0),
    /** Ein eigener Bauplan (Dragon Architect): Preis, Betten und Baustoffe stehen im Plan selbst ({@link io.github.sirschrolli.kingdomsatwar.architect.PlanInfo}). */
    CUSTOM(TownTier.CAMP, 0, 0, 0);

    public static final Codec<BuildingType> CODEC = StringRepresentable.fromEnum(BuildingType::values);
    private static final Map<BuildingType, Blueprint[]> PLANS = new EnumMap<>(BuildingType.class);

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
            case STOREHOUSE -> Items.CHEST;
            case WELL -> Items.WATER_BUCKET;
            case SMITHY -> Items.ANVIL;
            case MINE -> Items.IRON_PICKAXE;
            case MARKET -> Items.BARREL;
            case TAVERN -> io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.FEAST.get().asItem();
            case TOWNHOUSE -> ModBlocks.TIMBER_FRAME.get().asItem();
            case BARRACKS -> Items.IRON_SWORD;
            case MAP_HOUSE -> Items.CARTOGRAPHY_TABLE;
            case THRONE_HALL -> ModBlocks.THRONE.get().asItem();
            case CUSTOM -> io.github.sirschrolli.kingdomsatwar.registry.ModItems.SURVEYORS_ROD.get();
        };
    }

    /** Ein eigener Bauplan (Dragon Architect)? Der steht nicht in der Liste der Baupläne der Siedlung. */
    public boolean custom() {
        return this == CUSTOM;
    }

    /** Die Bauten, die das Stadtfenster zum Nehmen anbietet (alle außer den eigenen Plänen). */
    public static java.util.List<BuildingType> standard() {
        java.util.List<BuildingType> list = new java.util.ArrayList<>(java.util.List.of(values()));
        list.remove(CUSTOM);
        return list;
    }

    /** Kostet der Bau Baustoffe? Das Lagerhaus nicht – es ist das erste, was eine Siedlung braucht. */
    public boolean costsMaterials() {
        return this != STOREHOUSE;
    }

    /** Was der Bau an Baustoffen kostet (Einheiten je {@link Material}; im ersten Entwurf). */
    public int[] costs() {
        return this.costs(0);
    }

    /** Was ein Entwurf an Baustoffen kostet. */
    public int[] costs(int design) {
        return this.costsMaterials() ? this.blueprint(design).costs() : new int[Material.values().length];
    }

    /** Die meisten Baustoffe, die einer der Entwürfe braucht (je Baustoff), für die Anzeige. */
    public int[] maxCosts() {
        int[] max = new int[Material.values().length];
        for (int d = 0; d < this.designs(); d++) {
            int[] c = this.costs(d);
            for (int i = 0; i < max.length; i++) {
                max[i] = Math.max(max[i], c[i]);
            }
        }
        return max;
    }

    /** Die wenigsten Baustoffe, die einer der Entwürfe braucht (je Baustoff). */
    public int[] minCosts() {
        int[] min = this.costs(0);
        for (int d = 1; d < this.designs(); d++) {
            int[] c = this.costs(d);
            for (int i = 0; i < min.length; i++) {
                min[i] = Math.min(min[i], c[i]);
            }
        }
        return min;
    }

    /** Größte Grundfläche (x, z) über alle Entwürfe. */
    public int[] maxSize() {
        int[] size = new int[2];
        for (int d = 0; d < this.designs(); d++) {
            var b = this.blueprint(d).bounds();
            size[0] = Math.max(size[0], b.getXSpan());
            size[1] = Math.max(size[1], b.getZSpan());
        }
        return size;
    }

    /** Wie viele Entwürfe es gibt; beim Nehmen eines Bauplans wird einer davon zufällig gewählt. */
    public int designs() {
        return this.plans().length;
    }

    /** Übersetzungsschlüssel für den Namen eines Entwurfs. */
    public String designKey(int design) {
        return "building.kingdomsatwar." + this.id() + ".design." + Math.floorMod(design, this.designs());
    }

    /** Der Bauplan des ersten Entwurfs. */
    public Blueprint blueprint() {
        return this.blueprint(0);
    }

    /** Der Bauplan eines Entwurfs (einmal erzeugt, dann zwischengespeichert). */
    public Blueprint blueprint(int design) {
        Blueprint[] plans = this.plans();
        return plans[Math.floorMod(design, plans.length)];
    }

    private Blueprint[] plans() {
        synchronized (PLANS) {
            return PLANS.computeIfAbsent(this, Blueprints::createAll);
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
