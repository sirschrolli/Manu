package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Glutwurms braucht: Spucken, Biss, wie weit er sich aufgerichtet hat und ob er schwimmt. */
public class EmberWyrmRenderState extends LivingEntityRenderState {
    public final AnimationState spit = new AnimationState();
    public final AnimationState bite = new AnimationState();
    /** 0 im Lavastrom liegend, 1 wie eine Kobra aufgerichtet (weich überblendet). */
    public float rear;
    public boolean inLava;
}
