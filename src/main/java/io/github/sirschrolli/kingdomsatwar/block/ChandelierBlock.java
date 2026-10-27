package io.github.sirschrolli.kingdomsatwar.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Kronleuchter: hängt an der Decke (oder an einer Kette), acht Kerzen brennen mit kleinen Flammen und
 * erhellen den ganzen Saal.
 */
public class ChandelierBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.column(1.0, 9.0, 16.0),           // Aufhängung
            Block.box(1.5, 4.0, 1.5, 14.5, 13.0, 14.5)); // Arme, Ring und Kerzen
    /** Spitzen der acht Kerzendochte (in Blockkoordinaten), passend zum Modell. */
    private static final List<Vec3> WICKS = List.of(
            wick(2.5, 12.8, 8), wick(13.5, 12.8, 8), wick(8, 12.8, 2.5), wick(8, 12.8, 13.5),
            wick(4, 11.55, 4), wick(12, 11.55, 4), wick(4, 11.55, 12), wick(12, 11.55, 12));

    public ChandelierBlock(Properties properties) {
        super(properties);
    }

    private static Vec3 wick(double x, double y, double z) {
        return new Vec3(x / 16, y / 16, z / 16);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    /** Braucht Halt von oben: einen festen Block, eine Kette oder einen Zaun. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return directionToNeighbour == Direction.UP && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (Vec3 wick : WICKS) {
            double x = pos.getX() + wick.x, y = pos.getY() + wick.y, z = pos.getZ() + wick.z;
            float chance = random.nextFloat();
            if (chance < 0.3f) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
                if (chance < 0.05f) {
                    level.playLocalSound(x, y, z, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS,
                            1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f, false);
                }
            }
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
