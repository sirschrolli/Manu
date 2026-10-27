package io.github.sirschrolli.kingdomsatwar.client.renderer.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Zeichenzustand eines Erzmagiers (oder Spiegelbilds). */
public class MageRenderState extends LivingEntityRenderState {
    public DragonType element = DragonType.FIRE;
    /** Aktuelle Geste (siehe {@link MageBossEntity#ANIM_CAST} …). */
    public byte gesture = MageBossEntity.ANIM_NONE;
    /** Sekunden seit Beginn der Geste (-1 = keine). */
    public float gestureTime = -1.0f;
    /** 0..1 sichtbar (Blinzeln, Spiegelbild). */
    public float visibility = 1.0f;
    /** 0..1 Stärke des Schilds. */
    public float shield;
    public boolean dormant;
    /** Ticks seit dem Tod (0 = lebt). */
    public float dying;
    public boolean image;
    /** Abstand zum Boden unter dem Magier (für das Siegel). */
    public float floorOffset = 0.5f;
}
