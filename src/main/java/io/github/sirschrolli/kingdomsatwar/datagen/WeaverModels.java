package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlock;
import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import java.util.List;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle des Sternennetzes und der Sternenweberin (0.99): Sternenseide, Sternengewebe (durchsichtige Maschen), Sternennetz
 * (über Kreuz, leuchtend), der Sternenkokon (ein länglicher, umwickelter Kokon an einem Faden), das Sternengelege (ein Bett
 * aus Seide mit sieben Eiern – sie leuchten, solange die Weberin lebt) und der Sternenbogen (im Inventar das Bild, in der Hand
 * plastisch; gespannt in drei Stufen).
 */
final class WeaverModels {
    private WeaverModels() {}

    private static final TextureSlot EGG = TextureSlot.create("egg");
    private static final TextureSlot END = TextureSlot.create("end");
    private static final ModelTemplate COCOON = cocoon();
    private static final ModelTemplate NEST_LIT = nest(true);
    private static final ModelTemplate NEST_COLD = nest(false);

    /** Der Kokon: ein länglicher Leib, oben ein Knoten und der Faden, an dem er hängt, unten eine Spitze. */
    private static ModelTemplate cocoon() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE).requiredTextureSlot(END).requiredTextureSlot(TextureSlot.PARTICLE);
        b.element(e -> e.from(5, 2, 5).to(11, 13, 11).allFaces((dir, face) -> {
            if (dir.getAxis() == Direction.Axis.Y) {
                face.uvs(5, 5, 11, 11).texture(END);
            } else {
                face.uvs(5, 2, 11, 13).texture(TextureSlot.SIDE);
            }
        }));
        b.element(e -> e.from(6, 0, 6).to(10, 2, 10).allFaces((dir, face) -> face.uvs(6, 6, 10, 10).texture(END)));
        b.element(e -> e.from(6, 13, 6).to(10, 15, 10).allFaces((dir, face) -> face.uvs(6, 6, 10, 10).texture(END)));
        b.element(e -> e.from(7.5f, 15, 7.5f).to(8.5f, 16, 8.5f).allFaces((dir, face) -> face.uvs(7, 7, 8, 8).texture(TextureSlot.SIDE)));
        return b.build();
    }

    /** Das Gelege: ein flaches Bett aus wirrer Seide, darauf sieben Eier verschiedener Größe (leuchtend oder erloschen). */
    private static ModelTemplate nest(boolean lit) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.TOP).requiredTextureSlot(EGG).requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        b.element(e -> e.from(1, 0, 1).to(15, 1.5f, 15).allFaces((dir, face) -> face.uvs(1, 1, 15, 2.5f).texture(TextureSlot.TOP)));
        b.element(e -> e.from(2.5f, 1.5f, 2.5f).to(13.5f, 2.5f, 13.5f).allFaces((dir, face) -> face.uvs(2, 2, 13, 3).texture(TextureSlot.TOP)));
        // die Eier: (x, z, Größe, Höhe über dem Bett)
        float[][] eggs = {{5.0f, 5.5f, 4.0f, 0.0f}, {9.5f, 4.5f, 3.5f, 0.0f}, {10.5f, 9.5f, 4.0f, 0.0f}, {5.5f, 10.5f, 3.5f, 0.0f},
                {8.0f, 8.0f, 4.5f, 1.2f}, {3.5f, 8.0f, 2.5f, 0.0f}, {12.5f, 7.0f, 2.5f, 0.0f}};
        for (float[] egg : eggs) {
            float h = egg[2] / 2.0f;
            float y0 = 2.5f + egg[3];
            b.element(e -> {
                e.from(egg[0] - h, y0, egg[1] - h).to(egg[0] + h, y0 + egg[2] * 1.15f, egg[1] + h)
                        .allFaces((dir, face) -> face.uvs(0, 0, egg[2] * 2.0f, egg[2] * 2.0f).texture(EGG));
                if (lit) {
                    e.lightEmission(12);
                }
            });
        }
        return b.build();
    }

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createTrivialCube(StarWeaver.STAR_SILK.get());
        blocks.createTrivialCube(StarWeaver.STAR_WEAVE.get());
        blocks.createCrossBlockWithDefaultItem(StarWeaver.STAR_WEB.get(), BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);

        Block cocoon = StarWeaver.STAR_COCOON.get();
        TextureMapping cocoonTextures = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(cocoon, "_side"))
                .put(END, TextureMapping.getBlockTexture(cocoon, "_end"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(cocoon, "_side"));
        Identifier cocoonModel = COCOON.create(cocoon, cocoonTextures, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(cocoon, BlockModelGenerators.plainVariant(cocoonModel)));

        Block nest = StarWeaver.WEAVER_NEST.get();
        TextureMapping lit = new TextureMapping()
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(nest, "_silk"))
                .put(EGG, TextureMapping.getBlockTexture(nest, "_egg"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(nest, "_silk"));
        TextureMapping cold = lit.copyAndUpdate(EGG, TextureMapping.getBlockTexture(nest, "_egg_cold"));
        Identifier litModel = NEST_LIT.create(nest, lit, blocks.modelOutput);
        Identifier coldModel = NEST_COLD.createWithSuffix(nest, "_cold", cold, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(nest).with(PropertyDispatch.initial(WeaverNestBlock.PHASE)
                .generate(phase -> BlockModelGenerators.plainVariant(phase == WeaverNestBlock.Phase.DEFEATED ? coldModel : litModel))));

        // der Sternenbogen: im Inventar das Bild, sonst plastisch; ruhend und drei Stufen gespannt
        Item bow = StarWeaver.STAR_BOW.get();
        ItemModel.Unbaked standby = bowState(items, bow, "");
        ItemModel.Unbaked pulling0 = bowState(items, bow, "_pulling_0");
        ItemModel.Unbaked pulling1 = bowState(items, bow, "_pulling_1");
        ItemModel.Unbaked pulling2 = bowState(items, bow, "_pulling_2");
        items.itemModelOutput.accept(bow, ItemModelUtils.conditional(ItemModelUtils.isUsingItem(),
                ItemModelUtils.rangeSelect(new UseDuration(false), 0.05f, pulling0, ItemModelUtils.override(pulling1, 0.65f),
                        ItemModelUtils.override(pulling2, 0.9f)),
                standby));
        ModModelProvider.flat(items, StarWeaver.STAR_WEAVER_SPAWN_EGG.get());
    }

    private static ItemModel.Unbaked bowState(ItemModelGenerators items, Item bow, String suffix) {
        ItemModel.Unbaked flat = ItemModelUtils.plainModel(items.createFlatItemModel(bow, suffix, ModelTemplates.BOW));
        ItemModel.Unbaked relief = ItemModelUtils.plainModel(KingdomsAtWar.id("item/relief/star_bow" + suffix));
        return ItemModelUtils.select(new DisplayContext(), relief, ItemModelUtils.when(List.of(ItemDisplayContext.GUI), flat));
    }
}
