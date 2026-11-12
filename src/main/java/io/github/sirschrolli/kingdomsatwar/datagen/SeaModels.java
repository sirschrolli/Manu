package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.block.sea.SeashellBlock;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle des Meeres: die Tiefseelaterne (wie eine Laterne, stehend und hängend), die Blöcke des Meeresgrunds (0.62),
 * die Beute und die Spawn-Eier.
 *
 * <ul>
 *   <li><b>Seeanemone:</b> ein Fuß (6 × 5 × 6) mit Mundscheibe, darüber vier sich kreuzende Flächen mit dem bewegten
 *   Kranz der Fangarme – je zweimal: einmal gewöhnlich, einmal die leuchtende Maske (Lichtstärke 15).</li>
 *   <li><b>Muscheln:</b> ein bis drei kleine Schalen, jede schräg gedreht: die Herzmuschel als gewölbter Hügel aus drei
 *   Stufen, die Kammmuschel als flacher Fächer (zwei Lagen), die Wellhornschnecke als liegender Kegel aus vier Windungen
 *   mit der Mündung vorn.</li>
 *   <li><b>Röhrenwürmer:</b> fünf geringelte Röhren verschiedener Höhe, auf jeder eine rote Federkrone aus zwei sich
 *   kreuzenden Flächen.</li>
 *   <li><b>Heißer Schlot:</b> wie ein Säulenblock, oben zusätzlich die glühende Öffnung (Lichtstärke 15).</li>
 *   <li><b>Leuchtalgen:</b> ein hauchdünner Belag mit leuchtender Maske, zufällig gedreht.</li>
 * </ul>
 */
final class SeaModels {
    private SeaModels() {}

    static final TextureSlot BODY = TextureSlot.create("body");
    static final TextureSlot ARMS = TextureSlot.create("arms");
    static final TextureSlot GLOW = TextureSlot.create("glow");
    static final TextureSlot SHELL = TextureSlot.create("shell");
    static final TextureSlot TUBE = TextureSlot.create("tube");

    /** Der Hexenkessel (0.97): vier Füße, der Boden, vier Wände mit Innenseite, ein vorstehender Rand und der Trank darin. */
    static final TextureSlot INSIDE = TextureSlot.create("inside");
    static final TextureSlot RIM = TextureSlot.create("rim");
    static final TextureSlot BREW = TextureSlot.create("brew");

    private static ModelTemplate cauldron(boolean lit) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE).requiredTextureSlot(TextureSlot.TOP).requiredTextureSlot(TextureSlot.BOTTOM)
                .requiredTextureSlot(INSIDE).requiredTextureSlot(RIM).requiredTextureSlot(BREW).requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        // die Füße
        for (int[] f : new int[][] {{1, 1}, {12, 1}, {1, 12}, {12, 12}}) {
            int x = f[0];
            int z = f[1];
            b.element(e -> e.from(x, 0, z).to(x + 3, 3, z + 3).allFaces((dir, face) -> face.uvs(x, 13, x + 3, 16)
                    .texture(dir.getAxis() == Direction.Axis.Y ? TextureSlot.BOTTOM : TextureSlot.SIDE)));
        }
        // der Boden
        b.element(e -> e.from(1, 3, 1).to(15, 5, 15).allFaces((dir, face) -> face.uvs(1, 11, 15, 13)
                .texture(dir == Direction.UP ? INSIDE : dir == Direction.DOWN ? TextureSlot.BOTTOM : TextureSlot.SIDE)));
        // die Wände: außen die Seite, innen die Innenseite, oben die Kante
        int[][] walls = {{1, 5, 1, 15, 14, 3}, {1, 5, 13, 15, 14, 15}, {1, 5, 3, 3, 14, 13}, {13, 5, 3, 15, 14, 13}};
        for (int[] w : walls) {
            b.element(e -> e.from(w[0], w[1], w[2]).to(w[3], w[4], w[5]).allFaces((dir, face) -> {
                boolean outer = switch (dir) {
                    case NORTH -> w[2] == 1;
                    case SOUTH -> w[5] == 15;
                    case WEST -> w[0] == 1;
                    case EAST -> w[3] == 15;
                    default -> false;
                };
                if (dir == Direction.UP) {
                    face.uvs(w[0], w[2], w[3], w[5]).texture(TextureSlot.TOP);
                } else if (dir == Direction.DOWN) {
                    face.uvs(w[0], w[2], w[3], w[5]).texture(INSIDE);
                } else {
                    face.uvs(1, 2, 15, 11).texture(outer ? TextureSlot.SIDE : INSIDE);
                }
            }));
        }
        // der vorstehende Rand
        int[][] rims = {{0, 13, 0, 16, 15, 2}, {0, 13, 14, 16, 15, 16}, {0, 13, 2, 2, 15, 14}, {14, 13, 2, 16, 15, 14}};
        for (int[] r : rims) {
            b.element(e -> e.from(r[0], r[1], r[2]).to(r[3], r[4], r[5]).allFaces((dir, face) -> face
                    .uvs(dir.getAxis() == Direction.Axis.Y ? r[0] : 0, dir.getAxis() == Direction.Axis.Y ? r[2] : 0,
                            dir.getAxis() == Direction.Axis.Y ? r[3] : 16, dir.getAxis() == Direction.Axis.Y ? r[5] : 2)
                    .texture(RIM)));
        }
        // der Trank: brodelnd und leuchtend – erkaltet tiefer und dunkel
        int level = lit ? 12 : 7;
        b.element(e -> {
            e.from(3, level, 3).to(13, level, 13).face(Direction.UP, f -> f.uvs(3, 3, 13, 13).texture(BREW));
            if (lit) {
                e.lightEmission(15);
            }
        });
        return b.build();
    }

    private static final ModelTemplate CAULDRON_LIT = cauldron(true);
    private static final ModelTemplate CAULDRON_COLD = cauldron(false);

    /** Der Hexenkessel: brodelnd (Hexe schläft oder kämpft) und erkaltet (besiegt). */
    private static void witchCauldron(BlockModelGenerators blocks) {
        Block cauldron = io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.WITCH_CAULDRON.get();
        TextureMapping lit = new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(cauldron, "_side"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(cauldron, "_top"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(cauldron, "_bottom"))
                .put(INSIDE, TextureMapping.getBlockTexture(cauldron, "_inside"))
                .put(RIM, TextureMapping.getBlockTexture(cauldron, "_rim"))
                .put(BREW, TextureMapping.getBlockTexture(cauldron, "_brew"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(cauldron, "_side"));
        TextureMapping cold = lit.copyAndUpdate(BREW, TextureMapping.getBlockTexture(cauldron, "_brew_cold"));
        Identifier litModel = CAULDRON_LIT.create(cauldron, lit, blocks.modelOutput);
        Identifier coldModel = CAULDRON_COLD.createWithSuffix(cauldron, "_cold", cold, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(cauldron).with(PropertyDispatch.initial(
                io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlock.PHASE).generate(phase ->
                BlockModelGenerators.plainVariant(phase == io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlock.Phase.DEFEATED
                        ? coldModel : litModel))));
    }

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createLantern(SeaBlocks.DEEP_SEA_LANTERN.get());
        // das Urmeer (0.89): gemeißelter Gezeitenstein mit leuchtender Welle, der Schleier des Gezeitentors (wie beim
        // Feenportal eine dünne, leuchtende Scheibe mit bewegter, durchscheinender Textur)
        ModModelProvider.glowingCube(blocks, SeaBlocks.CHISELED_TIDE_STONE.get());
        Block gate = SeaBlocks.TIDE_GATE.get();
        net.minecraft.client.resources.model.sprite.Material gateTexture = TextureMapping.getBlockTexture(gate).withForceTranslucent(true);
        TextureMapping gateMapping = new TextureMapping().put(FaeModels.PORTAL, gateTexture).put(TextureSlot.PARTICLE, gateTexture);
        Identifier gateNs = FaeModels.PORTAL_NS.create(gate, gateMapping, blocks.modelOutput);
        Identifier gateEw = FaeModels.PORTAL_EW.create(gate, gateMapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(gate).with(PropertyDispatch.initial(
                        io.github.sirschrolli.kingdomsatwar.block.sea.TideGateBlock.AXIS)
                .select(Direction.Axis.X, BlockModelGenerators.plainVariant(gateNs))
                .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(gateEw))));
        for (var item : SeaBlocks.flatItems()) {
            ModModelProvider.flat(items, item.get());
        }
        for (var egg : SeaBlocks.spawnEggs()) {
            ModModelProvider.flat(items, egg.get());
        }
        seafloor(blocks);
        atoll(blocks, items);
        seaWoods(blocks);
        // Aquamarin (0.97): Erz im Gezeitenstein, Block im Treppenschliff, der Edelstein, die Taucherrüstung
        ModModelProvider.ore(blocks, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_ORE, SeaBlocks.TIDE_STONE.base().get(), "aquamarine");
        ModModelProvider.glowingCube(blocks, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_BLOCK.get());
        ModModelProvider.flat(items, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE.get());
        ModModelProvider.armor(items, io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_HELMET.get(), io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_CHESTPLATE.get(),
                io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_LEGGINGS.get(), io.github.sirschrolli.kingdomsatwar.registry.SeaGear.AQUAMARINE_BOOTS.get());
        // die Meerhexe (0.97): der Hexenkessel, das Spawn-Ei (der Hexenstab steht bei der Waffenkammer)
        witchCauldron(blocks);
        ModModelProvider.flat(items, io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.SEA_WITCH_SPAWN_EGG.get());
    }

    /** Die Meereshölzer (0.92): wie Glimmerholz – Stamm, Holz, Laub, Setzling, Tür, Falltür, Zaun und Zauntor. */
    private static void seaWoods(BlockModelGenerators blocks) {
        for (io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.SeaWood wood : io.github.sirschrolli.kingdomsatwar.registry.SeaWoods.ALL) {
            blocks.woodProvider(wood.log.get()).logWithHorizontal(wood.log.get()).wood(wood.wood.get());
            blocks.woodProvider(wood.strippedLog.get()).logWithHorizontal(wood.strippedLog.get()).wood(wood.strippedWood.get());
            blocks.createTrivialBlock(wood.leaves.get(), net.minecraft.client.data.models.model.TexturedModel.LEAVES);
            blocks.createPlantWithDefaultItem(wood.sapling.get(), wood.pottedSapling.get(), BlockModelGenerators.PlantType.NOT_TINTED);
            blocks.createDoor(wood.door.get());
            blocks.createOrientableTrapdoor(wood.trapdoor.get());
            blocks.familyWithExistingFullBlock(wood.planks.base().get()).fence(wood.fence.get()).fenceGate(wood.fenceGate.get());
        }
    }

    // =====================================================================================
    // Der Gezeiten-Speer und das Atoll
    // =====================================================================================

    static final TextureSlot SPEAR = TextureSlot.create("spear");
    static final TextureSlot SPEAR_GLOW = TextureSlot.create("spear_glow");

    /** Teile des Speers längs der Y-Achse (Mitte x = z = 8): {von x, y, z, bis x, y, z, Bereich der Textur u0, v0, u1, v1, leuchtet}. */
    private static final float[][] SPEAR_PARTS = {
            {7.3f, -3.6f, 7.3f, 8.7f, -2.2f, 8.7f, 4, 0, 5.4f, 1.4f, 0},          // goldene Kappe
            {7.4f, -2.2f, 7.4f, 8.6f, 2.0f, 8.6f, 2, 0, 3.2f, 4.2f, 0},           // Griffwicklung
            {7.5f, 2.0f, 7.5f, 8.5f, 13.6f, 8.5f, 0, 0, 1, 11.6f, 0},             // Schaft aus Treibholz
            {7.35f, 6.0f, 7.35f, 8.65f, 6.7f, 8.65f, 4, 2, 5.3f, 2.7f, 0},        // Goldringe
            {7.35f, 9.6f, 7.35f, 8.65f, 10.3f, 8.65f, 4, 2, 5.3f, 2.7f, 0},
            {6.3f, 13.6f, 7.2f, 9.7f, 14.8f, 8.8f, 4, 3, 6, 4.2f, 0},             // Kragen
            {7.4f, 13.7f, 6.9f, 8.6f, 14.7f, 7.2f, 12, 0, 14, 2, 1},              // Perle vorn
            {7.4f, 13.7f, 8.8f, 8.6f, 14.7f, 9.1f, 12, 0, 14, 2, 1},              // Perle hinten
            {5.6f, 14.2f, 7.6f, 6.6f, 16.8f, 8.4f, 6, 0, 7, 2.6f, 0},             // Widerhaken
            {9.4f, 14.2f, 7.6f, 10.4f, 16.8f, 8.4f, 6, 0, 7, 2.6f, 0},
            {6.8f, 14.8f, 7.6f, 9.2f, 17.4f, 8.4f, 6, 6, 10, 10, 0},              // Klinge in vier Stufen
            {7.1f, 17.4f, 7.62f, 8.9f, 19.4f, 8.38f, 6.5f, 3, 9.5f, 6, 0},
            {7.4f, 19.4f, 7.65f, 8.6f, 20.8f, 8.35f, 7, 1, 9, 3, 0},
            {7.7f, 20.8f, 7.7f, 8.3f, 21.8f, 8.3f, 7.5f, 0, 8.5f, 1, 0},
            {7.8f, 15.0f, 7.55f, 8.2f, 20.6f, 8.45f, 10, 0, 12, 10, 2},           // leuchtende Ader
    };

    /**
     * Der Speer als 3D-Modell: alle Teile gemeinsam um {@code origin} gedreht ({@code angles} wie im Spiel: x, y, z), um
     * {@code lift} angehoben. In der Hand liegt er diagonal wie die Speere von Minecraft – die Spitze oben <em>links</em>
     * (Drehung +45° um Z), denn dafür sind deren Griffhaltungen gemacht: Bis 0.78 zeigte die Spitze nach oben rechts (wie
     * bei einem Schwert), und der Speer stand beim Tragen senkrecht und lag beim Ansturm quer. Im Hügel steckt er schräg.
     */
    private static ExtendedModelTemplateBuilder spearElements(ExtendedModelTemplateBuilder b, float lift, float[] origin, float[] angles) {
        for (float[] p : SPEAR_PARTS) {
            boolean glow = p[10] > 0;
            boolean core = p[10] > 1;
            b.element(e -> {
                e.from(p[0], p[1] + lift, p[2]).to(p[3], p[4] + lift, p[5]);
                e.allFaces((direction, face) -> {
                    face.texture(core ? SPEAR_GLOW : SPEAR);
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

    private static void atoll(BlockModelGenerators blocks, ItemModelGenerators items) {
        // Gezeiten-Speer: im Inventar das Bild, in der Hand das 3D-Modell (wie die Speere von Minecraft gehalten)
        net.minecraft.world.item.Item spear = SeaBlocks.TIDE_SPEAR.get();
        TextureMapping spearTextures = new TextureMapping()
                .put(SPEAR, new net.minecraft.client.resources.model.sprite.Material(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("item/tide_spear_model")))
                .put(SPEAR_GLOW, new net.minecraft.client.resources.model.sprite.Material(
                        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("item/tide_spear_model_glow")))
                .put(TextureSlot.PARTICLE, new net.minecraft.client.resources.model.sprite.Material(
                        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("item/tide_spear")));
        ExtendedModelTemplateBuilder hand = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(SPEAR).requiredTextureSlot(SPEAR_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        t -> t.rotation(-20, 90, -35).translation(3.13f, 2.0f, 0.13f).scale(1.36f, 1.36f, 0.68f))
                .transform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        t -> t.rotation(-20, -90, 35).translation(3.13f, 2.0f, 0.13f).scale(1.36f, 1.36f, 0.68f))
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        t -> t.rotation(5, 270, -40).translation(0, 2, 2).scale(1.7f, 1.7f, 0.85f))
                .transform(net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        t -> t.rotation(5, -270, 40).translation(0, 2, 2).scale(1.7f, 1.7f, 0.85f));
        spearElements(hand, 0.0f, new float[] {8, 8, 8}, new float[] {0, 0, 45});
        Identifier inHand = hand.build().create(net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation(spear, "_in_hand"),
                spearTextures, items.modelOutput);
        net.minecraft.client.renderer.item.ItemModel.Unbaked flat = net.minecraft.client.data.models.model.ItemModelUtils.plainModel(
                items.createFlatItemModel(spear, net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(spear, ItemModelGenerators.createFlatModelDispatch(flat,
                net.minecraft.client.data.models.model.ItemModelUtils.plainModel(inHand)),
                new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.95f));

        // der Speer im Hügel: schräg nach hinten geneigt, die Spitze leuchtet
        Block embedded = SeaBlocks.EMBEDDED_TIDE_SPEAR.get();
        ExtendedModelTemplateBuilder stuck = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(SPEAR).requiredTextureSlot(SPEAR_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        spearElements(stuck, 2.0f, new float[] {8, 0, 8}, new float[] {-22, 30, 12});
        // Blockmodelle sehen nur den Block-Atlas: dort liegt eine Kopie der Textur des Speers
        TextureMapping stuckTextures = new TextureMapping()
                .put(SPEAR, new net.minecraft.client.resources.model.sprite.Material(
                        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("block/embedded_tide_spear")))
                .put(SPEAR_GLOW, new net.minecraft.client.resources.model.sprite.Material(
                        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("block/embedded_tide_spear_glow")))
                .put(TextureSlot.PARTICLE, new net.minecraft.client.resources.model.sprite.Material(
                        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("block/embedded_tide_spear")));
        Identifier stuckModel = stuck.build().create(embedded, stuckTextures, blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(embedded, BlockModelGenerators.plainVariant(stuckModel)));

        // Herz des Atolls: verborgen unter dem Sand, wie dunkles Prismarin
        Block core = SeaBlocks.ATOLL_CORE.get();
        Identifier coreModel = net.minecraft.client.data.models.model.ModelTemplates.CUBE_ALL.create(core,
                TextureMapping.cube(new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/dark_prismarine"))),
                blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(core, BlockModelGenerators.plainVariant(coreModel)));
        // Herz des Schlunds (0.91): ganz unten im Krakenschlund, glimmt wie ein Leitfeuer
        Block krakenCore = SeaBlocks.KRAKEN_CORE.get();
        Identifier krakenCoreModel = net.minecraft.client.data.models.model.ModelTemplates.CUBE_ALL.create(krakenCore,
                TextureMapping.cube(new net.minecraft.client.resources.model.sprite.Material(Identifier.withDefaultNamespace("block/conduit"))),
                blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(krakenCore, BlockModelGenerators.plainVariant(krakenCoreModel)));
    }

    // =====================================================================================
    // Meeresgrund
    // =====================================================================================

    private static void seafloor(BlockModelGenerators blocks) {
        ModelTemplate anemone = anemone();
        for (var holder : SeaBlocks.anemones()) {
            Block block = holder.get();
            Identifier model = anemone.create(block, new TextureMapping()
                    .put(BODY, TextureMapping.getBlockTexture(block))
                    .put(ARMS, TextureMapping.getBlockTexture(block, "_tentacles"))
                    .put(GLOW, TextureMapping.getBlockTexture(block, "_tentacles_glow"))
                    .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block)), blocks.modelOutput);
            blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
            blocks.registerSimpleItemModel(block, model);
        }

        shells(blocks, SeaBlocks.COCKLE_SHELLS.get(), SeaModels::cockle);
        shells(blocks, SeaBlocks.SCALLOP_SHELLS.get(), SeaModels::scallop);
        shells(blocks, SeaBlocks.WHELK_SHELLS.get(), SeaModels::whelk);

        Block worms = SeaBlocks.TUBE_WORMS.get();
        Identifier wormsModel = tubeWorms().create(worms, new TextureMapping().put(TUBE, TextureMapping.getBlockTexture(worms))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(worms)), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(worms, BlockModelGenerators.plainVariant(wormsModel)));
        blocks.registerSimpleFlatItemModel(worms.asItem());

        blocks.createTrivialCube(SeaBlocks.VENT_ROCK.get());
        blocks.createTrivialCube(SeaBlocks.SULFUR_CRUST.get());

        Block vent = SeaBlocks.HYDROTHERMAL_VENT.get();
        Identifier ventModel = vent().create(vent, new TextureMapping()
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(vent, "_side"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(vent, "_top"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(vent, "_bottom"))
                .put(GLOW, TextureMapping.getBlockTexture(vent, "_top_glow"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(vent, "_side")), blocks.modelOutput);
        blocks.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(vent, BlockModelGenerators.plainVariant(ventModel)));

        Block algae = SeaBlocks.GLOW_ALGAE.get();
        Identifier algaeModel = algae().create(algae, new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(algae))
                .put(GLOW, TextureMapping.getBlockTexture(algae, "_glow"))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(algae)), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(algae,
                BlockModelGenerators.createRotatedVariants(BlockModelGenerators.plainModel(algaeModel))));
        blocks.registerSimpleFlatItemModel(algae.asItem());

        blocks.createGrowingPlant(SeaBlocks.GIANT_KELP.get(), SeaBlocks.GIANT_KELP_PLANT.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blocks.registerSimpleFlatItemModel(SeaBlocks.GIANT_KELP.get().asItem());
    }

    // ------------------------------------------------------------------------------------------------ Seeanemone

    private static ModelTemplate anemone() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BODY).requiredTextureSlot(ARMS).requiredTextureSlot(GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        // Fuß: Seiten aus der Fuß-Textur (unten), oben verdeckt die Mundscheibe
        b.element(e -> e.from(5, 0, 5).to(11, 5, 11)
                .face(Direction.NORTH, f -> f.uvs(0, 11, 6, 16).texture(BODY))
                .face(Direction.SOUTH, f -> f.uvs(6, 11, 12, 16).texture(BODY))
                .face(Direction.WEST, f -> f.uvs(10, 11, 16, 16).texture(BODY))
                .face(Direction.EAST, f -> f.uvs(3, 11, 9, 16).texture(BODY))
                .face(Direction.DOWN, f -> f.uvs(0, 0, 6, 6).texture(BODY).cullface(Direction.DOWN)));
        // Mundscheibe
        b.element(e -> e.from(4.5f, 5, 4.5f).to(11.5f, 6, 11.5f)
                .face(Direction.UP, f -> f.uvs(0, 0, 8, 8).texture(BODY))
                .face(Direction.NORTH, f -> f.uvs(8, 0, 15, 1).texture(BODY))
                .face(Direction.SOUTH, f -> f.uvs(8, 0, 15, 1).texture(BODY))
                .face(Direction.WEST, f -> f.uvs(8, 1, 15, 2).texture(BODY))
                .face(Direction.EAST, f -> f.uvs(8, 1, 15, 2).texture(BODY)));
        // Kranz der Fangarme: vier Flächen durch die Mitte (0°, 45°, 90°, 135°), je gewöhnlich und leuchtend
        for (int k = 0; k < 4; k++) {
            float yaw = k * 45.0f - 22.5f;
            for (int layer = 0; layer < 2; layer++) {
                boolean glow = layer == 1;
                b.element(e -> {
                    e.from(1, 3, 8).to(15, 16, 8)
                            .face(Direction.NORTH, f -> f.uvs(1, 3, 15, 16).texture(glow ? GLOW : ARMS))
                            .face(Direction.SOUTH, f -> f.uvs(15, 3, 1, 16).texture(glow ? GLOW : ARMS))
                            .rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, yaw, 0))
                            .shadeDirectionOverride(Direction.UP);
                    if (glow) {
                        e.lightEmission(15);
                    }
                });
            }
        }
        return b.build();
    }

    // ------------------------------------------------------------------------------------------------ Muscheln

    /** Eine Muschelform an einer Stelle (x, z) mit Drehung (Grad) – fügt ihre Elemente hinzu. */
    @FunctionalInterface
    private interface ShellShape {
        void add(ExtendedModelTemplateBuilder b, float x, float z, float yaw);
    }

    /** Lagen von einer, zwei und drei Muscheln: {x, z, Drehung}. */
    private static final float[][][] SHELL_SPOTS = {
            {{8, 8, 20}},
            {{5.5f, 6, -30}, {10.5f, 10.5f, 55}},
            {{5, 5, 10}, {11, 6.5f, -40}, {7.5f, 11.5f, 75}}};

    private static void shells(BlockModelGenerators blocks, Block block, ShellShape shape) {
        TextureMapping mapping = new TextureMapping().put(SHELL, TextureMapping.getBlockTexture(block))
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block));
        MultiVariant[] variants = new MultiVariant[SeashellBlock.MAX_SHELLS];
        for (int count = 1; count <= SeashellBlock.MAX_SHELLS; count++) {
            ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                    .parent(Identifier.withDefaultNamespace("block/block"))
                    .requiredTextureSlot(SHELL).requiredTextureSlot(TextureSlot.PARTICLE)
                    .suffix(count == 1 ? "" : "_" + count);
            for (float[] spot : SHELL_SPOTS[count - 1]) {
                shape.add(b, spot[0], spot[1], spot[2]);
            }
            variants[count - 1] = BlockModelGenerators.plainVariant(b.build().create(block, mapping, blocks.modelOutput));
        }
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(SeashellBlock.SHELLS)
                        .select(1, variants[0]).select(2, variants[1]).select(3, variants[2]))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blocks.registerSimpleFlatItemModel(block.asItem());
    }

    /** Herzmuschel: eine Wölbung aus drei übereinander liegenden, kleiner werdenden Ovalen (Ränder durchsichtig). */
    private static void cockle(ExtendedModelTemplateBuilder b, float x, float z, float yaw) {
        float[][] tiers = {{2.6f, 2.5f, 0.3f}, {2.0f, 1.9f, 0.9f}, {1.3f, 1.2f, 1.4f}};
        for (float[] t : tiers) {
            float hw = t[0];
            float hd = t[1];
            float y = t[2];
            b.element(e -> e.from(x - hw, y, z - hd).to(x + hw, y, z + hd)
                    .face(Direction.UP, f -> f.uvs(0, 0, 8, 8).texture(SHELL))
                    .face(Direction.DOWN, f -> f.uvs(8, 0, 12, 4).texture(SHELL))
                    .rotation(r -> r.origin(x, 0, z).eulerXYZ(0, yaw, 0)));
        }
    }

    /** Kammmuschel: ein flacher Fächer in zwei Lagen (die obere etwas kleiner), Ränder durchsichtig. */
    private static void scallop(ExtendedModelTemplateBuilder b, float x, float z, float yaw) {
        b.element(e -> e.from(x - 2.75f, 0.3f, z - 2.5f).to(x + 2.75f, 0.3f, z + 2.5f)
                .face(Direction.UP, f -> f.uvs(0, 0, 10, 9).texture(SHELL))
                .face(Direction.DOWN, f -> f.uvs(0, 0, 10, 9).texture(SHELL))
                .rotation(r -> r.origin(x, 0, z).eulerXYZ(0, yaw, 0)));
        b.element(e -> e.from(x - 2.2f, 0.75f, z - 2.1f).to(x + 2.2f, 0.75f, z + 1.6f)
                .face(Direction.UP, f -> f.uvs(0, 0, 10, 9).texture(SHELL))
                .face(Direction.DOWN, f -> f.uvs(0, 0, 10, 9).texture(SHELL))
                .rotation(r -> r.origin(x, 0, z).eulerXYZ(-4, yaw, 0)));
    }

    /** Wellhornschnecke: ein liegender Kegel aus vier Windungen, vorn (−X) die rosige Mündung. */
    private static void whelk(ExtendedModelTemplateBuilder b, float x, float z, float yaw) {
        float[][] whorls = {{-2.5f, 0.5f, 0.0f, 2.4f, 1.3f}, {0.5f, 2.2f, 0.2f, 2.0f, 1.0f}, {2.2f, 3.4f, 0.45f, 1.65f, 0.7f},
                {3.4f, 4.2f, 0.7f, 1.3f, 0.35f}};
        for (int i = 0; i < whorls.length; i++) {
            float[] w = whorls[i];
            boolean first = i == 0;
            boolean last = i == whorls.length - 1;
            b.element(e -> {
                e.from(x + w[0], w[2], z - w[4]).to(x + w[1], w[3], z + w[4])
                        .face(Direction.UP, f -> f.uvs(0, 0, (w[1] - w[0]) * 1.5f, 2).texture(SHELL))
                        .face(Direction.NORTH, f -> f.uvs(2, 0, 2 + (w[1] - w[0]) * 1.5f, 4).texture(SHELL))
                        .face(Direction.SOUTH, f -> f.uvs(5, 0, 5 + (w[1] - w[0]) * 1.5f, 4).texture(SHELL))
                        .face(Direction.DOWN, f -> f.uvs(0, 2, (w[1] - w[0]) * 1.5f, 4).texture(SHELL))
                        .rotation(r -> r.origin(x, 0, z).eulerXYZ(0, yaw, 0));
                if (first) {
                    e.face(Direction.WEST, f -> f.uvs(0, 5, 4, 9).texture(SHELL));
                }
                if (last) {
                    e.face(Direction.EAST, f -> f.uvs(5, 5, 9, 9).texture(SHELL));
                }
            });
        }
    }

    // ------------------------------------------------------------------------------------------------ Röhrenwürmer

    /** Röhren: {x, z, Höhe}. */
    private static final float[][] TUBES = {{5, 5.5f, 9}, {9.5f, 5, 12}, {11.5f, 10, 7}, {5.5f, 10.5f, 10}, {8.5f, 8.5f, 13}};

    private static ModelTemplate tubeWorms() {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TUBE).requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false);
        for (float[] t : TUBES) {
            float x = t[0];
            float z = t[1];
            float h = t[2];
            b.element(e -> e.from(x - 1, 0, z - 1).to(x + 1, h, z + 1)
                    .face(Direction.NORTH, f -> f.uvs(0, 16 - h, 2, 16).texture(TUBE))
                    .face(Direction.SOUTH, f -> f.uvs(2, 16 - h, 4, 16).texture(TUBE))
                    .face(Direction.WEST, f -> f.uvs(1, 16 - h, 3, 16).texture(TUBE))
                    .face(Direction.EAST, f -> f.uvs(0, 16 - h, 2, 16).texture(TUBE))
                    .face(Direction.UP, f -> f.uvs(12, 0, 14, 2).texture(TUBE)));
            for (int k = 0; k < 2; k++) {
                float yaw = 45.0f + k * 90.0f;
                b.element(e -> e.from(x - 2, h - 0.5f, z).to(x + 2, h + 3.5f, z)
                        .face(Direction.NORTH, f -> f.uvs(4, 0, 12, 8).texture(TUBE))
                        .face(Direction.SOUTH, f -> f.uvs(12, 0, 4, 8).texture(TUBE))
                        .rotation(r -> r.origin(x, h, z).eulerXYZ(0, yaw, 0))
                        .shadeDirectionOverride(Direction.UP));
            }
        }
        return b.build();
    }

    // ------------------------------------------------------------------------------------------------ Schlot und Algen

    private static ModelTemplate vent() {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.SIDE).requiredTextureSlot(TextureSlot.TOP).requiredTextureSlot(TextureSlot.BOTTOM)
                .requiredTextureSlot(GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .element(e -> e.from(0, 0, 0).to(16, 16, 16).allFaces((direction, face) -> face.uvs(0, 0, 16, 16)
                        .texture(direction == Direction.UP ? TextureSlot.TOP : direction == Direction.DOWN ? TextureSlot.BOTTOM : TextureSlot.SIDE)
                        .cullface(direction)))
                .element(e -> e.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                        .face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(GLOW).cullface(Direction.UP)))
                .build();
    }

    private static ModelTemplate algae() {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.TEXTURE).requiredTextureSlot(GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .ambientOcclusion(false)
                .element(e -> e.from(0, 0.25f, 0).to(16, 0.25f, 16)
                        .face(Direction.UP, f -> f.uvs(0, 0, 16, 16).texture(TextureSlot.TEXTURE))
                        .face(Direction.DOWN, f -> f.uvs(0, 16, 16, 0).texture(TextureSlot.TEXTURE)))
                .element(e -> e.from(0, 0.25f, 0).to(16, 0.25f, 16).lightEmission(15)
                        .face(Direction.UP, f -> f.uvs(0, 0, 16, 16).texture(GLOW)))
                .build();
    }
}
