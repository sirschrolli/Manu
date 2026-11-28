package io.github.sirschrolli.kingdomsatwar.worldgen.throne;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

/**
 * Platzierung eines einzigartigen Bauwerks (des Throns der Alten in der Leere, des Schlunds der Welten im Abgrund): genau
 * einmal je Welt, in einem Ring um die Mitte ({@code min_distance} bis {@code max_distance} Blöcke). Es schwebt frei – das
 * Gelände spielt keine Rolle; den Ort berechnet {@link ThroneLocator} allein aus dem Startwert der Welt.
 *
 * <p>Sie gibt sich als „zufällig verteilte“ Platzierung aus (deren Raster der Befehl {@code /locate} absucht), schlägt aber
 * in jeder Rasterzelle denselben, festen Chunk vor – so findet {@code /locate structure} das Bauwerk sofort.
 */
public class ThronePlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<ThronePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(0, 100000).fieldOf("min_distance").forGetter(ThronePlacement::minDistance),
            Codec.intRange(0, 100000).fieldOf("max_distance").forGetter(ThronePlacement::maxDistance),
            Codec.INT.fieldOf("salt").forGetter(ThronePlacement::salt)
    ).apply(instance, ThronePlacement::new));

    private final int minDistance;
    private final int maxDistance;
    private final int ringSalt;

    public ThronePlacement(int minDistance, int maxDistance, int salt) {
        // Raster und Abstand spielen keine Rolle (jede Zelle schlägt denselben Chunk vor); /locate zeigt auf die Chunkmitte
        super(new Vec3i(8, 0, 8), FrequencyReductionMethod.DEFAULT, 1.0f, salt, Optional.empty(), 64, 16, RandomSpreadType.LINEAR);
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.ringSalt = salt;
    }

    public int minDistance() {
        return this.minDistance;
    }

    public int maxDistance() {
        return this.maxDistance;
    }

    @Override
    public int salt() {
        return this.ringSalt;
    }

    /** In jeder Rasterzelle derselbe Chunk: der feste Ort des Bauwerks in dieser Welt. */
    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int sourceX, int sourceZ) {
        return ThroneLocator.chunk(seed, this);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        ChunkPos chunk = ThroneLocator.chunk(state.getLevelSeed(), this);
        return chunk.x() == chunkX && chunk.z() == chunkZ;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
        return this.isPlacementChunk(state, chunkX, chunkZ);
    }

    /** Der eigene Codec (die Elternklasse verlangt ihren Typ; zur Laufzeit ist es derselbe Codec-Gegenstand). */
    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<RandomSpreadStructurePlacement> codec() {
        return (MapCodec<RandomSpreadStructurePlacement>) (MapCodec<?>) CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ThronePlacement p && p.minDistance == this.minDistance && p.maxDistance == this.maxDistance
                && p.ringSalt == this.ringSalt;
    }

    @Override
    public int hashCode() {
        return (this.minDistance * 31 + this.maxDistance) * 31 + this.ringSalt;
    }
}
