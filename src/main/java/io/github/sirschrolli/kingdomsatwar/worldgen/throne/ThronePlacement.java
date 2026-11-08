package io.github.sirschrolli.kingdomsatwar.worldgen.throne;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

/**
 * Platzierung des Throns der Alten: genau einmal in der Leere, in einem Ring um die Mitte der Welt
 * ({@code min_distance} bis {@code max_distance} Blöcke). Er schwebt frei – das Gelände spielt keine Rolle; den Ort
 * berechnet {@link ThroneLocator} allein aus dem Startwert der Welt.
 */
public record ThronePlacement(int minDistance, int maxDistance, int salt) implements StructurePlacement {
    public static final MapCodec<ThronePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(ThronePlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(ThronePlacement::maxDistance),
            Codec.INT.fieldOf("salt").forGetter(ThronePlacement::salt)
    ).apply(instance, ThronePlacement::new));

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos throne = ThroneLocator.chunk(state.getLevelSeed(), this);
        return throne.x() == chunkX && throne.z() == chunkZ;
    }

    @Override
    public MapCodec<ThronePlacement> codec() {
        return CODEC;
    }
}
