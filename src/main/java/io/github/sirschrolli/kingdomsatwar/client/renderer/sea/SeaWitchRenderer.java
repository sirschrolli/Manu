package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SeaWitchEntity;
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

/**
 * Die Meerhexe, 1,5-fach vergrößert: Augen, Haarspitzen, Korallenpolypen, Leuchtpunkte auf dem Schwanz und die Kugel ihres
 * Stabs leuchten – in Trance nur gedämpft, im Zorn flackernd hell, im Tod erlöschend.
 */
public class SeaWitchRenderer extends MobRenderer<SeaWitchEntity, SeaWitchRenderState, SeaWitchModel> {
    public static final float SCALE = 1.5f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/sea_witch/witch.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/sea_witch/witch_glow.png"));

    public SeaWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new SeaWitchModel(context.bakeLayer(ModModelLayers.SEA_WITCH)), 1.0f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SeaWitchRenderState state, float yRot, float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float glow = glow(state);
                if (glow > 0.01f) {
                    collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
                }
            }
        });
    }

    private static float glow(SeaWitchRenderState state) {
        float t = state.ageInTicks;
        SeaWitchEntity.Attack attack = SeaWitchEntity.Attack.values()[Mth.clamp(state.attack, 0, SeaWitchEntity.Attack.values().length - 1)];
        float glow = 0.82f + Mth.sin(t * 0.09f) * 0.08f + Mth.sin(t * 0.31f) * 0.03f;
        switch (attack) {
            case DORMANT -> glow = 0.35f + Mth.sin(t * 0.05f) * 0.1f;
            case AWAKEN -> glow = 0.35f + 0.65f * Mth.clamp((state.attackTime - 10.0f) / 26.0f, 0.0f, 1.0f);
            case INK, CURSE, FURY -> glow = Math.min(1.0f, glow + 0.15f);
            default -> { }
        }
        if (state.enraged && attack != SeaWitchEntity.Attack.DORMANT) {
            glow = Math.min(1.0f, glow + (Mth.sin(t * 0.6f) > 0.2f ? 0.12f : 0.0f));
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / (SeaWitchEntity.DEATH_TICKS * 0.8f), 0.0f, 1.0f);
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(SeaWitchRenderState state) {
        return TEXTURE;
    }

    @Override
    public SeaWitchRenderState createRenderState() {
        return new SeaWitchRenderState();
    }

    @Override
    public void extractRenderState(SeaWitchEntity entity, SeaWitchRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.enraged = entity.isEnraged();
        state.swim = Mth.clamp((float) entity.getDeltaMovement().horizontalDistance() / 0.22f, 0.0f, 1.0f) * 0.75f;
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(SeaWitchRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (state.deathTime > SeaWitchEntity.DEATH_TICKS - 16) {
            // in den letzten Augenblicken zerfließt sie
            float k = Mth.clamp((state.deathTime - (SeaWitchEntity.DEATH_TICKS - 16)) / 14.0f, 0.0f, 1.0f);
            poseStack.scale(1.0f + k * 0.2f, 1.0f - k * 0.7f, 1.0f + k * 0.2f);
        }
    }

    /** Sie kippt im Tod nicht zur Seite (siehe {@link SeaWitchModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }
}
