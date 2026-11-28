package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.ReefFish;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.fish.TropicalFishLargeModel;
import net.minecraft.client.model.animal.fish.TropicalFishSmallModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.TropicalFish;

/**
 * Rifffisch: zwei Körperformen wie bei Vanilla (schlank und hochrückig), die Grundfarbe getönt, darüber eins von zwölf
 * eigenen Mustern in der Musterfarbe – so bleiben alle Vanilla-Spielarten (Muster und Farben) erhalten.
 */
public class ReefFishRenderer extends WildRenderer<ReefFish, TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
    private static final Identifier SMALL_TEXTURE = WaterRenderers.texture("tropical_fish", "rifffisch_a");
    private static final Identifier LARGE_TEXTURE = WaterRenderers.texture("tropical_fish", "rifffisch_b");
    private final EntityModel<TropicalFishRenderState> smallModel;
    private final EntityModel<TropicalFishRenderState> largeModel;

    public ReefFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalFishSmallModel(context.bakeLayer(WaterRenderers.REEF_SMALL)), 0.15f, new TropicalFishRenderer(context));
        this.smallModel = this.getModel();
        this.largeModel = new TropicalFishLargeModel(context.bakeLayer(WaterRenderers.REEF_LARGE));
        this.addLayer(new PatternLayer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTextureLocation(TropicalFishRenderState state) {
        return switch (state.pattern.base()) {
            case SMALL -> SMALL_TEXTURE;
            case LARGE -> LARGE_TEXTURE;
        };
    }

    @Override
    public TropicalFishRenderState createRenderState() {
        return new TropicalFishRenderState();
    }

    @Override
    public void submit(TropicalFishRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = switch (state.pattern.base()) {
            case SMALL -> this.smallModel;
            case LARGE -> this.largeModel;
        };
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    protected int getModelTint(TropicalFishRenderState state) {
        return state.baseColor;
    }

    @Override
    protected void setupRotations(TropicalFishRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        poseStack.rotateDegrees(Axis.YP, 4.3f * Mth.sin(0.6f * state.ageInTicks));
        if (!state.isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.rotateDegrees(Axis.ZP, 90.0f);
        }
    }

    /** Das Muster in der Musterfarbe – eigene Muster-Texturen statt der Vanilla-Muster, gleiche Zuordnung. */
    private static final class PatternLayer extends RenderLayer<TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
        private final TropicalFishSmallModel small;
        private final TropicalFishLargeModel large;

        PatternLayer(RenderLayerParent<TropicalFishRenderState, EntityModel<TropicalFishRenderState>> parent, EntityModelSet models) {
            super(parent);
            this.small = new TropicalFishSmallModel(models.bakeLayer(WaterRenderers.REEF_SMALL_PATTERN));
            this.large = new TropicalFishLargeModel(models.bakeLayer(WaterRenderers.REEF_LARGE_PATTERN));
        }

        private static Identifier texture(TropicalFish.Pattern pattern) {
            int number = switch (pattern) {
                case KOB, FLOPPER -> 1;
                case SUNSTREAK, STRIPEY -> 2;
                case SNOOPER, GLITTER -> 3;
                case DASHER, BLOCKFISH -> 4;
                case BRINELY, BETTY -> 5;
                case SPOTTY, CLAYFISH -> 6;
            };
            String shape = pattern.base() == TropicalFish.Base.SMALL ? "a" : "b";
            return WaterRenderers.texture("tropical_fish", "rifffisch_" + shape + "_muster_" + number);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, TropicalFishRenderState state, float yRot,
                float xRot) {
            EntityModel<TropicalFishRenderState> model = state.pattern.base() == TropicalFish.Base.SMALL ? this.small : this.large;
            coloredCutoutModelCopyLayerRender(model, texture(state.pattern), poseStack, collector, lightCoords, state, state.patternColor, 1);
        }
    }
}
