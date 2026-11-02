package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.block.TableBlock;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Erzeugt Blockstates, Block- und Gegenstandsmodelle.
 */
public class ModModelProvider extends ModelProvider {
    private static final TextureSlot BASE = TextureSlot.create("base");
    private static final TextureSlot OVERLAY = TextureSlot.create("overlay");
    private static final TextureSlot GLOW = TextureSlot.create("glow");
    /** Erze mit selbstleuchtendem Kern (eigene Glow-Textur, im Dunkeln voll hell). */
    private static final java.util.Set<String> GLOWING_ORES = java.util.Set.of("aetherit", "draconium", "soul_crystal", "abyssite");

    /**
     * Erz-Modell aus zwei Schichten: unten die echte Vanilla-Textur (Stein oder Tiefenschiefer),
     * darüber die Erz-Flecken der Mod. So passen die Erze immer zum umgebenden Gestein.
     */
    private static final ModelTemplate ORE_OVERLAY = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(BASE)
            .requiredTextureSlot(OVERLAY)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(BASE).cullface(direction)))
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(OVERLAY).cullface(direction)))
            .build();

    /** Wie {@link #ORE_OVERLAY}, mit dritter, selbstleuchtender Schicht für den Kristallkern. */
    private static final ModelTemplate ORE_OVERLAY_GLOW = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(BASE)
            .requiredTextureSlot(OVERLAY)
            .requiredTextureSlot(GLOW)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(BASE).cullface(direction)))
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(OVERLAY).cullface(direction)))
            .element(element -> element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(GLOW).cullface(direction)))
            .build();

    /** Vollblock mit zweiter, selbstleuchtender Schicht (glühende Adern bzw. Kristallkern). */
    private static final ModelTemplate CUBE_GLOW = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.ALL)
            .requiredTextureSlot(GLOW)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(TextureSlot.ALL).cullface(direction)))
            .element(element -> element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(GLOW).cullface(direction)))
            .build();

    /** Risse eines brütenden Eis (dunkler Rand) und das leuchtende Innere. */
    private static final TextureSlot CRACKS = TextureSlot.create("cracks");
    /**
     * Eiform wie beim echten Drachenei: ineinander geschobene Quader (x/z von, y von, x/z bis, y bis) –
     * am breitesten im unteren Drittel, nach oben spitz zulaufend.
     */
    private static final float[][] EGG_LAYERS = {
            {3, 0, 13, 13}, {2, 1, 14, 11}, {1, 3, 15, 8}, {4, 13, 12, 14}, {5, 14, 11, 15}, {6, 15, 10, 16}};

    /** Ei-Form aus ineinander geschobenen Quadern. */
    private static final ModelTemplate DRAGON_EGG = eggTemplate(false);
    /** Wie {@link #DRAGON_EGG}, mit Rissen und selbstleuchtendem Inneren darüber (brütendes Ei). */
    private static final ModelTemplate DRAGON_EGG_CRACKED = eggTemplate(true);

    private static ModelTemplate eggTemplate(boolean cracked) {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.ALL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        java.util.List<TextureSlot> slots = cracked ? java.util.List.of(TextureSlot.ALL, CRACKS, GLOW) : java.util.List.of(TextureSlot.ALL);
        if (cracked) {
            builder.requiredTextureSlot(CRACKS).requiredTextureSlot(GLOW);
        }
        for (TextureSlot slot : slots) {
            for (float[] layer : EGG_LAYERS) {
                builder.element(element -> eggLayer(element, slot, layer[0], layer[1], layer[2], layer[3]));
            }
        }
        return builder.build();
    }

    public ModModelProvider(PackOutput output) {
        super(output, KingdomsAtWar.MODID);
    }

    private static void eggLayer(net.neoforged.neoforge.client.model.generators.template.ElementBuilder element,
            TextureSlot slot, float min, float y1, float max, float y2) {
        element.from(min, y1, min).to(max, y2, max)
                .allFaces((direction, face) -> {
                    switch (direction) {
                        case UP, DOWN -> face.uvs(min, min, max, max);
                        default -> face.uvs(min, 16 - y2, max, 16 - y1);
                    }
                    face.texture(slot);
                });
        if (slot == GLOW) {
            element.lightEmission(15);
        }
    }

    @Override
    protected void registerModels(BlockModelGenerators blocks, ItemModelGenerators items) {
        // --- Baublöcke: Vollblock, Treppe, Stufe, Mauer ---
        for (BuildingSet set : ModBlocks.BUILDING_SETS) {
            BlockModelGenerators.BlockFamilyProvider family = blocks.family(set.base().get())
                    .stairs(set.stairs().get())
                    .slab(set.slab().get());
            if (set.wall() != null) {
                family.wall(set.wall().get());
            }
        }
        blocks.createAxisAlignedPillarBlock(ModBlocks.STONE_PILLAR.get(), TexturedModel.COLUMN);
        blocks.createAxisAlignedPillarBlock(ModBlocks.TIMBER_BEAM.get(), TexturedModel.COLUMN);
        blocks.createTrivialCube(ModBlocks.TIMBER_FRAME.get());
        blocks.createTrivialCube(ModBlocks.TIMBER_FRAME_DIAGONAL.get());
        blocks.createTrivialCube(ModBlocks.TIMBER_FRAME_CROSS.get());

        // --- Fenster, Türen, Fensterläden und Licht ---
        blocks.createGlassBlocks(ModBlocks.LEADED_GLASS.get(), ModBlocks.LEADED_GLASS_PANE.get());
        blocks.createDoor(ModBlocks.CASTLE_DOOR.get());
        // nicht "orientable": so steht das ausgesägte Herz im geöffneten Laden immer aufrecht
        blocks.createTrapdoor(ModBlocks.GREEN_SHUTTERS.get());
        blocks.createTrapdoor(ModBlocks.RED_SHUTTERS.get());
        blocks.createLantern(ModBlocks.STEEL_LANTERN.get());
        blocks.createLantern(ModBlocks.AETHERIT_LANTERN.get());
        Block chandelier = ModBlocks.CHANDELIER.get();
        Identifier chandelierModel = DecorModels.chandelier().create(chandelier,
                TextureMapping.cube(chandelier).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(chandelier, BlockModelGenerators.plainVariant(chandelierModel)));
        blocks.registerSimpleFlatItemModel(chandelier.asItem());
        // Bannsiegel der Erzmagier: eine Wand je Element (durchscheinend, schimmernd)
        Block barrier = ModBlocks.ARCANE_BARRIER.get();
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(barrier).with(
                PropertyDispatch.initial(io.github.sirschrolli.kingdomsatwar.block.ArcaneBarrierBlock.ELEMENT).generate(element -> {
                    Material tex = new Material(KingdomsAtWar.id("block/arcane_barrier_" + element.id()));
                    return BlockModelGenerators.plainVariant(ModelTemplates.CUBE_ALL.createWithSuffix(barrier, "_" + element.id(),
                            new TextureMapping().put(TextureSlot.ALL, tex).put(TextureSlot.PARTICLE, tex), blocks.modelOutput));
                })));
        // Baugerüst des Baumeisters (ohne Gegenstand)
        Block scaffold = ModBlocks.BUILDER_SCAFFOLD.get();
        Material scaffoldDeck = new Material(KingdomsAtWar.id("block/builder_scaffold_deck"));
        Identifier scaffoldModel = DecorModels.scaffold().create(scaffold, new TextureMapping()
                .put(DecorModels.POLE, new Material(KingdomsAtWar.id("block/builder_scaffold_pole")))
                .put(DecorModels.DECK, scaffoldDeck)
                .put(DecorModels.SLAT, new Material(KingdomsAtWar.id("block/builder_scaffold_slat")))
                .put(TextureSlot.PARTICLE, scaffoldDeck), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(scaffold, BlockModelGenerators.plainVariant(scaffoldModel)));

        // --- Zinnen (Textur des Baustoffs) ---
        ModelTemplate battlementTemplate = DecorModels.battlement();
        ModBlocks.BATTLEMENTS.forEach((battlement, material) -> {
            TextureMapping stone = TextureMapping.cube(material.base().get()).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE);
            facing(blocks, battlement.get(), battlementTemplate.create(battlement.get(), stone, blocks.modelOutput));
        });

        // --- Möbel (Vanilla-Holztexturen) ---
        furniture(blocks);

        // --- Erze (zweischichtig) ---
        ore(blocks, ModBlocks.CARBON_ORE, Blocks.STONE, "carbon");
        ore(blocks, ModBlocks.DEEPSLATE_CARBON_ORE, Blocks.DEEPSLATE, "carbon");
        ore(blocks, ModBlocks.GEMSTONE_ORE, Blocks.STONE, "gemstone");
        ore(blocks, ModBlocks.DEEPSLATE_GEMSTONE_ORE, Blocks.DEEPSLATE, "gemstone");
        ore(blocks, ModBlocks.KONTERIT_ORE, Blocks.STONE, "konterit");
        ore(blocks, ModBlocks.DEEPSLATE_KONTERIT_ORE, Blocks.DEEPSLATE, "konterit");
        ore(blocks, ModBlocks.AETHERIT_ORE, Blocks.STONE, "aetherit");
        ore(blocks, ModBlocks.DEEPSLATE_AETHERIT_ORE, Blocks.DEEPSLATE, "aetherit");
        ore(blocks, ModBlocks.DRACONIUM_ORE, Blocks.STONE, "draconium");
        ore(blocks, ModBlocks.DEEPSLATE_DRACONIUM_ORE, Blocks.DEEPSLATE, "draconium");
        ore(blocks, ModBlocks.WYVERNIT_ORE, Blocks.STONE, "wyvernit");

        // --- Speicherblöcke ---
        blocks.createTrivialCube(ModBlocks.CARBON_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.GEMSTONE_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.KONTERIT_BLOCK.get());
        glowingCube(blocks, ModBlocks.AETHERIT_BLOCK.get());
        glowingCube(blocks, ModBlocks.DRACONIUM_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.WYVERNIT_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.STEEL_BLOCK.get());

        // --- Dracheneier: heil (hatch=0) und drei Riss-Stufen beim Brüten ---
        for (DeferredBlock<ElementalDragonEggBlock> egg : ModBlocks.DRAGON_EGGS.values()) {
            ElementalDragonEggBlock block = egg.get();
            TextureMapping mapping = TextureMapping.cube(block).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE);
            Identifier whole = DRAGON_EGG.create(block, mapping, blocks.modelOutput);
            String kind = block.getDragonType().id();
            PropertyDispatch.C1<MultiVariant, Integer> stages = PropertyDispatch.initial(ElementalDragonEggBlock.HATCH)
                    .select(0, BlockModelGenerators.plainVariant(whole));
            for (int stage = 1; stage <= 3; stage++) {
                TextureMapping cracked = mapping.copyAndUpdate(CRACKS, new Material(KingdomsAtWar.id("block/dragon_egg_cracks_" + stage)))
                        .copyAndUpdate(GLOW, new Material(KingdomsAtWar.id("block/" + kind + "_dragon_egg_glow_" + stage)));
                Identifier model = DRAGON_EGG_CRACKED.createWithSuffix(block, "_hatch_" + stage, cracked, blocks.modelOutput);
                stages.select(stage, BlockModelGenerators.plainVariant(model));
            }
            blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(stages));
        }

        // --- Rekrutierungstisch (dreht sich zum Spieler) ---
        Block table = ModBlocks.RECRUITMENT_TABLE.get();
        Material spruce = new Material(Identifier.withDefaultNamespace("block/spruce_planks"));
        TextureMapping tableTextures = new TextureMapping()
                .put(RecruitmentTableModel.WOOD, spruce)
                .put(RecruitmentTableModel.LEG, new Material(Identifier.withDefaultNamespace("block/stripped_spruce_log")))
                .put(RecruitmentTableModel.CLOTH, new Material(KingdomsAtWar.id("block/recruitment_table_cloth")))
                .put(RecruitmentTableModel.BANNER, new Material(KingdomsAtWar.id("block/recruitment_table_banner")))
                .put(RecruitmentTableModel.ITEMS, new Material(KingdomsAtWar.id("block/recruitment_table_items")))
                .put(TextureSlot.PARTICLE, spruce);
        Identifier tableModel = RecruitmentTableModel.template().create(table, tableTextures, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(table, BlockModelGenerators.plainVariant(tableModel))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        // --- Söldnertafel: Pfosten, Brett mit Anschlägen, Abdeckbrett; dreht sich wie der Tisch ---
        Block mercenaryBoard = ModBlocks.MERCENARY_BOARD.get();
        Material boardBack = new Material(Identifier.withDefaultNamespace("block/spruce_planks"));
        TextureMapping boardTextures = new TextureMapping()
                .put(MercenaryBoardModel.POST, new Material(Identifier.withDefaultNamespace("block/dark_oak_log")))
                .put(MercenaryBoardModel.POST_END, new Material(Identifier.withDefaultNamespace("block/dark_oak_log_top")))
                .put(MercenaryBoardModel.BOARD, new Material(KingdomsAtWar.id("block/mercenary_board")))
                .put(MercenaryBoardModel.BACK, boardBack)
                .put(MercenaryBoardModel.ROOF, new Material(Identifier.withDefaultNamespace("block/dark_oak_planks")))
                .put(TextureSlot.PARTICLE, boardBack);
        Identifier boardModel = MercenaryBoardModel.template().create(mercenaryBoard, boardTextures, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(mercenaryBoard, BlockModelGenerators.plainVariant(boardModel))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        // --- Magietisch: Sockel, Säule, Platte mit Siegelkreis; Runen und Kristalle leuchten ---
        Block magicTable = ModBlocks.MAGIC_TABLE.get();
        TextureMapping magicTextures = new TextureMapping()
                .put(MagicTableModel.BASE, new Material(KingdomsAtWar.id("block/magic_table_base")))
                .put(MagicTableModel.BASE_TOP, new Material(KingdomsAtWar.id("block/magic_table_base_top")))
                .put(MagicTableModel.BOTTOM, new Material(KingdomsAtWar.id("block/magic_table_bottom")))
                .put(MagicTableModel.GOLD, new Material(KingdomsAtWar.id("block/magic_table_gold")))
                .put(MagicTableModel.COLUMN, new Material(KingdomsAtWar.id("block/magic_table_column")))
                .put(MagicTableModel.COLUMN_GLOW, new Material(KingdomsAtWar.id("block/magic_table_column_glow")))
                .put(MagicTableModel.TOP, new Material(KingdomsAtWar.id("block/magic_table_top")))
                .put(MagicTableModel.TOP_GLOW, new Material(KingdomsAtWar.id("block/magic_table_top_glow")))
                .put(MagicTableModel.TOP_SIDE, new Material(KingdomsAtWar.id("block/magic_table_top_side")))
                .put(MagicTableModel.TOP_SIDE_GLOW, new Material(KingdomsAtWar.id("block/magic_table_top_side_glow")))
                .put(MagicTableModel.CRYSTAL, new Material(KingdomsAtWar.id("block/magic_table_crystal")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/magic_table_base")));
        Identifier magicModel = MagicTableModel.template().create(magicTable, magicTextures, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(magicTable, BlockModelGenerators.plainVariant(magicModel)));

        // --- Stadtbanner: zwei Hälften, dreht sich zum Spieler ---
        Block banner = ModBlocks.TOWN_BANNER.get();
        TextureMapping bannerLower = new TextureMapping()
                .put(TownBannerModel.STONE, vanilla("stone_bricks"))
                .put(TownBannerModel.SLAB, vanilla("smooth_stone"))
                .put(TownBannerModel.POLE, vanilla("stripped_dark_oak_log"))
                .put(TownBannerModel.GOLD, vanilla("gold_block"))
                .put(TownBannerModel.CLOTH, new Material(KingdomsAtWar.id("block/town_banner_cloth_lower")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/town_banner_cloth_upper")));
        TextureMapping bannerUpper = new TextureMapping()
                .put(TownBannerModel.POLE, vanilla("stripped_dark_oak_log"))
                .put(TownBannerModel.GOLD, vanilla("gold_block"))
                .put(TownBannerModel.CLOTH, new Material(KingdomsAtWar.id("block/town_banner_cloth_upper")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/town_banner_cloth_upper")));
        Identifier bannerLowerModel = TownBannerModel.lower().createWithSuffix(banner, "_lower", bannerLower, blocks.modelOutput);
        Identifier bannerUpperModel = TownBannerModel.upper().createWithSuffix(banner, "_upper", bannerUpper, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(banner)
                .with(PropertyDispatch.initial(io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock.HALF)
                        .select(net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER, BlockModelGenerators.plainVariant(bannerLowerModel))
                        .select(net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER, BlockModelGenerators.plainVariant(bannerUpperModel)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleFlatItemModel(banner.asItem());

        // --- Feldfrüchte: 8 Altersstufen, 4 Texturen ---
        for (DeferredBlock<? extends CropBlock> crop : ModBlocks.CROPS) {
            blocks.createCropBlock(crop.get(), CropBlock.AGE, 0, 0, 1, 1, 2, 2, 2, 3);
        }

        // --- Zierblumen mit Blumentopf ---
        ModBlocks.FLOWERS.forEach((flower, pot) ->
                blocks.createPlantWithDefaultItem(flower.get(), pot.get(), BlockModelGenerators.PlantType.NOT_TINTED));

        // --- Festmahl: vier Stufen, dreht sich zum Spieler ---
        Block feast = ModBlocks.FEAST.get();
        TextureMapping feastTextures = new TextureMapping().put(DecorModels.BOARD, vanilla("spruce_planks"))
                .put(DecorModels.FOOD, new Material(KingdomsAtWar.id("block/feast"))).put(TextureSlot.PARTICLE, vanilla("spruce_planks"));
        PropertyDispatch.C1<MultiVariant, Integer> servings = PropertyDispatch.initial(io.github.sirschrolli.kingdomsatwar.block.FeastBlock.EATEN);
        for (int eaten = 0; eaten < io.github.sirschrolli.kingdomsatwar.block.FeastBlock.SERVINGS; eaten++) {
            Identifier model = DecorModels.feast(eaten).createWithSuffix(feast, eaten == 0 ? "" : "_eaten_" + eaten, feastTextures,
                    blocks.modelOutput);
            servings.select(eaten, BlockModelGenerators.plainVariant(model));
        }
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(feast).with(servings).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleFlatItemModel(feast.asItem());

        // --- Gegenstände (flache 2D-Modelle) ---
        // Samen und Zwiebel bekommen ihr Modell schon über createCropBlock (Gegenstand der Feldfrucht).
        for (DeferredItem<?> item : java.util.List.of(
                ModItems.CARBON, ModItems.GEMSTONE, ModItems.KONTERIT, ModItems.AETHERIT, ModItems.DRACONIUM,
                ModItems.WYVERNIT, ModItems.STEEL_BLEND, ModItems.REINFORCED_HANDLE, ModItems.STEEL_INGOT,
                ModItems.DRAGON_FOOD, ModItems.TOMATO, ModItems.CABBAGE, ModItems.BARLEY, ModItems.BREAD_ROLL,
                ModItems.VEGETABLE_STEW, ModItems.MEAT_PIE, ModItems.APPLE_PIE)) {
            flat(items, item.get());
        }
        for (DeferredItem<Item> scale : ModItems.DRAGON_SCALES.values()) {
            flat(items, scale.get());
        }
        for (DeferredItem<?> spawnEgg : ModItems.DRAGON_SPAWN_EGGS.values()) {
            flat(items, spawnEgg.get());
        }
        for (DeferredItem<?> spawnEgg : ModItems.MAGE_SPAWN_EGGS.values()) {
            flat(items, spawnEgg.get());
        }
        for (DeferredItem<?> spawnEgg : ModItems.SOLDIER_SPAWN_EGGS.values()) {
            flat(items, spawnEgg.get());
        }
        flat(items, ModItems.BANDIT_SPAWN_EGG.get());
        flat(items, ModItems.VILLAGER_SPAWN_EGG.get());
        flat(items, ModItems.BANDIT_ARCHER_SPAWN_EGG.get());
        items.generateFlatItem(ModItems.COMMAND_BATON.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        flat(items, ModItems.BLUEPRINT.get());
        for (io.github.sirschrolli.kingdomsatwar.economy.Coin coin : io.github.sirschrolli.kingdomsatwar.economy.Coin.values()) {
            coinModel(items, coin.item(), coin.id() + "_coin");
        }

        // --- Werkzeuge, Waffen und Rüstungen ---
        for (DeferredItem<?> tool : java.util.List.of(ModItems.STEEL_SWORD, ModItems.STEEL_PICKAXE, ModItems.STEEL_AXE,
                ModItems.STEEL_SHOVEL, ModItems.STEEL_HOE, ModItems.DRACONIUM_SWORD, ModItems.DRACONIUM_AXE)) {
            items.generateFlatItem(tool.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        }
        armor(items, ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(), ModItems.STEEL_LEGGINGS.get(),
                ModItems.STEEL_BOOTS.get());
        armor(items, ModItems.KONTERIT_HELMET.get(), ModItems.KONTERIT_CHESTPLATE.get(), ModItems.KONTERIT_LEGGINGS.get(),
                ModItems.KONTERIT_BOOTS.get());
        ModItems.DRAGON_SCALE_ARMOR.values().forEach(set ->
                armor(items, set.get(0).get(), set.get(1).get(), set.get(2).get(), set.get(3).get()));
        items.generateElytra(ModItems.WYVERNIT_GLIDER.get());
        flat(items, ModItems.GEMSTONE_CROWN.get());
        flat(items, ModItems.STEEL_DRAGON_ARMOR.get());
        items.generateFlatItem(ModItems.TROLL_CLUB.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        for (DeferredItem<?> item : java.util.List.of(ModItems.CATTLE_SPAWN_EGG, ModItems.PIG_SPAWN_EGG, ModItems.SHEEP_SPAWN_EGG,
                ModItems.CHICKEN_SPAWN_EGG, ModItems.HORSE_SPAWN_EGG, ModItems.DONKEY_SPAWN_EGG, ModItems.MULE_SPAWN_EGG,
                ModItems.GOAT_SPAWN_EGG, ModItems.HARE_SPAWN_EGG, ModItems.WOLF_SPAWN_EGG, ModItems.FOX_SPAWN_EGG, ModItems.BEAR_SPAWN_EGG,
                ModItems.PANDA_SPAWN_EGG, ModItems.CAT_SPAWN_EGG, ModItems.WILDCAT_SPAWN_EGG, ModItems.PARROT_SPAWN_EGG,
                ModItems.BEE_SPAWN_EGG, ModItems.BAT_SPAWN_EGG, ModItems.ARMADILLO_SPAWN_EGG, ModItems.CAMEL_SPAWN_EGG,
                ModItems.LLAMA_SPAWN_EGG, ModItems.TRADER_LLAMA_SPAWN_EGG)) {
            flat(items, item.get());
        }
        for (DeferredItem<?> item : java.util.List.of(ModItems.ZOMBIE_SPAWN_EGG, ModItems.HUSK_SPAWN_EGG, ModItems.DROWNED_SPAWN_EGG,
                ModItems.ZOMBIE_VILLAGER_SPAWN_EGG, ModItems.SKELETON_SPAWN_EGG, ModItems.STRAY_SPAWN_EGG, ModItems.BOGGED_SPAWN_EGG,
                ModItems.PARCHED_SPAWN_EGG, ModItems.CREEPER_SPAWN_EGG, ModItems.SPIDER_SPAWN_EGG, ModItems.CAVE_SPIDER_SPAWN_EGG,
                ModItems.SLIME_SPAWN_EGG, ModItems.SULFUR_CUBE_SPAWN_EGG, ModItems.WITCH_SPAWN_EGG, ModItems.PHANTOM_SPAWN_EGG,
                ModItems.SILVERFISH_SPAWN_EGG, ModItems.ZOMBIE_HORSE_SPAWN_EGG, ModItems.SKELETON_HORSE_SPAWN_EGG,
                ModItems.CAMEL_HUSK_SPAWN_EGG, ModItems.PILLAGER_SPAWN_EGG, ModItems.VINDICATOR_SPAWN_EGG, ModItems.EVOKER_SPAWN_EGG,
                ModItems.ILLUSIONER_SPAWN_EGG, ModItems.VEX_SPAWN_EGG, ModItems.RAVAGER_SPAWN_EGG, ModItems.GUARDIAN_SPAWN_EGG,
                ModItems.ELDER_GUARDIAN_SPAWN_EGG, ModItems.BREEZE_SPAWN_EGG, ModItems.CREAKING_SPAWN_EGG, ModItems.WARDEN_SPAWN_EGG,
                ModItems.COD_SPAWN_EGG, ModItems.SALMON_SPAWN_EGG, ModItems.PUFFERFISH_SPAWN_EGG, ModItems.TROPICAL_FISH_SPAWN_EGG, ModItems.SQUID_SPAWN_EGG, ModItems.GLOW_SQUID_SPAWN_EGG, ModItems.DOLPHIN_SPAWN_EGG, ModItems.TURTLE_SPAWN_EGG, ModItems.AXOLOTL_SPAWN_EGG, ModItems.FROG_SPAWN_EGG, ModItems.TADPOLE_SPAWN_EGG, ModItems.NAUTILUS_SPAWN_EGG, ModItems.ZOMBIE_NAUTILUS_SPAWN_EGG,
                ModItems.IRON_GOLEM_SPAWN_EGG, ModItems.SNOW_GOLEM_SPAWN_EGG, ModItems.COPPER_GOLEM_SPAWN_EGG, ModItems.ALLAY_SPAWN_EGG,
                ModItems.SNIFFER_SPAWN_EGG, ModItems.HAPPY_GHAST_SPAWN_EGG, ModItems.WANDERING_TRADER_SPAWN_EGG)) {
            flat(items, item.get());
        }
        for (DeferredItem<?> item : java.util.List.of(ModItems.WILD_BOAR_SPAWN_EGG, ModItems.DEER_SPAWN_EGG, ModItems.GOOSE_SPAWN_EGG,
                ModItems.WILL_O_WISP_SPAWN_EGG, ModItems.CAVE_TROLL_SPAWN_EGG, ModItems.LINDWORM_SPAWN_EGG, ModItems.VENISON,
                ModItems.COOKED_VENISON, ModItems.WISP_DUST, ModItems.GARLIC_BREAD, ModItems.ALE, ModItems.WINE, ModItems.MEAD)) {
            flat(items, item.get());
        }
        flat(items, ModItems.DRACONIUM_DRAGON_ARMOR.get());

        // --- Magie ---
        items.generateFlatItem(ModItems.AETHERIT_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        ModItems.RUNES.values().forEach(rune -> flat(items, rune.get()));
        ModItems.SPELL_TABLETS.values().forEach(tablet -> flat(items, tablet.get()));

        // --- Aschelande ---
        blocks.createTrivialCube(ModBlocks.ASH_BLOCK.get());
        ashLayer(blocks, ModBlocks.ASH_LAYER.get(), ModBlocks.ASH_BLOCK.get());
        blocks.createTrivialBlock(ModBlocks.CINDER_SOIL.get(), TexturedModel.CUBE_BOTTOM_TOP);
        glowingCube(blocks, ModBlocks.GLOWING_VOLCANIC_ROCK.get());
        blocks.createAxisAlignedPillarBlock(ModBlocks.CHARRED_LOG.get(), TexturedModel.COLUMN);
        emberVent(blocks, ModBlocks.EMBER_VENT.get());

        // --- Drachennester: Nestkern (verborgen, mit glimmendem Siegel) und Drachenkompass ---
        glowingCube(blocks, ModBlocks.DRAGON_LAIR_CORE.get());
        glowingCube(blocks, ModBlocks.SHADOW_THRONE_CORE.get());
        dragonCompass(items, ModItems.DRAGON_COMPASS.get());

        // --- Siegelkreis, Siegelsplitter und Chronik ---
        SealModels.register(blocks, items);

        // --- Schattenwelt ---
        ShadowModels.register(blocks, items);

        // --- Frostkessel ---
        Block frostCrystal = ModBlocks.FROST_CRYSTAL.get();
        Identifier crystalModel = ModelTemplates.CUBE_ALL.create(frostCrystal,
                TextureMapping.cube(TextureMapping.getBlockTexture(frostCrystal).withForceTranslucent(true)), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(frostCrystal, BlockModelGenerators.plainVariant(crystalModel)));
        blocks.createTrivialCube(ModBlocks.GLACIER_ICE.get());
        blocks.createTrivialBlock(ModBlocks.FROZEN_SOIL.get(), TexturedModel.CUBE_BOTTOM_TOP);
        blocks.createCrossBlockWithDefaultItem(ModBlocks.ICICLE.get(), BlockModelGenerators.PlantType.NOT_TINTED);

        // --- Uralter Hain ---
        blocks.woodProvider(ModBlocks.ANCIENT_LOG.get()).logWithHorizontal(ModBlocks.ANCIENT_LOG.get());
        blocks.createTrivialBlock(ModBlocks.ANCIENT_LEAVES.get(), TexturedModel.LEAVES);
        blocks.createTrivialCube(ModBlocks.GLOWING_MOSS.get());
        blocks.createCrossBlockWithDefaultItem(ModBlocks.GLOWCAP.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.createCrossBlockWithDefaultItem(ModBlocks.BRAMBLE.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.createTrivialBlock(ModBlocks.GNARLED_ROOTS.get(), TexturedModel.COLUMN);

        // --- Zerklüftete Weiten ---
        Block amber = ModBlocks.AMBER_CRYSTAL.get();
        Identifier amberModel = ModelTemplates.CUBE_ALL.create(amber,
                TextureMapping.cube(TextureMapping.getBlockTexture(amber).withForceTranslucent(true)), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(amber, BlockModelGenerators.plainVariant(amberModel)));
        blocks.createCrossBlockWithDefaultItem(ModBlocks.AMBER_CLUSTER.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.createTrivialCube(ModBlocks.RUBBLE.get());
        blocks.createTrivialCube(ModBlocks.RIFT_STONE.get());

        // --- Sturmklippen ---
        blocks.createTrivialCube(ModBlocks.CHARGED_STORMSTONE.get());
        blocks.woodProvider(ModBlocks.FULGURITE.get()).logWithHorizontal(ModBlocks.FULGURITE.get());
        blocks.createCrossBlockWithDefaultItem(ModBlocks.STORM_CRYSTAL.get(), BlockModelGenerators.PlantType.NOT_TINTED);

        // --- Abgrundgrotte ---
        blocks.createTrivialCube(ModBlocks.ABYSSAL_SAND.get());
        blocks.createCrossBlockWithDefaultItem(ModBlocks.GLOW_CORAL.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.createTrivialCube(ModBlocks.NACRE_BLOCK.get());

        // --- Eigene Oberwelt-Biome ---
        blocks.createTrivialCube(ModBlocks.PEAT.get());
        blocks.createDoublePlantWithDefaultItem(ModBlocks.REEDS.get(), BlockModelGenerators.PlantType.NOT_TINTED);
    }

    /** Ascheschicht: sieben flache Modelle (2 bis 14 Pixel hoch), acht Lagen sind ein voller Aschenblock. */
    static void ashLayer(BlockModelGenerators blocks, Block layer, Block full) {
        Material texture = TextureMapping.getBlockTexture(full);
        TextureMapping textures = new TextureMapping().put(TextureSlot.TEXTURE, texture).put(TextureSlot.PARTICLE, texture);
        Identifier[] models = new Identifier[8];
        for (int layers = 1; layers < 8; layers++) {
            models[layers] = layerTemplate(layers * 2).createWithSuffix(layer, "_height" + layers * 2, textures, blocks.modelOutput);
        }
        Identifier fullModel = ModelLocationUtils.getModelLocation(full);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(layer).with(
                PropertyDispatch.initial(net.minecraft.world.level.block.state.properties.BlockStateProperties.LAYERS)
                        .generate(layers -> BlockModelGenerators.plainVariant(layers < 8 ? models[layers] : fullModel))));
        blocks.registerSimpleItemModel(layer, models[1]);
    }

    private static ModelTemplate layerTemplate(int height) {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace(height == 2 ? "block/thin_block" : "block/block"))
                .requiredTextureSlot(TextureSlot.TEXTURE)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, height, 16)
                        .allFaces((direction, face) -> {
                            switch (direction) {
                                case UP -> face.uvs(0, 0, 16, 16);
                                case DOWN -> face.uvs(0, 0, 16, 16).cullface(net.minecraft.core.Direction.DOWN);
                                default -> face.uvs(0, 16 - height, 16, 16).cullface(direction);
                            }
                            face.texture(TextureSlot.TEXTURE);
                        }))
                .build();
    }

    /** Glutspalte: Vulkangestein mit Schlot oben; der Schlot leuchtet von selbst. */
    private static void emberVent(BlockModelGenerators blocks, Block vent) {
        ModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.TOP)
                .requiredTextureSlot(TextureSlot.BOTTOM)
                .requiredTextureSlot(GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                        .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).cullface(direction)
                                .texture(switch (direction) {
                                    case UP -> TextureSlot.TOP;
                                    case DOWN -> TextureSlot.BOTTOM;
                                    default -> TextureSlot.SIDE;
                                })))
                .element(element -> element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                        .face(net.minecraft.core.Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(GLOW)
                                .cullface(net.minecraft.core.Direction.UP)))
                .build();
        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(vent))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(vent, "_top"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(ModBlocks.VOLCANIC_ROCK.base().get()))
                .put(GLOW, TextureMapping.getBlockTexture(vent, "_top_glow"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(vent));
        Identifier model = template.create(vent, textures, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(vent, BlockModelGenerators.plainVariant(model)));
    }

    /**
     * Drachenkompass: 32 Nadelstellungen wie beim Vanilla-Kompass (die Nadel dreht das Spiel über den
     * {@code lodestone_tracker}); darüber der Edelstein, getönt in der Farbe des gesuchten Drachen.
     */
    private static void dragonCompass(ItemModelGenerators items, Item compass) {
        java.util.List<net.minecraft.client.renderer.item.RangeSelectItemModel.Entry> entries = new java.util.ArrayList<>();
        net.minecraft.client.renderer.item.ItemModel.Unbaked up = compassFrame(items, compass, 16);
        entries.add(net.minecraft.client.data.models.model.ItemModelUtils.override(up, 0.0F));
        for (int i = 1; i < 32; i++) {
            entries.add(net.minecraft.client.data.models.model.ItemModelUtils.override(
                    compassFrame(items, compass, net.minecraft.util.Mth.positiveModulo(i - 16, 32)), i - 0.5F));
        }
        entries.add(net.minecraft.client.data.models.model.ItemModelUtils.override(up, 31.5F));
        items.itemModelOutput.accept(compass, net.minecraft.client.data.models.model.ItemModelUtils.rangeSelect(
                new net.minecraft.client.renderer.item.properties.numeric.CompassAngle(true,
                        net.minecraft.client.renderer.item.properties.numeric.CompassAngleState.CompassTarget.LODESTONE),
                32.0F, entries));
    }

    private static net.minecraft.client.renderer.item.ItemModel.Unbaked compassFrame(ItemModelGenerators items, Item compass, int index) {
        String suffix = String.format(java.util.Locale.ROOT, "_%02d", index);
        Identifier model = ModelTemplates.TWO_LAYERED_ITEM.create(ModelLocationUtils.getModelLocation(compass, suffix),
                TextureMapping.layered(new Material(KingdomsAtWar.id("item/dragon_compass" + suffix)),
                        new Material(KingdomsAtWar.id("item/dragon_compass_gem"))),
                items.modelOutput);
        return net.minecraft.client.data.models.model.ItemModelUtils.tintedModel(model,
                net.minecraft.client.data.models.model.ItemModelUtils.constantTint(-1),
                new net.minecraft.client.color.item.CustomModelDataSource(0, 0xF2CC5A));
    }

    /** Münze: immer dieselbe einzelne Münze, egal wie viele im Stapel liegen (die Zahl steht ja daneben). */
    private static void coinModel(ItemModelGenerators items, Item coin, String name) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(coin),
                TextureMapping.layer0(new Material(KingdomsAtWar.id("item/" + name))), items.modelOutput);
        items.itemModelOutput.accept(coin, net.minecraft.client.data.models.model.ItemModelUtils.plainModel(model));
    }

    /** Stuhl, Bank, Tisch und Thron. */
    private static void furniture(BlockModelGenerators blocks) {
        Material oakPlanks = vanilla("oak_planks");
        TextureMapping oak = new TextureMapping().put(DecorModels.WOOD, oakPlanks).put(DecorModels.FRAME, vanilla("stripped_oak_log"))
                .put(TextureSlot.PARTICLE, oakPlanks);
        Block chair = ModBlocks.OAK_CHAIR.get();
        facing(blocks, chair, DecorModels.chair().create(chair, oak, blocks.modelOutput));
        Block bench = ModBlocks.OAK_BENCH.get();
        facing(blocks, bench, DecorModels.bench().create(bench, oak, blocks.modelOutput));

        // Tisch: Platte immer, Beine nur an Ecken ohne Nachbartisch
        Block table = ModBlocks.OAK_TABLE.get();
        MultiVariant top = BlockModelGenerators.plainVariant(DecorModels.tableTop().createWithSuffix(table, "_top", oak, blocks.modelOutput));
        MultiVariant leg = BlockModelGenerators.plainVariant(DecorModels.tableLeg().createWithSuffix(table, "_leg", oak, blocks.modelOutput));
        blocks.blockStateOutput.accept(MultiPartGenerator.multiPart(table)
                .with(top)
                .with(BlockModelGenerators.condition().term(TableBlock.NORTH, false).term(TableBlock.WEST, false), leg)
                .with(BlockModelGenerators.condition().term(TableBlock.NORTH, false).term(TableBlock.EAST, false),
                        leg.with(BlockModelGenerators.Y_ROT_90))
                .with(BlockModelGenerators.condition().term(TableBlock.SOUTH, false).term(TableBlock.EAST, false),
                        leg.with(BlockModelGenerators.Y_ROT_180))
                .with(BlockModelGenerators.condition().term(TableBlock.SOUTH, false).term(TableBlock.WEST, false),
                        leg.with(BlockModelGenerators.Y_ROT_270)));
        blocks.registerSimpleItemModel(table, DecorModels.tableItem().create(table, oak, blocks.modelOutput));

        Material darkOak = vanilla("dark_oak_planks");
        TextureMapping royal = new TextureMapping().put(DecorModels.WOOD, darkOak).put(DecorModels.CLOTH, vanilla("red_wool"))
                .put(DecorModels.GOLD, vanilla("gold_block")).put(TextureSlot.PARTICLE, darkOak);
        Block throne = ModBlocks.THRONE.get();
        facing(blocks, throne, DecorModels.throne().create(throne, royal, blocks.modelOutput));
    }

    /** Block, der sich nach der Blickrichtung beim Setzen dreht (Modell mit Blick nach Norden). */
    private static void facing(BlockModelGenerators blocks, Block block, Identifier model) {
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private static Material vanilla(String texture) {
        return new Material(Identifier.withDefaultNamespace("block/" + texture));
    }

    /** Rüstungsteile mit Rüstungsbesatz (Trims) aus dem Schmiedetisch. */
    static void armor(ItemModelGenerators items, Item helmet, Item chestplate, Item leggings, Item boots) {
        for (Item piece : java.util.List.of(helmet, chestplate, leggings, boots)) {
            ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(piece), TextureMapping.layer0(piece), items.modelOutput);
        }
        items.generateDynamicTrimmableArmorSet(helmet, chestplate, leggings, boots, null);
    }

    static void glowingCube(BlockModelGenerators blocks, Block block) {
        TextureMapping mapping = TextureMapping.cube(block).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE)
                .put(GLOW, TextureMapping.getBlockTexture(block, "_glow"));
        Identifier model = CUBE_GLOW.create(block, mapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    static void flat(ItemModelGenerators items, Item item) {
        items.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    static void ore(BlockModelGenerators blocks, DeferredBlock<? extends Block> ore, Block baseRock, String material) {
        ore(blocks, ore, TextureMapping.getBlockTexture(baseRock), material);
    }

    /** Erz aus Gestein mit eigener Textur ({@code base}) und durchsichtigem Einschluss {@code <material>_ore_overlay}. */
    static void ore(BlockModelGenerators blocks, DeferredBlock<? extends Block> ore, Material base, String material) {
        TextureMapping mapping = new TextureMapping()
                .put(BASE, base)
                .put(OVERLAY, new Material(KingdomsAtWar.id("block/" + material + "_ore_overlay")))
                .put(TextureSlot.PARTICLE, base);
        ModelTemplate template = ORE_OVERLAY;
        if (GLOWING_ORES.contains(material)) {
            mapping.put(GLOW, new Material(KingdomsAtWar.id("block/" + material + "_ore_glow")));
            template = ORE_OVERLAY_GLOW;
        }
        Identifier model = template.create(ore.get(), mapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ore.get(), BlockModelGenerators.plainVariant(model)));
    }
}
