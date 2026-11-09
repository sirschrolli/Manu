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
        event.add(Registries.NOISE, context -> {
                    io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain.bootstrapNoises(context);
                    io.github.sirschrolli.kingdomsatwar.voidrealm.VoidTerrain.bootstrapNoises(context);
                    io.github.sirschrolli.kingdomsatwar.fae.FaeTerrain.bootstrapNoises(context);
                })
                .add(Registries.DENSITY_FUNCTION, context -> {
                    io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain.bootstrapDensityFunctions(context);
                    io.github.sirschrolli.kingdomsatwar.voidrealm.VoidTerrain.bootstrapDensityFunctions(context);
                    io.github.sirschrolli.kingdomsatwar.fae.FaeTerrain.bootstrapDensityFunctions(context);
                })
                .add(Registries.MATERIAL_RULE, context -> {
                    io.github.sirschrolli.kingdomsatwar.shadow.ShadowSurface.bootstrap(context);
                    io.github.sirschrolli.kingdomsatwar.voidrealm.VoidSurface.bootstrap(context);
                    io.github.sirschrolli.kingdomsatwar.fae.FaeSurface.bootstrap(context);
                    io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldSurface.bootstrap(context);
                })
                .add(Registries.NOISE_SETTINGS, context -> {
                    io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain.bootstrapSettings(context);
                    io.github.sirschrolli.kingdomsatwar.voidrealm.VoidTerrain.bootstrapSettings(context);
                    io.github.sirschrolli.kingdomsatwar.fae.FaeTerrain.bootstrapSettings(context);
                })
                .add(Registries.DIMENSION_TYPE, context -> {
                    io.github.sirschrolli.kingdomsatwar.shadow.ShadowDimension.bootstrapType(context);
                    io.github.sirschrolli.kingdomsatwar.voidrealm.VoidDimension.bootstrapType(context);
                    io.github.sirschrolli.kingdomsatwar.fae.FaeDimension.bootstrapType(context);
                    io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere.bootstrapType(context);
                })
                .add(Registries.LEVEL_STEM, context -> {
                    io.github.sirschrolli.kingdomsatwar.shadow.ShadowDimension.bootstrapStem(context);
                    io.github.sirschrolli.kingdomsatwar.voidrealm.VoidDimension.bootstrapStem(context);
                    io.github.sirschrolli.kingdomsatwar.fae.FaeDimension.bootstrapStem(context);
                    io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere.bootstrapStem(context);
                })
                .add(Registries.FEATURE, ModWorldgen::bootstrapFeatures)
                .add(Registries.PLACED_FEATURE, ModWorldgen::bootstrapPlacedFeatures)
                .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldgen::bootstrapBiomeModifiers)
                .add(Registries.BIOME, io.github.sirschrolli.kingdomsatwar.worldgen.ModBiomes::bootstrap)
                .add(Registries.STRUCTURE, io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures::bootstrapStructures)
                .add(Registries.STRUCTURE_SET, io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures::bootstrapStructureSets)
                .add(Registries.BANNER_PATTERN, io.github.sirschrolli.kingdomsatwar.war.Realm::bootstrapPatterns)
                .add(Registries.ENCHANTMENT, io.github.sirschrolli.kingdomsatwar.enchant.ModEnchantments::bootstrap)
                .add(Registries.DAMAGE_TYPE, io.github.sirschrolli.kingdomsatwar.registry.ModDamageTypes::bootstrap)
                .lootTable(
                        new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(ModExtraLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(ModEntityLootProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(ModGameplayEntityLootProvider::new, LootContextParamSets.ENTITY),
                        new LootTableProvider.SubProviderEntry(ModChestLootProvider::new, LootContextParamSets.CHEST),
                        new LootTableProvider.SubProviderEntry(ModArchaeologyLootProvider::new, LootContextParamSets.ARCHAEOLOGY))
                .recipe(ModRecipeProvider::new);
        // Vanilla-Einträge, die die Mod ersetzt: die Oberwelt-Oberflächen, die Weltvoreinstellungen mit den eigenen Biomen
        // und die Bäume (neue Gestalten unter den alten Namen)
        event.gatherFor("minecraft")
                .add(Registries.FEATURE, io.github.sirschrolli.kingdomsatwar.worldgen.tree.ModTrees::bootstrapVanillaOverride)
                .add(Registries.MATERIAL_RULE, io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldSurface::bootstrapVanillaOverride)
                .add(Registries.WORLD_PRESET, io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldPresets::bootstrap);
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
        event.createProvider(ModStructureTagsProvider::new);
        event.createProvider(ModEntityTypeTagsProvider::new);
        event.createProvider(ModEnchantmentTagsProvider::new);
        event.createProvider(ModDamageTypeTagsProvider::new);
    }
}
