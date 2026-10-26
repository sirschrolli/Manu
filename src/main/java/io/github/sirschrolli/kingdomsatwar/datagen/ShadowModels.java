package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/** Block- und Gegenstandsmodelle der Schattenwelt (die Baustoff-Sets erledigt {@link ModModelProvider} selbst). */
final class ShadowModels {
    private ShadowModels() {}

    private static final TextureSlot GRASS_OVERLAY = TextureSlot.create("overlay");

    /** Wie das Gras der Oberwelt: Oberseite und Seitenstreifen nehmen die Grasfarbe des Bioms an. */
    private static final ModelTemplate GRASS_BLOCK = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/grass_block"))
            .requiredTextureSlot(TextureSlot.BOTTOM)
            .requiredTextureSlot(TextureSlot.TOP)
            .requiredTextureSlot(TextureSlot.SIDE)
            .requiredTextureSlot(GRASS_OVERLAY)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();

    /** Farben der Gegenstände im Inventar (dort gibt es kein Biom). */
    private static final int GRASS_ITEM_TINT = 0xFF6A4E9A;
    private static final int LEAVES_ITEM_TINT = 0xFF7448B0;

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        // Gestein
        blocks.createTrivialCube(ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get());
        blocks.createTrivialCube(ShadowBlocks.CHISELED_SHADOW_STONE.get());
        blocks.createAxisAlignedPillarBlock(ShadowBlocks.DEEP_SHADOW_STONE.get(), TexturedModel.COLUMN);

        // Böden
        blocks.createTrivialCube(ShadowBlocks.SHADOW_SOIL.get());
        grassBlock(blocks, ShadowBlocks.SHADOW_GRASS.get(), ShadowBlocks.SHADOW_SOIL.get());
        blocks.createTrivialCube(ShadowBlocks.SHADOW_SAND.get());
        blocks.createTrivialCube(ShadowBlocks.ASH_SNOW_BLOCK.get());
        ModModelProvider.ashLayer(blocks, ShadowBlocks.ASH_SNOW.get(), ShadowBlocks.ASH_SNOW_BLOCK.get());

        // Holz und Laub
        blocks.woodProvider(ShadowBlocks.SHADOW_LOG.get()).logWithHorizontal(ShadowBlocks.SHADOW_LOG.get()).wood(ShadowBlocks.SHADOW_WOOD.get());
        blocks.woodProvider(ShadowBlocks.STRIPPED_SHADOW_LOG.get()).logWithHorizontal(ShadowBlocks.STRIPPED_SHADOW_LOG.get())
                .wood(ShadowBlocks.STRIPPED_SHADOW_WOOD.get());
        Block leaves = ShadowBlocks.SHADOW_LEAVES.get();
        blocks.createTrivialBlock(leaves, TexturedModel.LEAVES);
        blocks.itemModelOutput.accept(leaves.asItem(),
                ItemModelUtils.tintedModel(ModelLocationUtils.getModelLocation(leaves), ItemModelUtils.constantTint(LEAVES_ITEM_TINT)));

        // Licht und Pflanzen
        Block crystal = ShadowBlocks.NIGHT_CRYSTAL.get();
        Identifier crystalModel = ModelTemplates.CUBE_ALL.create(crystal,
                TextureMapping.cube(TextureMapping.getBlockTexture(crystal).withForceTranslucent(true)), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(crystal, BlockModelGenerators.plainVariant(crystalModel)));
        blocks.createCrossBlockWithDefaultItem(ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        ModModelProvider.glowingCube(blocks, ShadowBlocks.GLIMMER_MOSS.get());
        blocks.createCrossBlockWithDefaultItem(ShadowBlocks.GHOSTCAP.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        Block sprouts = ShadowBlocks.SHADOW_SPROUTS.get();
        blocks.createCrossBlock(sprouts, BlockModelGenerators.PlantType.TINTED);
        blocks.itemModelOutput.accept(sprouts.asItem(), ItemModelUtils.tintedModel(
                blocks.createFlatItemModel(sprouts.asItem(), TextureMapping.getBlockTexture(sprouts)), ItemModelUtils.constantTint(GRASS_ITEM_TINT)));
        blocks.createCrossBlockWithDefaultItem(ShadowBlocks.PALE_GRASS.get(), BlockModelGenerators.PlantType.NOT_TINTED);

        // Erze: im Schattenstein bzw. Tiefschattenstein (Einschluss als eigene Ebene)
        Material stone = TextureMapping.getBlockTexture(ShadowBlocks.SHADOW_STONE.base().get());
        Material deep = TextureMapping.getBlockTexture(ShadowBlocks.DEEP_SHADOW_STONE.get(), "_side");
        ModModelProvider.ore(blocks, ShadowBlocks.MOONSILVER_ORE, stone, "moonsilver");
        ModModelProvider.ore(blocks, ShadowBlocks.DEEP_MOONSILVER_ORE, deep, "moonsilver");
        ModModelProvider.ore(blocks, ShadowBlocks.NIGHTSTEEL_ORE, stone, "nightsteel");
        ModModelProvider.ore(blocks, ShadowBlocks.DEEP_NIGHTSTEEL_ORE, deep, "nightsteel");
        ModModelProvider.ore(blocks, ShadowBlocks.SOUL_CRYSTAL_ORE, deep, "soul_crystal");
        ModModelProvider.ore(blocks, ShadowBlocks.ABYSSITE_ORE, deep, "abyssite");

        // Speicherblöcke, Laterne, Setzling
        blocks.createTrivialCube(ShadowBlocks.MOONSILVER_BLOCK.get());
        blocks.createTrivialCube(ShadowBlocks.NIGHTSTEEL_BLOCK.get());
        ModModelProvider.glowingCube(blocks, ShadowBlocks.SOUL_CRYSTAL_BLOCK.get());
        ModModelProvider.glowingCube(blocks, ShadowBlocks.ABYSSITE_BLOCK.get());
        blocks.createLantern(ShadowBlocks.MOON_LANTERN.get());
        blocks.createPlantWithDefaultItem(ShadowBlocks.SHADOW_SAPLING.get(), ShadowBlocks.POTTED_SHADOW_SAPLING.get(),
                BlockModelGenerators.PlantType.NOT_TINTED);

        // Gegenstände
        for (var item : java.util.List.of(ShadowItems.RAW_MOONSILVER, ShadowItems.MOONSILVER_INGOT, ShadowItems.MOONSILVER_NUGGET,
                ShadowItems.RAW_NIGHTSTEEL, ShadowItems.NIGHTSTEEL_INGOT, ShadowItems.SOUL_CRYSTAL, ShadowItems.ABYSSITE_SHARD,
                ShadowItems.ABYSSITE_INGOT, ShadowItems.NIGHT_CRYSTAL_SHARD, ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE)) {
            ModModelProvider.flat(items, item.get());
        }
        for (var tool : java.util.List.of(ShadowItems.NIGHTSTEEL_SWORD, ShadowItems.NIGHTSTEEL_PICKAXE, ShadowItems.NIGHTSTEEL_AXE,
                ShadowItems.NIGHTSTEEL_SHOVEL, ShadowItems.NIGHTSTEEL_HOE, ShadowItems.ABYSSITE_SWORD, ShadowItems.ABYSSITE_PICKAXE,
                ShadowItems.ABYSSITE_AXE, ShadowItems.ABYSSITE_SHOVEL, ShadowItems.ABYSSITE_HOE, ShadowItems.MOONSILVER_BLADE)) {
            items.generateFlatItem(tool.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        }
        ModModelProvider.armor(items, ShadowItems.NIGHTSTEEL_HELMET.get(), ShadowItems.NIGHTSTEEL_CHESTPLATE.get(),
                ShadowItems.NIGHTSTEEL_LEGGINGS.get(), ShadowItems.NIGHTSTEEL_BOOTS.get());
        ModModelProvider.armor(items, ShadowItems.ABYSSITE_HELMET.get(), ShadowItems.ABYSSITE_CHESTPLATE.get(),
                ShadowItems.ABYSSITE_LEGGINGS.get(), ShadowItems.ABYSSITE_BOOTS.get());
    }

    private static void grassBlock(BlockModelGenerators blocks, Block grass, Block soil) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(soil))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(grass, "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(grass, "_side"))
                .put(GRASS_OVERLAY, TextureMapping.getBlockTexture(grass, "_side_overlay"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(soil));
        Identifier model = GRASS_BLOCK.create(grass, mapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(grass, BlockModelGenerators.plainVariant(model)));
        blocks.itemModelOutput.accept(grass.asItem(), ItemModelUtils.tintedModel(model, ItemModelUtils.constantTint(GRASS_ITEM_TINT)));
    }
}
