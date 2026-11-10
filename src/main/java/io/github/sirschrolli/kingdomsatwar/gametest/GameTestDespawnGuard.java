package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.gametest.framework.GameTestServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobDespawnEvent;

/**
 * Auf dem Testserver verschwinden keine Wesen von selbst. Viele Tests stellen einen Schein-Spieler in die Welt; bleibt einer
 * stehen, ließe Minecraft jedes Monster, das mehr als 128 Blöcke von ihm entfernt erscheint – also in den Prüfflächen
 * späterer Durchgänge –, sofort wieder verschwinden, und welche Tests das trifft, hinge nur davon ab, wie die Tests auf die
 * Durchgänge verteilt sind. Im echten Spiel ändert sich nichts.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
final class GameTestDespawnGuard {
    private GameTestDespawnGuard() {}

    @SubscribeEvent
    static void onDespawn(MobDespawnEvent event) {
        if (event.getLevel().getLevel().getServer() instanceof GameTestServer) {
            event.setResult(MobDespawnEvent.Result.DENY);
        }
    }
}
