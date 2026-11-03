package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein Moorauge: ein kleiner, unregelmäßiger Tümpel mit dunklem Wasser, einen Block unter dem Gelände, in der Mitte zwei
 * Blöcke tief. Der Grund ist Torf und Schlamm, das Ufer weicher Torf. Schilf und Seerosen kommen später dazu.
 */
public record BogPoolFeature() implements Feature {
    public static final MapCodec<BogPoolFeature> CODEC = MapCodec.unit(BogPoolFeature::new);

    @Override
    public MapCodec<BogPoolFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        int water = origin.getY() - 1;
        // die Form: zwei, drei ineinanderliegende Ellipsen
        int blobs = 2 + random.nextInt(2);
        double[][] circles = new double[blobs][];
        for (int b = 0; b < blobs; b++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double off = b == 0 ? 0.0 : 1.2 + random.nextDouble() * 1.6;
            circles[b] = new double[] {Math.cos(a) * off, Math.sin(a) * off, 1.8 + random.nextDouble() * 1.8};
        }
        Map<BlockPos, Integer> depth = new HashMap<>();
        int reach = 7;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double best = 2.0;
                for (double[] c : circles) {
                    double ex = (dx - c[0]) / c[2];
                    double ez = (dz - c[1]) / c[2];
                    best = Math.min(best, ex * ex + ez * ez);
                }
                if (best <= 1.0 - 0.15 * random.nextDouble()) {
                    depth.put(new BlockPos(origin.getX() + dx, water, origin.getZ() + dz), best < 0.35 ? 2 : 1);
                }
            }
        }
        // nur auf ebenem, festem Boden
        for (BlockPos cell : depth.keySet()) {
            int top = Kit.surface(level, cell.getX(), cell.getZ()) - 1;
            if (top < water - 1 || top > water + 1 || !Kit.ground(level.getBlockState(new BlockPos(cell.getX(), top, cell.getZ())))) {
                return false;
            }
            if (!level.getFluidState(cell.above()).isEmpty()) {
                return false;
            }
        }
        BlockState peat = ModBlocks.PEAT.get().defaultBlockState();
        BlockState mud = Blocks.MUD.defaultBlockState();
        BlockState fluid = Blocks.WATER.defaultBlockState();
        // das Ufer zuerst: rund um das Wasser muss fester Boden sein, sonst läuft es aus
        for (BlockPos cell : depth.keySet()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos side = cell.offset(dx, 0, dz);
                    if (depth.containsKey(side)) {
                        continue;
                    }
                    for (int k = -2; k <= 0; k++) {
                        BlockPos bank = side.above(k);
                        if (!Kit.ground(level.getBlockState(bank))) {
                            level.setBlock(bank, peat, 2);
                        }
                    }
                    BlockPos top = side.above(1);
                    if (Kit.ground(level.getBlockState(side)) && level.getBlockState(top).isAir() && random.nextFloat() < 0.5f) {
                        level.setBlock(side, random.nextFloat() < 0.7f ? peat : mud, 2);
                    }
                }
            }
        }
        // das Wasser, darunter Torf und Schlamm
        for (Map.Entry<BlockPos, Integer> entry : depth.entrySet()) {
            BlockPos cell = entry.getKey();
            int d = entry.getValue();
            for (int k = 1; k <= 2; k++) {
                BlockPos above = cell.above(k);
                if (Kit.open(level.getBlockState(above)) || Kit.ground(level.getBlockState(above))) {
                    level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
                }
            }
            for (int k = 0; k < d; k++) {
                level.setBlock(cell.below(k), fluid, 2);
            }
            BlockPos bottom = cell.below(d);
            level.setBlock(bottom, random.nextFloat() < 0.65f ? peat : mud, 2);
            if (!Kit.ground(level.getBlockState(bottom.below()))) {
                level.setBlock(bottom.below(), peat, 2);
            }
        }
        return true;
    }
}
