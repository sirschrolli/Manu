package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Modelle zur Arena der Prüfungen (seit 1.9): das Arenasiegel (ein Würfel), das Netz, das jemanden gefangen hält (gekreuzt wie
 * ein Spinnennetz), das Wurfnetz und das Spawn-Ei (Bilder) und der Sturmdreizack: im Inventar das Bild, in der Hand ein
 * 3D-Modell, gehalten wie der Dreizack von Minecraft – aufrecht, und beim Ausholen über der Schulter (dafür gelten genau
 * dessen Haltungen, auf die Lage und Länge dieses Dreizacks umgerechnet).
 */
final class ArenaModels {
    private ArenaModels() {}

    private static final TextureSlot MODEL = TextureSlot.create("model");
    private static final TextureSlot MODEL_GLOW = TextureSlot.create("model_glow");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createTrivialCube(ArenaGear.ARENA_SEAL.get());
        blocks.createCrossBlock(ArenaGear.GLADIATOR_NET_BLOCK.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        items.generateFlatItem(ArenaGear.GLADIATOR_NET.get(), ModelTemplates.FLAT_ITEM);
        ModModelProvider.flat(items, ArenaGear.VENTOR_SPAWN_EGG.get());
        stormTrident(items);
    }

    // =====================================================================================
    // Der Sturmdreizack
    // =====================================================================================

    /**
     * Die Teile (Spitze oben; y = 0 ist die Oberkante des Querstücks, wo beim Dreizack von Minecraft die Zinken beginnen):
     * {von x, y, z, bis x, y, z, UV vorn/hinten (u0, v0, u1, v1), UV der Seiten, UV oben/unten, Leuchten}. Die UV-Bereiche
     * (Einheiten 0..16 = 32 Pixel) malt tools/arena_textures.py aus.
     */
    static final float[][] TRIDENT = {
            // der Knauf, der Schaft aus Eschenholz mit Kupferspirale, die Lederwicklung, ein Kupferband, die Tülle
            {-0.95f, -30.2f, -0.95f, 0.95f, -28.6f, 0.95f, 3, 0, 5, 2, 3, 0, 5, 2, 3, 2, 5, 4, 0},
            {-0.55f, -28.6f, -0.55f, 0.55f, -3.0f, 0.55f, 0, 0, 1, 16, 1, 0, 2, 16, 3, 2, 4, 3, 0},
            {-0.75f, -22.0f, -0.75f, 0.75f, -15.5f, 0.75f, 2, 0, 3, 8, 2, 0, 3, 8, 3, 2, 4, 3, 0},
            {-0.7f, -9.6f, -0.7f, 0.7f, -9.0f, 0.7f, 3, 0, 5, 1, 3, 0, 4, 1, 3, 2, 4, 3, 0},
            {-0.95f, -3.0f, -0.95f, 0.95f, -1.5f, 0.95f, 3, 0, 5, 2, 3, 0, 5, 2, 3, 2, 5, 4, 0},
            // das Querstück aus Bronze, in der Mitte vorn und hinten die Windrune (leuchtet)
            {-3.3f, -1.5f, -0.7f, 3.3f, 0.0f, 0.7f, 5, 0, 11, 2, 5, 2, 6, 4, 5, 4, 11, 6, 0},
            {-0.6f, -1.35f, -0.78f, 0.6f, -0.15f, 0.78f, 6, 2, 8, 4, 6, 2, 7, 4, 6, 2, 8, 3, 1},
            // die mittlere Zinke und ihre Spitze (leuchtet)
            {-0.5f, 0.0f, -0.5f, 0.5f, 7.5f, 0.5f, 11, 0, 12, 10, 12, 0, 13, 10, 13, 0, 14, 1, 0},
            {-0.3f, 7.5f, -0.3f, 0.3f, 8.8f, 0.3f, 13, 0, 14, 3, 13, 0, 14, 3, 13, 0, 14, 1, 1},
            // die seitlichen Zinken, ihre Spitzen und Widerhaken
            {2.3f, 0.0f, -0.45f, 3.2f, 5.6f, 0.45f, 11, 0, 12, 8, 12, 0, 13, 8, 13, 0, 14, 1, 0},
            {2.45f, 5.6f, -0.3f, 3.05f, 6.6f, 0.3f, 13, 0, 14, 3, 13, 0, 14, 3, 13, 0, 14, 1, 1},
            {3.2f, 3.4f, -0.35f, 3.8f, 4.9f, 0.35f, 13, 3, 14, 6, 13, 3, 14, 6, 13, 3, 14, 4, 0},
            {-3.2f, 0.0f, -0.45f, -2.3f, 5.6f, 0.45f, 11, 0, 12, 8, 12, 0, 13, 8, 13, 0, 14, 1, 0},
            {-3.05f, 5.6f, -0.3f, -2.45f, 6.6f, 0.3f, 13, 0, 14, 3, 13, 0, 14, 3, 13, 0, 14, 1, 1},
            {-3.8f, 3.4f, -0.35f, -3.2f, 4.9f, 0.35f, 13, 3, 14, 6, 13, 3, 14, 6, 13, 3, 14, 4, 0},
    };
    /** So weit liegen die Teile im Modell verschoben (die Elemente müssen zwischen −16 und 32 bleiben). */
    private static final Vector3f SHIFT = new Vector3f(8.0f, 20.0f, 8.0f);
    /** So viel kleiner als gebaut erscheint er in der Hand: so lang wie der Dreizack von Minecraft (31 Pixel). */
    private static final float SCALE = 0.8f;

    private static void stormTrident(ItemModelGenerators items) {
        Item item = ArenaGear.STORM_TRIDENT.get();
        TextureMapping textures = new TextureMapping()
                .put(MODEL, new Material(KingdomsAtWar.id("item/storm_trident_model")))
                .put(MODEL_GLOW, new Material(KingdomsAtWar.id("item/storm_trident_model_glow")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("item/storm_trident")));
        // in der Hand (die Haltungen von „trident_in_hand“) und beim Ausholen (die von „trident_throwing“)
        ExtendedModelTemplateBuilder hand = base();
        hold(hand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 0, 60, 0, 11, 17, -2, false);
        hold(hand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 0, 60, 0, 3, 17, 12, true);
        hold(hand, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0, -90, 25, -3, 17, 1, false);
        hold(hand, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0, 90, -25, 13, 17, 1, true);
        parts(hand);
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(item, "_in_hand"), textures, items.modelOutput);
        ExtendedModelTemplateBuilder raised = base();
        hold(raised, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 0, 90, 180, 8, -17, 9, false);
        hold(raised, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 0, 90, 180, 8, -17, -7, true);
        hold(raised, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0, -90, 25, -3, 17, 1, false);
        hold(raised, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0, 90, -25, 13, 17, 1, true);
        parts(raised);
        Identifier throwing = raised.build().create(ModelLocationUtils.getModelLocation(item, "_throwing"), textures, items.modelOutput);
        ItemModel.Unbaked flat = ItemModelUtils.plainModel(items.createFlatItemModel(item, ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked held = ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), ItemModelUtils.plainModel(throwing),
                ItemModelUtils.plainModel(inHand));
        items.itemModelOutput.accept(item, ItemModelGenerators.createFlatModelDispatch(flat, held), new ClientItem.Properties(true, false, 1.0f));
    }

    private static ExtendedModelTemplateBuilder base() {
        return ExtendedModelTemplateBuilder.builder().requiredTextureSlot(MODEL).requiredTextureSlot(MODEL_GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE).guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT);
    }

    /**
     * Eine Haltung, wie sie beim Dreizack von Minecraft steht (Drehung, Verschiebung wie im JSON), umgerechnet auf dieses Modell:
     * Es ist um {@link #SHIFT} verschoben und um {@link #SCALE} kleiner – die Verschiebung wird so ausgeglichen, dass die
     * Zinken genau dort sitzen, wo die des Dreizacks von Minecraft säßen (das Spiel spiegelt Verschiebung x und Drehungen y, z
     * für die linke Hand selbst).
     */
    private static void hold(ExtendedModelTemplateBuilder b, ItemDisplayContext context, float rx, float ry, float rz, float tx, float ty,
            float tz, boolean left) {
        float applyTx = left ? -tx : tx;
        float applyRy = left ? -ry : ry;
        float applyRz = left ? -rz : rz;
        // Ein Punkt v des Modells landet bei T + R·S·(v − 8) (in Sechzehnteln), einer des Dreizacks von Minecraft bei T + R·(w − 8):
        // Damit das Querstück (v = SHIFT, w = 0) am selben Fleck sitzt, verschiebt man um R·(S·SHIFT + 8·(1 − S)).
        Vector3f offset = new Vector3f(SHIFT).mul(SCALE).add(8.0f * (1.0f - SCALE), 8.0f * (1.0f - SCALE), 8.0f * (1.0f - SCALE));
        new Quaternionf().rotationXYZ(rx * (float) (Math.PI / 180.0), applyRy * (float) (Math.PI / 180.0), applyRz * (float) (Math.PI / 180.0))
                .transform(offset);
        float nx = applyTx - offset.x;
        float jsonTx = left ? -nx : nx;
        float jsonTy = ty - offset.y;
        float jsonTz = tz - offset.z;
        b.transform(context, t -> t.rotation(rx, ry, rz).translation(jsonTx, jsonTy, jsonTz).scale(SCALE, SCALE, SCALE));
    }

    private static void parts(ExtendedModelTemplateBuilder b) {
        for (float[] p : TRIDENT) {
            boolean glow = p[18] > 0;
            b.element(e -> {
                e.from(p[0] + SHIFT.x, p[1] + SHIFT.y, p[2] + SHIFT.z).to(p[3] + SHIFT.x, p[4] + SHIFT.y, p[5] + SHIFT.z);
                e.allFaces((direction, face) -> {
                    face.texture(glow ? MODEL_GLOW : MODEL);
                    int i = switch (direction.getAxis()) {
                        case Y -> 14;
                        case X -> 10;
                        case Z -> 6;
                    };
                    if (direction == Direction.NORTH || direction == Direction.EAST) {
                        face.uvs(p[i + 2], p[i + 1], p[i], p[i + 3]);
                    } else {
                        face.uvs(p[i], p[i + 1], p[i + 2], p[i + 3]);
                    }
                });
                if (glow) {
                    e.lightEmission(15);
                }
            });
        }
    }
}
