package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Was das Modell des Nachtwidders braucht: Hornstoß, Scharren, Aufbäumen, Benommenheit und den Ansturm. */
public class NightRamRenderState extends LivingEntityRenderState {
    public final AnimationState butt = new AnimationState();
    public final AnimationState prepare = new AnimationState();
    public final AnimationState stomp = new AnimationState();
    public final AnimationState stun = new AnimationState();
    /** Zustand des Widders (siehe NightRamEntity: IDLE, PREPARE, CHARGE, STUNNED, STOMP). */
    public byte ramState;
    /** 0..1: wie tief er den Kopf zum Ansturm gesenkt hat (weich übergeblendet). */
    public float lowered;
}
