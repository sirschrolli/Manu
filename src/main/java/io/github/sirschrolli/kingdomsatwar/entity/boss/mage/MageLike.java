package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.world.entity.AnimationState;

/** Was der Zeichner von einem Erzmagier (oder seinem Spiegelbild) wissen muss. */
public interface MageLike {
    DragonType element();

    /** Aktuelle Geste (siehe {@link MageBossEntity#ANIM_NONE} …). */
    byte gesture();

    /** Startzeit der aktuellen Geste (für Animationen). */
    AnimationState gestureAnimation();

    /** 0..1: wie sichtbar der Magier gerade ist (beim Teleportieren blendet er aus und ein). */
    float visibility(float partialTicks);

    /** 0..1: Stärke des Schutzschilds. */
    float shield(float partialTicks);

    /** Meditiert der Magier (schlafend im Turm)? */
    boolean dormant();

    /** Sinkt gerade sterbend zu Boden bzw. steigt auf (Ticks seit dem Tod, sonst 0). */
    int dyingTicks();

    /** Ein Spiegelbild (durchscheinend, flackernd)? */
    boolean isImage();
}
