package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.SealMarkBlock;
import io.github.sirschrolli.kingdomsatwar.block.SealPlateBlock;
import io.github.sirschrolli.kingdomsatwar.block.SealSocketBlock;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
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

/**
 * Modelle und Blockzustände des Siegelkreises: Siegelstein, Monolithen, Runen und Bodenzeichen (dunkel / leuchtend mit
 * selbstleuchtender Schicht), Sockel (leer / mit Splitter, nach der Mitte gedreht), die 25 Siegelplatten und das
 * Schattentor (eine wogende Fläche, vier Pixel unter dem Boden).
 */
final class SealModels {
    private static final TextureSlot GLOW = TextureSlot.create("glow");
    private static final TextureSlot FRONT = TextureSlot.FRONT;

    private SealModels() {}

    /** Seiten mit eigener Textur, oben/unten Ende; leuchtende Schicht auf den vier Seiten. */
    private static final ModelTemplate SIDES_GLOW = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.SIDE)
            .requiredTextureSlot(TextureSlot.END)
            .requiredTextureSlot(GLOW)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).cullface(direction)
                            .texture(direction.getAxis() == Direction.Axis.Y ? TextureSlot.END : TextureSlot.SIDE)))
            .element(element -> {
                element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    element.face(direction, face -> face.uvs(0, 0, 16, 16).cullface(direction).texture(GLOW));
                }
            })
            .build();

    /** Oben eigene Textur (mit leuchtender Schicht), Seiten und unten gleich. */
    private static final ModelTemplate TOP_GLOW = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.TOP)
            .requiredTextureSlot(TextureSlot.SIDE)
            .requiredTextureSlot(GLOW)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).cullface(direction)
                            .texture(direction == Direction.UP ? TextureSlot.TOP : TextureSlot.SIDE)))
            .element(element -> element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                    .face(Direction.UP, face -> face.uvs(0, 0, 16, 16).cullface(Direction.UP).texture(GLOW)))
            .build();

    /** Vorderseite (Norden) eigene Textur, sonst Seite/Ende – optional mit leuchtender Schicht vorn. */
    private static ModelTemplate front(boolean glow) {
        ExtendedModelTemplateBuilder builder = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(FRONT)
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.END)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                        .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).cullface(direction)
                                .texture(direction == Direction.NORTH ? FRONT
                                        : direction.getAxis() == Direction.Axis.Y ? TextureSlot.END : TextureSlot.SIDE)));
        if (glow) {
            builder.requiredTextureSlot(GLOW)
                    .element(element -> element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                            .face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).cullface(Direction.NORTH).texture(GLOW)));
        }
        return builder.build();
    }

    /** Das Schattentor: eine leuchtende Fläche in 12 Pixel Höhe. */
    private static final ModelTemplate PORTAL = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(TextureSlot.TEXTURE)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 12, 0).to(16, 12, 16).lightEmission(15).ambientOcclusion(false)
                    .face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.TEXTURE))
                    .face(Direction.DOWN, face -> face.uvs(0, 0, 16, 16).texture(TextureSlot.TEXTURE)))
            .build();

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createTrivialCube(ModBlocks.CRACKED_SEALSTONE_BRICKS.get());
        blocks.createTrivialCube(ModBlocks.MOSSY_SEALSTONE_BRICKS.get());
        blocks.createTrivialCube(ModBlocks.POLISHED_SEALSTONE.get());
        blocks.createTrivialCube(ModBlocks.CHISELED_SEALSTONE.get());

        Block monolith = ModBlocks.SEAL_MONOLITH.get();
        Material monolithSide = tex("seal_monolith");
        Material monolithTop = tex("seal_monolith_top");
        blocks.createTrivialBlock(monolith, TexturedModel.COLUMN.updateTexture(t -> t.put(TextureSlot.SIDE, monolithSide)
                .put(TextureSlot.END, monolithTop)));

        // Runen am Monolithen
        Block rune = ModBlocks.SEAL_RUNE.get();
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(rune).with(PropertyDispatch.initial(SealMarkBlock.ELEMENT, SealMarkBlock.LIT)
                .generate((element, lit) -> {
                    String name = "seal_rune_" + element.id();
                    Identifier model = lit
                            ? SIDES_GLOW.createWithSuffix(rune, "_" + element.id() + "_lit", new TextureMapping()
                                    .put(TextureSlot.SIDE, tex(name + "_lit")).put(TextureSlot.END, monolithTop)
                                    .put(GLOW, tex(name + "_glow")).put(TextureSlot.PARTICLE, tex(name + "_lit")), blocks.modelOutput)
                            : ModelTemplates.CUBE_COLUMN.createWithSuffix(rune, "_" + element.id(),
                                    TextureMapping.column(tex(name), monolithTop), blocks.modelOutput);
                    return BlockModelGenerators.plainVariant(model);
                })));

        // Zeichen im Boden
        Block inlay = ModBlocks.SEAL_INLAY.get();
        Material polished = TextureMapping.getBlockTexture(ModBlocks.POLISHED_SEALSTONE.get());
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(inlay).with(PropertyDispatch.initial(SealMarkBlock.ELEMENT, SealMarkBlock.LIT)
                .generate((element, lit) -> {
                    String name = "seal_inlay_" + element.id();
                    Identifier model = lit
                            ? TOP_GLOW.createWithSuffix(inlay, "_" + element.id() + "_lit", new TextureMapping()
                                    .put(TextureSlot.TOP, tex(name + "_lit")).put(TextureSlot.SIDE, polished)
                                    .put(GLOW, tex(name + "_glow")).put(TextureSlot.PARTICLE, polished), blocks.modelOutput)
                            : ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(inlay, "_" + element.id(), new TextureMapping()
                                    .put(TextureSlot.TOP, tex(name)).put(TextureSlot.SIDE, polished).put(TextureSlot.BOTTOM, polished),
                                    blocks.modelOutput);
                    return BlockModelGenerators.plainVariant(model);
                })));

        // Sockel: vorn die Nische, zur Mitte gedreht
        Block socket = ModBlocks.SEAL_SOCKET.get();
        ModelTemplate empty = front(false);
        ModelTemplate filled = front(true);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(socket)
                .with(PropertyDispatch.initial(SealSocketBlock.ELEMENT, SealSocketBlock.FILLED).generate((element, full) -> {
                    String name = "seal_socket_" + element.id();
                    TextureMapping mapping = new TextureMapping().put(TextureSlot.SIDE, monolithSide).put(TextureSlot.END, monolithTop)
                            .put(TextureSlot.PARTICLE, monolithSide);
                    Identifier model = full
                            ? filled.createWithSuffix(socket, "_" + element.id() + "_filled", mapping.put(FRONT, tex(name + "_filled"))
                                    .put(GLOW, tex(name + "_filled_glow")), blocks.modelOutput)
                            : empty.createWithSuffix(socket, "_" + element.id(), mapping.put(FRONT, tex(name)), blocks.modelOutput);
                    return BlockModelGenerators.plainVariant(model);
                }))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        // Die 25 Platten des Siegels
        Block plate = ModBlocks.SEAL_PLATE.get();
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(plate).with(PropertyDispatch.initial(SealPlateBlock.PART).generate(part -> {
            String name = String.format(java.util.Locale.ROOT, "seal_plate_%02d", part);
            Identifier model = TOP_GLOW.createWithSuffix(plate, String.format(java.util.Locale.ROOT, "_%02d", part), new TextureMapping()
                    .put(TextureSlot.TOP, tex(name)).put(TextureSlot.SIDE, polished).put(GLOW, tex(name + "_glow"))
                    .put(TextureSlot.PARTICLE, tex(name)), blocks.modelOutput);
            return BlockModelGenerators.plainVariant(model);
        })));

        // Schattentor
        Block portal = ModBlocks.SHADOW_PORTAL.get();
        Identifier portalModel = PORTAL.create(portal, new TextureMapping().put(TextureSlot.TEXTURE, tex("shadow_portal"))
                .put(TextureSlot.PARTICLE, tex("shadow_portal")), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(portal, BlockModelGenerators.plainVariant(portalModel)));

        // Gegenstände
        items.generateFlatItem(ModItems.CHRONICLE.get(), ModelTemplates.FLAT_ITEM);
        for (DragonType type : DragonType.values()) {
            items.generateFlatItem(ModItems.SEAL_SHARDS.get(type).get(), ModelTemplates.FLAT_ITEM);
        }
    }

    private static Material tex(String name) {
        return new Material(KingdomsAtWar.id("block/" + name));
    }
}
