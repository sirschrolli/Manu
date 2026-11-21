package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.StormGullEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/** Sturmmöwe: zwei Arten, in der Kurve geneigt, ein gestohlener Fisch im Schnabel. */
public class StormGullRenderer extends MobRenderer<StormGullEntity, StormGullRenderState, StormGullModel> {
    private static final Identifier[] TEXTURES = {
            KingdomsAtWar.id("textures/entity/storm_gull/storm_gull.png"),
            KingdomsAtWar.id("textures/entity/storm_gull/storm_gull_dark.png")};

    public StormGullRenderer(EntityRendererProvider.Context context) {
        super(context, new StormGullModel(context.bakeLayer(ModModelLayers.STORM_GULL)), 0.3f);
        this.addLayer(new CarryLayer(this));
    }

    @Override
    public Identifier getTextureLocation(StormGullRenderState state) {
        return TEXTURES[Math.floorMod(state.variant, TEXTURES.length)];
    }

    @Override
    public StormGullRenderState createRenderState() {
        return new StormGullRenderState();
    }

    @Override
    public void extractRenderState(StormGullEntity entity, StormGullRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        float age = state.ageInTicks;
        boolean flying = entity.isFlying() && !entity.isInWater();
        state.fly = Anim.follow(entity, 0, flying ? 1.0f : 0.0f, age, 6.0f);
        // schlagen beim Steigen, Abheben und auf der Flucht mit Beute; gleiten beim Sinken; dazwischen in kurzen Schüben
        double vy = entity.getY() - entity.yo;
        float wantFlap = vy > 0.02 || !entity.getCarried().isEmpty() ? 1.0f
                : vy < -0.04 ? 0.0f : (Mth.sin(age * 0.07f + entity.getId()) > 0.35f ? 0.85f : 0.1f);
        state.flap = Anim.follow(entity, 1, wantFlap, age, 5.0f);
        // Schräglage aus der Drehgeschwindigkeit
        float turn = Mth.wrapDegrees(entity.getYRot() - entity.yRotO);
        state.bank = Anim.follow(entity, 2, flying ? Mth.clamp(-turn * 0.09f, -0.7f, 0.7f) : 0.0f, age, 4.0f);
        double flat = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        state.pitch = Anim.follow(entity, 3, flying ? (float) Mth.clamp(-Math.atan2(vy, Math.max(flat, 0.05)), -0.6, 0.6) : 0.0f, age, 4.0f);
        state.floating = entity.isInWater() && !entity.isFlying();
        this.itemModelResolver.updateForLiving(state.carried, entity.getCarried(), ItemDisplayContext.GROUND, entity);
        state.call.copyFrom(entity.callAnimation);
        state.peck.copyFrom(entity.peckAnimation);
        state.snatch.copyFrom(entity.snatchAnimation);
    }

    @Override
    protected void setupRotations(StormGullRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.bank != 0.0f) {
            // um die Längsachse in Höhe des Leibs neigen
            poseStack.translate(0.0f, 0.4f, 0.0f);
            poseStack.rotate(Axis.ZP, state.bank);
            poseStack.translate(0.0f, -0.4f, 0.0f);
        }
    }

    /** Der gestohlene Fisch im Schnabel. */
    static final class CarryLayer extends RenderLayer<StormGullRenderState, StormGullModel> {
        CarryLayer(RenderLayerParent<StormGullRenderState, StormGullModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, StormGullRenderState state, float yRot, float xRot) {
            if (state.carried.isEmpty()) {
                return;
            }
            StormGullModel model = this.getParentModel();
            poseStack.pushPose();
            model.root().translateAndRotate(poseStack);
            model.body.translateAndRotate(poseStack);
            model.head.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -1.2f / 16.0f, -4.8f / 16.0f);
            poseStack.rotateDegrees(Axis.YP, 90.0f);
            poseStack.rotateDegrees(Axis.XP, -20.0f);
            poseStack.scale(0.55f, 0.55f, 0.55f);
            state.carried.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
