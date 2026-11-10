package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Chorusschlunds braucht: wie weit der Schlund offen ist, Rankenhiebe, Biss und Spucken. */
public class ChorusMawRenderState extends LivingEntityRenderState {
    public final AnimationState lashLeft = new AnimationState();
    public final AnimationState lashRight = new AnimationState();
    public final AnimationState bite = new AnimationState();
    public final AnimationState spit = new AnimationState();
    /** 0 = geschlossen, 1 = offen (weich übergeblendet). */
    public float open;
}
