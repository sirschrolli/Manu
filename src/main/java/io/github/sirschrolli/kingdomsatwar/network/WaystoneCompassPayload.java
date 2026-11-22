package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Spiel an Server: eine Wahl im Fenster des Wegkompasses – einen Wegstein als Ziel wählen (noch einmal: kein Ziel),
 * vergessen, oder zurück zum Spawn ({@link WaystoneCompass#SELECT} …). Der Server prüft alles selbst.
 */
public record WaystoneCompassPayload(boolean mainHand, int action, int index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WaystoneCompassPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("waystone_compass"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneCompassPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, WaystoneCompassPayload::mainHand, ByteBufCodecs.VAR_INT, WaystoneCompassPayload::action, ByteBufCodecs.VAR_INT,
            WaystoneCompassPayload::index, WaystoneCompassPayload::new);

    @Override
    public CustomPacketPayload.Type<WaystoneCompassPayload> type() {
        return TYPE;
    }

    static void handle(WaystoneCompassPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            WaystoneCompass.act(player, payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, payload.action(), payload.index());
        }
    }
}
