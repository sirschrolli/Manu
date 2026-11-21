package io.github.sirschrolli.kingdomsatwar.item.glitch;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Der Bannhammer merkt sich (auf dem Server), wo die Wesen in der Nähe seiner Träger eben noch standen (für den Ruckler). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class GlitchEvents {
    private GlitchEvents() {}

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BannhammerItem.track(level);
        }
    }
}
