package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.block.abyss.AbyssRiftBlock;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/** Modelle des Abgrunds: Chaosgestein, Weltennaht, die Kristalle der verdrehten Schwerkraft und der Abgrundriss. */
final class AbyssModels {
    private AbyssModels() {}

    private static final TextureSlot PORTAL = TextureSlot.create("portal");
    private static final ModelTemplate RIFT_NS = riftTemplate(true);
    private static final ModelTemplate RIFT_EW = riftTemplate(false);

    private static ModelTemplate riftTemplate(boolean northSouth) {
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

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createTrivialCube(AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS.get());
        ModModelProvider.glowingCube(blocks, AbyssBlocks.CHISELED_CHAOS_STONE.get());
        ModModelProvider.glowingCube(blocks, AbyssBlocks.WORLD_SEAM.get());
        crystal(blocks, AbyssBlocks.FLOAT_CRYSTAL.get());
        crystal(blocks, AbyssBlocks.LIFT_CRYSTAL.get());
        // der Schlund der Welten: Kronensplitter und Siegel (beide ohne Gegenstand)
        crystalBlock(blocks, AbyssBlocks.SEAL_SHARD.get());
        ModModelProvider.glowingCube(blocks, AbyssBlocks.HOARD_SEAL.get());

        Block rift = AbyssBlocks.ABYSS_RIFT.get();
        Material riftTexture = TextureMapping.getBlockTexture(rift).withForceTranslucent(true);
        TextureMapping riftMapping = new TextureMapping().put(PORTAL, riftTexture).put(TextureSlot.PARTICLE, riftTexture);
        Identifier ns = RIFT_NS.create(rift, riftMapping, blocks.modelOutput);
        Identifier ew = RIFT_EW.create(rift, riftMapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(rift).with(PropertyDispatch.initial(AbyssRiftBlock.AXIS)
                .select(Direction.Axis.X, BlockModelGenerators.plainVariant(ns))
                .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(ew))));

        ModModelProvider.flat(items, AbyssBlocks.RIFT_KEY.get());
    }

    /** Eine Kristalldruse mit Leuchtebene, nach ihrer Wuchsrichtung gedreht (wie eine Amethystdruse). */
    private static void crystal(BlockModelGenerators blocks, Block block) {
        crystalBlock(blocks, block);
        blocks.registerSimpleFlatItemModel(block);
    }

    /** Nur Blockmodell und -zustand einer Druse (für Blöcke ohne Gegenstand). */
    private static void crystalBlock(BlockModelGenerators blocks, Block block) {
        BlockModelGenerators.PlantType type = BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED;
        Identifier model = type.getCross().create(block, type.getTextureMapping(block), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
    }
}
