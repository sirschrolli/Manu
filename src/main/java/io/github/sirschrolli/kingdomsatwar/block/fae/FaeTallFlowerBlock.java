package io.github.sirschrolli.kingdomsatwar.block.fae;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.function.Supplier;

/** Eine hohe Blume des Feenreichs (zwei Blöcke): aus ihren Dolden rieseln leuchtende Funken. */
public class FaeTallFlowerBlock extends TallFlowerBlock {
    private final Supplier<? extends ParticleOptions> sparkle;

    public FaeTallFlowerBlock(Supplier<? extends ParticleOptions> sparkle, BlockBehaviour.Properties properties) {
        super(properties);
        this.sparkle = sparkle;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER || random.nextInt(4) != 0) {
            return;
        }
        level.addParticle(this.sparkle.get(), pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.4 + random.nextDouble() * 0.5,
                pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, -0.01, 0.0);
    }
}
