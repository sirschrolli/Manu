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

    private static FishSpecies byId(String id) {
        FishSpecies species = FishSpecies.byId(id);
        if (species == null) {
            LOGGER.error("[Showcase] Unbekannte Art: {}", id);
            return FishSpecies.PIKE;
        }
        return species;
    }

    /**
     * Die zweite Welle der Biom-Fische ({@code newfish}, seit 0.83): neue Arten im Drill (der nur noch mit der Maus gespielt wird),
     * das Band zu einer neuen Art, ein echter Drill im Teich, das Fischbuch mit einem Biom je Kapitel (zwanzig, zwölf oder zehn
     * Arten) und neue Fische im Gepäck mit ihrem Hinweis.
     */
    static void newFish(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        int guiScale = Integer.getInteger("kingdomsatwar.showcaseGuiScale", 0);
        if (guiScale > 0) {
            steps.add(Step.once(minecraft -> {
                minecraft.options.guiScale().set(guiScale);
                minecraft.resizeGui();
            }));
        }
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
            for (int x = -9; x <= 9; x++) {
                for (int z = -2; z <= 14; z++) {
                    for (int y = -3; y <= 6; y++) {
                        BlockPos pos = top.offset(x, y, z);
                        boolean pond = z >= 4 && z <= 13 && Math.abs(x) <= 7;
                        if (y > 0) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        } else if (pond && y >= -2) {
                            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(pos, (y < 0 ? Blocks.STONE : z <= 3 && Math.abs(x) <= 3 ? Blocks.SPRUCE_PLANKS : Blocks.GRASS_BLOCK).defaultBlockState());
                        }
                    }
                }
            }
            player.teleportTo(level, top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 1.5, Set.of(), 0.0f, 18.0f, true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));

        // --- gestellte Bilder des Drills mit neuen Arten ---
        steps.add(posed(byId("tropic_mantis_shrimp"), Medium.WATER, true, 0.4f, 0.55f, 0.7f, 0.12f, 0.55f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("neu_drill_wasser"));
        steps.add(posed(byId("smoke_hammerhead"), Medium.LAVA, false, 0.2f, 0.33f, 0.42f, -1.0f, 0.0f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("neu_drill_lava"));
        steps.add(posed(byId("glitter_wyrm"), Medium.VOID, true, 0.56f, 0.7f, 0.86f, -1.0f, 0.0f));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("neu_drill_leere"));
        steps.add(posed(byId("jewel_hammerhead"), Medium.WATER, true, 0.3f, 0.42f, 0.95f, -1.0f, 0.0f));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                screen.devCaught(388, 2, true);
            }
        }));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("neu_fang_band"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- die Maus im echten Drill: Jeder Klick gibt der Zone einen Schubs (geprüft an einem offenen, laufenden Drill) ---
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(new FishingScreen(0, byId("reef_triggerfish"), Medium.WATER, true, false, 0.0f))));
        steps.add(Step.waitTicks(16));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                float before = screen.devFight().zonePos();
                for (int i = 0; i < 4; i++) {
                    screen.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(0.0, 0.0, new net.minecraft.client.input.MouseButtonInfo(0, 0)), false);
                    screen.mouseReleased(new net.minecraft.client.input.MouseButtonEvent(0.0, 0.0, new net.minecraft.client.input.MouseButtonInfo(0, 0)));
                }
                LOGGER.info("[Showcase] Vier Klicks: Zone bei {}, Schwung {}, Klicks gezählt {}", before, screen.devFight().zoneVel(), screen.devFight().kicks());
            }
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                LOGGER.info("[Showcase] Nach den Klicks: Zone bei {} (gestiegen: {})", screen.devFight().zonePos(), screen.devFight().zonePos() > 0.02f);
            }
        }));
        steps.add(Step.screenshot("neu_klick_drill"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.waitTicks(10));

        // --- echt: Wurf in den Teich, Biss, Drill, Fang (bis zu drei Würfe, falls Kram am Haken hängt) ---
        for (int attempt = 0; attempt < 3; attempt++) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                if (io.github.sirschrolli.kingdomsatwar.fishing.Fishing.fighting(player) == null) {
                    ServerLevel level = player.level();
                    if (player.fishing != null) {
                        player.fishing.discard();
                    }
                    FishingHook hook = new FishingHook(player, level, 0, 0);
                    hook.snapTo(player.getX(), player.getY() + 0.2, player.getZ() + 6.0, 0.0f, 0.0f);
                    hook.setDeltaMovement(0.0, -0.05, 0.0);
                    level.addFreshEntity(hook);
                }
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                if (io.github.sirschrolli.kingdomsatwar.fishing.Fishing.fighting(player) == null && player.fishing != null) {
                    ((HookAccess) player.fishing).kingdomsatwar$forceBite();
                    player.fishing.retrieve(player.getMainHandItem());
                }
            }));
            steps.add(Step.waitTicks(15));
        }
        steps.add(Step.until(() -> net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof FishingScreen, 60));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                LOGGER.info("[Showcase] Drill offen: {}", screen.species().id());
                screen.devAuto(true);
            } else {
                LOGGER.error("[Showcase] Kein Drill – nach drei Würfen nur Kram am Haken");
            }
        }));
        steps.add(Step.waitTicks(45));
        steps.add(Step.screenshot("neu_echt_drill"));
        steps.add(Step.until(() -> !(net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof FishingScreen screen)
                || (screen.outcome() != 0 && (screen.outcome() < 0 || screen.confirmed())), 700));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishingScreen screen) {
                LOGGER.info("[Showcase] Drill beendet: {} – {}", screen.species().id(), screen.outcome() > 0 ? "gefangen" : "entkommen");
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("neu_echt_fang"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.waitTicks(10));

        // --- das Fischbuch: je Kapitel ein Biom mit seinen neuen Arten ---
        FishLog log = wellFilledLog();
        String[] shown = {"tropic_mantis_shrimp", "magma_scallop", "icedepth_lindworm", "bloodred_triggerfish", "glitter_wyrm", "rose_clownfish",
                "jewel_hammerhead", "forge_mantis_shrimp", "coral_nudibranch", "reef_triggerfish", "ember_sea_urchin", "volcano_lindworm"};
        for (String id : shown) {
            FishSpecies species = byId(id);
            log.record(species, species.minCm() + (species.maxCm() - species.minCm()) * 7 / 10);
        }
        steps.add(Step.once(minecraft -> hover(minecraft, 400, 300)));
        steps.add(Step.screen(minecraft -> new FishBookScreen(log)));
        record Page(String name, String species) {}
        for (Page page : new Page[] {new Page("neu_buch_korallenkueste", "tropic_mantis_shrimp"), new Page("neu_buch_aschelande", "volcano_lindworm"),
                new Page("neu_buch_tiefsee", "icedepth_lindworm"), new Page("neu_buch_karmesinwald", "bloodred_triggerfish"),
                new Page("neu_buch_sternenstaub", "glitter_wyrm"), new Page("neu_buch_feenwiesen", "rose_clownfish"),
                new Page("neu_buch_kristallschluchten", "jewel_hammerhead"), new Page("neu_buch_glutfaelle", "forge_mantis_shrimp")}) {
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof FishBookScreen book) {
                    FishSpecies species = byId(page.species());
                    book.open(species.chapter(), species);
                }
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot(page.name()));
        }
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- neue Fische im Gepäck: lauter Arten der zweiten Welle, dazu ihr Hinweis ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getInventory().clearContent();
            java.util.List<FishSpecies> second = FishSpecies.values().stream().filter(sp -> sp.ordinal() >= 1000).toList();
            for (int slot = 0; slot < 36; slot++) {
                FishSpecies species = second.get((slot * 17 + 3) % second.size());
                player.getInventory().setItem(slot, new ItemStack(species.item(), 1 + slot % 4));
            }
            player.getInventory().setItem(13, new ItemStack(byId("tropic_mantis_shrimp").item()));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 4 + 8, -83 + 84 + 18 + 8)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("neu_gepaeck"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    /** Mauszeiger auf eine Stelle des Bildschirms (in Oberflächen-Koordinaten, von der Mitte aus gerechnet). */
    private static void hover(net.minecraft.client.Minecraft minecraft, int fromCenterX, int fromCenterY) {
        double scale = minecraft.getWindow().getGuiScale();
        double x = (minecraft.getWindow().getGuiScaledWidth() / 2.0 + fromCenterX) * scale;
        double y = (minecraft.getWindow().getGuiScaledHeight() / 2.0 + fromCenterY) * scale;
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? x : y);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Ein Fischbuch, in dem schon viel steht: alle Allerweltsfische und – je Biom verschieden viele – Arten der Biome. */
    private static FishLog wellFilledLog() {
        FishLog log = new FishLog();
        int n = 0;
        for (FishSpecies species : FishSpecies.values()) {
            boolean local = species.biome() != null;
            int biome = local ? Math.floorMod(species.biome().hashCode(), 7) : 0;
            boolean caught = local ? (biome <= 1 || (n * 31 + biome) % 10 < 3 + biome)
                    && (species.rarity() != FishSpecies.Rarity.EPIC || biome <= 2) : species.rarity() != FishSpecies.Rarity.LEGENDARY || n % 3 == 0;
            if (caught) {
                log.record(species, species.minCm() + (species.maxCm() - species.minCm()) * (3 + n % 6) / 10);
                for (int i = 0; i < n % 4; i++) {
                    log.record(species, species.minCm());
                }
            }
            n++;
        }
        return log;
    }

    /**
     * Fischbeutel und Fischbuch ({@code fishbag}, seit 0.81): das Fenster des Beutels (gefüllt, mit Hinweis unter der Maus, leer),
     * der Beutel in der Hand, das Fischbuch mit den Gewässern eines Kapitels und den Arten einzelner Biome, der Fischer im Dorf
     * mit Erweiterung und Aufkauf, und Fische der Biome im Gepäck.
     */
    static void bagAndBook(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        // die Oberfläche eine Stufe größer (Bücher und Fenster passen bei 1280 x 720 auch so): Die Bilder werden schärfer lesbar
        int guiScale = Integer.getInteger("kingdomsatwar.showcaseGuiScale", 0);
        if (guiScale > 0) {
            steps.add(Step.once(minecraft -> {
                minecraft.options.guiScale().set(guiScale);
                minecraft.resizeGui();
            }));
        }
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
            // ein Steg am Teich
            for (int x = -9; x <= 9; x++) {
                for (int z = -2; z <= 14; z++) {
                    for (int y = -3; y <= 6; y++) {
                        BlockPos pos = top.offset(x, y, z);
                        boolean pond = z >= 4 && z <= 13 && Math.abs(x) <= 7;
                        if (y > 0) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        } else if (pond && y >= -2) {
                            level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(pos, (y < 0 ? Blocks.STONE : z <= 3 && Math.abs(x) <= 3 ? Blocks.SPRUCE_PLANKS : Blocks.GRASS_BLOCK).defaultBlockState());
                        }
                    }
                }
            }
            player.teleportTo(level, top.getX() + 0.5, top.getY() + 1.0, top.getZ() + 1.5, Set.of(), 0.0f, 18.0f, true);
            // ein gut gefüllter Beutel der Stufe 7: Allerweltsfische, Fische vieler Biome, zwei Legenden
            java.util.Map<String, Integer> fish = new java.util.HashMap<>();
            int n = 0;
            for (FishSpecies species : FishSpecies.values()) {
                boolean take = species.biome() == null ? n % 3 == 0 : n % 23 == 0;
                if (take && species.rarity() != FishSpecies.Rarity.LEGENDARY) {
                    fish.put(species.id(), 1 + (n * 7) % (species.rarity() == FishSpecies.Rarity.COMMON ? 23 : 5));
                }
                n++;
            }
            fish.put(FishSpecies.OLD_ONE.id(), 1);
            fish.put(FishSpecies.STAR_WHALE.id(), 1);
            ItemStack bag = new ItemStack(FishingItems.FISH_BAG.get());
            bag.set(io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents.FISH_BAG.get(),
                    new io.github.sirschrolli.kingdomsatwar.fishing.FishBag(6, fish));
            player.setItemInHand(InteractionHand.MAIN_HAND, bag);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.FISHING_ROD));
            io.github.sirschrolli.kingdomsatwar.economy.Coins.give(player, 3 * 81 + 5 * 9 + 4);
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.screenshot("beutel_hand"));

        // --- das Fenster des Beutels ---
        steps.add(Step.server(any -> io.github.sirschrolli.kingdomsatwar.fishing.FishBag.open(player(any), InteractionHand.MAIN_HAND)));
        steps.add(Step.until(() -> net.minecraft.client.Minecraft.getInstance().gui.screen()
                instanceof io.github.sirschrolli.kingdomsatwar.client.fishing.FishBagScreen, 60));
        steps.add(Step.waitTicks(4));
        steps.add(Step.once(minecraft -> hover(minecraft, 300, 200)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("beutel_fenster"));
        steps.add(Step.once(minecraft -> hover(minecraft, -98 + 10 + 22 * 2 + 11, -93 + 49 + 22 + 11)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("beutel_hinweis"));
        // ein Stapel heraus, dann alles (soweit das Inventar reicht)
        steps.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(
                new io.github.sirschrolli.kingdomsatwar.network.FishBagTakePayload(true, FishSpecies.BROOK_TROUT.ordinal() + 1, 0))));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> LOGGER.info("[Showcase] Nach dem Entnehmen: {} Bachforellen im Inventar, Beutel {}",
                minecraft.player.getInventory().countItem(FishSpecies.BROOK_TROUT.item()),
                io.github.sirschrolli.kingdomsatwar.fishing.FishBag.of(minecraft.player.getMainHandItem()).total())));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- Fische der Biome im Gepäck: jeder aus einem anderen Biom ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ItemStack bag = player.getMainHandItem().copy();
            player.getInventory().clearContent();
            int slot = 9;
            int k = 0;
            for (String biome : FishSpecies.biomes()) {
                java.util.List<FishSpecies> local = FishSpecies.ofBiome(biome);
                if (slot < 36 && k % 4 == 0) {
                    player.getInventory().setItem(slot++, new ItemStack(local.get((k / 4) % local.size()).item(), 1 + k % 3));
                }
                k++;
            }
            java.util.List<FishSpecies> shadow = FishSpecies.ofBiome("kingdomsatwar:shadow_forest");
            for (int i = 0; i < 8; i++) {
                player.getInventory().setItem(i, new ItemStack(shadow.get(i).item()));
            }
            player.getInventory().setItem(8, bag);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 3 + 8, -83 + 84 + 8)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("fische_gepaeck_biome"));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 18 * 8 + 8, -83 + 142 + 8)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("beutel_gegenstand"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- das Fischbuch: Gewässer eines Kapitels, Arten eines Bioms ---
        FishLog log = wellFilledLog();
        steps.add(Step.once(minecraft -> hover(minecraft, 150, 100)));
        steps.add(Step.screen(minecraft -> {
            FishBookScreen book = new FishBookScreen(log);
            return book;
        }));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishBookScreen book) {
                book.open(FishSpecies.Chapter.RIVERS, null);
            }
        }));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("buch_gewaesser_fluesse"));
        record Page(String name, FishSpecies.Chapter chapter, String biome, int pick) {}
        for (Page page : new Page[] {
                new Page("buch_biom_korallenkueste", FishSpecies.Chapter.SEA, "kingdomsatwar:coral_coast", 3),
                new Page("buch_biom_ebene", FishSpecies.Chapter.RIVERS, "minecraft:plains", 1),
                new Page("buch_biom_schattenwald", FishSpecies.Chapter.SHADOW, "kingdomsatwar:shadow_forest", 5),
                new Page("buch_biom_sternenstaub", FishSpecies.Chapter.END, "kingdomsatwar:void_stardust_fields", 2),
                new Page("buch_biom_karmesinwald", FishSpecies.Chapter.NETHER, "minecraft:crimson_forest", 0),
                new Page("buch_biom_naht", FishSpecies.Chapter.ABYSS, "kingdomsatwar:abyss_seam", 7)}) {
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof FishBookScreen book) {
                    java.util.List<FishSpecies> local = FishSpecies.ofBiome(page.biome());
                    FishSpecies shown = local.stream().filter(log::has).skip(Math.min(page.pick(), Math.max(0, local.stream().filter(log::has).count() - 1)))
                            .findFirst().orElse(local.getFirst());
                    book.open(page.chapter(), shown);
                }
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot(page.name()));
        }
        for (FishSpecies.Chapter chapter : new FishSpecies.Chapter[] {FishSpecies.Chapter.SEA, FishSpecies.Chapter.CAVES, FishSpecies.Chapter.END,
                FishSpecies.Chapter.SHADOW, FishSpecies.Chapter.LEGENDS}) {
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof FishBookScreen book) {
                    book.open(chapter, chapter == FishSpecies.Chapter.LEGENDS ? FishSpecies.STAR_WHALE : null);
                }
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot("buch_kapitel_" + chapter.name().toLowerCase(java.util.Locale.ROOT)));
        }
        // eine noch nie gefangene Art: nur ihr Schattenriss und die Fährte
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof FishBookScreen book) {
                FishSpecies unknown = FishSpecies.ofBiome("kingdomsatwar:rock_gorges").stream().filter(s -> !log.has(s)).findFirst()
                        .orElse(FishSpecies.ofBiome("kingdomsatwar:rock_gorges").getLast());
                book.open(FishSpecies.Chapter.RIVERS, unknown);
            }
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("buch_unbekannt"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));

        // --- der Fischer im Dorf: Beutel, Erweiterung, Aufkauf ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity fisher =
                    io.github.sirschrolli.kingdomsatwar.registry.ModEntities.VILLAGER.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            fisher.setup(io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession.FISHER, 0, "Ole", player.blockPosition(), "Seedorf", null, null, 7L);
            fisher.snapTo(player.getX() + 0.5, player.getY(), player.getZ() + 2.2, 180.0f, 0.0f);
            fisher.setNoAi(true);
            level.addFreshEntity(fisher);
            player.getInventory().setItem(0, new ItemStack(FishSpecies.BROOK_TROUT.item(), 12));
            for (int i = 1; i < 8; i++) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
            io.github.sirschrolli.kingdomsatwar.economy.Coins.give(player, 2 * 81 + 6 * 9 + 5);
            io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.open(player, fisher);
        }));
        steps.add(Step.until(() -> net.minecraft.client.Minecraft.getInstance().gui.screen()
                instanceof io.github.sirschrolli.kingdomsatwar.client.town.TradeScreen, 60));
        steps.add(Step.once(minecraft -> hover(minecraft, 150, 110)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("fischer_handel"));
        steps.add(Step.once(minecraft -> hover(minecraft, -30, -40)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("fischer_handel_hinweis"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("fischer_im_dorf"));
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

    // =====================================================================================
    // Die Angeln und die neuen Verzauberungen (0.87)
    // =====================================================================================

    /** Führt einen Befehl auf dem Server aus (mit festen Koordinaten, die erst hier bekannt sind). */
    private static void run(ServerLevel level, String command) {
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(), command);
    }

    /** Die Angeln von 0.90 in 3D: Name im Bild und Gegenstand. */
    private static final String[][] RODS_3D = {
            {"holz", "minecraft:fishing_rod"}, {"eisen", "kingdomsatwar:iron_fishing_rod"}, {"gold", "kingdomsatwar:gold_fishing_rod"},
            {"drache", "kingdomsatwar:dragon_fishing_rod"}, {"schatten", "kingdomsatwar:shadow_fishing_rod"},
            {"glut", "kingdomsatwar:glut_fishing_rod"}, {"sterne", "kingdomsatwar:star_fishing_rod"}};

    private static ItemStack stack(ServerLevel level, String id) {
        return new ItemStack(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM)
                .getValue(net.minecraft.resources.Identifier.parse(id)));
    }

    /**
     * Angeln 0.90 ({@code angeln_090}): alle Angeln in 3D – an der Wand, in der Hand (eigene Sicht und von vorn) und ausgeworfen;
     * Angeln unter Wasser (der Haken bleibt in der Tiefe stehen, der Drill unter Wasser); die Legenden der Biome – funkelnd an der
     * Wand, im Gepäck und im Fischbuch.
     */
    static void angeln090(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "effect clear @s", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        BlockPos[] top = new BlockPos[1];
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            BlockPos at = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            // ein Ufer, davor ein tiefes Becken mit Seegras, Tang und Meereslaternen, dahinter eine Wand aus Brettern und Stein
            net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(90L);
            for (int x = -13; x <= 13; x++) {
                for (int z = -3; z <= 19; z++) {
                    for (int y = -8; y <= 9; y++) {
                        BlockPos pos = at.offset(x, y, z);
                        boolean pool = z >= 4 && z <= 15 && Math.abs(x) <= 9;
                        net.minecraft.world.level.block.state.BlockState state;
                        if (z == 18 && y >= 0 && y <= 8) {
                            state = (y == 0 || y == 8 || Math.abs(x) == 13 ? Blocks.STRIPPED_DARK_OAK_LOG : Blocks.POLISHED_ANDESITE).defaultBlockState();
                        } else if (y > 0) {
                            state = Blocks.AIR.defaultBlockState();
                        } else if (pool && y >= -6) {
                            state = Blocks.WATER.defaultBlockState();
                        } else if (pool && y == -7) {
                            state = ((x + z) % 5 == 0 ? Blocks.SEA_LANTERN : random.nextInt(3) == 0 ? Blocks.GRAVEL : Blocks.SAND).defaultBlockState();
                        } else if (pool) {
                            state = Blocks.STONE.defaultBlockState();
                        } else if (y == 0) {
                            state = (z <= 3 && Math.abs(x) <= 3 ? Blocks.SPRUCE_PLANKS : Blocks.GRASS_BLOCK).defaultBlockState();
                        } else {
                            state = (Math.abs(x) == 10 || z == 3 || z == 16 ? Blocks.PRISMARINE_BRICKS : Blocks.STONE).defaultBlockState();
                        }
                        level.setBlock(pos, state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                }
            }
            // Seegras und Tang auf dem Grund
            for (int i = 0; i < 70; i++) {
                BlockPos ground = at.offset(-9 + random.nextInt(19), -6, 4 + random.nextInt(12));
                if (!level.getBlockState(ground.below()).is(Blocks.SEA_LANTERN)) {
                    if (random.nextInt(3) == 0) {
                        int height = 2 + random.nextInt(3);
                        for (int k = 0; k < height; k++) {
                            level.setBlock(ground.above(k), (k == height - 1 ? Blocks.KELP : Blocks.KELP_PLANT).defaultBlockState(),
                                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                        }
                    } else {
                        level.setBlock(ground, Blocks.SEAGRASS.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                }
            }
            top[0] = at;
            // die sieben Angeln in 3D vor der Wand, leicht gedreht (Blick nach Süden: Osten liegt links)
            for (int i = 0; i < RODS_3D.length; i++) {
                double x = at.getX() + 0.5 - (i - 3) * 2.75;
                run(level, String.format(java.util.Locale.ROOT, "summon minecraft:item_display %.2f %.2f %.2f {item:{id:\"%s\",count:1},"
                        + "item_display:\"none\",billboard:\"fixed\",Rotation:[180f,0f],brightness:{sky:15,block:15},"
                        + "transformation:{left_rotation:[0f,0.17f,0f,0.985f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2.7f,2.7f,2.7f]}}",
                        x, at.getY() + 3.9, at.getZ() + 17.3, RODS_3D[i][1]));
            }
            player.teleportTo(level, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 1.5, Set.of(), 0.0f, 4.0f, true);
        }));
        steps.add(Step.until(() -> top[0] != null, 200));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = true;
            if (!minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.screenshot("ruten3d_wand"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            BlockPos at = top[0];
            // schwebend vor der Wand (sonst fiele er ins Becken)
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(player.level(), at.getX() - 3.5, at.getY() + 2.6, at.getZ() + 12.6, Set.of(), 0.0f, 10.0f, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("ruten3d_wand_nah"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            BlockPos at = top[0];
            player.teleportTo(player.level(), at.getX() + 4.5, at.getY() + 2.6, at.getZ() + 12.6, Set.of(), 0.0f, 10.0f, true);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("ruten3d_wand_nah2"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
            BlockPos at = top[0];
            player.teleportTo(player.level(), at.getX() - 0.25, at.getY() + 1.0, at.getZ() - 1.2, Set.of(), 0.0f, 16.0f, true);
        }));
        // --- von der Seite: jede Angel in der Hand eines Rüstungsständers (der Blick des Spielers fällt auf ihn) ---
        for (String[] rod : RODS_3D) {
            steps.add(Step.server(any -> {
                ServerLevel level = player(any).level();
                BlockPos at = top[0];
                // auf dem Server selbst aufräumen (ein Befehl vom Spiel käme womöglich erst nach dem neuen Ständer an)
                run(level, "kill @e[type=minecraft:armor_stand]");
                run(level, String.format(java.util.Locale.ROOT, "summon minecraft:armor_stand %.2f %.2f %.2f {ShowArms:1b,NoBasePlate:1b,"
                        + "Rotation:[90f,0f],Pose:{RightArm:[-28f,0f,0f],LeftArm:[-10f,0f,-10f]},equipment:{mainhand:{id:\"%s\",count:1}}}",
                        at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 1.0, rod[1]));
            }));
            steps.add(Step.waitTicks(8));
            steps.add(Step.screenshot("rute3d_" + rod[0] + "_seite"));
        }
        steps.add(Step.server(any -> run(player(any).level(), "kill @e[type=minecraft:armor_stand]")));
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = false;
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));

        // --- jede Angel in der Hand: eigene Sicht und von vorn ---
        for (String[] rod : RODS_3D) {
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                BlockPos at = top[0];
                if (player.fishing != null) {
                    player.fishing.discard();
                }
                player.teleportTo(player.level(), at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 1.5, Set.of(), 0.0f, 10.0f, true);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack(player.level(), rod[1]));
            }));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON)));
            steps.add(Step.waitTicks(16));
            steps.add(Step.screenshot("rute3d_" + rod[0] + "_ich"));
            steps.add(Step.hud(false));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("rute3d_" + rod[0] + "_vorn"));
            steps.add(Step.hud(true));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON)));
        }
        // --- ausgeworfen: die Schnur läuft von der Spitze zum Schwimmer ---
        for (String[] rod : new String[][] {RODS_3D[3], RODS_3D[6]}) {
            steps.add(Step.server(any -> player(any).setItemInHand(InteractionHand.MAIN_HAND, stack(player(any).level(), rod[1]))));
            steps.add(cast(-1.5, -0.05, 5.5));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("rute3d_" + rod[0] + "_wurf_ich"));
            steps.add(Step.hud(false));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK)));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot("rute3d_" + rod[0] + "_wurf_hinten"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot("rute3d_" + rod[0] + "_wurf_vorn"));
            steps.add(Step.hud(true));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON)));
        }

        // --- unter Wasser: ausgeworfen wie mit der Angel, der Haken gleitet und bleibt in der Tiefe stehen ---
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos at = top[0];
            if (player.fishing != null) {
                player.fishing.discard();
            }
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 20 * 600, 0, false, false, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 20 * 600, 0, false, false, false));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack(level, RODS_3D[6][1]));
            player.teleportTo(level, at.getX() + 0.5, at.getY() - 5.0, at.getZ() + 5.5, Set.of(), 0.0f, 6.0f, true);
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            FishingHook hook = new FishingHook(player, player.level(), 0, 0);
            player.level().addFreshEntity(hook);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            LOGGER.info("[Showcase] Haken unter Wasser: {} – {} Blöcke vor dem Taucher, {} unter der Oberfläche", player.fishing != null
                    && io.github.sirschrolli.kingdomsatwar.fishing.Fishing.deep(player.fishing), player.fishing == null ? -1 : player.fishing.distanceTo(player),
                    player.fishing == null ? -1 : top[0].getY() - player.fishing.getY());
        }));
        steps.add(Step.screenshot("unterwasser_ich"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("unterwasser_hinten"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("unterwasser_vorn"));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON)));
        steps.add(Step.waitTicks(4));
        // der Biss unter Wasser: Bläschen statt Spritzer, dann der Drill
        steps.add(biteAndReel());
        steps.add(Step.until(() -> net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof FishingScreen, 80));
        steps.add(Step.waitTicks(24));
        steps.add(Step.screenshot("unterwasser_drill"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            if (player.fishing != null) {
                player.fishing.discard();
            }
            BlockPos at = top[0];
            player.teleportTo(player.level(), at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 1.5, Set.of(), 0.0f, 4.0f, true);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }));

        // --- die Legenden der Biome: funkelnd an der Wand ---
        steps.add(Step.command("kill @e[type=minecraft:item_display]"));
        steps.add(Step.server(any -> {
            ServerLevel level = player(any).level();
            BlockPos at = top[0];
            java.util.List<FishSpecies> legends = new java.util.ArrayList<>();
            for (FishSpecies species : FishSpecies.values()) {
                if (species.biome() != null && species.rarity() == FishSpecies.Rarity.LEGENDARY && legends.size() < 300) {
                    legends.add(species);
                }
            }
            // sechzehn Legenden aus ganz verschiedenen Biomen: zwei Reihen zu acht
            int step = Math.max(1, legends.size() / 16);
            for (int i = 0; i < 16; i++) {
                FishSpecies species = legends.get((i * step + 3) % legends.size());
                double x = at.getX() + 0.5 - (i % 8 - 3.5) * 2.3;
                double y = at.getY() + (i < 8 ? 5.4 : 2.9);
                run(level, String.format(java.util.Locale.ROOT, "summon minecraft:item_display %.2f %.2f %.2f {item:{id:\"kingdomsatwar:fish_%s\",count:1},"
                        + "item_display:\"gui\",billboard:\"fixed\",Rotation:[0f,0f],brightness:{sky:15,block:15},"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2.2f,2.2f,2.2f]}}",
                        x, y, at.getZ() + 17.4, species.id()));
            }
        }));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = true;
            if (!minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("legenden_wand"));
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = false;
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        // im Gepäck: eine Legende unter der Maus (goldener Name, „Legendär“)
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.getInventory().clearContent();
            java.util.List<FishSpecies> reef = new java.util.ArrayList<>(FishSpecies.ofBiome("kingdomsatwar:urmeer_reef"));
            // die Legenden des Riffs vorn
            reef.sort(java.util.Comparator.comparing(species -> species.rarity() != FishSpecies.Rarity.LEGENDARY));
            int slot = 9;
            for (FishSpecies species : reef) {
                if (slot < 36) {
                    player.getInventory().setItem(slot++, new ItemStack(species.item(), species.rarity() == FishSpecies.Rarity.COMMON ? 7 : 1));
                }
            }
            player.getInventory().setItem(0, stack(player.level(), RODS_3D[3][1]));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new InventoryScreen(minecraft.player)));
        // die Maus auf der ersten Legende (oben links im Gepäck) – erst, wenn das Fenster offen ist und das Spiel die Maus
        // freigegeben hat (das setzt sie in die Mitte), und noch einmal zur Sicherheit
        steps.add(Step.waitTicks(6));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 8, -83 + 84 + 8)));
        steps.add(Step.waitTicks(4));
        steps.add(Step.once(minecraft -> hover(minecraft, -88 + 8 + 8, -83 + 84 + 8)));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("legende_gepaeck"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        // im Fischbuch: die Arten des Korallenriffs im Urmeer, aufgeschlagen bei seiner Legende
        FishLog log = new FishLog();
        int n = 0;
        for (FishSpecies species : FishSpecies.values()) {
            if (species.biome() != null && (species.biome().startsWith("kingdomsatwar:urmeer") || species.biome().equals("minecraft:plains")) && n % 4 != 3) {
                log.record(species, species.minCm() + (species.maxCm() - species.minCm()) * (2 + n % 7) / 10);
            }
            n++;
        }
        for (String biome : new String[] {"kingdomsatwar:urmeer_reef", "minecraft:plains"}) {
            for (FishSpecies species : FishSpecies.ofBiome(biome)) {
                if (species.rarity() == FishSpecies.Rarity.LEGENDARY) {
                    log.record(species, species.maxCm() - 3);
                }
            }
        }
        steps.add(Step.once(minecraft -> hover(minecraft, 150, 100)));
        steps.add(Step.screen(minecraft -> new FishBookScreen(log)));
        for (String[] page : new String[][] {{"buch_legende_riff", "SEA", "kingdomsatwar:urmeer_reef"}, {"buch_legende_ebene", "RIVERS", "minecraft:plains"},
                {"buch_legende_graben", "SEA", "kingdomsatwar:urmeer_trench"}}) {
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof FishBookScreen book) {
                    FishSpecies legend = FishSpecies.ofBiome(page[2]).stream().filter(s -> s.rarity() == FishSpecies.Rarity.LEGENDARY).findFirst()
                            .orElse(FishSpecies.ofBiome(page[2]).getLast());
                    book.open(FishSpecies.Chapter.valueOf(page[1]), legend);
                }
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot(page[0]));
        }
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.quit());
    }

    /**
     * Die Angeln ({@code angeln_ruten}): alle sechs groß vor einer Bretterwand, einige in der Hand und ausgeworfen über Wasser
     * und Lava; dazu Donnerschlag (der Funkenschlag) und Eulenauge (eine dunkle Kammer ohne und mit dem Helm).
     */
    static void rods(Deque<Step> steps) {
        steps.add(Step.once(minecraft -> ShowcaseRunner.hideHand = false));
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "kill @e[type=!minecraft:player]", "clear", "effect clear @s", "gamemode survival"}) {
            steps.add(Step.command(command));
        }
        BlockPos[] top = new BlockPos[1];
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = any.getServer().overworld();
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            BlockPos at = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(0, 0, 0));
            // Teich links, Lava rechts, dahinter eine Wand aus dunklen Brettern
            for (int x = -10; x <= 10; x++) {
                for (int z = -2; z <= 15; z++) {
                    for (int y = -3; y <= 8; y++) {
                        BlockPos pos = at.offset(x, y, z);
                        boolean basin = z >= 3 && z <= 12 && Math.abs(x) <= 7;
                        if (z == 14 && y >= 0 && y <= 7) {
                            level.setBlockAndUpdate(pos, (y == 0 || y == 7 || Math.abs(x) == 10 ? Blocks.STRIPPED_DARK_OAK_LOG : Blocks.POLISHED_ANDESITE)
                                    .defaultBlockState());
                        } else if (y > 0) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        } else if (basin && y >= -2 && x != 0) {
                            level.setBlockAndUpdate(pos, (x < 0 ? Blocks.WATER : Blocks.LAVA).defaultBlockState());
                        } else {
                            level.setBlockAndUpdate(pos, (basin || y < 0 ? Blocks.STONE_BRICKS : Blocks.GRASS_BLOCK).defaultBlockState());
                        }
                    }
                }
            }
            top[0] = at;
            // die sechs Angeln, groß und hell vor der Wand: zwei Reihen zu drei
            int i = 0;
            for (io.github.sirschrolli.kingdomsatwar.fishing.RodType type : io.github.sirschrolli.kingdomsatwar.fishing.RodType.values()) {
                // Blick nach Süden: Osten liegt links – die erste Angel jeder Reihe kommt nach links
                double x = at.getX() + 0.5 - (i % 3 - 1) * 3.45;
                double y = at.getY() + (i < 3 ? 5.3 : 2.35);
                run(level, String.format(java.util.Locale.ROOT, "summon minecraft:item_display %.2f %.2f %.2f {item:{id:\"kingdomsatwar:%s\",count:1},"
                        + "item_display:\"gui\",billboard:\"fixed\",Rotation:[0f,0f],brightness:{sky:15,block:15},"
                        + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2.9f,2.9f,2.9f]}}",
                        x, y, at.getZ() + 13.4, type.id()));
                i++;
            }
            player.teleportTo(level, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 6.8, Set.of(), 0.0f, -10.0f, true);
        }));
        steps.add(Step.until(() -> top[0] != null, 200));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = true;
            if (!minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.screenshot("ruten_reihe"));
        steps.add(Step.command("kill @e[type=minecraft:item_display]"));
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = false;
            // ohne HUD blendet das Spiel auch die Hand aus
            if (minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));

        // in der Hand und ausgeworfen: über dem Teich (links) und über der Lava (rechts)
        Object[][] shots = {{"IRON", -3.0}, {"GOLD", -3.0}, {"DRAGON", -3.0}, {"SHADOW", -3.0}, {"STAR", -3.0}, {"GLUT", 4.0}};
        for (Object[] shot : shots) {
            var type = io.github.sirschrolli.kingdomsatwar.fishing.RodType.valueOf((String) shot[0]);
            double side = (Double) shot[1];
            steps.add(Step.server(any -> {
                ServerPlayer player = player(any);
                if (player.fishing != null) {
                    player.fishing.discard();
                }
                BlockPos at = top[0];
                // Blick schräg auf den Teich (links, Westen) oder die Lava (rechts, Osten)
                player.teleportTo(player.level(), at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 1.5, Set.of(), side < 0 ? 34.0f : -34.0f, 22.0f, true);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FishingItems.rod(type)));
            }));
            steps.add(Step.waitTicks(14));
            steps.add(Step.screenshot("rute_" + type.name().toLowerCase(java.util.Locale.ROOT) + "_hand"));
            steps.add(cast(side < 0 ? -2.6 : 2.8, side < 0 ? 0.2 : 0.5, 5.0));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("rute_" + type.name().toLowerCase(java.util.Locale.ROOT) + "_wurf"));
        }
        // die Glutangel von hinten: die Schnur zum Schwimmer auf der Lava
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            if (!minecraft.gui.hud.isHidden()) {
                minecraft.gui.hud.toggle();
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("rute_glut_hinten"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON)));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            if (player.fishing != null) {
                player.fishing.discard();
            }
        }));

        // Donnerschlag: der Funkenschlag fährt in einen zähen Wiedergänger
        net.minecraft.world.entity.LivingEntity[] target = new net.minecraft.world.entity.LivingEntity[1];
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos at = top[0];
            player.teleportTo(level, at.getX() + 0.5, at.getY() + 1.0, at.getZ() - 1.0, Set.of(), 0.0f, 8.0f, true);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(io.github.sirschrolli.kingdomsatwar.enchant.ModEnchantments.THUNDERSTRIKE), 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
            var zombie = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            zombie.snapTo(at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 2.0, 180.0f, 0.0f);
            zombie.setNoAi(true);
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(500.0);
            zombie.setHealth(500.0f);
            zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            level.addFreshEntity(zombie);
            target[0] = zombie;
        }));
        steps.add(Step.until(() -> target[0] != null, 200));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> io.github.sirschrolli.kingdomsatwar.enchant.ThunderstrikeEffect.strike(player(any).level(), target[0], player(any), 3.0f)));
        steps.add(Step.record("donnerschlag", 8, minecraft -> { }));
        steps.add(Step.command("kill @e[type=!minecraft:player]"));

        // Eulenauge: eine dunkle Kammer unter der Erde – ohne Helm, dann mit
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos room = top[0].offset(30, -22, 0);
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    for (int y = -1; y <= 5; y++) {
                        boolean wall = Math.abs(x) == 6 || Math.abs(z) == 6 || y == -1 || y == 5;
                        level.setBlockAndUpdate(room.offset(x, y, z), wall ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    }
                }
            }
            level.setBlockAndUpdate(room.offset(-3, 0, 4), Blocks.CHEST.defaultBlockState());
            level.setBlockAndUpdate(room.offset(-2, 0, 4), Blocks.CRAFTING_TABLE.defaultBlockState());
            level.setBlockAndUpdate(room.offset(3, 0, 4), Blocks.BARREL.defaultBlockState());
            level.setBlockAndUpdate(room.offset(4, 1, 6), Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
            level.setBlockAndUpdate(room.offset(-4, 2, 6), Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState());
            level.setBlockAndUpdate(room.offset(1, 3, 6), Blocks.DEEPSLATE_IRON_ORE.defaultBlockState());
            level.setBlockAndUpdate(room.offset(-1, 0, 5), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.teleportTo(level, room.getX() + 0.5, room.getY(), room.getZ() - 4.0, Set.of(), 0.0f, 10.0f, true);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.screenshot("eulenauge_ohne"));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ItemStack helmet = new ItemStack(Items.IRON_HELMET);
            helmet.enchant(player.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getOrThrow(io.github.sirschrolli.kingdomsatwar.enchant.ModEnchantments.OWL_EYE), 1);
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("eulenauge_mit"));
    }
}
