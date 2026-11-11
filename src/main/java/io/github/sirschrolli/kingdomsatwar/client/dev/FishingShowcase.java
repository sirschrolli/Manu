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
}
