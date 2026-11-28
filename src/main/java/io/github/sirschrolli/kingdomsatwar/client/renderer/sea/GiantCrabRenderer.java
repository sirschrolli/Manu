package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.GiantCrabModel;
import io.github.sirschrolli.kingdomsatwar.entity.sea.GiantCrabEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

/** Riesenkrabbe: beim Laufen dreht sie sich quer zur Laufrichtung und läuft seitwärts; Jungtiere sind halb so groß. */
public class GiantCrabRenderer extends MobRenderer<GiantCrabEntity, SeaRenderState, GiantCrabModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/giant_crab/giant_crab.png");

    public GiantCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new GiantCrabModel(context.bakeLayer(ModModelLayers.GIANT_CRAB)), 0.6f);
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
    public void extractRenderState(GiantCrabEntity entity, SeaRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.inWater = entity.isInWater();
        state.attack.copyFrom(entity.pinchAnimation);
        state.special.copyFrom(entity.clackAnimation);
        float walking = state.walkAnimationSpeed > 0.06f ? 1.0f : 0.0f;
        state.sideways = Anim.follow(entity, 0, walking, state.ageInTicks, 5.0f);
    }

    @Override
    protected void setupRotations(SeaRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.deathTime <= 0.0f && state.sideways > 0.001f) {
            poseStack.rotateDegrees(Axis.YP, state.sideways * 90.0f);
        }
    }

    @Override
    protected void scale(SeaRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(0.45f, 0.45f, 0.45f);
        }
    }

    @Override
    protected float getShadowStrength(SeaRenderState state) {
        return 0.5f;
    }
}
