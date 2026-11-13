package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity;
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

/**
 * Die Sternenweberin, doppelt so groß: die Augen, die Galaxie auf dem Hinterleib, die Kristalle, die Gelenke und die
 * Spinnwarzen leuchten – in Andacht sanft, im Sternbild und im Zorn hell, im Tod erlöschend. Hängt sie am Faden, kippt sie
 * kopfüber (der Kopf zeigt hinab, die Spinnwarzen hinauf), und der Faden aus Sternenlicht führt hoch hinauf.
 */
public class StarWeaverRenderer extends MobRenderer<StarWeaverEntity, StarWeaverRenderState, StarWeaverModel> {
    public static final float SCALE = 2.0f;
    static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/star_weaver/star_weaver.png");
    static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/star_weaver/star_weaver_glow.png"));
    private static final Identifier BEAM = KingdomsAtWar.id("textures/entity/spell/beam.png");
    /** Mitte des Leibs über den Füßen (Blöcke) – um sie kippt sie, wenn sie hängt. */
    private static final float PIVOT = 1.13f;
    /** Die Spinnwarzen hinter der Mitte (Blöcke). */
    private static final float SPINNERETS = 2.9f;
    private static final float THREAD = 15.0f;

    public StarWeaverRenderer(EntityRendererProvider.Context context) {
        super(context, new StarWeaverModel(context.bakeLayer(ModModelLayers.STAR_WEAVER)), 1.8f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, StarWeaverRenderState state, float yRot, float xRot) {
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
            }
        });
    }

    static float glow(StarWeaverRenderState state) {
        float t = state.ageInTicks;
        StarWeaverEntity.Attack attack = StarWeaverEntity.Attack.values()[Mth.clamp(state.attack, 0, StarWeaverEntity.Attack.values().length - 1)];
        float glow = 0.82f + Mth.sin(t * 0.08f) * 0.06f + Mth.sin(t * 0.31f + 1.1f) * 0.03f;
        switch (attack) {
            case DORMANT -> glow = 0.55f + Mth.sin(t * 0.04f) * 0.1f;
            case AWAKEN -> glow = 0.55f + 0.45f * Mth.clamp((state.attackTime - 2.0f) / 20.0f, 0.0f, 1.0f);
            case CONSTELLATION, FURY, SPINDLE -> glow = Math.min(1.0f, glow + 0.16f);
            default -> { }
        }
        if (state.enraged && attack != StarWeaverEntity.Attack.DORMANT) {
            glow = Math.min(1.0f, glow + (Mth.sin(t * 0.6f) > 0.2f ? 0.1f : 0.0f));
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / (StarWeaverEntity.DEATH_TICKS * 0.8f), 0.0f, 1.0f);
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(StarWeaverRenderState state) {
        return TEXTURE;
    }

    @Override
    public StarWeaverRenderState createRenderState() {
        return new StarWeaverRenderState();
    }

    @Override
    public void extractRenderState(StarWeaverEntity entity, StarWeaverRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.enraged = entity.isEnraged();
        state.owner = entity;
        state.spiderling = false;
        state.hang = Anim.follow(entity, 7, entity.isHanging() ? 1.0f : 0.0f, state.ageInTicks, 5.0f);
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void setupRotations(StarWeaverRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state.hang > 0.001f) {
            // kopfüber am Faden: um die Mitte des Leibs gekippt, der Kopf nach unten
            poseStack.translate(0.0f, PIVOT, 0.0f);
            poseStack.rotate(Axis.XP.rotationDegrees(-90.0f * state.hang));
            poseStack.translate(0.0f, -PIVOT, 0.0f);
        }
    }

    @Override
    protected void scale(StarWeaverRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (state.deathTime > StarWeaverEntity.DEATH_TICKS - 20) {
            // zuletzt zerfällt sie zu Sternenstaub: sie wird flach und schwindet
            float k = Mth.clamp((state.deathTime - (StarWeaverEntity.DEATH_TICKS - 20)) / 18.0f, 0.0f, 1.0f);
            poseStack.scale(1.0f - k * 0.5f, 1.0f - k * 0.9f, 1.0f - k * 0.5f);
        }
    }

    @Override
    public void submit(StarWeaverRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.hang <= 0.02f || state.deathTime > 0.0f) {
            return;
        }
        // der Faden: von den Spinnwarzen (kopfüber oben) senkrecht hinauf
        float h = state.hang;
        float angle = h * Mth.HALF_PI;
        float up = PIVOT + Mth.sin(angle) * SPINNERETS;
        float back = Mth.cos(angle) * SPINNERETS;
        float yaw = state.bodyRot * Mth.DEG_TO_RAD;
        float bx = Mth.sin(yaw) * back;
        float bz = -Mth.cos(yaw) * back;
        float t = state.ageInTicks;
        RenderType swirl = RenderTypes.energySwirl(BEAM, 0.0f, -t * 0.03f);
        int color = ARGB.color(255, Math.round(200 * h), Math.round(222 * h), Math.round(255 * h));
        poseStack.pushPose();
        poseStack.translate(bx, up, bz);
        poseStack.rotate(Axis.YP.rotationDegrees(t * 2.0f));
        collector.submitCustomGeometry(poseStack, swirl, (p, buffer) -> cross(p, buffer, THREAD, 0.12f, THREAD / 2.0f, color));
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color) {
        buffer.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
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

    @Override
    protected float getShadowRadius(StarWeaverRenderState state) {
        float base = super.getShadowRadius(state) * (1.0f - state.hang * 0.6f);
        return base * (state.deathTime > 0.0f ? 1.0f - Mth.clamp(state.deathTime / StarWeaverEntity.DEATH_TICKS, 0.0f, 1.0f) : 1.0f);
    }

    /** Sie kippt im Tod nicht zur Seite (siehe {@link StarWeaverModel}: sie rollt die Beine ein). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }
}
