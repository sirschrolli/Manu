package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.client.waystone.WaystoneCompassScreen;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompass;
import io.github.sirschrolli.kingdomsatwar.waystone.Waypoints;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Vorführung von Wegkompass und Wegsteinen (seit 1.12): drei Wegsteine auf einer Wiese, der Kompass in der Hand (von vorne,
 * von hinten und bei Nacht), sein Fenster mit sechs gemerkten Wegsteinen (eins davon im Nether) und das Inventar.
 */
final class WaystoneShowcase {
    private static final int OZ = -40;

    private WaystoneShowcase() {}

    static void waystones(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(2);
            minecraft.resizeGui();
        }));
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-14, ground - 1, OZ - 22), new BlockPos(14, ground - 1, OZ + 8), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-14, ground, OZ - 22), new BlockPos(14, ground + 12, OZ + 8), Blocks.AIR.defaultBlockState());
            // ein Pfad zu den Steinen, ein paar Blumen
            Showcase.fill(level, new BlockPos(0, ground - 1, OZ - 12), new BlockPos(1, ground - 1, OZ + 2), Blocks.DIRT_PATH.defaultBlockState());
            for (int[] f : new int[][] {{-3, -1}, {4, -2}, {-6, -6}, {6, -9}, {-2, -11}, {3, -13}}) {
                level.setBlock(new BlockPos(f[0], ground, OZ + f[1]), Blocks.CORNFLOWER.defaultBlockState(), 3);
            }
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("effect give @s minecraft:resistance infinite 255 true"));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.hud(true));
        steps.add(Showcase.tp(0.5, 0.0, OZ + 1.5, 180, 21));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            place(level, new BlockPos(0, ground, OZ - 2), "Heimat");
            place(level, new BlockPos(4, ground, OZ - 6), "Rabenfels");
            place(level, new BlockPos(-5, ground, OZ - 9), "Mühle am Fluss");
            ServerPlayer me = level.getServer().getPlayerList().getPlayers().getFirst();
            List<Waypoints.Waypoint> entries = new ArrayList<>();
            entries.add(waypoint(level.dimension(), new BlockPos(0, ground, OZ - 2), "Heimat"));
            entries.add(waypoint(level.dimension(), new BlockPos(4, ground, OZ - 6), "Rabenfels"));
            entries.add(waypoint(level.dimension(), new BlockPos(-5, ground, OZ - 9), "Mühle am Fluss"));
            entries.add(waypoint(level.dimension(), new BlockPos(330, ground, OZ - 410), "Drachenhorst"));
            entries.add(waypoint(Level.NETHER, new BlockPos(12, 64, -30), "Nethertor"));
            entries.add(waypoint(level.dimension(), new BlockPos(-780, ground, OZ + 260), "Hafen von Weißbucht"));
            ItemStack compass = new ItemStack(ModItems.WAYSTONE_COMPASS.get());
            WaystoneCompass.set(compass, new Waypoints(entries, 0));
            me.setItemInHand(InteractionHand.MAIN_HAND, compass);
            me.getInventory().setItem(1, new ItemStack(ModBlocks.WAYSTONE.get(), 3));
            me.getInventory().setItem(2, new ItemStack(ModItems.WAYSTONE_COMPASS.get()));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("waystone_1_hand"));
        // von hinten: wie man den Kompass hält
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("waystone_2_hinten"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("waystone_3_vorne"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        // der Wegstein als Gegenstand in der Hand (von vorne und von hinten)
        steps.add(Step.once(minecraft -> minecraft.player.getInventory().setSelectedSlot(1)));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("waystone_3b_stein_hand"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("waystone_3c_stein_vorne"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.getInventory().setSelectedSlot(0);
        }));
        steps.add(Step.waitTicks(15));
        // nachts leuchten Rune und Kristall
        steps.add(Step.command("time set 14500"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("waystone_4_nacht"));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.waitTicks(10));
        // das Kompassfenster
        steps.add(Step.screen(minecraft -> new WaystoneCompassScreen(InteractionHand.MAIN_HAND)));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("waystone_5_fenster"));
        steps.add(Step.once(minecraft -> hover(minecraft, card(minecraft, 1, 10, 12))));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("waystone_6_fenster_tooltip"));
        // größere Schrift (Stufe 3): passt das Fenster noch?
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(3);
            minecraft.resizeGui();
        }));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("waystone_7_fenster_gross"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(2);
            minecraft.resizeGui();
        }));
        // Inventar mit dem Kompass unter der Maus
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> {
            // Schnellleiste, erster Platz (Kompass in der Hand)
            int x = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2 + 8 + 8;
            int y = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2 + 142 + 8;
            hover(minecraft, new int[] {x, y});
        }));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("waystone_8_inventar"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("effect clear @s"));
    }

    private static void place(ServerLevel level, BlockPos pos, String name) {
        level.setBlock(pos, ModBlocks.WAYSTONE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof WaystoneBlockEntity stone) {
            stone.setName(Component.literal(name));
        }
    }

    private static Waypoints.Waypoint waypoint(net.minecraft.resources.ResourceKey<Level> dimension, BlockPos pos, String name) {
        return new Waypoints.Waypoint(GlobalPos.of(dimension, pos), Component.literal(name));
    }

    /** Ein Punkt auf einer Karte des Kompassfensters (Platz {@code index}, Versatz in der Karte). */
    private static int[] card(Minecraft minecraft, int index, int dx, int dy) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - 304) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - 210) / 2;
        int x = left + (index < 4 ? 14 : 304 - 14 - 86) + dx;
        int y = top + 37 + (index % 4) * 35 + dy;
        return new int[] {x, y};
    }

    private static void hover(Minecraft minecraft, int[] at) {
        double scale = minecraft.getWindow().getGuiScale();
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? at[0] * scale : at[1] * scale);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
