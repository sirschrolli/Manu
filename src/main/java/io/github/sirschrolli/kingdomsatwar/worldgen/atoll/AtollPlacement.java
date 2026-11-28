package io.github.sirschrolli.kingdomsatwar.worldgen.atoll;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Platzierung des Versunkenen Atolls: genau einmal in der Oberwelt, in einem Meer ({@code biomes}) in einem Ring um die
 * Mitte der Welt ({@code min_distance} bis {@code max_distance} Blöcke). Den Ort berechnet {@link AtollLocator} aus dem
 * Startwert der Welt – fern der Drachennester.
 */
public record AtollPlacement(int minDistance, int maxDistance, HolderSet<Biome> biomes, int salt) implements StructurePlacement {
    public static final MapCodec<AtollPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(AtollPlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(AtollPlacement::maxDistance),
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(AtollPlacement::biomes),
            Codec.INT.fieldOf("salt").forGetter(AtollPlacement::salt)
    ).apply(instance, AtollPlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos atoll = AtollLocator.find(state, this);
        return atoll != null && atoll.x() == chunkX && atoll.z() == chunkZ;
    }

    @Override
    public MapCodec<AtollPlacement> codec() {
        return CODEC;
    }
}
