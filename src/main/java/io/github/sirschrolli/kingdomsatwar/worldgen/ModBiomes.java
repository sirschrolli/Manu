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

    private ModBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    /** Biom um das Nest dieser Art (oder null, solange die Art noch keins hat). */
    public static @Nullable ResourceKey<Biome> forNest(DragonType type) {
        return type == DragonType.FIRE ? ASHLANDS : null;
    }

    /** Ist das eines der Nest-Biome? */
    public static boolean isNestBiome(net.minecraft.core.Holder<Biome> biome) {
        return biome.is(ASHLANDS);
    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        context.register(ASHLANDS, ashlands());
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
