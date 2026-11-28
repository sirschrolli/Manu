package io.github.sirschrolli.kingdomsatwar.urmeer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Das Urmeer als Dimension (seit 0.89): ein Meer ohne Küsten unter einem hohen, klaren Himmel, mit Tag und Nacht wie in
 * der Oberwelt. Über dem Wasser Seeluft in hellem Türkis, unter Wasser ein tiefes, kühles Blau, in den Gräben fast
 * Schwarz. Betreten wird es durch das Gezeitentor im Versunkenen Tempel.
 */
public final class UrmeerDimension {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id("urmeer"));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, KingdomsAtWar.id("urmeer"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, LEVEL.identifier());

    private UrmeerDimension() {}

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x4A8EC8))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0x9CCAD8))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0xE6F4FAFF))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 178.0f)
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_UNDER_WATER))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)), java.util.List.of()))
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                .build();
        context.register(TYPE, new DimensionType(
                false,     // Tag und Nacht
                true,      // Himmelslicht
                false,     // keine Decke
                false,     // kein Enderdrache
                1.0,
                UrmeerTerrain.MIN_Y, UrmeerTerrain.HEIGHT, UrmeerTerrain.HEIGHT,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.0f,
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
                MultiNoiseBiomeSource.createFromList(UrmeerBiomes.parameters(biomes)), settings.getOrThrow(UrmeerTerrain.SETTINGS))));
    }
}
