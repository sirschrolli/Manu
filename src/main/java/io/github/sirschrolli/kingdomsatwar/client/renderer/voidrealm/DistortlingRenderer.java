package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.voidrealm.DistortlingModel;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.DistortlingEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Zerrling: neben dem Leib flackern immer wieder ein türkiser und ein magentafarbener Schatten (die Farben spalten sich
 * auf); nach einem Sprung bleibt dort, wo er stand, kurz ein verblassendes Nachbild.
 */
public class DistortlingRenderer extends MobRenderer<DistortlingEntity, VoidCreatureRenderState, DistortlingModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/distortling/distortling.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/distortling/distortling_glow.png"));
    private static final RenderType GHOST = RenderTypes.entityTranslucentEmissive(KingdomsAtWar.id("textures/entity/distortling/distortling_ghost.png"));
    private static final int CYAN = 0x5AF0FF;
    private static final int MAGENTA = 0xFF5AE0;

    public DistortlingRenderer(EntityRendererProvider.Context context) {
        super(context, new DistortlingModel(context.bakeLayer(ModModelLayers.DISTORTLING)), 0.45f);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, VoidCreatureRenderState state, float yRot,
                    float xRot) {
                ghosts(this.getParentModel(), poseStack, collector, state);
            }
        });
    }

    private static void ghosts(DistortlingModel model, PoseStack poseStack, SubmitNodeCollector collector, VoidCreatureRenderState state) {
        float t = state.ageInTicks;
        int bucket = (int) (t / 2.0f);
        float after = Math.max(0.0f, 1.0f - state.sinceBlink / 14.0f);
        // nur in kurzen Bildfehler-Momenten (etwa jeder vierte Augenblick) spalten sich die Farben auf
        int h = (bucket * 0x9E3779B1 + state.seed * 0x85EBCA6B) >>> 16;
        float glitch = (h & 15) < 4 ? 1.0f : 0.0f;
        float split = 0.1f + 0.06f * ((h >> 4) & 3) + 0.2f * after;
        int alpha = Math.round(Mth.clamp(75.0f * glitch + 140.0f * after, 0.0f, 200.0f));
        if (alpha > 8) {
            for (int side = -1; side <= 1; side += 2) {
                poseStack.pushPose();
                poseStack.translate(split * side, 0.0f, 0.0f);
                collector.order(2).submitModel(model, state, poseStack, GHOST, 0xF000F0, OverlayTexture.NO_OVERLAY,
                        ARGB.color(alpha, side < 0 ? CYAN : MAGENTA), null, state.outlineColor);
                poseStack.popPose();
            }
        }
        // Nachbild am alten Ort (die Verschiebung von der Welt in die Lage des Modells umgerechnet)
        if (after > 0.0f) {
            float yaw = (180.0f - state.bodyRot) * Mth.DEG_TO_RAD;
            double dx = state.blinkDx;
            double dz = state.blinkDz;
            float lx = (float) (dx * Mth.cos(yaw) - dz * Mth.sin(yaw));
            float lz = (float) (dx * Mth.sin(yaw) + dz * Mth.cos(yaw));
            poseStack.pushPose();
            poseStack.translate(-lx, (float) -state.blinkDy, lz);
            collector.order(2).submitModel(model, state, poseStack, GHOST, 0xF000F0, OverlayTexture.NO_OVERLAY,
                    ARGB.color(Math.round(150.0f * after), (bucket & 1) == 0 ? CYAN : MAGENTA), null, state.outlineColor);
            poseStack.popPose();
        }
    }

    @Override
    public Identifier getTextureLocation(VoidCreatureRenderState state) {
        return TEXTURE;
    }

    @Override
    public VoidCreatureRenderState createRenderState() {
        return new VoidCreatureRenderState();
    }

    @Override
    public void extractRenderState(DistortlingEntity entity, VoidCreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.attack.copyFrom(entity.attackAnimation);
        state.seed = entity.getId();
        state.sinceBlink = entity.tickCount - entity.blinkTick + partialTicks;
        double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
        state.blinkDx = entity.blinkFrom.x - x;
        state.blinkDy = entity.blinkFrom.y - y;
        state.blinkDz = entity.blinkFrom.z - z;
    }
}
