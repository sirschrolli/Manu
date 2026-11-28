package io.github.sirschrolli.kingdomsatwar.client.renderer.miniboss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.TrollBoulderEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Ein Trollbrocken: der Block, den der Trollvater aus dem Boden gerissen hat – über seinem Kopf ruhig, im Flug überschlägt
 * er sich; die kleinen Brocken, die von der Decke fallen, sind halb so groß.
 */
public class TrollBoulderRenderer extends EntityRenderer<TrollBoulderEntity, TrollBoulderRenderer.State> {
    public static class State extends EntityRenderState {
        public final BlockModelRenderState block = new BlockModelRenderState();
        public float spin;
        public float size;
    }

    private final BlockModelResolver blockModelResolver;

    public TrollBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
        this.shadowRadius = 0.6f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TrollBoulderEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        this.blockModelResolver.update(state.block, entity.blockState(), EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
        state.spin = entity.spin(partialTicks);
        state.size = entity.isSmall() ? 0.7f : 1.25f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.block.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0f, state.size * 0.5f, 0.0f);
            poseStack.rotate(Axis.YP.rotationDegrees(state.spin * 0.6f + 20.0f));
            poseStack.rotate(Axis.XP.rotationDegrees(state.spin));
            poseStack.scale(state.size, state.size, state.size);
            poseStack.translate(-0.5f, -0.5f, -0.5f);
            state.block.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
