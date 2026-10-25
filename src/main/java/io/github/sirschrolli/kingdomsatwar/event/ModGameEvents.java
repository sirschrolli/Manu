package io.github.sirschrolli.kingdomsatwar.event;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Spielereignisse auf dem Server. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ModGameEvents {
    /** Wie weit ein Blitzeinschlag Blitzdrachen-Eier erreicht. */
    private static final int LIGHTNING_EGG_RADIUS = 6;

    private ModGameEvents() {}

    /** Blitzeinschläge in der Nähe lassen Blitzdrachen-Eier schneller schlüpfen. */
    @SubscribeEvent
    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LightningBolt bolt) || event.loadedFromDisk()
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos center = bolt.blockPosition();
        int r = LIGHTNING_EGG_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ElementalDragonEggBlock egg && egg.getDragonType() == DragonType.LIGHTNING) {
                egg.advance(level, pos.immutable(), state, 1);
            }
        }
    }
}
