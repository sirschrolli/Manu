package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fae.feature.FaeRuinFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.FairyRingFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.GiantMushroomFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature;
import io.github.sirschrolli.kingdomsatwar.fae.feature.ThornTreeFeature;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BoulderFeature;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

/**
 * Was im Feenreich wächst und steht: Glimmerbäume, Glimmerriesen und Tränenglimmer, Riesenpilze, Dornbäume und
 * Dornendickicht, Feenringe, Findlinge, Ruinen, Blumen, Feenhalme und Mondlilien – und welches Biom was davon bekommt
 * (alle Biome in derselben Reihenfolge je Erzeugungsschritt).
 */
public final class FaeFeatures {
    private FaeFeatures() {}

    record Entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("fae/" + this.name));
        }

        ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("fae/" + this.name));
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static Entry entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, feature, placement);
        ENTRIES.add(entry);
        return entry;
    }

    // =====================================================================================
    // Platzierungen
    // =====================================================================================

    /** Auf dem Boden (auch am Grund von höchstens {@code maxWater} Blöcken Wasser). */
    private static List<PlacementModifier> ground(PlacementModifier frequency, int maxWater) {
        return List.of(frequency, InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(maxWater),
                HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR), BiomeFilter.biome(),
                io.github.sirschrolli.kingdomsatwar.fae.Mondhain.Filter.INSTANCE);
    }

    /** Ein Grüppchen Pflanzen: {@code tries} Versuche rund um eine Stelle (nur in Luft). */
    private static List<PlacementModifier> patch(PlacementModifier frequency, int tries, int spread) {
        return List.of(frequency, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(), CountPlacement.of(tries),
                OffsetPlacement.ofTriangle(spread, 2), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    /** Auf der Wasseroberfläche (Mondlilien). */
    private static List<PlacementModifier> water(PlacementModifier frequency, int tries, int spread) {
        return List.of(frequency, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome(),
                CountPlacement.of(tries), OffsetPlacement.ofTriangle(spread, 1),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    private static PlacementModifier rare(int oneIn) {
        return RarityFilter.onAverageOnceEvery(oneIn);
    }

    private static PlacementModifier count(int count) {
        return CountPlacement.of(count);
    }

    private static Supplier<Feature> block(Supplier<? extends Block> block) {
        return () -> new SimpleBlockFeature(BlockStateProvider.of(block.get()));
    }

    private static Supplier<Feature> weighted(Consumer<WeightedList.Builder<BlockState>> states) {
        return () -> {
            WeightedList.Builder<BlockState> builder = WeightedList.builder();
            states.accept(builder);
            return new SimpleBlockFeature(new WeightedStateProvider(builder));
        };
    }

    private static BlockState state(Supplier<? extends Block> block) {
        return block.get().defaultBlockState();
    }

    private static final GenerationStep.Decoration LOCAL = GenerationStep.Decoration.LOCAL_MODIFICATIONS;
    private static final GenerationStep.Decoration STRUCTURES = GenerationStep.Decoration.SURFACE_STRUCTURES;
    private static final GenerationStep.Decoration VEGETATION = GenerationStep.Decoration.VEGETAL_DECORATION;

    // =====================================================================================
    // Die Features (Reihenfolge je Schritt = Reihenfolge in den Biomen)
    // =====================================================================================

    // --- Findlinge ---
    static final Entry BOULDERS = entry(LOCAL, "boulders", () -> new BoulderFeature(state(FaeBlocks.FAESTONE.base()),
            Blocks.CALCITE.defaultBlockState(), 0.22f, Blocks.AMETHYST_BLOCK.defaultBlockState(), 0.035f, state(FaeBlocks.FAE_MOSS),
            1.4f, 2.8f, false), ground(rare(4), 0));
    static final Entry BOULDERS_MOSSY = entry(LOCAL, "mossy_boulders", () -> new BoulderFeature(state(FaeBlocks.FAESTONE.base()),
            state(FaeBlocks.FAE_MOSS), 0.4f, Blocks.AMETHYST_BLOCK.defaultBlockState(), 0.05f, state(FaeBlocks.FAE_MOSS), 1.6f, 3.0f, false),
            ground(rare(3), 0));
    static final Entry BOULDERS_SHORE = entry(LOCAL, "shore_boulders", () -> new BoulderFeature(state(FaeBlocks.FAESTONE.base()),
            Blocks.CALCITE.defaultBlockState(), 0.3f, state(FaeBlocks.MOONSTONE_BRICKS.base()), 0.0f, Blocks.AIR.defaultBlockState(),
            1.2f, 2.4f, true), ground(rare(3), 4));

    // --- Der Mondhain (einer pro Feld, in allen Biomen; die Lichtung der Mottenkönigin) ---
    static final Entry MONDHAIN = entry(STRUCTURES, "mondhain", io.github.sirschrolli.kingdomsatwar.fae.feature.MondhainFeature::new,
            List.of(new SpacedPlacement(io.github.sirschrolli.kingdomsatwar.fae.Mondhain.SPACING,
                    io.github.sirschrolli.kingdomsatwar.fae.Mondhain.SEPARATION, io.github.sirschrolli.kingdomsatwar.fae.Mondhain.SALT, 0),
                    BiomeFilter.biome()));

    // --- Die Zauberbühne (höchstens eine pro Feld, in allen Biomen; MissMystiks Bühne) ---
    static final Entry MYSTIK_STAGE = entry(STRUCTURES, "mystik_stage", io.github.sirschrolli.kingdomsatwar.fae.feature.MystikStageFeature::new,
            List.of(new SpacedPlacement(io.github.sirschrolli.kingdomsatwar.fae.MystikStage.SPACING,
                    io.github.sirschrolli.kingdomsatwar.fae.MystikStage.SEPARATION, io.github.sirschrolli.kingdomsatwar.fae.MystikStage.SALT, 0),
                    BiomeFilter.biome()));

    // --- Ruinen ---
    static final Entry RUINS = entry(STRUCTURES, "ruins", FaeRuinFeature::new,
            List.of(new SpacedPlacement(6, 2, 7_411, 0), InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(0),
                    HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR), BiomeFilter.biome(),
                    io.github.sirschrolli.kingdomsatwar.fae.Mondhain.Filter.INSTANCE));

    // --- Bäume und Riesenpilze ---
    static final Entry GRAND_TREES = entry(VEGETATION, "glimmer_giants",
            () -> new GlimmerTreeFeature(GlimmerTreeFeature.Kind.GRAND, GlimmerTreeFeature.Palette.MIXED), ground(rare(2), 0));
    static final Entry FOREST_TREES = entry(VEGETATION, "glimmer_trees",
            () -> new GlimmerTreeFeature(GlimmerTreeFeature.Kind.SLENDER, GlimmerTreeFeature.Palette.MIXED), ground(count(7), 0));
    static final Entry MEADOW_TREES = entry(VEGETATION, "meadow_trees",
            () -> new GlimmerTreeFeature(GlimmerTreeFeature.Kind.SLENDER, GlimmerTreeFeature.Palette.ROSE), ground(rare(2), 0));
    static final Entry LAKE_TREES = entry(VEGETATION, "weeping_glimmers",
            () -> new GlimmerTreeFeature(GlimmerTreeFeature.Kind.WEEPING, GlimmerTreeFeature.Palette.AZURE), ground(count(2), 0));
    static final Entry SHORE_TREES = entry(VEGETATION, "weeping_glimmers_rare",
            () -> new GlimmerTreeFeature(GlimmerTreeFeature.Kind.WEEPING, GlimmerTreeFeature.Palette.MIXED), ground(rare(4), 0));
    static final Entry MUSHROOM_CLUSTERS = entry(VEGETATION, "giant_mushroom_clusters",
            () -> new GiantMushroomFeature(GiantMushroomFeature.Kind.RANDOM, true), ground(count(1), 0));
    static final Entry MUSHROOMS = entry(VEGETATION, "giant_mushrooms",
            () -> new GiantMushroomFeature(GiantMushroomFeature.Kind.RANDOM, false), ground(count(3), 0));
    static final Entry MUSHROOMS_RARE = entry(VEGETATION, "giant_mushrooms_rare",
            () -> new GiantMushroomFeature(GiantMushroomFeature.Kind.RANDOM, false), ground(rare(6), 0));
    static final Entry THORN_TREES = entry(VEGETATION, "thorn_trees", () -> new ThornTreeFeature(ThornTreeFeature.Kind.TREE),
            ground(count(3), 0));
    static final Entry THICKETS = entry(VEGETATION, "thorn_thickets", () -> new ThornTreeFeature(ThornTreeFeature.Kind.THICKET),
            ground(count(4), 0));

    // --- Pflanzen ---
    static final Entry SPROUTS_DENSE = entry(VEGETATION, "sprouts_dense", block(FaeBlocks.FAE_SPROUTS), patch(count(6), 32, 6));
    static final Entry SPROUTS = entry(VEGETATION, "sprouts", block(FaeBlocks.FAE_SPROUTS), patch(count(2), 24, 5));
    static final Entry MEADOW_FLOWERS = entry(VEGETATION, "meadow_flowers", weighted(b -> b.add(state(FaeBlocks.MOONBELL), 3)
            .add(state(FaeBlocks.FAE_ROSE), 3).add(state(FaeBlocks.STARLACE), 1)), patch(count(4), 28, 6));
    static final Entry FLOWERS = entry(VEGETATION, "flowers", weighted(b -> b.add(state(FaeBlocks.MOONBELL), 2)
            .add(state(FaeBlocks.FAE_ROSE), 2)), patch(count(1), 16, 5));
    static final Entry STARLACE = entry(VEGETATION, "starlace", block(FaeBlocks.STARLACE), patch(rare(2), 12, 4));
    static final Entry LANTERNS = entry(VEGETATION, "lantern_mushrooms", block(FaeBlocks.LANTERN_MUSHROOM), patch(count(2), 18, 5));
    static final Entry LANTERNS_RARE = entry(VEGETATION, "lantern_mushrooms_rare", block(FaeBlocks.LANTERN_MUSHROOM),
            patch(rare(2), 8, 4));
    static final Entry BRIARS = entry(VEGETATION, "thornbriars", block(FaeBlocks.THORNBRIAR), patch(count(3), 20, 5));
    static final Entry LILIES = entry(VEGETATION, "moon_lilies", block(FaeBlocks.MOON_LILY), water(count(4), 14, 6));
    static final Entry LILIES_RARE = entry(VEGETATION, "moon_lilies_rare", block(FaeBlocks.MOON_LILY), water(rare(2), 8, 5));
    /** Feenringe zuletzt: sie räumen die Blumen in ihrem Kreis ab. */
    static final Entry FAIRY_RINGS = entry(VEGETATION, "fairy_rings", FairyRingFeature::new, ground(rare(5), 0));
    static final Entry FAIRY_RINGS_RARE = entry(VEGETATION, "fairy_rings_rare", FairyRingFeature::new, ground(rare(12), 0));

    /** Setzlinge: ein Glimmerbaum – vier Setzlinge im Quadrat werden zu einem Glimmerriesen. */
    public static final TreeGrower GLIMMER_TREE = new TreeGrower("kingdomsatwar_glimmer", WeightedList.of(FOREST_TREES.featureKey()),
            WeightedList.of(GRAND_TREES.featureKey()), WeightedList.of(), null);

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

    /** Alle platzierten Features (für Tests). */
    public static List<ResourceKey<PlacedFeature>> placedKeys() {
        return ENTRIES.stream().map(Entry::placedKey).toList();
    }

    // =====================================================================================
    // Was jedes Biom bekommt
    // =====================================================================================

    private static BiomeGenerationSettings settings(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers, Entry... chosen) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        List<Entry> wanted = List.of(chosen);
        Map<GenerationStep.Decoration, List<Entry>> byStep = new EnumMap<>(GenerationStep.Decoration.class);
        for (Entry entry : ENTRIES) {
            if (wanted.contains(entry)) {
                byStep.computeIfAbsent(entry.step(), s -> new ArrayList<>()).add(entry);
            }
        }
        byStep.forEach((step, list) -> list.forEach(entry -> builder.addFeature(step, entry.placedKey())));
        return builder.build();
    }

    static BiomeGenerationSettings meadows(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, BOULDERS, MONDHAIN, MYSTIK_STAGE, RUINS, MEADOW_TREES, MUSHROOMS_RARE, SPROUTS_DENSE, MEADOW_FLOWERS, STARLACE,
                LANTERNS_RARE, LILIES_RARE, FAIRY_RINGS);
    }

    static BiomeGenerationSettings glimmerwood(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, BOULDERS, MONDHAIN, MYSTIK_STAGE, RUINS, GRAND_TREES, FOREST_TREES, MUSHROOMS_RARE, SPROUTS, FLOWERS, STARLACE,
                LANTERNS_RARE, LILIES_RARE, FAIRY_RINGS_RARE);
    }

    static BiomeGenerationSettings mushroomGrove(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, BOULDERS_MOSSY, MONDHAIN, MYSTIK_STAGE, RUINS, MUSHROOM_CLUSTERS, MUSHROOMS, SPROUTS, FLOWERS, LANTERNS, LILIES_RARE,
                FAIRY_RINGS_RARE);
    }

    static BiomeGenerationSettings moonlitLakes(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, BOULDERS_SHORE, MONDHAIN, MYSTIK_STAGE, RUINS, LAKE_TREES, MEADOW_TREES, SPROUTS_DENSE, FLOWERS, STARLACE, LILIES);
    }

    static BiomeGenerationSettings thornwild(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, BOULDERS, MONDHAIN, MYSTIK_STAGE, RUINS, THORN_TREES, SHORE_TREES, MUSHROOMS_RARE, THICKETS, SPROUTS, LANTERNS_RARE,
                BRIARS, LILIES_RARE);
    }
}
