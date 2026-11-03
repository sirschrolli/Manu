package io.github.sirschrolli.kingdomsatwar.army;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Verteilt Soldaten in einer dichten Formation um einen Punkt – jeder bekommt einen eigenen Posten. */
public final class Formation {
    private static final List<int[]> OFFSETS = new ArrayList<>();

    static {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                OFFSETS.add(new int[] {x, z});
            }
        }
        OFFSETS.sort(Comparator.comparingInt((int[] o) -> o[0] * o[0] + o[1] * o[1]).thenComparingInt(o -> Math.abs(o[0])));
    }

    private Formation() {}

    /** Bis zu {@code count} begehbare Posten rund um {@code center}, die nächsten zuerst. Nie leer. */
    public static List<BlockPos> around(Level level, BlockPos center, int count) {
        List<BlockPos> posts = new ArrayList<>();
        for (int[] offset : OFFSETS) {
            if (posts.size() >= count) {
                break;
            }
            BlockPos ground = standable(level, center.offset(offset[0], 0, offset[1]));
            if (ground != null) {
                posts.add(ground);
            }
        }
        if (posts.isEmpty()) {
            posts.add(center);
        }
        return posts;
    }

    /** Sucht in der Spalte (zwei Blöcke hoch, drei tief) eine Stelle mit festem Boden und zwei freien Blöcken. */
    private static @Nullable BlockPos standable(Level level, BlockPos start) {
        for (int dy = 2; dy >= -3; dy--) {
            BlockPos pos = start.above(dy);
            BlockPos below = pos.below();
            if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                    && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                    && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                    && level.getFluidState(pos).isEmpty()) {
                return pos;
            }
        }
        return null;
    }
}
