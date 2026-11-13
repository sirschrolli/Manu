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
 *   <li><b>Speer des Schöpfers</b> (bis 0.77 Zepter): im Inventar das Bild, in der Hand ein 3D-Speer – Goldschaft mit
 *   weißen Bändern und umwickeltem Griff, ein Kragen mit zwei weißen Federflügeln, ein blauer Stein und darüber die Klinge
 *   aus Licht in goldener Fassung, umgeben von einem Ring aus Licht (siehe {@link #spearParts()}).</li>
 *   <li>Schöpferfunke, Schwingen, Heiligenschein, Räder und Spawn-Eier: flach.</li>
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
    // Der Speer (bis 0.77 das Zepter)
    // =====================================================================================

    /** Ausschnitte des Atlas (32×32 Bildpunkte): Gold, Griff, Weiß, Licht der Klinge, leuchtendes Gold, Ring, Stein. */
    private static final int[] GOLD = {0, 0, 8, 16};
    private static final int[] GRIP = {0, 16, 8, 32};
    private static final int[] WHITE = {8, 0, 12, 16};
    private static final int[] CORE = {12, 0, 16, 16};
    private static final int[] EDGE = {16, 0, 20, 16};
    private static final int[] RING = {20, 0, 24, 16};
    private static final int[] GEM = {24, 0, 32, 16};
    /** Wie die Werkzeuge von Minecraft steht der Speer schräg. */
    private static final float TILT = 45.0f;

    /**
     * Ein Teil des Speers: ein Quader im aufrechten Speer (Fuß unten, Klinge oben), um die eigene Mitte um {@code angle}
     * gedreht; {@code flat}: ungeschattet (die weißen Federn).
     */
    private record Part(float x0, float y0, float z0, float x1, float y1, float z1, int[] region, int light, float angle, boolean flat) {
        boolean glows() {
            return this.region == CORE || this.region == EDGE || this.region == RING || this.region == GEM;
        }
    }

    private static void part(java.util.List<Part> parts, float x0, float y0, float z0, float x1, float y1, float z1, int[] region, int light) {
        parts.add(new Part(x0, y0, z0, x1, y1, z1, region, light, 0.0f, false));
    }

    /** Ein Quader um die Mitte (cx, cy) – w breit, h hoch, depth tief –, um seine Mitte um angle Grad gedreht. */
    private static void centered(java.util.List<Part> parts, double cx, double cy, double w, double h, double depth, int[] region, int light,
            double angle, boolean flat) {
        parts.add(new Part((float) (cx - w / 2), (float) (cy - h / 2), (float) (8 - depth / 2), (float) (cx + w / 2), (float) (cy + h / 2),
                (float) (8 + depth / 2), region, light, (float) angle, flat));
    }

    /**
     * Der Speer des Schöpfers (seit 0.78): Fuß aus blauem Stein in Gold, ein Goldschaft mit weißen Bändern, ein umwickelter
     * Griff, der Kragen mit zwei weißen Flügeln (je vier Federn mit leuchtend goldenen Spitzen), ein blauer Stein und darüber
     * die Klinge – ein Blatt aus Licht in goldener Fassung mit weißem Grat –, umgeben von einem Ring aus Licht.
     */
    private static java.util.List<Part> spearParts() {
        java.util.List<Part> parts = new java.util.ArrayList<>();
        part(parts, 7.45f, -8.6f, 7.45f, 8.55f, -7.2f, 8.55f, GEM, 12);
        part(parts, 7.0f, -7.2f, 7.0f, 9.0f, -5.8f, 9.0f, GOLD, 0);
        part(parts, 7.25f, -5.8f, 7.25f, 8.75f, -5.0f, 8.75f, EDGE, 6);
        part(parts, 7.38f, -5.0f, 7.38f, 8.62f, 15.0f, 8.62f, GOLD, 0);
        for (float y : new float[] {-1.6f, 2.4f, 6.4f}) {
            part(parts, 7.18f, y, 7.18f, 8.82f, y + 1.1f, 8.82f, WHITE, 0);
        }
        part(parts, 7.22f, 9.4f, 7.22f, 8.78f, 13.6f, 8.78f, GRIP, 0);
        part(parts, 7.12f, 13.6f, 7.12f, 8.88f, 14.2f, 8.88f, EDGE, 6);
        part(parts, 6.7f, 14.6f, 6.7f, 9.3f, 16.2f, 9.3f, GOLD, 0);
        part(parts, 6.95f, 16.2f, 6.95f, 9.05f, 16.8f, 9.05f, EDGE, 8);
        // die Flügel am Kragen
        double[][] feathers = {{36, 4.6}, {52, 4.0}, {68, 3.3}, {84, 2.6}};
        for (int side : new int[] {-1, 1}) {
            for (int i = 0; i < feathers.length; i++) {
                double a = -side * feathers[i][0];
                double length = feathers[i][1];
                double ra = Math.toRadians(a);
                double rx = 8 + side * 1.15;
                double ry = 16.3 - i * 0.45;
                double dx = -Math.sin(ra);
                double dy = Math.cos(ra);
                centered(parts, rx + dx * length / 2, ry + dy * length / 2, 1.0, length, 0.6 - i * 0.04, WHITE, 6, a, true);
                centered(parts, rx + dx * (length + 0.3), ry + dy * (length + 0.3), 0.8, 0.9, 0.5, EDGE, 12, a, false);
            }
        }
        part(parts, 7.15f, 16.8f, 7.15f, 8.85f, 18.4f, 8.85f, GEM, 15);
        // die Klinge: Fassung aus Gold, darin das Licht, in der Mitte der Grat
        float[][] rows = {{17.9f, 19.0f, 1.25f}, {19.0f, 20.3f, 1.95f}, {20.3f, 21.8f, 2.4f}, {21.8f, 23.6f, 2.55f}, {23.6f, 25.3f, 2.4f},
                {25.3f, 26.8f, 2.0f}, {26.8f, 28.1f, 1.5f}, {28.1f, 29.2f, 1.05f}, {29.2f, 30.2f, 0.65f}, {30.2f, 31.0f, 0.32f}};
        for (float[] row : rows) {
            float hw = row[2];
            part(parts, 8 - hw, row[0], 7.74f, 8 + hw, row[1], 8.26f, EDGE, 9);
            float core = hw - 0.5f;
            if (core > 0.12f) {
                part(parts, 8 - core, row[0] + 0.15f, 7.58f, 8 + core, Math.min(row[1] + 0.15f, 30.4f), 8.42f, CORE, 15);
            }
        }
        part(parts, 7.8f, 18.4f, 7.42f, 8.2f, 29.6f, 8.58f, WHITE, 15);
        // der Ring aus Licht um die Klinge: sechzehn Glieder
        double ringY = 23.4;
        double ringR = 4.1;
        int n = 16;
        double seg = 2 * Math.PI * ringR / n;
        for (int k = 0; k < n; k++) {
            double a = k * 360.0 / n;
            centered(parts, 8 + Math.cos(Math.toRadians(a)) * ringR, ringY + Math.sin(Math.toRadians(a)) * ringR, 0.62, seg + 0.18, 0.62,
                    RING, 15, a, false);
        }
        return parts;
    }

    /** Winkel auf (-180, 180]. */
    private static float wrap(float degrees) {
        float d = degrees % 360.0f;
        if (d > 180.0f) {
            d -= 360.0f;
        } else if (d <= -180.0f) {
            d += 360.0f;
        }
        return d;
    }

    private static void scepter(ItemModelGenerators items) {
        Item scepter = CreatorBlocks.CREATOR_SCEPTER.get();
        TextureMapping textures = new TextureMapping().put(STAFF, tex("item/creator_scepter_model")).put(STAFF_GLOW, tex("item/creator_scepter_model_glow"))
                .put(TextureSlot.PARTICLE, tex("item/creator_scepter"));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(STAFF).requiredTextureSlot(STAFF_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 25).translation(1.13f, 3.2f, 1.13f).scale(0.5f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 90, -25).translation(1.13f, 3.2f, 1.13f).scale(0.5f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 90, 55).translation(0, 4.0f, 0.5f).scale(0.85f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, -90, -55).translation(0, 4.0f, 0.5f).scale(0.85f));
        for (Part p : spearParts()) {
            // erst um die eigene Mitte drehen, dann alles um (8, 8, 8) schräg stellen: ein Element dreht nur um einen Punkt –
            // also so verschieben, dass die eine Drehung um TILT + angle die Mitte an ihren Platz bringt
            double cx = (p.x0() + p.x1()) / 2.0;
            double cy = (p.y0() + p.y1()) / 2.0;
            double turn = Math.toRadians(-p.angle());
            double dx = 8.0 + (cx - 8.0) * Math.cos(turn) - (cy - 8.0) * Math.sin(turn) - cx;
            double dy = 8.0 + (cx - 8.0) * Math.sin(turn) + (cy - 8.0) * Math.cos(turn) - cy;
            TextureSlot slot = p.glows() ? STAFF_GLOW : STAFF;
            int[] r = p.region();
            hand.element(e -> {
                e.from((float) (p.x0() + dx), (float) (p.y0() + dy), p.z0()).to((float) (p.x1() + dx), (float) (p.y1() + dy), p.z1());
                // der Atlas ist 32×32, die Ausschnitte in Bildpunkten: Modelle rechnen in 16 Einheiten je Bild
                e.allFaces((direction, face) -> face.texture(slot).uvs(r[0] / 2.0f, r[1] / 2.0f, r[2] / 2.0f, r[3] / 2.0f));
                if (p.light() > 0) {
                    e.lightEmission(p.light());
                }
                if (p.flat()) {
                    e.shadeDirectionOverride(Direction.UP);
                }
                e.rotation(rot -> rot.origin(8, 8, 8).eulerXYZ(0, 0, wrap(TILT + p.angle())));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(scepter, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(scepter, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(scepter, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.6f));
    }
}
