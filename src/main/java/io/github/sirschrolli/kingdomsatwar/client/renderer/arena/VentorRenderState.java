package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand von Ventor, dem Gladiator der Winde. */
public class VentorRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code VentorEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Hält er Dreizack und Netz in den Händen? Hat er den Sturm entfesselt? */
    public boolean trident = true;
    public boolean net = true;
    public boolean enraged;
    /** Wie schnell er sich gerade bewegt (Blöcke je Tick, waagerecht) – der Wirbel neigt sich, der Schurz weht. */
    public float drift;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
