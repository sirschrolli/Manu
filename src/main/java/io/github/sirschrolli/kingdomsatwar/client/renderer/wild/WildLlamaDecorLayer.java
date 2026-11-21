package io.github.sirschrolli.kingdomsatwar.client.renderer.wild;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.llama.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Teppich bzw. Händlerdecke des Lamas – die Vanilla-Decken passen, weil das Skelett das Vanilla-Skelett ist. Anders als
 * bei Vanilla immer mit dem erwachsenen Deckenmodell, denn Jungtiere sind hier das verkleinerte erwachsene Modell.
 */
public class WildLlamaDecorLayer extends RenderLayer<LlamaRenderState, LlamaModel> {
    private final LlamaModel model;
    private final EquipmentLayerRenderer equipmentRenderer;

    public WildLlamaDecorLayer(RenderLayerParent<LlamaRenderState, LlamaModel> renderer, EntityModelSet modelSet,
            EquipmentLayerRenderer equipmentRenderer) {
        super(renderer);
        this.model = new LlamaModel(modelSet.bakeLayer(ModelLayers.LLAMA_DECOR));
        this.equipmentRenderer = equipmentRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, LlamaRenderState state, float yRot, float xRot) {
        ItemStack stack = state.bodyItem;
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent() && !state.isBaby) {
            this.render(poseStack, collector, state, stack, equippable.assetId().get(), light);
        } else if (state.isTraderLlama) {
            this.render(poseStack, collector, state, ItemStack.EMPTY, EquipmentAssets.TRADER_LLAMA, light);
        }
    }

    private void render(PoseStack poseStack, SubmitNodeCollector collector, LlamaRenderState state, ItemStack stack,
            ResourceKey<EquipmentAsset> asset, int light) {
        this.equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.LLAMA_BODY, asset, this.model, state, stack, poseStack,
                collector, light, state.outlineColor);
    }
}
