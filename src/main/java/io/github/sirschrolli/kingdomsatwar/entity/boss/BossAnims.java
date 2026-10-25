package io.github.sirschrolli.kingdomsatwar.entity.boss;

/**
 * Nummern der Boss-Animationen (Server und Client). Element-Angriffe benutzen die allgemeinen Plätze:
 * {@link #RAIN} ist z. B. beim Feuerdrachen der Meteorregen und beim Eisdrachen der Eiszapfenregen.
 */
public final class BossAnims {
    public static final int NONE = 0;
    /** Biss nach vorn. */
    public static final int BITE = 1;
    /** Prankenhieb mit einem Vorderbein. */
    public static final int CLAW = 2;
    /** Schwanzhieb im Halbkreis (dreht den Körper mit). */
    public static final int TAIL_SWEEP = 3;
    /** Flügelschlag nach vorn – Sturmböe. */
    public static final int WING_BUFFET = 4;
    /** Brüllen (Phasenwechsel, Donnerschrei). */
    public static final int ROAR = 5;
    /** Aufbäumen und auf den Boden stampfen. */
    public static final int STOMP = 6;
    public static final int TAKE_OFF = 7;
    public static final int LAND = 8;
    /** Odem am Boden (Kopf schwenkt). */
    public static final int BREATH = 9;
    /** Odem im Tiefflug (Sturzflug über das Ziel). */
    public static final int AIR_BREATH = 10;
    /** Salve von Geschossen (Feuerbälle, Eisspeere …). */
    public static final int VOLLEY = 11;
    /** Regen vom Himmel (Meteore, Eiszapfen, Blitze …) – der Boss schwebt und brüllt. */
    public static final int RAIN = 12;
    /** Ring/Welle um den Boss (Flammenring, Flutwelle …). */
    public static final int RING = 13;
    /** Aus dem Boden brechend (Lava-Geysire, Eisspeere, Stalagmiten …) – der Boss schlägt auf den Boden. */
    public static final int ERUPT = 14;
    /** Besondere Angriffe der einzelnen Bosse. */
    public static final int SPECIAL_1 = 15;
    public static final int SPECIAL_2 = 16;

    private BossAnims() {}
}
