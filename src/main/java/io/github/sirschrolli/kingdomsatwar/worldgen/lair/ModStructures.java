package io.github.sirschrolli.kingdomsatwar.worldgen.lair;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Bauwerke der Mod: Bauwerk-Arten, Bauteile und Platzierungen (Registrierung beim Start) sowie die Einträge
 * {@code worldgen/structure} und {@code worldgen/structure_set} (bei der Datengenerierung).
 */
public final class ModStructures {
    public static final DeferredRegister<MapCodec<? extends StructurePlacement>> PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, KingdomsAtWar.MODID);
    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, KingdomsAtWar.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, KingdomsAtWar.MODID);

    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<UniqueNestPlacement>> UNIQUE_NEST =
            PLACEMENTS.register("unique_nest", () -> UniqueNestPlacement.CODEC);
    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<AnyNestPlacement>> ANY_NEST =
            PLACEMENTS.register("any_nest", () -> AnyNestPlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<DragonLairStructure>> DRAGON_LAIR =
            TYPES.register("dragon_lair", () -> () -> DragonLairStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> DRAGON_LAIR_PIECE =
            PIECES.register("dragon_lair", () -> (StructurePieceType.ContextlessType) DragonLairPiece::new);

    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCirclePlacement>>
            SEAL_CIRCLE_PLACEMENT = PLACEMENTS.register("seal_circle", () -> io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCirclePlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleStructure>> SEAL_CIRCLE =
            TYPES.register("seal_circle", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SEAL_CIRCLE_PIECE =
            PIECES.register("seal_circle", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCirclePiece::new);

    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePlacement>>
            REALM_CASTLE_PLACEMENT = PLACEMENTS.register("realm_castle",
                    () -> io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastleStructure>>
            REALM_CASTLE = TYPES.register("realm_castle", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastleStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> REALM_CASTLE_PIECE =
            PIECES.register("realm_castle", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure>>
            UMBRER_VILLAGE = TYPES.register("umbrer_village", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> UMBRER_VILLAGE_PIECE =
            PIECES.register("umbrer_village", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece::new);

    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPlacement>>
            CITADEL_PLACEMENT = PLACEMENTS.register("shadow_citadel", () -> io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelStructure>>
            SHADOW_CITADEL = TYPES.register("shadow_citadel", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SHADOW_CITADEL_PIECE =
            PIECES.register("shadow_citadel", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPiece::new);
    /** Thron der Alten: einmal in der Leere, der Platz des Zerbrochenen Königs. */
    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement>>
            VOID_THRONE_PLACEMENT = PLACEMENTS.register("void_throne", () -> io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement.CODEC);
    // das Versunkene Atoll (0.62): einmal je Welt im Meer, Boss Karkinos
    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPlacement>>
            SUNKEN_ATOLL_PLACEMENT = PLACEMENTS.register("sunken_atoll", () -> io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollStructure>>
            SUNKEN_ATOLL = TYPES.register("sunken_atoll", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SUNKEN_ATOLL_PIECE =
            PIECES.register("sunken_atoll", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPiece::new);
    // der Krakenschlund (0.91): einmal im Urmeer, Boss der Kraken
    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawPlacement>>
            KRAKEN_MAW_PLACEMENT = PLACEMENTS.register("kraken_maw", () -> io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawPlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawStructure>>
            KRAKEN_MAW = TYPES.register("kraken_maw", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> KRAKEN_MAW_PIECE =
            PIECES.register("kraken_maw", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawPiece::new);
    // die Meeresdörfer (0.93): das Meervolk auf dem Grund des Urmeers
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.merfolk.SeaVillageStructure>>
            SEA_VILLAGE = TYPES.register("sea_village", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.merfolk.SeaVillageStructure.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoStructure>>
            WITCH_GROTTO = TYPES.register("witch_grotto", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> WITCH_GROTTO_PIECE =
            PIECES.register("witch_grotto", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoPiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SEA_VILLAGE_PIECE =
            PIECES.register("sea_village", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.merfolk.SeaVillagePiece::new);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneStructure>>
            VOID_THRONE = TYPES.register("void_throne", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> VOID_THRONE_PIECE =
            PIECES.register("void_throne", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePiece::new);
    // der Schlund der Welten (0.73): der Hort des Weltenfressers im Abgrund
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardStructure>>
            WORLD_EATER_HOARD = TYPES.register("world_eater_hoard", () -> () -> io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> WORLD_EATER_HOARD_PIECE =
            PIECES.register("world_eater_hoard", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure>>
            SHADOW_SITE = TYPES.register("shadow_site", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SHADOW_SITE_PIECE =
            PIECES.register("shadow_site", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSitePiece::new);

    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPlacement>>
            MAGE_TOWER_PLACEMENT = PLACEMENTS.register("mage_tower", () -> io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerStructure>>
            MAGE_TOWER = TYPES.register("mage_tower", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> MAGE_TOWER_PIECE =
            PIECES.register("mage_tower", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPiece::new);

    public static final DeferredHolder<MapCodec<? extends StructurePlacement>, MapCodec<io.github.sirschrolli.kingdomsatwar.worldgen.village.VillagePlacement>>
            VILLAGE_PLACEMENT = PLACEMENTS.register("village", () -> io.github.sirschrolli.kingdomsatwar.worldgen.village.VillagePlacement.CODEC);
    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure>>
            VILLAGE = TYPES.register("village", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> VILLAGE_PIECE =
            PIECES.register("village", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece::new);

    public static final DeferredHolder<StructureType<?>, StructureType<io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteStructure>>
            SITE = TYPES.register("site", () -> () -> io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SITE_PIECE =
            PIECES.register("site", () -> (StructurePieceType.ContextlessType) io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece::new);

    /** Ein eigenes Bauwerk der Oberwelt (an Stelle eines Vanilla-Bauwerks). */
    public static ResourceKey<Structure> site(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind kind) {
        return ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id(kind.id()));
    }

    public static ResourceKey<StructureSet> siteSet(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind kind) {
        return ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id(kind.id()));
    }

    /** Ein Dorf der jeweiligen Bauweise. */
    public static ResourceKey<Structure> village(io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle style) {
        return ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("village_" + style.id()));
    }

    /** Alle Dörfer teilen sich eine Verteilung (wie die Dörfer früher): je Feld höchstens eins, nie nahe der Nester. */
    public static final ResourceKey<StructureSet> VILLAGES = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("villages"));

    /** Turm des Erzmagiers eines Elements. */
    public static ResourceKey<Structure> mageTower(DragonType type) {
        return ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id(type.id() + "_mage_tower"));
    }

    /** Alle sechs Magiertürme teilen sich eine Verteilung: je Feld entsteht höchstens einer – der, dessen Land dort liegt. */
    public static final ResourceKey<StructureSet> MAGE_TOWERS = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("mage_towers"));

    /** Bauwerk einer Art der Schattenwelt (Mondtempel, Kristallmine, Leuchtturm, Ruine der alten Welt). */
    public static ResourceKey<Structure> shadowSite(io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind kind) {
        return ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id(kind.id()));
    }

    /**
     * Alle kleineren Bauwerke der Schattenwelt teilen sich eine Gruppe: je Feld der Verteilung entsteht höchstens eines
     * (passt die gewürfelte Art nicht ins Gelände, wird die nächste versucht) – so überschneiden sie sich nie.
     */
    public static final ResourceKey<StructureSet> SHADOW_SITES_SET = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("shadow_sites"));

    /** Biome, Abstand (Chunks), Mindestabstand und Startwert je Art. */
    private static TagKey<Biome> siteBiomes(io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind kind) {
        return switch (kind) {
            case TEMPLE -> ModTags.Biomes.HAS_MOON_TEMPLE;
            case MINE -> ModTags.Biomes.HAS_CRYSTAL_MINE;
            case LIGHTHOUSE -> ModTags.Biomes.HAS_LIGHTHOUSE;
            case RUIN -> ModTags.Biomes.HAS_OLD_WORLD_RUIN;
        };
    }

    /** Gewicht der Art beim Würfeln (Leuchttürme etwas öfter, weil sie nur an der Küste passen). */
    private static int siteWeight(io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind kind) {
        return switch (kind) {
            case TEMPLE, MINE, RUIN -> 3;
            case LIGHTHOUSE -> 4;
        };
    }

    /** Nyxars Schattenzitadelle (einmal in der Schattenwelt). */
    public static final ResourceKey<Structure> SHADOW_CITADEL_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("shadow_citadel"));
    public static final ResourceKey<StructureSet> SHADOW_CITADEL_SET = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("shadow_citadel"));
    public static final ResourceKey<Structure> VOID_THRONE_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("void_throne"));
    public static final ResourceKey<StructureSet> VOID_THRONE_SET = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("void_throne"));
    public static final ResourceKey<Structure> WORLD_EATER_HOARD_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("world_eater_hoard"));
    public static final ResourceKey<StructureSet> WORLD_EATER_HOARD_SET = ResourceKey.create(Registries.STRUCTURE_SET,
            KingdomsAtWar.id("world_eater_hoard"));
    public static final ResourceKey<Structure> SUNKEN_ATOLL_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("sunken_atoll"));
    public static final ResourceKey<StructureSet> SUNKEN_ATOLL_SET = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("sunken_atoll"));
    public static final ResourceKey<Structure> KRAKEN_MAW_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("kraken_maw"));
    public static final ResourceKey<StructureSet> KRAKEN_MAW_SET = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("kraken_maw"));
    public static final ResourceKey<Structure> SEA_VILLAGE_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("sea_village"));
    public static final ResourceKey<StructureSet> SEA_VILLAGES = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("sea_villages"));
    public static final ResourceKey<Structure> WITCH_GROTTO_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("witch_grotto"));
    public static final ResourceKey<StructureSet> WITCH_GROTTOS = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("witch_grottos"));

    /** Die Dörfer der Umbrer in der Schattenwelt. */
    public static final ResourceKey<Structure> UMBRER_VILLAGE_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("umbrer_village"));
    public static final ResourceKey<StructureSet> UMBRER_VILLAGES = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("umbrer_villages"));

    /** Die Stammburgen der vier Reiche (ein Bauwerk: welches Reich dort herrscht, steht im Bauteil). */
    public static final ResourceKey<Structure> REALM_CASTLE_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("realm_castle"));
    public static final ResourceKey<StructureSet> REALM_CASTLES = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("realm_castles"));

    public static final ResourceKey<Structure> SEAL_CIRCLE_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("seal_circle"));
    public static final ResourceKey<StructureSet> SEAL_CIRCLE_SET = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("seal_circle"));

    /** Entfernungsring um den Weltursprung je Art (Natur am nächsten, Feuer am weitesten). */
    private static int minDistance(DragonType type) {
        return switch (type) {
            case NATURE -> 700;
            case WATER -> 800;
            case EARTH -> 900;
            case ICE -> 1000;
            case LIGHTNING -> 1100;
            case FIRE -> 1200;
        };
    }

    private ModStructures() {}

    public static ResourceKey<Structure> lair(DragonType type) {
        return ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id(type.id() + "_dragon_lair"));
    }

    /** Strukturgruppe ohne eigene Bauwerke, die alle Nester umfasst – Bezug für Sperrzonen (z. B. der Dörfer). */
    public static final ResourceKey<StructureSet> ALL_LAIRS = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("dragon_lairs"));

    public static ResourceKey<StructureSet> lairSet(DragonType type) {
        return ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id(type.id() + "_dragon_lair"));
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(UMBRER_VILLAGE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_UMBRER_VILLAGE))
                        .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind.values()) {
            context.register(shadowSite(kind), new io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure(
                    new Structure.StructureSettings.Builder(biomes.getOrThrow(siteBiomes(kind)))
                            .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                            .terrainAdapation(TerrainAdjustment.NONE)
                            .build(), kind));
        }
        context.register(SHADOW_CITADEL_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_SHADOW_CITADEL))
                        // ganz zum Schluss: so räumt die Zitadelle auch Bäume und Pflanzen auf ihrem Grund ab
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // am Schlund der Welten erscheinen keine Wesen von selbst (dort wartet der Weltenfresser)
        // ganz zum Schluss gebaut: so räumt der freie Raum auch Kristalle, Ranken und Bäume der Bruchstücke ab
        context.register(WORLD_EATER_HOARD_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_WORLD_EATER_HOARD))
                        .spawnOverrides(java.util.Map.of(
                                net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of()),
                                net.minecraft.world.entity.MobCategory.CREATURE, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // auf dem Thron der Alten erscheinen keine Wesen von selbst (dort wacht der König)
        context.register(VOID_THRONE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_VOID_THRONE))
                        .spawnOverrides(java.util.Map.of(
                                net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of()),
                                net.minecraft.world.entity.MobCategory.CREATURE, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // im Versunkenen Atoll erscheinen keine Monster von selbst (dort schläft Karkinos)
        context.register(SUNKEN_ATOLL_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_SUNKEN_ATOLL))
                        .spawnOverrides(java.util.Map.of(
                                net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // im Krakenschlund erscheinen keine Monster von selbst (dort schläft der Kraken)
        context.register(KRAKEN_MAW_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_KRAKEN_MAW))
                        .spawnOverrides(java.util.Map.of(
                                net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // die Hexengrotten (0.97): nach Tang und Seegras gebaut; in der Grotte entstehen keine Monster (dort wacht die Hexe)
        context.register(WITCH_GROTTO_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_WITCH_GROTTO))
                        .spawnOverrides(java.util.Map.of(
                                net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // die Meeresdörfer: nach Bäumen und Tang gebaut (die im Dorf weichen); keine Monster im Dorf
        context.register(SEA_VILLAGE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.merfolk.SeaVillageStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_SEA_VILLAGE))
                        .spawnOverrides(java.util.Map.of(
                                net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        // in den Mauern der Stammburgen entstehen keine Monster (dort wacht die Besatzung)
        context.register(REALM_CASTLE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastleStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
                        .spawnOverrides(java.util.Map.of(net.minecraft.world.entity.MobCategory.MONSTER,
                                new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                        net.minecraft.util.random.WeightedList.of())))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        context.register(SEAL_CIRCLE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
        for (var style : io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.values()) {
            context.register(village(style), new io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure(
                    new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_VILLAGE.get(style)))
                            .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                            .terrainAdapation(TerrainAdjustment.NONE)
                            .build(), style));
        }
        for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.values()) {
            context.register(site(kind), new io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteStructure(
                    new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_SITE.get(kind)))
                            .spawnOverrides(siteSpawns(kind))
                            .generationStep(kind.step())
                            .terrainAdapation(TerrainAdjustment.NONE)
                            .build(), kind));
        }
        for (DragonType type : DragonType.values()) {
            context.register(mageTower(type), new io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerStructure(
                    new Structure.StructureSettings.Builder(biomes.getOrThrow(ModTags.Biomes.HAS_MAGE_TOWERS.get(type)))
                            .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                            .terrainAdapation(TerrainAdjustment.NONE)
                            .build(), type));
        }
        for (DragonType type : DragonType.values()) {
            if (!LairDesigns.available(type)) {
                continue;
            }
            context.register(lair(type), new DragonLairStructure(new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
                    // ganz zum Schluss: so gestaltet das Nest auch Bäume und Pflanzen seiner Umgebung um
                    .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                    .terrainAdapation(TerrainAdjustment.NONE)
                    .build(), type));
        }
    }

    /**
     * Die Verteilung der eigenen Bauwerke der Oberwelt – eher seltener als die der Bauwerke, deren Rolle sie übernehmen,
     * damit jedes ein Fund bleibt: der Sternensturz in Ringen um den Weltursprung (wie die Festungen, Enderaugen finden ihn
     * so), Wasserstollen und Gezeitenschreine selten in vielen Chunks, sonst Felder von 28 bis 56 Chunks mit weitem
     * Mindestabstand.
     */
    private static void siteSets(BootstrapContext<StructureSet> context, HolderGetter<Structure> structures, HolderGetter<Biome> biomes) {
        // kein Bauwerk näher als zehn Chunks an einer Stammburg
        var castles = java.util.Optional.of(new net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement
                .ExclusionZone(context.lookup(Registries.STRUCTURE_SET).getOrThrow(REALM_CASTLES), 10));
        for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind.values()) {
            var structure = structures.getOrThrow(site(kind));
            StructurePlacement placement = switch (kind) {
                case FALLEN_STAR -> new net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement(32, 3, 128,
                        biomes.getOrThrow(BiomeTags.STRONGHOLD_BIASED_TO));
                case AQUEDUCT -> new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(
                        net.minecraft.core.Vec3i.ZERO,
                        net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_3,
                        0.0009f, 71_404_413, castles, 1, 0,
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR);
                case TIDE_SHRINE -> new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(
                        new net.minecraft.core.Vec3i(9, 0, 9),
                        net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2,
                        0.003f, 50_918_377, castles, 1, 0,
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR);
                case SEA_DRAGON -> spread(32, 8, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 165_745_311, castles);
                case CARAVANSERAI -> spread(40, 12, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 14_357_631, castles);
                case SERPENT_TEMPLE -> spread(40, 12, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 14_357_633, castles);
                case WITCH_TREE -> spread(40, 12, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 14_357_635, castles);
                case FROST_GROTTO -> spread(40, 12, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 14_357_637, castles);
                case BANDIT_CAMP -> new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(
                        net.minecraft.core.Vec3i.ZERO,
                        net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod.DEFAULT,
                        0.25f, 165_745_297, castles, wider(40), gap(wider(40)),
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR);
                case CHARCOAL_CAMP -> spread(50, 14, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 91_231_129, castles);
                case EMBER_GATE -> spread(56, 20, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 34_222_647, castles);
                case CLAM_FIELD -> spread(28, 10, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 14_357_639, castles);
                case CORAL_PALACE -> spread(40, 10, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.TRIANGULAR, 10_387_315, castles);
                case BURIED_ABBEY -> spread(44, 12, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 83_469_869, castles);
                case SILENT_CATHEDRAL -> spread(30, 10, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 20_083_239, castles);
                case TRIAL_ARENA -> spread(40, 14, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 94_251_331, castles);
                case ROBBER_CASTLE -> spread(80, 20, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.TRIANGULAR, 10_387_321, castles);
                // die Aschenesse teilt sich die Plätze mit den Bastionen (data/minecraft/worldgen/structure_set/nether_complexes.json)
                case ASH_FORGE -> null;
                // die Hortfeste nimmt die Plätze der Bastionen ein (ebenda)
                case HORTFESTE -> null;
                // die Riesengebeine nehmen die Plätze der Netherfossilien ein (data/minecraft/worldgen/structure_set/nether_fossils.json),
                // die Verlorene Pforte die der Portalruinen im Nether (ruined_portals.json)
                case GIANT_BONES, LOST_GATE -> null;
                // die Sternwarte nimmt die Plätze der Endstädte ein (end_cities.json)
                case STARWATCH -> null;
                // die Fehlerzone (Anni der Troll): selten, ein Fund für sich
                case ERROR_ZONE -> spread(56, 18, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 40_404_404, castles);
                // der Versunkene Tempel: das Tor ins Urmeer – nicht zu selten, damit man es findet
                case SUNKEN_TEMPLE -> spread(24, 8, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 58_913_117, castles);
            };
            if (placement != null) {
                context.register(siteSet(kind), new StructureSet(structure, placement));
            }
        }
    }

    /**
     * Seit 0.79 stehen alle Bauwerke seltener und weiter auseinander: Jedes Feld ist um diesen Faktor breiter als bisher, und
     * zwei Bauwerke derselben Art halten mindestens {@link #SPREAD_GAP} der Feldbreite Abstand.
     */
    public static final float SPREAD_WIDER = 1.6f;
    public static final float SPREAD_GAP = 0.45f;

    /** Feldbreite (Chunks) eines Bauwerks nach der Verbreiterung. */
    public static int wider(int spacing) {
        return Math.round(spacing * SPREAD_WIDER);
    }

    /** Mindestabstand (Chunks) zweier Bauwerke derselben Art in Feldern der Breite {@code spacing}. */
    public static int gap(int spacing) {
        return Math.max(1, Math.round(spacing * SPREAD_GAP));
    }

    /** Zufallsraster wie Vanilla „random_spread“, mit Sperrzone – {@code spacing} ist die Feldbreite vor der Verbreiterung. */
    private static StructurePlacement spread(int spacing, int separation, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType type,
            int salt, java.util.Optional<net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.ExclusionZone> exclusion) {
        int field = wider(spacing);
        return new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(net.minecraft.core.Vec3i.ZERO,
                net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0f,
                salt, exclusion, field, Math.max(separation, gap(field)), type);
    }

    /** Wer an den eigenen Bauwerken auftaucht: Hexen (und eine Katze) am Hexenbaum, Wegelagerer im Räuberlager. */
    private static java.util.Map<net.minecraft.world.entity.MobCategory, net.minecraft.world.level.levelgen.structure.StructureSpawnOverride> siteSpawns(
            io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind kind) {
        return switch (kind) {
            case WITCH_TREE -> java.util.Map.of(
                    net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                            net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.PIECE,
                            net.minecraft.util.random.WeightedList.of(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                    io.github.sirschrolli.kingdomsatwar.registry.ModEntities.WITCH.get(),
                                    new net.minecraft.util.valueproviders.ConstantInt(1)))),
                    net.minecraft.world.entity.MobCategory.CREATURE, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                            net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.PIECE,
                            net.minecraft.util.random.WeightedList.of(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                    io.github.sirschrolli.kingdomsatwar.registry.ModEntities.CAT.get(),
                                    new net.minecraft.util.valueproviders.ConstantInt(1)))));
            case SUNKEN_TEMPLE, CORAL_PALACE -> java.util.Map.of(
                    net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                            net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                            net.minecraft.util.random.WeightedList.of(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                    io.github.sirschrolli.kingdomsatwar.registry.ModEntities.GUARDIAN.get(),
                                    new net.minecraft.util.valueproviders.ConstantInt(1)))));
            case BANDIT_CAMP -> java.util.Map.of(
                    net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                            net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                            net.minecraft.util.random.WeightedList.of(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                    io.github.sirschrolli.kingdomsatwar.registry.ModEntities.PILLAGER.get(),
                                    new net.minecraft.util.valueproviders.ConstantInt(1)))));
            // in der Aschenesse wie in der Festung: Glutgeister, Brandgebeine, Moderborstlinge, Gerippe und Glutschleime
            case ASH_FORGE -> java.util.Map.of(
                    net.minecraft.world.entity.MobCategory.MONSTER, new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                            net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.PIECE,
                            net.minecraft.util.random.WeightedList.<net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData>builder()
                                    .add(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                            io.github.sirschrolli.kingdomsatwar.registry.ModEntities.BLAZE.get(),
                                            net.minecraft.util.valueproviders.UniformInt.of(2, 3)), 10)
                                    .add(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                            io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIFIED_PIGLIN.get(),
                                            net.minecraft.util.valueproviders.ConstantInt.of(4)), 5)
                                    .add(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                            io.github.sirschrolli.kingdomsatwar.registry.ModEntities.WITHER_SKELETON.get(),
                                            net.minecraft.util.valueproviders.ConstantInt.of(5)), 8)
                                    .add(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                            io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SKELETON.get(),
                                            net.minecraft.util.valueproviders.ConstantInt.of(5)), 2)
                                    .add(new net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData(
                                            io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MAGMA_CUBE.get(),
                                            net.minecraft.util.valueproviders.ConstantInt.of(4)), 3)
                                    .build()));
            // in der Kathedrale herrscht Stille: dort entsteht nichts von selbst (wie in der antiken Stadt)
            case SILENT_CATHEDRAL -> {
                var none = new net.minecraft.world.level.levelgen.structure.StructureSpawnOverride(
                        net.minecraft.world.level.levelgen.structure.StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                        net.minecraft.util.random.WeightedList.of());
                var map = new java.util.EnumMap<net.minecraft.world.entity.MobCategory, net.minecraft.world.level.levelgen.structure.StructureSpawnOverride>(
                        net.minecraft.world.entity.MobCategory.class);
                for (var category : net.minecraft.world.entity.MobCategory.values()) {
                    map.put(category, none);
                }
                yield map;
            }
            default -> java.util.Map.of();
        };
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(ALL_LAIRS, new StructureSet(java.util.List.of(), new AnyNestPlacement()));
        context.register(UMBRER_VILLAGES, new StructureSet(structures.getOrThrow(UMBRER_VILLAGE_STRUCTURE),
                new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(wider(28), gap(wider(28)),
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 73_419_211)));
        // Felder von 18 Chunks, mindestens 9 Chunks zwischen zwei Bauwerken (weiter, als zwei Bauplätze reichen)
        java.util.List<StructureSet.StructureSelectionEntry> sites = new java.util.ArrayList<>();
        for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind.values()) {
            sites.add(StructureSet.entry(structures.getOrThrow(shadowSite(kind)), siteWeight(kind)));
        }
        context.register(SHADOW_SITES_SET, new StructureSet(sites,
                new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(wider(18), gap(wider(18)),
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 61_442_917)));
        context.register(SHADOW_CITADEL_SET, new StructureSet(structures.getOrThrow(SHADOW_CITADEL_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPlacement(900, 1700,
                        biomes.getOrThrow(ModTags.Biomes.HAS_SHADOW_CITADEL), 66_613)));
        context.register(WORLD_EATER_HOARD_SET, new StructureSet(structures.getOrThrow(WORLD_EATER_HOARD_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement(500, 900, 73_731)));
        context.register(VOID_THRONE_SET, new StructureSet(structures.getOrThrow(VOID_THRONE_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement(450, 800, 77_031)));
        context.register(SUNKEN_ATOLL_SET, new StructureSet(structures.getOrThrow(SUNKEN_ATOLL_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollPlacement(450, 1300, biomes.getOrThrow(ModTags.Biomes.HAS_SUNKEN_ATOLL),
                        88_207)));
        context.register(KRAKEN_MAW_SET, new StructureSet(structures.getOrThrow(KRAKEN_MAW_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawPlacement(350, 900, biomes.getOrThrow(ModTags.Biomes.HAS_KRAKEN_MAW),
                        91_113)));
        // Hexengrotten (0.97): Felder von 30 Chunks, mindestens 12 Chunks Abstand, fern des Krakenschlunds und der Meeresdörfer
        context.register(WITCH_GROTTOS, new StructureSet(structures.getOrThrow(WITCH_GROTTO_STRUCTURE),
                new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(net.minecraft.core.Vec3i.ZERO,
                        net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod.DEFAULT,
                        1.0f, 47_920_311, java.util.Optional.of(new net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement
                                .ExclusionZone(context.lookup(Registries.STRUCTURE_SET).getOrThrow(KRAKEN_MAW_SET), 8)),
                        30, 12, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR)));
        // Meeresdörfer: Felder von 22 Chunks, mindestens 9 Chunks Abstand, 7 Chunks fern des Krakenschlunds
        context.register(SEA_VILLAGES, new StructureSet(structures.getOrThrow(SEA_VILLAGE_STRUCTURE),
                new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(net.minecraft.core.Vec3i.ZERO,
                        net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod.DEFAULT,
                        1.0f, 93_311_207, java.util.Optional.of(new net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement
                                .ExclusionZone(context.lookup(Registries.STRUCTURE_SET).getOrThrow(KRAKEN_MAW_SET), 7)),
                        22, 9, net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR)));
        // Magiertürme: Felder von 46 Chunks, mindestens 16 Chunks Abstand; fern von Dörfern, Nestern und dem Siegelkreis
        java.util.List<StructureSet.StructureSelectionEntry> towers = new java.util.ArrayList<>();
        for (DragonType type : DragonType.values()) {
            towers.add(StructureSet.entry(structures.getOrThrow(mageTower(type))));
        }
        // Dörfer: Felder von 40 Chunks, mindestens 12 Chunks Abstand, 28 Chunks fern der Nester, 10 vom Siegelkreis
        java.util.List<StructureSet.StructureSelectionEntry> villageEntries = new java.util.ArrayList<>();
        for (var style : io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle.values()) {
            villageEntries.add(StructureSet.entry(structures.getOrThrow(village(style))));
        }
        context.register(VILLAGES, new StructureSet(villageEntries, new io.github.sirschrolli.kingdomsatwar.worldgen.village.VillagePlacement(wider(40), gap(wider(40)),
                10_387_312, java.util.Optional.empty(), 28, 10)));
        var villages = context.lookup(Registries.STRUCTURE_SET).getOrThrow(VILLAGES);
        context.register(MAGE_TOWERS, new StructureSet(towers, new io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPlacement(wider(46), gap(wider(46)),
                47_719_163, java.util.Optional.of(new net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement
                        .ExclusionZone(villages, 7)), 14)));
        siteSets(context, structures, biomes);
        // die vier Stammburgen: 900 bis 2400 Blöcke vom Weltursprung, je eine in einer Himmelsrichtung
        context.register(REALM_CASTLES, new StructureSet(structures.getOrThrow(REALM_CASTLE_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePlacement(900, 2400,
                        biomes.getOrThrow(ModTags.Biomes.HAS_REALM_CASTLE), 70_717)));
        context.register(SEAL_CIRCLE_SET, new StructureSet(structures.getOrThrow(SEAL_CIRCLE_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCirclePlacement(220, 640,
                        biomes.getOrThrow(ModTags.Biomes.HAS_SEAL_CIRCLE), 51_731)));
        for (DragonType type : DragonType.values()) {
            if (!LairDesigns.available(type)) {
                continue;
            }
            int min = minDistance(type);
            context.register(lairSet(type), new StructureSet(structures.getOrThrow(lair(type)),
                    new UniqueNestPlacement(type, min, min + 1600, biomes.getOrThrow(ModTags.Biomes.HAS_DRAGON_NESTS.get(type)),
                            type != DragonType.WATER, 17 + type.ordinal() * 101)));
        }
    }
}
