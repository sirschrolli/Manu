package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hilfen für Bauwerke tief im Fels: eine weite Höhle (eine Kuppel mit ebenem Grund) aushöhlen, ohne dass Wasser oder Lava
 * hineinlaufen, und weiches Rauschen für natürliche Formen. Alles in eigenen Koordinaten des Bauteils, je Chunk nur die
 * Säulen, die in ihm liegen.
 */
final class SiteCave {
    private SiteCave() {}

    /** Liegt die Säulen (x, z) im Chunk, der gerade gebaut wird? */
    static boolean column(VillageBuilder b, int x, int z) {
        BlockPos p = b.world(x, 0, z);
        return p.getX() >= b.chunkBox.minX() && p.getX() <= b.chunkBox.maxX() && p.getZ() >= b.chunkBox.minZ() && p.getZ() <= b.chunkBox.maxZ();
    }

    /** Weiches Rauschen 0..1: Zufallswerte auf einem Gitter (alle {@code scale} Blöcke), dazwischen weich gemischt. */
    static double smooth(VillageBuilder b, int x, int y, int z, int salt, int scale) {
        int x0 = Math.floorDiv(x, scale);
        int y0 = Math.floorDiv(y, scale);
        int z0 = Math.floorDiv(z, scale);
        double fx = fade((x - x0 * scale) / (double) scale);
        double fy = fade((y - y0 * scale) / (double) scale);
        double fz = fade((z - z0 * scale) / (double) scale);
        double v = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    double w = (i == 0 ? 1 - fx : fx) * (j == 0 ? 1 - fy : fy) * (k == 0 ? 1 - fz : fz);
                    v += w * b.noise(x0 + i + salt * 997, y0 + j + 4001, z0 + k - salt * 389);
                }
            }
        }
        return v;
    }

    private static double fade(double t) {
        return t * t * (3 - 2 * t);
    }

    /**
     * Höhlt eine Kuppel aus: Halbachsen {@code rx, ry, rz}, Mitte bei (0, cy, cz); der Rand wellt sich um ±{@code wobble}
     * (Anteil). Unten bleibt bei y = 0 ein ebener Grund ({@code ground} je Säule), darunter wird bis y = −4 aufgefüllt, wo
     * eine andere Höhle liegt. Wasser und Lava gleich jenseits des Randes werden zu Tiefenschiefer.
     */
    static void dome(VillageBuilder b, int rx, int ry, int rz, int cy, int cz, double wobble, BiFunction<Integer, Integer, BlockState> ground) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState rock = Blocks.DEEPSLATE.defaultBlockState();
        for (int x = -rx - 3; x <= rx + 3; x++) {
            for (int z = cz - rz - 3; z <= cz + rz + 3; z++) {
                if (!column(b, x, z)) {
                    continue;
                }
                double hx = x / (double) rx;
                double hz = (z - cz) / (double) rz;
                double h = hx * hx + hz * hz;
                if (h > 1.45) {
                    continue;
                }
                double hy1 = (1 - cy) / (double) ry;
                boolean floored = Math.sqrt(h + hy1 * hy1) / wave(b, x, 1, z, wobble) < 1;
                for (int y = -4; y <= cy + ry + 4; y++) {
                    double hy = (y - cy) / (double) ry;
                    double rel = Math.sqrt(h + hy * hy) / wave(b, x, y, z, wobble);
                    BlockState old = b.get(x, y, z);
                    if (y >= 1 && rel < 1) {
                        if (!old.isAir()) {
                            b.set(x, y, z, air);
                        }
                    } else if (y == 0 && floored) {
                        b.set(x, y, z, ground.apply(x, z));
                    } else if (y < 0 && floored && !old.isSolidRender()) {
                        b.set(x, y, z, rock);
                    } else if (rel < 1.14 && !old.getFluidState().isEmpty()) {
                        b.set(x, y, z, rock);
                    }
                }
            }
        }
    }

    private static double wave(VillageBuilder b, int x, int y, int z, double wobble) {
        return 1 + 2 * wobble * (smooth(b, x, y, z, 1, 7) - 0.5);
    }

    /** Die Höhe der Decke über (x, z) im aktuellen Chunk (erste feste Schicht ab {@code from}), höchstens {@code max}. */
    static int ceiling(VillageBuilder b, int x, int z, int from, int max) {
        for (int y = from; y <= max; y++) {
            if (b.get(x, y, z).isSolidRender()) {
                return y;
            }
        }
        return max;
    }
}
