package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Zeichenzustand MissMystiks (und ihrer Spiegelbilder). */
public class MissMystikRenderState extends LivingEntityRenderState {
    /** Laufender Trick (Ordinalzahl von {@code MissMystikEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Zugabe (unter 60 %). */
    public boolean encore;
    /** Trägt sie den Zylinder? */
    public boolean hat = true;
    /** Phase des Flügelschlags. */
    public float flap;
    /** Ein Spiegelbild (durchscheinend, schimmernd)? */
    public boolean image;
    /** Wie schnell sie gerade schwebt (Blöcke je Tick). */
    public float speed;
}
