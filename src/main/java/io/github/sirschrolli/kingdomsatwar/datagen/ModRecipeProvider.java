package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.registry.StoneChains;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
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
        siegeRecipes();
        equipmentRecipes();
        magicRecipes();
        cookVenison();
        sea();
        fishing();
        relics();
        shadowRecipes();
        voidRecipes();
        shadowTreasureRecipes();
        faeRecipes();
        monsterGearRecipes();
        armoryRecipes();
        creatorRecipes();
        naturalChains();
    }

    // =====================================================================================
    // Schrolli der Schöpfer (0.75)
    // =====================================================================================

    private void creatorRecipes() {
        Item spark = io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SPARK.get();
        // der Schöpferfunke: aus den Trophäen aller großen Gegner um einen Netherstern
        Item heart = ModItems.HEART_CRYSTAL.get();
        shaped(RecipeCategory.MISC, spark)
                .pattern("HSH").pattern("GNV").pattern("FWM")
                .define('H', heart)
                .define('S', io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD.get())
                .define('G', io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.GLITCH_SHARD.get())
                .define('N', Items.NETHER_STAR)
                .define('V', io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get())
                .define('F', io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_DUST.get())
                .define('W', io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SEAM.get())
                .define('M', io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.MOLTEN_GLOB.get())
                .unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR)).save(output, key("creator_spark"));
        // der Altar: Gold, Quarz, ein Diamantblock und ein Totem
        shaped(RecipeCategory.MISC, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_ALTAR.get())
                .pattern("GTG").pattern("QDQ").pattern("QQQ")
                .define('G', Items.GOLD_BLOCK).define('T', Items.TOTEM_OF_UNDYING).define('D', Items.DIAMOND_BLOCK)
                .define('Q', Items.CHISELED_QUARTZ_BLOCK)
                .unlockedBy(getHasName(spark), has(spark)).save(output, key("creator_altar"));
    }

    // =====================================================================================
    // Waffen und Rüstungen aus Monsterteilen (0.70)
    // =====================================================================================

    // =====================================================================================
    // Die Waffenkammer (0.84)
    // =====================================================================================

    private void armoryRecipes() {
        Item steel = ModItems.STEEL_INGOT.get();
        Item steelBlock = ModBlocks.STEEL_BLOCK.get().asItem();
        Item draconium = ModItems.DRACONIUM.get();
        Item nightsteel = io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_INGOT.get();
        Item abyssite = io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_INGOT.get();
        // Stahl-Hellebarde: Axtblatt und Stoßklinge aus Stahl an einer langen Stange
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_HALBERD.get())
                .pattern(" SS").pattern(" RS").pattern("R  ")
                .define('S', steel).define('R', Items.STICK)
                .unlockedBy(getHasName(steel), has(steel)).save(output, key("steel_halberd"));
        // Draconium-Kriegshammer: ein Draconiumblock mit Goldkappen auf Lohenruten
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.DRACONIUM_WARHAMMER.get())
                .pattern("DGD").pattern("DBD").pattern(" B ")
                .define('D', draconium).define('G', Items.GOLD_INGOT).define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(draconium), has(draconium)).save(output, key("draconium_warhammer"));
        // Nachtstahl-Dolch: eine kurze Klinge, ein Lederheft
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.NIGHTSTEEL_DAGGER.get())
                .pattern(" N").pattern("L ")
                .define('N', nightsteel).define('L', Items.LEATHER)
                .unlockedBy(getHasName(nightsteel), has(nightsteel)).save(output, key("nightsteel_dagger"));
        // Abyssit-Großschwert: eine Klinge aus Abyssit, die Parierstange aus Nachtstahl
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.ABYSSITE_GREATSWORD.get())
                .pattern(" A ").pattern(" A ").pattern("NRN")
                .define('A', abyssite).define('N', nightsteel).define('R', Items.STICK)
                .unlockedBy(getHasName(abyssite), has(abyssite)).save(output, key("abyssite_greatsword"));
        // Stahl-Doppelaxt: zwei Blätter beiderseits des Stiels
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_DOUBLE_AXE.get())
                .pattern("SRS").pattern("SRS").pattern(" R ")
                .define('S', steel).define('R', Items.STICK)
                .unlockedBy(getHasName(steel), has(steel)).save(output, key("steel_double_axe"));
        // Bergmannshammer: ein Stahlblock zwischen zwei Barren, auf einem kräftigen Stiel
        shaped(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.Armory.MINER_HAMMER.get())
                .pattern("SBS").pattern(" R ").pattern(" R ")
                .define('S', steel).define('B', steelBlock).define('R', Items.STICK)
                .unlockedBy(getHasName(steel), has(steel)).save(output, key("miner_hammer"));
        // Glutspitzhacke: eine Draconium-Picke auf Lohenruten
        shaped(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.Armory.EMBER_PICKAXE.get())
                .pattern("DDD").pattern(" B ").pattern(" B ")
                .define('D', draconium).define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(draconium), has(draconium)).save(output, key("ember_pickaxe"));
        // Adernbrecher: Nachtstahl mit Amethyst, der den Adern folgt
        shaped(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.Armory.VEIN_BREAKER.get())
                .pattern("NNN").pattern("ARA").pattern(" R ")
                .define('N', nightsteel).define('A', Items.AMETHYST_SHARD).define('R', Items.STICK)
                .unlockedBy(getHasName(nightsteel), has(nightsteel)).save(output, key("vein_breaker"));
        // Erzspürer: Abyssit um ein Enderauge
        shaped(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.Armory.ORE_SEEKER.get())
                .pattern("AEA").pattern(" R ").pattern(" R ")
                .define('A', abyssite).define('E', Items.ENDER_EYE).define('R', Items.STICK)
                .unlockedBy(getHasName(abyssite), has(abyssite)).save(output, key("ore_seeker"));
        // Stahl-Rundschild: Stahl um eine Holzscheibe
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_BUCKLER.get())
                .pattern(" S ").pattern("SPS").pattern(" S ")
                .define('S', steel).define('P', net.minecraft.tags.ItemTags.PLANKS)
                .unlockedBy(getHasName(steel), has(steel)).save(output, key("steel_buckler"));
        // Drachenschild: ein Schild, mit Draconium beschlagen, die Maske aus Gold
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.DRAGON_SHIELD.get())
                .pattern("DGD").pattern("DSD").pattern(" D ")
                .define('D', draconium).define('G', Items.GOLD_INGOT).define('S', Items.SHIELD)
                .unlockedBy(getHasName(draconium), has(draconium)).save(output, key("dragon_shield"));
        // Turmschild: ein Schild, mit Nachtstahl verlängert und beschlagen
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.TOWER_SHIELD.get())
                .pattern("NNN").pattern("NSN").pattern(" N ")
                .define('N', nightsteel).define('S', Items.SHIELD)
                .unlockedBy(getHasName(nightsteel), has(nightsteel)).save(output, key("tower_shield"));
        // Spiegelschild: poliertes Abyssit (getöntes Glas) auf einem Schild
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.Armory.MIRROR_SHIELD.get())
                .pattern("AGA").pattern("ASA").pattern(" A ")
                .define('A', abyssite).define('G', Items.TINTED_GLASS).define('S', Items.SHIELD)
                .unlockedBy(getHasName(abyssite), has(abyssite)).save(output, key("mirror_shield"));
    }

    private void monsterGearRecipes() {
        Item claw = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.WEREWOLF_CLAW.get();
        Item pelt = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.WEREWOLF_PELT.get();
        Item root = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_ROOT.get();
        Item heart = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_HEART.get();
        Item scale = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_SCALE.get();
        Item fang = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_FANG.get();
        Item ash = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.SOUL_ASH.get();
        Item veil = io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.ASH_VEIL.get();
        // Krallenklinge: drei Klauen, mit Pelz an einen eisernen Faustgriff gebunden
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.CLAW_BLADE.get())
                .pattern("CCC").pattern(" P ").pattern(" I ")
                .define('C', claw).define('P', pelt).define('I', Items.IRON_INGOT)
                .unlockedBy(getHasName(claw), has(claw)).save(output, key("claw_blade"));
        // Moorkeule: Wurzeln um ein Moorherz geflochten
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_CLUB.get())
                .pattern(" HR").pattern(" RR").pattern("R  ")
                .define('H', heart).define('R', root)
                .unlockedBy(getHasName(heart), has(heart)).save(output, key("bog_club"));
        // Glutzahn-Klinge: zwei Fangzähne, Schuppen als Parierstange, eine Lohenrute als Griff
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_FANG_BLADE.get())
                .pattern(" F ").pattern("SFS").pattern(" B ")
                .define('F', fang).define('S', scale).define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(fang), has(fang)).save(output, key("ember_fang_blade"));
        // Seelensense: Seelenasche als Klinge, ein Aschenschleier, Knochen als Schaft
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.SOUL_SCYTHE.get())
                .pattern("AAV").pattern(" BA").pattern("B  ")
                .define('A', ash).define('V', veil).define('B', Items.BONE)
                .unlockedBy(getHasName(veil), has(veil)).save(output, key("soul_scythe"));
        // Werwolfspelz-Rüstung: Pelz auf Leder
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_HELMET.get()).pattern("LPL").pattern("L L")
                .define('P', pelt).define('L', Items.LEATHER).unlockedBy(getHasName(pelt), has(pelt)).save(output, key("werewolf_pelt_helmet"));
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_CHESTPLATE.get()).pattern("P P").pattern("LPL").pattern("LLL")
                .define('P', pelt).define('L', Items.LEATHER).unlockedBy(getHasName(pelt), has(pelt)).save(output, key("werewolf_pelt_chestplate"));
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_LEGGINGS.get()).pattern("LPL").pattern("L L").pattern("L L")
                .define('P', pelt).define('L', Items.LEATHER).unlockedBy(getHasName(pelt), has(pelt)).save(output, key("werewolf_pelt_leggings"));
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_BOOTS.get()).pattern("P P").pattern("L L")
                .define('P', pelt).define('L', Items.LEATHER).unlockedBy(getHasName(pelt), has(pelt)).save(output, key("werewolf_pelt_boots"));
        // Glutwurm-Panzer: Schuppen wie bei jeder Rüstung
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_HELMET.get()).pattern("SSS").pattern("S S")
                .define('S', scale).unlockedBy(getHasName(scale), has(scale)).save(output, key("ember_scale_helmet"));
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_CHESTPLATE.get()).pattern("S S").pattern("SSS").pattern("SSS")
                .define('S', scale).unlockedBy(getHasName(scale), has(scale)).save(output, key("ember_scale_chestplate"));
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_LEGGINGS.get()).pattern("SSS").pattern("S S").pattern("S S")
                .define('S', scale).unlockedBy(getHasName(scale), has(scale)).save(output, key("ember_scale_leggings"));
        shaped(RecipeCategory.COMBAT, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_BOOTS.get()).pattern("S S").pattern("S S")
                .define('S', scale).unlockedBy(getHasName(scale), has(scale)).save(output, key("ember_scale_boots"));
    }

    // =====================================================================================
    // Natürliche Stoffe: dieselben Ketten wie in Vanilla
    // =====================================================================================

    /**
     * Bruchstein wird im Ofen wieder zu glattem Fels (die Werkzeuge, Öfen und Braustände kommen über die Vanilla-Tags
     * dazu), Blumen geben Farbstoff, Leuchtpilze kommen in die Pilzsuppe, Bernsteindrusen ergeben Bernsteinblöcke,
     * Sturmkristalle laden Sturmstein auf, Magmacreme bringt Vulkangestein zum Glühen, Schilf gibt Papier und Fulgurit
     * schmilzt zu Glas.
     */
    private void naturalChains() {
        for (StoneChains.Chain chain : StoneChains.all()) {
            ItemLike cobble = chain.cobble().base().get();
            ItemLike stone = chain.stone().get();
            save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(cobble), RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, stone,
                    0.1f, 200).unlockedBy(getHasName(cobble), has(cobble)), getItemName(stone) + "_from_smelting");
        }
        dye(ModBlocks.LAVENDER.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.PURPLE), 1);
        dye(ModBlocks.GARDEN_ROSE.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.RED), 1);
        dye(ModBlocks.FIRE_LILY.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.ORANGE), 1);
        dye(ModBlocks.FROST_FLOWER.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE), 1);
        dye(ModBlocks.MOONFLOWER.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.WHITE), 1);
        dye(ModBlocks.GROVE_BELL.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.BLUE), 1);
        dye(ModBlocks.LIFE_BLOSSOM.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.PINK), 1);
        dye(ModBlocks.GLOW_CORAL.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.CYAN), 2);
        dye(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.STAR_BLOSSOM.get(), Items.DYE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE), 2);
        // Pilzsuppe auch mit Leuchtpilzen (einer davon muss ein Leuchtpilz sein)
        ItemLike glowcap = ModBlocks.GLOWCAP.get();
        ItemLike ghostcap = ShadowBlocks.GHOSTCAP.get();
        shapeless(RecipeCategory.FOOD, Items.MUSHROOM_STEW)
                .requires(Ingredient.of(glowcap, ghostcap))
                .requires(Ingredient.of(glowcap, ghostcap, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM))
                .requires(Items.BOWL)
                .unlockedBy(getHasName(glowcap), has(glowcap))
                .save(output, key("mushroom_stew_from_glowcaps"));
        // vier Bernsteindrusen zu einem Bernsteinblock (wie Amethystsplitter)
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.AMBER_CRYSTAL.get())
                .pattern("AA")
                .pattern("AA")
                .define('A', ModBlocks.AMBER_CLUSTER.get())
                .unlockedBy(getHasName(ModBlocks.AMBER_CLUSTER.get()), has(ModBlocks.AMBER_CLUSTER.get()))
                .save(output, key("amber_crystal"));
        // geladener Sturmstein: Sturmstein und ein Sturmkristall
        shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARGED_STORMSTONE.get())
                .requires(ModBlocks.STORMSTONE.base().get())
                .requires(ModBlocks.STORM_CRYSTAL.get())
                .unlockedBy(getHasName(ModBlocks.STORM_CRYSTAL.get()), has(ModBlocks.STORM_CRYSTAL.get()))
                .save(output, key("charged_stormstone"));
        // glühendes Vulkangestein: Vulkangestein und Magmacreme (wie der Magmablock)
        shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOWING_VOLCANIC_ROCK.get())
                .requires(ModBlocks.VOLCANIC_ROCK.base().get())
                .requires(Items.MAGMA_CREAM)
                .unlockedBy(getHasName(Items.MAGMA_CREAM), has(Items.MAGMA_CREAM))
                .save(output, key("glowing_volcanic_rock"));
        // Schilf gibt Papier (wie Zuckerrohr)
        shaped(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("RRR")
                .define('R', ModBlocks.REEDS.get())
                .group("paper")
                .unlockedBy(getHasName(ModBlocks.REEDS.get()), has(ModBlocks.REEDS.get()))
                .save(output, key("paper_from_reeds"));
        // Fulgurit ist vom Blitz geschmolzener Sand: im Ofen wird er zu Glas
        save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.FULGURITE.get()), RecipeCategory.BUILDING_BLOCKS,
                CookingBookCategory.BLOCKS, Blocks.GLASS, 0.1f, 200).unlockedBy(getHasName(ModBlocks.FULGURITE.get()),
                has(ModBlocks.FULGURITE.get())), "glass_from_fulgurite");
    }

    /** Farbstoff aus einer Blume (oder Koralle). */
    private void dye(ItemLike source, Item dye, int count) {
        shapeless(RecipeCategory.MISC, dye, count)
                .requires(source)
                .group(getItemName(dye))
                .unlockedBy(getHasName(source), has(source))
                .save(output, key(getItemName(dye) + "_from_" + getItemName(source)));
    }

    // =====================================================================================
    // Werkzeuge, Waffen und Rüstungen
    // =====================================================================================

    private void equipmentRecipes() {
        // Beschlagener Griff: zwei Stöcke, mit Leder umwickelt und mit einer eisernen Zwinge beschlagen
        Item handle = ModItems.REINFORCED_HANDLE.get();
        shapeless(RecipeCategory.MISC, handle)
                .requires(Items.STICK, 2)
                .requires(Items.LEATHER)
                .requires(Items.IRON_NUGGET)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(output, key("reinforced_handle"));
        // Stahl: Klingen und Köpfe aus Stahlbarren am beschlagenen Griff; Rüstung mit Lederpolster
        Item steel = ModItems.STEEL_INGOT.get();
        tools(steel, ModItems.STEEL_SWORD.get(), ModItems.STEEL_PICKAXE.get(), ModItems.STEEL_AXE.get(),
                ModItems.STEEL_SHOVEL.get(), ModItems.STEEL_HOE.get());
        armorSet(steel, ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(), ModItems.STEEL_LEGGINGS.get(),
                ModItems.STEEL_BOOTS.get());
        // Konterit: leichte Rüstung, ebenfalls gepolstert
        armorSet(ModItems.KONTERIT.get(), ModItems.KONTERIT_HELMET.get(), ModItems.KONTERIT_CHESTPLATE.get(),
                ModItems.KONTERIT_LEGGINGS.get(), ModItems.KONTERIT_BOOTS.get());
        // Drachenschuppen: auf die Stahlrüstung genietet – je Art ein Satz
        ModItems.DRAGON_SCALE_ARMOR.forEach((type, set) -> scaleArmor(ModItems.DRAGON_SCALES.get(type).get(),
                set.get(0).get(), set.get(1).get(), set.get(2).get(), set.get(3).get()));

        // Draconium-Waffen: Lohenrute als Griff, Parierstange bzw. Beschlag aus Stahl
        Item draconium = ModItems.DRACONIUM.get();
        shaped(RecipeCategory.COMBAT, ModItems.DRACONIUM_SWORD.get())
                .pattern(" D ")
                .pattern(" D ")
                .pattern("SBS")
                .define('D', draconium)
                .define('S', steel)
                .define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(draconium), has(draconium))
                .save(output, key("draconium_sword"));
        shaped(RecipeCategory.COMBAT, ModItems.DRACONIUM_AXE.get())
                .pattern("DDS")
                .pattern("DB ")
                .pattern(" B ")
                .define('D', draconium)
                .define('S', steel)
                .define('B', Items.BLAZE_ROD)
                .unlockedBy(getHasName(draconium), has(draconium))
                .save(output, key("draconium_axe"));

        // Wyvernit-Gleiter: Stahlrahmen, Wyvernit-Streben, Phantomhaut als Bespannung, Ledergurt
        shaped(RecipeCategory.TRANSPORTATION, ModItems.WYVERNIT_GLIDER.get())
                .pattern("WSW")
                .pattern("MLM")
                .pattern("M M")
                .define('W', ModItems.WYVERNIT.get())
                .define('S', steel)
                .define('L', Items.LEATHER)
                .define('M', Items.PHANTOM_MEMBRANE)
                .unlockedBy(getHasName(ModItems.WYVERNIT.get()), has(ModItems.WYVERNIT.get()))
                .save(output, key("wyvernit_glider"));

        // Königskrone: Edelsteine auf einem Reif aus Gold
        shaped(RecipeCategory.COMBAT, ModItems.GEMSTONE_CROWN.get())
                .pattern("EGE")
                .pattern("GBG")
                .define('E', ModItems.GEMSTONE.get())
                .define('G', Items.GOLD_INGOT)
                .define('B', Items.GOLD_BLOCK)
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
        // Aetherkern: Aetherit und Irrlichtstaub um die Essenz eines Drachen (Draconium) – das Herz des Stabs
        Item draconium = ModItems.DRACONIUM.get();
        shaped(RecipeCategory.MISC, ModItems.AETHER_CORE.get())
                .pattern("WAW")
                .pattern("ADA")
                .pattern("WAW")
                .define('A', aetherit)
                .define('W', dust)
                .define('D', draconium)
                .unlockedBy(getHasName(draconium), has(draconium))
                .save(output, key("aether_core"));
        // Aetherit-Stab: der Kern in einer Fassung aus Stahl auf zwei beschlagenen Griffen (nur so zu bekommen)
        Item core = ModItems.AETHER_CORE.get();
        shaped(RecipeCategory.COMBAT, ModItems.AETHERIT_STAFF.get())
                .pattern(" SK")
                .pattern(" HS")
                .pattern("H  ")
                .define('K', core)
                .define('S', ModItems.STEEL_INGOT.get())
                .define('H', ModItems.REINFORCED_HANDLE.get())
                .unlockedBy(getHasName(core), has(core))
                .save(output, key("aetherit_staff"));
        // Magietisch: Buch und Aetherit auf Gold und Obsidian, darunter polierter Tiefenschiefer (wie ein Zaubertisch der Erzmagier)
        shaped(RecipeCategory.DECORATIONS, ModBlocks.MAGIC_TABLE.get())
                .pattern("ABA")
                .pattern("GOG")
                .pattern("DDD")
                .define('A', aetherit)
                .define('B', Items.BOOK)
                .define('G', Items.GOLD_INGOT)
                .define('O', Items.OBSIDIAN)
                .define('D', Items.POLISHED_DEEPSLATE)
                .unlockedBy(getHasName(aetherit), has(aetherit))
                .save(output, key("magic_table"));
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
        // Chronik des Siegels (falls verloren): Buch und Gold
        shapeless(RecipeCategory.MISC, ModItems.CHRONICLE.get())
                .requires(Items.BOOK)
                .requires(Items.GOLD_INGOT)
                .unlockedBy("has_book", has(Items.BOOK))
                .save(output, key("chronicle"));
        sealstoneRecipes();
        goldLeafRecipe();
        // Runen werden nicht mehr hergestellt: Zauber lehren jetzt die Zaubertafeln der Erzmagier (am Magietisch)
    }

    /**
     * Siegelstein: aus Tiefenschiefer-Bruchstein und einer Amethystscherbe (die Hüter mischten Kristallstaub in ihren
     * Stein); poliert, als Ziegel, gemeißelt, rissig (im Ofen) und bemoost.
     */
    /** Blattgold: acht Stein um einen Goldbarren (ein Barren reicht für acht Blöcke – nur zur Zierde, kein Gold zurück). */
    private void goldLeafRecipe() {
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GOLD_LEAF_BLOCK.get(), 8)
                .pattern("SSS")
                .pattern("SGS")
                .pattern("SSS")
                .define('S', Items.STONE)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output, key("gold_leaf_block"));
    }

    private void sealstoneRecipes() {
        ItemLike sealstone = ModBlocks.SEALSTONE.base().get();
        ItemLike bricks = ModBlocks.SEALSTONE_BRICKS.base().get();
        ItemLike polished = ModBlocks.POLISHED_SEALSTONE.get();
        shaped(RecipeCategory.BUILDING_BLOCKS, sealstone, 8)
                .pattern("DDD")
                .pattern("DAD")
                .pattern("DDD")
                .define('D', Items.COBBLED_DEEPSLATE)
                .define('A', Items.AMETHYST_SHARD)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output, key("sealstone"));
        shaped(RecipeCategory.BUILDING_BLOCKS, polished, 4)
                .pattern("SS")
                .pattern("SS")
                .define('S', sealstone)
                .unlockedBy(getHasName(sealstone), has(sealstone))
                .save(output, key("polished_sealstone"));
        shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
                .pattern("PP")
                .pattern("PP")
                .define('P', polished)
                .unlockedBy(getHasName(polished), has(polished))
                .save(output, key("sealstone_bricks"));
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHISELED_SEALSTONE.get())
                .pattern("S")
                .pattern("S")
                .define('S', ModBlocks.SEALSTONE_BRICKS.slab().get())
                .unlockedBy(getHasName(bricks), has(bricks))
                .save(output, key("chiseled_sealstone"));
        save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(bricks), RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS,
                ModBlocks.CRACKED_SEALSTONE_BRICKS.get(), 0.1f, 200).unlockedBy(getHasName(bricks), has(bricks)), "cracked_sealstone_bricks");
        shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MOSSY_SEALSTONE_BRICKS.get())
                .requires(bricks)
                .requires(Items.MOSS_BLOCK)
                .unlockedBy(getHasName(bricks), has(bricks))
                .save(output, key("mossy_sealstone_bricks"));
        stonecut(polished, 1, sealstone);
        stonecut(bricks, 1, sealstone, polished);
        stonecut(ModBlocks.CHISELED_SEALSTONE.get(), 1, sealstone, polished);
    }

    // =====================================================================================
    // Schattenwelt
    // =====================================================================================

    // =====================================================================================
    // Die Leere
    // =====================================================================================

    private void voidRecipes() {
        ItemLike stone = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_STONE.base().get();
        ItemLike polished = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.POLISHED_VOID_STONE.base().get();
        ItemLike bricks = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_STONE_BRICKS.base().get();
        ItemLike chiseled = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_VOID_STONE.get();
        shaped(RecipeCategory.BUILDING_BLOCKS, polished, 4).pattern("SS").pattern("SS").define('S', stone)
                .unlockedBy(getHasName(stone), has(stone)).save(output, key("polished_void_stone"));
        shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4).pattern("PP").pattern("PP").define('P', polished)
                .unlockedBy(getHasName(polished), has(polished)).save(output, key("void_stone_bricks"));
        shaped(RecipeCategory.BUILDING_BLOCKS, chiseled).pattern("S").pattern("S")
                .define('S', io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_STONE_BRICKS.slab().get())
                .unlockedBy(getHasName(bricks), has(bricks)).save(output, key("chiseled_void_stone"));
        save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(bricks), RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CRACKED_VOID_STONE_BRICKS.get(), 0.1f, 200)
                .unlockedBy(getHasName(bricks), has(bricks)), "cracked_void_stone_bricks");
        stonecut(polished, 1, stone);
        stonecut(bricks, 1, stone, polished);
        stonecut(chiseled, 1, stone, polished, bricks);
        for (BuildingSet set : List.of(io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.POLISHED_VOID_STONE,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_STONE_BRICKS)) {
            stonecut(set.stairs().get(), 1, stone);
            stonecut(set.slab().get(), 2, stone);
            if (set.wall() != null) {
                stonecut(set.wall().get(), 1, stone);
            }
        }
        // Sternenmarmor (nur aus den Ruinen der Alten)
        ItemLike marble = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_MARBLE.base().get();
        ItemLike marbleBricks = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_MARBLE_BRICKS.base().get();
        ItemLike pillar = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_PILLAR.get();
        ItemLike chiseledMarble = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.CHISELED_ASTRAL_MARBLE.get();
        shaped(RecipeCategory.BUILDING_BLOCKS, marbleBricks, 4).pattern("MM").pattern("MM").define('M', marble)
                .unlockedBy(getHasName(marble), has(marble)).save(output, key("astral_marble_bricks"));
        shaped(RecipeCategory.BUILDING_BLOCKS, pillar, 2).pattern("M").pattern("M").define('M', marble)
                .unlockedBy(getHasName(marble), has(marble)).save(output, key("astral_pillar"));
        shaped(RecipeCategory.BUILDING_BLOCKS, chiseledMarble).pattern("S").pattern("S")
                .define('S', io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_MARBLE_BRICKS.slab().get())
                .unlockedBy(getHasName(marbleBricks), has(marbleBricks)).save(output, key("chiseled_astral_marble"));
        stonecut(marbleBricks, 1, marble);
        stonecut(pillar, 1, marble);
        stonecut(chiseledMarble, 1, marble, marbleBricks);
        // Leerenkristall aus vier Splittern
        ItemLike shard = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get();
        shaped(RecipeCategory.BUILDING_BLOCKS, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL.get())
                .pattern("SS").pattern("SS").define('S', shard)
                .unlockedBy(getHasName(shard), has(shard)).save(output, key("void_crystal"));
    }

    private void shadowRecipes() {
        ItemLike stone = ShadowBlocks.SHADOW_STONE.base().get();
        ItemLike polished = ShadowBlocks.POLISHED_SHADOW_STONE.base().get();
        ItemLike bricks = ShadowBlocks.SHADOW_STONE_BRICKS.base().get();
        shaped(RecipeCategory.BUILDING_BLOCKS, polished, 4)
                .pattern("SS")
                .pattern("SS")
                .define('S', stone)
                .unlockedBy(getHasName(stone), has(stone))
                .save(output, key("polished_shadow_stone"));
        shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
                .pattern("PP")
                .pattern("PP")
                .define('P', polished)
                .unlockedBy(getHasName(polished), has(polished))
                .save(output, key("shadow_stone_bricks"));
        shaped(RecipeCategory.BUILDING_BLOCKS, ShadowBlocks.CHISELED_SHADOW_STONE.get())
                .pattern("S")
                .pattern("S")
                .define('S', ShadowBlocks.SHADOW_STONE_BRICKS.slab().get())
                .unlockedBy(getHasName(bricks), has(bricks))
                .save(output, key("chiseled_shadow_stone"));
        save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(bricks), RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS,
                ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get(), 0.1f, 200).unlockedBy(getHasName(bricks), has(bricks)),
                "cracked_shadow_stone_bricks");
        stonecut(polished, 1, stone);
        stonecut(bricks, 1, stone, polished);
        stonecut(ShadowBlocks.CHISELED_SHADOW_STONE.get(), 1, stone, polished, bricks);
        for (BuildingSet set : List.of(ShadowBlocks.POLISHED_SHADOW_STONE, ShadowBlocks.SHADOW_STONE_BRICKS)) {
            stonecut(set.stairs().get(), 1, stone);
            stonecut(set.slab().get(), 2, stone);
            if (set.wall() != null) {
                stonecut(set.wall().get(), 1, stone);
            }
        }
        // Tiefschattenstein lässt sich zu Schattenstein zerschlagen (für alle, die Ziegel brauchen)
        stonecut(stone, 1, ShadowBlocks.DEEP_SHADOW_STONE.get());

        // Schattenholz
        ItemLike planks = ShadowBlocks.SHADOW_PLANKS.base().get();
        shapeless(RecipeCategory.BUILDING_BLOCKS, planks, 4)
                .requires(ModTags.Items.SHADOW_LOGS)
                .unlockedBy("has_shadow_log", has(ModTags.Items.SHADOW_LOGS))
                .save(output, key("shadow_planks"));
        wood(ShadowBlocks.SHADOW_WOOD.get(), ShadowBlocks.SHADOW_LOG.get(), "shadow_wood");
        wood(ShadowBlocks.STRIPPED_SHADOW_WOOD.get(), ShadowBlocks.STRIPPED_SHADOW_LOG.get(), "stripped_shadow_wood");
        Ingredient shadowPlanks = Ingredient.of(planks);
        doorBuilder(ShadowBlocks.SHADOW_DOOR.get(), shadowPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("shadow_door"));
        trapdoorBuilder(ShadowBlocks.SHADOW_TRAPDOOR.get(), shadowPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("shadow_trapdoor"));
        fenceBuilder(ShadowBlocks.SHADOW_FENCE.get(), shadowPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("shadow_fence"));
        fenceGateBuilder(ShadowBlocks.SHADOW_FENCE_GATE.get(), shadowPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("shadow_fence_gate"));

        // Dämmerbrot: drei Ähren Dämmerkorn
        Item duskgrain = ShadowItems.DUSKGRAIN.get();
        shaped(RecipeCategory.FOOD, ShadowItems.DUSK_BREAD.get())
                .pattern("###")
                .define('#', duskgrain)
                .unlockedBy(getHasName(duskgrain), has(duskgrain))
                .save(output, key("dusk_bread"));

        // Aschschnee: drei Blöcke ergeben sechs Schichten, vier Schichten wieder einen Block
        shaped(RecipeCategory.BUILDING_BLOCKS, ShadowBlocks.ASH_SNOW.get(), 6)
                .pattern("###")
                .define('#', ShadowBlocks.ASH_SNOW_BLOCK.get())
                .unlockedBy(getHasName(ShadowBlocks.ASH_SNOW_BLOCK.get()), has(ShadowBlocks.ASH_SNOW_BLOCK.get()))
                .save(output, key("ash_snow"));
        shaped(RecipeCategory.BUILDING_BLOCKS, ShadowBlocks.ASH_SNOW_BLOCK.get())
                .pattern("##")
                .pattern("##")
                .define('#', ShadowBlocks.ASH_SNOW.get())
                .unlockedBy(getHasName(ShadowBlocks.ASH_SNOW.get()), has(ShadowBlocks.ASH_SNOW.get()))
                .save(output, key("ash_snow_block_from_layers"));
    }

    /**
     * Das Feenreich: Feenstein- und Mondsteinziegel, gemeißelter Mondstein, Glimmerholz – und der Feenstaub, der das Portal
     * entzündet (Amethyst, Leuchtbeeren und eine Blüte). Mondsteinziegel gibt es schon in der Oberwelt: vier Steinziegel um
     * einen Amethystsplitter.
     */
    private void faeRecipes() {
        Item amethyst = Items.AMETHYST_SHARD;
        shapeless(RecipeCategory.MISC, FaeBlocks.FAE_DUST.get(), 2)
                .requires(amethyst)
                .requires(Items.GLOW_BERRIES)
                .requires(net.minecraft.tags.BlockItemTags.SMALL_FLOWERS.item())
                .unlockedBy(getHasName(amethyst), has(amethyst))
                .save(output, key("fae_dust"));
        ItemLike moonstone = FaeBlocks.MOONSTONE_BRICKS.base().get();
        shaped(RecipeCategory.BUILDING_BLOCKS, moonstone, 4)
                .pattern(" B ")
                .pattern("BAB")
                .pattern(" B ")
                .define('B', Blocks.STONE_BRICKS)
                .define('A', amethyst)
                .unlockedBy(getHasName(amethyst), has(amethyst))
                .save(output, key("moonstone_bricks"));
        shaped(RecipeCategory.BUILDING_BLOCKS, FaeBlocks.CHISELED_MOONSTONE.get())
                .pattern("#")
                .pattern("#")
                .define('#', FaeBlocks.MOONSTONE_BRICKS.slab().get())
                .unlockedBy(getHasName(moonstone), has(moonstone))
                .save(output, key("chiseled_moonstone"));
        stonecut(FaeBlocks.CHISELED_MOONSTONE.get(), 1, moonstone);
        ItemLike faestone = FaeBlocks.FAESTONE.base().get();
        ItemLike faeBricks = FaeBlocks.FAESTONE_BRICKS.base().get();
        shaped(RecipeCategory.BUILDING_BLOCKS, faeBricks, 4)
                .pattern("##")
                .pattern("##")
                .define('#', faestone)
                .unlockedBy(getHasName(faestone), has(faestone))
                .save(output, key("faestone_bricks"));
        stonecut(faeBricks, 1, faestone);
        for (ItemLike part : List.of(FaeBlocks.FAESTONE_BRICKS.stairs().get(), FaeBlocks.FAESTONE_BRICKS.wall().get())) {
            stonecut(part, 1, faestone);
        }
        stonecut(FaeBlocks.FAESTONE_BRICKS.slab().get(), 2, faestone);
        shapeless(RecipeCategory.BUILDING_BLOCKS, FaeBlocks.MOSSY_FAESTONE_BRICKS.get())
                .requires(faeBricks)
                .requires(FaeBlocks.FAE_MOSS.get())
                .unlockedBy(getHasName(FaeBlocks.FAE_MOSS.get()), has(FaeBlocks.FAE_MOSS.get()))
                .save(output, key("mossy_faestone_bricks"));

        // Glimmerholz
        ItemLike planks = FaeBlocks.GLIMMERWOOD_PLANKS.base().get();
        shapeless(RecipeCategory.BUILDING_BLOCKS, planks, 4)
                .requires(ModTags.Items.GLIMMERWOOD_LOGS)
                .unlockedBy("has_glimmerwood_log", has(ModTags.Items.GLIMMERWOOD_LOGS))
                .save(output, key("glimmerwood_planks"));
        wood(FaeBlocks.GLIMMERWOOD_WOOD.get(), FaeBlocks.GLIMMERWOOD_LOG.get(), "glimmerwood_wood");
        wood(FaeBlocks.STRIPPED_GLIMMERWOOD_WOOD.get(), FaeBlocks.STRIPPED_GLIMMERWOOD_LOG.get(), "stripped_glimmerwood_wood");
        Ingredient glimmerPlanks = Ingredient.of(planks);
        doorBuilder(FaeBlocks.GLIMMERWOOD_DOOR.get(), glimmerPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("glimmerwood_door"));
        trapdoorBuilder(FaeBlocks.GLIMMERWOOD_TRAPDOOR.get(), glimmerPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("glimmerwood_trapdoor"));
        fenceBuilder(FaeBlocks.GLIMMERWOOD_FENCE.get(), glimmerPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("glimmerwood_fence"));
        fenceGateBuilder(FaeBlocks.GLIMMERWOOD_FENCE_GATE.get(), glimmerPlanks).unlockedBy(getHasName(planks), has(planks))
                .save(output, key("glimmerwood_fence_gate"));
    }

    /** Erze, Barren, Werkzeuge, Rüstungen und die Abyssit-Aufwertung der Schattenwelt. */
    private void shadowTreasureRecipes() {
        Item moonsilver = ShadowItems.MOONSILVER_INGOT.get();
        Item nightsteel = ShadowItems.NIGHTSTEEL_INGOT.get();
        Item soul = ShadowItems.SOUL_CRYSTAL.get();
        Item abyssite = ShadowItems.ABYSSITE_INGOT.get();
        smeltOre(moonsilver, 0.7f, ShadowItems.RAW_MOONSILVER.get(), ShadowBlocks.MOONSILVER_ORE.get(), ShadowBlocks.DEEP_MOONSILVER_ORE.get());
        smeltOre(nightsteel, 1.0f, ShadowItems.RAW_NIGHTSTEEL.get(), ShadowBlocks.NIGHTSTEEL_ORE.get(), ShadowBlocks.DEEP_NIGHTSTEEL_ORE.get());
        smeltOre(soul, 1.0f, ShadowBlocks.SOUL_CRYSTAL_ORE.get());
        smeltOre(ShadowItems.ABYSSITE_SHARD.get(), 2.0f, ShadowBlocks.ABYSSITE_ORE.get());
        storage(ShadowItems.MOONSILVER_NUGGET.get(), moonsilver);
        storage(moonsilver, ShadowBlocks.MOONSILVER_BLOCK.get());
        storage(nightsteel, ShadowBlocks.NIGHTSTEEL_BLOCK.get());
        storage(soul, ShadowBlocks.SOUL_CRYSTAL_BLOCK.get());
        storage(abyssite, ShadowBlocks.ABYSSITE_BLOCK.get());
        // Abyssit: vier Splitter, vier Nachtstahlbarren (wie Netherit)
        shapeless(RecipeCategory.MISC, abyssite)
                .requires(ShadowItems.ABYSSITE_SHARD.get(), 4)
                .requires(nightsteel, 4)
                .unlockedBy(getHasName(ShadowItems.ABYSSITE_SHARD.get()), has(ShadowItems.ABYSSITE_SHARD.get()))
                .save(output, key("abyssite_ingot"));
        // Nachtkristall aus Splittern
        shaped(RecipeCategory.BUILDING_BLOCKS, ShadowBlocks.NIGHT_CRYSTAL.get())
                .pattern("SS")
                .pattern("SS")
                .define('S', ShadowItems.NIGHT_CRYSTAL_SHARD.get())
                .unlockedBy(getHasName(ShadowItems.NIGHT_CRYSTAL_SHARD.get()), has(ShadowItems.NIGHT_CRYSTAL_SHARD.get()))
                .save(output, key("night_crystal"));
        // Mondlaterne: Mondsilber-Klümpchen um einen Nachtkristallsplitter
        shaped(RecipeCategory.DECORATIONS, ShadowBlocks.MOON_LANTERN.get())
                .pattern("NNN")
                .pattern("NSN")
                .pattern("NNN")
                .define('N', ShadowItems.MOONSILVER_NUGGET.get())
                .define('S', ShadowItems.NIGHT_CRYSTAL_SHARD.get())
                .unlockedBy(getHasName(ShadowItems.MOONSILVER_NUGGET.get()), has(ShadowItems.MOONSILVER_NUGGET.get()))
                .save(output, key("moon_lantern"));
        // Mondsilberklinge: Mondsilber-Klinge mit einer Parierstange aus Seelenkristallen
        shaped(RecipeCategory.COMBAT, ShadowItems.MOONSILVER_BLADE.get())
                .pattern(" M ")
                .pattern(" M ")
                .pattern("CSC")
                .define('M', moonsilver)
                .define('C', soul)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(moonsilver), has(moonsilver))
                .save(output, key("moonsilver_blade"));
        // Nachtstahl
        tools(nightsteel, ShadowItems.NIGHTSTEEL_SWORD.get(), ShadowItems.NIGHTSTEEL_PICKAXE.get(), ShadowItems.NIGHTSTEEL_AXE.get(),
                ShadowItems.NIGHTSTEEL_SHOVEL.get(), ShadowItems.NIGHTSTEEL_HOE.get());
        armorSet(nightsteel, ShadowItems.NIGHTSTEEL_HELMET.get(), ShadowItems.NIGHTSTEEL_CHESTPLATE.get(),
                ShadowItems.NIGHTSTEEL_LEGGINGS.get(), ShadowItems.NIGHTSTEEL_BOOTS.get());
        // Abyssit: Nachtstahl im Schmiedetisch aufwerten
        Item template = ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get();
        List<List<Item>> upgrades = List.of(
                List.of(ShadowItems.NIGHTSTEEL_SWORD.get(), ShadowItems.ABYSSITE_SWORD.get()),
                List.of(ShadowItems.NIGHTSTEEL_PICKAXE.get(), ShadowItems.ABYSSITE_PICKAXE.get()),
                List.of(ShadowItems.NIGHTSTEEL_AXE.get(), ShadowItems.ABYSSITE_AXE.get()),
                List.of(ShadowItems.NIGHTSTEEL_SHOVEL.get(), ShadowItems.ABYSSITE_SHOVEL.get()),
                List.of(ShadowItems.NIGHTSTEEL_HOE.get(), ShadowItems.ABYSSITE_HOE.get()),
                List.of(ShadowItems.NIGHTSTEEL_HELMET.get(), ShadowItems.ABYSSITE_HELMET.get()),
                List.of(ShadowItems.NIGHTSTEEL_CHESTPLATE.get(), ShadowItems.ABYSSITE_CHESTPLATE.get()),
                List.of(ShadowItems.NIGHTSTEEL_LEGGINGS.get(), ShadowItems.ABYSSITE_LEGGINGS.get()),
                List.of(ShadowItems.NIGHTSTEEL_BOOTS.get(), ShadowItems.ABYSSITE_BOOTS.get()));
        for (List<Item> pair : upgrades) {
            Item result = pair.get(1);
            RecipeCategory category = result == ShadowItems.ABYSSITE_SWORD.get() || isArmor(result) ? RecipeCategory.COMBAT : RecipeCategory.TOOLS;
            SmithingTransformRecipeBuilder.smithing(Ingredient.of(template), Ingredient.of(pair.get(0)), Ingredient.of(abyssite), category, result)
                    .unlocks(getHasName(abyssite), has(abyssite))
                    .save(output, key(getItemName(result) + "_smithing"));
        }
        // Vorlage vervielfältigen: Vorlage, Tiefschattenstein und sieben Seelenkristalle ergeben zwei Vorlagen
        shaped(RecipeCategory.MISC, template, 2)
                .pattern("#S#")
                .pattern("#C#")
                .pattern("###")
                .define('#', soul)
                .define('C', ShadowBlocks.DEEP_SHADOW_STONE.get())
                .define('S', template)
                .unlockedBy(getHasName(template), has(template))
                .save(output, key("abyssite_upgrade_smithing_template"));
    }

    private static boolean isArmor(Item item) {
        return item == ShadowItems.ABYSSITE_HELMET.get() || item == ShadowItems.ABYSSITE_CHESTPLATE.get()
                || item == ShadowItems.ABYSSITE_LEGGINGS.get() || item == ShadowItems.ABYSSITE_BOOTS.get();
    }

    /** Holzblock (vier Seiten Rinde): vier Stämme im Quadrat ergeben drei Holzblöcke. */
    private void wood(ItemLike wood, ItemLike log, String name) {
        shaped(RecipeCategory.BUILDING_BLOCKS, wood, 3)
                .pattern("##")
                .pattern("##")
                .define('#', log)
                .unlockedBy(getHasName(log), has(log))
                .save(output, key(name));
    }

    private void planksFromLogs(ItemLike planks, ItemLike log, String name) {
        shapeless(RecipeCategory.BUILDING_BLOCKS, planks, 4)
                .requires(log)
                .unlockedBy(getHasName(log), has(log))
                .save(output, key(name));
    }

    /** Neun kleine Münzen ergeben eine große, eine große wieder neun kleine. */
    private void coins(Item small, Item large, String largeName, String smallName) {
        shaped(RecipeCategory.MISC, large)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', small)
                .unlockedBy(getHasName(small), has(small))
                .save(output, key(largeName + "_from_" + smallName));
        shapeless(RecipeCategory.MISC, small, 9)
                .requires(large)
                .unlockedBy(getHasName(large), has(large))
                .save(output, key(smallName + "_from_" + largeName));
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

    /**
     * Totems und Äpfel der Macht (seit 0.80) – alle sehr teuer: Jedes Totem braucht ein Totem der Unsterblichkeit und Beute
     * eines Drachen oder einer fremden Welt, das Totem des Schöpfers die sechs Totems der Drachen und einen Schöpferfunken;
     * der Drachenapfel einen verzauberten goldenen Apfel und die Schuppen aller sechs Drachen, der Apfel der Unsterblichkeit
     * die drei anderen Äpfel.
     */
    private void relics() {
        java.util.function.Function<io.github.sirschrolli.kingdomsatwar.relic.TotemKind, Item> totem =
                io.github.sirschrolli.kingdomsatwar.registry.RelicItems::totem;
        java.util.function.Function<io.github.sirschrolli.kingdomsatwar.dragon.DragonType, Item> scale = type -> ModItems.DRAGON_SCALES.get(type).get();
        record Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind kind, ItemLike edge, ItemLike corner) {}
        for (Ring ring : List.of(
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.EMBER, scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.FIRE), Items.BLAZE_ROD),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.FROST, scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.ICE), ModBlocks.FROST_CRYSTAL.get()),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.STORM, scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.LIGHTNING), ModBlocks.STORM_CRYSTAL.get()),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.STONE, scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.EARTH), ModItems.GEMSTONE.get()),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.GROVE, scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.NATURE), ModBlocks.AMBER_CRYSTAL.get()),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.TIDE, scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.WATER), Items.NAUTILUS_SHELL),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.SHADOW, ShadowItems.SHADOW_ESSENCE.get(), ShadowItems.NIGHT_CRYSTAL_SHARD.get()),
                new Ring(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.VOID, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get(), Items.ENDER_PEARL))) {
            Item result = totem.apply(ring.kind());
            shaped(RecipeCategory.COMBAT, result)
                    .pattern("CEC").pattern("ETE").pattern("CEC")
                    .define('T', Items.TOTEM_OF_UNDYING).define('E', ring.edge()).define('C', ring.corner())
                    .unlockedBy(getHasName(ring.edge()), has(ring.edge())).save(output, key(ring.kind().itemName()));
        }
        Item heart = ModItems.HEART_CRYSTAL.get();
        Item spark = io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SPARK.get();
        // das Totem des Schöpfers: die sechs Totems der Drachen, vereint durch einen Schöpferfunken
        shaped(RecipeCategory.COMBAT, totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.CREATOR))
                .pattern("EFS").pattern("HCH").pattern("RGT")
                .define('E', totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.EMBER))
                .define('F', totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.FROST))
                .define('S', totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.STORM))
                .define('R', totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.STONE))
                .define('G', totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.GROVE))
                .define('T', totem.apply(io.github.sirschrolli.kingdomsatwar.relic.TotemKind.TIDE))
                .define('H', heart).define('C', spark)
                .unlockedBy(getHasName(spark), has(spark)).save(output, key("totem_creator"));

        java.util.function.Function<io.github.sirschrolli.kingdomsatwar.relic.PowerApple, Item> apple =
                io.github.sirschrolli.kingdomsatwar.registry.RelicItems::apple;
        // Drachenapfel: ein verzauberter goldener Apfel zwischen den Schuppen aller sechs Drachen
        shaped(RecipeCategory.FOOD, apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.DRAGON))
                .pattern("FGI").pattern("LAN").pattern("EGW")
                .define('A', Items.ENCHANTED_GOLDEN_APPLE).define('G', Items.GOLD_BLOCK)
                .define('F', scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.FIRE))
                .define('I', scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.ICE))
                .define('L', scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.LIGHTNING))
                .define('N', scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.NATURE))
                .define('E', scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.EARTH))
                .define('W', scale.apply(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.WATER))
                .unlockedBy(getHasName(Items.ENCHANTED_GOLDEN_APPLE), has(Items.ENCHANTED_GOLDEN_APPLE)).save(output, key("dragon_apple"));
        // Sternenapfel: Sternsplitter der Sternenreiterin und Splitter der Leere
        Item star = io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD.get();
        shaped(RecipeCategory.FOOD, apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.STAR))
                .pattern("VSV").pattern("SAS").pattern("VSV")
                .define('A', Items.ENCHANTED_GOLDEN_APPLE).define('S', star)
                .define('V', io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD.get())
                .unlockedBy(getHasName(star), has(star)).save(output, key("star_apple"));
        // Schattenapfel: Schattenessenz und Seelenkristalle
        Item essence = ShadowItems.SHADOW_ESSENCE.get();
        shaped(RecipeCategory.FOOD, apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.SHADOW))
                .pattern("KSK").pattern("SAS").pattern("KSK")
                .define('A', Items.ENCHANTED_GOLDEN_APPLE).define('S', essence).define('K', ShadowItems.SOUL_CRYSTAL.get())
                .unlockedBy(getHasName(essence), has(essence)).save(output, key("shadow_apple"));
        // Apfel der Unsterblichkeit: die drei Äpfel, ein Schöpferfunke, ein Netherstern, Herz- und Manakristalle
        shaped(RecipeCategory.FOOD, apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.IMMORTAL))
                .pattern("HDM").pattern("SCX").pattern("MNH")
                .define('D', apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.DRAGON))
                .define('S', apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.STAR))
                .define('X', apple.apply(io.github.sirschrolli.kingdomsatwar.relic.PowerApple.SHADOW))
                .define('C', spark).define('N', Items.NETHER_STAR).define('H', heart).define('M', ModItems.MANA_CRYSTAL.get())
                .unlockedBy(getHasName(spark), has(spark)).save(output, key("immortal_apple"));
    }

    /** Das Angeln (seit 0.79): aus jedem eigenen Fisch wird gebraten ein Filet; Köder zum Selbermachen. */
    private void fishing() {
        Item fillet = io.github.sirschrolli.kingdomsatwar.registry.FishingItems.COOKED_FILLET.get();
        net.minecraft.tags.TagKey<Item> fish = io.github.sirschrolli.kingdomsatwar.registry.ModTags.Items.FISH;
        save(SimpleCookingRecipeBuilder.smelting(tag(fish), RecipeCategory.FOOD, CookingBookCategory.FOOD, fillet, 0.35f, 200)
                .unlockedBy("has_fish", has(fish)), "fish_fillet_cooked");
        save(SimpleCookingRecipeBuilder.smoking(tag(fish), RecipeCategory.FOOD, fillet, 0.35f, 100)
                .unlockedBy("has_fish", has(fish)), "fish_fillet_cooked_from_smoking");
        save(SimpleCookingRecipeBuilder.campfireCooking(tag(fish), RecipeCategory.FOOD, fillet, 0.35f, 600)
                .unlockedBy("has_fish", has(fish)), "fish_fillet_cooked_from_campfire_cooking");
        // Fischbeutel: Leder, eine Truhe als Futter, oben die Zugschnur
        Item bag = io.github.sirschrolli.kingdomsatwar.registry.FishingItems.FISH_BAG.get();
        shaped(RecipeCategory.TOOLS, bag)
                .pattern("S S").pattern("LCL").pattern("LLL")
                .define('S', Items.STRING).define('L', Items.LEATHER).define('C', Items.CHEST)
                .unlockedBy(getHasName(Items.FISHING_ROD), has(Items.FISHING_ROD))
                .save(output, key("fish_bag"));
        // die Angeln (0.87): wie die Angel von Minecraft gebaut – oben der Werkstoff, unten der Griff, rechts die Schnur
        rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.IRON, Items.IRON_INGOT, Items.STICK);
        rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.GOLD, Items.GOLD_INGOT, Items.STICK);
        rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.SHADOW, io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_INGOT.get(),
                io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_INGOT.get());
        rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.GLUT, Items.BLAZE_ROD, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_SCALE.get());
        rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.STAR, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD.get(), Items.STICK);
        // die Drachenangel: Draconium, der Griff eine Drachenschuppe
        shaped(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType.DRAGON))
                .pattern("  M").pattern(" ML").pattern("G L")
                .define('M', ModItems.DRACONIUM.get()).define('G', ModTags.Items.DRAGON_SCALES).define('L', Items.STRING)
                .unlockedBy("has_draconium", has(ModItems.DRACONIUM.get()))
                .save(output, key(io.github.sirschrolli.kingdomsatwar.fishing.RodType.DRAGON.id()));
        // Köderwurm: was im Kompost kriecht – verrottetes Fleisch, Erde, Saat
        Item worm = io.github.sirschrolli.kingdomsatwar.registry.FishingItems.BAIT_WORM.get();
        shapeless(RecipeCategory.TOOLS, worm, 6)
                .requires(Items.ROTTEN_FLESH).requires(Items.DIRT).requires(Items.WHEAT_SEEDS)
                .unlockedBy(getHasName(Items.FISHING_ROD), has(Items.FISHING_ROD))
                .save(output, key("bait_worm"));
        // Glühköder: Würmer, in Leuchttinte und Glühbeeren getaucht
        shapeless(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.BAIT_GLOW.get(), 4)
                .requires(worm, 4).requires(Items.GLOW_INK_SAC).requires(Items.GLOW_BERRIES)
                .unlockedBy(getHasName(worm), has(worm))
                .save(output, key("bait_glow"));
        shapeless(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.BAIT_GLOW.get(), 4)
                .requires(worm, 4).requires(Items.GLOWSTONE_DUST).requires(Items.SPIDER_EYE)
                .unlockedBy(getHasName(worm), has(worm))
                .save(output, key("bait_glow_from_glowstone"));
    }

    /** Eine Angel der Mod: zweimal der Werkstoff schräg nach oben, unten links der Griff, rechts zwei Fäden Schnur. */
    private void rod(io.github.sirschrolli.kingdomsatwar.fishing.RodType type, Item material, Item grip) {
        shaped(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.rod(type))
                .pattern("  M").pattern(" ML").pattern("G L")
                .define('M', material).define('G', grip).define('L', Items.STRING)
                .unlockedBy(getHasName(material), has(material))
                .save(output, key(type.id()));
    }

    /** Das Meer: Tiefseelaterne aus dem Leuchtköder, Pfeile mit Haizahnspitzen, gegartes Krabbenfleisch. */
    private void sea() {
        // das Urmeer (0.89): Gezeitenstein aus Stein und Prismarinscherben, daraus Ziegel und gemeißelter Gezeitenstein
        ItemLike tideStone = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE.base().get();
        ItemLike tideBricks = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE_BRICKS.base().get();
        ItemLike chiseledTide = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.CHISELED_TIDE_STONE.get();
        shaped(RecipeCategory.BUILDING_BLOCKS, tideStone, 4)
                .pattern(" S ")
                .pattern("SPS")
                .pattern(" S ")
                .define('S', Blocks.STONE)
                .define('P', Items.PRISMARINE_SHARD)
                .unlockedBy(getHasName(Items.PRISMARINE_SHARD), has(Items.PRISMARINE_SHARD))
                .save(output, key("tide_stone"));
        shaped(RecipeCategory.BUILDING_BLOCKS, tideBricks, 4)
                .pattern("##")
                .pattern("##")
                .define('#', tideStone)
                .unlockedBy(getHasName(tideStone), has(tideStone))
                .save(output, key("tide_stone_bricks"));
        stonecut(tideBricks, 1, tideStone);
        shaped(RecipeCategory.BUILDING_BLOCKS, chiseledTide)
                .pattern("#")
                .pattern("#")
                .define('#', io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_STONE_BRICKS.slab().get())
                .unlockedBy(getHasName(tideBricks), has(tideBricks))
                .save(output, key("chiseled_tide_stone"));
        stonecut(chiseledTide, 1, tideStone, tideBricks);
        Item lure = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.ANGLER_LURE.get();
        shaped(RecipeCategory.DECORATIONS, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.DEEP_SEA_LANTERN.get())
                .pattern("NPN")
                .pattern("PLP")
                .pattern("NPN")
                .define('N', Items.IRON_NUGGET)
                .define('P', Items.PRISMARINE_SHARD)
                .define('L', lure)
                .unlockedBy(getHasName(lure), has(lure))
                .save(output, key("deep_sea_lantern"));
        Item tooth = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.SHARK_TOOTH.get();
        shaped(RecipeCategory.COMBAT, Items.ARROW, 6)
                .pattern("T")
                .pattern("S")
                .pattern("F")
                .define('T', tooth)
                .define('S', Items.STICK)
                .define('F', Items.FEATHER)
                .unlockedBy(getHasName(tooth), has(tooth))
                .save(output, key("arrows_from_shark_tooth"));
        Item raw = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.CRAB_MEAT.get();
        Item cooked = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.COOKED_CRAB_MEAT.get();
        save(SimpleCookingRecipeBuilder.smelting(Ingredient.of(raw), RecipeCategory.FOOD, CookingBookCategory.FOOD, cooked, 0.35f, 200)
                .unlockedBy(getHasName(raw), has(raw)), "cooked_crab_meat");
        save(SimpleCookingRecipeBuilder.smoking(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35f, 100)
                .unlockedBy(getHasName(raw), has(raw)), "cooked_crab_meat_from_smoking");
        save(SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(raw), RecipeCategory.FOOD, cooked, 0.35f, 600)
                .unlockedBy(getHasName(raw), has(raw)), "cooked_crab_meat_from_campfire_cooking");
        // Gezeitenhorn: eine große Wellhornschnecke mit goldenem Mundstück und Prismarin
        Item whelk = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.WHELK_SHELLS.get().asItem();
        shapeless(RecipeCategory.TOOLS, io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_HORN.get())
                .requires(whelk).requires(whelk).requires(Items.GOLD_INGOT).requires(Items.PRISMARINE_CRYSTALS)
                .unlockedBy(getHasName(whelk), has(whelk))
                .save(output, key("tide_horn"));
        // Muscheln: zerstoßen zu Knochenmehl (Kalk)
        for (var shells : io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.shells()) {
            shapeless(RecipeCategory.MISC, Items.BONE_MEAL, 1).requires(shells.get())
                    .unlockedBy(getHasName(shells.get()), has(shells.get()))
                    .save(output, key("bone_meal_from_" + shells.getId().getPath()));
        }
        // Leuchtgelee im seltsamen Trank: Nachtsicht (für Trank, Wurftrank und Verweiltrank)
        Item jelly = io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.GLOW_JELLY.get();
        for (Item container : java.util.List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)) {
            net.minecraft.data.recipes.BrewingRecipeBuilder.brewingMix(container, net.minecraft.world.item.alchemy.Potions.AWKWARD, jelly,
                            net.minecraft.world.item.alchemy.Potions.NIGHT_VISION)
                    .save(output, key("night_vision_from_glow_jelly_" + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(container).getPath()));
        }
    }

    /** Werkzeug und Schwert: Kopf bzw. Klinge aus dem Stoff am beschlagenen Griff (unten ein einfacher Stock). */
    private void tools(Item material, Item sword, Item pickaxe, Item axe, Item shovel, Item hoe) {
        String has = getHasName(material);
        Item handle = ModItems.REINFORCED_HANDLE.get();
        shaped(RecipeCategory.COMBAT, sword).pattern("M").pattern("M").pattern("H")
                .define('M', material).define('H', handle).unlockedBy(has, has(material))
                .save(output, key(getItemName(sword)));
        shaped(RecipeCategory.TOOLS, pickaxe).pattern("MMM").pattern(" H ").pattern(" S ")
                .define('M', material).define('H', handle).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(pickaxe)));
        shaped(RecipeCategory.TOOLS, axe).pattern("MM").pattern("MH").pattern(" S")
                .define('M', material).define('H', handle).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(axe)));
        shaped(RecipeCategory.TOOLS, shovel).pattern("M").pattern("H").pattern("S")
                .define('M', material).define('H', handle).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(shovel)));
        shaped(RecipeCategory.TOOLS, hoe).pattern("MM").pattern(" H").pattern(" S")
                .define('M', material).define('H', handle).define('S', Items.STICK).unlockedBy(has, has(material))
                .save(output, key(getItemName(hoe)));
    }

    /** Rüstung aus Platten des Stoffs, innen mit Leder gepolstert. */
    private void armorSet(Item material, Item helmet, Item chestplate, Item leggings, Item boots) {
        String has = getHasName(material);
        shaped(RecipeCategory.COMBAT, helmet).pattern("MMM").pattern("MLM")
                .define('M', material).define('L', Items.LEATHER).unlockedBy(has, has(material)).save(output, key(getItemName(helmet)));
        shaped(RecipeCategory.COMBAT, chestplate).pattern("MLM").pattern("MMM").pattern("MMM")
                .define('M', material).define('L', Items.LEATHER).unlockedBy(has, has(material)).save(output, key(getItemName(chestplate)));
        shaped(RecipeCategory.COMBAT, leggings).pattern("MMM").pattern("MLM").pattern("M M")
                .define('M', material).define('L', Items.LEATHER).unlockedBy(has, has(material)).save(output, key(getItemName(leggings)));
        shaped(RecipeCategory.COMBAT, boots).pattern("MLM").pattern("M M")
                .define('M', material).define('L', Items.LEATHER).unlockedBy(has, has(material)).save(output, key(getItemName(boots)));
    }

    /** Drachenschuppen-Rüstung: Schuppen rund um das passende Stück Stahlrüstung genietet. */
    private void scaleArmor(Item scales, Item helmet, Item chestplate, Item leggings, Item boots) {
        String has = getHasName(scales);
        shaped(RecipeCategory.COMBAT, helmet).pattern("SSS").pattern("SAS")
                .define('S', scales).define('A', ModItems.STEEL_HELMET.get()).unlockedBy(has, has(scales))
                .save(output, key(getItemName(helmet)));
        shaped(RecipeCategory.COMBAT, chestplate).pattern("SAS").pattern("SSS").pattern("SSS")
                .define('S', scales).define('A', ModItems.STEEL_CHESTPLATE.get()).unlockedBy(has, has(scales))
                .save(output, key(getItemName(chestplate)));
        shaped(RecipeCategory.COMBAT, leggings).pattern("SSS").pattern("SAS").pattern("S S")
                .define('S', scales).define('A', ModItems.STEEL_LEGGINGS.get()).unlockedBy(has, has(scales))
                .save(output, key(getItemName(leggings)));
        shaped(RecipeCategory.COMBAT, boots).pattern("SAS").pattern("S S")
                .define('S', scales).define('A', ModItems.STEEL_BOOTS.get()).unlockedBy(has, has(scales))
                .save(output, key(getItemName(boots)));
    }

    // =====================================================================================
    // Armee
    // =====================================================================================

    /** Belagerungswaffen (Bausätze) und ihre Munition. */
    private void siegeRecipes() {
        // Steinkugeln: vier Bruchsteine werden zu zwei runden Kugeln behauen
        shaped(RecipeCategory.COMBAT, ModItems.SIEGE_STONE.get(), 2)
                .pattern("CC")
                .pattern("CC")
                .define('C', Blocks.COBBLESTONE)
                .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                .save(output, key("siege_stone"));
        // Ballistenbolzen: eiserne Spitze, Schaft, Federn
        shaped(RecipeCategory.COMBAT, ModItems.BALLISTA_BOLT.get(), 4)
                .pattern(" I ")
                .pattern(" S ")
                .pattern("FSF")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .define('F', Items.FEATHER)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output, key("ballista_bolt"));
        // Katapult: Wurfarm aus einem Stamm, Seile und eiserne Beschläge auf einem Gestell
        shaped(RecipeCategory.COMBAT, ModItems.CATAPULT_KIT.get())
                .pattern(" L ")
                .pattern("SIS")
                .pattern("PPP")
                .define('L', ItemTags.LOGS)
                .define('S', Items.STRING)
                .define('I', Items.IRON_INGOT)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output, key("catapult"));
        // Tribok: hohes Gerüst, Ketten und ein Gegengewicht aus Eisen
        shaped(RecipeCategory.COMBAT, ModItems.TREBUCHET_KIT.get())
                .pattern("LCL")
                .pattern("SBS")
                .pattern("PPP")
                .define('L', ItemTags.LOGS)
                .define('C', Items.IRON_CHAIN)
                .define('S', Items.STRING)
                .define('B', Items.IRON_BLOCK)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output, key("trebuchet"));
        // Balliste: eine Armbrust als Herz, Sehnen, Beschläge und ein Dreibein
        shaped(RecipeCategory.COMBAT, ModItems.BALLISTA_KIT.get())
                .pattern("SXS")
                .pattern("PIP")
                .pattern("L L")
                .define('S', Items.STRING)
                .define('X', Items.CROSSBOW)
                .define('P', ItemTags.PLANKS)
                .define('I', Items.IRON_INGOT)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_crossbow", has(Items.CROSSBOW))
                .save(output, key("ballista"));
        // Rammbock: Schutzdach, ein an Ketten hängender Stamm mit Eisenkopf, Gestell
        shaped(RecipeCategory.COMBAT, ModItems.BATTERING_RAM_KIT.get())
                .pattern("PPP")
                .pattern("CLC")
                .pattern("LIL")
                .define('P', ItemTags.PLANKS)
                .define('C', Items.IRON_CHAIN)
                .define('L', ItemTags.LOGS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output, key("battering_ram"));
        // Belagerungsturm: Balkengerüst, vorn Häute, innen eine Leiter, oben die Kette der Fallbrücke, eiserne Achsen
        shaped(RecipeCategory.COMBAT, ModItems.SIEGE_TOWER_KIT.get())
                .pattern("LCL")
                .pattern("HAH")
                .pattern("PIP")
                .define('L', ItemTags.LOGS)
                .define('C', Items.IRON_CHAIN)
                .define('H', Items.LEATHER)
                .define('A', Items.LADDER)
                .define('P', ItemTags.PLANKS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(output, key("siege_tower"));
        // Planwagen: Leinenplane aus Wolle über dem Kasten mit einer Truhe, Räder mit eisernen Reifen
        shaped(RecipeCategory.TRANSPORTATION, ModItems.WAGON_KIT.get())
                .pattern("WWW")
                .pattern("PCP")
                .pattern("LIL")
                .define('W', ItemTags.WOOL)
                .define('P', ItemTags.PLANKS)
                .define('C', Items.CHEST)
                .define('L', ItemTags.LOGS)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_chest", has(Items.CHEST))
                .save(output, key("wagon"));
    }

    private void armyRecipes() {
        // Feldherrentisch: Buch und Smaragde auf einem Holztisch
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
        // Drachenhorn: ein Ziegenhorn mit Goldbeschlägen und zwei Drachenschuppen
        shaped(RecipeCategory.TOOLS, ModItems.DRAGON_HORN.get())
                .pattern(" G ")
                .pattern("SHS")
                .pattern(" G ")
                .define('G', Items.GOLD_INGOT)
                .define('S', ModTags.Items.DRAGON_SCALES)
                .define('H', Items.GOAT_HORN)
                .unlockedBy("has_dragon_scale", has(ModTags.Items.DRAGON_SCALES))
                .save(output, key("dragon_horn"));
        // Vermessungsstab (Dragon Architect): zwei Stöcke mit Messingspitze
        shaped(RecipeCategory.TOOLS, ModItems.SURVEYORS_ROD.get())
                .pattern("  C")
                .pattern(" S ")
                .pattern("S  ")
                .define('C', Items.COPPER_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(output, key("surveyors_rod"));
        // Stadtbanner: ein Banner (beliebige Farbe) mit Goldknauf an einem Mast auf einem Steinsockel
        shaped(RecipeCategory.DECORATIONS, ModBlocks.TOWN_BANNER.get())
                .pattern(" G ")
                .pattern("BSB")
                .pattern("CCC")
                .define('G', Items.GOLD_INGOT)
                .define('B', ItemTags.BANNERS)
                .define('S', Items.STICK)
                .define('C', Items.STONE_BRICKS)
                .unlockedBy("has_banner", has(ItemTags.BANNERS))
                .save(output, key("town_banner"));
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
        shaped(RecipeCategory.DECORATIONS, ModBlocks.STEEL_LANTERN.get())
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

        // Stahl: zwei Eisenbarren mit Kohlenstoff mischen, dann im Hochofen härten
        shapeless(RecipeCategory.MISC, ModItems.STEEL_BLEND.get())
                .requires(Items.IRON_INGOT, 2)
                .requires(ModItems.CARBON.get(), 2)
                .unlockedBy("has_carbon", has(ModItems.CARBON.get()))
                .save(output, key("steel_blend"));
        save(SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.STEEL_BLEND.get()), RecipeCategory.MISC,
                        CookingBookCategory.MISC, ModItems.STEEL_INGOT.get(), 0.7f, 200)
                .unlockedBy("has_steel_blend", has(ModItems.STEEL_BLEND.get())), "steel_ingot_from_blasting");
        // Bretter aus den eigenen Stämmen (wie bei Vanilla vier je Stamm)
        planksFromLogs(ModBlocks.ANCIENT_PLANKS.base().get(), ModBlocks.ANCIENT_LOG.get(), "ancient_planks");
        planksFromLogs(ModBlocks.CHARRED_PLANKS.base().get(), ModBlocks.CHARRED_LOG.get(), "charred_planks");
        // Münzen zusammenlegen und wechseln: 9 Bronze = 1 Silber, 9 Silber = 1 Gold
        coins(ModItems.BRONZE_COIN.get(), ModItems.SILVER_COIN.get(), "silver_coin", "bronze_coins");
        coins(ModItems.SILVER_COIN.get(), ModItems.GOLD_COIN.get(), "gold_coin", "silver_coins");
        // Kohlenstoff auch aus Holzkohle (Köhlerei)
        save(SimpleCookingRecipeBuilder.blasting(Ingredient.of(Items.CHARCOAL), RecipeCategory.MISC,
                        CookingBookCategory.MISC, ModItems.CARBON.get(), 0.1f, 200)
                .unlockedBy("has_charcoal", has(Items.CHARCOAL)), "carbon_from_charcoal");
    }

    // =====================================================================================
    // Drachen
    // =====================================================================================

    private void dragonRecipes() {
        // Dracheneier lassen sich nicht herstellen: Es gibt sie nur auf dem Nest eines besiegten Drachen-Bosses.

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
