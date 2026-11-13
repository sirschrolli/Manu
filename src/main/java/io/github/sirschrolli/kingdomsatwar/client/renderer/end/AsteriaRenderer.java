package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
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
 * Asteria auf ihrem Sternenrochen, 1,2-fach vergrößert (gut acht Blöcke Spannweite): Sterne, Sternbilder, Kronenspitzen,
 * Augen, Lanzenspitze und der Schweif aus Sternenlicht leuchten; die Spitzen der Schwingen sind durchscheinend. Strahlend
 * leuchtet alles doppelt. Im Sterben vergeht sie in Sternenlicht (sie wird durchsichtig), statt umzukippen.
 */
public class AsteriaRenderer extends MobRenderer<AsteriaEntity, AsteriaRenderState, AsteriaModel> {
    public static final float SCALE = 1.2f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/asteria/asteria.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/asteria/asteria_glow.png"));

    public AsteriaRenderer(EntityRendererProvider.Context context) {
        super(context, new AsteriaModel(context.bakeLayer(ModModelLayers.ASTERIA)), 2.2f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AsteriaRenderState state, float yRot, float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
                if (state.radiant && state.deathTime <= 0.0f) {
                    // strahlend: das Sternenlicht ein zweites Mal, heller und bläulich
                    float shine = 0.5f + Mth.sin(state.ageInTicks * 0.25f) * 0.2f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(shine * 255.0f), 0xC8E6FF), null, state.outlineColor);
                }
            }
        });
    }

    /** Wie hell das Sternenlicht ist: ein ruhiges Funkeln, im Tod verlöschend. */
    private static float glow(AsteriaRenderState state) {
        float t = state.ageInTicks;
        float glow = 0.86f + Mth.sin(t * 0.11f) * 0.08f + Mth.sin(t * 0.53f) * 0.04f;
        return Mth.clamp(glow * fade(state), 0.0f, 1.0f);
    }

    /** Im Sterben wird sie durchsichtig (die letzten zwei Fünftel). */
    private static float fade(AsteriaRenderState state) {
        if (state.deathTime <= 0.0f) {
            return 1.0f;
        }
        float d = Mth.clamp(state.deathTime / AsteriaEntity.DEATH_TICKS, 0.0f, 1.0f);
        return d < 0.6f ? 1.0f : 1.0f - (d - 0.6f) / 0.4f;
    }

    @Override
    public Identifier getTextureLocation(AsteriaRenderState state) {
        return TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(AsteriaRenderState state, boolean isBodyVisible, boolean forceTransparent,
            boolean appearGlowing) {
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(TEXTURE) : RenderTypes.entityTranslucent(TEXTURE);
    }

    @Override
    protected int getModelTint(AsteriaRenderState state) {
        return ARGB.color(Math.round(fade(state) * 255.0f), 0xFFFFFF);
    }

    @Override
    public AsteriaRenderState createRenderState() {
        return new AsteriaRenderState();
    }

    @Override
    public void extractRenderState(AsteriaEntity entity, AsteriaRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.radiant = entity.isRadiant();
        state.lance = entity.holdsLance(state.attackTime);
        state.flap = Mth.lerp(partialTicks, entity.flapO, entity.flap);
        state.bank = Mth.lerp(partialTicks, entity.bankO, entity.bank);
        state.climb = (float) entity.getDeltaMovement().y;
        // im Sterben nicht rot getönt (sie vergeht in Sternenlicht)
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(AsteriaRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Sie kippt im Tod nicht zur Seite, sie sinkt in einer Spirale hinab (siehe {@link AsteriaEntity}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(AsteriaEntity entity, BlockPos pos) {
        // ihr Sternenlicht: selbst im Schatten nie ganz dunkel
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override
    protected float getShadowStrength(AsteriaRenderState state) {
        return 0.35f;
    }
}
