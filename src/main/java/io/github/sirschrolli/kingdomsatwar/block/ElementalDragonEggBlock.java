package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Ein Drachenei einer bestimmten {@link DragonType Drachenart}.
 *
 * <p>Im Moment ist das Ei ein dekorativer Block. Das Ausbrüten (Wärme/Kälte/Gewitter je nach Art)
 * und das Schlüpfen der Jungdrachen kommen mit dem Drachensystem in Phase 4.
 */
public class ElementalDragonEggBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private final DragonType dragonType;

    public ElementalDragonEggBlock(DragonType dragonType, BlockBehaviour.Properties properties) {
        super(properties);
        this.dragonType = dragonType;
    }

    public DragonType getDragonType() {
        return dragonType;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
