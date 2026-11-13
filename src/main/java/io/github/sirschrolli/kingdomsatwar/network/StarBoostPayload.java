package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.end.StarWings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler hat mit den Sternenschwingen den Sternenschub gezündet (der Server prüft alles nach). */
public record StarBoostPayload() implements CustomPacketPayload {
    public static final StarBoostPayload INSTANCE = new StarBoostPayload();
    public static final CustomPacketPayload.Type<StarBoostPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("star_boost"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StarBoostPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<StarBoostPayload> type() {
        return TYPE;
    }

    static void handle(StarBoostPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            StarWings.boost(player);
        }
    }
}
