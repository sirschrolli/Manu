package io.github.sirschrolli.kingdomsatwar.block;

import io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity;
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
 * Nestkern: liegt verborgen unter jedem Drachennest und wacht darüber. Kommt ein Spieler nahe genug, erwacht der
 * Boss; nach seinem Sieg bleibt das Nest besiegt. Unzerstörbar.
 */
public class DragonLairCoreBlock extends Block implements EntityBlock {
    public DragonLairCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DragonLairCoreBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.DRAGON_LAIR_CORE.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (lvl instanceof ServerLevel server && be instanceof DragonLairCoreBlockEntity core) {
                core.serverTick(server);
            }
        };
    }
}
