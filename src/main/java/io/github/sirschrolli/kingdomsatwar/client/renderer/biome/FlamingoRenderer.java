package io.github.sirschrolli.kingdomsatwar.client.renderer.biome;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.FlamingoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Flamingo: zwei Arten; in der Kurve geneigt. */
public class FlamingoRenderer extends MobRenderer<FlamingoEntity, FlamingoRenderState, FlamingoModel> {
    private static final Identifier[] TEXTURES = {
            KingdomsAtWar.id("textures/entity/flamingo/flamingo.png"),
            KingdomsAtWar.id("textures/entity/flamingo/flamingo_crimson.png")};

    public FlamingoRenderer(EntityRendererProvider.Context context) {
        super(context, new FlamingoModel(context.bakeLayer(ModModelLayers.FLAMINGO)), 0.35f);
    }

    @Override
    public Identifier getTextureLocation(FlamingoRenderState state) {
        return TEXTURES[Math.floorMod(state.variant, TEXTURES.length)];
    }

    @Override
    public FlamingoRenderState createRenderState() {
        return new FlamingoRenderState();
    }

    @Override
    public void extractRenderState(FlamingoEntity entity, FlamingoRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        float age = state.ageInTicks;
        boolean flying = entity.isFlying() && !entity.isInWater();
        state.fly = Anim.follow(entity, 0, flying ? 1.0f : 0.0f, age, 6.0f);
        double vy = entity.getY() - entity.yo;
        float wantFlap = vy > 0.02 ? 1.0f : vy < -0.05 ? 0.15f : (Mth.sin(age * 0.09f + entity.getId()) > 0.1f ? 0.95f : 0.3f);
        state.flap = Anim.follow(entity, 1, wantFlap, age, 5.0f);
        state.feed = Anim.follow(entity, 2, entity.isFeeding() ? 1.0f : 0.0f, age, 3.5f);
        state.rest = Anim.follow(entity, 3, entity.isResting() ? 1.0f : 0.0f, age, 3.0f);
        boolean night = entity.level().isDarkOutside();
        state.tuck = Anim.follow(entity, 4, entity.isResting() && night ? 1.0f : 0.0f, age, 2.0f);
        float turn = Mth.wrapDegrees(entity.getYRot() - entity.yRotO);
        state.bank = Anim.follow(entity, 5, flying ? Mth.clamp(-turn * 0.08f, -0.6f, 0.6f) : 0.0f, age, 4.0f);
        state.wading = entity.isInWater();
        state.call.copyFrom(entity.callAnimation);
    }

    @Override
    protected void setupRotations(FlamingoRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.bank != 0.0f) {
            poseStack.translate(0.0f, 0.7f, 0.0f);
            poseStack.rotate(Axis.ZP, state.bank);
            poseStack.translate(0.0f, -0.7f, 0.0f);
        }
    }
}
