package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.ReefColossusModel;
import io.github.sirschrolli.kingdomsatwar.entity.sea.ReefColossusEntity;
import net.minecraft.client.model.geom.ModelPart;
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
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Karkinos, der Riffkoloss, doppelt vergrößert: Augen, Speerklinge, Perle und Anemonen leuchten (im Gezeitenzorn
 * pulsierend heller, im Tod verlöschend); um die Spitze des Speers liegt ein türkiser Schein. Er versinkt im Sand (beim
 * Erwachen, Eingraben und im Tod) und kreiselt im Panzerwirbel.
 */
public class ReefColossusRenderer extends MobRenderer<ReefColossusEntity, ReefColossusRenderState, ReefColossusModel> {
    public static final float SCALE = 2.0f;
    /** So tief (Blöcke) sinkt er, wenn er ganz vergraben ist. */
    public static final float BURIED = 4.2f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/reef_colossus/reef_colossus.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/reef_colossus/reef_colossus_glow.png"));
    private static final int SPEAR_GLOW = 0x5AE8F0;

    public ReefColossusRenderer(EntityRendererProvider.Context context) {
        super(context, new ReefColossusModel(context.bakeLayer(ModModelLayers.REEF_COLOSSUS)), 2.4f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ReefColossusRenderState state, float yRot,
                    float xRot) {
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell Augen, Speer und Anemonen leuchten. */
    private static float glow(ReefColossusRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.7f + Mth.sin(t * 0.08f) * 0.08f;
        if (state.inTide) {
            glow = 0.85f + Mth.sin(t * 0.25f) * 0.15f;
        }
        float awaken = Anim.seconds(state.awaken, t);
        if (awaken >= 0.0f && awaken < 4.0f) {
            glow *= 0.2f + 0.8f * Mth.clamp((awaken - 0.5f) / 2.2f, 0.0f, 1.0f);
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / ReefColossusEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.9f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(ReefColossusRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(ReefColossusRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityCutout(TEXTURE);
    }

    @Override
    protected void scale(ReefColossusRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected void setupRotations(ReefColossusRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        // im Sand versunken (Erwachen, Eingraben, Tod)
        poseStack.translate(0.0f, -state.sink * BURIED, 0.0f);
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state.rolling) {
            // Panzerwirbel: er kreiselt wie ein Kreisel um sich selbst, leicht geneigt
            poseStack.rotate(Axis.YP.rotationDegrees(state.ageInTicks * 38.0f));
            poseStack.rotate(Axis.XP.rotationDegrees(6.0f));
        }
    }

    /** Er kippt im Tod nicht um wie ein gewöhnliches Wesen – er sackt zusammen und versinkt (siehe Modell). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected float getShadowRadius(ReefColossusRenderState state) {
        return super.getShadowRadius(state) * (1.0f - state.sink);
    }

    @Override
    protected float getShadowStrength(ReefColossusRenderState state) {
        return 0.5f;
    }

    @Override
    protected AABB getBoundingBoxForCulling(ReefColossusEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(5.0);
    }

    @Override
    public ReefColossusRenderState createRenderState() {
        return new ReefColossusRenderState();
    }

    @Override
    public void extractRenderState(ReefColossusEntity entity, ReefColossusRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hasRedOverlay = entity.hurtTime > 0 && entity.deathTime <= 0;
        state.awaken.copyFrom(entity.awakenAnimation);
        state.slam.copyFrom(entity.slamAnimation);
        state.grab.copyFrom(entity.grabAnimation);
        state.fling.copyFrom(entity.throwAnimation);
        state.jet.copyFrom(entity.jetAnimation);
        state.burrow.copyFrom(entity.burrowAnimation);
        state.erupt.copyFrom(entity.eruptAnimation);
        state.roll.copyFrom(entity.rollAnimation);
        state.summon.copyFrom(entity.summonAnimation);
        state.tide.copyFrom(entity.tideAnimation);
        state.lance.copyFrom(entity.lanceAnimation);
        state.stun.copyFrom(entity.stunAnimation);
        state.sink = entity.sink(partialTicks);
        state.rolling = entity.isRolling();
        state.jetting = entity.isJetting();
        state.stunned = entity.isStunned();
        state.spear = entity.hasSpear();
        state.inTide = entity.inTide();
    }

    @Override
    public void submit(ReefColossusRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.spear || state.isInvisible || state.sink > 0.85f) {
            return;
        }
        // der Schein um die Klinge des Speers: dem Modell bis zur Spitze folgen (in einem eigenen, leeren Stapel)
        ReefColossusModel model = this.getModel();
        model.setupAnim(state);
        PoseStack local = new PoseStack();
        local.scale(state.scale, state.scale, state.scale);
        this.setupRotations(state, local, state.bodyRot, state.scale);
        local.scale(-1.0f, -1.0f, 1.0f);
        this.scale(state, local);
        local.translate(0.0f, -1.501f, 0.0f);
        for (ModelPart part : model.spearChain()) {
            part.translateAndRotate(local);
        }
        // die Klinge sitzt 22 Pixel über dem Drehpunkt des Speers
        Vector3f tip = local.last().pose().transformPosition(new Vector3f(0.0f, -22.0f / 16.0f, 0.0f));
        float t = state.ageInTicks;
        float strength = state.inTide ? 0.75f + 0.25f * Mth.sin(t * 0.25f) : 0.3f + 0.06f * Mth.sin(t * 0.1f);
        if (state.deathTime > 0.0f) {
            strength *= 1.0f - Mth.clamp(state.deathTime / 55.0f, 0.0f, 1.0f);
        }
        SeaGlow.halo(poseStack, collector, camera.orientation, tip, state.inTide ? 3.4f : 2.4f, SPEAR_GLOW, strength);
    }
}
