package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ein Sternenkokon (seit 0.99): Was sich im Sternennetz verfing, hat die Weberin eingesponnen – ein länglicher Kokon aus
 * Sternenseide, der unter einem Faden hängt. Zerschlagen gibt er frei, was darin steckt (siehe seine Beutetabelle). Ohne Halt
 * darüber fällt er herunter (wie eine Laterne).
 */
public class StarCocoonBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 15.0, 12.0);

    public StarCocoonBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return !above.isAir() && !above.is(Blocks.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == Direction.UP && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    /** Hin und wieder rieselt Sternenstaub von ihm. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(18) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.1,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, -0.01, 0.0);
        }
    }
}
