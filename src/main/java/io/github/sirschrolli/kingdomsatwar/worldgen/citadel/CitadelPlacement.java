package io.github.sirschrolli.kingdomsatwar.worldgen.citadel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Platzierung der Schattenzitadelle: genau einmal in der Schattenwelt, auf weitem, festem Land in einem Ring um die
 * Mitte der Welt ({@code min_distance} bis {@code max_distance} Blöcke). Den Ort berechnet {@link CitadelLocator}.
 */
public record CitadelPlacement(int minDistance, int maxDistance, HolderSet<Biome> biomes, int salt) implements StructurePlacement {
    public static final MapCodec<CitadelPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(CitadelPlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(CitadelPlacement::maxDistance),
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(CitadelPlacement::biomes),
            Codec.INT.fieldOf("salt").forGetter(CitadelPlacement::salt)
    ).apply(instance, CitadelPlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos citadel = CitadelLocator.find(state, this);
        return citadel != null && citadel.x() == chunkX && citadel.z() == chunkZ;
    }

    @Override
    public MapCodec<CitadelPlacement> codec() {
        return CODEC;
    }
}
