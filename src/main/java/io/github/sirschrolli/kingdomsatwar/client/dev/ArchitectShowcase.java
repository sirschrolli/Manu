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

    /**
     * Vorführung: Schematiken. Drei eigene Pläne werden als .litematic, .schem und .nbt abgelegt, ein Turm mit dem
     * Strukturblock-Code als Struktur gespeichert; das Stadtfenster zeigt die Dateien zum Einlesen, eine wird eingelesen
     * und von den Baumeistern nachgebaut.
     */
    static void io(Deque<Step> steps) {
        BlockPos[] banner = new BlockPos[1];
        Town[] town = {null};
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        steps.add(Step.command("gamemode creative"));
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
            PlanLibrary library = PlanLibrary.of(level.getServer());
            // drei Bauten vermessen und als Schematiken ablegen
            String[] names = {"Kaufmannshaus", "Sägehütte", "Brunnen am Markt"};
            BuildingType[] types = {BuildingType.TOWNHOUSE, BuildingType.LUMBER_HUT, BuildingType.WELL};
            int[] designs = {2, 1, 1};
            for (int i = 0; i < types.length; i++) {
                Blueprint plan = types[i].blueprint(designs[i]);
                BlockPos at = new BlockPos(X0 - 30 + i * 22, g, Z0 - 10);
                plan.placeAll(level, at, Rotation.NONE);
                BoundingBox b = plan.worldBounds(at, Rotation.NONE);
                BlockPos a = new BlockPos(b.minX(), g - 1, b.minZ());
                BlockPos c = new BlockPos(b.maxX(), b.maxY(), b.maxZ());
                int ground = PlanScan.autoGround(level, a, c);
                Direction door = PlanScan.doorSide(level, a, c, ground, null);
                try {
                    PlanInfo info = library.add(level.getServer(), PlanScan.scan(level, a, c, ground, door != null ? door : Direction.NORTH,
                            level.registryAccess()), names[i], player.getUUID(), player.getName().getString());
                    if (i == 0) {
                        io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.export(player, info);
                        library.hide(level.getServer(), info.id()); // die Abschrift aus der Datei soll gleich neu dazukommen
                    } else {
                        io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.export(player, info);
                    }
                } catch (java.io.IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            // ein Wachturm, gespeichert wie mit dem Strukturblock
            Blueprint tower = BuildingType.BARRACKS.blueprint(1);
            BlockPos at = new BlockPos(X0 + 40, g, Z0 + 30);
            tower.placeAll(level, at, Rotation.NONE);
            BoundingBox tb = tower.worldBounds(at, Rotation.NONE);
            var manager = level.getServer().getStructureTemplateManager();
            net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.fromNamespaceAndPath("kingdomsatwar", "wachturm");
            var template = manager.getOrCreate(id);
            template.fillFromWorld(level, new BlockPos(tb.minX(), g - 1, tb.minZ()),
                    new net.minecraft.core.Vec3i(tb.getXSpan(), tb.getYSpan() + 1, tb.getZSpan()), false, java.util.List.of());
            manager.save(id);
            // die Siedlung
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
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }));
        steps.add(Step.until(() -> town[0] != null, 600));
        // das Stadtfenster: eigene Pläne, dann die Schematiken zum Einlesen
        steps.add(Step.once(minecraft -> lookAbs(minecraft, banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() - 4.0,
                banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() + 0.5)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            TownActions.open(player, town[0], banner[0]);
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof TownScreen, 200));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof TownScreen screen) {
                screen.showImport();
            }
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("architect_import"));
        // die .schem des Kaufmannshauses einlesen (wie mit dem Knopf „Einlesen“)
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            TownActions.handle(player, banner[0], TownActions.IMPORT, 0, "0:" + io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.MODID + "/Kaufmannshaus.schem");
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof TownScreen screen) {
                screen.showPlans();
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("architect_imported"));
        // Tooltip über dem Knopf zum Ablegen
        steps.add(Step.once(minecraft -> TownShowcase.hover(minecraft, 9 + 2 + 298 - 86 + 48 + 8, 52 + 19 + 10)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("architect_export_tip"));
        steps.add(Step.once(minecraft -> {
            Showcase.parkMouse(minecraft);
            minecraft.gui.setScreen(null);
        }));
        // den eingelesenen Plan aufstellen und bauen lassen
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            for (PlanInfo info : PlanLibrary.of(level.getServer()).byAuthor(player.getUUID())) {
                if (info.name().equals("Kaufmannshaus")) {
                    TownManager.placeCustom(player, info, new BlockPos(X0 + COPY_DX, Showcase.ground, Z0), Rotation.NONE);
                    for (PlanInfo.Precious p : info.costs().precious()) {
                        TownStorage.store(level, town[0], new ItemStack(p.item(), p.count()), banner[0]);
                    }
                    break;
                }
            }
            if (!town[0].projects().isEmpty()) {
                town[0].projects().getFirst().fast = true;
            }
        }));
        steps.add(Step.command("tick rate 80"));
        steps.add(Step.until(() -> town[0].projects().isEmpty() || town[0].projects().getFirst().fraction() >= 0.5, 80000));
        steps.add(Step.command("tick freeze"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, X0 + COPY_DX + 19.0, Showcase.ground + 12.0, Z0 - 21.0, X0 + COPY_DX,
                Showcase.ground + 7.0, Z0)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("architect_import_build"));
        steps.add(Step.command("tick unfreeze"));
        steps.add(Step.until(() -> town[0].projects().isEmpty(), 80000));
        steps.add(Step.command("tick rate 20"));
        steps.add(Step.waitTicks(60));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, X0 + COPY_DX + 19.0, Showcase.ground + 12.0, Z0 - 21.0, X0 + COPY_DX,
                Showcase.ground + 7.0, Z0)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("architect_import_done"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, X0 + 6.0, Showcase.ground + 12.0, Z0 - 30.0, X0 + 6.0, Showcase.ground + 4.0, Z0)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("architect_import_overview"));
    }

    /** Wo die Vorschau der Burg steht (weit weg von den anderen Szenen). */
    private static final int CX = 2000;
    private static final int CZ = 2000;

    /** Setzt ein Volumen in die Welt (Lage 0 unter dem Boden), danach passen sich Treppen, Zäune und Scheiben an. */
    static void paste(ServerLevel level, io.github.sirschrolli.kingdomsatwar.architect.io.Volume v, BlockPos min) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        java.util.List<BlockPos> placed = new java.util.ArrayList<>();
        for (int y = 0; y < v.sizeY; y++) {
            for (int z = 0; z < v.sizeZ; z++) {
                for (int x = 0; x < v.sizeX; x++) {
                    BlockState state = v.getBlockState(new BlockPos(x, y, z));
                    if (state.is(Blocks.STRUCTURE_VOID) || state.isAir() && y == 0) {
                        continue;
                    }
                    pos.set(min.getX() + x, min.getY() - 1 + y, min.getZ() + z);
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                    if (!state.isAir()) {
                        placed.add(pos.immutable());
                    }
                }
            }
        }
        for (BlockPos p : placed) {
            BlockState state = level.getBlockState(p);
            BlockState shaped = Block.updateFromNeighbourShapes(state, level, p);
            if (shaped != state && !shaped.isAir()) {
                level.setBlock(p, shaped, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    /** Vorschau der Burg, die der Dragon Architect selbst entwirft (zum Gestalten). */
    static void castle(Deque<Step> steps) {
        int size = Integer.getInteger("kingdomsatwar.castleSize", 160);
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 5000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(16)));
        steps.add(Step.command("tp @s " + (CX + size / 2) + " 180 " + (CZ - 40) + " 0 40"));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int cx = (CX - 32) >> 4; cx <= (CX + size + 32) >> 4; cx++) {
                for (int cz = (CZ - 32) >> 4; cz <= (CZ + size + 32) >> 4; cz++) {
                    level.getChunk(cx, cz);
                    level.setChunkForced(cx, cz, true);
                }
            }
            Showcase.fill(level, new BlockPos(CX - 30, g - 4, CZ - 30), new BlockPos(CX + size + 30, g - 2, CZ + size + 30), Blocks.DIRT.defaultBlockState());
            Showcase.fill(level, new BlockPos(CX - 30, g - 1, CZ - 30), new BlockPos(CX + size + 30, g - 1, CZ + size + 30), Blocks.GRASS_BLOCK.defaultBlockState());
            long t0 = System.nanoTime();
            var v = io.github.sirschrolli.kingdomsatwar.architect.castle.CastleGenerator.generate(size, size, 7L);
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Burg {}x{}x{} entworfen in {} ms, {} Blöcke", v.sizeX, v.sizeY, v.sizeZ,
                    (System.nanoTime() - t0) / 1_000_000, v.blocks());
            paste(level, v, new BlockPos(CX, g, CZ));
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, CX - 50.0, Showcase.ground + 75.0, CZ - 60.0, CX + size * 0.5, Showcase.ground + 8.0,
                CZ + size * 0.45)));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(120));
        steps.add(Step.screenshot("castle_overview"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, CX + size * 0.5 - 40.0, Showcase.ground + 30.0, CZ + size * 0.5 - 30.0,
                CX + size * 0.5, Showcase.ground + 14.0, CZ + size - 22.0)));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("castle_keep"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, CX + size * 0.5, Showcase.ground + 16.0, CZ - 34.0, CX + size * 0.5, Showcase.ground + 10.0,
                CZ + 4.0)));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("castle_gate"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, CX + size * 0.5 + 3.5, Showcase.ground + 3.0, CZ + size * 0.22, CX + size * 0.5,
                Showcase.ground + 14.0, CZ + size * 0.8)));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("castle_courtyard"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, CX + size + 50.0, Showcase.ground + 60.0, CZ + size + 40.0, CX + size * 0.5,
                Showcase.ground + 8.0, CZ + size * 0.5)));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("castle_back"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, CX + size * 0.5, Showcase.ground + 170.0, CZ + size * 0.5 - 1.0, CX + size * 0.5,
                Showcase.ground, CZ + size * 0.5)));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("castle_top"));
    }

    /** Wo die Großbaustelle steht. */
    private static final int GX = 3000;
    private static final int GZ = 3000;

    /**
     * Vorführung: eine Großbaustelle. Eine Burg (vom Dragon Architect selbst entworfen, als hätte sie der Spieler gebaut)
     * wird mit dem Vermessungsstab vermessen und gezeichnet, neben der Siedlung aufgestellt und von zwölf Baumeistern in
     * Abschnitten nachgebaut – mit der Karte der Abschnitte im Stadtfenster und am Ende Vorbild und Nachbau nebeneinander.
     */
    static void great(Deque<Step> steps) {
        int size = Integer.getInteger("kingdomsatwar.castleSize", 112);
        BlockPos[] sel = new BlockPos[2];
        BlockPos[] banner = new BlockPos[1];
        Town[] town = {null};
        int[] height = {0};
        int copyX = GX + size + 48;
        int copyCenterX = copyX + (size - 1) / 2;
        int centerZ = GZ + (size - 1) / 2;
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 3000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule doDaylightCycle false"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(16)));
        steps.add(Step.command("tp @s " + (GX + size) + " 150 " + (GZ - 30) + " 0 50"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int cx = (GX - 40) >> 4; cx <= (copyX + size + 40) >> 4; cx++) {
                for (int cz = (GZ - 40) >> 4; cz <= (GZ + size + 40) >> 4; cz++) {
                    level.getChunk(cx, cz);
                    level.setChunkForced(cx, cz, true);
                }
            }
            Showcase.fill(level, new BlockPos(GX - 36, g - 4, GZ - 36), new BlockPos(copyX + size + 36, g - 2, GZ + size + 36), Blocks.DIRT.defaultBlockState());
            // weit und breit Wiese (statt Sand), damit die Bilder von oben natürlich aussehen
            BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
            for (int x = GX - 200; x <= copyX + size + 200; x++) {
                for (int z = GZ - 200; z <= GZ + size + 200; z++) {
                    BlockPos p = new BlockPos(x, g - 1, z);
                    if (level.hasChunkAt(p)) {
                        level.setBlock(p, grass, Block.UPDATE_CLIENTS);
                    }
                }
            }
            var v = io.github.sirschrolli.kingdomsatwar.architect.castle.CastleGenerator.generate(size, size, 11L);
            paste(level, v, new BlockPos(GX, g, GZ));
            height[0] = v.sizeY;
            sel[0] = new BlockPos(GX, g - 1, GZ);
            sel[1] = new BlockPos(GX + size - 1, g + v.sizeY - 2, GZ + size - 1);
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            // die Siedlung zwischen Vorbild und Bauplatz
            BlockPos b = new BlockPos(GX + size + 22, g, GZ + size / 2);
            BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState().setValue(TownBannerBlock.FACING, Direction.SOUTH);
            level.setBlock(b, lower, Block.UPDATE_ALL);
            level.setBlock(b.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            TownManager.placeBanner(player, level, b);
            banner[0] = b;
            town[0] = Towns.of(level.getServer()).byOwner(player.getUUID());
            ItemStack rod = new ItemStack(ModItems.SURVEYORS_ROD.get());
            rod.set(ModDataComponents.SURVEY_FIRST.get(), GlobalPos.of(level.dimension(), sel[0]));
            rod.set(ModDataComponents.SURVEY_SECOND.get(), GlobalPos.of(level.dimension(), sel[1]));
            player.getInventory().setSelectedSlot(0);
            player.getInventory().setItem(0, rod);
            player.getInventory().setItem(1, new ItemStack(net.minecraft.world.item.Items.PAPER, 12));
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }));
        steps.add(Step.until(() -> town[0] != null, 600));

        // 1. die Burg im Rahmen des Vermessungsstabs (der Spieler steht auf einem unsichtbaren Sockel hoch über dem Sand)
        double[] eye = {GX - 34.0, 0.0, GZ - 44.0};
        steps.add(Step.server(level -> {
            eye[1] = Showcase.ground + 58.0;
            BlockPos feet = BlockPos.containing(eye[0], eye[1] - 1.62, eye[2]);
            level.setBlock(feet.below(), Blocks.BARRIER.defaultBlockState(), Block.UPDATE_ALL);
        }));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, eye[0], eye[1], eye[2], GX + size * 0.5, Showcase.ground + 6.0, GZ + size * 0.42)));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.waitTicks(160));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("great_rod"));
        steps.add(Step.hud(false));

        // 2. „Bauplan zeichnen“ mit Ansicht und Grundriss der Burg (über der Burg, damit alles geladen ist)
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, GX + size * 0.5, Showcase.ground + 60.0, GZ - 30.0, GX + size * 0.5, Showcase.ground,
                GZ + size * 0.5)));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.once(minecraft -> {
            SavePlanScreen screen = new SavePlanScreen(sel[0], sel[1]);
            minecraft.gui.setScreen(screen);
            screen.setName("Burg Falkenstein");
            screen.setFront(Direction.NORTH);
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("great_save"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(GX + size / 2.0, Showcase.ground + 40.0, GZ + size / 2.0);
            int ground = PlanScan.autoGround(level, sel[0], sel[1]);
            Architect.save(player, sel[0], sel[1], "Burg Falkenstein", ground, Direction.NORTH);
            // Stab und Bauplan weglegen (sonst zeichnen sie Rahmen und Vorschau in jedes Bild)
            player.getInventory().clearContent();
        }));
        steps.add(Step.waitTicks(40));
        // 3. neben der Siedlung aufstellen; zwölf Baumeister, ein volles Lager und ein Baustellenlager
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            Town t = town[0];
            for (PlanInfo info : PlanLibrary.of(level.getServer()).byAuthor(player.getUUID())) {
                if (!info.name().equals("Burg Falkenstein")) {
                    continue;
                }
                TownManager.startCustom(level, t, info, new BlockPos(copyCenterX, Showcase.ground, centerZ), Rotation.NONE);
                // ein Baustellenlager vor der Westseite: eine Reihe Truhen voller Baustoffe und kostbarer Blöcke
                int[] units = info.costs().unitArray();
                java.util.List<ItemStack> goods = new java.util.ArrayList<>();
                Object[][] kinds = {{net.minecraft.world.item.Items.OAK_LOG, (units[0] + units[0] / 5 + 64 + 3) / 4},
                        {net.minecraft.world.item.Items.COBBLESTONE, units[1] + units[1] / 5 + 64},
                        {net.minecraft.world.item.Items.IRON_INGOT, units[2] + units[2] / 5 + 64}};
                for (Object[] kind : kinds) {
                    net.minecraft.world.item.Item item = (net.minecraft.world.item.Item) kind[0];
                    for (int left = (Integer) kind[1]; left > 0; left -= 64) {
                        goods.add(new ItemStack(item, Math.min(64, left)));
                    }
                }
                for (PlanInfo.Precious p : info.costs().precious()) {
                    for (int left = p.count(); left > 0; left -= p.item().getDefaultMaxStackSize()) {
                        goods.add(new ItemStack(p.item(), Math.min(p.item().getDefaultMaxStackSize(), left)));
                    }
                }
                int slot = 27;
                net.minecraft.world.Container chest = null;
                int row = 0;
                for (ItemStack stack : goods) {
                    if (chest == null || slot >= chest.getContainerSize()) {
                        BlockPos at = new BlockPos(copyX - 5, Showcase.ground, GZ + 6 + 2 * row++);
                        level.setBlock(at, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                        chest = level.getBlockEntity(at) instanceof net.minecraft.world.Container c ? c : null;
                        slot = 0;
                        if (chest == null) {
                            break;
                        }
                    }
                    chest.setItem(slot++, stack);
                }
            }
            for (int i = 0; i < 12; i++) {
                TownManager.spawnCitizen(level, t, Profession.BUILDER);
            }
            player.teleportTo(copyX - 30.0, Showcase.ground + 50.0, GZ - 40.0);
        }));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.command("tick rate 80"));
        double[] marks = {0.12, 0.45, 2.0};
        int[] logTicks = {0};
        for (int k = 0; k < marks.length; k++) {
            double target = marks[k];
            int index = k;
            steps.add(Step.until(() -> {
                Town t = town[0];
                if (t != null && !t.projects().isEmpty() && ++logTicks[0] % 400 == 0) {
                    Town.Project p = t.projects().getFirst();
                    var site = p.site();
                    var server = Minecraft.getInstance().getSingleplayerServer();
                    long now = server == null ? 0L : server.overworld().getGameTime();
                    io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info(
                            "[Showcase] Großbaustelle {} %, {} von {} Abschnitten, {} Baumeister, wartet {} {}, Tick {} ({} ms)",
                            Math.round(p.fraction() * 100), site == null ? 0 : site.done(), site == null ? 0 : site.count(),
                            site == null ? 0 : site.builders(now), p.waiting, p.waitingItem, now,
                            server == null ? 0 : server.getAverageTickTimeNanos() / 1_000_000);
                }
                return t != null && (t.projects().isEmpty() || target <= 1.0 && t.projects().getFirst().fraction() >= target);
            }, 400000));
            if (index == 2) {
                steps.add(Step.waitTicks(80));
            }
            steps.add(Step.command("tick freeze"));
            steps.add(Step.once(minecraft -> lookAbs(minecraft, copyX - 18.0, Showcase.ground + 62.0, GZ - 26.0, copyCenterX, Showcase.ground,
                    GZ + size * 0.5)));
            steps.add(Step.waitTicks(40));
            steps.add(Step.waitForChunks(600));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("great_site_" + index));
            if (index == 1) {
                // aus der Nähe: der Baumeister, der gerade am höchsten arbeitet (meist auf seinem Gerüst)
                steps.add(Step.once(minecraft -> {
                    io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity best = null;
                    for (var e : minecraft.level.entitiesForRendering()) {
                        if (e instanceof io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity c && c.getX() > copyX && c.getX() < copyX + size
                                && (best == null || c.getY() > best.getY())) {
                            best = c;
                        }
                    }
                    double bx = best == null ? copyX + 20.0 : best.getX();
                    double by = best == null ? Showcase.ground + 6.0 : best.getY() + 1.0;
                    double bz = best == null ? GZ + 20.0 : best.getZ();
                    lookAbs(minecraft, bx - 7.0, by + 4.0, bz - 9.0, bx, by, bz);
                }));
                steps.add(Step.waitTicks(40));
                steps.add(Step.screenshot("great_close"));
            }
            if (index == 1) {
                // die Karte der Abschnitte im Stadtfenster
                steps.add(Step.once(minecraft -> lookAbs(minecraft, banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() - 4.0,
                        banner[0].getX() + 0.5, Showcase.ground + 1.0, banner[0].getZ() + 0.5)));
                steps.add(Step.waitTicks(20));
                steps.add(Step.server(level -> {
                    ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
                    TownActions.open(player, town[0], banner[0]);
                }));
                steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof TownScreen, 200));
                steps.add(Step.waitTicks(8));
                steps.add(Step.once(minecraft -> TownShowcase.hover(minecraft, 9 + 158 + 48, 52 + 17 + 18)));
                steps.add(Step.waitTicks(10));
                steps.add(Step.screenshot("great_map"));
                steps.add(Step.once(minecraft -> {
                    Showcase.parkMouse(minecraft);
                    minecraft.gui.setScreen(null);
                }));
            }
            steps.add(Step.command("tick unfreeze"));
        }
        steps.add(Step.command("tick rate 20"));
        // 4. Vorbild und Nachbau nebeneinander
        steps.add(Step.once(minecraft -> lookAbs(minecraft, GX + size + 24.0, Showcase.ground + 92.0, GZ - 62.0, GX + size + 24.0,
                Showcase.ground, GZ + size * 0.5)));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("great_twins"));
        steps.add(Step.once(minecraft -> lookAbs(minecraft, copyCenterX + 3.5, Showcase.ground + 3.0, GZ + size * 0.24, copyCenterX,
                Showcase.ground + 12.0, GZ + size * 0.8)));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("great_inside"));
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
