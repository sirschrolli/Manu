package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import io.github.sirschrolli.kingdomsatwar.fishing.Fishing;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Drill ist entschieden. {@code success}: der Fisch ist gefangen; {@code perfect}: er war keinen Augenblick aus der
 * Fangzone; {@code chest}: die Truhe wurde gehoben. Der Server prüft, ob das möglich war. */
public record FishingResultPayload(int session, boolean success, boolean perfect, boolean chest) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FishingResultPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("fishing_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FishingResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FishingResultPayload::session, ByteBufCodecs.BOOL, FishingResultPayload::success, ByteBufCodecs.BOOL, FishingResultPayload::perfect, ByteBufCodecs.BOOL, FishingResultPayload::chest, FishingResultPayload::new);

    @Override
    public CustomPacketPayload.Type<FishingResultPayload> type() {
        return TYPE;
    }

    static void handle(FishingResultPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            Fishing.onResult(player, payload.session(), payload.success(), payload.perfect(), payload.chest());
        }
    }
}
