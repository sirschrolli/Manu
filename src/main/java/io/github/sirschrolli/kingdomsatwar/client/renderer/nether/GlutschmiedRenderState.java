package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand von Malleus, dem Glutschmied. */
public class GlutschmiedRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code GlutschmiedEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Weißglühend (unter halber Kraft). */
    public boolean stoked;
    /** Trägt er den Glutspeer (oder liegt der noch auf dem Amboss)? */
    public boolean spear = true;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
