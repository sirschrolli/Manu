package io.github.sirschrolli.kingdomsatwar.fishing;

/** Worin der Schwimmer treibt: Wasser, Lava (seit 0.79 schwimmt er auch dort) oder die Leere unter den Inseln des Ends. */
public enum Medium {
    WATER, LAVA, VOID;

    public static Medium byIndex(int index) {
        return values()[Math.max(0, Math.min(values().length - 1, index))];
    }
}
