package io.github.sirschrolli.kingdomsatwar.entity.end;

/**
 * Was das Modell von Phasmor, dem Leerwandlerkönig, zum Zeichnen braucht – gemeinsam für den König selbst und seine
 * Trugbilder (die dieselben Haltungen zeigen).
 */
public interface PhasmorLike {
    /** Der laufende Angriff (Ordinalzahl von {@link PhasmorEntity.Attack}). */
    int attackOrdinal();

    /** Wie lange der laufende Angriff schon dauert (im Client, für die Animationen). */
    float attackTime(float partialTicks);

    /** Hat er sich gekrönt (unter halber Kraft: die Krone lodert, er ist schneller)? */
    boolean isCrowned();

    /** Ist das nur ein Trugbild? */
    default boolean isIllusion() {
        return false;
    }
}
