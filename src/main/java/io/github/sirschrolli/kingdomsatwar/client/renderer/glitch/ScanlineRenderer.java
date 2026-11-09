package io.github.sirschrolli.kingdomsatwar.client.renderer.glitch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.ScanlineEntity;
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
 * Ein Zeilenfehler: eine leuchtende Zeile (rot und tief oder blau und hoch), vierzehn Blöcke lang, die um Anni kreist. Sie
 * hat Farbsäume (ein wenig darüber und darunter versetzt) und zieht einen blassen Fächer hinter sich her, wie ein Radar.
 * Vor dem Fegen zeigt sie sich blass und blinkend an ihrer Startstelle.
 */
public class ScanlineRenderer extends EntityRenderer<ScanlineEntity, ScanlineRenderer.State> {
    private static final RenderType TYPE = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/anni_troll/scanline.png"));
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends EntityRenderState {
        public float height;
        public boolean low;
        public float angle;
        public float speed;
        public float age;
        public int sweep;
    }

    public ScanlineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ScanlineEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.height = entity.height();
        state.low = entity.isLow();
        state.age = entity.tickCount + partialTicks;
        state.angle = entity.angleAt(state.age);
        state.speed = entity.speed();
        state.sweep = entity.sweepTicks();
    }

    @Override
    protected int getBlockLightLevel(ScanlineEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected boolean affectedByCulling(ScanlineEntity entity) {
        return false;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        boolean warming = state.age < ScanlineEntity.WARMUP;
        float end = ScanlineEntity.WARMUP + state.sweep;
        float out = Mth.clamp((end + 3.0f - state.age) / 3.0f, 0.0f, 1.0f);
        if (warming && ((int) state.age / 2) % 2 == 1) {
            out *= 0.35f;
        }
        int main = state.low ? 0xFF3040 : 0x3080FF;
        float alpha = (warming ? 0.55f : 1.0f) * out;
        float r = ScanlineEntity.RADIUS;
        float a = state.angle * Mth.DEG_TO_RAD;
        float y = state.height;
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> {
            // die Zeile selbst (zwei gekreuzte Bänder, damit man sie von oben und von der Seite sieht)
            band(buffer, pose, a, r, y, 0.09f, ARGB.color(Math.round(alpha * 255.0f), main), true);
            band(buffer, pose, a, r, y, 0.09f, ARGB.color(Math.round(alpha * 255.0f), main), false);
            // Farbsäume
            band(buffer, pose, a, r, y + 0.07f, 0.03f, ARGB.color(Math.round(alpha * 150.0f), 0xFF40E0), true);
            band(buffer, pose, a, r, y - 0.07f, 0.03f, ARGB.color(Math.round(alpha * 150.0f), 0x30F0FF), true);
            if (!warming) {
                // der Fächer hinter der Zeile (wie ein Radar), in zwölf Schritten verblassend
                float dir = Math.signum(state.speed);
                for (int i = 0; i < 12; i++) {
                    float a0 = a - dir * i * 2.5f * Mth.DEG_TO_RAD;
                    float a1 = a - dir * (i + 1) * 2.5f * Mth.DEG_TO_RAD;
                    int c = ARGB.color(Math.round(alpha * 70.0f * (1.0f - i / 12.0f)), main);
                    wedge(buffer, pose, a0, a1, r, y, c);
                }
            }
        });
        super.submit(state, poseStack, collector, camera);
    }

    /** Ein Band von der Mitte bis zum Halbmesser in Richtung {@code a}: waagerecht (flach) oder senkrecht (hochkant). */
    private static void band(VertexConsumer buffer, PoseStack.Pose pose, float a, float r, float y, float w, int color, boolean flat) {
        float dx = Mth.cos(a);
        float dz = Mth.sin(a);
        float px = -dz * w;
        float pz = dx * w;
        float x0 = dx * 0.6f;
        float z0 = dz * 0.6f;
        float x1 = dx * r;
        float z1 = dz * r;
        if (flat) {
            quad(buffer, pose, x0 - px, y, z0 - pz, x1 - px, y, z1 - pz, x1 + px, y, z1 + pz, x0 + px, y, z0 + pz, color);
        } else {
            quad(buffer, pose, x0, y - w, z0, x1, y - w, z1, x1, y + w, z1, x0, y + w, z0, color);
        }
    }

    /** Ein flaches Tortenstück zwischen den Winkeln a0 und a1. */
    private static void wedge(VertexConsumer buffer, PoseStack.Pose pose, float a0, float a1, float r, float y, int color) {
        quad(buffer, pose, 0.0f, y, 0.0f, Mth.cos(a0) * r, y, Mth.sin(a0) * r, Mth.cos(a1) * r, y, Mth.sin(a1) * r, 0.0f, y, 0.0f, color);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, float x2,
            float y2, float z2, float x3, float y3, float z3, int color) {
        // beidseitig
        vertex(buffer, pose, x0, y0, z0, 0, 0, color);
        vertex(buffer, pose, x1, y1, z1, 1, 0, color);
        vertex(buffer, pose, x2, y2, z2, 1, 1, color);
        vertex(buffer, pose, x3, y3, z3, 0, 1, color);
        vertex(buffer, pose, x3, y3, z3, 0, 1, color);
        vertex(buffer, pose, x2, y2, z2, 1, 1, color);
        vertex(buffer, pose, x1, y1, z1, 1, 0, color);
        vertex(buffer, pose, x0, y0, z0, 0, 0, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
