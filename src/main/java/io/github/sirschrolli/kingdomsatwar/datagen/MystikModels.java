package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlock;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zu MissMystik, der Feen-Zauberkünstlerin (0.66):
 * <ul>
 *   <li><b>Großer Zauberzylinder:</b> Krempe, hoher Hut aus Seidenfilz, Satinband mit goldenem Stern (leuchtend oder
 *   erloschen), Deckel – anderthalb Blöcke hoch.</li>
 *   <li><b>Samtvorhänge:</b> ganze Blöcke.</li>
 *   <li><b>Kartendeck der Mystik:</b> im Inventar das Bild, in der Hand ein Fächer aus fünf Karten.</li>
 *   <li><b>Zylinder der Mystik:</b> im Inventar das Bild, auf dem Kopf (und in der Hand) ein echter Zylinder mit Band,
 *   Schnalle und Feder.</li>
 * </ul>
 */
final class MystikModels {
    private MystikModels() {}

    static final TextureSlot SIDE = TextureSlot.create("side");
    static final TextureSlot TOP = TextureSlot.create("top");
    static final TextureSlot BRIM = TextureSlot.create("brim");
    static final TextureSlot BAND = TextureSlot.create("band");
    static final TextureSlot CARDS = TextureSlot.create("cards");
    static final TextureSlot HAT = TextureSlot.create("hat");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        stageHat(blocks);
        blocks.createTrivialBlock(MystikBlocks.VELVET_CURTAIN.get(), TexturedModel.CUBE);
        blocks.createTrivialBlock(MystikBlocks.VELVET_CURTAIN_FRINGE.get(), TexturedModel.CUBE);
        deck(items);
        topHat(items);
        for (var egg : MystikBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    // =====================================================================================
    // Der große Zauberzylinder
    // =====================================================================================

    /** {von x, y, z, bis x, y, z, Teil}: 0 Krempe, 1 Hut, 2 Band, 3 Deckel. */
    private static final float[][] HAT_PARTS = {
            {0, 0, 0, 16, 1.5f, 16, 0},
            {2.5f, 1.5f, 2.5f, 13.5f, 22, 13.5f, 1},
            {2.3f, 2.5f, 2.3f, 13.7f, 6.5f, 13.7f, 2},
            {2.2f, 21.5f, 2.2f, 13.8f, 23, 13.8f, 3},
    };

    private static ExtendedModelTemplateBuilder hatElements(ExtendedModelTemplateBuilder b, boolean lit) {
        for (float[] p : HAT_PARTS) {
            int part = (int) p[6];
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                for (Direction side : Direction.values()) {
                    e.face(side, f -> {
                        boolean vertical = side.getAxis() == Direction.Axis.Y;
                        switch (part) {
                            case 0 -> {
                                f.texture(BRIM);
                                f.uvs(vertical ? 0 : 0, vertical ? 0 : 14.5f, 16, 16);
                            }
                            case 1 -> {
                                f.texture(vertical ? TOP : SIDE);
                                f.uvs(vertical ? p[0] : 2.5f, vertical ? p[2] : 0, vertical ? p[3] : 13.5f, 16);
                            }
                            case 2 -> {
                                f.texture(BAND);
                                f.uvs(vertical ? 0 : 0, vertical ? 6 : 5, 16, vertical ? 9 : 11);
                            }
                            default -> {
                                f.texture(vertical ? TOP : SIDE);
                                f.uvs(vertical ? p[0] : 0, vertical ? p[2] : 0, vertical ? p[3] : 16, vertical ? p[5] : 2);
                            }
                        }
                    });
                }
                if (lit && part == 2) {
                    e.lightEmission(10);
                }
            });
        }
        return b;
    }

    private static void stageHat(BlockModelGenerators blocks) {
        Block hat = MystikBlocks.STAGE_HAT.get();
        TextureMapping lit = new TextureMapping().put(SIDE, tex("block/stage_hat_side")).put(TOP, tex("block/stage_hat_top"))
                .put(BRIM, tex("block/stage_hat_brim")).put(BAND, tex("block/stage_hat_band"))
                .put(TextureSlot.PARTICLE, tex("block/stage_hat_side"));
        ExtendedModelTemplateBuilder litBuilder = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(SIDE).requiredTextureSlot(TOP).requiredTextureSlot(BRIM).requiredTextureSlot(BAND)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        hatElements(litBuilder, true);
        Identifier litModel = litBuilder.build().create(hat, lit, blocks.modelOutput);
        ExtendedModelTemplateBuilder dimBuilder = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(SIDE).requiredTextureSlot(TOP).requiredTextureSlot(BRIM).requiredTextureSlot(BAND)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        hatElements(dimBuilder, false);
        Identifier dimModel = dimBuilder.build().createWithSuffix(hat, "_empty", lit.copy().put(BAND, tex("block/stage_hat_band_dim")),
                blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(hat)
                .with(PropertyDispatch.initial(StageHatBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(
                        phase == StageHatBlock.Phase.EMPTY ? dimModel : litModel))));
        blocks.registerSimpleItemModel(hat, litModel);
    }

    // =====================================================================================
    // Das Kartendeck der Mystik
    // =====================================================================================

    private static void deck(ItemModelGenerators items) {
        Item deck = MystikBlocks.MYSTIC_DECK.get();
        TextureMapping textures = new TextureMapping().put(CARDS, tex("item/mystic_deck_model")).put(TextureSlot.PARTICLE, tex("item/mystic_deck"));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(CARDS).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -75, 18).translation(1.5f, 3.2f, 1.0f).scale(0.68f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 75, -18).translation(1.5f, 3.2f, 1.0f).scale(0.68f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(-10, 0, 0).translation(0, 3.0f, 1.0f).scale(0.55f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(-10, 0, 0).translation(0, 3.0f, 1.0f).scale(0.55f));
        float[] angles = {-34, -17, 0, 17, 34};
        for (int i = 0; i < angles.length; i++) {
            float a = angles[i];
            float z = 7.6f + i * 0.26f;
            hand.element(e -> {
                e.from(5.0f, 4.0f, z).to(11.0f, 13.0f, z + 0.2f);
                e.face(Direction.NORTH, f -> f.texture(CARDS).uvs(0, 0, 8, 16));
                e.face(Direction.SOUTH, f -> f.texture(CARDS).uvs(8, 0, 16, 16));
                e.face(Direction.EAST, f -> f.texture(CARDS).uvs(7, 0, 8, 16));
                e.face(Direction.WEST, f -> f.texture(CARDS).uvs(7, 0, 8, 16));
                e.face(Direction.UP, f -> f.texture(CARDS).uvs(0, 0, 8, 1));
                e.face(Direction.DOWN, f -> f.texture(CARDS).uvs(0, 15, 8, 16));
                e.rotation(r -> r.origin(8, 4, 8).eulerXYZ(0, 0, a));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(deck, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(deck, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(deck, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.0f));
    }

    // =====================================================================================
    // Der Zylinder der Mystik
    // =====================================================================================

    /**
     * Ein Teil des Huts: {von x, y, z, bis x, y, z, Atlas u0, v0, u1, v1, Drehung x, y, z, Drehpunkt x, y, z} (Kopf-Mitte bei
     * 8, 8, 8; vorn ist Norden).
     */
    private static final float[][] TOP_HAT_PARTS = {
            // Krempe, Hut, Band, Deckel, Schnalle
            {-0.8f, 14.0f, -0.8f, 16.8f, 15.2f, 16.8f, 8, 0, 16, 8, 0, 0, 0, 8, 8, 8},
            {2.4f, 15.2f, 2.4f, 13.6f, 25.6f, 13.6f, 0, 0, 8, 8, 0, 0, 0, 8, 8, 8},
            {2.2f, 15.4f, 2.2f, 13.8f, 17.8f, 13.8f, 0, 8, 8, 10.5f, 0, 0, 0, 8, 8, 8},
            {2.2f, 25.0f, 2.2f, 13.8f, 26.4f, 13.8f, 0, 10.5f, 8, 16, 0, 0, 0, 8, 8, 8},
            {6.6f, 15.5f, 1.7f, 9.4f, 17.7f, 2.2f, 8, 8, 11, 11, 0, 0, 0, 8, 8, 8},
            // vorn rechts am Band eine Feenrose mit einem Blatt
            {10.4f, 15.0f, 1.2f, 13.2f, 17.8f, 3.4f, 11, 8, 14, 11, 0, 20, 0, 11.8f, 16.4f, 2.3f},
            {12.4f, 14.9f, 0.4f, 15.2f, 15.1f, 3.2f, 11.5f, 11, 14, 13.5f, 0, -25, 0, 13.8f, 15.0f, 1.8f},
            // dahinter eine Feder (zwei gekreuzte Fahnen), nach hinten und außen geneigt
            {11.3f, 16.4f, 3.9f, 14.3f, 28.4f, 4.1f, 14, 8, 16, 16, 22, 0, -16, 12.8f, 16.4f, 4.0f},
            {12.7f, 16.4f, 2.5f, 12.9f, 28.4f, 5.5f, 14, 8, 16, 16, 22, 0, -16, 12.8f, 16.4f, 4.0f},
            // links im Band steckt ein Herz-Ass
            {1.9f, 15.6f, 5.4f, 2.1f, 20.6f, 9.0f, 8, 11, 11.5f, 16, -10, 0, 12, 2.0f, 15.6f, 7.2f},
    };

    private static void topHat(ItemModelGenerators items) {
        Item hat = MystikBlocks.MYSTIC_TOP_HAT.get();
        TextureMapping textures = new TextureMapping().put(HAT, tex("item/mystic_top_hat_model"))
                .put(TextureSlot.PARTICLE, tex("item/mystic_top_hat"));
        ExtendedModelTemplateBuilder worn = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(HAT).requiredTextureSlot(TextureSlot.PARTICLE)
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 0, 0).translation(0, -6.0f, 2.0f).scale(0.45f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, 0, 0).translation(0, -6.0f, 2.0f).scale(0.45f))
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -30, 0).translation(1.0f, -3.0f, 0).scale(0.5f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 30, 0).translation(1.0f, -3.0f, 0).scale(0.5f));
        for (float[] p : TOP_HAT_PARTS) {
            worn.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                e.allFaces((direction, face) -> face.texture(HAT).uvs(p[6], p[7], p[8], p[9]));
                if (p[10] != 0.0f || p[11] != 0.0f || p[12] != 0.0f) {
                    e.rotation(r -> r.origin(p[13], p[14], p[15]).eulerXYZ(p[10], p[11], p[12]));
                }
            });
        }
        Identifier model3d = worn.build().create(ModelLocationUtils.getModelLocation(hat, "_worn"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(hat, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(hat, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(model3d)),
                new ClientItem.Properties(true, false, 1.0f));
    }
}
