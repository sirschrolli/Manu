package io.github.sirschrolli.kingdomsatwar.arena;

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
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/**
 * Die Arkane Sphäre: die Dimension, in die ein Erzmagier seine Gegner zieht. Ein endloser violetter Abgrund unter
 * kalten Sternen, ein Wolkenmeer tief unten, schwebende Felsen ringsum – und in der Mitte die Arena im Element des
 * Magiers. Hier lässt sich nichts bauen und nichts abbauen; wer hier stirbt, verliert alles, was er bei sich trägt.
 */
public final class ArcaneSphere {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id("arcane_sphere"));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, KingdomsAtWar.id("arcane_sphere"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, LEVEL.identifier());
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, KingdomsAtWar.id("arcane_sphere"));

    /** Höhe des Arenabodens. */
    public static final int FLOOR_Y = 100;
    /** Abstand der Arenen voneinander (jeder Kampf bekommt eine eigene). */
    public static final int SPACING = 1024;

    private static final int SKY = 0x120A26;
    private static final int FOG = 0x2A1A4C;

    private ArcaneSphere() {}

    public static boolean is(Level level) {
        return level.dimension() == LEVEL;
    }

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(SKY))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(FOG))
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 28.0f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 150.0f)
                // ein Wolkenmeer tief unter der Arena
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0xB06A4AA0))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, (float) (FLOOR_Y - 42))
                // keine Sonne, kein Mond, nur kalte Sterne
                .set(EnvironmentAttributes.SUN_ANGLE, 180.0f)
                .set(EnvironmentAttributes.MOON_PHASE, MoonPhase.NEW_MOON)
                .set(EnvironmentAttributes.STAR_ANGLE, 23.0f)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 1.0f)
                // fahles, violettes Licht aus dem Nichts
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 11.0f)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.9f)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0xD8C8FF))
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x3C2E66))
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(ParticleTypes.END_ROD, 0.0008f)))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_END))
                .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.NEVER, BedRule.Rule.NEVER, false, false,
                        Optional.of(Component.translatable("block.kingdomsatwar.bed.arcane_sphere"))))
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                .build();
        context.register(TYPE, new DimensionType(
                true,      // feste Zeit
                true,      // Himmelslicht (fahl und violett)
                false,     // keine Decke
                false,     // kein Enderdrache
                1.0,
                0, 256, 256,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.12f,     // nie ganz dunkel
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.OVERWORLD,
                CardinalLighting.Type.DEFAULT,
                attributes,
                HolderSet.empty(),
                Optional.empty()));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.of(HolderSet.<net.minecraft.world.level.levelgen.structure.StructureSet>direct()),
                biomes.getOrThrow(BIOME), List.of());
        settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.AIR));
        settings.updateLayers();
        context.register(STEM, new LevelStem(types.getOrThrow(TYPE), new FlatLevelSource(settings)));
    }

    /** Das Biom der Sphäre: leer, ohne Wesen, ohne Wetter, in den Farben des Abgrunds. */
    public static Biome biome() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(0x6A4AC8).build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(SKY))
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(FOG))
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }
}
