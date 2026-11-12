package io.github.sirschrolli.kingdomsatwar.client.renderer.merfolk;

import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidLook;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidProfession;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand des Meervolks (seit 0.93). */
public class MermaidRenderState extends LivingEntityRenderState {
    public MermaidProfession profession = MermaidProfession.PEARL_DIVER;
    public MermaidLook look = MermaidLook.CORAL;
    /** 0 = aufrecht wassertretend, 1 = waagrecht schwimmend. */
    public float swimPose;
    /** Phase des Schwanzschlags. */
    public float swimPhase;
    /** Im Wasser (sonst liegt sie und zappelt). */
    public boolean inWater = true;
    /** Wie weit die Ware gerade vorgezeigt wird bzw. der Kopf geschüttelt wird (0..1, weich). */
    public float show;
    public float sulk;
    /** Wache: Kampfhaltung 0..1 (Dreizack gesenkt). */
    public float stance;
    /** Wache: Stoß und Wurf. */
    public final AnimationState thrust = new AnimationState();
    public final AnimationState toss = new AnimationState();
}
