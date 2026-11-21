package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand von Phasmor, dem Leerwandlerkönig (und seinen Trugbildern). */
public class PhasmorRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code PhasmorEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Gekrönt (unter halber Kraft: die Krone lodert, er ist schneller). */
    public boolean crowned;
    /** Nur ein Trugbild (durchscheinend). */
    public boolean illusion;
    /** Wie schnell er sich gerade bewegt (Blöcke je Tick, waagerecht) – der Mantel weht. */
    public float drift;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel zwischen den Haltungen). */
    public @Nullable Object owner;
}
