package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Seelenreihers braucht: Stoß, Schwingenschlag, Sturzflug und wie sehr er gerade fliegt. */
public class SoulHeronRenderState extends LivingEntityRenderState {
    public final AnimationState stab = new AnimationState();
    public final AnimationState buffet = new AnimationState();
    public final AnimationState dive = new AnimationState();
    /** 0 = am Boden, 1 = im Flug (weich übergeblendet). */
    public float flying;
}
