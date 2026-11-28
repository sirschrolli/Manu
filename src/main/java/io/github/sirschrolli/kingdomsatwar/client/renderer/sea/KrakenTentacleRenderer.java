package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.sea.KrakenTentacleModel;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenTentacleEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * Ein Arm des Kraken aus dem Tentakelkäfig: mit seiner Textur und seinen leuchtenden Saugnäpfen, gut halb so groß wie seine
 * eigenen Arme (der Kraken selbst ist fünffach vergrößert).
 */
public class KrakenTentacleRenderer extends EntityRenderer<KrakenTentacleEntity, KrakenTentacleModel.State> {
    public static final float SCALE = 2.6f;
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/kraken/kraken.png");
    private static final RenderType BASE = RenderTypes.entityCutout(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/kraken/kraken_glow.png"));

    private final KrakenTentacleModel model;

    public KrakenTentacleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new KrakenTentacleModel(context.bakeLayer(ModModelLayers.KRAKEN_TENTACLE));
        this.shadowRadius = 0.0f;
    }

    @Override
    public KrakenTentacleModel.State createRenderState() {
        return new KrakenTentacleModel.State();
    }

    @Override
    public void extractRenderState(KrakenTentacleEntity entity, KrakenTentacleModel.State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        int cut = entity.cutAt();
        state.cutAge = cut < 0 ? -1.0f : Math.max(0.0f, state.age - cut);
        state.yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.seed = entity.getId();
    }

    @Override
    protected AABB getBoundingBoxForCulling(KrakenTentacleEntity entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(6.0, 4.0, 6.0);
    }

    @Override
    public void submit(KrakenTentacleModel.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f - state.yaw));
        poseStack.scale(-SCALE, -SCALE, SCALE);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        collector.submitModel(this.model, state, poseStack, BASE, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
        float glow = state.cutAge >= 0.0f ? Math.max(0.0f, 0.8f - state.cutAge * 0.08f) : 0.8f + Mth.sin(state.age * 0.2f) * 0.15f;
        if (glow > 0.01f) {
            collector.order(1).submitModel(this.model, state, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY,
                    ARGB.color(Math.round(glow * 255.0f), 0xFFFFFF), null, state.outlineColor);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
