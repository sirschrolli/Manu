package io.github.sirschrolli.kingdomsatwar.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Wie weit ein Spieler in der Geschichte ist (Anhang am Spieler, bleibt beim Tod erhalten): erreichte Ziele,
 * entdeckte Drachennester und welche Kapitelkarte er zuletzt gesehen hat.
 */
public final class StoryProgress {
    /** Ziele und Ereignisse der Geschichte. */
    public enum Flag {
        /** Die Chronik wurde dem Spieler überreicht. */
        CHRONICLE_GIVEN,
        /** Die Chronik wurde gelesen. */
        CHRONICLE_READ,
        /** Ein Rekrutierungstisch wurde aufgestellt. */
        TABLE_PLACED,
        /** Der erste Soldat wurde angeworben. */
        RECRUITED,
        /** Der Siegelkreis wurde gefunden. */
        FOUND_CIRCLE,
        /** Die Schattenwelt wurde betreten. */
        ENTERED_SHADOW,
        /** Der Spieler wurde gekrönt. */
        CROWNED,
        /** Der Spieler hat mit dem Stadtbanner seine Siedlung gegründet. */
        TOWN_FOUNDED;

        static final Codec<Flag> CODEC = Codec.STRING.xmap(s -> Flag.valueOf(s.toUpperCase(Locale.ROOT)), f -> f.name().toLowerCase(Locale.ROOT));
    }

    public static final Codec<StoryProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Flag.CODEC.listOf().optionalFieldOf("flags", List.of()).forGetter(p -> List.copyOf(p.flags)),
            DragonType.CODEC.listOf().optionalFieldOf("discovered", List.of()).forGetter(p -> List.copyOf(p.discovered)),
            Codec.INT.optionalFieldOf("last_card", -1).forGetter(p -> p.lastCard),
            Codec.INT.optionalFieldOf("defeats_told", 0).forGetter(p -> p.defeatsTold)
    ).apply(instance, StoryProgress::new));

    private final EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
    private final EnumSet<DragonType> discovered = EnumSet.noneOf(DragonType.class);
    /** Kapitel, dessen Titelkarte der Spieler zuletzt gesehen hat (Index, -1: noch keine). */
    private int lastCard;
    /** Wie viele besiegte Drachen dem Spieler schon gemeldet wurden. */
    private int defeatsTold;

    public StoryProgress() {
        this(List.of(), List.of(), -1, 0);
    }

    private StoryProgress(List<Flag> flags, List<DragonType> discovered, int lastCard, int defeatsTold) {
        this.flags.addAll(flags);
        this.discovered.addAll(discovered);
        this.lastCard = lastCard;
        this.defeatsTold = defeatsTold;
    }

    public boolean has(Flag flag) {
        return this.flags.contains(flag);
    }

    /** Setzt ein Ziel; true, wenn es neu ist. */
    public boolean set(Flag flag) {
        return this.flags.add(flag);
    }

    public boolean hasDiscovered(DragonType type) {
        return this.discovered.contains(type);
    }

    public boolean discover(DragonType type) {
        return this.discovered.add(type);
    }

    public Set<DragonType> discovered() {
        return this.discovered.isEmpty() ? EnumSet.noneOf(DragonType.class) : EnumSet.copyOf(this.discovered);
    }

    public int lastCard() {
        return this.lastCard;
    }

    public void setLastCard(int lastCard) {
        this.lastCard = lastCard;
    }

    public int defeatsTold() {
        return this.defeatsTold;
    }

    public void setDefeatsTold(int defeatsTold) {
        this.defeatsTold = defeatsTold;
    }

    /** Ziele als Bitmaske (für die Chronik auf dem Bildschirm). */
    public int flagBits() {
        int bits = 0;
        for (Flag flag : this.flags) {
            bits |= 1 << flag.ordinal();
        }
        return bits;
    }

    public void reset() {
        this.flags.clear();
        this.discovered.clear();
        this.lastCard = -1;
        this.defeatsTold = 0;
    }
}
