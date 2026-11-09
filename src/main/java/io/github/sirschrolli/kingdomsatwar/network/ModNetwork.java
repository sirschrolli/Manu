package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Netzwerk-Nachrichten zwischen Spiel (Client) und Server. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ModNetwork {
    /** Erhöhen, wenn sich Nachrichten ändern – Client und Server müssen zusammenpassen. */
    private static final String PROTOCOL = "11";

    private ModNetwork() {}

    /**
     * Schickt eine Nachricht an einen Spieler – nur, wenn sein Spiel sie versteht (Spieler der automatischen Tests haben
     * zum Beispiel keine Verbindung zur Mod).
     */
    public static void send(net.minecraft.server.level.ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (player.connection != null && player.connection.hasChannel(payload)) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL)
                .playToServer(DragonBreathPayload.TYPE, DragonBreathPayload.STREAM_CODEC, DragonBreathPayload::handle)
                .playToServer(HealPayload.TYPE, HealPayload.STREAM_CODEC, HealPayload::handle)
                .playToServer(FaeFlapPayload.TYPE, FaeFlapPayload.STREAM_CODEC, FaeFlapPayload::handle)
                .playToServer(StarBoostPayload.TYPE, StarBoostPayload.STREAM_CODEC, StarBoostPayload::handle)
                .playToServer(ProvisionPayload.TYPE, ProvisionPayload.STREAM_CODEC, ProvisionPayload::handle)
                .playToServer(CampaignPayload.TYPE, CampaignPayload.STREAM_CODEC, CampaignPayload::handle)
                .playToServer(TrainPayload.TYPE, TrainPayload.STREAM_CODEC, TrainPayload::handle)
                .playToServer(HeroTalentPayload.TYPE, HeroTalentPayload.STREAM_CODEC, HeroTalentPayload::handle)
                .playToServer(HeroPatPayload.TYPE, HeroPatPayload.STREAM_CODEC, HeroPatPayload::handle)
                .playToServer(HirePayload.TYPE, HirePayload.STREAM_CODEC, HirePayload::handle)
                .playToServer(TradePayload.TYPE, TradePayload.STREAM_CODEC, TradePayload::handle)
                .playToServer(TownActionPayload.TYPE, TownActionPayload.STREAM_CODEC, TownActionPayload::handle)
                .playToServer(SelectSpellPayload.TYPE, SelectSpellPayload.STREAM_CODEC, SelectSpellPayload::handle)
                .playToServer(SavePlanPayload.TYPE, SavePlanPayload.STREAM_CODEC, SavePlanPayload::handle)
                .playToClient(OpenSavePlanPayload.TYPE, OpenSavePlanPayload.STREAM_CODEC)
                .playToClient(PlanListPayload.TYPE, PlanListPayload.STREAM_CODEC)
                .playToClient(ImportListPayload.TYPE, ImportListPayload.STREAM_CODEC)
                .playToClient(OpenTradePayload.TYPE, OpenTradePayload.STREAM_CODEC)
                .playToClient(OpenTownPayload.TYPE, OpenTownPayload.STREAM_CODEC)
                .playToClient(StartIntroPayload.TYPE, StartIntroPayload.STREAM_CODEC)
                .playToClient(OpenRecruitScreenPayload.TYPE, OpenRecruitScreenPayload.STREAM_CODEC)
                .playToClient(OpenMercenaryBoardPayload.TYPE, OpenMercenaryBoardPayload.STREAM_CODEC)
                .playToClient(RecruitResultPayload.TYPE, RecruitResultPayload.STREAM_CODEC)
                .playToClient(ScreenShakePayload.TYPE, ScreenShakePayload.STREAM_CODEC)
                .playToClient(OpenChroniclePayload.TYPE, OpenChroniclePayload.STREAM_CODEC)
                .playToClient(ChapterCardPayload.TYPE, ChapterCardPayload.STREAM_CODEC)
                .playToClient(HeroLevelUpPayload.TYPE, HeroLevelUpPayload.STREAM_CODEC)
                .playToClient(HeroPatEffectPayload.TYPE, HeroPatEffectPayload.STREAM_CODEC);
    }
}
