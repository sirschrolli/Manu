package io.github.sirschrolli.kingdomsatwar.client.renderer;

import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Das Leuchten des Erzspürers (0.84) ist ein gewöhnliches Blockbild – gezeichnet wie jedes Blockbild von Minecraft. */
public class OreGlowRenderer extends DisplayRenderer.BlockDisplayRenderer {
    public OreGlowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
