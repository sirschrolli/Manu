package io.github.sirschrolli.kingdomsatwar.block.abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Kopf des Weltenfressers: die Trophäe des Weltenretters – der Kopf, auf ein Viertel geschrumpft, auf einem Sockel aus
 * Chaosgestein mit Goldband, in dessen Flanken gemeißelte Augen glimmen. Die Nähte glühen, der Rachen atmet langsam,
 * ab und zu steigt Glut aus ihm auf. Den Kopf zeichnet {@code client.renderer.abyss.WorldEaterTrophyRenderer}.
 */
public class WorldEaterTrophyBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(Block.column(14.0, 0.0, 3.0), Block.column(11.0, 3.0, 6.0),
            Block.column(10.0, 6.0, 14.5));

    public WorldEaterTrophyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Der Kopf blickt den an, der ihn aufstellt. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorldEaterTrophyBlockEntity(pos, state);
    }

    /** Ab und zu Glut aus dem Rachen, selten ein Funke aus den Nähten. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5 + facing.getStepX() * 0.62;
        double y = pos.getY() + 0.66;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.62;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, x + (random.nextDouble() - 0.5) * 0.12, y, z + (random.nextDouble() - 0.5) * 0.12,
                    facing.getStepX() * 0.01, 0.012, facing.getStepZ() * 0.01);
        }
        if (random.nextInt(7) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.05, z, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(18) == 0) {
            level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }
}
