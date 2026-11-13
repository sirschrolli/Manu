package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Ein uralter Baum des Urhains aus Urholz: ein dicker Stamm ({@code thickness} × {@code thickness}) auf Wurzelfüßen, mit
 * knorrigen Wurzeln, die sich über den Boden ziehen, und Moos am Fuß. Weit oben gabelt er sich in drei bis fünf
 * mächtige Äste, die eine weite, lockere Krone aus mehreren Laubwolken tragen; darunter hängen Ranken mit Leuchtbeeren.
 */
public record AncientTreeFeature(int minHeight, int maxHeight, int thickness, float crown) implements Feature {
    public static final MapCodec<AncientTreeFeature> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.intRange(4, 48).fieldOf("min_height").forGetter(AncientTreeFeature::minHeight),
            Codec.intRange(4, 48).fieldOf("max_height").forGetter(AncientTreeFeature::maxHeight),
            Codec.intRange(1, 3).fieldOf("thickness").forGetter(AncientTreeFeature::thickness),
            Codec.floatRange(2.0f, 9.0f).fieldOf("crown").forGetter(AncientTreeFeature::crown)
    ).apply(i, AncientTreeFeature::new));

    @Override
    public MapCodec<AncientTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        int t = this.thickness;
        // alle Fußfelder brauchen Boden
        for (int dx = 0; dx < t; dx++) {
            for (int dz = 0; dz < t; dz++) {
                BlockPos foot = origin.offset(dx, -1, dz);
                if (!Kit.ground(level.getBlockState(foot)) && !Kit.ground(level.getBlockState(foot.below()))) {
                    return false;
                }
            }
        }
        BlockState log = ModBlocks.ANCIENT_LOG.get().defaultBlockState();
        BlockState leaf = ModBlocks.ANCIENT_LEAVES.get().defaultBlockState();
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        int height = this.minHeight + random.nextInt(Math.max(1, this.maxHeight - this.minHeight + 1));
        // Stamm, zwei Blöcke tief verankert
        for (int k = -2; k < height; k++) {
            for (int dx = 0; dx < t; dx++) {
                for (int dz = 0; dz < t; dz++) {
                    BlockPos pos = origin.offset(dx, k, dz);
                    BlockState old = level.getBlockState(pos);
                    if (Kit.open(old) || k < 0 && Kit.ground(old) || old.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        level.setBlock(pos, log, 2);
                        logs.add(pos);
                    }
                }
            }
        }
        double cx = origin.getX() + t / 2.0;
        double cz = origin.getZ() + t / 2.0;
        // Wurzelfüße: Stammblöcke, die schräg nach außen in den Boden laufen
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int len = 1 + random.nextInt(t + 2);
            int baseX = dir.getStepX() > 0 ? origin.getX() + t - 1 : dir.getStepX() < 0 ? origin.getX() : origin.getX() + random.nextInt(t);
            int baseZ = dir.getStepZ() > 0 ? origin.getZ() + t - 1 : dir.getStepZ() < 0 ? origin.getZ() : origin.getZ() + random.nextInt(t);
            for (int s = 1; s <= len; s++) {
                int px = baseX + dir.getStepX() * s;
                int pz = baseZ + dir.getStepZ() * s;
                int up = Math.max(0, t + 1 - s);
                for (int k = -1; k <= up; k++) {
                    BlockPos pos = new BlockPos(px, origin.getY() + k, pz);
                    BlockState old = level.getBlockState(pos);
                    if (Kit.open(old) || Kit.ground(old) && k < 0) {
                        level.setBlock(pos, s == len && k <= 0 ? ModBlocks.GNARLED_ROOTS.get().defaultBlockState() : Kit.log(log, dir.getAxis()), 2);
                    }
                }
            }
        }
        // Krone in zwei Stockwerken: unten zwei, drei weit ausgreifende Äste, oben drei, vier, die sich am Ende noch
        // einmal teilen; jeder trägt eine Laubwolke, über dem Stamm sitzt die Kuppe
        int top = origin.getY() + height;
        double reach = this.crown;
        Kit.leafBlob(level, leaves, leaf, cx, top + 1.0, cz, reach * 0.7, reach * 0.45, random, 0.25);
        double turn = random.nextDouble() * Math.PI * 2.0;
        int lower = 2 + random.nextInt(2);
        for (int a = 0; a < lower; a++) {
            double angle = turn + a * Math.PI * 2.0 / lower + (random.nextDouble() - 0.5) * 0.7;
            double startY = origin.getY() + height * (0.45 + random.nextDouble() * 0.15);
            this.arm(level, logs, leaves, log, leaf, random, cx, cz, startY, angle, reach * (0.6 + random.nextDouble() * 0.25),
                    0.25 + random.nextDouble() * 0.2, reach * 0.45, reach * 0.32, t, false);
        }
        int upper = 3 + random.nextInt(2);
        for (int a = 0; a < upper; a++) {
            double angle = turn + (a + 0.5) * Math.PI * 2.0 / upper + (random.nextDouble() - 0.5) * 0.6;
            double startY = origin.getY() + height * (0.7 + random.nextDouble() * 0.18);
            this.arm(level, logs, leaves, log, leaf, random, cx, cz, startY, angle, reach * (0.75 + random.nextDouble() * 0.35),
                    0.3 + random.nextDouble() * 0.3, reach * 0.52, reach * 0.36, t, true);
        }
        // Ranken mit Leuchtbeeren unter der Krone
        int r = (int) Math.ceil(reach * 1.4);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (random.nextFloat() > 0.045f) {
                    continue;
                }
                int vx = (int) Math.floor(cx) + dx;
                int vz = (int) Math.floor(cz) + dz;
                for (int y = top + (int) reach; y > origin.getY() + 3; y--) {
                    BlockPos pos = new BlockPos(vx, y, vz);
                    if (leaves.contains(pos.above()) && level.getBlockState(pos).isAir()) {
                        int wanted = 2 + random.nextInt(6);
                        int length = 0;
                        while (length < wanted && level.getBlockState(pos.below(length)).isAir() && level.getBlockState(pos.below(length + 1)).isAir()) {
                            length++;
                        }
                        for (int k = 0; k < length; k++) {
                            BlockPos v = pos.below(k);
                            boolean end = k == length - 1;
                            BlockState vine = (end ? Blocks.CAVE_VINES : Blocks.CAVE_VINES_PLANT).defaultBlockState()
                                    .setValue(CaveVines.BERRIES, random.nextFloat() < 0.35f);
                            level.setBlock(v, vine, 2);
                        }
                        break;
                    }
                }
            }
        }
        // Moos am Fuß
        for (int dx = -t - 2; dx <= t + 2; dx++) {
            for (int dz = -t - 2; dz <= t + 2; dz++) {
                if (random.nextFloat() > 0.45f) {
                    continue;
                }
                BlockPos ground = new BlockPos(origin.getX() + dx, Kit.surface(level, origin.getX() + dx, origin.getZ() + dz), origin.getZ() + dz);
                if (level.getBlockState(ground).isAir() && level.getBlockState(ground.below()).is(Blocks.GRASS_BLOCK)) {
                    level.setBlock(ground, Blocks.MOSS_CARPET.defaultBlockState(), 2);
                }
            }
        }
        Kit.settleLeaves(level, logs, leaves);
        return true;
    }

    /**
     * Ein Ast vom Stamm aus: in der ersten Hälfte so dick wie der Stamm es erlaubt, dann einfach; am Ende eine Laubwolke.
     * Obere Äste ({@code fork}) teilen sich am Ende noch einmal in einen kurzen, steilen Zweig mit eigener Wolke.
     */
    private void arm(WorldGenLevel level, Set<BlockPos> logs, Set<BlockPos> leaves, BlockState log, BlockState leaf, RandomSource random,
            double cx, double cz, double startY, double angle, double length, double rise, double blobRadius, double blobHeight, int thick,
            boolean fork) {
        double ex = cx + Math.cos(angle) * length;
        double ez = cz + Math.sin(angle) * length;
        double ey = startY + length * rise;
        double mx = cx + Math.cos(angle) * length * 0.5;
        double mz = cz + Math.sin(angle) * length * 0.5;
        double my = startY + length * rise * 0.4;
        Kit.branch(level, logs, log, cx - 0.5, startY, cz - 0.5, mx, my, mz);
        if (thick > 1) {
            Kit.branch(level, logs, log, cx - 0.5, startY - 1, cz - 0.5, mx, my - 1, mz);
        }
        Kit.branch(level, logs, log, mx, my, mz, ex, ey, ez);
        Kit.leafBlob(level, leaves, leaf, ex, ey + 1.0, ez, blobRadius, blobHeight, random, 0.3);
        if (fork) {
            double side = angle + (random.nextBoolean() ? 0.8 : -0.8);
            double fx = ex + Math.cos(side) * length * 0.3;
            double fz = ez + Math.sin(side) * length * 0.3;
            double fy = ey + length * 0.25;
            Kit.branch(level, logs, log, ex, ey, ez, fx, fy, fz);
            Kit.leafBlob(level, leaves, leaf, fx, fy + 1.0, fz, blobRadius * 0.75, blobHeight * 0.8, random, 0.3);
        }
    }
}
