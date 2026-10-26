package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.ModWorldgen;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Startpunkt der Datengenerierung ({@code gradlew runData}).
 * Erzeugt alle JSON-Dateien in {@code src/generated/resources}.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class DataGenerators {
    private DataGenerators() {}

    /** Datapack-Registereinträge: Weltgenerierung, Loot-Tables und Rezepte. */
    @SubscribeEvent
    public static void gatherRegistries(GatherDataRegistryEntriesEvent event) {
        event.add(Registries.FEATURE, ModWorldgen::bootstrapFeatures)
                .add(Registries.PLACED_FEATURE, ModWorldgen::bootstrapPlacedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldgen::bootstrapBiomeModifiers)
                .add(Registries.STRUCTURE, io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures::bootstrapStructures)
                .add(Registries.STRUCTURE_SET, io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures::bootstrapStructureSets)
                .lootTable(
                        new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(ModExtraLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(ModEntityLootProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(ModChestLootProvider::new, LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(ModArchaeologyLootProvider::new, LootContextParamSets.ARCHAEOLOGY))
                .recipe(ModRecipeProvider::new);
    }

    /** Alle übrigen Daten: Modelle, Tags, Übersetzungen, Loot-Modifier und Data Maps. */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEquipmentAssetProvider::new);
        event.createBlockAndItemTags(ModBlockTagsProvider::new, ModItemTagsProvider::new);
        event.createProvider(output -> new ModLanguageProvider(output, "en_us"));
        event.createProvider(output -> new ModLanguageProvider(output, "de_de"));
        event.createProvider(ModLootModifierProvider::new);
        event.createProvider(ModDataMapProvider::new);
        event.createProvider(ModBiomeTagsProvider::new);
    }
}
