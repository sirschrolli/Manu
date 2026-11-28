package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Nur Träger für den Renderer des Kopfes (keine Daten). */
public class WorldEaterTrophyBlockEntity extends BlockEntity {
    public WorldEaterTrophyBlockEntity(BlockPos pos, BlockState state) {
        super(AbyssBlocks.WORLD_EATER_TROPHY_ENTITY.get(), pos, state);
    }
}
