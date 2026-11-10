package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Zeichenzustand Schrollis des Schöpfers. */
public class SchrolliRenderState extends LivingEntityRenderState {
    /** Laufende Kunst (Ordinalzahl von {@code SchrolliEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Die Wahre Gestalt? */
    public boolean trueForm;
    /** Merker (Heiligenschein geworfen, Weltkugel geworfen, verborgen, Neuschöpfung, Taumeln, gleißende Augen). */
    public int flags;
    /** Phase des Flügelschlags. */
    public float wing;
    /** Wie weit der Aufstieg in die Wahre Gestalt ist (0 – 1). */
    public float ascension;
    /** Wie hell er gerade aufleuchtet (Erscheinen, Aufsteigen, Lichtsprung, Neuschöpfung, Vergehen). */
    public float flash;
    /** Wie weit Beine und Klinge geformt sind (0 schwebend – 1 am Boden). */
    public float ground;
}
