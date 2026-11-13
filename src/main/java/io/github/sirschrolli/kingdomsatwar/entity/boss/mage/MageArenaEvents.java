package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * Das versiegelte Sanktum eines Erzmagiers: Solange der Kampf dauert, hält seine Magie Boden, Mauern und Fenster zusammen –
 * nichts lässt sich abbauen, hinzusetzen oder wegsprengen, und keine Enderperle trägt hinaus.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class MageArenaEvents {
    private MageArenaEvents() {}

    /** Hält ein Erzmagier diese Stelle gerade versiegelt? */
    public static boolean sealed(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        return !server.getEntitiesOfClass(MageBossEntity.class, new AABB(pos).inflate(12.0, 14.0, 12.0), mage -> mage.seals(pos)).isEmpty();
    }

    private static void refuse(Player player) {
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.mage.sealed").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @SubscribeEvent
    static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        if (event.getLevel() instanceof Level level && sealed(level, event.getPos())) {
            event.setCanceled(true);
            refuse(event.getPlayer());
        }
    }

    @SubscribeEvent
    static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && sealed(level, event.getPos())) {
            event.setCanceled(true);
            if (event.getEntity() instanceof Player player) {
                refuse(player);
            }
        }
    }

    @SubscribeEvent
    static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> sealed(event.getLevel(), pos));
    }

    @SubscribeEvent
    static void onPearl(EntityTeleportEvent.EnderPearl event) {
        if (sealed(event.getPlayer().level(), event.getPlayer().blockPosition())) {
            event.setCanceled(true);
            refuse(event.getPlayer());
        }
    }

    @SubscribeEvent
    static void onChorus(EntityTeleportEvent.ItemConsumption event) {
        if (event.getEntity() instanceof Player player && sealed(player.level(), player.blockPosition())) {
            event.setCanceled(true);
            refuse(player);
        }
    }
}
