package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jspecify.annotations.Nullable;

/** Zeichenzustand der Sternenweberin (und ihrer Brut, die dasselbe Modell klein trägt). */
public class StarWeaverRenderState extends LivingEntityRenderState {
    /** Laufender Angriff (Ordinalzahl von {@code StarWeaverEntity.Attack}) und Ticks darin. */
    public int attack;
    public float attackTime;
    /** Unter halber Kraft (im Zorn). */
    public boolean enraged;
    /** Wie sehr sie gerade kopfüber am Faden hängt (0 = steht, 1 = hängt). */
    public float hang;
    /** Ein Sternspinnling (dasselbe Modell, klein, ohne Angriffe). */
    public boolean spiderling;
    /** Das Wesen selbst (nur als Schlüssel für die weichen Wechsel). */
    public @Nullable Object owner;
}
