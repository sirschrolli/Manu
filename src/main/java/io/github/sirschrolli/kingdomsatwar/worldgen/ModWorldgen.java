package io.github.sirschrolli.kingdomsatwar.worldgen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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
 * Weltgenerierung: neue Erze, wilde Zierblumen, Drachennester und Burgruinen.
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

    /** Herz- oder Manakristalle auf Höhlenböden: im Schnitt in jedem {@code rarity}-ten Chunk ein Versuch. */
    public record CrystalVein(String name, Supplier<? extends Block> crystal, int rarity, int minY, int maxY) {
        public ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id(name));
        }

        public ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id(name));
        }

        public ResourceKey<BiomeModifier> modifierKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_" + name));
        }
    }

    /** Herzkristalle wachsen in Höhlen jeder Tiefe, Manakristalle eher tief unten. */
    public static final List<CrystalVein> CRYSTAL_VEINS = List.of(
            new CrystalVein("heart_crystals", ModBlocks.HEART_CRYSTAL_FORMATION, 3, -56, 48),
            new CrystalVein("mana_crystals", ModBlocks.MANA_CRYSTAL_FORMATION, 3, -60, 24));

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

    /**
     * Feueropal (seit 1.1.6): kleine Knollen im Netherrack und im Schwarzstein, überall im Nether – am häufigsten um die Höhe
     * des Lavameers (wie Gold im Nether, aber seltener: etwa vier Adern je Chunk zu höchstens fünf Knollen).
     */
    public static final ResourceKey<Feature> ORE_FIRE_OPAL = ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("ore_fire_opal"));
    public static final ResourceKey<PlacedFeature> ORE_FIRE_OPAL_PLACED = ResourceKey.create(Registries.PLACED_FEATURE,
            KingdomsAtWar.id("ore_fire_opal"));
    public static final ResourceKey<BiomeModifier> ADD_ORE_FIRE_OPAL = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
            KingdomsAtWar.id("add_ore_fire_opal"));
    public static final int FIRE_OPAL_VEINS = 4;
    public static final int FIRE_OPAL_SIZE = 5;

    /** Wilde Zierblumen (Bild "Nahrung & Pflanzen", Abschnitt 5). */
    public static final List<FlowerPatch> FLOWER_PATCHES = List.of(
            new FlowerPatch("lavender", ModBlocks.LAVENDER, 6, Tags.Biomes.IS_PLAINS),
            new FlowerPatch("garden_rose", ModBlocks.GARDEN_ROSE, 8, BiomeTags.IS_FOREST),
            new FlowerPatch("fire_lily", ModBlocks.FIRE_LILY, 8, BiomeTags.IS_SAVANNA),
            new FlowerPatch("frost_flower", ModBlocks.FROST_FLOWER, 6, Tags.Biomes.IS_SNOWY),
            new FlowerPatch("moonflower", ModBlocks.MOONFLOWER, 5, ModTags.Biomes.HAS_WILL_O_WISPS));

    /**
     * Kleine Bauwerke (Burgruinen); die Drachennester sind eigene, riesige Bauwerke (siehe worldgen/lair).
     * Je Feld von {@code spacing}×{@code spacing} Chunks höchstens eins, mindestens {@code separation} Chunks
     * auseinander und weit weg von den Drachennestern.
     */
    public record Ruin(String name, Feature feature, int spacing, int separation, int salt, TagKey<Biome> biomes) {
        public ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id(name));
        }

        public ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id(name));
        }

        public ResourceKey<BiomeModifier> modifierKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_" + name));
        }
    }

    /** Alle Bauwerke (feste Reihenfolge). */
    public static final List<Ruin> STRUCTURES = structures();

    private static List<Ruin> structures() {
        List<Ruin> list = new ArrayList<>();
        // Drachennester sind jetzt riesige, einzigartige Bauwerke (worldgen/lair)
        // seit 0.79 seltener und weiter auseinander (wie alle Bauwerke): Felder von 58 Chunks, mindestens 26 dazwischen
        list.add(new Ruin("castle_ruin", new CastleRuinFeature(), 58, 26, 704_311_219, ModTags.Biomes.HAS_CASTLE_RUINS));
        return List.copyOf(list);
    }

    /** So weit bleiben kleine Bauwerke (und später Dörfer) von den Drachennestern weg. */
    public static final int NEST_CLEARANCE = 320;

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

        context.register(ORE_FIRE_OPAL, new OreFeature(List.of(
                BlockReplacement.replace(new net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest(
                        net.minecraft.world.level.block.Blocks.NETHERRACK), io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL_ORE.get().defaultBlockState()),
                BlockReplacement.replace(new net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest(
                        net.minecraft.world.level.block.Blocks.BLACKSTONE), io.github.sirschrolli.kingdomsatwar.registry.FlameGear.BLACKSTONE_FIRE_OPAL_ORE.get().defaultBlockState())),
                FIRE_OPAL_SIZE, 0.2f));

        for (FlowerPatch patch : FLOWER_PATCHES) {
            context.register(patch.featureKey(), new SimpleBlockFeature(BlockStateProvider.of(patch.flower().get())));
        }

        for (CrystalVein vein : CRYSTAL_VEINS) {
            context.register(vein.featureKey(), new CrystalFormationFeature(vein.crystal().get().defaultBlockState(), 14));
        }

        for (Ruin ruin : STRUCTURES) {
            context.register(ruin.featureKey(), ruin.feature());
        }
        io.github.sirschrolli.kingdomsatwar.shadow.ShadowFeatures.bootstrapFeatures(context);
        io.github.sirschrolli.kingdomsatwar.voidrealm.VoidFeatures.bootstrapFeatures(context);
        io.github.sirschrolli.kingdomsatwar.abyss.AbyssFeatures.bootstrapFeatures(context);
        io.github.sirschrolli.kingdomsatwar.fae.FaeFeatures.bootstrapFeatures(context);
        io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerFeatures.bootstrapFeatures(context);
        io.github.sirschrolli.kingdomsatwar.worldgen.tree.ModTrees.bootstrapModFeatures(context);
        io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldFeatures.bootstrapFeatures(context);
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

        context.register(ORE_FIRE_OPAL_PLACED, new PlacedFeature(features.getOrThrow(ORE_FIRE_OPAL), List.of(
                CountPlacement.of(FIRE_OPAL_VEINS),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(112)),
                BiomeFilter.biome())));

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

        for (CrystalVein vein : CRYSTAL_VEINS) {
            List<PlacementModifier> placement = List.of(
                    RarityFilter.onAverageOnceEvery(vein.rarity()),
                    InSquarePlacement.spread(),
                    HeightRangePlacement.uniform(VerticalAnchor.absolute(vein.minY()), VerticalAnchor.absolute(vein.maxY())),
                    BiomeFilter.biome());
            context.register(vein.placedKey(), new PlacedFeature(features.getOrThrow(vein.featureKey()), placement));
        }

        for (Ruin ruin : STRUCTURES) {
            List<PlacementModifier> placement = List.of(
                    new SpacedPlacement(ruin.spacing(), ruin.separation(), ruin.salt(), NEST_CLEARANCE),
                    InSquarePlacement.spread(),
                    HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                    BiomeFilter.biome(),
                    io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageClearing.Filter.INSTANCE);
            context.register(ruin.placedKey(), new PlacedFeature(features.getOrThrow(ruin.featureKey()), placement));
        }
        io.github.sirschrolli.kingdomsatwar.shadow.ShadowFeatures.bootstrapPlacedFeatures(context);
        io.github.sirschrolli.kingdomsatwar.voidrealm.VoidFeatures.bootstrapPlacedFeatures(context);
        io.github.sirschrolli.kingdomsatwar.abyss.AbyssFeatures.bootstrapPlacedFeatures(context);
        io.github.sirschrolli.kingdomsatwar.fae.FaeFeatures.bootstrapPlacedFeatures(context);
        io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerFeatures.bootstrapPlacedFeatures(context);
        io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldFeatures.bootstrapPlacedFeatures(context);
        io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageTrees.bootstrapPlacedFeatures(context);
    }

    /** Biom-Modifikator für das Erscheinen der Banditen. */
    public static final ResourceKey<BiomeModifier> STANDARD_BIOMES =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("standard_biomes"));
    public static final ResourceKey<BiomeModifier> BANDIT_SPAWNS =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_bandit_spawns"));
    /** Eigene Hof- und Weidetiere statt der Vanilla-Tiere. */
    public static final ResourceKey<BiomeModifier> REMOVE_VANILLA_ANIMALS = spawnModifier("remove_vanilla_farm_animals");
    public static final ResourceKey<BiomeModifier> FARM_ANIMAL_SPAWNS = spawnModifier("add_farm_animal_spawns");
    public static final ResourceKey<BiomeModifier> FEW_FARM_ANIMAL_SPAWNS = spawnModifier("add_few_farm_animal_spawns");
    public static final ResourceKey<BiomeModifier> MUSHROOM_CATTLE_SPAWNS = spawnModifier("add_mushroom_cattle_spawns");
    public static final ResourceKey<BiomeModifier> HORSE_SPAWNS = spawnModifier("add_horse_spawns");
    public static final ResourceKey<BiomeModifier> FEW_HORSE_SPAWNS = spawnModifier("add_few_horse_spawns");
    public static final ResourceKey<BiomeModifier> DONKEY_SPAWNS = spawnModifier("add_donkey_spawns");
    public static final ResourceKey<BiomeModifier> GOAT_SPAWNS = spawnModifier("add_goat_spawns");
    public static final ResourceKey<BiomeModifier> FEW_GOAT_SPAWNS = spawnModifier("add_few_goat_spawns");
    public static final ResourceKey<BiomeModifier> MANY_HARE_SPAWNS = spawnModifier("add_many_hare_spawns");
    public static final ResourceKey<BiomeModifier> HARE_SPAWNS = spawnModifier("add_hare_spawns");
    public static final ResourceKey<BiomeModifier> FEW_HARE_SPAWNS = spawnModifier("add_few_hare_spawns");
    public static final ResourceKey<BiomeModifier> WILD_BOAR_SPAWNS = spawnModifier("add_wild_boar_spawns");
    public static final ResourceKey<BiomeModifier> DEER_SPAWNS = spawnModifier("add_deer_spawns");
    public static final ResourceKey<BiomeModifier> SNOW_LEOPARD_SPAWNS = spawnModifier("add_snow_leopard_spawns");
    public static final ResourceKey<BiomeModifier> PRIMEVAL_ELK_SPAWNS = spawnModifier("add_primeval_elk_spawns");
    public static final ResourceKey<BiomeModifier> STORM_GULL_SPAWNS = spawnModifier("add_storm_gull_spawns");
    public static final ResourceKey<BiomeModifier> GRIFFIN_SPAWNS = spawnModifier("add_griffin_spawns");
    public static final ResourceKey<BiomeModifier> EMBER_SALAMANDER_SPAWNS = spawnModifier("add_ember_salamander_spawns");
    public static final ResourceKey<BiomeModifier> MIRE_SNAPPER_SPAWNS = spawnModifier("add_mire_snapper_spawns");
    public static final ResourceKey<BiomeModifier> FLAMINGO_SPAWNS = spawnModifier("add_flamingo_spawns");
    public static final ResourceKey<BiomeModifier> BUTTERFLY_SPAWNS = spawnModifier("add_butterfly_spawns");
    public static final ResourceKey<BiomeModifier> GOOSE_SPAWNS = spawnModifier("add_goose_spawns");
    public static final ResourceKey<BiomeModifier> WILL_O_WISP_SPAWNS = spawnModifier("add_will_o_wisp_spawns");
    public static final ResourceKey<BiomeModifier> CAVE_TROLL_SPAWNS = spawnModifier("add_cave_troll_spawns");
    public static final ResourceKey<BiomeModifier> LINDWORM_SPAWNS = spawnModifier("add_lindworm_spawns");
    public static final ResourceKey<BiomeModifier> WEREWOLF_SPAWNS = spawnModifier("add_werewolf_spawns");
    public static final ResourceKey<BiomeModifier> MIRE_SHAMBLER_SPAWNS = spawnModifier("add_mire_shambler_spawns");
    public static final ResourceKey<BiomeModifier> EMBER_WYRM_SPAWNS = spawnModifier("add_ember_wyrm_spawns");
    public static final ResourceKey<BiomeModifier> ASH_WRAITH_SPAWNS = spawnModifier("add_ash_wraith_spawns");
    public static final ResourceKey<BiomeModifier> RIFT_HOUND_SPAWNS = spawnModifier("add_rift_hound_spawns");
    public static final ResourceKey<BiomeModifier> CHORUS_MAW_SPAWNS = spawnModifier("add_chorus_maw_spawns");
    // eigene Wildtiere (E3b): Tausch gegen die Vanilla-Wildtiere, dazu neue Arten in neuen Gegenden
    public static final ResourceKey<BiomeModifier> SWAP_WILD_ANIMALS = spawnModifier("swap_vanilla_wild_animals");
    public static final ResourceKey<BiomeModifier> SWAP_MONSTERS = spawnModifier("swap_vanilla_monsters");
    public static final ResourceKey<BiomeModifier> SWAP_WATER_CREATURES = spawnModifier("swap_vanilla_water_creatures");
    public static final ResourceKey<BiomeModifier> SWAP_NETHER_CREATURES = spawnModifier("swap_vanilla_nether_creatures");
    public static final ResourceKey<BiomeModifier> SWAP_END_CREATURES = spawnModifier("swap_vanilla_end_creatures");
    public static final ResourceKey<BiomeModifier> FOREST_BEAR_SPAWNS = spawnModifier("add_forest_bear_spawns");
    public static final ResourceKey<BiomeModifier> FEW_FOREST_BEAR_SPAWNS = spawnModifier("add_few_forest_bear_spawns");
    public static final ResourceKey<BiomeModifier> FENNEC_SPAWNS = spawnModifier("add_fennec_spawns");
    public static final ResourceKey<BiomeModifier> LYNX_SPAWNS = spawnModifier("add_lynx_spawns");
    public static final ResourceKey<BiomeModifier> FOREST_WILDCAT_SPAWNS = spawnModifier("add_forest_wildcat_spawns");
    public static final ResourceKey<BiomeModifier> BACTRIAN_CAMEL_SPAWNS = spawnModifier("add_bactrian_camel_spawns");
    // Wesen des Meeres
    public static final ResourceKey<BiomeModifier> MANTA_RAY_SPAWNS = spawnModifier("add_manta_ray_spawns");
    public static final ResourceKey<BiomeModifier> GLOW_JELLYFISH_SPAWNS = spawnModifier("add_glow_jellyfish_spawns");
    public static final ResourceKey<BiomeModifier> SHARK_SPAWNS = spawnModifier("add_shark_spawns");
    public static final ResourceKey<BiomeModifier> ANGLERFISH_SPAWNS = spawnModifier("add_anglerfish_spawns");
    public static final ResourceKey<BiomeModifier> GIANT_CRAB_SPAWNS = spawnModifier("add_giant_crab_spawns");

    private static ResourceKey<BiomeModifier> spawnModifier(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id(name));
    }

    private static BiomeModifier spawns(HolderGetter<Biome> biomes, TagKey<Biome> where, EntityType<?> type, int weight, int min, int max) {
        return new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(where),
                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(type, UniformInt.of(min, max)), weight)
                        .build());
    }

    @SafeVarargs
    private static BiomeModifier spawnsIn(HolderGetter<Biome> biomes, EntityType<?> type, int weight, int min, int max,
            net.minecraft.world.entity.MobCategory category, ResourceKey<Biome>... where) {
        return new AddCategorySpawnsBiomeModifier(HolderSet.direct(java.util.Arrays.stream(where).map(biomes::getOrThrow).toList()),
                category, WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(type, UniformInt.of(min, max)), weight)
                        .build());
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        // die Standard-Biome im Stil der eigenen (Felsen, Stämme, Farne, Oasen, Schneewehen …)
        context.register(STANDARD_BIOMES, io.github.sirschrolli.kingdomsatwar.worldgen.overworld.StandardBiomes.INSTANCE);

        for (OreVein vein : ORE_VEINS) {
            context.register(vein.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(vein.biomes()),
                    HolderSet.direct(placed.getOrThrow(vein.placedKey())),
                    GenerationStep.Decoration.UNDERGROUND_ORES));
        }

        context.register(ADD_ORE_FIRE_OPAL, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placed.getOrThrow(ORE_FIRE_OPAL_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        for (CrystalVein vein : CRYSTAL_VEINS) {
            context.register(vein.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                    HolderSet.direct(placed.getOrThrow(vein.placedKey())),
                    GenerationStep.Decoration.UNDERGROUND_DECORATION));
        }

        for (FlowerPatch patch : FLOWER_PATCHES) {
            context.register(patch.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(patch.biomes()),
                    HolderSet.direct(placed.getOrThrow(patch.placedKey())),
                    GenerationStep.Decoration.VEGETAL_DECORATION));
        }

        for (Ruin ruin : STRUCTURES) {
            context.register(ruin.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(ruin.biomes()),
                    HolderSet.direct(placed.getOrThrow(ruin.placedKey())),
                    GenerationStep.Decoration.SURFACE_STRUCTURES));
        }

        // Banditen: nachts unter freiem Himmel, in kleinen Gruppen
        context.register(BANDIT_SPAWNS, new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_BANDITS),
                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.BANDIT.get(), UniformInt.of(2, 3)), 8)
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.BANDIT_ARCHER.get(), UniformInt.of(1, 2)), 5)
                        .build()));
        // Eigene Hof- und Weidetiere: die Vanilla-Tiere verschwinden aus den Spawn-Listen, die eigenen kommen mit den
        // gleichen Gewichten hinzu (Kühe 8, Schweine 10, Schafe 12, Hühner 10 …)
        HolderGetter<EntityType<?>> types = context.lookup(Registries.ENTITY_TYPE);
        context.register(REMOVE_VANILLA_ANIMALS, new BiomeModifiers.RemoveSpawnsBiomeModifier(
                biomes.getOrThrow(ModTags.Biomes.REPLACES_VANILLA_ANIMALS),
                HolderSet.direct(java.util.stream.Stream.of(EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTypes.PIG, EntityTypes.SHEEP,
                                EntityTypes.CHICKEN, EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.MULE, EntityTypes.GOAT,
                                EntityTypes.RABBIT)
                        .map(type -> (Holder<EntityType<?>>) types.getOrThrow(BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).orElseThrow()))
                        .toList())));
        context.register(FARM_ANIMAL_SPAWNS, new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_CATTLE),
                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.CATTLE.get(), UniformInt.of(4, 4)), 8)
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.FARM_PIG.get(), UniformInt.of(4, 4)), 10)
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.FARM_SHEEP.get(), UniformInt.of(4, 4)), 12)
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.FARM_CHICKEN.get(), UniformInt.of(4, 4)), 10)
                        .build()));
        context.register(FEW_FARM_ANIMAL_SPAWNS, new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.HAS_FEW_SHEEP),
                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.FARM_SHEEP.get(), UniformInt.of(2, 4)), 2)
                        .add(new MobSpawnSettings.SpawnerData(ModEntities.FARM_PIG.get(), UniformInt.of(1, 2)), 1)
                        .build()));
        context.register(MUSHROOM_CATTLE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_MUSHROOM_CATTLE, ModEntities.CATTLE.get(), 8, 4, 8));
        context.register(HORSE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_HORSES, ModEntities.FARM_HORSE.get(), 5, 2, 6));
        context.register(FEW_HORSE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_FEW_HORSES, ModEntities.FARM_HORSE.get(), 1, 2, 6));
        context.register(DONKEY_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_DONKEYS, ModEntities.FARM_DONKEY.get(), 1, 1, 3));
        context.register(GOAT_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_GOATS, ModEntities.FARM_GOAT.get(), 5, 1, 3));
        context.register(FEW_GOAT_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_FEW_GOATS, ModEntities.FARM_GOAT.get(), 2, 1, 3));
        context.register(MANY_HARE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_MANY_HARES, ModEntities.HARE.get(), 10, 2, 3));
        context.register(HARE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_HARES, ModEntities.HARE.get(), 4, 2, 3));
        context.register(FEW_HARE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_FEW_HARES, ModEntities.HARE.get(), 2, 2, 4));

        // Wilde Kreaturen
        context.register(WILD_BOAR_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_WILD_BOARS, ModEntities.WILD_BOAR.get(), 8, 2, 4));
        context.register(DEER_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_DEER, ModEntities.DEER.get(), 8, 2, 4));
        // Tiere der eigenen Biome (0.85): Schneeleoparden selten, allein oder zu zweit
        context.register(SNOW_LEOPARD_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_SNOW_LEOPARDS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.SNOW_LEOPARD.get(), 4, 1, 2));
        // Urelche: kleine Herden aus Kühen, Kälbern und einem Bullen
        context.register(PRIMEVAL_ELK_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_PRIMEVAL_ELKS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.PRIMEVAL_ELK.get(), 5, 2, 4));
        // Sturmmöwen: Schwärme von drei bis sechs
        context.register(STORM_GULL_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_STORM_GULLS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.STORM_GULL.get(), 10, 3, 6));
        context.register(GRIFFIN_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_GRIFFINS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.GRIFFIN.get(), 3, 1, 2));
        context.register(EMBER_SALAMANDER_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_EMBER_SALAMANDERS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.EMBER_SALAMANDER.get(), 8, 1, 3));
        context.register(MIRE_SNAPPER_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_MIRE_SNAPPERS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.MIRE_SNAPPER.get(), 4, 1, 1));
        context.register(FLAMINGO_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_FLAMINGOS,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.FLAMINGO.get(), 10, 4, 8));
        context.register(BUTTERFLY_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_BUTTERFLIES,
                io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts.BUTTERFLY.get(), 20, 2, 4));
        context.register(GOOSE_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_GEESE, ModEntities.GOOSE.get(), 7, 3, 5));
        context.register(WILL_O_WISP_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_WILL_O_WISPS, ModEntities.WILL_O_WISP.get(), 12, 1, 3));
        context.register(CAVE_TROLL_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_CAVE_TROLLS, ModEntities.CAVE_TROLL.get(), 6, 1, 1));
        context.register(LINDWORM_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_LINDWORMS, ModEntities.LINDWORM.get(), 3, 1, 1));
        // Neue Monster (0.69)
        context.register(WEREWOLF_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_WEREWOLVES, ModEntities.WEREWOLF.get(), 10, 1, 1));
        context.register(MIRE_SHAMBLER_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_MIRE_SHAMBLERS, ModEntities.MIRE_SHAMBLER.get(), 14, 1, 1));
        context.register(EMBER_WYRM_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_EMBER_WYRMS, ModEntities.EMBER_WYRM.get(), 6, 1, 1));
        context.register(ASH_WRAITH_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_ASH_WRAITHS, ModEntities.ASH_WRAITH.get(), 14, 1, 2));
        // Monster des Ends (0.71)
        context.register(RIFT_HOUND_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_RIFT_HOUNDS, ModEntities.RIFT_HOUND.get(), 6, 2, 2));
        context.register(CHORUS_MAW_SPAWNS, spawns(biomes, ModTags.Biomes.HAS_CHORUS_MAWS, ModEntities.CHORUS_MAW.get(), 4, 1, 1));

        // Eigene Wildtiere: überall, wo Vanilla seine Wildtiere vorsieht, erscheinen mit gleichem Gewicht die eigenen
        context.register(SWAP_WILD_ANIMALS, new ReplaceSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.REPLACES_VANILLA_ANIMALS),
                java.util.Map.ofEntries(
                        java.util.Map.entry(EntityTypes.WOLF, ModEntities.WOLF.get()),
                        java.util.Map.entry(EntityTypes.FOX, ModEntities.FOX.get()),
                        java.util.Map.entry(EntityTypes.POLAR_BEAR, ModEntities.BEAR.get()),
                        java.util.Map.entry(EntityTypes.PANDA, ModEntities.PANDA.get()),
                        java.util.Map.entry(EntityTypes.CAT, ModEntities.CAT.get()),
                        java.util.Map.entry(EntityTypes.OCELOT, ModEntities.WILDCAT.get()),
                        java.util.Map.entry(EntityTypes.PARROT, ModEntities.PARROT.get()),
                        java.util.Map.entry(EntityTypes.BEE, ModEntities.BEE.get()),
                        java.util.Map.entry(EntityTypes.BAT, ModEntities.BAT.get()),
                        java.util.Map.entry(EntityTypes.ARMADILLO, ModEntities.ARMADILLO.get()),
                        java.util.Map.entry(EntityTypes.CAMEL, ModEntities.CAMEL.get()),
                        java.util.Map.entry(EntityTypes.LLAMA, ModEntities.LLAMA.get()),
                        java.util.Map.entry(EntityTypes.TRADER_LLAMA, ModEntities.TRADER_LLAMA.get()))));
        // Eigene Monster (E4a): überall, wo Vanilla seine Monster vorsieht, erscheinen mit gleichem Gewicht die eigenen
        context.register(SWAP_MONSTERS, new ReplaceSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.REPLACES_VANILLA_ANIMALS),
                java.util.Map.ofEntries(
                        java.util.Map.entry(EntityTypes.ZOMBIE, ModEntities.ZOMBIE.get()),
                        java.util.Map.entry(EntityTypes.HUSK, ModEntities.HUSK.get()),
                        java.util.Map.entry(EntityTypes.DROWNED, ModEntities.DROWNED.get()),
                        java.util.Map.entry(EntityTypes.ZOMBIE_VILLAGER, ModEntities.ZOMBIE_VILLAGER.get()),
                        java.util.Map.entry(EntityTypes.SKELETON, ModEntities.SKELETON.get()),
                        java.util.Map.entry(EntityTypes.STRAY, ModEntities.STRAY.get()),
                        java.util.Map.entry(EntityTypes.BOGGED, ModEntities.BOGGED.get()),
                        java.util.Map.entry(EntityTypes.PARCHED, ModEntities.PARCHED.get()),
                        java.util.Map.entry(EntityTypes.CREEPER, ModEntities.CREEPER.get()),
                        java.util.Map.entry(EntityTypes.SPIDER, ModEntities.SPIDER.get()),
                        java.util.Map.entry(EntityTypes.CAVE_SPIDER, ModEntities.CAVE_SPIDER.get()),
                        java.util.Map.entry(EntityTypes.SLIME, ModEntities.SLIME.get()),
                        java.util.Map.entry(EntityTypes.SULFUR_CUBE, ModEntities.SULFUR_CUBE.get()),
                        java.util.Map.entry(EntityTypes.WITCH, ModEntities.WITCH.get()),
                        java.util.Map.entry(EntityTypes.PHANTOM, ModEntities.PHANTOM.get()),
                        java.util.Map.entry(EntityTypes.SILVERFISH, ModEntities.SILVERFISH.get()),
                        java.util.Map.entry(EntityTypes.ZOMBIE_HORSE, ModEntities.ZOMBIE_HORSE.get()),
                        java.util.Map.entry(EntityTypes.SKELETON_HORSE, ModEntities.SKELETON_HORSE.get()),
                        java.util.Map.entry(EntityTypes.CAMEL_HUSK, ModEntities.CAMEL_HUSK.get()),
                        java.util.Map.entry(EntityTypes.ENDERMAN, ModEntities.ENDERMAN.get()))));
        // Wassertiere (E4c): Fische, Tintenfische, Delfine, Schildkröten, Molche, Frösche und Nautilusse mit gleichem Gewicht
        context.register(SWAP_WATER_CREATURES, new ReplaceSpawnsBiomeModifier(biomes.getOrThrow(ModTags.Biomes.REPLACES_VANILLA_ANIMALS),
                java.util.Map.ofEntries(
                        java.util.Map.entry(EntityTypes.COD, ModEntities.COD.get()),
                        java.util.Map.entry(EntityTypes.SALMON, ModEntities.SALMON.get()),
                        java.util.Map.entry(EntityTypes.PUFFERFISH, ModEntities.PUFFERFISH.get()),
                        java.util.Map.entry(EntityTypes.TROPICAL_FISH, ModEntities.TROPICAL_FISH.get()),
                        java.util.Map.entry(EntityTypes.SQUID, ModEntities.SQUID.get()),
                        java.util.Map.entry(EntityTypes.GLOW_SQUID, ModEntities.GLOW_SQUID.get()),
                        java.util.Map.entry(EntityTypes.DOLPHIN, ModEntities.DOLPHIN.get()),
                        java.util.Map.entry(EntityTypes.TURTLE, ModEntities.TURTLE.get()),
                        java.util.Map.entry(EntityTypes.AXOLOTL, ModEntities.AXOLOTL.get()),
                        java.util.Map.entry(EntityTypes.FROG, ModEntities.FROG.get()),
                        java.util.Map.entry(EntityTypes.NAUTILUS, ModEntities.NAUTILUS.get()))));
        // Nether-Wesen (E5): im ganzen Nether mit gleichem Gewicht die eigenen
        context.register(SWAP_NETHER_CREATURES, new ReplaceSpawnsBiomeModifier(biomes.getOrThrow(net.minecraft.tags.BiomeTags.IS_NETHER),
                java.util.Map.ofEntries(
                        java.util.Map.entry(EntityTypes.PIGLIN, ModEntities.PIGLIN.get()),
                        java.util.Map.entry(EntityTypes.ZOMBIFIED_PIGLIN, ModEntities.ZOMBIFIED_PIGLIN.get()),
                        java.util.Map.entry(EntityTypes.HOGLIN, ModEntities.HOGLIN.get()),
                        java.util.Map.entry(EntityTypes.STRIDER, ModEntities.STRIDER.get()),
                        java.util.Map.entry(EntityTypes.GHAST, ModEntities.GHAST.get()),
                        java.util.Map.entry(EntityTypes.BLAZE, ModEntities.BLAZE.get()),
                        java.util.Map.entry(EntityTypes.MAGMA_CUBE, ModEntities.MAGMA_CUBE.get()),
                        java.util.Map.entry(EntityTypes.ENDERMAN, ModEntities.ENDERMAN.get()))));
        // Wesen des Ends (E5c): im ganzen End mit gleichem Gewicht die eigenen
        context.register(SWAP_END_CREATURES, new ReplaceSpawnsBiomeModifier(biomes.getOrThrow(net.minecraft.tags.BiomeTags.IS_END),
                java.util.Map.ofEntries(
                        java.util.Map.entry(EntityTypes.ENDERMAN, ModEntities.ENDERMAN.get()))));
        // Braun- und Schwarzbären in Taiga und alten Wäldern (selten, wie der Eisbär), vereinzelt in den übrigen Wäldern
        context.register(FOREST_BEAR_SPAWNS, spawnsIn(biomes, ModEntities.BEAR.get(), 2, 1, 2, net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.level.biome.Biomes.TAIGA, net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA,
                net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA, net.minecraft.world.level.biome.Biomes.WINDSWEPT_FOREST,
                io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.PRIMEVAL_FOREST));
        context.register(FEW_FOREST_BEAR_SPAWNS, spawnsIn(biomes, ModEntities.BEAR.get(), 1, 1, 1, net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.level.biome.Biomes.FOREST, net.minecraft.world.level.biome.Biomes.DARK_FOREST,
                net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST, net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA));
        // Wüstenfüchse in Wüste und Tafelbergen
        context.register(FENNEC_SPAWNS, spawnsIn(biomes, ModEntities.FOX.get(), 4, 1, 2, net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.level.biome.Biomes.DESERT, net.minecraft.world.level.biome.Biomes.BADLANDS,
                net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS));
        // Luchs und Waldkatze wie der Ozelot im Durchgang der Monster
        context.register(LYNX_SPAWNS, spawnsIn(biomes, ModEntities.WILDCAT.get(), 2, 1, 1, net.minecraft.world.entity.MobCategory.MONSTER,
                net.minecraft.world.level.biome.Biomes.TAIGA, net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA,
                net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA, net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                net.minecraft.world.level.biome.Biomes.GROVE));
        context.register(FOREST_WILDCAT_SPAWNS, spawnsIn(biomes, ModEntities.WILDCAT.get(), 2, 1, 1, net.minecraft.world.entity.MobCategory.MONSTER,
                net.minecraft.world.level.biome.Biomes.FOREST, net.minecraft.world.level.biome.Biomes.BIRCH_FOREST,
                net.minecraft.world.level.biome.Biomes.DARK_FOREST, net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST,
                net.minecraft.world.level.biome.Biomes.DAPPLED_FOREST,
                io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.PRIMEVAL_FOREST));
        // Trampeltiere in Tafelbergen, Felsschluchten und auf den Savannen-Hochebenen
        context.register(BACTRIAN_CAMEL_SPAWNS, spawnsIn(biomes, ModEntities.CAMEL.get(), 1, 1, 1, net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.level.biome.Biomes.BADLANDS, net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS,
                net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU,
                io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.ROCK_GORGES));
        // Wesen des Meeres: Rochen und Haie in warmen und tiefen Meeren, Quallen in Schwärmen fast überall, Anglerfische in
        // der dunklen Tiefe, Krabben an Stränden, Felsküsten und auf dem Grund flacher, warmer Meere
        var water = net.minecraft.world.entity.MobCategory.WATER_CREATURE;
        context.register(MANTA_RAY_SPAWNS, spawnsIn(biomes, ModEntities.MANTA_RAY.get(), 2, 1, 2, water,
                Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, Biomes.OCEAN));
        context.register(GLOW_JELLYFISH_SPAWNS, spawnsIn(biomes, ModEntities.GLOW_JELLYFISH.get(), 8, 3, 6,
                net.minecraft.world.entity.MobCategory.WATER_AMBIENT, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN,
                Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN));
        context.register(SHARK_SPAWNS, spawnsIn(biomes, ModEntities.SHARK.get(), 2, 1, 2, water,
                Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN));
        context.register(ANGLERFISH_SPAWNS, spawnsIn(biomes, ModEntities.ANGLERFISH.get(), 15, 1, 1, net.minecraft.world.entity.MobCategory.MONSTER,
                Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN));
        context.register(GIANT_CRAB_SPAWNS, spawnsIn(biomes, ModEntities.GIANT_CRAB.get(), 6, 2, 4, net.minecraft.world.entity.MobCategory.CREATURE,
                Biomes.BEACH, Biomes.STONY_SHORE, Biomes.MANGROVE_SWAMP, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN,
                io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.CORAL_COAST,
                io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.STORM_COAST));
        // Bäume, Kakteen und Felsbrocken der Dorf-Biome wachsen nicht mehr in die Dörfer hinein
        io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageTrees.bootstrapBiomeModifiers(context);
    }
}
