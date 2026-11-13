package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.Fishing;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Der Spieler drückt die Taste des Fischbuchs (Standard: H): Der Server schickt seine Fänge, das Buch schlägt auf. */
public record RequestFishBookPayload() implements CustomPacketPayload {
    public static final RequestFishBookPayload INSTANCE = new RequestFishBookPayload();
    public static final CustomPacketPayload.Type<RequestFishBookPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("request_fish_book"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestFishBookPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<RequestFishBookPayload> type() {
        return TYPE;
    }

    static void handle(RequestFishBookPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Fishing.openBook(player);
        }
    }
}
