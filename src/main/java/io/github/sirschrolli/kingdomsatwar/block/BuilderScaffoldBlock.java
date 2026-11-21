package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Baugerüst des Baumeisters: vier Stangen mit Schrägstreben und oben ein Bohlenbelag zum Draufstehen. Der Baumeister
 * stellt es sich neben hohe Mauern, steigt Lage für Lage darauf hoch und baut es wieder ab, wenn er weiterzieht; es gibt
 * dafür keinen Gegenstand, und es lässt nichts fallen.
 */
public class BuilderScaffoldBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),    // Bohlenbelag
            Block.box(0.5, 0.0, 0.5, 2.5, 14.0, 2.5),       // Stangen
            Block.box(13.5, 0.0, 0.5, 15.5, 14.0, 2.5),
            Block.box(0.5, 0.0, 13.5, 2.5, 14.0, 15.5),
            Block.box(13.5, 0.0, 13.5, 15.5, 14.0, 15.5));

    public BuilderScaffoldBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Wege führen nicht durch das Gerüst hindurch (nur obendrauf). */
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
