package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import net.minecraft.client.renderer.entity.CopperGolemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.BlockDecorationLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.CopperGolemRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Kupferknecht: eigenes Modell und je Stufe der Verwitterung ein eigenes Bild; die Glasaugen leuchten. Was er trägt,
 * die Blume auf dem Blitzableiter und ein Kopf auf dem Kopf werden wie bei Vanilla gezeichnet.
 */
public class CopperServantRenderer extends CopperGolemRenderer {
    static final String[] STAGES = {"kupfer", "angelaufen", "verwittert", "oxidiert"};
    static final Identifier[] TEXTURES = GolemRenderers.textures("copper_golem", "", STAGES);
    private static final Identifier[] GLOW = GolemRenderers.textures("copper_golem", "_glow", STAGES);

    public CopperServantRenderer(EntityRendererProvider.Context context) {
        super(context);
        CopperServantModel model = new CopperServantModel(context.bakeLayer(GolemRenderers.COPPER_GOLEM));
        this.model = model;
        this.layers.clear();                                   // Vanillas Ebenen hängen an Vanillas Modell
        this.addLayer(new LivingEntityEmissiveLayer<>(this, state -> GLOW[stage(state)], (state, ageInTicks) -> 1.0f,
                new CopperServantModel(context.bakeLayer(GolemRenderers.COPPER_GOLEM)), RenderTypes::eyes, false));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new BlockDecorationLayer<>(this, state -> state.blockOnAntenna, model::applyBlockOnAntennaTransform));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    static int stage(CopperGolemRenderState state) {
        return Mth.clamp(state.weathering.ordinal(), 0, STAGES.length - 1);
    }

    @Override
    public Identifier getTextureLocation(CopperGolemRenderState state) {
        return TEXTURES[stage(state)];
    }
}
