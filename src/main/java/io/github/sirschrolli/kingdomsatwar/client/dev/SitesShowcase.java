package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Vorführung der kleineren Bauwerke der Schattenwelt: jedes wird auf echtem Gelände gebaut und von zwei Seiten gezeigt. */
final class SitesShowcase {
    private static final String DIM = "kingdomsatwar:shadow_world";

    private SitesShowcase() {}

    static void sites(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        int i = 0;
        for (ShadowSiteStructure.Kind kind : ShadowSiteStructure.Kind.values()) {
            double[] c = new double[4];
            int sx = 3000 + i * 400;
            int sz = 3000;
            i++;
            steps.add(Step.server(level -> {
                ServerLevel shadow = ShadowShowcase.shadow(level);
                int[] spot = findSpot(shadow, kind, sx, sz);
                // erst die Bäume fort (nur für freie Sicht), dann bauen – sonst gingen auch Balken des Bauwerks mit
                // so weit, wie die echte Welt die Bäume vom Bauwerk fernhält (Bauplatz plus Abstand der großen Bäume)
                int clearing = switch (kind) {
                    case MINE -> 16;
                    case RUIN -> 22;
                    default -> 32;
                };
                clearTrees(shadow, spot[0], spot[1], clearing);
                place(shadow, kind, spot[0], spot[1], c);
            }));
            steps.add(Step.until(() -> c[3] != 0, 3000));
            double[][] views = switch (kind) {
                case TEMPLE -> new double[][] {{-24, 14, -30, 0, 8, 0}, {20, 10, 34, 0, 8, 6}, {0, 4, -6, 0, 5, 14}, {6, -2, 6, -3, -3, 9}};
                case MINE -> new double[][] {{-14, 10, -16, 0, 4, 0}, {4, -24, -3, -6, -24, 3}, {12, -26, 0, 22, -25.5, 0},
                        {-24, -26, 1, -30, -25, -1}};
                case LIGHTHOUSE -> new double[][] {{20, 8, 34, 0, 16, 0}, {14, 30, -12, 0, 30, 0}, {-8, 3, -17, -8, 2, -5}, {10, 4, 26, 0, 10, 4}};
                case RUIN -> new double[][] {{-12, 6, -13, -1, 1, 1}, {11, 5, 11, -2, 1, 0}, {0, 13, -4, 0, 0, 1}};
            };
            int n = 0;
            for (double[] v : views) {
                steps.add(at(c, v));
                steps.add(Step.waitTicks(n == 0 ? 160 : 40));
                steps.add(Step.waitForChunks(1500));
                steps.add(Step.screenshot("site_" + kind.id() + "_" + n++));
            }
        }
    }

    /** Ein passender Bauplatz in der Nähe: Land (beim Leuchtturm mit Wasser dahinter), möglichst eben. */
    private static int[] findSpot(ServerLevel shadow, ShadowSiteStructure.Kind kind, int sx, int sz) {
        var generator = shadow.getChunkSource().getGenerator();
        var random = shadow.getChunkSource().randomState();
        int sea = generator.getSeaLevel();
        for (int r = 0; r <= 1600; r += 24) {
            for (int a = 0; a < Math.max(1, r / 6); a++) {
                double ang = a * Math.PI * 2 / Math.max(1, r / 6);
                int x = sx + (int) Math.round(Math.cos(ang) * r);
                int z = sz + (int) Math.round(Math.sin(ang) * r);
                int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, shadow, random);
                if (surface > floor || floor < sea + 2) {
                    continue;
                }
                if (kind == ShadowSiteStructure.Kind.LIGHTHOUSE) {
                    int wf = generator.getBaseHeight(x, z + 20, Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                    int ws = generator.getBaseHeight(x, z + 20, Heightmap.Types.WORLD_SURFACE_WG, shadow, random);
                    if (ws > wf && floor < sea + 8) {
                        return new int[] {x, z};
                    }
                    continue;
                }
                int max = floor;
                int min = floor;
                for (int[] d : new int[][] {{-12, -12}, {12, -12}, {-12, 12}, {12, 12}}) {
                    int h = generator.getBaseHeight(x + d[0], z + d[1], Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                    max = Math.max(max, h);
                    min = Math.min(min, h);
                }
                if (max - min <= 5) {
                    return new int[] {x, z};
                }
            }
        }
        return new int[] {sx, sz};
    }

    private static void place(ServerLevel shadow, ShadowSiteStructure.Kind kind, int sx, int sz, double[] c) {
        ShadowSitePiece probe = new ShadowSitePiece(kind, new BlockPos(sx, 100, sz), Rotation.NONE, 77L);
        BoundingBox box = probe.getBoundingBox();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                shadow.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        // Höhe des Geländes ohne Bäume, wie bei der echten Platzwahl
        int y = shadow.getChunkSource().getGenerator().getBaseHeight(sx, sz, Heightmap.Types.OCEAN_FLOOR_WG, shadow,
                shadow.getChunkSource().randomState());
        BlockPos origin = new BlockPos(sx, y, sz);
        ShadowSitePiece piece = new ShadowSitePiece(kind, origin, Rotation.NONE, 77L);
        BoundingBox full = piece.getBoundingBox();
        for (int cx = full.minX() >> 4; cx <= full.maxX() >> 4; cx++) {
            for (int cz = full.minZ() >> 4; cz <= full.maxZ() >> 4; cz++) {
                BoundingBox chunkBox = new BoundingBox(cx << 4, shadow.getMinY(), cz << 4, (cx << 4) + 15, shadow.getMaxY(), (cz << 4) + 15);
                piece.postProcess(shadow, shadow.structureManager(), shadow.getChunkSource().getGenerator(), RandomSource.create(1),
                        chunkBox, new ChunkPos(cx, cz), origin);
            }
        }
        c[0] = sx + 0.5;
        c[1] = sz + 0.5;
        c[2] = y;
        c[3] = 1;
        KingdomsAtWar.LOGGER.info("[Showcase] {} gebaut bei {}", kind.id(), origin);
    }

    /** Nur für die Vorführung: Bäume rund um den Bauplatz entfernen, damit die Kamera freie Sicht auf das Bauwerk hat. */
    private static void clearTrees(ServerLevel shadow, int sx, int sz, int radius) {
        for (int cx = (sx - radius) >> 4; cx <= (sx + radius) >> 4; cx++) {
            for (int cz = (sz - radius) >> 4; cz <= (sz + radius) >> 4; cz++) {
                shadow.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = sx - radius; x <= sx + radius; x++) {
            for (int z = sz - radius; z <= sz + radius; z++) {
                int top = shadow.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                for (int y = top + 1; y >= shadow.getSeaLevel() - 4; y--) {
                    pos.set(x, y, z);
                    BlockState state = shadow.getBlockState(pos);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(ShadowBlocks.GLIMMER_VINES.get())
                            || state.is(ShadowBlocks.GLIMMER_VINES_PLANT.get())) {
                        shadow.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
                    }
                }
            }
        }
    }

    private static Step at(double[] c, double[] v) {
        return Step.once(minecraft -> {
            double dx = v[0];
            double dy = v[1];
            double dz = v[2];
            double lx = v[3] - dx;
            double ly = v[4] - (dy + 1.62);
            double lz = v[5] - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                    DIM, c[0] + dx, c[2] + dy, c[1] + dz, yaw, pitch));
        });
    }
}
