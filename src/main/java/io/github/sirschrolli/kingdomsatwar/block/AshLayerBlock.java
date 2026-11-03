package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ascheschicht: liegt wie Schnee in bis zu acht Lagen auf dem Boden der Aschelande und bildet dort Dünen.
 * Anders als Schnee schmilzt sie nicht.
 */
public class AshLayerBlock extends SnowLayerBlock {
    public AshLayerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Asche schmilzt nicht
    }
}
