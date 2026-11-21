package io.github.sirschrolli.kingdomsatwar.world;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Schattentor im Siegelkreis: führt in die Schattenwelt und von dort zurück. Solange die Welt keine Schattenwelt
 * kennt, bleibt das Tor ein dunkler Strudel, der niemanden durchlässt.
 */
public final class ShadowGate {
    public static final ResourceKey<Level> SHADOW_WORLD = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id("shadow_world"));

    private ShadowGate() {}

    /** Lässt das Tor dieses Wesen durch? Spieler erfahren sonst, warum nicht. */
    public static boolean isOpen(ServerLevel level, Entity entity) {
        if (level.dimension() == SHADOW_WORLD || level.getServer().getLevel(SHADOW_WORLD) != null) {
            return true;
        }
        if (entity instanceof ServerPlayer player && (level.getGameTime() + player.getId()) % 40 == 0) {
            player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.shadow_portal.sealed").withStyle(ChatFormatting.DARK_PURPLE));
        }
        return false;
    }

    /**
     * Ziel einer Reise durch das Tor (null: das Tor führt nirgendwohin): vom Siegelkreis auf die Plattform des Gegentors
     * in der Schattenwelt (beim ersten Mal wird sie gebaut), vom Gegentor zurück an den Siegelkreis.
     */
    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
        TeleportTransition.PostTeleportTransition after = TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET);
        if (level.dimension() == SHADOW_WORLD) {
            ServerLevel overworld = level.getServer().overworld();
            BlockPos center = SealState.of(level).center();
            BlockPos target = center != null ? center.offset(0, 1, 4)
                    : overworld.getRespawnData().pos();
            return new TeleportTransition(overworld, Vec3.atBottomCenterOf(target), Vec3.ZERO, 180.0f, 0.0f, after);
        }
        ServerLevel shadow = level.getServer().getLevel(SHADOW_WORLD);
        if (shadow == null) {
            return null;
        }
        BlockPos gate = io.github.sirschrolli.kingdomsatwar.shadow.CounterGate.ensure(shadow, entry);
        BlockPos arrival = io.github.sirschrolli.kingdomsatwar.shadow.CounterGate.arrival(gate);
        return new TeleportTransition(shadow, Vec3.atBottomCenterOf(arrival), Vec3.ZERO, 180.0f, 0.0f, after);
    }
}
