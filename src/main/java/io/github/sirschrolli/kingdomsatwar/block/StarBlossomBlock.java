package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sternblüte: sternförmige, fahlblau leuchtende Blüten der Glimmerweiten. Wächst, wo auch die Schattenhalme wachsen;
 * über ihr tanzt ab und zu ein Lichtfunke.
 */
public class StarBlossomBlock extends ShadowPlantBlock {
    public StarBlossomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.addParticle(ParticleTypes.GLOW, pos.getX() + 0.25 + random.nextDouble() * 0.5, pos.getY() + 0.6 + random.nextDouble() * 0.3,
                    pos.getZ() + 0.25 + random.nextDouble() * 0.5, 0.0, 0.008, 0.0);
        }
    }
}
