package io.github.sirschrolli.kingdomsatwar.abyss.hoard;

import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.jspecify.annotations.Nullable;

/** Wo der Schlund der Welten schwebt – allein aus dem Startwert der Welt berechnet (wie der Thron der Alten). */
public final class HoardLocator {
    private HoardLocator() {}

    /** Chunk des Horts in dieser Welt (null, wenn sie keinen hat). */
    public static @Nullable ChunkPos find(ServerLevel level) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        for (Holder<StructureSet> set : state.possibleStructureSets()) {
            if (set.is(ModStructures.WORLD_EATER_HOARD_SET) && set.value().placement() instanceof ThronePlacement p) {
                return ThroneLocator.chunk(state.getLevelSeed(), p);
            }
        }
        return null;
    }

    /** Mitte des Rings (ein Block über dem Boden), null ohne Hort. */
    public static @Nullable BlockPos center(ServerLevel level) {
        ChunkPos chunk = find(level);
        return chunk == null ? null : new BlockPos(chunk.getMiddleBlockX(), HoardPiece.FLOOR + 1, chunk.getMiddleBlockZ());
    }
}
