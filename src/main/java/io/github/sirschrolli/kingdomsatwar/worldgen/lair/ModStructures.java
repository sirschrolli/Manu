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
        context.register(SEAL_CIRCLE_STRUCTURE, new io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
                        .generationStep(GenerationStep.Decoration.TOP_LAYER_MODIFICATION)
                        .terrainAdapation(TerrainAdjustment.NONE)
                        .build()));
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
