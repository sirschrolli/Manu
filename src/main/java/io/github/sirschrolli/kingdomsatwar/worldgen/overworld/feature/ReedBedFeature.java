package io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.block.ReedsBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.FluidState;

/**
 * Ein Schilfgürtel: rund um den Ursprung wächst Schilf im knietiefen Wasser und auf dem Ufer direkt daneben – dicht an
 * der Wasserkante, nach außen lichter.
 */
public record ReedBedFeature(int tries) implements Feature {
    public static final MapCodec<ReedBedFeature> CODEC = Codec.intRange(1, 256).fieldOf("tries").xmap(ReedBedFeature::new, ReedBedFeature::tries);

    @Override
    public MapCodec<ReedBedFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockState reeds = ModBlocks.REEDS.get().defaultBlockState();
        boolean placed = false;
        for (int t = 0; t < this.tries; t++) {
            int x = origin.getX() + random.nextInt(15) - 7;
            int z = origin.getZ() + random.nextInt(15) - 7;
            int top = Kit.surface(level, x, z);     // erster Block über Boden bzw. Wasseroberfläche
            BlockPos pos = new BlockPos(x, top - 1, z);
            FluidState fluid = level.getFluidState(pos);
            if (fluid.is(FluidTags.WATER)) {
                // im Wasser: nur einen Block tief, der Kolben ragt in die Luft
                if (!fluid.isSource() || !level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.WATER)) {
                    continue;
                }
            } else {
                pos = new BlockPos(x, top, z);
                if (!level.getBlockState(pos).isAir() || !this.nearWater(level, pos)) {
                    continue;
                }
            }
            if (!level.getBlockState(pos.above()).isAir() || !reeds.canSurvive(level, pos)) {
                continue;
            }
            DoublePlantBlock.placeAt(level, reeds.setValue(ReedsBlock.WATERLOGGED, false), pos, 2);
            placed = true;
        }
        return placed;
    }

    /** Liegt Wasser direkt daneben (auf gleicher Höhe oder einen Block tiefer)? */
    private boolean nearWater(WorldGenLevel level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(dir);
            if (level.getFluidState(side).is(FluidTags.WATER) || level.getFluidState(side.below()).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }
}
