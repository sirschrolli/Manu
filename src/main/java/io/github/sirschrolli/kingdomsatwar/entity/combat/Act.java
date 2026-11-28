package io.github.sirschrolli.kingdomsatwar.entity.combat;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

/**
 * Was ein Monster gerade tut – für die Haltung im Spiel (wird an die Spieler in der Nähe geschickt, nicht gespeichert).
 *
 * @param move   Nummer des Angriffs ({@link MoveKind#ordinal()}), -1: nichts
 * @param windup Länge der Vorwarnung in Ticks
 * @param start  Spielzeit, zu der die Vorwarnung begann
 */
public record Act(int move, int windup, long start) {
    public static final Act NONE = new Act(-1, 0, 0L);

    public static final StreamCodec<ByteBuf, Act> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, a -> a.move() + 1,
            ByteBufCodecs.VAR_INT, Act::windup,
            ByteBufCodecs.VAR_LONG, Act::start,
            (move, windup, start) -> new Act(move - 1, windup, start));

    public @Nullable MoveKind kind() {
        return MoveKind.byId(this.move);
    }
}
