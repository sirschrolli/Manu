package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fae.FaeElytra;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler hat mit den Feen-Elytren einen Flügelschlag gemacht (der Server prüft alles nach). */
public record FaeFlapPayload() implements CustomPacketPayload {
    public static final FaeFlapPayload INSTANCE = new FaeFlapPayload();
    public static final CustomPacketPayload.Type<FaeFlapPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("fae_flap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FaeFlapPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<FaeFlapPayload> type() {
        return TYPE;
    }

    static void handle(FaeFlapPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            FaeElytra.flap(player);
        }
    }
}
