package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Schneeleoparden. */
public class SnowLeopardRenderState extends LivingEntityRenderState {
    public int variant;
    /** Wie tief er sich gerade duckt (Anschleichen, 0..1, weich). */
    public float stalk;
    /** Wie weit er liegt (Ruhen, auf Befehl, 0..1, weich). */
    public float lie;
    /** Wie sehr er rennt statt geht (0..1, weich). */
    public float run;
    public boolean tame;
    public final AnimationState pounce = new AnimationState();
    public final AnimationState swipe = new AnimationState();
    public final AnimationState eat = new AnimationState();
}
