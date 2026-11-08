package io.github.sirschrolli.kingdomsatwar.voidrealm.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Bauhilfen für die Features der Leere: nur innerhalb der Chunks schreiben, die ein Feature verändern darf (der eigene und
 * die acht Nachbarn), Luft und Pflanzen ersetzen, festen Fels in Ruhe lassen; Kugeln, Klumpen und dicke Linien.
 */
final class VoidBuild {
    private VoidBuild() {}

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

    /** Setzt den Block, wenn hier Platz ist. */
    static boolean place(WorldGenLevel level, BlockPos origin, BlockPos pos, BlockState state) {
        if (!inReach(origin, pos) || !open(level, pos)) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    /** Setzt den Block auf jeden Fall (auch in Fels), solange die Stelle erreichbar ist. */
    static boolean force(WorldGenLevel level, BlockPos origin, BlockPos pos, BlockState state) {
        if (!inReach(origin, pos)) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    /** Eine dicke Linie von {@code a} nach {@code b} (Radius {@code r}, bei {@code r} &lt; 0,6 eine einfache Blocklinie). */
    static void line(WorldGenLevel level, BlockPos origin, Vec3 a, Vec3 b, double r, BlockState state, boolean force) {
        double length = a.distanceTo(b);
        int steps = Math.max(1, (int) Math.ceil(length * 2.5));
        int ir = (int) Math.ceil(r);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = a.lerp(b, i / (double) steps);
            if (r < 0.6) {
                BlockPos pos = BlockPos.containing(p);
                if (force) {
                    force(level, origin, pos, state);
                } else {
                    place(level, origin, pos, state);
                }
                continue;
            }
            for (int dx = -ir; dx <= ir; dx++) {
                for (int dy = -ir; dy <= ir; dy++) {
                    for (int dz = -ir; dz <= ir; dz++) {
                        if (dx * dx + dy * dy + dz * dz > r * r + 0.3) {
                            continue;
                        }
                        BlockPos pos = BlockPos.containing(p.x + dx, p.y + dy, p.z + dz);
                        if (force) {
                            force(level, origin, pos, state);
                        } else {
                            place(level, origin, pos, state);
                        }
                    }
                }
            }
        }
    }

    /** Ein Klumpen um {@code center}: eine ausgefranste Kugel (die Ränder bleiben zufällig offen). */
    static void blob(WorldGenLevel level, BlockPos origin, BlockPos center, double r, BlockState state, net.minecraft.util.RandomSource random,
            double ragged) {
        int ir = (int) Math.ceil(r);
        for (int dx = -ir; dx <= ir; dx++) {
            for (int dy = -ir; dy <= ir; dy++) {
                for (int dz = -ir; dz <= ir; dz++) {
                    double d = Math.sqrt(dx * dx + dy * dy * 1.4 + dz * dz);
                    if (d > r || d > r - ragged && random.nextFloat() < 0.5f) {
                        continue;
                    }
                    place(level, origin, center.offset(dx, dy, dz), state);
                }
            }
        }
    }
}
