package io.github.sirschrolli.kingdomsatwar.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
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
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Das Feenreich als Dimension: ewiges Abendrot. Die Sonne steht für immer knapp über dem Horizont und taucht alles in
 * Gold und Rosa, über ihr verdämmert der Himmel zu Violett, in dem schon die ersten Sterne stehen, und im Osten hängt groß
 * der Vollmond. Rosa Wolken, warmes Licht, ein Hauch von Glitzern in der Luft. Die Zeit steht still – schlafen kann man
 * hier nicht (es wird nie Nacht), aber den Wiedereinstiegspunkt setzen schon.
 */
public final class FaeDimension {
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, KingdomsAtWar.id("fae_realm"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, FaeGate.FAE_REALM.identifier());

    /** Wo die Sonne steht: knapp über dem Horizont (90 = Horizont). */
    public static final float SUN_ANGLE = 83.0f;

    private FaeDimension() {}

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder()
                .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0x7A5AB8))
                .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(0xF2A27C))
                .set(EnvironmentAttributes.FOG_START_DISTANCE, 36.0f)
                .set(EnvironmentAttributes.FOG_END_DISTANCE, 240.0f)
                // die Abendröte am Horizont: kräftiges Orange-Rosa
                .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ARGB.vector4fFromARGB32(0xE6FF8A4C))
                .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0xE6FFC2CC))
                .set(EnvironmentAttributes.CLOUD_HEIGHT, 182.0f)
                .set(EnvironmentAttributes.SUN_ANGLE, SUN_ANGLE)
                // der Vollmond steht hoch im Osten, die ersten Sterne sind schon da
                .set(EnvironmentAttributes.MOON_ANGLE, 305.0f)
                .set(EnvironmentAttributes.MOON_PHASE, MoonPhase.FULL_MOON)
                .set(EnvironmentAttributes.STAR_ANGLE, 25.0f)
                .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.28f)
                // warmes, goldenes Abendlicht – nicht ganz taghell
                .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 13.0f)
                .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.9f)
                .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(0xFFC8A8))
                .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(0x34224C))
                .set(EnvironmentAttributes.AMBIENT_PARTICLES, List.of(new AmbientParticle(ModParticles.FAE_SPARK.get(), 0.002f)))
                .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(SoundEvents.MUSIC_BIOME_CHERRY_GROVE))
                .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                        Optional.of(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0)), List.of()))
                .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.NEVER, BedRule.Rule.ALWAYS, false, false,
                        Optional.of(Component.translatable("block.kingdomsatwar.bed.fae_realm"))))
                .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                .set(EnvironmentAttributes.CAN_START_RAID, false)
                .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                .build();
        context.register(TYPE, new DimensionType(
                true,      // feste Zeit
                true,      // Himmelslicht
                false,     // keine Decke
                false,     // kein Enderdrache
                1.0,
                0, 256, 256,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.05f,
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
                MultiNoiseBiomeSource.createFromList(FaeBiomes.parameters(biomes)), settings.getOrThrow(FaeTerrain.SETTINGS))));
    }
}
