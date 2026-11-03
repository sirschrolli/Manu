package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.animal.fox.FoxModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Was der Fuchs im Maul trägt – wie bei Vanilla, aber immer mit den Maßen des erwachsenen Modells (Jungtiere sind hier
 * das verkleinerte erwachsene Modell, die Verkleinerung gilt für das Maul mit).
 */
public class WildFoxHeldItemLayer extends RenderLayer<WildFoxRenderState, FoxModel> {
    public WildFoxHeldItemLayer(RenderLayerParent<WildFoxRenderState, FoxModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, WildFoxRenderState state, float yRot, float xRot) {
        ItemStackRenderState item = state.heldItem;
        if (item.isEmpty()) {
            return;
        }
        boolean sleeping = state.isSleeping;
        FoxModel model = this.getParentModel();
        poseStack.pushPose();
        poseStack.translate(model.head.x / 16.0f, model.head.y / 16.0f, model.head.z / 16.0f);
        poseStack.rotate(Axis.ZP, state.headRollAngle);
        poseStack.rotateDegrees(Axis.YP, yRot);
        poseStack.rotateDegrees(Axis.XP, xRot);
        if (sleeping) {
            poseStack.translate(0.46f, 0.26f, 0.22f);
        } else {
            poseStack.translate(0.06f, 0.27f, -0.5f);
        }
        poseStack.rotateDegrees(Axis.XP, 90.0f);
        if (sleeping) {
            poseStack.rotateDegrees(Axis.ZP, 90.0f);
        }
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
