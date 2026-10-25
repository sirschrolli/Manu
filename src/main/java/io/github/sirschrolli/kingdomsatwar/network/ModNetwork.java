package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Netzwerk-Nachrichten zwischen Spiel (Client) und Server. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ModNetwork {
    /** Erhöhen, wenn sich Nachrichten ändern – Client und Server müssen zusammenpassen. */
    private static final String PROTOCOL = "1";

    private ModNetwork() {}

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL)
                .playToServer(DragonBreathPayload.TYPE, DragonBreathPayload.STREAM_CODEC, DragonBreathPayload::handle);
    }
}
