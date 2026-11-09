package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicCardEntity;
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
import net.minecraft.util.Mth;

/**
 * Eine Zauberkarte: flach und um sich wirbelnd wie ein Wurfstern, oben die Kartenfarbe, unten das violett-goldene Muster;
 * immer hell. Die riesige Schaukarte („Zieh eine Karte!“) steht aufrecht, wendet sich dem Betrachter zu, zeigt erst den
 * Rücken und dreht sich dann um; ein heller Lichthof umgibt sie.
 */
public class MagicCardRenderer extends EntityRenderer<MagicCardEntity, MagicCardRenderer.State> {
    private static final String[] SUITS = {"heart", "spade", "diamond", "club"};
    private static final RenderType[] FRONT = new RenderType[4];
    private static final RenderType BACK = RenderTypes.entityCutoutCull(texture("card_back"));
    private static final RenderType HALO = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/starfall/halo.png"));

    static {
        for (int i = 0; i < 4; i++) {
            FRONT[i] = RenderTypes.entityCutoutCull(texture("card_" + SUITS[i]));
        }
    }

    private static Identifier texture(String name) {
        return KingdomsAtWar.id("textures/entity/miss_mystik/" + name + ".png");
    }

    public static class State extends EntityRenderState {
        public int suit;
        public boolean show;
        public float flip;
        public float age;
        public float yRot;
        public float xRot;
    }

    public MagicCardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagicCardEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.suit = entity.suit();
        state.show = entity.isShow();
        state.flip = entity.flip(partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
    }

    @Override
    protected int getBlockLightLevel(MagicCardEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderType front = FRONT[Mth.clamp(state.suit, 0, 3)];
        poseStack.pushPose();
        if (state.show) {
            // aufrecht, dem Betrachter zugewandt, sich umdrehend; sanft schwebend
            float w = 1.0f;
            float h = 1.45f;
            poseStack.translate(0.0f, Mth.sin(state.age * 0.15f) * 0.1f, 0.0f);
            poseStack.rotate(Axis.YP.rotationDegrees(180.0f - camera.yRot + 180.0f * (1.0f - state.flip)));
            poseStack.pushPose();
            poseStack.scale(4.2f, 4.2f, 4.2f);
            poseStack.translate(0.0f, 0.0f, -0.01f);
            collector.submitCustomGeometry(poseStack, HALO, (pose, buffer) -> {
                vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0, 1, 0x80FFC8F0);
                vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0, 0, 0x80FFC8F0);
                vertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1, 0, 0x80FFC8F0);
                vertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1, 1, 0x80FFC8F0);
            });
            poseStack.popPose();
            collector.submitCustomGeometry(poseStack, front, (pose, buffer) -> {
                vertex(buffer, pose, -w, -h, 0.0f, 0, 1, -1);
                vertex(buffer, pose, w, -h, 0.0f, 1, 1, -1);
                vertex(buffer, pose, w, h, 0.0f, 1, 0, -1);
                vertex(buffer, pose, -w, h, 0.0f, 0, 0, -1);
            });
            collector.submitCustomGeometry(poseStack, BACK, (pose, buffer) -> {
                vertex(buffer, pose, -w, -h, 0.0f, 0, 1, -1);
                vertex(buffer, pose, -w, h, 0.0f, 0, 0, -1);
                vertex(buffer, pose, w, h, 0.0f, 1, 0, -1);
                vertex(buffer, pose, w, -h, 0.0f, 1, 1, -1);
            });
        } else {
            // flach, in Flugrichtung, wirbelnd
            float w = 0.19f;
            float h = 0.27f;
            poseStack.translate(0.0f, 0.12f, 0.0f);
            poseStack.rotate(Axis.YP.rotationDegrees(state.yRot));
            poseStack.rotate(Axis.XP.rotationDegrees(-state.xRot * 0.5f));
            poseStack.rotate(Axis.YP.rotationDegrees(state.age * 47.0f));
            collector.submitCustomGeometry(poseStack, front, (pose, buffer) -> {
                flat(buffer, pose, -w, -h, 0, 0);
                flat(buffer, pose, -w, h, 0, 1);
                flat(buffer, pose, w, h, 1, 1);
                flat(buffer, pose, w, -h, 1, 0);
            });
            collector.submitCustomGeometry(poseStack, BACK, (pose, buffer) -> {
                flatDown(buffer, pose, -w, -h, 0, 0);
                flatDown(buffer, pose, w, -h, 1, 0);
                flatDown(buffer, pose, w, h, 1, 1);
                flatDown(buffer, pose, -w, h, 0, 1);
            });
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, int u, int v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0.0f, 0.0f, 1.0f);
    }

    private static void flat(VertexConsumer buffer, PoseStack.Pose pose, float x, float z, int u, int v) {
        buffer.addVertex(pose, x, 0.0f, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void flatDown(VertexConsumer buffer, PoseStack.Pose pose, float x, float z, int u, int v) {
        buffer.addVertex(pose, x, -0.005f, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0.0f, -1.0f, 0.0f);
    }
}
