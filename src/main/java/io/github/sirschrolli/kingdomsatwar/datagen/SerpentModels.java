package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlock;
import io.github.sirschrolli.kingdomsatwar.registry.SerpentGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zum Schlangentempel (seit 1.10): der Schlangenaltar in drei Zuständen (das goldene Schlangenbild mit dunklen Augen,
 * während sie schläft; mit leuchtenden Smaragdaugen, während sie wacht; zerbrochen, nur Splitter, wenn sie besiegt ist), der
 * Schlangenzahn (im Inventar das Bild, in der Hand das 3D-Modell), die Quetzalfeder und das Spawn-Ei Quetzaras.
 */
final class SerpentModels {
    private SerpentModels() {}

    private static final TextureSlot STONE = TextureSlot.create("stone");
    private static final TextureSlot TOP = TextureSlot.create("top");
    private static final TextureSlot PLINTH = TextureSlot.create("plinth");
    private static final TextureSlot GOLD = TextureSlot.create("gold");
    private static final TextureSlot FEATHER = TextureSlot.create("feather");
    private static final TextureSlot EYE = TextureSlot.create("eye");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        altar(blocks);
        items.generateFlatItem(SerpentGear.SERPENT_ALTAR.get().asItem(), ModelTemplates.FLAT_ITEM);
        items.generateFlatItem(SerpentGear.QUETZAL_FEATHER.get(), ModelTemplates.FLAT_ITEM);
        MonsterGearModels.weapon(items, SerpentGear.SERPENT_FANG.get(), "serpent_fang", ArmoryShapes.SERPENT_FANG, 0.68f, 0.0f);
        ModModelProvider.flat(items, SerpentGear.QUETZARA_SPAWN_EGG.get());
    }

    /** Ein Quader; die Seiten zeigen den Ausschnitt, der ihrer Lage im Block entspricht (über 16 hinaus: verschoben). */
    private static void box(ExtendedModelTemplateBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, TextureSlot side,
            TextureSlot cap, boolean glow) {
        b.element(e -> {
            e.from(x0, y0, z0).to(x1, y1, z1);
            float[] u = span(x0, x1);
            float[] w = span(z0, z1);
            float[] v = span(16.0f - y1, 16.0f - y0);
            e.allFaces((direction, face) -> {
                switch (direction) {
                    case UP, DOWN -> face.uvs(u[0], w[0], u[1], w[1]).texture(cap);
                    case NORTH, SOUTH -> face.uvs(u[0], v[0], u[1], v[1]).texture(side);
                    default -> face.uvs(w[0], v[0], w[1], v[1]).texture(side);
                }
                if (direction == Direction.DOWN && y0 <= 0.0f) {
                    face.cullface(Direction.DOWN);
                }
            });
            if (glow) {
                e.lightEmission(15);
            }
        });
    }

    /** Der Ausschnitt der Textur (0..16) für eine Strecke von c0 bis c1: dieselbe Länge, in die Textur geschoben. */
    private static float[] span(float c0, float c1) {
        float len = Math.min(16.0f, c1 - c0);
        float a = c0 % 16.0f;
        if (a < 0.0f) {
            a += 16.0f;
        }
        if (a + len > 16.0f) {
            a = 16.0f - len;
        }
        return new float[] {a, a + len};
    }

    /**
     * Der Altar: ein Sockel, der Stein mit der Reliefschlange, die Deckplatte; darauf (ganz) das goldene Schlangenbild – zwei
     * Windungen, der aufgerichtete Hals, der Kopf mit spitzer Schnauze (zum Eingang, −z), ein Kamm aus drei Federn und zwei
     * Smaragdaugen – oder (zerbrochen) nur ein Stumpf und Splitter.
     */
    private static Identifier altarModel(BlockModelGenerators blocks, Block altar, SerpentAltarBlock.Phase phase) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(STONE).requiredTextureSlot(TOP).requiredTextureSlot(PLINTH).requiredTextureSlot(GOLD)
                .requiredTextureSlot(FEATHER).requiredTextureSlot(EYE).requiredTextureSlot(TextureSlot.PARTICLE);
        box(b, 1.0f, 0.0f, 1.0f, 15.0f, 3.0f, 15.0f, PLINTH, PLINTH, false);
        box(b, 2.0f, 3.0f, 2.0f, 14.0f, 10.0f, 14.0f, STONE, TOP, false);
        box(b, 1.0f, 10.0f, 1.0f, 15.0f, 11.0f, 15.0f, PLINTH, TOP, false);
        if (phase == SerpentAltarBlock.Phase.FALLEN) {
            // zerbrochen: ein Stumpf der Windung und Splitter auf der Platte
            box(b, 5.5f, 11.0f, 5.5f, 10.5f, 12.5f, 10.5f, GOLD, GOLD, false);
            box(b, 6.5f, 12.5f, 6.5f, 9.0f, 13.6f, 8.5f, GOLD, GOLD, false);
            box(b, 2.5f, 11.0f, 10.0f, 4.2f, 11.6f, 11.6f, GOLD, GOLD, false);
            box(b, 11.0f, 11.0f, 3.0f, 12.4f, 11.5f, 4.4f, GOLD, GOLD, false);
            box(b, 10.2f, 11.0f, 11.6f, 11.8f, 11.9f, 12.8f, GOLD, GOLD, false);
            box(b, 3.4f, 11.0f, 3.2f, 4.4f, 11.4f, 4.4f, FEATHER, FEATHER, false);
        } else {
            boolean awake = phase == SerpentAltarBlock.Phase.AWAKE;
            // zwei Windungen
            box(b, 4.5f, 11.0f, 4.5f, 11.5f, 13.0f, 11.5f, GOLD, GOLD, false);
            box(b, 5.5f, 13.0f, 5.5f, 10.5f, 15.0f, 10.5f, GOLD, GOLD, false);
            // der Hals, der Kopf, die Schnauze
            box(b, 7.0f, 15.0f, 6.5f, 9.0f, 19.5f, 8.5f, GOLD, GOLD, false);
            box(b, 6.4f, 19.0f, 4.6f, 9.6f, 21.4f, 9.0f, GOLD, GOLD, false);
            box(b, 7.0f, 19.3f, 3.2f, 9.0f, 20.8f, 4.6f, GOLD, GOLD, false);
            // der Federkamm
            box(b, 7.5f, 21.4f, 7.0f, 8.5f, 24.6f, 8.2f, FEATHER, FEATHER, false);
            box(b, 6.2f, 21.0f, 7.4f, 7.2f, 23.6f, 8.4f, FEATHER, FEATHER, false);
            box(b, 8.8f, 21.0f, 7.4f, 9.8f, 23.6f, 8.4f, FEATHER, FEATHER, false);
            // die Smaragdaugen (leuchten, wenn sie wacht)
            box(b, 6.2f, 20.1f, 5.2f, 6.45f, 20.8f, 6.2f, EYE, EYE, awake);
            box(b, 9.55f, 20.1f, 5.2f, 9.8f, 20.8f, 6.2f, EYE, EYE, awake);
        }
        String suffix = "_" + phase.getSerializedName();
        TextureMapping textures = new TextureMapping()
                .put(STONE, new Material(KingdomsAtWar.id("block/serpent_altar_stone")))
                .put(TOP, new Material(KingdomsAtWar.id("block/serpent_altar_top")))
                .put(PLINTH, new Material(KingdomsAtWar.id("block/serpent_altar_plinth")))
                .put(GOLD, new Material(KingdomsAtWar.id("block/serpent_altar_gold")))
                .put(FEATHER, new Material(KingdomsAtWar.id("block/serpent_altar_feather")))
                .put(EYE, new Material(KingdomsAtWar.id(phase == SerpentAltarBlock.Phase.AWAKE ? "block/serpent_altar_eye" : "block/serpent_altar_feather")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/serpent_altar_stone")));
        return b.build().create(ModelLocationUtils.getModelLocation(altar, suffix), textures, blocks.modelOutput);
    }

    private static void altar(BlockModelGenerators blocks) {
        Block altar = SerpentGear.SERPENT_ALTAR.get();
        Identifier sleeping = altarModel(blocks, altar, SerpentAltarBlock.Phase.SLEEPING);
        Identifier awake = altarModel(blocks, altar, SerpentAltarBlock.Phase.AWAKE);
        Identifier fallen = altarModel(blocks, altar, SerpentAltarBlock.Phase.FALLEN);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(altar)
                .with(PropertyDispatch.initial(SerpentAltarBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case SLEEPING -> sleeping;
                    case AWAKE -> awake;
                    case FALLEN -> fallen;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }
}
