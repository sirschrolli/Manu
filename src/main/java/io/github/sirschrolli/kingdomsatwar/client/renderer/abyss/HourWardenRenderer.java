package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.HourWardenEntity;
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
 * Der Stundenwächter, 1,6-fach vergrößert: Augen, Chronit, Stundenmarken und Pendelscheibe leuchten – erstarrt nur ein
 * schwaches Glimmen, beim Erwachen glühen sie auf, im Zeitstopp gleißen sie (und erlöschen, wenn die Zeit steht), in der
 * Zwölften Stunde pulsieren sie mit jedem Glockenschlag, im Tod erlöschen sie.
 */
public class HourWardenRenderer extends MobRenderer<HourWardenEntity, HourWardenRenderState, HourWardenModel> {
    public static final float SCALE = 1.6f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/hour_warden/hour_warden.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/hour_warden/hour_warden_glow.png"));

    public HourWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new HourWardenModel(context.bakeLayer(ModModelLayers.HOUR_WARDEN)), 1.2f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, HourWardenRenderState state, float yRot, float xRot) {
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

    private static float glow(HourWardenRenderState state) {
        float t = state.ageInTicks;
        HourWardenEntity.Attack attack = HourWardenEntity.Attack.values()[Mth.clamp(state.attack, 0, HourWardenEntity.Attack.values().length - 1)];
        float a = state.attackTime;
        float glow = 0.82f + Mth.sin(t * 0.21f) * 0.06f;
        switch (attack) {
            case DORMANT -> glow = 0.18f + Mth.sin(t * 0.04f) * 0.05f;
            case AWAKEN -> glow = 0.18f + 0.82f * Mth.clamp((a - 8.0f) / 26.0f, 0.0f, 1.0f);
            case TIME_STOP -> glow = a < HourWardenEntity.STOP_AT ? Math.min(1.0f, 0.85f + a * 0.005f + (Mth.sin(a * (0.4f + a * 0.03f)) > 0.0f ? 0.1f : 0.0f))
                    : 1.0f;
            case REWIND -> glow = 0.6f + Mth.sin(t * 0.9f) * 0.25f;
            case STAGGER -> glow = Mth.sin(t * 1.7f) > 0.2f ? 0.9f : 0.3f;
            case FURY -> glow = 1.0f;
            case TOLLS -> {
                float since = Math.max(0.0f, a - 6.0f) % HourWardenEntity.TOLL_GAP;
                glow = 0.8f + 0.2f * Math.max(0.0f, 1.0f - since / 5.0f);
            }
            default -> { }
        }
        if (state.enraged && attack != HourWardenEntity.Attack.DORMANT) {
            glow = Math.min(1.0f, glow + 0.08f);
        }
        if (state.deathTime > 0.0f) {
            glow *= 1.0f - Mth.clamp(state.deathTime / (HourWardenEntity.DEATH_TICKS * 0.75f), 0.0f, 1.0f);
        }
        return Mth.clamp(glow, 0.0f, 1.0f);
    }

    @Override
    public Identifier getTextureLocation(HourWardenRenderState state) {
        return TEXTURE;
    }

    @Override
    public HourWardenRenderState createRenderState() {
        return new HourWardenRenderState();
    }

    @Override
    public void extractRenderState(HourWardenEntity entity, HourWardenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack = entity.currentAttack().ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.enraged = entity.isEnraged();
        state.minute = entity.minuteAngle(partialTicks);
        state.gears = entity.gearAngle(partialTicks);
        state.owner = entity;
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(HourWardenRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite (er sinkt auf die Knie, siehe {@link HourWardenModel}). */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }
}
