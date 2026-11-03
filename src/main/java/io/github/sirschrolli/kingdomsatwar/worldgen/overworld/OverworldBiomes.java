package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
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
import net.minecraft.world.attribute.modifier.FloatModifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jspecify.annotations.Nullable;

/**
 * Die eigenen Biome der Oberwelt. Wo sie liegen, bestimmt {@link OverworldRegions}; ihren Boden
 * {@link OverworldSurface}, was auf ihnen wächst und steht {@link OverworldFeatures}.
 *
 * <ul>
 *     <li><b>Aschefelder</b> – dunkle Asche und Aschenerde, verkohlte Bäume, Vulkanfelsen mit Glutrissen, rauchende
 *     Glutspalten, Feuerlilien; ein dunstiger, warmgrauer Himmel, in dem feine Asche treibt.</li>
 *     <li><b>Frostgipfel</b> – Gletschereis, Raureifstein und Pulverschnee, Eisnadeln und Frostkristalle; klirrend kalt.</li>
 *     <li><b>Sturmküste</b> – Klippen aus Sturmstein, Brandungstürme, Blitzglas und vom Wind gebeugte Bäume; über ihr
 *     ein schiefergrauer Himmel und fernes Donnergrollen.</li>
 *     <li><b>Urhain</b> – uralte Riesenbäume aus Urholz, Moos, Wurzelwerk, Leuchtpilze und Glühwürmchen.</li>
 *     <li><b>Felsschluchten</b> – Schluchten zwischen gebänderten Felswänden, Felsnadeln, Bernsteinkristalle, Geröll.</li>
 *     <li><b>Korallenküste</b> – helle Strände und flache, türkise Lagunen mit Korallenriffen, Palmen und Perlmuttfelsen.</li>
 *     <li><b>Königsauen</b> – weite Blumenwiesen mit Lavendel, Gartenrosen und Hainglocken und mächtigen alten Eichen.</li>
 *     <li><b>Nebelmoor</b> – ein kühles Moor im Nebel: Torf, abgestorbene Bäume mit hängendem Moos, Schilf und
 *     Mondblumen; nachts tanzen Irrlichter über dem Wasser.</li>
 * </ul>
 */
public final class OverworldBiomes {
    public static final ResourceKey<Biome> ASH_FIELDS = key("ash_fields");
    public static final ResourceKey<Biome> FROST_PEAKS = key("frost_peaks");
    public static final ResourceKey<Biome> STORM_COAST = key("storm_coast");
    public static final ResourceKey<Biome> PRIMEVAL_FOREST = key("primeval_forest");
    public static final ResourceKey<Biome> ROCK_GORGES = key("rock_gorges");
    public static final ResourceKey<Biome> CORAL_COAST = key("coral_coast");
    public static final ResourceKey<Biome> ROYAL_MEADOWS = key("royal_meadows");
    public static final ResourceKey<Biome> MIST_MOOR = key("mist_moor");

    /** Alle eigenen Biome der Oberwelt (feste Reihenfolge). */
    public static final List<ResourceKey<Biome>> ALL = List.of(ASH_FIELDS, FROST_PEAKS, STORM_COAST, PRIMEVAL_FOREST, ROCK_GORGES, CORAL_COAST,
            ROYAL_MEADOWS, MIST_MOOR);

    private OverworldBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldCarver> carvers = context.lookup(Registries.CARVER);

        context.register(ASH_FIELDS, biome(new Look(0xA89484, 0xB49C88, 0x5C584F, 0x34302B, 0x8E7A50, 0x6E5A40)
                        .particles(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.010f), new AmbientParticle(ParticleTypes.ASH, 0.0025f))
                        .music(SoundEvents.MUSIC_BIOME_BADLANDS)
                        .additions(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.004),
                OverworldFeatures.ashFields(placed, carvers), spawns -> BiomeDefaultFeatures.desertSpawns(spawns), false, 2.0f, 0.0f, true));

        context.register(FROST_PEAKS, biome(new Look(0xB4CCE8, 0xDAE6F4, 0x3A6EA5, 0x2A4A7A, 0x86B69C, 0x66A486)
                        .particles(new AmbientParticle(ParticleTypes.SNOWFLAKE, 0.004f))
                        .music(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS),
                OverworldFeatures.frostPeaks(placed, carvers), spawns -> {
                    BiomeDefaultFeatures.commonSpawns(spawns);
                    spawns.addSpawn(EntityTypes.GOAT, 5, 1, 3);
                }, true, -0.7f, 0.9f, false));

        context.register(STORM_COAST, biome(new Look(0x7A8AA4, 0x9AA8BC, 0x3A5A7A, 0x1E3448, 0x6E8A70, 0x5E7A62)
                        .particles(new AmbientParticle(ParticleTypes.ELECTRIC_SPARK, 0.0004f))
                        .music(SoundEvents.MUSIC_BIOME_STONY_PEAKS)
                        .additions(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.LIGHTNING_BOLT_THUNDER),
                                0.0005),
                OverworldFeatures.stormCoast(placed, carvers), BiomeDefaultFeatures::commonSpawns, true, 0.4f, 0.9f, false));

        context.register(PRIMEVAL_FOREST, biome(new Look(0x86B4D2, 0xA6C6A8, 0x2E7A5A, 0x1E4A3A, 0x4E8E34, 0x3E7E2C)
                        .particles(new AmbientParticle(ParticleTypes.SPORE_BLOSSOM_AIR, 0.003f), new AmbientParticle(ParticleTypes.FIREFLY, 0.0015f))
                        .music(SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA),
                OverworldFeatures.primevalForest(placed, carvers), spawns -> {
                    BiomeDefaultFeatures.farmAnimals(spawns);
                    BiomeDefaultFeatures.commonSpawns(spawns);
                    spawns.addSpawn(EntityTypes.WOLF, 5, 4, 4);
                }, true, 0.7f, 0.9f, false));

        context.register(ROCK_GORGES, biome(new Look(0xC0AA88, 0xCCB690, 0x4A6A6E, 0x2A3A38, 0xA89858, 0x958A50)
                        .particles(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.002f))
                        .music(SoundEvents.MUSIC_BIOME_STONY_PEAKS),
                OverworldFeatures.rockGorges(placed, carvers), spawns -> {
                    BiomeDefaultFeatures.farmAnimals(spawns);
                    spawns.addSpawn(EntityTypes.HORSE, 1, 2, 6).addSpawn(EntityTypes.LLAMA, 8, 4, 4).addSpawn(EntityTypes.ARMADILLO, 10, 2, 3);
                    BiomeDefaultFeatures.commonSpawnWithZombieHorse(spawns);
                }, false, 1.6f, 0.05f, true));

        context.register(CORAL_COAST, biome(new Look(0x7ACCF0, 0xC4ECF4, 0x2FD4C8, 0x0B5C66, 0x7ACC5A, 0x5ABC4A),
                OverworldFeatures.coralCoast(placed, carvers), spawns -> {
                    spawns.addSpawn(EntityTypes.TURTLE, 5, 2, 5);
                    BiomeDefaultFeatures.warmOceanSpawns(spawns, 10, 1);
                }, true, 0.95f, 0.6f, false));

        context.register(ROYAL_MEADOWS, biome(new Look(0x82B4FA, 0xC4DAFF, 0x3F76E4, 0x050533, 0x79C85A, 0x5FB848)
                        .music(SoundEvents.MUSIC_BIOME_MEADOW),
                OverworldFeatures.royalMeadows(placed, carvers), spawns -> BiomeDefaultFeatures.plainsSpawns(spawns), true, 0.8f, 0.5f, false));

        context.register(MIST_MOOR, biome(new Look(0x707E86, 0x8C9A96, 0x4C5A3C, 0x1E2818, 0x6E7E4A, 0x5C6C3C)
                        .fog(6.0f, 76.0f)
                        .murkyWater()
                        .music(SoundEvents.MUSIC_BIOME_SWAMP),
                OverworldFeatures.mistMoor(placed, carvers), spawns -> {
                    BiomeDefaultFeatures.swampSpawns(spawns, 70);
                    spawns.addSpawn(EntityTypes.FROG, 10, 2, 5);
                }, true, 0.6f, 0.9f, false));
    }

    /** Farben, Nebel, Partikel und Klänge eines Bioms. */
    private static final class Look {
        final int sky;
        final int fog;
        final int water;
        final int waterFog;
        final int grass;
        final int foliage;
        float fogStart = -1.0f;
        float fogEnd = -1.0f;
        final List<AmbientParticle> particles = new ArrayList<>();
        @Nullable Holder<SoundEvent> music;
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

        Look particles(AmbientParticle... particles) {
            this.particles.addAll(List.of(particles));
            return this;
        }

        Look music(Holder<SoundEvent> music) {
            this.music = music;
            return this;
        }

        boolean murkyWater;

        /** Trübes Wasser wie im Sumpf: man sieht unter Wasser weniger weit. */
        Look murkyWater() {
            this.murkyWater = true;
            return this;
        }

        Look additions(Holder<SoundEvent> sound, double chance) {
            this.additions.add(new AmbientAdditionsSettings(sound, chance));
            return this;
        }
    }

    private static Biome biome(Look look, BiomeGenerationSettings generation, java.util.function.Consumer<MobSpawnSettings.Builder> spawns,
            boolean precipitation, float temperature, float downfall, boolean hot) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        spawns.accept(mobs);
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
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)), List.copyOf(look.additions)))
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation);
        if (look.fogStart >= 0.0f) {
            builder.setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, look.fogStart)
                    .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, look.fogEnd);
        }
        if (look.music != null) {
            builder.setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(look.music));
        }
        if (!look.particles.isEmpty()) {
            builder.setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.copyOf(look.particles));
        }
        if (hot) {
            builder.setAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS, true);
        }
        if (look.murkyWater) {
            builder.modifyAttribute(EnvironmentAttributes.WATER_FOG_END_DISTANCE, FloatModifier.MULTIPLY, 0.85f)
                    .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true);
        }
        return builder.build();
    }
}
