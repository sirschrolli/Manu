package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Seelenschilf: wie Schilf am Ufer und im flachen Wasser der Seelensümpfe, aber mit dunklen, blaugrünen Halmen und
 * türkis leuchtenden Samenständen, von denen ab und zu ein Funke aufsteigt. Wächst auch auf Schattensand.
 */
public class SoulReedsBlock extends ReedsBlock {
    public SoulReedsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(ShadowBlocks.SHADOW_SAND.get());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER && random.nextInt(20) == 0) {
            level.addParticle(ParticleTypes.GLOW, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.5 + random.nextDouble() * 0.4,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.012, 0.0);
        }
    }
}
