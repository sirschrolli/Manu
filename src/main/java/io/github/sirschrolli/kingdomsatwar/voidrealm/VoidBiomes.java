package io.github.sirschrolli.kingdomsatwar.voidrealm;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Die Biome der Leere:
 * <ul>
 *     <li><b>Sternennichts</b> – die weiten, leeren Strecken zwischen den Archipelen: nur einzelne Felsbrocken, ferne
 *     Bruchstücke und das Funkeln der Sterne;</li>
 *     <li><b>Kristallwald</b> – Bruchstücke voller verdrehter Kristallbäume mit leuchtendem Kristalllaub, Sternenranken
 *     und Leerenblüten;</li>
 *     <li><b>Schwerelose Ruinen</b> – die Trümmer der Alten aus Sternenmarmor: zerbrochene Säulen, Bögen und Treppen, über
 *     denen Mauerstücke frei in der Luft schweben;</li>
 *     <li><b>Sternenstaubfelder</b> – fahl leuchtende Ebenen aus Sternenstaub mit Kratern und gefallenen Sternen;</li>
 *     <li><b>Splitterklippen</b> – zerklüftete Bruchstücke, aus denen riesige Kristallspitzen schräg in die Leere ragen;</li>
 *     <li><b>Weltenreste</b> – Bruchstücke fremder Welten: ein Stück Wiese mit toten Bäumen, ein Stück Wüste, ein Stück
 *     Gletscher, mit den Resten der Häuser, die einmal darauf standen.</li>
 * </ul>
 */
public final class VoidBiomes {
    public static final ResourceKey<Biome> EXPANSE = key("void_expanse");
    public static final ResourceKey<Biome> CRYSTAL_FOREST = key("void_crystal_forest");
    public static final ResourceKey<Biome> WEIGHTLESS_RUINS = key("void_ruins");
    public static final ResourceKey<Biome> STARDUST_FIELDS = key("void_stardust_fields");
    public static final ResourceKey<Biome> SHARD_CLIFFS = key("void_shard_cliffs");
    public static final ResourceKey<Biome> WORLD_REMNANTS = key("void_world_remnants");

    public static final List<ResourceKey<Biome>> ALL = List.of(EXPANSE, CRYSTAL_FOREST, WEIGHTLESS_RUINS, STARDUST_FIELDS, SHARD_CLIFFS,
            WORLD_REMNANTS);

    private VoidBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    private static final Climate.Parameter FULL = Climate.Parameter.span(-1.0f, 1.0f);

    /** Ab diesem Wert der Zonen-Noise (Erosion) treiben Weltenreste – etwa jedes achte Gebiet. */
    public static final float REMNANT_ZONE = 0.42f;

    /**
     * Klimapunkte: die leeren Strecken über die Archipel-Maske, die Weltenreste über eine eigene Zonen-Noise (als
     * abgegrenzte Gebiete), die übrigen Biome über Temperatur und Feuchte.
     */
    public static Climate.ParameterList<Holder<Biome>> parameters(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = new ArrayList<>();
        Climate.Parameter open = Climate.Parameter.span(-2.0f, -0.22f);
        Climate.Parameter land = Climate.Parameter.span(-0.22f, 2.0f);
        Climate.Parameter usual = Climate.Parameter.span(-2.0f, REMNANT_ZONE);
        add(list, biomes, EXPANSE, FULL, FULL, open, FULL);
        add(list, biomes, WORLD_REMNANTS, FULL, FULL, land, Climate.Parameter.span(REMNANT_ZONE, 2.0f));
        Climate.Parameter cold = Climate.Parameter.span(-2.0f, -0.15f);
        Climate.Parameter warm = Climate.Parameter.span(0.15f, 2.0f);
        Climate.Parameter middle = Climate.Parameter.span(-0.15f, 0.15f);
        // kühl: Splitterklippen, im Feuchten Kristallwald
        add(list, biomes, SHARD_CLIFFS, cold, Climate.Parameter.span(-2.0f, 0.1f), land, usual);
        add(list, biomes, CRYSTAL_FOREST, cold, Climate.Parameter.span(0.1f, 2.0f), land, usual);
        // warm: Sternenstaubfelder, im Feuchten die Ruinen
        add(list, biomes, STARDUST_FIELDS, warm, Climate.Parameter.span(-2.0f, 0.15f), land, usual);
        add(list, biomes, WEIGHTLESS_RUINS, warm, Climate.Parameter.span(0.15f, 2.0f), land, usual);
        // dazwischen: Ruinen und Kristallwald
        add(list, biomes, WEIGHTLESS_RUINS, middle, Climate.Parameter.span(-2.0f, 0.0f), land, usual);
        add(list, biomes, CRYSTAL_FOREST, middle, Climate.Parameter.span(0.0f, 2.0f), land, usual);
        return new Climate.ParameterList<>(list);
    }

    private static void add(List<Pair<Climate.ParameterPoint, Holder<Biome>>> list, HolderGetter<Biome> biomes, ResourceKey<Biome> biome,
            Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter zone) {
        list.add(Pair.of(Climate.parameters(temperature, humidity, continentalness, zone, FULL, FULL, 0.0f), biomes.getOrThrow(biome)));
    }

    // =====================================================================================
    // Die Biome
    // =====================================================================================

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldCarver> carvers = context.lookup(Registries.CARVER);
        context.register(EXPANSE, biome(0x07061A, 0x1A1238, 0x5A4CA8, ParticleTypes.END_ROD, 0.0009f, SoundEvents.MUSIC_END,
                VoidFeatures.expanse(placed, carvers), VoidSpawns.expanse()));
        context.register(CRYSTAL_FOREST, biome(0x0A0A24, 0x1E1648, 0x6A5CE0, ParticleTypes.END_ROD, 0.0018f, SoundEvents.MUSIC_END,
                VoidFeatures.crystalForest(placed, carvers), VoidSpawns.crystalForest()));
        context.register(WEIGHTLESS_RUINS, biome(0x0C0820, 0x241840, 0x8A70C8, ParticleTypes.WHITE_ASH, 0.004f, SoundEvents.MUSIC_END,
                VoidFeatures.ruins(placed, carvers), VoidSpawns.ruins()));
        context.register(STARDUST_FIELDS, biome(0x0A0C26, 0x22285A, 0x8AA8F0, ParticleTypes.WAX_OFF, 0.0035f, SoundEvents.MUSIC_END,
                VoidFeatures.stardust(placed, carvers), VoidSpawns.stardust()));
        context.register(SHARD_CLIFFS, biome(0x080616, 0x1C1034, 0x9A60E0, ParticleTypes.END_ROD, 0.0012f, SoundEvents.MUSIC_END,
                VoidFeatures.shardCliffs(placed, carvers), VoidSpawns.shardCliffs()));
        context.register(WORLD_REMNANTS, biome(0x0C0A1E, 0x22183A, 0x7A6AB0, ParticleTypes.WHITE_ASH, 0.002f, SoundEvents.MUSIC_END,
                VoidFeatures.remnants(placed, carvers), VoidSpawns.remnants()));
    }

    private static Biome biome(int sky, int fog, int foliage, net.minecraft.core.particles.ParticleOptions particle, float density,
            Holder<SoundEvent> music, BiomeGenerationSettings generation, MobSpawnSettings spawns) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3A2A8A)
                        .grassColorOverride(0x6A7A9E)
                        .foliageColorOverride(foliage)
                        .dryFoliageColorOverride(foliage)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(sky))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(fog))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x0A0624))
                .setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, 40.0f)
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, 260.0f)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(particle, density)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(music))
                // Klangteppiche und Einzelklänge: world.ambience.AmbienceProfiles
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)), List.of()))
                .mobSpawnSettings(spawns)
                .generationSettings(generation)
                .build();
    }
}
