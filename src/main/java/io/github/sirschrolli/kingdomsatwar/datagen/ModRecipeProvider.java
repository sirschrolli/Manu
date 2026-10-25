package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/**
 * Alle Rezepte der Mod.
 *
 * <p>Jedes Rezept bekommt ausdrücklich einen Namen im Namensraum {@code kingdomsatwar},
 * damit nichts versehentlich im {@code minecraft}-Namensraum landet.
 */
public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        super(recipes, advancements);
    }

    @Override
    protected void buildRecipes() {
        buildingRecipes();
        resourceRecipes();
        dragonRecipes();
        foodRecipes();
    }

    // =====================================================================================
    // Baublöcke
    // =====================================================================================

    private void buildingRecipes() {
        // Steinmetz: Vanilla-Gestein -> mittelalterliche Steine
        stonecut(ModBlocks.MEDIEVAL_STONE.base().get(), 1, Blocks.STONE, Blocks.COBBLESTONE);
        stonecut(ModBlocks.ROUGH_FIELDSTONE.base().get(), 1, Blocks.COBBLESTONE);
        stonecut(ModBlocks.HEWN_SANDSTONE.base().get(), 1, Blocks.SANDSTONE, Blocks.CUT_SANDSTONE);
        stonecut(ModBlocks.DARK_STONE_BRICKS.base().get(), 1, Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE);
        stonecut(ModBlocks.LIMESTONE_BRICKS.base().get(), 1, Blocks.CALCITE);
        stonecut(ModBlocks.WEATHERED_STONE_BRICKS.base().get(), 1, Blocks.STONE_BRICKS, Blocks.STONE);
        stonecut(ModBlocks.MOSSY_WALL_STONES.base().get(), 1, Blocks.MOSSY_STONE_BRICKS, Blocks.MOSSY_COBBLESTONE);
        stonecut(ModBlocks.CRACKED_STONE_BLOCKS.base().get(), 1, Blocks.CRACKED_STONE_BRICKS, Blocks.STONE);
        stonecut(ModBlocks.STONE_PILLAR.get(), 1, Blocks.STONE, ModBlocks.MEDIEVAL_STONE.base().get());

        // Grober Naturstein auch ohne Steinmetz: Bruchstein + Kies
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ROUGH_FIELDSTONE.base().get(), 4)
                .pattern("CG")
                .pattern("GC")
                .define('C', Blocks.COBBLESTONE)
                .define('G', Blocks.GRAVEL)
                .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                .save(output, key("rough_fieldstone_from_gravel"));
        // Steinpfeiler: zwei mittelalterliche Steine übereinander
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STONE_PILLAR.get(), 2)
                .pattern("S")
                .pattern("S")
                .define('S', ModBlocks.MEDIEVAL_STONE.base().get())
                .unlockedBy("has_medieval_stone", has(ModBlocks.MEDIEVAL_STONE.base().get()))
                .save(output, key("stone_pillar"));

        // Dächer
        stonecut(ModBlocks.RED_ROOF_TILES.base().get(), 1, Blocks.DYED_TERRACOTTA.red(), Blocks.BRICKS);
        stonecut(ModBlocks.BROWN_ROOF_TILES.base().get(), 1, Blocks.DYED_TERRACOTTA.brown());
        stonecut(ModBlocks.LIGHT_ROOF_TILES.base().get(), 1, Blocks.DYED_TERRACOTTA.white(), Blocks.DYED_TERRACOTTA.lightGray());
        stonecut(ModBlocks.DARK_SLATE_SHINGLES.base().get(), 1, Blocks.DEEPSLATE_TILES, Blocks.COBBLED_DEEPSLATE);
        stonecut(ModBlocks.COPPER_ROOF.base().get(), 4, Blocks.COPPER_BLOCK.weathering().unaffected());
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.WOODEN_SHINGLES.base().get(), 4)
                .pattern("SS")
                .pattern("SS")
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_wooden_slab", has(ItemTags.WOODEN_SLABS))
                .save(output, key("wooden_shingles"));
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_ROOF_TILES.base().get(), 4)
                .pattern("B B")
                .pattern("BBB")
                .define('B', Items.BRICK)
                .unlockedBy("has_brick", has(Items.BRICK))
                .save(output, key("red_roof_tiles_from_bricks"));

        // Treppen, Stufen und Mauern für alle Baustoffe (Werkbank und Steinmetz)
        for (BuildingSet set : ModBlocks.BUILDING_SETS) {
            ItemLike base = set.base().get();
            RecipeCategory category = RecipeCategory.BUILDING_BLOCKS;
            save(stairBuilder(set.stairs().get(), Ingredient.of(base)).unlockedBy(getHasName(base), has(base)),
                    getItemName(set.stairs().get()));
            save(slabBuilder(category, set.slab().get(), Ingredient.of(base)).unlockedBy(getHasName(base), has(base)),
                    getItemName(set.slab().get()));
            if (set.wall() != null) {
                save(wallBuilder(RecipeCategory.DECORATIONS, set.wall().get(), Ingredient.of(base))
                        .unlockedBy(getHasName(base), has(base)), getItemName(set.wall().get()));
            }
            if (set.tool() == BuildingSet.Tool.PICKAXE) {
                stonecut(set.stairs().get(), 1, base);
                stonecut(set.slab().get(), 2, base);
                if (set.wall() != null) {
                    stonecut(set.wall().get(), 1, base);
                }
            }
        }

        // Fachwerk
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TIMBER_BEAM.get(), 4)
                .pattern("L")
                .pattern("L")
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_log", has(ItemTags.LOGS))
                .save(output, key("timber_beam"));
        shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TIMBER_FRAME.get(), 2)
                .requires(ModBlocks.TIMBER_BEAM.get())
                .requires(Blocks.CALCITE)
                .unlockedBy("has_timber_beam", has(ModBlocks.TIMBER_BEAM.get()))
                .save(output, key("timber_frame"));
        // Fachwerk-Muster durchwechseln: gerade -> diagonal -> Kreuz -> gerade
        cycle(ModBlocks.TIMBER_FRAME.get(), ModBlocks.TIMBER_FRAME_DIAGONAL.get(), "timber_frame_diagonal");
        cycle(ModBlocks.TIMBER_FRAME_DIAGONAL.get(), ModBlocks.TIMBER_FRAME_CROSS.get(), "timber_frame_cross");
        cycle(ModBlocks.TIMBER_FRAME_CROSS.get(), ModBlocks.TIMBER_FRAME.get(), "timber_frame_from_cross");
    }

    // =====================================================================================
    // Erze, Rohstoffe, Stahl
    // =====================================================================================

    private void resourceRecipes() {
        smeltOre(ModItems.CARBON.get(), 0.1f, ModBlocks.CARBON_ORE.get(), ModBlocks.DEEPSLATE_CARBON_ORE.get());
        smeltOre(ModItems.GEMSTONE.get(), 1.0f, ModBlocks.GEMSTONE_ORE.get(), ModBlocks.DEEPSLATE_GEMSTONE_ORE.get());
        smeltOre(ModItems.KONTERIT.get(), 0.7f, ModBlocks.KONTERIT_ORE.get(), ModBlocks.DEEPSLATE_KONTERIT_ORE.get());
        smeltOre(ModItems.AETHERIT.get(), 0.7f, ModBlocks.AETHERIT_ORE.get(), ModBlocks.DEEPSLATE_AETHERIT_ORE.get());
        smeltOre(ModItems.DRACONIUM.get(), 1.5f, ModBlocks.DRACONIUM_ORE.get(), ModBlocks.DEEPSLATE_DRACONIUM_ORE.get());
        smeltOre(ModItems.WYVERNIT.get(), 1.0f, ModBlocks.WYVERNIT_ORE.get());

        storage(ModItems.CARBON.get(), ModBlocks.CARBON_BLOCK.get());
        storage(ModItems.GEMSTONE.get(), ModBlocks.GEMSTONE_BLOCK.get());
        storage(ModItems.KONTERIT.get(), ModBlocks.KONTERIT_BLOCK.get());
        storage(ModItems.AETHERIT.get(), ModBlocks.AETHERIT_BLOCK.get());
        storage(ModItems.DRACONIUM.get(), ModBlocks.DRACONIUM_BLOCK.get());
        storage(ModItems.WYVERNIT.get(), ModBlocks.WYVERNIT_BLOCK.get());
        storage(ModItems.STEEL_INGOT.get(), ModBlocks.STEEL_BLOCK.get());

        // Stahl: Eisen + Kohlenstoff mischen, dann im Hochofen härten
        shapeless(RecipeCategory.MISC, ModItems.STEEL_BLEND.get())
                .requires(Items.IRON_INGOT)
                .requires(ModItems.CARBON.get(), 2)
                .unlockedBy("has_carbon", has(ModItems.CARBON.get()))
                .save(output, key("steel_blend"));
        save(SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.STEEL_BLEND.get()), RecipeCategory.MISC,
                        CookingBookCategory.MISC, ModItems.STEEL_INGOT.get(), 0.7f, 200)
                .unlockedBy("has_steel_blend", has(ModItems.STEEL_BLEND.get())), "steel_ingot_from_blasting");
        // Kohlenstoff auch aus Holzkohle (Köhlerei)
        save(SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.CHARCOAL), RecipeCategory.MISC,
                        CookingBookCategory.MISC, ModItems.CARBON.get(), 0.1f, 200)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL)), "carbon_from_charcoal");
    }

    // =====================================================================================
    // Drachen
    // =====================================================================================

    private void dragonRecipes() {
        // Drachenei-Fragment aus vier seltenen Drachenmaterialien
        shapeless(RecipeCategory.MISC, ModItems.DRAGON_EGG_FRAGMENT.get())
                .requires(ModItems.DRACONIUM.get())
                .requires(ModItems.AETHERIT.get())
                .requires(ModItems.KONTERIT.get())
                .requires(ModItems.GEMSTONE.get())
                .unlockedBy("has_draconium", has(ModItems.DRACONIUM.get()))
                .save(output, key("dragon_egg_fragment"));

        // Drachenei: vier Fragmente um das Nest-Material der jeweiligen Art
        for (DragonType type : DragonType.values()) {
            ItemLike egg = ModBlocks.DRAGON_EGGS.get(type).get();
            shaped(RecipeCategory.MISC, egg)
                    .pattern(" F ")
                    .pattern("FNF")
                    .pattern(" F ")
                    .define('F', ModItems.DRAGON_EGG_FRAGMENT.get())
                    .define('N', type.nestMaterial())
                    .unlockedBy("has_dragon_egg_fragment", has(ModItems.DRAGON_EGG_FRAGMENT.get()))
                    .save(output, key(type.id() + "_dragon_egg"));
        }

        // Drachenfutter aus Fleisch und Beeren
        shapeless(RecipeCategory.FOOD, ModItems.DRAGON_FOOD.get(), 2)
                .requires(Items.BEEF)
                .requires(Items.PORKCHOP)
                .requires(Items.MUTTON)
                .requires(Items.SWEET_BERRIES)
                .unlockedBy("has_beef", has(Items.BEEF))
                .save(output, key("dragon_food"));
    }

    // =====================================================================================
    // Nahrung
    // =====================================================================================

    private void foodRecipes() {
        shapeless(RecipeCategory.FOOD, ModItems.TOMATO_SEEDS.get())
                .requires(ModItems.TOMATO.get())
                .unlockedBy("has_tomato", has(ModItems.TOMATO.get()))
                .save(output, key("tomato_seeds"));
        shapeless(RecipeCategory.FOOD, ModItems.CABBAGE_SEEDS.get())
                .requires(ModItems.CABBAGE.get())
                .unlockedBy("has_cabbage", has(ModItems.CABBAGE.get()))
                .save(output, key("cabbage_seeds"));

        shaped(RecipeCategory.FOOD, ModItems.BREAD_ROLL.get(), 2)
                .pattern("BB")
                .define('B', ModItems.BARLEY.get())
                .unlockedBy("has_barley", has(ModItems.BARLEY.get()))
                .save(output, key("bread_roll"));
        shapeless(RecipeCategory.FOOD, ModItems.VEGETABLE_STEW.get())
                .requires(ModItems.TOMATO.get())
                .requires(ModItems.CABBAGE.get())
                .requires(ModItems.ONION.get())
                .requires(Items.CARROT)
                .requires(Items.BOWL)
                .unlockedBy("has_tomato", has(ModItems.TOMATO.get()))
                .save(output, key("vegetable_stew"));
        shapeless(RecipeCategory.FOOD, ModItems.MEAT_PIE.get())
                .requires(Items.COOKED_BEEF)
                .requires(ModItems.ONION.get())
                .requires(Items.WHEAT)
                .requires(ItemTags.EGGS) // alle Eier-Sorten, wie beim Vanilla-Kuchen
                .unlockedBy("has_onion", has(ModItems.ONION.get()))
                .save(output, key("meat_pie"));
        shapeless(RecipeCategory.FOOD, ModItems.APPLE_PIE.get())
                .requires(Items.APPLE, 2)
                .requires(Items.SUGAR)
                .requires(ItemTags.EGGS)
                .requires(Items.WHEAT)
                .unlockedBy("has_apple", has(Items.APPLE))
                .save(output, key("apple_pie"));
    }

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    private static ResourceKey<Recipe<?>> key(String name) {
        return ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name));
    }

    private void save(RecipeBuilder builder, String name) {
        builder.save(output, key(name));
    }

    private void stonecut(ItemLike result, int count, ItemLike... sources) {
        for (ItemLike source : sources) {
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(source), RecipeCategory.BUILDING_BLOCKS, result, count)
                    .unlockedBy(getHasName(source), has(source))
                    .save(output, key(getItemName(result) + "_from_" + getItemName(source) + "_stonecutting"));
        }
    }

    private void cycle(ItemLike from, ItemLike to, String name) {
        shapeless(RecipeCategory.BUILDING_BLOCKS, to)
                .requires(from)
                .unlockedBy(getHasName(from), has(from))
                .save(output, key(name));
    }

    private void smeltOre(ItemLike result, float experience, ItemLike... ores) {
        List<ItemLike> list = List.of(ores);
        for (ItemLike ore : list) {
            save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(ore), RecipeCategory.MISC, CookingBookCategory.MISC,
                            result, experience, 200)
                    .group(getItemName(result))
                    .unlockedBy(getHasName(ore), has(ore)),
                    getItemName(result) + "_from_smelting_" + getItemName(ore));
            save(SimpleCookingRecipeBuilder.blasting(Ingredient.of(ore), RecipeCategory.MISC, CookingBookCategory.MISC,
                            result, experience, 100)
                    .group(getItemName(result))
                    .unlockedBy(getHasName(ore), has(ore)),
                    getItemName(result) + "_from_blasting_" + getItemName(ore));
        }
    }

    private void storage(ItemLike item, ItemLike block) {
        shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', item)
                .unlockedBy(getHasName(item), has(item))
                .save(output, key(getItemName(block)));
        shapeless(RecipeCategory.MISC, item, 9)
                .requires(block)
                .unlockedBy(getHasName(block), has(block))
                .save(output, key(getItemName(item) + "_from_" + getItemName(block)));
    }
}
