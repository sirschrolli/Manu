package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
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

        // --- Drachen ---
        ModItems.DRAGON_SCALES.values().forEach(scale -> add(ModTags.Items.DRAGON_SCALES, scale));

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
        tag(Tags.Items.CROPS).addTag(ModTags.Items.CROPS_TOMATO).addTag(ModTags.Items.CROPS_CABBAGE)
                .addTag(ModTags.Items.CROPS_ONION).addTag(ModTags.Items.CROPS_BARLEY);

        // Bauern-Dorfbewohner pflanzen unsere Samen an, Hühner fressen sie
        add(ItemTags.VILLAGER_PLANTABLE_SEEDS, ModItems.TOMATO_SEEDS, ModItems.CABBAGE_SEEDS, ModItems.BARLEY_SEEDS,
                ModItems.ONION);
        add(ItemTags.VILLAGER_PICKS_UP, ModItems.TOMATO_SEEDS, ModItems.CABBAGE_SEEDS, ModItems.BARLEY_SEEDS,
                ModItems.ONION, ModItems.TOMATO, ModItems.CABBAGE, ModItems.BARLEY);
        add(ItemTags.CHICKEN_FOOD, ModItems.TOMATO_SEEDS, ModItems.CABBAGE_SEEDS, ModItems.BARLEY_SEEDS);

        // --- Nahrung ---
        add(Tags.Items.FOODS_VEGETABLE, ModItems.TOMATO, ModItems.CABBAGE, ModItems.ONION);
        add(Tags.Items.FOODS_BREAD, ModItems.BREAD_ROLL);
        add(Tags.Items.FOODS_SOUP, ModItems.VEGETABLE_STEW);
        add(Tags.Items.FOODS_PIE, ModItems.MEAT_PIE, ModItems.APPLE_PIE);
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
