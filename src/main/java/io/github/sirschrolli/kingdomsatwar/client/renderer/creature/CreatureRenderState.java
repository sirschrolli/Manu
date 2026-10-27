package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand der wilden Kreaturen (nicht jede Kreatur nutzt jedes Feld). */
public class CreatureRenderState extends LivingEntityRenderState {
    /** Aussehen (z. B. Graugans/Hausgans). */
    public int variant;
    /** Wildschwein: stürmt los · Hirsch: männlich · Gans/Troll: wütend. */
    public boolean charging;
    public boolean stag;
    public boolean angry;
    /** Troll im Sonnenlicht. */
    public boolean petrifying;
    /** Gans: Flügelschlag. */
    public float flap;
    public float flapSpeed;
    /** Angriff (Biss, Keulenhieb, Hauerstoß, Schnattern). */
    public final AnimationState attack = new AnimationState();
    /** Besondere Bewegung (Grasen, Schwanzhieb). */
    public final AnimationState special = new AnimationState();
}
