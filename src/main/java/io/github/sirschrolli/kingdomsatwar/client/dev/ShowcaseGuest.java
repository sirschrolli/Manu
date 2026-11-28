package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Nur für Vorführungen: ein zweiter Spieler ohne eigenen Client (was der Server ihm schickt, verschwindet). Er steht still,
 * wo man ihn hinstellt, und lässt sich vom Server aus bewegen und kämpfen lassen – so lassen sich Mehrspieler-Fenster und
 * gemeinsame Kämpfe im Einzelspieler zeigen.
 */
final class ShowcaseGuest {
    private ShowcaseGuest() {}

    /** Lässt {@code name} mitspielen: an {@code pos}, mit Blick nach {@code yaw}, {@code hand} in der Hand. */
    static ServerPlayer join(ServerLevel level, String name, Vec3 pos, float yaw, ItemStack hand, GameType mode) {
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(id, name), false);
        ServerPlayer guest = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        guest.snapTo(pos.x, pos.y, pos.z, yaw, 0.0f);
        guest.setItemInHand(InteractionHand.MAIN_HAND, hand);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, guest, cookie);
        guest.setGameMode(mode);
        guest.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), yaw, 0.0f, true);
        guest.setYHeadRot(yaw);
        guest.setYBodyRot(yaw);
        return guest;
    }

    /** Er geht wieder. */
    static void leave(ServerLevel level, ServerPlayer guest) {
        level.getServer().getPlayerList().remove(guest);
    }
}
