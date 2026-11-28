package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand des Seelenhirten. */
public class SoulShepherdRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code SoulShepherdEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Unter halber Kraft (im Zorn). */
    public boolean enraged;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel). */
    public @Nullable Object owner;
}
