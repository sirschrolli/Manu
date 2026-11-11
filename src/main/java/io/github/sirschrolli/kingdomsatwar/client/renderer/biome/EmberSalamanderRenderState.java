package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Zeichenzustand des Glutsalamanders. */
public class EmberSalamanderRenderState extends LivingEntityRenderState {
    public int variant;
    /** Liegt flach und sonnt sich (0..1, weich). */
    public float bask;
    /** Eigener Takt, damit nicht alle im Gleichschritt züngeln. */
    public float offset;
}
