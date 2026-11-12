package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.ClockGearEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Ein Zahnrad des Stundenwächters: steht auf der Kante, rollt in Fahrtrichtung, die Nabe glüht. */
public class ClockGearRenderer extends EntityRenderer<ClockGearEntity, ClockGearRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/clock_gear/clock_gear.png");
    private static final RenderType BASE = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/clock_gear/clock_gear_glow.png"));
    /** Das Modell ist so groß wie das Zahnrad im Spiel (Halbmesser {@link ClockGearEntity#RADIUS}). */
    private static final float SCALE = ClockGearEntity.RADIUS * 32.0f / 20.8f;

    /** Was das Zahnrad zum Zeichnen braucht. */
    public static class State extends EntityRenderState {
        public float yaw;
        public float roll;
    }

    /** Das Modell: ein Knochen „gear“ (siehe {@code ClockGearGeometry}). */
    static final class GearModel extends EntityModel<State> {
        private final ModelPart gear;

        GearModel(ModelPart root) {
            super(root);
            this.gear = root.getChild("gear");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.gear.xRot = -state.roll * Mth.DEG_TO_RAD;
        }
    }

    private final GearModel model;

    public ClockGearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GearModel(context.bakeLayer(ModModelLayers.CLOCK_GEAR));
        this.shadowRadius = 0.5f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ClockGearEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.roll = entity.roll(partialTicks);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, ClockGearEntity.RADIUS, 0.0f);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - state.yaw));
        poseStack.scale(-SCALE, -SCALE, SCALE);
        collector.submitModel(this.model, state, poseStack, BASE, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
        collector.order(1).submitModel(this.model, state, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null,
                state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
