package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Zeichnet eine Schlange aus Kopf, Gliedern und Schwanz (Quetzara selbst oder ihre abgestreifte Haut): jedes Glied dort, wo die
 * Spur es hinlegt, zum vorderen ausgerichtet und je nach Dicke größer oder kleiner; der Schwanz mit dem Federbusch hinten.
 */
final class SerpentDraw {
    /** Wie groß Quetzara gezeichnet wird (die Modelle sind in Sechzehntel-Blöcken gebaut). */
    static final float SCALE = 1.5f;

    /** Ein Kleid: die Bilder für Kopf, Glied und Schwanz (und, wo es leuchtet, ihre Leuchtbilder). */
    record Skin(Identifier head, Identifier segment, Identifier tail, @Nullable Identifier headGlow, @Nullable Identifier segmentGlow,
            @Nullable Identifier tailGlow) {
        static Skin of(String suffix, boolean headGlow, boolean bodyGlow) {
            return new Skin(tex("quetzara" + suffix), tex("quetzara_segment" + suffix), tex("quetzara_tail" + suffix),
                    headGlow ? tex("quetzara" + suffix + "_glow") : null, bodyGlow ? tex("quetzara_segment" + suffix + "_glow") : null,
                    bodyGlow ? tex("quetzara_tail" + suffix + "_glow") : null);
        }
    }

    static final Skin NORMAL = Skin.of("", true, false);
    static final Skin MOLTED = Skin.of("_molted", true, true);
    static final Skin SHED = Skin.of("_shed", false, false);

    private final QuetzaraModel head;
    private final QuetzaraSegmentModel segment;
    private final QuetzaraTailModel tail;

    SerpentDraw(EntityRendererProvider.Context context) {
        this.head = new QuetzaraModel(context.bakeLayer(SerpentRenderers.QUETZARA));
        this.segment = new QuetzaraSegmentModel(context.bakeLayer(SerpentRenderers.QUETZARA_SEGMENT));
        this.tail = new QuetzaraTailModel(context.bakeLayer(SerpentRenderers.QUETZARA_TAIL));
    }

    static Identifier tex(String name) {
        return KingdomsAtWar.id("textures/entity/quetzara/" + name + ".png");
    }

    /**
     * Legt die Glieder in den Zustand: Lage relativ zum Wesen ({@code origin}), Richtung zum vorderen Glied (das erste zum
     * Nacken), Dicke; {@code roll} wiegt den Leib leicht.
     */
    static void lay(QuetzaraRenderState state, Vec3 origin, Vec3 joint, Vec3[] body, float age, float shudder) {
        Vec3 ahead = joint;
        for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
            QuetzaraRenderState.Segment seg = state.segments[i];
            Vec3 p = body[i];
            Vec3 d = ahead.subtract(p);
            if (d.lengthSqr() < 1.0e-6) {
                d = new Vec3(0.0, 0.0, 1.0);
            }
            d = d.normalize();
            seg.dx = p.x - origin.x;
            seg.dy = p.y - origin.y;
            seg.dz = p.z - origin.z;
            seg.yaw = (float) (Mth.atan2(-d.x, d.z) * Mth.RAD_TO_DEG);
            seg.pitch = (float) (-Math.asin(Mth.clamp(d.y, -1.0, 1.0)) * Mth.RAD_TO_DEG);
            seg.roll = Mth.sin(age * 0.05f + i * 0.6f) * 3.0f + (shudder > 0.0f ? Mth.sin(age * 2.6f + i * 1.9f) * 9.0f * shudder : 0.0f);
            seg.girth = QuetzaraEntity.GIRTH[i];
            seg.index = i;
            seg.ageInTicks = state.ageInTicks;
            seg.lightCoords = state.lightCoords;
            seg.outlineColor = state.outlineColor;
            seg.visible = true;
            ahead = p;
        }
        state.jx = joint.x - origin.x;
        state.jy = joint.y - origin.y;
        state.jz = joint.z - origin.z;
    }

    /** Dreht ein Modell (Blick nach −z) in die Richtung (Gier, Neigung), rollt es und vergrößert es. */
    private static void orient(PoseStack pose, float yaw, float pitch, float roll, float scale) {
        pose.rotate(Axis.YP.rotationDegrees(180.0f - yaw));
        pose.rotate(Axis.XP.rotationDegrees(-pitch));
        pose.rotate(Axis.ZP.rotationDegrees(roll));
        pose.scale(scale, scale, scale);
        pose.scale(-1.0f, -1.0f, 1.0f);
    }

    void submit(QuetzaraRenderState state, PoseStack pose, SubmitNodeCollector collector, Skin skin, float glow) {
        int overlay = OverlayTexture.pack(0.0f, state.hurt);
        boolean translucent = state.alpha < 0.999f || state.shed;
        int color = ARGB.color(Math.round(Mth.clamp(state.alpha, 0.0f, 1.0f) * 255.0f), 0xFFFFFF);
        int glowColor = ARGB.color(Math.round(Mth.clamp(glow * state.alpha, 0.0f, 1.0f) * 255.0f), 0xFFFFFF);
        // der Kopf
        pose.pushPose();
        pose.translate(state.jx, state.jy, state.jz);
        orient(pose, state.headYaw, state.headPitch, 0.0f, SCALE);
        this.submitPart(this.head, state, pose, collector, skin.head(), skin.headGlow(), translucent, color, glowColor, overlay,
                state.lightCoords, state.outlineColor, !state.asleep);
        pose.popPose();
        // die Glieder und der Schwanz
        for (QuetzaraRenderState.Segment seg : state.segments) {
            if (!seg.visible) {
                continue;
            }
            boolean isTail = seg.index == QuetzaraEntity.SEGMENTS;
            pose.pushPose();
            pose.translate(seg.dx, seg.dy, seg.dz);
            orient(pose, seg.yaw, seg.pitch, seg.roll, isTail ? SCALE : SCALE * seg.girth);
            if (isTail) {
                this.submitPart(this.tail, seg, pose, collector, skin.tail(), skin.tailGlow(), translucent, color, glowColor, overlay,
                        seg.lightCoords, seg.outlineColor, true);
            } else {
                this.submitPart(this.segment, seg, pose, collector, skin.segment(), skin.segmentGlow(), translucent, color, glowColor, overlay,
                        seg.lightCoords, seg.outlineColor, true);
            }
            pose.popPose();
        }
    }

    private <S> void submitPart(net.minecraft.client.model.Model<? super S> model, S state, PoseStack pose, SubmitNodeCollector collector,
            Identifier texture, @Nullable Identifier glowTexture, boolean translucent, int color, int glowColor, int overlay, int light, int outline,
            boolean glows) {
        RenderType type = translucent ? RenderTypes.entityTranslucent(texture) : model.renderType(texture);
        collector.submitModel(model, state, pose, type, light, overlay, color, null, outline);
        if (glowTexture != null && glows && ARGB.alpha(glowColor) > 4) {
            collector.order(1).submitModel(model, state, pose, RenderTypes.eyes(glowTexture), light, OverlayTexture.NO_OVERLAY, glowColor, null,
                    outline);
        }
    }
}
