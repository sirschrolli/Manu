package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Kraken. */
public class KrakenRenderState extends LivingEntityRenderState {
    public final AnimationState roar = new AnimationState();
    public final AnimationState slamLeft = new AnimationState();
    public final AnimationState slamRight = new AnimationState();
    public final AnimationState sweepLeft = new AnimationState();
    public final AnimationState sweepRight = new AnimationState();
    public final AnimationState grab = new AnimationState();
    public final AnimationState ink = new AnimationState();
    public final AnimationState whirl = new AnimationState();
    public final AnimationState wave = new AnimationState();
    /** Welcher Fangarm greift (0 links, 1 rechts). */
    public int grabSide;
    /** Wie tief er im Schlund steckt (0 aufgetaucht, 1 ganz unten). */
    public float sink;
    /** Raserei (unter einem Drittel der Kraft): schneller, heller. */
    public boolean enraged;
    /** Wie weit er sich im Strudel gedreht hat (Grad). */
    public float spin;
}
