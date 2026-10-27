package io.github.sirschrolli.kingdomsatwar.worldgen.overworld;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;

/**
 * Die Biomverteilung der Oberwelt von Kingdoms at War: das Vanilla-Klima, in dem an bestimmten Stellen eigene Biome
 * liegen ({@link OverworldRegions}). Wie bei Vanilla werden die sechs Klimagrößen für einen ganzen Chunk auf einmal
 * berechnet – die eigenen Biome kosten beim Erzeugen der Welt keine Zeit.
 *
 * <p>Die Weltvoreinstellungen „Standard“, „Große Biome“ und „Zerklüftet“ verwenden diese Verteilung
 * ({@code data/minecraft/worldgen/world_preset}); Welten, die vorher erzeugt wurden, behalten ihre.
 */
public final class KingdomBiomeSource extends BiomeSource {
    public static final MapCodec<KingdomBiomeSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RegistryOps.retrieveGetter(Registries.BIOME)
    ).apply(i, KingdomBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;

    public KingdomBiomeSource(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> list = new ArrayList<>();
        for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry : OverworldRegions.parameters()) {
            list.add(Pair.of(entry.getFirst(), biomes.getOrThrow(entry.getSecond())));
        }
        this.parameters = new Climate.ParameterList<>(list);
    }

    @Override
    protected MapCodec<KingdomBiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.parameters.values().stream().map(Pair::getSecond);
    }

    /** Das Biom an einem Klimapunkt. */
    public Holder<Biome> biomeAt(Climate.TargetPoint target) {
        return this.parameters.findValue(target);
    }

    @Override
    public void addDebugInfo(List<String> result, net.minecraft.core.BlockPos feetPos, Climate.Sampler sampler) {
        // wie bei Vanilla: die Klimagrößen an der Fußposition (F3)
        Climate.TargetPoint target = sampler.sample(QuartPos.fromBlock(feetPos.getX()), QuartPos.fromBlock(feetPos.getY()),
                QuartPos.fromBlock(feetPos.getZ()));
        float weirdness = Climate.unquantizeCoord(target.weirdness());
        net.minecraft.world.level.biome.OverworldBiomeBuilder builder = new net.minecraft.world.level.biome.OverworldBiomeBuilder();
        result.add("Biome builder PV: "
                + net.minecraft.world.level.biome.OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(
                        net.minecraft.world.level.levelgen.NoiseRouterData.peaksAndValleys(weirdness))
                + " C: " + builder.getDebugStringForContinentalness(Climate.unquantizeCoord(target.continentalness()))
                + " E: " + builder.getDebugStringForErosion(Climate.unquantizeCoord(target.erosion()))
                + " T: " + builder.getDebugStringForTemperature(Climate.unquantizeCoord(target.temperature()))
                + " H: " + builder.getDebugStringForHumidity(Climate.unquantizeCoord(target.humidity())));
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return (quartX, quartY, quartZ) -> this.biomeAt(sampler.sample(quartX, quartY, quartZ));
    }

    @Override
    public BiomeResolver createResolverForChunk(Climate.Sampler sampler, int minQuartX, int minQuartY, int minQuartZ, int quartSizeX, int quartSizeY,
            int quartSizeZ) {
        DensityVolume volume = new DensityVolume(quartSizeX, quartSizeY, quartSizeZ, QuartPos.toBlock(minQuartX), QuartPos.toBlock(minQuartY),
                QuartPos.toBlock(minQuartZ), 4, 4, 4);
        DensityBuffer temperature = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer humidity = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer continentalness = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer erosion = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer depth = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer weirdness = DensityBuffer.createUnpooled(volume.size());
        sampler.temperature().sampleVolume(temperature, volume);
        sampler.humidity().sampleVolume(humidity, volume);
        sampler.continentalness().sampleVolume(continentalness, volume);
        sampler.erosion().sampleVolume(erosion, volume);
        sampler.depth().sampleVolume(depth, volume);
        sampler.weirdness().sampleVolume(weirdness, volume);
        return (quartX, quartY, quartZ) -> {
            int index = volume.indexUnchecked(quartX - minQuartX, quartY - minQuartY, quartZ - minQuartZ);
            return this.biomeAt(Climate.target(temperature.get(index), humidity.get(index), continentalness.get(index), erosion.get(index),
                    depth.get(index), weirdness.get(index)));
        };
    }
}
