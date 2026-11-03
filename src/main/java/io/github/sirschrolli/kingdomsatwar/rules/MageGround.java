package io.github.sirschrolli.kingdomsatwar.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

/**
 * Der Boden unter dem Erzmagier ist unzerstörbar: der Lichtstein, auf dem er im Sanktum meditiert, und der Block, auf dem
 * er gerade steht. Niemand kann ihn darunter wegschlagen, wegsprengen oder ihn so aus seinem Turm stürzen lassen.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class MageGround {
    private MageGround() {}

    /** Steht (oder meditiert) hier ein lebender Erzmagier über diesem Block? */
    public static boolean isProtected(Level level, BlockPos pos) {
        for (MageBossEntity mage : level.getEntitiesOfClass(MageBossEntity.class, new AABB(pos).inflate(24.0), Entity::isAlive)) {
            BlockPos home = mage.home();
            if (home != null && home.below().equals(pos) || mage.blockPosition().below().equals(pos) || mage.getOnPos().equals(pos)) {
                return true;
            }
        }
        return false;
    }

    /** Wie Grundgestein: kein Abbaufortschritt (auch auf dem Client, damit kein Riss erscheint). */
    @SubscribeEvent
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getPosition().isPresent() && isProtected(event.getEntity().level(), event.getPosition().get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        if (event.getLevel() instanceof Level level && isProtected(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> isProtected(event.getLevel(), pos));
    }
}
