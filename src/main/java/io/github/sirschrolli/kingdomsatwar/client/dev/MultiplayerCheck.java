package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.network.CampaignPayload;
import io.github.sirschrolli.kingdomsatwar.network.HealPayload;
import io.github.sirschrolli.kingdomsatwar.network.HeroTalentPayload;
import io.github.sirschrolli.kingdomsatwar.network.ProvisionPayload;
import io.github.sirschrolli.kingdomsatwar.network.RequestChroniclePayload;
import io.github.sirschrolli.kingdomsatwar.network.RequestFishBookPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

/**
 * Mehrspieler-Prüfung ({@code -Dkingdomsatwar.mpcheck=1}): Der Client ist mit einem echten Server verbunden (über das Netz
 * wie bei LAN, einem Server oder einer mit Essential geöffneten Welt – anders als im Einzelspieler wird dabei jedes Paket
 * wirklich verpackt und ausgepackt). Er lässt sich jeden Gegenstand und jedes Wesen der Mod geben bzw. herbeirufen, prüft,
 * ob sie beim Client ankommen, öffnet die Fenster (Feldherrentisch, Söldnertafel, Stadt, Chronik, Fischbuch, Fischbeutel,
 * Magietisch, Held) und schickt die Pakete der Knöpfe. Alles landet als {@code [MP]} im Protokoll, dazu Screenshots.
 *
 * <p>Ein Server kann weit hinterherhinken (hier teilt er sich den Rechner mit dem zeichnenden Spiel). Deshalb wartet jeder
 * Schritt auf eine Antwort statt eine feste Zeit: Nach den Befehlen setzt der Server die Erfahrungsstufe auf eine neue Zahl
 * ({@link #barrier}) – sieht das Spiel sie, ist alles davor Geschickte erledigt und angekommen.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class MultiplayerCheck {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean planned;
    private static int stepTicks;
    private static int problems;
    private static int barriers;
    private static int itemsChecked;
    private static int entitiesChecked;

    private MultiplayerCheck() {}

    static boolean enabled() {
        return !System.getProperty("kingdomsatwar.mpcheck", "").isBlank();
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (!enabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }
        if (!planned) {
            planned = true;
            plan();
            LOGGER.info("[MP] {} Schritte geplant", STEPS.size());
        }
        Step step = STEPS.peek();
        if (step == null) {
            return;
        }
        stepTicks++;
        try {
            if (step.run(minecraft, stepTicks)) {
                STEPS.poll();
                stepTicks = 0;
            }
        } catch (RuntimeException e) {
            problems++;
            LOGGER.error("[MP] Schritt schlug fehl", e);
            STEPS.poll();
            stepTicks = 0;
        }
    }

    private static Step pause(int ticks) {
        return (minecraft, ticks2) -> ticks2 >= ticks;
    }

    private static Step log(String message) {
        return Step.once(minecraft -> LOGGER.info("[MP] {}", message));
    }

    private static void problem(String message, Object... args) {
        problems++;
        LOGGER.warn("[MP] PROBLEM: " + message, args);
    }

    /** Wartet, bis {@code condition} gilt – höchstens {@code maxTicks}, dann ist es ein Problem. */
    private static Step waitFor(String what, Predicate<Minecraft> condition, int maxTicks) {
        return (minecraft, ticks) -> {
            if (condition.test(minecraft)) {
                return true;
            }
            if (ticks >= maxTicks) {
                problem("{} – nach {} Ticks nicht geschehen", what, maxTicks);
                return true;
            }
            return false;
        };
    }

    /**
     * Wartet, bis der Server alles zuvor Geschickte erledigt hat: Er setzt die Erfahrungsstufe auf eine neue Zahl, und das
     * Spiel wartet, bis es sie sieht (Befehle und Nachrichten erledigt der Server der Reihe nach).
     */
    private static void barrier(String what) {
        int level = 100 + (++barriers % 900);
        STEPS.add(Step.command("xp set @s " + level + " levels"));
        STEPS.add(waitFor("Antwort des Servers (" + what + ")", minecraft -> minecraft.player.experienceLevel == level, 2400));
    }

    private static void plan() {
        STEPS.add(pause(40));
        STEPS.add(Step.once(minecraft -> {
            var connection = minecraft.getConnection().getConnection();
            LOGGER.info("[MP] verbunden: Spieler {}, Server {}, im Speicher (Einzelspieler)? {}", minecraft.player.getName().getString(),
                    connection.getRemoteAddress(), connection.isMemoryConnection());
            if (connection.isMemoryConnection()) {
                problem("keine echte Netzverbindung");
            }
        }));
        // wer eine Welt zum ersten Mal betritt, bekommt vom Server das Intro – ansehen, dann überspringen (wie mit Leertaste)
        STEPS.add(waitFor("Das Intro beginnt", minecraft -> minecraft.gui.screen()
                instanceof io.github.sirschrolli.kingdomsatwar.client.intro.IntroScreen, 1200));
        STEPS.add(pause(60));
        STEPS.add(Step.screenshot("mp_01_intro"));
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(Step.command("gamemode creative"));
        STEPS.add(Step.command("time set noon"));
        STEPS.add(Step.command("weather clear"));
        STEPS.add(Step.command("gamerule advance_time false"));
        STEPS.add(Step.command("gamerule advance_weather false"));
        STEPS.add(Step.command("tp @s 0 -60 0 0 10"));
        barrier("Anfang");
        STEPS.add(Step.hud(false));
        STEPS.add(pause(40));
        STEPS.add(Step.screenshot("mp_02_angekommen"));

        planItems();
        planEntities();
        planWindows();

        STEPS.add(pause(20));
        STEPS.add(Step.once(minecraft -> {
            LOGGER.info("[MP] FERTIG – {} Gegenstände und {} Wesen geprüft, Probleme: {}", itemsChecked, entitiesChecked, problems);
            minecraft.stop();
        }));
    }

    /** Jeder Gegenstand der Mod: geben lassen und prüfen, ob er im Inventar des Spiels ankommt (36 auf einmal). */
    private static void planItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(KingdomsAtWar.MODID)) {
                items.add(item);
            }
        }
        STEPS.add(log("Gegenstände: " + items.size()));
        for (int from = 0; from < items.size(); from += 36) {
            List<Item> batch = items.subList(from, Math.min(items.size(), from + 36));
            int first = from;
            STEPS.add(Step.command("clear @s"));
            STEPS.add(Step.once(minecraft -> {
                for (Item item : batch) {
                    minecraft.player.connection.sendCommand("give @s " + BuiltInRegistries.ITEM.getKey(item));
                }
            }));
            barrier("Gegenstände ab " + first);
            STEPS.add(Step.once(minecraft -> {
                for (Item item : batch) {
                    itemsChecked++;
                    if (!minecraft.player.getInventory().contains(stack -> stack.is(item))) {
                        problem("Gegenstand kam nicht an: {}", BuiltInRegistries.ITEM.getKey(item));
                    }
                }
                if ((first / 36) % 25 == 0) {
                    LOGGER.info("[MP] Gegenstände bis {} geprüft", first + batch.size());
                }
            }));
        }
        STEPS.add(Step.command("clear @s"));
        STEPS.add(log("Gegenstände fertig"));
    }

    /** Jedes Wesen der Mod: herbeirufen (ohne KI), ansehen und prüfen, ob es beim Spiel ankommt (fünf auf einmal). */
    private static void planEntities() {
        List<EntityType<?>> types = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(KingdomsAtWar.MODID) && type.canSummon()) {
                types.add(type);
            }
        }
        STEPS.add(log("Wesen: " + types.size()));
        for (int from = 0; from < types.size(); from += 5) {
            List<EntityType<?>> batch = types.subList(from, Math.min(types.size(), from + 5));
            int index = from / 5;
            Set<EntityType<?>> seen = new HashSet<>();
            STEPS.add(Step.once(minecraft -> {
                for (int i = 0; i < batch.size(); i++) {
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(batch.get(i));
                    // mit Namensschild, damit man auf den Bildern sieht, wer wer ist
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                            "summon %s %.1f -60 %.1f {NoAI:1b,Silent:1b,CustomName:\"%s\",CustomNameVisible:1b}", id, -8.0 + i * 4.0, 9.0,
                            id.getPath()));
                }
            }));
            barrier("Wesen " + index);
            // auch kurzlebige Wesen zählen: Jeder Tick, in dem eins zu sehen ist, wird gemerkt
            STEPS.add((minecraft, ticks) -> {
                for (Entity entity : minecraft.level.entitiesForRendering()) {
                    if (batch.contains(entity.getType())) {
                        seen.add(entity.getType());
                    }
                }
                if (seen.size() < batch.size() && ticks < 200) {
                    return false;
                }
                for (EntityType<?> type : batch) {
                    entitiesChecked++;
                    if (!seen.contains(type)) {
                        problem("Wesen kam nicht an: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
                    }
                }
                return true;
            });
            STEPS.add(pause(6));
            STEPS.add(Step.screenshot(String.format(Locale.ROOT, "mp_wesen_%02d", index)));
            STEPS.add(Step.command("kill @e[type=!minecraft:player]"));
            barrier("aufgeräumt " + index);
        }
        STEPS.add(log("Wesen fertig"));
    }

    /** Die Fenster: Held, Feldherrentisch, Söldnertafel, Magietisch, Stadt, Chronik, Fischbuch, Fischbeutel, Titelkarte. */
    private static void planWindows() {
        // der Held: Stufe vom Server, Fenster beim Spiel, Talentpunkt zurück zum Server
        STEPS.add(Step.command("kaw hero level 5"));
        barrier("Heldenstufe");
        STEPS.add(waitFor("Heldenerfahrung kommt an", minecraft -> minecraft.player.getData(ModAttachments.HERO).experience() > 0, 200));
        STEPS.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.CharacterScreen()));
        STEPS.add(pause(20));
        STEPS.add(Step.screenshot("mp_held"));
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new HeroTalentPayload(0))));
        barrier("Talent");
        STEPS.add(waitFor("Talentpunkt kommt zurück", minecraft -> minecraft.player.getData(ModAttachments.HERO).talents().stream()
                .anyMatch(points -> points > 0), 200));
        STEPS.add(pause(5));
        STEPS.add(Step.screenshot("mp_held_talent"));
        STEPS.add(Step.screen(minecraft -> null));

        // Feldherrentisch mit Soldaten: öffnen, dann Proviant, Heilen und Schlachtzug schicken
        BlockPos table = new BlockPos(3, -60, 3);
        STEPS.add(Step.command("tp @s 0 -60 0 -45 20"));
        STEPS.add(Step.command("setblock 3 -60 3 kingdomsatwar:recruitment_table"));
        STEPS.add(Step.command("kaw army archer 2"));
        STEPS.add(Step.command("kaw army swordsman 2"));
        barrier("Feldherrentisch");
        STEPS.add(blockArrives(table, "kingdomsatwar:recruitment_table"));
        STEPS.add(pause(20));
        openAndShoot(use(table), "Feldherrentisch", "mp_feldherrentisch");
        STEPS.add(Step.once(minecraft -> {
            ClientPacketDistributor.sendToServer(new ProvisionPayload(table));
            ClientPacketDistributor.sendToServer(new HealPayload(table));
            ClientPacketDistributor.sendToServer(new CampaignPayload(table, true));
        }));
        barrier("Knöpfe des Feldherrentischs");
        STEPS.add(pause(10));
        STEPS.add(Step.screenshot("mp_feldherrentisch_antwort"));
        STEPS.add(Step.screen(minecraft -> null));

        // Söldnertafel
        BlockPos board = new BlockPos(-3, -60, 3);
        STEPS.add(Step.command("setblock -3 -60 3 kingdomsatwar:mercenary_board"));
        barrier("Söldnertafel");
        STEPS.add(blockArrives(board, "kingdomsatwar:mercenary_board"));
        openAndShoot(use(board), "Söldnertafel", "mp_soeldnertafel");
        STEPS.add(Step.screen(minecraft -> null));

        // Magietisch
        BlockPos magic = new BlockPos(0, -60, 4);
        STEPS.add(Step.command("setblock 0 -60 4 kingdomsatwar:magic_table"));
        barrier("Magietisch");
        STEPS.add(blockArrives(magic, "kingdomsatwar:magic_table"));
        openAndShoot(use(magic), "Magietisch", "mp_magietisch");
        STEPS.add(Step.screen(minecraft -> null));

        // eine Siedlung gründen (Banner setzen, wie ein Spieler im Überlebensmodus) und ihr Fenster öffnen
        BlockPos banner = new BlockPos(0, -60, -4);
        STEPS.add(Step.command("gamemode survival"));
        STEPS.add(Step.command("effect give @s minecraft:resistance infinite 255 true"));
        STEPS.add(Step.command("tp @s 0 -60 0 180 20"));
        STEPS.add(Step.command("clear @s"));
        STEPS.add(Step.command("give @s kingdomsatwar:town_banner"));
        barrier("Banner");
        STEPS.add(Step.once(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(0);
            BlockPos ground = banner.below();
            minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false));
        }));
        barrier("Banner gesetzt");
        STEPS.add(waitFor("Banner steht", minecraft -> !minecraft.level.getBlockState(banner).isAir(), 200));
        STEPS.add(pause(10));
        openAndShoot(use(banner), "Stadtfenster", "mp_stadt");
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(Step.command("gamemode creative"));

        // Chronik und Fischbuch (auf Anfrage vom Server), Fischbeutel (Gegenstand benutzen)
        openAndShoot(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new RequestChroniclePayload())), "Chronik", "mp_chronik");
        STEPS.add(Step.screen(minecraft -> null));
        openAndShoot(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new RequestFishBookPayload())), "Fischbuch", "mp_fischbuch");
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(Step.command("clear @s"));
        STEPS.add(Step.command("give @s kingdomsatwar:fish_bag"));
        barrier("Fischbeutel");
        openAndShoot(Step.once(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(0);
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        }), "Fischbeutel", "mp_fischbeutel");
        STEPS.add(Step.screen(minecraft -> null));

        // Titelkarte eines Kapitels (Server an Spiel)
        STEPS.add(Step.command("kaw story card 1"));
        barrier("Titelkarte");
        STEPS.add(pause(40));
        STEPS.add(Step.screenshot("mp_titelkarte"));
    }

    /** Wartet, bis der gesetzte Block auch im Spiel steht. */
    private static Step blockArrives(BlockPos pos, String id) {
        return waitFor(id + " steht bei " + pos.toShortString(), minecraft -> {
            Block block = minecraft.level.getBlockState(pos).getBlock();
            return BuiltInRegistries.BLOCK.getKey(block).toString().equals(id);
        }, 200);
    }

    /** Löst etwas aus, wartet, bis sich ein Fenster öffnet, und fotografiert es. */
    private static void openAndShoot(Step trigger, String what, String shot) {
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(trigger);
        STEPS.add(waitFor(what + " öffnet sich", minecraft -> minecraft.gui.screen() != null, 600));
        STEPS.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() != null) {
                LOGGER.info("[MP] {} offen: {}", what, minecraft.gui.screen().getClass().getSimpleName());
            }
        }));
        STEPS.add(pause(15));
        STEPS.add(Step.screenshot(shot));
    }

    /** Rechtsklick auf einen Block (wie der Spieler mit leerer Hand). */
    private static Step use(BlockPos pos) {
        return Step.once(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(8);
            minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        });
    }
}
