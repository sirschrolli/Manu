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
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_ASSET);
        humanoid(output, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_ASSET);
        // Die Krone sitzt nur auf dem Kopf
        output.accept(ModEquipment.CROWN_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.HUMANOID, new EquipmentClientInfo.Layer(ModEquipment.CROWN_ASSET.identifier()))
                .build());
        // Krone des Zerbrochenen Königs: eigenes 3D-Modell (Reif und schwebende Splitter), nur auf dem Kopf
        output.accept(ModEquipment.SHATTERED_CROWN_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.HUMANOID, new EquipmentClientInfo.Layer(ModEquipment.SHATTERED_CROWN_ASSET.identifier()))
                .build());
        // Feen-Elytren: die Flügel der Mottenkönigin (nicht vom Umhang des Spielers ersetzt)
        output.accept(ModEquipment.FAE_ELYTRA_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WINGS,
                        new EquipmentClientInfo.Layer(ModEquipment.FAE_ELYTRA_ASSET.identifier(), Optional.empty(), false))
                .build());
        // Sternenschwingen: die Schwingen von Asterias Sternenrochen (nicht vom Umhang des Spielers ersetzt)
        output.accept(ModEquipment.STAR_WINGS_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WINGS,
                        new EquipmentClientInfo.Layer(ModEquipment.STAR_WINGS_ASSET.identifier(), Optional.empty(), false))
                .build());
        // Fehlerflügel: fehlende Textur mit Löchern und Farbsäumen (nicht vom Umhang des Spielers ersetzt)
        output.accept(ModEquipment.GLITCH_WINGS_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WINGS,
                        new EquipmentClientInfo.Layer(ModEquipment.GLITCH_WINGS_ASSET.identifier(), Optional.empty(), false))
                .build());
        // Schwingen des Schöpfers: weiße Federn mit goldenen Spitzen (nicht vom Umhang des Spielers ersetzt)
        // die Ausrüstung des Schöpfers: keine Rüstungsebenen am Menschen (die 3D-Teile zeichnet CreatorGearLayer); ein Asset
        // braucht aber eine Ebene – die Ebene des Wolfspanzers zeichnet nur der Wolf
        output.accept(ModEquipment.CREATOR_GEAR_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WOLF_BODY, new EquipmentClientInfo.Layer(ModEquipment.CREATOR_WINGS_ASSET.identifier()))
                .build());
        output.accept(ModEquipment.CREATOR_WINGS_ASSET, EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WINGS,
                        new EquipmentClientInfo.Layer(ModEquipment.CREATOR_WINGS_ASSET.identifier(), Optional.empty(), false))
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
