package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
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
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class MultiplayerCheck {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean planned;
    private static int stepTicks;
    private static int problems;

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

    private static void plan() {
        Minecraft mc = Minecraft.getInstance();
        STEPS.add(pause(100));
        STEPS.add(Step.once(minecraft -> {
            var connection = minecraft.getConnection().getConnection();
            LOGGER.info("[MP] verbunden: Spieler {}, Server {}, im Speicher (Einzelspieler)? {}", minecraft.player.getName().getString(),
                    connection.getRemoteAddress(), connection.isMemoryConnection());
            if (connection.isMemoryConnection()) {
                problem("keine echte Netzverbindung");
            }
        }));
        STEPS.add(Step.command("gamemode creative"));
        STEPS.add(Step.command("time set noon"));
        STEPS.add(Step.command("weather clear"));
        STEPS.add(Step.hud(false));
        STEPS.add(pause(40));
        STEPS.add(Step.screenshot("mp_01_angekommen"));

        // --- jeder Gegenstand der Mod: geben lassen und prüfen, ob er (mit seinen Daten) im Inventar des Clients ankommt ---
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(KingdomsAtWar.MODID)) {
                items.add(item);
            }
        }
        STEPS.add(log("Gegenstände: " + items.size()));
        for (int from = 0; from < items.size(); from += 36) {
            List<Item> batch = items.subList(from, Math.min(items.size(), from + 36));
            STEPS.add(Step.command("clear @s"));
            STEPS.add(pause(2));
            STEPS.add(Step.once(minecraft -> {
                for (Item item : batch) {
                    minecraft.player.connection.sendCommand("give @s " + BuiltInRegistries.ITEM.getKey(item));
                }
            }));
            STEPS.add(pause(8));
            int first = from;
            STEPS.add(Step.once(minecraft -> {
                for (Item item : batch) {
                    if (!minecraft.player.getInventory().contains(stack -> stack.is(item))) {
                        problem("Gegenstand kam nicht an: {}", BuiltInRegistries.ITEM.getKey(item));
                    }
                }
                if (first % 360 == 0) {
                    LOGGER.info("[MP] Gegenstände bis {} geprüft", first + batch.size());
                }
            }));
        }
        STEPS.add(Step.command("clear @s"));
        STEPS.add(log("Gegenstände fertig"));

        // --- jedes Wesen der Mod: herbeirufen (ohne KI), ansehen, prüfen, ob es beim Client ist ---
        List<EntityType<?>> types = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(KingdomsAtWar.MODID) && type.canSummon()) {
                types.add(type);
            }
        }
        STEPS.add(log("Wesen: " + types.size()));
        STEPS.add(Step.command("tp @s 0 -60 0 0 10"));
        STEPS.add(pause(60));
        for (int from = 0; from < types.size(); from += 5) {
            List<EntityType<?>> batch = types.subList(from, Math.min(types.size(), from + 5));
            int index = from / 5;
            STEPS.add(Step.once(minecraft -> {
                for (int i = 0; i < batch.size(); i++) {
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(batch.get(i));
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "summon %s %.1f -60 %.1f {NoAI:1b,Silent:1b}", id,
                            -8.0 + i * 4.0, 9.0));
                }
            }));
            STEPS.add(pause(30));
            STEPS.add(Step.once(minecraft -> {
                for (EntityType<?> type : batch) {
                    boolean seen = false;
                    for (var entity : minecraft.level.entitiesForRendering()) {
                        if (entity.getType() == type) {
                            seen = true;
                            break;
                        }
                    }
                    if (!seen) {
                        problem("Wesen kam nicht an: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type));
                    }
                }
            }));
            STEPS.add(Step.screenshot(String.format(Locale.ROOT, "mp_wesen_%02d", index)));
            STEPS.add(Step.command("kill @e[type=!minecraft:player]"));
            STEPS.add(pause(10));
        }
        STEPS.add(log("Wesen fertig"));

        // --- der Held: Erfahrung vom Server, Fenster beim Client, Talentpunkt zurück zum Server ---
        STEPS.add(Step.command("kaw hero level 5"));
        STEPS.add(pause(20));
        STEPS.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.CharacterScreen()));
        STEPS.add(pause(20));
        STEPS.add(Step.screenshot("mp_held"));
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new io.github.sirschrolli.kingdomsatwar.network.HeroTalentPayload(0))));
        STEPS.add(pause(20));
        STEPS.add(Step.screenshot("mp_held_talent"));
        STEPS.add(Step.screen(minecraft -> null));

        // --- Feldherrentisch mit Soldaten ---
        BlockPos table = new BlockPos(3, -60, 3);
        STEPS.add(Step.command("tp @s 0 -60 0 -45 20"));
        STEPS.add(Step.command("setblock 3 -60 3 kingdomsatwar:recruitment_table"));
        STEPS.add(Step.command("kaw army archer 2"));
        STEPS.add(Step.command("kaw army swordsman 2"));
        STEPS.add(pause(40));
        STEPS.add(use(table));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_feldherrentisch", "Feldherrentisch"));
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new io.github.sirschrolli.kingdomsatwar.network.ProvisionPayload(table))));
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new io.github.sirschrolli.kingdomsatwar.network.HealPayload(table))));
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new io.github.sirschrolli.kingdomsatwar.network.CampaignPayload(table, true))));
        STEPS.add(pause(20));
        STEPS.add(Step.screenshot("mp_feldherrentisch_antwort"));
        STEPS.add(Step.screen(minecraft -> null));

        // --- Söldnertafel ---
        BlockPos board = new BlockPos(-3, -60, 3);
        STEPS.add(Step.command("setblock -3 -60 3 kingdomsatwar:mercenary_board"));
        STEPS.add(pause(20));
        STEPS.add(use(board));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_soeldnertafel", "Söldnertafel"));
        STEPS.add(Step.screen(minecraft -> null));

        // --- Magietisch ---
        BlockPos magic = new BlockPos(0, -60, 4);
        STEPS.add(Step.command("setblock 0 -60 4 kingdomsatwar:magic_table"));
        STEPS.add(pause(20));
        STEPS.add(use(magic));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_magietisch", "Magietisch"));
        STEPS.add(Step.screen(minecraft -> null));

        // --- Siedlung gründen (Banner setzen) und ihr Fenster öffnen ---
        STEPS.add(Step.command("gamemode survival"));
        STEPS.add(Step.command("effect give @s minecraft:resistance infinite 255 true"));
        STEPS.add(Step.command("clear @s"));
        STEPS.add(Step.command("give @s kingdomsatwar:town_banner"));
        STEPS.add(pause(10));
        STEPS.add(Step.once(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(0);
            BlockPos ground = new BlockPos(0, -61, -4);
            minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground).add(0, 0.5, 0), Direction.UP, ground, false));
        }));
        STEPS.add(pause(30));
        STEPS.add(use(new BlockPos(0, -60, -4)));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_stadt", "Stadtfenster"));
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(Step.command("gamemode creative"));

        // --- Chronik, Fischbuch, Fischbeutel ---
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new io.github.sirschrolli.kingdomsatwar.network.RequestChroniclePayload())));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_chronik", "Chronik"));
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(Step.once(minecraft -> ClientPacketDistributor.sendToServer(new io.github.sirschrolli.kingdomsatwar.network.RequestFishBookPayload())));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_fischbuch", "Fischbuch"));
        STEPS.add(Step.screen(minecraft -> null));
        STEPS.add(Step.command("clear @s"));
        STEPS.add(Step.command("give @s kingdomsatwar:fish_bag"));
        STEPS.add(pause(10));
        STEPS.add(Step.once(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(0);
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        }));
        STEPS.add(pause(30));
        STEPS.add(screenshotIfOpen("mp_fischbeutel", "Fischbeutel"));
        STEPS.add(Step.screen(minecraft -> null));

        // --- Erzähl-Intro und Titelkarte (Server -> Client) ---
        STEPS.add(Step.command("kaw story card 1"));
        STEPS.add(pause(40));
        STEPS.add(Step.screenshot("mp_titelkarte"));

        STEPS.add(pause(20));
        STEPS.add(Step.once(minecraft -> {
            LOGGER.info("[MP] FERTIG – Probleme: {}", problems);
            minecraft.stop();
        }));
    }

    /** Rechtsklick auf einen Block (wie der Spieler mit leerer Hand). */
    private static Step use(BlockPos pos) {
        return Step.once(minecraft -> {
            minecraft.player.getInventory().setSelectedSlot(8);
            minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        });
    }

    /** Screenshot, wenn ein Fenster offen ist – sonst ein Problem im Protokoll. */
    private static Step screenshotIfOpen(String name, String what) {
        Step shot = Step.screenshot(name);
        return (minecraft, ticks) -> {
            if (ticks == 1) {
                if (minecraft.gui.screen() == null) {
                    problem("{} öffnet sich nicht", what);
                    return true;
                }
                LOGGER.info("[MP] {} offen: {}", what, minecraft.gui.screen().getClass().getSimpleName());
            }
            return shot.run(minecraft, ticks - 1);
        };
    }
}
