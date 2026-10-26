package io.github.sirschrolli.kingdomsatwar.worldgen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jspecify.annotations.Nullable;

/**
 * Die Biome der Drachennester. Sie entstehen nicht über die normale Biomverteilung, sondern das Nest-Bauwerk malt sie
 * in seine Umgebung (siehe {@code DragonLairPiece}). Darum haben sie keine eigenen Features – die Landschaft gestaltet
 * das Nest selbst –, aber eigene Farben, Partikel, Geräusche und Regeln.
 */
public final class ModBiomes {
    /** Aschelande um Pyrrhons Vulkankrater. */
    public static final ResourceKey<Biome> ASHLANDS = key("ashlands");
    /** Frostkessel um Glaciaras Gletscherkessel. */
    public static final ResourceKey<Biome> FROSTBASIN = key("frostbasin");
    /** Zerklüftete Weiten um Terragons Felsschlucht. */
    public static final ResourceKey<Biome> RUGGED_WILDS = key("rugged_wilds");
    /** Uralter Hain um Verdantis' Nest. */
    public static final ResourceKey<Biome> ANCIENT_GROVE = key("ancient_grove");
    /** Sturmklippen um Voltarions Sturmgipfel. */
    public static final ResourceKey<Biome> STORM_CLIFFS = key("storm_cliffs");
    /** Abgrundgrotte um Thalassas Versunkene Grotte (am Meeresgrund). */
    public static final ResourceKey<Biome> ABYSSAL_GROTTO = key("abyssal_grotto");

    private ModBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    /** Biom um das Nest dieser Art (oder null, solange die Art noch keins hat). */
    public static @Nullable ResourceKey<Biome> forNest(DragonType type) {
        return switch (type) {
            case FIRE -> ASHLANDS;
            case ICE -> FROSTBASIN;
            case NATURE -> ANCIENT_GROVE;
            case EARTH -> RUGGED_WILDS;
            case LIGHTNING -> STORM_CLIFFS;
            case WATER -> ABYSSAL_GROTTO;
        };
    }

    /** Ist das eines der Nest-Biome? */
    public static boolean isNestBiome(net.minecraft.core.Holder<Biome> biome) {
        return biome.is(ASHLANDS) || biome.is(FROSTBASIN) || biome.is(ANCIENT_GROVE) || biome.is(RUGGED_WILDS) || biome.is(STORM_CLIFFS)
                || biome.is(ABYSSAL_GROTTO);
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        context.register(ASHLANDS, ashlands());
        context.register(FROSTBASIN, frostbasin());
        context.register(ANCIENT_GROVE, ancientGrove());
        context.register(RUGGED_WILDS, ruggedWilds());
        context.register(STORM_CLIFFS, stormCliffs());
        context.register(ABYSSAL_GROTTO, abyssalGrotto());
    }

    /**
     * Abgrundgrotte: tiefes, dunkles Petrol im Wasser, ein grünlich-blauer Dunst, die Musik unter Wasser und das ferne
     * Rauschen der Tiefe – auch in der Luftblase der Grotte.
     */
    private static Biome abyssalGrotto() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.5f)
                .downfall(0.5f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x1E5A66)
                        .grassColorOverride(0x3A7A6A)
                        .foliageColorOverride(0x2E6A5A)
                        .dryFoliageColorOverride(0x4A6A5A)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x6A9AA8))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x2A5A64))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x15404C))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_UNDER_WATER))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
                        Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)),
                        List.of(new AmbientAdditionsSettings(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT
                                .wrapAsHolder(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE), 0.004))))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }

    /**
     * Sturmklippen: ein dunkler, schiefergrauer Gewitterhimmel, fahler Dunst, Funken knistern in der Luft, fernes
     * Donnergrollen, graue, zähe Pflanzenfarben und die Musik der zerklüfteten Gipfel.
     */
    private static Biome stormCliffs() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.5f)
                .downfall(0.9f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3A5A7A)
                        .grassColorOverride(0x6A8478)
                        .foliageColorOverride(0x5A7468)
                        .dryFoliageColorOverride(0x6A7470)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x5C6478))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x7C8698))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x223448))
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(
                        new AmbientParticle(ParticleTypes.ELECTRIC_SPARK, 0.003f)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
                        Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)),
                        List.of(new AmbientAdditionsSettings(
                                net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.LIGHTNING_BOLT_THUNDER),
                                0.0012))))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }

    /**
     * Zerklüftete Weiten: trocken und staubig, ein dunstig-ockerfarbener Himmel, Staub in der Luft, verblichene Farben
     * und die Musik der steinigen Gipfel.
     */
    private static Biome ruggedWilds() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(1.2f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x5A6A5E)
                        .grassColorOverride(0x9A9050)
                        .foliageColorOverride(0x8A8448)
                        .dryFoliageColorOverride(0x9A8A5A)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0xB8A88A))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0xC8B48E))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x3A4038))
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(
                        new AmbientParticle(ParticleTypes.WHITE_ASH, 0.006f)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }

    /**
     * Uralter Hain: sattes, tiefes Grün, ein goldgrüner Dunst unter dem Blätterdach, schwebende Sporen und
     * Glühwürmchen, die Musik der üppigen Höhlen und das leise Rauschen der Wälder.
     */
    private static Biome ancientGrove() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.75f)
                .downfall(0.9f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x2E7A5A)
                        .grassColorOverride(0x3E8A2C)
                        .foliageColorOverride(0x2E7A26)
                        .dryFoliageColorOverride(0x6A8A3A)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x8CC4A0))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0xA8D49A))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x1E4A3A))
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(
                        new AmbientParticle(ParticleTypes.SPORE_BLOSSOM_AIR, 0.012f),
                        new AmbientParticle(ParticleTypes.FIREFLY, 0.004f)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }

    /**
     * Frostkessel: bitterkalt, ständiger Schneefall, ein fahler, weißlich-blauer Himmel, glitzernde Eiskristalle in der
     * Luft und die Musik der vereisten Gipfel.
     */
    private static Biome frostbasin() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(-0.9f)
                .downfall(0.7f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x3A6EA5)
                        .grassColorOverride(0x86B69C)
                        .foliageColorOverride(0x66A486)
                        .dryFoliageColorOverride(0x8AA4A0)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0xB4CCE6))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0xD8E6F4))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x2A4A7A))
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(
                        new AmbientParticle(ParticleTypes.SNOWFLAKE, 0.012f),
                        new AmbientParticle(ParticleTypes.WHITE_ASH, 0.02f)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }

    /**
     * Aschelande: dunstiger, rötlich-grauer Himmel, fallende Asche, das Knistern und Grollen der Basaltdeltas,
     * vertrocknete Farben. Wasser verdampft sofort.
     */
    private static Biome ashlands() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0x5C584F)
                        .grassColorOverride(0x857650)
                        .foliageColorOverride(0x665640)
                        .dryFoliageColorOverride(0x5A4A34)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x8A7268))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x7A5C4E))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x34302B))
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(
                        new AmbientParticle(ParticleTypes.WHITE_ASH, 0.045f),
                        new AmbientParticle(ParticleTypes.ASH, 0.02f)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(
                        Optional.of(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0)),
                        List.of(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))))
                .setAttribute(EnvironmentAttributes.WATER_EVAPORATES, true)
                .setAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS, true)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }
}
