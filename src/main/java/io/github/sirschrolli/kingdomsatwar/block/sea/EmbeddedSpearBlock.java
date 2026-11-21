package io.github.sirschrolli.kingdomsatwar.block.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Der Gezeiten-Speer, wie er schräg aus dem Hügel in der Lagune ragt – die Spitze leuchtet, Funken steigen auf. Wer ihn
 * greifen will, muss dem Hügel zu nahe kommen – und weckt den, in dessen Kuppel er steckt.
 */
public class EmbeddedSpearBlock extends Block {
    private static final VoxelShape SHAPE = Block.column(6.0, 0.0, 16.0);

    public EmbeddedSpearBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.SEA_GLOW.get(),
                    pos.getX() + 0.7 + random.nextDouble() * 0.6, pos.getY() + 1.6 + random.nextDouble() * 0.6,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(12) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.9, pos.getY() + 1.9, pos.getZ() + 0.5, 0.0, 0.03, 0.0);
        }
    }
}
