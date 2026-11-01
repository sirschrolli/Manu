package io.github.sirschrolli.kingdomsatwar.client.renderer.water;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.renderer.wild.WildRenderer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaDolphin;
import net.minecraft.client.model.animal.dolphin.DolphinModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.DolphinRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Großer Tümmler und Schwertwal: Finne je Art, Schwimmen und das Tragen von Gegenständen im Schnabel wie bei Vanilla. */
public class SeaDolphinRenderer extends WildRenderer<SeaDolphin, SeaDolphinRenderer.State, DolphinModel> {
    private static final Identifier[] TEXTURES = {WaterRenderers.texture("dolphin", "tuemmler"),
            WaterRenderers.texture("dolphin", "schwertwal")};

    public static class State extends DolphinRenderState {
        public int look;
    }

    public SeaDolphinRenderer(EntityRendererProvider.Context context) {
        super(context, new DolphinModel(context.bakeLayer(WaterRenderers.DOLPHIN)), 0.7f, new DolphinRenderer(context));
        this.addLayer(new CarriedItemLayer(this));
    }

    @Override
    protected void extract(SeaDolphin entity, State state, float partialTicks) {
        state.look = entity.getLook().ordinal();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return pick(TEXTURES, state.look);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    /** Der Gegenstand im Schnabel – an derselben Stelle wie beim Vanilla-Delfin (der Schnabel sitzt dort, wo Vanilla ihn hat). */
    private static final class CarriedItemLayer extends RenderLayer<State, DolphinModel> {
        CarriedItemLayer(RenderLayerParent<State, DolphinModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, State state, float yRot, float xRot) {
            ItemStackRenderState item = state.heldItem;
            if (item.isEmpty()) {
                return;
            }
            poseStack.pushPose();
            float tilt = Mth.abs(state.xRot) / 60.0f;
            if (state.xRot < 0.0f) {
                poseStack.translate(0.0f, 1.0f - tilt * 0.5f, -1.0f + tilt * 0.5f);
            } else {
                poseStack.translate(0.0f, 1.0f + tilt * 0.8f, -1.0f + tilt * 0.2f);
            }
            item.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
