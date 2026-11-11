package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
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
 * Modelle zu Malleus, dem Glutschmied (0.64):
 * <ul>
 *   <li><b>Glutspeer:</b> im Inventar das Bild, in der Hand das 3D-Modell (wie die Speere von Minecraft gehalten): Kappe,
 *   Lederwicklung, Schaft mit Goldringen, Parierstück mit Widderhörnern und Glutstein, die blattförmige Klinge in fünf
 *   Stufen mit weißglühender Ader (Klinge und Glutstein mit Lichtstärke 15).</li>
 *   <li><b>Glutamboss:</b> Fuß, Hals, Bahn mit spitzem Horn und Absatz – wie ein Amboss, nur wuchtiger; heiß mit einer
 *   leuchtenden Ebene über den glühenden Nähten, erkaltet ohne. Ruht der Schmied, liegt der Glutspeer quer auf der Bahn
 *   (der Schaft zeigt zum Schmied).</li>
 *   <li>Glutklumpen und Spawn-Ei als flache Bilder.</li>
 * </ul>
 */
final class ForgeModels {
    private ForgeModels() {}

    static final TextureSlot SPEAR = TextureSlot.create("spear");
    static final TextureSlot SPEAR_GLOW = TextureSlot.create("spear_glow");
    static final TextureSlot BODY = TextureSlot.create("body");
    static final TextureSlot TOP = TextureSlot.create("top");
    static final TextureSlot BODY_GLOW = TextureSlot.create("body_glow");
    static final TextureSlot TOP_GLOW = TextureSlot.create("top_glow");

    /**
     * Teile des Glutspeers längs der Y-Achse (Mitte x = z = 8): {von x, y, z, bis x, y, z, Bereich der Textur u0, v0, u1, v1,
     * Leuchten (0 nein, 1 Klinge, 2 Ader)}.
     */
    private static final float[][] SPEAR_PARTS = {
            {7.3f, -4.4f, 7.3f, 8.7f, -3.0f, 8.7f, 4, 0, 5.4f, 1.4f, 0},          // goldene Kappe
            {7.4f, -3.0f, 7.4f, 8.6f, 1.6f, 8.6f, 2, 0, 3.2f, 4.6f, 0},           // Lederwicklung
            {7.5f, 1.6f, 7.5f, 8.5f, 12.4f, 8.5f, 0, 0, 1, 10.8f, 0},             // Schaft aus geschwärztem Eisenholz
            {7.35f, 4.8f, 7.35f, 8.65f, 5.5f, 8.65f, 4, 2, 5.3f, 2.7f, 0},        // Goldringe
            {7.35f, 8.6f, 7.35f, 8.65f, 9.3f, 8.65f, 4, 2, 5.3f, 2.7f, 0},
            {5.4f, 12.4f, 7.25f, 10.6f, 13.6f, 8.75f, 14, 0, 16, 1.2f, 0},        // Parierstück
            {4.7f, 12.9f, 7.5f, 5.6f, 14.9f, 8.5f, 14, 2, 15, 4, 0},              // Widderhörner
            {10.4f, 12.9f, 7.5f, 11.3f, 14.9f, 8.5f, 14, 2, 15, 4, 0},
            {4.5f, 14.9f, 7.6f, 5.3f, 15.7f, 8.4f, 4, 4, 4.8f, 4.8f, 0},          // ihre goldenen Spitzen
            {10.7f, 14.9f, 7.6f, 11.5f, 15.7f, 8.4f, 4, 4, 4.8f, 4.8f, 0},
            {7.3f, 12.5f, 7.0f, 8.7f, 13.5f, 7.3f, 12, 0, 13.4f, 1, 1},           // Glutstein vorn und hinten
            {7.3f, 12.5f, 8.7f, 8.7f, 13.5f, 9.0f, 12, 0, 13.4f, 1, 1},
            {6.7f, 13.6f, 7.6f, 9.3f, 15.6f, 8.4f, 6, 9, 10, 12, 1},              // Klinge in fünf Stufen
            {6.1f, 15.6f, 7.62f, 9.9f, 18.2f, 8.38f, 6, 6, 10, 9, 1},
            {6.6f, 18.2f, 7.65f, 9.4f, 20.4f, 8.35f, 6.5f, 3.5f, 9.5f, 6, 1},
            {7.1f, 20.4f, 7.7f, 8.9f, 22.2f, 8.3f, 7, 1.5f, 9, 3.5f, 1},
            {7.6f, 22.2f, 7.75f, 8.4f, 23.6f, 8.25f, 7.5f, 0, 8.5f, 1.5f, 1},
            {7.8f, 14.0f, 7.55f, 8.2f, 22.0f, 8.45f, 10, 0, 12, 12, 2},           // weißglühende Ader
    };

    /** Der Speer als 3D-Modell: alle Teile um {@code lift} verschoben und gemeinsam um {@code origin} gedreht. */
    private static ExtendedModelTemplateBuilder spearElements(ExtendedModelTemplateBuilder b, float lift, float[] origin, float[] angles) {
        for (float[] p : SPEAR_PARTS) {
            boolean glow = p[10] > 0;
            boolean core = p[10] > 1;
            b.element(e -> {
                e.from(p[0], p[1] + lift, p[2]).to(p[3], p[4] + lift, p[5]);
                e.allFaces((direction, face) -> {
                    face.texture(core || glow ? SPEAR_GLOW : SPEAR);
                    if (direction.getAxis() == Direction.Axis.Y) {
                        face.uvs(p[6], p[7], Math.min(p[8], p[6] + 1.5f), Math.min(p[9], p[7] + 1.5f));
                    } else {
                        face.uvs(p[6], p[7], p[8], p[9]);
                    }
                });
                if (glow) {
                    e.lightEmission(15);
                }
                e.rotation(r -> r.origin(origin[0], origin[1], origin[2]).eulerXYZ(angles[0], angles[1], angles[2]));
            });
        }
        return b;
    }

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        spear(items);
        anvil(blocks);
        for (var item : ForgeBlocks.flatItems()) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : ForgeBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    // =====================================================================================
    // Der Glutspeer
    // =====================================================================================

    private static void spear(ItemModelGenerators items) {
        Item spear = ForgeBlocks.GLUT_SPEAR.get();
        TextureMapping textures = new TextureMapping()
                .put(SPEAR, tex("item/glut_spear_model"))
                .put(SPEAR_GLOW, tex("item/glut_spear_model_glow"))
                .put(TextureSlot.PARTICLE, tex("item/glut_spear"));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(SPEAR).requiredTextureSlot(SPEAR_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        t -> t.rotation(-20, 90, -35).translation(3.13f, 2.0f, 0.13f).scale(1.3f, 1.3f, 0.65f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        t -> t.rotation(-20, -90, 35).translation(3.13f, 2.0f, 0.13f).scale(1.3f, 1.3f, 0.65f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        t -> t.rotation(5, 270, -40).translation(0, 2, 2).scale(1.62f, 1.62f, 0.81f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        t -> t.rotation(5, -270, 40).translation(0, 2, 2).scale(1.62f, 1.62f, 0.81f));
        spearElements(hand, 0.0f, new float[] {8, 8, 8}, new float[] {0, 0, 45});
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(spear, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(spear, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(spear, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.95f));
    }

    // =====================================================================================
    // Der Glutamboss
    // =====================================================================================

    /** Die Teile des Ambosses (Bahn längs X, wie bei Blick nach Norden): {von x, y, z, bis x, y, z, oben?}. */
    private static final float[][] ANVIL_PARTS = {
            {1, 0, 2, 15, 4, 14, 0},          // Fuß
            {3, 4, 4, 13, 5, 12, 0},          // Absatz des Fußes
            {4, 5, 5.5f, 12, 10, 10.5f, 0},   // Hals
            {0, 10, 3, 16, 16, 13, 1},        // Bahn
            {-3, 12, 5, 0, 15.5f, 11, 1},     // Horn, in zwei Stufen spitz zulaufend
            {-5.5f, 13.2f, 6.5f, -3, 15, 9.5f, 1},
            {16, 13, 4, 17.5f, 16, 12, 1},    // Absatz am anderen Ende
    };

    /** Auf [0, 16] gebrachter Ausschnitt (Teile, die über den Block hinausragen, nehmen den Rand der Textur). */
    private static float[] span(float a, float b) {
        float lo = Math.max(0.0f, Math.min(a, 16.0f));
        float hi = Math.max(0.0f, Math.min(b, 16.0f));
        if (hi - lo < 1.0f) {
            float w = Math.min(16.0f, b - a);
            lo = a < 0.0f ? 0.0f : 16.0f - w;
            hi = lo + w;
        }
        return new float[] {lo, hi};
    }

    private static ExtendedModelTemplateBuilder anvilElements(ExtendedModelTemplateBuilder b, boolean glow) {
        for (float[] p : ANVIL_PARTS) {
            boolean top = p[6] > 0;
            b.element(e -> {
                float g = glow ? 0.01f : 0.0f;
                e.from(p[0] - g, p[1] - g, p[2] - g).to(p[3] + g, p[4] + g, p[5] + g);
                float[] x = span(p[0], p[3]);
                float[] z = span(p[2], p[5]);
                float[] v = span(16.0f - p[4], 16.0f - p[1]);
                for (Direction side : Direction.values()) {
                    TextureSlot slot = side == Direction.UP && top ? (glow ? TOP_GLOW : TOP) : (glow ? BODY_GLOW : BODY);
                    e.face(side, f -> {
                        switch (side.getAxis()) {
                            case Y -> f.uvs(x[0], z[0], x[1], z[1]);
                            case Z -> f.uvs(x[0], v[0], x[1], v[1]);
                            default -> f.uvs(z[0], v[0], z[1], v[1]);
                        }
                        f.texture(slot);
                    });
                }
                if (glow) {
                    e.lightEmission(15);
                }
            });
        }
        return b;
    }

    private static void anvil(BlockModelGenerators blocks) {
        Block anvil = ForgeBlocks.GLUT_ANVIL.get();
        // erkaltet: nur das Eisen
        ExtendedModelTemplateBuilder cold = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BODY).requiredTextureSlot(TOP).requiredTextureSlot(TextureSlot.PARTICLE);
        anvilElements(cold, false);
        Identifier coldModel = cold.build().createWithSuffix(anvil, "_cold", new TextureMapping()
                .put(BODY, tex("block/glut_anvil_cold")).put(TOP, tex("block/glut_anvil_top_cold"))
                .put(TextureSlot.PARTICLE, tex("block/glut_anvil_cold")), blocks.modelOutput);
        // heiß: dazu die leuchtende Ebene über den Nähten
        TextureMapping hotTextures = new TextureMapping()
                .put(BODY, tex("block/glut_anvil")).put(TOP, tex("block/glut_anvil_top"))
                .put(BODY_GLOW, tex("block/glut_anvil_glow")).put(TOP_GLOW, tex("block/glut_anvil_top_glow"))
                .put(TextureSlot.PARTICLE, tex("block/glut_anvil"));
        ExtendedModelTemplateBuilder hot = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BODY).requiredTextureSlot(TOP).requiredTextureSlot(BODY_GLOW).requiredTextureSlot(TOP_GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        anvilElements(anvilElements(hot, false), true);
        Identifier hotModel = hot.build().createWithSuffix(anvil, "_hot", hotTextures, blocks.modelOutput);
        // ruhend: der Glutspeer liegt quer auf der Bahn, die Klinge über der Mitte, der Schaft zeigt zum Schmied (Norden)
        ExtendedModelTemplateBuilder rest = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BODY).requiredTextureSlot(TOP).requiredTextureSlot(BODY_GLOW).requiredTextureSlot(TOP_GLOW)
                .requiredTextureSlot(SPEAR).requiredTextureSlot(SPEAR_GLOW).requiredTextureSlot(TextureSlot.PARTICLE);
        anvilElements(anvilElements(rest, false), true);
        spearElements(rest, -1.6f, new float[] {8, 17, 8}, new float[] {90, 0, 0});
        Identifier restModel = rest.build().create(anvil, hotTextures.copy()
                .put(SPEAR, tex("block/glut_anvil_spear")).put(SPEAR_GLOW, tex("block/glut_anvil_spear_glow")), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(anvil)
                .with(PropertyDispatch.initial(GlutAnvilBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case DORMANT -> restModel;
                    case AWAKE -> hotModel;
                    case COLD -> coldModel;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleItemModel(anvil, restModel);
    }
}
