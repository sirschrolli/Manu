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
        for (DragonType type : DragonType.values()) {
            if (!LairDesigns.available(type)) {
                continue;
            }
            context.register(lair(type), new DragonLairStructure(new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
                    .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                    .terrainAdapation(TerrainAdjustment.NONE)
                    .build(), type));
        }
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(ALL_LAIRS, new StructureSet(java.util.List.of(), new AnyNestPlacement()));
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
