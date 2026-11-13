package io.github.sirschrolli.kingdomsatwar.client.renderer.serpent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraFeatherEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Eine Feder aus Quetzaras Krause im Flug: die Spitze voran, um sich selbst trudelnd, die Giftspitze glimmt. */
public class QuetzaraFeatherRenderer extends EntityRenderer<QuetzaraFeatherEntity, QuetzaraFeatherRenderer.State> {
    private static final RenderType FEATHER = RenderTypes.entityCutout(SerpentDraw.tex("feather"));
    private static final RenderType FEATHER_GLOW = RenderTypes.eyes(SerpentDraw.tex("feather_glow"));

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public float spin;
        public boolean stuck;
    }

    private final float[] aim = new float[2];
    private final ModelPart feather;

    public QuetzaraFeatherRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.feather = context.bakeLayer(SerpentRenderers.QUETZARA_FEATHER).getChild("feather");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(QuetzaraFeatherEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        FlightAim.aim(entity, partialTicks, false, false, this.aim);
        state.yRot = this.aim[0];
        state.xRot = this.aim[1];
        state.stuck = entity.stuck();
        state.spin = state.stuck ? 30.0f : (entity.tickCount + partialTicks) * 25.0f;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.rotate(Axis.YP.rotationDegrees(state.yRot + 180.0f));
        pose.rotate(Axis.XP.rotationDegrees(state.xRot));
        pose.rotate(Axis.ZP.rotationDegrees(state.spin));
        pose.scale(-1.0f, -1.0f, 1.0f);
        this.feather.resetPose();
        collector.submitModelPart(this.feather, pose, FEATHER, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.feather, pose, FEATHER_GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
