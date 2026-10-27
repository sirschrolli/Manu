package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.client.town.TownScreen;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownActions;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Vorführung der Baupläne der Siedlung: jedes Gebäude wird auf ebenem Land gebaut und von vorn, hinten und innen gezeigt. */
final class TownShowcase {
    private TownShowcase() {}

    static void plans(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1500"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        int i = 0;
        for (BuildingType type : BuildingType.values()) {
            if (!only.isEmpty() && !type.id().equals(only)) {
                continue;
            }
            double[] c = new double[4];
            int ox = 40 + i * 40;
            int oz = 40;
            i++;
            steps.add(Step.server(level -> {
                for (int cx = (ox - 24) >> 4; cx <= (ox + 24) >> 4; cx++) {
                    for (int cz = (oz - 24) >> 4; cz <= (oz + 24) >> 4; cz++) {
                        level.getChunk(cx, cz);
                    }
                }
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox, oz);
                BlockPos origin = new BlockPos(ox, y, oz);
                type.blueprint().placeAll(level, origin, Rotation.NONE);
                c[0] = ox + 0.5;
                c[1] = oz + 0.5;
                c[2] = y;
                c[3] = 1;
                KingdomsAtWar.LOGGER.info("[Showcase] {} gebaut bei {}", type.id(), origin);
            }));
            steps.add(Step.until(() -> c[3] != 0, 600));
            BoundingBox b = type.blueprint().bounds();
            double cx = (b.minX() + b.maxX()) / 2.0;
            double cz = (b.minZ() + b.maxZ()) / 2.0;
            double h = b.maxY();
            double d = Math.max(Math.max(b.getXSpan(), b.getZSpan()), h) * 0.75 + 3;
            double[][] views = {
                    {cx - d * 0.5, h * 0.5 + d * 0.22, b.minZ() - d * 0.7, cx, h * 0.38, cz},
                    {cx + d * 0.55, h * 0.5 + d * 0.25, b.maxZ() + d * 0.65, cx, h * 0.38, cz},
                    {cx, h + d * 0.9, cz - d * 0.3, cx, 0, cz},
                    interior(type, b)
            };
            int n = 0;
            for (double[] v : views) {
                if (v == null) {
                    continue;
                }
                steps.add(at(c, v));
                steps.add(Step.waitTicks(n == 0 ? 80 : 30));
                steps.add(Step.waitForChunks(800));
                steps.add(Step.screenshot("plan_" + type.id() + "_" + n++));
            }
        }
    }

    /**
     * Die Siedlung im Spiel: Stadtbanner, Gebäude, Bürger aller Berufe (vorn und hinten, bei der Arbeit), dazu das
     * Stadtfenster mit allen Reitern und das Handelsfenster des Händlers.
     */
    static void town(Deque<Step> steps) {
        int ox = -240;
        int oz = 240;
        int[] g = {Integer.MIN_VALUE};
        int[] merchant = {-1};
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1500"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        steps.add(Step.command("tp @s " + ox + " 140 " + (oz + 30) + " 180 30"));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.server(level -> {
            for (int cx = (ox - 40) >> 4; cx <= (ox + 70) >> 4; cx++) {
                for (int cz = (oz - 40) >> 4; cz <= (oz + 50) >> 4; cz++) {
                    level.getChunk(cx, cz);
                }
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ox, oz);
            BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
            BlockState dirt = Blocks.DIRT.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            for (int x = ox - 36; x <= ox + 66; x++) {
                for (int z = oz - 36; z <= oz + 46; z++) {
                    for (int dy = -4; dy <= 32; dy++) {
                        BlockPos p = new BlockPos(x, y + dy, z);
                        level.setBlock(p, dy < -1 ? dirt : dy == -1 ? grass : air, Block.UPDATE_CLIENTS);
                    }
                }
            }
            g[0] = y;
            KingdomsAtWar.LOGGER.info("[Showcase] Siedlung auf Höhe {}", y);
        }));
        steps.add(Step.until(() -> g[0] != Integer.MIN_VALUE, 600));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            BlockPos banner = new BlockPos(ox, g[0], oz);
            BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState().setValue(TownBannerBlock.FACING, Direction.SOUTH);
            level.setBlock(banner, lower, Block.UPDATE_ALL);
            level.setBlock(banner.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            TownManager.placeBanner(player, level, banner);
            Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
            TownManager.buildInstantly(level, town, BuildingType.HOUSE, new BlockPos(ox - 13, g[0], oz - 8), Rotation.CLOCKWISE_90);
            TownManager.buildInstantly(level, town, BuildingType.FIELD, new BlockPos(ox + 14, g[0], oz - 10), Rotation.NONE);
            TownManager.buildInstantly(level, town, BuildingType.SMITHY, new BlockPos(ox - 14, g[0], oz + 8), Rotation.CLOCKWISE_90);
            TownManager.buildInstantly(level, town, BuildingType.MARKET, new BlockPos(ox + 1, g[0], oz - 16), Rotation.NONE);
            TownManager.buildInstantly(level, town, BuildingType.TOWNHOUSE, new BlockPos(ox + 13, g[0], oz + 6), Rotation.COUNTERCLOCKWISE_90);
            for (Profession p : new Profession[] {Profession.BUILDER, Profession.FARMER, Profession.SMITH, Profession.MERCHANT,
                    Profession.MERCHANT}) {
                CitizenEntity c = TownManager.spawnCitizen(level, town, p);
                if (c != null && p == Profession.MERCHANT && merchant[0] < 0) {
                    merchant[0] = c.getId();
                }
            }
            Coins.give(player, 2345);
            TownManager.placeProject(player, BuildingType.HOUSE, new BlockPos(ox + 1, g[0], oz + 12), Rotation.NONE);
            // Reihen mit allen Bürgern (je Gesicht eine Reihe, Blick nach Süden)
            for (int v = 0; v < 3; v++) {
                for (Profession p : Profession.values()) {
                    CitizenEntity c = ModEntities.CITIZEN.get().create(level, EntitySpawnReason.COMMAND);
                    c.setProfession(p);
                    c.setVariant(v);
                    c.setNoAi(true);
                    c.setCustomName(net.minecraft.network.chat.Component.literal(p.id() + " " + (v + 1)));
                    double x = ox + 30 + v * 20 + p.ordinal() * 2 + 0.5;
                    c.snapTo(x, g[0], oz + 30.5, 0.0f, 0.0f);
                    c.setYBodyRot(0.0f);
                    c.setYHeadRot(0.0f);
                    level.addFreshEntity(c);
                }
            }
            // zwei bei der Arbeit (Werkzeug in der Hand)
            for (Profession p : new Profession[] {Profession.LUMBERJACK, Profession.MINER}) {
                CitizenEntity c = ModEntities.CITIZEN.get().create(level, EntitySpawnReason.COMMAND);
                c.setProfession(p);
                c.setNoAi(true);
                c.setWorking(true);
                c.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, p.workItem());
                c.snapTo(ox + 36 + p.ordinal() * 2 + 0.5, g[0], oz + 38.5, 0.0f, 0.0f);
                c.setYBodyRot(0.0f);
                c.setYHeadRot(0.0f);
                level.addFreshEntity(c);
            }
        }));
        steps.add(Step.waitTicks(60));
        // Reihen von vorn und hinten
        for (int v = 0; v < 3; v++) {
            double cx = ox + 30 + v * 20 + 7.5;
            steps.add(Step.command(String.format(Locale.ROOT, "tp @s %.1f %d %.1f 180 8", cx, g[0] == Integer.MIN_VALUE ? 100 : 0, oz + 38.5)));
            int vv = v;
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "tp @s %.1f %.1f %.1f 180 8", ox + 30 + vv * 20 + 7.5, g[0] + 1.2, oz + 38.0))));
            steps.add(Step.waitTicks(30));
            steps.add(Step.waitForChunks(300));
            steps.add(Step.screenshot("citizens_front_" + (v + 1)));
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "tp @s %.1f %.1f %.1f 0 8", ox + 30 + vv * 20 + 7.5, g[0] + 1.2, oz + 23.0))));
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot("citizens_back_" + (v + 1)));
        }
        // nah: Bauer, Bergmann und Händler (Hut, Lampe, Barett mit Feder)
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 160 12", ox + 34.5, g[0] + 1.9, oz + 33.8))));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("citizens_close_1"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 200 12", ox + 82.5, g[0] + 1.9, oz + 33.8))));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("citizens_close_women"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 0 15", ox + 80.5, g[0] + 2.0, oz + 27.0))));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("citizens_back_women_close"));
        // die Siedlung von oben und das Banner
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 180 35", ox + 0.5, g[0] + 18.0, oz + 34.0))));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.screenshot("town_overview"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 153 15", ox + 2.5, g[0] + 0.8, oz + 4.5))));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("town_banner"));
        // Baumeister an der Baustelle
        steps.add(Step.waitTicks(300));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 200 25", ox - 4.0, g[0] + 6.0, oz + 22.0))));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("town_builder_working"));
        steps.add(Step.waitTicks(400));
        steps.add(Step.screenshot("town_builder_working_2"));
        // Stadtfenster mit allen Reitern
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "tp @s %.1f %.1f %.1f 180 10", ox + 0.5, g[0] + 0.5, oz + 5.0))));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
            TownActions.open(player, town, new BlockPos(ox, g[0], oz));
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof TownScreen, 100));
        steps.add(Step.once(Showcase::parkMouse));
        String[] tabs = {"overview", "hire", "residents", "build", "crown"};
        for (int i = 0; i < tabs.length; i++) {
            int index = i;
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof TownScreen screen) {
                    screen.selectTab(index);
                }
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot("town_ui_" + tabs[i]));
        }
        // Tooltip über einem Bauplan
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof TownScreen screen) {
                screen.selectTab(3);
            }
            hover(minecraft, 9 + 2 + 75 + 30, 52 + 15 + 44 + 20);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("town_ui_build_tooltip"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof TownScreen screen) {
                screen.selectTab(1);
            }
            hover(minecraft, 9 + 2 + 40, 52 + 15 + 10);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("town_ui_hire_tooltip"));
        steps.add(Step.once(minecraft -> {
            Showcase.parkMouse(minecraft);
            minecraft.gui.setScreen(null);
        }));
        // Handelsfenster des Händlers
        steps.add(Step.server(level -> {
            if (level.getEntity(merchant[0]) instanceof CitizenEntity m) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
                m.setNoAi(true);
                player.teleportTo(m.getX() + 2.0, m.getY(), m.getZ() + 0.5);
                Trading.open(player, m);
            }
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TradeScreen, 100));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("town_ui_trade"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
    }

    /** Mauszeiger auf eine Stelle des Fensters (in Fensterkoordinaten der Oberfläche, links oben = 0). */
    private static void hover(Minecraft minecraft, int guiX, int guiY) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - 320) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - 210) / 2;
        double scale = minecraft.getWindow().getGuiScale();
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? (left + guiX) * scale : (top + guiY) * scale);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Blick von innen (vom Eingang zur Rückwand), wo es ein Inneres gibt. */
    private static double[] interior(BuildingType type, BoundingBox b) {
        return switch (type) {
            case HOUSE -> new double[] {1.5, 1, -2, -1.5, 1.5, 3};
            case LUMBER_HUT -> new double[] {1.5, 1, -1, -1.5, 1.2, 2};
            case SMITHY -> new double[] {2.5, 1, -3.5, -3, 1.5, 0};
            case MINE -> new double[] {2.5, -11, 0.5, 0, -10.5, -3.5};
            case TAVERN -> new double[] {2.5, 1, -4, -2, 1.5, 3};
            case TOWNHOUSE -> new double[] {1.5, 1, -3, -1.5, 2, 2};
            case BARRACKS -> new double[] {3.5, 1, -3, -2, 1.5, 2};
            case MAP_HOUSE -> new double[] {1.5, 1, -2, -0.5, 1, 2};
            case THRONE_HALL -> new double[] {0, 1, -9, 0, 4, 8};
            default -> null;
        };
    }

    private static Step at(double[] c, double[] v) {
        return Step.once(minecraft -> {
            double dx = v[0];
            double dy = v[1];
            double dz = v[2];
            double lx = v[3] - dx;
            double ly = v[4] - (dy + 1.62);
            double lz = v[5] - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    c[0] + dx, c[2] + dy, c[1] + dz, yaw, pitch));
        });
    }
}
