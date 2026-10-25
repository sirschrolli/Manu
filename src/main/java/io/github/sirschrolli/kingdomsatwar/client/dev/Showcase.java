package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModCreativeTabs;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
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
                case "items" -> items(steps);
                case "dragons" -> dragons(steps);
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
                ModCreativeTabs.DRAGONS.get(), ModCreativeTabs.FARMING.get());
        String[] names = {"tab_building", "tab_resources", "tab_dragons", "tab_farming"};
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
