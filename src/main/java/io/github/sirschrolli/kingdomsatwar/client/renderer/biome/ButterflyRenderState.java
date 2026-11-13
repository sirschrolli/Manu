package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Zeichenzustand eines Schmetterlings. */
public class ButterflyRenderState extends LivingEntityRenderState {
    public int variant;
    /** Sitzt (0..1, weich): Flügel zusammengeklappt, ab und zu langsam geöffnet. */
    public float perch;
    /** Flügel beim Sitzen fest geöffnet (Vorführung ohne KI), sonst -1. */
    public float openWings = -1.0f;
    /** Eigener Takt je Falter, damit ein Schwarm nicht im Gleichschritt flattert. */
    public float offset;
}
