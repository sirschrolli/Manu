package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.MantaRayModel;
import io.github.sirschrolli.kingdomsatwar.entity.sea.MantaRayEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Mantarochen: dreht sich beim Sprung einmal um die eigene Längsachse. */
public class MantaRayRenderer extends MobRenderer<MantaRayEntity, SeaRenderState, MantaRayModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/manta_ray/manta_ray.png");
    public static final float SCALE = 0.85f;

    public MantaRayRenderer(EntityRendererProvider.Context context) {
        super(context, new MantaRayModel(context.bakeLayer(ModModelLayers.MANTA_RAY)), 1.3f);
    }

    @Override
    public Identifier getTextureLocation(SeaRenderState state) {
        return TEXTURE;
    }

    @Override
    public SeaRenderState createRenderState() {
        return new SeaRenderState();
    }

    @Override
    public void extractRenderState(MantaRayEntity entity, SeaRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flap = Mth.lerp(partialTicks, entity.flapO, entity.flap);
        state.bank = Mth.lerp(partialTicks, entity.bankO, entity.bank);
        state.spin = Mth.lerp(partialTicks, entity.spinO, entity.spin);
        state.inWater = entity.isInWater();
    }

    @Override
    protected void setupRotations(SeaRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.spin > 0.0f && state.deathTime <= 0.0f) {
            // eine ganze Rolle um die Längsachse, weich ein- und ausgeleitet
            float roll = Mth.sin(state.spin * Mth.HALF_PI) * 360.0f;
            poseStack.translate(0.0f, 0.3f, 0.0f);
            poseStack.rotateDegrees(Axis.ZP, roll);
            poseStack.translate(0.0f, -0.3f, 0.0f);
        }
    }

    @Override
    protected void scale(SeaRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    protected float getShadowStrength(SeaRenderState state) {
        return 0.4f;
    }
}
