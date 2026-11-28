package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Inkfish;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.resources.Identifier;

/** Tintenfisch: eigenes Modell mit Pfeilflossen und Saugnapf-Armen; Schwimmstöße und Fangarme wie beim Vanilla-Tintenfisch. */
public class InkfishRenderer extends SquidRenderer<Inkfish> {
    private static final Identifier TEXTURE = WaterRenderers.texture("squid", "tintenfisch");

    public InkfishRenderer(EntityRendererProvider.Context context) {
        super(context, new SquidModel(context.bakeLayer(WaterRenderers.SQUID)), new SquidModel(context.bakeLayer(WaterRenderers.SQUID_BABY)));
    }

    @Override
    public Identifier getTextureLocation(SquidRenderState state) {
        return TEXTURE;
    }
}
