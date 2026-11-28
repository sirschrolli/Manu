package io.github.sirschrolli.kingdomsatwar.block.sea;

import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Herz des Atolls: liegt verborgen unter dem Sand der Lagune des Versunkenen Atolls. Kommt jemand dem Hügel mit dem
 * Speer zu nahe, erwacht Karkinos, der Riffkoloss; nach seinem Sieg kann ihn nur das Gezeitenhorn zurückrufen.
 * Unzerstörbar.
 */
public class AtollCoreBlock extends Block implements EntityBlock {
    public AtollCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AtollCoreBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != SeaBlocks.ATOLL_CORE_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof AtollCoreBlockEntity core) {
                core.serverTick(server);
            }
        };
    }
}
