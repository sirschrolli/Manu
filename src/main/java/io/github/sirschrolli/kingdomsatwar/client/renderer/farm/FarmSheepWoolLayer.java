package io.github.sirschrolli.kingdomsatwar.client.renderer.farm;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.model.farm.FarmSheepModel;
import net.minecraft.client.color.ColorLerper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Die Wolle des Schafs: dasselbe Modell (nur die Wollteile), eingefärbt in der Wollfarbe – "jeb_" schillert bunt. */
public class FarmSheepWoolLayer extends RenderLayer<FarmRenderState, EntityModel<FarmRenderState>> {
    private final FarmAnimalRenderer<?> renderer;
    private final FarmSheepModel wool;

    public FarmSheepWoolLayer(FarmAnimalRenderer<?> renderer, FarmSheepModel wool) {
        super(renderer);
        this.renderer = renderer;
        this.wool = wool;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, FarmRenderState state, float yRot, float xRot) {
        if (state.sheared) {
            return;
        }
        var texture = this.renderer.texture(state.breed);
        if (state.isInvisible) {
            if (state.appearsGlowing()) {
                collector.submitModel(this.wool, state, poseStack, RenderTypes.outline(texture), light,
                        LivingEntityRenderer.getOverlayCoords(state, 0.0f), -16777216, null, state.outlineColor);
            }
            return;
        }
        int color = state.jeb ? ColorLerper.getLerpedColor(ColorLerper.Type.SHEEP, state.ageInTicks)
                : ColorLerper.Type.SHEEP.getColor(state.woolColor);
        coloredCutoutModelCopyLayerRender(this.wool, texture, poseStack, collector, light, state, color, 1);
    }
}
