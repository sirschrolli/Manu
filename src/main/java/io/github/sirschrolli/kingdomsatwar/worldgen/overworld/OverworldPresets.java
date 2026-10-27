package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * Die Weltvoreinstellungen „Standard“, „Große Biome“ und „Zerklüftet“ mit der Biomverteilung von Kingdoms at War
 * ({@link KingdomBiomeSource}): gleiches Gelände, gleicher Nether und gleiches Ende wie bei Vanilla, aber mit den
 * eigenen Biomen. Sie ersetzen die gleichnamigen Vanilla-Voreinstellungen (Namensraum {@code minecraft}), damit jede
 * neue Welt – im Einzelspieler wie auf dem Server – die eigenen Biome hat. Bestehende Welten behalten ihre Erzeugung.
 */
public final class OverworldPresets {
    private OverworldPresets() {}

    public static void bootstrap(BootstrapContext<WorldPreset> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<MultiNoiseBiomeSourceParameterList> parameterLists = context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        LevelStem nether = new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER), new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromPreset(parameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)),
                noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER)));
        LevelStem end = new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                new NoiseBasedChunkGenerator(TheEndBiomeSource.create(biomes), noiseSettings.getOrThrow(NoiseGeneratorSettings.END)));

        register(context, WorldPresets.NORMAL, dimensionTypes, noiseSettings, biomes, NoiseGeneratorSettings.OVERWORLD, nether, end);
        register(context, WorldPresets.LARGE_BIOMES, dimensionTypes, noiseSettings, biomes, NoiseGeneratorSettings.LARGE_BIOMES, nether, end);
        register(context, WorldPresets.AMPLIFIED, dimensionTypes, noiseSettings, biomes, NoiseGeneratorSettings.AMPLIFIED, nether, end);
    }

    private static void register(BootstrapContext<WorldPreset> context, ResourceKey<WorldPreset> key, HolderGetter<DimensionType> dimensionTypes,
            HolderGetter<NoiseGeneratorSettings> noiseSettings, HolderGetter<Biome> biomes, ResourceKey<NoiseGeneratorSettings> settings,
            LevelStem nether, LevelStem end) {
        LevelStem overworld = new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                new NoiseBasedChunkGenerator(new KingdomBiomeSource(biomes), noiseSettings.getOrThrow(settings)));
        context.register(key, new WorldPreset(Map.of(LevelStem.OVERWORLD, overworld, LevelStem.NETHER, nether, LevelStem.END, end)));
    }
}
