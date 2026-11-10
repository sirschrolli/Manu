package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity.Kind;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Die Geschosse Schrollis: der Lichtspeer (Spitze voran, um sich selbst wirbelnd), die goldene Feder, der kreisende
 * Heiligenschein, die Weltkugel und die Welten (sich drehend, manche mit Ring), die Sonne und die Sterne – alles leuchtend,
 * dazu ein Lichthof, der immer der Kamera zugewandt ist.
 */
public class DivineShotRenderer extends EntityRenderer<DivineShotEntity, DivineShotRenderer.State> {
    private static final RenderType SPEAR = RenderTypes.entityTranslucent(shot("divine_spear"));
    private static final RenderType SPEAR_GLOW = RenderTypes.eyes(shot("divine_spear_glow"));
    private static final RenderType FEATHER = RenderTypes.entityTranslucent(shot("divine_feather"));
    private static final RenderType FEATHER_GLOW = RenderTypes.eyes(shot("divine_feather_glow"));
    private static final RenderType HALO = RenderTypes.entityTranslucent(shot("divine_halo"));
    private static final RenderType HALO_GLOW = RenderTypes.eyes(shot("divine_halo_glow"));
    private static final RenderType GLOW_DISC = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/schrolli/glow_disc.png"));
    private static final RenderType STAR4 = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/schrolli/star4.png"));
    private static final RenderType[] PLANETS = new RenderType[DivineShotEntity.PLANET_NAMES.length];
    private static final RenderType[] PLANETS_GLOW = new RenderType[DivineShotEntity.PLANET_NAMES.length];
    private static final int[] PLANET_LIGHT = {0x8AC8FF, 0xFF7A30, 0xC8F0FF, 0xC080FF, 0xFFE070, 0xFFD24A};

    static {
        for (int i = 0; i < PLANETS.length; i++) {
            Identifier id = KingdomsAtWar.id("textures/entity/schrolli/planet/" + DivineShotEntity.PLANET_NAMES[i] + ".png");
            PLANETS[i] = RenderTypes.entityTranslucent(id);
            PLANETS_GLOW[i] = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/schrolli/planet/" + DivineShotEntity.PLANET_NAMES[i] + "_glow.png"));
        }
    }

    private static Identifier shot(String name) {
        return KingdomsAtWar.id("textures/entity/schrolli/shots/" + name + ".png");
    }

    public static class State extends EntityRenderState {
        public Kind kind = Kind.SPEAR;
        public int shotState;
        public int variant;
        public float scale = 1.0f;
        public float yRot;
        public float xRot;
        public float age;
    }

    private final ModelPart spear;
    private final ModelPart feather;
    private final ModelPart halo;
    private final ModelPart planet;
    private final ModelPart ring;

    public DivineShotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.spear = context.bakeLayer(CreatorRenderers.DIVINE_SPEAR).getChild("spear");
        this.feather = context.bakeLayer(CreatorRenderers.DIVINE_FEATHER).getChild("feather");
        this.halo = context.bakeLayer(CreatorRenderers.DIVINE_HALO).getChild("disc");
        this.planet = context.bakeLayer(CreatorRenderers.DIVINE_PLANET).getChild("planet");
        this.ring = this.planet.getChild("ring");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DivineShotEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.kind = entity.kind();
        state.shotState = entity.state();
        state.variant = Mth.clamp(entity.variant(), 0, PLANETS.length - 1);
        state.scale = entity.visualScale();
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(DivineShotEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        switch (state.kind) {
            case SPEAR, BOLT -> this.spear(state, poseStack, collector, state.kind == Kind.SPEAR ? 1.4f : 0.5f);
            case FEATHER -> this.feather(state, poseStack, collector);
            case HALO -> this.halo(state, poseStack, collector, camera);
            case ORB -> this.planet(state, poseStack, collector, camera, DivineShotEntity.PLANET_GLOBE, 0.42f, true,
                    state.shotState == DivineShotEntity.VORTEX ? 3.6f : 1.6f);
            case PLANET -> this.planet(state, poseStack, collector, camera, state.variant, 1.5f * state.scale,
                    state.variant == DivineShotEntity.PLANET_ICE || state.variant == DivineShotEntity.PLANET_VOID, 4.5f * state.scale);
            case SUN -> this.planet(state, poseStack, collector, camera, DivineShotEntity.PLANET_SUN, 0.5f * state.scale, false, 2.6f * state.scale);
            case STAR -> this.star(state, poseStack, collector, camera, 1.5f);
            case FIXED_STAR -> this.star(state, poseStack, collector, camera, 1.0f + Mth.sin(state.age * 0.5f) * 0.15f);
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** Der Lichtspeer: Spitze voran, um sich selbst wirbelnd. */
    private void spear(State state, PoseStack poseStack, SubmitNodeCollector collector, float scale) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.25f, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(state.yRot + 180.0f));
        poseStack.rotate(Axis.XP.rotationDegrees(state.xRot));
        poseStack.rotate(Axis.ZP.rotationDegrees(state.age * 18.0f));
        poseStack.scale(-scale, -scale, scale);
        this.spear.resetPose();
        collector.submitModelPart(this.spear, poseStack, SPEAR, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.spear, poseStack, SPEAR_GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    /** Die goldene Feder: Spitze voran, sie dreht sich langsam um ihren Kiel. */
    private void feather(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.2f, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(state.yRot + 180.0f));
        poseStack.rotate(Axis.XP.rotationDegrees(state.xRot));
        poseStack.rotate(Axis.ZP.rotationDegrees(state.age * 12.0f));
        poseStack.scale(-1.1f, -1.1f, 1.1f);
        this.feather.resetPose();
        collector.submitModelPart(this.feather, poseStack, FEATHER, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.feather, poseStack, FEATHER_GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    /** Der Heiligenschein: ein waagrechtes Sonnenrad, das sich rasend dreht, in einem goldenen Lichthof. */
    private void halo(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.3f, 0.0f);
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(state.age * 35.0f));
        poseStack.rotate(Axis.XP.rotationDegrees(Mth.sin(state.age * 0.2f) * 8.0f));
        poseStack.scale(-1.5f, -1.5f, 1.5f);
        this.halo.resetPose();
        collector.submitModelPart(this.halo, poseStack, HALO, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.halo, poseStack, HALO_GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        billboard(poseStack, collector, camera, GLOW_DISC, 3.4f, ARGB.color(110, 0xFFD24A), 0.0f);
        poseStack.popPose();
    }

    /** Eine Welt (oder die Weltkugel, oder die Sonne): eine sich drehende Kugel, manche mit Ring, in ihrem Licht. */
    private void planet(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, int variant, float scale,
            boolean ringed, float glow) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.3f, 0.0f);
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(state.age * 2.5f));
        poseStack.rotate(Axis.ZP.rotationDegrees(14.0f));
        poseStack.scale(-scale, -scale, scale);
        this.planet.resetPose();
        this.ring.resetPose();
        this.ring.visible = ringed;
        this.ring.yRot += state.age * 0.02f;
        collector.submitModelPart(this.planet, poseStack, PLANETS[variant], 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.planet, poseStack, PLANETS_GLOW[variant], 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        int alpha = variant == DivineShotEntity.PLANET_SUN ? 200 : 120;
        billboard(poseStack, collector, camera, GLOW_DISC, glow, ARGB.color(alpha, PLANET_LIGHT[variant]), 0.0f);
        if (variant == DivineShotEntity.PLANET_SUN) {
            billboard(poseStack, collector, camera, STAR4, glow * 1.3f, ARGB.color(180, 0xFFF2C4), state.age * 1.5f);
        }
        poseStack.popPose();
    }

    /** Ein Stern: ein vierzackiger Stern in einem goldenen Lichthof. */
    private void star(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float size) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.25f, 0.0f);
        billboard(poseStack, collector, camera, GLOW_DISC, size * 2.4f, ARGB.color(150, 0xFFD24A), 0.0f);
        billboard(poseStack, collector, camera, STAR4, size * (1.0f + Mth.sin(state.age * 0.9f) * 0.12f), -1, state.age * 6.0f);
        poseStack.popPose();
    }

    private static void billboard(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, RenderType type, float size, int color,
            float spin) {
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        if (spin != 0.0f) {
            poseStack.rotate(Axis.ZP.rotationDegrees(spin));
        }
        poseStack.scale(size, size, size);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> quad(pose, buffer, color));
        poseStack.popPose();
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, -0.5f, 0, 1, color);
        vertex(buffer, pose, 0.5f, -0.5f, 1, 1, color);
        vertex(buffer, pose, 0.5f, 0.5f, 1, 0, color);
        vertex(buffer, pose, -0.5f, 0.5f, 0, 0, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, int u, int v, int color) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
