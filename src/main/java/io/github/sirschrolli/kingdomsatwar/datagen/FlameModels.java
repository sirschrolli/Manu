package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.nether.LavaCrustBlock;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle zum Feueropal (seit 1.1.6): die Erze (Knollen über Netherrack und Schwarzstein, die hellsten Pixel leuchten), der
 * Block (mit wogendem Feuer in der Leuchtebene), der Stein (im Inventar das Bild, in der Hand plastisch), die Rüstung der
 * Glutwandler und die Glutkruste in vier Stufen (je älter, desto weiter aufgebrochen und heller glühend).
 * <p>
 * Seit 1.2 das Flammenheiligtum: der <b>Flammenaltar</b> (Sockel aus Schwarzsteinziegeln, ein goldener Absatz, der Schaft
 * aus poliertem Schwarzstein mit eingelegten Flammenzeichen und vorn einem Feueropal in goldener Fassung, ein goldener
 * Kragen, darauf das goldene Becken mit Rand; im Becken Glut, darüber zwei gekreuzte Flammen – solange die Priesterin
 * betet, im Kampf lodern sie höher; ist sie besiegt, liegt im Becken kalte Asche und der Opal glimmt nicht mehr) und die
 * <b>Flammenpeitsche</b> (im Inventar das Bild, in der Hand das 3D-Modell: Opalknauf, Goldkappe, Lederwicklung, Goldringe,
 * die geflochtene Schnur schwingt in einer Welle, ihr Ende glüht, an der Spitze brennt eine Flamme).
 */
final class FlameModels {
    private FlameModels() {}

    private static final TextureSlot STONE = TextureSlot.create("stone");
    private static final TextureSlot BASE = TextureSlot.create("base");
    private static final TextureSlot GOLD = TextureSlot.create("gold");
    private static final TextureSlot EMBERS = TextureSlot.create("embers");
    private static final TextureSlot OPAL = TextureSlot.create("opal");
    private static final TextureSlot FIRE = TextureSlot.create("fire");

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        ModModelProvider.ore(blocks, FlameGear.FIRE_OPAL_ORE, Blocks.NETHERRACK, "fire_opal");
        ModModelProvider.ore(blocks, FlameGear.BLACKSTONE_FIRE_OPAL_ORE, Blocks.BLACKSTONE, "fire_opal");
        ModModelProvider.glowingCube(blocks, FlameGear.FIRE_OPAL_BLOCK.get());
        ModModelProvider.flat(items, FlameGear.FIRE_OPAL.get());
        ModModelProvider.armor(items, FlameGear.EMBERWALKER_HELMET.get(), FlameGear.EMBERWALKER_CHESTPLATE.get(),
                FlameGear.EMBERWALKER_LEGGINGS.get(), FlameGear.EMBERWALKER_BOOTS.get());

        var crust = FlameGear.LAVA_CRUST.get();
        Identifier[] ages = new Identifier[LavaCrustBlock.MAX_AGE + 1];
        for (int age = 0; age <= LavaCrustBlock.MAX_AGE; age++) {
            ages[age] = ModModelProvider.glowingCubeModel(blocks, crust, "_" + age);
        }
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(crust).with(PropertyDispatch.initial(LavaCrustBlock.AGE)
                .generate(age -> BlockModelGenerators.plainVariant(ages[age]))));

        altar(blocks);
        MonsterGearModels.weapon(items, FlameSanctuary.FLAME_WHIP.get(), "flame_whip", whipParts(), 0.55f, 2.4f);
        ModModelProvider.flat(items, FlameSanctuary.IGNARA_SPAWN_EGG.get());
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    // =====================================================================================
    // Der Flammenaltar
    // =====================================================================================

    /** Ein Quader; jede Seite zeigt den Ausschnitt der Textur, der ihrer Lage im Block entspricht (Pixel bleiben quadratisch). */
    private static void box(ExtendedModelTemplateBuilder b, float x0, float y0, float z0, float x1, float y1, float z1, TextureSlot side,
            TextureSlot top, int light) {
        b.element(e -> {
            e.from(x0, y0, z0).to(x1, y1, z1);
            e.allFaces((direction, face) -> {
                switch (direction.getAxis()) {
                    case Y -> face.uvs(x0, z0, x1, z1).texture(top);
                    case Z -> face.uvs(x0, 16.0f - y1, x1, 16.0f - y0).texture(side);
                    default -> face.uvs(z0, 16.0f - y1, z1, 16.0f - y0).texture(side);
                }
                if (direction == Direction.DOWN && y0 <= 0.0f) {
                    face.cullface(Direction.DOWN);
                }
            });
            if (light > 0) {
                e.lightEmission(light);
            }
        });
    }

    /** Das Modell des Altars (Blick nach Norden: der Opal sitzt vorn). {@code flame}: Höhe der Flammen (0 = erloschen). */
    private static Identifier altarModel(BlockModelGenerators blocks, Block altar, String suffix, boolean lit, float flame) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(STONE).requiredTextureSlot(BASE).requiredTextureSlot(GOLD).requiredTextureSlot(EMBERS)
                .requiredTextureSlot(OPAL).requiredTextureSlot(TextureSlot.PARTICLE);
        if (flame > 0.0f) {
            b.requiredTextureSlot(FIRE);
        }
        // der Sockel aus Schwarzsteinziegeln und der goldene Absatz
        box(b, 0, 0, 0, 16, 3, 16, BASE, BASE, 0);
        box(b, 1, 3, 1, 15, 4, 15, GOLD, GOLD, 0);
        // der Schaft aus poliertem Schwarzstein (mit den Flammenzeichen) und der goldene Kragen
        box(b, 3, 4, 3, 13, 10, 13, STONE, STONE, 0);
        box(b, 2, 10, 2, 14, 11, 14, GOLD, GOLD, 0);
        // vorn der Feueropal in seiner goldenen Fassung
        box(b, 5.5f, 4.5f, 2.6f, 10.5f, 9.5f, 3.0f, OPAL, GOLD, 0);
        b.element(e -> {
            e.from(6, 5, 2.2f).to(10, 9, 2.6f);
            e.allFaces((direction, face) -> {
                switch (direction.getAxis()) {
                    case Z -> face.uvs(6, 6, 10, 10);
                    case X -> face.uvs(6, 6, 6.4f, 10);
                    default -> face.uvs(6, 6, 10, 6.4f);
                }
                face.texture(OPAL);
            });
            if (lit) {
                e.lightEmission(12);
            }
        });
        // das Becken, sein Rand und darin die Glut (oder die Asche)
        box(b, 1, 11, 1, 15, 14, 15, GOLD, GOLD, 0);
        box(b, 0.5f, 14, 0.5f, 15.5f, 15, 1.5f, GOLD, GOLD, 0);
        box(b, 0.5f, 14, 14.5f, 15.5f, 15, 15.5f, GOLD, GOLD, 0);
        box(b, 0.5f, 14, 1.5f, 1.5f, 15, 14.5f, GOLD, GOLD, 0);
        box(b, 14.5f, 14, 1.5f, 15.5f, 15, 14.5f, GOLD, GOLD, 0);
        b.element(e -> {
            e.from(1.5f, 13.6f, 1.5f).to(14.5f, 14.3f, 14.5f);
            e.face(Direction.UP, face -> face.uvs(1.5f, 1.5f, 14.5f, 14.5f).texture(EMBERS));
            if (lit) {
                e.lightEmission(15);
            }
        });
        // die Flammen: zwei gekreuzte Flächen über der Glut
        if (flame > 0.0f) {
            for (float angle : new float[] {45.0f, -45.0f}) {
                b.element(e -> {
                    e.from(1.5f, 14.0f, 8.0f).to(14.5f, 14.0f + flame, 8.0f);
                    e.face(Direction.NORTH, face -> face.uvs(1.5f, 0, 14.5f, 16).texture(FIRE));
                    e.face(Direction.SOUTH, face -> face.uvs(1.5f, 0, 14.5f, 16).texture(FIRE));
                    e.lightEmission(15).ambientOcclusion(false);
                    e.rotation(r -> r.origin(8, 14, 8).eulerXYZ(0, angle, 0));
                });
            }
        }
        TextureMapping textures = new TextureMapping()
                .put(STONE, tex("block/flame_altar_stone")).put(BASE, tex("block/flame_altar_base"))
                .put(GOLD, tex("block/flame_altar_gold")).put(OPAL, tex("block/flame_altar_opal"))
                .put(EMBERS, tex(lit ? "block/flame_altar_coals" : "block/flame_altar_ash"))
                .put(TextureSlot.PARTICLE, tex("block/flame_altar_base"));
        if (flame > 0.0f) {
            textures.put(FIRE, tex("block/flame_altar_fire"));
        }
        return b.build().createWithSuffix(altar, suffix, textures, blocks.modelOutput);
    }

    private static void altar(BlockModelGenerators blocks) {
        Block altar = FlameSanctuary.FLAME_ALTAR.get();
        Identifier praying = altarModel(blocks, altar, "", true, 13.0f);
        Identifier awake = altarModel(blocks, altar, "_awake", true, 17.0f);
        Identifier defeated = altarModel(blocks, altar, "_defeated", false, 0.0f);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(altar)
                .with(PropertyDispatch.initial(FlameAltarBlock.PHASE).generate(phase -> BlockModelGenerators.plainVariant(switch (phase) {
                    case PRAYING -> praying;
                    case AWAKE -> awake;
                    case DEFEATED -> defeated;
                })))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleItemModel(altar, praying);
    }

    // =====================================================================================
    // Die Flammenpeitsche
    // =====================================================================================

    /** Ein Teil des 3D-Modells im Format von {@link MonsterGearModels#addParts} (senkrecht gebaut, Mitte x = z = 8). */
    private static float[] part(float x0, float y0, float z0, float x1, float y1, float z1, float[] uvSide, float[] uvTop, boolean glow,
            float rotZ, float px, float py) {
        return new float[] {x0, y0, z0, x1, y1, z1, uvSide[0], uvSide[1], uvSide[2], uvSide[3], uvSide[0], uvSide[1], uvSide[2], uvSide[3],
                uvTop[0], uvTop[1], uvTop[2], uvTop[3], glow ? 1 : 0, 0, 0, rotZ, px, py, 8};
    }

    /**
     * Die Peitsche: unten der Griff (Opalknauf, Goldkappe, Lederwicklung, Goldring, goldener Hals), darüber die Schnur aus
     * dreizehn Gliedern, die in einer Welle schwingt und dünner wird; die letzten vier glühen, an der Spitze brennt die Flamme.
     */
    static float[][] whipParts() {
        List<float[]> parts = new ArrayList<>();
        float[] gold = {0, 0, 1.6f, 1.6f};
        parts.add(part(7.3f, -8.6f, 7.3f, 8.7f, -7.2f, 8.7f, new float[] {0, 0, 1.4f, 1.4f}, new float[] {0, 0, 1.4f, 1.4f}, true, 0, 8, -7.9f));
        parts.add(part(7.2f, -7.2f, 7.2f, 8.8f, -6.6f, 8.8f, gold, gold, false, 0, 8, -6.9f));
        parts.add(part(7.4f, -6.6f, 7.4f, 8.6f, -0.6f, 8.6f, new float[] {2, 0, 3.2f, 6}, new float[] {2, 0, 3.2f, 1.2f}, false, 0, 8, -3.6f));
        parts.add(part(7.25f, -0.6f, 7.25f, 8.75f, 0.2f, 8.75f, gold, gold, false, 0, 8, -0.2f));
        parts.add(part(7.45f, 0.2f, 7.45f, 8.55f, 1.4f, 8.55f, gold, gold, false, 0, 8, 0.8f));
        // die Schnur: Glied für Glied, jedes um seinen Fuß gedreht
        float x = 8.0f;
        float y = 1.3f;
        float angle = 0.0f;
        int links = 13;
        float length = 1.75f;
        for (int i = 0; i < links; i++) {
            angle = (float) (30.0 * Math.sin(i * 0.48 + 0.2));
            float w = 0.95f - 0.04f * i;
            float v0 = i * 1.2f;
            boolean glow = i >= links - 4;
            parts.add(part(x - w / 2, y, 8 - w / 2, x + w / 2, y + length, 8 + w / 2, new float[] {4, v0, 5, v0 + 1.2f},
                    new float[] {4, v0, 5, v0 + 1.0f}, glow, angle, x, y));
            double rad = Math.toRadians(angle);
            x -= (float) (Math.sin(rad) * (length - 0.15f));
            y += (float) (Math.cos(rad) * (length - 0.15f));
        }
        // die Flamme an der Spitze: ein breiterer und ein schmaler, weißglühender Teil
        parts.add(part(x - 0.55f, y - 0.1f, 7.45f, x + 0.55f, y + 1.3f, 8.55f, new float[] {6, 1, 8, 4}, new float[] {6, 1, 8, 3}, true, angle,
                x, y));
        parts.add(part(x - 0.3f, y + 1.3f, 7.7f, x + 0.3f, y + 2.5f, 8.3f, new float[] {6, 0, 7, 1}, new float[] {6, 0, 7, 1}, true, angle,
                x, y));
        return parts.toArray(new float[0][]);
    }
}
