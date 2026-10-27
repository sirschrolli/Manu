package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEquipment;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

/**
 * Beschreibt, welche Texturen getragene Ausrüstung benutzt
 * ({@code assets/kingdomsatwar/equipment/*.json} → {@code textures/entity/equipment/...}).
 */
public class ModEquipmentAssetProvider extends EquipmentAssetProvider {
    public ModEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        humanoid(output, ModEquipment.STEEL_ASSET);
        humanoid(output, ModEquipment.KONTERIT_ASSET);
        humanoid(output, ModEquipment.NIGHTSTEEL_ASSET);
        humanoid(output, ModEquipment.ABYSSITE_ASSET);
        ModEquipment.SCALE_ASSETS.values().forEach(asset -> humanoid(output, asset));
        // Die Krone sitzt nur auf dem Kopf
        output.accept(ModEquipment.CROWN_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.HUMANOID, new EquipmentClientInfo.Layer(ModEquipment.CROWN_ASSET.identifier()))
                .build());
        // Gleiter: eigene Flügel-Textur (nicht vom Umhang des Spielers ersetzt)
        output.accept(ModEquipment.GLIDER_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WINGS,
                        new EquipmentClientInfo.Layer(ModEquipment.GLIDER_ASSET.identifier(), Optional.empty(), false))
                .build());
    }

    private static void humanoid(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output, ResourceKey<EquipmentAsset> asset) {
        output.accept(asset, EquipmentClientInfo.builder().addHumanoidLayers(asset.identifier()).build());
    }

    @Override
    public String getName() {
        return "Equipment Assets: " + KingdomsAtWar.MODID;
    }
}
