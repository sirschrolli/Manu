package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * Himmel, Licht und Klang der sechs Kampfdimensionen (siehe {@link BossArenaKind}). Jede ist eine leere Welt aus Luft – das
 * Abbild des Horts schwebt darin –, aber jede hat ihr eigenes Wetter aus Farben:
 *
 * <ul>
 *     <li>die Mondbühne: violette Nacht, der Vollmond hoch über der Bühne, ein rosa Nachglühen am Horizont, ein Meer aus
 *     fliederfarbenen Wolken tief unten, Feenfunken in der Luft, dazu die Spieluhr;</li>
 *     <li>die Glutesse: ein Himmel wie glimmende Kohle, am Horizont brennt es orange, Rauchwolken ziehen darüber, Asche rieselt;</li>
 *     <li>das Sternenzelt: tiefblaue Nacht voller Sterne, eine schmale Mondsichel, ein silbern leuchtendes Wolkenmeer, Sternenstaub –
 *     und die Musik des Drachenkampfs;</li>
 *     <li>der Fehlerraum: fast schwarz, Dunst in Magenta, der Mond steht senkrecht oben, türkise Pixelwolken, Bildrauschen in der
 *     Luft;</li>
 *     <li>die Thronwelt: violettes Dunkel unter einem Himmel aus kalten Sternen, der volle Mond über dem Thron;</li>
 *     <li>der Weltenschlund: schwarzrot, Glutasche in der Luft – darüber flammen die Risse des Abgrunds (eigener Himmel).</li>
 * </ul>
 */
public final class BossArenaDimensions {
    private BossArenaDimensions() {}

    /** Farben und Klang einer Kampfdimension. */
    private record Look(int sky, int fog, float fogStart, float fogEnd, int sunset, float sunAngle, float moonAngle, MoonPhase moon,
            float starAngle, float stars, int clouds, float cloudHeight, float skyLight, float skyFactor, int skyLightColor, int ambient,
            List<AmbientParticle> particles, Holder<SoundEvent> music, Holder<SoundEvent> mood, int minY, int height,
            TagKey<Block> infiniburn, float ambientLight, int grass, int foliage, int water) {}

    private static Look look(BossArenaKind kind) {
        return switch (kind) {
            case MYSTIK -> new Look(0x2A1F5C, 0x6A4A9A, 48.0f, 260.0f, 0xC0FF7AB8, 100.0f, 340.0f, MoonPhase.FULL_MOON,
                    20.0f, 0.8f, 0xE0F0A8E0, 34.0f, 10.0f, 0.85f, 0xC8B8FF, 0x3A2A5C,
                    List.of(new AmbientParticle(ModParticles.FAE_SPARK.get(), 0.004f)), SoundEvents.MUSIC_DISC_CREATOR_MUSIC_BOX,
                    SoundEvents.AMBIENT_CAVE, 0, 256, BlockTags.INFINIBURN_OVERWORLD, 0.08f, 0xA6F0BE, 0xF6A6C8, 0x4FA8D8);
            case MALLEUS -> new Look(0x3A0A04, 0x7A2208, 30.0f, 200.0f, 0xE6FF6A10, 96.0f, 180.0f, MoonPhase.NEW_MOON,
                    0.0f, 0.0f, 0xD0301810, 150.0f, 8.0f, 0.75f, 0xFF9A6A, 0x4A1A0A,
                    List.of(new AmbientParticle(ParticleTypes.ASH, 0.008f), new AmbientParticle(ParticleTypes.WHITE_ASH, 0.002f)),
                    SoundEvents.MUSIC_BIOME_CRIMSON_FOREST, SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 0, 256, BlockTags.INFINIBURN_NETHER,
                    0.12f, 0xBFB755, 0xAEA42A, 0x3F76E4);
            case ASTERIA -> new Look(0x070B24, 0x1A2450, 60.0f, 300.0f, 0x00000000, 180.0f, 45.0f, MoonPhase.WAXING_CRESCENT,
                    60.0f, 1.0f, 0xB0A8C8FF, 20.0f, 11.0f, 0.9f, 0xB8D0FF, 0x22305A,
                    List.of(new AmbientParticle(ParticleTypes.END_ROD, 0.0015f)), SoundEvents.MUSIC_DRAGON, SoundEvents.AMBIENT_CAVE,
                    0, 256, BlockTags.INFINIBURN_END, 0.1f, 0x8EB971, 0x71A74D, 0x6A7AE0);
            case ANNI -> new Look(0x0C0014, 0x2A0A3A, 40.0f, 220.0f, 0x00000000, 180.0f, 0.0f, MoonPhase.FULL_MOON,
                    33.0f, 1.0f, 0xC820E8D8, 40.0f, 12.0f, 0.9f, 0xE8C8FF, 0x301040,
                    List.of(new AmbientParticle(ModParticles.GLITCH_PIXEL.get(), 0.006f)), SoundEvents.MUSIC_DISC_13,
                    SoundEvents.AMBIENT_CAVE, -64, 384, BlockTags.INFINIBURN_OVERWORLD, 0.1f, 0x91BD59, 0x77AB2F, 0x3F76E4);
            case KING -> new Look(0x0B0618, 0x2A1A40, 40.0f, 260.0f, 0x00000000, 180.0f, 10.0f, MoonPhase.FULL_MOON,
                    35.0f, 1.0f, 0x00000000, 1000.0f, 8.0f, 0.7f, 0xC8B4FF, 0x2A2050,
                    List.of(new AmbientParticle(ParticleTypes.END_ROD, 0.001f), new AmbientParticle(ParticleTypes.REVERSE_PORTAL, 0.002f)),
                    SoundEvents.MUSIC_BIOME_DEEP_DARK, SoundEvents.AMBIENT_CAVE, -64, 384, BlockTags.INFINIBURN_END, 0.08f,
                    0x8EB971, 0x71A74D, 0x6A4AC8);
            case WORLD_EATER -> new Look(0x12060A, 0x3A1012, 30.0f, 240.0f, 0x00000000, 180.0f, 180.0f, MoonPhase.NEW_MOON,
                    70.0f, 0.3f, 0x00000000, 1000.0f, 7.0f, 0.65f, 0xFFB4A8, 0x3A1420,
                    List.of(new AmbientParticle(ParticleTypes.ASH, 0.005f), new AmbientParticle(ParticleTypes.LAVA, 0.0002f)),
                    SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY, SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, -64, 384, BlockTags.INFINIBURN_END,
                    0.1f, 0x8EB971, 0x71A74D, 0x8A3A2A);
        };
    }

    public static void bootstrapTypes(BootstrapContext<DimensionType> context) {
        HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
        for (BossArenaKind kind : BossArenaKind.values()) {
            Look l = look(kind);
            EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder()
                    .set(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(l.sky()))
                    .set(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(l.fog()))
                    .set(EnvironmentAttributes.FOG_START_DISTANCE, l.fogStart())
                    .set(EnvironmentAttributes.FOG_END_DISTANCE, l.fogEnd())
                    .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(l.clouds()))
                    .set(EnvironmentAttributes.CLOUD_HEIGHT, l.cloudHeight())
                    .set(EnvironmentAttributes.SUN_ANGLE, l.sunAngle())
                    .set(EnvironmentAttributes.MOON_ANGLE, l.moonAngle())
                    .set(EnvironmentAttributes.MOON_PHASE, l.moon())
                    .set(EnvironmentAttributes.STAR_ANGLE, l.starAngle())
                    .set(EnvironmentAttributes.STAR_BRIGHTNESS, l.stars())
                    .set(EnvironmentAttributes.SKY_LIGHT_LEVEL, l.skyLight())
                    .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, l.skyFactor())
                    .set(EnvironmentAttributes.SKY_LIGHT_COLOR, ARGB.vector3fFromRGB24(l.skyLightColor()))
                    .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, ARGB.vector3fFromRGB24(l.ambient()))
                    .set(EnvironmentAttributes.AMBIENT_PARTICLES, l.particles())
                    .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(l.music()))
                    .set(EnvironmentAttributes.AMBIENT_SOUNDS, new AmbientSounds(Optional.empty(),
                            Optional.of(new AmbientMoodSettings(l.mood(), 6000, 8, 2.0)), List.of()))
                    .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.NEVER, BedRule.Rule.NEVER, false, false,
                            Optional.of(Component.translatable("block.kingdomsatwar.bed.boss_arena"))))
                    .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                    .set(EnvironmentAttributes.CAN_START_RAID, false)
                    .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false);
            if ((l.sunset() >>> 24) != 0) {
                attributes.set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ARGB.vector4fFromARGB32(l.sunset()));
            }
            if (kind == BossArenaKind.MALLEUS) {
                attributes.set(EnvironmentAttributes.FAST_LAVA, true).set(EnvironmentAttributes.WATER_EVAPORATES, true);
            }
            context.register(kind.type(), new DimensionType(
                    true,      // feste Zeit
                    true,      // Himmelslicht
                    false,     // keine Decke
                    false,     // kein Enderdrache
                    1.0,
                    l.minY(), l.height(), l.height(),
                    blocks.getOrThrow(l.infiniburn()),
                    l.ambientLight(),
                    new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                    DimensionType.Skybox.OVERWORLD,
                    CardinalLighting.Type.DEFAULT,
                    attributes.build(),
                    HolderSet.empty(),
                    Optional.empty()));
        }
    }

    public static void bootstrapStems(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        for (BossArenaKind kind : BossArenaKind.values()) {
            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.of(HolderSet.<StructureSet>direct()),
                    biomes.getOrThrow(kind.biome()), List.of());
            settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.AIR));
            settings.updateLayers();
            context.register(kind.stem(), new LevelStem(types.getOrThrow(kind.type()), new FlatLevelSource(settings)));
        }
    }

    /** Die Biome der Kampfdimensionen: leer, ohne Wesen, ohne Wetter – Gras und Laub in den Farben des Horts. */
    public static void bootstrapBiomes(BootstrapContext<Biome> context) {
        for (BossArenaKind kind : BossArenaKind.values()) {
            Look l = look(kind);
            context.register(kind.biome(), new Biome.BiomeBuilder()
                    .hasPrecipitation(false)
                    .temperature(0.8f)
                    .downfall(0.4f)
                    .specialEffects(new BiomeSpecialEffects.Builder()
                            .waterColor(l.water())
                            .grassColorOverride(l.grass())
                            .foliageColorOverride(l.foliage())
                            .dryFoliageColorOverride(l.foliage())
                            .build())
                    .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(l.sky()))
                    .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(l.fog()))
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .build());
        }
    }

    /** Für die Vorführung: die Partikel, die in dieser Kampfdimension in der Luft liegen. */
    public static List<ParticleOptions> particles(BossArenaKind kind) {
        return look(kind).particles().stream().map(AmbientParticle::particle).toList();
    }
}
