package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.shadow.feature.BoneArchFeature;
import io.github.sirschrolli.kingdomsatwar.shadow.feature.CrystalGrowthFeature;
import io.github.sirschrolli.kingdomsatwar.shadow.feature.RockSpireFeature;
import io.github.sirschrolli.kingdomsatwar.shadow.feature.ShadowTreeFeature;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

/**
 * Was in der Schattenwelt wächst und steht: Bäume, Pflanzen, Felsnadeln, Knochenbögen und Nachtkristalle – und welches
 * Biom was davon bekommt. Alle Biome nehmen ihre Features in derselben Reihenfolge auf (sonst verweigert das Spiel die
 * Welt), darum stehen sie hier in einer festen Liste je Erzeugungsschritt.
 */
public final class ShadowFeatures {
    private ShadowFeatures() {}

    /** Ein Feature samt Platzierung: Schritt, Schlüssel und wie es entsteht. */
    private record Entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("shadow/" + this.name));
        }

        ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("shadow/" + this.name));
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private static Entry entry(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, feature, placement);
        ENTRIES.add(entry);
        return entry;
    }

    // --- Platzierungen ---

    private static List<PlacementModifier> surface(PlacementModifier frequency, int maxWater) {
        return List.of(frequency, InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(maxWater),
                HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR), BiomeFilter.biome());
    }

    private static List<PlacementModifier> patch(PlacementModifier frequency, int tries) {
        return List.of(frequency, InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                BiomeFilter.biome(), CountPlacement.of(tries), OffsetPlacement.ofTriangle(6, 2),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    private static List<PlacementModifier> volume(int count, int minY, int maxY) {
        return List.of(CountPlacement.of(count), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY)), BiomeFilter.biome());
    }

    private static List<PlacementModifier> ores(int count, int minY, int maxY, boolean triangle) {
        VerticalAnchor bottom = VerticalAnchor.absolute(minY);
        VerticalAnchor top = VerticalAnchor.absolute(maxY);
        return List.of(CountPlacement.of(count), InSquarePlacement.spread(),
                triangle ? HeightRangePlacement.triangle(bottom, top) : HeightRangePlacement.uniform(bottom, top), BiomeFilter.biome());
    }

    /** Erzader: im Schattenstein {@code stone} (falls vorhanden), im Tiefschattenstein {@code deep}. */
    private static Supplier<Feature> ore(Supplier<? extends Block> stone, Supplier<? extends Block> deep, int size, float airDiscard) {
        return () -> {
            List<BlockReplacement> targets = new ArrayList<>();
            if (stone != null) {
                targets.add(BlockReplacement.replace(new TagMatchTest(ModTags.Blocks.SHADOW_STONE_ORE_REPLACEABLES),
                        stone.get().defaultBlockState()));
            }
            targets.add(BlockReplacement.replace(new TagMatchTest(ModTags.Blocks.DEEP_SHADOW_STONE_ORE_REPLACEABLES),
                    deep.get().defaultBlockState()));
            return new OreFeature(targets, size, airDiscard);
        };
    }

    private static Supplier<Feature> block(Supplier<? extends Block> block) {
        return () -> new SimpleBlockFeature(BlockStateProvider.of(block.get()));
    }

    // =====================================================================================
    // Die Features (Reihenfolge je Schritt = Reihenfolge in den Biomen)
    // =====================================================================================

    private static final GenerationStep.Decoration ORES = GenerationStep.Decoration.UNDERGROUND_ORES;
    private static final GenerationStep.Decoration LOCAL = GenerationStep.Decoration.LOCAL_MODIFICATIONS;
    private static final GenerationStep.Decoration UNDERGROUND = GenerationStep.Decoration.UNDERGROUND_DECORATION;
    private static final GenerationStep.Decoration VEGETATION = GenerationStep.Decoration.VEGETAL_DECORATION;

    // --- Erze: in allen Biomen, nach Tiefe gestaffelt ---
    static final Entry ORE_MOONSILVER = entry(ORES, "ore_moonsilver",
            ore(ShadowBlocks.MOONSILVER_ORE, ShadowBlocks.DEEP_MOONSILVER_ORE, 9, 0.0f), ores(14, -40, 170, true));
    static final Entry ORE_NIGHTSTEEL = entry(ORES, "ore_nightsteel",
            ore(ShadowBlocks.NIGHTSTEEL_ORE, ShadowBlocks.DEEP_NIGHTSTEEL_ORE, 7, 0.0f), ores(8, -64, 72, true));
    static final Entry ORE_SOUL_CRYSTAL = entry(ORES, "ore_soul_crystal",
            ore(null, ShadowBlocks.SOUL_CRYSTAL_ORE, 5, 0.25f), ores(4, -64, 4, false));
    /** Abyssit nur ganz unten und nur ganz im Gestein eingeschlossen (wie antiker Schutt). */
    static final Entry ORE_ABYSSITE = entry(ORES, "ore_abyssite",
            ore(null, ShadowBlocks.ABYSSITE_ORE, 3, 1.0f), ores(2, -64, -28, true));

    static final Entry SPIRES_WASTES = entry(LOCAL, "rock_spires_wastes", () -> new RockSpireFeature(9, 24, 2.4f),
            surface(RarityFilter.onAverageOnceEvery(3), 0));
    static final Entry SPIRES_PEAKS = entry(LOCAL, "rock_spires_peaks", () -> new RockSpireFeature(10, 28, 2.6f),
            surface(CountPlacement.of(1), 0));
    static final Entry SPIRES_SMALL = entry(LOCAL, "rock_spires_small", () -> new RockSpireFeature(4, 9, 1.3f),
            surface(RarityFilter.onAverageOnceEvery(5), 0));
    static final Entry BONE_ARCH = entry(LOCAL, "bone_arch", BoneArchFeature::new, surface(RarityFilter.onAverageOnceEvery(7), 0));

    static final Entry CRYSTALS_CHASM = entry(UNDERGROUND, "night_crystals_chasm", () -> new CrystalGrowthFeature(64, 7), volume(30, -30, 110));
    static final Entry CRYSTALS_DEPTHS = entry(UNDERGROUND, "night_crystals_depths", () -> new CrystalGrowthFeature(40, 6), volume(6, -60, 40));

    static final Entry TREE_GIANT = entry(VEGETATION, "tree_giant", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.GIANT),
            surface(RarityFilter.onAverageOnceEvery(2), 0));
    static final Entry TREES_FOREST = entry(VEGETATION, "trees_forest", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.SHADOW),
            surface(CountPlacement.of(7), 0));
    static final Entry TREES_SPARSE = entry(VEGETATION, "trees_sparse", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.SHADOW),
            surface(RarityFilter.onAverageOnceEvery(3), 0));
    static final Entry TREES_BLEACHED = entry(VEGETATION, "trees_bleached", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.BLEACHED),
            surface(RarityFilter.onAverageOnceEvery(2), 0));
    static final Entry TREES_MARSH = entry(VEGETATION, "trees_marsh", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.MARSH),
            surface(CountPlacement.of(2), 2));
    static final Entry TREES_ISLE = entry(VEGETATION, "trees_isle", () -> new ShadowTreeFeature(ShadowTreeFeature.Kind.ISLE),
            surface(CountPlacement.of(2), 0));
    static final Entry SPROUTS_DENSE = entry(VEGETATION, "sprouts_dense", block(ShadowBlocks.SHADOW_SPROUTS), patch(CountPlacement.of(4), 32));
    static final Entry SPROUTS = entry(VEGETATION, "sprouts", block(ShadowBlocks.SHADOW_SPROUTS), patch(CountPlacement.of(1), 24));
    static final Entry PALE_GRASS = entry(VEGETATION, "pale_grass", block(ShadowBlocks.PALE_GRASS), patch(CountPlacement.of(2), 20));
    static final Entry DEAD_BUSH = entry(VEGETATION, "dead_bush", () -> new SimpleBlockFeature(BlockStateProvider.of(Blocks.DEAD_BUSH)),
            patch(RarityFilter.onAverageOnceEvery(2), 6));
    static final Entry MOONFLOWERS = entry(VEGETATION, "moonflowers", block(ModBlocks.MOONFLOWER), patch(CountPlacement.of(2), 28));
    static final Entry GHOSTCAPS = entry(VEGETATION, "ghostcaps", block(ShadowBlocks.GHOSTCAP), patch(RarityFilter.onAverageOnceEvery(2), 16));
    static final Entry CRYSTAL_CLUSTERS = entry(VEGETATION, "night_crystal_clusters", block(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER),
            patch(RarityFilter.onAverageOnceEvery(3), 10));
    static final Entry SOUL_BERRIES = entry(VEGETATION, "soul_berry_bushes", () -> new SimpleBlockFeature(BlockStateProvider.of(
            ShadowBlocks.SOUL_BERRY_BUSH.get().defaultBlockState().setValue(io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock.AGE, 3))),
            patch(RarityFilter.onAverageOnceEvery(4), 12));
    /** Glimmerranken unter den Baumkronen (nach den Bäumen, damit es Laub gibt). */
    static final Entry VINES_TREES = entry(VEGETATION, "glimmer_vines_trees",
            () -> new io.github.sirschrolli.kingdomsatwar.shadow.feature.HangingVinesFeature(10, 6),
            List.of(CountPlacement.of(6), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                    BiomeFilter.biome()));
    /** Glimmerranken an Höhlendecken und Schluchtwänden (in allen Biomen). */
    static final Entry VINES_CAVES = entry(UNDERGROUND, "glimmer_vines_caves",
            () -> new io.github.sirschrolli.kingdomsatwar.shadow.feature.HangingVinesFeature(8, 10), volume(28, -56, 150));

    /** Setzlinge: ein Schattenbaum – vier Setzlinge im Quadrat werden zu einem Schattenriesen. */
    public static final TreeGrower SHADOW_TREE = new TreeGrower("kingdomsatwar_shadow", WeightedList.of(TREES_SPARSE.featureKey()),
            WeightedList.of(TREE_GIANT.featureKey()), WeightedList.of(), null);

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
        wanted.addAll(List.of(ORE_MOONSILVER, ORE_NIGHTSTEEL, ORE_SOUL_CRYSTAL, ORE_ABYSSITE, VINES_CAVES));
        Map<GenerationStep.Decoration, List<Entry>> byStep = new EnumMap<>(GenerationStep.Decoration.class);
        for (Entry entry : ENTRIES) {   // feste Reihenfolge
            if (wanted.contains(entry)) {
                byStep.computeIfAbsent(entry.step(), s -> new ArrayList<>()).add(entry);
            }
        }
        byStep.forEach((step, list) -> list.forEach(entry -> builder.addFeature(step, entry.placedKey())));
        return builder.build();
    }

    static BiomeGenerationSettings forest(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, CRYSTALS_DEPTHS, TREE_GIANT, TREES_FOREST, SPROUTS_DENSE, GHOSTCAPS, MOONFLOWERS, SOUL_BERRIES,
                VINES_TREES);
    }

    static BiomeGenerationSettings wastes(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_WASTES, BONE_ARCH, CRYSTALS_DEPTHS, TREES_BLEACHED, PALE_GRASS, DEAD_BUSH);
    }

    static BiomeGenerationSettings chasms(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, CRYSTALS_CHASM, CRYSTALS_DEPTHS, TREES_SPARSE, SPROUTS, CRYSTAL_CLUSTERS);
    }

    static BiomeGenerationSettings marsh(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, CRYSTALS_DEPTHS, TREES_MARSH, SPROUTS_DENSE, GHOSTCAPS, SOUL_BERRIES, VINES_TREES);
    }

    static BiomeGenerationSettings peaks(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_PEAKS, CRYSTALS_DEPTHS, PALE_GRASS);
    }

    static BiomeGenerationSettings meadows(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, SPIRES_SMALL, CRYSTALS_DEPTHS, TREES_SPARSE, SPROUTS, MOONFLOWERS, CRYSTAL_CLUSTERS);
    }

    static BiomeGenerationSettings sea(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, CRYSTALS_DEPTHS);
    }

    static BiomeGenerationSettings isles(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, TREES_ISLE, SPROUTS_DENSE, MOONFLOWERS, GHOSTCAPS, VINES_TREES);
    }

    static BiomeGenerationSettings depths(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        return settings(placed, carvers, CRYSTALS_DEPTHS, GHOSTCAPS);
    }
}
