package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicSwordEntity;
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
import net.minecraft.world.phys.Vec3;

/**
 * Ein Schwert der Schwerterkiste: schwebt mit der Spitze zur Mitte der Kiste, zittert leicht und dreht sich langsam um sich
 * selbst; im Stoß schnellt es hinein, danach verblasst es. Die Klinge aus Licht leuchtet.
 */
public class MagicSwordRenderer extends EntityRenderer<MagicSwordEntity, MagicSwordRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/miss_mystik/magic_sword.png");
    private static final RenderType BODY = RenderTypes.entityTranslucent(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/miss_mystik/magic_sword_glow.png"));

    public static class State extends EntityRenderState {
        public Vec3 to = Vec3.ZERO;
        public float thrust;
        public float alpha;
        public float age;
    }

    private final ModelPart sword;

    public MagicSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.sword = context.bakeLayer(ModModelLayers.MAGIC_SWORD).getChild("sword");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagicSwordEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.to = entity.center().subtract(entity.getPosition(partialTicks));
        state.thrust = entity.thrust(partialTicks);
        state.alpha = entity.alpha(partialTicks);
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(MagicSwordEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.alpha <= 0.01f) {
            return;
        }
        Vec3 to = state.to;
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        float yaw = (float) (Mth.atan2(to.x, to.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(to.y, flat) * Mth.RAD_TO_DEG);
        float k = state.thrust * state.thrust;
        poseStack.pushPose();
        // im Stoß hinein (die Spitze bis über die Mitte hinaus), vorher ein leichtes Zittern
        float shake = state.thrust > 0.0f ? 0.0f : Mth.sin(state.age * 2.1f) * 0.03f;
        poseStack.translate(to.x * k * 0.62 + shake, to.y * k * 0.62, to.z * k * 0.62);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f + yaw));
        poseStack.rotate(Axis.XP.rotationDegrees(pitch));
        poseStack.rotate(Axis.ZP.rotationDegrees(state.age * 6.0f * (1.0f - state.thrust)));
        poseStack.scale(-1.2f, -1.2f, 1.2f);
        this.sword.resetPose();
        int color = ARGB.color(Math.round(state.alpha * 230.0f), 0xFFFFFF);
        collector.submitModelPart(this.sword, poseStack, BODY, 0xF000F0, OverlayTexture.NO_OVERLAY, null, color);
        collector.order(1).submitModelPart(this.sword, poseStack, GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null,
                ARGB.color(Math.round(state.alpha * 255.0f), 0xFFFFFF));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
