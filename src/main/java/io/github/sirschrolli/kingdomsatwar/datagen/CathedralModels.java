package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlock;
import io.github.sirschrolli.kingdomsatwar.registry.CathedralGear;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zur Stillen Kathedrale (seit 1.7): das Orgelpult (der Kasten des Spieltischs mit Registerknöpfen, obenauf das
 * Manual und das schräge Notenpult aus Kupfer mit dem Notenblatt, dessen Noten leuchten – verstummt grau und dunkel), die
 * Stimmgabel (im Inventar das Bild, in der Hand das 3D-Modell) und Sordins Spawn-Ei.
 */
final class CathedralModels {
    private CathedralModels() {}

    private static final TextureSlot FRONT = TextureSlot.create("front");
    private static final TextureSlot SIDE = TextureSlot.create("side");
    private static final TextureSlot TOP = TextureSlot.create("top");
    private static final TextureSlot KEYS = TextureSlot.create("keys");
    private static final TextureSlot RACK = TextureSlot.create("rack");
    private static final TextureSlot SHEET = TextureSlot.create("sheet");
    private static final TextureSlot GLOW = TextureSlot.create("glow");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        desk(blocks);
        MonsterGearModels.weapon(items, CathedralGear.TUNING_FORK.get(), "tuning_fork", ArmoryShapes.TUNING_FORK, 0.45f, 3.0f, 2.5f);
        ModModelProvider.flat(items, CathedralGear.SORDIN_SPAWN_EGG.get());
    }

    /** Ein Quader; die Seiten zeigen den Ausschnitt, der ihrer Lage im Block entspricht. */
    private static void box(ExtendedModelTemplateBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, TextureSlot north,
            TextureSlot side, TextureSlot top) {
        b.element(e -> {
            e.from(x0, y0, z0).to(x1, y1, z1);
            e.allFaces((direction, face) -> {
                switch (direction) {
                    case UP, DOWN -> face.uvs(x0, z0, x1, z1).texture(top);
                    case NORTH -> face.uvs(x0, 16.0f - y1, x1, 16.0f - y0).texture(north);
                    case SOUTH -> face.uvs(x0, 16.0f - y1, x1, 16.0f - y0).texture(side);
                    default -> face.uvs(z0, 16.0f - y1, z1, 16.0f - y0).texture(side);
                }
                if (direction == Direction.DOWN && y0 <= 0.0f) {
                    face.cullface(Direction.DOWN);
                }
            });
        });
    }

    /**
     * Das Modell des Pults (Blick nach Norden: dort steht der Kantor): der Kasten mit der Front voller Registerknöpfe, vorn
     * obenauf das Manual, dahinter der obere Kasten; darauf das Notenpult aus Kupfer, schräg nach hinten gelehnt, auf dem das
     * Notenblatt liegt. {@code lit}: Die Noten leuchten (eine zweite, leuchtende Schicht vor dem Blatt).
     */
    private static Identifier deskModel(BlockModelGenerators blocks, Block desk, String suffix, boolean lit) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(FRONT).requiredTextureSlot(SIDE).requiredTextureSlot(TOP).requiredTextureSlot(KEYS)
                .requiredTextureSlot(RACK).requiredTextureSlot(SHEET).requiredTextureSlot(TextureSlot.PARTICLE);
        if (lit) {
            b.requiredTextureSlot(GLOW);
        }
        // der Kasten (vorn die Registerknöpfe) und das Manual
        box(b, 0, 0, 0, 16, 12, 16, FRONT, SIDE, TOP);
        box(b, 0.5f, 12, 0, 15.5f, 13.5f, 7, SIDE, SIDE, KEYS);
        box(b, 0, 12, 7, 16, 16, 16, SIDE, SIDE, TOP);
        // das Notenpult: eine Platte aus Kupfer, schräg nach hinten gelehnt; vorn das Blatt (und seine leuchtende Schicht)
        b.element(e -> {
            e.from(1.5f, 16, 10.5f).to(14.5f, 27, 11.5f);
            e.rotation(r -> r.origin(8, 16, 11).singleAxis(Direction.Axis.X, 22.5f));
            e.allFaces((direction, face) -> {
                switch (direction) {
                    case NORTH -> face.uvs(1.5f, 1.0f, 14.5f, 15.0f).texture(SHEET);
                    case SOUTH -> face.uvs(1.5f, 1.0f, 14.5f, 15.0f).texture(RACK);
                    default -> face.uvs(0, 0, 1, 11).texture(RACK);
                }
            });
        });
        if (lit) {
            b.element(e -> {
                e.from(1.5f, 16, 10.45f).to(14.5f, 27, 10.5f);
                e.rotation(r -> r.origin(8, 16, 11).singleAxis(Direction.Axis.X, 22.5f));
                e.face(Direction.NORTH, face -> face.uvs(1.5f, 1.0f, 14.5f, 15.0f).texture(GLOW));
                e.lightEmission(15);
                e.ambientOcclusion(false);
            });
        }
        // die Leiste unten am Pult, auf der das Blatt aufliegt
        box(b, 1.5f, 16, 9.0f, 14.5f, 17, 11.0f, RACK, RACK, RACK);
        TextureMapping textures = new TextureMapping()
                .put(FRONT, new Material(KingdomsAtWar.id("block/organ_desk_front")))
                .put(SIDE, new Material(KingdomsAtWar.id("block/organ_desk_side")))
                .put(TOP, new Material(KingdomsAtWar.id("block/organ_desk_top")))
                .put(KEYS, new Material(KingdomsAtWar.id("block/organ_desk_keys")))
                .put(RACK, new Material(KingdomsAtWar.id("block/organ_desk_rack")))
                .put(SHEET, new Material(KingdomsAtWar.id(lit ? "block/organ_desk_sheet" : "block/organ_desk_sheet_silent")))
                .put(TextureSlot.PARTICLE, new Material(KingdomsAtWar.id("block/organ_desk_side")));
        if (lit) {
            textures.put(GLOW, new Material(KingdomsAtWar.id("block/organ_desk_sheet_glow")));
        }
        return b.build().createWithSuffix(desk, suffix, textures, blocks.modelOutput);
    }

    private static void desk(BlockModelGenerators blocks) {
        Block desk = CathedralGear.ORGAN_DESK.get();
        Identifier lit = deskModel(blocks, desk, "", true);
        Identifier silent = deskModel(blocks, desk, "_silenced", false);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(desk)
                .with(PropertyDispatch.initial(OrganDeskBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case GUARDED, AWAKE -> lit;
                    case SILENCED -> silent;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleItemModel(desk, lit);
    }
}
