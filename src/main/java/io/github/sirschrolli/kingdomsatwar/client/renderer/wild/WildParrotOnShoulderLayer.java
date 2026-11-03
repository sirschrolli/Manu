package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.parrot.Parrot;

/**
 * Papagei auf der Schulter – mit dem eigenen Modell und Fellbild statt des Vanilla-Papageis (ersetzt die Vanilla-Ebene
 * im Spieler-Renderer, siehe {@link WildRenderers#replaceShoulderParrots}).
 */
public class WildParrotOnShoulderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final ParrotModel model;

    public WildParrotOnShoulderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new ParrotModel(modelSet.bakeLayer(WildRenderers.PARROT));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        if (state.parrotOnLeftShoulder != null) {
            this.submitOnShoulder(poseStack, collector, light, state, state.parrotOnLeftShoulder, yRot, xRot, true);
        }
        if (state.parrotOnRightShoulder != null) {
            this.submitOnShoulder(poseStack, collector, light, state, state.parrotOnRightShoulder, yRot, xRot, false);
        }
    }

    private void submitOnShoulder(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState player,
            Parrot.Variant variant, float yRot, float xRot, boolean left) {
        poseStack.pushPose();
        poseStack.translate(left ? 0.4f : -0.4f, player.isCrouching ? -1.3f : -1.5f, 0.0f);
        ParrotRenderState parrot = new ParrotRenderState();
        parrot.pose = ParrotModel.Pose.ON_SHOULDER;
        parrot.ageInTicks = player.ageInTicks;
        parrot.walkAnimationPos = player.walkAnimationPos;
        parrot.walkAnimationSpeed = player.walkAnimationSpeed;
        parrot.yRot = yRot;
        parrot.xRot = xRot;
        collector.submitModel(this.model, parrot, poseStack, WildParrotRenderer.texture(variant), light, OverlayTexture.NO_OVERLAY,
                player.outlineColor);
        poseStack.popPose();
    }
}
