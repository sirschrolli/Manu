package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Netzwerk-Nachrichten zwischen Spiel (Client) und Server. */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class ModNetwork {
    /** Erhöhen, wenn sich Nachrichten ändern – Client und Server müssen zusammenpassen. */
    private static final String PROTOCOL = "15";

    /** Eine angemeldete Nachricht: ihre Art, ihr Codec und die Richtung ({@code toServer}: vom Spiel zum Server). */
    public record Registered(CustomPacketPayload.Type<?> type, StreamCodec<? super RegistryFriendlyByteBuf, ?> codec, boolean toServer) {}

    private static final List<Registered> REGISTERED = new ArrayList<>();

    private ModNetwork() {}

    /**
     * Alle Nachrichten der Mod – für den Test, dass jede über das Netz heil ankommt (im Einzelspieler werden Nachrichten
     * nicht verpackt; ein Fehler im Codec zeigte sich sonst erst auf einem Server, im LAN oder in einer Essential-Welt).
     */
    public static List<Registered> registered() {
        return List.copyOf(REGISTERED);
    }

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
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        REGISTERED.clear();
        toServer(registrar, DragonBreathPayload.TYPE, DragonBreathPayload.STREAM_CODEC, DragonBreathPayload::handle);
        toServer(registrar, RequestChroniclePayload.TYPE, RequestChroniclePayload.STREAM_CODEC, RequestChroniclePayload::handle);
        toServer(registrar, RequestFishBookPayload.TYPE, RequestFishBookPayload.STREAM_CODEC, RequestFishBookPayload::handle);
        toServer(registrar, FishingResultPayload.TYPE, FishingResultPayload.STREAM_CODEC, FishingResultPayload::handle);
        toServer(registrar, FishBagTakePayload.TYPE, FishBagTakePayload.STREAM_CODEC, FishBagTakePayload::handle);
        toServer(registrar, HealPayload.TYPE, HealPayload.STREAM_CODEC, HealPayload::handle);
        toServer(registrar, FaeFlapPayload.TYPE, FaeFlapPayload.STREAM_CODEC, FaeFlapPayload::handle);
        toServer(registrar, StarBoostPayload.TYPE, StarBoostPayload.STREAM_CODEC, StarBoostPayload::handle);
        toServer(registrar, CreatorBoostPayload.TYPE, CreatorBoostPayload.STREAM_CODEC, CreatorBoostPayload::handle);
        toServer(registrar, GlitchBlinkPayload.TYPE, GlitchBlinkPayload.STREAM_CODEC, GlitchBlinkPayload::handle);
        toServer(registrar, ProvisionPayload.TYPE, ProvisionPayload.STREAM_CODEC, ProvisionPayload::handle);
        toServer(registrar, CampaignPayload.TYPE, CampaignPayload.STREAM_CODEC, CampaignPayload::handle);
        toServer(registrar, TrainPayload.TYPE, TrainPayload.STREAM_CODEC, TrainPayload::handle);
        toServer(registrar, HeroTalentPayload.TYPE, HeroTalentPayload.STREAM_CODEC, HeroTalentPayload::handle);
        toServer(registrar, HeroAbilityPayload.TYPE, HeroAbilityPayload.STREAM_CODEC, HeroAbilityPayload::handle);
        toServer(registrar, HeroPatPayload.TYPE, HeroPatPayload.STREAM_CODEC, HeroPatPayload::handle);
        toServer(registrar, HirePayload.TYPE, HirePayload.STREAM_CODEC, HirePayload::handle);
        toServer(registrar, TradePayload.TYPE, TradePayload.STREAM_CODEC, TradePayload::handle);
        toServer(registrar, TownActionPayload.TYPE, TownActionPayload.STREAM_CODEC, TownActionPayload::handle);
        toServer(registrar, SelectSpellPayload.TYPE, SelectSpellPayload.STREAM_CODEC, SelectSpellPayload::handle);
        toServer(registrar, SavePlanPayload.TYPE, SavePlanPayload.STREAM_CODEC, SavePlanPayload::handle);
        toClient(registrar, OpenSavePlanPayload.TYPE, OpenSavePlanPayload.STREAM_CODEC);
        toClient(registrar, PlanListPayload.TYPE, PlanListPayload.STREAM_CODEC);
        toClient(registrar, ImportListPayload.TYPE, ImportListPayload.STREAM_CODEC);
        toClient(registrar, OpenTradePayload.TYPE, OpenTradePayload.STREAM_CODEC);
        toClient(registrar, OpenTownPayload.TYPE, OpenTownPayload.STREAM_CODEC);
        toClient(registrar, StartIntroPayload.TYPE, StartIntroPayload.STREAM_CODEC);
        toClient(registrar, OpenRecruitScreenPayload.TYPE, OpenRecruitScreenPayload.STREAM_CODEC);
        toClient(registrar, OpenMercenaryBoardPayload.TYPE, OpenMercenaryBoardPayload.STREAM_CODEC);
        toClient(registrar, RecruitResultPayload.TYPE, RecruitResultPayload.STREAM_CODEC);
        toClient(registrar, ScreenShakePayload.TYPE, ScreenShakePayload.STREAM_CODEC);
        toClient(registrar, OpenChroniclePayload.TYPE, OpenChroniclePayload.STREAM_CODEC);
        toClient(registrar, OpenFishBookPayload.TYPE, OpenFishBookPayload.STREAM_CODEC);
        toClient(registrar, OpenFishBagPayload.TYPE, OpenFishBagPayload.STREAM_CODEC);
        toClient(registrar, FishingStartPayload.TYPE, FishingStartPayload.STREAM_CODEC);
        toClient(registrar, FishingCatchPayload.TYPE, FishingCatchPayload.STREAM_CODEC);
        toClient(registrar, FishingEndPayload.TYPE, FishingEndPayload.STREAM_CODEC);
        toClient(registrar, HookVoidPayload.TYPE, HookVoidPayload.STREAM_CODEC);
        toClient(registrar, ChapterCardPayload.TYPE, ChapterCardPayload.STREAM_CODEC);
        toClient(registrar, HeroLevelUpPayload.TYPE, HeroLevelUpPayload.STREAM_CODEC);
        toClient(registrar, HeroImmortalPayload.TYPE, HeroImmortalPayload.STREAM_CODEC);
        toClient(registrar, HeroPatEffectPayload.TYPE, HeroPatEffectPayload.STREAM_CODEC);
        toClient(registrar, ItemActivationPayload.TYPE, ItemActivationPayload.STREAM_CODEC);
    }

    private static <T extends CustomPacketPayload> void toServer(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        registrar.playToServer(type, codec, handler);
        REGISTERED.add(new Registered(type, codec, true));
    }

    /** Nachrichten an das Spiel: Ihre Empfänger meldet das Spiel selbst an (nur dort gibt es die Fenster). */
    private static <T extends CustomPacketPayload> void toClient(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        registrar.playToClient(type, codec);
        REGISTERED.add(new Registered(type, codec, false));
    }
}
