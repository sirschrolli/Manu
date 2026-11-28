package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand von Karkinos, dem Riffkoloss. */
public class ReefColossusRenderState extends LivingEntityRenderState {
    public final AnimationState awaken = new AnimationState();
    public final AnimationState slam = new AnimationState();
    public final AnimationState grab = new AnimationState();
    public final AnimationState fling = new AnimationState();
    public final AnimationState jet = new AnimationState();
    public final AnimationState burrow = new AnimationState();
    public final AnimationState erupt = new AnimationState();
    public final AnimationState roll = new AnimationState();
    public final AnimationState summon = new AnimationState();
    public final AnimationState tide = new AnimationState();
    public final AnimationState lance = new AnimationState();
    public final AnimationState stun = new AnimationState();
    /** Wie tief er im Sand steckt (0 oben, 1 ganz vergraben). */
    public float sink;
    public boolean rolling;
    public boolean jetting;
    public boolean stunned;
    public boolean spear = true;
    public boolean inTide;
}
