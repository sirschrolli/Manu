package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Netzwerk-Nachrichten zwischen Spiel (Client) und Server. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ModNetwork {
    /** Erhöhen, wenn sich Nachrichten ändern – Client und Server müssen zusammenpassen. */
    private static final String PROTOCOL = "2";

    private ModNetwork() {}

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL)
                .playToServer(DragonBreathPayload.TYPE, DragonBreathPayload.STREAM_CODEC, DragonBreathPayload::handle)
                .playToServer(RecruitPayload.TYPE, RecruitPayload.STREAM_CODEC, RecruitPayload::handle)
                .playToClient(StartIntroPayload.TYPE, StartIntroPayload.STREAM_CODEC)
                .playToClient(OpenRecruitScreenPayload.TYPE, OpenRecruitScreenPayload.STREAM_CODEC)
                .playToClient(RecruitResultPayload.TYPE, RecruitResultPayload.STREAM_CODEC);
    }
}
