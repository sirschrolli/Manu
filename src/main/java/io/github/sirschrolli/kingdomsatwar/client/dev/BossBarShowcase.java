package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.boss.BossBarRenderer;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.util.Arrays;
import java.util.Deque;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.util.Util;
import net.minecraft.world.BossEvent;

/**
 * Probe der eigenen Bossleisten ({@code boss_bars}, seit 0.94 {@code bossleisten}): Leisten direkt in der Anzeige, ohne dass
 * ein Boss im Spiel ist – alle Leisten auf drei Bildern, dann in Nahaufnahme (verlangsamt) das Erscheinen, ein Treffer, das
 * Zerspringen eines Phasen-Edelsteins und die letzte Phase, zuletzt zwei Leisten so, wie sie im Spiel oben stehen.
 */
final class BossBarShowcase {
    private static final String[] ALL = {"fire", "ice", "lightning", "nature", "earth", "water", "shadow", "void", "reef", "kraken",
        "moth", "forge", "star", "mystic", "anni", "anni2", "schrolli", "schrolli_true", "schrolli_almighty", "world_eater",
        "mage_fire", "mage_ice", "mage_lightning", "mage_nature", "mage_earth", "mage_water"};
    private static final float[] LEVELS = {0.92f, 0.55f, 0.2f, 0.74f, 0.42f, 0.14f, 0.63f, 0.28f, 0.86f, 0.47f};

    private BossBarShowcase() {}

    static void bars(Deque<Step> steps) {
        setup(steps);
        show(steps, "bossleisten_schrolli", new String[] {"schrolli", "schrolli_true", "schrolli_almighty"}, new float[] {0.85f, 0.55f, 0.35f});
        show(steps, "bossleisten_neu", new String[] {"schrolli_almighty", "world_eater"}, new float[] {0.8f, 0.45f});
        show(steps, "bossleisten_letzte_phase", new String[] {"schrolli_almighty", "world_eater"}, new float[] {0.22f, 0.12f});
        steps.add(Step.once(minecraft -> minecraft.gui.hud.getBossOverlay().reset()));
        steps.add(Step.quit());
    }

    /** Seit 0.94: alle Leisten, dann die Bewegungen in Nahaufnahme, zuletzt im Spiel. */
    static void gallery(Deque<Step> steps) {
        setup(steps);
        steps.add(Step.hud(false));
        // alle Leisten, je zehn auf einem Bild
        for (int page = 0; page * 10 < ALL.length; page++) {
            String[] keys = Arrays.copyOfRange(ALL, page * 10, Math.min(ALL.length, page * 10 + 10));
            float[] progress = Arrays.copyOf(LEVELS, keys.length);
            steps.add(Step.once(minecraft -> {
                BossBarRenderer.devReset();
                minecraft.gui.setScreen(new BossBarGalleryScreen(keys, progress, 2, 16));
            }));
            steps.add(Step.waitTicks(60));
            steps.add(Step.screenshot("bossleisten_galerie_" + (page + 1)));
        }

        // Nahaufnahmen (vierfach): der Kraken und der Weltenfresser
        steps.add(Step.once(minecraft -> {
            minecraft.options.guiScale().set(4);
            minecraft.resizeGui();
        }));
        BossBarGalleryScreen[] screen = new BossBarGalleryScreen[1];
        String[] pair = {"kraken", "world_eater"};
        // das Erscheinen: verlangsamt (ein Fünftel), Bilder mitten im Herabsenken und beim Aufladen
        steps.add(Step.once(minecraft -> {
            BossBarRenderer.devReset();
            slow(0.2f);
            screen[0] = new BossBarGalleryScreen(pair, new float[] {0.82f, 0.71f}, 1, 30);
            minecraft.gui.setScreen(screen[0]);
        }));
        steps.add(Step.waitTicks(28));
        steps.add(Step.screenshot("bossleisten_auftritt_1"));
        steps.add(Step.waitTicks(55));
        steps.add(Step.screenshot("bossleisten_auftritt_2"));
        steps.add(Step.once(minecraft -> BossBarRenderer.devClock(null)));
        steps.add(Step.waitTicks(50));
        steps.add(Step.screenshot("bossleisten_voll"));
        // der Glanz, der über die Rahmen zieht (festgehalten an zwei Stellen)
        steps.add(Step.once(minecraft -> BossBarRenderer.devGlint(0.3f)));
        steps.add(Step.screenshot("bossleisten_glanz_1"));
        steps.add(Step.once(minecraft -> BossBarRenderer.devGlint(0.62f)));
        steps.add(Step.screenshot("bossleisten_glanz_2"));
        steps.add(Step.once(minecraft -> BossBarRenderer.devGlint(-1.0f)));
        // ein Treffer (verlangsamt): der Rahmen bebt, das Stück blitzt weiß, das Bildnis rot
        steps.add(Step.once(minecraft -> {
            slow(0.15f);
            screen[0].progress[0] = 0.74f;
            screen[0].progress[1] = 0.69f;
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("bossleisten_treffer"));
        steps.add(Step.once(minecraft -> BossBarRenderer.devClock(null)));
        steps.add(Step.waitTicks(40));
        // eine Phase ist erreicht: der Edelstein zerspringt (verlangsamt)
        steps.add(Step.once(minecraft -> {
            slow(0.25f);
            screen[0].progress[0] = 0.62f;
            screen[0].progress[1] = 0.64f;
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("bossleisten_phase_1"));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("bossleisten_phase_2"));
        steps.add(Step.once(minecraft -> BossBarRenderer.devClock(null)));
        steps.add(Step.waitTicks(40));
        // die letzte Phase
        steps.add(Step.once(minecraft -> {
            screen[0].progress[0] = 0.21f;
            screen[0].progress[1] = 0.18f;
        }));
        steps.add(Step.waitTicks(70));
        steps.add(Step.screenshot("bossleisten_letzte_phase_1"));
        steps.add(Step.waitTicks(9));
        steps.add(Step.screenshot("bossleisten_letzte_phase_2"));

        // weitere Nahaufnahmen
        String[][] closeups = {{"fire", "ice"}, {"anni", "schrolli_almighty"}, {"reef", "mystic"}, {"void", "star"}};
        for (String[] keys : closeups) {
            steps.add(Step.once(minecraft -> {
                BossBarRenderer.devReset();
                minecraft.gui.setScreen(new BossBarGalleryScreen(keys, new float[] {0.52f, 0.27f}, 1, 30));
            }));
            steps.add(Step.waitTicks(60));
            steps.add(Step.screenshot("bossleisten_nah_" + keys[0]));
        }

        // im Spiel: zwei Leisten oben in der Anzeige (doppelt)
        steps.add(Step.once(minecraft -> {
            minecraft.gui.setScreen(null);
            minecraft.options.guiScale().set(2);
            minecraft.resizeGui();
            BossBarRenderer.devReset();
        }));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> {
            BossHealthOverlay overlay = minecraft.gui.hud.getBossOverlay();
            overlay.reset();
            String[] keys = {"kraken", "schrolli_true"};
            float[] progress = {0.58f, 0.36f};
            for (int i = 0; i < keys.length; i++) {
                BossEvent event = new BossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar." + keys[i]),
                        BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS) {};
                event.setProgress(progress[i]);
                overlay.update(ClientboundBossEventPacket.createAddPacket(event));
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("bossleisten_im_spiel"));
        steps.add(Step.once(minecraft -> minecraft.gui.hud.getBossOverlay().reset()));
        steps.add(Step.quit());
    }

    /** Die Leisten laufen ab jetzt verlangsamt (ohne Sprung in der Zeit). */
    private static void slow(float factor) {
        long start = Util.getMillis();
        BossBarRenderer.devClock(() -> start + (long) ((Util.getMillis() - start) * factor));
    }

    private static void setup(Deque<Step> steps) {
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamemode spectator",
                "tp @s 0 150 0 -35 8"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
    }

    /** Zeigt die Leisten {@code boss.kingdomsatwar.<key>} mit den Ständen {@code progress} und nimmt ein Bild auf. */
    private static void show(Deque<Step> steps, String shot, String[] keys, float[] progress) {
        steps.add(Step.once(minecraft -> {
            BossHealthOverlay overlay = minecraft.gui.hud.getBossOverlay();
            overlay.reset();
            for (int i = 0; i < keys.length; i++) {
                BossEvent event = new BossEvent(UUID.randomUUID(), Component.translatable("boss.kingdomsatwar." + keys[i]),
                        BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS) {};
                event.setProgress(progress[i]);
                overlay.update(ClientboundBossEventPacket.createAddPacket(event));
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot(shot));
    }
}
