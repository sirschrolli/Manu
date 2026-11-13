package io.github.sirschrolli.kingdomsatwar.client.intro;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.IntroVisionEntity;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Die Lichteffekte des Erzähl-Intros ({@link IntroFlight}): die Säule der Dunkelheit, die Strahlen aus den Herzen der
 * sechs Drachen, die Lichtkugel um den Schattendrachen, der Blitz beim Schmieden des Siegels mit Druckwelle, das Siegel
 * als leuchtendes Hexagramm am Boden, die Splitter, die in die Herzen der Drachen zurückfliegen, die sechs Lichtsäulen
 * über den fernen Nestern und das dunkle Pochen unter der Erde am Schluss.
 */
public class IntroVisionRenderer extends EntityRenderer<IntroVisionEntity, IntroVisionRenderer.State> {
    private static final Identifier GLOW = KingdomsAtWar.id("textures/entity/seal_glow.png");
    private static final RenderType GLOW_TYPE = RenderTypes.eyes(GLOW);
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
    private static final int SHADOW = 0xFF2A1046;
    private static final int GOLD = 0xFFF2CF7A;

    public static class State extends EntityRenderState {
        float partial;
    }

    public IntroVisionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IntroVisionEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.partial = partialTicks;
    }

    @Override
    protected AABB getBoundingBoxForCulling(IntroVisionEntity entity, float partialTicks) {
        return entity.getBoundingBox().inflate(260.0, 0.0, 260.0).expandTowards(0.0, 220.0, 0.0).expandTowards(0.0, -60.0, 0.0);
    }

    @Override
    protected int getBlockLightLevel(IntroVisionEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        IntroFlight flight = IntroFlight.active();
        if (flight == null) {
            return;
        }
        double t = flight.now();
        float ticks = (float) (t * 20.0);
        Vec3 origin = flight.center();
        this.column(poseStack, collector, flight, t, ticks);
        this.sealing(poseStack, collector, camera, flight, t, ticks, origin, state.partial);
        this.glyph(poseStack, collector, flight, flight.glyph(t), (float) (t * 0.08));
        this.shards(poseStack, collector, camera, flight, t, origin, state.partial);
        this.pillars(poseStack, collector, flight, t, ticks, origin);
        this.pulses(poseStack, collector, camera, flight, t);
        super.submit(state, poseStack, collector, camera);
    }

    // =====================================================================================
    // Die Säule der Dunkelheit
    // =====================================================================================

    private void column(PoseStack poseStack, SubmitNodeCollector collector, IntroFlight flight, double t, float ticks) {
        float height = flight.columnHeight(t);
        float radius = flight.columnRadius(t);
        if (height < 1.0f || radius < 0.05f) {
            return;
        }
        float sink = flight.columnSink(t);
        poseStack.pushPose();
        poseStack.translate(-0.5f, -4.0f - sink, -0.5f);
        // ein dunkler Kern und ein weiter, dünner Schleier, beide drehen sich gegeneinander
        BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, -ticks * 0.6f, 0,
                Math.max(1, Math.round(height + 4.0f)), SHADOW, radius * 0.55f, radius);
        BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, ticks * 0.35f, 0,
                Math.max(1, Math.round(height * 0.75f + 4.0f)), 0xFF120618, radius * 0.3f, radius * 1.6f);
        poseStack.popPose();
    }

    // =====================================================================================
    // Das Schmieden des Siegels: Strahlen, Lichtkugel, Blitz, Druckwelle
    // =====================================================================================

    private void sealing(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, IntroFlight flight, double t,
            float ticks, Vec3 origin, float partial) {
        double beams = flight.beams(t);
        Vec3 focus = flight.focus(partial);
        if (beams > 0.01 && focus != null) {
            Vector3f to = local(focus, origin);
            for (int i = 0; i < IntroFlight.RING.length; i++) {
                Vec3 chest = flight.chest(i, partial);
                if (chest == null) {
                    continue;
                }
                DragonType element = IntroFlight.RING[i];
                float pulse = 1.0f + 0.2f * Mth.sin(ticks * 0.5f + i * 1.3f);
                float width = (float) ((0.22 + 0.4 * beams) * pulse);
                beam(poseStack, collector, local(chest, origin), to, ARGB.opaque(element.color()), width, width * 2.2f, ticks,
                        (float) Mth.clamp(beams * 1.6, 0.0, 1.0));
            }
        }
        double sphere = flight.sphere(t);
        if (sphere > 0.01 && focus != null) {
            // die Lichtkugel liegt vor dem Schattendrachen (zur Kamera hin), sonst verdeckte sein Körper sie
            Vec3 toCamera = camera.pos.subtract(focus);
            double away = toCamera.length();
            Vec3 front = away > 1.0e-3 ? focus.add(toCamera.scale(Math.min(9.0, away * 0.4) / away)) : focus;
            Vector3f at = local(front, origin);
            float size = (float) (2.0 + sphere * 14.0 + 0.6 * Math.sin(t * 9.0));
            this.glow(poseStack, collector, camera, at, size * 2.3f, ARGB.colorFromFloat((float) (0.55 * sphere), 0.8f, 0.72f, 1.0f));
            this.glow(poseStack, collector, camera, at, size, ARGB.colorFromFloat((float) Math.min(1.0, sphere * 1.4), 1.0f, 1.0f, 1.0f));
            if (sphere > 0.45) {
                poseStack.pushPose();
                poseStack.translate(at.x, at.y, at.z);
                poseStack.scale(0.4f, 0.4f, 0.4f);
                rays(poseStack, collector, (float) ((sphere - 0.45) / 0.55 * 0.5), 0xFFFFFFFF, 0x00C8A0FF);
                poseStack.popPose();
            }
        }
        double flash = flight.flash(t);
        if (flash > 0.01) {
            Vector3f at = new Vector3f(0.0f, 3.0f, 0.0f);
            this.glow(poseStack, collector, camera, at, (float) (10.0 + 30.0 * (1.0 - flash)),
                    ARGB.colorFromFloat((float) flash, 1.0f, 0.96f, 1.0f));
            poseStack.pushPose();
            poseStack.translate(0.0f, 3.0f, 0.0f);
            poseStack.scale(0.9f, 0.9f, 0.9f);
            rays(poseStack, collector, (float) (0.4 + 0.6 * (1.0 - flash)), ARGB.colorFromFloat((float) flash, 1.0f, 1.0f, 1.0f), 0x00A070FF);
            poseStack.popPose();
        }
        double wave = flight.shockwave(t);
        if (wave > 0.0) {
            shockwave(poseStack, collector, (float) wave, 4.0f, (float) Mth.clamp(1.0 - wave / 120.0, 0.0, 1.0), 0.95f, 0.85f, 1.0f, 0.4f);
        }
    }

    // =====================================================================================
    // Das Siegel am Boden: Kreise und Hexagramm, an den Spitzen die Farben der Elemente
    // =====================================================================================

    private void glyph(PoseStack poseStack, SubmitNodeCollector collector, IntroFlight flight, double alpha, float spin) {
        if (alpha <= 0.01) {
            return;
        }
        float a = (float) alpha;
        float r = 13.0f;
        int gold = ARGB.color(Math.round(a * 255), GOLD);
        int clear = ARGB.color(0, GOLD);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.12f, 0.0f);
        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            circle(buffer, pose, r * 1.18f, 0.55f, gold, clear);
            circle(buffer, pose, r, 0.35f, gold, clear);
            circle(buffer, pose, r * 0.42f, 0.3f, gold, clear);
            float[] xs = new float[6];
            float[] zs = new float[6];
            for (int i = 0; i < 6; i++) {
                double angle = flight.ringAngle(i) + spin;
                xs[i] = (float) (Math.cos(angle) * r);
                zs[i] = (float) (Math.sin(angle) * r);
            }
            for (int i = 0; i < 6; i++) {
                int j = (i + 2) % 6;     // zwei Dreiecke: 0-2-4 und 1-3-5
                line(buffer, pose, xs[i], zs[i], xs[j], zs[j], 0.32f, gold, clear);
            }
            for (int i = 0; i < 6; i++) {
                int color = ARGB.color(Math.round(a * 255), IntroFlight.RING[i].color());
                int none = ARGB.color(0, IntroFlight.RING[i].color());
                ring(buffer, pose, xs[i] * 1.18f, zs[i] * 1.18f, 2.1f, 0.45f, color, none);
            }
        });
        poseStack.popPose();
    }

    // =====================================================================================
    // Die Splitter fliegen in die Herzen der Drachen
    // =====================================================================================

    private void shards(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, IntroFlight flight, double t,
            Vec3 origin, float partial) {
        for (int i = 0; i < IntroFlight.RING.length; i++) {
            // der Splitter im Herzen leuchtet nach
            double heart = flight.heart(i, t);
            Vec3 chest = heart > 0.01 ? flight.chest(i, partial) : null;
            if (chest != null) {
                // ein Stück zur Kamera hin, sonst verdeckte die Brust das Leuchten
                Vec3 toCamera = camera.pos.subtract(chest);
                double away = toCamera.length();
                Vec3 front = away > 3.0 ? chest.add(toCamera.scale(2.4 / away)) : chest;
                Vector3f c = local(front, origin);
                int color = IntroFlight.RING[i].color();
                this.glow(poseStack, collector, camera, c, (float) (2.6 + heart * 2.2), ARGB.color((int) Math.min(255, heart * 190), color));
                this.glow(poseStack, collector, camera, c, (float) (0.9 + heart * 0.6), ARGB.colorFromFloat((float) Math.min(1.0, heart), 1.0f, 1.0f, 1.0f));
            }
            double k = flight.shard(i, t);
            if (k < 0.0) {
                continue;
            }
            Vec3 at = flight.shardPosition(i, k, partial);
            if (at == null) {
                continue;
            }
            Vector3f p = local(at, origin);
            int color = IntroFlight.RING[i].color();
            float fade = (float) (k > 0.92 ? (1.0 - k) / 0.08 : 1.0);
            this.glow(poseStack, collector, camera, p, 3.2f, ARGB.color(Math.round(fade * 200), color));
            this.glow(poseStack, collector, camera, p, 1.2f, ARGB.colorFromFloat(fade, 1.0f, 1.0f, 1.0f));
        }
    }

    // =====================================================================================
    // Lichtsäulen über den fernen Nestern
    // =====================================================================================

    private void pillars(PoseStack poseStack, SubmitNodeCollector collector, IntroFlight flight, double t, float ticks, Vec3 origin) {
        double alpha = flight.pillars(t);
        if (alpha <= 0.01) {
            return;
        }
        for (int j = 0; j < 6; j++) {
            Vec3 base = flight.pillar(j);
            Vector3f p = local(base, origin);
            int color = ARGB.opaque(IntroFlight.MONTAGE[j].color());
            float grow = (float) Mth.clamp((alpha - j * 0.08) / 0.5, 0.0, 1.0);
            if (grow <= 0.0f) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(p.x - 0.5f, p.y, p.z - 0.5f);
            BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, ticks, 0,
                    Math.max(1, Math.round(190 * grow)), color, 0.75f * grow, 1.5f * grow);
            poseStack.popPose();
        }
    }

    // =====================================================================================
    // Das Pochen unter der Erde (Warnung am Schluss)
    // =====================================================================================

    private void pulses(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, IntroFlight flight, double t) {
        for (int k = 0; k < IntroFlight.HEARTBEATS; k++) {
            double age = flight.heartbeatAge(k, t);
            if (age < 0.0 || age > 3.0) {
                continue;
            }
            float radius = (float) (age * 30.0);
            float alpha = (float) Math.max(0.0, 1.0 - age / 3.0);
            shockwave(poseStack, collector, radius, 3.5f, alpha * 0.9f, 0.55f, 0.12f, 0.85f, 0.25f);
            if (age < 0.6) {
                this.glow(poseStack, collector, camera, new Vector3f(0.0f, 1.0f, 0.0f), (float) (6.0 + age * 18.0),
                        ARGB.colorFromFloat((float) (0.8 * (1.0 - age / 0.6)), 0.6f, 0.2f, 0.95f));
            }
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static Vector3f local(Vec3 world, Vec3 origin) {
        return new Vector3f((float) (world.x - origin.x), (float) (world.y - origin.y), (float) (world.z - origin.z));
    }

    /** Lichtstrahl von {@code from} nach {@code to} (Leuchtfeuer-Strahl, gedreht); {@code reach} 0..1 lässt ihn wachsen. */
    private static void beam(PoseStack poseStack, SubmitNodeCollector collector, Vector3f from, Vector3f to, int color, float radius,
            float glow, float ticks, float reach) {
        Vector3f dir = new Vector3f(to).sub(from);
        float length = dir.length() * reach;
        if (length < 0.5f) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(from.x, from.y, from.z);
        poseStack.rotate(new Quaternionf().rotationTo(new Vector3f(0.0f, 1.0f, 0.0f), dir.normalize()));
        poseStack.translate(-0.5f, 0.0f, -0.5f);
        BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION, 1.0f, ticks, 0, Math.max(1, Math.round(length)),
                color, radius, glow);
        poseStack.popPose();
    }

    /** Leuchtender, zur Kamera gedrehter Fleck (additiv). */
    private void glow(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vector3f at, float size, int color) {
        poseStack.pushPose();
        poseStack.translate(at.x, at.y, at.z);
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
            RandomSource random = RandomSource.createThreadLocalInstance(4242L);
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

    /** Leuchtende Linie am Boden: in der Mitte hell, zu den Rändern durchsichtig. */
    private static void line(VertexConsumer buffer, PoseStack.Pose pose, float x0, float z0, float x1, float z1, float width, int bright,
            int clear) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dz * dz);
        if (length < 1.0e-4f) {
            return;
        }
        float nx = -dz / length * width;
        float nz = dx / length * width;
        quad(buffer, pose, x0 + nx, z0 + nz, x1 + nx, z1 + nz, x1, z1, x0, z0, 0.0f, clear, bright);
        quad(buffer, pose, x0 - nx, z0 - nz, x1 - nx, z1 - nz, x1, z1, x0, z0, 0.0f, clear, bright);
    }

    /** Leuchtender Kreis um die Mitte. */
    private static void circle(VertexConsumer buffer, PoseStack.Pose pose, float radius, float width, int bright, int clear) {
        ring(buffer, pose, 0.0f, 0.0f, radius, width, bright, clear);
    }

    private static void ring(VertexConsumer buffer, PoseStack.Pose pose, float cx, float cz, float radius, float width, int bright, int clear) {
        int segments = Math.max(16, Math.round(radius * 5.0f));
        for (int i = 0; i < segments; i++) {
            float a0 = i * Mth.TWO_PI / segments;
            float a1 = (i + 1) * Mth.TWO_PI / segments;
            float c0 = Mth.cos(a0);
            float s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1);
            float s1 = Mth.sin(a1);
            float inner = radius - width;
            float outer = radius + width;
            quad(buffer, pose, cx + c0 * inner, cz + s0 * inner, cx + c1 * inner, cz + s1 * inner, cx + c1 * radius, cz + s1 * radius,
                    cx + c0 * radius, cz + s0 * radius, 0.0f, clear, bright);
            quad(buffer, pose, cx + c0 * outer, cz + s0 * outer, cx + c1 * outer, cz + s1 * outer, cx + c1 * radius, cz + s1 * radius,
                    cx + c0 * radius, cz + s0 * radius, 0.0f, clear, bright);
        }
    }

    /** Ring aus Licht, der sich über den Boden ausbreitet (Farbe r, g, b; {@code inner} = Helligkeit der Innenkante). */
    private static void shockwave(PoseStack poseStack, SubmitNodeCollector collector, float radius, float width, float alpha, float r, float g,
            float b, float inner) {
        if (alpha <= 0.0f || radius <= 0.0f) {
            return;
        }
        int bright = ARGB.colorFromFloat(alpha, r, g, b);
        int clear = ARGB.colorFromFloat(alpha * inner, r * 0.5f, g * 0.5f, b * 0.5f);
        float innerR = Math.max(0.0f, radius - width);
        float outerR = radius + width;
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.2f, 0.0f);
        collector.submitCustomGeometry(poseStack, RenderTypes.lightning(), (pose, buffer) -> {
            int segments = 96;
            for (int i = 0; i < segments; i++) {
                float a0 = i * Mth.TWO_PI / segments;
                float a1 = (i + 1) * Mth.TWO_PI / segments;
                float c0 = Mth.cos(a0);
                float s0 = Mth.sin(a0);
                float c1 = Mth.cos(a1);
                float s1 = Mth.sin(a1);
                quad(buffer, pose, c0 * innerR, s0 * innerR, c1 * innerR, s1 * innerR, c1 * radius, s1 * radius, c0 * radius, s0 * radius,
                        0.0f, clear, bright);
                quad(buffer, pose, c0 * outerR, s0 * outerR, c1 * outerR, s1 * outerR, c1 * radius, s1 * radius, c0 * radius, s0 * radius,
                        0.0f, clear, bright);
            }
        });
        poseStack.popPose();
    }
}
