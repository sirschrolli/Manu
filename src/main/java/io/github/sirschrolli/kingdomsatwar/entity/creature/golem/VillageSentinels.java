package io.github.sirschrolli.kingdomsatwar.entity.creature.golem;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Der Eisenwächter eines Dorfs. Beim Erzeugen der Welt steht noch nicht fest, welche Stelle des Platzes frei bleibt
 * (Brunnen, Bänke und Stände entstehen Chunk für Chunk); deshalb ruft ihn die erste Wache des Platzes herbei, sobald sie
 * zum ersten Mal erwacht – an eine freie Stelle nahe ihrem Posten, und nur, wenn das Dorf noch keinen hat.
 */
public final class VillageSentinels {
    private VillageSentinels() {}

    public static void summon(ServerLevel level, BlockPos post) {
        summon(level, post, 40.0);
    }

    /** Wie {@link #summon(ServerLevel, BlockPos)}; {@code reach}: so weit darf kein anderer Eisenwächter stehen. */
    public static void summon(ServerLevel level, BlockPos post, double reach) {
        if (!level.getEntitiesOfClass(IronSentinel.class, new AABB(post).inflate(reach, 16.0, reach)).isEmpty()) {
            return;
        }
        BlockPos spot = freeSpot(level, post);
        if (spot == null) {
            return;
        }
        IronSentinel golem = ModEntities.IRON_GOLEM.get().create(level, EntitySpawnReason.STRUCTURE);
        if (golem == null) {
            return;
        }
        golem.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        golem.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.STRUCTURE, null);
        golem.setHomeTo(post, VillagerEntity.VILLAGE_RADIUS - 8);
        golem.setPersistenceRequired();
        level.addFreshEntity(golem);
    }

    /** Eine Stelle in 2 bis 8 Blöcken Abstand: fester Boden, darüber drei mal drei mal drei Blöcke frei und trocken. */
    static @Nullable BlockPos freeSpot(ServerLevel level, BlockPos post) {
        for (int r = 2; r <= 8; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos pos = post.offset(dx, dy, dz);
                        if (fits(level, pos)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean fits(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) {
            return false;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 2, 1))) {
            if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty() || !level.getFluidState(p).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
