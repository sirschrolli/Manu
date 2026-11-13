package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.client.boss.BossRenderState;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemStack;

/**
 * Alles, was zum Zeichnen eines Drachen in diesem Bild gebraucht wird. Die Bewegungen entstehen wie bei den Drachen-Bossen:
 * eine Zielhaltung aus Zustand, Gang, Flug und Laune, von Federn geglättet (die Felder dafür erbt der Zustand von
 * {@link BossRenderState}).
 */
public class DragonRenderState extends BossRenderState {
    public DragonType dragonType = DragonType.FIRE;
    public DragonStage stage = DragonStage.HATCHLING;
    public boolean flying;
    public boolean sitting;
    public boolean breathing;
    public boolean ridden;
    /** 0 = stehend, 1 = sitzend (weich). */
    public float sit;
    /** Weg in Schrittfolgen und wie kräftig der Drache schreitet (0 = steht, 1 = Galopp). */
    public float stride;
    public float strideSpeed;
    /** Sekunden seit Beginn des Odems (oder -1). */
    public float breathTime = -1.0f;
    /** Getragener Drachenpanzer (oder leer). */
    public ItemStack armor = ItemStack.EMPTY;
    public final AnimationState roarAnimation = new AnimationState();
    public final AnimationState biteAnimation = new AnimationState();
    /** Wohin der Biss geht: -1 hinab, 0 geradeaus, 1 hinauf. */
    public float biteDip;
}
