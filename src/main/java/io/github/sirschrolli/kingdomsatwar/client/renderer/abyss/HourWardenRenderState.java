package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand des Stundenwächters. */
public class HourWardenRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code HourWardenEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Unter halber Kraft (nach der Zwölften Stunde). */
    public boolean enraged;
    /** Stellung des Minutenzeigers (Grad, 0 = zwölf Uhr, im Uhrzeigersinn) und der Zahnräder (Grad). */
    public float minute;
    public float gears;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel). */
    public @Nullable Object owner;
}
