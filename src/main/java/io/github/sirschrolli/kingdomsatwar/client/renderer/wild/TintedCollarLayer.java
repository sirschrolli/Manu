package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.jspecify.annotations.Nullable;

/**
 * Halsband gezähmter Wölfe und Katzen: Ein zweites Exemplar des Modells zeichnet nur den Halsband-Knochen – mit dem
 * Fellbild des Tiers (dort ist das Halsband hell gemalt) und eingefärbt in der Farbe des Halsbands.
 */
public class TintedCollarLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private final EntityModel<? super S> collarModel;
    private final Function<S, @Nullable DyeColor> color;
    private final Function<S, Identifier> texture;

    public TintedCollarLayer(RenderLayerParent<S, M> renderer, EntityModel<? super S> collarModel, Function<S, @Nullable DyeColor> color,
            Function<S, Identifier> texture) {
        super(renderer);
        this.collarModel = collarModel;
        this.color = color;
        this.texture = texture;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        DyeColor dye = this.color.apply(state);
        if (dye == null || state.isInvisible) {
            return;
        }
        collector.order(1).submitModel(this.collarModel, state, poseStack, RenderTypes.entityCutout(this.texture.apply(state)), light,
                OverlayTexture.NO_OVERLAY, dye.getTextureDiffuseColor(), null, state.outlineColor);
    }
}
