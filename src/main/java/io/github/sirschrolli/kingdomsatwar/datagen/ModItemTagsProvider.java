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
        copy(Tags.BlockItems.COBBLESTONES);
        // Bruchstein aus eigenem Gestein taugt wie Bruchstein für Steinwerkzeuge, Öfen und Braustände
        copy(ModTags.Blocks.COBBLED_STONES, ModTags.Items.COBBLED_STONES);
        tag(ItemTags.STONE_TOOL_MATERIALS).addTag(ModTags.Items.COBBLED_STONES);
        tag(ItemTags.STONE_CRAFTING_MATERIALS).addTag(ModTags.Items.COBBLED_STONES);
        copy(BlockItemTags.MOSS_BLOCKS);
        copy(ModTags.Blocks.SHADOW_LOGS, ModTags.Items.SHADOW_LOGS);
        copy(ModTags.Blocks.GLIMMERWOOD_LOGS, ModTags.Items.GLIMMERWOOD_LOGS);
        for (io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.SeaWood wood : io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.ALL) {
            copy(wood.logsTag, wood.logsItemTag);
        }
        copy(BlockItemTags.SAPLINGS);
        copy(BlockItemTags.LANTERNS);
        copy(ModTags.Blocks.ORES_MOONSILVER, ModTags.Items.ORES_MOONSILVER);
        copy(ModTags.Blocks.ORES_NIGHTSTEEL, ModTags.Items.ORES_NIGHTSTEEL);
        copy(ModTags.Blocks.ORES_SOUL_CRYSTAL, ModTags.Items.ORES_SOUL_CRYSTAL);
        copy(ModTags.Blocks.ORES_ABYSSITE, ModTags.Items.ORES_ABYSSITE);
        copy(ModTags.Blocks.ORES_MOONSTONE, ModTags.Items.ORES_MOONSTONE);
        copy(ModTags.Blocks.STORAGE_BLOCKS_MOONSTONE, ModTags.Items.STORAGE_BLOCKS_MOONSTONE);
        copy(ModTags.Blocks.ORES_AQUAMARINE, ModTags.Items.ORES_AQUAMARINE);
        copy(ModTags.Blocks.STORAGE_BLOCKS_AQUAMARINE, ModTags.Items.STORAGE_BLOCKS_AQUAMARINE);
        copy(ModTags.Blocks.ORES_DUSK_QUARTZ, ModTags.Items.ORES_DUSK_QUARTZ);
        copy(ModTags.Blocks.STORAGE_BLOCKS_DUSK_QUARTZ, ModTags.Items.STORAGE_BLOCKS_DUSK_QUARTZ);
        copy(ModTags.Blocks.STORAGE_BLOCKS_MOONSILVER, ModTags.Items.STORAGE_BLOCKS_MOONSILVER);
        copy(ModTags.Blocks.STORAGE_BLOCKS_NIGHTSTEEL, ModTags.Items.STORAGE_BLOCKS_NIGHTSTEEL);
        copy(ModTags.Blocks.STORAGE_BLOCKS_SOUL_CRYSTAL, ModTags.Items.STORAGE_BLOCKS_SOUL_CRYSTAL);
        copy(ModTags.Blocks.STORAGE_BLOCKS_ABYSSITE, ModTags.Items.STORAGE_BLOCKS_ABYSSITE);
        copy(BlockItemTags.WOODEN_DOORS);
        copy(BlockItemTags.WOODEN_TRAPDOORS);
        copy(BlockItemTags.WOODEN_FENCES);
        copy(BlockItemTags.FENCE_GATES);
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
        add(ModTags.Items.REPAIRS_SHATTERED_CROWN, io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_SHARD);
        tag(ModTags.Items.REPAIRS_TIDE_SPEAR).add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(net.minecraft.world.item.Items.PRISMARINE_CRYSTALS).orElseThrow());
        tag(ModTags.Items.REPAIRS_CROWN).addTag(Tags.Items.INGOTS_GOLD);
        ModItems.DRAGON_SCALES.forEach((type, scale) -> add(ModTags.Items.scaleArmorRepair(type), scale));

        add(ItemTags.SWORDS, ModItems.STEEL_SWORD, ModItems.DRACONIUM_SWORD);
        add(ItemTags.PICKAXES, ModItems.STEEL_PICKAXE);
        add(ItemTags.AXES, ModItems.STEEL_AXE, ModItems.DRACONIUM_AXE);
        add(ItemTags.SHOVELS, ModItems.STEEL_SHOVEL);
        add(ItemTags.HOES, ModItems.STEEL_HOE);
        add(ItemTags.HEAD_ARMOR, ModItems.STEEL_HELMET, ModItems.KONTERIT_HELMET, ModItems.GEMSTONE_CROWN,
                io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.SHATTERED_CROWN);
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
        add(ItemTags.DURABILITY_ENCHANTABLE, ModItems.WYVERNIT_GLIDER, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_ELYTRA);
        add(ItemTags.EQUIPPABLE_ENCHANTABLE, ModItems.WYVERNIT_GLIDER, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_ELYTRA);
        add(ModTags.Items.REPAIRS_FAE_ELYTRA, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_DUST);
        // die Waffenkammer (0.84): Schwerter, Äxte und Spitzhacken wie die von Minecraft (verzauberbar, Reparatur am Werkstoff)
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_HALBERD,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.DRACONIUM_WARHAMMER, io.github.sirschrolli.kingdomsatwar.registry.Armory.NIGHTSTEEL_DAGGER,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.ABYSSITE_GREATSWORD);
        add(ItemTags.AXES, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_DOUBLE_AXE);
        add(ItemTags.PICKAXES, io.github.sirschrolli.kingdomsatwar.registry.Armory.MINER_HAMMER,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.EMBER_PICKAXE, io.github.sirschrolli.kingdomsatwar.registry.Armory.VEIN_BREAKER,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.ORE_SEEKER);
        add(Tags.Items.TOOLS_SHIELD, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_BUCKLER,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.DRAGON_SHIELD, io.github.sirschrolli.kingdomsatwar.registry.Armory.TOWER_SHIELD,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.MIRROR_SHIELD);
        add(ItemTags.DURABILITY_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.Armory.STEEL_BUCKLER,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.DRAGON_SHIELD, io.github.sirschrolli.kingdomsatwar.registry.Armory.TOWER_SHIELD,
                io.github.sirschrolli.kingdomsatwar.registry.Armory.MIRROR_SHIELD);
        // Waffen und Rüstungen aus Monsterteilen (0.70)
        add(ModTags.Items.REPAIRS_CLAW_BLADE, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.WEREWOLF_CLAW);
        add(ModTags.Items.REPAIRS_BOG_CLUB, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_ROOT);
        add(ModTags.Items.REPAIRS_EMBER_FANG, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_SCALE);
        add(ModTags.Items.REPAIRS_SOUL_SCYTHE, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.SOUL_ASH);
        add(ModTags.Items.REPAIRS_PELT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.WEREWOLF_PELT);
        add(ModTags.Items.REPAIRS_EMBER_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_SCALE);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.CLAW_BLADE, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.BOG_CLUB, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_FANG_BLADE, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.SOUL_SCYTHE);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_HELMET, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_CHESTPLATE, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_LEGGINGS, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_BOOTS, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_BOOTS);
        // Pelz schützt vor dem Frost im Pulverschnee
        add(ItemTags.FREEZE_IMMUNE_WEARABLES, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_HELMET, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_CHESTPLATE, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_LEGGINGS, io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.PELT_BOOTS);
        tag(ModTags.Items.REPAIRS_GLUT_SPEAR).add(net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getResourceKey(net.minecraft.world.item.Items.NETHERITE_SCRAP).orElseThrow());
        // die Speere der Bosse sind Speere wie die von Minecraft: verzauberbar (Ausfallschritt, Schärfe, Haltbarkeit …)
        add(ItemTags.SPEARS, io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUT_SPEAR,
                io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks.TIDE_SPEAR, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_LANCE,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SPEAR);
        // der Weltenspeer: mit Weltennähten zu reparieren
        tag(ModTags.Items.REPAIRS_WORLD_SPEAR).add(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_SEAM.getId()));
        // Asteria: Sternsplitter reparieren Lanze und Schwingen, die Schwingen auch Phantomhaut; verzauberbar wie Elytren
        add(ModTags.Items.REPAIRS_STAR_LANCE, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD);
        add(ModTags.Items.REPAIRS_STAR_WINGS, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD);
        tag(ModTags.Items.REPAIRS_STAR_WINGS).add(net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getResourceKey(net.minecraft.world.item.Items.PHANTOM_MEMBRANE).orElseThrow());
        add(ItemTags.DURABILITY_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_WINGS);
        add(ModTags.Items.REPAIRS_MYSTIC_GEAR, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.FAE_DUST);
        // Mondstein (0.96): Edelstein, repariert die Mondsteinrüstung und den Hüterhammer; die Rüstung lässt sich besetzen
        add(ModTags.Items.GEMS_MOONSTONE, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_MOONSTONE);
        add(ModTags.Items.REPAIRS_MOONSTONE_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE);
        add(ModTags.Items.REPAIRS_KEEPER_MAUL, io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks.MOONSTONE);
        add(ModTags.Items.REPAIRS_SEA_WITCH_STAFF, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE);
        add(ModTags.Items.REPAIRS_SHEPHERD_CROOK, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd.CROOK);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.WITCH_STAFF);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.KEEPER_MAUL);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.MOONSTONE_BOOTS);
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.FaeGear.moonstoneSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        // Aquamarin (0.97): Edelstein, repariert die Taucherrüstung; die Rüstung lässt sich besetzen
        add(ModTags.Items.GEMS_AQUAMARINE, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_AQUAMARINE);
        add(ModTags.Items.REPAIRS_AQUAMARINE_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_BOOTS);
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.SeaGear.aquamarineSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        // Dämmerquarz und die Dämmerrüstung (0.98)
        add(ModTags.Items.GEMS_DUSK_QUARTZ, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_DUSK_QUARTZ);
        add(ModTags.Items.REPAIRS_DUSK_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_HOOD);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_CLOAK);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_BOOTS);
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.duskSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        // Himmelseisen und die Rüstung der Sternwandler (0.99)
        copy(ModTags.Blocks.ORES_SKY_IRON, ModTags.Items.ORES_SKY_IRON);
        copy(ModTags.Blocks.STORAGE_BLOCKS_SKY_IRON, ModTags.Items.STORAGE_BLOCKS_SKY_IRON);
        add(ModTags.Items.RAW_MATERIALS_SKY_IRON, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.RAW_SKY_IRON);
        tag(Tags.Items.RAW_MATERIALS).addTag(ModTags.Items.RAW_MATERIALS_SKY_IRON);
        add(ModTags.Items.INGOTS_SKY_IRON, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_INGOT);
        tag(Tags.Items.INGOTS).addTag(ModTags.Items.INGOTS_SKY_IRON);
        add(ItemTags.BEACON_PAYMENT_ITEMS, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_INGOT);
        add(ModTags.Items.REPAIRS_STARWALKER_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_INGOT);
        // Chronit (seit 1.1)
        copy(ModTags.Blocks.ORES_CHRONIT, ModTags.Items.ORES_CHRONIT);
        copy(ModTags.Blocks.STORAGE_BLOCKS_CHRONIT, ModTags.Items.STORAGE_BLOCKS_CHRONIT);
        add(ModTags.Items.GEMS_CHRONIT, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_CHRONIT);
        add(ItemTags.BEACON_PAYMENT_ITEMS, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT);
        add(ModTags.Items.REPAIRS_TIMEWARDEN_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT);
        // Feueropal und die Rüstung der Glutwandler (seit 1.1.6)
        copy(ModTags.Blocks.ORES_FIRE_OPAL, ModTags.Items.ORES_FIRE_OPAL);
        copy(ModTags.Blocks.STORAGE_BLOCKS_FIRE_OPAL, ModTags.Items.STORAGE_BLOCKS_FIRE_OPAL);
        add(ModTags.Items.GEMS_FIRE_OPAL, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_FIRE_OPAL);
        add(ItemTags.BEACON_PAYMENT_ITEMS, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL);
        add(ModTags.Items.REPAIRS_EMBERWALKER_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.EMBERWALKER_BOOTS);
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.FlameGear.emberwalkerSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        // Phasit und die Rüstung der Leerwandler (seit 1.4)
        copy(ModTags.Blocks.ORES_PHASIT, ModTags.Items.ORES_PHASIT);
        copy(ModTags.Blocks.STORAGE_BLOCKS_PHASIT, ModTags.Items.STORAGE_BLOCKS_PHASIT);
        add(ModTags.Items.GEMS_PHASIT, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT);
        tag(Tags.Items.GEMS).addTag(ModTags.Items.GEMS_PHASIT);
        add(ItemTags.BEACON_PAYMENT_ITEMS, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT);
        add(ModTags.Items.REPAIRS_VOIDWALKER_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.VOIDWALKER_BOOTS);
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.voidwalkerSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        // der Minutenzeiger (1.1): ein Schwert, mit Chronit auszubessern
        add(ModTags.Items.REPAIRS_MINUTE_HAND, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.ClockTower.MINUTE_HAND);
        // die Flammenpeitsche (1.2): mit Feueropalen zu reparieren, verzauberbar wie ein Schwert
        add(ModTags.Items.REPAIRS_FLAME_WHIP, io.github.sirschrolli.kingdomsatwar.registry.FlameGear.FIRE_OPAL);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary.FLAME_WHIP);
        // die Phasenklinge (1.5): ein Schwert (Schärfe, Schwung …), mit Phasit auszubessern
        add(ModTags.Items.REPAIRS_PHASE_BLADE, io.github.sirschrolli.kingdomsatwar.registry.PhaseGear.PHASIT);
        add(ItemTags.SWORDS, io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary.PHASE_BLADE);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_BOOTS);
        // der Sternenbogen (0.99): ein Bogen wie jeder andere (Verzauberungen, Köcher anderer Mods), mit Himmelseisen zu reparieren
        add(ModTags.Items.REPAIRS_STAR_BOW, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.SKY_IRON_INGOT);
        add(ItemTags.BOW_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_BOW);
        add(ItemTags.DURABILITY_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_BOW);
        add(ItemTags.VANISHING_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_BOW);
        add(Tags.Items.TOOLS_BOW, io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_BOW);
        add(ItemTags.HEAD_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_HELMET);
        add(ItemTags.CHEST_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_CHESTPLATE);
        add(ItemTags.LEG_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_LEGGINGS);
        add(ItemTags.FOOT_ARMOR, io.github.sirschrolli.kingdomsatwar.registry.VoidGear.STARWALKER_BOOTS);
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.VoidGear.starwalkerSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        for (var piece : io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.timewardenSet()) {
            add(ItemTags.TRIMMABLE_ARMOR, piece);
        }
        add(ModTags.Items.REPAIRS_GLITCH_GEAR, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.GLITCH_SHARD);
        add(ItemTags.DURABILITY_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.BANNHAMMER,
                io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.GLITCH_WINGS);
        add(ItemTags.MELEE_WEAPON_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.BANNHAMMER);
        add(ItemTags.SHARP_WEAPON_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.BANNHAMMER);
        add(net.neoforged.neoforge.common.Tags.Items.MELEE_WEAPON_TOOLS, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.BANNHAMMER);
        add(ItemTags.EQUIPPABLE_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks.GLITCH_WINGS);
        add(ItemTags.DURABILITY_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MYSTIC_DECK,
                io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MYSTIC_TOP_HAT);
        add(ItemTags.HEAD_ARMOR_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MYSTIC_TOP_HAT);
        add(ItemTags.ARMOR_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks.MYSTIC_TOP_HAT);
        add(ItemTags.EQUIPPABLE_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_WINGS);
        add(Tags.Items.ARMORS, ModItems.STEEL_DRAGON_ARMOR, ModItems.DRACONIUM_DRAGON_ARMOR);
        add(ItemTags.DURABILITY_ENCHANTABLE, ModItems.TROLL_CLUB);
        add(ItemTags.MELEE_WEAPON_ENCHANTABLE, ModItems.TROLL_CLUB);
        add(Tags.Items.MELEE_WEAPON_TOOLS, ModItems.TROLL_CLUB);
        add(ItemTags.PIGLIN_LOVED, ModItems.GEMSTONE_CROWN);
        // die Ausrüstung des Schöpfers (0.78): Rüstungsverzauberungen auf Heiligenschein, Schwingen und Rädern, Waffenverzauberungen auf dem Speer
        add(ItemTags.HEAD_ARMOR_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_HALO);
        add(ItemTags.CHEST_ARMOR_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_WINGS);
        add(ItemTags.LEG_ARMOR_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_RINGS);
        add(ItemTags.ARMOR_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_HALO,
                io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_WINGS, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_RINGS);
        add(ItemTags.EQUIPPABLE_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_HALO,
                io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_WINGS, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_RINGS);
        add(ItemTags.MELEE_WEAPON_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SCEPTER);
        add(ItemTags.SHARP_WEAPON_ENCHANTABLE, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SCEPTER);
        add(Tags.Items.MELEE_WEAPON_TOOLS, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_SCEPTER);
        add(Tags.Items.ARMORS, io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_HALO,
                io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks.CREATOR_RINGS);

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

        // Essen, das Bürger und Soldaten nicht nehmen (Lager und Proviant)
        tag(ModTags.Items.UNFIT_FOOD).add(net.minecraft.world.item.Items.ROTTEN_FLESH.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.SPIDER_EYE.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.POISONOUS_POTATO.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.PUFFERFISH.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.SUSPICIOUS_STEW.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.CHORUS_FRUIT.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.GOLDEN_APPLE.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE.builtInRegistryHolder().key(),
                net.minecraft.world.item.Items.GOLDEN_CARROT.builtInRegistryHolder().key());
        fishing();
    }

    /** Das Angeln (seit 0.79): die eigenen Fische, ihr Filet. */
    private void fishing() {
        TagAppender<Item> fish = tag(ModTags.Items.FISH);
        TagAppender<Item> unfit = tag(ModTags.Items.UNFIT_FOOD);
        for (var item : io.github.sirschrolli.kingdomsatwar.registry.FishingItems.fishes()) {
            fish.add(item.key());
            // roh wirkt jeder Fisch anders – das ist nichts für Lager und Proviant (das Filet schon)
            unfit.add(item.key());
        }
        tag(ItemTags.FISHES).addTag(ModTags.Items.FISH);
        tag(Tags.Items.FOODS_RAW_FISH).addTag(ModTags.Items.FISH);
        add(Tags.Items.FOODS_COOKED_FISH, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.COOKED_FILLET);
        add(ItemTags.CAT_FOOD, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.COOKED_FILLET);
        // die Angeln (seit 0.87): Köder, Glück des Meeres, Drillmeister, Haltbarkeit, Reparatur … wie bei der Angel von Minecraft
        for (var rod : io.github.sirschrolli.kingdomsatwar.registry.FishingItems.rods()) {
            tag(ItemTags.FISHING_ENCHANTABLE).add(rod.key());
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(rod.key());
            tag(Tags.Items.TOOLS_FISHING_ROD).add(rod.key());
            tag(Tags.Items.TOOLS).add(rod.key());
        }
        tag(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.IRON)).add(vanilla(net.minecraft.world.item.Items.IRON_INGOT));
        tag(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.GOLD)).add(vanilla(net.minecraft.world.item.Items.GOLD_INGOT));
        add(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.DRAGON), ModItems.DRACONIUM);
        add(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.SHADOW),
                io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_INGOT);
        add(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.GLUT),
                io.github.sirschrolli.kingdomsatwar.registry.MonsterGear.EMBER_SCALE);
        add(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.STAR),
                io.github.sirschrolli.kingdomsatwar.registry.StarBlocks.STAR_SHARD);
        tag(ModTags.Items.rodRepair(io.github.sirschrolli.kingdomsatwar.fishing.RodType.KRAKEN)).add(vanilla(net.minecraft.world.item.Items.PRISMARINE_CRYSTALS));
        add(ItemTags.OCELOT_FOOD, io.github.sirschrolli.kingdomsatwar.registry.FishingItems.COOKED_FILLET);
    }

    private static net.minecraft.resources.ResourceKey<Item> vanilla(Item item) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
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
