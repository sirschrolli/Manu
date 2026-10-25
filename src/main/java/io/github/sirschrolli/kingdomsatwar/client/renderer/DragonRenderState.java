package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemStack;

/** Alles, was zum Zeichnen eines Drachen in diesem Bild gebraucht wird. */
public class DragonRenderState extends LivingEntityRenderState {
    public DragonType dragonType = DragonType.FIRE;
    public DragonStage stage = DragonStage.HATCHLING;
    public boolean flying;
    public boolean gliding;
    public boolean sitting;
    public boolean breathing;
    public boolean ridden;
    public float flapPhase;
    public float flightPitch;
    /** Getragener Drachenpanzer (oder leer). */
    public ItemStack armor = ItemStack.EMPTY;
    public final AnimationState roarAnimation = new AnimationState();
    public final AnimationState biteAnimation = new AnimationState();
}
