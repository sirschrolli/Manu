package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: mit dem Bürger {@code entity} das Angebot Nummer {@code offer} handeln. */
public record TradePayload(int entity, int offer) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TradePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("trade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TradePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TradePayload::entity,
            ByteBufCodecs.VAR_INT, TradePayload::offer,
            TradePayload::new);

    @Override
    public CustomPacketPayload.Type<TradePayload> type() {
        return TYPE;
    }

    static void handle(TradePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        Trading.Result result = Trading.trade(player, payload.entity(), payload.offer());
        if (result != Trading.Result.DONE) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.trade." + result.name().toLowerCase(java.util.Locale.ROOT))
                    .withStyle(ChatFormatting.RED));
        }
    }
}
