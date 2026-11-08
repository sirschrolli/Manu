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
    /** Rangabzeichen der Soldaten (Schärpe, Ärmelstreifen, Schulterstücke) knapp über der Rüstung. */
    public static final ModelLayerLocation SOLDIER_RANK = new ModelLayerLocation(KingdomsAtWar.id("soldier"), "rank");
    /** Bürger der eigenen Siedlung. */
    public static final ModelLayerLocation CITIZEN = new ModelLayerLocation(KingdomsAtWar.id("citizen"), "main");

    /** Eigene Hof- und Weidetiere. */
    public static final ModelLayerLocation CATTLE = new ModelLayerLocation(KingdomsAtWar.id("cattle"), "main");
    public static final ModelLayerLocation FARM_PIG = new ModelLayerLocation(KingdomsAtWar.id("pig"), "main");
    public static final ModelLayerLocation FARM_SHEEP = new ModelLayerLocation(KingdomsAtWar.id("sheep"), "main");
    public static final ModelLayerLocation FARM_CHICKEN = new ModelLayerLocation(KingdomsAtWar.id("chicken"), "main");
    public static final ModelLayerLocation FARM_GOAT = new ModelLayerLocation(KingdomsAtWar.id("goat"), "main");
    public static final ModelLayerLocation HARE = new ModelLayerLocation(KingdomsAtWar.id("hare"), "main");
    public static final ModelLayerLocation FARM_HORSE = new ModelLayerLocation(KingdomsAtWar.id("horse"), "main");
    public static final ModelLayerLocation FARM_DONKEY = new ModelLayerLocation(KingdomsAtWar.id("donkey"), "main");
    public static final ModelLayerLocation FARM_MULE = new ModelLayerLocation(KingdomsAtWar.id("mule"), "main");

    public static final ModelLayerLocation WILD_BOAR = new ModelLayerLocation(KingdomsAtWar.id("wild_boar"), "main");
    public static final ModelLayerLocation DEER = new ModelLayerLocation(KingdomsAtWar.id("deer"), "main");
    public static final ModelLayerLocation GOOSE = new ModelLayerLocation(KingdomsAtWar.id("goose"), "main");
    public static final ModelLayerLocation WILL_O_WISP = new ModelLayerLocation(KingdomsAtWar.id("will_o_wisp"), "main");
    public static final ModelLayerLocation CAVE_TROLL = new ModelLayerLocation(KingdomsAtWar.id("cave_troll"), "main");
    public static final ModelLayerLocation LINDWORM = new ModelLayerLocation(KingdomsAtWar.id("lindworm"), "main");

    /** Zauberwirkungen und Zaubergeschosse. */
    public static final ModelLayerLocation PHOENIX = new ModelLayerLocation(KingdomsAtWar.id("phoenix"), "main");
    public static final ModelLayerLocation ICE_LANCE = new ModelLayerLocation(KingdomsAtWar.id("ice_lance"), "main");
    public static final ModelLayerLocation ICE_PRISON = new ModelLayerLocation(KingdomsAtWar.id("ice_prison"), "main");
    public static final ModelLayerLocation STONE_SPIKE = new ModelLayerLocation(KingdomsAtWar.id("stone_spike"), "main");
    public static final ModelLayerLocation STONE_FIST = new ModelLayerLocation(KingdomsAtWar.id("stone_fist"), "main");
    public static final ModelLayerLocation THORN_VINE = new ModelLayerLocation(KingdomsAtWar.id("thorn_vine"), "main");
    public static final ModelLayerLocation POISON_FLOWER = new ModelLayerLocation(KingdomsAtWar.id("poison_flower"), "main");

    /** Belagerungswaffen und ihre Geschosse. */
    public static final ModelLayerLocation CATAPULT = new ModelLayerLocation(KingdomsAtWar.id("catapult"), "main");
    public static final ModelLayerLocation TREBUCHET = new ModelLayerLocation(KingdomsAtWar.id("trebuchet"), "main");
    public static final ModelLayerLocation BALLISTA = new ModelLayerLocation(KingdomsAtWar.id("ballista"), "main");
    public static final ModelLayerLocation BATTERING_RAM = new ModelLayerLocation(KingdomsAtWar.id("battering_ram"), "main");
    public static final ModelLayerLocation SIEGE_TOWER = new ModelLayerLocation(KingdomsAtWar.id("siege_tower"), "main");
    public static final ModelLayerLocation WAGON = new ModelLayerLocation(KingdomsAtWar.id("wagon"), "main");
    public static final ModelLayerLocation SIEGE_STONE = new ModelLayerLocation(KingdomsAtWar.id("siege_stone"), "main");
    public static final ModelLayerLocation BALLISTA_BOLT = new ModelLayerLocation(KingdomsAtWar.id("ballista_bolt"), "main");

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
    // Wesen der Leere
    public static final ModelLayerLocation VOID_MAW = new ModelLayerLocation(KingdomsAtWar.id("void_maw"), "main");
    public static final ModelLayerLocation SHARD_WARDEN = new ModelLayerLocation(KingdomsAtWar.id("shard_warden"), "main");
    public static final ModelLayerLocation DISTORTLING = new ModelLayerLocation(KingdomsAtWar.id("distortling"), "main");
    public static final ModelLayerLocation STAR_RAY = new ModelLayerLocation(KingdomsAtWar.id("star_ray"), "main");
    public static final ModelLayerLocation WARDEN_SHARD = new ModelLayerLocation(KingdomsAtWar.id("warden_shard"), "main");
    public static final ModelLayerLocation SHATTERED_KING = new ModelLayerLocation(KingdomsAtWar.id("shattered_king"), "main");
    public static final ModelLayerLocation SHATTERED_CROWN = new ModelLayerLocation(KingdomsAtWar.id("shattered_crown"), "main");
    /** Erzmagier (alle Elemente, Zubehör je Element). */
    public static final ModelLayerLocation MAGE = new ModelLayerLocation(KingdomsAtWar.id("mage"), "main");

    private ModModelLayers() {}
}
