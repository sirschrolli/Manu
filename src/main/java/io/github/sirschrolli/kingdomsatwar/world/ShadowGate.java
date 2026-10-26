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

    /** Ziel einer Reise durch das Tor (null: das Tor führt nirgendwohin). */
    public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
        return null;
    }
}
