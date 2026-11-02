package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePiece;
import java.util.Arrays;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Vorführung der Stammburgen der vier Reiche: {@code castles} baut jede Burg auf echtem Gelände (ebenster Platz in der
 * Nähe) und zeigt sie aus sechs Blickwinkeln, {@code castleworld} lässt die Weltgenerierung die vier Burgen an ihren
 * berechneten Orten erzeugen.
 */
final class CastleShowcase {
    private CastleShowcase() {}

    /** Blickwinkel: Auge (x, y, z) und Ziel (x, y, z) in den Koordinaten der Burg (Tor im Süden). */
    private static final double[][] VIEWS = {
            {40, 30, 52, 0, 6, 0},
            {0, 5, 64, 0, 12, 28},
            {0, 2.6, 23, 0, 9, -14},
            {0, 8.6, -11.5, 0, 9.5, -23},
            {31.5, 19.6, 31.5, 0, 6, 0},
            {-46, 34, -40, 0, 6, 0}};

    static void castles(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        steps.add(Step.command("weather clear"));
        int i = 0;
        for (Realm realm : Realm.values()) {
            double[] c = new double[5];
            int sx = 6000 + i * 700;
            int sz = 6000;
            i++;
            steps.add(Step.server(level -> place(level, realm, sx, sz, c)));
            steps.add(Step.until(() -> c[3] != 0, 4000));
            views(steps, c, "castle_" + realm.id());
        }
    }

    static void castleWorld(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        for (Realm realm : Realm.values()) {
            double[] c = new double[5];
            steps.add(Step.server(level -> generate(level, realm, c)));
            steps.add(Step.until(() -> c[3] != 0, 6000));
            views(steps, c, "castleworld_" + realm.id());
        }
    }

    private static void views(Deque<Step> steps, double[] c, String prefix) {
        int n = 0;
        for (double[] v : VIEWS) {
            steps.add(at(c, v));
            steps.add(Step.waitTicks(n == 0 ? 160 : 50));
            steps.add(Step.waitForChunks(1500));
            steps.add(Step.screenshot(prefix + "_" + n++));
        }
    }

    /** Ebensten Platz nahe (sx, sz) suchen, Chunks erzeugen und die Burg Chunk für Chunk bauen. */
    private static void place(ServerLevel level, Realm realm, int sx, int sz, double[] c) {
        long started = System.currentTimeMillis();
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        int[] best = {sx, sz};
        int bestRelief = Integer.MAX_VALUE;
        for (int r = 0; r <= 600 && bestRelief > 4; r += 40) {
            int steps = Math.max(1, r / 20);
            for (int a = 0; a < steps; a++) {
                double ang = a * Math.PI * 2 / steps;
                int x = sx + (int) Math.round(Math.cos(ang) * r);
                int z = sz + (int) Math.round(Math.sin(ang) * r);
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                boolean water = false;
                for (int dx = -40; dx <= 40; dx += 20) {
                    for (int dz = -40; dz <= 40; dz += 20) {
                        int floor = generator.getBaseHeight(x + dx, z + dz, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                        int surface = generator.getBaseHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                        water |= surface > floor;
                        min = Math.min(min, floor);
                        max = Math.max(max, floor);
                    }
                }
                if (!water && max - min < bestRelief) {
                    bestRelief = max - min;
                    best = new int[] {x, z};
                }
            }
        }
        int x = best[0];
        int z = best[1];
        int[] heights = new int[81];
        int n = 0;
        for (int dx = -32; dx <= 32; dx += 8) {
            for (int dz = -32; dz <= 32; dz += 8) {
                heights[n++] = generator.getBaseHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, level, random);
            }
        }
        Arrays.sort(heights);
        int floor = Math.max(heights[40] - 1, generator.getSeaLevel() + 1);
        BlockPos center = new BlockPos(x, floor, z);
        RealmCastlePiece piece = new RealmCastlePiece(realm, center, Rotation.NONE, 77L + realm.ordinal());
        BoundingBox box = piece.getBoundingBox();
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                BoundingBox chunkBox = new BoundingBox(cx << 4, level.getMinY(), cz << 4, (cx << 4) + 15, level.getMaxY(), (cz << 4) + 15);
                piece.postProcess(level, level.structureManager(), generator, RandomSource.create(1), chunkBox, new ChunkPos(cx, cz), center);
            }
        }
        c[0] = x + 0.5;
        c[1] = z + 0.5;
        c[2] = floor;
        c[4] = Rotation.NONE.ordinal();
        c[3] = 1;
        KingdomsAtWar.LOGGER.info("[Showcase] Burg {} gebaut bei {} (Unebenheit {}), {} ms", realm.id(), center, bestRelief,
                System.currentTimeMillis() - started);
    }

    /** Echte Weltgenerierung: die Burg an ihrem berechneten Ort von der Welt erzeugen lassen. */
    private static void generate(ServerLevel level, Realm realm, double[] c) {
        long started = System.currentTimeMillis();
        ChunkPos chunk = RealmLocator.find(level, realm);
        Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.REALM_CASTLE_STRUCTURE);
        StructureStart start = chunk == null ? null
                : level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
        if (start == null || !start.isValid() || !(start.getPieces().getFirst() instanceof RealmCastlePiece piece)) {
            KingdomsAtWar.LOGGER.info("[Showcase] Burg {} nicht erzeugt (Chunk {})", realm.id(), chunk);
            c[3] = -1;
            return;
        }
        BoundingBox box = piece.getBoundingBox();
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        BlockPos center = piece.center();
        c[0] = center.getX() + 0.5;
        c[1] = center.getZ() + 0.5;
        c[2] = center.getY();
        c[4] = piece.rotation().ordinal();
        c[3] = 1;
        KingdomsAtWar.LOGGER.info("[Showcase] Burg {} ({}) von der Welt erzeugt bei {} ({}), {} ms", realm.id(), piece.realm().id(), center,
                piece.rotation(), System.currentTimeMillis() - started);
    }

    /** Wie {@code BlockPos.rotate}: lokale Lage (x, z) in die Drehung der Burg bringen. */
    private static double[] turn(double x, double z, Rotation rotation) {
        return switch (rotation) {
            case NONE -> new double[] {x, z};
            case CLOCKWISE_90 -> new double[] {-z, x};
            case CLOCKWISE_180 -> new double[] {-x, -z};
            case COUNTERCLOCKWISE_90 -> new double[] {z, -x};
        };
    }

    private static Step at(double[] c, double[] v) {
        return Step.once(minecraft -> {
            if (c[3] < 0) {
                return;
            }
            Rotation rotation = Rotation.values()[(int) c[4]];
            double[] eye = turn(v[0], v[2], rotation);
            double[] look = turn(v[3], v[5], rotation);
            double lx = look[0] - eye[0];
            double ly = v[4] - (v[1] + 1.62);
            double lz = look[1] - eye[1];
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in minecraft:overworld run tp @s %.2f %.2f %.2f %.1f %.1f", c[0] + eye[0], c[2] + v[1], c[1] + eye[1], yaw, pitch));
        });
    }
}
