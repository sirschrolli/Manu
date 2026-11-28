package io.github.sirschrolli.kingdomsatwar.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Ruhm (seit 1.12): Die Heldenstufe steht für alle sichtbar vor dem Namen – über dem Kopf, im Chat und in der Spielerliste,
 * die Zahl in der Farbe des Rangs („[47] Dev“). Dafür bekommt jeder Rechner die Stufe der anderen zugeschickt (nur die
 * Stufe, nicht die Talente), und zwar nur, wenn sie sich ändert. Titel wie der Weltenretter stehen dahinter.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HeroFame {
    private HeroFame() {}

    /** Die Stufe, die alle sehen (0: noch nicht bekannt). */
    public static int level(Player player) {
        return player.getData(ModAttachments.HERO_FAME);
    }

    /** Nach einer neuen Stufe (und beim Einloggen): allen die Stufe zeigen und die Namen neu bilden. */
    static void publish(ServerPlayer player, int level) {
        if (level(player) != level) {
            player.setData(ModAttachments.HERO_FAME, level);
            player.refreshDisplayName();
            player.refreshTabListName();
        }
    }

    /** Die Stufe vor dem Namen: „[47] Dev“, die Zahl in der Farbe des Rangs. */
    public static MutableComponent prefixed(Component name, int level) {
        HeroLevel.Rank rank = HeroLevel.Rank.of(level);
        return Component.empty()
                .append(Component.literal("[" + level + "]").withColor(rank.color() & 0xFFFFFF))
                .append(Component.literal(" "))
                .append(name);
    }

    /** Als letztes: Titel anderer Teile (Weltenretter, Gottestöter) stehen dann zwischen Stufe und Name. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onNameFormat(PlayerEvent.NameFormat event) {
        int level = level(event.getEntity());
        if (level > 0) {
            event.setDisplayname(prefixed(event.getDisplayname(), level));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        int level = level(event.getEntity());
        if (level > 0) {
            Component name = event.getDisplayName() != null ? event.getDisplayName() : event.getEntity().getName();
            event.setDisplayName(prefixed(name, level));
        }
    }

    /**
     * Auf jedem Rechner einmal je Sekunde die Namen neu bilden: Die Stufe eines anderen Spielers kommt erst nach ihm an,
     * und eine neue Stufe soll auch über seinem Kopf stehen.
     */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 20 == 11 && level(player) > 0) {
            player.refreshDisplayName();
        }
    }
}
