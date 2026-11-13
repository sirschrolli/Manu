package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.block.abyss.AbyssRiftBlock;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/** Modelle des Abgrunds: Chaosgestein, Weltennaht, die Kristalle der verdrehten Schwerkraft und der Abgrundriss. */
final class AbyssModels {
    private AbyssModels() {}

    private static final TextureSlot PORTAL = TextureSlot.create("portal");
    private static final ModelTemplate RIFT_NS = riftTemplate(true);
    private static final ModelTemplate RIFT_EW = riftTemplate(false);

    private static ModelTemplate riftTemplate(boolean northSouth) {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(PORTAL)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .suffix(northSouth ? "_ns" : "_ew")
                .element(element -> {
                    if (northSouth) {
                        element.from(0, 0, 6).to(16, 16, 10).lightEmission(15).ambientOcclusion(false)
                                .face(Direction.NORTH, face -> face.uvs(0, 0, 16, 16).texture(PORTAL))
                                .face(Direction.SOUTH, face -> face.uvs(0, 0, 16, 16).texture(PORTAL));
                    } else {
                        element.from(6, 0, 0).to(10, 16, 16).lightEmission(15).ambientOcclusion(false)
                                .face(Direction.EAST, face -> face.uvs(0, 0, 16, 16).texture(PORTAL))
                                .face(Direction.WEST, face -> face.uvs(0, 0, 16, 16).texture(PORTAL));
                    }
                })
                .build();
    }

    /** Das Uhrwerkherz: oben ein Zifferblatt, an den Seiten Uhrwerk, darüber (nur oben) eine Leuchtebene je Phase. */
    private static final TextureSlot HEART_GLOW = TextureSlot.create("glow");
    private static final ModelTemplate CLOCK_HEART = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.TOP)
            .requiredTextureSlot(TextureSlot.SIDE)
            .requiredTextureSlot(TextureSlot.BOTTOM)
            .requiredTextureSlot(HEART_GLOW)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(element -> element.from(0, 0, 0).to(16, 16, 16)
                    .allFaces((direction, face) -> face.uvs(0, 0, 16, 16).texture(direction == Direction.UP ? TextureSlot.TOP
                            : direction == Direction.DOWN ? TextureSlot.BOTTOM : TextureSlot.SIDE).cullface(direction)))
            .element(element -> element.from(0, 0, 0).to(16, 16, 16).lightEmission(15).ambientOcclusion(false)
                    .face(Direction.UP, face -> face.uvs(0, 0, 16, 16).texture(HEART_GLOW).cullface(Direction.UP)))
            .build();

    /** Der Uhrturm (1.1): Uhrwerk, Zifferblatt, das Uhrwerkherz (je Phase), der Minutenzeiger, das Spawn-Ei. */
    private static void clockTower(BlockModelGenerators blocks, ItemModelGenerators items) {
        blocks.createTrivialCube(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCKWORK.get());
        // das Email liegt gedreht (zufällig je Block) – auf dem großen Zifferblatt wiederholt sich kein Muster
        blocks.createRotatedVariantBlock(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCK_FACE.get());
        Block heart = io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCK_HEART.get();
        Material side = TextureMapping.getBlockTexture(heart, "_side");
        TextureMapping base = new TextureMapping().put(TextureSlot.TOP, TextureMapping.getBlockTexture(heart, "_top")).put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(io.github.sirschrolli.kingdomsatwar.registry.ClockTower.CLOCK_BRONZE.base().get()))
                .put(TextureSlot.PARTICLE, side);
        Identifier sleeping = CLOCK_HEART.create(heart, base.copyAndUpdate(HEART_GLOW, TextureMapping.getBlockTexture(heart, "_glow_sleeping")),
                blocks.modelOutput);
        Identifier awake = CLOCK_HEART.createWithSuffix(heart, "_awake", base.copyAndUpdate(HEART_GLOW, TextureMapping.getBlockTexture(heart, "_glow_awake")),
                blocks.modelOutput);
        Identifier defeated = CLOCK_HEART.createWithSuffix(heart, "_defeated", base.copyAndUpdate(TextureSlot.TOP, TextureMapping.getBlockTexture(heart,
                "_top_defeated")).copyAndUpdate(HEART_GLOW, TextureMapping.getBlockTexture(heart, "_glow_defeated")), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(heart).with(PropertyDispatch.initial(
                io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlock.PHASE)
                .select(io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlock.Phase.SLEEPING, BlockModelGenerators.plainVariant(sleeping))
                .select(io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlock.Phase.AWAKE, BlockModelGenerators.plainVariant(awake))
                .select(io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlock.Phase.DEFEATED, BlockModelGenerators.plainVariant(defeated))));
        ModModelProvider.flat(items, io.github.sirschrolli.kingdomsatwar.registry.ClockTower.HOUR_WARDEN_SPAWN_EGG.get());
        MonsterGearModels.weapon(items, io.github.sirschrolli.kingdomsatwar.registry.ClockTower.MINUTE_HAND.get(), "minute_hand",
                ArmoryShapes.MINUTE_HAND, 0.5f, 3.2f);
    }

    static void register(BlockModelGenerators blocks, ItemModelGenerators items) {
        clockTower(blocks, items);
        blocks.createTrivialCube(AbyssBlocks.CRACKED_CHAOS_STONE_BRICKS.get());
        ModModelProvider.glowingCube(blocks, AbyssBlocks.CHISELED_CHAOS_STONE.get());
        ModModelProvider.glowingCube(blocks, AbyssBlocks.WORLD_SEAM.get());
        crystal(blocks, AbyssBlocks.FLOAT_CRYSTAL.get());
        crystal(blocks, AbyssBlocks.LIFT_CRYSTAL.get());
        // der Schlund der Welten: Kronensplitter und Siegel (beide ohne Gegenstand)
        crystalBlock(blocks, AbyssBlocks.SEAL_SHARD.get());
        ModModelProvider.glowingCube(blocks, AbyssBlocks.HOARD_SEAL.get());

        Block rift = AbyssBlocks.ABYSS_RIFT.get();
        Material riftTexture = TextureMapping.getBlockTexture(rift).withForceTranslucent(true);
        TextureMapping riftMapping = new TextureMapping().put(PORTAL, riftTexture).put(TextureSlot.PARTICLE, riftTexture);
        Identifier ns = RIFT_NS.create(rift, riftMapping, blocks.modelOutput);
        Identifier ew = RIFT_EW.create(rift, riftMapping, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(rift).with(PropertyDispatch.initial(AbyssRiftBlock.AXIS)
                .select(Direction.Axis.X, BlockModelGenerators.plainVariant(ns))
                .select(Direction.Axis.Z, BlockModelGenerators.plainVariant(ew))));

        ModModelProvider.flat(items, AbyssBlocks.RIFT_KEY.get());
        // Chronit (seit 1.1): Erz im Chaosgestein, Kristall, Block, die Rüstung der Zeitwächter
        ModModelProvider.ore(blocks, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT_ORE, AbyssBlocks.CHAOS_STONE.base().get(), "chronit");
        ModModelProvider.glowingCube(blocks, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT_BLOCK.get());
        ModModelProvider.flat(items, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.CHRONIT.get());
        ModModelProvider.armor(items, io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_HELMET.get(), io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_CHESTPLATE.get(), io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_LEGGINGS.get(),
                io.github.sirschrolli.kingdomsatwar.registry.AbyssGear.TIMEWARDEN_BOOTS.get());
        ModModelProvider.flat(items, AbyssBlocks.WORLD_EATER_SPAWN_EGG.get());

        // der Schlund der Welten: der Weltenspeer und die Trophäe
        worldSpear(items);
        trophy(blocks);
    }

    // =====================================================================================
    // Der Weltenspeer
    // =====================================================================================

    private static final TextureSlot SPEAR = TextureSlot.create("spear");
    private static final TextureSlot SPEAR_GLOW = TextureSlot.create("spear_glow");

    /**
     * Teile des Weltenspeers längs der Y-Achse (Mitte x = z = 8): {von x, y, z, bis x, y, z, Bereich der Textur u0, v0, u1,
     * v1, Leuchten (0 nein, 1 ja)}.
     */
    private static final float[][] SPEAR_PARTS = {
            {7.55f, -5.6f, 7.55f, 8.45f, -4.7f, 8.45f, 4, 8, 5, 9, 1},             // Kronensplitter am Knauf
            {7.2f, -4.7f, 7.2f, 8.8f, -3.4f, 8.8f, 4, 0, 5.6f, 1.3f, 0},          // goldener Knauf
            {7.4f, -3.4f, 7.4f, 8.6f, 1.0f, 8.6f, 2, 0, 3.2f, 4.4f, 0},           // Griff mit Goldbändern
            {6.7f, 1.0f, 6.7f, 9.3f, 2.0f, 9.3f, 4, 2, 6, 3, 0},                  // Kern des Kreuzes
            {4.8f, 0.7f, 7.25f, 6.7f, 2.3f, 8.75f, 12, 0, 14, 1.6f, 0},           // das Kreuz: Wiese,
            {9.3f, 0.7f, 7.25f, 11.2f, 2.3f, 8.75f, 14, 0, 16, 1.6f, 0},          // Netherrack,
            {7.25f, 0.7f, 4.8f, 8.75f, 2.3f, 6.7f, 12, 4, 14, 5.6f, 0},           // Endstein,
            {7.25f, 0.7f, 9.3f, 8.75f, 2.3f, 11.2f, 14, 4, 16, 5.6f, 0},          // Eis
            {7.5f, 2.0f, 7.5f, 8.5f, 15.6f, 8.5f, 0, 0, 1, 13.6f, 0},             // Schaft aus Chaosgestein
            {7.43f, 4.6f, 7.43f, 8.57f, 5.0f, 8.57f, 12, 8, 13.2f, 8.4f, 1},      // glühende Nahtringe
            {7.43f, 8.2f, 7.43f, 8.57f, 8.6f, 8.57f, 12, 9, 13.2f, 9.4f, 1},
            {7.43f, 11.8f, 7.43f, 8.57f, 12.2f, 8.57f, 12, 10, 13.2f, 10.4f, 1},
            {7.15f, 15.6f, 7.15f, 8.85f, 16.6f, 8.85f, 4, 4, 5.7f, 5, 0},         // Goldkragen
            {6.2f, 16.0f, 7.7f, 7.15f, 17.7f, 8.3f, 4, 9, 5, 10.7f, 1},           // vier Kronenzacken
            {8.85f, 16.0f, 7.7f, 9.8f, 17.7f, 8.3f, 4, 9, 5, 10.7f, 1},
            {7.7f, 16.0f, 6.2f, 8.3f, 17.7f, 7.15f, 5, 9, 6, 10.7f, 1},
            {7.7f, 16.0f, 8.85f, 8.3f, 17.7f, 9.8f, 5, 9, 6, 10.7f, 1},
            {6.7f, 16.6f, 7.55f, 9.3f, 18.6f, 8.45f, 6, 10, 10, 12, 0},           // der Fangzahn in fünf Stufen
            {6.95f, 18.6f, 7.6f, 9.05f, 20.8f, 8.4f, 6, 7.5f, 10, 10, 0},
            {7.25f, 20.8f, 7.65f, 8.75f, 22.6f, 8.35f, 6, 5, 10, 7.5f, 0},
            {7.5f, 22.6f, 7.7f, 8.5f, 24.0f, 8.3f, 6, 2.5f, 10, 5, 0},
            {7.75f, 24.0f, 7.8f, 8.25f, 25.0f, 8.2f, 6, 0, 10, 2.5f, 0},
            {7.8f, 16.9f, 7.5f, 8.2f, 23.4f, 8.5f, 10, 0, 12, 12, 1},             // die weißglühende Naht
    };

    private static void worldSpear(ItemModelGenerators items) {
        net.minecraft.world.item.Item spear = AbyssBlocks.WORLD_SPEAR.get();
        TextureMapping textures = new TextureMapping()
                .put(SPEAR, new Material(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("item/world_spear_model")))
                .put(SPEAR_GLOW, new Material(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("item/world_spear_model_glow")))
                .put(TextureSlot.PARTICLE, new Material(io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.id("item/world_spear")));
        // gehalten wie die Speere von Minecraft: auf die Mitte gesetzt und auf ihre Länge gebracht (SpearHold)
        ExtendedModelTemplateBuilder hand = SpearHold.hold(ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(SPEAR).requiredTextureSlot(SPEAR_GLOW).requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT), SPEAR_PARTS);
        float lift = SpearHold.lift(SPEAR_PARTS);
        for (float[] p : SPEAR_PARTS) {
            boolean glow = p[10] > 0;
            hand.element(e -> {
                e.from(p[0], p[1] + lift, p[2]).to(p[3], p[4] + lift, p[5]);
                e.allFaces((direction, face) -> {
                    face.texture(glow ? SPEAR_GLOW : SPEAR);
                    if (direction.getAxis() == Direction.Axis.Y) {
                        face.uvs(p[6], p[7], Math.min(p[8], p[6] + 1.5f), Math.min(p[9], p[7] + 1.5f));
                    } else {
                        face.uvs(p[6], p[7], p[8], p[9]);
                    }
                });
                if (glow) {
                    e.lightEmission(15);
                }
                e.rotation(r -> r.origin(8, 8, 8).eulerXYZ(0, 0, 45));
            });
        }
        Identifier inHand = hand.build().create(net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation(spear, "_in_hand"),
                textures, items.modelOutput);
        var flat = net.minecraft.client.data.models.model.ItemModelUtils.plainModel(items.createFlatItemModel(spear,
                net.minecraft.client.data.models.model.ModelTemplates.FLAT_ITEM));
        items.itemModelOutput.accept(spear, ItemModelGenerators.createFlatModelDispatch(flat,
                net.minecraft.client.data.models.model.ItemModelUtils.plainModel(inHand)),
                new net.minecraft.client.renderer.item.ClientItem.Properties(true, false, 1.95f));
    }

    // =====================================================================================
    // Kopf des Weltenfressers (Trophäe)
    // =====================================================================================

    private static final TextureSlot BASE = TextureSlot.create("base");
    private static final TextureSlot GOLD = TextureSlot.create("gold");
    private static final TextureSlot EYES = TextureSlot.create("eyes");
    private static final TextureSlot EYES_GLOW = TextureSlot.create("eyes_glow");

    /** Sockel: {von x, y, z, bis x, y, z, Werkstoff (0 Chaosgestein, 1 Gold, 2 gemeißelte Flanken, 3 deren Leuchten)}. */
    private static final float[][] TROPHY_PARTS = {
            {1, 0, 1, 15, 2, 15, 0},
            {1.5f, 2, 1.5f, 14.5f, 3, 14.5f, 1},
            {2.5f, 3, 2.5f, 13.5f, 5.5f, 13.5f, 2},
            {2.49f, 3, 2.49f, 13.51f, 5.5f, 13.51f, 3},
            {2, 5.5f, 2, 14, 6, 14, 1},
    };

    private static void trophy(BlockModelGenerators blocks) {
        Block trophy = AbyssBlocks.WORLD_EATER_TROPHY.get();
        String mod = io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.MODID;
        TextureMapping textures = new TextureMapping()
                .put(BASE, new Material(Identifier.fromNamespaceAndPath(mod, "block/polished_chaos_stone")))
                .put(GOLD, new Material(Identifier.withDefaultNamespace("block/gold_block")))
                .put(EYES, new Material(Identifier.fromNamespaceAndPath(mod, "block/chiseled_chaos_stone")))
                .put(EYES_GLOW, new Material(Identifier.fromNamespaceAndPath(mod, "block/chiseled_chaos_stone_glow")))
                .put(TextureSlot.PARTICLE, new Material(Identifier.fromNamespaceAndPath(mod, "block/polished_chaos_stone")));
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(BASE).requiredTextureSlot(GOLD).requiredTextureSlot(EYES).requiredTextureSlot(EYES_GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE);
        for (float[] p : TROPHY_PARTS) {
            int kind = (int) p[6];
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                for (Direction side : Direction.values()) {
                    if (kind == 3 && side.getAxis() == Direction.Axis.Y) {
                        continue;
                    }
                    e.face(side, f -> {
                        switch (side.getAxis()) {
                            case Y -> f.uvs(p[0], p[2], p[3], p[5]);
                            case Z -> f.uvs(p[0], 16 - p[4], p[3], 16 - p[1]);
                            default -> f.uvs(p[2], 16 - p[4], p[5], 16 - p[1]);
                        }
                        TextureSlot slot = switch (kind) {
                            case 1 -> GOLD;
                            case 2 -> side.getAxis() == Direction.Axis.Y ? BASE : EYES;
                            case 3 -> EYES_GLOW;
                            default -> BASE;
                        };
                        f.texture(slot);
                        if (side == Direction.DOWN && p[1] == 0) {
                            f.cullface(Direction.DOWN);
                        }
                    });
                }
                if (kind == 3) {
                    e.lightEmission(15).ambientOcclusion(false);
                }
            });
        }
        Identifier model = b.build().create(trophy, textures, blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(trophy, BlockModelGenerators.plainVariant(model)));
        blocks.itemModelOutput.accept(trophy.asItem(), net.minecraft.client.data.models.model.ItemModelUtils.composite(
                net.minecraft.client.data.models.model.ItemModelUtils.plainModel(model),
                net.minecraft.client.data.models.model.ItemModelUtils.specialModel(model,
                        new io.github.sirschrolli.kingdomsatwar.client.renderer.abyss.WorldEaterTrophyRenderer.ItemRenderer.Unbaked())));
    }

    /** Eine Kristalldruse mit Leuchtebene, nach ihrer Wuchsrichtung gedreht (wie eine Amethystdruse). */
    private static void crystal(BlockModelGenerators blocks, Block block) {
        crystalBlock(blocks, block);
        blocks.registerSimpleFlatItemModel(block);
    }

    /** Nur Blockmodell und -zustand einer Druse (für Blöcke ohne Gegenstand). */
    private static void crystalBlock(BlockModelGenerators blocks, Block block) {
        BlockModelGenerators.PlantType type = BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED;
        Identifier model = type.getCross().create(block, type.getTextureMapping(block), blocks.modelOutput);
        blocks.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));
    }
}
