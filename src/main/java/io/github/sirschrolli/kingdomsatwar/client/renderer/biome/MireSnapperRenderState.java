package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Moorschnappers. */
public class MireSnapperRenderState extends LivingEntityRenderState {
    /** In den Panzer zurückgezogen (0..1, weich). */
    public float withdraw;
    /** Liegt im Wasser. */
    public boolean submerged;
    public float offset;
    public final AnimationState snap = new AnimationState();
    public final AnimationState hiss = new AnimationState();
}
