package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import io.github.sirschrolli.kingdomsatwar.client.model.wild.WildCamelModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildCamel;
import net.minecraft.client.model.animal.camel.CamelModel;
import net.minecraft.client.model.animal.camel.CamelSaddleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;

/** Kamel: Dromedar oder Trampeltier; Vanilla-Sattel. */
public class WildCamelRenderer extends WildRenderer<WildCamel, WildCamelRenderState, CamelModel> {
    private static final Identifier[] TEXTURES = textures("camel", "camel_", WildCamel.Breed.values(), "");

    public WildCamelRenderer(EntityRendererProvider.Context context) {
        super(context, new WildCamelModel(context.bakeLayer(WildRenderers.CAMEL)), 0.7f, new CamelRenderer(context));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.CAMEL_SADDLE,
                state -> state.saddle, new CamelSaddleModel(context.bakeLayer(ModelLayers.CAMEL_SADDLE)), null));
    }

    @Override
    protected void extract(WildCamel camel, WildCamelRenderState state, float partialTicks) {
        state.breed = camel.getBreed().ordinal();
    }

    @Override
    public Identifier getTextureLocation(WildCamelRenderState state) {
        return pick(TEXTURES, state.breed);
    }

    @Override
    public WildCamelRenderState createRenderState() {
        return new WildCamelRenderState();
    }
}
