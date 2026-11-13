package io.github.sirschrolli.kingdomsatwar.urmeer;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Die Biome des Urmeers (seit 0.89):
 * <ul>
 *     <li><b>Urmeer-Weiten</b> – das offene, tiefe Meer: Wälder aus Riesentang, Seegraswiesen, Felsnadeln; Haie, Rochen,
 *     Delfine, Quallenschwärme;</li>
 *     <li><b>Korallenbänke</b> – flache, warme Riffe knapp unter der Oberfläche, voller Korallen, Anemonen und Muscheln;
 *     bunte Fische und Schildkröten;</li>
 *     <li><b>Tiefseegraben</b> – Schluchten im Grund aus Abgrundstein, dunkel, mit heißen Quellen und Leuchtfeldern;
 *     Anglerfische und Leuchtkraken;</li>
 *     <li><b>Sturminseln</b> – Felseninseln über dem Meer mit Stränden und Palmen, um sie Felsnadeln; Riesenkrabben am Strand.</li>
 * </ul>
 * Seit 0.92 vier weitere Landschaften unter Wasser, jede mit ihrem Meeresholz:
 * <ul>
 *     <li><b>Tangwald</b> – in den Weiten, wo es grünt: hohe Tangbäume, dazwischen Riesentang und Seegras;</li>
 *     <li><b>Korallengarten</b> – an den Flanken der Korallenbänke: Korallenbäume, Korallen und Anemonen;</li>
 *     <li><b>Perlmuttgründe</b> – helle Gründe aus Sand und Kalk: Perlbäume, Muschelbänke und Perlmuttfelsen;</li>
 *     <li><b>Leuchtende Tiefe</b> – die Hälfte der Gräben, in der alles leuchtet: Tiefseebäume, Leuchtfelder, heiße Schlote.</li>
 * </ul>
 */
public final class UrmeerBiomes {
    public static final ResourceKey<Biome> DEEP = key("urmeer_deep");
    public static final ResourceKey<Biome> REEF = key("urmeer_reef");
    public static final ResourceKey<Biome> TRENCH = key("urmeer_trench");
    public static final ResourceKey<Biome> ISLES = key("urmeer_isles");

    public static final ResourceKey<Biome> KELP_FOREST = key("urmeer_kelp_forest");
    public static final ResourceKey<Biome> CORAL_GARDEN = key("urmeer_coral_garden");
    public static final ResourceKey<Biome> PEARL_GROUNDS = key("urmeer_pearl_grounds");
    public static final ResourceKey<Biome> GLOW_DEPTHS = key("urmeer_glow_depths");

    public static final List<ResourceKey<Biome>> ALL = List.of(DEEP, REEF, TRENCH, ISLES, KELP_FOREST, CORAL_GARDEN, PEARL_GROUNDS, GLOW_DEPTHS);
    /** Die vier Landschaften unter Wasser (seit 0.92). */
    public static final List<ResourceKey<Biome>> UNDERWATER = List.of(KELP_FOREST, CORAL_GARDEN, PEARL_GROUNDS, GLOW_DEPTHS);

    /** Grenzen in der Pflanzen-Noise (der "Feuchte"): darüber Tangwald, darunter Perlmuttgründe; in den Gräben die Leuchtende Tiefe. */
    public static final float KELP_EDGE = 0.2f;
    public static final float PEARL_EDGE = -0.24f;
    public static final float GLOW_EDGE = 0.05f;
    /** So breit ist der Korallengarten am Rand der Korallenbänke (in der Riff-Noise). */
    public static final float GARDEN_WIDTH = 0.1f;

    private static final Climate.Parameter FULL = Climate.Parameter.span(-2.0f, 2.0f);

    private UrmeerBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    private static ResourceKey<PlacedFeature> own(String path) {
        return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("overworld/" + path));
    }

    /** Klimapunkte passend zum Gelände: Inseln (Kontinentalität), Gräben (Erosion), Riffe (Temperatur), sonst das weite Meer. */
    public static Climate.ParameterList<Holder<Biome>> parameters(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = new ArrayList<>();
        Climate.Parameter sea = Climate.Parameter.span(-2.0f, UrmeerTerrain.ISLAND_EDGE);
        Climate.Parameter flat = Climate.Parameter.span(UrmeerTerrain.TRENCH_EDGE, 2.0f);
        Climate.Parameter trench = Climate.Parameter.span(-2.0f, UrmeerTerrain.TRENCH_EDGE);
        Climate.Parameter open = Climate.Parameter.span(-2.0f, UrmeerTerrain.REEF_EDGE - GARDEN_WIDTH);
        add(list, biomes, ISLES, FULL, FULL, Climate.Parameter.span(UrmeerTerrain.ISLAND_EDGE, 2.0f), FULL);
        add(list, biomes, GLOW_DEPTHS, FULL, Climate.Parameter.span(GLOW_EDGE, 2.0f), sea, trench);
        add(list, biomes, TRENCH, FULL, Climate.Parameter.span(-2.0f, GLOW_EDGE), sea, trench);
        add(list, biomes, REEF, Climate.Parameter.span(UrmeerTerrain.REEF_EDGE, 2.0f), FULL, sea, flat);
        add(list, biomes, CORAL_GARDEN, Climate.Parameter.span(UrmeerTerrain.REEF_EDGE - GARDEN_WIDTH, UrmeerTerrain.REEF_EDGE), FULL, sea, flat);
        add(list, biomes, KELP_FOREST, open, Climate.Parameter.span(KELP_EDGE, 2.0f), sea, flat);
        add(list, biomes, PEARL_GROUNDS, open, Climate.Parameter.span(-2.0f, PEARL_EDGE), sea, flat);
        add(list, biomes, DEEP, open, Climate.Parameter.span(PEARL_EDGE, KELP_EDGE), sea, flat);
        return new Climate.ParameterList<>(list);
    }

    private static void add(List<Pair<Climate.ParameterPoint, Holder<Biome>>> list, HolderGetter<Biome> biomes, ResourceKey<Biome> biome,
            Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion) {
        list.add(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, FULL, FULL, 0.0f), biomes.getOrThrow(biome)));
    }

    private static ResourceKey<PlacedFeature> tree(String wood) {
        return ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("overworld/urmeer/" + wood + "_tree"));
    }

    // =====================================================================================
    // Die Biome
    // =====================================================================================

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldCarver> carvers = context.lookup(Registries.CARVER);
        context.register(DEEP, biome(0x4A8EC8, 0x9CCAD8, 0x1E78A8, 0x0E3A5C, features(placed, carvers,
                        own("standard/ocean/kelp_forests"), own("standard/ocean/cold_kelp_forests"), own("standard/ocean/sea_stacks"),
                        AquaticPlacements.SEAGRASS_DEEP, AquaticPlacements.KELP_COLD),
                spawns().add(ModEntities.SHARK.get(), 3, 1, 2).add(ModEntities.MANTA_RAY.get(), 3, 1, 2).add(ModEntities.DOLPHIN.get(), 2, 2, 4)
                        .add(ModEntities.SQUID.get(), 4, 2, 4).add(ModEntities.GLOW_JELLYFISH.get(), 10, 3, 6)
                        .add(ModEntities.COD.get(), 10, 3, 6).add(ModEntities.SALMON.get(), 6, 2, 5).build()));
        context.register(REEF, biome(0x56A8D8, 0xA8DCE4, 0x2EA6C8, 0x145A78, features(placed, carvers,
                        own("coral_coast/reefs"), own("coral_coast/coral_gardens"), own("standard/ocean/anemone_gardens"),
                        own("standard/ocean/shell_beds"), own("standard/ocean/warm_sea_stacks"),
                        AquaticPlacements.WARM_OCEAN_VEGETATION, AquaticPlacements.SEAGRASS_WARM, AquaticPlacements.SEA_PICKLE),
                spawns().add(ModEntities.TROPICAL_FISH.get(), 25, 8, 8).add(ModEntities.PUFFERFISH.get(), 10, 1, 3)
                        .add(ModEntities.TURTLE.get(), 3, 1, 2).add(ModEntities.MANTA_RAY.get(), 2, 1, 1)
                        .add(ModEntities.GLOW_JELLYFISH.get(), 6, 2, 4).add(ModEntities.GIANT_CRAB.get(), 3, 1, 2).build()));
        context.register(TRENCH, biome(0x3A70A8, 0x6A9AB8, 0x14507A, 0x061A30, features(placed, carvers,
                        own("standard/ocean/glow_fields"), own("standard/ocean/vent_fields"), AquaticPlacements.SEAGRASS_DEEP_COLD),
                spawns().add(ModEntities.ANGLERFISH.get(), 8, 1, 2).add(ModEntities.GLOW_SQUID.get(), 10, 1, 3)
                        .add(ModEntities.GLOW_JELLYFISH.get(), 10, 3, 6).add(ModEntities.SHARK.get(), 1, 1, 1).build()));
        context.register(ISLES, biome(0x4E96CE, 0xA6D2DE, 0x2A8CC0, 0x10405E, features(placed, carvers,
                        own("storm_coast/sea_stacks"), own("storm_coast/boulders"), own("coral_coast/nacre_rocks"),
                        own("coral_coast/palms"), own("storm_coast/windswept_trees"),
                        net.minecraft.data.worldgen.placement.VegetationPlacements.PATCH_GRASS_JUNGLE,
                        net.minecraft.data.worldgen.placement.VegetationPlacements.PATCH_TALL_GRASS,
                        net.minecraft.data.worldgen.placement.VegetationPlacements.FLOWER_WARM,
                        net.minecraft.data.worldgen.placement.VegetationPlacements.PATCH_SUGAR_CANE,
                        net.minecraft.data.worldgen.placement.VegetationPlacements.PATCH_MELON_SPARSE,
                        AquaticPlacements.SEAGRASS_NORMAL, AquaticPlacements.KELP_WARM),
                spawns().add(ModEntities.GIANT_CRAB.get(), 8, 2, 4).add(ModEntities.TURTLE.get(), 4, 1, 3)
                        .add(ModEntities.COD.get(), 6, 2, 4).add(ModEntities.DOLPHIN.get(), 2, 1, 3).build()));
        // seit 0.92: die Landschaften unter Wasser, jede mit ihren Bäumen (die Reihenfolge der Features passt zu den übrigen
        // Biomen: die Bäume immer zuerst)
        context.register(KELP_FOREST, biome(0x4A9A88, 0x9CCAB8, 0x2A8A6A, 0x0E3A2C, features(placed, carvers,
                        tree("kelpwood"), own("standard/ocean/kelp_forests"), own("standard/ocean/cold_kelp_forests"), own("standard/ocean/sea_stacks"),
                        AquaticPlacements.SEAGRASS_DEEP, AquaticPlacements.KELP_COLD),
                spawns().add(ModEntities.SQUID.get(), 4, 2, 4).add(ModEntities.SALMON.get(), 10, 3, 6).add(ModEntities.COD.get(), 10, 3, 6)
                        .add(ModEntities.MANTA_RAY.get(), 2, 1, 2).add(ModEntities.SHARK.get(), 1, 1, 1).add(ModEntities.DOLPHIN.get(), 2, 2, 3)
                        .build()));
        context.register(CORAL_GARDEN, biome(0x5AA8D8, 0xACDCE4, 0x3AB0C8, 0x1A6078, features(placed, carvers,
                        tree("coralwood"), own("coral_coast/coral_gardens"), own("standard/ocean/anemone_gardens"),
                        AquaticPlacements.WARM_OCEAN_VEGETATION, AquaticPlacements.SEAGRASS_WARM, AquaticPlacements.SEA_PICKLE),
                spawns().add(ModEntities.TROPICAL_FISH.get(), 25, 8, 8).add(ModEntities.PUFFERFISH.get(), 8, 1, 3)
                        .add(ModEntities.TURTLE.get(), 2, 1, 2).add(ModEntities.GLOW_JELLYFISH.get(), 4, 2, 4).build()));
        context.register(PEARL_GROUNDS, biome(0x6AA8D8, 0xC8E0EC, 0x4AA8D0, 0x1A5070, features(placed, carvers,
                        tree("pearlwood"), own("standard/ocean/anemone_gardens"), own("standard/ocean/shell_beds"), own("coral_coast/nacre_rocks"),
                        AquaticPlacements.SEAGRASS_NORMAL),
                spawns().add(ModEntities.MANTA_RAY.get(), 4, 1, 2).add(ModEntities.GLOW_JELLYFISH.get(), 8, 3, 6)
                        .add(ModEntities.GIANT_CRAB.get(), 2, 1, 2).add(ModEntities.COD.get(), 8, 3, 6).add(ModEntities.DOLPHIN.get(), 2, 2, 4)
                        .build()));
        context.register(GLOW_DEPTHS, biome(0x2A5898, 0x4A7AA8, 0x10406A, 0x020C1A, features(placed, carvers,
                        tree("abysswood"), own("standard/ocean/glow_fields"), own("standard/ocean/vent_fields"), AquaticPlacements.SEAGRASS_DEEP_COLD),
                spawns().add(ModEntities.ANGLERFISH.get(), 8, 1, 2).add(ModEntities.GLOW_SQUID.get(), 14, 1, 3)
                        .add(ModEntities.GLOW_JELLYFISH.get(), 14, 3, 6).build()));
    }

    @SafeVarargs
    private static BiomeGenerationSettings features(HolderGetter<PlacedFeature> placed, HolderGetter<WorldCarver> carvers,
            ResourceKey<PlacedFeature>... vegetation) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(placed, carvers);
        // seit 0.97: Aquamarinerz in jedem Biom des Urmeers (Adern und Findlinge am Grund)
        builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, UrmeerFeatures.ORE_AQUAMARINE_PLACED);
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, UrmeerFeatures.AQUAMARINE_ROCKS_PLACED);
        for (ResourceKey<PlacedFeature> key : vegetation) {
            String path = key.identifier().getPath();
            GenerationStep.Decoration step = path.contains("sea_stacks") || path.contains("boulders") || path.contains("rocks")
                    ? GenerationStep.Decoration.LOCAL_MODIFICATIONS : GenerationStep.Decoration.VEGETAL_DECORATION;
            builder.addFeature(step, key);
        }
        return builder.build();
    }

    private static final class Spawns {
        private final MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();

        Spawns add(EntityType<?> type, int weight, int min, int max) {
            this.builder.addSpawn(type, weight, min, max);
            return this;
        }

        MobSpawnSettings build() {
            return this.builder.build();
        }
    }

    private static Spawns spawns() {
        return new Spawns();
    }

    /** Ein Biom des Urmeers: Himmel, Dunst, Wasser und Wassernebel. */
    private static Biome biome(int sky, int fog, int water, int waterFog, BiomeGenerationSettings generation, MobSpawnSettings spawns) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.6f)
                .downfall(0.7f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(water)
                        .grassColorOverride(0x6ABF5A)
                        .foliageColorOverride(0x4FAE4A)
                        .dryFoliageColorOverride(0x8A9A4A)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(sky))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(fog))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(waterFog))
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(ParticleTypes.UNDERWATER, 0.004f)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_UNDER_WATER))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)), List.of()))
                .mobSpawnSettings(spawns)
                .generationSettings(generation)
                .build();
    }
}
