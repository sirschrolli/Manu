package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Sternennetz (seit 0.99): Spinnweben aus Sternenlicht, wie sie die Sternenweberin spannt – es hält fest wie Spinnweben und
 * glimmt im Dunkeln. Was sie im Kampf spuckt ({@link #TEMPORARY}), zerfällt nach einer Weile wieder zu Sternenstaub.
 */
public class StarWebBlock extends WebBlock {
    /** Gespuckt (im Kampf): zerfällt nach {@link #LIFE} Ticks und lässt nichts fallen. */
    public static final BooleanProperty TEMPORARY = BooleanProperty.create("temporary");
    public static final int LIFE = 120;

    public StarWebBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TEMPORARY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TEMPORARY);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getValue(TEMPORARY) && !level.isClientSide()) {
            level.scheduleTick(pos, this, LIFE + level.getRandom().nextInt(30));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TEMPORARY)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.sendParticles(new DustParticleOptions(0xE4F0FF, 1.0f), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
        }
    }

    /** Ab und zu funkelt ein Tautropfen aus Sternenlicht in den Fäden. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(state.getValue(TEMPORARY) ? 4 : 14) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
                    0.0, -0.004, 0.0);
        }
    }
}
