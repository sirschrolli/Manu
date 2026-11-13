package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * MissMystik: Augen, Zauberstabspitzen, Knöpfe, Saumsterne, die Adern und Kartenfarben ihrer Flügel leuchten (sanft
 * funkelnd); die Flügel sind durchscheinend. In der Zugabe schimmert alles ein zweites Mal in Rosa. Im Tod kippt sie nicht
 * um – sie verbeugt sich und vergeht am Ende in Konfetti (sie wird durchsichtig).
 */
public class MissMystikRenderer extends MobRenderer<MissMystikEntity, MissMystikRenderState, MissMystikModel> {
    public static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/miss_mystik/miss_mystik.png");
    public static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/miss_mystik/miss_mystik_glow.png"));

    public MissMystikRenderer(EntityRendererProvider.Context context) {
        super(context, new MissMystikModel(context.bakeLayer(ModModelLayers.MISS_MYSTIK)), 0.6f);
        this.addLayer(new GlowLayer(this));
    }

    /** Die leuchtenden Teile, sanft funkelnd; in der Zugabe ein zweites Mal in Rosa. */
    static final class GlowLayer extends RenderLayer<MissMystikRenderState, MissMystikModel> {
        GlowLayer(RenderLayerParent<MissMystikRenderState, MissMystikModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, MissMystikRenderState state, float yRot, float xRot) {
            if (state.isInvisible) {
                return;
            }
            float time = state.ageInTicks;
            float glow = (0.82f + Mth.sin(time * 0.13f) * 0.1f + Mth.sin(time * 0.61f) * 0.05f) * fade(state) * (state.image ? 0.7f : 1.0f);
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                    ARGB.color(Math.round(Mth.clamp(glow, 0.0f, 1.0f) * 255.0f), 0xFFFFFF), null, state.outlineColor);
            if (state.encore && !state.image && state.deathTime <= 0.0f) {
                float shine = 0.45f + Mth.sin(time * 0.3f) * 0.2f;
                collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                        ARGB.color(Math.round(shine * 255.0f), 0xFFB0E8), null, state.outlineColor);
            }
        }
    }

    /** Im Sterben: die letzten zwei Zehntel wird sie durchsichtig (sie vergeht in Konfetti). */
    static float fade(MissMystikRenderState state) {
        if (state.deathTime <= 0.0f) {
            return 1.0f;
        }
        float d = Mth.clamp(state.deathTime / MissMystikEntity.DEATH_TICKS, 0.0f, 1.0f);
        return d < 0.8f ? 1.0f : 1.0f - (d - 0.8f) / 0.2f;
    }

    @Override
    public Identifier getTextureLocation(MissMystikRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(MissMystikRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(MissMystikRenderState state) {
        return ARGB.color(Math.round(fade(state) * 255.0f), 0xFFFFFF);
    }

    @Override
    public MissMystikRenderState createRenderState() {
        return new MissMystikRenderState();
    }

    @Override
    public void extractRenderState(MissMystikEntity entity, MissMystikRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.encore = entity.isEncore();
        state.hat = entity.wearsHat();
        state.flap = Mth.lerp(partialTicks, entity.flapO, entity.flap);
        state.image = false;
        state.speed = (float) entity.getDeltaMovement().length();
        if (entity.isHidden()) {
            state.isInvisible = true;
        }
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    /** Sie kippt im Tod nicht um, sie verbeugt sich (siehe {@link MissMystikModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(MissMystikEntity entity, BlockPos pos) {
        return Math.max(9, super.getBlockLightLevel(entity, pos));
    }

    @Override
    protected float getShadowStrength(MissMystikRenderState state) {
        return 0.4f;
    }
}
