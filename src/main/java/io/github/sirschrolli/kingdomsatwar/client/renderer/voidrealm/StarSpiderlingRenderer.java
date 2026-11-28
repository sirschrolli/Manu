package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarSpiderlingEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Ein Sternspinnling: die Weberin in klein (dasselbe Modell, gut ein Drittel so groß), seine Sterne glimmen. */
public class StarSpiderlingRenderer extends MobRenderer<StarSpiderlingEntity, StarWeaverRenderState, StarWeaverModel> {
    public static final float SCALE = 0.34f;

    public StarSpiderlingRenderer(EntityRendererProvider.Context context) {
        super(context, new StarWeaverModel(context.bakeLayer(ModModelLayers.STAR_WEAVER)), 0.45f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, StarWeaverRenderState state, float yRot, float xRot) {
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, StarWeaverRenderer.GLOW, light, OverlayTexture.NO_OVERLAY,
                        ARGB.color(200, 0xFFFFFF), null, state.outlineColor);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(StarWeaverRenderState state) {
        return StarWeaverRenderer.TEXTURE;
    }

    @Override
    public StarWeaverRenderState createRenderState() {
        return new StarWeaverRenderState();
    }

    @Override
    public void extractRenderState(StarSpiderlingEntity entity, StarWeaverRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.spiderling = true;
        state.attack = 0;
        state.attackTime = 0.0f;
        state.hang = 0.0f;
        state.enraged = false;
        state.owner = entity;
    }

    @Override
    protected void scale(StarWeaverRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
