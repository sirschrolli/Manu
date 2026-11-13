package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.GhostNautilus;
import net.minecraft.client.model.animal.nautilus.NautilusArmorModel;
import net.minecraft.client.model.animal.nautilus.NautilusModel;
import net.minecraft.client.model.animal.nautilus.NautilusSaddleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.nautilus.ZombieNautilusCoralModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieNautilusRenderer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.NautilusRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;

/**
 * Geisternautilus: verwittertes, bleiches Gehäuse mit Seepocken und Tang, seegrün glimmende Augen. Unter der Rüstung
 * verschwinden Seepocken und Tang wie die Korallen beim Vanilla-Zombienautilus; Sattel und Rüstung sitzen wie bei Vanilla.
 */
public class GhostNautilusRenderer extends WildRenderer<GhostNautilus, NautilusRenderState, NautilusModel> {
    private static final Identifier TEXTURE = WaterRenderers.texture("nautilus", "geisternautilus");
    private static final RenderType GLOW = RenderTypes.eyes(WaterRenderers.texture("nautilus", "geisternautilus_glow"));

    public GhostNautilusRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieNautilusCoralModel(context.bakeLayer(WaterRenderers.NAUTILUS)), 0.7f, new ZombieNautilusRenderer(context));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.NAUTILUS_BODY,
                state -> state.bodyArmorItem, new NautilusArmorModel(context.bakeLayer(ModelLayers.NAUTILUS_ARMOR)), null));
        this.addLayer(new SimpleEquipmentLayer<>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.NAUTILUS_SADDLE,
                state -> state.saddle, new NautilusSaddleModel(context.bakeLayer(ModelLayers.NAUTILUS_SADDLE)), null));
        this.addLayer(new GlowLayer<>(this, state -> GLOW));
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
