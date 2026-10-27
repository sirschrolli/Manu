package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.ReedsBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.KingdomBiomeSource;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldFeatures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der eigenen Oberwelt-Biome: Weltvoreinstellungen und Biomverteilung, eine Volkszählung der Biome über ein weites
 * Gebiet, die Reihenfolge der Features (sonst ließe sich keine Welt laden), jedes eigene Feature auf passendem Boden
 * und die neuen Moorblöcke.
 */
final class OverworldGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OVERWORLD_BIOMES =
            ModGameTests.TEST_FUNCTIONS.register("overworld_biomes", () -> OverworldGameTests::biomes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OVERWORLD_FEATURE_ORDER =
            ModGameTests.TEST_FUNCTIONS.register("overworld_feature_order", () -> OverworldGameTests::featureOrder);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> OVERWORLD_FEATURES =
            ModGameTests.TEST_FUNCTIONS.register("overworld_features", () -> OverworldGameTests::features);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MOOR_BLOCKS =
            ModGameTests.TEST_FUNCTIONS.register("moor_blocks", () -> OverworldGameTests::moorBlocks);

    private OverworldGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(OVERWORLD_BIOMES, OVERWORLD_FEATURE_ORDER, OVERWORLD_FEATURES, MOOR_BLOCKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /**
     * Die Weltvoreinstellungen „Standard“, „Große Biome“ und „Zerklüftet“ verwenden die eigene Biomverteilung; sie kennt
     * alle acht eigenen Biome, aber kein Nest-Biom. Eine Volkszählung über 25 600 × 25 600 Blöcke (an der echten
     * Geländeoberfläche) findet jedes eigene Biom und verdrängt kein Vanilla-Biom ganz; die Anteile stehen im Log.
     */
    private static void biomes(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().getServer().registryAccess();
        HolderLookup.RegistryLookup<WorldPreset> presets = registries.lookupOrThrow(Registries.WORLD_PRESET);
        for (ResourceKey<WorldPreset> key : List.of(WorldPresets.NORMAL, WorldPresets.LARGE_BIOMES, WorldPresets.AMPLIFIED)) {
            LevelStem stem = presets.getOrThrow(key).value().overworld().orElse(null);
            helper.assertTrue(stem != null && stem.generator().getBiomeSource() instanceof KingdomBiomeSource,
                    "Die Weltvoreinstellung " + key.identifier() + " nutzt nicht die Biomverteilung von Kingdoms at War");
        }
        ChunkGenerator generator = WorldPresets.getNormalOverworld(registries).generator();
        Set<Holder<Biome>> possible = generator.getBiomeSource().possibleBiomes();
        for (ResourceKey<Biome> biome : OverworldBiomes.ALL) {
            helper.assertTrue(possible.stream().anyMatch(holder -> holder.is(biome)), "Biom fehlt in der Oberwelt: " + biome.identifier());
        }
        for (Holder<Biome> holder : possible) {
            helper.assertFalse(ModBiomes.isNestBiome(holder), "Ein Nest-Biom liegt in der normalen Biomverteilung: " + holder);
        }
        // die ersetzten Vanilla-Biome gibt es (bis auf die vereisten Gipfel, die felsige Küste und die Savannenhochebenen) weiter
        for (ResourceKey<Biome> vanilla : List.of(Biomes.DESERT, Biomes.SNOWY_SLOPES, Biomes.FOREST, Biomes.BEACH, Biomes.PLAINS, Biomes.SWAMP)) {
            helper.assertTrue(possible.stream().anyMatch(holder -> holder.is(vanilla)), "Vanilla-Biom fehlt ganz: " + vanilla.identifier());
        }

        NoiseGeneratorSettings settings = ((NoiseBasedChunkGenerator) generator).generatorSettings().value();
        RandomState random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), 20_261_027L, settings);
        BiomeResolver resolver = generator.getBiomeSource().createUncachedResolver(random);
        LevelHeightAccessor height = LevelHeightAccessor.create(settings.noiseSettings().minY(), settings.noiseSettings().height());
        Map<ResourceKey<Biome>, Integer> counts = new HashMap<>();
        int samples = 0;
        int land = 0;
        for (int i = 0; i < 80; i++) {
            for (int j = 0; j < 80; j++) {
                int x = (i - 40) * 320 + 17;
                int z = (j - 40) * 320 - 41;
                int ground = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, random);
                Holder<Biome> biome = resolver.getNoiseBiome(x >> 2, Math.max(ground, 63) >> 2, z >> 2);
                biome.unwrapKey().ifPresent(key -> counts.merge(key, 1, Integer::sum));
                samples++;
                if (ground >= 63) {
                    land++;
                }
            }
        }
        StringBuilder report = new StringBuilder();
        List<ResourceKey<Biome>> missing = new ArrayList<>();
        for (ResourceKey<Biome> biome : OverworldBiomes.ALL) {
            int count = counts.getOrDefault(biome, 0);
            report.append(String.format(Locale.ROOT, "%s %.2f%% ", biome.identifier().getPath(), 100.0 * count / samples));
            if (count == 0) {
                missing.add(biome);
            }
        }
        for (ResourceKey<Biome> vanilla : List.of(Biomes.DESERT, Biomes.FROZEN_PEAKS, Biomes.SNOWY_SLOPES, Biomes.STONY_SHORE, Biomes.FOREST,
                Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA, Biomes.BEACH, Biomes.PLAINS, Biomes.SWAMP)) {
            report.append(String.format(Locale.ROOT, "| %s %.2f%% ", vanilla.identifier().getPath(),
                    100.0 * counts.getOrDefault(vanilla, 0) / samples));
        }
        KingdomsAtWar.LOGGER.info("[Biome] Volkszählung ({} Proben, {} an Land): {}", samples, land, report);
        logNoise(random);
        helper.assertTrue(missing.isEmpty(), "Diese Biome kamen in der Volkszählung nicht vor: " + missing);
        // die vollständig ersetzten Vanilla-Biome dürfen an der Oberfläche nicht mehr vorkommen
        for (ResourceKey<Biome> replaced : List.of(Biomes.FROZEN_PEAKS, Biomes.STONY_SHORE, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA)) {
            helper.assertTrue(counts.getOrDefault(replaced, 0) == 0, "Das Vanilla-Biom " + replaced.identifier() + " kommt noch vor");
        }
        helper.succeed();
    }

    /** Verteilung der Rauschfelder, nach denen die Oberflächen gewählt werden (zum Abstimmen der Schwellen, nur fürs Log). */
    private static void logNoise(RandomState random) {
        record Probe(ResourceKey<net.minecraft.world.level.levelgen.synth.NormalNoise> noise, double[][] ranges) {}
        List<Probe> probes = List.of(
                new Probe(net.minecraft.world.level.levelgen.Noises.SURFACE,
                        new double[][] {{0.28, 9}, {0.22, 9}, {0.3, 9}, {0.12, 9}, {-0.12, 9}, {-0.06, 0.02}, {-0.31, -0.27}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.PATCH, new double[][] {{0.18, 9}, {0.3, 9}, {0.12, 9}, {0.25, 9}, {0.0, 9}, {-0.1, 9}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.SMALL_PATCH, new double[][] {{2.3, 9}, {1.8, 9}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.PACKED_ICE, new double[][] {{0.0, 0.18}, {-0.15, 0.15}, {-0.5, 0.2}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.ICE, new double[][] {{0.0, 0.03}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.POWDER_SNOW, new double[][] {{0.42, 0.56}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.GRAVEL, new double[][] {{-0.06, 0.06}}),
                new Probe(net.minecraft.world.level.levelgen.Noises.SWAMP, new double[][] {{0.0, 9}}));
        for (Probe probe : probes) {
            var noise = random.getOrCreateNoise(probe.noise());
            double[] values = new double[40_000];
            for (int i = 0; i < values.length; i++) {
                values[i] = noise.get((i % 200) * 3 + 1000, 0.0, (i / 200) * 3 - 700);
            }
            double[] sorted = values.clone();
            java.util.Arrays.sort(sorted);
            StringBuilder out = new StringBuilder(String.format(Locale.ROOT, "%s q05 %.3f q25 %.3f q50 %.3f q75 %.3f q95 %.3f |",
                    probe.noise().identifier().getPath(), sorted[2000], sorted[10000], sorted[20000], sorted[30000], sorted[38000]));
            for (double[] range : probe.ranges()) {
                long inside = java.util.Arrays.stream(values).filter(v -> v >= range[0] && v <= range[1]).count();
                out.append(String.format(Locale.ROOT, " [%.2f..%s] %.1f%%", range[0], range[1] > 8 ? "" : String.format(Locale.ROOT, "%.2f", range[1]),
                        100.0 * inside / values.length));
            }
            KingdomsAtWar.LOGGER.info("[Biome] Rauschen {}", out);
        }
    }

    /**
     * Die Features aller Biome der Oberwelt (samt den Biom-Modifikatoren der Mod) lassen sich in eine widerspruchsfreie
     * Reihenfolge bringen – sonst stürzt das Spiel beim Laden einer neuen Welt ab. Jedes eigene Biom hat seine Features.
     */
    private static void featureOrder(GameTestHelper helper) {
        HolderLookup.Provider registries = helper.getLevel().getServer().registryAccess();
        ChunkGenerator generator = WorldPresets.getNormalOverworld(registries).generator();
        List<Holder<Biome>> biomes = List.copyOf(generator.getBiomeSource().possibleBiomes());
        try {
            List<FeatureSorter.StepFeatureData> steps = FeatureSorter.buildFeaturesPerStep(biomes,
                    biome -> biome.value().getGenerationSettings().features(), true);
            helper.assertTrue(!steps.isEmpty(), "Keine Features in der Oberwelt");
        } catch (IllegalStateException e) {
            helper.fail("Die Features der Oberwelt-Biome widersprechen sich in der Reihenfolge: " + e.getMessage());
            return;
        }
        var placed = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        for (ResourceKey<Biome> key : OverworldBiomes.ALL) {
            Biome biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(key).value();
            long own = OverworldFeatures.entries().stream()
                    .filter(entry -> entry.name().startsWith(key.identifier().getPath() + "/"))
                    .filter(entry -> biome.getGenerationSettings().hasFeature(placed.getOrThrow(entry.placedKey()).value()))
                    .count();
            helper.assertTrue(own >= 4, "Das Biom " + key.identifier() + " hat zu wenige eigene Features: " + own);
        }
        helper.succeed();
    }

    /**
     * Jedes eigene Feature, wie es aus den Daten geladen wurde, entsteht auf passendem Boden: Bäume, Felsen, Nadeln,
     * Risse, Riffe, Tümpel, Schilf, Eiszapfen und Pflanzen. Jedes bekommt ein eigenes Stück Land weit draußen.
     */
    private static void features(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        var features = level.getServer().registryAccess().lookupOrThrow(Registries.FEATURE);
        List<String> failed = new ArrayList<>();
        int index = 0;
        int y0 = 120;
        for (OverworldFeatures.Entry entry : OverworldFeatures.entries()) {
            if (entry.own() == null) {
                continue;
            }
            BlockPos origin = new BlockPos(60_000 + index * 80, y0, 60_000);
            index++;
            Feature feature = features.getOrThrow(entry.featureKey()).value();
            boolean placed = false;
            for (int attempt = 0; attempt < 6 && !placed; attempt++) {
                prepare(level, origin, entry.name());
                placed = feature.place(level, generator, RandomSource.create(7_000L + attempt * 131L + index), origin);
            }
            if (!placed) {
                failed.add(entry.name());
            }
        }
        helper.assertTrue(failed.isEmpty(), "Diese Features entstanden nicht: " + failed);
        helper.succeed();
    }

    /** Ein Stück Land für ein Feature: Boden je nach Biom, darüber Luft; für manche ein Teich, ein Becken oder ein Felsdach. */
    private static void prepare(ServerLevel level, BlockPos origin, String name) {
        BlockState ground = Blocks.GRASS_BLOCK.defaultBlockState();
        if (name.startsWith("ash_fields/")) {
            ground = (name.endsWith("fire_lilies") ? ModBlocks.CINDER_SOIL.get() : ModBlocks.ASH_BLOCK.get()).defaultBlockState();
        } else if (name.startsWith("frost_peaks/")) {
            ground = name.endsWith("frost_flowers") ? ModBlocks.FROZEN_SOIL.get().defaultBlockState()
                    : name.endsWith("icicles") ? ModBlocks.RIME_STONE.base().get().defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState();
        } else if (name.startsWith("storm_coast/") && !name.endsWith("trees")) {
            ground = ModBlocks.STORMSTONE.base().get().defaultBlockState();
        } else if (name.startsWith("coral_coast/")) {
            ground = Blocks.SAND.defaultBlockState();
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -15; dx <= 15; dx++) {
            for (int dz = -15; dz <= 15; dz++) {
                for (int dy = -6; dy <= 40; dy++) {
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = dy >= 0 ? Blocks.AIR.defaultBlockState() : dy == -6 ? Blocks.STONE.defaultBlockState()
                            : dy == -1 ? ground : ground.is(Blocks.GRASS_BLOCK) ? Blocks.DIRT.defaultBlockState() : ground;
                    level.setBlock(pos, state, 2);
                }
            }
        }
        if (name.equals("coral_coast/reefs")) {
            // ein flaches Becken: drei Blöcke Wasser über Sand
            for (int dx = -9; dx <= 9; dx++) {
                for (int dz = -9; dz <= 9; dz++) {
                    for (int dy = -1; dy >= -3; dy--) {
                        level.setBlock(origin.offset(dx, dy, dz), Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }
        } else if (name.equals("mist_moor/reeds")) {
            // ein knietiefer Teich mitten im Gras
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    level.setBlock(origin.offset(dx, -1, dz), Blocks.WATER.defaultBlockState(), 2);
                }
            }
        } else if (name.equals("frost_peaks/icicles")) {
            // ein Felsdach fünf Blöcke über dem Boden
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    level.setBlock(origin.offset(dx, 5, dz), Blocks.PACKED_ICE.defaultBlockState(), 2);
                }
            }
        }
    }

    /**
     * Torf und Schilf: Torf brennt im Ofen; Schilf steht im knietiefen Wasser (der Fuß ist geflutet, der Kolben nicht), und
     * wer den Kolben abbricht, hinterlässt Wasser statt einer Luftblase; an Land wächst Schilf auf Erde, nicht auf Stein.
     */
    private static void moorBlocks(GameTestHelper helper) {
        helper.assertTrue(new ItemStack(ModBlocks.PEAT.get()).has(DataComponents.COOKING_FUEL), "Torf brennt nicht im Ofen");
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(3, 1, 3));
        BlockPos water = base.above();
        level.setBlock(base, Blocks.DIRT.defaultBlockState(), 3);
        for (BlockPos side : List.of(water.north(), water.south(), water.east(), water.west())) {
            level.setBlock(side, Blocks.DIRT.defaultBlockState(), 3);
        }
        level.setBlock(water, Blocks.WATER.defaultBlockState(), 3);
        level.setBlock(water.above(), Blocks.AIR.defaultBlockState(), 3);
        BlockState reeds = ModBlocks.REEDS.get().defaultBlockState();
        helper.assertTrue(reeds.canSurvive(level, water), "Schilf kann nicht im knietiefen Wasser stehen");
        DoublePlantBlock.placeAt(level, reeds, water, 3);
        BlockState lower = level.getBlockState(water);
        BlockState upper = level.getBlockState(water.above());
        helper.assertTrue(lower.is(ModBlocks.REEDS.get()) && lower.getValue(ReedsBlock.WATERLOGGED)
                && lower.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER, "Der Schilffuß steht nicht im Wasser: " + lower);
        helper.assertTrue(upper.is(ModBlocks.REEDS.get()) && !upper.getValue(ReedsBlock.WATERLOGGED)
                && upper.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER, "Der Schilfkolben fehlt: " + upper);
        level.destroyBlock(water.above(), false);
        helper.assertTrue(level.getBlockState(water).is(Blocks.WATER), "Nach dem Abbrechen bleibt kein Wasser: " + level.getBlockState(water));
        // an Land: auf Erde ja, auf Stein nein
        BlockPos land = helper.absolutePos(new BlockPos(6, 1, 6));
        level.setBlock(land, Blocks.DIRT.defaultBlockState(), 3);
        helper.assertTrue(reeds.canSurvive(level, land.above()), "Schilf wächst nicht auf Erde");
        level.setBlock(land, Blocks.STONE.defaultBlockState(), 3);
        helper.assertFalse(reeds.canSurvive(level, land.above()), "Schilf wächst auf Stein");
        helper.succeed();
    }
}
