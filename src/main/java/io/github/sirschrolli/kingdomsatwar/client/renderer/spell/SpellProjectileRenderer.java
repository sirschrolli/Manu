package io.github.sirschrolli.kingdomsatwar.client.renderer.spell;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.spell.SpellProjectileModels;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellProjectile;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Zaubergeschosse: der Feuerblitz als wirbelnder Glutball mit Flammenkranz und Nachglühen, der Phönix und die Frostlanze
 * als Modelle (in Flugrichtung gedreht, mit Leuchtebene und einem Schein darum).
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderer.State> {
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Identifier FIREBALL = KingdomsAtWar.id("textures/entity/boss/projectile_fireball.png");
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final RenderType FIREBALL_TYPE = RenderTypes.entityTranslucentEmissive(FIREBALL);
    private static final Identifier PHOENIX = KingdomsAtWar.id("textures/entity/spell/phoenix/phoenix.png");
    private static final Identifier PHOENIX_GLOW = KingdomsAtWar.id("textures/entity/spell/phoenix/phoenix_glow.png");
    private static final Identifier LANCE = KingdomsAtWar.id("textures/entity/spell/ice_lance/ice_lance.png");
    private static final Identifier LANCE_GLOW = KingdomsAtWar.id("textures/entity/spell/ice_lance/ice_lance_glow.png");

    public static class State extends EntityRenderState {
        public SpellProjectile.Kind kind = SpellProjectile.Kind.FIREBOLT;
        public float age;
        public float yRot;
        public float xRot;
        public Vec3 motion = Vec3.ZERO;
    }

    private final SpellProjectileModels.Phoenix phoenix;
    private final SpellProjectileModels.Lance lance;

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.phoenix = new SpellProjectileModels.Phoenix(context.bakeLayer(ModModelLayers.PHOENIX));
        this.lance = new SpellProjectileModels.Lance(context.bakeLayer(ModModelLayers.ICE_LANCE));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpellProjectile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.kind = entity.kind();
        state.age = entity.tickCount + partialTicks;
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.motion = entity.getDeltaMovement();
    }

    @Override
    protected int getBlockLightLevel(SpellProjectile entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        switch (state.kind) {
            case FIREBOLT -> this.firebolt(state, poseStack, collector, camera);
            case PHOENIX -> this.phoenix(state, poseStack, collector, camera);
            case ICE_LANCE -> this.lance(state, poseStack, collector, camera);
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** Glutball: Kern aus Flammen, ein heißer Schein, ein Kranz kleiner Flammen und drei verglühende Nachbilder. */
    private void firebolt(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.15f, 0.0f);
        // Nachglühen hinter dem Ball
        for (int i = 3; i >= 1; i--) {
            Vec3 back = s.motion.scale(-0.28 * i);
            float size = 0.75f - i * 0.14f;
            int col = scale(0xFF6A1A, 0.55f - i * 0.12f);
            poseStack.pushPose();
            poseStack.translate(back.x, back.y, back.z);
            poseStack.rotate(camera.orientation);
            poseStack.scale(size, size, size);
            collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, col));
            poseStack.popPose();
        }
        poseStack.rotate(camera.orientation);
        float pulse = 1.0f + 0.1f * Mth.sin(s.age * 1.1f);
        poseStack.pushPose();
        poseStack.scale(1.25f * pulse, 1.25f * pulse, 1.25f * pulse);
        int glow = scale(0xFF8A2A, 0.9f);
        collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, glow));
        poseStack.popPose();
        // Flammenkranz
        for (int i = 0; i < 6; i++) {
            poseStack.pushPose();
            poseStack.rotate(Axis.ZP.rotationDegrees(s.age * 14.0f + i * 60.0f));
            poseStack.translate(0.0f, 0.3f, 0.0f);
            poseStack.scale(0.28f, 0.28f, 0.28f);
            int col = scale(0xFFB040, 0.8f);
            collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, col));
            poseStack.popPose();
        }
        poseStack.rotate(Axis.ZP.rotationDegrees(s.age * -20.0f));
        poseStack.scale(0.62f, 0.62f, 0.62f);
        collector.submitCustomGeometry(poseStack, FIREBALL_TYPE, (pose, buffer) -> billboard(pose, buffer, 0xFFFFFFFF));
        poseStack.popPose();
    }

    /** Phönix: in Flugrichtung, mit schlagenden Schwingen und einem großen, warmen Schein. */
    private void phoenix(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.3f, 0.0f);
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        float size = 3.4f + 0.3f * Mth.sin(s.age * 0.9f);
        poseStack.scale(size, size, size);
        int glow = scale(0xFF6A1A, 0.55f);
        collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, glow));
        poseStack.popPose();
        this.orient(s, poseStack);
        poseStack.scale(0.8f, 0.8f, 0.8f);
        collector.submitModel(this.phoenix, s, poseStack, RenderTypes.entityTranslucentEmissive(PHOENIX), FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                -1, null, s.outlineColor);
        collector.order(1).submitModel(this.phoenix, s, poseStack, RenderTypes.eyes(PHOENIX_GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1,
                null, s.outlineColor);
        poseStack.popPose();
    }

    /** Frostlanze: ein Eisspeer in Flugrichtung mit kaltem Schein. */
    private void lance(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.2f, 0.0f);
        RenderType halo = RenderTypes.energySwirl(HALO, 0.0f, 0.0f);
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.scale(1.3f, 1.3f, 1.3f);
        int glow = scale(0x9FE8FF, 0.45f);
        collector.submitCustomGeometry(poseStack, halo, (pose, buffer) -> billboard(pose, buffer, glow));
        poseStack.popPose();
        this.orient(s, poseStack);
        poseStack.rotate(Axis.ZP.rotationDegrees(s.age * 12.0f));
        poseStack.scale(1.3f, 1.3f, 1.3f);
        collector.submitModel(this.lance, s, poseStack, RenderTypes.entityTranslucent(LANCE), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1, null,
                s.outlineColor);
        collector.order(1).submitModel(this.lance, s, poseStack, RenderTypes.eyes(LANCE_GLOW), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1, null,
                s.outlineColor);
        poseStack.popPose();
    }

    /** Modell (Spitze bei -Z) in Flugrichtung drehen; Modellkoordinaten zeigen mit Y nach unten. */
    private void orient(State s, PoseStack poseStack) {
        poseStack.rotate(Axis.YP.rotationDegrees(s.yRot + 180.0f));
        poseStack.rotate(Axis.XP.rotationDegrees(s.xRot));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
    }

    private static int scale(int rgb, float f) {
        f = Mth.clamp(f, 0.0f, 1.0f);
        return ARGB.color(255, Math.round(ARGB.red(rgb) * f), Math.round(ARGB.green(rgb) * f), Math.round(ARGB.blue(rgb) * f));
    }

    private static void billboard(PoseStack.Pose pose, VertexConsumer buffer, int color) {
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
