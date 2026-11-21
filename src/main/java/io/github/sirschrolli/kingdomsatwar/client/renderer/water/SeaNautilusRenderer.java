package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaNautilus;
import net.minecraft.client.model.animal.nautilus.NautilusArmorModel;
import net.minecraft.client.model.animal.nautilus.NautilusModel;
import net.minecraft.client.model.animal.nautilus.NautilusSaddleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NautilusRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.NautilusRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;

/**
 * Nautilus: gestreiftes Perlboot mit Fangarmen an den Mundteilen. Der Panzer oben ist flach wie beim Vanilla-Nautilus,
 * deshalb sitzen Sattel und Nautilus-Rüstung genau wie bei Vanilla.
 */
public class SeaNautilusRenderer extends WildRenderer<SeaNautilus, NautilusRenderState, NautilusModel> {
    private static final Identifier TEXTURE = WaterRenderers.texture("nautilus", "nautilus");

    public SeaNautilusRenderer(EntityRendererProvider.Context context) {
        super(context, new NautilusModel(context.bakeLayer(WaterRenderers.NAUTILUS)), 0.7f, new NautilusRenderer<SeaNautilus>(context));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.NAUTILUS_BODY,
                state -> state.bodyArmorItem, new NautilusArmorModel(context.bakeLayer(ModelLayers.NAUTILUS_ARMOR)), null));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.NAUTILUS_SADDLE,
                state -> state.saddle, new NautilusSaddleModel(context.bakeLayer(ModelLayers.NAUTILUS_SADDLE)), null));
    }

    @Override
    public Identifier getTextureLocation(NautilusRenderState state) {
        return TEXTURE;
    }

    @Override
    public NautilusRenderState createRenderState() {
        return new NautilusRenderState();
    }
}
