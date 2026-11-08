package io.github.sirschrolli.kingdomsatwar.voidrealm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.CrystalTreeFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.FloatingDebrisFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.HangingGrowthFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.ShardSpireFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.StardustCraterFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.VoidRuinFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.WorldRemnantFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

/**
 * Was auf den Bruchstücken der Leere wächst und steht: Kristallbäume, Kristallspitzen, Ruinen der Alten, schwebende
 * Trümmer, Sternenkrater, Reste fremder Welten, Drusen, Leerenblüten und Sternenranken – und welches Biom was davon
 * bekommt (alle Biome in derselben Reihenfolge je Erzeugungsschritt).
 */
public final class VoidFeatures {
    private VoidFeatures() {}

    private record Entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("void/" + this.name));
        }

        ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("void/" + this.name));
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static Entry entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, feature, placement);
        ENTRIES.add(entry);
        return entry;
    }

    // --- Platzierungen ---

    private static List<PlacementModifier> floor(PlacementModifier frequency) {
        return List.of(frequency, InSquarePlacement.spread(), AnySurfacePlacement.floor(), BiomeFilter.biome());
    }

    private static List<PlacementModifier> ceiling(PlacementModifier frequency) {
        return List.of(frequency, InSquarePlacement.spread(), AnySurfacePlacement.ceilingSide(), BiomeFilter.biome());
    }

    private static List<PlacementModifier> patch(PlacementModifier frequency, int tries) {
        return List.of(frequency, InSquarePlacement.spread(), AnySurfacePlacement.floor(), BiomeFilter.biome(), CountPlacement.of(tries),
                OffsetPlacement.ofTriangle(5, 1), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    private static Supplier<Feature> block(Supplier<? extends Block> block) {
        return () -> new SimpleBlockFeature(BlockStateProvider.of(block.get()));
    }

    private static final GenerationStep.Decoration LOCAL = GenerationStep.Decoration.LOCAL_MODIFICATIONS;
    private static final GenerationStep.Decoration STRUCTURES = GenerationStep.Decoration.SURFACE_STRUCTURES;
    private static final GenerationStep.Decoration VEGETATION = GenerationStep.Decoration.VEGETAL_DECORATION;

    // =====================================================================================
    // Die Features (Reihenfolge je Schritt = Reihenfolge in den Biomen)
    // =====================================================================================

    static final Entry SPIRES_GREAT = entry(LOCAL, "shard_spires_great", () -> new ShardSpireFeature(14, 30, 3.4f, false),
            floor(RarityFilter.onAverageOnceEvery(2)));
    static final Entry SPIRES = entry(LOCAL, "shard_spires", () -> new ShardSpireFeature(6, 14, 1.8f, false),
            floor(CountPlacement.of(1)));
    static final Entry SPIRES_HANGING = entry(LOCAL, "shard_spires_hanging", () -> new ShardSpireFeature(6, 18, 2.2f, true),
            ceiling(RarityFilter.onAverageOnceEvery(2)));
    static final Entry CRATER = entry(LOCAL, "stardust_crater", StardustCraterFeature::new, floor(RarityFilter.onAverageOnceEvery(3)));

    static final Entry RUIN = entry(STRUCTURES, "ruin", VoidRuinFeature::new,
            List.of(new SpacedPlacement(3, 1, 7_311, 0), InSquarePlacement.spread(), AnySurfacePlacement.floor(), BiomeFilter.biome()));
    static final Entry RUIN_RARE = entry(STRUCTURES, "ruin_rare", VoidRuinFeature::new,
            List.of(new SpacedPlacement(9, 4, 7_312, 0), InSquarePlacement.spread(), AnySurfacePlacement.floor(), BiomeFilter.biome()));
    static final Entry DEBRIS = entry(STRUCTURES, "floating_debris", FloatingDebrisFeature::new, floor(RarityFilter.onAverageOnceEvery(2)));
    static final Entry REMNANTS = entry(STRUCTURES, "world_remnants", WorldRemnantFeature::new, floor(CountPlacement.of(2)));

    static final Entry TREE_GIANT = entry(VEGETATION, "crystal_tree_giant", () -> new CrystalTreeFeature(CrystalTreeFeature.Kind.GIANT),
            floor(RarityFilter.onAverageOnceEvery(3)));
    static final Entry TREES = entry(VEGETATION, "crystal_trees", () -> new CrystalTreeFeature(CrystalTreeFeature.Kind.TWISTED),
            floor(CountPlacement.of(3)));
    static final Entry TREES_SPARSE = entry(VEGETATION, "crystal_trees_sparse", () -> new CrystalTreeFeature(CrystalTreeFeature.Kind.SMALL),
            floor(RarityFilter.onAverageOnceEvery(3)));
    static final Entry BLOOMS_DENSE = entry(VEGETATION, "void_blooms_dense", block(VoidBlocks.VOID_BLOOM), patch(CountPlacement.of(2), 24));
    static final Entry BLOOMS = entry(VEGETATION, "void_blooms", block(VoidBlocks.VOID_BLOOM), patch(RarityFilter.onAverageOnceEvery(2), 12));
    static final Entry CLUSTERS = entry(VEGETATION, "void_crystal_clusters", block(VoidBlocks.VOID_CRYSTAL_CLUSTER),
            patch(CountPlacement.of(1), 10));
    static final Entry REMNANT_GRASS = entry(VEGETATION, "remnant_grass", () -> new SimpleBlockFeature(BlockStateProvider.of(Blocks.SHORT_GRASS)),
            patch(CountPlacement.of(2), 20));
    static final Entry HANGING = entry(VEGETATION, "hanging_growth", HangingGrowthFeature::new, ceiling(CountPlacement.of(4)));

    // =====================================================================================
    // Registrierung (Datengenerierung)
    // =====================================================================================

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        for (Entry entry : ENTRIES) {
            context.register(entry.featureKey(), entry.feature().get());
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
        for (Entry entry : ENTRIES) {
            context.register(entry.placedKey(), new PlacedFeature(features.getOrThrow(entry.featureKey()), entry.placement()));
        }
    }

    // =====================================================================================
    // Was jedes Biom bekommt
    // =====================================================================================

    private static BiomeGenerationSettings settings(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers, Entry... chosen) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        List<Entry> wanted = new ArrayList<>(List.of(chosen));
        wanted.add(HANGING);
        Map<GenerationStep.Decoration, List<Entry>> byStep = new EnumMap<>(GenerationStep.Decoration.class);
        for (Entry entry : ENTRIES) {
            if (wanted.contains(entry)) {
                byStep.computeIfAbsent(entry.step(), s -> new ArrayList<>()).add(entry);
            }
        }
        byStep.forEach((step, list) -> list.forEach(entry -> builder.addFeature(step, entry.placedKey())));
        return builder.build();
    }

    static BiomeGenerationSettings expanse(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_HANGING, RUIN_RARE, TREES_SPARSE, BLOOMS, CLUSTERS);
    }

    static BiomeGenerationSettings crystalForest(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_HANGING, RUIN_RARE, TREE_GIANT, TREES, BLOOMS_DENSE, CLUSTERS);
    }

    static BiomeGenerationSettings ruins(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_HANGING, RUIN, DEBRIS, TREES_SPARSE, BLOOMS, CLUSTERS);
    }

    static BiomeGenerationSettings stardust(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_HANGING, CRATER, RUIN_RARE, BLOOMS, CLUSTERS);
    }

    static BiomeGenerationSettings shardCliffs(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_GREAT, SPIRES, SPIRES_HANGING, RUIN_RARE, CLUSTERS);
    }

    static BiomeGenerationSettings remnants(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_HANGING, RUIN_RARE, REMNANTS, DEBRIS, REMNANT_GRASS);
    }
}
