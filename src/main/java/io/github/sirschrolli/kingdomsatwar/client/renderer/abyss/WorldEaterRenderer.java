package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Der Weltenfresser, doppelt so groß gezeichnet: der Kopf an der Stelle des Wesens, dahinter die zwölf Glieder und der
 * Schwanz dort, wo die Bahn des Kopfes sie hinlegt (jedes zum vorigen ausgerichtet und leicht verdreht, damit der Leib
 * sich windet). Nähte, Augen, Schlund und Kristalle glühen – im Zorn heller, im Tod verlöschend; Glied um Glied bricht weg.
 * Dazu der Rissatem: ein Strahl aus Glut vom Rachen zum Ring.
 */
public class WorldEaterRenderer extends EntityRenderer<WorldEaterEntity, WorldEaterRenderState> {
    public static final float SCALE = 2.0f;
    private static final Identifier HEAD = KingdomsAtWar.id("textures/entity/world_eater/world_eater.png");
    private static final Identifier SEGMENT = KingdomsAtWar.id("textures/entity/world_eater/world_eater_segment.png");
    private static final Identifier TAIL = KingdomsAtWar.id("textures/entity/world_eater/world_eater_tail.png");
    private static final RenderType HEAD_GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/world_eater/world_eater_glow.png"));
    private static final RenderType SEGMENT_GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/world_eater/world_eater_segment_glow.png"));
    private static final RenderType TAIL_GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/world_eater/world_eater_tail_glow.png"));
    private static final Identifier BEAM = KingdomsAtWar.id("textures/entity/spell/beam.png");
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final int EMBER = 0xFF7A2A;
    private static final int DEEP_EMBER = 0x9A240C;
    private static final int WHITE_HOT = 0xFFF0C8;
    private static final int FULL_BRIGHT = 0xF000F0;

    private final WorldEaterModel head;
    private final WorldEaterSegmentModel segment;
    private final WorldEaterTailModel tail;

    public WorldEaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.head = new WorldEaterModel(context.bakeLayer(ModModelLayers.WORLD_EATER));
        this.segment = new WorldEaterSegmentModel(context.bakeLayer(ModModelLayers.WORLD_EATER_SEGMENT));
        this.tail = new WorldEaterTailModel(context.bakeLayer(ModModelLayers.WORLD_EATER_TAIL));
    }

    @Override
    public WorldEaterRenderState createRenderState() {
        return new WorldEaterRenderState();
    }

    /** Der Leib reicht weit über den Kopf hinaus. */
    @Override
    protected AABB getBoundingBoxForCulling(WorldEaterEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(64.0);
    }

    @Override
    public void extractRenderState(WorldEaterEntity entity, WorldEaterRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.maw = entity.maw(partialTicks);
        state.hurt = entity.hurtTime > 0 && entity.deathTime <= 0;
        state.deathTime = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0f;
        state.wrath = entity.isWrathful();
        state.awakening = entity.isAwakening();
        Vec3 origin = new Vec3(state.x, state.y, state.z);
        Vec3 headCenter = origin.add(0.0, WorldEaterEntity.HEAD_CENTER, 0.0);
        Vec3 ahead = headCenter;
        int broken = state.deathTime <= 10.0f ? 0 : (int) ((state.deathTime - 10.0f) / 9.0f) + 1;
        for (int i = 0; i <= WorldEaterEntity.SEGMENTS; i++) {
            WorldEaterRenderState.Segment seg = state.segments[i];
            Vec3 p = entity.bodyPoint(i, partialTicks);
            Vec3 d = ahead.subtract(p);
            if (d.lengthSqr() < 1.0e-6) {
                d = entity.heading();
            }
            d = d.normalize();
            seg.dx = p.x - origin.x;
            seg.dy = p.y - origin.y;
            seg.dz = p.z - origin.z;
            seg.yaw = (float) (Mth.atan2(-d.x, d.z) * Mth.RAD_TO_DEG);
            seg.pitch = (float) (-Math.asin(Mth.clamp(d.y, -1.0, 1.0)) * Mth.RAD_TO_DEG);
            seg.roll = Mth.sin(state.ageInTicks * 0.045f + i * 0.55f) * 7.0f;
            seg.index = i;
            seg.world = WorldEaterEntity.segmentWorldIndex(i);
            seg.ageInTicks = state.ageInTicks;
            seg.lightCoords = state.lightCoords;
            seg.outlineColor = state.outlineColor;
            seg.broken = i >= WorldEaterEntity.SEGMENTS + 1 - broken;
            ahead = p;
        }
        state.breathing = entity.isBreathing() && entity.deathTime <= 0;
        if (state.breathing) {
            // der Rachen bewegt sich mit dem Kopf (relativ zum Wesen), das Ziel liegt fest in der Welt
            state.breathFrom = entity.mawPosition().subtract(entity.position());
            state.breathTo = entity.breathPoint().subtract(origin);
        }
    }

    /** Wie hell Nähte, Augen, Schlund und Kristalle glühen. */
    private static float glow(WorldEaterRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.72f + Mth.sin(t * 0.08f) * 0.08f + state.maw * 0.15f;
        if (state.wrath) {
            glow += 0.12f + Mth.sin(t * 0.35f) * 0.06f;
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / WorldEaterEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.8f;
            glow += Mth.sin(state.deathTime * 1.1f) * 0.1f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    /** Dreht ein Modell (Blick nach −z) in die Richtung (Gier, Neigung) und rollt es um die eigene Achse. */
    private static void orient(PoseStack pose, float yaw, float pitch, float roll) {
        pose.rotate(Axis.YP.rotationDegrees(180.0f - yaw));
        pose.rotate(Axis.XP.rotationDegrees(-pitch));
        pose.rotate(Axis.ZP.rotationDegrees(roll));
        pose.scale(SCALE, SCALE, SCALE);
        pose.scale(-1.0f, -1.0f, 1.0f);
    }

    @Override
    public void submit(WorldEaterRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int overlay = OverlayTexture.pack(0.0f, state.hurt);
        int glow = ARGB.color(Math.round(glow(state) * 255.0f), 0xFFFFFF);
        // der Kopf
        if (state.deathTime < WorldEaterEntity.DEATH_TICKS - 12) {
            pose.pushPose();
            pose.translate(0.0, WorldEaterEntity.HEAD_CENTER, 0.0);
            orient(pose, state.yaw, state.pitch, 0.0f);
            collector.submitModel(this.head, state, pose, HEAD, state.lightCoords, overlay, state.outlineColor);
            collector.order(1).submitModel(this.head, state, pose, HEAD_GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, glow, null,
                    state.outlineColor);
            pose.popPose();
        }
        // die Glieder und der Schwanz
        for (WorldEaterRenderState.Segment seg : state.segments) {
            if (seg.broken) {
                continue;
            }
            boolean isTail = seg.index == WorldEaterEntity.SEGMENTS;
            pose.pushPose();
            pose.translate(seg.dx, seg.dy, seg.dz);
            orient(pose, seg.yaw, seg.pitch, seg.roll);
            if (isTail) {
                pose.translate(0.0f, 0.0f, -0.6f);
                collector.submitModel(this.tail, seg, pose, TAIL, seg.lightCoords, overlay, seg.outlineColor);
                collector.order(1).submitModel(this.tail, seg, pose, TAIL_GLOW, seg.lightCoords, OverlayTexture.NO_OVERLAY, glow, null,
                        seg.outlineColor);
            } else {
                collector.submitModel(this.segment, seg, pose, SEGMENT, seg.lightCoords, overlay, seg.outlineColor);
                collector.order(1).submitModel(this.segment, seg, pose, SEGMENT_GLOW, seg.lightCoords, OverlayTexture.NO_OVERLAY, glow, null,
                        seg.outlineColor);
            }
            pose.popPose();
        }
        if (state.breathing) {
            this.submitBreath(state, pose, collector, camera);
        }
        super.submit(state, pose, collector, camera);
    }

    /** Der Rissatem: ein glühender Strahl vom Rachen zum Ring, am Ende ein heller Schein. */
    private void submitBreath(WorldEaterRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        Vec3 from = state.breathFrom;
        Vec3 span = state.breathTo.subtract(from);
        double length = span.length();
        if (length < 0.1) {
            return;
        }
        float t = state.ageInTicks;
        float len = (float) length;
        float flicker = 1.0f + 0.15f * Mth.sin(t * 2.1f);
        RenderType swirl = RenderTypes.energySwirl(BEAM, 0.0f, -t * 0.3f);
        float repeat = len / 2.5f;
        pose.pushPose();
        pose.translate(from.x, from.y, from.z);
        Vec3 d = span.scale(1.0 / length);
        pose.rotate(new Quaternionf().rotationTo(0.0f, 1.0f, 0.0f, (float) d.x, (float) d.y, (float) d.z));
        pose.rotate(Axis.YP.rotationDegrees(t * 11.0f));
        // ein gewaltiger Strahl: ein breiter, glühender Mantel, darin ein orangeroter Strom und ein weißglühender Kern
        float outer = 6.4f * flicker;
        float wide = 3.6f * flicker;
        float thin = 1.4f * flicker;
        RenderType slow = RenderTypes.energySwirl(BEAM, 0.0f, -t * 0.12f);
        collector.submitCustomGeometry(pose, slow, (p, buffer) -> cross(p, buffer, len, outer, repeat * 0.5f, DEEP_EMBER | 0xFF000000));
        collector.submitCustomGeometry(pose, swirl, (p, buffer) -> cross(p, buffer, len, wide, repeat, EMBER | 0xFF000000));
        collector.submitCustomGeometry(pose, swirl, (p, buffer) -> cross(p, buffer, len, thin, repeat, WHITE_HOT | 0xFF000000));
        pose.popPose();
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        for (int end = 0; end < 2; end++) {
            Vec3 at = end == 0 ? from : state.breathTo;
            float size = (end == 0 ? 6.0f : 9.0f) * (1.0f + 0.2f * Mth.sin(t * 1.5f + end));
            int col = ARGB.srgbLerp(0.4f, EMBER, 0xFFFFFF) | 0xFF000000;
            pose.pushPose();
            pose.translate(at.x, at.y, at.z);
            pose.rotate(camera.orientation);
            pose.scale(size, size, size);
            collector.submitCustomGeometry(pose, halo, (p, buffer) -> billboard(p, buffer, col));
            pose.popPose();
        }
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void cross(PoseStack.Pose pose, VertexConsumer buffer, float length, float width, float repeat, int color) {
        float w = width * 0.5f;
        vertex(buffer, pose, -w, 0.0f, 0.0f, 0.0f, repeat, color);
        vertex(buffer, pose, w, 0.0f, 0.0f, 1.0f, repeat, color);
        vertex(buffer, pose, w, length, 0.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -w, length, 0.0f, 0.0f, 0.0f, color);
        vertex(buffer, pose, 0.0f, 0.0f, -w, 0.0f, repeat, color);
        vertex(buffer, pose, 0.0f, 0.0f, w, 1.0f, repeat, color);
        vertex(buffer, pose, 0.0f, length, w, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.0f, length, -w, 0.0f, 0.0f, color);
    }

    private static void billboard(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, color);
    }
}
