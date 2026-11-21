package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhaseBoulderEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.BlockModelRenderState;

/**
 * Ein Phasenbrocken: der Block, den Phasmor aus dem Heiligtum gerissen hat – erst kreist er langsam um den König und wächst
 * dabei aus dem Boden, im Flug überschlägt er sich.
 */
public class PhaseBoulderRenderer extends EntityRenderer<PhaseBoulderEntity, PhaseBoulderRenderer.State> {
    public static class State extends EntityRenderState {
        public final BlockModelRenderState block = new BlockModelRenderState();
        public float spin;
        public float size;
    }

    private final BlockModelResolver blockModelResolver;

    public PhaseBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
        this.shadowRadius = 0.5f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PhaseBoulderEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.blockModelResolver.update(state.block, entity.blockState(), EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
        state.spin = entity.spin(partialTicks);
        state.size = entity.rise(partialTicks) * 0.9f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.block.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.45f, 0.0f);
            poseStack.rotate(Axis.YP.rotationDegrees(state.spin));
            poseStack.rotate(Axis.XP.rotationDegrees(state.spin * 0.7f));
            poseStack.scale(state.size, state.size, state.size);
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            state.block.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
