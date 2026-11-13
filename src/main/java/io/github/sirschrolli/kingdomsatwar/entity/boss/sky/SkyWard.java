package io.github.sirschrolli.kingdomsatwar.entity.boss.sky;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Himmelswacht der Bosse (seit 0.82): Im Kampf achtet jeder Boss auf Flieger. Wer in seiner Nähe gleitet, sammelt
 * „Flugzeit“ – ein kurzer Satz durch die Luft bleibt folgenlos, doch nach zwei Sekunden (unter halber Lebenskraft des
 * Bosses schon nach gut einer) nimmt er den Flieger ins Visier: Über ihm lädt ein Siegel, eine Ziellinie zeigt auf den
 * Flieger, der hört den Warnton. Wer jetzt landet, dem geschieht nichts. Sonst löst sich der {@linkplain SkyBoltEntity
 * Bannpfeil} und jagt ihm nach. Danach braucht der Boss eine Weile, bis er das nächste Siegel laden kann.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class SkyWard {
    /** So weit (waagerecht) reicht die Himmelswacht. */
    public static final double RANGE = 64.0;
    /** So hoch über und so tief unter dem Boss gilt sie. */
    public static final double HEIGHT = 96.0;
    /** So viele Ticks darf man in der Nähe gleiten, bevor der Boss einen ins Visier nimmt. */
    public static final int NOTICE = 40;
    /** … und so viele, wenn er unter halber Lebenskraft ist. */
    public static final int NOTICE_ENRAGED = 25;
    /** So lange lädt das Siegel. */
    public static final int CHARGE = 30;
    /** … und so lange unter halber Lebenskraft. */
    public static final int CHARGE_ENRAGED = 22;
    /** Pause nach einem Bannpfeil. */
    public static final int COOLDOWN = 80;
    /** Kurze Pause, wenn das Siegel erloschen ist, weil der Flieger gelandet ist. */
    public static final int COOLDOWN_CANCELLED = 30;

    private static final class Watch {
        final Map<UUID, Integer> airTime = new HashMap<>();
        int cooldown;
        @Nullable SkyBoltEntity bolt;
    }

    private static final Map<Mob, Watch> WATCHES = new WeakHashMap<>();
    /** Bosse, die auch ohne Kampf wachen (Tests und Vorführung). */
    private static final Set<Mob> FORCED = Collections.newSetFromMap(new WeakHashMap<>());
    /** Tests: Dieser Boss achtet nur auf diesen einen Spieler … */
    private static final Map<Mob, UUID> ONLY = new WeakHashMap<>();
    /** … und diese Spieler beachtet dafür kein anderer Boss (sonst mischten sich die Bosse der Nachbartests ein). */
    private static final Set<UUID> RESERVED = new HashSet<>();

    private SkyWard() {}

    /** Der Sternenfresser (der Enderdrache in seinem eigenen Gewand) ist kein eigenes Wesen der Mod – er wacht, solange er lebt. */
    private static final SkyWarden STAR_EATER = new SkyWarden() {
        @Override
        public SkyTheme skyTheme() {
            return SkyTheme.STAR;
        }

        @Override
        public boolean guardsSky() {
            return true;
        }
    };

    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob boss) || !(boss.level() instanceof ServerLevel level)) {
            return;
        }
        if (boss instanceof SkyWarden warden) {
            tick(level, boss, warden);
        } else if (boss instanceof EnderDragon dragon && dragon.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.DYING) {
            tick(level, boss, STAR_EATER);
        }
    }

    /** Das Thema der Himmelswacht dieses Wesens – oder {@code null}, wenn es keine hat. */
    public static @Nullable SkyTheme themeOf(net.minecraft.world.entity.Entity entity) {
        return entity instanceof SkyWarden warden ? warden.skyTheme() : entity instanceof EnderDragon ? SkyTheme.STAR : null;
    }

    /** Tests und Vorführung: Dieser Boss wacht auch dann, wenn er gerade nicht kämpft (oder keine KI hat). */
    public static void force(Mob boss, boolean on) {
        if (on) {
            FORCED.add(boss);
        } else {
            FORCED.remove(boss);
            UUID only = ONLY.remove(boss);
            if (only != null) {
                RESERVED.remove(only);
            }
        }
    }

    /**
     * Tests: Dieser Boss wacht (auch ohne Kampf) nur über {@code player} – und kein anderer Boss beachtet diesen Spieler.
     * Mit {@link #force(Mob, boolean) force(boss, false)} endet das wieder.
     */
    public static void forceOnly(Mob boss, ServerPlayer player) {
        FORCED.add(boss);
        ONLY.put(boss, player.getUUID());
        RESERVED.add(player.getUUID());
    }

    /** Der Bannpfeil, den der Boss gerade lädt oder dem er nachsieht. */
    public static @Nullable SkyBoltEntity bolt(Mob boss) {
        Watch watch = WATCHES.get(boss);
        return watch == null || watch.bolt == null || watch.bolt.isRemoved() ? null : watch.bolt;
    }

    /** Wie viele Ticks dieser Spieler bei diesem Boss schon gleitet. */
    public static int airTime(Mob boss, ServerPlayer player) {
        Watch watch = WATCHES.get(boss);
        return watch == null ? 0 : watch.airTime.getOrDefault(player.getUUID(), 0);
    }

    /** Darf der Boss diesen Spieler vom Himmel holen? (gleitet in Reichweite, nicht im Kreativ- oder Zuschauermodus, nicht schon gebannt) */
    public static boolean flying(Mob boss, ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.isCreative() || !player.isFallFlying() || FlightBan.banned(player)) {
            return false;
        }
        double dx = player.getX() - boss.getX();
        double dz = player.getZ() - boss.getZ();
        return dx * dx + dz * dz <= RANGE * RANGE && Math.abs(player.getY() - boss.getY()) <= HEIGHT;
    }

    static void tick(ServerLevel level, Mob boss, SkyWarden warden) {
        Watch watch = WATCHES.get(boss);
        boolean forced = FORCED.contains(boss);
        if (!boss.isAlive() || (!forced && (boss.isNoAi() || !warden.guardsSky()))) {
            if (watch != null) {
                watch.airTime.clear();
            }
            return;
        }
        if (watch == null) {
            watch = new Watch();
            WATCHES.put(boss, watch);
        }
        if (watch.cooldown > 0) {
            watch.cooldown--;
        }
        if (watch.bolt != null && watch.bolt.isRemoved()) {
            if (watch.bolt.outcome() == SkyBoltEntity.Outcome.CANCELLED) {
                // der Flieger ist rechtzeitig gelandet: bald darf der Boss wieder
                watch.cooldown = Math.min(watch.cooldown, COOLDOWN_CANCELLED);
            }
            watch.bolt = null;
        }
        ServerPlayer worst = null;
        int worstTime = 0;
        UUID only = ONLY.get(boss);
        for (ServerPlayer player : level.players()) {
            if (only != null ? !only.equals(player.getUUID()) : !RESERVED.isEmpty() && RESERVED.contains(player.getUUID())) {
                continue;
            }
            if (!flying(boss, player)) {
                continue;
            }
            int time = watch.airTime.merge(player.getUUID(), 3, Integer::sum);
            if (time > worstTime) {
                worst = player;
                worstTime = time;
            }
        }
        // alle verlieren zwei Ticks – wer gleitet, hat eben drei gewonnen (also +1), wer nicht, baut ab
        for (Iterator<Map.Entry<UUID, Integer>> it = watch.airTime.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, Integer> entry = it.next();
            int time = entry.getValue() - 2;
            if (time <= 0) {
                it.remove();
            } else {
                entry.setValue(time);
            }
        }
        boolean enraged = boss.getHealth() < boss.getMaxHealth() * 0.5f;
        if (worst != null && watch.bolt == null && watch.cooldown <= 0 && worstTime - 2 >= (enraged ? NOTICE_ENRAGED : NOTICE)) {
            int charge = enraged ? CHARGE_ENRAGED : CHARGE;
            watch.bolt = mark(level, boss, worst, warden.skyTheme(), charge);
            watch.cooldown = COOLDOWN + charge;
            watch.airTime.remove(worst.getUUID());
        }
    }

    /** Der Boss nimmt den Flieger ins Visier: Siegel, Warnton und Hinweis. */
    public static SkyBoltEntity mark(ServerLevel level, Mob boss, ServerPlayer player, SkyTheme theme, int chargeTicks) {
        SkyBoltEntity bolt = SkyBoltEntity.charge(level, boss, player, theme, chargeTicks);
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sky_ward.marked", boss.getDisplayName()).withColor(theme.color));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SKY_WARD_MARK.get(), SoundSource.HOSTILE, 1.2f, 1.0f);
        boss.getLookControl().setLookAt(player, 30.0f, 30.0f);
        return bolt;
    }
}
