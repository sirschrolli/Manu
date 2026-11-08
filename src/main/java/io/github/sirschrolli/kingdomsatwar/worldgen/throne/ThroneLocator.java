package io.github.sirschrolli.kingdomsatwar.worldgen.throne;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/** Wo der Thron der Alten in der Leere schwebt (nur aus dem Startwert der Welt, ohne Suche). */
public final class ThroneLocator {
    private ThroneLocator() {}

    /** Chunk des Throns. */
    public static ChunkPos chunk(long seed, ThronePlacement placement) {
        RandomSource random = RandomSource.create(seed ^ 0x7A12_0E5EL ^ placement.salt());
        double angle = random.nextDouble() * Mth.TWO_PI;
        double distance = placement.minDistance() + random.nextDouble() * (placement.maxDistance() - placement.minDistance());
        int x = (int) Math.round(Math.cos(angle) * distance);
        int z = (int) Math.round(Math.sin(angle) * distance);
        return new ChunkPos(x >> 4, z >> 4);
    }

    /** Chunk des Throns in dieser Welt (null, wenn sie keinen Thron hat). */
    public static @Nullable ChunkPos find(ServerLevel level) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.value().placement() instanceof ThronePlacement p) {
                return chunk(state.getLevelSeed(), p);
            }
        }
        return null;
    }

    /** Lage des Thronsiegels (unter dem Thron) zu einer Mitte aus {@link #center}. */
    public static BlockPos seal(BlockPos center) {
        return new BlockPos(center.getX(), ThronePiece.FLOOR - 3, center.getZ() + ThronePiece.THRONE_Z);
    }

    /** Mitte des Thronplatzes (ein Block über dem Boden), null ohne Thron. */
    public static @Nullable BlockPos center(ServerLevel level) {
        ChunkPos chunk = find(level);
        return chunk == null ? null : new BlockPos(chunk.getMiddleBlockX(), ThronePiece.FLOOR + 1, chunk.getMiddleBlockZ());
    }
}
