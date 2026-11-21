package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Greifen. */
public class GriffinRenderState extends LivingEntityRenderState {
    public int variant;
    /** In der Luft (0..1, weich): Schwingen ausgebreitet, Läufe angelegt. */
    public float fly;
    /** Wie kräftig er gerade schlägt (0 = segeln, 1 = schlagen; weich). */
    public float flap;
    /** Im Sturzflug (0..1, weich): Schwingen zurückgelegt. */
    public float dive;
    /** Sitzt aufrecht und ruht (0..1, weich). */
    public float rest;
    /** Schräglage in der Kurve (Bogenmaß, weich). */
    public float bank;
    /** Neigung nach oben/unten im Flug (Bogenmaß, weich). */
    public float pitch;
    public final AnimationState screech = new AnimationState();
    public final AnimationState strike = new AnimationState();
    public final AnimationState swipe = new AnimationState();
    public final AnimationState eat = new AnimationState();
}
