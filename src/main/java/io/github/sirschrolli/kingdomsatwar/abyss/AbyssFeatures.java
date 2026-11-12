package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.feature.GravityCrystalFeature;
import io.github.sirschrolli.kingdomsatwar.abyss.feature.SeamVeinFeature;
import io.github.sirschrolli.kingdomsatwar.abyss.feature.WorldShardFeature;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.shadow.feature.ShadowTreeFeature;
import io.github.sirschrolli.kingdomsatwar.voidrealm.AnySurfacePlacement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

/**
 * Was im Abgrund treibt, steht und wächst: die Weltensplitter (gekippt und kopfüber, aus allen Welten), die Weltennähte
 * über dem Boden, die Kristalle der verdrehten Schwerkraft – und auf den Bruchstücken, was in ihrer Welt
 * wächst: Eichen und Gras, Karmesinwurzeln, Chorus, Schattenbäume, Leerenblüten. Alle Biome bekommen ihre Features je
 * Erzeugungsschritt in derselben Reihenfolge.
 */
public final class AbyssFeatures {
    private AbyssFeatures() {}

    /** Ein Feature: entweder ein eigenes ({@code feature}) oder ein vorhandenes aus dem Register ({@code existing}). */
    private record Entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, ResourceKey<Feature> existing,
            List<PlacementModifier> placement) {
        ResourceKey<Feature> featureKey() {
            return this.existing != null ? this.existing : ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("abyss/" + this.name));
        }

        ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("abyss/" + this.name));
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static Entry entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, feature, null, placement);
        ENTRIES.add(entry);
        return entry;
    }

    private static Entry existing(GenerationStep.Decoration step, String name, ResourceKey<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, null, feature, placement);
        ENTRIES.add(entry);
        return entry;
    }

    // --- Platzierungen ---

    // alle halten Abstand zu den Uhrtürmen (seit 1.1): deren Auge im Chaos bleibt leer
    private static final PlacementModifier AWAY_FROM_CLOCKS = io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerClearing.Filter.INSTANCE;

    private static List<PlacementModifier> floor(PlacementModifier frequency) {
        return List.of(frequency, InSquarePlacement.spread(), AWAY_FROM_CLOCKS, AnySurfacePlacement.floor(), BiomeFilter.biome());
    }

    private static List<PlacementModifier> patch(PlacementModifier frequency, int tries) {
        return List.of(frequency, InSquarePlacement.spread(), AWAY_FROM_CLOCKS, AnySurfacePlacement.floor(), BiomeFilter.biome(), CountPlacement.of(tries),
                OffsetPlacement.ofTriangle(5, 1), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    /** Frei in der Luft, zwischen {@code low} und {@code high}. */
    private static List<PlacementModifier> air(PlacementModifier frequency, int low, int high) {
        return List.of(frequency, InSquarePlacement.spread(), AWAY_FROM_CLOCKS,
                HeightRangePlacement.uniform(VerticalAnchor.absolute(low), VerticalAnchor.absolute(high)), BiomeFilter.biome());
    }

    private static Supplier<Feature> block(Supplier<? extends Block> block) {
        return () -> new SimpleBlockFeature(BlockStateProvider.of(block.get()));
    }

    private static Supplier<Feature> shards(int min, int max, float inverted, Integer... palettes) {
        return () -> new WorldShardFeature(List.of(palettes), min, max, inverted);
    }

    private static final GenerationStep.Decoration LOCAL = GenerationStep.Decoration.LOCAL_MODIFICATIONS;
    private static final GenerationStep.Decoration VEGETATION = GenerationStep.Decoration.VEGETAL_DECORATION;

    private static final int O = WorldShardFeature.OVERWORLD;
    private static final int N = WorldShardFeature.NETHER;
    private static final int E = WorldShardFeature.END;
    private static final int S = WorldShardFeature.SHADOW;
    private static final int V = WorldShardFeature.VOID;
    private static final int F = WorldShardFeature.FROST;
    private static final int D = WorldShardFeature.DESERT;

    // =====================================================================================
    // Die Features (Reihenfolge je Schritt = Reihenfolge in den Biomen)
    // =====================================================================================

    static final Entry SHARDS_LANDS = entry(LOCAL, "shards_lands", shards(4, 11, 0.2f, O, O, O, O, F, D), air(CountPlacement.of(1), 45, 205));
    static final Entry SHARDS_EMBER = entry(LOCAL, "shards_ember", shards(4, 11, 0.2f, N, N, N, N, D), air(CountPlacement.of(1), 45, 205));
    static final Entry SHARDS_STAR = entry(LOCAL, "shards_star", shards(4, 11, 0.2f, E, E, E, V), air(CountPlacement.of(1), 45, 205));
    static final Entry SHARDS_SHADOW = entry(LOCAL, "shards_shadow", shards(4, 11, 0.2f, S, S, S, V), air(CountPlacement.of(1), 45, 205));
    static final Entry SHARDS_SEAM = entry(LOCAL, "shards_seam", shards(4, 12, 0.3f, O, N, E, S, V, F, D), air(CountPlacement.of(2), 45, 205));
    static final Entry SHARDS_GREAT = entry(LOCAL, "shards_great", shards(10, 13, 0.25f, O, N, E, S, V, F, D),
            air(RarityFilter.onAverageOnceEvery(4), 60, 190));
    static final Entry SEAMS = entry(LOCAL, "world_seams", SeamVeinFeature::new, floor(CountPlacement.of(1)));
    static final Entry SEAMS_DENSE = entry(LOCAL, "world_seams_dense", SeamVeinFeature::new, floor(CountPlacement.of(4)));
    static final Entry FLOAT_CRYSTALS = entry(LOCAL, "float_crystals", () -> new GravityCrystalFeature(false),
            floor(RarityFilter.onAverageOnceEvery(3)));
    static final Entry LIFT_CRYSTALS = entry(LOCAL, "lift_crystals", () -> new GravityCrystalFeature(true),
            floor(RarityFilter.onAverageOnceEvery(5)));

    static final Entry OAKS = existing(VEGETATION, "oaks", TreeFeatures.OAK, floor(RarityFilter.onAverageOnceEvery(2)));
    static final Entry GRASS = entry(VEGETATION, "grass", () -> new SimpleBlockFeature(BlockStateProvider.of(Blocks.SHORT_GRASS)),
            patch(CountPlacement.of(3), 24));
    static final Entry FLOWERS = entry(VEGETATION, "flowers", () -> new SimpleBlockFeature(BlockStateProvider.of(Blocks.POPPY)),
            patch(RarityFilter.onAverageOnceEvery(2), 10));
    static final Entry CRIMSON_FUNGI = existing(VEGETATION, "crimson_fungi", TreeFeatures.CRIMSON_FUNGUS_PLANTED,
            floor(RarityFilter.onAverageOnceEvery(2)));
    static final Entry CRIMSON_ROOTS = entry(VEGETATION, "crimson_roots", () -> new SimpleBlockFeature(BlockStateProvider.of(Blocks.CRIMSON_ROOTS)),
            patch(CountPlacement.of(2), 20));
    static final Entry CHORUS = existing(VEGETATION, "chorus", EndFeatures.CHORUS_PLANT, floor(CountPlacement.of(1)));
    static final Entry VOID_CLUSTERS = entry(VEGETATION, "void_clusters", block(VoidBlocks.VOID_CRYSTAL_CLUSTER), patch(CountPlacement.of(1), 8));
    static final Entry VOID_BLOOMS = entry(VEGETATION, "void_blooms", block(VoidBlocks.VOID_BLOOM), patch(RarityFilter.onAverageOnceEvery(2), 12));
    static final Entry SHADOW_TREES = entry(VEGETATION, "shadow_trees", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.SHADOW),
            floor(RarityFilter.onAverageOnceEvery(2)));
    static final Entry PALE_GRASS = entry(VEGETATION, "pale_grass", block(ShadowBlocks.PALE_GRASS), patch(CountPlacement.of(2), 20));
    static final Entry SPROUTS = entry(VEGETATION, "shadow_sprouts", block(ShadowBlocks.SHADOW_SPROUTS), patch(CountPlacement.of(1), 16));

    // =====================================================================================
    // Registrierung (Datengenerierung)
    // =====================================================================================

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        for (Entry entry : ENTRIES) {
            if (entry.existing() == null) {
                context.register(entry.featureKey(), entry.feature().get());
            }
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
        for (Entry entry : ENTRIES) {
            Holder<Feature> feature = features.getOrThrow(entry.featureKey());
            context.register(entry.placedKey(), new PlacedFeature(feature, entry.placement()));
        }
    }

    // =====================================================================================
    // Was jedes Biom bekommt
    // =====================================================================================

    private static BiomeGenerationSettings settings(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers, Entry... chosen) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        List<Entry> wanted = new ArrayList<>(List.of(chosen));
        wanted.add(SHARDS_GREAT);
        wanted.add(SEAMS);
        wanted.add(FLOAT_CRYSTALS);
        wanted.add(LIFT_CRYSTALS);
        Map<GenerationStep.Decoration, List<Entry>> byStep = new EnumMap<>(GenerationStep.Decoration.class);
        for (Entry entry : ENTRIES) {
            if (wanted.contains(entry)) {
                byStep.computeIfAbsent(entry.step(), s -> new ArrayList<>()).add(entry);
            }
        }
        byStep.forEach((step, list) -> list.forEach(entry -> builder.addFeature(step, entry.placedKey())));
        return builder.build();
    }

    static BiomeGenerationSettings fracturedLands(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SHARDS_LANDS, OAKS, GRASS, FLOWERS);
    }

    static BiomeGenerationSettings emberFalls(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SHARDS_EMBER, CRIMSON_FUNGI, CRIMSON_ROOTS);
    }

    static BiomeGenerationSettings starWreck(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SHARDS_STAR, CHORUS, VOID_CLUSTERS, VOID_BLOOMS);
    }

    static BiomeGenerationSettings shadowChasm(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SHARDS_SHADOW, SHADOW_TREES, PALE_GRASS, SPROUTS);
    }

    static BiomeGenerationSettings seam(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SHARDS_SEAM, SEAMS_DENSE, GRASS, CRIMSON_ROOTS, VOID_CLUSTERS);
    }
}
