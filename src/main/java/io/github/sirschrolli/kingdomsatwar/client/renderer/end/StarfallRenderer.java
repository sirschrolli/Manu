package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.end.StarfallEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Ein fallender Stern: ein vierzackiger, weiß glühender Stern (der sich langsam dreht und pulsiert) in einem bläulichen
 * Lichthof – beides immer der Kamera zugewandt und leuchtend; den Schweif zeichnen die Teilchen.
 */
public class StarfallRenderer extends EntityRenderer<StarfallEntity, StarfallRenderer.State> {
    private static final Identifier STAR = KingdomsAtWar.id("textures/entity/starfall/star.png");
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/starfall/halo.png");
    private static final RenderType STAR_TYPE = RenderTypes.eyes(STAR);
    private static final RenderType HALO_TYPE = RenderTypes.eyes(HALO);

    public static class State extends EntityRenderState {
        public boolean small;
        public float age;
    }

    public StarfallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StarfallEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.small = entity.isSmall();
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(StarfallEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float size = state.small ? 0.9f : 1.8f;
        float pulse = 1.0f + Mth.sin(state.age * 0.9f) * 0.12f;
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.25f, 0.0f);
        poseStack.rotate(camera.orientation);
        poseStack.pushPose();
        poseStack.scale(size * 1.9f, size * 1.9f, size * 1.9f);
        collector.submitCustomGeometry(poseStack, HALO_TYPE, (pose, buffer) -> quad(pose, buffer, ARGB.color(170, 0xFFFFFF)));
        poseStack.popPose();
        poseStack.rotate(Axis.ZP.rotationDegrees(state.age * 6.0f));
        poseStack.scale(size * pulse, size * pulse, size * pulse);
        collector.submitCustomGeometry(poseStack, STAR_TYPE, (pose, buffer) -> quad(pose, buffer, -1));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0, 1, color);
        vertex(buffer, pose, 0.5f, -0.5f, 1, 1, color);
        vertex(buffer, pose, 0.5f, 0.5f, 1, 0, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0, 0, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, int u, int v, int color) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
