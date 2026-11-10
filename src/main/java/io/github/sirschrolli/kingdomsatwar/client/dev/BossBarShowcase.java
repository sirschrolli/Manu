package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import java.util.Deque;
import java.util.UUID;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;

/**
 * Probe der eigenen Bossleisten ({@code boss_bars}): Leisten direkt in der Anzeige, ohne dass ein Boss im Spiel ist – die
 * drei Gestalten Schrollis untereinander, dann der Allmächtige und der Weltenfresser voll und in der letzten Phase.
 */
final class BossBarShowcase {
    private BossBarShowcase() {}

    static void bars(Deque<Step> steps) {
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        show(steps, "bossleisten_schrolli", new String[] {"schrolli", "schrolli_true", "schrolli_almighty"}, new float[] {0.85f, 0.55f, 0.35f});
        show(steps, "bossleisten_neu", new String[] {"schrolli_almighty", "world_eater"}, new float[] {0.8f, 0.45f});
        show(steps, "bossleisten_letzte_phase", new String[] {"schrolli_almighty", "world_eater"}, new float[] {0.22f, 0.12f});
        steps.add(Step.once(minecraft -> minecraft.gui.hud.getBossOverlay().reset()));
        steps.add(Step.quit());
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
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot(shot));
    }
}
