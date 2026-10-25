package io.github.sirschrolli.kingdomsatwar.client.model;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** Modell-Ebenen der Mob-Modelle. */
public final class ModModelLayers {
    public static final ModelLayerLocation DRAGON = new ModelLayerLocation(KingdomsAtWar.id("dragon"), "main");
    /** Soldaten und Banditen. */
    public static final ModelLayerLocation SOLDIER = new ModelLayerLocation(KingdomsAtWar.id("soldier"), "main");

    private ModModelLayers() {}
}
