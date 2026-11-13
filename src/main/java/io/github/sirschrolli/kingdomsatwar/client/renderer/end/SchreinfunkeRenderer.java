package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.object.projectile.ShulkerBulletModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ShulkerBulletRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;

/**
 * Die Geschosse der Schreinlinge (das Vanilla-Geschoss, eigenes Aussehen): ein Stern aus Kristallnadeln, der sich
 * taumelnd dreht, leuchtet und von einem schwachen Schein umgeben ist.
 */
public class SchreinfunkeRenderer extends EntityRenderer<ShulkerBullet, ShulkerBulletRenderState> {
    private static final Identifier TEXTURE = EndRenderers.texture("shulker", "schreinfunke");
    private static final RenderType GLOW = RenderTypes.eyes(EndRenderers.texture("shulker", "schreinfunke_glow"));
    private static final RenderType AURA = RenderTypes.entityTranslucent(TEXTURE);
    private final ShulkerBulletModel model;

    public SchreinfunkeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ShulkerBulletModel(context.bakeLayer(EndRenderers.SCHREINFUNKE));
    }

    @Override
    protected int getBlockLightLevel(ShulkerBullet entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(ShulkerBulletRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        float t = state.ageInTicks;
        poseStack.translate(0.0f, 0.15f, 0.0f);
        poseStack.rotateDegrees(Axis.YP, Mth.sin(t * 0.1f) * 180.0f);
        poseStack.rotateDegrees(Axis.XP, Mth.cos(t * 0.1f) * 180.0f);
        poseStack.rotateDegrees(Axis.ZP, Mth.sin(t * 0.15f) * 360.0f);
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        collector.order(1).submitModel(this.model, state, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        // violetter Schein (Farbe 0x30B060FF: durchscheinend, violett getönt)
        collector.order(2).submitModel(this.model, state, poseStack, AURA, state.lightCoords, OverlayTexture.NO_OVERLAY, 0x30B060FF, null,
                state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    public ShulkerBulletRenderState createRenderState() {
        return new ShulkerBulletRenderState();
    }

    @Override
    public void extractRenderState(ShulkerBullet entity, ShulkerBulletRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
    }
}
