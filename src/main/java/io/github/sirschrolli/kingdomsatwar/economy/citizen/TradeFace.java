package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Wo das Gesicht eines Händlers in seiner Haut liegt (für die Kopfzeile des Handelsfensters, seit 0.99): Ausschnitt
 * ({@code u}, {@code v}, {@code size} Pixel im Quadrat) auf einer Textur von {@code texWidth} × {@code texHeight}, darüber
 * die Haube/Haare ({@code hatU}, {@code hatV}; -1: keine), gezeichnet {@code drawSize} Pixel groß.
 */
public record TradeFace(int u, int v, int size, int texWidth, int texHeight, int hatU, int hatV, int drawSize) {
    /** Die Bürger, Dorfbewohner und Händler (Haut 128 × 64, Gesicht bei 8/8, Haube bei 40/8). */
    public static final TradeFace HUMAN = new TradeFace(8, 8, 8, 128, 64, 40, 8, 16);

    public static final StreamCodec<ByteBuf, TradeFace> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TradeFace::u,
            ByteBufCodecs.VAR_INT, TradeFace::v,
            ByteBufCodecs.VAR_INT, TradeFace::size,
            ByteBufCodecs.VAR_INT, TradeFace::texWidth,
            ByteBufCodecs.VAR_INT, TradeFace::texHeight,
            ByteBufCodecs.INT, TradeFace::hatU,
            ByteBufCodecs.INT, TradeFace::hatV,
            ByteBufCodecs.VAR_INT, TradeFace::drawSize,
            TradeFace::new);
}
