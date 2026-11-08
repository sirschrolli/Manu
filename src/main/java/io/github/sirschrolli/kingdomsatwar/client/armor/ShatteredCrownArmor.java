package io.github.sirschrolli.kingdomsatwar.client.armor;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Krone des Zerbrochenen Königs wird nicht als flache Rüstungsschicht gezeichnet, sondern als eigenes 3D-Modell:
 * ein goldener Reif um den Kopf, darüber sieben schwebende Kristallsplitter (Geometrie: ShatteredCrownGeometry, Textur:
 * textures/entity/equipment/humanoid/shattered_crown.png). NeoForge überträgt die Haltung des Spielers auf das Modell.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ShatteredCrownArmor {
    private static @Nullable HumanoidModel<HumanoidRenderState> model;

    private ShatteredCrownArmor() {}

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
                if (layerType != EquipmentClientInfo.LayerType.HUMANOID) {
                    return original;
                }
                if (model == null) {
                    model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.SHATTERED_CROWN));
                }
                return model;
            }
        }, VoidBlocks.SHATTERED_CROWN.get());
    }
}
