package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Codfish;
import net.minecraft.client.model.animal.fish.CodModel;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Dorsch und Seelachs: eigenes Modell und Schuppenkleid, Schwimmen und Zappeln an Land wie beim Vanilla-Kabeljau. */
public class CodfishRenderer extends WildRenderer<Codfish, CodfishRenderer.State, CodModel> {
    private static final Identifier[] TEXTURES = {WaterRenderers.texture("cod", "dorsch"), WaterRenderers.texture("cod", "seelachs")};

    public static class State extends LivingEntityRenderState {
        public int look;
    }

    public CodfishRenderer(EntityRendererProvider.Context context) {
        super(context, new CodModel(context.bakeLayer(WaterRenderers.COD)), 0.3f, new CodRenderer(context));
    }

    @Override
    protected void extract(Codfish entity, State state, float partialTicks) {
        state.look = entity.getLook().ordinal();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return pick(TEXTURES, state.look);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    protected void setupRotations(State state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        poseStack.rotateDegrees(Axis.YP, 4.3f * Mth.sin(0.6f * state.ageInTicks));
        if (!state.isInWater) {
            poseStack.translate(0.1f, 0.1f, -0.1f);
            poseStack.rotateDegrees(Axis.ZP, 90.0f);
        }
    }
}
