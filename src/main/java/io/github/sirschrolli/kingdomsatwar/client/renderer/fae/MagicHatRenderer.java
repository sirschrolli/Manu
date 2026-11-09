package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicHatEntity;
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

/**
 * MissMystiks Zylinder beim Hut-Trick (derselbe Hut wie auf ihrem Kopf, mit Feder und Rose): im Flug überschlägt er sich,
 * gelandet steht er kopfüber und dreht sich immer schneller, bis es knallt; dann fliegt er sich drehend zurück.
 */
public class MagicHatRenderer extends EntityRenderer<MagicHatEntity, MagicHatRenderer.State> {
    private static final RenderType BODY = RenderTypes.entityTranslucent(MissMystikRenderer.TEXTURE);

    public static class State extends EntityRenderState {
        public MagicHatEntity.Phase phase = MagicHatEntity.Phase.FLY;
        public float phaseTime;
        public float age;
    }

    private final ModelPart hat;

    public MagicHatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.4f;
        ModelPart root = context.bakeLayer(ModModelLayers.MISS_MYSTIK);
        this.hat = root.getChild("body").getChild("chest").getChild("head").getChild("hat");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagicHatEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.phase = entity.phase();
        state.phaseTime = entity.phaseTime(partialTicks);
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(MagicHatEntity entity, BlockPos pos) {
        return Math.max(10, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        switch (state.phase) {
            case FLY, RETURN -> {
                poseStack.translate(0.0f, 0.3f, 0.0f);
                poseStack.rotate(Axis.YP.rotationDegrees(state.age * 20.0f));
                poseStack.rotate(Axis.XP.rotationDegrees(state.age * 26.0f));
            }
            case SPIN -> {
                // kopfüber, die Öffnung nach oben, sich immer schneller drehend und leicht wippend
                float t = state.phaseTime;
                float spin = t * (6.0f + t * 0.35f);
                poseStack.translate(0.0f, 0.55f + Mth.sin(t * 0.5f) * 0.04f, 0.0f);
                poseStack.rotate(Axis.YP.rotationDegrees(spin));
                poseStack.rotate(Axis.XP.rotationDegrees(180.0f + Mth.sin(t * 0.7f) * 6.0f));
                float pop = t > MagicHatEntity.BURST - 3 && t < MagicHatEntity.BURST + 4 ? 1.25f : 1.0f;
                poseStack.scale(pop, pop, pop);
            }
            case BACK -> {
                poseStack.popPose();
                return;
            }
        }
        poseStack.scale(-1.3f, -1.3f, 1.3f);
        this.hat.resetPose();
        this.hat.x = 0.0f;
        this.hat.y = 4.0f;
        this.hat.z = 0.0f;
        this.hat.xRot = 0.0f;
        this.hat.yRot = 0.0f;
        this.hat.zRot = 0.0f;
        collector.submitModelPart(this.hat, poseStack, BODY, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.hat, poseStack, MissMystikRenderer.GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
