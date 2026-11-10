package io.github.sirschrolli.kingdomsatwar.abyss;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
 * Die Biome des Abgrunds – jedes ein Strudel, in dem Bruchstücke einer anderen Welt treiben, und mit ihnen ihre Wesen:
 * <ul>
 *     <li><b>Bruchlande</b> – Wiesen und Wälder der Oberwelt, schief und kopfüber, dazwischen Eis und Wüste; Untote;</li>
 *     <li><b>Glutsturz</b> – Netherrack und Karmesinnyzel, Lava stürzt von den Rändern ins Nichts; Glutgeister,
 *     Brandgebeine, Aschengeister;</li>
 *     <li><b>Sternentrümmer</b> – Endstein, Chorus und Sternenmarmor; Leerwandler, Rissjäger, Chorusschlünde;</li>
 *     <li><b>Schattenschlund</b> – Schattengras und Schattenbäume, Sternenranken hängen von kopfüber treibenden Inseln;
 *     Schattenpirscher und Nachtwidder;</li>
 *     <li><b>Die Naht</b> – alles durcheinander, durchzogen von glühenden Weltennähten; hier ist das Chaos am dichtesten.</li>
 * </ul>
 */
public final class AbyssBiomes {
    public static final ResourceKey<Biome> FRACTURED_LANDS = key("abyss_fractured_lands");
    public static final ResourceKey<Biome> EMBER_FALLS = key("abyss_ember_falls");
    public static final ResourceKey<Biome> STAR_WRECK = key("abyss_star_wreck");
    public static final ResourceKey<Biome> SHADOW_CHASM = key("abyss_shadow_chasm");
    public static final ResourceKey<Biome> THE_SEAM = key("abyss_seam");

    public static final List<ResourceKey<Biome>> ALL = List.of(FRACTURED_LANDS, EMBER_FALLS, STAR_WRECK, SHADOW_CHASM, THE_SEAM);

    private AbyssBiomes() {}

    private static ResourceKey<Biome> key(String name) {
        return ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(name));
    }

    private static final Climate.Parameter FULL = Climate.Parameter.span(-1.0f, 1.0f);

    /** Klimapunkte: Temperatur und Feuchte teilen die vier Welten auf, dazwischen (um die Mitte) liegt die Naht. */
    public static Climate.ParameterList<Holder<Biome>> parameters(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = new ArrayList<>();
        Climate.Parameter cold = Climate.Parameter.span(-2.0f, -0.12f);
        Climate.Parameter warm = Climate.Parameter.span(0.12f, 2.0f);
        Climate.Parameter mid = Climate.Parameter.span(-0.12f, 0.12f);
        Climate.Parameter dry = Climate.Parameter.span(-2.0f, 0.0f);
        Climate.Parameter wet = Climate.Parameter.span(0.0f, 2.0f);
        add(list, biomes, FRACTURED_LANDS, cold, wet);
        add(list, biomes, SHADOW_CHASM, cold, dry);
        add(list, biomes, EMBER_FALLS, warm, dry);
        add(list, biomes, STAR_WRECK, warm, wet);
        add(list, biomes, THE_SEAM, mid, FULL);
        return new Climate.ParameterList<>(list);
    }

    private static void add(List<Pair<Climate.ParameterPoint, Holder<Biome>>> list, HolderGetter<Biome> biomes, ResourceKey<Biome> biome,
            Climate.Parameter temperature, Climate.Parameter humidity) {
        list.add(Pair.of(Climate.parameters(temperature, humidity, FULL, FULL, FULL, FULL, 0.0f), biomes.getOrThrow(biome)));
    }

    // =====================================================================================
    // Die Biome
    // =====================================================================================

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<WorldCarver> carvers = context.lookup(Registries.CARVER);
        context.register(FRACTURED_LANDS, biome(0x1A0A12, 0x2C1018, 0x5A8A3A, 0x6A9A4A, 0x4A7ADA, ParticleTypes.ASH, 0.003f,
                SoundEvents.MUSIC_BIOME_BASALT_DELTAS, AbyssFeatures.fracturedLands(placed, carvers), new MobSpawnSettings.Builder()
                        .addSpawn(ModEntities.ZOMBIE.get(), 60, 1, 3)
                        .addSpawn(ModEntities.SKELETON.get(), 50, 1, 2)
                        .addSpawn(ModEntities.SPIDER.get(), 30, 1, 2)
                        .addSpawn(ModEntities.DISTORTLING.get(), 20, 1, 2)
                        .build()));
        context.register(EMBER_FALLS, biome(0x2A0808, 0x3A0C0A, 0x8A3A1A, 0x9A4A2A, 0x8A4A2A, ParticleTypes.LAVA, 0.0006f,
                SoundEvents.MUSIC_BIOME_CRIMSON_FOREST, AbyssFeatures.emberFalls(placed, carvers), new MobSpawnSettings.Builder()
                        .addSpawn(ModEntities.BLAZE.get(), 30, 1, 2)
                        .addSpawn(ModEntities.WITHER_SKELETON.get(), 30, 1, 2)
                        .addSpawn(ModEntities.ASH_WRAITH.get(), 30, 1, 2)
                        .addSpawn(ModEntities.GHAST.get(), 6, 1, 1)
                        .build()));
        context.register(STAR_WRECK, biome(0x140818, 0x24102C, 0x9A70C8, 0xAA80D8, 0x7A5AD8, ParticleTypes.REVERSE_PORTAL, 0.002f,
                SoundEvents.MUSIC_END, AbyssFeatures.starWreck(placed, carvers), new MobSpawnSettings.Builder()
                        .addSpawn(ModEntities.ENDERMAN.get(), 40, 1, 2)
                        .addSpawn(ModEntities.RIFT_HOUND.get(), 30, 2, 2)
                        .addSpawn(ModEntities.CHORUS_MAW.get(), 12, 1, 1)
                        .addSpawn(ModEntities.VOID_MAW.get(), 10, 1, 1)
                        .build()));
        context.register(SHADOW_CHASM, biome(0x0E0A18, 0x1A1028, 0x4A5A8A, 0x5A6A9A, 0x3A52B0, ParticleTypes.WHITE_ASH, 0.004f,
                SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY, AbyssFeatures.shadowChasm(placed, carvers), new MobSpawnSettings.Builder()
                        .addSpawn(ModEntities.SHADE_STALKER.get(), 40, 1, 2)
                        .addSpawn(ModEntities.NIGHT_RAM.get(), 20, 1, 2)
                        .addSpawn(ModEntities.SOUL_HERON.get(), 8, 1, 1)
                        .build()));
        context.register(THE_SEAM, biome(0x22060E, 0x3A0A16, 0x8A4A6A, 0x9A5A7A, 0x9A4A7A, ParticleTypes.ASH, 0.006f,
                SoundEvents.MUSIC_BIOME_BASALT_DELTAS, AbyssFeatures.seam(placed, carvers), new MobSpawnSettings.Builder()
                        .addSpawn(ModEntities.DISTORTLING.get(), 40, 1, 2)
                        .addSpawn(ModEntities.SHARD_WARDEN.get(), 15, 1, 1)
                        .addSpawn(ModEntities.VOID_MAW.get(), 15, 1, 1)
                        .addSpawn(ModEntities.RIFT_HOUND.get(), 20, 2, 2)
                        .addSpawn(ModEntities.SHADE_STALKER.get(), 20, 1, 1)
                        .build()));
    }

    /** {@code water}: Wasserfarbe – je Welt anders, damit die Wasserfälle ins Nichts als Wasser zu erkennen sind. */
    private static Biome biome(int sky, int fog, int grass, int foliage, int water, ParticleOptions particle, float density,
            Holder<SoundEvent> music, BiomeGenerationSettings generation, MobSpawnSettings spawns) {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.6f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(water)
                        .grassColorOverride(grass)
                        .foliageColorOverride(foliage)
                        .dryFoliageColorOverride(foliage)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(sky))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(fog))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(ARGB.scaleRGB(water, 0.35f)))
                .setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, 30.0f)
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, 240.0f)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(particle, density)))
                .setAttribute(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(music))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0)), List.of()))
                .mobSpawnSettings(spawns)
                .generationSettings(generation)
                .build();
    }
}
