package io.github.sirschrolli.kingdomsatwar.client.relic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.network.ItemActivationPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Im Spiel: zeigt einen Gegenstand groß im Bild, wenn der Server es sagt (das Zweite Leben). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class RelicClient {
    private RelicClient() {}

    @SubscribeEvent
    static void registerHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(ItemActivationPayload.TYPE, RelicClient::onActivation);
    }

    private static void onActivation(ItemActivationPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !payload.stack().isEmpty()) {
            minecraft.player.displayItemActivation(payload.stack());
        }
    }
}
