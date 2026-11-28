package io.github.sirschrolli.kingdomsatwar.worldgen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes;
import io.github.sirschrolli.kingdomsatwar.fae.FaeBiomes;
import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Die Feldfrüchte der Welten wachsen auch wild (seit 2.0): Reis im seichten Wasser der Sümpfe und Flüsse, Glutschoten auf der
 * Seelenerde der Unterwelt, Schwebebeeren auf den äußeren Inseln des End, Schattenknollen im Schattenwald und auf den
 * Glimmerwiesen, Sternfrüchte auf dem Leerenmoos, Chaosbohnen auf den Splittern des Abgrunds, Meertrauben am Grund des Urmeers
 * und Feenbeeren auf den Wiesen des Feenreichs – jeweils ein Grüppchen, halb- oder ganz reif. Wer eine Pflanze findet, hat
 * ihr Saatgut: Man pflanzt sie mit der Frucht selbst.
 *
 * <p>Wie die wilden Zierblumen kommen sie über Biom-Modifikatoren in ihre Biome (auch in die der eigenen Dimensionen).
 */
public final class WildCrops {
    /** Wo das Grüppchen wächst. */
    public enum Ground {
        /** auf dem Boden unter freiem Himmel */
        LAND,
        /** auf den Böden der Unterwelt (jede Ebene zwischen Lavasee und Decke) */
        NETHER,
        /** am Meeresgrund, unter Wasser */
        SEABED,
        /** im seichten Wasser (ein Block tief, darüber Luft) */
        SHALLOWS
    }

    /** Ein wildes Grüppchen: im Schnitt in jedem {@code rarity}-ten Chunk {@code tries} Versuche im Umkreis von {@code spread}. */
    public record Patch(String name, Supplier<? extends Block> plant, IntegerProperty age, int minAge, int maxAge, int rarity, int tries, int spread,
            Ground ground, List<ResourceKey<Biome>> biomes) {
        public ResourceKey<Feature> featureKey() {
            return ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("wild_" + this.name));
        }

        public ResourceKey<PlacedFeature> placedKey() {
            return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("wild_" + this.name));
        }

        public ResourceKey<BiomeModifier> modifierKey() {
            return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, KingdomsAtWar.id("add_wild_" + this.name));
        }
    }

    private static final IntegerProperty BUSH_AGE = io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock.AGE;

    /** Alle wilden Grüppchen (feste Reihenfolge). */
    public static final List<Patch> PATCHES = List.of(
            new Patch("rice", Cuisine.RICE_PLANT, net.minecraft.world.level.block.CropBlock.AGE, 5, 7, 4, 32, 5, Ground.SHALLOWS,
                    List.of(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.RIVER, io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes.MIST_MOOR)),
            new Patch("ember_peppers", Cuisine.EMBER_PEPPERS, net.minecraft.world.level.block.NetherWartBlock.AGE, 2, 3, 3, 20, 4, Ground.NETHER,
                    List.of(Biomes.SOUL_SAND_VALLEY)),
            new Patch("float_berries", Cuisine.FLOAT_BERRY_BUSH, BUSH_AGE, 2, 3, 6, 14, 4, Ground.LAND,
                    List.of(Biomes.END_HIGHLANDS, Biomes.END_MIDLANDS)),
            new Patch("shadow_tubers", Cuisine.SHADOW_TUBERS, net.minecraft.world.level.block.CropBlock.AGE, 5, 7, 5, 18, 4, Ground.LAND,
                    List.of(ShadowBiomes.SHADOW_FOREST, ShadowBiomes.GLIMMER_MEADOWS, ShadowBiomes.SOUL_MARSH)),
            new Patch("starfruit", Cuisine.STARFRUIT_BUSH, BUSH_AGE, 2, 3, 5, 12, 4, Ground.LAND,
                    List.of(VoidBiomes.CRYSTAL_FOREST, VoidBiomes.STARDUST_FIELDS, VoidBiomes.EXPANSE, VoidBiomes.WEIGHTLESS_RUINS)),
            new Patch("chaos_beans", Cuisine.CHAOS_BEAN_BUSH, BUSH_AGE, 2, 3, 4, 12, 4, Ground.LAND,
                    List.of(AbyssBiomes.FRACTURED_LANDS, AbyssBiomes.EMBER_FALLS, AbyssBiomes.STAR_WRECK, AbyssBiomes.SHADOW_CHASM, AbyssBiomes.THE_SEAM)),
            new Patch("sea_grapes", Cuisine.SEA_GRAPE_PLANT, io.github.sirschrolli.kingdomsatwar.block.crop.SeaGrapeBlock.AGE, 1, 3, 2, 24, 6, Ground.SEABED,
                    List.of(UrmeerBiomes.REEF, UrmeerBiomes.KELP_FOREST, UrmeerBiomes.CORAL_GARDEN, UrmeerBiomes.PEARL_GROUNDS, UrmeerBiomes.ISLES,
                            UrmeerBiomes.DEEP)),
            new Patch("fae_berries", Cuisine.FAE_BERRY_BUSH, BUSH_AGE, 2, 3, 5, 14, 4, Ground.LAND,
                    List.of(FaeBiomes.FAE_MEADOWS, FaeBiomes.GLIMMERWOOD, FaeBiomes.THORNWILD)));

    private WildCrops() {}

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        for (Patch patch : PATCHES) {
            context.register(patch.featureKey(), new SimpleBlockFeature(new RandomizedIntStateProvider(
                    BlockStateProvider.of(patch.plant().get()), patch.age(), UniformInt.of(patch.minAge(), patch.maxAge()))));
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
        for (Patch patch : PATCHES) {
            context.register(patch.placedKey(), new PlacedFeature(features.getOrThrow(patch.featureKey()), placement(patch)));
        }
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        for (Patch patch : PATCHES) {
            context.register(patch.modifierKey(), new BiomeModifiers.AddFeaturesBiomeModifier(
                    HolderSet.direct(patch.biomes().stream().map(biomes::getOrThrow).toList()),
                    HolderSet.direct(placed.getOrThrow(patch.placedKey())),
                    GenerationStep.Decoration.VEGETAL_DECORATION));
        }
    }

    private static List<PlacementModifier> placement(Patch patch) {
        Block plant = patch.plant().get();
        List<PlacementModifier> list = new ArrayList<>();
        list.add(RarityFilter.onAverageOnceEvery(patch.rarity()));
        switch (patch.ground()) {
            case NETHER -> list.add(CountOnEveryLayerPlacement.of(1));
            case LAND -> {
                list.add(InSquarePlacement.spread());
                list.add(HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING));
            }
            case SEABED, SHALLOWS -> {
                list.add(InSquarePlacement.spread());
                list.add(HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR_WG));
            }
        }
        list.add(BiomeFilter.biome());
        list.add(CountPlacement.of(patch.tries()));
        list.add(OffsetPlacement.ofTriangle(patch.spread(), patch.ground() == Ground.SHALLOWS ? 1 : 2));
        BlockPredicate where = switch (patch.ground()) {
            case LAND, NETHER -> BlockPredicate.ONLY_IN_AIR_PREDICATE;
            case SEABED -> BlockPredicate.matchesFluids(Fluids.WATER);
            case SHALLOWS -> BlockPredicate.allOf(BlockPredicate.matchesFluids(Fluids.WATER),
                    BlockPredicate.matchesBlocks(new Vec3i(0, 1, 0), List.of(Blocks.AIR)));
        };
        list.add(BlockPredicateFilter.forPredicate(BlockPredicate.allOf(where, BlockPredicate.wouldSurvive(plant))));
        return List.copyOf(list);
    }
}
