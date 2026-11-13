package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sternenstaub: feiner, fahlblauer Staub, in dem winzige Sterne funkeln – der Boden der Sternenstaubfelder. Er rieselt
 * nicht (in der Leere fiele er sonst ins Nichts), aber ab und zu steigt ein Funke aus ihm auf.
 */
public class StardustBlock extends Block {
    public StardustBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(40) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(ParticleTypes.WAX_OFF, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(),
                    0.0, 0.02, 0.0);
        }
    }
}
