package io.github.sirschrolli.kingdomsatwar.client.renderer.castle;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand von Kuno von Rabenfels, dem Raubritter. */
public class RobberKnightRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code RobberKnightEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Sitzt er im Sattel? Hat er seine Lanze (sie bricht an einem Schild)? */
    public boolean mounted = true;
    public boolean lance = true;
    /** Wie schnell er sich gerade bewegt (Blöcke je Tick, waagerecht) – das Ross galoppiert, der Wimpel flattert. */
    public float drift;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
