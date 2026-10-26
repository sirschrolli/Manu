package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server an Spiel: Titelkarte eines Kapitels einblenden. {@code fallen}: Element des eben besiegten Drachen
 * (Ordinalzahl, -1 = keiner) – dann nennt die Karte seinen Namen.
 */
public record ChapterCardPayload(int chapter, int fallen) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChapterCardPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("chapter_card"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChapterCardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChapterCardPayload::chapter, ByteBufCodecs.INT, ChapterCardPayload::fallen, ChapterCardPayload::new);

    @Override
    public CustomPacketPayload.Type<ChapterCardPayload> type() {
        return TYPE;
    }
}
