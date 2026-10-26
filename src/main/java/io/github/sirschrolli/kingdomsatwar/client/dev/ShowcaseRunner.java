package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FileUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.slf4j.Logger;

/**
 * Nur für die Entwicklung: erzeugt automatisch eine Testwelt, baut Szenen auf und macht Screenshots.
 *
 * <p>Aktiv ausschließlich mit der JVM-Option {@code -Dkingdomsatwar.showcase=<szenen>} (z. B. {@code blocks,items}).
 * Im normalen Spiel passiert hier nichts.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ShowcaseRunner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SCENES = System.getProperty("kingdomsatwar.showcase");

    private static final Deque<Step> STEPS = new ArrayDeque<>();
    private static boolean worldRequested;
    private static boolean scenesQueued;
    private static int waitTicks;
    private static int stepTicks;

    private ShowcaseRunner() {}

    public static boolean enabled() {
        return SCENES != null && !SCENES.isBlank();
    }

    /**
     * Fügt Schritte direkt hinter dem gerade laufenden ein (für Szenen, deren Ablauf erst beim Spielen feststeht, z. B.
     * je Gebäude eines Dorfs ein Bild). Nur aus einem laufenden Schritt heraus aufrufen.
     */
    static void insertSteps(Deque<Step> extra) {
        Step current = STEPS.poll();
        java.util.Iterator<Step> reversed = extra.descendingIterator();
        while (reversed.hasNext()) {
            STEPS.addFirst(reversed.next());
        }
        if (current != null) {
            STEPS.addFirst(current);
        }
    }

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!enabled() || worldRequested || !(event.getScreen() instanceof TitleScreen title)) {
            return;
        }
        worldRequested = true;
        Minecraft minecraft = Minecraft.getInstance();
        boolean flat = !(SCENES.contains("terrain") || SCENES.contains("lair") || SCENES.contains("seal"));
        try {
            String folder = FileUtil.findAvailableName(minecraft.getLevelSource().getBaseDir(), "Showcase", "");
            LevelSettings settings = new LevelSettings("Showcase", GameType.CREATIVE,
                    new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
            // Bauwerke: in echten Welten immer, in der flachen Testwelt nur für Szenen, die sie brauchen (z. B. Dörfer)
            boolean structures = !flat || SCENES.contains("village") || SCENES.contains("citadel") || SCENES.contains("nyxar");
            WorldOptions options = new WorldOptions("kingdoms at war".hashCode(), structures, false);
            WorldOpenFlows flows = minecraft.createWorldOpenFlows();
            LOGGER.info("[Showcase] Erzeuge Welt '{}' (flach: {})", folder, flat);
            flows.createFreshLevel(folder, settings, options,
                    flat ? WorldPresets::createTestWorldDimensions : WorldPresets::createNormalWorldDimensions, title);
        } catch (IOException e) {
            LOGGER.error("[Showcase] Welt konnte nicht erzeugt werden", e);
        }
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (!enabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.getSingleplayerServer() == null) {
            return;
        }
        if (!scenesQueued) {
            scenesQueued = true;
            Showcase.queue(SCENES, STEPS);
            LOGGER.info("[Showcase] {} Schritte geplant", STEPS.size());
        }
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        Step step = STEPS.peek();
        if (step == null) {
            return;
        }
        stepTicks++;
        if (step.run(minecraft, stepTicks)) {
            STEPS.poll();
            stepTicks = 0;
        }
    }

    /** Ein Schritt im Ablauf. Gibt {@code true} zurück, wenn er fertig ist. */
    @FunctionalInterface
    public interface Step {
        boolean run(Minecraft minecraft, int ticksInStep);

        static Step once(Consumer<Minecraft> action) {
            return (minecraft, ticks) -> {
                action.accept(minecraft);
                return true;
            };
        }

        static Step server(Consumer<ServerLevel> action) {
            return once(minecraft -> {
                var server = minecraft.getSingleplayerServer();
                server.execute(() -> action.accept(server.overworld()));
            });
        }

        static Step command(String command) {
            return once(minecraft -> minecraft.player.connection.sendCommand(command));
        }

        static Step waitTicks(int ticks) {
            return once(minecraft -> waitTicks = ticks);
        }

        /** Wartet, bis alle sichtbaren Chunks gebaut sind (höchstens {@code maxTicks}). */
        static Step waitForChunks(int maxTicks) {
            return (minecraft, ticks) -> ticks > maxTicks || (ticks > 20 && minecraft.levelRenderer.hasRenderedAllSections());
        }

        static Step until(BooleanSupplier condition, int maxTicks) {
            return (minecraft, ticks) -> ticks > maxTicks || condition.getAsBoolean();
        }

        static Step hud(boolean visible) {
            return once(minecraft -> {
                if (minecraft.gui.hud.isHidden() == visible) {
                    minecraft.gui.hud.toggle();
                }
                // Chat leeren und ausblenden, solange Screenshots entstehen (Befehlsrückmeldungen kämen sonst ins Bild)
                minecraft.options.chatVisibility().set(visible
                        ? net.minecraft.world.entity.player.ChatVisiblity.FULL
                        : net.minecraft.world.entity.player.ChatVisiblity.HIDDEN);
                if (!visible) {
                    minecraft.gui.hud.getChat().clearMessages(false);
                }
            });
        }

        static Step screen(java.util.function.Function<Minecraft, Screen> factory) {
            return once(minecraft -> minecraft.gui.setScreen(factory.apply(minecraft)));
        }

        static Step screenshot(String name) {
            // drei Ticks warten, damit Änderungen (HUD aus, Chat geleert) sicher im Bild angekommen sind
            return (minecraft, ticks) -> {
                if (ticks < 3) {
                    return false;
                }
                LOGGER.info("[Showcase] Screenshot {}", name);
                Screenshot.grab(minecraft.gameDirectory, name + ".png", minecraft.gameRenderer.mainRenderTarget(), 1,
                        message -> LOGGER.info("[Showcase] {}", message.getString()));
                return true;
            };
        }

        /**
         * Nimmt eine Bildfolge auf (für Videos): Das Spiel steht still ({@code /tick freeze}); für jedes Bild läuft genau
         * ein Spieltick ({@code /tick step 1}), dann wird – nach zwei gezeichneten Bildern – ein Screenshot gemacht.
         * So entsteht bei 20 Bildern je Sekunde ein Video in Echtzeit, egal wie langsam der Rechner zeichnet.
         * {@code beforeFrame} läuft vor jedem Tick (z. B. um die Kamera nachzuführen).
         */
        static Step record(String name, int frames, Consumer<Minecraft> beforeFrame) {
            int[] state = new int[3];   // Bild, Phase, Wartezeit
            return (minecraft, ticks) -> {
                switch (state[1]) {
                    case 0 -> {
                        if (state[0] == 0) {
                            minecraft.player.connection.sendCommand("tick freeze");
                        }
                        beforeFrame.accept(minecraft);
                        minecraft.player.connection.sendCommand("tick step 1");
                        state[1] = 1;
                        state[2] = 0;
                    }
                    case 1 -> {
                        if (++state[2] >= 3) {
                            state[1] = 2;
                        }
                    }
                    default -> {
                        Screenshot.grab(minecraft.gameDirectory, String.format(java.util.Locale.ROOT, "%s_%04d.png", name, state[0]),
                                minecraft.gameRenderer.mainRenderTarget(), 1, message -> { });
                        state[0]++;
                        state[1] = 0;
                    }
                }
                if (state[0] >= frames) {
                    minecraft.player.connection.sendCommand("tick unfreeze");
                    LOGGER.info("[Showcase] Bildfolge {} fertig ({} Bilder)", name, frames);
                    return true;
                }
                return false;
            };
        }

        static Step quit() {
            return once(minecraft -> {
                LOGGER.info("[Showcase] Fertig, beende das Spiel");
                minecraft.stop();
            });
        }
    }
}
