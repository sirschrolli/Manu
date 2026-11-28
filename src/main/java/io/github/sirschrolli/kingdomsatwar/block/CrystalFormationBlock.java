package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ein Herz- oder Manakristall, wie er in Höhlen wächst: auf einem Brocken Fels ein leuchtendes Herz aus rotem Kristall
 * bzw. ein Grüppchen blauer Kristallspitzen. Abgebaut gibt er den Kristall, den man aufnehmen kann; ohne festen Boden
 * darunter zerbricht er.
 */
public class CrystalFormationBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape HEART_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 13.0, 13.0);
    private static final VoxelShape MANA_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);

    private final Crystals.Kind kind;

    public CrystalFormationBlock(Crystals.Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public Crystals.Kind kind() {
        return this.kind;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.kind == Crystals.Kind.HEART ? HEART_SHAPE : MANA_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return direction == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    /** Er funkelt: rote Funken (ab und zu ein kleines Herz) bzw. blaue Funken und aufsteigende Zeichen. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
        double y = pos.getY() + 0.3 + random.nextDouble() * 0.6;
        double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
        if (this.kind == Crystals.Kind.HEART) {
            if (random.nextInt(3) == 0) {
                level.addParticle(new DustParticleOptions(random.nextBoolean() ? 0xFF3B5C : 0xFFB3C1, 0.6f), x, y, z, 0.0, 0.01, 0.0);
            }
            if (random.nextInt(40) == 0) {
                level.addParticle(ParticleTypes.HEART, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0, 0.02, 0.0);
            }
        } else {
            if (random.nextInt(3) == 0) {
                level.addParticle(new DustParticleOptions(random.nextBoolean() ? 0x4FA8FF : 0xB8E4FF, 0.6f), x, y, z, 0.0, 0.01, 0.0);
            }
            if (random.nextInt(6) == 0) {
                level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5, random.nextGaussian() * 0.5,
                        -0.6, random.nextGaussian() * 0.5);
            }
        }
    }
}
