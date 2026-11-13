package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Aschengeists braucht: Seelenfeuer, Aschewolke und Krallenhieb. */
public class AshWraithRenderState extends LivingEntityRenderState {
    public final AnimationState bolt = new AnimationState();
    public final AnimationState cloud = new AnimationState();
    public final AnimationState slash = new AnimationState();
    /** Wie schnell er gerade fliegt (für die Neigung und die nachwehende Kutte). */
    public float drift;
}
