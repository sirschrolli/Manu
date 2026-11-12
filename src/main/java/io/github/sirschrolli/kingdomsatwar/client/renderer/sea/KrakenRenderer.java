package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.KrakenModel;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Der Kraken, fünffach vergrößert (so hoch wie ein Turm): Augen und Leuchtpunkte glühen (in der Raserei pulsierend heller, im Tod verlöschend).
 * Er steigt aus dem Schlund empor (Erwachen) und sinkt im Tod wieder hinab; im Strudel kreist er um sich selbst.
 */
public class KrakenRenderer extends MobRenderer<KrakenEntity, KrakenRenderState, KrakenModel> {
    public static final float SCALE = 5.0f;
    /** So tief (Blöcke) steckt er, wenn er ganz im Schlund ist. */
    public static final float DEPTH = 20.0f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/kraken/kraken.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/kraken/kraken_glow.png"));

    public KrakenRenderer(EntityRendererProvider.Context context) {
        super(context, new KrakenModel(context.bakeLayer(ModModelLayers.KRAKEN)), 4.0f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, KrakenRenderState state, float yRot, float xRot) {
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell Augen und Leuchtpunkte glühen. */
    private static float glow(KrakenRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.75f + Mth.sin(t * 0.07f) * 0.1f;
        if (state.enraged) {
            glow = 0.85f + Mth.sin(t * 0.3f) * 0.15f;
        }
        float roar = Anim.seconds(state.roar, t);
        if (roar >= 0.0f && roar < 3.0f) {
            glow = Math.max(glow, 0.9f + Mth.sin(t * 0.8f) * 0.1f);
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / KrakenEntity.DEATH_TICKS, 0.0f, 1.0f) * 0.95f;
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(KrakenRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(KrakenRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityCutout(TEXTURE);
    }

    @Override
    protected void scale(KrakenRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected void setupRotations(KrakenRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        // im Schlund versunken (Erwachen, Tod)
        poseStack.translate(0.0f, -state.sink * DEPTH, 0.0f);
        super.setupRotations(state, poseStack, bodyRot, scale);
        if (state.spin != 0.0f) {
            poseStack.rotate(Axis.YP.rotationDegrees(state.spin));
        }
    }

    /** Er kippt im Tod nicht um – er sinkt zurück in den Schlund. */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected float getShadowRadius(KrakenRenderState state) {
        return 0.0f;
    }

    @Override
    protected AABB getBoundingBoxForCulling(KrakenEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(26.0);
    }

    @Override
    public KrakenRenderState createRenderState() {
        return new KrakenRenderState();
    }

    @Override
    public void extractRenderState(KrakenEntity entity, KrakenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.hasRedOverlay = entity.hurtTime > 0 && entity.deathTime <= 0;
        state.roar.copyFrom(entity.roarAnimation);
        state.slamLeft.copyFrom(entity.slamLeftAnimation);
        state.slamRight.copyFrom(entity.slamRightAnimation);
        state.sweepLeft.copyFrom(entity.sweepLeftAnimation);
        state.sweepRight.copyFrom(entity.sweepRightAnimation);
        state.grab.copyFrom(entity.grabAnimation);
        state.ink.copyFrom(entity.inkAnimation);
        state.whirl.copyFrom(entity.whirlAnimation);
        state.wave.copyFrom(entity.waveAnimation);
        state.grabSide = entity.grabSide();
        state.sink = entity.sink(partialTicks);
        state.enraged = entity.isEnraged();
        state.spin = entity.spin(partialTicks);
    }
}
