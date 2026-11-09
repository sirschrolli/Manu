package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlock;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zu Asteria, der Sternenreiterin (0.65):
 * <ul>
 *   <li><b>Sternenlanze:</b> im Inventar das Bild, in der Hand das 3D-Modell (wie die Speere von Minecraft gehalten):
 *   goldener Knauf, violette Griffwicklung, Brechscheibe, silberner Schaft mit Goldringen, ein Wimpel, Goldkragen und
 *   die Spitze aus einem Sternkristall mit vier Zacken (Kristall mit Lichtstärke 15).</li>
 *   <li><b>Sternstein:</b> ein Sockel aus Quarz mit Goldsaum, darauf ein hoher, über Eck gestellter Sternkristall in drei
 *   Stufen und vier kleine Kristalle an den Ecken – leuchtend (Lichtstärke 15) oder erloschen.</li>
 *   <li>Sternenschwingen wie Elytren (heil und zerrissen), Sternsplitter und Spawn-Ei als flache Bilder.</li>
 * </ul>
 */
final class StarModels {
    private StarModels() {}

    static final TextureSlot LANCE = TextureSlot.create("lance");
    static final TextureSlot LANCE_GLOW = TextureSlot.create("lance_glow");
    static final TextureSlot BASE = TextureSlot.create("base");
    static final TextureSlot BASE_TOP = TextureSlot.create("base_top");
    static final TextureSlot CRYSTAL = TextureSlot.create("crystal");

    /**
     * Teile der Sternenlanze längs der Y-Achse (Mitte x = z = 8): {von x, y, z, bis x, y, z, Bereich der Textur u0, v0, u1,
     * v1, Leuchten (0 nein, 1 Kristall)}.
     */
    private static final float[][] LANCE_PARTS = {
            {7.3f, -4.6f, 7.3f, 8.7f, -3.3f, 8.7f, 4, 0, 5.4f, 1.3f, 0},          // goldener Knauf
            {7.4f, -3.3f, 7.4f, 8.6f, 1.2f, 8.6f, 2, 0, 3.2f, 4.5f, 0},           // violette Griffwicklung
            {5.9f, 1.2f, 5.9f, 10.1f, 2.1f, 10.1f, 14, 0, 16, 2, 0},              // Brechscheibe
            {6.6f, 2.1f, 6.6f, 9.4f, 2.9f, 9.4f, 4, 2, 5.4f, 2.8f, 0},
            {7.5f, 2.9f, 7.5f, 8.5f, 16.0f, 8.5f, 0, 0, 1, 13.1f, 0},             // silberner Schaft mit Wendel
            {7.35f, 7.0f, 7.35f, 8.65f, 7.6f, 8.65f, 4, 4, 5.3f, 4.6f, 0},        // Goldringe
            {7.35f, 11.4f, 7.35f, 8.65f, 12.0f, 8.65f, 4, 4, 5.3f, 4.6f, 0},
            {7.95f, 12.2f, 8.5f, 8.05f, 14.8f, 12.4f, 12, 8, 16, 12, 0},          // Wimpel
            {7.2f, 16.0f, 7.2f, 8.8f, 16.9f, 8.8f, 4, 5, 5.6f, 5.9f, 0},          // Goldkragen
            {7.1f, 16.9f, 7.1f, 8.9f, 18.3f, 8.9f, 6, 10, 8, 12, 1},              // Sternkristall: Fuß
            {5.1f, 17.3f, 7.65f, 10.9f, 18.1f, 8.35f, 6, 8, 10, 9, 1},            // vier Zacken quer
            {7.65f, 17.3f, 5.1f, 8.35f, 18.1f, 10.9f, 6, 8, 10, 9, 1},
            {7.35f, 18.3f, 7.35f, 8.65f, 20.6f, 8.65f, 6, 5, 9, 8, 1},            // die Spitze in drei Stufen
            {7.6f, 20.6f, 7.6f, 8.4f, 22.5f, 8.4f, 7, 2.5f, 9, 5, 1},
            {7.8f, 22.5f, 7.8f, 8.2f, 24.0f, 8.2f, 10, 0, 11, 2.5f, 1},
    };

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        lance(items);
        starstone(blocks);
        items.generateElytra(StarBlocks.STAR_WINGS.get());
        for (var item : StarBlocks.flatItems()) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : StarBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    // =====================================================================================
    // Die Sternenlanze
    // =====================================================================================

    private static void lance(ItemModelGenerators items) {
        Item lance = StarBlocks.STAR_LANCE.get();
        TextureMapping textures = new TextureMapping()
                .put(LANCE, tex("item/star_lance_model"))
                .put(LANCE_GLOW, tex("item/star_lance_model_glow"))
                .put(TextureSlot.PARTICLE, tex("item/star_lance"));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(LANCE).requiredTextureSlot(LANCE_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        t -> t.rotation(-20, 90, -35).translation(3.13f, 2.0f, 0.13f).scale(1.3f, 1.3f, 0.65f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        t -> t.rotation(-20, -90, 35).translation(3.13f, 2.0f, 0.13f).scale(1.3f, 1.3f, 0.65f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        t -> t.rotation(5, 270, -40).translation(0, 2, 2).scale(1.62f, 1.62f, 0.81f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        t -> t.rotation(5, -270, 40).translation(0, 2, 2).scale(1.62f, 1.62f, 0.81f));
        for (float[] p : LANCE_PARTS) {
            boolean glow = p[10] > 0;
            hand.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                e.allFaces((direction, face) -> {
                    face.texture(glow ? LANCE_GLOW : LANCE);
                    if (direction.getAxis() == Direction.Axis.Y) {
                        face.uvs(p[6], p[7], Math.min(p[8], p[6] + 1.5f), Math.min(p[9], p[7] + 1.5f));
                    } else {
                        face.uvs(p[6], p[7], p[8], p[9]);
                    }
                });
                if (glow) {
                    e.lightEmission(15);
                }
                e.rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, 0, -45));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(lance, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(lance, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(lance, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.95f));
    }

    // =====================================================================================
    // Der Sternstein
    // =====================================================================================

    /** Sockel: {von x, y, z, bis x, y, z}. */
    private static final float[][] BASE_PARTS = {
            {2, 0, 2, 14, 3, 14},
    };

    /** Kristalle: {von x, y, z, bis x, y, z, Drehung um die Hochachse (Grad)}. */
    private static final float[][] CRYSTAL_PARTS = {
            {5.5f, 3, 5.5f, 10.5f, 11.5f, 10.5f, 45},       // der hohe Sternkristall, über Eck
            {6.3f, 11.5f, 6.3f, 9.7f, 14.2f, 9.7f, 45},
            {7.15f, 14.2f, 7.15f, 8.85f, 16, 8.85f, 45},
            {3.2f, 3, 3.2f, 5.2f, 7.5f, 5.2f, 0},            // vier kleine Kristalle an den Ecken
            {10.8f, 3, 3.2f, 12.8f, 6.5f, 5.2f, 0},
            {3.2f, 3, 10.8f, 5.2f, 6.5f, 12.8f, 0},
            {10.8f, 3, 10.8f, 12.8f, 8.0f, 12.8f, 0},
            {3.7f, 7.5f, 3.7f, 4.7f, 8.7f, 4.7f, 0},         // ihre Spitzen
            {11.3f, 6.5f, 3.7f, 12.3f, 7.5f, 4.7f, 0},
            {3.7f, 6.5f, 11.3f, 4.7f, 7.5f, 12.3f, 0},
            {11.3f, 8.0f, 11.3f, 12.3f, 9.4f, 12.3f, 0},
    };

    private static ExtendedModelTemplateBuilder starstoneElements(ExtendedModelTemplateBuilder b, boolean lit) {
        for (float[] p : BASE_PARTS) {
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                for (Direction side : Direction.values()) {
                    e.face(side, f -> {
                        switch (side.getAxis()) {
                            case Y -> f.uvs(p[0], p[2], p[3], p[5]);
                            case Z -> f.uvs(p[0], 16 - p[4], p[3], 16 - p[1]);
                            default -> f.uvs(p[2], 16 - p[4], p[5], 16 - p[1]);
                        }
                        f.texture(side == Direction.UP ? BASE_TOP : BASE);
                    });
                }
            });
        }
        for (float[] p : CRYSTAL_PARTS) {
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                for (Direction side : Direction.values()) {
                    e.face(side, f -> {
                        switch (side.getAxis()) {
                            case Y -> f.uvs(p[0], p[2], p[3], p[5]);
                            case Z -> f.uvs(p[0], 16 - p[4], p[3], 16 - p[1]);
                            default -> f.uvs(p[2], 16 - p[4], p[5], 16 - p[1]);
                        }
                        f.texture(CRYSTAL);
                    });
                }
                if (p[6] != 0) {
                    e.rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, p[6], 0));
                }
                if (lit) {
                    e.lightEmission(15);
                }
            });
        }
        return b;
    }

    private static void starstone(BlockModelGenerators blocks) {
        Block stone = StarBlocks.STARSTONE.get();
        TextureMapping litTextures = new TextureMapping().put(BASE, tex("block/starstone_base")).put(BASE_TOP, tex("block/starstone_base_top"))
                .put(CRYSTAL, tex("block/starstone_crystal")).put(TextureSlot.PARTICLE, tex("block/starstone_crystal"));
        ExtendedModelTemplateBuilder lit = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BASE).requiredTextureSlot(BASE_TOP).requiredTextureSlot(CRYSTAL).requiredTextureSlot(TextureSlot.PARTICLE);
        starstoneElements(lit, true);
        Identifier litModel = lit.build().create(stone, litTextures, blocks.modelOutput);
        ExtendedModelTemplateBuilder dim = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BASE).requiredTextureSlot(BASE_TOP).requiredTextureSlot(CRYSTAL).requiredTextureSlot(TextureSlot.PARTICLE);
        starstoneElements(dim, false);
        Identifier dimModel = dim.build().createWithSuffix(stone, "_dim", litTextures.copy()
                .put(CRYSTAL, tex("block/starstone_crystal_dim")).put(TextureSlot.PARTICLE, tex("block/starstone_crystal_dim")),
                blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(stone)
                .with(PropertyDispatch.initial(StarstoneBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(
                        phase == StarstoneBlock.Phase.DIM ? dimModel : litModel))));
        blocks.registerSimpleItemModel(stone, litModel);
    }
}
