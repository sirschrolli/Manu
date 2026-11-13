package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand des Steinhüters. */
public class StoneKeeperRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code StoneKeeperEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Trägt er den Kristallpanzer? */
    public boolean armored;
    /** Unter halber Kraft. */
    public boolean enraged;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
