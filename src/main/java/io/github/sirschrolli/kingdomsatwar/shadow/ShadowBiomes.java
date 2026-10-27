package io.github.sirschrolli.kingdomsatwar.shadow;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
        context.register(SHADOW_FOREST, biome(new Look(0x2E2252, 0x3E2E66, 0x3A3C90, 0x101236, 0x6450A8, 0x7A4AC8)
                        .fog(20.0f, 150.0f).particles(new AmbientParticle(ParticleTypes.SPORE_BLOSSOM_AIR, 0.0012f))
                        .music(SoundEvents.MUSIC_BIOME_DEEP_DARK).loop(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
                        .additions(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111),
                ShadowFeatures.forest(placed, carvers),
                new Spawns().stalkers(100).crawlers(25).nightWings(8).moonStags(10).moths(25).worldGen(0.08f), false, 0.6f, 0.8f));
        context.register(PALE_WASTES, biome(new Look(0x4C4658, 0x8A8294, 0x5A5A7A, 0x1E1E30, 0xA49EAE, 0x9C94A8)
                        .fog(28.0f, 200.0f).particles(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.012f))
                        .music(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY).loop(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP),
                ShadowFeatures.wastes(placed, carvers),
                new Spawns().stalkers(90).crawlers(20).nightWings(30), false, 1.2f, 0.0f));
        context.register(CRYSTAL_CHASMS, biome(new Look(0x26306A, 0x34488A, 0x2E5AAA, 0x0E1C42, 0x4A68B4, 0x4A7AC4)
                        .fog(24.0f, 180.0f).particles(new AmbientParticle(ParticleTypes.END_ROD, 0.0009f))
                        .music(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES),
                ShadowFeatures.chasms(placed, carvers),
                new Spawns().crawlers(100).stalkers(40).nightWings(10).moths(10), false, 0.8f, 0.4f));
        context.register(SOUL_MARSH, biome(new Look(0x203C44, 0x2C5050, 0x2A5E58, 0x0A1E1C, 0x3E7A6A, 0x2E7A62)
                        .fog(10.0f, 110.0f).particles(new AmbientParticle(ParticleTypes.SOUL, 0.0006f))
                        .music(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY).loop(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
                        .additions(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111),
                ShadowFeatures.marsh(placed, carvers),
                new Spawns().stalkers(80).crawlers(15).nightWings(10).moonStags(5).moths(35), true, 0.7f, 0.9f));
        context.register(SHADOW_PEAKS, biome(new Look(0x323A62, 0x5A5E80, 0x4A5A8A, 0x121A34, 0x6A70A0, 0x5E6898)
                        .fog(32.0f, 240.0f).particles(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.004f))
                        .music(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS),
                ShadowFeatures.peaks(placed, carvers),
                new Spawns().nightWings(50).stalkers(60).crawlers(25), true, -0.6f, 0.4f));
        context.register(GLIMMER_MEADOWS, biome(new Look(0x3A3C7A, 0x5A5C9E, 0x4A5CB8, 0x141A44, 0x7E88DC, 0x9A8ADA)
                        .fog(32.0f, 230.0f).particles(new AmbientParticle(ParticleTypes.FIREFLY, 0.0035f))
                        .music(SoundEvents.MUSIC_END),
                ShadowFeatures.meadows(placed, carvers),
                new Spawns().stalkers(60).crawlers(10).nightWings(12).moonStags(14).moths(45).worldGen(0.1f), false, 0.7f, 0.5f));
        context.register(ABYSSAL_SEA, biome(new Look(0x1E1C46, 0x262658, 0x1A2468, 0x060A24, 0x4A4480, 0x4A4486)
                        .fog(24.0f, 200.0f).music(SoundEvents.MUSIC_UNDER_WATER),
                ShadowFeatures.sea(placed, carvers),
                new Spawns().nightWings(15), false, 0.5f, 0.5f));
        context.register(FLOATING_ISLES, biome(new Look(0x3E2E72, 0x5E4C8E, 0x6A5AC8, 0x1A1846, 0xB07AD8, 0xC88AE8)
                        .fog(40.0f, 260.0f).particles(new AmbientParticle(ParticleTypes.END_ROD, 0.0007f))
                        .music(SoundEvents.MUSIC_END),
                ShadowFeatures.isles(placed, carvers),
                new Spawns().nightWings(70).stalkers(25).moonStags(6).moths(20), false, 0.7f, 0.5f));
        context.register(SHADOW_DEPTHS, biome(new Look(0x16122A, 0x1E1630, 0x1C2050, 0x080A1E, 0x5A4E80, 0x5A4E80)
                        .fog(12.0f, 90.0f).particles(new AmbientParticle(ParticleTypes.ASH, 0.002f))
                        .music(SoundEvents.MUSIC_BIOME_DEEP_DARK),
                ShadowFeatures.depths(placed, carvers),
                new Spawns().crawlers(100).stalkers(60), false, 0.8f, 0.4f));
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

    /**
     * Wer in einem Biom von selbst erscheint: Schattenpirscher (Gruppen von 1–2), Kristallkriecher (1–3),
     * Nachtschwingen (1–2), Mondhirsche (Herden von 2–4) und Glimmerfalter (Schwärme von 2–5).
     */
    private static final class Spawns {
        final MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        float worldGen = MobSpawnSettings.DEFAULT_CREATURE_WORLD_GEN_SPAWN_PROBABILITY;

        Spawns stalkers(int weight) {
            this.builder.addSpawn(ModEntities.SHADE_STALKER.get(), weight, 1, 2);
            return this;
        }

        Spawns crawlers(int weight) {
            this.builder.addSpawn(ModEntities.CRYSTAL_CRAWLER.get(), weight, 1, 3);
            return this;
        }

        Spawns nightWings(int weight) {
            this.builder.addSpawn(ModEntities.NIGHT_WING.get(), weight, 1, 2);
            return this;
        }

        Spawns moonStags(int weight) {
            this.builder.addSpawn(ModEntities.MOON_STAG.get(), weight, 2, 4);
            return this;
        }

        Spawns moths(int weight) {
            this.builder.addSpawn(ModEntities.GLIMMER_MOTH.get(), weight, 2, 5);
            return this;
        }

        /** Wie oft beim Erzeugen eines Chunks Tiere erscheinen. */
        Spawns worldGen(float probability) {
            this.worldGen = probability;
            return this;
        }
    }

    private static Biome biome(Look look, BiomeGenerationSettings generation, Spawns spawns, boolean precipitation, float temperature,
            float downfall) {
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
                .mobSpawnSettings(spawns.builder.build())
                .setAttribute(EnvironmentAttributes.CREATURE_WORLD_GEN_SPAWN_PROBABILITY, spawns.worldGen)
                .generationSettings(generation);
        if (!look.particles.isEmpty()) {
            builder.setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.copyOf(look.particles));
        }
        return builder.build();
    }
}
