package io.github.sirschrolli.kingdomsatwar.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.creature.GooseModel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.GooseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Gans: Graugans oder weiße Hausgans. */
public class GooseRenderer extends MobRenderer<GooseEntity, CreatureRenderState, GooseModel> {
    private static final Identifier[] TEXTURES = {
            KingdomsAtWar.id("textures/entity/goose/goose_grey.png"),
            KingdomsAtWar.id("textures/entity/goose/goose_white.png")};

    public GooseRenderer(EntityRendererProvider.Context context) {
        super(context, new GooseModel(context.bakeLayer(ModModelLayers.GOOSE)), 0.35f);
    }

    @Override
    public Identifier getTextureLocation(CreatureRenderState state) {
        return TEXTURES[Mth.clamp(state.variant, 0, TEXTURES.length - 1)];
    }

    /** Jungtiere halb so groß (der Kopf wird im Modell vergrößert). */
    @Override
    protected void scale(CreatureRenderState state, PoseStack poseStack) {
        if (state.isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
        }
    }

    @Override
    public CreatureRenderState createRenderState() {
        return new CreatureRenderState();
    }

    @Override
    public void extractRenderState(GooseEntity entity, CreatureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getVariant();
        state.angry = entity.isAggressive();
        state.flap = Mth.lerp(partialTicks, entity.oFlap, entity.flap);
        state.flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.flapSpeed);
        state.attack.copyFrom(entity.honkAnimation);
    }
}
