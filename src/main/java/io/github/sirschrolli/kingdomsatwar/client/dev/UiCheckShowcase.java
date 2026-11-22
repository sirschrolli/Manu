package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownActions;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Nur für die Entwicklung: öffnet jedes Fenster der Mod nacheinander und macht ein Bild – im GUI-Maßstab aus
 * {@code -Dkingdomsatwar.showcaseGuiScale} (ohne Angabe 3: bei 1280×720 eine Fläche von 426×240, so klein wie bei 1440p oder 4K
 * mit automatischem Maßstab). So fällt auf, was auf kleinen Bildschirmen abgeschnitten wird oder übereinanderliegt.
 */
final class UiCheckShowcase {
    private UiCheckShowcase() {}

    static void all(Deque<Step> steps) {
        int scale = Integer.getInteger("kingdomsatwar.showcaseGuiScale", 3);
        String tag = "s" + scale + "_";
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(scale);
            minecraft.resizeGui();
        }));
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(320), List.of(), 0));
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("time set day"));
        steps.add(Step.hud(true));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(40));

        // die Fenster des Helden
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.HeroPathScreen(), tag + "heldenweg", 30);
        for (int tab = 0; tab < 3; tab++) {
            int t = tab;
            steps.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.CharacterScreen()));
            steps.add(Step.waitTicks(4));
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.hero.CharacterScreen screen) {
                    screen.devTab(t);
                }
                Showcase.parkMouse(minecraft);
            }));
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot(tag + "charakter_" + t));
        }
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.AbilityWheelScreen(), tag + "faehigkeitenrad", 20);
        // das Zauberrad nur mit einem Stab in der Hand
        steps.add(Step.server(level -> {
            var staff = new ItemStack(io.github.sirschrolli.kingdomsatwar.registry.ModItems.AETHERIT_STAFF.get());
            io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem.setSpells(staff, io.github.sirschrolli.kingdomsatwar.magic.KnownSpells.DEFAULT
                    .withLevel(io.github.sirschrolli.kingdomsatwar.magic.Spell.FIRE, 2)
                    .withLevel(io.github.sirschrolli.kingdomsatwar.magic.Spell.ICE, 1)
                    .withLevel(io.github.sirschrolli.kingdomsatwar.magic.Spell.NATURE, 1));
            first(level).setItemInHand(InteractionHand.MAIN_HAND, staff);
        }));
        steps.add(Step.waitTicks(10));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.magic.SpellWheelScreen(), tag + "zauberrad", 20);

        // Heer (Kriegstisch und Söldnerbrett stehen wirklich neben dem Spieler, sonst gehen die Fenster gleich wieder zu), Bauplan, Fischen
        BlockPos here = new BlockPos(0, 64, 0);
        BlockPos[] army = new BlockPos[2];
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            army[0] = player.blockPosition().offset(2, 0, 1);
            army[1] = player.blockPosition().offset(-2, 0, 1);
            level.setBlockAndUpdate(army[0], ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState());
            level.setBlockAndUpdate(army[1], ModBlocks.MERCENARY_BOARD.get().defaultBlockState());
        }));
        steps.add(Step.until(() -> army[1] != null, 40));
        steps.add(Step.waitTicks(10));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen(army[0], 32, 4, true), tag + "kriegstisch", 15);
        // erst eine leere Tafel (wer schon wartet, geht)
        steps.add(Step.server(level -> {
            // der Tag ist schon "versorgt" – sonst kämen gleich wieder welche
            if (level.getBlockEntity(army[1]) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.MercenaryBoardBlockEntity entity) {
                entity.refreshIfDue(level);
            }
            level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class,
                    new net.minecraft.world.phys.AABB(army[1]).inflate(24), io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity::isMercenary)
                    .forEach(net.minecraft.world.entity.Entity::discard);
        }));
        steps.add(Step.waitTicks(5));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.army.MercenaryScreen(army[1], 32, "Lindenau"), tag + "soeldner_leer",
                15);
        // jetzt warten Söldner an der Tafel
        steps.add(Step.server(level -> {
            if (level.getBlockEntity(army[1]) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.MercenaryBoardBlockEntity entity) {
                entity.setup(java.util.List.of(army[1].offset(-2, 0, 2), army[1].offset(-3, 0, 1), army[1].offset(-1, 0, 3),
                        army[1].offset(-4, 0, 2)), "Lindenau");
                entity.refill(level, false);
            }
        }));
        steps.add(Step.waitTicks(20));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.army.MercenaryScreen(army[1], 32, "Lindenau"), tag + "soeldner", 15);
        // ein volles Feldlager: verwundet, hungrig, einer auf dem Schlachtzug, verschiedene Ränge und Stufen
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType[] types = {
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.SWORDSMAN,
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.KNIGHT,
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.ARCHER,
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.CAPTAIN,
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.SPEARMAN,
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.SHIELDBEARER};
            for (int i = 0; i < types.length; i++) {
                var soldier = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SOLDIERS.get(types[i]).get().create(level,
                        net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (soldier == null) {
                    continue;
                }
                soldier.equip();
                soldier.setVariant(Math.min(i % 3, types[i].variants() - 1));
                soldier.giveRandomName();
                soldier.setRank(i % 4 == 3 ? 2 : i % 3);
                soldier.setCombatExperience(i * 180);
                soldier.setNoAi(true);
                soldier.setOwner(player);
                soldier.snapTo(player.getX() + 4.0 + (i % 3) * 1.5, player.getY(), player.getZ() + 4.0 + (i / 3) * 1.5, 180.0f, 0.0f);
                soldier.setMode(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.MODE_HOLD, soldier.blockPosition());
                soldier.setHealth(soldier.getMaxHealth() * (i == 0 ? 0.3f : i == 2 ? 0.6f : 1.0f));
                soldier.setRations(i == 1 ? 0 : i == 4 ? 1 : 5);
                soldier.setHungry(i == 1);
                soldier.setCampaign(i == 3);
                level.addFreshEntity(soldier);
            }
        }));
        steps.add(Step.waitTicks(20));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen(army[0], 32, 10, true), tag + "kriegstisch_voll",
                20);
        // weiter unten in der Liste (gerollt), mit dem Ausbilden eines anderen Soldaten
        steps.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen(army[0], 32, 10, true)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen screen) {
                screen.mouseScrolled(0, 0, 0, -1);
                screen.mouseScrolled(0, 0, 0, -1);
            }
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot(tag + "kriegstisch_gerollt"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.waitTicks(5));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.architect.SavePlanScreen(here, here.offset(8, 6, 10)), tag + "bauplan", 15);
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.fishing.FishBookScreen(new io.github.sirschrolli.kingdomsatwar.fishing.FishLog()),
                tag + "fischbuch", 15);
        steps.add(Step.server(level -> first(level).setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(io.github.sirschrolli.kingdomsatwar.registry.FishingItems.FISH_BAG.get()))));
        steps.add(Step.waitTicks(10));
        shot(steps, minecraft -> new io.github.sirschrolli.kingdomsatwar.client.fishing.FishBagScreen(InteractionHand.MAIN_HAND), tag + "fischbeutel", 15);
        steps.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.fishing.FishingScreen(0,
                io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.BROOK_TROUT, io.github.sirschrolli.kingdomsatwar.fishing.Medium.WATER, true, false, 0.0f)));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(70));
        steps.add(Step.screenshot(tag + "angelspiel"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.waitTicks(5));

        // Chronik (vom Server geöffnet)
        steps.add(Step.server(level -> io.github.sirschrolli.kingdomsatwar.story.Story.openChronicle(first(level))));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.story.ChronicleScreen, 100));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot(tag + "chronik"));
        steps.add(Step.screen(minecraft -> null));

        // Handel mit einem Fischer
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            var fisher = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.VILLAGER.get().create(level,
                    net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            fisher.setup(io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession.FISHER, 0, "Ole", player.blockPosition(), "Seedorf",
                    null, null, 7L);
            fisher.snapTo(player.getX() + 0.5, player.getY(), player.getZ() + 2.2, 180.0f, 0.0f);
            fisher.setNoAi(true);
            level.addFreshEntity(fisher);
            io.github.sirschrolli.kingdomsatwar.economy.Coins.give(player, 2 * 81 + 6 * 9 + 5);
            io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.open(player, fisher);
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TradeScreen, 100));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot(tag + "handel"));
        steps.add(Step.screen(minecraft -> null));

        // Stadtfenster (mit eigenem Banner) – alle Reiter
        BlockPos[] banner = new BlockPos[1];
        Town[] town = new Town[1];
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            BlockPos pos = player.blockPosition().offset(4, 0, -4);
            BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState().setValue(TownBannerBlock.FACING, Direction.SOUTH);
            level.setBlock(pos, lower, Block.UPDATE_ALL);
            level.setBlock(pos.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            TownManager.placeBanner(player, level, pos);
            banner[0] = pos;
            town[0] = Towns.of(level.getServer()).byOwner(player.getUUID());
        }));
        steps.add(Step.until(() -> town[0] != null, 100));
        for (int tab = 0; tab < 6; tab++) {
            int t = tab;
            steps.add(Step.server(level -> {
                if (town[0] != null) {
                    TownActions.open(first(level), town[0], banner[0]);
                }
            }));
            steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TownScreen, 100));
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TownScreen screen) {
                    screen.selectTab(t);
                }
                Showcase.parkMouse(minecraft);
            }));
            steps.add(Step.waitTicks(12));
            steps.add(Step.screenshot(tag + "stadt_" + t));
            steps.add(Step.screen(minecraft -> null));
        }
        // der Rat: ein Freund steht am Banner – erst als Anwärter, dann als Mitregent
        ServerPlayer[] friend = new ServerPlayer[1];
        steps.add(Step.server(level -> friend[0] = guest(level, "Freund", banner[0].offset(2, 0, 2))));
        for (int round = 0; round < 2; round++) {
            boolean appointed = round == 1;
            if (appointed) {
                steps.add(Step.server(level -> {
                    if (town[0] != null && friend[0] != null) {
                        TownManager.appoint(first(level), town[0], friend[0]);
                    }
                }));
            }
            steps.add(Step.server(level -> {
                if (town[0] != null) {
                    TownActions.open(first(level), town[0], banner[0]);
                }
            }));
            steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TownScreen, 100));
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TownScreen screen) {
                    screen.selectTab(4);
                }
                Showcase.parkMouse(minecraft);
            }));
            steps.add(Step.waitTicks(12));
            steps.add(Step.screenshot(tag + (appointed ? "rat_mitregent" : "rat_anwaerter")));
            if (appointed) {
                // seit 1.12: Tooltips im Stil des Fensters – die Maus auf dem ✘ des Mitregenten
                steps.add(Step.once(minecraft -> hover(minecraft, (minecraft.getWindow().getGuiScaledWidth() - 320) / 2 + 150,
                        (minecraft.getWindow().getGuiScaledHeight() - 210) / 2 + 94)));
                steps.add(Step.waitTicks(6));
                steps.add(Step.screenshot(tag + "rat_tooltip"));
                steps.add(Step.once(Showcase::parkMouse));
            }
            steps.add(Step.screen(minecraft -> null));
        }
        // … und umgekehrt: der Freund gründet weiter draußen seine eigene Siedlung und ernennt uns – so sieht ein Mitregent den Rat
        BlockPos[] far = new BlockPos[1];
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            BlockPos column = player.blockPosition().offset(96, 0, 0);
            int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
            far[0] = new BlockPos(column.getX(), top, column.getZ());
            player.teleportTo(level, far[0].getX() + 0.5, far[0].getY(), far[0].getZ() + 3.5, java.util.Set.of(), 180.0f, 10.0f, true);
            if (friend[0] != null) {
                friend[0].teleportTo(level, far[0].getX() + 2.5, far[0].getY(), far[0].getZ() + 2.5, java.util.Set.of(), 160.0f, 0.0f, true);
            }
        }));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            if (friend[0] == null || far[0] == null) {
                return;
            }
            BlockPos pos = new BlockPos(far[0].getX(), level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    far[0].getX(), far[0].getZ()), far[0].getZ());
            BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState().setValue(TownBannerBlock.FACING, Direction.SOUTH);
            level.setBlock(pos, lower, Block.UPDATE_ALL);
            level.setBlock(pos.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            TownManager.placeBanner(friend[0], level, pos);
            Town theirs = Towns.of(level.getServer()).byOwner(friend[0].getUUID());
            if (theirs != null) {
                TownManager.appoint(friend[0], theirs, first(level));
                TownActions.open(first(level), theirs, pos);
            }
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TownScreen, 100));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TownScreen screen) {
                screen.selectTab(4);
            }
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot(tag + "rat_als_mitregent"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.server(level -> {
            if (friend[0] != null) {
                level.getServer().getPlayerList().remove(friend[0]);
            }
        }));

        // Zaubertisch (ein echtes Menü vom Server)
        steps.add(Step.server(level -> {
            ServerPlayer player = first(level);
            BlockPos table = player.blockPosition().offset(-3, 0, 2);
            level.setBlockAndUpdate(table, ModBlocks.MAGIC_TABLE.get().defaultBlockState());
            level.getBlockState(table).useWithoutItem(level, player, new net.minecraft.world.phys.BlockHitResult(
                    net.minecraft.world.phys.Vec3.atCenterOf(table), Direction.UP, table, false));
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot(tag + "zaubertisch"));
        steps.add(Step.screen(minecraft -> null));

        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(2);
            minecraft.resizeGui();
        }));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    /** Die Maus an diese Stelle (GUI-Koordinaten) – für Tooltips in Vorführungen. */
    private static void hover(Minecraft minecraft, int guiX, int guiY) {
        double scale = minecraft.getWindow().getGuiScale();
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? guiX * scale : guiY * scale);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ServerPlayer first(ServerLevel level) {
        return level.getServer().getPlayerList().getPlayers().getFirst();
    }

    /** Ein zweiter Spieler für die Vorführung (siehe {@link ShowcaseGuest}). */
    private static ServerPlayer guest(ServerLevel level, String name, BlockPos pos) {
        return ShowcaseGuest.join(level, name, net.minecraft.world.phys.Vec3.atBottomCenterOf(pos), 210.0f, ItemStack.EMPTY,
                net.minecraft.world.level.GameType.SURVIVAL);
    }

    private static void shot(Deque<Step> steps, Function<Minecraft, Screen> screen, String name, int wait) {
        steps.add(Step.screen(screen));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(wait));
        steps.add(Step.screenshot(name));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.waitTicks(3));
    }

    @SuppressWarnings("unused")
    private static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }
}
