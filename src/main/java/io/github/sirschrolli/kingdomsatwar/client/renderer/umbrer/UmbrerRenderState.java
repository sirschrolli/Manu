package io.github.sirschrolli.kingdomsatwar.client.renderer.umbrer;

import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerProfession;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand der Umbrer und ihrer Wächter. */
public class UmbrerRenderState extends LivingEntityRenderState {
    public UmbrerProfession profession = UmbrerProfession.MOONSMITH;
    /** Handelt gerade (zeigt die Ware). */
    public boolean trading;
    /** Schüttelt den Kopf (nichts zu tauschen). */
    public boolean unhappy;
    /** Wächter: Kampfhaltung 0..1 (Speer gesenkt). */
    public float stance;
    /** Wächter: Speerstoß. */
    public final AnimationState thrust = new AnimationState();
}
