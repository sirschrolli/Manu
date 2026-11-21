package io.github.sirschrolli.kingdomsatwar.fae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.jspecify.annotations.Nullable;

/**
 * Wo die alten Mondsteinbrüche liegen (seit 0.96) – die verlassenen Gruben des alten Volks, an deren Grund der Steinhüter
 * kniet. Wie beim {@link Mondhain} ist die Welt in Felder von {@link #SPACING}×{@link #SPACING} Chunks eingeteilt (gut 500
 * Blöcke); in jedem Feld liegt höchstens ein Bruch – keiner, wenn er einem Mondhain oder einer Zauberbühne zu nahe käme. So
 * lässt er sich ohne Suche finden ({@code /kaw mondsteinbruch}), und Bäume, Pilze und Felsen lassen seinen Rand frei.
 */
public final class Mondsteinbruch {
    public static final int SPACING = 32;
    public static final int SEPARATION = 14;
    public static final int SALT = 61_337;
    /** Um die Mitte bleibt dieser Umkreis frei von Bäumen, Pilzen, Felsen, Ruinen und Feenringen. */
    public static final int CLEARING = 30;
    /** So weit müssen ein Mondhain und eine Zauberbühne mindestens entfernt sein. */
    public static final int MIN_DISTANCE = 96;

    private Mondsteinbruch() {}

    /** Der Chunk des Bruchs im Feld ({@code regionX}, {@code regionZ}) – ob er dort auch liegt, sagt {@link #exists}. */
    public static ChunkPos chunk(long seed, int regionX, int regionZ) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, SALT);
        int range = Math.max(1, SPACING - SEPARATION);
        int x = regionX * SPACING + random.nextInt(range);
        int z = regionZ * SPACING + random.nextInt(range);
        return new ChunkPos(x, z);
    }

    /** Mitte des Bruchs im Feld (Höhe 0). */
    public static BlockPos center(long seed, int regionX, int regionZ) {
        ChunkPos chunk = chunk(seed, regionX, regionZ);
        return new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
    }

    /** Liegt im Feld ein Bruch (oder käme er einem Mondhain oder einer Zauberbühne zu nahe)? */
    public static boolean exists(long seed, int regionX, int regionZ) {
        BlockPos c = center(seed, regionX, regionZ);
        return !Mondhain.near(seed, c.getX(), c.getZ(), MIN_DISTANCE) && !MystikStage.near(seed, c.getX(), c.getZ(), MIN_DISTANCE);
    }

    /** Liegt im Chunk ({@code chunkX}, {@code chunkZ}) ein Bruch? */
    public static boolean isQuarryChunk(long seed, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, SPACING);
        int regionZ = Math.floorDiv(chunkZ, SPACING);
        ChunkPos chunk = chunk(seed, regionX, regionZ);
        return chunk.x() == chunkX && chunk.z() == chunkZ && exists(seed, regionX, regionZ);
    }

    /** Liegt der Punkt näher als {@code radius} Blöcke an der Mitte eines Bruchs? */
    public static boolean near(long seed, int x, int z, int radius) {
        return nearest(seed, x, z, 1, radius) != null;
    }

    /** Der nächste Bruch (Mitte, Höhe 0), gesucht in den Feldern ringsum; null, wenn keiner in der Nähe liegt. */
    public static @Nullable BlockPos nearest(long seed, int x, int z) {
        return nearest(seed, x, z, 3, Integer.MAX_VALUE);
    }

    private static @Nullable BlockPos nearest(long seed, int x, int z, int rings, int radius) {
        int regionX = Math.floorDiv(x >> 4, SPACING);
        int regionZ = Math.floorDiv(z >> 4, SPACING);
        BlockPos best = null;
        double bestDistance = radius == Integer.MAX_VALUE ? Double.MAX_VALUE : (double) radius * radius;
        for (int dx = -rings; dx <= rings; dx++) {
            for (int dz = -rings; dz <= rings; dz++) {
                BlockPos center = center(seed, regionX + dx, regionZ + dz);
                double ddx = center.getX() - x;
                double ddz = center.getZ() - z;
                double d = ddx * ddx + ddz * ddz;
                if (d < bestDistance && exists(seed, regionX + dx, regionZ + dz)) {
                    bestDistance = d;
                    best = center;
                }
            }
        }
        return best;
    }
}
