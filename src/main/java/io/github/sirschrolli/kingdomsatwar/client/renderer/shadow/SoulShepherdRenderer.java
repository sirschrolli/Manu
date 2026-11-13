package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulShepherdEntity;
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
 * Der Seelenhirte, 1,5-fach vergrößert: die Augen im Schädel, die Laterne, die Seelenrunen am Saum, die Irrlichter und das
 * Fläschchen am Strick leuchten – in Andacht nur glimmend, beim Erwachen auflodernd, im Zorn flackernd, im Tod erlöschend.
 * Zuletzt zerfällt er zu Nebel (er sinkt in sich zusammen und wird flach).
 */
public class SoulShepherdRenderer extends MobRenderer<SoulShepherdEntity, SoulShepherdRenderState, SoulShepherdModel> {
    public static final float SCALE = 1.5f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/soul_shepherd/shepherd.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/soul_shepherd/shepherd_glow.png"));

    public SoulShepherdRenderer(EntityRendererProvider.Context context) {
        super(context, new SoulShepherdModel(context.bakeLayer(ModModelLayers.SOUL_SHEPHERD)), 1.1f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SoulShepherdRenderState state, float yRot, float xRot) {
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

    private static float glow(SoulShepherdRenderState state) {
        float t = state.ageInTicks;
        SoulShepherdEntity.Attack attack = SoulShepherdEntity.Attack.values()[Mth.clamp(state.attack, 0, SoulShepherdEntity.Attack.values().length - 1)];
        // die Laterne flackert wie ein Feuer
        float glow = 0.84f + Mth.sin(t * 0.37f) * 0.05f + Mth.sin(t * 0.91f + 1.3f) * 0.03f + Mth.sin(t * 0.07f) * 0.04f;
        switch (attack) {
            case DORMANT -> glow = 0.32f + Mth.sin(t * 0.045f) * 0.08f;
            case AWAKEN -> glow = 0.32f + 0.68f * Mth.clamp((state.attackTime - 8.0f) / 30.0f, 0.0f, 1.0f);
            case WISPS, PEN, STORM, FURY -> glow = Math.min(1.0f, glow + 0.14f);
            default -> { }
        }
        if (state.enraged && attack != SoulShepherdEntity.Attack.DORMANT) {
            glow = Math.min(1.0f, glow + (Mth.sin(t * 0.55f) > 0.3f ? 0.12f : 0.0f));
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / (SoulShepherdEntity.DEATH_TICKS * 0.7f), 0.0f, 1.0f);
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(SoulShepherdRenderState state) {
        return TEXTURE;
    }

    @Override
    public SoulShepherdRenderState createRenderState() {
        return new SoulShepherdRenderState();
    }

    @Override
    public void extractRenderState(SoulShepherdEntity entity, SoulShepherdRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.enraged = entity.isEnraged();
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(SoulShepherdRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (state.deathTime > SoulShepherdEntity.DEATH_TICKS - 18) {
            // zuletzt zerfällt er zu Nebel: er sackt in sich zusammen und zerfließt
            float k = Mth.clamp((state.deathTime - (SoulShepherdEntity.DEATH_TICKS - 18)) / 16.0f, 0.0f, 1.0f);
            poseStack.scale(1.0f + k * 0.35f, 1.0f - k * 0.85f, 1.0f + k * 0.35f);
        }
    }

    @Override
    protected float getShadowRadius(SoulShepherdRenderState state) {
        return super.getShadowRadius(state) * (state.deathTime > 0.0f ? 1.0f - Mth.clamp(state.deathTime / SoulShepherdEntity.DEATH_TICKS, 0.0f, 1.0f) : 1.0f);
    }

    /** Er kippt im Tod nicht zur Seite (siehe {@link SoulShepherdModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }
}
