package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.entity.fae.LightDoveEntity;
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
import net.minecraft.util.Mth;

/**
 * Eine Lichttaube: ein kleiner weißer Vogel aus Licht, Kopf voran in Flugrichtung; die Flügel schlagen schnell, der
 * Schwanz wippt. Leuchtet immer.
 */
public class LightDoveRenderer extends EntityRenderer<LightDoveEntity, LightDoveRenderer.State> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/miss_mystik/light_dove.png");
    private static final RenderType BODY = RenderTypes.entityTranslucent(TEXTURE);
    private static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/miss_mystik/light_dove_glow.png"));

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public float age;
    }

    private final ModelPart dove;
    private final ModelPart wingL;
    private final ModelPart wingR;
    private final ModelPart tail;

    public LightDoveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
        this.dove = context.bakeLayer(ModModelLayers.LIGHT_DOVE).getChild("dove");
        this.wingL = this.dove.getChild("left_wing");
        this.wingR = this.dove.getChild("right_wing");
        this.tail = this.dove.getChild("tail");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LightDoveEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.age = entity.tickCount + partialTicks;
    }

    @Override
    protected int getBlockLightLevel(LightDoveEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.15f, 0.0f);
        // Kopf voran in Flugrichtung (Gier wie bei Geschossen, die Neigung nach oben positiv)
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f + state.yRot));
        poseStack.rotate(Axis.XP.rotationDegrees(state.xRot));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.dove.resetPose();
        this.wingL.resetPose();
        this.wingR.resetPose();
        this.tail.resetPose();
        float beat = Mth.sin(state.age * 1.3f);
        this.wingL.zRot = beat * 0.9f;
        this.wingR.zRot = -beat * 0.9f;
        this.tail.xRot = Mth.sin(state.age * 0.6f) * 0.15f;
        this.dove.y = -beat * 0.6f;
        collector.submitModelPart(this.dove, poseStack, BODY, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        collector.order(1).submitModelPart(this.dove, poseStack, GLOW, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
