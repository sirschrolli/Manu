package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
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
                case "items" -> items(steps);
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
                        } else if ((edgeX && edgeZ) || y == 3 || y == 6) {
                            state = beam;
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
