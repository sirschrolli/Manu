package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Werwolfs braucht: Hieb, Heulen, Ansprung und wie weit er auf alle viere gesunken ist. */
public class WerewolfRenderState extends LivingEntityRenderState {
    public final AnimationState swipe = new AnimationState();
    public final AnimationState howl = new AnimationState();
    public final AnimationState pounce = new AnimationState();
    /** 0 aufrecht, 1 auf allen vieren (weich überblendet). */
    public float run;
}
