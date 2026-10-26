package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Gegenstands-Tags. Viele werden aus den Block-Tags übernommen (Treppen, Erze, Speicherblöcke ...).
 */
public class ModItemTagsProvider extends BlockTagCopyingItemTagProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        // --- aus den Block-Tags übernehmen ---
        copy(BlockItemTags.STAIRS);
        copy(BlockItemTags.SLABS);
        copy(BlockItemTags.WALLS);
        copy(BlockItemTags.SMALL_FLOWERS);
        copy(BlockItemTags.LOGS);
        copy(BlockItemTags.LOGS_THAT_BURN);
        copy(BlockItemTags.LEAVES);
        copy(BlockItemTags.PLANKS);
        copy(BlockItemTags.WOODEN_STAIRS);
        copy(BlockItemTags.WOODEN_SLABS);
        copy(BlockItemTags.DIRT);
        copy(BlockItemTags.SAND);
        copy(BlockItemTags.SMELTS_TO_GLASS);
        copy(BlockItemTags.MOSS_BLOCKS);
        copy(ModTags.Blocks.SHADOW_LOGS, ModTags.Items.SHADOW_LOGS);
        copy(BlockItemTags.SAPLINGS);
        copy(BlockItemTags.LANTERNS);
        copy(ModTags.Blocks.ORES_MOONSILVER, ModTags.Items.ORES_MOONSILVER);
        copy(ModTags.Blocks.ORES_NIGHTSTEEL, ModTags.Items.ORES_NIGHTSTEEL);
        copy(ModTags.Blocks.ORES_SOUL_CRYSTAL, ModTags.Items.ORES_SOUL_CRYSTAL);
        copy(ModTags.Blocks.ORES_ABYSSITE, ModTags.Items.ORES_ABYSSITE);
        copy(ModTags.Blocks.STORAGE_BLOCKS_MOONSILVER, ModTags.Items.STORAGE_BLOCKS_MOONSILVER);
        copy(ModTags.Blocks.STORAGE_BLOCKS_NIGHTSTEEL, ModTags.Items.STORAGE_BLOCKS_NIGHTSTEEL);
        copy(ModTags.Blocks.STORAGE_BLOCKS_SOUL_CRYSTAL, ModTags.Items.STORAGE_BLOCKS_SOUL_CRYSTAL);
        copy(ModTags.Blocks.STORAGE_BLOCKS_ABYSSITE, ModTags.Items.STORAGE_BLOCKS_ABYSSITE);
        copy(BlockItemTags.WOODEN_DOORS);
        copy(BlockItemTags.WOODEN_TRAPDOORS);
        copy(Tags.BlockItems.GLASS_BLOCKS);
        copy(Tags.BlockItems.GLASS_PANES);
        copy(BlockItemTags.ORES);
        copy(Tags.BlockItems.ORES);
        copy(Tags.BlockItems.ORES_IN_GROUND_STONE);
        copy(Tags.BlockItems.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.BlockItems.ORE_RATES_SINGULAR);
        copy(Tags.BlockItems.STORAGE_BLOCKS);
        copy(ModTags.Blocks.DRAGON_EGGS, ModTags.Items.DRAGON_EGGS);
        copy(ModTags.Blocks.ORES_CARBON, ModTags.Items.ORES_CARBON);
        copy(ModTags.Blocks.ORES_GEMSTONE, ModTags.Items.ORES_GEMSTONE);
        copy(ModTags.Blocks.ORES_KONTERIT, ModTags.Items.ORES_KONTERIT);
        copy(ModTags.Blocks.ORES_AETHERIT, ModTags.Items.ORES_AETHERIT);
        copy(ModTags.Blocks.ORES_DRACONIUM, ModTags.Items.ORES_DRACONIUM);
        copy(ModTags.Blocks.ORES_WYVERNIT, ModTags.Items.ORES_WYVERNIT);
        copy(ModTags.Blocks.STORAGE_BLOCKS_CARBON, ModTags.Items.STORAGE_BLOCKS_CARBON);
        copy(ModTags.Blocks.STORAGE_BLOCKS_GEMSTONE, ModTags.Items.STORAGE_BLOCKS_GEMSTONE);
        copy(ModTags.Blocks.STORAGE_BLOCKS_KONTERIT, ModTags.Items.STORAGE_BLOCKS_KONTERIT);
        copy(ModTags.Blocks.STORAGE_BLOCKS_AETHERIT, ModTags.Items.STORAGE_BLOCKS_AETHERIT);
        copy(ModTags.Blocks.STORAGE_BLOCKS_DRACONIUM, ModTags.Items.STORAGE_BLOCKS_DRACONIUM);
        copy(ModTags.Blocks.STORAGE_BLOCKS_WYVERNIT, ModTags.Items.STORAGE_BLOCKS_WYVERNIT);
        copy(ModTags.Blocks.STORAGE_BLOCKS_STEEL, ModTags.Items.STORAGE_BLOCKS_STEEL);

        // --- Rohstoffe ---
        add(ItemTags.COALS, ModItems.CARBON);
        add(ModTags.Items.GEMS_GEMSTONE, ModItems.GEMSTONE);
        add(ModTags.Items.GEMS_KONTERIT, ModItems.KONTERIT);
        add(ModTags.Items.GEMS_AETHERIT, ModItems.AETHERIT);
        add(ModTags.Items.GEMS_DRACONIUM, ModItems.DRACONIUM);
        add(ModTags.Items.GEMS_WYVERNIT, ModItems.WYVERNIT);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_GEMSTONE).addTag(ModTags.Items.GEMS_KONTERIT)
                .addTag(ModTags.Items.GEMS_AETHERIT).addTag(ModTags.Items.GEMS_DRACONIUM)
                .addTag(ModTags.Items.GEMS_WYVERNIT);
        add(ModTags.Items.INGOTS_STEEL, ModItems.STEEL_INGOT);
        tag(Tags.Items.INGOTS).addTag(ModTags.Items.INGOTS_STEEL);
        add(ItemTags.BEACON_PAYMENT_ITEMS, ModItems.STEEL_INGOT);

        // --- Schätze der Schattenwelt ---
        add(ModTags.Items.INGOTS_MOONSILVER, ShadowItems.MOONSILVER_INGOT);
        add(ModTags.Items.INGOTS_NIGHTSTEEL, ShadowItems.NIGHTSTEEL_INGOT);
        add(ModTags.Items.INGOTS_ABYSSITE, ShadowItems.ABYSSITE_INGOT);
        tag(Tags.Items.INGOTS).addTag(ModTags.Items.INGOTS_MOONSILVER).addTag(ModTags.Items.INGOTS_NIGHTSTEEL)
                .addTag(ModTags.Items.INGOTS_ABYSSITE);
        add(ModTags.Items.NUGGETS_MOONSILVER, ShadowItems.MOONSILVER_NUGGET);
        tag(Tags.Items.NUGGETS).addTag(ModTags.Items.NUGGETS_MOONSILVER);
        add(ModTags.Items.RAW_MATERIALS_MOONSILVER, ShadowItems.RAW_MOONSILVER);
        add(ModTags.Items.RAW_MATERIALS_NIGHTSTEEL, ShadowItems.RAW_NIGHTSTEEL);
        tag(Tags.Items.RAW_MATERIALS).addTag(ModTags.Items.RAW_MATERIALS_MOONSILVER).addTag(ModTags.Items.RAW_MATERIALS_NIGHTSTEEL);
        add(ModTags.Items.GEMS_SOUL_CRYSTAL, ShadowItems.SOUL_CRYSTAL);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_SOUL_CRYSTAL);
        add(ItemTags.BEACON_PAYMENT_ITEMS, ShadowItems.MOONSILVER_INGOT, ShadowItems.NIGHTSTEEL_INGOT, ShadowItems.ABYSSITE_INGOT);
        add(ModTags.Items.NIGHTSTEEL_TOOL_MATERIALS, ShadowItems.NIGHTSTEEL_INGOT);
        add(ModTags.Items.REPAIRS_NIGHTSTEEL_ARMOR, ShadowItems.NIGHTSTEEL_INGOT);
        add(ModTags.Items.ABYSSITE_TOOL_MATERIALS, ShadowItems.ABYSSITE_INGOT);
        add(ModTags.Items.REPAIRS_ABYSSITE_ARMOR, ShadowItems.ABYSSITE_INGOT);
        add(ModTags.Items.MOONSILVER_TOOL_MATERIALS, ShadowItems.MOONSILVER_INGOT);
        add(ItemTags.SWORDS, ShadowItems.NIGHTSTEEL_SWORD, ShadowItems.ABYSSITE_SWORD, ShadowItems.MOONSILVER_BLADE);
        add(ItemTags.PICKAXES, ShadowItems.NIGHTSTEEL_PICKAXE, ShadowItems.ABYSSITE_PICKAXE);
        add(ItemTags.AXES, ShadowItems.NIGHTSTEEL_AXE, ShadowItems.ABYSSITE_AXE);
        add(ItemTags.SHOVELS, ShadowItems.NIGHTSTEEL_SHOVEL, ShadowItems.ABYSSITE_SHOVEL);
        add(ItemTags.HOES, ShadowItems.NIGHTSTEEL_HOE, ShadowItems.ABYSSITE_HOE);
        add(ItemTags.HEAD_ARMOR, ShadowItems.NIGHTSTEEL_HELMET, ShadowItems.ABYSSITE_HELMET);
        add(ItemTags.CHEST_ARMOR, ShadowItems.NIGHTSTEEL_CHESTPLATE, ShadowItems.ABYSSITE_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, ShadowItems.NIGHTSTEEL_LEGGINGS, ShadowItems.ABYSSITE_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, ShadowItems.NIGHTSTEEL_BOOTS, ShadowItems.ABYSSITE_BOOTS);
        add(ItemTags.TRIMMABLE_ARMOR, ShadowItems.NIGHTSTEEL_HELMET, ShadowItems.NIGHTSTEEL_CHESTPLATE, ShadowItems.NIGHTSTEEL_LEGGINGS,
                ShadowItems.NIGHTSTEEL_BOOTS, ShadowItems.ABYSSITE_HELMET, ShadowItems.ABYSSITE_CHESTPLATE, ShadowItems.ABYSSITE_LEGGINGS,
                ShadowItems.ABYSSITE_BOOTS);

        // --- Drachen ---
        ModItems.DRAGON_SCALES.values().forEach(scale -> add(ModTags.Items.DRAGON_SCALES, scale));

        // --- Werkzeuge, Waffen und Rüstungen ---
        add(ModTags.Items.STEEL_TOOL_MATERIALS, ModItems.STEEL_INGOT);
        add(ModTags.Items.REPAIRS_STEEL_ARMOR, ModItems.STEEL_INGOT);
        add(ModTags.Items.DRACONIUM_TOOL_MATERIALS, ModItems.DRACONIUM);
        add(ModTags.Items.REPAIRS_DRACONIUM, ModItems.DRACONIUM);
        add(ModTags.Items.REPAIRS_KONTERIT_ARMOR, ModItems.KONTERIT);
        add(ModTags.Items.REPAIRS_GLIDER, ModItems.WYVERNIT);
        add(ModTags.Items.REPAIRS_CROWN, ModItems.GEMSTONE);
        tag(ModTags.Items.REPAIRS_CROWN).addTag(Tags.Items.INGOTS_GOLD);
        ModItems.DRAGON_SCALES.forEach((type, scale) -> add(ModTags.Items.scaleArmorRepair(type), scale));

        add(ItemTags.SWORDS, ModItems.STEEL_SWORD, ModItems.DRACONIUM_SWORD);
        add(ItemTags.PICKAXES, ModItems.STEEL_PICKAXE);
        add(ItemTags.AXES, ModItems.STEEL_AXE, ModItems.DRACONIUM_AXE);
        add(ItemTags.SHOVELS, ModItems.STEEL_SHOVEL);
        add(ItemTags.HOES, ModItems.STEEL_HOE);
        add(ItemTags.HEAD_ARMOR, ModItems.STEEL_HELMET, ModItems.KONTERIT_HELMET, ModItems.GEMSTONE_CROWN);
        add(ItemTags.CHEST_ARMOR, ModItems.STEEL_CHESTPLATE, ModItems.KONTERIT_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, ModItems.STEEL_LEGGINGS, ModItems.KONTERIT_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, ModItems.STEEL_BOOTS, ModItems.KONTERIT_BOOTS);
        add(ItemTags.TRIMMABLE_ARMOR, ModItems.STEEL_HELMET, ModItems.STEEL_CHESTPLATE, ModItems.STEEL_LEGGINGS,
                ModItems.STEEL_BOOTS, ModItems.KONTERIT_HELMET, ModItems.KONTERIT_CHESTPLATE, ModItems.KONTERIT_LEGGINGS,
                ModItems.KONTERIT_BOOTS);
        List<TagKey<Item>> slots = List.of(ItemTags.HEAD_ARMOR, ItemTags.CHEST_ARMOR, ItemTags.LEG_ARMOR, ItemTags.FOOT_ARMOR);
        ModItems.DRAGON_SCALE_ARMOR.forEach((type, set) -> {
            for (int i = 0; i < set.size(); i++) {
                add(slots.get(i), set.get(i));
                add(ItemTags.TRIMMABLE_ARMOR, set.get(i));
                add(ModTags.Items.DRAGON_SCALE_ARMOR, set.get(i));
                if (type == DragonType.ICE) {
                    add(ItemTags.FREEZE_IMMUNE_WEARABLES, set.get(i));
                }
            }
        });
        // Der Gleiter lässt sich wie Elytren verzaubern (Haltbarkeit, Reparatur, Flüche)
        add(ItemTags.DURABILITY_ENCHANTABLE, ModItems.WYVERNIT_GLIDER);
        add(ItemTags.EQUIPPABLE_ENCHANTABLE, ModItems.WYVERNIT_GLIDER);
        add(Tags.Items.ARMORS, ModItems.STEEL_DRAGON_ARMOR, ModItems.DRACONIUM_DRAGON_ARMOR);
        add(ItemTags.DURABILITY_ENCHANTABLE, ModItems.TROLL_CLUB);
        add(ItemTags.MELEE_WEAPON_ENCHANTABLE, ModItems.TROLL_CLUB);
        add(Tags.Items.MELEE_WEAPON_TOOLS, ModItems.TROLL_CLUB);
        add(ItemTags.PIGLIN_LOVED, ModItems.GEMSTONE_CROWN);

        // --- Landwirtschaft ---
        add(ModTags.Items.SEEDS_TOMATO, ModItems.TOMATO_SEEDS);
        add(ModTags.Items.SEEDS_CABBAGE, ModItems.CABBAGE_SEEDS);
        add(ModTags.Items.SEEDS_BARLEY, ModItems.BARLEY_SEEDS);
        tag(Tags.Items.SEEDS).addTag(ModTags.Items.SEEDS_TOMATO).addTag(ModTags.Items.SEEDS_CABBAGE)
                .addTag(ModTags.Items.SEEDS_BARLEY);
        add(ModTags.Items.CROPS_TOMATO, ModItems.TOMATO);
        add(ModTags.Items.CROPS_CABBAGE, ModItems.CABBAGE);
        add(ModTags.Items.CROPS_ONION, ModItems.ONION);
        add(ModTags.Items.CROPS_BARLEY, ModItems.BARLEY);
        add(ModTags.Items.CROPS_HOPS, ModItems.HOPS);
        add(ModTags.Items.CROPS_GRAPE, ModItems.GRAPES);
        add(ModTags.Items.CROPS_GARLIC, ModItems.GARLIC);
        tag(Tags.Items.CROPS).addTag(ModTags.Items.CROPS_TOMATO).addTag(ModTags.Items.CROPS_CABBAGE)
                .addTag(ModTags.Items.CROPS_ONION).addTag(ModTags.Items.CROPS_BARLEY).addTag(ModTags.Items.CROPS_HOPS)
                .addTag(ModTags.Items.CROPS_GRAPE).addTag(ModTags.Items.CROPS_GARLIC);

        // Bauern-Dorfbewohner pflanzen unsere Samen an, Hühner fressen sie
        add(ItemTags.VILLAGER_PLANTABLE_SEEDS, ModItems.TOMATO_SEEDS, ModItems.CABBAGE_SEEDS, ModItems.BARLEY_SEEDS,
                ModItems.ONION, ModItems.HOPS, ModItems.GRAPES, ModItems.GARLIC);
        add(ItemTags.VILLAGER_PICKS_UP, ModItems.TOMATO_SEEDS, ModItems.CABBAGE_SEEDS, ModItems.BARLEY_SEEDS,
                ModItems.ONION, ModItems.TOMATO, ModItems.CABBAGE, ModItems.BARLEY, ModItems.HOPS, ModItems.GRAPES, ModItems.GARLIC);
        add(ItemTags.CHICKEN_FOOD, ModItems.TOMATO_SEEDS, ModItems.CABBAGE_SEEDS, ModItems.BARLEY_SEEDS);

        // --- Magie: Haltbarkeits-Verzauberungen (Haltbarkeit, Reparatur) für den Stab ---
        add(ItemTags.DURABILITY_ENCHANTABLE, ModItems.AETHERIT_STAFF);

        // --- Nahrung ---
        add(Tags.Items.FOODS_VEGETABLE, ModItems.TOMATO, ModItems.CABBAGE, ModItems.ONION, ModItems.GARLIC);
        add(Tags.Items.FOODS_FRUIT, ModItems.GRAPES);
        add(Tags.Items.FOODS_BREAD, ModItems.BREAD_ROLL, ModItems.GARLIC_BREAD);
        add(Tags.Items.DRINKS, ModItems.ALE, ModItems.WINE, ModItems.MEAD);
        add(Tags.Items.DRINKS_HONEY, ModItems.MEAD);
        add(Tags.Items.DRINK_CONTAINING_BOTTLE, ModItems.ALE, ModItems.WINE, ModItems.MEAD);
        tag(Tags.Items.FOODS_EDIBLE_WHEN_PLACED).add(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ITEM, ModBlocks.FEAST.getId()));
        add(Tags.Items.FOODS_SOUP, ModItems.VEGETABLE_STEW);
        add(Tags.Items.FOODS_PIE, ModItems.MEAT_PIE, ModItems.APPLE_PIE);
        add(ItemTags.MEAT, ModItems.VENISON, ModItems.COOKED_VENISON);
        add(Tags.Items.FOODS_RAW_MEAT, ModItems.VENISON);
        add(Tags.Items.FOODS_COOKED_MEAT, ModItems.COOKED_VENISON);
        add(ItemTags.WOLF_FOOD, ModItems.VENISON, ModItems.COOKED_VENISON);
    }

    @SafeVarargs
    private void add(TagKey<Item> tag, Supplier<? extends Item>... items) {
        TagAppender<Item> appender = tag(tag);
        for (Supplier<? extends Item> item : items) {
            if (item instanceof DeferredItem<?> deferred) {
                appender.add(deferred.key());
            } else {
                throw new IllegalArgumentException("Nur registrierte Mod-Gegenstände: " + item);
            }
        }
    }
}
