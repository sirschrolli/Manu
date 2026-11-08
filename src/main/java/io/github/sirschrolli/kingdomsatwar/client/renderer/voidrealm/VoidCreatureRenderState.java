package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand der Wesen der Leere (nicht jedes Wesen nutzt jedes Feld). */
public class VoidCreatureRenderState extends CreatureRenderState {
    /** Leerenschlund: wie weit das Maul offen steht (0..1). Splitterwächter: wie wach er ist (0..1). */
    public float open;
    /** Splitterwächter: Erdstoß, Splittersalve und Erwachen (Faustschlag: {@link #attack}). Schlund: Zuschnappen. */
    public final AnimationState slam = new AnimationState();
    public final AnimationState volley = new AnimationState();
    public final AnimationState awaken = new AnimationState();
    /** Zerrling: Ticks seit dem letzten Sprung (groß = lange her) und eine Zahl je Wesen für sein Flackern. */
    public float sinceBlink = 100.0f;
    public double blinkDx;
    public double blinkDy;
    public double blinkDz;
    public int seed;
    /** Sternenrochen: Phase des Flügelschlags, Neigung in der Kurve (Grad) und Steigen/Sinken. */
    public float flap;
    public float bank;
    public float climb;
}
