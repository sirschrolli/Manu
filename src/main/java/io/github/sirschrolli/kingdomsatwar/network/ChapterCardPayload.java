package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server an Spiel: Titelkarte eines Kapitels einblenden ({@code chapter} = −1: nur der Erzähler, ohne Karte).
 * {@code fallen}: Element des eben besiegten Drachen (Ordinalzahl, −1 = keiner) – dann nennt die Karte seinen Namen.
 * {@code narration}: was der Erzähler dazu sagt (Kennungen der Zeilen, durch Kommas getrennt; leer = nichts).
 */
public record ChapterCardPayload(int chapter, int fallen, String narration) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ChapterCardPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("chapter_card"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChapterCardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChapterCardPayload::chapter, ByteBufCodecs.INT, ChapterCardPayload::fallen,
            ByteBufCodecs.STRING_UTF8, ChapterCardPayload::narration, ChapterCardPayload::new);

    @Override
    public CustomPacketPayload.Type<ChapterCardPayload> type() {
        return TYPE;
    }
}
