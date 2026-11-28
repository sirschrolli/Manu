package io.github.sirschrolli.kingdomsatwar.architect;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.SurveyorsRodItem;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Dragon Architect im Spiel: der Vermessungsstab markiert Ecken auch auf Türen, Truhen und Knöpfen (statt sie zu benutzen). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ArchitectEvents {
    private ArchitectEvents() {}

    /** Große Bereiche werden Lage für Lage vermessen. */
    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        Architect.tick(event.getServer());
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof SurveyorsRodItem) {
            event.setUseBlock(TriState.FALSE);
        }
    }
}
