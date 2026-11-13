package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.Nullable;

/**
 * Leuchtende Stellen (Augen, Pilze, Tränke) eines Monsters: zeichnet das Modell noch einmal mit der Leucht-Textur der
 * Art – voll hell, auch in finsterer Nacht. Arten ohne Leucht-Textur liefern {@code null}.
 */
public class GlowLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private final Function<S, @Nullable RenderType> glow;

    public GlowLayer(RenderLayerParent<S, M> parent, Function<S, @Nullable RenderType> glow) {
        super(parent);
        this.glow = glow;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        RenderType type = this.glow.apply(state);
        if (type != null) {
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, type, lightCoords, OverlayTexture.NO_OVERLAY,
                    state.outlineColor);
        }
    }
}
