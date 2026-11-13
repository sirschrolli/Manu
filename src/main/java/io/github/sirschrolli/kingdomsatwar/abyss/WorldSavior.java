package io.github.sirschrolli.kingdomsatwar.abyss;

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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Der Ehrentitel „Weltenretter“: Wer beim Sieg über den Weltenfresser am Schlund der Welten war, trägt ihn für immer – vor
 * seinem Namen über dem Kopf, im Chat und in der Spielerliste. Beim Sieg erscheint er groß auf dem Bildschirm, und alle
 * erfahren davon.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class WorldSavior {
    private static final int GOLD = 0xFFC94A;

    private WorldSavior() {}

    public static boolean is(Player player) {
        return player.getData(ModAttachments.WORLD_SAVIOR.get());
    }

    /** Alle Spieler im Umkreis werden Weltenretter. */
    public static void grantAround(ServerLevel level, Vec3 center, double radius) {
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && player.position().distanceTo(center) <= radius) {
                grant(player);
            }
        }
    }

    public static void grant(ServerPlayer player) {
        boolean first = !is(player);
        player.setData(ModAttachments.WORLD_SAVIOR.get(), true);
        player.refreshDisplayName();
        player.refreshTabListName();
        player.connection.send(new ClientboundSetTitlesAnimationPacket(20, 100, 40));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.kingdomsatwar.world_savior").withColor(GOLD)
                .withStyle(ChatFormatting.BOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("title.kingdomsatwar.world_savior.sub")
                .withColor(0xFFE6B0)));
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (first) {
            Component message = Component.translatable("message.kingdomsatwar.world_savior", player.getName().copy().withColor(GOLD))
                    .withColor(0xFFE6B0);
            for (ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
                other.sendSystemMessage(message);
            }
        }
    }

    /** Der Titel vor dem Namen (über dem Kopf und im Chat). */
    public static MutableComponent prefixed(Component name) {
        return Component.empty().append(Component.translatable("title.kingdomsatwar.world_savior.prefix").withColor(GOLD))
                .append(Component.literal(" ")).append(name);
    }

    @SubscribeEvent
    static void onNameFormat(PlayerEvent.NameFormat event) {
        if (is(event.getEntity())) {
            event.setDisplayname(prefixed(event.getDisplayname()));
        }
    }

    @SubscribeEvent
    static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        if (is(event.getEntity())) {
            Component name = event.getDisplayName() != null ? event.getDisplayName() : event.getEntity().getName();
            event.setDisplayName(prefixed(name));
        }
    }

    /** Auf jedem Rechner den Namen ab und zu neu bilden – der Titel kommt erst nach dem Spieler an. */
    @SubscribeEvent
    static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 40 == 7 && is(player)) {
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
