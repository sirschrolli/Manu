package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import io.github.sirschrolli.kingdomsatwar.client.renderer.creature.CreatureRenderState;

/** Zeichenzustand der Kreaturen der Schattenwelt (nicht jede Kreatur nutzt jedes Feld). */
public class ShadowCreatureRenderState extends CreatureRenderState {
    /** Schattenpirscher: Sichtbarkeit 0..1. */
    public float visibility = 1.0f;
    /** Kristallkriecher: wie weit er sich an einer Wand aufgerichtet hat (0..1). */
    public float climb;
    /** Nachtschwinge: Flügelschlag-Zeit und Größe. */
    public float flapTime;
    public int size;
}
