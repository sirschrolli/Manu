package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.block.fae.FaePortalBlock;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/** Block- und Gegenstandsmodelle des Feenreichs (die Baustoff-Sets erledigt {@link ModModelProvider} selbst). */
final class FaeModels {
    private FaeModels() {}

    private static final TextureSlot GRASS_OVERLAY = TextureSlot.create("overlay");
    static final TextureSlot PORTAL = TextureSlot.create("portal");
    private static final TextureSlot PAD = TextureSlot.create("pad");
    private static final TextureSlot FLOWER = TextureSlot.create("flower");
    private static final TextureSlot FLOWER_GLOW = TextureSlot.create("flower_glow");

    /** Wie das Gras der Oberwelt: Oberseite und Seitenstreifen nehmen die Grasfarbe des Bioms an. */
    private static final ModelTemplate GRASS_BLOCK = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/grass_block"))
            .requiredTextureSlot(TextureSlot.BOTTOM)
            .requiredTextureSlot(TextureSlot.TOP)
            .requiredTextureSlot(TextureSlot.SIDE)
            .requiredTextureSlot(GRASS_OVERLAY)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();

    /** Farben im Inventar (dort gibt es kein Biom). */
    static final int GRASS_ITEM_TINT = 0xFF8FE6B4;

    /** Der Schleier des Portals: eine dünne, leuchtende Scheibe in der Mitte des Blocks (wie beim Netherportal). */
    static final ModelTemplate PORTAL_NS = portalTemplate(true);
    static final ModelTemplate PORTAL_EW = portalTemplate(false);

    private static ModelTemplate portalTemplate(boolean northSouth) {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(PORTAL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .suffix(northSouth ? "_ns" : "_ew")
                .element(element -> {
                    if (northSouth) {
                        element.from(0, 0, 6).to(16, 16, 10).lightEmission(15).ambientOcclusion(false)
                                .face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(PORTAL))
                                .face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(PORTAL));
                    } else {
                        element.from(6, 0, 0).to(10, 16, 16).lightEmission(15).ambientOcclusion(false)
                                .face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(PORTAL))
                                .face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(PORTAL));
                    }
                })
                .build();
    }

    /** Mondlilie: flaches Blatt knapp über dem Wasser, darauf eine kleine, leuchtende Kreuzblüte. */
    private static final ModelTemplate MOON_LILY = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(PAD)
            .requiredTextureSlot(FLOWER)
            .requiredTextureSlot(FLOWER_GLOW)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .ambientOcclusion(false)
            .element(e -> e.from(0, 0.25f, 0).to(16, 0.25f, 16)
                    .face(Direction.UP, f -> f.uvs(16, 0, 0, 16).texture(PAD))
                    .face(Direction.DOWN, f -> f.uvs(16, 16, 0, 0).texture(PAD)))
            .element(e -> flowerPlane(e, true, FLOWER, false))
            .element(e -> flowerPlane(e, false, FLOWER, false))
            .element(e -> flowerPlane(e, true, FLOWER_GLOW, true))
            .element(e -> flowerPlane(e, false, FLOWER_GLOW, true))
            .build();

    private static void flowerPlane(net.neoforged.neoforge.client.model.generators.template.ElementBuilder e, boolean first,
            TextureSlot slot, boolean glow) {
        if (first) {
            e.from(3, 0.3f, 8).to(13, 10.3f, 8).rotation(r -> r.origin(8, 8, 8).singleAxis(Direction.Axis.Y, 45))
                    .face(Direction.NORTH, f -> f.uvs(0, 0, 16, 16).texture(slot))
                    .face(Direction.SOUTH, f -> f.uvs(0, 0, 16, 16).texture(slot));
        } else {
            e.from(8, 0.3f, 3).to(8, 10.3f, 13).rotation(r -> r.origin(8, 8, 8).singleAxis(Direction.Axis.Y, 45))
                    .face(Direction.WEST, f -> f.uvs(0, 0, 16, 16).texture(slot))
                    .face(Direction.EAST, f -> f.uvs(0, 0, 16, 16).texture(slot));
        }
        if (glow) {
            e.lightEmission(15);
        }
    }

    private static final TextureSlot SILK = TextureSlot.create("silk");
    private static final TextureSlot SILK_GLOW = TextureSlot.create("silk_glow");

    /**
     * Kokon der Mottenkönigin: eine Spindel aus übereinander gestapelten Quadern (fast zwei Blöcke hoch), die Seitenflächen
     * teilen sich die Höhe der Textur (die Wicklung läuft so über die ganze Spindel). Darüber liegt dieselbe Spindel noch
     * einmal mit der pulsierenden Leuchtschicht.
     */
    private static final int[][] COCOON_PROFILE = {
            // von unten nach oben: {y0, y1, halbe Breite × 2}
            {0, 2, 6}, {2, 5, 10}, {5, 10, 13}, {10, 18, 14}, {18, 23, 12}, {23, 27, 9}, {27, 30, 5}, {30, 32, 2}};

    private static final ModelTemplate COCOON = cocoonTemplate();

    private static ModelTemplate cocoonTemplate() {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(SILK)
                .requiredTextureSlot(SILK_GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        for (boolean glow : new boolean[] {false, true}) {
            for (int[] layer : COCOON_PROFILE) {
                float y0 = layer[0];
                float y1 = layer[1];
                float w = layer[2];
                float a = 8.0f - w / 2.0f;
                float b = 8.0f + w / 2.0f;
                // die Textur deckt die ganze Höhe (32) ab: v läuft von oben (0) nach unten (16)
                float v0 = (32.0f - y1) / 2.0f;
                float v1 = (32.0f - y0) / 2.0f;
                TextureSlot slot = glow ? SILK_GLOW : SILK;
                builder.element(e -> {
                    e.from(a, y0, a).to(b, y1, b);
                    for (Direction side : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                        e.face(side, f -> f.uvs(a, v0, b, v1).texture(slot));
                    }
                    e.face(Direction.UP, f -> f.uvs(a, a, b, b).texture(slot));
                    e.face(Direction.DOWN, f -> f.uvs(a, a, b, b).texture(slot));
                    if (glow) {
                        e.lightEmission(15);
                    }
                });
            }
        }
        return builder.build();
    }

    /** Pilzhut: oben und an den Seiten die Haut mit Tupfen, unten die Lamellen. */
    private static void cap(BlockModelGenerators blocks, Block cap) {
        blocks.createTrivialBlock(cap, TexturedModel.CUBE_BOTTOM_TOP);
    }

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        // Gestein
        blocks.createTrivialCube(FaeBlocks.MOSSY_FAESTONE_BRICKS.get());
        ModModelProvider.glowingCube(blocks, FaeBlocks.CHISELED_MOONSTONE.get());

        // Böden: Feengras (getönt), Feenerde, Feenmoos (zufällig gedreht, mit leuchtenden Sporen)
        blocks.createTrivialCube(FaeBlocks.FAE_SOIL.get());
        Block grass = FaeBlocks.FAE_GRASS.get();
        TextureMapping grassMapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(FaeBlocks.FAE_SOIL.get()))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(grass, "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(grass, "_side"))
                .put(GRASS_OVERLAY, TextureMapping.getBlockTexture(grass, "_side_overlay"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(FaeBlocks.FAE_SOIL.get()));
        Identifier grassModel = GRASS_BLOCK.create(grass, grassMapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(grass,
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(grassModel))));
        blocks.itemModelOutput.accept(grass.asItem(), ItemModelUtils.tintedModel(grassModel, ItemModelUtils.constantTint(GRASS_ITEM_TINT)));
        Block moss = FaeBlocks.FAE_MOSS.get();
        Identifier mossModel = ModModelProvider.glowingCubeModel(blocks, moss);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(moss,
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(mossModel))));

        // Glimmerholz
        blocks.woodProvider(FaeBlocks.GLIMMERWOOD_LOG.get()).logWithHorizontal(FaeBlocks.GLIMMERWOOD_LOG.get())
                .wood(FaeBlocks.GLIMMERWOOD_WOOD.get());
        blocks.woodProvider(FaeBlocks.STRIPPED_GLIMMERWOOD_LOG.get()).logWithHorizontal(FaeBlocks.STRIPPED_GLIMMERWOOD_LOG.get())
                .wood(FaeBlocks.STRIPPED_GLIMMERWOOD_WOOD.get());
        blocks.createTrivialBlock(FaeBlocks.ROSE_GLIMMER_LEAVES.get(), TexturedModel.LEAVES);
        blocks.createTrivialBlock(FaeBlocks.AZURE_GLIMMER_LEAVES.get(), TexturedModel.LEAVES);
        blocks.createPlantWithDefaultItem(FaeBlocks.GLIMMERWOOD_SAPLING.get(), FaeBlocks.POTTED_GLIMMERWOOD_SAPLING.get(),
                BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.createDoor(FaeBlocks.GLIMMERWOOD_DOOR.get());
        blocks.createOrientableTrapdoor(FaeBlocks.GLIMMERWOOD_TRAPDOOR.get());
        blocks.familyWithExistingFullBlock(FaeBlocks.GLIMMERWOOD_PLANKS.base().get())
                .fence(FaeBlocks.GLIMMERWOOD_FENCE.get())
                .fenceGate(FaeBlocks.GLIMMERWOOD_FENCE_GATE.get());

        // Riesenpilze
        blocks.createRotatedPillarWithHorizontalVariant(FaeBlocks.FAE_MUSHROOM_STEM.get(), TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        cap(blocks, FaeBlocks.ROSE_MUSHROOM_CAP.get());
        cap(blocks, FaeBlocks.AZURE_MUSHROOM_CAP.get());
        cap(blocks, FaeBlocks.VIOLET_MUSHROOM_CAP.get());

        // Pflanzen
        Block sprouts = FaeBlocks.FAE_SPROUTS.get();
        blocks.createCrossBlock(sprouts, BlockModelGenerators.PlantType.TINTED);
        blocks.itemModelOutput.accept(sprouts.asItem(), ItemModelUtils.tintedModel(
                blocks.createFlatItemModel(sprouts.asItem(), TextureMapping.getBlockTexture(sprouts)), ItemModelUtils.constantTint(GRASS_ITEM_TINT)));
        blocks.createCrossBlockWithDefaultItem(FaeBlocks.MOONBELL.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        blocks.createCrossBlockWithDefaultItem(FaeBlocks.FAE_ROSE.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        blocks.createCrossBlockWithDefaultItem(FaeBlocks.LANTERN_MUSHROOM.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        blocks.createCrossBlockWithDefaultItem(FaeBlocks.THORNBRIAR.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        glowingDoublePlant(blocks, FaeBlocks.STARLACE.get());
        blocks.createGrowingPlant(FaeBlocks.GLOW_VINES.get(), FaeBlocks.GLOW_VINES_PLANT.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        blocks.registerSimpleFlatItemModel(FaeBlocks.GLOW_VINES.get(), "_plant");
        blocks.createTrivialBlock(FaeBlocks.THORN_HEDGE.get(), TexturedModel.LEAVES);

        // Mondlilie: zufällig gedreht wie die Seerose
        Block lily = FaeBlocks.MOON_LILY.get();
        Material pad = TextureMapping.getBlockTexture(lily);
        Identifier lilyModel = MOON_LILY.create(lily, new TextureMapping().put(PAD, pad).put(TextureSlot.PARTICLE, pad)
                .put(FLOWER, TextureMapping.getBlockTexture(lily, "_flower"))
                .put(FLOWER_GLOW, TextureMapping.getBlockTexture(lily, "_flower_emissive")), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(lily,
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(lilyModel))));
        ModModelProvider.flat(items, FaeBlocks.MOON_LILY_ITEM.get());

        // das Portal: zwei Ausrichtungen, durchscheinende, bewegte Textur
        Block portal = FaeBlocks.FAE_PORTAL.get();
        Material portalTexture = TextureMapping.getBlockTexture(portal).withForceTranslucent(true);
        TextureMapping portalMapping = new TextureMapping().put(PORTAL, portalTexture).put(TextureSlot.PARTICLE, portalTexture);
        Identifier ns = PORTAL_NS.create(portal, portalMapping, blocks.modelOutput);
        Identifier ew = PORTAL_EW.create(portal, portalMapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(portal).with(PropertyDispatch.initial(FaePortalBlock.AXIS)
                .select(Direction.Axis.X, BlockModelGenerators.plainVariant(ns))
                .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(ew))));

        // der Mondhain: Mottenseide (leuchtende Glanzpunkte, zufällig gedreht), Seidenfäden, der Kokon
        Block silk = FaeBlocks.MOTH_SILK.get();
        Identifier silkModel = ModModelProvider.glowingCubeModel(blocks, silk);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(silk,
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(silkModel))));
        blocks.createCrossBlockWithDefaultItem(FaeBlocks.SILK_STRANDS.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        Block cocoon = FaeBlocks.MOTH_COCOON.get();
        Material cocoonTexture = TextureMapping.getBlockTexture(cocoon);
        Identifier cocoonModel = COCOON.create(cocoon, new TextureMapping().put(SILK, cocoonTexture).put(TextureSlot.PARTICLE, cocoonTexture)
                .put(SILK_GLOW, TextureMapping.getBlockTexture(cocoon, "_glow")), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(cocoon, BlockModelGenerators.plainVariant(cocoonModel)));
        ModModelProvider.flat(items, FaeBlocks.MOTH_COCOON.get().asItem());
        items.generateElytra(FaeBlocks.FAE_ELYTRA.get());

        // Gegenstände
        ModModelProvider.flat(items, FaeBlocks.FAE_DUST.get());
        for (var egg : FaeBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
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
