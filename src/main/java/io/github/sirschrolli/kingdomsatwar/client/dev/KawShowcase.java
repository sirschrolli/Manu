package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.command.KawLocate;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Vorführung von {@code /kaw} (seit 1.1.2): Bosse und Bauwerke aus der Oberwelt heraus finden (der Chat mit den anklickbaren
 * Koordinaten), ein Klick bringt einen in den Abgrund zum Uhrturm, dann eine Rundreise durch alle Dimensionen – nach jeder
 * Landung steht im Protokoll, worauf man steht ({@code [Showcase] Reise …}).
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = KingdomsAtWar.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
final class KawShowcase {
    /** Die Rundreise: jede Art zu landen, jede Dimension. */
    private static final String[] TOUR = {
            // Oberwelt
            "fachwerkdorf", "raubritterburg", "wasserstollen", "pruefungsarena", "korallenpalast", "vulkankrater", "magierturm_eis",
            "karawanserei", "stammburg", "fehlerzone", "atoll",
            // Nether, End
            "aschenesse", "hortfeste", "verlorene_pforte", "riesengebeine", "sternwarte",
            // Schattenwelt, Leere, Abgrund
            "zitadelle", "seelenpferch", "umbrerdorf", "leuchtturm", "thron", "sternennetz", "schlund_der_welten",
            // Urmeer, Feenreich
            "krakenschlund", "hexengrotte", "meeresdorf", "mondhain", "zauberbuehne", "mondsteinbruch"};

    private KawShowcase() {}

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    static void tour(Deque<Step> steps) {
        for (String command : new String[] {"gamerule advance_time false", "time set 6000", "weather clear", "gamerule spawn_mobs false",
                "gamemode creative"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.waitForChunks(600));
        if (Step.wanted("kaw_chat")) {
            // der Chat: Bosse und Bauwerke anderer Dimensionen, aus der Oberwelt gesucht (die Rückmeldungen der Befehle braucht es dafür)
            steps.add(Step.hud(true));
            steps.add(Step.command("gamerule send_command_feedback true"));
            for (String command : new String[] {"kaw boss stundenwaechter", "kaw boss sternenweberin", "kaw structure sternwarte",
                    "kaw structure fachwerkdorf", "kaw boss karkinos", "kaw boss malleus", "kaw structure mondhain"}) {
                steps.add(Step.command(command));
                steps.add(Step.waitTicks(30));
            }
            // warten, bis die Antwort auf den letzten Befehl da ist (das Suchen hält den Server auf)
            steps.add(Step.until(() -> chatLines() >= 7, 1200));
            steps.add(Step.waitTicks(20));
            steps.add(Step.once(minecraft -> {
                minecraft.gui.openChatScreen(net.minecraft.client.gui.components.ChatComponent.ChatMethod.MESSAGE);
                KingdomsAtWar.LOGGER.info("[Showcase] Chat offen: {}, Zeilen {}", minecraft.gui.screen(), chatLines());
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot("kaw_chat"));
            steps.add(Step.screen(minecraft -> null));
        }
        // ein Klick auf die Koordinaten des Uhrturms (dasselbe Paket, das der Klick im Chat schickt)
        steps.add(Step.once(minecraft -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("site", "uhrturm");
            tag.putString("dim", KawPlaces.CLOCK_TOWER.dimension().identifier().toString());
            tag.putInt("x", 0);
            tag.putInt("y", 0);
            tag.putInt("z", 0);
            minecraft.player.connection.send(new ServerboundCustomClickActionPacket(KawLocate.TRAVEL, Optional.of(tag)));
        }));
        steps.add(Step.waitTicks(60));
        steps.add(AbyssShowcase.settle(1400));
        steps.add(report("klick_uhrturm"));
        steps.add(Step.screenshot("kaw_klick_uhrturm"));
        // die Rundreise
        for (String site : TOUR) {
            if (!Step.wanted("kaw_" + site)) {
                continue;
            }
            steps.add(Step.hud(false));
            Object[] before = new Object[2];
            steps.add(Step.once(minecraft -> {
                before[0] = minecraft.player.level().dimension();
                before[1] = minecraft.player.position();
            }));
            steps.add(Step.command("kaw structure " + site + " tp"));
            // erst weiter, wenn man wirklich dort ist (das Suchen und Erzeugen hält den Server eine Weile auf)
            steps.add(Step.until(() -> {
                var player = net.minecraft.client.Minecraft.getInstance().player;
                return player != null && (player.level().dimension() != before[0]
                        || player.position().distanceTo((net.minecraft.world.phys.Vec3) before[1]) > 8.0);
            }, 2400));
            steps.add(Step.waitTicks(40));
            steps.add(AbyssShowcase.settle(1200));
            steps.add(report(site));
            steps.add(Step.screenshot("kaw_" + site));
        }
    }

    /** Wie viele Antworten von /kaw im Chat stehen (Zeilen mit Koordinaten in eckigen Klammern). */
    private static int chatLines() {
        int count = 0;
        for (var line : CHAT) {
            if (line.contains("[") && line.contains("]")) {
                count++;
            }
        }
        return count;
    }

    /** Was im Chat ankam (für das Warten oben). */
    private static final java.util.List<String> CHAT = new java.util.concurrent.CopyOnWriteArrayList<>();

    @net.neoforged.bus.api.SubscribeEvent
    static void onSystemChat(net.neoforged.neoforge.client.event.ClientChatReceivedEvent.System event) {
        CHAT.add(event.getMessage().getString());
    }

    /** Ins Protokoll: wo man gelandet ist und worauf man steht. */
    private static Step report(String name) {
        return Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = player.level();
            BlockPos feet = player.blockPosition();
            KingdomsAtWar.LOGGER.info("[Showcase] Reise {}: {} {} Boden={} Füße={} Kopf={} sicher={}", name, level.dimension().identifier(), feet,
                    level.getBlockState(feet.below()).getBlock(), level.getBlockState(feet).getBlock(), level.getBlockState(feet.above()).getBlock(),
                    KawPlaces.standable(level, feet, true));
        });
    }
}
