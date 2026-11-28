package io.github.sirschrolli.kingdomsatwar.entity.boss;

/**
 * Ein Boss, gegen den mehrere Spieler gemeinsam antreten können (siehe {@link BossParty}): Wer ihm schadet – selbst oder
 * mit seinen Soldaten – oder von ihm getroffen wird, kämpft mit. Mit jedem weiteren Mitstreiter wird er stärker, und beim
 * Sieg bekommt jeder Mitstreiter seine Heldenerfahrung, den Sieg in seiner Statistik und seine eigene Beute.
 */
public interface PartyBoss {
    /** Gilt dieses Wesen als Boss? (Ein Krieger eines Reichs nur, wenn er dessen Fürst ist.) */
    default boolean isPartyBoss() {
        return true;
    }

    /**
     * Wird er mit jedem Mitstreiter stärker? Die Drachen, die Erzmagier und Schrolli zählen ihre Herausforderer schon beim
     * Erwachen selbst – sie werden nicht doppelt stärker.
     */
    default boolean scalesWithParty() {
        return true;
    }
}
