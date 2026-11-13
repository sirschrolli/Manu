package io.github.sirschrolli.kingdomsatwar.block.abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Die Weltennaht: eine glühende Fuge, aus der an offenen Seiten Funken und feiner Rauch steigen. */
public class WorldSeamBlock extends Block {
    public WorldSeamBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) {
            return;
        }
        Direction side = Direction.getRandom(random);
        BlockPos out = pos.relative(side);
        if (!level.getBlockState(out).isAir()) {
            return;
        }
        double x = pos.getX() + 0.5 + side.getStepX() * 0.55 + (side.getStepX() == 0 ? (random.nextDouble() - 0.5) * 0.8 : 0.0);
        double y = pos.getY() + 0.5 + side.getStepY() * 0.55 + (side.getStepY() == 0 ? (random.nextDouble() - 0.5) * 0.8 : 0.0);
        double z = pos.getZ() + 0.5 + side.getStepZ() * 0.55 + (side.getStepZ() == 0 ? (random.nextDouble() - 0.5) * 0.8 : 0.0);
        level.addParticle(random.nextInt(3) == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SMOKE, x, y, z, side.getStepX() * 0.02,
                0.02, side.getStepZ() * 0.02);
    }
}
