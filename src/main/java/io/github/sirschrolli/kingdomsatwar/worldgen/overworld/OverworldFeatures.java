package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.AncientTreeFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BogPoolFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.BoulderFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.CharredTreeFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.DuneFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.FallenTrunkFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.FissureFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.GrandOakFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.IceSpikeFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.IcicleFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.MoorTreeFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.PalmFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ReedBedFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ReefFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SpireFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.VentFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.WindsweptTreeFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.Nullable;

/**
 * Was in den eigenen Biomen der Oberwelt wächst und steht – und welches Biom was davon bekommt.
 *
 * <p>Alle Biome einer Welt müssen ihre Features je Erzeugungsschritt in einer widerspruchsfreien Reihenfolge haben, sonst
 * verweigert das Spiel die Welt. Darum übernimmt jedes eigene Biom die Vanilla-Features eines verwandten Vanilla-Bioms
 * in dessen Reihenfolge (höchstens mit Lücken) und hängt die eigenen danach an – in der festen Reihenfolge von
 * {@link #ENTRIES}. Eigene Features stehen nie vor Vanilla-Features.
 */
public final class OverworldFeatures {
    private OverworldFeatures() {}

    /**
     * Ein platziertes Feature der eigenen Biome: Schritt, Name und entweder ein eigenes Feature ({@code own}) oder ein
     * vorhandenes (Vanilla-)Feature ({@code existing}), dazu wie und wie oft es gesetzt wird.
     */
    public record Entry(GenerationStep.Decoration step, String name, @Nullable Supplier<Feature> own, @Nullable ResourceKey<Feature> existing,
            List<PlacementModifier> placement) {
        public ResourceKey<Feature> featureKey() {
            return this.own != null ? ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("overworld/" + this.name)) : this.existing;
        }

        public ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("overworld/" + this.name));
        }
    }

    /** Alle Einträge in ihrer festen Reihenfolge. */
    static final List<Entry> ENTRIES = new ArrayList<>();

    /** Alle Einträge (für Tests und Vorführung). */
    public static List<Entry> entries() {
        return java.util.Collections.unmodifiableList(ENTRIES);
    }

    private static Entry own(GenerationStep.Decoration step, String name, Supplier<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, feature, null, placement);
        ENTRIES.add(entry);
        return entry;
    }

    private static Entry existing(GenerationStep.Decoration step, String name, ResourceKey<Feature> feature, List<PlacementModifier> placement) {
        Entry entry = new Entry(step, name, null, feature, placement);
        ENTRIES.add(entry);
        return entry;
    }

    // =====================================================================================
    // Platzierungen
    // =====================================================================================

    /** Auf dem Boden (auch am Grund von höchstens {@code maxWater} Blöcken Wasser). */
    private static List<PlacementModifier> ground(PlacementModifier frequency, int maxWater) {
        return List.of(frequency, InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(maxWater),
                HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR), BiomeFilter.biome());
    }

    /** Ein Grüppchen Pflanzen: {@code tries} Versuche rund um eine Stelle (nur in Luft). */
    private static List<PlacementModifier> patch(PlacementModifier frequency, int tries, int spread) {
        return List.of(frequency, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome(), CountPlacement.of(tries),
                OffsetPlacement.ofTriangle(spread, 2), BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    /** Ein Vanilla-Baum, wo sein Setzling wachsen könnte. */
    private static List<PlacementModifier> tree(PlacementModifier frequency, Block sapling) {
        return List.of(frequency, InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(0), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BiomeFilter.biome(), PlacementUtils.filteredByBlockSurvival(sapling));
    }

    /** Am Grund flachen Wassers (wie Seegras). */
    private static List<PlacementModifier> seabed(int count) {
        return List.of(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, CountPlacement.of(count), BiomeFilter.biome());
    }

    /** An der Oberfläche (Wasseroberfläche eingeschlossen). */
    private static List<PlacementModifier> anywhere(PlacementModifier frequency) {
        return List.of(frequency, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    }

    private static PlacementModifier rare(int oneIn) {
        return RarityFilter.onAverageOnceEvery(oneIn);
    }

    private static PlacementModifier count(int count) {
        return CountPlacement.of(count);
    }

    // =====================================================================================
    // Bausteine
    // =====================================================================================

    private static BlockState state(Supplier<? extends Block> block) {
        return block.get().defaultBlockState();
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

    private static final GenerationStep.Decoration LAKES = GenerationStep.Decoration.LAKES;
    private static final GenerationStep.Decoration LOCAL = GenerationStep.Decoration.LOCAL_MODIFICATIONS;
    private static final GenerationStep.Decoration UNDERGROUND = GenerationStep.Decoration.UNDERGROUND_DECORATION;
    private static final GenerationStep.Decoration VEGETATION = GenerationStep.Decoration.VEGETAL_DECORATION;
    private static final GenerationStep.Decoration TOP = GenerationStep.Decoration.TOP_LAYER_MODIFICATION;

    // =====================================================================================
    // Die Features (Reihenfolge je Schritt = Reihenfolge in den Biomen)
    // =====================================================================================

    // --- Seen ---
    /** Moorauge: Tümpel mit Torfufer im Nebelmoor. */
    static final Entry MOOR_POOLS = own(LAKES, "mist_moor/bog_pools", BogPoolFeature::new, anywhere(count(1)));

    // --- Felsen, Nadeln, Risse ---
    static final Entry ASH_SPIRES = own(LOCAL, "ash_fields/spires", () -> new SpireFeature(state(ModBlocks.VOLCANIC_ROCK.base()),
            Blocks.SMOOTH_BASALT.defaultBlockState(), 4, Blocks.AIR.defaultBlockState(), 4, 10, 1.5f, 0.15f, false), ground(rare(6), 0));
    static final Entry ASH_BOULDERS = own(LOCAL, "ash_fields/boulders", () -> new BoulderFeature(state(ModBlocks.VOLCANIC_ROCK.base()),
            Blocks.SMOOTH_BASALT.defaultBlockState(), 0.25f, state(ModBlocks.GLOWING_VOLCANIC_ROCK), 0.05f,
            ModBlocks.ASH_LAYER.get().defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1), 1.8f, 3.4f, false), ground(rare(3), 0));
    static final Entry ASH_FISSURES = own(LOCAL, "ash_fields/fissures", FissureFeature::new, ground(rare(3), 0));
    static final Entry ASH_VENTS = own(LOCAL, "ash_fields/vents", VentFeature::new, ground(rare(4), 0));

    static final Entry FROST_SPIKES = own(LOCAL, "frost_peaks/ice_spikes", () -> new IceSpikeFeature(5, 14), ground(rare(4), 0));
    static final Entry FROST_BOULDERS = own(LOCAL, "frost_peaks/boulders", () -> new BoulderFeature(state(ModBlocks.RIME_STONE.base()),
            Blocks.PACKED_ICE.defaultBlockState(), 0.2f, state(ModBlocks.FROST_CRYSTAL), 0.04f, Blocks.SNOW.defaultBlockState(), 1.6f, 3.0f,
            false), ground(rare(3), 0));

    static final Entry STORM_STACKS = own(LOCAL, "storm_coast/sea_stacks", () -> new SpireFeature(state(ModBlocks.STORMSTONE.base()),
            state(ModBlocks.SHALE.base()), 6, Blocks.AIR.defaultBlockState(), 5, 13, 2.2f, 0.1f, true), ground(rare(2), 6));
    static final Entry STORM_BOULDERS = own(LOCAL, "storm_coast/boulders", () -> new BoulderFeature(state(ModBlocks.STORMSTONE.base()),
            Blocks.ANDESITE.defaultBlockState(), 0.25f, state(ModBlocks.CHARGED_STORMSTONE), 0.05f, Blocks.AIR.defaultBlockState(), 1.6f, 3.0f,
            true), ground(count(1), 4));
    static final Entry STORM_FULGURITES = own(LOCAL, "storm_coast/fulgurites", () -> new SpireFeature(state(ModBlocks.FULGURITE),
            state(ModBlocks.FULGURITE), 64, state(ModBlocks.STORM_CRYSTAL), 1, 2, 0.5f, 0.0f, false), ground(rare(3), 0));

    static final Entry PRIMEVAL_BOULDERS = own(LOCAL, "primeval_forest/mossy_boulders", () -> new BoulderFeature(
            Blocks.COBBLESTONE.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 0.55f, state(ModBlocks.GLOWING_MOSS), 0.03f,
            Blocks.MOSS_CARPET.defaultBlockState(), 1.4f, 2.6f, false), ground(rare(3), 0));

    static final Entry GORGE_SPIRES = own(LOCAL, "rock_gorges/spires", () -> new SpireFeature(Blocks.TUFF.defaultBlockState(),
            state(ModBlocks.SHALE.base()), 3, Blocks.AIR.defaultBlockState(), 7, 18, 2.0f, 0.08f, false), ground(rare(2), 0));
    static final Entry GORGE_BOULDERS = own(LOCAL, "rock_gorges/boulders", () -> new BoulderFeature(state(ModBlocks.SHALE.base()),
            Blocks.TUFF.defaultBlockState(), 0.3f, state(ModBlocks.AMBER_CRYSTAL), 0.03f, Blocks.AIR.defaultBlockState(), 1.6f, 3.2f, false),
            ground(count(1), 0));

    static final Entry CORAL_ROCKS = own(LOCAL, "coral_coast/nacre_rocks", () -> new BoulderFeature(state(ModBlocks.NACRE_BLOCK),
            Blocks.CALCITE.defaultBlockState(), 0.35f, Blocks.PRISMARINE.defaultBlockState(), 0.05f, Blocks.AIR.defaultBlockState(), 1.4f,
            2.6f, true), ground(rare(2), 3));

    static final Entry MOOR_BOULDERS = own(LOCAL, "mist_moor/boulders", () -> new BoulderFeature(Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 0.6f, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 0.0f,
            Blocks.MOSS_CARPET.defaultBlockState(), 1.2f, 2.2f, false), ground(rare(4), 0));

    // --- unter Überhängen ---
    static final Entry FROST_ICICLES = own(UNDERGROUND, "frost_peaks/icicles", () -> new IcicleFeature(24), anywhere(count(3)));

    // --- Pflanzen und Bäume ---
    static final Entry ASH_DUNES = own(VEGETATION, "ash_fields/ash_drifts", () -> new DuneFeature(
            ModBlocks.ASH_LAYER.get().defaultBlockState(), 4.0f, 3), ground(count(2), 0));
    /** Verkohlte Haine, wo einst Wald stand … */
    static final Entry ASH_GROVES = own(VEGETATION, "ash_fields/charred_groves", CharredTreeFeature::new,
            ground(NoiseThresholdCountPlacement.of(0.45, 0, 3), 0));
    /** … und einzelne verkohlte Bäume auf der offenen Asche. */
    static final Entry ASH_TREES = own(VEGETATION, "ash_fields/charred_trees", CharredTreeFeature::new, ground(rare(3), 0));
    static final Entry ASH_LILIES = own(VEGETATION, "ash_fields/fire_lilies", block(ModBlocks.FIRE_LILY), patch(rare(4), 14, 5));

    static final Entry FROST_FLOWERS = own(VEGETATION, "frost_peaks/frost_flowers", block(ModBlocks.FROST_FLOWER), patch(rare(3), 12, 5));

    static final Entry STORM_TREES = own(VEGETATION, "storm_coast/windswept_trees", WindsweptTreeFeature::new, ground(count(2), 0));
    static final Entry STORM_CRYSTALS = own(VEGETATION, "storm_coast/storm_crystals", block(ModBlocks.STORM_CRYSTAL), patch(rare(3), 8, 4));

    static final Entry PRIMEVAL_GIANTS = own(VEGETATION, "primeval_forest/giant_trees", () -> new AncientTreeFeature(17, 24, 2, 6.5f),
            ground(rare(2), 0));
    static final Entry PRIMEVAL_ELDERS = own(VEGETATION, "primeval_forest/elder_trees", () -> new AncientTreeFeature(22, 30, 3, 7.5f),
            ground(rare(9), 0));
    static final Entry PRIMEVAL_TREES = own(VEGETATION, "primeval_forest/ancient_trees", () -> new AncientTreeFeature(9, 14, 1, 4.6f),
            ground(count(3), 0));
    static final Entry PRIMEVAL_OAKS = existing(VEGETATION, "primeval_forest/fancy_oaks", TreeFeatures.FANCY_OAK,
            tree(rare(2), Blocks.OAK_SAPLING));
    static final Entry PRIMEVAL_FALLEN = own(VEGETATION, "primeval_forest/fallen_trunks", () -> new FallenTrunkFeature(
            ModBlocks.ANCIENT_LOG.get().defaultBlockState(), 5, 10), ground(rare(3), 0));
    static final Entry PRIMEVAL_FERNS = own(VEGETATION, "primeval_forest/ferns", weighted(b -> b.add(Blocks.FERN.defaultBlockState(), 3)
            .add(Blocks.LARGE_FERN.defaultBlockState(), 1)), patch(count(3), 24, 6));
    static final Entry PRIMEVAL_GLOWCAPS = own(VEGETATION, "primeval_forest/glowcaps", block(ModBlocks.GLOWCAP), patch(rare(2), 10, 4));
    static final Entry PRIMEVAL_BELLS = own(VEGETATION, "primeval_forest/grove_bells", block(ModBlocks.GROVE_BELL), patch(rare(3), 12, 4));
    static final Entry PRIMEVAL_BRAMBLES = own(VEGETATION, "primeval_forest/brambles", block(ModBlocks.BRAMBLE), patch(rare(3), 8, 3));
    static final Entry PRIMEVAL_LITTER = existing(VEGETATION, "primeval_forest/leaf_litter", VegetationFeatures.LEAF_LITTER,
            patch(count(2), 32, 7));
    static final Entry PRIMEVAL_MOSS = own(VEGETATION, "primeval_forest/moss_carpets", block(() -> Blocks.MOSS_CARPET), patch(count(2), 20, 5));

    static final Entry GORGE_ACACIAS = existing(VEGETATION, "rock_gorges/acacias", TreeFeatures.ACACIA, tree(rare(2), Blocks.ACACIA_SAPLING));
    static final Entry GORGE_DRY_GRASS = existing(VEGETATION, "rock_gorges/dry_grass", VegetationFeatures.DRY_GRASS, patch(count(2), 16, 5));
    static final Entry GORGE_AMBER_CLUSTERS = own(VEGETATION, "rock_gorges/amber_clusters", block(ModBlocks.AMBER_CLUSTER),
            patch(rare(4), 6, 3));

    static final Entry CORAL_PALMS = own(VEGETATION, "coral_coast/palms", PalmFeature::new, ground(count(3), 0));
    static final Entry CORAL_REEFS = own(VEGETATION, "coral_coast/reefs", ReefFeature::new,
            List.of(count(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome()));
    static final Entry CORAL_SEAGRASS = existing(VEGETATION, "coral_coast/seagrass", AquaticFeatures.SEAGRASS_SHORT, seabed(40));
    /** Korallenbäume, -krallen und -pilze wie im warmen Meer, nur lichter: die Riffe der Lagunen. */
    static final Entry CORAL_GARDENS = existing(VEGETATION, "coral_coast/coral_gardens", AquaticFeatures.WARM_OCEAN_VEGETATION,
            List.of(NoiseBasedCountPlacement.of(8, 400.0, 0.0), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome()));
    static final Entry CORAL_PICKLES = existing(VEGETATION, "coral_coast/sea_pickles", AquaticFeatures.SEA_PICKLE,
            List.of(rare(6), InSquarePlacement.spread(), CountPlacement.of(16), OffsetPlacement.ofTriangle(7, 0),
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome()));

    static final Entry MEADOW_GRAND_OAKS = own(VEGETATION, "royal_meadows/grand_oaks", GrandOakFeature::new, ground(rare(4), 0));
    static final Entry MEADOW_GROVES = existing(VEGETATION, "royal_meadows/oak_groves", TreeFeatures.FANCY_OAK,
            tree(NoiseThresholdCountPlacement.of(0.7, 0, 2), Blocks.OAK_SAPLING));
    static final Entry MEADOW_BIRCHES = existing(VEGETATION, "royal_meadows/birches", TreeFeatures.BIRCH,
            tree(rare(6), Blocks.BIRCH_SAPLING));
    /** Blumenfelder: in jedem Fleck blüht vor allem eine Sorte – Lavendel am häufigsten. */
    static final Entry MEADOW_FLOWER_FIELDS = own(VEGETATION, "royal_meadows/flower_fields", () -> new SimpleBlockFeature(new NoiseProvider(
            7731L, NormalNoise.createParity(0, 1.0), 0.035f, List.of(
                    state(ModBlocks.LAVENDER), state(ModBlocks.LAVENDER), Blocks.CORNFLOWER.defaultBlockState(),
                    state(ModBlocks.GARDEN_ROSE), state(ModBlocks.LAVENDER), Blocks.OXEYE_DAISY.defaultBlockState(),
                    Blocks.POPPY.defaultBlockState(), state(ModBlocks.LAVENDER), Blocks.ALLIUM.defaultBlockState(),
                    state(ModBlocks.GARDEN_ROSE), Blocks.WHITE_TULIP.defaultBlockState(), state(ModBlocks.LAVENDER),
                    Blocks.AZURE_BLUET.defaultBlockState(), state(ModBlocks.LAVENDER)))),
            patch(NoiseThresholdCountPlacement.of(0.0, 2, 4), 64, 7));
    static final Entry MEADOW_TALL_GRASS = existing(VEGETATION, "royal_meadows/tall_grass", VegetationFeatures.TALL_GRASS,
            patch(count(3), 24, 6));
    static final Entry MEADOW_WILDFLOWERS = existing(VEGETATION, "royal_meadows/wildflowers", VegetationFeatures.WILDFLOWER,
            patch(count(1), 48, 6));

    static final Entry MOOR_DEAD_TREES = own(VEGETATION, "mist_moor/dead_trees", () -> new MoorTreeFeature(true), ground(count(1), 0));
    static final Entry MOOR_BIRCHES = own(VEGETATION, "mist_moor/moor_birches", () -> new MoorTreeFeature(false),
            ground(NoiseThresholdCountPlacement.of(0.2, 0, 2), 0));
    /** Fahle Eichen: bleiche Riesen, die im Nebel wie Gespenster stehen. */
    static final Entry MOOR_PALE_OAKS = existing(VEGETATION, "mist_moor/pale_oaks", TreeFeatures.PALE_OAK, tree(rare(3), Blocks.PALE_OAK_SAPLING));
    static final Entry MOOR_REEDS = own(VEGETATION, "mist_moor/reeds", () -> new ReedBedFeature(28), anywhere(count(4)));
    static final Entry MOOR_FERNS = own(VEGETATION, "mist_moor/ferns", weighted(b -> b.add(Blocks.FERN.defaultBlockState(), 2)
            .add(Blocks.SHORT_GRASS.defaultBlockState(), 3).add(Blocks.LARGE_FERN.defaultBlockState(), 1)), patch(count(2), 20, 6));
    static final Entry MOOR_MOONFLOWERS = own(VEGETATION, "mist_moor/moonflowers", block(ModBlocks.MOONFLOWER), patch(rare(2), 10, 4));

    // --- oberste Schicht ---
    static final Entry FROST_DRIFTS = own(TOP, "frost_peaks/snow_drifts", () -> new DuneFeature(Blocks.SNOW.defaultBlockState(), 4.5f, 4),
            ground(count(2), 0));

    // =====================================================================================
    // Registrierung (Datengenerierung)
    // =====================================================================================

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        for (Entry entry : ENTRIES) {
            if (entry.own() != null) {
                context.register(entry.featureKey(), entry.own().get());
            }
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

    /** Was alle Oberwelt-Biome haben: Höhlen, Seen, Geoden, Verliese, Gesteinsvielfalt, Quellen, Frost. */
    private static void global(BiomeGenerationSettings.Builder builder) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(builder);
        BiomeDefaultFeatures.addDefaultSprings(builder);
        BiomeDefaultFeatures.addSurfaceFreezing(builder);
    }

    /** Hängt die eigenen Einträge in der festen Reihenfolge an. */
    private static BiomeGenerationSettings finish(BiomeGenerationSettings.Builder builder, Entry... chosen) {
        List<Entry> wanted = List.of(chosen);
        for (Entry entry : ENTRIES) {
            if (wanted.contains(entry)) {
                builder.addFeature(entry.step(), entry.placedKey());
            }
        }
        return builder.build();
    }

    /** Aschefelder: wie die Wüste (ohne Kakteen, Brunnen, Blumen), dazu Vulkangestein, Glutrisse und verkohlte Bäume. */
    static BiomeGenerationSettings ashFields(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        BiomeDefaultFeatures.addFossilDecoration(builder);
        global(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_DRY_GRASS_DESERT);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_DEAD_BUSH_2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        return finish(builder, ASH_SPIRES, ASH_BOULDERS, ASH_FISSURES, ASH_VENTS, ASH_DUNES, ASH_GROVES, ASH_TREES, ASH_LILIES);
    }

    /** Frostgipfel: wie die vereisten Gipfel, dazu Eisnadeln, Raureiffelsen, Eiszapfen, Schneewehen und Frostblumen. */
    static BiomeGenerationSettings frostPeaks(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        global(builder);
        BiomeDefaultFeatures.addFrozenSprings(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        return finish(builder, FROST_SPIKES, FROST_BOULDERS, FROST_ICICLES, FROST_FLOWERS, FROST_DRIFTS);
    }

    /** Sturmküste: wie die felsige Küste, dazu Brandungstürme, Sturmsteinblöcke, Blitzglas, Sturmkristalle und Windkiefern. */
    static BiomeGenerationSettings stormCoast(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        global(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_GRASS_BADLANDS);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        return finish(builder, STORM_STACKS, STORM_BOULDERS, STORM_FULGURITES, STORM_TREES, STORM_CRYSTALS);
    }

    /** Urhain: wie der Wald (ohne seine Bäume und bunten Blumen), dazu Urbäume, Moosfelsen, Farne, Leuchtkappen, Hainglocken. */
    static BiomeGenerationSettings primevalForest(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        global(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addBushes(builder);
        BiomeDefaultFeatures.addForestGrass(builder);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
        return finish(builder, PRIMEVAL_BOULDERS, PRIMEVAL_ELDERS, PRIMEVAL_GIANTS, PRIMEVAL_TREES, PRIMEVAL_OAKS, PRIMEVAL_FALLEN, PRIMEVAL_FERNS,
                PRIMEVAL_GLOWCAPS, PRIMEVAL_BELLS, PRIMEVAL_BRAMBLES, PRIMEVAL_LITTER, PRIMEVAL_MOSS);
    }

    /** Felsschluchten: wie die zerklüftete Savanne, dazu Felsnadeln, Schieferblöcke, Bernsteindrusen, Akazien und trockenes Gras. */
    static BiomeGenerationSettings rockGorges(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        global(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addShatteredSavannaGrass(builder);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        return finish(builder, GORGE_SPIRES, GORGE_BOULDERS, GORGE_ACACIAS, GORGE_DRY_GRASS, GORGE_AMBER_CLUSTERS);
    }

    /** Korallenküste: wie der Strand, dazu Palmen, Riffe, Seegras und Perlmuttfelsen. */
    static BiomeGenerationSettings coralCoast(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        global(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        BiomeDefaultFeatures.addNearWaterVegetation(builder);
        return finish(builder, CORAL_ROCKS, CORAL_PALMS, CORAL_REEFS, CORAL_SEAGRASS, CORAL_GARDENS, CORAL_PICKLES);
    }

    /** Königsauen: wie die Ebene (ohne ihre Bäume), dazu Königseichen, Eichenhaine, Blumenfelder und hohes Gras. */
    static BiomeGenerationSettings royalMeadows(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        global(builder);
        BiomeDefaultFeatures.addPlainGrass(builder);
        BiomeDefaultFeatures.addBushes(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        builder.addFeature(VEGETATION, VegetationPlacements.FLOWER_PLAINS);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder, true);
        return finish(builder, MEADOW_GRAND_OAKS, MEADOW_GROVES, MEADOW_BIRCHES, MEADOW_FLOWER_FIELDS, MEADOW_TALL_GRASS, MEADOW_WILDFLOWERS);
    }

    /** Nebelmoor: wie der Sumpf (ohne Sumpfeichen und Zuckerrohr), dazu Mooraugen, tote Bäume, Moorbirken, Schilf, Mondblumen. */
    static BiomeGenerationSettings mistMoor(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        BiomeDefaultFeatures.addFossilDecoration(builder);
        global(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addSwampClayDisk(builder);
        builder.addFeature(VEGETATION, VegetationPlacements.FLOWER_SWAMP);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_GRASS_NORMAL);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_DEAD_BUSH);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_WATERLILY);
        builder.addFeature(VEGETATION, VegetationPlacements.BROWN_MUSHROOM_SWAMP);
        builder.addFeature(VEGETATION, VegetationPlacements.RED_MUSHROOM_SWAMP);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_FIREFLY_BUSH_SWAMP);
        builder.addFeature(VEGETATION, VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP);
        builder.addFeature(VEGETATION, AquaticPlacements.SEAGRASS_SWAMP);
        return finish(builder, MOOR_POOLS, MOOR_BOULDERS, MOOR_DEAD_TREES, MOOR_BIRCHES, MOOR_PALE_OAKS, MOOR_REEDS, MOOR_FERNS, MOOR_MOONFLOWERS);
    }
}
