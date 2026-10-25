package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.army.RecruitScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.client.intro.IntroController;
import io.github.sirschrolli.kingdomsatwar.client.intro.IntroScreen;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModCreativeTabs;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Szenen für die automatischen Screenshots (siehe {@link ShowcaseRunner}).
 * Die Welt ist flach; die Bodenhöhe wird zu Beginn gemessen ({@link #ground}).
 */
final class Showcase {
    /** Erste freie Höhe über dem Boden. */
    private static volatile int ground = Integer.MIN_VALUE;

    private Showcase() {}

    static void queue(String scenes, Deque<Step> steps) {
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility()
                .set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.server(level -> ground = level.getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0)));
        steps.add(Step.until(() -> ground != Integer.MIN_VALUE, 200));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("gamerule advance_weather false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.waitTicks(20));
        for (String scene : scenes.split(",")) {
            switch (scene.trim()) {
                case "blocks" -> blocks(steps);
                case "house" -> house(steps);
                case "samples" -> samples(steps);
                case "intro" -> intro(steps);
                case "items" -> items(steps);
                case "dragons" -> dragons(steps);
                case "soldiers" -> soldiers(steps);
                case "eggs" -> eggs(steps);
                case "equipment" -> equipment(steps);
                case "creatures" -> creatures(steps);
                case "decor" -> decor(steps);
                case "farm" -> farm(steps);
                case "magic" -> magic(steps);
                case "structures" -> structures(steps);
                default -> { }
            }
        }
        steps.add(Step.waitTicks(40));
        steps.add(Step.quit());
    }

    // =====================================================================================
    // Szenen
    // =====================================================================================

    /** Alle Blöcke der Mod in Reihen auf einer Steinplatte. */
    private static void blocks(Deque<Step> steps) {
        steps.add(Step.server(level -> {
            List<Block> blocks = new ArrayList<>();
            for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
                Block block = holder.value();
                if (!(block instanceof FlowerPotBlock) || block == Blocks.FLOWER_POT) {
                    blocks.add(block);
                }
            }
            int perRow = 16;
            int rows = (blocks.size() + perRow - 1) / perRow;
            fill(level, new BlockPos(-2, ground - 1, -2), new BlockPos(perRow * 2, ground - 1, rows * 3 + 1), Blocks.SMOOTH_STONE.defaultBlockState());
            for (int i = 0; i < blocks.size(); i++) {
                BlockPos pos = new BlockPos((i % perRow) * 2, ground, (i / perRow) * 3);
                place(level, pos, blocks.get(i));
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(tp(15, 12, -9, 0, 42));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(false));
        steps.add(Step.screenshot("blocks_overview"));
        // Nahaufnahmen der ersten und zweiten Hälfte
        steps.add(tp(7, 6, -4, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_1"));
        steps.add(tp(23, 6, -4, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_2"));
        steps.add(tp(7, 6, 8, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_3"));
        steps.add(tp(23, 6, 8, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_4"));
        steps.add(Step.hud(true));
    }

    /** Ein kleines Fachwerkhaus aus Mod-Blöcken – zeigt, wie die Blöcke zusammenwirken. */
    private static void house(Deque<Step> steps) {
        int ox = 60;
        int oz = 0;
        int w = 9;
        int d = 7;
        steps.add(Step.server(level -> {
            BlockState stone = ModBlocks.MEDIEVAL_STONE.base().get().defaultBlockState();
            BlockState beam = ModBlocks.TIMBER_BEAM.get().defaultBlockState();
            BlockState frame = ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            BlockState cross = ModBlocks.TIMBER_FRAME_CROSS.get().defaultBlockState();
            BlockState diagonal = ModBlocks.TIMBER_FRAME_DIAGONAL.get().defaultBlockState();
            BlockState floor = ModBlocks.ROUGH_FIELDSTONE.base().get().defaultBlockState();
            fill(level, new BlockPos(ox - 4, ground - 1, oz - 4), new BlockPos(ox + w + 4, ground - 1, oz + d + 4), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(ox - 1, ground - 1, oz - 1), new BlockPos(ox + w, ground - 1, oz + d), floor);
            // Erdgeschoss aus Stein, Obergeschoss in Fachwerk
            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < w; x++) {
                    for (int z = 0; z < d; z++) {
                        boolean edgeX = x == 0 || x == w - 1;
                        boolean edgeZ = z == 0 || z == d - 1;
                        if (!edgeX && !edgeZ) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(ox + x, ground + y, oz + z);
                        BlockState state;
                        if (y < 3) {
                            state = stone;
                        } else if (edgeX && edgeZ) {
                            state = beam; // Eckpfosten (senkrecht)
                        } else if (y == 3 || y == 6) {
                            // Schwelle und Rähm: liegende Balken entlang der Wand
                            state = beam.setValue(RotatedPillarBlock.AXIS, edgeZ ? Direction.Axis.X : Direction.Axis.Z);
                        } else {
                            int k = (x + z + y) % 3;
                            state = k == 0 ? cross : (k == 1 ? diagonal : frame);
                        }
                        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                    }
                }
            }
            // Tür und Fenster
            level.setBlock(new BlockPos(ox + 4, ground, oz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(ox + 4, ground + 1, oz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(ox + 2, ground + 4, oz), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(ox + 6, ground + 4, oz), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_CLIENTS);
            // Satteldach aus roten Dachziegeln (First entlang x)
            BlockState south = ModBlocks.RED_ROOF_TILES.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState north = ModBlocks.RED_ROOF_TILES.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            int roofBase = ground + 7;
            for (int step = 0; step <= d / 2; step++) {
                for (int x = -1; x <= w; x++) {
                    BlockPos front = new BlockPos(ox + x, roofBase + step, oz - 1 + step);
                    BlockPos back = new BlockPos(ox + x, roofBase + step, oz + d - step);
                    if (front.getZ() < back.getZ()) {
                        level.setBlock(front, south, Block.UPDATE_CLIENTS);
                        level.setBlock(back, north, Block.UPDATE_CLIENTS);
                    } else {
                        level.setBlock(front, ModBlocks.RED_ROOF_TILES.slab().get().defaultBlockState()
                                .setValue(SlabBlock.TYPE, SlabType.BOTTOM), Block.UPDATE_CLIENTS);
                    }
                }
                // Giebel mit Fachwerk füllen
                for (int z = step; z < d - step; z++) {
                    level.setBlock(new BlockPos(ox, roofBase + step, oz + z), frame, Block.UPDATE_CLIENTS);
                    level.setBlock(new BlockPos(ox + w - 1, roofBase + step, oz + z), frame, Block.UPDATE_CLIENTS);
                }
            }
            // Ein paar Blumen und Feldfrüchte vor dem Haus
            int i = 0;
            for (var flower : ModBlocks.FLOWERS.keySet()) {
                level.setBlock(new BlockPos(ox + 1 + i * 2, ground, oz - 3), flower.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                i++;
            }
            for (int c = 0; c < ModBlocks.CROPS.size(); c++) {
                CropBlock crop = ModBlocks.CROPS.get(c).get();
                for (int z = 0; z < 3; z++) {
                    BlockPos soil = new BlockPos(ox + w + 2 + c, ground - 1, oz + 1 + z);
                    level.setBlock(soil, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(soil.above(), crop.getStateForAge(crop.getMaxAge()), Block.UPDATE_CLIENTS);
                }
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(tp(ox - 7, 6, oz - 12, -30, 18));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(false));
        steps.add(Step.screenshot("house_front"));
        steps.add(tp(ox + w + 9, 8, oz + d + 9, 135, 22));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("house_back"));
        steps.add(Step.hud(true));
    }

    /** Das Erzähl-Intro mit Screenshots zu festen Zeitpunkten (am besten zusammen mit "terrain"). */
    private static void intro(Deque<Step> steps) {
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.once(minecraft -> IntroController.request()));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof IntroScreen, 200));
        double[] times = {3.5, 10.0, 17.0, 23.0, 29.0, 35.0, 42.0, 48.0, 54.5, 58.0, 63.0, 70.5};
        for (int i = 0; i < times.length; i++) {
            double at = times[i];
            steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof IntroScreen intro && intro.elapsed() >= at, 2400));
            steps.add(Step.screenshot("intro_%02d".formatted(i + 1)));
        }
        steps.add(Step.until(() -> !(Minecraft.getInstance().gui.screen() instanceof IntroScreen), 400));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("intro_after"));
    }

    /** Mauerproben (3x3 Blöcke) aus der Nähe – zum Vergleichen der Texturen. Kamera schaut nach Norden. */
    private static void samples(Deque<Step> steps) {
        int ox = 120;
        int oz = 0;
        // erst hinfliegen, damit alle Chunks geladen sind, bevor gebaut und fotografiert wird
        steps.add(tp(ox + 60.5, 30.0, oz + 9.0, 180, 30));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.waitTicks(60));
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(ox - 4, ground - 1, oz - 6), new BlockPos(ox + 140, ground - 1, oz + 12), Blocks.GRASS_BLOCK.defaultBlockState());
            // 1) acht Stein-Sets in zwei Gruppen
            List<BuildingSet> stones = List.of(ModBlocks.MEDIEVAL_STONE, ModBlocks.ROUGH_FIELDSTONE, ModBlocks.HEWN_SANDSTONE,
                    ModBlocks.DARK_STONE_BRICKS, ModBlocks.LIMESTONE_BRICKS, ModBlocks.WEATHERED_STONE_BRICKS,
                    ModBlocks.MOSSY_WALL_STONES, ModBlocks.CRACKED_STONE_BLOCKS);
            for (int i = 0; i < stones.size(); i++) {
                int gx = ox + (i / 4) * 20 + (i % 4) * 4;
                wall(level, gx, oz, uniform(stones.get(i).base().get().defaultBlockState()));
            }
            // 2) Holz: Fachwerk, Balken, Säule
            BlockState frame = ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            BlockState cross = ModBlocks.TIMBER_FRAME_CROSS.get().defaultBlockState();
            BlockState diagonal = ModBlocks.TIMBER_FRAME_DIAGONAL.get().defaultBlockState();
            int wx = ox + 40;
            wall(level, wx, oz, new BlockState[][] {{diagonal, frame, diagonal}, {cross, frame, cross}, {frame, cross, frame}});
            wall(level, wx + 4, oz, uniform(ModBlocks.TIMBER_BEAM.get().defaultBlockState()));
            for (int y = 0; y < 3; y++) {
                level.setBlock(new BlockPos(wx + 9, ground + y, oz), ModBlocks.STONE_PILLAR.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(wx + 11, ground + y, oz), ModBlocks.STONE_PILLAR.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            // 3) sechs kleine Dächer (fallen zur Kamera hin ab)
            List<BuildingSet> roofs = List.of(ModBlocks.RED_ROOF_TILES, ModBlocks.BROWN_ROOF_TILES, ModBlocks.LIGHT_ROOF_TILES,
                    ModBlocks.DARK_SLATE_SHINGLES, ModBlocks.WOODEN_SHINGLES, ModBlocks.COPPER_ROOF);
            for (int i = 0; i < roofs.size(); i++) {
                int rx = ox + 60 + (i / 3) * 20 + (i % 3) * 4;
                BlockState stairs = roofs.get(i).stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
                BlockState full = roofs.get(i).base().get().defaultBlockState();
                for (int step = 0; step < 3; step++) {
                    for (int x = 0; x < 3; x++) {
                        level.setBlock(new BlockPos(rx + x, ground + step, oz + 2 - step), stairs, Block.UPDATE_CLIENTS);
                        for (int below = 0; below < step; below++) {
                            level.setBlock(new BlockPos(rx + x, ground + below, oz + 2 - step), full, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            // 4) Erze (oben Stein, Mitte Tiefenschiefer) und Rohstoffblöcke (unten)
            int ex = ox + 100;
            List<Block> stoneOres = List.of(ModBlocks.CARBON_ORE.get(), ModBlocks.GEMSTONE_ORE.get(), ModBlocks.KONTERIT_ORE.get(),
                    ModBlocks.AETHERIT_ORE.get(), ModBlocks.DRACONIUM_ORE.get(), ModBlocks.WYVERNIT_ORE.get(), Blocks.STONE);
            List<Block> deepOres = List.of(ModBlocks.DEEPSLATE_CARBON_ORE.get(), ModBlocks.DEEPSLATE_GEMSTONE_ORE.get(),
                    ModBlocks.DEEPSLATE_KONTERIT_ORE.get(), ModBlocks.DEEPSLATE_AETHERIT_ORE.get(), ModBlocks.DEEPSLATE_DRACONIUM_ORE.get(),
                    Blocks.DEEPSLATE, Blocks.DEEPSLATE);
            List<Block> storage = List.of(ModBlocks.CARBON_BLOCK.get(), ModBlocks.GEMSTONE_BLOCK.get(), ModBlocks.KONTERIT_BLOCK.get(),
                    ModBlocks.AETHERIT_BLOCK.get(), ModBlocks.DRACONIUM_BLOCK.get(), ModBlocks.WYVERNIT_BLOCK.get(), ModBlocks.STEEL_BLOCK.get());
            for (int i = 0; i < 7; i++) {
                level.setBlock(new BlockPos(ex + i, ground + 2, oz), stoneOres.get(i).defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(ex + i, ground + 1, oz), deepOres.get(i).defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(ex + i, ground, oz), storage.get(i).defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            // 5) Dracheneier: alle Arten, dahinter ein Feuerei in jeder Brutstufe
            int gx = ox + 120;
            int k = 0;
            for (var egg : ModBlocks.DRAGON_EGGS.values()) {
                level.setBlock(new BlockPos(gx + k, ground, oz + 2), egg.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                k++;
            }
            var fire = ModBlocks.DRAGON_EGGS.get(DragonType.FIRE).get();
            for (int h = 0; h <= 3; h++) {
                level.setBlock(new BlockPos(gx + 1 + h, ground, oz), fire.defaultBlockState().setValue(
                        io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock.HATCH, h), Block.UPDATE_CLIENTS);
            }
        }));
        steps.add(Step.waitTicks(40));
        String[] names = {"samples_stone_1", "samples_stone_2", "samples_wood", "samples_roof_1", "samples_roof_2", "samples_ores", "samples_eggs"};
        double[][] cams = { // x, dy, z, yaw, pitch
            {ox + 7.5, 1.6, oz + 9.0, 180, 4}, {ox + 27.5, 1.6, oz + 9.0, 180, 4}, {ox + 46.5, 1.6, oz + 9.0, 180, 4},
            {ox + 65.5, 4.2, oz + 9.5, 180, 22}, {ox + 85.5, 4.2, oz + 9.5, 180, 22},
            {ox + 103.5, 1.6, oz + 6.5, 180, 4}, {ox + 123.5, 1.4, oz + 4.6, 180, 25},
        };
        for (int i = 0; i < names.length; i++) {
            steps.add(tp(cams[i][0], cams[i][1], cams[i][2], (float) cams[i][3], (float) cams[i][4]));
            steps.add(Step.waitForChunks(200));
            steps.add(Step.waitTicks(i == 0 ? 40 : 25));
            steps.add(Step.hud(false));
            steps.add(Step.screenshot(names[i]));
        }
        steps.add(Step.hud(true));
    }

    private static void wall(ServerLevel level, int x0, int z, BlockState[][] wall) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                level.setBlock(new BlockPos(x0 + x, ground + 2 - y, z), wall[y][x], Block.UPDATE_CLIENTS);
            }
        }
    }

    private static BlockState[][] uniform(BlockState state) {
        return new BlockState[][] {{state, state, state}, {state, state, state}, {state, state, state}};
    }

    /** Alle Drachenarten, alle Wachstumsstufen, Flug-, Sitz- und Odem-Haltung. */
    private static void dragons(Deque<Step> steps) {
        int oz = 40;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 6), new BlockPos(46, ground - 1, oz + 40), Blocks.GRASS_BLOCK.defaultBlockState());
            DragonType[] types = DragonType.values();
            // Reihe 1: sechs ausgewachsene Drachen, seitlich (Blick nach Osten)
            for (int i = 0; i < types.length; i++) {
                spawnDragon(level, types[i], DragonStage.ADULT, new BlockPos(i * 7, ground, oz), -90, d -> { });
            }
            // Reihe 2: alle Wachstumsstufen des Feuerdrachen
            DragonStage[] stages = DragonStage.values();
            for (int i = 0; i < stages.length; i++) {
                spawnDragon(level, DragonType.FIRE, stages[i], new BlockPos(i * 7, ground, oz + 12), -90, d -> { });
            }
            // Reihe 3: fliegend, sitzend, Odem
            spawnDragon(level, DragonType.ICE, DragonStage.ADULT, new BlockPos(0, ground + 3, oz + 24), -90, d -> d.setFlying(true));
            spawnDragon(level, DragonType.NATURE, DragonStage.ADULT, new BlockPos(9, ground, oz + 24), -90,
                    d -> { d.setOrderedToSit(true); d.setInSittingPose(true); });
            spawnDragon(level, DragonType.FIRE, DragonStage.ELDER, new BlockPos(18, ground, oz + 24), -90, d -> d.startBreath(null));
            spawnDragon(level, DragonType.LIGHTNING, DragonStage.ADULT, new BlockPos(27, ground + 3, oz + 24), 180, d -> d.setFlying(true));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(tp(17, 7, oz - 14, 0, 20));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(20));
        steps.add(Step.hud(false));
        steps.add(Step.screenshot("dragons_adults"));
        steps.add(tp(14, 5, oz + 3, 0, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("dragons_stages"));
        steps.add(tp(14, 7, oz + 13, 0, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> DragonEntity.nearby(level, new net.minecraft.world.phys.Vec3(18, ground, oz + 24), 4)
                .forEach(d -> d.startBreath(null))));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("dragons_poses"));
        steps.add(Step.waitTicks(7));
        steps.add(Step.screenshot("dragons_poses_2"));
        // Nahaufnahme Kopf
        steps.add(tp(3, 4, oz - 5, -20, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("dragon_closeup"));
        steps.add(Step.hud(true));
    }

    /** Rüstungen auf Rüstungsständern (vorne und hinten), Drachen mit Panzer und Waffen in der Hand. */
    private static void equipment(Deque<Step> steps) {
        int oz = 140;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 30), new BlockPos(40, ground - 1, oz + 12), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-6, ground, oz - 30), new BlockPos(40, ground + 8, oz + 12), Blocks.AIR.defaultBlockState());
            List<List<Item>> sets = new ArrayList<>();
            sets.add(List.of(ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(), ModItems.STEEL_LEGGINGS.get(),
                    ModItems.STEEL_BOOTS.get(), ModItems.STEEL_SWORD.get()));
            sets.add(List.of(ModItems.KONTERIT_HELMET.get(), ModItems.KONTERIT_CHESTPLATE.get(), ModItems.KONTERIT_LEGGINGS.get(),
                    ModItems.KONTERIT_BOOTS.get(), ModItems.STEEL_PICKAXE.get()));
            for (DragonType type : DragonType.values()) {
                var set = ModItems.DRAGON_SCALE_ARMOR.get(type);
                sets.add(List.of(set.get(0).get(), set.get(1).get(), set.get(2).get(), set.get(3).get(),
                        type == DragonType.FIRE ? ModItems.DRACONIUM_SWORD.get() : type == DragonType.EARTH
                                ? ModItems.DRACONIUM_AXE.get() : Items.AIR));
            }
            sets.add(List.of(ModItems.GEMSTONE_CROWN.get(), ModItems.WYVERNIT_GLIDER.get(), Items.AIR, Items.AIR, ModItems.STEEL_AXE.get()));
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
            for (int i = 0; i < sets.size(); i++) {
                ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (stand == null) {
                    continue;
                }
                stand.setShowArms(true);
                stand.setNoBasePlate(true);
                stand.setRightArmPose(new Rotations(-20, 0, 10));
                for (int j = 0; j < slots.length; j++) {
                    stand.setItemSlot(slots[j], new ItemStack(sets.get(i).get(j)));
                }
                stand.snapTo(i * 2 + 0.5, ground, oz + 0.5, 0, 0);
                stand.setYBodyRot(0);
                level.addFreshEntity(stand);
            }
            // Drachen mit Panzer (Stahl und Draconium)
            spawnDragon(level, DragonType.EARTH, DragonStage.ADULT, new BlockPos(4, ground, oz - 14), -90, d -> {
                d.setTame(true, false);
                d.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.STEEL_DRAGON_ARMOR.get()));
            });
            spawnDragon(level, DragonType.FIRE, DragonStage.ADULT, new BlockPos(16, ground, oz - 14), -90, d -> {
                d.setTame(true, false);
                d.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.DRACONIUM_DRAGON_ARMOR.get()));
            });
            spawnDragon(level, DragonType.ICE, DragonStage.ELDER, new BlockPos(28, ground + 3, oz - 14), -90, d -> {
                d.setTame(true, false);
                d.setFlying(true);
                d.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.STEEL_DRAGON_ARMOR.get()));
            });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(8.5, 1.4, oz + 5.5, 180, 8));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("equipment_front"));
        steps.add(tp(4.0, 1.5, oz + 3.2, 180, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_front_close"));
        steps.add(tp(13.0, 1.5, oz + 3.2, 180, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_front_close_2"));
        steps.add(tp(8.5, 1.6, oz - 4.5, 0, 8));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_back"));
        steps.add(tp(16.5, 6, oz - 1, 180, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_dragons"));
        steps.add(tp(10.5, 3.5, oz - 6.5, 160, 15));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_dragon_close"));
        steps.add(Step.hud(true));
    }

    /** Die wilden Kreaturen: alle Arten und Varianten, von vorne, von der Seite und das Irrlicht bei Nacht. */
    private static void creatures(Deque<Step> steps) {
        int oz = 200;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 20), new BlockPos(40, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-6, ground, oz - 20), new BlockPos(40, ground + 8, oz + 14), Blocks.AIR.defaultBlockState());
            int x = 0;
            spawnCreature(level, ModEntities.WILD_BOAR.get(), x, oz, 0, m -> { });
            spawnCreature(level, ModEntities.WILD_BOAR.get(), x + 2, oz, 0, m -> m.setBaby(true));
            spawnCreature(level, ModEntities.DEER.get(), x + 5, oz, 0, m -> m.setStag(true));
            spawnCreature(level, ModEntities.DEER.get(), x + 7, oz, 0, m -> m.setStag(false));
            spawnCreature(level, ModEntities.DEER.get(), x + 9, oz, 0, m -> m.setBaby(true));
            spawnCreature(level, ModEntities.GOOSE.get(), x + 11, oz, 0, m -> m.setVariant(0));
            spawnCreature(level, ModEntities.GOOSE.get(), x + 12, oz, 0, m -> m.setVariant(1));
            spawnCreature(level, ModEntities.GOOSE.get(), x + 13, oz, 0, m -> m.setBaby(true));
            spawnCreature(level, ModEntities.WILL_O_WISP.get(), x + 15, oz, 0, m -> {
                m.setNoGravity(true);
                m.setPos(m.getX(), m.getY() + 1.2, m.getZ());
            });
            spawnCreature(level, ModEntities.CAVE_TROLL.get(), x + 18, oz, 0, m -> { });
            spawnCreature(level, ModEntities.LINDWORM.get(), x + 24, oz, -90, m -> { });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(13.0, 2.2, oz + 10.5, 180, 8));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("creatures_overview"));
        steps.add(tp(3.5, 1.3, oz + 4.2, 200, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_boars"));
        steps.add(tp(8.0, 1.6, oz + 4.6, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_animals"));
        steps.add(tp(1.5, 1.6, oz - 3.0, -50, 8));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_animals_side"));
        steps.add(tp(14.5, 1.0, oz + 3.2, 180, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_geese_wisp"));
        steps.add(tp(21.5, 2.2, oz + 6.5, 160, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_troll_lindworm"));
        steps.add(tp(11.0, 1.5, oz - 5.5, -30, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_side"));
        steps.add(Step.command("time set 18000"));
        steps.add(tp(15.5, 1.8, oz + 4.0, 180, 6));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("creatures_wisp_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
    }

    /**
     * Ein Thronsaal aus den Einrichtungs-Blöcken: Putzwände mit Bleiglasfenstern und Fensterläden, Burgtür,
     * Zinnen, Kronleuchter und Laternen, Tafeln mit Stühlen und Bänken, ein Ritter auf dem Thron.
     */
    private static void decor(Deque<Step> steps) {
        int oz = 260;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-12, ground - 1, oz - 8), new BlockPos(22, ground - 1, oz + 18), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-12, ground, oz - 8), new BlockPos(22, ground + 9, oz + 18), Blocks.AIR.defaultBlockState());
            fill(level, new BlockPos(0, ground - 1, oz), new BlockPos(10, ground - 1, oz + 7), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            BlockState plaster = ModBlocks.WHITE_PLASTER.base().get().defaultBlockState();
            BlockState clay = ModBlocks.CLAY_PLASTER.base().get().defaultBlockState();
            BlockState limestone = ModBlocks.LIMESTONE_BRICKS.base().get().defaultBlockState();
            BlockState beam = ModBlocks.TIMBER_BEAM.get().defaultBlockState();
            BlockState glass = ModBlocks.LEADED_GLASS.get().defaultBlockState();
            // Rückwand (Kalkputz, zwei Fenster), Seitenwände (Lehmputz links, Kalkstein rechts), Ecken aus Balken
            fill(level, new BlockPos(1, ground, oz), new BlockPos(9, ground + 4, oz), plaster);
            fill(level, new BlockPos(0, ground, oz + 1), new BlockPos(0, ground + 4, oz + 6), clay);
            fill(level, new BlockPos(10, ground, oz + 1), new BlockPos(10, ground + 4, oz + 6), limestone);
            for (int[] corner : new int[][] {{0, oz}, {10, oz}, {0, oz + 7}, {10, oz + 7}}) {
                fill(level, new BlockPos(corner[0], ground, corner[1]), new BlockPos(corner[0], ground + 4, corner[1]), beam);
            }
            fill(level, new BlockPos(2, ground + 1, oz), new BlockPos(3, ground + 2, oz), glass);
            fill(level, new BlockPos(7, ground + 1, oz), new BlockPos(8, ground + 2, oz), glass);
            // Fensterläden innen neben den Fenstern (geöffnet, flach an der Wand)
            for (int x : new int[] {1, 4, 6, 9}) {
                Block shutters = x < 5 ? ModBlocks.GREEN_SHUTTERS.get() : ModBlocks.RED_SHUTTERS.get();
                for (int y = 1; y <= 2; y++) {
                    level.setBlock(new BlockPos(x, ground + y, oz + 1), shutters.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.SOUTH)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF, y == 1 ? Half.BOTTOM : Half.TOP), Block.UPDATE_CLIENTS);
                }
            }
            // Fenster mit Bleiglasscheiben in der linken Wand
            for (int z = oz + 3; z <= oz + 4; z++) {
                for (int y = 1; y <= 2; y++) {
                    BlockPos pos = new BlockPos(0, ground + y, z);
                    level.setBlock(pos, ModBlocks.LEADED_GLASS_PANE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            // Burgtür in der rechten Wand
            BlockState door = ModBlocks.CASTLE_DOOR.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.WEST);
            level.setBlock(new BlockPos(10, ground, oz + 3), door, Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(10, ground + 1, oz + 3), door.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
            // Decke und Zinnen obenauf
            fill(level, new BlockPos(0, ground + 5, oz), new BlockPos(10, ground + 5, oz + 7), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            for (int x = 0; x <= 10; x++) {
                level.setBlock(new BlockPos(x, ground + 6, oz), ModBlocks.MEDIEVAL_STONE_BATTLEMENT.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
            }
            for (int z = oz + 1; z <= oz + 7; z++) {
                level.setBlock(new BlockPos(0, ground + 6, z), ModBlocks.DARK_STONE_BRICK_BATTLEMENT.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, Direction.EAST), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(10, ground + 6, z), ModBlocks.LIMESTONE_BRICK_BATTLEMENT.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, Direction.WEST), Block.UPDATE_CLIENTS);
            }
            // Licht: Kronleuchter in der Mitte, Laternen an der Decke und auf den Tischen
            level.setBlock(new BlockPos(5, ground + 4, oz + 4), ModBlocks.CHANDELIER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            for (int[] spot : new int[][] {{2, oz + 6}, {8, oz + 6}}) {
                level.setBlock(new BlockPos(spot[0], ground + 4, spot[1]), ModBlocks.AETHERIT_LANTERN.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), Block.UPDATE_CLIENTS);
            }
            // Thron mit rotem Teppich
            level.setBlock(new BlockPos(5, ground, oz + 1), ModBlocks.THRONE.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
            fill(level, new BlockPos(5, ground, oz + 2), new BlockPos(5, ground, oz + 9), Blocks.CARPET.red().defaultBlockState());
            // Tafeln links und rechts, mit Stühlen und Bänken
            for (int x : new int[] {2, 8}) {
                for (int z = oz + 3; z <= oz + 5; z++) {
                    BlockPos pos = new BlockPos(x, ground, z);
                    level.setBlock(pos, ModBlocks.OAK_TABLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            for (int x : new int[] {2, 8}) {
                for (int z = oz + 3; z <= oz + 5; z++) {
                    BlockPos pos = new BlockPos(x, ground, z);
                    level.setBlock(pos, Block.updateFromNeighbourShapes(level.getBlockState(pos), level, pos), Block.UPDATE_CLIENTS);
                }
                boolean left = x < 5;
                Direction outer = left ? Direction.EAST : Direction.WEST;    // Stühle an der Wand schauen zum Tisch
                for (int z = oz + 3; z <= oz + 5; z += 2) {
                    level.setBlock(new BlockPos(left ? 1 : 9, ground, z), ModBlocks.OAK_CHAIR.get().defaultBlockState()
                            .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, outer), Block.UPDATE_CLIENTS);
                }
                for (int z = oz + 3; z <= oz + 5; z++) {
                    level.setBlock(new BlockPos(left ? 3 : 7, ground, z), ModBlocks.OAK_BENCH.get().defaultBlockState()
                            .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, outer.getOpposite()), Block.UPDATE_CLIENTS);
                }
            }
            level.setBlock(new BlockPos(2, ground + 1, oz + 4), ModBlocks.STEEL_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(8, ground + 1, oz + 4), ModBlocks.STEEL_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            // Ein Ritter auf dem Thron, Soldaten an der Tafel
            spawnSoldier(level, SoldierType.CAPTAIN, 0, 5, oz + 1, 0, s -> seat(level, s, new BlockPos(5, ground, oz + 1)));
            spawnSoldier(level, SoldierType.SWORDSMAN, 1, 3, oz + 4, 0, s -> seat(level, s, new BlockPos(3, ground, oz + 4)));
            spawnSoldier(level, SoldierType.ARCHER, 0, 9, oz + 3, 0, s -> seat(level, s, new BlockPos(9, ground, oz + 3)));

            // Ausstellung daneben: jedes Stück einzeln (Thron leer), Zinnenreihe, Tür offen und zu
            Direction south = Direction.SOUTH;
            level.setBlock(new BlockPos(15, ground, oz + 4), ModBlocks.THRONE.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(17, ground, oz + 4), ModBlocks.OAK_CHAIR.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(19, ground, oz + 4), ModBlocks.OAK_BENCH.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(20, ground, oz + 4), ModBlocks.OAK_BENCH.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(22, ground, oz + 4), ModBlocks.OAK_TABLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(22, ground + 1, oz + 4), ModBlocks.AETHERIT_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(24, ground + 2, oz + 4), Blocks.DARK_OAK_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(24, ground + 1, oz + 4), ModBlocks.CHANDELIER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            for (int x = 14; x <= 24; x++) {
                Block battlement = x < 18 ? ModBlocks.MEDIEVAL_STONE_BATTLEMENT.get()
                        : x < 22 ? ModBlocks.DARK_STONE_BRICK_BATTLEMENT.get() : ModBlocks.LIMESTONE_BRICK_BATTLEMENT.get();
                level.setBlock(new BlockPos(x, ground, oz), battlement.defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, south), Block.UPDATE_CLIENTS);
            }
            for (int i = 0; i < 2; i++) {
                BlockState castleDoor = ModBlocks.CASTLE_DOOR.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.DoorBlock.FACING, south)
                        .setValue(net.minecraft.world.level.block.DoorBlock.OPEN, i == 1);
                level.setBlock(new BlockPos(14 + i * 2, ground, oz + 8), castleDoor, Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(14 + i * 2, ground + 1, oz + 8), castleDoor.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
                BlockState shutter = ModBlocks.RED_SHUTTERS.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH)
                        .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, i == 1);
                level.setBlock(new BlockPos(19 + i * 2, ground, oz + 8), shutter, Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(19 + i * 2, ground + 1, oz + 8), ModBlocks.STEEL_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            level.setBlock(new BlockPos(23, ground, oz + 8), ModBlocks.LEADED_GLASS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(24, ground, oz + 8), ModBlocks.LEADED_GLASS_PANE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(5.5, 2.6, oz + 14.5, 180, 8));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("decor_hall"));
        steps.add(tp(5.5, 1.7, oz + 6.6, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_throne"));
        steps.add(tp(7.2, 2.0, oz + 6.9, 120, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_furniture"));
        steps.add(tp(4.0, 1.6, oz + 3.5, -90, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_door"));
        steps.add(tp(5.5, 1.4, oz + 6.5, 180, -32));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_lights"));
        steps.add(tp(-6.0, 5.0, oz + 13.0, 225, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_outside"));
        steps.add(tp(19.5, 2.4, oz + 13.5, 180, 14));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_display"));
        steps.add(tp(17.5, 1.6, oz + 7.5, 160, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_display_close"));
        steps.add(tp(19.0, 2.4, oz + 3.0, 180, 30));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_battlements"));
        steps.add(tp(19.5, 1.8, oz + 11.5, 180, 12));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_doors"));
        steps.add(Step.command("time set 18000"));
        steps.add(tp(5.5, 2.6, oz + 14.5, 180, 8));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("decor_hall_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
    }

    /** Hopfen, Weinreben und Knoblauch in allen Wachstumsstufen, Mondblumen bei Nacht, Festmahl auf der Tafel. */
    private static void farm(Deque<Step> steps) {
        int oz = 320;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-8, ground - 1, oz - 10), new BlockPos(24, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-8, ground, oz - 10), new BlockPos(24, ground + 6, oz + 14), Blocks.AIR.defaultBlockState());
            List<io.github.sirschrolli.kingdomsatwar.block.ModCropBlock> crops = List.of(ModBlocks.HOPS.get(), ModBlocks.GRAPES.get(),
                    ModBlocks.GARLIC_CROP.get());
            int[] ages = {0, 2, 5, 7};
            for (int row = 0; row < crops.size(); row++) {
                for (int i = 0; i < 8; i++) {
                    BlockPos soil = new BlockPos(i, ground - 1, oz - row * 2);
                    level.setBlock(soil, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(soil.above(), crops.get(row).getStateForAge(ages[Math.min(3, i / 2)]), Block.UPDATE_CLIENTS);
                }
                level.setBlock(new BlockPos(8, ground - 1, oz - row * 2), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            // Mondblumen
            for (int[] spot : new int[][] {{12, 0}, {13, 1}, {14, -1}, {15, 1}, {12, 2}, {16, 0}, {14, 2}}) {
                level.setBlock(new BlockPos(spot[0], ground, oz + spot[1]), ModBlocks.MOONFLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            level.setBlock(new BlockPos(17, ground, oz + 2), ModBlocks.POTTED_MOONFLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            // Tafel mit vier Festmahlen (voll bis fast aufgegessen)
            for (int i = 0; i < 4; i++) {
                BlockPos table = new BlockPos(1 + i, ground, oz + 4);
                level.setBlock(table, ModBlocks.OAK_TABLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (int i = 0; i < 4; i++) {
                BlockPos table = new BlockPos(1 + i, ground, oz + 4);
                level.setBlock(table, Block.updateFromNeighbourShapes(level.getBlockState(table), level, table), Block.UPDATE_CLIENTS);
                level.setBlock(table.above(), ModBlocks.FEAST.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.FeastBlock.FACING, Direction.SOUTH)
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.FeastBlock.EATEN, i), Block.UPDATE_CLIENTS);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(4.0, 2.6, oz + 5.5, 180, 30));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("farm_crops"));
        steps.add(tp(4.0, 1.6, oz + 2.8, 180, 22));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("farm_crops_close"));
        steps.add(tp(3.0, 2.2, oz + 7.5, 180, 38));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("farm_feast"));
        steps.add(tp(2.6, 2.0, oz + 5.9, 180, 48));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("farm_feast_close"));
        steps.add(Step.command("time set 18000"));
        steps.add(tp(14.5, 1.6, oz + 5.0, 180, 20));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("farm_moonflowers_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
    }

    /** Der Aetherit-Stab in Aktion: jeder Zauber einmal, kurz nach dem Wirken fotografiert. */
    private static void magic(Deque<Step> steps) {
        int oz = 380;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-8, ground - 1, oz - 16), new BlockPos(16, ground - 1, oz + 6), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-8, ground, oz - 16), new BlockPos(16, ground + 8, oz + 6), Blocks.AIR.defaultBlockState());
            fill(level, new BlockPos(2, ground - 1, oz - 6), new BlockPos(7, ground - 1, oz - 3), Blocks.WATER.defaultBlockState());
            for (int x : new int[] {1, 8}) {
                var golem = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                golem.setNoAi(true);
                golem.snapTo(x + 0.5, ground, oz - 9 + 0.5, 0, 0);
                level.addFreshEntity(golem);
            }
        }));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:aetherit_staff"));
        steps.add(Step.command("item replace entity @s hotbar.1 with kingdomsatwar:fire_rune"));
        steps.add(Step.command("item replace entity @s hotbar.2 with kingdomsatwar:ice_rune"));
        steps.add(Step.command("item replace entity @s hotbar.3 with kingdomsatwar:lightning_rune"));
        steps.add(Step.command("item replace entity @s hotbar.4 with kingdomsatwar:nature_rune"));
        steps.add(Step.command("item replace entity @s hotbar.5 with kingdomsatwar:earth_rune"));
        steps.add(Step.command("item replace entity @s hotbar.6 with kingdomsatwar:water_rune"));
        steps.add(tp(4.5, 0, oz + 1.5, 180, 14));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(120));
        String[][] casts = {{"", "AETHER_BOLT"}, {"fire_rune", "FIRE"}, {"ice_rune", "ICE"}, {"lightning_rune", "LIGHTNING"},
                {"nature_rune", "NATURE"}, {"earth_rune", "EARTH"}, {"water_rune", "WATER"}};
        for (String[] cast : casts) {
            steps.add(Step.command(cast[0].isEmpty() ? "item replace entity @s weapon.offhand with air"
                    : "item replace entity @s weapon.offhand with kingdomsatwar:" + cast[0]));
            steps.add(Step.waitTicks(4));
            steps.add(Step.server(level -> {
                var player = level.getServer().getPlayerList().getPlayers().get(0);
                io.github.sirschrolli.kingdomsatwar.magic.Spell.valueOf(cast[1]).cast(level, player);
            }));
            steps.add(Step.waitTicks(cast[1].equals("LIGHTNING") ? 2 : 5));
            steps.add(Step.screenshot("magic_" + cast[1].toLowerCase(java.util.Locale.ROOT)));
            steps.add(Step.waitTicks(30));
        }
        steps.add(Step.command("gamemode spectator"));
    }

    /** Die sechs Drachennester und eine Burgruine, wie sie die Weltgenerierung baut. */
    private static void structures(Deque<Step> steps) {
        int oz = 440;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-10, ground - 3, oz - 14), new BlockPos(90, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-10, ground, oz - 14), new BlockPos(90, ground + 10, oz + 14), Blocks.AIR.defaultBlockState());
            var generator = level.getChunkSource().getGenerator();
            int x = 0;
            for (DragonType type : DragonType.values()) {
                new io.github.sirschrolli.kingdomsatwar.worldgen.DragonNestFeature(type)
                        .place(level, generator, net.minecraft.util.RandomSource.create(5 + x), new BlockPos(x, ground, oz));
                x += 14;
            }
            new io.github.sirschrolli.kingdomsatwar.worldgen.CastleRuinFeature()
                    .place(level, generator, net.minecraft.util.RandomSource.create(3), new BlockPos(x + 2, ground, oz));
            level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new net.minecraft.world.phys.AABB(-20, ground - 5, oz - 20, 120, ground + 20, oz + 20))
                    .forEach(mob -> {
                        mob.setNoAi(true);
                        mob.setPersistenceRequired();
                    });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(0.5, 9.0, oz + 13.0, 180, 35));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.waitTicks(100));
        String[] names = {"fire", "ice", "lightning", "nature", "earth", "water"};
        for (int i = 0; i < names.length; i++) {
            steps.add(tp(i * 14 + 0.5, 7.0, oz + 10.0, 180, 35));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("structure_nest_" + names[i]));
        }
        steps.add(tp(14.5, 2.2, oz + 3.0, 180, 30));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("structure_nest_ice_close"));
        steps.add(tp(6 * 14 + 2.5, 9.0, oz + 13.0, 180, 32));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("structure_castle_ruin"));
        steps.add(tp(6 * 14 + 10.0, 6.0, oz + 9.0, 135, 22));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("structure_castle_ruin_side"));
        steps.add(Step.hud(true));
    }

    /** Setzt ein Lebewesen auf das Sitzmöbel bei {@code pos}. */
    private static void seat(ServerLevel level, net.minecraft.world.entity.LivingEntity sitter, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof io.github.sirschrolli.kingdomsatwar.block.SeatBlock seat) {
            io.github.sirschrolli.kingdomsatwar.entity.SeatEntity.sit(level, pos, seat.seatHeight(),
                    state.getValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING), sitter);
        }
    }

    private static <T extends net.minecraft.world.entity.Mob> void spawnCreature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type,
            int x, int z, float yaw, java.util.function.Consumer<T> setup) {
        T mob = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (mob == null) {
            return;
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.snapTo(x + 0.5, ground, z + 0.5, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setup.accept(mob);
        level.addFreshEntity(mob);
    }

    /** Die sechs Dracheneier neben dem echten Drachenei, aus Spielersicht. */
    private static void eggs(Deque<Step> steps) {
        int oz = 100;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 6), new BlockPos(18, ground - 1, oz + 8), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-6, ground, oz - 6), new BlockPos(18, ground + 4, oz + 8), Blocks.AIR.defaultBlockState());
            level.setBlock(new BlockPos(0, ground, oz), Blocks.DRAGON_EGG.defaultBlockState(), Block.UPDATE_CLIENTS);
            int x = 2;
            for (DragonType type : DragonType.values()) {
                level.setBlock(new BlockPos(x, ground, oz), ModBlocks.DRAGON_EGGS.get(type).get().defaultBlockState(), Block.UPDATE_CLIENTS);
                x += 2;
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(false));
        steps.add(tp(6.5, 1.62, oz + 5.5, 180, 18));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("eggs_player_view"));
        steps.add(tp(3.5, 1.2, oz + 2.6, 180, 20));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("eggs_close"));
        steps.add(Step.hud(true));
    }

    /** Soldaten aller Arten und Gesichter, Banditen, Gesten, ein Gefecht und das Anwerbe-Fenster. */
    private static void soldiers(Deque<Step> steps) {
        int oz = -60;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-10, ground - 1, oz - 30), new BlockPos(40, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-10, ground, oz - 30), new BlockPos(40, ground + 6, oz + 14), Blocks.AIR.defaultBlockState());
            // Reihe 1: Schwertkämpfer und Bogenschützen (alle Gesichter), Blick nach Süden zur Kamera
            int x = 0;
            for (SoldierType type : List.of(SoldierType.SWORDSMAN, SoldierType.ARCHER)) {
                for (int v = 0; v < type.variants(); v++) {
                    spawnSoldier(level, type, v, x, oz, 0, s -> { });
                    x += 2;
                }
            }
            // Reihe 2: Ritter, Hauptleute, Banditen
            x = 0;
            for (SoldierType type : List.of(SoldierType.KNIGHT, SoldierType.CAPTAIN)) {
                for (int v = 0; v < type.variants(); v++) {
                    spawnSoldier(level, type, v, x, oz - 6, 0, s -> { });
                    x += 2;
                }
            }
            for (boolean archer : new boolean[] {false, true}) {
                for (int v = 0; v < BanditEntity.VARIANTS; v++) {
                    BanditEntity bandit = (archer ? ModEntities.BANDIT_ARCHER : ModEntities.BANDIT).get()
                            .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    bandit.setVariant(v);
                    bandit.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                            new net.minecraft.world.item.ItemStack(archer ? net.minecraft.world.item.Items.BOW : net.minecraft.world.item.Items.IRON_AXE));
                    bandit.setNoAi(true);
                    bandit.snapTo(x + 0.5, ground, oz - 12 + 0.5, 0, 0);
                    bandit.setYBodyRot(0);
                    bandit.setYHeadRot(0);
                    bandit.setPersistenceRequired();
                    level.addFreshEntity(bandit);
                    x += 2;
                }
            }
            // Reihe 3: Gesten und Kampfhaltungen
            spawnSoldier(level, SoldierType.CAPTAIN, 0, 0, oz - 18, 0, s -> { });
            spawnSoldier(level, SoldierType.SWORDSMAN, 0, 2, oz - 18, 0, s -> { });
            spawnSoldier(level, SoldierType.KNIGHT, 1, 4, oz - 18, 0, s -> s.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND));
            spawnSoldier(level, SoldierType.ARCHER, 0, 6, oz - 18, 0, s -> {
                s.setAggressive(true);
                s.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
            });
            // Rekrutierungstisch
            level.setBlock(new BlockPos(30, ground, oz), ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(7.0, 1.7, oz + 7.0, 180, 6));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("soldiers_line_1"));
        steps.add(tp(7.0, 1.7, oz - 7.0 + 0.0, 0, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("soldiers_back_1"));
        steps.add(tp(4.0, 1.7, oz + 1.0, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("soldiers_line_2"));
        steps.add(tp(15.0, 1.7, oz - 5.5, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("soldiers_bandits"));
        // Gesten: Salut und Jubel, Schild, Bogen
        steps.add(tp(3.5, 1.6, oz - 13.5, 180, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> level.getEntitiesOfClass(SoldierEntity.class,
                new net.minecraft.world.phys.AABB(-1, ground - 1, oz - 19, 3, ground + 3, oz - 17)).forEach(s -> {
                    if (s.getSoldierType() == SoldierType.CAPTAIN) {
                        s.salute();
                    } else {
                        s.cheer();
                    }
                })));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("soldiers_gestures_a"));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("soldiers_gestures"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("soldiers_gestures_c"));
        // Rückansicht der Zusatzteile (Umhang, Feder, Köcher)
        steps.add(tp(3.5, 1.9, oz - 22.0, 0, 6));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("soldiers_gestures_back"));
        // Gefecht: vier Soldaten gegen vier Banditen
        steps.add(Step.server(level -> {
            int bx = 14;
            int bz = oz - 24;
            SoldierType[] army = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.CAPTAIN, SoldierType.ARCHER};
            for (int i = 0; i < army.length; i++) {
                SoldierEntity soldier = ModEntities.SOLDIERS.get(army[i]).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                soldier.equip();
                soldier.setVariant(i % army[i].variants());
                soldier.snapTo(bx + i * 2 + 0.5, ground, bz + 0.5 + (army[i] == SoldierType.ARCHER ? -3 : 0), 0, 0);
                soldier.setPersistenceRequired();
                level.addFreshEntity(soldier);
            }
            for (int i = 0; i < 4; i++) {
                BanditEntity bandit = (i == 3 ? ModEntities.BANDIT_ARCHER : ModEntities.BANDIT).get()
                        .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                bandit.setVariant(i % BanditEntity.VARIANTS);
                bandit.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                        new net.minecraft.world.item.ItemStack(i == 3 ? net.minecraft.world.item.Items.BOW : net.minecraft.world.item.Items.IRON_AXE));
                bandit.snapTo(bx + i * 2 + 0.5, ground, bz + 9.5, 180, 0);
                bandit.setPersistenceRequired();
                level.addFreshEntity(bandit);
            }
        }));
        steps.add(tp(25.0, 4.0, oz - 19.5, 125, 18));
        steps.add(Step.waitTicks(45));
        steps.add(Step.screenshot("soldiers_battle_1"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("soldiers_battle_2"));
        // Rekrutierungstisch und Anwerbe-Fenster
        steps.add(tp(30.5, 1.4, oz + 2.6, 180, 32));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("recruitment_table"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
        steps.add(Step.screen(minecraft -> new RecruitScreen(new BlockPos(30, ground, oz), 32)));
        steps.add(Step.waitTicks(15));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("recruit_screen_1"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof RecruitScreen screen) {
                screen.select(SoldierType.KNIGHT);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("recruit_screen_2"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.waitTicks(5));
    }

    private static void spawnSoldier(ServerLevel level, SoldierType type, int variant, int x, int z, float yaw,
            java.util.function.Consumer<SoldierEntity> setup) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (soldier == null) {
            return;
        }
        soldier.equip();
        soldier.setVariant(variant);
        soldier.giveRandomName();
        soldier.setNoAi(true);
        soldier.snapTo(x + 0.5, ground, z + 0.5, yaw, 0);
        soldier.setYBodyRot(yaw);
        soldier.setYHeadRot(yaw);
        level.addFreshEntity(soldier);
        setup.accept(soldier);
    }

    private static void spawnDragon(ServerLevel level, DragonType type, DragonStage stage, BlockPos pos, float yaw,
            java.util.function.Consumer<DragonEntity> setup) {
        DragonEntity dragon = ModEntities.DRAGONS.get(type).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (dragon == null) {
            return;
        }
        dragon.applyStage(stage, true);
        dragon.setNoAi(true);
        dragon.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        dragon.setYBodyRot(yaw);
        dragon.setYHeadRot(yaw);
        dragon.setPersistenceRequired();
        level.addFreshEntity(dragon);
        setup.accept(dragon);
    }

    /** Die vier Kreativ-Tabs der Mod. */
    private static void items(Deque<Step> steps) {
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        List<CreativeModeTab> tabs = List.of(ModCreativeTabs.BUILDING.get(), ModCreativeTabs.RESOURCES.get(),
                ModCreativeTabs.DRAGONS.get(), ModCreativeTabs.FARMING.get(), ModCreativeTabs.KINGDOM.get(),
                ModCreativeTabs.EQUIPMENT.get());
        String[] names = {"tab_building", "tab_resources", "tab_dragons", "tab_farming", "tab_kingdom", "tab_equipment"};
        for (int i = 0; i < tabs.size(); i++) {
            CreativeModeTab tab = tabs.get(i);
            steps.add(Step.screen(minecraft -> new CreativeModeInventoryScreen(minecraft.player,
                    minecraft.player.connection.enabledFeatures(), false)));
            steps.add(Step.waitTicks(5));
            steps.add(Step.once(minecraft -> {
                selectTab(minecraft, tab);
                parkMouse(minecraft);
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot(names[i]));
            if (i == 0) {
                // Der Bau-Tab ist länger als eine Seite: ans Ende blättern
                steps.add(Step.once(minecraft -> minecraft.gui.screen().mouseScrolled(100, 100, 0, -30)));
                steps.add(Step.waitTicks(5));
                steps.add(Step.screenshot(names[i] + "_2"));
            }
            steps.add(Step.screen(minecraft -> null));
            steps.add(Step.waitTicks(5));
        }
        steps.add(Step.command("gamemode spectator"));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Kamera (Zuschauer) an eine Stelle relativ zur Bodenhöhe setzen. */
    private static Step tp(int x, int dy, int z, float yaw, float pitch) {
        return Step.once(minecraft -> minecraft.player.connection.sendCommand(
                "tp @s %d %d %d %s %s".formatted(x, ground + dy, z, yaw, pitch)));
    }

    private static Step tp(double x, double dy, double z, float yaw, float pitch) {
        return Step.once(minecraft -> minecraft.player.connection.sendCommand(
                String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %s %s", x, ground + dy, z, yaw, pitch)));
    }

    /** Mauszeiger aus dem Bild schieben, damit keine Tooltips erscheinen. */
    static void parkMouse(Minecraft minecraft) {
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, 1.0);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void selectTab(Minecraft minecraft, CreativeModeTab tab) {
        if (minecraft.gui.screen() instanceof CreativeModeInventoryScreen screen) {
            try {
                // Nur Entwicklungscode: selectTab ist privat
                Method select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
                select.setAccessible(true);
                select.invoke(screen, tab);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void place(ServerLevel level, BlockPos pos, Block block) {
        BlockState state = block.defaultBlockState();
        if (block instanceof CropBlock crop) {
            level.setBlock(pos.below(), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
            state = crop.getStateForAge(crop.getMaxAge());
        } else if (block instanceof FlowerBlock) {
            level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        } else if (block instanceof StairBlock) {
            state = state.setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.BOTTOM);
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}
