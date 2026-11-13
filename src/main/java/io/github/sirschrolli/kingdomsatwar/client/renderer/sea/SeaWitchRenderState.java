package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand der Meerhexe. */
public class SeaWitchRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code SeaWitchEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Unter halber Kraft (im Zorn). */
    public boolean enraged;
    /** Wie schnell sie schwimmt (0 = sie tritt Wasser, 1 = volle Fahrt). */
    public float swim;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel). */
    public @Nullable Object owner;
}
