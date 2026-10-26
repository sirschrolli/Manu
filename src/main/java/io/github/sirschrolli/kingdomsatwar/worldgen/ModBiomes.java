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
    /** Uralter Hain um Verdantis' Nest. */
    public static final ResourceKey<Biome> ANCIENT_GROVE = key("ancient_grove");

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
            default -> null;
        };
    }

    /** Ist das eines der Nest-Biome? */
    public static boolean isNestBiome(net.minecraft.core.Holder<Biome> biome) {
        return biome.is(ASHLANDS) || biome.is(FROSTBASIN) || biome.is(ANCIENT_GROVE);
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        context.register(ASHLANDS, ashlands());
        context.register(FROSTBASIN, frostbasin());
        context.register(ANCIENT_GROVE, ancientGrove());
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
                        .grassColorOverride(0x4E9A32)
                        .foliageColorOverride(0x3A8A2A)
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
