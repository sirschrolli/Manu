package io.github.sirschrolli.kingdomsatwar.worldgen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.TrapezoidInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

/**
 * Weltgenerierung: neue Erze und wilde Zierblumen.
 *
 * <p>Die Einträge werden bei der Datengenerierung als JSON-Dateien geschrieben
 * ({@code worldgen/feature}, {@code worldgen/placed_feature}, {@code neoforge/biome_modifier}).
 */
public final class ModWorldgen {
    /** Eine Erzader: wo und wie oft sie erscheint. */
    public record OreVein(String name, Supplier<? extends Block> stoneOre, @Nullable Supplier<? extends Block> deepslateOre,
            int veinSize, float airExposureDiscard, int veinsPerChunk, boolean triangle, int minY, int maxY,
            TagKey<Biome> biomes) {
        public ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("ore_" + name));
        }

        public ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("ore_" + name));
        }

        public ResourceKey<BiomeModifier> modifierKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_ore_" + name));
        }
    }

    /** Ein Feld wilder Blumen. */
    public record FlowerPatch(String name, Supplier<? extends Block> flower, int rarity, TagKey<Biome> biomes) {
        public ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("flower_" + name));
        }

        public ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("flower_" + name));
        }

        public ResourceKey<BiomeModifier> modifierKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_flower_" + name));
        }
    }

    /**
     * Alle Erzadern. Höhen orientieren sich an den Beschreibungen im Konzeptbild:
     * Kohlenstoff überall, Konterit und Wyvernit in den Bergen, Draconium ganz tief und sehr selten.
     *
     * <p>Wie bei Vanilla (Smaragd, Diamant) reicht jede Höhenverteilung ein Stück in die andere
     * Gesteinsschicht hinein. Tiefenschiefer gibt es nur bis etwa Höhe 8, Stein erst ab etwa Höhe 0.
     * Darum ist Tiefenschiefer-Konteriterz sehr selten und Draconiumerz im normalen Stein ebenso.
     */
    public static final List<OreVein> ORE_VEINS = List.of(
            new OreVein("carbon", ModBlocks.CARBON_ORE, ModBlocks.DEEPSLATE_CARBON_ORE,
                    12, 0.0f, 18, false, 0, 192, BiomeTags.IS_OVERWORLD),
            new OreVein("gemstone", ModBlocks.GEMSTONE_ORE, ModBlocks.DEEPSLATE_GEMSTONE_ORE,
                    5, 0.5f, 3, true, -64, 16, BiomeTags.IS_OVERWORLD),
            new OreVein("konterit", ModBlocks.KONTERIT_ORE, ModBlocks.DEEPSLATE_KONTERIT_ORE,
                    7, 0.0f, 10, true, -16, 320, BiomeTags.IS_MOUNTAIN),
            new OreVein("aetherit", ModBlocks.AETHERIT_ORE, ModBlocks.DEEPSLATE_AETHERIT_ORE,
                    5, 0.3f, 4, false, -48, 48, BiomeTags.IS_OVERWORLD),
            new OreVein("draconium", ModBlocks.DRACONIUM_ORE, ModBlocks.DEEPSLATE_DRACONIUM_ORE,
                    3, 0.7f, 2, true, -80, 8, BiomeTags.IS_OVERWORLD),
            new OreVein("wyvernit", ModBlocks.WYVERNIT_ORE, null,
                    4, 0.0f, 8, false, 100, 320, BiomeTags.IS_MOUNTAIN));

    /** Wilde Zierblumen (Bild "Nahrung & Pflanzen", Abschnitt 5). */
    public static final List<FlowerPatch> FLOWER_PATCHES = List.of(
            new FlowerPatch("lavender", ModBlocks.LAVENDER, 6, Tags.Biomes.IS_PLAINS),
            new FlowerPatch("garden_rose", ModBlocks.GARDEN_ROSE, 8, BiomeTags.IS_FOREST),
            new FlowerPatch("fire_lily", ModBlocks.FIRE_LILY, 8, BiomeTags.IS_SAVANNA),
            new FlowerPatch("frost_flower", ModBlocks.FROST_FLOWER, 6, Tags.Biomes.IS_SNOWY));

    private ModWorldgen() {}

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        RuleTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        for (OreVein vein : ORE_VEINS) {
            List<BlockReplacement> targets = new ArrayList<>();
            targets.add(BlockReplacement.replace(stone, vein.stoneOre().get().defaultBlockState()));
            if (vein.deepslateOre() != null) {
                targets.add(BlockReplacement.replace(deepslate, vein.deepslateOre().get().defaultBlockState()));
            }
            context.register(vein.featureKey(), new OreFeature(targets, vein.veinSize(), vein.airExposureDiscard()));
        }

        for (FlowerPatch patch : FLOWER_PATCHES) {
            context.register(patch.featureKey(), new SimpleBlockFeature(BlockStateProvider.of(patch.flower().get())));
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);

        for (OreVein vein : ORE_VEINS) {
            VerticalAnchor bottom = VerticalAnchor.absolute(vein.minY());
            VerticalAnchor top = VerticalAnchor.absolute(vein.maxY());
            List<PlacementModifier> placement = List.of(
                    CountPlacement.of(vein.veinsPerChunk()),
                    InSquarePlacement.spread(),
                    vein.triangle() ? HeightRangePlacement.triangle(bottom, top) : HeightRangePlacement.uniform(bottom, top),
                    BiomeFilter.biome());
            context.register(vein.placedKey(), new PlacedFeature(features.getOrThrow(vein.featureKey()), placement));
        }

        for (FlowerPatch patch : FLOWER_PATCHES) {
            List<PlacementModifier> placement = List.of(
                    RarityFilter.onAverageOnceEvery(patch.rarity()),
                    InSquarePlacement.spread(),
                    HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING),
                    BiomeFilter.biome(),
                    CountPlacement.of(24),
                    new OffsetPlacement(TrapezoidInt.triangle(6), TrapezoidInt.triangle(2), TrapezoidInt.triangle(6)),
                    BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                            BlockPredicate.ONLY_IN_AIR_PREDICATE,
                            BlockPredicate.wouldSurvive(patch.flower().get()))));
            context.register(patch.placedKey(), new PlacedFeature(features.getOrThrow(patch.featureKey()), placement));
        }
    }

    /** Biom-Modifikator für das Erscheinen der Banditen. */
    public static final ResourceKey<BiomeModifier> BANDIT_SPAWNS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_bandit_spawns"));
    public static final ResourceKey<BiomeModifier> WILD_BOAR_SPAWNS = spawnModifier("add_wild_boar_spawns");
    public static final ResourceKey<BiomeModifier> DEER_SPAWNS = spawnModifier("add_deer_spawns");
    public static final ResourceKey<BiomeModifier> GOOSE_SPAWNS = spawnModifier("add_goose_spawns");
    public static final ResourceKey<BiomeModifier> WILL_O_WISP_SPAWNS = spawnModifier("add_will_o_wisp_spawns");
    public static final ResourceKey<BiomeModifier> CAVE_TROLL_SPAWNS = spawnModifier("add_cave_troll_spawns");
    public static final ResourceKey<BiomeModifier> LINDWORM_SPAWNS = spawnModifier("add_lindworm_spawns");

    private static ResourceKey<BiomeModifier> spawnModifier(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id(name));
    }

    private static BiomeModifier spawns(HolderGetter<Biome> biomes, TagKey<Biome> where, EntityType<?> type, int weight, int min, int max) {
        return new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(where),
                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(type, UniformInt.of(min, max)), weight)
                        .build());
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        for (OreVein vein : ORE_VEINS) {
            context.register(vein.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(vein.biomes()),
                    HolderSet.direct(placed.getOrThrow(vein.placedKey())),
                    GenerationStep.Decoration.UNDERGROUND_ORES));
        }

        for (FlowerPatch patch : FLOWER_PATCHES) {
            context.register(patch.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(patch.biomes()),
                    HolderSet.direct(placed.getOrThrow(patch.placedKey())),
                    GenerationStep.Decoration.VEGETAL_DECORATION));
        }

        // Banditen: nachts unter freiem Himmel, in kleinen Gruppen
        context.register(BANDIT_SPAWNS, new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_BANDITS),
                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.BANDIT.get(), UniformInt.of(2, 3)), 8)
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.BANDIT_ARCHER.get(), UniformInt.of(1, 2)), 5)
                        .build()));
        // Wilde Kreaturen
        context.register(WILD_BOAR_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_WILD_BOARS, ModEntities.WILD_BOAR.get(), 8, 2, 4));
        context.register(DEER_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_DEER, ModEntities.DEER.get(), 8, 2, 4));
        context.register(GOOSE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_GEESE, ModEntities.GOOSE.get(), 7, 3, 5));
        context.register(WILL_O_WISP_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_WILL_O_WISPS, ModEntities.WILL_O_WISP.get(), 12, 1, 3));
        context.register(CAVE_TROLL_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_CAVE_TROLLS, ModEntities.CAVE_TROLL.get(), 6, 1, 1));
        context.register(LINDWORM_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_LINDWORMS, ModEntities.LINDWORM.get(), 3, 1, 1));
    }
}
