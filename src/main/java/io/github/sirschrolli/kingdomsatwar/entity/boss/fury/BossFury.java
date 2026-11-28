package io.github.sirschrolli.kingdomsatwar.entity.boss.fury;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.PartyBoss;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Hängt jedem Boss seinen {@link Fury Zorn} an (seit 1.14) und lässt ihn mitlaufen. Ein Boss ohne Profil (siehe
 * {@link FuryProfile}) bleibt, wie er ist; ein Krieger eines Reichs bekommt ihn erst, wenn er dessen Fürst ist.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class BossFury {
    private static final Map<Mob, Fury> FURIES = new WeakHashMap<>();

    private BossFury() {}

    /** Der Zorn dieses Bosses (null: er hat keinen). */
    public static @Nullable Fury of(Mob boss) {
        return FURIES.get(boss);
    }

    /** Beginnt sofort einen Zornangriff (für Tests und Vorführungen; ein laufender bricht ab). */
    public static boolean force(Mob boss, FuryAttack attack) {
        Fury fury = attach(boss);
        if (fury == null) {
            return false;
        }
        fury.stop();
        fury.start(attack);
        return true;
    }

    /** Lässt den Boss sofort in Zorn geraten (Wutausbruch). */
    public static boolean enrage(Mob boss) {
        Fury fury = attach(boss);
        if (fury == null || fury.enraged) {
            return false;
        }
        fury.stop();
        fury.enrage();
        return true;
    }

    /** Hält die Zornangriffe so lange zurück (Ticks; für Tests und Vorführungen). */
    public static void hold(Mob boss, int ticks) {
        Fury fury = attach(boss);
        if (fury != null) {
            fury.cooldown = Math.max(fury.cooldown, ticks);
            fury.novaCooldown = Math.max(fury.novaCooldown, ticks);
        }
    }

    /** Nur Gegner in diesem Bereich werden getroffen (Tests: die Nachbartests bleiben verschont). */
    public static void confine(Mob boss, @Nullable AABB area) {
        Fury fury = attach(boss);
        if (fury != null) {
            fury.confine = area;
        }
    }

    public static @Nullable FuryAttack current(Mob boss) {
        Fury fury = FURIES.get(boss);
        return fury == null ? null : fury.current;
    }

    public static boolean enraged(Mob boss) {
        Fury fury = FURIES.get(boss);
        return fury != null && fury.enraged;
    }

    /** Hat der laufende (oder letzte) Zornangriff dieses Wesen getroffen? */
    public static boolean struck(Mob boss, net.minecraft.world.entity.Entity victim) {
        Fury fury = FURIES.get(boss);
        return fury != null && fury.victims.contains(victim.getId());
    }

    /** Legt den Zorn an, falls der Boss einen hat und noch keinen bekommen hat. */
    static @Nullable Fury attach(Mob boss) {
        Fury fury = FURIES.get(boss);
        if (fury != null || boss.level().isClientSide() || !(boss instanceof PartyBoss party) || !party.isPartyBoss() || !boss.isAlive()) {
            return fury;
        }
        FuryProfile profile = FuryProfile.of(boss);
        if (profile == null) {
            return null;
        }
        fury = new Fury(boss, profile);
        FURIES.put(boss, fury);
        return fury;
    }

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob && mob instanceof PartyBoss) {
            attach(mob);
        }
    }

    @SubscribeEvent
    static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof PartyBoss) || mob.level().isClientSide()) {
            return;
        }
        Fury fury = FURIES.get(mob);
        if (fury == null && mob.tickCount % 20 == 0) {
            // ein Krieger wird erst später zum Fürsten eines Reichs
            fury = attach(mob);
        }
        if (fury != null) {
            fury.tick();
        }
    }

    @SubscribeEvent
    static void onLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob) {
            Fury fury = FURIES.remove(mob);
            if (fury != null) {
                fury.stop();
            }
        }
    }
}
