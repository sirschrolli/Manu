package io.github.sirschrolli.kingdomsatwar.block.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Leuchtalgen: ein hauchdünner Belag aus feinen Fäden mit leuchtenden Pünktchen, der in der Tiefe ganze Felder des
 * Meeresgrunds türkis schimmern lässt. Lebt nur unter Wasser; man läuft einfach darüber hinweg.
 */
public class GlowAlgaeBlock extends SeaAnemoneBlock {
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 1.0);

    public GlowAlgaeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(90) == 0) {
            level.addParticle(ParticleTypes.GLOW, pos.getX() + random.nextDouble(), pos.getY() + 0.15, pos.getZ() + random.nextDouble(),
                    0.0, 0.02, 0.0);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
}
