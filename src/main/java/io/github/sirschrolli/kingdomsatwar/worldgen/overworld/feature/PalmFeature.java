package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Eine Palme der Korallenküste: ein schlanker Stamm, der sich im Bogen zur Seite neigt, oben eine Krone aus acht
 * Wedeln, die nach außen hin herabhängen, und unter der Krone ein paar Kokosnüsse.
 */
public record PalmFeature() implements Feature {
    public static final MapCodec<PalmFeature> CODEC = MapCodec.unit(PalmFeature::new);

    private static final int[][] FRONDS = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    @Override
    public MapCodec<PalmFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!Kit.dryGround(level, origin)) {
            return false;
        }
        BlockState log = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState leaf = Blocks.JUNGLE_LEAVES.defaultBlockState();
        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        int height = 5 + random.nextInt(5);
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        double bend = 1.0 + random.nextDouble() * 1.8;          // wie weit die Spitze zur Seite steht
        BlockPos.MutableBlockPos pos = origin.mutable();
        int shifted = 0;
        for (int k = 0; k < height; k++) {
            int want = (int) Math.round(bend * Math.pow(k / (double) height, 2.0));
            while (shifted < want) {
                // seitlich weiter: erst daneben, dann weiter hinauf (so bleibt der Stamm verbunden)
                pos.move(lean);
                shifted++;
                if (!Kit.setIfOpen(level, pos, Kit.log(log, lean.getAxis()))) {
                    return !logs.isEmpty();
                }
                logs.add(pos.immutable());
            }
            if (!Kit.setIfOpen(level, pos, log)) {
                if (k == 0) {
                    return false;
                }
                break;
            }
            logs.add(pos.immutable());
            pos.move(Direction.UP);
        }
        BlockPos top = pos.below();
        // Krone: Mitte und acht Wedel, die nach außen absinken
        for (int k = 1; k <= (random.nextBoolean() ? 2 : 1); k++) {
            if (Kit.setIfOpen(level, top.above(k), leaf)) {
                leaves.add(top.above(k));
            }
        }
        for (int[] f : FRONDS) {
            boolean diagonal = f[0] != 0 && f[1] != 0;
            int length = (diagonal ? 2 : 3) + random.nextInt(2);
            for (int d = 1; d <= length; d++) {
                int drop = d <= 1 ? 0 : d == 2 ? (diagonal ? 1 : 0) : d - 2;
                BlockPos at = top.offset(f[0] * d, 1 - drop, f[1] * d);
                if (Kit.setIfOpen(level, at, leaf)) {
                    leaves.add(at.immutable());
                }
                // die Wedelspitzen hängen noch ein Stück weiter herab
                if (d == length && random.nextBoolean()) {
                    BlockPos tip = at.below();
                    if (Kit.setIfOpen(level, tip, leaf)) {
                        leaves.add(tip.immutable());
                    }
                }
            }
        }
        // Kokosnüsse am obersten Stammstück
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.45f) {
                BlockPos nut = top.below().relative(side);
                if (level.getBlockState(nut).isAir()) {
                    level.setBlock(nut, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, side.getOpposite())
                            .setValue(CocoaBlock.AGE, 1 + random.nextInt(2)), 2);
                }
            }
        }
        Kit.settleLeaves(level, logs, leaves);
        return true;
    }
}
