package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulWispEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Ein Irrlicht: eine kleine Flamme aus Seelenfeuer mit zwei dunklen Augen, darum ein weicher, türkiser Schein (beides zum
 * Betrachter gedreht); die Flamme flackert und zuckt.
 */
public class SoulWispRenderer extends EntityRenderer<SoulWispEntity, EntityRenderState> {
    private static final RenderType BODY = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/soul_shepherd/wisp.png"));
    private static final RenderType HALO = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/soul_shepherd/wisp_glow.png"));
    private static final int FULL_BRIGHT = 0xF000F0;

    public SoulWispRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.ageInTicks;
        float flicker = 1.0f + 0.08f * Mth.sin(t * 1.3f) + 0.05f * Mth.sin(t * 2.9f + 1.0f);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.25f, 0.0f);
        poseStack.rotate(camera.orientation);
        poseStack.pushPose();
        poseStack.rotate(Axis.ZP.rotationDegrees(t * 11.0f));
        float halo = 1.35f * flicker;
        poseStack.scale(halo, halo, halo);
        int haloColor = ARGB.color(Math.round(150 + 60 * Mth.sin(t * 0.8f)), 0xFFFFFF);
        collector.submitCustomGeometry(poseStack, HALO, (pose, buffer) -> quad(pose, buffer, haloColor));
        poseStack.popPose();
        poseStack.rotate(Axis.ZP.rotationDegrees(Mth.sin(t * 0.5f) * 8.0f));
        poseStack.scale(0.7f, 0.7f * flicker, 0.7f);
        collector.submitCustomGeometry(poseStack, BODY, (pose, buffer) -> quad(pose, buffer, 0xFFFFFFFF));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
