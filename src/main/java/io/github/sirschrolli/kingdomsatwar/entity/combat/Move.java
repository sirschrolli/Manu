package io.github.sirschrolli.kingdomsatwar.entity.combat;

import net.minecraft.util.RandomSource;

/**
 * Ein besonderer Angriff eines Monsters. Er läuft in drei Abschnitten ab: die <b>Vorwarnung</b> (eine Haltung, ein Laut,
 * Teilchen – Zeit zum Ausweichen), die <b>Ausführung</b> und die <b>Erholung</b> (das Monster steht kurz da und ist
 * angreifbar). Die Angriffe selbst haben keinen Zustand; alles Laufende steht im {@link Fight}.
 */
public abstract class Move {
    /** Geht der Angriff jetzt? {@code dist} = Abstand zum Ziel (Blöcke). */
    public abstract boolean ready(Fight f, double dist);

    /** Länge der Vorwarnung in Ticks. */
    public int windup(Fight f) {
        return 12;
    }

    /** Beginn der Vorwarnung. */
    public void begin(Fight f) {
    }

    /** Jeder Tick der Vorwarnung ({@code t} = 0 … windup-1). */
    public void warn(Fight f, int t) {
    }

    /** Jeder Tick der Ausführung ({@code t} = 0, 1, …); false, wenn sie vorbei ist. */
    public abstract boolean act(Fight f, int t);

    /** Am Ende (auch bei Abbruch). */
    public void end(Fight f) {
    }

    /** So lange steht es danach still (Ticks). */
    public int recover(Fight f) {
        return 10;
    }

    /** Wartezeit, bis es diesen Angriff wieder nutzt (Ticks, vor dem Tempo). */
    public int cooldown(RandomSource random) {
        return 140 + random.nextInt(100);
    }

    /** Wie gern es diesen Angriff gerade wählt (bei mehreren, die gehen). */
    public float weight(Fight f, double dist) {
        return 1.0f;
    }

    /** Darf der Blick während der Vorwarnung dem Ziel folgen? (Sonst bleibt die Richtung vom Beginn.) */
    public boolean tracks(Fight f, int t) {
        return true;
    }
}
