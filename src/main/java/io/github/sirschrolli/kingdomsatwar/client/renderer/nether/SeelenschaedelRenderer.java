package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;

/**
 * Die Schädel, die der Aschenkönig abfeuert (das Vanilla-Geschoss, eigenes Aussehen): ein verkohlter Seelenschädel mit
 * glühenden Augen und einem Schweif aus Seelenfeuer; der gefährliche Zornschädel lodert weißblau. Gedreht wie der
 * Vanilla-Schädel, das Gesicht voran.
 */
public class SeelenschaedelRenderer extends EntityRenderer<WitherSkull, WitherSkullRenderState> {
    private static final Identifier TEXTURE = NetherRenderers.texture("wither", "seelenschaedel");
    private static final Identifier ZORN = NetherRenderers.texture("wither", "seelenschaedel_zorn");
    private static final RenderType GLOW = RenderTypes.eyes(NetherRenderers.texture("wither", "seelenschaedel_glow"));
    private static final RenderType ZORN_GLOW = RenderTypes.eyes(NetherRenderers.texture("wither", "seelenschaedel_zorn_glow"));
    private final SkullModel model;

    public SeelenschaedelRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(NetherRenderers.SEELENSCHAEDEL));
    }

    @Override
    protected int getBlockLightLevel(WitherSkull entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(WitherSkullRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        collector.submitModel(this.model, state.modelState, poseStack, state.isDangerous ? ZORN : TEXTURE, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.order(1).submitModel(this.model, state.modelState, poseStack, state.isDangerous ? ZORN_GLOW : GLOW, state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    public WitherSkullRenderState createRenderState() {
        return new WitherSkullRenderState();
    }

    @Override
    public void extractRenderState(WitherSkull entity, WitherSkullRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isDangerous = entity.isDangerous();
        state.modelState.animationPos = 0.0f;
        state.modelState.yRot = entity.getYRot(partialTicks);
        state.modelState.xRot = entity.getXRot(partialTicks);
    }
}
