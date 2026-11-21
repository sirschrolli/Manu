package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownActions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Spiel an Server: eine Handlung im Stadtfenster (Bürger anheuern, Bauplan nehmen, umbenennen, Anrede, entlassen,
 * Baustelle aufgeben). {@code banner} ist das Banner, an dem das Fenster geöffnet wurde.
 */
public record TownActionPayload(BlockPos banner, int action, int arg, String text) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TownActionPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("town_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TownActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TownActionPayload::banner,
            ByteBufCodecs.VAR_INT, TownActionPayload::action,
            ByteBufCodecs.VAR_INT, TownActionPayload::arg,
            ByteBufCodecs.STRING_UTF8, TownActionPayload::text,
            TownActionPayload::new);

    @Override
    public CustomPacketPayload.Type<TownActionPayload> type() {
        return TYPE;
    }

    static void handle(TownActionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            TownActions.handle(player, payload.banner(), payload.action(), payload.arg(), payload.text());
        }
    }
}
