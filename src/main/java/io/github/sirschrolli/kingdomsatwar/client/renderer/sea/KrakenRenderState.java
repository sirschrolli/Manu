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
    /** Seit 1.00: Rammstoß, Abtauchen, Ausbruch, Geysirfeld, Tentakelkäfig. */
    public final AnimationState charge = new AnimationState();
    public final AnimationState dive = new AnimationState();
    public final AnimationState breach = new AnimationState();
    public final AnimationState geyser = new AnimationState();
    public final AnimationState cage = new AnimationState();
    /** Welcher Fangarm greift (0 links, 1 rechts). */
    public int grabSide;
    /** Wie tief er im Schlund steckt (0 aufgetaucht, 1 ganz unten, kurz unter 0, wenn er aus dem Wasser bricht). */
    public float sink;
    /** Raserei (unter einem Drittel der Kraft): schneller, heller. */
    public boolean enraged;
    /** Wie weit er sich im Strudel gedreht hat (Grad). */
    public float spin;
    /** Wie kräftig er schwimmt (0 ruhig, 1 volle Fahrt, darüber im Rammstoß). */
    public float swim;
}
