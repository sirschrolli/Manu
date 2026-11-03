package io.github.sirschrolli.kingdomsatwar.story;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Was die Chronik beim Aufschlagen zeigt – vom Server für einen Spieler zusammengestellt: aktuelles Kapitel, erreichte
 * Ziele, Lage des Siegelkreises und je Drache Zustand, Entdeckung und ungefähre Richtung.
 *
 * <p>Richtungen: 0..7 = N, NO, O, SO, S, SW, W, NW; {@link #HERE} = ganz nah; {@link #UNKNOWN} = unbekannt
 * (z. B. in einer anderen Dimension); {@link #NOWHERE} = in dieser Welt (noch) ohne Lage.
 */
public record ChronicleData(int chapter, int flags, int defeated, boolean sealBroken, Place circle, List<Dragon> dragons) {
    public static final int HERE = -2;
    public static final int UNKNOWN = -1;
    /** Für diesen Ort gibt es in der Welt (noch) keine Lage. */
    public static final int NOWHERE = -3;

    /** Ein Ort mit Richtung und Entfernung (in Blöcken) vom Spieler aus. */
    public record Place(int direction, int distance) {
        public static final Place NONE = new Place(UNKNOWN, 0);
        public static final Place NOWHERE_PLACE = new Place(NOWHERE, 0);
    }

    /**
     * Ein Drache: {@code phase} 0 = schläft, 1 = wach, 2 = besiegt; {@code discovered}: der Spieler war schon an seinem
     * Nest; {@code inserted}: sein Splitter steckt im Monolithen.
     */
    public record Dragon(DragonType element, int phase, boolean discovered, boolean inserted, Place place) {}

    public boolean has(StoryProgress.Flag flag) {
        return (this.flags & (1 << flag.ordinal())) != 0;
    }

    public Chapter current() {
        return Chapter.byIndex(this.chapter);
    }

    public int insertedCount() {
        int n = 0;
        for (Dragon dragon : this.dragons) {
            if (dragon.inserted()) {
                n++;
            }
        }
        return n;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ChronicleData> STREAM_CODEC = StreamCodec.of(ChronicleData::write, ChronicleData::read);

    private static void write(RegistryFriendlyByteBuf buf, ChronicleData data) {
        buf.writeVarInt(data.chapter);
        buf.writeVarInt(data.flags);
        buf.writeVarInt(data.defeated);
        buf.writeBoolean(data.sealBroken);
        writePlace(buf, data.circle);
        buf.writeVarInt(data.dragons.size());
        for (Dragon dragon : data.dragons) {
            buf.writeEnum(dragon.element());
            buf.writeVarInt(dragon.phase());
            buf.writeBoolean(dragon.discovered());
            buf.writeBoolean(dragon.inserted());
            writePlace(buf, dragon.place());
        }
    }

    private static ChronicleData read(RegistryFriendlyByteBuf buf) {
        int chapter = buf.readVarInt();
        int flags = buf.readVarInt();
        int defeated = buf.readVarInt();
        boolean broken = buf.readBoolean();
        Place circle = readPlace(buf);
        int n = Math.min(buf.readVarInt(), DragonType.values().length);
        List<Dragon> dragons = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            dragons.add(new Dragon(buf.readEnum(DragonType.class), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), readPlace(buf)));
        }
        return new ChronicleData(chapter, flags, defeated, broken, circle, List.copyOf(dragons));
    }

    private static void writePlace(RegistryFriendlyByteBuf buf, Place place) {
        buf.writeVarInt(place.direction() + 3);
        buf.writeVarInt(Math.max(0, place.distance()));
    }

    private static Place readPlace(RegistryFriendlyByteBuf buf) {
        return new Place(buf.readVarInt() - 3, buf.readVarInt());
    }
}
