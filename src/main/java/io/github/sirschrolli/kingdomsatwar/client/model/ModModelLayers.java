package io.github.sirschrolli.kingdomsatwar.client.model;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** Modell-Ebenen der Mob-Modelle. */
public final class ModModelLayers {
    public static final ModelLayerLocation DRAGON = new ModelLayerLocation(KingdomsAtWar.id("dragon"), "main");
    /** Drachenpanzer (gleiche Knochen wie der Drache). */
    public static final ModelLayerLocation DRAGON_ARMOR = new ModelLayerLocation(KingdomsAtWar.id("dragon"), "armor");
    /** Soldaten und Banditen. */
    public static final ModelLayerLocation SOLDIER = new ModelLayerLocation(KingdomsAtWar.id("soldier"), "main");
    /** Bürger der eigenen Siedlung. */
    public static final ModelLayerLocation CITIZEN = new ModelLayerLocation(KingdomsAtWar.id("citizen"), "main");

    public static final ModelLayerLocation WILD_BOAR = new ModelLayerLocation(KingdomsAtWar.id("wild_boar"), "main");
    public static final ModelLayerLocation DEER = new ModelLayerLocation(KingdomsAtWar.id("deer"), "main");
    public static final ModelLayerLocation GOOSE = new ModelLayerLocation(KingdomsAtWar.id("goose"), "main");
    public static final ModelLayerLocation WILL_O_WISP = new ModelLayerLocation(KingdomsAtWar.id("will_o_wisp"), "main");
    public static final ModelLayerLocation CAVE_TROLL = new ModelLayerLocation(KingdomsAtWar.id("cave_troll"), "main");
    public static final ModelLayerLocation LINDWORM = new ModelLayerLocation(KingdomsAtWar.id("lindworm"), "main");

    /** Drachen-Bosse. */
    public static final ModelLayerLocation NYXAR = new ModelLayerLocation(KingdomsAtWar.id("shadow_dragon_boss"), "main");
    public static final ModelLayerLocation PYRRHON = new ModelLayerLocation(KingdomsAtWar.id("fire_dragon_boss"), "main");
    public static final ModelLayerLocation GLACIARA = new ModelLayerLocation(KingdomsAtWar.id("ice_dragon_boss"), "main");
    public static final ModelLayerLocation VERDANTIS = new ModelLayerLocation(KingdomsAtWar.id("nature_dragon_boss"), "main");
    public static final ModelLayerLocation TERRAGON = new ModelLayerLocation(KingdomsAtWar.id("earth_dragon_boss"), "main");
    public static final ModelLayerLocation VOLTARION = new ModelLayerLocation(KingdomsAtWar.id("lightning_dragon_boss"), "main");
    public static final ModelLayerLocation THALASSA = new ModelLayerLocation(KingdomsAtWar.id("water_dragon_boss"), "main");
    /** Rankling (Diener von Verdantis). */
    public static final ModelLayerLocation VINELING = new ModelLayerLocation(KingdomsAtWar.id("vineling"), "main");
    // Kreaturen der Schattenwelt
    public static final ModelLayerLocation SHADE_STALKER = new ModelLayerLocation(KingdomsAtWar.id("shade_stalker"), "main");
    public static final ModelLayerLocation CRYSTAL_CRAWLER = new ModelLayerLocation(KingdomsAtWar.id("crystal_crawler"), "main");
    public static final ModelLayerLocation NIGHT_WING = new ModelLayerLocation(KingdomsAtWar.id("night_wing"), "main");
    public static final ModelLayerLocation GLIMMER_MOTH = new ModelLayerLocation(KingdomsAtWar.id("glimmer_moth"), "main");
    public static final ModelLayerLocation UMBRER = new ModelLayerLocation(KingdomsAtWar.id("umbrer"), "main");

    private ModModelLayers() {}
}
