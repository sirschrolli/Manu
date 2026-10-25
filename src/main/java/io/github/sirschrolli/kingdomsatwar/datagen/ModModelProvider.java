package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
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
    private static final java.util.Set<String> GLOWING_ORES = java.util.Set.of("aetherit", "draconium");

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

        // --- Feldfrüchte: 8 Altersstufen, 4 Texturen ---
        for (DeferredBlock<? extends CropBlock> crop : ModBlocks.CROPS) {
            blocks.createCropBlock(crop.get(), CropBlock.AGE, 0, 0, 1, 1, 2, 2, 2, 3);
        }

        // --- Zierblumen mit Blumentopf ---
        ModBlocks.FLOWERS.forEach((flower, pot) ->
                blocks.createPlantWithDefaultItem(flower.get(), pot.get(), BlockModelGenerators.PlantType.NOT_TINTED));

        // --- Gegenstände (flache 2D-Modelle) ---
        // Samen und Zwiebel bekommen ihr Modell schon über createCropBlock (Gegenstand der Feldfrucht).
        for (DeferredItem<?> item : java.util.List.of(
                ModItems.CARBON, ModItems.GEMSTONE, ModItems.KONTERIT, ModItems.AETHERIT, ModItems.DRACONIUM,
                ModItems.WYVERNIT, ModItems.STEEL_BLEND, ModItems.STEEL_INGOT, ModItems.DRAGON_EGG_FRAGMENT,
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
        for (DeferredItem<?> spawnEgg : ModItems.SOLDIER_SPAWN_EGGS.values()) {
            flat(items, spawnEgg.get());
        }
        flat(items, ModItems.BANDIT_SPAWN_EGG.get());
        flat(items, ModItems.BANDIT_ARCHER_SPAWN_EGG.get());
        items.generateFlatItem(ModItems.COMMAND_BATON.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

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
        for (DeferredItem<?> item : java.util.List.of(ModItems.WILD_BOAR_SPAWN_EGG, ModItems.DEER_SPAWN_EGG, ModItems.GOOSE_SPAWN_EGG,
                ModItems.WILL_O_WISP_SPAWN_EGG, ModItems.CAVE_TROLL_SPAWN_EGG, ModItems.LINDWORM_SPAWN_EGG, ModItems.VENISON,
                ModItems.COOKED_VENISON, ModItems.WISP_DUST)) {
            flat(items, item.get());
        }
        flat(items, ModItems.DRACONIUM_DRAGON_ARMOR.get());
    }

    /** Rüstungsteile mit Rüstungsbesatz (Trims) aus dem Schmiedetisch. */
    private static void armor(ItemModelGenerators items, Item helmet, Item chestplate, Item leggings, Item boots) {
        for (Item piece : java.util.List.of(helmet, chestplate, leggings, boots)) {
            ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(piece), TextureMapping.layer0(piece), items.modelOutput);
        }
        items.generateDynamicTrimmableArmorSet(helmet, chestplate, leggings, boots, null);
    }

    private static void glowingCube(BlockModelGenerators blocks, Block block) {
        TextureMapping mapping = TextureMapping.cube(block).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE)
                .put(GLOW, TextureMapping.getBlockTexture(block, "_glow"));
        Identifier model = CUBE_GLOW.create(block, mapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    private static void flat(ItemModelGenerators items, Item item) {
        items.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private static void ore(BlockModelGenerators blocks, DeferredBlock<? extends Block> ore, Block baseRock, String material) {
        Material base = TextureMapping.getBlockTexture(baseRock);
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
