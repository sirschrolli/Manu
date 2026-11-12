package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
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
 * Der Steinhüter, 1,4-fach vergrößert (gut dreieinhalb Blöcke hoch): Augen, Runen, Mondsteinbänder, Kristalle und das
 * Hüterherz leuchten – im Schlaf nur ein schwaches Glimmen, beim Erwachen glimmt es auf, beim Mondstrahl wird das Herz
 * gleißend hell, benommen flackert es, im Tod erlischt es. Er kippt nicht um – er sinkt auf die Knie und zerfällt.
 */
public class StoneKeeperRenderer extends MobRenderer<StoneKeeperEntity, StoneKeeperRenderState, StoneKeeperModel> {
    public static final float SCALE = 1.4f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/stone_keeper/stone_keeper.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/stone_keeper/stone_keeper_glow.png"));

    public StoneKeeperRenderer(EntityRendererProvider.Context context) {
        super(context, new StoneKeeperModel(context.bakeLayer(ModModelLayers.STONE_KEEPER)), 2.0f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, StoneKeeperRenderState state, float yRot,
                    float xRot) {
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

    /** Wie hell das Mondlicht in ihm ist. */
    private static float glow(StoneKeeperRenderState state) {
        float t = state.ageInTicks;
        StoneKeeperEntity.Attack attack = StoneKeeperEntity.Attack.values()[Mth.clamp(state.attack, 0, StoneKeeperEntity.Attack.values().length - 1)];
        float glow = 0.8f + Mth.sin(t * 0.1f) * 0.08f + Mth.sin(t * 0.33f) * 0.03f;
        switch (attack) {
            case DORMANT -> glow = 0.16f + Mth.sin(t * 0.05f) * 0.08f;
            case AWAKEN -> glow = 0.16f + 0.84f * Mth.clamp((state.attackTime - 8.0f) / 30.0f, 0.0f, 1.0f);
            case BEAM -> glow = Math.min(1.0f, glow + Mth.clamp(state.attackTime / 30.0f, 0.0f, 1.0f) * 0.2f);
            case STAGGER -> glow = 0.35f + (Mth.sin(t * 0.9f) > 0.3f ? 0.35f : 0.0f);
            default -> { }
        }
        if (state.armored) {
            glow = Math.min(1.0f, glow + 0.1f);
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / (StoneKeeperEntity.DEATH_TICKS * 0.8f), 0.0f, 1.0f);
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(StoneKeeperRenderState state) {
        return TEXTURE;
    }

    @Override
    public StoneKeeperRenderState createRenderState() {
        return new StoneKeeperRenderState();
    }

    @Override
    public void extractRenderState(StoneKeeperEntity entity, StoneKeeperRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.armored = entity.isArmored();
        state.enraged = entity.isEnraged();
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(StoneKeeperRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (state.deathTime > StoneKeeperEntity.DEATH_TICKS - 12) {
            // in den letzten Augenblicken sackt er in sich zusammen
            float k = Mth.clamp((state.deathTime - (StoneKeeperEntity.DEATH_TICKS - 12)) / 10.0f, 0.0f, 1.0f);
            poseStack.scale(1.0f + k * 0.15f, 1.0f - k * 0.55f, 1.0f + k * 0.15f);
        }
    }

    /** Er kippt im Tod nicht zur Seite (siehe {@link StoneKeeperModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }
}
