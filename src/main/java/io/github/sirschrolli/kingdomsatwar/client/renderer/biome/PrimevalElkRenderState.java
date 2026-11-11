package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Urelchs. */
public class PrimevalElkRenderState extends LivingEntityRenderState {
    public boolean bull;
    public boolean saddled;
    public boolean chested;
    /** Grasen (0..1, aus dem Pferdeverhalten). */
    public float eat;
    /** Aufbäumen (0..1). */
    public float stand;
    /** Maul auf (0..1). */
    public float mouth;
    /** Ansturm mit gesenkten Schaufeln (0..1, weich). */
    public float charge;
    /** Traben statt Gehen (0..1, weich). */
    public float trot;
    public final AnimationState bugle = new AnimationState();
    public final AnimationState impact = new AnimationState();
}
