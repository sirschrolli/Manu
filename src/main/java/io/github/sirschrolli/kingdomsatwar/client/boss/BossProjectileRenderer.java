package io.github.sirschrolli.kingdomsatwar.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;

/** Geschosse der Bosse als leuchtende, zur Kamera gedrehte Bilder (Feuerball, Meteor …). */
public class BossProjectileRenderer extends EntityRenderer<BossProjectile, BossProjectileRenderer.State> {
    private static final Map<BossProjectile.Kind, RenderType> TYPES = new EnumMap<>(BossProjectile.Kind.class);

    static {
        for (BossProjectile.Kind kind : BossProjectile.Kind.values()) {
            net.minecraft.resources.Identifier texture = KingdomsAtWar.id("textures/entity/boss/projectile_" + kind.name().toLowerCase() + ".png");
            TYPES.put(kind, kind == BossProjectile.Kind.BUBBLE || kind == BossProjectile.Kind.ICE_SPEAR
                    ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture));
        }
    }

    public static class State extends EntityRenderState {
        BossProjectile.Kind kind = BossProjectile.Kind.FIREBALL;
        float spin;
    }

    public BossProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(BossProjectile entity, BlockPos pos) {
        return 15;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BossProjectile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.kind = entity.getKind();
        state.spin = (entity.tickCount + partialTicks) * 12.0f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        float size = state.kind.size * 1.6f;
        poseStack.translate(0.0f, 0.25f * size, 0.0f);
        poseStack.scale(size, size, size);
        poseStack.rotate(camera.orientation);
        poseStack.rotateDegrees(com.mojang.math.Axis.ZP, state.spin);
        RenderType type = TYPES.get(state.kind);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> quad(pose, buffer, state.lightCoords));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int light) {
        vertex(buffer, pose, light, -0.5f, -0.5f, 0, 1);
        vertex(buffer, pose, light, 0.5f, -0.5f, 1, 1);
        vertex(buffer, pose, light, 0.5f, 0.5f, 1, 0);
        vertex(buffer, pose, light, -0.5f, 0.5f, 0, 0);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, float y, int u, int v) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
