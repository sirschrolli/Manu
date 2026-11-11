package io.github.sirschrolli.kingdomsatwar.fishing;

/** Zugriff auf das Innere des Angelhakens (umgesetzt von {@code mixin.FishingHookMixin}). */
public interface HookAccess {
    /** Der Biss ist vorbei: Der Haken wartet wieder auf den nächsten Fisch. */
    void kingdomsatwar$endBite();

    /** Worin der Haken gerade treibt. */
    Medium kingdomsatwar$medium();

    /** Der Haken treibt in der Leere auf dieser Höhe (vom Server gemeldet). */
    void kingdomsatwar$anchor(double voidLevel);

    /** Der Haken steht unter Wasser auf dieser Höhe (seit 0.90; vom Server gemeldet). */
    void kingdomsatwar$hold(double depthLevel);

    /** Steht der Haken unter Wasser (ausgeworfen von einem tauchenden Angler), statt an der Oberfläche zu treiben? */
    boolean kingdomsatwar$submerged();

    /** Nur für Prüfungen und Vorführungen: Ein Fisch beißt – jetzt. */
    void kingdomsatwar$forceBite();
}
