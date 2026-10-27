package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
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
 * Thronkern: liegt verborgen unter Nyxars Thron in der Schattenzitadelle. Betritt jemand den Thronhof, nimmt Nyxar
 * Gestalt an; nach seinem Sieg bleibt der Thron leer. Unzerstörbar.
 */
public class ShadowThroneCoreBlock extends Block implements EntityBlock {
    public ShadowThroneCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShadowThroneCoreBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.SHADOW_THRONE_CORE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof ShadowThroneCoreBlockEntity core) {
                core.serverTick(server);
            }
        };
    }
}
