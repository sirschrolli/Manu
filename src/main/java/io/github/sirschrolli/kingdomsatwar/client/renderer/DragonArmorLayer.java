package io.github.sirschrolli.kingdomsatwar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.DragonModel;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Zeichnet den Drachenpanzer über dem Drachen. Das Panzer-Modell hat dieselben Knochen wie der Drache,
 * daher bewegt es sich mit allen Animationen mit. Draconium-Panzer haben glühende Adern.
 */
public class DragonArmorLayer extends RenderLayer<DragonRenderState, DragonModel> {
    private static final Identifier STEEL = KingdomsAtWar.id("textures/entity/dragon/armor/steel_dragon_armor.png");
    private static final Identifier DRACONIUM = KingdomsAtWar.id("textures/entity/dragon/armor/draconium_dragon_armor.png");
    private static final Identifier DRACONIUM_GLOW = KingdomsAtWar.id("textures/entity/dragon/armor/draconium_dragon_armor_glow.png");

    private final DragonModel model;

    public DragonArmorLayer(RenderLayerParent<DragonRenderState, DragonModel> renderer, EntityModelSet models) {
        super(renderer);
        this.model = new DragonModel(models.bakeLayer(ModModelLayers.DRAGON_ARMOR));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, DragonRenderState state, float yRot, float xRot) {
        Identifier texture = texture(state.armor);
        if (texture == null || state.isInvisible) {
            return;
        }
        renderColoredCutoutModel(this.model, texture, poseStack, collector, light, state, -1, 1);
        if (texture == DRACONIUM) {
            collector.order(2).submitModel(this.model, state, poseStack, RenderTypes.eyes(DRACONIUM_GLOW), light,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
    }

    private static @Nullable Identifier texture(ItemStack armor) {
        if (armor.is(ModItems.STEEL_DRAGON_ARMOR.get())) {
            return STEEL;
        }
        if (armor.is(ModItems.DRACONIUM_DRAGON_ARMOR.get())) {
            return DRACONIUM;
        }
        return null;
    }
}
