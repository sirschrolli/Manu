package io.github.sirschrolli.kingdomsatwar.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Die Ankerinseln der Leere: Wo ein Riss in die Leere führt, entsteht ein kleiner, runder Platz aus poliertem Leerenstein
 * mit vier Sternenmarmor-Säulen, auf denen Leerenkristalle leuchten – in seiner Mitte das Gegenstück des Risses, das
 * zurückführt. Der Platz liegt auf dem nächsten treibenden Bruchstück, das groß genug ist; treibt in der Nähe keins, schwebt
 * er als kleine, eigene Insel im Nichts.
 */
public final class VoidLanding {
    private static final int SEARCH_RADIUS = 160;
    private static final int PLAZA = 4;
    private static final int FLOATING_Y = 110;

    private VoidLanding() {}

    /** Baut die Ankerinsel nahe {@code near} und gibt die Unterkante ihres Risses zurück. */
    public static BlockPos ensure(ServerLevel level, BlockPos near, Direction.Axis axis) {
        BlockPos site = findSite(level, near);
        boolean floating = site == null;
        if (floating) {
            site = new BlockPos(near.getX(), FLOATING_Y, near.getZ());
        }
        for (int cx = (site.getX() - 12) >> 4; cx <= (site.getX() + 12) >> 4; cx++) {
            for (int cz = (site.getZ() - 12) >> 4; cz <= (site.getZ() + 12) >> 4; cz++) {
                level.getChunk(cx, cz);
            }
        }
        if (floating) {
            floatingIsland(level, site);
        }
        plaza(level, site);
        BlockPos bottom = site.above();
        VoidGate.build(level, bottom, axis);
        return bottom;
    }

    /** Sucht spiralförmig ein Bruchstück, dessen Oberseite auf 9 × 9 Blöcken ziemlich eben ist (Oberfläche, nicht Unterseite). */
    static BlockPos findSite(ServerLevel level, BlockPos near) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState random = level.getChunkSource().randomState();
        for (int r = 0; r <= SEARCH_RADIUS; r += 12) {
            int steps = r == 0 ? 1 : Math.max(8, (int) (2 * Math.PI * r / 12));
            for (int i = 0; i < steps; i++) {
                double a = 2 * Math.PI * i / steps;
                int x = near.getX() + (int) Math.round(Math.cos(a) * r);
                int z = near.getZ() + (int) Math.round(Math.sin(a) * r);
                int h = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                if (h < 30 || h > 230) {
                    continue;
                }
                boolean flat = true;
                for (int[] o : new int[][] {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, 5}, {5, 0}, {-5, 0}, {0, -5}}) {
                    int ho = generator.getBaseHeight(x + o[0], z + o[1], Heightmap.Types.WORLD_SURFACE_WG, level, random);
                    if (Math.abs(ho - h) > 2) {
                        flat = false;
                        break;
                    }
                }
                if (flat) {
                    return new BlockPos(x, h - 1, z);
                }
            }
        }
        return null;
    }

    /** Eine kleine Insel im Nichts: oben flach, unten ein spitzer Kegel aus Leerenstein. */
    private static void floatingIsland(ServerLevel level, BlockPos top) {
        BlockState stone = VoidBlocks.VOID_STONE.base().get().defaultBlockState();
        BlockState moss = VoidBlocks.VOID_MOSS.get().defaultBlockState();
        int radius = PLAZA + 3;
        for (int dy = 0; dy <= 9; dy++) {
            double r = radius * (1.0 - dy / 10.0) + (dy == 0 ? 0.5 : 0.0);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= r * r) {
                        level.setBlock(top.offset(dx, -dy, dz), dy == 0 ? moss : stone, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** Der runde Platz: Ringe aus poliertem Leerenstein und Ziegeln, vier Säulen mit Kristallen, darüber freie Luft. */
    private static void plaza(ServerLevel level, BlockPos center) {
        BlockState polished = VoidBlocks.POLISHED_VOID_STONE.base().get().defaultBlockState();
        BlockState bricks = VoidBlocks.VOID_STONE_BRICKS.base().get().defaultBlockState();
        BlockState chiseled = VoidBlocks.CHISELED_VOID_STONE.get().defaultBlockState();
        BlockState marble = VoidBlocks.ASTRAL_PILLAR.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState crystal = VoidBlocks.VOID_CRYSTAL.get().defaultBlockState();
        BlockState foundation = VoidBlocks.VOID_STONE.base().get().defaultBlockState();
        for (int dx = -PLAZA - 1; dx <= PLAZA + 1; dx++) {
            for (int dz = -PLAZA - 1; dz <= PLAZA + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > PLAZA + 0.6) {
                    continue;
                }
                BlockPos floor = center.offset(dx, 0, dz);
                BlockState top = d < 1.6 ? chiseled : (d > PLAZA - 0.6 ? bricks : polished);
                level.setBlock(floor, top, Block.UPDATE_CLIENTS);
                // Fundament bis zum Fels (höchstens 6 tief), darüber Luft
                for (int dy = 1; dy <= 6; dy++) {
                    BlockPos below = floor.below(dy);
                    if (!level.getBlockState(below).isAir()) {
                        break;
                    }
                    level.setBlock(below, foundation, Block.UPDATE_CLIENTS);
                }
                for (int dy = 1; dy <= VoidGate.RIFT_HEIGHT + 2; dy++) {
                    BlockPos air = floor.above(dy);
                    if (!level.getBlockState(air).isAir()) {
                        level.setBlock(air, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        for (int[] c : new int[][] {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            BlockPos base = center.offset(c[0], 1, c[1]);
            for (int dy = 0; dy < 3; dy++) {
                level.setBlock(base.above(dy), marble, Block.UPDATE_CLIENTS);
            }
            level.setBlock(base.above(3), crystal, Block.UPDATE_CLIENTS);
        }
    }
}
