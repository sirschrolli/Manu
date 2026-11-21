package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

/**
 * Bäume (und Kakteen, Felsbrocken), die in den Dorf-Biomen wachsen, gibt es dort nur noch außerhalb der Dörfer: Die
 * Vanilla-Merkmale werden in diesen Biomen gegen gleiche Merkmale mit dem Filter {@link VillageClearing.Filter} getauscht
 * (gleiche Bäume, gleiche Dichte – nur nicht auf dem Dorfplatz, auf den Wegen oder durch die Fenster).
 */
public final class VillageTrees {
    private VillageTrees() {}

    /** Ein getauschtes Merkmal: das Original, sein Baum, seine Platzierung und die Biome, in denen getauscht wird. */
    private record Wrap(String name, ResourceKey<PlacedFeature> original, ResourceKey<Feature> feature, List<PlacementModifier> placement,
            GenerationStep.Decoration step, List<ResourceKey<Biome>> biomes) {
        ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("village_safe/" + this.name));
        }

        ResourceKey<BiomeModifier> removeKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("village_clearing/remove_" + this.name));
        }

        ResourceKey<BiomeModifier> addKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("village_clearing/add_" + this.name));
        }
    }

    private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

    /** Die Platzierung der Vanilla-Bäume (wie in {@code VegetationPlacements.treePlacement}). */
    private static List<PlacementModifier> trees(PlacementModifier frequency, @Nullable Block sapling) {
        List<PlacementModifier> list = new ArrayList<>(List.of(frequency, InSquarePlacement.spread(), TREE_THRESHOLD,
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome()));
        if (sapling != null) {
            list.add(BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(sapling)));
        }
        return list;
    }

    private static final List<Wrap> WRAPS = List.of(
            new Wrap("trees_plains", VegetationPlacements.TREES_PLAINS, VegetationFeatures.TREES_PLAINS,
                    List.of(PlacementUtils.countExtra(0, 0.05f, 1), InSquarePlacement.spread(), TREE_THRESHOLD, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                            BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING)), BiomeFilter.biome()),
                    GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS)),
            new Wrap("trees_meadow", VegetationPlacements.TREES_MEADOW, VegetationFeatures.MEADOW_TREES,
                    trees(RarityFilter.onAverageOnceEvery(100), null), GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.MEADOW)),
            new Wrap("trees_birch_and_oak_leaf_litter", VegetationPlacements.TREES_BIRCH_AND_OAK_LEAF_LITTER,
                    VegetationFeatures.TREES_BIRCH_AND_OAK_LEAF_LITTER, trees(PlacementUtils.countExtra(10, 0.1f, 1), null),
                    GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.FOREST)),
            new Wrap("trees_flower_forest", VegetationPlacements.TREES_FLOWER_FOREST, VegetationFeatures.TREES_FLOWER_FOREST,
                    trees(PlacementUtils.countExtra(6, 0.1f, 1), null), GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.FLOWER_FOREST)),
            new Wrap("trees_birch", VegetationPlacements.TREES_BIRCH, VegetationFeatures.TREES_BIRCH,
                    trees(PlacementUtils.countExtra(10, 0.1f, 1), Blocks.BIRCH_SAPLING), GenerationStep.Decoration.VEGETAL_DECORATION,
                    List.of(Biomes.BIRCH_FOREST)),
            new Wrap("trees_taiga", VegetationPlacements.TREES_TAIGA, VegetationFeatures.TREES_TAIGA,
                    trees(PlacementUtils.countExtra(10, 0.1f, 1), null), GenerationStep.Decoration.VEGETAL_DECORATION,
                    List.of(Biomes.TAIGA, Biomes.SNOWY_TAIGA)),
            new Wrap("trees_snowy", VegetationPlacements.TREES_SNOWY, VegetationFeatures.TREES_SNOWY,
                    trees(PlacementUtils.countExtra(0, 0.1f, 1), Blocks.SPRUCE_SAPLING), GenerationStep.Decoration.VEGETAL_DECORATION,
                    List.of(Biomes.SNOWY_PLAINS)),
            new Wrap("trees_old_growth_pine_taiga", VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA, VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA,
                    trees(PlacementUtils.countExtra(10, 0.1f, 1), null), GenerationStep.Decoration.VEGETAL_DECORATION,
                    List.of(Biomes.OLD_GROWTH_PINE_TAIGA)),
            new Wrap("trees_old_growth_spruce_taiga", VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA,
                    VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA, trees(PlacementUtils.countExtra(10, 0.1f, 1), null),
                    GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.OLD_GROWTH_SPRUCE_TAIGA)),
            new Wrap("trees_savanna", VegetationPlacements.TREES_SAVANNA, VegetationFeatures.TREES_SAVANNA,
                    trees(PlacementUtils.countExtra(1, 0.1f, 1), null), GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.SAVANNA)),
            new Wrap("trees_swamp", VegetationPlacements.TREES_SWAMP, TreeFeatures.SWAMP_OAK,
                    List.of(PlacementUtils.countExtra(2, 0.1f, 1), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(2),
                            PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome(),
                            BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING))),
                    GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.SWAMP)),
            new Wrap("trees_mangrove", VegetationPlacements.TREES_MANGROVE, VegetationFeatures.MANGROVE_VEGETATION,
                    List.of(CountPlacement.of(25), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(5),
                            PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome()),
                    GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.MANGROVE_SWAMP)),
            new Wrap("patch_cactus_desert", VegetationPlacements.PATCH_CACTUS_DESERT, VegetationFeatures.CACTUS,
                    List.of(RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(),
                            CountPlacement.of(10), OffsetPlacement.ofTriangle(7, 3),
                            BlockPredicateFilter.forPredicate(BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                    BlockPredicate.wouldSurvive(Blocks.CACTUS)))),
                    GenerationStep.Decoration.VEGETAL_DECORATION, List.of(Biomes.DESERT)),
            new Wrap("forest_rock", MiscOverworldPlacements.FOREST_ROCK, MiscOverworldFeatures.FOREST_ROCK,
                    List.of(CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()),
                    GenerationStep.Decoration.LOCAL_MODIFICATIONS, List.of(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA)));

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
        for (Wrap wrap : WRAPS) {
            List<PlacementModifier> placement = new ArrayList<>(wrap.placement());
            placement.add(VillageClearing.Filter.INSTANCE);
            context.register(wrap.placedKey(), new PlacedFeature(features.getOrThrow(wrap.feature()), placement));
        }
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        for (Wrap wrap : WRAPS) {
            List<Holder<Biome>> where = new ArrayList<>();
            for (ResourceKey<Biome> key : wrap.biomes()) {
                where.add(biomes.getOrThrow(key));
            }
            HolderSet<Biome> set = HolderSet.direct(where);
            context.register(wrap.removeKey(), new BiomeModifiers.RemoveFeaturesBiomeModifier(set,
                    HolderSet.direct(placed.getOrThrow(wrap.original())), java.util.Set.of(wrap.step())));
            context.register(wrap.addKey(), new BiomeModifiers.AddFeaturesBiomeModifier(set,
                    HolderSet.direct(placed.getOrThrow(wrap.placedKey())), wrap.step()));
        }
    }
}
