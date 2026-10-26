package io.github.sirschrolli.kingdomsatwar.shadow;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.AmbientAdditionsSettings;
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
 * Die Biome der Schattenwelt und ihre Lage im Klima des Generators ({@link ShadowTerrain}):
 * <ul>
 *     <li><b>Abgrundmeer</b> – das tiefschwarze Meer zwischen den Landmassen;</li>
 *     <li><b>Schattengipfel</b> – die zerklüfteten Gebirge mit schwarzen Felsnadeln und Aschschnee;</li>
 *     <li><b>Kristallabgründe</b> – die tiefen Schluchten in den warmen Tälern;</li>
 *     <li><b>Seelensumpf</b> – flache, nasse Niederungen und die kühlen, sumpfigen Flusstäler;</li>
 *     <li><b>Schattenwald</b> – feuchtes Hügelland unter violettem Laub;</li>
 *     <li><b>Glimmerweiten</b> – weite, silbrig schimmernde Wiesen mit Mondblumen;</li>
 *     <li><b>Bleiche Öde</b> – trockenes, fahles Land voller Knochen;</li>
 *     <li><b>Schwebende Inseln</b> – hoch über dem Land (alles, was weit über der Oberfläche liegt);</li>
 *     <li><b>Schattentiefen</b> – die Höhlen tief unter der Oberfläche.</li>
 * </ul>
 */
public final class ShadowBiomes {
    public static final ResourceKey<Biome> SHADOW_FOREST = key("shadow_forest");
    public static final ResourceKey<Biome> PALE_WASTES = key("pale_wastes");
    public static final ResourceKey<Biome> CRYSTAL_CHASMS = key("crystal_chasms");
    public static final ResourceKey<Biome> SOUL_MARSH = key("soul_marsh");
    public static final ResourceKey<Biome> SHADOW_PEAKS = key("shadow_peaks");
    public static final ResourceKey<Biome> GLIMMER_MEADOWS = key("glimmer_meadows");
    public static final ResourceKey<Biome> ABYSSAL_SEA = key("abyssal_sea");
    public static final ResourceKey<Biome> FLOATING_ISLES = key("floating_isles");
    public static final ResourceKey<Biome> SHADOW_DEPTHS = key("shadow_depths");

    /** Alle Biome der Schattenwelt (feste Reihenfolge). */
    public static final List<ResourceKey<Biome>> ALL = List.of(SHADOW_FOREST, PALE_WASTES, CRYSTAL_CHASMS, SOUL_MARSH, SHADOW_PEAKS,
            GLIMMER_MEADOWS, ABYSSAL_SEA, FLOATING_ISLES, SHADOW_DEPTHS);

    private ShadowBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    // =======================================================================================
    // Lage im Klima
    // =======================================================================================

    private static final Climate.Parameter FULL = Climate.Parameter.span(-1.0f, 1.0f);
    private static final Climate.Parameter SURFACE = Climate.Parameter.point(0.0f);
    /** Täler: hier liegen die Abgründe (warm) und die Sumpfflüsse (kühl). */
    private static final float VALLEY = 0.055f;
    private static final Climate.Parameter[] OUTSIDE_VALLEYS = {Climate.Parameter.span(-1.0f, -VALLEY), Climate.Parameter.span(VALLEY, 1.0f)};
    private static final Climate.Parameter LAND = Climate.Parameter.span(-0.19f, 1.0f);
    private static final Climate.Parameter INLAND = Climate.Parameter.span(-0.11f, 1.0f);

    /** Klimapunkte aller Biome für den Biom-Generator. */
    public static Climate.ParameterList<Holder<Biome>> parameters(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = new ArrayList<>();
        // Hoch über dem Land: die schwebenden Inseln
        add(list, biomes, FLOATING_ISLES, FULL, FULL, FULL, FULL, Climate.Parameter.span(-2.0f, -0.75f), FULL);
        // Das Meer
        add(list, biomes, ABYSSAL_SEA, FULL, FULL, Climate.Parameter.span(-1.2f, -0.19f), FULL, SURFACE, FULL);
        // Die Täler: Abgründe in warmen, Sumpfflüsse in kühlen Gegenden
        add(list, biomes, CRYSTAL_CHASMS, Climate.Parameter.span(0.0f, 1.0f), FULL, INLAND, FULL, Climate.Parameter.span(0.0f, 1.2f),
                Climate.Parameter.span(-VALLEY, VALLEY));
        add(list, biomes, SOUL_MARSH, Climate.Parameter.span(-1.0f, 0.0f), FULL, INLAND, FULL, SURFACE, Climate.Parameter.span(-VALLEY, VALLEY));
        for (Climate.Parameter weirdness : OUTSIDE_VALLEYS) {
            // Tief unter der Oberfläche
            add(list, biomes, SHADOW_DEPTHS, FULL, FULL, FULL, FULL, Climate.Parameter.span(0.2f, 1.1f), weirdness);
            // Gebirge
            add(list, biomes, SHADOW_PEAKS, FULL, FULL, LAND, Climate.Parameter.span(-1.0f, -0.375f), SURFACE, weirdness);
            // Feucht: Sumpf in der Ebene, Wald im Hügelland
            add(list, biomes, SOUL_MARSH, FULL, Climate.Parameter.span(0.1f, 1.0f), LAND, Climate.Parameter.span(0.45f, 1.0f), SURFACE, weirdness);
            add(list, biomes, SHADOW_FOREST, FULL, Climate.Parameter.span(0.1f, 1.0f), LAND, Climate.Parameter.span(-0.375f, 0.45f), SURFACE,
                    weirdness);
            // Mittel: die Glimmerweiten
            add(list, biomes, GLIMMER_MEADOWS, FULL, Climate.Parameter.span(-0.35f, 0.1f), LAND, Climate.Parameter.span(-0.375f, 1.0f), SURFACE,
                    weirdness);
            // Trocken: die Bleiche Öde
            add(list, biomes, PALE_WASTES, FULL, Climate.Parameter.span(-1.0f, -0.35f), LAND, Climate.Parameter.span(-0.375f, 1.0f), SURFACE,
                    weirdness);
        }
        return new Climate.ParameterList<>(list);
    }

    private static void add(List<Pair<Climate.ParameterPoint, Holder<Biome>>> list, HolderGetter<Biome> biomes, ResourceKey<Biome> biome,
            Climate.Parameter temperature, Climate.Parameter humidity, Climate.Parameter continentalness, Climate.Parameter erosion,
            Climate.Parameter depth, Climate.Parameter weirdness) {
        list.add(Pair.of(Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, 0.0f), biomes.getOrThrow(biome)));
    }

    // =======================================================================================
    // Die Biome
    // =======================================================================================

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldCarver> carvers = context.lookup(Registries.CARVER);
        context.register(SHADOW_FOREST, biome(new Look(0x241A3C, 0x2C2246, 0x2A2C5C, 0x0E1030, 0x5A3E8E, 0x6C3EA8)
                        .fog(20.0f, 150.0f).particles(new AmbientParticle(ParticleTypes.SPORE_BLOSSOM_AIR, 0.0012f))
                        .music(SoundEvents.MUSIC_BIOME_DEEP_DARK).loop(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
                        .additions(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111),
                ShadowFeatures.forest(placed, carvers), false, 0.6f, 0.8f));
        context.register(PALE_WASTES, biome(new Look(0x3A3448, 0x6A6278, 0x4A4A62, 0x1A1A2A, 0x8E879A, 0x8C8298)
                        .fog(28.0f, 200.0f).particles(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.012f))
                        .music(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY).loop(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP),
                ShadowFeatures.wastes(placed, carvers), false, 1.2f, 0.0f));
        context.register(CRYSTAL_CHASMS, biome(new Look(0x20244A, 0x2A3264, 0x2A4A8A, 0x0C1838, 0x4A5C9C, 0x4C6CAC)
                        .fog(24.0f, 180.0f).particles(new AmbientParticle(ParticleTypes.END_ROD, 0.0009f))
                        .music(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES),
                ShadowFeatures.chasms(placed, carvers), false, 0.8f, 0.4f));
        context.register(SOUL_MARSH, biome(new Look(0x1C2830, 0x2A3A3A, 0x1A3A34, 0x081A16, 0x3E5E4E, 0x305E4A)
                        .fog(10.0f, 110.0f).particles(new AmbientParticle(ParticleTypes.SOUL, 0.0006f))
                        .music(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY).loop(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
                        .additions(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111),
                ShadowFeatures.marsh(placed, carvers), true, 0.7f, 0.9f));
        context.register(SHADOW_PEAKS, biome(new Look(0x2A283E, 0x48445E, 0x3A4062, 0x101830, 0x5E5874, 0x544C74)
                        .fog(32.0f, 240.0f).particles(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.004f))
                        .music(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS),
                ShadowFeatures.peaks(placed, carvers), true, -0.6f, 0.4f));
        context.register(GLIMMER_MEADOWS, biome(new Look(0x2C2650, 0x3C3668, 0x3A3A7C, 0x12123A, 0x7C72BA, 0x8C7ACA)
                        .fog(32.0f, 230.0f).particles(new AmbientParticle(ParticleTypes.FIREFLY, 0.0035f))
                        .music(SoundEvents.MUSIC_END),
                ShadowFeatures.meadows(placed, carvers), false, 0.7f, 0.5f));
        context.register(ABYSSAL_SEA, biome(new Look(0x181630, 0x1C1A36, 0x0E0E24, 0x04040E, 0x3A3460, 0x3A3466)
                        .fog(24.0f, 200.0f).music(SoundEvents.MUSIC_UNDER_WATER),
                ShadowFeatures.sea(placed, carvers), false, 0.5f, 0.5f));
        context.register(FLOATING_ISLES, biome(new Look(0x382C64, 0x463A78, 0x4A4A9A, 0x16163E, 0x9C7CCC, 0xB28AE2)
                        .fog(40.0f, 260.0f).particles(new AmbientParticle(ParticleTypes.END_ROD, 0.0007f))
                        .music(SoundEvents.MUSIC_END),
                ShadowFeatures.isles(placed, carvers), false, 0.7f, 0.5f));
        context.register(SHADOW_DEPTHS, biome(new Look(0x120E1C, 0x16101E, 0x14183A, 0x06081A, 0x4A3E6A, 0x4A3E6A)
                        .fog(12.0f, 90.0f).particles(new AmbientParticle(ParticleTypes.ASH, 0.002f))
                        .music(SoundEvents.MUSIC_BIOME_DEEP_DARK),
                ShadowFeatures.depths(placed, carvers), false, 0.8f, 0.4f));
    }

    /** Farben, Nebel, Partikel und Klänge eines Bioms. */
    private static final class Look {
        final int sky;
        final int fog;
        final int water;
        final int waterFog;
        final int grass;
        final int foliage;
        float fogStart = 24.0f;
        float fogEnd = 200.0f;
        final List<AmbientParticle> particles = new ArrayList<>();
        Holder<SoundEvent> music = SoundEvents.MUSIC_BIOME_DEEP_DARK;
        Optional<Holder<SoundEvent>> loop = Optional.empty();
        final List<AmbientAdditionsSettings> additions = new ArrayList<>();

        Look(int sky, int fog, int water, int waterFog, int grass, int foliage) {
            this.sky = sky;
            this.fog = fog;
            this.water = water;
            this.waterFog = waterFog;
            this.grass = grass;
            this.foliage = foliage;
        }

        Look fog(float start, float end) {
            this.fogStart = start;
            this.fogEnd = end;
            return this;
        }

        Look particles(AmbientParticle particle) {
            this.particles.add(particle);
            return this;
        }

        Look music(Holder<SoundEvent> music) {
            this.music = music;
            return this;
        }

        Look loop(Holder<SoundEvent> loop) {
            this.loop = Optional.of(loop);
            return this;
        }

        Look additions(Holder<SoundEvent> sound, double chance) {
            this.additions.add(new AmbientAdditionsSettings(sound, chance));
            return this;
        }
    }

    private static Biome biome(Look look, BiomeGenerationSettings generation, boolean precipitation, float temperature, float downfall) {
        Biome.BiomeBuilder builder = new Biome.BiomeBuilder()
                .hasPrecipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(look.water)
                        .grassColorOverride(look.grass)
                        .foliageColorOverride(look.foliage)
                        .dryFoliageColorOverride(look.foliage)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(look.sky))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(look.fog))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(look.waterFog))
                .setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, look.fogStart)
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, look.fogEnd)
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(look.music))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(look.loop,
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)),
                        List.copyOf(look.additions)))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(generation);
        if (!look.particles.isEmpty()) {
            builder.setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.copyOf(look.particles));
        }
        return builder.build();
    }
}
