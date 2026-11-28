package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.story.Story;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Der Spieler drückt die Taste der Chronik (seit 0.79; Standard: J): Der Server stellt die Seiten zusammen und schlägt sie
 * auf – das Buch muss niemand mehr bei sich tragen.
 */
public record RequestChroniclePayload() implements CustomPacketPayload {
    public static final RequestChroniclePayload INSTANCE = new RequestChroniclePayload();
    public static final CustomPacketPayload.Type<RequestChroniclePayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("request_chronicle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestChroniclePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<RequestChroniclePayload> type() {
        return TYPE;
    }

    static void handle(RequestChroniclePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 0.9f);
            Story.openChronicle(player);
        }
    }
}
