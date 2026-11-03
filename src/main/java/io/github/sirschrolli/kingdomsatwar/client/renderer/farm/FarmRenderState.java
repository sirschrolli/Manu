package io.github.sirschrolli.kingdomsatwar.client.renderer.farm;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.DyeColor;

/** Zeichenzustand der eigenen Hof- und Weidetiere (nicht jedes Tier nutzt jedes Feld). */
public class FarmRenderState extends LivingEntityRenderState {
    /** Rasse (Index in der Rassen-Aufzählung des Tiers). */
    public int breed;
    /** Schwein: gesattelt. */
    public boolean saddled;
    /** Schaf: geschoren, Wollfarbe, "jeb_", Grasen. */
    public boolean sheared;
    public DyeColor woolColor = DyeColor.WHITE;
    public boolean jeb;
    public float headEatPosition;
    public float headEatAngle;
    /** Huhn: Flügelschlag. */
    public float flap;
    public float flapSpeed;
    /** Ziege: Hörner und Rammen. */
    public boolean leftHorn = true;
    public boolean rightHorn = true;
    public float rammingXHeadRot;
    /** Hase: Fortschritt des Sprungs (0..1). */
    public float jumpCompletion;
}
