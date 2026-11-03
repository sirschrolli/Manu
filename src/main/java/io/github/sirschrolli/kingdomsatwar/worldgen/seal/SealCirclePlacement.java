package io.github.sirschrolli.kingdomsatwar.worldgen.seal;

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
 * Platzierung des Siegelkreises: genau einmal pro Welt, auf ebenem Land in einem Ring um den Weltursprung
 * ({@code min_distance} bis {@code max_distance} Blöcke). Den Ort berechnet {@link SealCircleLocator}.
 */
public record SealCirclePlacement(int minDistance, int maxDistance, HolderSet<Biome> biomes, int salt) implements StructurePlacement {
    public static final MapCodec<SealCirclePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(SealCirclePlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(SealCirclePlacement::maxDistance),
            RegistryCodecs.holderSet(Registries.BIOME).fieldOf("biomes").forGetter(SealCirclePlacement::biomes),
            Codec.INT.fieldOf("salt").forGetter(SealCirclePlacement::salt)
    ).apply(instance, SealCirclePlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos circle = SealCircleLocator.find(state);
        return circle != null && circle.x() == chunkX && circle.z() == chunkZ;
    }

    @Override
    public MapCodec<SealCirclePlacement> codec() {
        return CODEC;
    }
}
