package io.github.sirschrolli.kingdomsatwar.abyss;

import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import org.jspecify.annotations.Nullable;

/**
 * Die Ankerinseln des Abgrunds: Wo ein Riss in den Abgrund führt, entsteht ein kleiner Platz aus poliertem Chaosgestein,
 * von glühenden Nähten durchzogen, mit vier zerbrochenen Pfeilern, auf denen Schwebekristalle sitzen – in seiner Mitte das
 * Gegenstück des Risses, das in die Leere zurückführt. Der Platz liegt auf dem nächsten Bruchstück, dessen Oberseite eben
 * genug ist; treibt in der Nähe keins, schwebt er als eigene kleine Insel im Chaos.
 */
public final class AbyssLanding {
    private static final int SEARCH_RADIUS = 160;
    private static final int PLAZA = 5;
    private static final int FLOATING_Y = 120;

    private AbyssLanding() {}

    /** Baut die Ankerinsel nahe {@code near} und gibt die Unterkante ihres Risses zurück. */
    public static BlockPos ensure(ServerLevel level, BlockPos near, Direction.Axis axis) {
        BlockPos site = findSite(level, near);
        boolean floating = site == null;
        if (floating) {
            site = new BlockPos(near.getX(), FLOATING_Y, near.getZ());
        }
        for (int cx = (site.getX() - 14) >> 4; cx <= (site.getX() + 14) >> 4; cx++) {
            for (int cz = (site.getZ() - 14) >> 4; cz <= (site.getZ() + 14) >> 4; cz++) {
                level.getChunk(cx, cz);
            }
        }
        if (floating) {
            floatingIsland(level, site);
        }
        plaza(level, site);
        BlockPos bottom = site.above();
        AbyssGate.build(level, bottom, axis);
        return bottom;
    }

    /** Sucht spiralförmig ein Bruchstück, dessen Oberseite auf 11 × 11 Blöcken ziemlich eben ist. */
    static @Nullable BlockPos findSite(ServerLevel level, BlockPos near) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState random = level.getChunkSource().randomState();
        // nicht auf das Zifferblatt eines Uhrturms (seit 1.1): dort, wo einer stehen kann, sucht sie weiter
        var towers = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET)
                .get(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.CLOCK_TOWERS).orElse(null);
        var state = level.getChunkSource().getGeneratorState();
        for (int r = 0; r <= SEARCH_RADIUS; r += 12) {
            int steps = r == 0 ? 1 : Math.max(8, (int) (2 * Math.PI * r / 12));
            for (int i = 0; i < steps; i++) {
                double a = 2 * Math.PI * i / steps;
                int x = near.getX() + (int) Math.round(Math.cos(a) * r);
                int z = near.getZ() + (int) Math.round(Math.sin(a) * r);
                if (towers != null && state.hasStructureChunkInRange(towers, x >> 4, z >> 4, 3)) {
                    continue;
                }
                int h = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                if (h < 40 || h > 220) {
                    continue;
                }
                boolean flat = true;
                for (int[] o : new int[][] {{-5, -5}, {5, -5}, {-5, 5}, {5, 5}, {0, 6}, {6, 0}, {-6, 0}, {0, -6}}) {
                    int ho = generator.getBaseHeight(x + o[0], z + o[1], Heightmap.Types.WORLD_SURFACE_WG, level, random);
                    if (Math.abs(ho - h) > 3) {
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

    /** Eine kleine Insel im Chaos: oben flach, unten ein zerklüfteter Kegel aus Chaosgestein mit glühenden Nähten. */
    private static void floatingIsland(ServerLevel level, BlockPos top) {
        BlockState stone = AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState();
        BlockState seam = AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        RandomSource random = RandomSource.create(top.asLong());
        int radius = PLAZA + 3;
        for (int dy = 0; dy <= 12; dy++) {
            double r = radius * (1.0 - dy / 13.0) + (dy == 0 ? 0.5 : 0.0);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double jag = (random.nextDouble() - 0.5) * 1.4;
                    if (dx * dx + dz * dz <= (r + jag) * (r + jag)) {
                        level.setBlock(top.offset(dx, -dy, dz), dy > 0 && random.nextInt(18) == 0 ? seam : stone, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static boolean vegetation(BlockState state) {
        return state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(Blocks.CHORUS_PLANT)
                || state.is(Blocks.CHORUS_FLOWER) || state.is(Blocks.CACTUS) || state.is(Blocks.VINE) || state.is(Blocks.WEEPING_VINES)
                || state.is(Blocks.WEEPING_VINES_PLANT) || state.is(Blocks.CRIMSON_FUNGUS) || state.is(Blocks.CRIMSON_ROOTS);
    }

    /** Der Platz: Ringe aus poliertem Chaosgestein und Ziegeln, glühende Nähte, vier Pfeiler mit Schwebekristallen. */
    private static void plaza(ServerLevel level, BlockPos center) {
        BlockState polished = AbyssBlocks.POLISHED_CHAOS_STONE.base().get().defaultBlockState();
        BlockState bricks = AbyssBlocks.CHAOS_STONE_BRICKS.base().get().defaultBlockState();
        BlockState cracked = AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS.get().defaultBlockState();
        BlockState chiseled = AbyssBlocks.CHISELED_CHAOS_STONE.get().defaultBlockState();
        BlockState seam = AbyssBlocks.WORLD_SEAM.get().defaultBlockState();
        BlockState foundation = AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState();
        RandomSource random = RandomSource.create(center.asLong() ^ 0x7A11L);
        for (int dx = -PLAZA - 1; dx <= PLAZA + 1; dx++) {
            for (int dz = -PLAZA - 1; dz <= PLAZA + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > PLAZA + 0.6) {
                    continue;
                }
                BlockPos floor = center.offset(dx, 0, dz);
                BlockState top;
                if (d < 1.6) {
                    top = chiseled;
                } else if (d > PLAZA - 0.6) {
                    top = random.nextInt(4) == 0 ? cracked : bricks;
                } else {
                    // eine glühende Naht läuft quer über den Platz
                    boolean onSeam = Math.abs(dx - dz * 0.6 + Math.sin(dz * 0.9) * 0.8) < 0.6 && d > 2.2;
                    top = onSeam ? seam : polished;
                }
                level.setBlock(floor, top, Block.UPDATE_CLIENTS);
                for (int dy = 1; dy <= 7; dy++) {
                    BlockPos below = floor.below(dy);
                    if (!level.getBlockState(below).isAir()) {
                        break;
                    }
                    level.setBlock(below, foundation, Block.UPDATE_CLIENTS);
                }
                for (int dy = 1; dy <= AbyssGate.RIFT_HEIGHT + 3; dy++) {
                    BlockPos air = floor.above(dy);
                    if (!level.getBlockState(air).isAir()) {
                        level.setBlock(air, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        // Pflanzen ringsum, die in den Platz oder den Riss ragen würden (Chorus, Laub, Ranken …), weichen
        for (int dx = -PLAZA - 4; dx <= PLAZA + 4; dx++) {
            for (int dz = -PLAZA - 4; dz <= PLAZA + 4; dz++) {
                if (dx * dx + dz * dz > (PLAZA + 4.5) * (PLAZA + 4.5)) {
                    continue;
                }
                for (int dy = 1; dy <= AbyssGate.RIFT_HEIGHT + 6; dy++) {
                    BlockPos at = center.offset(dx, dy, dz);
                    if (vegetation(level.getBlockState(at))) {
                        level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        // vier zerbrochene Pfeiler, auf den Stümpfen Schwebekristalle
        int[][] corners = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int i = 0; i < corners.length; i++) {
            BlockPos base = center.offset(corners[i][0], 1, corners[i][1]);
            int height = 2 + (i * 7 + (int) (center.asLong() & 3)) % 3;
            for (int dy = 0; dy < height; dy++) {
                level.setBlock(base.above(dy), dy == height - 1 && i % 2 == 0 ? cracked : bricks, Block.UPDATE_CLIENTS);
            }
            level.setBlock(base.above(height), AbyssBlocks.FLOAT_CRYSTAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
