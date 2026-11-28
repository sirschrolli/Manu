package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand der Meereswesen (nicht jedes Wesen nutzt jedes Feld). */
public class SeaRenderState extends LivingEntityRenderState {
    /** Leuchtqualle: Farbe (0 Meerblau, 1 Rosa, 2 Bernstein). */
    public int variant;
    /** Mantarochen: Flügelschlag, Neigung in der Kurve (Grad), Drehung beim Sprung (0..1). Hai und Anglerfisch: Schwimmphase. */
    public float flap;
    public float bank;
    public float spin;
    public float swim;
    /** Leuchtqualle: Ticks seit dem letzten Pumpstoß. */
    public float sincePulse = 100.0f;
    /** Ob das Wesen im Wasser ist (an Land zappeln die Fische). */
    public boolean inWater = true;
    /** Riesenkrabbe: wie weit sie sich zum Seitwärtslaufen gedreht hat (0..1). */
    public float sideways;
    /** Biss, Zuschnappen, Kneifen. */
    public final AnimationState attack = new AnimationState();
    /** Riesenkrabbe: Scherenklappern. */
    public final AnimationState special = new AnimationState();
}
