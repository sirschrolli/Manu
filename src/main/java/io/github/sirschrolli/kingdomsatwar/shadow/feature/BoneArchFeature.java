package io.github.sirschrolli.kingdomsatwar.shadow.feature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Ausgebleichte Knochenbögen: die Rippen eines riesigen, längst vergangenen Wesens, halb im fahlen Sand versunken –
 * ein Brustkorb aus mehreren Bögen hintereinander, dazwischen ein Stück Wirbelsäule.
 */
public record BoneArchFeature() implements Feature {
    public static final MapCodec<BoneArchFeature> CODEC = MapCodec.unit(BoneArchFeature::new);

    @Override
    public MapCodec<BoneArchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.isNearVillage(level, origin, 14)) {
            return false;
        }
        if (!level.getFluidState(origin).isEmpty() || !level.getBlockState(origin.below()).isSolidRender()) {
            return false;
        }
        Direction along = random.nextBoolean() ? Direction.EAST : Direction.SOUTH;
        Direction across = along.getClockWise();
        int ribs = 3 + random.nextInt(4);
        double radius = 4.5 + random.nextDouble() * 3.0;
        int sunk = 1 + random.nextInt(2);
        for (int r = 0; r < ribs; r++) {
            double scale = 1.0 - Math.abs(r - (ribs - 1) / 2.0) / ribs * 0.6;
            double rr = radius * scale;
            BlockPos ribBase = origin.relative(along, r * 2);
            int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, ribBase.getX(), ribBase.getZ());
            for (int step = 0; step <= 40; step++) {
                double a = Math.PI * step / 40.0;
                double side = Math.cos(a) * rr;
                double up = Math.sin(a) * rr * 1.15 - sunk;
                if (up < -1) {
                    continue;
                }
                BlockPos pos = new BlockPos(ribBase.getX() + (int) Math.round(side * across.getStepX()), groundY + (int) Math.round(up),
                        ribBase.getZ() + (int) Math.round(side * across.getStepZ()));
                Direction.Axis axis = Math.abs(Math.cos(a)) > 0.7 ? Direction.Axis.Y : across.getAxis();
                set(level, pos, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis));
            }
            // Wirbel der Wirbelsäule am Scheitel
            BlockPos spine = new BlockPos(ribBase.getX(), groundY + (int) Math.round(rr * 1.15) - sunk, ribBase.getZ());
            set(level, spine, Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, along.getAxis()));
            set(level, spine.relative(along), Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, along.getAxis()));
        }
        // verstreute Knochenstücke
        for (int i = 0; i < 6; i++) {
            BlockPos pos = origin.offset(random.nextInt(13) - 6, 0, random.nextInt(13) - 6);
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
            set(level, new BlockPos(pos.getX(), y, pos.getZ()), Blocks.BONE_BLOCK.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z));
        }
        return true;
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        BlockState old = level.getBlockState(pos);
        if (old.isAir() || old.canBeReplaced() || old.is(Blocks.BONE_BLOCK)) {
            level.setBlock(pos, state, 2);
        }
    }
}
