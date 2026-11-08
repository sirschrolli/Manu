package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.block.voidrealm.VoidRiftBlock;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
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

/** Modelle der Leere: Gestein, Marmor, Moos, Sternenstaub, Kristalle, Pflanzen und der Leerenriss. */
final class VoidModels {
    private VoidModels() {}

    private static final TextureSlot PORTAL = TextureSlot.create("portal");

    /** Der Riss: eine dünne, leuchtende Scheibe in der Mitte des Blocks (wie beim Netherportal). */
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
        blocks.createTrivialCube(VoidBlocks.CRACKED_VOID_STONE_BRICKS.get());
        blocks.createTrivialCube(VoidBlocks.CHISELED_VOID_STONE.get());
        blocks.createAxisAlignedPillarBlock(VoidBlocks.ASTRAL_PILLAR.get(), TexturedModel.COLUMN);
        ModModelProvider.glowingCube(blocks, VoidBlocks.CHISELED_ASTRAL_MARBLE.get());
        // Leerenmoos: wie Gras zufällig gedreht, damit die Fläche nicht gekachelt wirkt
        Block moss = VoidBlocks.VOID_MOSS.get();
        Identifier mossModel = TexturedModel.CUBE_BOTTOM_TOP.create(moss, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(moss,
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(mossModel))));
        ModModelProvider.glowingCube(blocks, VoidBlocks.STARDUST.get());

        Block crystal = VoidBlocks.VOID_CRYSTAL.get();
        Identifier crystalModel = ModelTemplates.CUBE_ALL.create(crystal,
                TextureMapping.cube(TextureMapping.getBlockTexture(crystal).withForceTranslucent(true)), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(crystal, BlockModelGenerators.plainVariant(crystalModel)));
        blocks.createCrossBlockWithDefaultItem(VoidBlocks.VOID_CRYSTAL_CLUSTER.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        ModModelProvider.glowingCube(blocks, VoidBlocks.CRYSTAL_FOLIAGE.get());
        blocks.createCrossBlockWithDefaultItem(VoidBlocks.VOID_BLOOM.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        blocks.createGrowingPlant(VoidBlocks.STAR_VINES.get(), VoidBlocks.STAR_VINES_PLANT.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        blocks.registerSimpleFlatItemModel(VoidBlocks.STAR_VINES.get(), "_plant");

        // der Riss: zwei Ausrichtungen, durchscheinende, bewegte Textur
        Block rift = VoidBlocks.VOID_RIFT.get();
        Material riftTexture = TextureMapping.getBlockTexture(rift).withForceTranslucent(true);
        TextureMapping riftMapping = new TextureMapping().put(PORTAL, riftTexture).put(TextureSlot.PARTICLE, riftTexture);
        Identifier ns = RIFT_NS.create(rift, riftMapping, blocks.modelOutput);
        Identifier ew = RIFT_EW.create(rift, riftMapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(rift).with(PropertyDispatch.initial(VoidRiftBlock.AXIS)
                .select(Direction.Axis.X, BlockModelGenerators.plainVariant(ns))
                .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(ew))));

        ModModelProvider.flat(items, VoidBlocks.VOID_SHARD.get());
        ModModelProvider.flat(items, VoidBlocks.SHATTERED_CROWN.get());
        ModModelProvider.glowingCube(blocks, VoidBlocks.THRONE_SEAL.get());
        for (var egg : VoidBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }
}
