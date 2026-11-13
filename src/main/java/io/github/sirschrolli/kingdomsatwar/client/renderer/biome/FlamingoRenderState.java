package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Flamingos. */
public class FlamingoRenderState extends LivingEntityRenderState {
    public int variant;
    /** In der Luft (0..1, weich): Hals vor, Beine nach hinten, Flügel offen. */
    public float fly;
    /** Wie kräftig er gerade schlägt (weich). */
    public float flap;
    /** Seiht mit dem Kopf nach unten (0..1, weich). */
    public float feed;
    /** Steht auf einem Bein (0..1, weich). */
    public float rest;
    /** Nachts beim Ruhen: Kopf ins Gefieder (0..1, weich). */
    public float tuck;
    /** Schräglage in der Kurve (Bogenmaß, weich). */
    public float bank;
    /** Steht im Wasser. */
    public boolean wading;
    public final AnimationState call = new AnimationState();
}
