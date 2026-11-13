package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.creator.CreatorWings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Spiel an Server: Der Spieler hat mit den Schwingen des Schöpfers den Schwingenschlag gezündet (der Server prüft alles nach). */
public record CreatorBoostPayload() implements CustomPacketPayload {
    public static final CreatorBoostPayload INSTANCE = new CreatorBoostPayload();
    public static final CustomPacketPayload.Type<CreatorBoostPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("creator_boost"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CreatorBoostPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<CreatorBoostPayload> type() {
        return TYPE;
    }

    static void handle(CreatorBoostPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            CreatorWings.boost(player);
        }
    }
}
