package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.creator.CreatorAltarBlock;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
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
 * Modelle zu Schrolli dem Schöpfer (0.75):
 * <ul>
 *   <li><b>Altar des Schöpfers:</b> ein Sockel aus weißem Quarz mit Goldband, vier goldene Eckpfeiler, eine Säule, darauf die
 *   Sonnenscheibe; mit Funken schwebt darüber ein gleißender Kristall (gerufen größer).</li>
 *   <li><b>Zepter des Schöpfers:</b> im Inventar das Bild, in der Hand ein 3D-Zepter (Goldstab mit weißen Bändern, oben der
 *   achtstrahlige Sonnenstern um einen blauen Stein, leuchtend).</li>
 *   <li>Schöpferfunke, Schwingen und Spawn-Eier: flach.</li>
 * </ul>
 */
final class CreatorModels {
    private CreatorModels() {}

    static final TextureSlot SIDE = TextureSlot.create("side");
    static final TextureSlot TOP = TextureSlot.create("top");
    static final TextureSlot COLUMN = TextureSlot.create("column");
    static final TextureSlot BOTTOM = TextureSlot.create("bottom");
    static final TextureSlot SPARK = TextureSlot.create("spark");
    static final TextureSlot STAFF = TextureSlot.create("staff");
    static final TextureSlot STAFF_GLOW = TextureSlot.create("staff_glow");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        altar(blocks);
        scepter(items);
        for (var item : CreatorBlocks.flatItems()) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : CreatorBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    // =====================================================================================
    // Der Altar
    // =====================================================================================

    private static ExtendedModelTemplateBuilder altarBuilder() {
        return ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(SIDE).requiredTextureSlot(TOP).requiredTextureSlot(COLUMN).requiredTextureSlot(BOTTOM)
                .requiredTextureSlot(SPARK).requiredTextureSlot(TextureSlot.PARTICLE);
    }

    private static void box(ExtendedModelTemplateBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, TextureSlot side,
            TextureSlot top, TextureSlot bottom, int light) {
        b.element(e -> {
            e.from(x0, y0, z0).to(x1, y1, z1);
            for (Direction d : Direction.values()) {
                TextureSlot slot = d == Direction.UP ? top : d == Direction.DOWN ? bottom : side;
                float[] uv = uv(d, new float[] {x0, y0, z0, x1, y1, z1});
                e.face(d, f -> f.texture(slot).uvs(uv[0], uv[1], uv[2], uv[3]));
            }
            if (light > 0) {
                e.lightEmission(light);
            }
        });
    }

    /** Texturausschnitt einer Seite nach ihrer Lage im Block (wie bei ganzen Blöcken). */
    private static float[] uv(Direction side, float[] p) {
        float[] q = new float[6];
        for (int i = 0; i < 6; i++) {
            q[i] = Math.max(0.0f, Math.min(16.0f, p[i]));
        }
        return switch (side.getAxis()) {
            case Y -> new float[] {q[0], q[2], q[3], q[5]};
            case Z -> new float[] {q[0], 16 - q[4], q[3], 16 - q[1]};
            default -> new float[] {q[2], 16 - q[4], q[5], 16 - q[1]};
        };
    }

    private static void altarElements(ExtendedModelTemplateBuilder b, int spark) {
        box(b, 0, 0, 0, 16, 4, 16, SIDE, COLUMN, BOTTOM, 0);
        box(b, 3, 4, 3, 13, 12, 13, COLUMN, COLUMN, BOTTOM, 0);
        for (float[] c : new float[][] {{0.5f, 0.5f}, {13.0f, 0.5f}, {0.5f, 13.0f}, {13.0f, 13.0f}}) {
            box(b, c[0], 4, c[1], c[0] + 2.5f, 12, c[1] + 2.5f, TOP, TOP, TOP, 0);
        }
        box(b, 1, 12, 1, 15, 15, 15, SIDE, TOP, BOTTOM, 0);
        if (spark > 0) {
            float h = spark == 2 ? 7.5f : 5.5f;
            float w = spark == 2 ? 2.2f : 1.7f;
            b.element(e -> {
                e.from(8 - w, 16.5f, 8 - w).to(8 + w, 16.5f + h, 8 + w);
                for (Direction d : Direction.values()) {
                    e.face(d, f -> f.texture(SPARK).uvs(4, 4, 12, 12));
                }
                e.lightEmission(15);
                e.rotation(r -> r.origin(8, 16, 8).eulerXYZ(0, 45, 0));
            });
        }
    }

    private static void altar(BlockModelGenerators blocks) {
        Block altar = CreatorBlocks.CREATOR_ALTAR.get();
        TextureMapping textures = new TextureMapping().put(SIDE, tex("block/creator_altar_side")).put(TOP, tex("block/creator_altar_top"))
                .put(COLUMN, tex("block/creator_altar_column")).put(BOTTOM, tex("block/creator_altar_bottom"))
                .put(SPARK, tex("block/creator_altar_spark")).put(TextureSlot.PARTICLE, tex("block/creator_altar_side"));
        ExtendedModelTemplateBuilder empty = altarBuilder();
        altarElements(empty, 0);
        Identifier emptyModel = empty.build().create(altar, textures, blocks.modelOutput);
        ExtendedModelTemplateBuilder armed = altarBuilder();
        altarElements(armed, 1);
        Identifier armedModel = armed.build().createWithSuffix(altar, "_armed", textures, blocks.modelOutput);
        ExtendedModelTemplateBuilder awake = altarBuilder();
        altarElements(awake, 2);
        Identifier awakeModel = awake.build().createWithSuffix(altar, "_awake", textures, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(altar)
                .with(PropertyDispatch.initial(CreatorAltarBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case EMPTY -> emptyModel;
                    case ARMED -> armedModel;
                    case AWAKE -> awakeModel;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleItemModel(altar, armedModel);
    }

    // =====================================================================================
    // Das Zepter
    // =====================================================================================

    /**
     * Teile des Zepters {von x, y, z, bis x, y, z, Atlas u0, v0, u1, v1, leuchtet, frei}: senkrecht gebaut (Fuß unten,
     * Stern oben) und dann wie die Werkzeuge von Minecraft schräg gestellt.
     */
    private static final float[][] SCEPTER_PARTS = {
            {7.3f, -6.0f, 7.3f, 8.7f, 18.0f, 8.7f, 0, 0, 4, 16, 0, 0},       // Goldstab
            {7.0f, -6.6f, 7.0f, 9.0f, -5.4f, 9.0f, 0, 0, 4, 4, 0, 0},        // Fuß
            {7.1f, 1.0f, 7.1f, 8.9f, 2.0f, 8.9f, 8, 0, 12, 4, 0, 0},         // weiße Bänder
            {7.1f, 9.0f, 7.1f, 8.9f, 10.0f, 8.9f, 8, 0, 12, 4, 0, 0},
            {6.6f, 17.4f, 6.6f, 9.4f, 18.4f, 9.4f, 0, 0, 4, 4, 0, 0},        // Kragen
            {6.8f, 19.4f, 6.8f, 9.2f, 21.8f, 9.2f, 24, 0, 32, 8, 1, 0},      // blauer Stein
            {3.6f, 20.2f, 7.6f, 12.4f, 21.0f, 8.4f, 12, 0, 24, 4, 1, 0},     // Sonnenstern: waagrecht
            {7.6f, 16.2f, 7.6f, 8.4f, 25.0f, 8.4f, 12, 0, 24, 16, 1, 0},     // senkrecht
            {7.7f, 20.2f, 3.6f, 8.3f, 21.0f, 12.4f, 12, 4, 24, 8, 1, 0},      // nach vorn und hinten
    };

    private static void scepter(ItemModelGenerators items) {
        Item scepter = CreatorBlocks.CREATOR_SCEPTER.get();
        TextureMapping textures = new TextureMapping().put(STAFF, tex("item/creator_scepter_model")).put(STAFF_GLOW, tex("item/creator_scepter_model"))
                .put(TextureSlot.PARTICLE, tex("item/creator_scepter"));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(STAFF).requiredTextureSlot(STAFF_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 25).translation(1.13f, 3.2f, 1.13f).scale(0.5f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 90, -25).translation(1.13f, 3.2f, 1.13f).scale(0.5f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 90, 55).translation(0, 4.0f, 0.5f).scale(0.85f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, -90, -55).translation(0, 4.0f, 0.5f).scale(0.85f));
        for (float[] p : SCEPTER_PARTS) {
            boolean glow = p[10] > 0;
            hand.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                e.allFaces((direction, face) -> face.texture(glow ? STAFF_GLOW : STAFF).uvs(p[6], p[7], p[8], p[9]));
                if (glow) {
                    e.lightEmission(14);
                }
                e.rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, 0, 45));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(scepter, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(scepter, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(scepter, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.6f));
    }
}
