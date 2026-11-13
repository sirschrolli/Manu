package io.github.sirschrolli.kingdomsatwar.abyss.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Ein Kristall der verdrehten Schwerkraft auf dem Boden ({@code lift}: Hebekristall, sonst Schwebekristall). */
public record GravityCrystalFeature(boolean lift) implements Feature {
    public static final MapCodec<GravityCrystalFeature> CODEC = Codec.BOOL.fieldOf("lift").xmap(GravityCrystalFeature::new, GravityCrystalFeature::lift);

    @Override
    public MapCodec<GravityCrystalFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockPos below = origin.below();
        if (!level.isEmptyBlock(origin) || !level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        level.setBlock(origin, (this.lift ? AbyssBlocks.LIFT_CRYSTAL : AbyssBlocks.FLOAT_CRYSTAL).get().defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
        return true;
    }
}
