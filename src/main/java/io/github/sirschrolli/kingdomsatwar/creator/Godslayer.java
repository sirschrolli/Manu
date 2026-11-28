package io.github.sirschrolli.kingdomsatwar.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Der höchste Ehrentitel: „Gottbezwinger“. Wer beim Sieg über Schrolli den Schöpfer in seinem Hof war, trägt ihn für immer –
 * vor seinem Namen über dem Kopf, im Chat und in der Spielerliste (er ersetzt den „Weltenretter“). Beim Sieg erscheint er
 * groß auf dem Bildschirm, und alle erfahren davon.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Godslayer {
    private static final int COLOR = 0xE8B4FF;

    private Godslayer() {}

    public static boolean is(Player player) {
        return player.getData(ModAttachments.GODSLAYER.get());
    }

    /** Alle Spieler im Umkreis werden Gottbezwinger. */
    public static void grantAround(ServerLevel level, Vec3 center, double radius) {
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && player.position().distanceTo(center) <= radius) {
                grant(player);
            }
        }
    }

    public static void grant(ServerPlayer player) {
        boolean first = !is(player);
        player.setData(ModAttachments.GODSLAYER.get(), true);
        player.refreshDisplayName();
        player.refreshTabListName();
        player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 120, 40));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.kingdomsatwar.godslayer").withColor(COLOR)
                .withStyle(ChatFormatting.BOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("title.kingdomsatwar.godslayer.sub").withColor(0xFFF2C4)));
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 0.8f);
        if (first) {
            Component message = Component.translatable("message.kingdomsatwar.godslayer", player.getName().copy().withColor(COLOR)).withColor(0xFFF2C4);
            for (ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
                other.sendSystemMessage(message);
            }
        }
    }

    /** Der Titel vor dem Namen (über dem Kopf und im Chat). */
    public static MutableComponent prefixed(Component name) {
        return Component.empty().append(Component.translatable("title.kingdomsatwar.godslayer.prefix").withColor(COLOR))
                .append(Component.literal(" ")).append(name);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onNameFormat(PlayerEvent.NameFormat event) {
        if (is(event.getEntity())) {
            event.setDisplayname(prefixed(event.getEntity().getName()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (is(event.getEntity())) {
            event.setDisplayName(prefixed(event.getEntity().getName()));
        }
    }

    /** Auf jedem Rechner den Namen ab und zu neu bilden – der Titel kommt erst nach dem Spieler an. */
    @SubscribeEvent
    static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 40 == 13 && is(player)) {
            player.refreshDisplayName();
        }
    }

    /** Nach dem Wiederbeleben und Einloggen den Namen neu bilden (der Titel bleibt). */
    @SubscribeEvent
    static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player && is(player)) {
            player.refreshDisplayName();
        }
    }
}
