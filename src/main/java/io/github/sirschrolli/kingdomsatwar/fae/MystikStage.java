package io.github.sirschrolli.kingdomsatwar.fae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.jspecify.annotations.Nullable;

/**
 * Wo die Zauberbühnen liegen – die verlassenen Freilichtbühnen, auf denen MissMystik in ihrem großen Zylinder wartet. Wie
 * beim {@link Mondhain} ist die Welt in Felder von {@link #SPACING}×{@link #SPACING} Chunks eingeteilt (gut 350 Blöcke); in
 * jedem Feld liegt höchstens eine Bühne – keine, wenn sie einem Mondhain zu nahe käme. So lässt sie sich ohne Suche finden
 * ({@code /kaw zauberbuehne}), und Bäume, Pilze und Felsen lassen ihre Lichtung frei.
 */
public final class MystikStage {
    public static final int SPACING = 36;
    public static final int SEPARATION = 16;
    public static final int SALT = 52_919;
    /** Um die Mitte bleibt dieser Umkreis frei von Bäumen, Pilzen, Felsen, Ruinen und Feenringen. */
    public static final int CLEARING = 26;
    /** So weit muss ein Mondhain mindestens entfernt sein. */
    public static final int MIN_DISTANCE_TO_GROVE = 80;

    private MystikStage() {}

    /** Der Chunk der Bühne im Feld ({@code regionX}, {@code regionZ}) – ob sie dort auch steht, sagt {@link #exists}. */
    public static ChunkPos chunk(long seed, int regionX, int regionZ) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, SALT);
        int range = Math.max(1, SPACING - SEPARATION);
        int x = regionX * SPACING + random.nextInt(range);
        int z = regionZ * SPACING + random.nextInt(range);
        return new ChunkPos(x, z);
    }

    /** Mitte der Bühne im Feld (Höhe 0). */
    public static BlockPos center(long seed, int regionX, int regionZ) {
        ChunkPos chunk = chunk(seed, regionX, regionZ);
        return new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
    }

    /** Steht im Feld eine Bühne (oder liegt dort zu nahe ein Mondhain)? */
    public static boolean exists(long seed, int regionX, int regionZ) {
        BlockPos c = center(seed, regionX, regionZ);
        return !Mondhain.near(seed, c.getX(), c.getZ(), MIN_DISTANCE_TO_GROVE);
    }

    /** Steht im Chunk ({@code chunkX}, {@code chunkZ}) eine Bühne? */
    public static boolean isStageChunk(long seed, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, SPACING);
        int regionZ = Math.floorDiv(chunkZ, SPACING);
        ChunkPos chunk = chunk(seed, regionX, regionZ);
        return chunk.x() == chunkX && chunk.z() == chunkZ && exists(seed, regionX, regionZ);
    }

    /** Liegt der Punkt näher als {@code radius} Blöcke an der Mitte einer Bühne? */
    public static boolean near(long seed, int x, int z, int radius) {
        return nearest(seed, x, z, 1, radius) != null;
    }

    /** Die nächste Bühne (Mitte, Höhe 0), gesucht in den Feldern ringsum; null, wenn keine in der Nähe steht. */
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
