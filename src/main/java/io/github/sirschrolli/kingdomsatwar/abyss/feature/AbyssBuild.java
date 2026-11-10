package io.github.sirschrolli.kingdomsatwar.abyss.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bauhilfen für die Features des Abgrunds: nur innerhalb der Chunks schreiben, die ein Feature verändern darf (der eigene
 * und die acht Nachbarn), Luft und Pflanzen ersetzen, festen Fels in Ruhe lassen.
 */
final class AbyssBuild {
    private AbyssBuild() {}

    /** Darf an dieser Stelle geschrieben werden (eigener Chunk oder ein Nachbar)? */
    static boolean inReach(BlockPos origin, BlockPos pos) {
        return Math.abs(SectionPos.blockToSectionCoord(pos.getX()) - SectionPos.blockToSectionCoord(origin.getX())) <= 1
                && Math.abs(SectionPos.blockToSectionCoord(pos.getZ()) - SectionPos.blockToSectionCoord(origin.getZ())) <= 1
                && pos.getY() > -60 && pos.getY() < 318;
    }

    /** Ist hier Platz (Luft, Pflanze, Laub, Schnee …)? */
    static boolean open(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(BlockTags.REPLACEABLE);
    }

    /** Setzt den Block, wenn hier Platz ist und die Stelle erreichbar ist. */
    static boolean place(WorldGenLevel level, BlockPos origin, BlockPos pos, BlockState state) {
        if (!inReach(origin, pos) || !open(level, pos)) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    /** Ein billiger, stetiger Wert in [0, 1) je Ort (für zackige Ränder). */
    static double hash(long seed, int x, int y, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L);
        h ^= h >>> 31;
        h *= 0x7FB5D329728EA185L;
        h ^= h >>> 27;
        return (h >>> 11) * 0x1.0p-53;
    }
}
