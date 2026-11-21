package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.sea.InkBoltEntity;
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

/** Eine Kugel aus Hexentinte: ein dunkler, wirbelnder Kern, darum ein leuchtender, meergrüner Saum (zum Betrachter gedreht). */
public class InkBoltRenderer extends EntityRenderer<InkBoltEntity, EntityRenderState> {
    private static final RenderType INK = RenderTypes.entityTranslucent(KingdomsAtWar.id("textures/entity/sea_witch/ink_bolt.png"));
    private static final RenderType GLOW = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/sea_witch/ink_bolt_glow.png"));
    private static final int FULL_BRIGHT = 0xF000F0;

    public InkBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.ageInTicks;
        float size = 0.85f + 0.08f * Mth.sin(t * 0.9f);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.25f, 0.0f);
        poseStack.rotate(camera.orientation);
        poseStack.rotate(Axis.ZP.rotationDegrees(t * 24.0f));
        poseStack.scale(size, size, size);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, INK, (pose, buffer) -> quad(pose, buffer, 0xFFFFFFFF, light));
        poseStack.rotate(Axis.ZP.rotationDegrees(-t * 40.0f));
        poseStack.scale(1.15f, 1.15f, 1.15f);
        int glow = ARGB.color(Math.round(200 + 55 * Mth.sin(t * 0.7f)), 0xFFFFFF);
        collector.submitCustomGeometry(poseStack, GLOW, (pose, buffer) -> quad(pose, buffer, glow, FULL_BRIGHT));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color, int light) {
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 1.0f, color, light);
        vertex(buffer, pose, 0.5f, -0.5f, 1.0f, 1.0f, color, light);
        vertex(buffer, pose, 0.5f, 0.5f, 1.0f, 0.0f, color, light);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, color, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color, int light) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
