package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Moorschlurfers braucht: Wurf, Wurzelgriff, Aufrichten und wie tief er im Schlamm steckt. */
public class MireShamblerRenderState extends LivingEntityRenderState {
    public final AnimationState throwing = new AnimationState();
    public final AnimationState grasp = new AnimationState();
    public final AnimationState rise = new AnimationState();
    /** 0 aufrecht, 1 halb versunken (weich überblendet). */
    public float sunk;
}
