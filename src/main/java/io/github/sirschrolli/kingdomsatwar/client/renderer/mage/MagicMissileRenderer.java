package io.github.sirschrolli.kingdomsatwar.client.renderer.mage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MagicMissile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/** Magisches Geschoss: ein farbiger Schein um einen hellen, pulsierenden Kern, immer zur Kamera gedreht. */
public class MagicMissileRenderer extends EntityRenderer<MagicMissile, MagicMissileRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final RenderType TYPE = RenderTypes.entityTranslucentEmissive(TEXTURE);
    private static final int FULL_BRIGHT = 0xF000F0;

    public static class State extends EntityRenderState {
        DragonType element = DragonType.FIRE;
        float time;
    }

    public MagicMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(MagicMissile entity, BlockPos pos) {
        return 15;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagicMissile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.element = entity.element();
        state.time = entity.tickCount + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.2f, 0.0f);
        poseStack.rotate(camera.orientation);
        float pulse = 1.0f + 0.12f * (float) Math.sin(state.time * 0.8f);
        int halo = ARGB.color(200, state.element.color());
        poseStack.pushPose();
        poseStack.rotateDegrees(Axis.ZP, state.time * 9.0f);
        poseStack.scale(1.1f * pulse, 1.1f * pulse, 1.1f * pulse);
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> quad(pose, buffer, halo));
        poseStack.popPose();
        int core = ARGB.color(255, ARGB.srgbLerp(0.75f, state.element.color(), 0xFFFFFF));
        poseStack.scale(0.5f * pulse, 0.5f * pulse, 0.5f * pulse);
        collector.submitCustomGeometry(poseStack, TYPE, (pose, buffer) -> quad(pose, buffer, core));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0, 1, color);
        vertex(buffer, pose, 0.5f, -0.5f, 1, 1, color);
        vertex(buffer, pose, 0.5f, 0.5f, 1, 0, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0, 0, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, int u, int v, int color) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
