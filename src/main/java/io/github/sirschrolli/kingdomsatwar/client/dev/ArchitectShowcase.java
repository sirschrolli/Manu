package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.architect.Architect;
import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.architect.PlanLibrary;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.client.architect.SavePlanScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.client.town.TownScreen;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownActions;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownStorage;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Vorführung: Dragon Architect. Ein Kaufmannshaus wird mit dem Vermessungsstab vermessen (goldener Rahmen mit Maßen,
 * Bodenlinie und Vorderseite), im Fenster „Bauplan zeichnen“ gezeichnet, als Bauplan in der Siedlung aufgestellt
 * (Vorschau mit Eingang) und von zwei Baumeistern abschnittsweise nachgebaut. Dazu das Stadtfenster mit den eigenen
 * Plänen (und dem Tooltip mit Ansicht und Grundriss) und die Baustelle in der Übersicht.
 */
final class ArchitectShowcase {
    private static final int X0 = 640;
    private static final int Z0 = -560;
    /** Wo der Nachbau steht (östlich vom Vorbild). */
    private static final int COPY_DX = 28;

    private ArchitectShowcase() {}

    static void architect(Deque<Step> steps) {
        BlockPos[] sel = new BlockPos[2];
        BlockPos[] banner = new BlockPos[1];
        BoundingBox[] house = new BoundingBox[1];
        Town[] town = {null};
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("attribute @s minecraft:block_interaction_range base set 48"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        steps.add(Step.command("tp @s " + X0 + " 140 " + (Z0 - 30) + " 0 30"));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int cx = (X0 - 48) >> 4; cx <= (X0 + 64) >> 4; cx++) {
                for (int cz = (Z0 - 40) >> 4; cz <= (Z0 + 48) >> 4; cz++) {
                    level.getChunk(cx, cz);
                    level.setChunkForced(cx, cz, true);
                }
            }
            Showcase.fill(level, new BlockPos(X0 - 44, g - 4, Z0 - 36), new BlockPos(X0 + 60, g - 2, Z0 + 44), Blocks.DIRT.defaultBlockState());
            Showcase.fill(level, new BlockPos(X0 - 44, g - 1, Z0 - 36), new BlockPos(X0 + 60, g - 1, Z0 + 44), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(X0 - 44, g, Z0 - 36), new BlockPos(X0 + 60, g + 32, Z0 + 44), Blocks.AIR.defaultBlockState());
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            // das Vorbild: ein Kaufmannshaus (so hätte es der Spieler selbst bauen können)
            Blueprint plan = BuildingType.TOWNHOUSE.blueprint(2);
            BlockPos origin = new BlockPos(X0, g, Z0);
            plan.placeAll(level, origin, Rotation.NONE);
            BoundingBox box = plan.worldBounds(origin, Rotation.NONE);
            house[0] = box;
            sel[0] = new BlockPos(box.minX(), g - 1, box.minZ());
            sel[1] = new BlockPos(box.maxX(), box.maxY(), box.maxZ());
            // zwei weitere kleine Bauten als frühere Pläne (Brunnen und Sägehütte), schon vermessen
            PlanLibrary library = PlanLibrary.of(level.getServer());
            String[] names = {"Brunnen am Markt", "Sägehütte"};
            BuildingType[] types = {BuildingType.WELL, BuildingType.LUMBER_HUT};
            for (int i = 0; i < types.length; i++) {
                Blueprint small = types[i].blueprint(1);
                BlockPos at = new BlockPos(X0 - 28, g, Z0 - 8 + i * 22);
                small.placeAll(level, at, Rotation.NONE);
                BoundingBox b = small.worldBounds(at, Rotation.NONE);
                BlockPos a = new BlockPos(b.minX(), g - 1, b.minZ());
                BlockPos c = new BlockPos(b.maxX(), b.maxY(), b.maxZ());
                int ground = PlanScan.autoGround(level, a, c);
                Direction door = PlanScan.doorSide(level, a, c, ground, null);
                try {
                    library.add(level.getServer(), PlanScan.scan(level, a, c, ground, door != null ? door : Direction.NORTH, level.registryAccess()),
                            names[i], player.getUUID(), player.getName().getString());
                } catch (java.io.IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            // die Siedlung: Banner, zwei Baumeister, ein Lager voller Baustoffe
            BlockPos b = new BlockPos(X0 + 22, g, Z0 + 14);
            BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState().setValue(TownBannerBlock.FACING, Direction.NORTH);
            level.setBlock(b, lower, Block.UPDATE_ALL);
            level.setBlock(b.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            TownManager.placeBanner(player, level, b);
            Town t = Towns.of(level.getServer()).byOwner(player.getUUID());
            for (int i = 0; i < 2; i++) {
                TownManager.spawnCitizen(level, t, Profession.BUILDER);
            }
            TownStorage.supply(level, b.offset(2, 0, 1), new int[] {1600, 2400, 300});
            Coins.give(player, 4000);
            banner[0] = b;
            town[0] = t;
            // der Vermessungsstab mit beiden Ecken in der Hand
            ItemStack rod = new ItemStack(ModItems.SURVEYORS_ROD.get());
            rod.set(ModDataComponents.SURVEY_FIRST.get(), GlobalPos.of(level.dimension(), sel[0]));
            rod.set(ModDataComponents.SURVEY_SECOND.get(), GlobalPos.of(level.dimension(), sel[1]));
            player.getInventory().setSelectedSlot(0);
            player.getInventory().setItem(0, rod);
            player.getInventory().setItem(1, new ItemStack(net.minecraft.world.item.Items.PAPER, 12));
            // fliegen, damit die Kamera dort bleibt, wo sie hingestellt wird
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }));
        steps.add(Step.until(() -> town[0] != null, 600));

        // 1. der Rahmen des Vermessungsstabs (mit Maßen, Bodenlinie und Vorderseite)
        steps.add(Step.once(minecraft -> {
            BoundingBox b = house[0];
            lookAbs(minecraft, b.minX() - 10.0, Showcase.ground + 7.0, b.minZ() - 13.0, (b.minX() + b.maxX()) / 2.0, Showcase.ground + 4.0,
                    (b.minZ() + b.maxZ()) / 2.0);
        }));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("architect_rod"));
        steps.add(Step.hud(false));

        // 2. das Fenster „Bauplan zeichnen“
        steps.add(Step.once(minecraft -> {
            SavePlanScreen screen = new SavePlanScreen(sel[0], sel[1]);
            minecraft.gui.setScreen(screen);
            screen.setName("Kaufmannshaus");
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("architect_save"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        // gezeichnet wird auf dem Server (wie mit dem Knopf „Zeichnen“)
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            int ground = PlanScan.autoGround(level, sel[0], sel[1]);
            Direction door = PlanScan.doorSide(level, sel[0], sel[1], ground, player.position());
            Architect.save(player, sel[0], sel[1], "Kaufmannshaus", ground, door != null ? door : Direction.NORTH);
            // den neuen Bauplan in die Hand
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                PlanInfo info = BlueprintItem.plan(stack);
                if (info != null && info.name().equals("Kaufmannshaus")) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.getInventory().setItem(0, stack);
                    break;
                }
            }
        }));
        steps.add(Step.waitTicks(10));

        // 3. Vorschau am Bauplatz: der Bauplan in der Hand, Blick auf den Boden, wo der Nachbau stehen soll
        steps.add(Step.once(minecraft -> lookAbs(minecraft, X0 + COPY_DX + 0.5, Showcase.ground + 15.0, Z0 - 30.0, X0 + COPY_DX + 0.5,
                Showcase.ground - 0.5, Z0 + 0.5)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("architect_preview"));
        steps.add(Step.hud(false));

        // 4. aufstellen und abschnittsweise bauen lassen
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            for (PlanInfo info : PlanLibrary.of(level.getServer()).byAuthor(player.getUUID())) {
                if (info.name().equals("Kaufmannshaus")) {
                    TownManager.placeCustom(player, info, new BlockPos(X0 + COPY_DX, Showcase.ground, Z0), Rotation.NONE);
                    // die kostbaren Blöcke (Braustand …) liegen im Lager bereit
                    for (PlanInfo.Precious p : info.costs().precious()) {
                        TownStorage.store(level, town[0], new ItemStack(p.item(), p.count()), banner[0]);
                    }
                }
            }
            Town t = town[0];
            if (t != null && !t.projects().isEmpty()) {
                t.projects().getFirst().fast = true;
            }
        }));
        steps.add(Step.command("tick rate 80"));
        int shot = 0;
        for (double target : new double[] {0.3, 0.65, 2.0}) {
            int index = shot++;
            steps.add(Step.until(() -> {
                Town t = town[0];
                if (t == null) {
                    return false;
                }
                return t.projects().isEmpty() || target <= 1.0 && t.projects().getFirst().fraction() >= target;
            }, 80000));
            if (index == 2) {
                steps.add(Step.waitTicks(60)); // Staub vom Abbau der Gerüste verwehen lassen
            }
            steps.add(Step.command("tick freeze"));
            steps.add(Step.once(minecraft -> lookAbs(minecraft, X0 + COPY_DX - 13.0, Showcase.ground + 9.0, Z0 - 15.0, X0 + COPY_DX,
                    Showcase.ground + 3.5, Z0)));
            steps.add(Step.waitTicks(30));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.screenshot("architect_build_" + index));
            if (index == 1) {
                // die Baustelle im Stadtfenster (mit Namen und Bauweise)
                steps.add(Step.once(minecraft -> lookAbs(minecraft, banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() - 4.0,
                        banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() + 0.5)));
                steps.add(Step.waitTicks(10));
                steps.add(Step.server(level -> {
                    ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
                    TownActions.open(player, town[0], banner[0]);
                }));
                steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof TownScreen, 200));
                steps.add(Step.once(Showcase::parkMouse));
                steps.add(Step.waitTicks(8));
                steps.add(Step.screenshot("architect_town_projects"));
                steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
            }
            steps.add(Step.command("tick unfreeze"));
        }
        steps.add(Step.command("tick rate 20"));

        // 5. die eigenen Pläne im Stadtfenster (und der Tooltip mit Ansicht und Grundriss)
        steps.add(Step.once(minecraft -> lookAbs(minecraft, banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() - 4.0,
                banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() + 0.5)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            TownActions.open(player, town[0], banner[0]);
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof TownScreen, 200));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof TownScreen screen) {
                screen.selectTab(3);
            }
            TownShowcase.hover(minecraft, 9 + 2 + 3 * 60 + 29, 52 + 15 + 2 * 44 + 20);
        }));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("architect_build_tab"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof TownScreen screen) {
                screen.showPlans();
            }
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("architect_plans"));
        steps.add(Step.once(minecraft -> TownShowcase.hover(minecraft, 9 + 4 + 20, 52 + 19 + 13)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("architect_plans_tooltip"));
        steps.add(Step.once(minecraft -> {
            Showcase.parkMouse(minecraft);
            minecraft.gui.setScreen(null);
        }));

        // 6. Vorbild und Nachbau nebeneinander
        steps.add(Step.once(minecraft -> lookAbs(minecraft, X0 + COPY_DX / 2.0, Showcase.ground + 8.0, Z0 - 26.0, X0 + COPY_DX / 2.0,
                Showcase.ground + 5.0, Z0)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("architect_twins"));
    }

    /** Kamera an eine Stelle (absolute Höhen), Blick auf einen Punkt. */
    private static void lookAbs(Minecraft minecraft, double ex, double ey, double ez, double tx, double ty, double tz) {
        double lx = tx - ex;
        double ly = ty - ey;
        double lz = tz - ez;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", ex, ey - 1.62, ez, yaw, pitch));
    }
}
