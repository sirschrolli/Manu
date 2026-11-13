package io.github.sirschrolli.kingdomsatwar.client.renderer.deep;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand von Sordin, dem Kantor der Stille. */
public class SordinRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code SordinEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Im Fortissimo (unter halber Kraft: die Pfeifen glühen, er ist schneller). */
    public boolean fortissimo;
    /** Wie schnell er sich gerade bewegt (Blöcke je Tick, waagerecht) – die Kutte schwingt. */
    public float drift;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
