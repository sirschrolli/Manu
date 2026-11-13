package io.github.sirschrolli.kingdomsatwar.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.spell.SpellProjectileModels;
import io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Geschosse der Bosse: Feuerball, Meteor, Blitzkugel, Blase … als leuchtende, zur Kamera gedrehte Bilder; Glaciaras
 * Eisspeer und Eiszapfen als Modelle aus Eis, die Spitze genau in Flugrichtung (seit 0.87 – vorher drehten sie sich als
 * flache Bilder und flogen scheinbar quer).
 */
public class BossProjectileRenderer extends EntityRenderer<BossProjectile, BossProjectileRenderer.State> {
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Map<BossProjectile.Kind, RenderType> TYPES = new EnumMap<>(BossProjectile.Kind.class);
    private static final Identifier HALO = KingdomsAtWar.id("textures/entity/mage/missile.png");
    private static final Identifier LANCE = KingdomsAtWar.id("textures/entity/spell/ice_lance/ice_lance.png");
    private static final Identifier LANCE_GLOW = KingdomsAtWar.id("textures/entity/spell/ice_lance/ice_lance_glow.png");
    private static final Identifier ICICLE = KingdomsAtWar.id("textures/entity/boss/icicle/icicle.png");
    private static final Identifier ICICLE_GLOW = KingdomsAtWar.id("textures/entity/boss/icicle/icicle_glow.png");

    static {
        for (BossProjectile.Kind kind : BossProjectile.Kind.values()) {
            Identifier texture = KingdomsAtWar.id("textures/entity/boss/projectile_" + kind.name().toLowerCase() + ".png");
            TYPES.put(kind, kind == BossProjectile.Kind.BUBBLE || kind == BossProjectile.Kind.ICE_SPEAR
                    ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture));
        }
    }

    public static class State extends EntityRenderState {
        BossProjectile.Kind kind = BossProjectile.Kind.FIREBALL;
        float spin;
        float age;
        float yRot;
        float xRot;
    }

    private final SpellProjectileModels.Lance lance;
    private final SpellProjectileModels.Lance icicle;
    private final float[] aim = new float[2];

    public BossProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.lance = new SpellProjectileModels.Lance(context.bakeLayer(ModModelLayers.ICE_LANCE));
        this.icicle = new SpellProjectileModels.Lance(context.bakeLayer(ModModelLayers.ICICLE));
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
        state.age = entity.tickCount + partialTicks;
        state.spin = state.age * (entity.getKind() == BossProjectile.Kind.ROLLING ? -22.0f : 12.0f);
        if (oriented(state.kind)) {
            FlightAim.aim(entity, partialTicks, false, false, this.aim);
            state.yRot = this.aim[0];
            state.xRot = this.aim[1];
        }
    }

    /** Speere und Zapfen fliegen mit der Spitze voran, alles andere ist rund und schaut zur Kamera. */
    private static boolean oriented(BossProjectile.Kind kind) {
        return kind == BossProjectile.Kind.ICE_SPEAR || kind == BossProjectile.Kind.ICICLE;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.kind == BossProjectile.Kind.ICE_SPEAR) {
            this.ice(state, poseStack, collector, camera, this.lance, LANCE, LANCE_GLOW, 1.75f, 0.5f, true);
        } else if (state.kind == BossProjectile.Kind.ICICLE) {
            this.ice(state, poseStack, collector, camera, this.icicle, ICICLE, ICICLE_GLOW, 1.85f, 0.35f, false);
        } else {
            poseStack.pushPose();
            float size = state.kind.size * 1.6f;
            // ein rollender Brocken liegt auf dem Boden auf
            poseStack.translate(0.0f, (state.kind == BossProjectile.Kind.ROLLING ? 0.5f : 0.25f) * size, 0.0f);
            poseStack.scale(size, size, size);
            poseStack.rotate(camera.orientation);
            poseStack.rotateDegrees(Axis.ZP, state.spin);
            RenderType type = TYPES.get(state.kind);
            collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> quad(pose, buffer, state.lightCoords, -1));
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** Ein Eisgeschoss als Modell (Spitze bei -Z) in Flugrichtung, mit kaltem Schein; der Speer dreht sich langsam um sich. */
    private void ice(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, SpellProjectileModels.Lance model,
            Identifier texture, Identifier glow, float scale, float halo, boolean spin) {
        poseStack.pushPose();
        poseStack.translate(0.0f, s.boundingBoxHeight * 0.5f, 0.0f);
        // kalter Schein, immer zur Kamera
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.scale(scale * 1.1f, scale * 1.1f, scale * 1.1f);
        int light = ARGB.color(255, Math.round(0x9F * halo), Math.round(0xE8 * halo), Math.round(0xFF * halo));
        collector.submitCustomGeometry(poseStack, RenderTypes.energySwirl(HALO, 0.0f, 0.0f), (pose, buffer) -> quad(pose, buffer, FULL_BRIGHT, light));
        poseStack.popPose();
        // Spitze in Flugrichtung (Gier wie bei Pfeilen; Modellkoordinaten zeigen mit Y nach unten)
        poseStack.rotate(Axis.YP.rotationDegrees(s.yRot + 180.0f));
        poseStack.rotate(Axis.XP.rotationDegrees(s.xRot));
        if (spin) {
            poseStack.rotate(Axis.ZP.rotationDegrees(s.age * 9.0f));
        }
        poseStack.scale(-scale, -scale, scale);
        collector.submitModel(model, s, poseStack, RenderTypes.entityTranslucent(texture), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1, null,
                s.outlineColor);
        collector.order(1).submitModel(model, s, poseStack, RenderTypes.eyes(glow), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1, null,
                s.outlineColor);
        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int light, int color) {
        vertex(buffer, pose, light, color, -0.5f, -0.5f, 0, 1);
        vertex(buffer, pose, light, color, 0.5f, -0.5f, 1, 1);
        vertex(buffer, pose, light, color, 0.5f, 0.5f, 1, 0);
        vertex(buffer, pose, light, color, -0.5f, 0.5f, 0, 0);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, int color, float x, float y, int u, int v) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
