package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AfterimageEntity;
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
import net.minecraft.util.Mth;

/**
 * Nachbilder: Annis erstarrtes Abbild (abwechselnd magenta und cyan getönt, verblassend, ruckend) – und beim Rückruck das
 * flimmernde Abbild eines Wesens: eine Säule in seiner Größe aus Zeilen, mit Farbsäumen und Pixelrahmen.
 */
public class AfterimageRenderer extends EntityRenderer<AfterimageEntity, AfterimageRenderer.State> {
    private static final RenderType GHOST = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/anni_troll/ghost.png"));
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends EntityRenderState {
        public boolean troll;
        public float fade;
        public float width;
        public float height;
        public float yRot;
        public final AnniTrollRenderState pose = new AnniTrollRenderState();
    }

    private final AnniTrollModel model;

    public AfterimageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.model = new AnniTrollModel(context.bakeLayer(ModModelLayers.ANNI_TROLL));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AfterimageEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.troll = entity.isTroll();
        state.fade = entity.fade(partialTicks);
        state.width = entity.width();
        state.height = entity.height();
        state.yRot = entity.getYRot();
        AnniTrollRenderState pose = state.pose;
        pose.attack = entity.attack();
        pose.attackTime = entity.attackTime();
        pose.glitchMode = entity.isGlitchMode();
        pose.lagging = false;
        pose.sinceLag = 999.0f;
        pose.seed = entity.getId();
        // erstarrt: die Zeit steht (nur die Bildfehler wechseln je Tick)
        pose.ageInTicks = entity.tickCount + partialTicks;
        pose.walkAnimationSpeed = 0.0f;
        pose.bodyRot = entity.getYRot();
        pose.yRot = 0.0f;
        pose.xRot = 0.0f;
        pose.deathTime = 0.0f;
    }

    @Override
    protected int getBlockLightLevel(AfterimageEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fade <= 0.01f) {
            return;
        }
        if (state.troll) {
            this.submitTroll(state, poseStack, collector);
        } else {
            this.submitGhost(state, poseStack, collector);
        }
        super.submit(state, poseStack, collector, camera);
    }

    private void submitTroll(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        AnniTrollRenderState pose = state.pose;
        this.model.setupAnim(pose);
        boolean magenta = ((int) state.ageInTicks / 2) % 2 == 0;
        int color = ARGB.color(Math.round(state.fade * 150.0f), magenta ? 0xFF50E8 : 0x40E8FF);
        poseStack.pushPose();
        AnniTrollRenderer.jitter(pose, poseStack);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - state.yRot));
        poseStack.scale(-AnniTrollRenderer.SCALE, -AnniTrollRenderer.SCALE, AnniTrollRenderer.SCALE);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        collector.submitModel(this.model, pose, poseStack, AnniTrollRenderer.SPLIT, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color, null, 0);
        poseStack.popPose();
    }

    /** Eine Säule in Größe des Wesens aus waagerechten Zeilen (in Abschnitten zu einem halben Block); dazu ein Saum in Magenta. */
    private void submitGhost(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        float w = state.width * 0.5f + 0.05f;
        float h = state.height;
        float pulse = 0.6f + Mth.sin(state.ageInTicks * 0.8f) * 0.2f;
        int body = ARGB.color(Math.round(state.fade * pulse * 170.0f), 0x40E8FF);
        int edge = ARGB.color(Math.round(state.fade * 220.0f), 0xFF40E0);
        float shift = ((int) state.ageInTicks % 4) * 0.25f;
        collector.submitCustomGeometry(poseStack, GHOST, (pose, buffer) -> {
            float[][] sides = {{-w, -w, w, -w}, {w, -w, w, w}, {w, w, -w, w}, {-w, w, -w, -w}};
            for (float[] side : sides) {
                float x0 = side[0];
                float z0 = side[1];
                float x1 = side[2];
                float z1 = side[3];
                for (float y = 0.0f; y < h; y += 0.5f) {
                    float top = Math.min(h, y + 0.5f);
                    quad(buffer, pose, x0, y, z0, x1, top, z1, 0.0f, shift * 0.5f, 1.0f, shift * 0.5f + (top - y), body);
                }
                // ein Saum unten und oben
                quad(buffer, pose, x0, 0.0f, z0, x1, 0.08f, z1, 0.0f, 0.0f, 1.0f, 0.1f, edge);
                quad(buffer, pose, x0, h - 0.08f, z0, x1, h, z1, 0.0f, 0.0f, 1.0f, 0.1f, edge);
            }
        });
    }

    /** Ein senkrechtes, beidseitiges Viereck von (x0, y0, z0) nach (x1, y1, z1). */
    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, float u0,
            float v0, float u1, float v1, int color) {
        vertex(buffer, pose, x0, y0, z0, u0, v1, color);
        vertex(buffer, pose, x1, y0, z1, u1, v1, color);
        vertex(buffer, pose, x1, y1, z1, u1, v0, color);
        vertex(buffer, pose, x0, y1, z0, u0, v0, color);
        vertex(buffer, pose, x0, y1, z0, u0, v0, color);
        vertex(buffer, pose, x1, y1, z1, u1, v0, color);
        vertex(buffer, pose, x1, y0, z1, u1, v1, color);
        vertex(buffer, pose, x0, y0, z0, u0, v1, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
