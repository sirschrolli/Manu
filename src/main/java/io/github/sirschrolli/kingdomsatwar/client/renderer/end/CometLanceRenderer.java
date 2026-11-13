package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.end.CometLanceEntity;
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

/**
 * Asterias Komet: ihre Sternenlanze (dieselben Teile wie in ihrer Hand, ohne Wimpel), Spitze voran in Flugrichtung, um sich
 * selbst wirbelnd; die Kristallspitze leuchtet, den Schweif zeichnen die Teilchen.
 */
public class CometLanceRenderer extends EntityRenderer<CometLanceEntity, CometLanceRenderer.State> {
    private final float[] aim = new float[2];
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/asteria/asteria.png");
    private static final RenderType SOLID = RenderTypes.entityTranslucent(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/asteria/asteria_glow.png"));
    /** Die Mitte der Lanze (in Modell-Einheiten längs ihrer Achse). */
    private static final float CENTER = 13.0f;

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public float age;
    }

    private final ModelPart lance;

    public CometLanceRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(ModModelLayers.ASTERIA);
        this.lance = root.getChild("body").getChild("rider").getChild("torso").getChild("right_arm").getChild("lance");
        this.lance.getChild("pennant").visible = false;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CometLanceEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // Spitze genau in Flugrichtung (aus der Bahn, nicht aus der nachhinkenden Drehung)
        io.github.sirschrolli.kingdomsatwar.client.renderer.FlightAim.aim(entity, partialTicks, false, false, this.aim);
        state.yRot = this.aim[0];
        state.xRot = this.aim[1];
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(CometLanceEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.3f, 0.0f);
        // die Spitze zeigt in Flugrichtung (Gier wie bei Pfeilen, die Neigung nach oben positiv)
        poseStack.rotate(Axis.YP.rotationDegrees(state.yRot));
        poseStack.rotate(Axis.XP.rotationDegrees(-state.xRot));
        poseStack.rotate(Axis.XP.rotationDegrees(-90.0f));
        poseStack.scale(-AsteriaRenderer.SCALE, -AsteriaRenderer.SCALE, AsteriaRenderer.SCALE);
        this.lance.resetPose();
        this.lance.x = 0.0f;
        this.lance.y = -CENTER;
        this.lance.z = 0.0f;
        this.lance.xRot = 0.0f;
        this.lance.zRot = 0.0f;
        this.lance.yRot = state.age * 0.35f;
        collector.submitModelPart(this.lance, poseStack, SOLID, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.lance, poseStack, GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
