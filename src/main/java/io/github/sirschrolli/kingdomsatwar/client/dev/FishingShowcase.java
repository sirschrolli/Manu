package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.client.fishing.FishBookScreen;
import io.github.sirschrolli.kingdomsatwar.client.fishing.FishingScreen;
import io.github.sirschrolli.kingdomsatwar.fishing.FishLog;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.fishing.HookAccess;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import io.github.sirschrolli.kingdomsatwar.registry.FishingItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.Deque;
import java.util.Set;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

/**
 * Das Angeln ({@code fishing}): der Drill in Wasser, Lava und Leere (gestellt und echt – vom Biss bis zum Fang), das
 * Fischbuch, die Fische im Gepäck, der Schwimmer auf der Lava und in der Leere unter einer Insel des Ends.
 */
final class FishingShowcase {
    private static final Logger LOGGER = LogUtils.getLogger();

    private FishingShowcase() {}

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    /** Wirft den Haken des Spielers an eine Stelle (als hätte er ihn dorthin geworfen). */
    private static Step cast(double dx, double dy, double dz) {
        return Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            if (player.fishing != null) {
                player.fishing.discard();
            }
            FishingHook hook = new FishingHook(player, level, 0, 0);
            hook.snapTo(player.getX() + dx, player.getY() + dy, player.getZ() + dz, 0.0f, 0.0f);
            hook.setDeltaMovement(0.0, -0.05, 0.0);
            level.addFreshEntity(hook);
        });
    }

    /** Lässt einen Fisch beißen und zieht an – wie ein Rechtsklick im richtigen Augenblick. */
    private static Step biteAndReel() {
        return Step.server(any -> {
            ServerPlayer player = player(any);
            if (player.fishing != null) {
                ((HookAccess) player.fishing).kingdomsatwar$forceBite();
                player.fishing.retrieve(player.getMainHandItem());
            }
        });
    }

    /** Wartet auf den echten Drill, lässt ihn von selbst laufen und nimmt Bilder mittendrin und vom Abschluss auf. */
    private static void realFight(Deque<Step> steps, String name) {
        steps.add(Step.until(() -> net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof FishingScreen, 60));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                LOGGER.info("[Showcase] Drill offen ({}): {}", name, screen.species().id());
                screen.devAuto(true);
            } else {
                LOGGER.error("[Showcase] Kein Drill ({}) – nur Kram am Haken oder der Biss kam nicht an", name);
            }
        }));
        steps.add(Step.waitTicks(45));
        steps.add(Step.screenshot(name + "_drill"));
        steps.add(Step.until(() -> !(net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof FishingScreen screen)
                || (screen.outcome() != 0 && (screen.outcome() < 0 || screen.confirmed())), 700));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                LOGGER.info("[Showcase] Drill beendet ({}): {} – {}", name, screen.species().id(), screen.outcome() > 0 ? "gefangen" : "entkommen");
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot(name + "_fang"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.waitTicks(10));
    }

    private static Step posed(FishSpecies species, Medium medium, boolean known, float zone, float fish, float progress, float chest, float chestProgress) {
        return Step.once(minecraft -> {
            FishingScreen screen = new FishingScreen(0, species, medium, known, chest >= 0.0f, 0.0f);
            minecraft.gui.setScreen(screen);
            screen.devPose(zone, fish, progress, chest, chestProgress);
        });
    }

    static void fishing(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            // ein Teich und daneben ein Lavabecken, in Stein gefasst; der Angler steht davor
            for (int x = -9; x <= 9; x++) {
                for (int z = -2; z <= 14; z++) {
                    for (int y = -3; y <= 6; y++) {
                        BlockPos pos = top.offset(x, y, z);
                        boolean basin = z >= 3 && z <= 12 && Math.abs(x) <= 7;
                        if (y > 0) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        } else if (basin && y >= -2 && x != 0) {
                            level.setBlockAndUpdate(pos, (x < 0 ? Blocks.WATER : Blocks.LAVA).defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(pos, (basin || y < 0 ? Blocks.STONE_BRICKS : Blocks.GRASS_BLOCK).defaultBlockState());
                        }
                    }
                }
            }
            player.teleportTo(level, top.getX() - 3.5, top.getY() + 1.0, top.getZ() + 1.5, Set.of(), 0.0f, 24.0f, true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(FishingItems.BAIT_WORM.get(), 16));
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));

        // --- gestellte Bilder des Drills: Wasser, Lava, Leere ---
        steps.add(posed(FishSpecies.PIKE, Medium.WATER, true, 0.38f, 0.5f, 0.62f, 0.86f, 0.4f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("angeln_drill_wasser"));
        steps.add(posed(FishSpecies.INFERNO_KING, Medium.LAVA, false, 0.12f, 0.28f, 0.34f, -1.0f, 0.0f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("angeln_drill_lava"));
        steps.add(posed(FishSpecies.DRAGON_KOI, Medium.VOID, true, 0.6f, 0.74f, 0.88f, 0.2f, 0.0f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("angeln_drill_leere"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                screen.devCaught(142, 2, true);
            }
        }));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("angeln_fang_band"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- echt: Wurf in den Teich, Biss, Drill, Fang ---
        steps.add(cast(0.0, 0.2, 6.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("angeln_schwimmer_wasser"));
        steps.add(biteAndReel());
        realFight(steps, "angeln_echt_wasser");

        // --- der Schwimmer auf der Lava, dann der Drill dort ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.teleportTo(player.level(), player.getX() + 7.0, player.getY(), player.getZ(), Set.of(), 0.0f, 24.0f, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(cast(0.0, 0.4, 5.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.once(minecraft -> {
            if (minecraft.player.fishing != null) {
                LOGGER.info("[Showcase] Schwimmer auf Lava: y={} (Spieler {}), brennt: {}", minecraft.player.fishing.getY(), minecraft.player.getY(),
                        minecraft.player.fishing.isOnFire());
            } else {
                LOGGER.error("[Showcase] Der Schwimmer ist in der Lava verschwunden");
            }
        }));
        steps.add(Step.screenshot("angeln_schwimmer_lava"));
        steps.add(biteAndReel());
        realFight(steps, "angeln_echt_lava");

        // --- das Fischbuch ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            FishLog log = new FishLog();
            int i = 0;
            for (FishSpecies species : FishSpecies.values()) {
                // etwa zwei Drittel gefangen, die Legenden bis auf eine noch nicht
                boolean caught = species.rarity() == FishSpecies.Rarity.LEGENDARY ? species == FishSpecies.GOLDSCALE : i % 3 != 2;
                if (caught) {
                    for (int n = 0; n < 1 + (i * 7) % 9; n++) {
                        log.record(species, species.rollSize(player.getRandom(), n == 0));
                    }
                }
                i++;
            }
            for (int n = 0; n < 7; n++) {
                log.addTreasure();
            }
            player.setData(ModAttachments.FISH_LOG, log);
        }));
        steps.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(
                io.github.sirschrolli.kingdomsatwar.network.RequestFishBookPayload.INSTANCE)));
        steps.add(Step.until(() -> net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof FishBookScreen, 60));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishBookScreen book) {
                book.open(FishSpecies.Chapter.RIVERS, FishSpecies.PIKE);
            } else {
                LOGGER.error("[Showcase] Das Fischbuch hat sich nicht geöffnet");
            }
        }));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("fischbuch_fluesse"));
        Object[][] pages = {{FishSpecies.Chapter.SEA, FishSpecies.SWORDFISH, "fischbuch_meer"}, {FishSpecies.Chapter.DEEP, FishSpecies.ANGLERFISH, "fischbuch_tiefsee"},
                {FishSpecies.Chapter.NETHER, FishSpecies.WARP_FIN, "fischbuch_nether"}, {FishSpecies.Chapter.END, FishSpecies.VOID_RAY, "fischbuch_end"},
                {FishSpecies.Chapter.LEGENDS, FishSpecies.GOLDSCALE, "fischbuch_legenden"}, {FishSpecies.Chapter.LEGENDS, FishSpecies.DRAGON_KOI, "fischbuch_ungefangen"}};
        for (Object[] page : pages) {
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof FishBookScreen book) {
                    book.open((FishSpecies.Chapter) page[0], (FishSpecies) page[1]);
                }
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot((String) page[2]));
        }
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- die Fische im Gepäck (ihre Bilder als Gegenstände) ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getInventory().clearContent();
            int slot = 0;
            for (var fish : FishingItems.fishes()) {
                if (slot < 36) {
                    player.getInventory().setItem(slot++, new ItemStack(fish.get(), 1 + slot % 5));
                }
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("angeln_fische_gepaeck_1"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getInventory().clearContent();
            int slot = 0;
            int index = 0;
            for (var fish : FishingItems.fishes()) {
                if (index++ >= 36 && slot < 36) {
                    player.getInventory().setItem(slot++, new ItemStack(fish.get()));
                }
            }
            player.getInventory().setItem(slot++, new ItemStack(FishingItems.COOKED_FILLET.get(), 8));
            player.getInventory().setItem(slot++, new ItemStack(FishingItems.BAIT_WORM.get(), 16));
            player.getInventory().setItem(slot, new ItemStack(FishingItems.BAIT_GLOW.get(), 16));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("angeln_fische_gepaeck_2"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        theVoid(steps);
        steps.add(Step.quit());
    }

    /** Nur die Leere ({@code fishing_void}). */
    static void voidOnly(Deque<Step> steps) {
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        theVoid(steps);
        steps.add(Step.quit());
    }

    /** Die Leere: eine Plattform im End, weit draußen zwischen den Inseln. */
    private static void theVoid(Deque<Step> steps) {
        steps.add(Step.command("execute in minecraft:the_end run forceload add 296 296 304 304"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel end = any.getServer().getLevel(Level.END);
            for (int x = 296; x <= 304; x++) {
                for (int z = 296; z <= 304; z++) {
                    end.setBlockAndUpdate(new BlockPos(x, 80, z), Blocks.END_STONE.defaultBlockState());
                    if ((x + z) % 3 == 0 && z < 303) {
                        end.setBlockAndUpdate(new BlockPos(x, 79, z), Blocks.END_STONE.defaultBlockState());
                    }
                }
            }
            player.getInventory().clearContent();
            player.teleportTo(end, 300.5, 81.0, 303.5, Set.of(), 0.0f, 38.0f, true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(300));
        steps.add(cast(0.0, 1.0, 5.0));
        steps.add(Step.waitTicks(70));
        steps.add(Step.once(minecraft -> {
            if (minecraft.player.fishing != null) {
                LOGGER.info("[Showcase] Schwimmer in der Leere: y={} (Spieler {}), Geschwindigkeit {}", minecraft.player.fishing.getY(), minecraft.player.getY(),
                        minecraft.player.fishing.getDeltaMovement().y);
            } else {
                LOGGER.error("[Showcase] Der Schwimmer ist in die Leere gefallen");
            }
        }));
        steps.add(Step.screenshot("angeln_schwimmer_leere"));
        steps.add(biteAndReel());
        realFight(steps, "angeln_echt_leere");
    }
}
