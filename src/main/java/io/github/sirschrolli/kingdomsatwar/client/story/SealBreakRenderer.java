package io.github.sirschrolli.kingdomsatwar.client.story;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.SealBreakEntity;
import io.github.sirschrolli.kingdomsatwar.world.SealCircle;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLayout;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Die Lichteffekte des Siegelbruchs:
 * <ol>
 *     <li>Aus den Decksteinen der sechs Monolithen steigen Lichtsäulen in den Farben der Elemente.</li>
 *     <li>Strahlen laufen von den Monolithen zu einem Punkt über der Mitte, dort wächst eine pulsierende Lichtkugel mit
 *     immer mehr Lichtstrahlen.</li>
 *     <li>Das Siegel zerspringt: greller Blitz, ein Strahlenkranz und eine Druckwelle über den Boden.</li>
 *     <li>Aus dem offenen Tor steigt eine Säule aus Schatten.</li>
 * </ol>
 */
public class SealBreakRenderer extends EntityRenderer<SealBreakEntity, SealBreakRenderer.State> {
    private static final Identifier GLOW = KingdomsAtWar.id("textures/entity/seal_glow.png");
    private static final RenderType GLOW_TYPE = RenderTypes.eyes(GLOW);
    private static final float HEART_Y = 8.0f;
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
    private static final int SHADOW = 0xFF3A1460;

    public static class State extends EntityRenderState {
        float time;
    }

    public SealBreakRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SealBreakEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.time = entity.breakTicks() + partialTicks;
    }

    @Override
    protected AABB getBoundingBoxForCulling(SealBreakEntity entity, float partialTicks) {
        return entity.getBoundingBox().inflate(40.0, 0.0, 40.0).expandTowards(0.0, 80.0, 0.0);
    }

    @Override
    protected int getBlockLightLevel(SealBreakEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float t = state.time;
        int shatter = SealCircle.SHATTER_TICK;
        for (DragonType element : SealCircleLayout.RING) {
            Vector3f top = top(element);
            int color = ARGB.opaque(element.color());
            // 1) Lichtsäulen
            if (t < 140) {
                float fade = t < 110 ? 1.0f : 1.0f - (t - 110) / 30.0f;
                int height = (int) Math.min(72.0f, t * 1.4f);
                if (height > 0) {
                    poseStack.pushPose();
                    poseStack.translate(top.x - 0.5f, top.y, top.z - 0.5f);
                    BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, t, 0, height, color,
                            0.2f * fade, 0.3f * fade);
                    poseStack.popPose();
                }
            }
            // 2) Strahlen zur Mitte
            if (t >= 80 && t < shatter + 6) {
                Vector3f dir = new Vector3f(0.0f, HEART_Y, 0.0f).sub(top);
                float length = dir.length();
                float grow = Mth.clamp((t - 80) / 30.0f, 0.0f, 1.0f);
                float pulse = 1.0f + 0.25f * Mth.sin(t * 0.6f + element.ordinal());
                float width = (0.12f + 0.18f * Mth.clamp((t - 110) / 100.0f, 0.0f, 1.0f)) * pulse;
                poseStack.pushPose();
                poseStack.translate(top.x, top.y, top.z);
                poseStack.rotate(new Quaternionf().rotationTo(new Vector3f(0.0f, 1.0f, 0.0f), dir.normalize()));
                poseStack.translate(-0.5f, 0.0f, -0.5f);
                BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, t, 0,
                        Math.max(1, Math.round(length * grow)), color, width, width * 1.6f);
                poseStack.popPose();
            }
        }
        // Lichtkugel über der Mitte, mit wachsendem Strahlenkranz
        if (t >= 80 && t < shatter) {
            float k = Mth.clamp((t - 80) / (shatter - 80.0f), 0.0f, 1.0f);
            float size = 0.6f + k * 3.4f + 0.3f * Mth.sin(t * 0.8f);
            this.glow(poseStack, collector, camera, HEART_Y, size * 2.2f, ARGB.colorFromFloat(0.55f, 0.85f, 0.75f, 1.0f));
            this.glow(poseStack, collector, camera, HEART_Y, size, ARGB.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f));
            if (t > 140) {
                poseStack.pushPose();
                poseStack.translate(0.0f, HEART_Y, 0.0f);
                poseStack.scale(0.35f, 0.35f, 0.35f);
                rays(poseStack, collector, (t - 140) / (shatter - 140.0f) * 0.55f, 0xFFFFFFFF, 0x00B48CFF);
                poseStack.popPose();
            }
        }
        // 3) Das Siegel zerspringt: Blitz, Strahlenkranz, Druckwelle
        if (t >= shatter && t < shatter + 50) {
            float s = (t - shatter) / 50.0f;
            float flash = 1.0f - Mth.clamp((t - shatter) / 18.0f, 0.0f, 1.0f);
            if (flash > 0.0f) {
                this.glow(poseStack, collector, camera, HEART_Y * 0.5f, 6.0f + 18.0f * (1.0f - flash), ARGB.colorFromFloat(flash, 1.0f, 0.95f, 1.0f));
            }
            poseStack.pushPose();
            poseStack.translate(0.0f, HEART_Y * 0.5f, 0.0f);
            poseStack.scale(0.8f, 0.8f, 0.8f);
            rays(poseStack, collector, 0.55f + s * 0.45f, ARGB.colorFromFloat(1.0f - s, 1.0f, 1.0f, 1.0f), 0x009060FF);
            poseStack.popPose();
            float radius = (t - shatter) * 1.4f;
            shockwave(poseStack, collector, radius, 3.0f, 1.0f - s);
        }
        // 4) Schattensäule aus dem Tor
        if (t >= shatter) {
            float k = Mth.clamp((t - shatter) / 60.0f, 0.0f, 1.0f);
            float fade = t > SealCircle.BREAK_TICKS - 30 ? Math.max(0.0f, (SealCircle.BREAK_TICKS - t) / 30.0f) : 1.0f;
            int height = (int) (6 + 60 * k);
            poseStack.pushPose();
            poseStack.translate(-0.5f, -0.25f, -0.5f);
            BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, -t, 0, height, SHADOW,
                    1.4f * fade + 0.01f, 2.2f * fade + 0.01f);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** Oberseite des Decksteins eines Monolithen, vom Träger aus (er steht auf der mittleren Siegelplatte). */
    private static Vector3f top(DragonType element) {
        BlockPos m = SealCircleLayout.monolith(BlockPos.ZERO, element);
        return new Vector3f(m.getX(), SealCircleLayout.MONOLITH_HEIGHT, m.getZ());
    }

    /** Leuchtender, zur Kamera gedrehter Fleck (additiv). */
    private void glow(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float y, float size, int color) {
        poseStack.pushPose();
        poseStack.translate(0.0f, y, 0.0f);
        poseStack.rotate(camera.orientation);
        poseStack.scale(size, size, size);
        collector.submitCustomGeometry(poseStack, GLOW_TYPE, (pose, buffer) -> {
            vertex(buffer, pose, color, -0.5f, -0.5f, 0, 1);
            vertex(buffer, pose, color, 0.5f, -0.5f, 1, 1);
            vertex(buffer, pose, color, 0.5f, 0.5f, 1, 0);
            vertex(buffer, pose, color, -0.5f, 0.5f, 0, 0);
        });
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int color, float x, float y, int u, int v) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    /** Lichtstrahlen in alle Richtungen (wie beim Tod des Enderdrachen); {@code amount} 0..1 bestimmt Zahl und Länge. */
    private static void rays(PoseStack poseStack, SubmitNodeCollector collector, float amount, int inner, int outer) {
        collector.submitCustomGeometry(poseStack, RenderTypes.dragonRays(), (pose, buffer) -> {
            RandomSource random = RandomSource.createThreadLocalInstance(1337L);
            Vector3f origin = new Vector3f();
            Vector3f left = new Vector3f();
            Vector3f right = new Vector3f();
            Vector3f bottom = new Vector3f();
            Quaternionf rotation = new Quaternionf();
            int count = Mth.floor((amount + amount * amount) / 2.0f * 70.0f);
            for (int i = 0; i < count; i++) {
                rotation.rotationXYZ(random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI)
                        .rotateXYZ(random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI,
                                random.nextFloat() * Mth.TWO_PI + amount * Mth.HALF_PI);
                pose.rotate(rotation);
                float length = random.nextFloat() * 20.0f + 5.0f;
                float width = random.nextFloat() * 2.0f + 1.0f;
                left.set(-HALF_SQRT_3 * width, length, -0.5f * width);
                right.set(HALF_SQRT_3 * width, length, -0.5f * width);
                bottom.set(0.0f, length, width);
                buffer.addVertex(pose, origin).setColor(inner);
                buffer.addVertex(pose, left).setColor(outer);
                buffer.addVertex(pose, right).setColor(outer);
                buffer.addVertex(pose, origin).setColor(inner);
                buffer.addVertex(pose, right).setColor(outer);
                buffer.addVertex(pose, bottom).setColor(outer);
                buffer.addVertex(pose, origin).setColor(inner);
                buffer.addVertex(pose, bottom).setColor(outer);
                buffer.addVertex(pose, left).setColor(outer);
            }
        });
    }

    /** Flaches Viereck in beiden Umlaufrichtungen: (a, b) mit Farbe {@code edge}, (c, d) mit Farbe {@code middle}. */
    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float ax, float az, float bx, float bz, float cx, float cz,
            float dx, float dz, float y, int edge, int middle) {
        buffer.addVertex(pose, ax, y, az).setColor(edge);
        buffer.addVertex(pose, bx, y, bz).setColor(edge);
        buffer.addVertex(pose, cx, y, cz).setColor(middle);
        buffer.addVertex(pose, dx, y, dz).setColor(middle);
        buffer.addVertex(pose, dx, y, dz).setColor(middle);
        buffer.addVertex(pose, cx, y, cz).setColor(middle);
        buffer.addVertex(pose, bx, y, bz).setColor(edge);
        buffer.addVertex(pose, ax, y, az).setColor(edge);
    }

    /** Ring aus Licht, der sich über den Boden ausbreitet. */
    private static void shockwave(PoseStack poseStack, SubmitNodeCollector collector, float radius, float width, float alpha) {
        if (alpha <= 0.0f || radius <= 0.0f) {
            return;
        }
        int bright = ARGB.colorFromFloat(alpha, 0.85f, 0.7f, 1.0f);
        int clear = ARGB.colorFromFloat(0.0f, 0.5f, 0.2f, 0.9f);
        float inner = Math.max(0.0f, radius - width);
        float outerR = radius + width;
        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            int segments = 64;
            for (int i = 0; i < segments; i++) {
                float a0 = i * Mth.TWO_PI / segments;
                float a1 = (i + 1) * Mth.TWO_PI / segments;
                float c0 = Mth.cos(a0);
                float s0 = Mth.sin(a0);
                float c1 = Mth.cos(a1);
                float s1 = Mth.sin(a1);
                float y = 0.15f;
                // innere Hälfte (durchsichtig → hell) und äußere Hälfte (hell → durchsichtig), beidseitig sichtbar
                quad(buffer, pose, c0 * inner, s0 * inner, c1 * inner, s1 * inner, c1 * radius, s1 * radius, c0 * radius, s0 * radius,
                        y, clear, bright);
                quad(buffer, pose, c0 * outerR, s0 * outerR, c1 * outerR, s1 * outerR, c1 * radius, s1 * radius, c0 * radius, s0 * radius,
                        y, clear, bright);
            }
        });
    }
}
