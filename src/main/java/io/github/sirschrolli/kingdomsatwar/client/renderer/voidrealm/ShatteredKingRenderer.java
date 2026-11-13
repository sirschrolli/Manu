package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.voidrealm.ShatteredKingModel;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

/**
 * Der Zerbrochene König, 2,2-fach vergrößert: Marmorleib mit leuchtenden Rissen, Herz, Augen, Runen und Kristallen
 * leuchten (beim Aufladen des Sternenstrahls heller, beim Erwachen glimmen sie erst auf, im Tod verlöschen sie). Dazu
 * der Warnkreis am Boden, wo eine Hand gleich aufschlägt, und der Sternenstrahl selbst.
 */
public class ShatteredKingRenderer extends MobRenderer<ShatteredKingEntity, ShatteredKingRenderState, ShatteredKingModel> {
    public static final float SCALE = 2.2f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/shattered_king/shattered_king.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/shattered_king/shattered_king_glow.png"));
    private static final Identifier RING = KingdomsAtWar.id("textures/entity/spell/ring.png");
    private static final RenderType RING_TYPE = RenderTypes.entityTranslucentEmissive(RING);
    private static final Identifier BEAM = KingdomsAtWar.id("textures/entity/spell/beam.png");
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final int VIOLET = 0xA070FF;
    private static final int PALE = 0xF2EAFF;
    private static final int FULL_BRIGHT = 0xF000F0;

    public ShatteredKingRenderer(EntityRendererProvider.Context context) {
        super(context, new ShatteredKingModel(context.bakeLayer(ModModelLayers.SHATTERED_KING)), 2.2f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ShatteredKingRenderState state, float yRot,
                    float xRot) {
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell Risse, Herz und Augen leuchten. */
    private static float glow(ShatteredKingRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.78f + Mth.sin(t * 0.07f) * 0.08f;
        float awaken = Anim.seconds(state.awaken, t);
        if (awaken >= 0.0f && awaken < 3.7f) {
            glow *= 0.15f + 0.85f * Mth.clamp((awaken - 0.4f) / 2.4f, 0.0f, 1.0f);
            glow += Anim.bump(awaken - 2.85f, 0.15f, 0.35f, 0.8f) * 0.25f;
        }
        float beam = Anim.seconds(state.beam, t);
        if (beam >= 0.0f && beam < ShatteredKingEntity.BEAM_TICKS / 20.0f + 0.4f) {
            glow += 0.25f * Mth.clamp(beam / 1.2f, 0.0f, 1.0f) * (1.0f - Mth.clamp((beam - ShatteredKingEntity.BEAM_TICKS / 20.0f) / 0.4f, 0.0f, 1.0f));
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / ShatteredKingEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.85f;
            glow += Mth.sin(state.deathTime * 1.3f) * 0.08f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(ShatteredKingRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(ShatteredKingRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected void scale(ShatteredKingRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht um wie ein gewöhnliches Wesen – er zerfällt (siehe Modell). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected float getShadowStrength(ShatteredKingRenderState state) {
        return 0.45f;
    }

    @Override
    protected AABB getBoundingBoxForCulling(ShatteredKingEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(entity.isBeaming() ? ShatteredKingEntity.BEAM_RANGE : 6.0);
    }

    @Override
    public ShatteredKingRenderState createRenderState() {
        return new ShatteredKingRenderState();
    }

    @Override
    public void extractRenderState(ShatteredKingEntity entity, ShatteredKingRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // im Tod nicht rot – er verblasst und zerfällt
        state.hasRedOverlay = entity.hurtTime > 0 && entity.deathTime <= 0;
        state.awaken.copyFrom(entity.awakenAnimation);
        state.slamLeft.copyFrom(entity.slamLeftAnimation);
        state.slamRight.copyFrom(entity.slamRightAnimation);
        state.sweep.copyFrom(entity.sweepAnimation);
        state.volley.copyFrom(entity.volleyAnimation);
        state.gravity.copyFrom(entity.gravityAnimation);
        state.summon.copyFrom(entity.summonAnimation);
        state.beam.copyFrom(entity.beamAnimation);
        state.rift.copyFrom(entity.riftAnimation);
        state.quake.copyFrom(entity.quakeAnimation);

        Vec3 position = entity.getPosition(partialTicks);
        // Ziel des Faustschlags in den Modellraum drehen (wie LivingEntityRenderer das Modell in die Welt dreht)
        Vec3 offset = entity.slamPoint().subtract(position);
        state.slamOffset = offset;
        float b = state.bodyRot * Mth.DEG_TO_RAD;
        float k = 16.0f / SCALE;
        float cos = Mth.cos(b);
        float sin = Mth.sin(b);
        state.slamX = (float) (offset.x * cos + offset.z * sin) * k;
        state.slamZ = (float) (offset.x * sin - offset.z * cos) * k;
        state.slamY = 24.0f - (float) offset.y * k;
        float slam = Math.max(slamWarn(Anim.seconds(state.slamLeft, state.ageInTicks)), slamWarn(Anim.seconds(state.slamRight, state.ageInTicks)));
        state.slamWarn = slam;

        // Sternenstrahl: vom Herzen bis dorthin, wo er auf Stein trifft
        state.beaming = entity.isBeaming() && entity.deathTime <= 0;
        if (state.beaming) {
            float beamSeconds = Anim.seconds(state.beam, state.ageInTicks);
            float stop = ShatteredKingEntity.BEAM_TICKS / 20.0f;
            state.beamPower = beamSeconds < 0.0f ? 1.0f
                    : Mth.clamp((beamSeconds - 1.45f) / 0.2f, 0.0f, 1.0f) * Mth.clamp((stop - beamSeconds) / 0.2f, 0.0f, 1.0f);
            Vec3 from = ShatteredKingEntity.heartPosition(position, state.bodyRot);
            Vec3 dir = ShatteredKingEntity.beamDirection(entity.beamYaw(partialTicks));
            Vec3 end = from.add(dir.scale(ShatteredKingEntity.BEAM_RANGE));
            HitResult hit = entity.level().clip(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            state.beamHit = hit.getType() != HitResult.Type.MISS;
            state.beamFrom = from.subtract(position);
            state.beamTo = (state.beamHit ? hit.getLocation() : end).subtract(position);
        } else {
            state.beamPower = 0.0f;
        }
    }

    /** Helligkeit des Warnkreises während eines Faustschlags (bis kurz nach dem Aufschlag). */
    private static float slamWarn(float s) {
        if (s < 0.0f || s > 1.4f) {
            return 0.0f;
        }
        float in = Mth.clamp(s / 0.3f, 0.0f, 1.0f);
        float out = 1.0f - Mth.clamp((s - 1.1f) / 0.3f, 0.0f, 1.0f);
        return in * out;
    }

    @Override
    public void submit(ShatteredKingRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.slamWarn > 0.01f) {
            this.submitWarning(state, poseStack, collector);
        }
        if (state.beaming && state.beamPower > 0.01f) {
            this.submitBeam(state, poseStack, collector, camera);
        }
    }

    /** Leuchtender Kreis am Boden, wo die Hand gleich herabfährt; er zieht sich bis zum Aufschlag zusammen. */
    private void submitWarning(ShatteredKingRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float t = state.ageInTicks;
        float pulse = 0.75f + 0.25f * Mth.sin(t * 0.9f);
        int color = ARGB.color(Math.round(200 * state.slamWarn * pulse), VIOLET);
        int inner = ARGB.color(Math.round(150 * state.slamWarn), PALE);
        Vec3 o = state.slamOffset;
        poseStack.pushPose();
        poseStack.translate(o.x, o.y + 0.06, o.z);
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.YP, t * 2.0f);
        float size = 7.6f;
        poseStack.scale(size, 1.0f, size);
        collector.submitCustomGeometry(poseStack, RING_TYPE, (pose, buffer) -> flatQuad(pose, buffer, color));
        poseStack.popPose();
        poseStack.rotateDegrees(Axis.YP, -t * 3.0f);
        float shrink = 2.0f + 5.0f * (1.0f - state.slamWarn) + 1.2f * Mth.sin(t * 0.35f);
        poseStack.translate(0.0f, 0.02f, 0.0f);
        poseStack.scale(shrink, 1.0f, shrink);
        collector.submitCustomGeometry(poseStack, RING_TYPE, (pose, buffer) -> flatQuad(pose, buffer, inner));
        poseStack.popPose();
    }

    /** Der Sternenstrahl: ein heller Kern in einem violetten Schein, Glanz am Herzen und ein Glutkreis, wo er auftrifft. */
    private void submitBeam(ShatteredKingRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Vec3 from = state.beamFrom;
        Vec3 span = state.beamTo.subtract(from);
        double length = span.length();
        if (length < 0.1) {
            return;
        }
        float power = state.beamPower;
        float t = state.ageInTicks;
        float len = (float) length;
        float flicker = 1.0f + 0.12f * Mth.sin(t * 1.9f);
        RenderType swirl = RenderTypes.energySwirl(BEAM, 0.0f, -t * 0.22f);
        int glow = scale(VIOLET, 0.85f * power);
        int core = scale(PALE, power);
        float repeat = len / 2.5f;
        poseStack.pushPose();
        poseStack.translate(from.x, from.y, from.z);
        Vec3 d = span.scale(1.0 / length);
        poseStack.rotate(new Quaternionf().rotationTo(0.0f, 1.0f, 0.0f, (float) d.x, (float) d.y, (float) d.z));
        poseStack.rotateDegrees(Axis.YP, t * 9.0f);
        float wide = 1.7f * flicker * power;
        float thin = 0.55f * flicker * power;
        collector.submitCustomGeometry(poseStack, swirl, (pose, buffer) -> cross(pose, buffer, len, wide, repeat, glow));
        collector.submitCustomGeometry(poseStack, swirl, (pose, buffer) -> cross(pose, buffer, len, thin, repeat, core));
        poseStack.popPose();

        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        for (int end = 0; end < 2; end++) {
            if (end == 1 && !state.beamHit) {
                continue;
            }
            Vec3 at = end == 0 ? from : state.beamTo;
            float size = (end == 0 ? 2.4f : 3.6f) * power * (1.0f + 0.18f * Mth.sin(t * 1.3f + end));
            int col = scale(ARGB.srgbLerp(0.45f, VIOLET, 0xFFFFFF), power);
            poseStack.pushPose();
            poseStack.translate(at.x, at.y, at.z);
            poseStack.rotate(camera.orientation);
            poseStack.scale(size, size, size);
            collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, col));
            poseStack.popPose();
        }
        if (state.beamHit) {
            Vec3 at = state.beamTo;
            int ring = ARGB.color(Math.round(210 * power), VIOLET);
            poseStack.pushPose();
            poseStack.translate(at.x, at.y + 0.05, at.z);
            poseStack.rotateDegrees(Axis.YP, t * 6.0f);
            float size = 3.2f + 0.4f * Mth.sin(t * 0.8f);
            poseStack.scale(size, 1.0f, size);
            collector.submitCustomGeometry(poseStack, RING_TYPE, (pose, buffer) -> flatQuad(pose, buffer, ring));
            poseStack.popPose();
        }
    }

    // ------------------------------------------------------------------ Geometrie

    private static int scale(int rgb, float f) {
        f = Mth.clamp(f, 0.0f, 1.0f);
        return ARGB.color(255, Math.round(ARGB.red(rgb) * f), Math.round(ARGB.green(rgb) * f), Math.round(ARGB.blue(rgb) * f));
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
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

    private static void billboard(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f, color);
    }

    /** Zwei gekreuzte Bänder entlang +Y. */
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
}
