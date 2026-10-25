package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplate;
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

    /** Ei-Form aus übereinander gestapelten Quadern. */
    private static final ModelTemplate DRAGON_EGG = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.ALL)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .ambientOcclusion(false)
            .element(element -> eggLayer(element, 3, 0, 13, 2))
            .element(element -> eggLayer(element, 2, 2, 14, 10))
            .element(element -> eggLayer(element, 3, 10, 13, 12))
            .element(element -> eggLayer(element, 4, 12, 12, 14))
            .element(element -> eggLayer(element, 5, 14, 11, 15))
            .element(element -> eggLayer(element, 6, 15, 10, 16))
            .build();

    public ModModelProvider(PackOutput output) {
        super(output, KingdomsAtWar.MODID);
    }

    private static void eggLayer(net.neoforged.neoforge.client.model.generators.template.ElementBuilder element,
            float min, float y1, float max, float y2) {
        element.from(min, y1, min).to(max, y2, max)
                .allFaces((direction, face) -> {
                    switch (direction) {
                        case UP, DOWN -> face.uvs(min, min, max, max);
                        default -> face.uvs(min, 16 - y2, max, 16 - y1);
                    }
                    face.texture(TextureSlot.ALL);
                });
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
        blocks.createTrivialCube(ModBlocks.AETHERIT_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.DRACONIUM_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.WYVERNIT_BLOCK.get());
        blocks.createTrivialCube(ModBlocks.STEEL_BLOCK.get());

        // --- Dracheneier ---
        for (DeferredBlock<ElementalDragonEggBlock> egg : ModBlocks.DRAGON_EGGS.values()) {
            Block block = egg.get();
            TextureMapping mapping = TextureMapping.cube(block).copySlot(TextureSlot.ALL, TextureSlot.PARTICLE);
            Identifier model = DRAGON_EGG.create(block, mapping, blocks.modelOutput);
            blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
        }

        // --- Feldfrüchte: 8 Altersstufen, 4 Texturen ---
        for (DeferredBlock<? extends CropBlock> crop : ModBlocks.CROPS) {
            blocks.createCropBlock(crop.get(), CropBlock.AGE, 0, 0, 1, 1, 2, 2, 2, 3);
        }

        // --- Zierblumen mit Blumentopf ---
        ModBlocks.FLOWERS.forEach((flower, pot) ->
                blocks.createPlantWithDefaultItem(flower.get(), pot.get(), BlockModelGenerators.PlantType.NOT_TINTED));

        // --- Gegenstände (flache 2D-Modelle) ---
        for (DeferredItem<?> item : java.util.List.of(
                ModItems.CARBON, ModItems.GEMSTONE, ModItems.KONTERIT, ModItems.AETHERIT, ModItems.DRACONIUM,
                ModItems.WYVERNIT, ModItems.STEEL_BLEND, ModItems.STEEL_INGOT, ModItems.DRAGON_EGG_FRAGMENT,
                ModItems.DRAGON_FOOD, ModItems.TOMATO_SEEDS, ModItems.TOMATO, ModItems.CABBAGE_SEEDS, ModItems.CABBAGE,
                ModItems.ONION, ModItems.BARLEY_SEEDS, ModItems.BARLEY, ModItems.BREAD_ROLL, ModItems.VEGETABLE_STEW,
                ModItems.MEAT_PIE, ModItems.APPLE_PIE)) {
            flat(items, item.get());
        }
        for (DeferredItem<Item> scale : ModItems.DRAGON_SCALES.values()) {
            flat(items, scale.get());
        }
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
        Identifier model = ORE_OVERLAY.create(ore.get(), mapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ore.get(), BlockModelGenerators.plainVariant(model)));
    }
}
