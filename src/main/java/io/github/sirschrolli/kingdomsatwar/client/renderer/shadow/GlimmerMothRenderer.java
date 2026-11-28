package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.shadow.GlimmerMothModel;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.GlimmerMothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Glimmerfalter: die Augenflecken auf den Flügeln glimmen silberblau. */
public class GlimmerMothRenderer extends MobRenderer<GlimmerMothEntity, ShadowCreatureRenderState, GlimmerMothModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/glimmer_moth/glimmer_moth.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/glimmer_moth/glimmer_moth_glow.png"));

    public GlimmerMothRenderer(EntityRendererProvider.Context context) {
        super(context, new GlimmerMothModel(context.bakeLayer(ModModelLayers.GLIMMER_MOTH)), 0.15f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(ShadowCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(GlimmerMothEntity entity, BlockPos pos) {
        return Math.max(8, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ShadowCreatureRenderState createRenderState() {
        return new ShadowCreatureRenderState();
    }
}
