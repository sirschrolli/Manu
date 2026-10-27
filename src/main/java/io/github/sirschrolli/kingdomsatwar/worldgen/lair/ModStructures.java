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

    /** Die Dörfer der Umbrer in der Schattenwelt. */
    public static final ResourceKey<Structure> UMBRER_VILLAGE_STRUCTURE = ResourceKey.create(Registries.STRUCTURE, KingdomsAtWar.id("umbrer_village"));
    public static final ResourceKey<StructureSet> UMBRER_VILLAGES = ResourceKey.create(Registries.STRUCTURE_SET, KingdomsAtWar.id("umbrer_villages"));

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
        context.register(SEAL_CIRCLE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
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

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(ALL_LAIRS, new StructureSet(java.util.List.of(), new AnyNestPlacement()));
        context.register(UMBRER_VILLAGES, new StructureSet(structures.getOrThrow(UMBRER_VILLAGE_STRUCTURE),
                new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(24, 9,
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 73_419_211)));
        // Felder von 14 Chunks, mindestens 9 Chunks zwischen zwei Bauwerken (weiter, als zwei Bauplätze reichen)
        java.util.List<StructureSet.StructureSelectionEntry> sites = new java.util.ArrayList<>();
        for (var kind : io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure.Kind.values()) {
            sites.add(StructureSet.entry(structures.getOrThrow(shadowSite(kind)), siteWeight(kind)));
        }
        context.register(SHADOW_SITES_SET, new StructureSet(sites,
                new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(14, 8,
                        net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR, 61_442_917)));
        context.register(SHADOW_CITADEL_SET, new StructureSet(structures.getOrThrow(SHADOW_CITADEL_STRUCTURE),
                new io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelPlacement(900, 1700,
                        biomes.getOrThrow(ModTags.Biomes.HAS_SHADOW_CITADEL), 66_613)));
        // Magiertürme: Felder von 36 Chunks, mindestens 12 Chunks Abstand; fern von Dörfern, Nestern und dem Siegelkreis
        java.util.List<StructureSet.StructureSelectionEntry> towers = new java.util.ArrayList<>();
        for (DragonType type : DragonType.values()) {
            towers.add(StructureSet.entry(structures.getOrThrow(mageTower(type))));
        }
        var villages = context.lookup(Registries.STRUCTURE_SET).getOrThrow(net.minecraft.world.level.levelgen.structure.BuiltinStructureSets.VILLAGES);
        context.register(MAGE_TOWERS, new StructureSet(towers, new io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPlacement(36, 12,
                47_719_163, java.util.Optional.of(new net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement
                        .ExclusionZone(villages, 7)), 14)));
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
