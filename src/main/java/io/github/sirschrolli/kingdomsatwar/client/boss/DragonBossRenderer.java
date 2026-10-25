package io.github.sirschrolli.kingdomsatwar.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Zeichnet einen Drachen-Boss: vergrößertes Modell, Schräglage und Neigung im Flug, leuchtende Teile mit
 * veränderlicher Helligkeit (pulsieren, glühen vor dem Odem auf, erlöschen im Tod).
 */
public class DragonBossRenderer<T extends DragonBossEntity, M extends EntityModel<BossRenderState>>
        extends MobRenderer<T, BossRenderState, M> {
    private final Identifier texture;
    private final float modelScale;
    /** Höhe des Körpermittelpunkts über dem Boden in Blöcken (Drehpunkt für Neigung und Schräglage). */
    private final float pivotHeight;

    public DragonBossRenderer(EntityRendererProvider.Context context, M model, Identifier texture, Identifier glowTexture,
            float modelScale, float pivotHeight, float shadow) {
        super(context, model, shadow);
        this.texture = texture;
        this.modelScale = modelScale;
        this.pivotHeight = pivotHeight;
        this.addLayer(new GlowLayer(this, RenderTypes.eyes(glowTexture)));
    }

    @Override
    public Identifier getTextureLocation(BossRenderState state) {
        return this.texture;
    }

    @Override
    public BossRenderState createRenderState() {
        return new BossRenderState();
    }

    @Override
    public void extractRenderState(T entity, BossRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bossState = entity.getBossState();
        state.moveMode = entity.getMoveMode();
        state.attack = entity.getAttackAnim();
        state.attackTime = state.attack == BossAnims.NONE ? 0 : (entity.tickCount - entity.clientAttackStart + partialTicks) / 20.0f;
        state.stateTime = (entity.tickCount - entity.clientStateStart + partialTicks) / 20.0f;
        state.deathSeconds = entity.isDeadOrDying() ? (entity.deathTime + partialTicks) / 20.0f : 0.0f;
        state.phase = entity.getPhase();
        state.flapPhase = Mth.lerp(partialTicks, entity.flapPhaseO, entity.flapPhase);
        state.airBlend = Mth.lerp(partialTicks, entity.airBlendO, entity.airBlend);
        state.bank = Mth.lerp(partialTicks, entity.bankO, entity.bank);
        double vy = entity.getDeltaMovement().y;
        double vh = entity.getDeltaMovement().horizontalDistance();
        state.flightPitch = entity.isFlying() ? (float) Mth.clamp(Math.atan2(vy, Math.max(vh, 0.05)) * Mth.RAD_TO_DEG, -35.0, 35.0) : 0.0f;
        float now = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        for (int i = 0; i < state.yawLag.length; i++) {
            state.yawLag[i] = Mth.wrapDegrees(entity.yawHistory[i] - now);
        }
        state.glow = this.glowLevel(state);
        // Kein rotes Aufblinken bei jedem Treffer (ein ganzes Heer trifft ständig) und kein Rot im Tod
        state.hasRedOverlay = false;
    }

    private float glowLevel(BossRenderState state) {
        float g = 0.82f + 0.18f * Mth.sin(state.ageInTicks * 0.12f);
        if (state.phase == 3) {
            g += 0.15f + 0.1f * Mth.sin(state.ageInTicks * 0.4f);
        }
        if ((state.attack == BossAnims.BREATH || state.attack == BossAnims.RING || state.attack == BossAnims.VOLLEY)
                && state.attackTime < 1.0f) {
            g += state.attackTime * 0.9f;
        }
        if (state.bossState == DragonBossEntity.State.AWAKENING) {
            g *= Mth.clamp(state.stateTime / 6.0f, 0.15f, 1.0f);
        }
        if (state.deathSeconds > 0) {
            g *= Mth.clamp(1.0f - (state.deathSeconds - 2.0f) / 4.0f, 0.0f, 1.0f) + Mth.clamp((state.deathSeconds - 7.0f) * 2.0f, 0.0f, 1.2f);
        }
        return Math.max(0.0f, g);
    }

    @Override
    protected void scale(BossRenderState state, PoseStack poseStack) {
        poseStack.scale(this.modelScale, this.modelScale, this.modelScale);
    }

    @Override
    protected void setupRotations(BossRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        poseStack.rotateDegrees(Axis.YP, 180.0f - bodyRot);
        if (state.flightPitch != 0.0f || state.bank != 0.0f) {
            poseStack.translate(0.0f, this.pivotHeight, 0.0f);
            poseStack.rotateDegrees(Axis.XP, -state.flightPitch);
            poseStack.rotateDegrees(Axis.ZP, state.bank);
            poseStack.translate(0.0f, -this.pivotHeight, 0.0f);
        }
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected AABB getBoundingBoxForCulling(T entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(16.0, 8.0, 16.0);
    }

    /** Leuchtende Teile (Augen, Risse, Adern) – Helligkeit aus dem Zustand. */
    private final class GlowLayer extends RenderLayer<BossRenderState, M> {
        private final RenderType renderType;

        GlowLayer(RenderLayerParent<BossRenderState, M> parent, RenderType renderType) {
            super(parent);
            this.renderType = renderType;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, BossRenderState state, float yRot, float xRot) {
            float g = Mth.clamp(state.glow, 0.0f, 1.0f);
            if (g <= 0.01f) {
                return;
            }
            int c = Math.round(255 * g);
            int color = ARGB.color(255, c, c, c);
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, this.renderType, light, OverlayTexture.NO_OVERLAY,
                    color, null, state.outlineColor);
            if (state.glow > 1.05f) {
                // zusätzlicher Schein bei sehr hellem Glühen (vor dem Odem, Phase 3)
                int extra = Math.round(255 * Mth.clamp(state.glow - 1.0f, 0.0f, 1.0f));
                collector.order(2).submitModel(this.getParentModel(), state, poseStack, this.renderType, light, OverlayTexture.NO_OVERLAY,
                        ARGB.color(255, extra, extra, extra), null, state.outlineColor);
            }
        }
    }
}
