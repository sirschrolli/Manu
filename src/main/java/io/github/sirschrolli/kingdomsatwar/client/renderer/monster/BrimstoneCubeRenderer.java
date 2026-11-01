package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BrimstoneCube;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.monster.slime.SulfurCubeModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SulfurCubeRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SulfurCubeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Schwefelwürfel: durchscheinende Hülle aus eigenem Bild, darin der Kern – oder der geschluckte Block bzw. Kopf, genau
 * wie bei Vanilla (den Zustand liest der Vanilla-Renderer aus, inklusive Zündschnur und Block-Modell).
 */
public class BrimstoneCubeRenderer extends AbstractCubeMobRenderer<BrimstoneCube, SulfurCubeRenderState, SulfurCubeModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/sulfur_cube/schwefelwuerfel.png");
    private final SulfurCubeRenderer vanilla;
    private final SulfurCubeModel normalModel;
    private final SulfurCubeModel smallModel;

    public BrimstoneCubeRenderer(EntityRendererProvider.Context context) {
        SulfurCubeModel normal = new SulfurCubeModel(context.bakeLayer(MonsterRenderers.SULFUR_CUBE));
        super(context, normal);
        this.normalModel = normal;
        this.smallModel = new SulfurCubeModel(context.bakeLayer(MonsterRenderers.SULFUR_CUBE_SMALL));
        this.vanilla = new SulfurCubeRenderer(context);
        this.addLayer(new Core(this, context.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache(),
                SulfurCubeRenderer.CUSTOM_HEAD_TRANSFORMS));
    }

    @Override
    public void submit(SulfurCubeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = state.isBaby ? this.smallModel : this.normalModel;
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected void scale(SulfurCubeRenderState state, PoseStack poseStack) {
        this.downscaleSlightly(poseStack);
        super.scale(state, poseStack);
        float fuse = state.fuseRemainingTicks;
        if (fuse < 10.0f && fuse > 0.0f) {
            float s = 1.0f + TntRenderer.getSwellAmount(fuse);
            poseStack.scale(s, s, s);
        }
        float offset = state.isBaby ? 1.24f : 0.98f;
        float downscale = state.isBaby ? 1.0f : 0.5f;
        float pixel = (state.isInvisible ? 0.0f : 1.0f) / 16.0f;
        poseStack.scale(downscale, downscale, downscale);
        poseStack.translate(0.0f, offset - pixel, 0.0f);
    }

    @Override
    protected void applySizeAndSquish(SulfurCubeRenderState state, PoseStack poseStack) {
        float size = state.size;
        float squish = state.containedBlock.isEmpty() ? state.squish / (size * 0.5f + 1.0f) : 0.0f;
        float w = 1.0f / (squish + 1.0f);
        poseStack.scale(w * size, 1.0f / w * size, w * size);
    }

    @Override
    public SulfurCubeRenderState createRenderState() {
        return new SulfurCubeRenderState();
    }

    @Override
    public void extractRenderState(BrimstoneCube entity, SulfurCubeRenderState state, float partialTicks) {
        this.vanilla.extractRenderState(entity, state, partialTicks);
    }

    @Override
    public Identifier getTextureLocation(SulfurCubeRenderState state) {
        return TEXTURE;
    }

    /** Der Kern – oder, wenn er einen Block geschluckt hat, dieser Block (wie Vanillas SulfurCubeInnerLayer). */
    private static final class Core extends RenderLayer<SulfurCubeRenderState, SulfurCubeModel> {
        private final SulfurCubeModel normal;
        private final SulfurCubeModel small;

        Core(RenderLayerParent<SulfurCubeRenderState, SulfurCubeModel> parent, EntityModelSet models) {
            super(parent);
            this.normal = new SulfurCubeModel(models.bakeLayer(MonsterRenderers.SULFUR_CUBE_INNER));
            this.small = new SulfurCubeModel(models.bakeLayer(MonsterRenderers.SULFUR_CUBE_SMALL_INNER));
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, SulfurCubeRenderState state, float yRot,
                float xRot) {
            if (state.wornHeadType != null) {
                return;
            }
            int overlay = state.fuseRemainingTicks > 0.0f && TntRenderer.isLit(state.fuseRemainingTicks)
                    ? OverlayTexture.pack(OverlayTexture.u(1.0f), 10) : LivingEntityRenderer.getOverlayCoords(state, 0.0f);
            if (!state.containedBlock.isEmpty()) {
                poseStack.pushPose();
                poseStack.rotateDegrees(Axis.XP, 180.0f);
                if (state.isBaby) {
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                }
                poseStack.translate(-0.5f, -0.518f, -0.5f);
                state.containedBlock.submit(poseStack, collector, state.lightCoords, overlay, state.outlineColor);
                poseStack.popPose();
            } else if (!state.isInvisible) {
                collector.order(-1).submitModel(state.isBaby ? this.small : this.normal, state, poseStack, RenderTypes.entityTranslucent(TEXTURE),
                        lightCoords, overlay, -1, null, state.outlineColor);
            }
        }
    }
}
