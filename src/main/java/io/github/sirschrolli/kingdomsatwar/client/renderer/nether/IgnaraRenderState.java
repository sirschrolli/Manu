package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand von Ignara, der Flammenpriesterin. */
public class IgnaraRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code IgnaraEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** In der Phönixgestalt (Schwingen aus Flammen). */
    public boolean phoenix;
    /** Wie schnell sie sich gerade bewegt (Blöcke je Tick, waagerecht) – der Umhang weht. */
    public float drift;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
