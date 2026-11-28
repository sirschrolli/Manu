package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Hält den Kristall der verdrehten Schwerkraft am Ticken (die Wirkung selbst: abyss.GravityField). */
public class GravityCrystalBlockEntity extends BlockEntity {
    public GravityCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(AbyssBlocks.GRAVITY_CRYSTAL_ENTITY.get(), pos, state);
    }
}
