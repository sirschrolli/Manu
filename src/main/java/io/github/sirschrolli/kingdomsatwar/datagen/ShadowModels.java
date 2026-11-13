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

    /** Die Seelenglocke (0.98): Krone, Schulter, Flanke, Schlagring (vier Wände) und Klöppel; im Mund das Seelenfeuer. */
    private static final TextureSlot BELL_LIP = TextureSlot.create("lip");
    private static final TextureSlot BELL_INSIDE = TextureSlot.create("inside");
    private static final TextureSlot BELL_CROWN = TextureSlot.create("crown");
    private static final TextureSlot BELL_MOUTH = TextureSlot.create("mouth");

    private static ModelTemplate soulBell(boolean lit) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE).requiredTextureSlot(TextureSlot.TOP).requiredTextureSlot(BELL_LIP)
                .requiredTextureSlot(BELL_INSIDE).requiredTextureSlot(BELL_CROWN).requiredTextureSlot(BELL_MOUTH)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        // die Krone (oben an den Balken des Glockenstuhls)
        b.element(e -> e.from(6, 13, 6).to(10, 16, 10).allFaces((dir, face) -> face.uvs(6, 0, 10, 3).texture(BELL_CROWN)));
        // die Schulter: Seiten aus den Zeilen 3–5 des Aufrisses, oben die Schulter von oben
        b.element(e -> e.from(4, 11, 4).to(12, 13, 12).allFaces((dir, face) -> {
            if (dir == net.minecraft.core.Direction.UP) {
                face.uvs(4, 4, 12, 12).texture(TextureSlot.TOP);
            } else if (dir == net.minecraft.core.Direction.DOWN) {
                face.uvs(4, 4, 12, 12).texture(BELL_INSIDE);
            } else {
                face.uvs(4, 3, 12, 5).texture(TextureSlot.SIDE);
            }
        }));
        // die Flanke mit dem Runenband; unten (zum Mund hin) das Seelenfeuer
        b.element(e -> {
            e.from(2, 4, 2).to(14, 11, 14).allFaces((dir, face) -> {
                if (dir == net.minecraft.core.Direction.UP) {
                    face.uvs(2, 2, 14, 14).texture(TextureSlot.TOP);
                } else if (dir == net.minecraft.core.Direction.DOWN) {
                    face.uvs(2, 2, 14, 14).texture(BELL_MOUTH);
                } else {
                    face.uvs(2, 5, 14, 12).texture(TextureSlot.SIDE);
                }
            });
            if (lit) {
                e.lightEmission(13);
            }
        });
        // der Schlagring: vier Wände (außen der Aufriss, innen das Dunkel, unten die Kante)
        int[][] walls = {{1, 2, 1, 15, 4, 2}, {1, 2, 14, 15, 4, 15}, {1, 2, 2, 2, 4, 14}, {14, 2, 2, 15, 4, 14}};
        for (int[] w : walls) {
            b.element(e -> e.from(w[0], w[1], w[2]).to(w[3], w[4], w[5]).allFaces((dir, face) -> {
                boolean outer = switch (dir) {
                    case NORTH -> w[2] == 1;
                    case SOUTH -> w[5] == 15;
                    case WEST -> w[0] == 1;
                    case EAST -> w[3] == 15;
                    default -> false;
                };
                if (dir.getAxis() == net.minecraft.core.Direction.Axis.Y) {
                    face.uvs(w[0], w[2], w[3], w[5]).texture(BELL_LIP);
                } else {
                    face.uvs(1, 12, 15, 14).texture(outer ? TextureSlot.SIDE : BELL_INSIDE);
                }
            }));
        }
        // der Klöppel
        b.element(e -> e.from(7, 0, 7).to(9, 4, 9).allFaces((dir, face) -> face.uvs(7, 10, 9, 14).texture(BELL_CROWN)));
        return b.build();
    }

    private static final ModelTemplate SOUL_BELL_LIT = soulBell(true);
    private static final ModelTemplate SOUL_BELL_COLD = soulBell(false);

    /** Die Seelenglocke: das Seelenfeuer im Mund brennt (der Hirte schläft oder kämpft) oder ist erloschen (besiegt). */
    private static void soulBell(BlockModelGenerators blocks) {
        Block bell = io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd.SOUL_BELL.get();
        TextureMapping lit = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(bell, "_side"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(bell, "_top"))
                .put(BELL_LIP, TextureMapping.getBlockTexture(bell, "_lip"))
                .put(BELL_INSIDE, TextureMapping.getBlockTexture(bell, "_inside"))
                .put(BELL_CROWN, TextureMapping.getBlockTexture(bell, "_crown"))
                .put(BELL_MOUTH, TextureMapping.getBlockTexture(bell, "_mouth"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(bell, "_side"));
        TextureMapping cold = lit.copyAndUpdate(BELL_MOUTH, TextureMapping.getBlockTexture(bell, "_mouth_cold"));
        Identifier litModel = SOUL_BELL_LIT.create(bell, lit, blocks.modelOutput);
        Identifier coldModel = SOUL_BELL_COLD.createWithSuffix(bell, "_cold", cold, blocks.modelOutput);
        blocks.blockStateOutput.accept(net.minecraft.client.data.models.blockstates.MultiVariantGenerator.dispatch(bell).with(
                net.minecraft.client.data.models.blockstates.PropertyDispatch.initial(
                        io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlock.PHASE).generate(phase ->
                        BlockModelGenerators.plainVariant(phase == io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlock.Phase.DEFEATED
                                ? coldModel : litModel))));
    }

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
        // Sternblüte (leuchtende Blüten), Mondfarn und Seelenschilf (zwei Blöcke hoch, oben leuchtend)
        blocks.createCrossBlockWithDefaultItem(ShadowBlocks.STAR_BLOSSOM.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        glowingDoublePlant(blocks, ShadowBlocks.MOON_FERN.get());
        glowingDoublePlant(blocks, ShadowBlocks.SOUL_REEDS.get());

        // Schattenholz: Tür, Falltür, Zaun, Zauntor
        blocks.createDoor(ShadowBlocks.SHADOW_DOOR.get());
        blocks.createOrientableTrapdoor(ShadowBlocks.SHADOW_TRAPDOOR.get());
        blocks.familyWithExistingFullBlock(ShadowBlocks.SHADOW_PLANKS.base().get())
                .fence(ShadowBlocks.SHADOW_FENCE.get())
                .fenceGate(ShadowBlocks.SHADOW_FENCE_GATE.get());

        // Ackerland, Pfad und Dämmerkorn
        Material soil = TextureMapping.getBlockTexture(ShadowBlocks.SHADOW_SOIL.get());
        blocks.createFarmland(ShadowBlocks.SHADOW_FARMLAND.get(), soil, soil, ModelTemplates.CUBE_BOTTOM_TOP_INDENTED);
        blocks.createRotatedVariantBlock(ShadowBlocks.SHADOW_PATH.get(),
                TexturedModel.CUBE_BOTTOM_TOP_INDENTED.updateTexture(m -> m.put(TextureSlot.BOTTOM, soil)));
        blocks.createCropBlock(ShadowBlocks.DUSKGRAIN.get(), net.minecraft.world.level.block.CropBlock.AGE, 0, 0, 1, 1, 2, 2, 2, 3);

        // Erze: im Schattenstein bzw. Tiefschattenstein (Einschluss als eigene Ebene)
        Material stone = TextureMapping.getBlockTexture(ShadowBlocks.SHADOW_STONE.base().get());
        Material deep = TextureMapping.getBlockTexture(ShadowBlocks.DEEP_SHADOW_STONE.get(), "_side");
        ModModelProvider.ore(blocks, ShadowBlocks.MOONSILVER_ORE, stone, "moonsilver");
        ModModelProvider.ore(blocks, ShadowBlocks.DEEP_MOONSILVER_ORE, deep, "moonsilver");
        ModModelProvider.ore(blocks, ShadowBlocks.NIGHTSTEEL_ORE, stone, "nightsteel");
        ModModelProvider.ore(blocks, ShadowBlocks.DEEP_NIGHTSTEEL_ORE, deep, "nightsteel");
        ModModelProvider.ore(blocks, ShadowBlocks.SOUL_CRYSTAL_ORE, deep, "soul_crystal");
        ModModelProvider.ore(blocks, ShadowBlocks.ABYSSITE_ORE, deep, "abyssite");
        // Dämmerquarz (0.98)
        ModModelProvider.ore(blocks, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ_ORE, stone, "dusk_quartz");
        ModModelProvider.ore(blocks, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DEEP_DUSK_QUARTZ_ORE, deep, "dusk_quartz");
        ModModelProvider.glowingCube(blocks, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ_BLOCK.get());
        ModModelProvider.flat(items, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_QUARTZ.get());
        ModModelProvider.armor(items, io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_HOOD.get(), io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_CLOAK.get(), io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_LEGGINGS.get(), io.github.sirschrolli.kingdomsatwar.registry.ShadowGear.DUSK_BOOTS.get());
        // der Seelenhirte (0.98): die Seelenglocke und sein Spawn-Ei (den Hirtenstab macht ArmoryModels)
        soulBell(blocks);
        ModModelProvider.flat(items, io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd.SOUL_SHEPHERD_SPAWN_EGG.get());

        // Speicherblöcke, Laterne, Setzling
        blocks.createTrivialCube(ShadowBlocks.MOONSILVER_BLOCK.get());
        blocks.createTrivialCube(ShadowBlocks.NIGHTSTEEL_BLOCK.get());
        ModModelProvider.glowingCube(blocks, ShadowBlocks.SOUL_CRYSTAL_BLOCK.get());
        ModModelProvider.glowingCube(blocks, ShadowBlocks.ABYSSITE_BLOCK.get());
        blocks.createLantern(ShadowBlocks.MOON_LANTERN.get());
        blocks.createPlantWithDefaultItem(ShadowBlocks.SHADOW_SAPLING.get(), ShadowBlocks.POTTED_SHADOW_SAPLING.get(),
                BlockModelGenerators.PlantType.NOT_TINTED);

        // Seelenbeeren und Glimmerranken
        blocks.createCrossBlock(ShadowBlocks.SOUL_BERRY_BUSH.get(), BlockModelGenerators.PlantType.NOT_TINTED,
                io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock.AGE, 0, 1, 2, 3);
        blocks.createGrowingPlant(ShadowBlocks.GLIMMER_VINES.get(), ShadowBlocks.GLIMMER_VINES_PLANT.get(),
                BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.registerSimpleFlatItemModel(ShadowBlocks.GLIMMER_VINES.get(), "_plant");

        // Gegenstände
        for (var item : java.util.List.of(ShadowItems.RAW_MOONSILVER, ShadowItems.MOONSILVER_INGOT, ShadowItems.MOONSILVER_NUGGET,
                ShadowItems.RAW_NIGHTSTEEL, ShadowItems.NIGHTSTEEL_INGOT, ShadowItems.SOUL_CRYSTAL, ShadowItems.ABYSSITE_SHARD,
                ShadowItems.ABYSSITE_INGOT, ShadowItems.NIGHT_CRYSTAL_SHARD, ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE,
                ShadowItems.SHADOW_ESSENCE, ShadowItems.SHADOW_HEART, ShadowItems.DUSKGRAIN, ShadowItems.DUSK_BREAD)) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : ShadowItems.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
        // die Werkzeuge aus Nachtstahl und Abyssit und die Mondsilberklinge: seit 0.88 in 3D (ToolsetModels)
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

    /**
     * Zwei Blöcke hohe Kreuzpflanze, deren obere Hälfte leuchtet: unten {@code <name>_bottom}, oben {@code <name>_top} mit
     * der Leuchtschicht {@code <name>_top_emissive}; als Gegenstand die obere Hälfte.
     */
    private static void glowingDoublePlant(BlockModelGenerators blocks, Block block) {
        Identifier bottom = ModelTemplates.CROSS.createWithSuffix(block, "_bottom",
                new TextureMapping().put(TextureSlot.CROSS, TextureMapping.getBlockTexture(block, "_bottom")), blocks.modelOutput);
        Identifier top = ModelTemplates.CROSS_EMISSIVE.createWithSuffix(block, "_top", new TextureMapping()
                .put(TextureSlot.CROSS, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.CROSS_EMISSIVE, TextureMapping.getBlockTexture(block, "_top_emissive")), blocks.modelOutput);
        blocks.createDoubleBlock(block, BlockModelGenerators.plainVariant(top), BlockModelGenerators.plainVariant(bottom));
        blocks.registerSimpleFlatItemModel(block, "_top");
    }
}
