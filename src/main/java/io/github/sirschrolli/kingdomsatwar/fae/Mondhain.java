package io.github.sirschrolli.kingdomsatwar.fae;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import org.jspecify.annotations.Nullable;

/**
 * Wo die Mondhaine liegen – die Lichtungen, auf denen die Mottenkönigin in ihrem Kokon schläft. Die Welt ist in Felder
 * von {@link #SPACING}×{@link #SPACING} Chunks eingeteilt (gut 300 Blöcke), in jedem Feld liegt genau ein Mondhain
 * (dieselbe Rechnung wie {@link io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement}). So lässt er sich
 * ohne Suche finden – für {@code /kaw mondhain}, und damit Bäume, Pilze und Felsen die Lichtung freilassen.
 */
public final class Mondhain {
    public static final int SPACING = 32;
    public static final int SEPARATION = 14;
    public static final int SALT = 9_163;
    /** Um die Mitte bleibt dieser Umkreis frei von Bäumen, Pilzen, Felsen, Ruinen und Feenringen. */
    public static final int CLEARING = 25;

    private Mondhain() {}

    /** Der Chunk des Mondhains im Feld ({@code regionX}, {@code regionZ}). */
    public static ChunkPos chunk(long seed, int regionX, int regionZ) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, SALT);
        int range = Math.max(1, SPACING - SEPARATION);
        int x = regionX * SPACING + random.nextInt(range);
        int z = regionZ * SPACING + random.nextInt(range);
        return new ChunkPos(x, z);
    }

    /** Mitte des Mondhains im Feld (Höhe 0). */
    public static BlockPos center(long seed, int regionX, int regionZ) {
        ChunkPos chunk = chunk(seed, regionX, regionZ);
        return new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ());
    }

    /** Liegt der Punkt näher als {@code radius} Blöcke an der Mitte eines Mondhains? */
    public static boolean near(long seed, int x, int z, int radius) {
        return nearest(seed, x, z, 1, radius) != null;
    }

    /** Der nächste Mondhain (Mitte, Höhe 0), gesucht in den Feldern ringsum. */
    public static BlockPos nearest(long seed, int x, int z) {
        BlockPos found = nearest(seed, x, z, 2, Integer.MAX_VALUE);
        return found != null ? found : center(seed, Math.floorDiv(x >> 4, SPACING), Math.floorDiv(z >> 4, SPACING));
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
                if (d < bestDistance) {
                    bestDistance = d;
                    best = center;
                }
            }
        }
        return best;
    }

    /** Platzierungsfilter: nicht im Mondhain, nicht auf der Lichtung einer Zauberbühne und nicht im Mondsteinbruch. */
    public enum Filter implements PlacementFilter {
        INSTANCE;

        public static final MapCodec<Filter> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos origin) {
            long seed = context.getLevel().getSeed();
            return !near(seed, origin.getX(), origin.getZ(), CLEARING)
                    && !MystikStage.near(seed, origin.getX(), origin.getZ(), MystikStage.CLEARING)
                    && !Mondsteinbruch.near(seed, origin.getX(), origin.getZ(), Mondsteinbruch.CLEARING);
        }

        @Override
        public MapCodec<Filter> codec() {
            return CODEC;
        }
    }
}
