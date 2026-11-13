package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Hohe Pflanze der Schattenwelt (Mondfarn): zwei Blöcke hoch, wächst überall dort, wo auch die Schattenhalme wachsen.
 * Aus den eingerollten, glimmenden Wedelspitzen steigen ab und zu leuchtende Sporen auf.
 */
public class ShadowTallPlantBlock extends DoublePlantBlock {
    private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

    public ShadowTallPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUPPORTS_VEGETATION) || state.is(ShadowBlocks.SHADOW_SAND.get()) || state.is(ShadowBlocks.ASH_SNOW_BLOCK.get());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(pos));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER && random.nextInt(16) == 0) {
            level.addParticle(ParticleTypes.GLOW, pos.getX() + 0.15 + random.nextDouble() * 0.7, pos.getY() + 0.45 + random.nextDouble() * 0.45,
                    pos.getZ() + 0.15 + random.nextDouble() * 0.7, 0.0, 0.01, 0.0);
        }
    }
}
