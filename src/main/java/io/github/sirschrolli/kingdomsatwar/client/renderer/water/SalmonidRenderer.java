package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Salmonid;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Lachs und Rotlachs: drei Größen wie bei Vanilla (klein, mittel, groß), Schwimmen und Zappeln wie beim Vanilla-Lachs. */
public class SalmonidRenderer extends WildRenderer<Salmonid, SalmonidRenderer.State, SalmonModel> {
    private static final Identifier[] TEXTURES = {WaterRenderers.texture("salmon", "lachs"), WaterRenderers.texture("salmon", "rotlachs")};
    private final SalmonModel small;
    private final SalmonModel medium;
    private final SalmonModel large;

    public static class State extends SalmonRenderState {
        public int look;
    }

    public SalmonidRenderer(EntityRendererProvider.Context context) {
        super(context, new SalmonModel(context.bakeLayer(WaterRenderers.SALMON)), 0.4f, new SalmonRenderer(context));
        this.small = new SalmonModel(context.bakeLayer(WaterRenderers.SALMON_SMALL));
        this.medium = this.getModel();
        this.large = new SalmonModel(context.bakeLayer(WaterRenderers.SALMON_LARGE));
    }

    @Override
    protected void extract(Salmonid entity, State state, float partialTicks) {
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
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = switch (state.variant) {
            case SMALL -> this.small;
            case MEDIUM -> this.medium;
            case LARGE -> this.large;
        };
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void setupRotations(State state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        float amplitude = state.isInWater ? 1.0f : 1.3f;
        float speed = state.isInWater ? 1.0f : 1.7f;
        poseStack.rotateDegrees(Axis.YP, amplitude * 4.3f * Mth.sin(speed * 0.6f * state.ageInTicks));
        if (!state.isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.rotateDegrees(Axis.ZP, 90.0f);
        }
    }
}
