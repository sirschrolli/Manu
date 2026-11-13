package io.github.sirschrolli.kingdomsatwar.client.renderer.arena;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim;
import io.github.sirschrolli.kingdomsatwar.entity.arena.StormTridentEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Der geworfene Sturmdreizack: derselbe Dreizack wie in Ventors Hand (dieselben Teile, dasselbe Bild), mit den Zinken voran
 * genau auf seiner Bahn ({@link FlightAim}); auf dem Heimweg fliegt er mit dem Griff voran. Die Spitzen und die Windrune
 * leuchten.
 */
public class StormTridentRenderer extends EntityRenderer<StormTridentEntity, StormTridentRenderer.State> {
    private static final RenderType SOLID = RenderTypes.entityCutout(ArenaRenderers.texture("ventor", "ventor"));
    private static final RenderType GLOW = RenderTypes.eyes(ArenaRenderers.texture("ventor", "ventor_glow"));
    /** Die Mitte des Dreizacks (in Modell-Einheiten längs seiner Achse): so liegt er mittig auf seiner Bahn. */
    private static final float CENTER = 10.6f;
    private final float[] aim = new float[2];
    private final ModelPart trident;

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
    }

    public StormTridentRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(ArenaRenderers.VENTOR);
        this.trident = root.getChild("body").getChild("arm_r").getChild("forearm_r").getChild("hand_r").getChild("trident");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StormTridentEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        FlightAim.aim(entity, partialTicks, entity.isReturning(), false, this.aim);
        state.yRot = this.aim[0];
        state.xRot = this.aim[1];
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        // die Spitze zeigt in Flugrichtung (Gier wie bei Pfeilen, die Neigung nach oben positiv)
        poseStack.rotate(Axis.YP.rotationDegrees(state.yRot));
        poseStack.rotate(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.rotate(Axis.XP.rotationDegrees(-90.0f));
        poseStack.scale(-VentorRenderer.SCALE, -VentorRenderer.SCALE, VentorRenderer.SCALE);
        this.trident.resetPose();
        this.trident.x = 0.0f;
        this.trident.y = -CENTER;
        this.trident.z = 0.0f;
        this.trident.xRot = 0.0f;
        this.trident.yRot = 0.0f;
        this.trident.zRot = 0.0f;
        collector.submitModelPart(this.trident, poseStack, SOLID, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.trident, poseStack, GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
