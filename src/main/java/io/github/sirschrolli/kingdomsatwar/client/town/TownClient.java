package io.github.sirschrolli.kingdomsatwar.client.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.network.OpenTownPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenTradePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Siedlung im Spiel: Stadt- und Handelsfenster öffnen (die Vorschau des Bauplans zeichnet {@link io.github.sirschrolli.kingdomsatwar.client.architect.ArchitectClient}). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class TownClient {
    private TownClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenTownPayload.TYPE, TownClient::onOpenTown);
        event.register(OpenTradePayload.TYPE, TownClient::onOpenTrade);
    }

    private static void onOpenTown(OpenTownPayload payload, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() instanceof TownScreen screen && screen.banner().equals(payload.banner())) {
            screen.update(payload.view(), payload.notice().orElse(null), payload.good());
        } else {
            TownScreen screen = new TownScreen(payload.banner(), payload.view());
            mc.gui.setScreen(screen);
            payload.notice().ifPresent(n -> screen.update(payload.view(), n, payload.good()));
        }
    }

    private static void onOpenTrade(OpenTradePayload payload, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() instanceof TradeScreen screen && screen.entityId() == payload.entity()) {
            screen.update(payload);
        } else {
            mc.gui.setScreen(new TradeScreen(payload));
        }
    }
}
