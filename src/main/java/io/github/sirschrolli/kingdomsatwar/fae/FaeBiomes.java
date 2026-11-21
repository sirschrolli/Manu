package io.github.sirschrolli.kingdomsatwar.fae;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleOptions;
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
 * Die Biome des Feenreichs, alle im ewigen Abendrot:
 * <ul>
 *     <li><b>Blütenwiesen</b> – weite, wellige Wiesen voller Mondglocken, Feenrosen und Sternspitzen, einzelne rosa
 *     Glimmerbäume, Feenringe und alte Steinkreise;</li>
 *     <li><b>Glimmerwald</b> – ein dichter Wald aus Glimmerbäumen und Glimmerriesen, deren Laub rosa und türkis leuchtet,
 *     Schleier aus Leuchtranken;</li>
 *     <li><b>Pilzhain</b> – Riesenpilze mit leuchtenden Hüten auf violettem Feenmoos, Laternenpilze überall;</li>
 *     <li><b>Mondseen</b> – stille Seen mit Mondlilien, Inseln und Tränenglimmern an den Ufern;</li>
 *     <li><b>Dornenwildnis</b> – düsteres Dickicht aus Dornenhecken und schwarzen Dornbäumen.</li>
 * </ul>
 */
public final class FaeBiomes {
    public static final ResourceKey<Biome> FAE_MEADOWS = key("fae_meadows");
    public static final ResourceKey<Biome> GLIMMERWOOD = key("glimmerwood");
    public static final ResourceKey<Biome> MUSHROOM_GROVE = key("fae_mushroom_grove");
    public static final ResourceKey<Biome> MOONLIT_LAKES = key("moonlit_lakes");
    public static final ResourceKey<Biome> THORNWILD = key("thornwild");

    public static final List<ResourceKey<Biome>> ALL = List.of(FAE_MEADOWS, GLIMMERWOOD, MUSHROOM_GROVE, MOONLIT_LAKES, THORNWILD);

    private FaeBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    private static final Climate.Parameter FULL = Climate.Parameter.span(-2.0f, 2.0f);

    /** Ab diesem Wert der Zonen-Noise (Erosion) liegt die Dornenwildnis – etwa jedes siebte Gebiet. */
    public static final float THORN_ZONE = 0.45f;

    /**
     * Klimapunkte: die Mondseen über die Seen-Noise (Kontinentalität, passend zum Gelände), die Dornenwildnis über eine
     * eigene Zonen-Noise, die übrigen Biome über Temperatur und Feuchte.
     */
    public static Climate.ParameterList<Holder<Biome>> parameters(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = new ArrayList<>();
        Climate.Parameter lake = Climate.Parameter.span(-2.0f, FaeTerrain.LAKE_EDGE);
        Climate.Parameter land = Climate.Parameter.span(FaeTerrain.LAKE_EDGE, 2.0f);
        Climate.Parameter usual = Climate.Parameter.span(-2.0f, THORN_ZONE);
        add(list, biomes, MOONLIT_LAKES, FULL, FULL, lake, FULL);
        add(list, biomes, THORNWILD, FULL, FULL, land, Climate.Parameter.span(THORN_ZONE, 2.0f));
        Climate.Parameter dry = Climate.Parameter.span(-2.0f, 0.15f);
        Climate.Parameter wet = Climate.Parameter.span(0.15f, 2.0f);
        // feucht: Glimmerwald; trocken und kühl: Pilzhain; trocken und warm: Blütenwiesen
        add(list, biomes, GLIMMERWOOD, FULL, wet, land, usual);
        add(list, biomes, MUSHROOM_GROVE, Climate.Parameter.span(-2.0f, -0.2f), dry, land, usual);
        add(list, biomes, FAE_MEADOWS, Climate.Parameter.span(-0.2f, 2.0f), dry, land, usual);
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
        context.register(FAE_MEADOWS, biome(new Look(0x7A5AB8, 0xF2A27C, 0xA6F0BE, 0xF6A6C8, 0x4FA8D8), ModParticles.FAE_SPARK.get(), 0.0025f,
                SoundEvents.MUSIC_BIOME_CHERRY_GROVE, FaeFeatures.meadows(placed, carvers), FaeSpawns.meadows()));
        context.register(GLIMMERWOOD, biome(new Look(0x6E4FB0, 0xE89A88, 0x8EE6BC, 0xF0A0D0, 0x3E9ACC), ModParticles.FAE_SPARK.get(), 0.006f,
                SoundEvents.MUSIC_BIOME_CHERRY_GROVE, FaeFeatures.glimmerwood(placed, carvers), FaeSpawns.glimmerwood()));
        context.register(MUSHROOM_GROVE, biome(new Look(0x5E4AA8, 0xD890A8, 0x96E2CC, 0xC890E8, 0x4A88D0), ModParticles.FAE_SPARK.get(),
                0.009f, SoundEvents.MUSIC_BIOME_LUSH_CAVES, FaeFeatures.mushroomGrove(placed, carvers), FaeSpawns.mushroomGrove()));
        context.register(MOONLIT_LAKES, biome(new Look(0x6A5AC0, 0xF0A890, 0xA2EEC6, 0xE8A8D8, 0x5AB4E8), ModParticles.FAE_SPARK.get(), 0.0035f,
                SoundEvents.MUSIC_BIOME_MEADOW, FaeFeatures.moonlitLakes(placed, carvers), FaeSpawns.moonlitLakes()));
        context.register(THORNWILD, biome(new Look(0x4A3478, 0xB0708A, 0x78B8A0, 0x9A70B8, 0x3A6A9A), ParticleTypes.WHITE_ASH, 0.004f,
                SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA, FaeFeatures.thornwild(placed, carvers), FaeSpawns.thornwild()));
    }

    /** Farben eines Bioms: Himmel (Zenit), Dunst am Horizont, Gras, Laub (für getöntes Laub) und Wasser. */
    private record Look(int sky, int fog, int grass, int foliage, int water) {}

    private static Biome biome(Look look, ParticleOptions particle, float density, Holder<SoundEvent> music,
            BiomeGenerationSettings generation, MobSpawnSettings spawns) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.7f)
                .downfall(0.6f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(look.water())
                        .grassColorOverride(look.grass())
                        .foliageColorOverride(look.foliage())
                        .dryFoliageColorOverride(look.foliage())
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(look.sky()))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(look.fog()))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(0x2A2A6A))
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
