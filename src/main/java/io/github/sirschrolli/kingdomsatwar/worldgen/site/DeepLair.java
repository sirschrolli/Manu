package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.entity.miniboss.MiniBoss;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jspecify.annotations.Nullable;
import java.util.function.BiFunction;

/**
 * Was die Hallen der Tiefe (seit 1.15) gemeinsam haben – die Gruft der Vergessenen, die Brutgrotte, die Zwergenhalle und die
 * Trollhöhle: der Platz tief im Fels der Oberwelt (genug Gestein darüber, nicht im Reich des Sculk), der Mini-Boss, der
 * beim Bau an seinem Platz eingesetzt wird (sein Heimplatz ist dieser Platz), Spawner und feste Zufallsauswahl.
 */
final class DeepLair {
    private DeepLair() {}

    /**
     * Die Bodenhöhe des Bauwerks unter der Mitte des Chunks: {@code low} bis {@code low + range − 1} (aus dem Startwert);
     * null, wenn dort über {@code reach} Blöcke im Umkreis keine {@code cover} Blöcke Gestein über dem Boden liegen, die
     * Welt zu flach ist oder das Reich des Sculk beginnt.
     */
    static @Nullable BlockPos origin(Structure.GenerationContext context, SiteTerrain terrain, long seed, int low, int range, int reach, int cover) {
        int x = context.chunkPos().getMiddleBlockX();
        int z = context.chunkPos().getMiddleBlockZ();
        int floor = low + Math.floorMod((int) (seed >>> 13), range);
        if (floor - 8 < terrain.minY()) {
            return null;
        }
        int[] span = terrain.span(x, z, reach, Math.max(4, reach / 3));
        if (span[0] < floor + cover) {
            return null;
        }
        if (sculk(terrain, x, floor, z, reach)) {
            return null;
        }
        return new BlockPos(x, floor, z);
    }

    /** Liegt die Stelle (oder ihr Umkreis) im Reich des Sculk? Das gehört der Stillen Kathedrale. */
    static boolean sculk(SiteTerrain terrain, int x, int floor, int z, int reach) {
        for (int[] d : new int[][] {{0, 0}, {reach / 2, 0}, {-reach / 2, 0}, {0, reach / 2}, {0, -reach / 2}}) {
            if (terrain.biome(x + d[0], floor + 4, z + d[1]).is(Biomes.DEEP_DARK)) {
                return true;
            }
        }
        return false;
    }

    static net.minecraft.world.level.block.Rotation rotation(long seed) {
        return net.minecraft.world.level.block.Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
    }

    /** Eine von mehreren Blockarten, fest je Block (Salz {@code salt} trennt die Auswahlen voneinander). */
    static BlockState pick(VillageBuilder b, int x, int y, int z, int salt, BlockState... states) {
        float n = b.noise(x * 31 + salt, y - salt * 7, z + salt * 13);
        return states[Math.min(states.length - 1, (int) (n * states.length))];
    }

    /** Ein Monster-Spawner für {@code type}. */
    static void spawner(VillageBuilder b, int x, int y, int z, EntityType<?> type) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        b.level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (b.level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, RandomSource.create(pos.asLong()));
        }
    }

    /**
     * Setzt den Mini-Boss an seinen Platz (Füße bei y, mittig im Block (x, z), der Blick nach {@code facing}): er schläft,
     * bis jemand kommt, und kehrt immer hierher zurück. {@code dy} hebt ihn an (eine Sitzfläche aus einer Stufe).
     */
    static void boss(VillageBuilder b, EntityType<? extends Mob> type, int x, int y, int z, double dy, Direction facing) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        Mob mob = type.create(b.level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (mob == null) {
            return;
        }
        float yaw = b.worldDirection(facing).toYRot();
        mob.snapTo(pos.getX() + 0.5, pos.getY() + dy, pos.getZ() + 0.5, yaw, 0.0f);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
        mob.finalizeSpawn(b.level, b.level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        mob.setPersistenceRequired();
        if (mob instanceof MiniBoss boss) {
            boss.miniBoss().setHome(dy > 0.01 ? pos.above() : pos);
        }
        b.level.addFreshEntityWithPassengers(mob);
    }

    /**
     * Ein gewundener Gang aus der Höhle hinaus: von (x, z) in Richtung {@code degrees} (Grad, 0 = +x, 90 = +z),
     * {@code length} Blöcke lang, {@code width} breit (halbe Breite), 3 bis 4 hoch, mit ebenem Boden ({@code ground}); Wasser
     * und Lava gleich daneben werden zu Tiefenschiefer.
     */
    static void tunnel(VillageBuilder b, double x, double z, double degrees, int length, double width, BiFunction<Integer, Integer, BlockState> ground) {
        double a = Math.toRadians(degrees);
        double fx = Math.cos(a);
        double fz = Math.sin(a);
        BlockState rock = Blocks.DEEPSLATE.defaultBlockState();
        for (int t = 0; t <= length; t++) {
            double sway = Math.sin(t * 0.27 + x * 0.1) * 1.6;
            double cx = x + fx * t - fz * sway;
            double cz = z + fz * t + fx * sway;
            int r = (int) Math.ceil(width) + 2;
            for (int bx = (int) Math.floor(cx) - r; bx <= (int) Math.floor(cx) + r; bx++) {
                for (int bz = (int) Math.floor(cz) - r; bz <= (int) Math.floor(cz) + r; bz++) {
                    if (!SiteCave.column(b, bx, bz)) {
                        continue;
                    }
                    double d = Math.hypot(bx + 0.5 - cx, bz + 0.5 - cz);
                    double edge = width + (b.noise(bx, 11, bz) - 0.5) * 0.8;
                    if (d < edge) {
                        int h = d < edge * 0.5 ? 4 : 3;
                        b.set(bx, 0, bz, ground.apply(bx, bz));
                        for (int y = 1; y <= h; y++) {
                            if (!b.get(bx, y, bz).isAir()) {
                                b.set(bx, y, bz, Blocks.AIR.defaultBlockState());
                            }
                        }
                    } else if (d < edge + 1.3) {
                        for (int y = 0; y <= 5; y++) {
                            if (!b.get(bx, y, bz).getFluidState().isEmpty()) {
                                b.set(bx, y, bz, rock);
                            }
                        }
                    }
                }
            }
        }
    }
}
