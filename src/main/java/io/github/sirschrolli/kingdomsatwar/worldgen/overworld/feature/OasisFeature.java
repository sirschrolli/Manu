package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Oase in der Wüste: ein kleiner, klarer Teich mit Sandgrund, eine Lage unter dem Gelände, in der Mitte zwei Blöcke
 * tief. Ringsum ein grüner Saum – Gras, Farne und Zuckerrohr direkt am Wasser, nach außen lichter, dann trockenes Gras
 * im Sand –, Seerosen auf dem Wasser und zwei, drei Palmen, die sich über den Teich neigen.
 */
public record OasisFeature() implements Feature {
    public static final MapCodec<OasisFeature> CODEC = MapCodec.unit(OasisFeature::new);
    /** So breit ist der grüne Saum um das Wasser. */
    private static final int GREEN = 3;

    @Override
    public MapCodec<OasisFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin) || !level.getBlockState(origin.below()).is(BlockTags.SAND)) {
            return false;
        }
        int water = origin.getY() - 1;
        // die Form: zwei, drei ineinanderliegende Ellipsen
        int blobs = 2 + random.nextInt(2);
        double[][] circles = new double[blobs][];
        for (int b = 0; b < blobs; b++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double off = b == 0 ? 0.0 : 1.4 + random.nextDouble() * 1.6;
            circles[b] = new double[] {Math.cos(a) * off, Math.sin(a) * off, 2.2 + random.nextDouble() * 1.6};
        }
        Map<BlockPos, Integer> depth = new HashMap<>();
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double best = 2.0;
                for (double[] c : circles) {
                    double ex = (dx - c[0]) / c[2];
                    double ez = (dz - c[1]) / c[2];
                    best = Math.min(best, ex * ex + ez * ez);
                }
                if (best <= 1.0 - 0.12 * random.nextDouble()) {
                    depth.put(new BlockPos(origin.getX() + dx, water, origin.getZ() + dz), best < 0.4 ? 2 : 1);
                }
            }
        }
        // der Saum: Abstand jeder Spalte zum Wasser (1 = direkt am Ufer)
        Map<BlockPos, Integer> ring = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(depth.keySet());
        Map<BlockPos, Integer> dist = new HashMap<>();
        for (BlockPos cell : depth.keySet()) {
            dist.put(cell, 0);
        }
        while (!queue.isEmpty()) {
            BlockPos cell = queue.poll();
            int d = dist.get(cell);
            if (d > GREEN) {
                continue;
            }
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos next = cell.relative(side);
                if (!dist.containsKey(next)) {
                    dist.put(next, d + 1);
                    ring.put(next, d + 1);
                    queue.add(next);
                }
            }
        }
        // nur auf ebenem Sand: das Wasser auf Höhe des Geländes, der Saum höchstens einen Block höher oder tiefer
        for (BlockPos cell : depth.keySet()) {
            int top = Kit.surface(level, cell.getX(), cell.getZ()) - 1;
            if (top < water || top > water + 1 || !level.getBlockState(new BlockPos(cell.getX(), top, cell.getZ())).is(BlockTags.SAND)
                    || !level.getFluidState(new BlockPos(cell.getX(), top + 1, cell.getZ())).isEmpty()) {
                return false;
            }
        }
        for (Map.Entry<BlockPos, Integer> entry : ring.entrySet()) {
            if (entry.getValue() > 2) {
                continue;
            }
            BlockPos cell = entry.getKey();
            int top = Kit.surface(level, cell.getX(), cell.getZ()) - 1;
            if (top < water - 1 || top > water + 2) {
                return false;
            }
        }
        BlockState sand = Blocks.SAND.defaultBlockState();
        // das Ufer zuerst: rund um das Wasser fester Sand, damit nichts ausläuft
        for (BlockPos cell : depth.keySet()) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos bank = cell.relative(side);
                if (depth.containsKey(bank)) {
                    continue;
                }
                for (int k = -2; k <= 0; k++) {
                    BlockPos p = bank.above(k);
                    if (!Kit.ground(level.getBlockState(p))) {
                        level.setBlock(p, sand, 2);
                    }
                }
            }
        }
        // das Wasser mit Sandgrund, hier und da Ton
        BlockState fluid = Blocks.WATER.defaultBlockState();
        List<BlockPos> surfaceWater = new ArrayList<>();
        for (Map.Entry<BlockPos, Integer> entry : depth.entrySet()) {
            BlockPos cell = entry.getKey();
            int d = entry.getValue();
            for (int k = 1; k <= 3; k++) {
                BlockPos above = cell.above(k);
                if (Kit.open(level.getBlockState(above)) || Kit.ground(level.getBlockState(above))) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                }
            }
            for (int k = 0; k < d; k++) {
                level.setBlock(cell.below(k), fluid, 2);
            }
            BlockPos bottom = cell.below(d);
            level.setBlock(bottom, random.nextFloat() < 0.2f ? Blocks.CLAY.defaultBlockState() : sand, 2);
            if (!Kit.ground(level.getBlockState(bottom.below()))) {
                level.setBlock(bottom.below(), Blocks.SANDSTONE.defaultBlockState(), 2);
            }
            surfaceWater.add(cell);
        }
        // der grüne Saum: Gras auf Erde, nach außen lichter; ganz außen trockenes Gras im Sand
        List<BlockPos> bankSpots = new ArrayList<>();
        for (Map.Entry<BlockPos, Integer> entry : ring.entrySet()) {
            int d = entry.getValue();
            BlockPos column = entry.getKey();
            int top = Kit.surface(level, column.getX(), column.getZ()) - 1;
            BlockPos ground = new BlockPos(column.getX(), top, column.getZ());
            BlockPos above = ground.above();
            if (!level.getBlockState(ground).is(BlockTags.SAND) || !level.getBlockState(above).isAir()) {
                continue;
            }
            boolean green = d == 1 || d == 2 && random.nextFloat() < 0.85f || d == 3 && random.nextFloat() < 0.4f;
            if (green && d <= GREEN) {
                level.setBlock(ground, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                if (Kit.ground(level.getBlockState(ground.below()))) {
                    level.setBlock(ground.below(), Blocks.DIRT.defaultBlockState(), 2);
                }
                float r = random.nextFloat();
                if (d == 1 && r < 0.35f) {
                    this.cane(level, random, above);
                } else if (r < 0.45f) {
                    level.setBlock(above, Blocks.SHORT_GRASS.defaultBlockState(), 2);
                } else if (r < 0.58f) {
                    level.setBlock(above, Blocks.FERN.defaultBlockState(), 2);
                } else if (r < 0.62f && level.getBlockState(above.above()).isAir()) {
                    level.setBlock(above, Blocks.TALL_GRASS.defaultBlockState(), 2);
                    level.setBlock(above.above(), Blocks.TALL_GRASS.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 2);
                } else if (d <= 2) {
                    bankSpots.add(above);   // frei: Platz für eine Palme
                }
            } else if (random.nextFloat() < 0.3f) {
                level.setBlock(above, Blocks.SHORT_DRY_GRASS.defaultBlockState(), 2);
            }
        }
        // Seerosen
        for (BlockPos cell : surfaceWater) {
            BlockPos above = cell.above();
            if (random.nextFloat() < 0.1f && level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.LILY_PAD.defaultBlockState(), 2);
            }
        }
        // zwei, drei Palmen im Saum
        int palms = 2 + random.nextInt(2);
        Feature palm = new PalmFeature();
        for (int i = 0; i < palms && !bankSpots.isEmpty(); i++) {
            BlockPos spot = bankSpots.remove(random.nextInt(bankSpots.size()));
            palm.place(level, generator, random, spot);
            // Abstand zur nächsten Palme
            bankSpots.removeIf(p -> p.distSqr(spot) < 16);
        }
        return true;
    }

    /** Zuckerrohr am Wasser, ein bis drei Blöcke hoch. */
    private void cane(WorldGenLevel level, RandomSource random, BlockPos pos) {
        BlockState cane = Blocks.SUGAR_CANE.defaultBlockState();
        if (!cane.canSurvive(level, pos)) {
            return;
        }
        int height = 1 + random.nextInt(3);
        for (int k = 0; k < height && level.getBlockState(pos.above(k)).isAir(); k++) {
            level.setBlock(pos.above(k), cane, 2);
        }
    }
}
