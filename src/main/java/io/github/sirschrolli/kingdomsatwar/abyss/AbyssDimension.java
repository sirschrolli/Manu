package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
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
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Der Abgrund als Dimension: ein Himmel aus tiefem Karmesin und Tinte, ohne Sonne und Mond, nur ein paar verlorene Sterne;
 * roter Dunst in der Ferne, gedämpftes, rötliches Licht, und immer rieselt Asche. Die Zeit steht still. Schlafen kann man
 * hier nicht. Die Risse am Himmel sind echte Risse in der Welt (siehe AbyssFeatures, Himmelsrisse).
 */
public final class AbyssDimension {
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, KingdomsAtWar.id("the_abyss"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, AbyssGate.THE_ABYSS.identifier());

    private AbyssDimension() {}

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x1A0610))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x2E0A18))
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 30.0f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 240.0f)
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0x00000000))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 1000.0f)
                // weder Sonne noch Mond – nur ein paar verlorene Sterne
                .set(EnvironmentAttributes.SUN_ANGLE, 180.0f)
                .set(EnvironmentAttributes.MOON_ANGLE, 180.0f)
                .set(EnvironmentAttributes.MOON_PHASE, net.minecraft.world.level.MoonPhase.NEW_MOON)
                .set(EnvironmentAttributes.STAR_ANGLE, 70.0f)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.3f)
                // gedämpftes, rötliches Licht
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 7.0f)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.65f)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0xFFB4A8))
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x3A1420))
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(ParticleTypes.ASH, 0.004f)))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0)), List.of()))
                .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.NEVER, BedRule.Rule.NEVER, false, false,
                        Optional.of(Component.translatable("block.kingdomsatwar.bed.the_abyss"))))
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                .build();
        context.register(TYPE, new DimensionType(
                true,      // feste Zeit
                true,      // Himmelslicht (schwach)
                false,     // keine Decke
                false,     // kein Enderdrache
                1.0,
                -64, 384, 384,
                blocks.getOrThrow(BlockTags.INFINIBURN_END),
                0.1f,      // nie ganz dunkel
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
                MultiNoiseBiomeSource.createFromList(AbyssBiomes.parameters(biomes)), settings.getOrThrow(AbyssTerrain.SETTINGS))));
    }
}
