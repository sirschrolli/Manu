package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server an Spiel: das Handelsfenster eines Bürgers öffnen (oder auffrischen) – mit seinen Angeboten, wie oft noch gehandelt
 * werden kann, wie lange der Laden noch geschlossen ist (ms), dem Rabatt durch Ansehen, ob alles umsonst ist, und den
 * Einstellungen des Servers (Handel bis zur Pause, Länge der Pause in Minuten).
 */
public record OpenTradePayload(int entity, int profession, List<Trading.Offer> offers, int tradesLeft, long closedMillis, int discount,
        boolean free, int limit, int pauseMinutes) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenTradePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("open_trade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenTradePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenTradePayload::entity,
            ByteBufCodecs.VAR_INT, OpenTradePayload::profession,
            Trading.Offer.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenTradePayload::offers,
            ByteBufCodecs.VAR_INT, OpenTradePayload::tradesLeft,
            ByteBufCodecs.VAR_LONG, OpenTradePayload::closedMillis,
            ByteBufCodecs.VAR_INT, OpenTradePayload::discount,
            ByteBufCodecs.BOOL, OpenTradePayload::free,
            ByteBufCodecs.VAR_INT, OpenTradePayload::limit,
            ByteBufCodecs.VAR_INT, OpenTradePayload::pauseMinutes,
            OpenTradePayload::new);

    @Override
    public CustomPacketPayload.Type<OpenTradePayload> type() {
        return TYPE;
    }
}
