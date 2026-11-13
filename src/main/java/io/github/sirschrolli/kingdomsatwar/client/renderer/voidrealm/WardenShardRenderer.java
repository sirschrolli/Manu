package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.client.model.voidrealm.WardenShardModel;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.WardenShardEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Kristallsplitter: zeigt in Flugrichtung, dreht sich um die eigene Achse und leuchtet. */
public class WardenShardRenderer extends EntityRenderer<WardenShardEntity, WardenShardRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/warden_shard/warden_shard.png");
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/warden_shard/warden_shard_glow.png"));
    private final WardenShardModel model;

    public static class State extends EntityRenderState {
        float yaw;
        float pitch;
        float spin;
    }

    public WardenShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WardenShardModel(context.bakeLayer(ModModelLayers.WARDEN_SHARD));
    }

    @Override
    protected int getBlockLightLevel(WardenShardEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WardenShardEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Vec3 v = entity.getDeltaMovement();
        double length = v.length();
        if (length > 1.0e-4) {
            v = v.scale(1.0 / length);
            // Modell zeigt entlang -Z: erst um X neigen, dann um Y drehen
            state.yaw = (float) Mth.atan2(-v.x, -v.z);
            state.pitch = (float) Math.asin(Mth.clamp(v.y, -1.0, 1.0));
        }
        state.spin = (entity.tickCount + partialTicks) * 0.5f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.2f, 0.0f);
        poseStack.rotate(Axis.YP.rotation(state.yaw));
        poseStack.rotate(Axis.XP.rotation(state.pitch));
        poseStack.rotate(Axis.ZP.rotation(state.spin));
        poseStack.scale(1.3f, 1.3f, 1.3f);
        collector.submitModel(this.model, 0.0f, poseStack, this.model.renderType(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY,
                state.outlineColor);
        collector.order(1).submitModel(this.model, 0.0f, poseStack, GLOW, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
