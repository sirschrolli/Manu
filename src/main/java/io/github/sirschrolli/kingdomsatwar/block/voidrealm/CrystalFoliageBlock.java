package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Kristalllaub: statt Blättern tragen die verdrehten Kristallbäume der Leere Büschel aus feinen, durchscheinenden
 * Splittern, die schwach leuchten und manchmal einen Lichtfunken fallen lassen. Es welkt nicht.
 */
public class CrystalFoliageBlock extends Block {
    public CrystalFoliageBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighbour, Direction direction) {
        return neighbour.is(this) || super.skipRendering(state, neighbour, direction);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) == 0 && level.getBlockState(pos.below()).isAir()) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() - 0.05, pos.getZ() + random.nextDouble(),
                    0.0, -0.02, 0.0);
        }
    }
}
