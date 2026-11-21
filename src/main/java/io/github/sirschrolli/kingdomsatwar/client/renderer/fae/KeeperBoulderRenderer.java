package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.KeeperBoulderEntity;
import net.minecraft.client.model.Model;
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

/**
 * Ein Felsbrocken des Steinhüters im Flug: der Klotz aus Feenstein überschlägt sich (jeder Wurf anders), seine Kristalle
 * leuchten; 1,4-fach wie der Hüter.
 */
public class KeeperBoulderRenderer extends EntityRenderer<KeeperBoulderEntity, KeeperBoulderRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/stone_keeper/boulder/keeper_boulder.png");
    private static final RenderType BASE = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/stone_keeper/boulder/keeper_boulder_glow.png"));

    public static class State extends EntityRenderState {
        public float age;
        public int seed;
    }

    private static final class BoulderModel extends Model<State> {
        BoulderModel(ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }
    }

    private final BoulderModel model;

    public KeeperBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BoulderModel(context.bakeLayer(ModModelLayers.KEEPER_BOULDER));
        this.shadowRadius = 0.8f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(KeeperBoulderEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.seed = entity.seed();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.6f, 0.0f);
        float spinX = 9.0f + (state.seed & 7);
        float spinZ = 5.0f + ((state.seed >> 3) & 7);
        poseStack.rotate(Axis.YP.rotationDegrees((state.seed & 0xFF) * 1.4f));
        poseStack.rotate(Axis.XP.rotationDegrees(state.age * spinX));
        poseStack.rotate(Axis.ZP.rotationDegrees(state.age * spinZ));
        poseStack.scale(-1.4f, -1.4f, 1.4f);
        collector.submitModel(this.model, state, poseStack, BASE, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
        collector.order(1).submitModel(this.model, state, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null,
                state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
