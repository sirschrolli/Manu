package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlock;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
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
 * Modelle zu Anni dem Troll (0.67):
 * <ul>
 *   <li><b>Fehlerkern:</b> ein zerbrochener Würfel – sechs der acht Ecken aus dunklem Metall mit Farbsaum, zwei fehlen und
 *   schweben als Brocken daneben – um einen flimmernden Kern aus Bildrauschen (leuchtend oder erloschen).</li>
 *   <li><b>Fehlerblock</b> und <b>Bildrauschen</b>: ganze Blöcke (das Rauschen bewegt sich).</li>
 *   <li><b>Bannhammer:</b> im Inventar das Bild, in der Hand ein 3D-Hammer (Stiel mit Lappen, Steinklotz mit Eisenband,
 *   die vordere Hälfte fehlende Textur, drei schwebende Pixel).</li>
 *   <li><b>Fehlerflügel:</b> wie Elytren; <b>Fehlersplitter</b> und Spawn-Ei: flach.</li>
 * </ul>
 */
final class AnniModels {
    private AnniModels() {}

    static final TextureSlot FRAME = TextureSlot.create("frame");
    static final TextureSlot INNER = TextureSlot.create("inner");
    static final TextureSlot HAMMER = TextureSlot.create("hammer");
    static final TextureSlot HAMMER_GLOW = TextureSlot.create("hammer_glow");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        errorCore(blocks);
        blocks.createTrivialBlock(AnniBlocks.MISSING_BLOCK.get(), TexturedModel.CUBE);
        blocks.createTrivialBlock(AnniBlocks.STATIC_BLOCK.get(), TexturedModel.CUBE);
        bannhammer(items);
        items.generateElytra(AnniBlocks.GLITCH_WINGS.get());
        for (var item : AnniBlocks.flatItems()) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : AnniBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    // =====================================================================================
    // Der Fehlerkern
    // =====================================================================================

    /** Ecken des zerbrochenen Würfels {von x, y, z, bis x, y, z} – zwei fehlen (oben vorn rechts, unten hinten links). */
    private static final float[][] CORNERS = {
            {0, 0, 0, 4, 4, 4}, {12, 0, 0, 16, 4, 4}, {12, 0, 12, 16, 4, 16},
            {0, 12, 0, 4, 16, 4}, {0, 12, 12, 4, 16, 16}, {12, 12, 12, 16, 16, 16},
            // die beiden fehlenden Ecken schweben als kleine Brocken daneben
            {13.5f, 13.5f, -1.5f, 15.5f, 15.5f, 0.5f}, {-1.5f, 0.5f, 13.5f, 0.5f, 2.5f, 15.5f},
    };

    private static ExtendedModelTemplateBuilder coreElements(ExtendedModelTemplateBuilder b, boolean lit) {
        // der Kern aus Bildrauschen
        b.element(e -> {
            e.from(4.5f, 4.5f, 4.5f).to(11.5f, 11.5f, 11.5f);
            for (Direction side : Direction.values()) {
                e.face(side, f -> f.texture(INNER).uvs(4.5f, 4.5f, 11.5f, 11.5f));
            }
            if (lit) {
                e.lightEmission(15);
            }
        });
        // vier Kanten verbinden die Ecken (oben und unten je zwei), eine ist gebrochen
        float[][] edges = {{4, 0, 0, 12, 1.5f, 1.5f}, {4, 14.5f, 14.5f, 12, 16, 16}, {0, 4, 0, 1.5f, 12, 1.5f}, {14.5f, 4, 14.5f, 16, 9, 16}};
        for (float[] p : edges) {
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                for (Direction side : Direction.values()) {
                    e.face(side, f -> f.texture(FRAME).uvs(uv(side, p)[0], uv(side, p)[1], uv(side, p)[2], uv(side, p)[3]));
                }
            });
        }
        for (float[] p : CORNERS) {
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                for (Direction side : Direction.values()) {
                    float[] u = uv(side, clamp(p));
                    e.face(side, f -> f.texture(FRAME).uvs(u[0], u[1], u[2], u[3]));
                }
            });
        }
        return b;
    }

    private static float[] clamp(float[] p) {
        float[] q = new float[6];
        for (int i = 0; i < 6; i++) {
            q[i] = Math.max(0.0f, Math.min(16.0f, p[i]));
        }
        return q;
    }

    /** Texturausschnitt einer Seite nach ihrer Lage im Block (wie bei ganzen Blöcken). */
    private static float[] uv(Direction side, float[] p) {
        return switch (side.getAxis()) {
            case Y -> new float[] {p[0], p[2], p[3], p[5]};
            case Z -> new float[] {p[0], 16 - p[4], p[3], 16 - p[1]};
            default -> new float[] {p[2], 16 - p[4], p[5], 16 - p[1]};
        };
    }

    private static void errorCore(BlockModelGenerators blocks) {
        Block core = AnniBlocks.ERROR_CORE.get();
        TextureMapping lit = new TextureMapping().put(FRAME, tex("block/error_core_frame")).put(INNER, tex("block/error_core_inner"))
                .put(TextureSlot.PARTICLE, tex("block/error_core_frame"));
        ExtendedModelTemplateBuilder litBuilder = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(FRAME).requiredTextureSlot(INNER).requiredTextureSlot(TextureSlot.PARTICLE);
        coreElements(litBuilder, true);
        Identifier litModel = litBuilder.build().create(core, lit, blocks.modelOutput);
        ExtendedModelTemplateBuilder deadBuilder = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(FRAME).requiredTextureSlot(INNER).requiredTextureSlot(TextureSlot.PARTICLE);
        coreElements(deadBuilder, false);
        Identifier deadModel = deadBuilder.build().createWithSuffix(core, "_crashed", lit.copy().put(INNER, tex("block/error_core_inner_dead")),
                blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(core)
                .with(PropertyDispatch.initial(ErrorCoreBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(
                        phase == ErrorCoreBlock.Phase.CRASHED ? deadModel : litModel))));
        blocks.registerSimpleItemModel(core, litModel);
    }

    // =====================================================================================
    // Der Bannhammer
    // =====================================================================================

    /**
     * Teile des Hammers {von x, y, z, bis x, y, z, Atlas u0, v0, u1, v1, leuchtet}: senkrecht gebaut (Griff unten, Kopf
     * oben) und dann wie die Werkzeuge von Minecraft schräg gestellt.
     */
    private static final float[][] HAMMER_PARTS = {
            {7.2f, -5.0f, 7.2f, 8.8f, 18.0f, 8.8f, 0, 0, 4, 16, 0},          // Stiel
            {6.9f, -3.0f, 6.9f, 9.1f, 3.0f, 9.1f, 4, 0, 7, 8, 0},            // Lappen am Griff
            {3.6f, 17.0f, 4.2f, 12.4f, 23.6f, 11.8f, 7, 0, 16, 9, 0},        // Steinklotz
            {3.3f, 16.8f, 3.9f, 12.7f, 23.8f, 7.6f, 7, 9, 16, 14, 1},        // vordere Hälfte: fehlende Textur
            {3.4f, 17.8f, 8.0f, 12.6f, 22.8f, 9.2f, 4, 8, 7, 16, 0},         // Eisenband
            {2.0f, 24.6f, 4.6f, 3.2f, 25.8f, 5.8f, 7, 14, 10, 16, 1},        // schwebende Pixel
            {13.0f, 22.4f, 3.0f, 14.0f, 23.4f, 4.0f, 10, 14, 13, 16, 1},
            {6.0f, 25.2f, 2.6f, 7.0f, 26.2f, 3.6f, 13, 14, 16, 16, 1},
    };

    private static void bannhammer(ItemModelGenerators items) {
        Item hammer = AnniBlocks.BANNHAMMER.get();
        TextureMapping textures = new TextureMapping().put(HAMMER, tex("item/bannhammer_model")).put(HAMMER_GLOW, tex("item/bannhammer_model"))
                .put(TextureSlot.PARTICLE, tex("item/bannhammer"));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(HAMMER).requiredTextureSlot(HAMMER_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 25).translation(1.13f, 3.2f, 1.13f).scale(0.8f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0, 90, -25).translation(1.13f, 3.2f, 1.13f).scale(0.8f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, -90, 55).translation(0, 4.0f, 0.5f).scale(1.0f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, 90, -55).translation(0, 4.0f, 0.5f).scale(1.0f));
        for (float[] p : HAMMER_PARTS) {
            boolean glow = p[10] > 0;
            hand.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                e.allFaces((direction, face) -> face.texture(glow ? HAMMER_GLOW : HAMMER).uvs(p[6], p[7], p[8], p[9]));
                if (glow) {
                    e.lightEmission(12);
                }
                e.rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, 0, -45));
            });
        }
        Identifier inHand = hand.build().create(ModelLocationUtils.getModelLocation(hammer, "_in_hand"), textures, items.modelOutput);
        var flat = ItemModelUtils.plainModel(items.createFlatItemModel(hammer, ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(hammer, ItemModelGenerators.createFlatModelDispatch(flat, ItemModelUtils.plainModel(inHand)),
                new ClientItem.Properties(true, false, 1.6f));
    }
}
