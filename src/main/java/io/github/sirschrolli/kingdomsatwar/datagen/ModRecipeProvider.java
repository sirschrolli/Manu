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
import net.minecraft.world.item.Item;
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
        decorRecipes();
        resourceRecipes();
        dragonRecipes();
        foodRecipes();
        armyRecipes();
        equipmentRecipes();
        magicRecipes();
        cookVenison();
    }

    // =====================================================================================
    // Werkzeuge, Waffen und Rüstungen
    // =====================================================================================

    private void equipmentRecipes() {
        // Stahl: Werkzeuge und Rüstung wie aus Eisen, nur aus Stahlbarren
        Item steel = ModItems.STEEL_INGOT.get();
        tools(steel, ModItems.STEEL_SWORD.get(), ModItems.STEEL_PICKAXE.get(), ModItems.STEEL_AXE.get(),
                ModItems.STEEL_SHOVEL.get(), ModItems.STEEL_HOE.get());
        armorSet(steel, ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(), ModItems.STEEL_LEGGINGS.get(),
                ModItems.STEEL_BOOTS.get());
        // Konterit: leichte Rüstung
        armorSet(ModItems.KONTERIT.get(), ModItems.KONTERIT_HELMET.get(), ModItems.KONTERIT_CHESTPLATE.get(),
                ModItems.KONTERIT_LEGGINGS.get(), ModItems.KONTERIT_BOOTS.get());
        // Drachenschuppen: je Art ein Satz
        ModItems.DRAGON_SCALE_ARMOR.forEach((type, set) -> armorSet(ModItems.DRAGON_SCALES.get(type).get(),
                set.get(0).get(), set.get(1).get(), set.get(2).get(), set.get(3).get()));

        // Draconium-Waffen mit Lohenruten als Griff
        Item draconium = ModItems.DRACONIUM.get();
        shaped(RecipeCategory.COMBAT, ModItems.DRACONIUM_SWORD.get())
                .pattern("D")
                .pattern("D")
                .pattern("B")
                .define('D', draconium)
                .define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(draconium), has(draconium))
                .save(output, key("draconium_sword"));
        shaped(RecipeCategory.COMBAT, ModItems.DRACONIUM_AXE.get())
                .pattern("DD")
                .pattern("DB")
                .pattern(" B")
                .define('D', draconium)
                .define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(draconium), has(draconium))
                .save(output, key("draconium_axe"));

        // Wyvernit-Gleiter: Stahlrahmen, Wyvernit-Streben, Phantomhaut als Bespannung
        shaped(RecipeCategory.TRANSPORTATION, ModItems.WYVERNIT_GLIDER.get())
                .pattern("WSW")
                .pattern("M M")
                .pattern("M M")
                .define('W', ModItems.WYVERNIT.get())
                .define('S', steel)
                .define('M', Items.PHANTOM_MEMBRANE)
                .unlockedBy(getHasName(ModItems.WYVERNIT.get()), has(ModItems.WYVERNIT.get()))
                .save(output, key("wyvernit_glider"));

        // Königskrone: Gold mit Edelsteinen
        shaped(RecipeCategory.COMBAT, ModItems.GEMSTONE_CROWN.get())
                .pattern("EGE")
                .pattern("GGG")
                .define('E', ModItems.GEMSTONE.get())
                .define('G', Items.GOLD_INGOT)
                .unlockedBy(getHasName(ModItems.GEMSTONE.get()), has(ModItems.GEMSTONE.get()))
                .save(output, key("gemstone_crown"));

        // Drachenpanzer: Stahl (mit Stahlblock), Draconium als Aufwertung des Stahlpanzers
        shaped(RecipeCategory.COMBAT, ModItems.STEEL_DRAGON_ARMOR.get())
                .pattern("S S")
                .pattern("SBS")
                .pattern("SSS")
                .define('S', steel)
                .define('B', ModBlocks.STEEL_BLOCK.get())
                .unlockedBy(getHasName(steel), has(steel))
                .save(output, key("steel_dragon_armor"));
        shaped(RecipeCategory.COMBAT, ModItems.DRACONIUM_DRAGON_ARMOR.get())
                .pattern("D D")
                .pattern("DAD")
                .pattern("DDD")
                .define('D', draconium)
                .define('A', ModItems.STEEL_DRAGON_ARMOR.get())
                .unlockedBy(getHasName(ModItems.STEEL_DRAGON_ARMOR.get()), has(ModItems.STEEL_DRAGON_ARMOR.get()))
                .save(output, key("draconium_dragon_armor"));
    }

    // =====================================================================================
    // Magie
    // =====================================================================================

    private void magicRecipes() {
        Item aetherit = ModItems.AETHERIT.get();
        Item dust = ModItems.WISP_DUST.get();
        shaped(RecipeCategory.COMBAT, ModItems.AETHERIT_STAFF.get())
                .pattern(" WA")
                .pattern(" SW")
                .pattern("S  ")
                .define('A', aetherit)
                .define('W', dust)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(aetherit), has(aetherit))
                .save(output, key("aetherit_staff"));
        // Drachenkompass: Kompass, gefasst in Edelsteine, mit Draconium (Essenz der Drachen) und Aetherit (Magie)
        shaped(RecipeCategory.TOOLS, ModItems.DRAGON_COMPASS.get())
                .pattern(" D ")
                .pattern("GCG")
                .pattern(" A ")
                .define('D', ModItems.DRACONIUM.get())
                .define('G', ModItems.GEMSTONE.get())
                .define('C', Items.COMPASS)
                .define('A', aetherit)
                .unlockedBy(getHasName(ModItems.DRACONIUM.get()), has(ModItems.DRACONIUM.get()))
                .save(output, key("dragon_compass"));
        // Runen: Schieferplatte, Aetherit, Irrlichtstaub und eine Zutat des Elements
        java.util.Map<DragonType, ItemLike> essence = new java.util.EnumMap<>(DragonType.class);
        essence.put(DragonType.FIRE, Items.BLAZE_POWDER);
        essence.put(DragonType.ICE, Items.PACKED_ICE);
        essence.put(DragonType.LIGHTNING, Items.COPPER_INGOT);
        essence.put(DragonType.NATURE, Items.MOSS_BLOCK);
        essence.put(DragonType.EARTH, Items.RAW_IRON);
        essence.put(DragonType.WATER, Items.PRISMARINE_SHARD);
        ModItems.RUNES.forEach((type, rune) -> shapeless(RecipeCategory.COMBAT, rune.get())
                .requires(Items.POLISHED_DEEPSLATE)
                .requires(aetherit)
                .requires(dust)
                .requires(essence.get(type))
                .unlockedBy(getHasName(dust), has(dust))
                .save(output, key(type.id() + "_rune")));
    }

    private void cookVenison() {
        Item raw = ModItems.VENISON.get();
        Item cooked = ModItems.COOKED_VENISON.get();
        save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(raw), RecipeCategory.FOOD, CookingBookCategory.FOOD, cooked, 0.35f, 200)
                .unlockedBy(getHasName(raw), has(raw)), "cooked_venison");
        save(SimpleCookingRecipeBuilder.smoking(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35f, 100)
                .unlockedBy(getHasName(raw), has(raw)), "cooked_venison_from_smoking");
        save(SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35f, 600)
                .unlockedBy(getHasName(raw), has(raw)), "cooked_venison_from_campfire_cooking");
    }

    private void tools(Item material, Item sword, Item pickaxe, Item axe, Item shovel, Item hoe) {
        String has = getHasName(material);
        shaped(RecipeCategory.COMBAT, sword).pattern("M").pattern("M").pattern("S")
                .define('M', material).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(sword)));
        shaped(RecipeCategory.TOOLS, pickaxe).pattern("MMM").pattern(" S ").pattern(" S ")
                .define('M', material).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(pickaxe)));
        shaped(RecipeCategory.TOOLS, axe).pattern("MM").pattern("MS").pattern(" S")
                .define('M', material).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(axe)));
        shaped(RecipeCategory.TOOLS, shovel).pattern("M").pattern("S").pattern("S")
                .define('M', material).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(shovel)));
        shaped(RecipeCategory.TOOLS, hoe).pattern("MM").pattern(" S").pattern(" S")
                .define('M', material).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(hoe)));
    }

    private void armorSet(Item material, Item helmet, Item chestplate, Item leggings, Item boots) {
        String has = getHasName(material);
        shaped(RecipeCategory.COMBAT, helmet).pattern("MMM").pattern("M M")
                .define('M', material).unlockedBy(has, has(material)).save(output, key(getItemName(helmet)));
        shaped(RecipeCategory.COMBAT, chestplate).pattern("M M").pattern("MMM").pattern("MMM")
                .define('M', material).unlockedBy(has, has(material)).save(output, key(getItemName(chestplate)));
        shaped(RecipeCategory.COMBAT, leggings).pattern("MMM").pattern("M M").pattern("M M")
                .define('M', material).unlockedBy(has, has(material)).save(output, key(getItemName(leggings)));
        shaped(RecipeCategory.COMBAT, boots).pattern("M M").pattern("M M")
                .define('M', material).unlockedBy(has, has(material)).save(output, key(getItemName(boots)));
    }

    // =====================================================================================
    // Armee
    // =====================================================================================

    private void armyRecipes() {
        // Rekrutierungstisch: Buch und Smaragde auf einem Holztisch
        shaped(RecipeCategory.DECORATIONS, ModBlocks.RECRUITMENT_TABLE.get())
                .pattern("EBE")
                .pattern("PPP")
                .pattern("S S")
                .define('E', Items.EMERALD)
                .define('B', Items.BOOK)
                .define('P', ItemTags.PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(output, key("recruitment_table"));
        // Kommandostab: Goldknauf, blaues Band, Stab
        shaped(RecipeCategory.TOOLS, ModItems.COMMAND_BATON.get())
                .pattern(" G")
                .pattern("DS")
                .pattern("S ")
                .define('G', Items.GOLD_INGOT)
                .define('D', Items.DYE.blue())
                .define('S', Items.STICK)
                .unlockedBy("has_recruitment_table", has(ModBlocks.RECRUITMENT_TABLE.get()))
                .save(output, key("command_baton"));
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
    // Einrichtung: Putz, Fenster, Türen, Licht, Zinnen, Möbel
    // =====================================================================================

    private void decorRecipes() {
        // Putz: Kalkputz aus Sand, Ton und Knochenmehl (Kalk), Lehmputz aus Ton, Erde und Stroh
        shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.WHITE_PLASTER.base().get(), 4)
                .requires(Items.SAND, 2)
                .requires(Items.CLAY_BALL)
                .requires(Items.BONE_MEAL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(output, key("white_plaster"));
        stonecut(ModBlocks.WHITE_PLASTER.base().get(), 1, Blocks.CALCITE);
        shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CLAY_PLASTER.base().get(), 4)
                .requires(Items.CLAY_BALL, 2)
                .requires(Items.DIRT)
                .requires(Items.WHEAT)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(output, key("clay_plaster"));

        // Bleiglas: acht Glasblöcke mit Eisen als "Blei", daraus Scheiben
        Item leadedGlass = ModBlocks.LEADED_GLASS.get().asItem();
        shaped(RecipeCategory.BUILDING_BLOCKS, leadedGlass, 8)
                .pattern("GGG")
                .pattern("GNG")
                .pattern("GGG")
                .define('G', Items.GLASS)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(output, key("leaded_glass"));
        shaped(RecipeCategory.DECORATIONS, ModBlocks.LEADED_GLASS_PANE.get(), 16)
                .pattern("GGG")
                .pattern("GGG")
                .define('G', leadedGlass)
                .unlockedBy(getHasName(leadedGlass), has(leadedGlass))
                .save(output, key("leaded_glass_pane"));

        // Burgtür: Bretter mit einem eisernen Band
        shaped(RecipeCategory.REDSTONE, ModBlocks.CASTLE_DOOR.get(), 2)
                .pattern("PP")
                .pattern("II")
                .pattern("PP")
                .define('P', ItemTags.PLANKS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output, key("castle_door"));
        // Fensterläden: wie eine Falltür, mit Farbstoff
        shutters(ModBlocks.GREEN_SHUTTERS.get(), Items.DYE.green());
        shutters(ModBlocks.RED_SHUTTERS.get(), Items.DYE.red());

        // Licht
        Item steel = ModItems.STEEL_INGOT.get();
        shaped(RecipeCategory.DECORATIONS, ModBlocks.STEEL_LANTERN.get(), 2)
                .pattern(" S ")
                .pattern("STS")
                .pattern(" S ")
                .define('S', steel)
                .define('T', Items.TORCH)
                .unlockedBy(getHasName(steel), has(steel))
                .save(output, key("steel_lantern"));
        Item aetherit = ModItems.AETHERIT.get();
        shaped(RecipeCategory.DECORATIONS, ModBlocks.AETHERIT_LANTERN.get())
                .pattern("NNN")
                .pattern("NAN")
                .pattern("NNN")
                .define('N', Items.IRON_NUGGET)
                .define('A', aetherit)
                .unlockedBy(getHasName(aetherit), has(aetherit))
                .save(output, key("aetherit_lantern"));
        shaped(RecipeCategory.DECORATIONS, ModBlocks.CHANDELIER.get())
                .pattern("CHC")
                .pattern("IGI")
                .define('C', Items.CANDLE)
                .define('H', Items.IRON_CHAIN)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_candle", has(Items.CANDLE))
                .save(output, key("chandelier"));

        // Zinnen: im Steinmetz oder an der Werkbank aus dem Baustoff
        ModBlocks.BATTLEMENTS.forEach((battlement, material) -> {
            ItemLike stone = material.base().get();
            shaped(RecipeCategory.BUILDING_BLOCKS, battlement.get(), 4)
                    .pattern("S S")
                    .pattern("SSS")
                    .define('S', stone)
                    .unlockedBy(getHasName(stone), has(stone))
                    .save(output, key(getItemName(battlement.get())));
            stonecut(battlement.get(), 1, stone);
        });

        // Möbel
        shaped(RecipeCategory.DECORATIONS, ModBlocks.OAK_CHAIR.get(), 2)
                .pattern("P  ")
                .pattern("PPP")
                .pattern("S S")
                .define('P', Items.OAK_PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                .save(output, key("oak_chair"));
        shaped(RecipeCategory.DECORATIONS, ModBlocks.OAK_BENCH.get(), 2)
                .pattern("PPP")
                .pattern("PPP")
                .pattern("S S")
                .define('P', Items.OAK_PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                .save(output, key("oak_bench"));
        shaped(RecipeCategory.DECORATIONS, ModBlocks.OAK_TABLE.get())
                .pattern("PPP")
                .pattern("S S")
                .pattern("S S")
                .define('P', Items.OAK_PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                .save(output, key("oak_table"));
        shaped(RecipeCategory.DECORATIONS, ModBlocks.THRONE.get())
                .pattern("GWG")
                .pattern("PWP")
                .pattern("PPP")
                .define('G', Items.GOLD_INGOT)
                .define('W', Items.WOOL.red())
                .define('P', Items.DARK_OAK_PLANKS)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output, key("throne"));
    }

    private void shutters(ItemLike shutters, ItemLike dye) {
        shaped(RecipeCategory.REDSTONE, shutters, 2)
                .pattern("PPP")
                .pattern("PDP")
                .define('P', ItemTags.PLANKS)
                .define('D', dye)
                .unlockedBy(getHasName(dye), has(dye))
                .save(output, key(getItemName(shutters)));
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

        // Knoblauchbrot, Ale, Wein und Met
        Item garlic = ModItems.GARLIC.get();
        Item hops = ModItems.HOPS.get();
        shapeless(RecipeCategory.FOOD, ModItems.GARLIC_BREAD.get())
                .requires(Items.BREAD)
                .requires(garlic)
                .unlockedBy(getHasName(garlic), has(garlic))
                .save(output, key("garlic_bread"));
        shapeless(RecipeCategory.FOOD, ModItems.ALE.get())
                .requires(ModItems.BARLEY.get(), 2)
                .requires(hops)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy(getHasName(hops), has(hops))
                .save(output, key("ale"));
        shapeless(RecipeCategory.FOOD, ModItems.WINE.get())
                .requires(ModItems.GRAPES.get(), 3)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy(getHasName(ModItems.GRAPES.get()), has(ModItems.GRAPES.get()))
                .save(output, key("wine"));
        shapeless(RecipeCategory.FOOD, ModItems.MEAD.get())
                .requires(Items.HONEYCOMB, 2)
                .requires(hops)
                .requires(Items.GLASS_BOTTLE)
                .unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
                .save(output, key("mead"));

        // Festmahl: Braten, Geflügel und Wildbret mit Beilagen auf einem Holzbrett
        shapeless(RecipeCategory.FOOD, ModBlocks.FEAST.get())
                .requires(Items.COOKED_PORKCHOP)
                .requires(ModItems.COOKED_VENISON.get())
                .requires(Items.COOKED_CHICKEN)
                .requires(Items.BAKED_POTATO, 2)
                .requires(Items.CARROT)
                .requires(garlic)
                .requires(Items.APPLE)
                .requires(ItemTags.WOODEN_SLABS)
                .unlockedBy(getHasName(ModItems.COOKED_VENISON.get()), has(ModItems.COOKED_VENISON.get()))
                .save(output, key("feast"));
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
