package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Zeichenzustand Asterias, der Sternenreiterin. */
public class AsteriaRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code AsteriaEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Strahlend (nach der Supernova). */
    public boolean radiant;
    /** Hält sie die Lanze (oder fliegt die gerade als Komet)? */
    public boolean lance = true;
    /** Phase des Flügelschlags, Neigung in Kurven (Grad), Steigen (Blöcke je Tick). */
    public float flap;
    public float bank;
    public float climb;
}
