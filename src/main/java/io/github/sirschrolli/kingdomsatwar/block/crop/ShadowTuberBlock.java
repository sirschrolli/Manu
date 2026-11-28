package io.github.sirschrolli.kingdomsatwar.block.crop;

import io.github.sirschrolli.kingdomsatwar.block.ModCropBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Schattenknollen (seit 2.0): die Kartoffel der Schattenwelt – violettgraues Kraut, darunter dunkle Knollen. Sie brauchen kein
 * Licht und wachsen auch in völliger Finsternis; auf Ackerland gedeihen sie wie Kartoffeln, wild (langsam) auch auf
 * Schattengras und Schattenerde. Reife Stauden schimmern ein wenig, damit man sie im Dunkeln findet.
 */
public class ShadowTuberBlock extends ModCropBlock {
    private static final VoxelShape[] SHAPES = Block.boxes(7, age -> Block.column(16.0, 0.0, 2 + age));

    public ShadowTuberBlock(Supplier<? extends ItemLike> seed, BlockBehaviour.Properties properties) {
        super(seed, properties);
    }

    /** Licht der reifen Stauden. */
    public static int light(BlockState state) {
        return state.getValue(AGE) >= MAX_AGE ? 3 : 0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[this.getAge(state)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(ShadowBlocks.SHADOW_GRASS.get()) || state.is(ShadowBlocks.SHADOW_SOIL.get());
    }

    /** Wie bei jeder Feldfrucht, nur ohne Licht. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);
        TriState decision = soil.canSustainPlant(level, below, Direction.UP, state);
        return !decision.isDefault() ? decision.isTrue() : this.mayPlaceOn(soil, level, below);
    }

    /** Wie bei jeder Feldfrucht (Ackerland und Nachbarn beschleunigen), nur ohne Licht. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) {
            return;
        }
        int age = this.getAge(state);
        if (age < this.getMaxAge()) {
            float speed = getGrowthSpeed(state, level, pos);
            if (net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (25.0f / speed) + 1) == 0)) {
                level.setBlock(pos, this.getStateForAge(age + 1), Block.UPDATE_CLIENTS);
                net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
            }
        }
    }
}
