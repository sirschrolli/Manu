package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.AnimationState;

/** Zeichenzustand der Sturmmöwe. */
public class StormGullRenderState extends LivingEntityRenderState {
    public int variant;
    /** In der Luft (0..1, weich): Flügel ausgebreitet, Beine angelegt. */
    public float fly;
    /** Wie kräftig sie gerade schlägt (0 = gleiten, 1 = schlagen; weich). */
    public float flap;
    /** Schräglage in der Kurve (Bogenmaß, weich). */
    public float bank;
    /** Neigung nach oben/unten (Bogenmaß). */
    public float pitch;
    /** Schwimmt auf dem Wasser. */
    public boolean floating;
    public final ItemStackRenderState carried = new ItemStackRenderState();
    public final AnimationState call = new AnimationState();
    public final AnimationState peck = new AnimationState();
    public final AnimationState snatch = new AnimationState();
}
