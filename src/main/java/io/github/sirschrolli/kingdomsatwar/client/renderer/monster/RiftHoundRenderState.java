package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Rissjägers braucht: Biss, Heulen und wie weit er gerade in die Leere zerfallen ist. */
public class RiftHoundRenderState extends LivingEntityRenderState {
    public final AnimationState bite = new AnimationState();
    public final AnimationState howl = new AnimationState();
    /** 0 = ganz da, 1 = ganz zerfallen (Phasensprung). */
    public float fade;
}
