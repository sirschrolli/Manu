package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Die Schattenwelt als Dimension: ewiges Zwielicht unter einem violetten Himmel ohne Sonne, ein fahler Mond, der still
 * am Himmel steht, fremd geneigte Sternbilder, violetter Dunst und gedämpftes, lilafarbenes Licht. Es wird nie ganz hell
 * und nie ganz dunkel. Schlafen kann man hier nicht – aber ein Bett merkt sich, wo man erwacht.
 */
public final class ShadowDimension {
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, KingdomsAtWar.id("shadow_world"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, ShadowGate.SHADOW_WORLD.identifier());

    private ShadowDimension() {}

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x2E2252))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x46366E))
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 32.0f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 220.0f)
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0x9A3A2E58))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 300.0f)
                // keine Sonne, ein großer, stiller Mond tief über dem Land, fremd geneigte Sterne
                .set(EnvironmentAttributes.SUN_ANGLE, 180.0f)
                .set(EnvironmentAttributes.MOON_ANGLE, 300.0f)
                .set(EnvironmentAttributes.MOON_PHASE, net.minecraft.world.level.MoonPhase.FULL_MOON)
                .set(EnvironmentAttributes.STAR_ANGLE, 71.0f)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 1.0f)
                // helles, kühles Mondlicht mit einem Hauch Violett – hell genug, dass Felder reifen (Lichtstufe 10)
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 10.0f)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.86f)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0xD4CCFA))
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x3A3256))
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(ParticleTypes.WHITE_ASH, 0.0015f)))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)), List.of()))
                .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.NEVER, BedRule.Rule.ALWAYS, false, false,
                        Optional.of(Component.translatable("block.kingdomsatwar.bed.shadow_world"))))
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                .build();
        context.register(TYPE, new DimensionType(
                true,      // feste Zeit: ewiges Zwielicht
                true,      // Himmelslicht
                false,     // keine Decke
                false,     // kein Enderdrache
                1.0,       // Maßstab wie die Oberwelt
                -64, 384, 384,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.06f,     // nie ganz dunkel
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.OVERWORLD,
                CardinalLighting.Type.DEFAULT,
                attributes,
                HolderSet.empty(),
                Optional.empty()));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> settings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(STEM, new LevelStem(types.getOrThrow(TYPE), new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(ShadowBiomes.parameters(biomes)), settings.getOrThrow(ShadowTerrain.SETTINGS))));
    }
}
