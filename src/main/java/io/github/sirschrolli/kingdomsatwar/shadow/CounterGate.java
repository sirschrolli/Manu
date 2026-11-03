package io.github.sirschrolli.kingdomsatwar.shadow;

import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.world.SealState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import org.jspecify.annotations.Nullable;

/**
 * Das Gegentor in der Schattenwelt: Wer zum ersten Mal durch das Schattentor tritt, findet sich auf einer uralten
 * Plattform aus Schattenstein wieder – an derselben Stelle wie der Siegelkreis in der Oberwelt oder, falls dort Meer,
 * Abgrund oder Gebirge liegen, auf dem nächsten ebenen Stück Land. In ihrer Mitte wogt das Gegentor, das zurück zum
 * Siegelkreis führt; vier Pfeiler mit Seelenlaternen und ein steinerner Torbogen rahmen es ein.
 */
public final class CounterGate {
    /** Halbmesser der Plattform. */
    public static final int RADIUS = 7;
    private static final int SEARCH_RADIUS = 256;

    private CounterGate() {}

    /** Mitte des Gegentors – wird beim ersten Mal gesucht und gebaut. */
    public static BlockPos ensure(ServerLevel shadow, BlockPos near) {
        SealState seal = SealState.of(shadow);
        BlockPos gate = seal.shadowGate();
        if (gate != null) {
            return gate;
        }
        BlockPos site = findSite(shadow, near);
        build(shadow, site);
        seal.setShadowGate(site);
        seal.save(shadow);
        return site;
    }

    /** Sucht ringförmig um {@code near} eine ebene Stelle an Land (ohne Wasser, Abgrund oder schwebende Insel darüber). */
    static BlockPos findSite(ServerLevel shadow, BlockPos near) {
        ChunkGenerator generator = shadow.getChunkSource().getGenerator();
        RandomState random = shadow.getChunkSource().randomState();
        BlockPos best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int r = 0; r <= SEARCH_RADIUS; r += 16) {
            int steps = r == 0 ? 1 : Math.max(8, (int) (2 * Math.PI * r / 24));
            for (int i = 0; i < steps; i++) {
                double a = 2 * Math.PI * i / steps;
                int x = near.getX() + (int) Math.round(Math.cos(a) * r);
                int z = near.getZ() + (int) Math.round(Math.sin(a) * r);
                int score = score(shadow, generator, random, x, z);
                if (score < bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, shadow, random), z);
                }
            }
            if (best != null && bestScore <= 3 + r / 32) {
                break;   // gut genug und nah genug
            }
        }
        if (best == null) {
            best = new BlockPos(near.getX(), 90, near.getZ());
        }
        return best;
    }

    /** Wie gut eignet sich diese Stelle? (kleiner = besser, MAX_VALUE = gar nicht) */
    private static int score(ServerLevel shadow, ChunkGenerator generator, RandomState random, int x, int z) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int dx = -RADIUS; dx <= RADIUS; dx += RADIUS) {
            for (int dz = -RADIUS; dz <= RADIUS; dz += RADIUS) {
                int surface = generator.getBaseHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, shadow, random);
                int floor = generator.getBaseHeight(x + dx, z + dz, Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                if (surface != floor || surface <= ShadowTerrain.SEA_LEVEL || surface > ShadowTerrain.ISLAND_BOTTOM - 20) {
                    return Integer.MAX_VALUE;   // Wasser, zu tief oder eine Insel darüber
                }
                min = Math.min(min, surface);
                max = Math.max(max, surface);
            }
        }
        Holder<Biome> biome = generator.getBiomeSource().createUncachedResolver(random).getNoiseBiome(x >> 2, min >> 2, z >> 2);
        if (biome.is(ShadowBiomes.CRYSTAL_CHASMS) || biome.is(ShadowBiomes.ABYSSAL_SEA) || biome.is(ShadowBiomes.SOUL_MARSH)) {
            return Integer.MAX_VALUE;
        }
        return max - min;
    }

    /** Baut Plattform, Torbogen, Pfeiler und das Gegentor um {@code center} (Mitte auf Höhe des Bodens). */
    static void build(ServerLevel level, BlockPos center) {
        for (int cx = (center.getX() - RADIUS - 6) >> 4; cx <= (center.getX() + RADIUS + 6) >> 4; cx++) {
            for (int cz = (center.getZ() - RADIUS - 6) >> 4; cz <= (center.getZ() + RADIUS + 6) >> 4; cz++) {
                level.getChunk(cx, cz);   // erzeugen
            }
        }
        BlockState bricks = ShadowBlocks.SHADOW_STONE_BRICKS.base().get().defaultBlockState();
        BlockState polished = ShadowBlocks.POLISHED_SHADOW_STONE.base().get().defaultBlockState();
        BlockState chiseled = ShadowBlocks.CHISELED_SHADOW_STONE.get().defaultBlockState();
        BlockState cracked = ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get().defaultBlockState();
        int y = center.getY();
        // Boden der Plattform: Ringe aus Ziegeln und poliertem Stein, darunter Fundament bis zum Grund, darüber frei
        for (int dx = -RADIUS - 2; dx <= RADIUS + 2; dx++) {
            for (int dz = -RADIUS - 2; dz <= RADIUS + 2; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > RADIUS + 1.5) {
                    continue;
                }
                BlockPos floor = center.offset(dx, -1, dz);
                if (d <= RADIUS + 0.5) {
                    int ring = (int) Math.floor(d);
                    BlockState top = ring % 3 == 2 ? bricks : polished;
                    if (ring == RADIUS && (dx + dz) % 5 == 0) {
                        top = cracked;
                    }
                    level.setBlock(floor, top, Block.UPDATE_CLIENTS);
                    for (int k = 2; k < 24; k++) {
                        BlockPos below = center.offset(dx, -k, dz);
                        BlockState old = level.getBlockState(below);
                        if (!old.canBeReplaced() && old.getFluidState().isEmpty()) {
                            break;
                        }
                        level.setBlock(below, (k % 4 == 0) ? polished : bricks, Block.UPDATE_CLIENTS);
                    }
                    for (int k = 0; k < 9; k++) {
                        level.setBlock(center.offset(dx, k, dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                } else {
                    // Stufen ringsum
                    level.setBlock(floor, ShadowBlocks.SHADOW_STONE_BRICKS.slab().get().defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.TOP), Block.UPDATE_CLIENTS);
                    for (int k = 2; k < 24; k++) {
                        BlockPos below = center.offset(dx, -k, dz);
                        BlockState old = level.getBlockState(below);
                        if (!old.canBeReplaced() && old.getFluidState().isEmpty()) {
                            break;
                        }
                        level.setBlock(below, bricks, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        // Rand des Tores: gemeißelter Schattenstein um 3×3 Torplatten
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = center.offset(dx, -1, dz);
                boolean inner = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
                level.setBlock(pos, inner ? polished : chiseled, Block.UPDATE_CLIENTS);
                if (inner) {
                    level.setBlock(center.offset(dx, 0, dz), ModBlocks.SHADOW_PORTAL.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        // Torbogen über dem Tor (Ost-West), mit Treppenstufen als Zierbogen
        for (int side = -1; side <= 1; side += 2) {
            for (int k = 0; k < 5; k++) {
                level.setBlock(center.offset(side * 3, k, 0), k == 4 ? chiseled : bricks, Block.UPDATE_CLIENTS);
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            level.setBlock(center.offset(dx, 5, 0), dx == 0 ? chiseled : polished, Block.UPDATE_CLIENTS);
        }
        level.setBlock(center.offset(-2, 4, 0), ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP),
                Block.UPDATE_CLIENTS);
        level.setBlock(center.offset(2, 4, 0), ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP),
                Block.UPDATE_CLIENTS);
        level.setBlock(center.offset(0, 4, 0), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true), Block.UPDATE_CLIENTS);
        // vier Pfeiler mit Seelenlaternen
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                BlockPos base = center.offset(sx * 5, 0, sz * 5);
                for (int k = 0; k < 3; k++) {
                    level.setBlock(base.above(k), k == 0 ? chiseled : ShadowBlocks.SHADOW_STONE_BRICKS.wall().get().defaultBlockState(),
                            Block.UPDATE_ALL);
                }
                level.setBlock(base.above(3), Blocks.SOUL_LANTERN.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Wo ein Reisender neben dem Gegentor ankommt (auf der Plattform, südlich des Tores). */
    public static BlockPos arrival(BlockPos gate) {
        return gate.offset(0, 0, 3);
    }

    /** Ist an dieser Stelle schon ein Gegentor? (Für Tests.) */
    public static boolean isGate(ServerLevel level, @Nullable BlockPos center) {
        return center != null && level.getBlockState(center).is(ModBlocks.SHADOW_PORTAL.get());
    }
}
