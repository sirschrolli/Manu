package io.github.sirschrolli.kingdomsatwar.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lage und Größe eines Wesens beim Zeichnen – so rechnet RodTip die Rutenspitze am echten Spielermodell nach. */
@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {
    @Invoker("setupRotations")
    void kingdomsatwar$setupRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale);

    @Invoker("scale")
    void kingdomsatwar$scale(LivingEntityRenderState state, PoseStack poseStack);
}
