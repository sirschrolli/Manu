package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand der Wesen des Feenreichs (nicht jedes Wesen nutzt jedes Feld). */
public class FaeRenderState extends LivingEntityRenderState {
    /** Gestalt (Farbe). */
    public int variant;
    /** Pilzling: wie weit er sich unter seinen Hut geduckt hat (0..1). */
    public float hide;
    /** Dornwicht: Klauenhieb und Dornensalve. */
    public final AnimationState slash = new AnimationState();
    public final AnimationState volley = new AnimationState();
    /** Mottenkönigin: laufender Angriff (Ordinalzahl), Ticks im Angriff, rasend. */
    public int attack;
    public float attackTime;
    public boolean enraged;
}
