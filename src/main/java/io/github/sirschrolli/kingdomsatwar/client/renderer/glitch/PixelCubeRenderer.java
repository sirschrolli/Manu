package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.PixelCubeEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;

/**
 * Ein Pixel aus dem Pixelregen: ein leuchtender Würfel mit hellen Kanten (wie ein Pixel aus einem alten Spiel), der sich
 * im Fallen langsam dreht; ein blasser, größerer Würfel umgibt ihn als Schein.
 */
public class PixelCubeRenderer extends EntityRenderer<PixelCubeEntity, PixelCubeRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/anni_troll/pixel.png"));
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends EntityRenderState {
        public int color;
        public float spin;
    }

    public PixelCubeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PixelCubeEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.color = entity.color();
        state.spin = (entity.tickCount + partialTicks) * 9.0f + entity.getYRot();
    }

    @Override
    protected int getBlockLightLevel(PixelCubeEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.3f, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(state.spin));
        poseStack.rotate(Axis.XP.rotationDegrees(state.spin * 0.6f));
        int core = ARGB.color(240, state.color);
        int halo = ARGB.color(70, state.color);
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> {
            cube(buffer, pose, 0.3f, core);
            cube(buffer, pose, 0.42f, halo);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    /** Ein Würfel mit Halbmesser {@code h} um den Ursprung (alle sechs Seiten, nach außen gewandt). */
    static void cube(VertexConsumer buffer, PoseStack.Pose pose, float h, int color) {
        // vorn/hinten (z), links/rechts (x), oben/unten (y)
        face(buffer, pose, color, -h, -h, h, h, -h, h, h, h, h, -h, h, h, 0, 0, 1);
        face(buffer, pose, color, h, -h, -h, -h, -h, -h, -h, h, -h, h, h, -h, 0, 0, -1);
        face(buffer, pose, color, h, -h, h, h, -h, -h, h, h, -h, h, h, h, 1, 0, 0);
        face(buffer, pose, color, -h, -h, -h, -h, -h, h, -h, h, h, -h, h, -h, -1, 0, 0);
        face(buffer, pose, color, -h, h, h, h, h, h, h, h, -h, -h, h, -h, 0, 1, 0);
        face(buffer, pose, color, -h, -h, -h, h, -h, -h, h, -h, h, -h, -h, h, 0, -1, 0);
    }

    private static void face(VertexConsumer buffer, PoseStack.Pose pose, int color, float x0, float y0, float z0, float x1, float y1, float z1,
            float x2, float y2, float z2, float x3, float y3, float z3, float nx, float ny, float nz) {
        buffer.addVertex(pose, x0, y0, z0).setColor(color).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x1, y1, z1).setColor(color).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x2, y2, z2).setColor(color).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x3, y3, z3).setColor(color).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, nx, ny, nz);
    }
}
