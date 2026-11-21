package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.VenomGlobEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/** Ein Klumpen Gift im Flug: ein zäher, grün leuchtender Tropfen, der Schweif hinten, er wabert. */
public class VenomGlobRenderer extends EntityRenderer<VenomGlobEntity, VenomGlobRenderer.State> {
    private static final RenderType GLOB = RenderTypes.entityTranslucent(SerpentDraw.tex("venom_glob"));
    private static final RenderType GLOB_GLOW = RenderTypes.eyes(SerpentDraw.tex("venom_glob_glow"));

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public float age;
    }

    private final float[] aim = new float[2];
    private final ModelPart glob;

    public VenomGlobRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.glob = context.bakeLayer(SerpentRenderers.VENOM_GLOB).getChild("glob");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(VenomGlobEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        FlightAim.aim(entity, partialTicks, false, false, this.aim);
        state.yRot = this.aim[0];
        state.xRot = this.aim[1];
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(VenomGlobEntity entity, BlockPos pos) {
        return 12;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.0f, 0.25f, 0.0f);
        pose.rotate(Axis.YP.rotationDegrees(state.yRot + 180.0f));
        pose.rotate(Axis.XP.rotationDegrees(state.xRot));
        float wobble = 1.0f + Mth.sin(state.age * 0.9f) * 0.08f;
        pose.scale(-1.6f * wobble, -1.6f / wobble, 1.6f);
        this.glob.resetPose();
        collector.submitModelPart(this.glob, pose, GLOB, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.glob, pose, GLOB_GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
