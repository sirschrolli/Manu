package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim;
import io.github.sirschrolli.kingdomsatwar.entity.arena.GladiatorNetEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * Das Wurfnetz im Flug: ein rundes Netz mit Bleigewichten am Rand, das sich im Flug öffnet und dreht – es zeigt zum Ziel wie
 * ein ausgeworfenes Fangnetz, der Rand hängt ein wenig durch.
 */
public class GladiatorNetRenderer extends EntityRenderer<GladiatorNetEntity, GladiatorNetRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityCutout(KingdomsAtWar.id("textures/entity/ventor/net.png"));
    /** Halber Durchmesser des offenen Netzes (Blöcke). */
    private static final float RADIUS = 0.85f;
    private static final int SEGMENTS = 12;
    private final float[] aim = new float[2];

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public float age;
    }

    public GladiatorNetRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GladiatorNetEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        FlightAim.aim(entity, partialTicks, false, false, this.aim);
        state.yRot = this.aim[0];
        state.xRot = this.aim[1];
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float open = Mth.clamp(state.age / 7.0f, 0.25f, 1.0f);
        float spin = state.age * 0.35f;
        float sag = 0.35f * open;
        int light = state.lightCoords;
        poseStack.pushPose();
        // die Fläche des Netzes steht quer zur Flugbahn (es öffnet sich zum Ziel hin) und dreht sich um sie
        poseStack.rotate(Axis.YP.rotationDegrees(state.yRot));
        poseStack.rotate(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.rotate(Axis.ZP.rotation(spin));
        poseStack.scale(open, open, open);
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> {
            // ein Fächer aus Dreiecken (als Vierecke): die Mitte vorn, der Rand hängt nach hinten durch
            for (int i = 0; i < SEGMENTS; i++) {
                float a0 = i * Mth.TWO_PI / SEGMENTS;
                float a1 = (i + 1) * Mth.TWO_PI / SEGMENTS;
                float x0 = Mth.cos(a0) * RADIUS;
                float y0 = Mth.sin(a0) * RADIUS;
                float x1 = Mth.cos(a1) * RADIUS;
                float y1 = Mth.sin(a1) * RADIUS;
                vertex(buffer, pose, 0.0f, 0.0f, 0.0f, 0.5f, 0.5f, light);
                vertex(buffer, pose, x0, y0, -sag, 0.5f + Mth.cos(a0) * 0.5f, 0.5f + Mth.sin(a0) * 0.5f, light);
                vertex(buffer, pose, x1, y1, -sag, 0.5f + Mth.cos(a1) * 0.5f, 0.5f + Mth.sin(a1) * 0.5f, light);
                vertex(buffer, pose, 0.0f, 0.0f, 0.0f, 0.5f, 0.5f, light);
            }
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0f, 0.0f, 1.0f);
    }
}
