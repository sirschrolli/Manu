package io.github.sirschrolli.kingdomsatwar.block.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Röhrenwürmer: ein Büschel weißer, geringelter Röhren mit roten Federkronen – sie wachsen an den heißen Quellen der
 * Tiefsee, wo das Wasser warm und voller Mineralien ist. Leben nur unter Wasser auf festem Grund.
 */
public class TubeWormsBlock extends SeaAnemoneBlock {
    public TubeWormsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(60) == 0) {
            level.addParticle(ParticleTypes.BUBBLE, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.8,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.03, 0.0);
        }
    }
}
