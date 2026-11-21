package io.github.sirschrolli.kingdomsatwar.client.renderer.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyBoltEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Bannpfeil der Himmelswacht. Solange er lädt: ein Runensiegel mit durchgestrichener Schwinge, das über dem Boss
 * wächst und sich dreht, darüber ein zweites, gegenläufiges, in der Mitte ein Lichtkern – und die Ziellinie zum Flieger,
 * über die Lichtpulse vom Boss zu ihm laufen. Im Flug: ein Pfeil aus Licht (zwei gekreuzte Bilder in Flugrichtung, außen in
 * der Farbe des Bosses, innen hell), ein Lichthof an der Spitze und ein Schweif, der seiner Bahn folgt. Alles leuchtet:
 * erst deckend in der Farbe des Bosses (so hat es auch vor hellem Himmel Farbe), dann als Licht obenauf.
 */
public class SkyBoltRenderer extends EntityRenderer<SkyBoltEntity, SkyBoltRenderer.State> {
    // deckend-durchscheinend (Farbe auch vor hellem Himmel) …
    private static final RenderType SIGIL = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/sky_ward/sigil.png"));
    private static final RenderType BOLT = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/sky_ward/bolt.png"));
    private static final RenderType LINE = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/sky_ward/line.png"));
    // … und als Licht obenauf
    private static final RenderType SIGIL_LIGHT = GlowRenderTypes.glow(KingdomsAtWar.id("textures/entity/sky_ward/sigil.png"));
    private static final RenderType BOLT_LIGHT = GlowRenderTypes.glow(KingdomsAtWar.id("textures/entity/sky_ward/bolt.png"));
    private static final RenderType LINE_LIGHT = GlowRenderTypes.glow(KingdomsAtWar.id("textures/entity/sky_ward/line.png"));
    private static final RenderType GLOW = GlowRenderTypes.glow(KingdomsAtWar.id("textures/entity/sky_ward/glow.png"));
    private static final int FULL_BRIGHT = 0xF000F0;
    /** In so viele Stücke ist die Ziellinie geteilt (für die wandernden Lichtpulse). */
    private static final int LINE_STEPS = 28;

    public static class State extends EntityRenderState {
        public SkyTheme theme = SkyTheme.FIRE;
        public int bolt;
        public float age;
        public float stateAge;
        public float charge;
        public float yRot;
        public float xRot;
        public @Nullable Vec3 target;
        /** Um so viel endet die Ziellinie vor dem Ziel (in der eigenen Sicht mehr – sie soll nicht vor den Augen liegen). */
        public float targetGap = 0.8f;
        public final Vec3[] trail = new Vec3[SkyBoltEntity.TRAIL];
        public int trailSize;
    }

    public SkyBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected int getBlockLightLevel(SkyBoltEntity entity, BlockPos pos) {
        return 15;
    }

    /** Die Ziellinie reicht weit über den Pfeil hinaus – er wird immer gezeichnet. */
    @Override
    protected boolean affectedByCulling(SkyBoltEntity entity) {
        return false;
    }

    @Override
    public void extractRenderState(SkyBoltEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.theme = entity.theme();
        state.bolt = entity.state();
        state.age = entity.tickCount + partialTicks;
        state.stateAge = Math.max(0.0f, entity.clientStateTicks() - 1 + partialTicks);
        state.charge = Mth.clamp(state.stateAge / entity.chargeTicks(), 0.0f, 1.0f);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        Vec3 pos = entity.getPosition(partialTicks);
        Entity target = entity.targetEntity();
        state.target = target != null && state.bolt == SkyBoltEntity.CHARGE
                ? target.getPosition(partialTicks).add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(pos).subtract(0.0, 0.3, 0.0) : null;
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        state.targetGap = target != null && target == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson() ? 3.5f : 0.8f;
        state.trailSize = entity.trailSize();
        for (int i = 0; i < state.trailSize; i++) {
            state.trail[i] = entity.trailPoint(i).subtract(pos);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.3f, 0.0f);
        // die Kamera, vom Pfeil aus gesehen (für Bänder, die sich ihr zuwenden)
        Vec3 eye = camera.pos.subtract(state.x, state.y + 0.3, state.z);
        if (state.bolt == SkyBoltEntity.CHARGE) {
            this.sigil(state, poseStack, collector, camera, eye);
        } else {
            this.bolt(state, poseStack, collector, camera, eye);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    // ------------------------------------------------------------------ das Siegel

    private void sigil(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vec3 eye) {
        float c = state.charge;
        float grow = 1.0f - (1.0f - c) * (1.0f - c) * (1.0f - c);
        float pulse = 0.86f + 0.14f * Mth.sin(state.age * 0.9f);
        // kurz vor dem Schuss blitzt es auf
        float flash = c > 0.8f ? (c - 0.8f) * 5.0f : 0.0f;
        float power = (0.45f + 0.55f * c) * pulse;
        int main = tint(state.theme.color, power);
        int mainLight = tint(state.theme.color, power * 0.7f + flash * 0.3f);
        int accent = tint(state.theme.accent, 0.4f + 0.5f * c);
        int accentLight = tint(state.theme.accent, 0.3f + 0.4f * c + flash * 0.3f);
        float size = 0.9f + 3.5f * grow;
        // das große Siegel, waagerecht, langsam drehend
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(state.age * 4.0f));
        poseStack.scale(size, size, size);
        collector.submitCustomGeometry(poseStack, SIGIL, (pose, buffer) -> flatQuad(pose, buffer, main));
        collector.order(1).submitCustomGeometry(poseStack, SIGIL_LIGHT, (pose, buffer) -> flatQuad(pose, buffer, mainLight));
        poseStack.popPose();
        // darüber ein kleineres, gegenläufig
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.45f + 0.25f * grow, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(-state.age * 7.0f));
        poseStack.scale(size * 0.52f, size * 0.52f, size * 0.52f);
        collector.submitCustomGeometry(poseStack, SIGIL, (pose, buffer) -> flatQuad(pose, buffer, accent));
        collector.order(1).submitCustomGeometry(poseStack, SIGIL_LIGHT, (pose, buffer) -> flatQuad(pose, buffer, accentLight));
        poseStack.popPose();
        // der Lichtkern, aus dem der Pfeil wird
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.25f, 0.0f);
        billboard(poseStack, collector, camera, GLOW, 1.2f + 2.0f * grow, tint(state.theme.color, 0.3f + 0.25f * c));
        billboard(poseStack, collector, camera, GLOW, 0.5f + 0.7f * grow + flash * 0.9f, tint(state.theme.accent, 0.45f + 0.35f * c + flash * 0.2f));
        poseStack.popPose();
        // die Ziellinie: Lichtpulse laufen vom Siegel zum Flieger
        Vec3 target = state.target;
        if (target != null && target.lengthSqr() > 36.0) {
            float width = 0.16f + 0.2f * c + flash * 0.2f;
            float phase = state.age * 0.07f;
            int color = state.theme.color;
            float base = 0.3f + 0.4f * c + flash * 0.3f;
            Vec3 from = new Vec3(0.0, 0.25, 0.0);
            // sie endet kurz vor dem Flieger
            Vec3 to = target.subtract(target.normalize().scale(state.targetGap));
            collector.submitCustomGeometry(poseStack, LINE, (pose, buffer) -> pulseLine(pose, buffer, from, to, eye, width * 0.45f, color, base * 0.8f, phase));
            collector.order(1).submitCustomGeometry(poseStack, LINE_LIGHT,
                    (pose, buffer) -> pulseLine(pose, buffer, from, to, eye, width * 1.6f, color, base * 0.6f, phase));
        }
    }

    /** Ein der Kamera zugewandtes Lichtband von {@code from} nach {@code to}, über das helle Pulse wandern. */
    private static void pulseLine(PoseStack.Pose pose, VertexConsumer buffer, Vec3 from, Vec3 to, Vec3 eye, float width, int color, float base,
            float phase) {
        Vec3 span = to.subtract(from);
        double length = span.length();
        Vec3 dir = span.scale(1.0 / length);
        // drei Pulse sind zugleich unterwegs
        double pulses = Math.max(1.0, Math.round(length / 14.0));
        Vec3 lastA = null;
        Vec3 lastB = null;
        int lastColor = 0;
        for (int i = 0; i <= LINE_STEPS; i++) {
            double t = i / (double) LINE_STEPS;
            Vec3 p = from.add(span.scale(t));
            Vec3 side = dir.cross(eye.subtract(p));
            side = side.lengthSqr() < 1.0e-8 ? new Vec3(0.0, 1.0, 0.0) : side.normalize();
            double wave = (t * pulses - phase) % 1.0;
            wave = wave < 0.0 ? wave + 1.0 : wave;
            float glow = (float) Math.pow(1.0 - Math.min(1.0, Math.abs(wave - 0.5) * 2.0 / 0.5), 2.0);
            // am Siegel setzt die Linie weich an, zum Flieger hin läuft sie spitz aus
            float ends = (float) Math.min(1.0, Math.min(t * 12.0, (1.0 - t) * 5.0));
            int c = tint(color, (base + glow * 0.75f) * ends);
            double half = width * 0.5 * Math.min(1.0, (1.0 - t) * 6.0 + 0.15);
            Vec3 a = p.subtract(side.scale(half));
            Vec3 b = p.add(side.scale(half));
            if (lastA != null) {
                quad(pose, buffer, lastA, lastB, b, a, 0.0f, 1.0f, lastColor, c);
            }
            lastA = a;
            lastB = b;
            lastColor = c;
        }
    }

    // ------------------------------------------------------------------ der Pfeil

    private void bolt(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, Vec3 eye) {
        float power = state.bolt == SkyBoltEntity.FREE ? 0.45f + 0.2f * Mth.sin(state.age * 2.3f) : 1.0f;
        // Flugrichtung: am genauesten aus der eigenen Bahn
        Vec3 dir;
        if (state.trailSize >= 2 && state.trail[1].lengthSqr() > 1.0e-4) {
            dir = state.trail[1].scale(-1.0).normalize();
        } else {
            float yaw = state.yRot * Mth.DEG_TO_RAD;
            float pitch = state.xRot * Mth.DEG_TO_RAD;
            dir = new Vec3(Mth.sin(yaw) * Mth.cos(pitch), Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch));
        }
        Vec3 side = Math.abs(dir.y) < 0.95 ? dir.cross(new Vec3(0.0, 1.0, 0.0)).normalize() : dir.cross(new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 up = side.cross(dir).normalize();
        float spin = state.age * 0.55f;
        Vec3 a = side.scale(Mth.cos(spin)).add(up.scale(Mth.sin(spin)));
        Vec3 b = side.scale(-Mth.sin(spin)).add(up.scale(Mth.cos(spin)));
        Vec3 head = dir.scale(1.1);
        Vec3 tail = dir.scale(-2.5);
        int outer = tint(state.theme.color, power);
        int outerLight = tint(state.theme.color, power * 0.6f);
        int inner = tint(state.theme.accent, power * 0.9f);
        Vec3 fa = a;
        Vec3 fb = b;
        collector.submitCustomGeometry(poseStack, BOLT, (pose, buffer) -> {
            blade(pose, buffer, tail, head, fa, 0.6f, outer);
            blade(pose, buffer, tail, head, fb, 0.6f, outer);
        });
        collector.order(1).submitCustomGeometry(poseStack, BOLT_LIGHT, (pose, buffer) -> {
            blade(pose, buffer, tail, head, fa, 0.75f, outerLight);
            blade(pose, buffer, tail, head, fb, 0.75f, outerLight);
            blade(pose, buffer, tail.scale(0.8), head.scale(0.85), fa, 0.36f, inner);
            blade(pose, buffer, tail.scale(0.8), head.scale(0.85), fb, 0.36f, inner);
        });
        // der Schweif folgt der Bahn
        int count = state.trailSize;
        if (count >= 3) {
            Vec3[] points = new Vec3[count];
            points[0] = tail.scale(0.5);
            for (int i = 1; i < count; i++) {
                points[i] = state.trail[i];
            }
            int color = state.theme.color;
            collector.submitCustomGeometry(poseStack, LINE, (pose, buffer) -> ribbon(pose, buffer, points, eye, 0.3f, color, power * 0.75f));
            collector.order(1).submitCustomGeometry(poseStack, LINE_LIGHT, (pose, buffer) -> ribbon(pose, buffer, points, eye, 0.9f, color, power * 0.7f));
        }
        // Lichthof an der Spitze
        poseStack.pushPose();
        poseStack.translate(head.x * 0.6, head.y * 0.6, head.z * 0.6);
        billboard(poseStack, collector, camera, GLOW, 3.6f, tint(state.theme.color, power * 0.6f));
        billboard(poseStack, collector, camera, GLOW, 1.4f, tint(state.theme.accent, power * 0.8f));
        poseStack.popPose();
    }

    /** Ein Bild des Pfeils in der Ebene aus Flugrichtung und {@code axis}, von beiden Seiten sichtbar (Spitze = rechter Bildrand). */
    private static void blade(PoseStack.Pose pose, VertexConsumer buffer, Vec3 tail, Vec3 head, Vec3 axis, float half, int color) {
        Vec3 o = axis.scale(half);
        Vec3 p0 = tail.subtract(o);
        Vec3 p1 = head.subtract(o);
        Vec3 p2 = head.add(o);
        Vec3 p3 = tail.add(o);
        vertex(buffer, pose, p0, 0.0f, 1.0f, color);
        vertex(buffer, pose, p1, 1.0f, 1.0f, color);
        vertex(buffer, pose, p2, 1.0f, 0.0f, color);
        vertex(buffer, pose, p3, 0.0f, 0.0f, color);
        vertex(buffer, pose, p3, 0.0f, 0.0f, color);
        vertex(buffer, pose, p2, 1.0f, 0.0f, color);
        vertex(buffer, pose, p1, 1.0f, 1.0f, color);
        vertex(buffer, pose, p0, 0.0f, 1.0f, color);
    }

    /** Der Schweif: ein der Kamera zugewandtes Band durch {@code points}, das nach hinten schmaler und dunkler wird. */
    private static void ribbon(PoseStack.Pose pose, VertexConsumer buffer, Vec3[] points, Vec3 eye, float width, int color, float power) {
        Vec3 lastA = null;
        Vec3 lastB = null;
        int lastColor = 0;
        int n = points.length;
        for (int i = 0; i < n; i++) {
            Vec3 p = points[i];
            Vec3 along = i + 1 < n ? points[i + 1].subtract(p) : p.subtract(points[i - 1]);
            if (along.lengthSqr() < 1.0e-8) {
                continue;
            }
            Vec3 side = along.normalize().cross(eye.subtract(p));
            side = side.lengthSqr() < 1.0e-8 ? new Vec3(0.0, 1.0, 0.0) : side.normalize();
            float t = i / (float) (n - 1);
            float w = width * (1.0f - t * 0.85f) * 0.5f;
            int c = tint(color, power * (1.0f - t) * (1.0f - t));
            Vec3 a = p.subtract(side.scale(w));
            Vec3 b = p.add(side.scale(w));
            if (lastA != null) {
                quad(pose, buffer, lastA, lastB, b, a, 0.0f, 1.0f, lastColor, c);
            }
            lastA = a;
            lastB = b;
            lastColor = c;
        }
    }

    // ------------------------------------------------------------------ Bausteine

    /** Farbe mit Stärke {@code power}: die Deckkraft (für das Licht obenauf: wie stark es aufhellt). */
    private static int tint(int rgb, float power) {
        return ARGB.color((int) (Mth.clamp(power, 0.0f, 1.0f) * 255.0f), rgb);
    }

    private static void billboard(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, RenderType type, float size, int color) {
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.scale(size, size, size);
        collector.order(1).submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, color);
            vertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f, color);
            vertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, color);
            vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, color);
        });
        poseStack.popPose();
    }

    /** Waagerechtes Viereck (Größe 1, Mitte im Ursprung), von beiden Seiten sichtbar. */
    private static void flatQuad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 0.5f, 1.0f, 1.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, 0.5f, 0.0f, 1.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f, color);
    }

    /** Ein Stück Band zwischen zwei Querschnitten (a0–b0 und a1–b1), von beiden Seiten sichtbar. */
    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a0, Vec3 b0, Vec3 b1, Vec3 a1, float v0, float v1, int color0, int color1) {
        vertex(buffer, pose, a0, 0.0f, v0, color0);
        vertex(buffer, pose, b0, 1.0f, v0, color0);
        vertex(buffer, pose, b1, 1.0f, v1, color1);
        vertex(buffer, pose, a1, 0.0f, v1, color1);
        vertex(buffer, pose, a1, 0.0f, v1, color1);
        vertex(buffer, pose, b1, 1.0f, v1, color1);
        vertex(buffer, pose, b0, 1.0f, v0, color0);
        vertex(buffer, pose, a0, 0.0f, v0, color0);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, Vec3 p, float u, float v, int color) {
        vertex(buffer, pose, (float) p.x, (float) p.y, (float) p.z, u, v, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
