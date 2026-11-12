package io.github.sirschrolli.kingdomsatwar.item.abyss;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

/**
 * Die Rüstung der Zeitwächter (seit 1.1) aus Chronit – geronnener Zeit aus dem Abgrund:
 * <ul>
 *     <li><b>Die Zeit stockt</b> (jeder Teil): Wer den Träger trifft, gerät manchmal ins Stocken – für einen Augenblick
 *     langsamer, auch seine Schläge ({@link #SLOW_CHANCE} je Teil);</li>
 *     <li><b>Zweite Chance</b> (voller Satz): Ein tödlicher Treffer spult die Zeit zurück – der Träger steht wieder dort, wo er
 *     vor {@link #REWIND_TICKS} Ticks stand, mit den Herzen von damals (mindestens {@link #MIN_HEALTH} Lebenspunkte), schädliche
 *     Wirkungen sind fort, für drei Sekunden ist er unverwundbar. Danach steht seine Uhr {@link #COOLDOWN} Ticks still; tickt
 *     sie wieder, hört er es. Ein Totem kommt zuerst, gegen /kill und die Leere hilft sie nicht.</li>
 * </ul>
 * Wie bei allen Rüstungen der Mod zeigen sich die Wirkungen ohne Symbol.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class TimewardenArmor {
    /** So weit spult die Zeit zurück (Ticks): drei Sekunden. */
    public static final int REWIND_TICKS = 60;
    /** So lange steht die Uhr danach still (Ticks): drei Minuten. */
    public static final int COOLDOWN = 20 * 180;
    /** So viele Lebenspunkte hat man nach dem Zurückspulen mindestens (vier Herzen). */
    public static final float MIN_HEALTH = 8.0f;
    /** So wahrscheinlich gerät ein Angreifer je Teil ins Stocken. */
    public static final float SLOW_CHANCE = 0.08f;
    /** So lange stockt er (Ticks). */
    public static final int SLOW_TICKS = 50;
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String COOLDOWN_KEY = "kingdomsatwar_timewarden_rewind";
    private static final String READY_KEY = "kingdomsatwar_timewarden_ready";
    /** Der Weg jedes Spielers mit dem vollen Satz in den letzten drei Sekunden. */
    private static final Map<UUID, ArrayDeque<Moment>> HISTORY = new HashMap<>();
    public static final DustParticleOptions AMBER = new DustParticleOptions(0xFFB347, 0.9f);
    private static final DustParticleOptions GOLD = new DustParticleOptions(0xFFE7A0, 0.6f);

    /** Ein Augenblick: wo der Spieler war, wohin er sah, wie viele Lebenspunkte er hatte. */
    public record Moment(ResourceKey<Level> dimension, Vec3 pos, float yRot, float xRot, float health) {}

    private TimewardenArmor() {}

    /** Wie viele Teile der Rüstung das Wesen trägt (0–4). */
    public static int pieces(LivingEntity entity) {
        List<DeferredItem<Item>> set = AbyssGear.timewardenSet();
        int count = 0;
        for (int i = 0; i < SLOTS.length; i++) {
            if (entity.getItemBySlot(SLOTS[i]).is(set.get(i).get())) {
                count++;
            }
        }
        return count;
    }

    /** Trägt das Wesen den vollen Satz? */
    public static boolean fullSet(LivingEntity entity) {
        return pieces(entity) == SLOTS.length;
    }

    /** Tickt die Uhr (ist die Zweite Chance bereit)? */
    public static boolean rewindReady(ServerPlayer player) {
        long last = player.getPersistentData().getLongOr(COOLDOWN_KEY, Long.MIN_VALUE);
        long now = player.level().getGameTime();
        return last == Long.MIN_VALUE || now - last >= COOLDOWN || now < last;
    }

    /** Für Tests: der älteste gemerkte Augenblick (oder null). */
    public static @Nullable Moment oldest(ServerPlayer player) {
        ArrayDeque<Moment> path = HISTORY.get(player.getUUID());
        return path == null || path.isEmpty() ? null : path.peekFirst();
    }

    /** Für Tests: die Uhr sofort wieder in Gang setzen. */
    public static void resetCooldown(ServerPlayer player) {
        player.getPersistentData().remove(COOLDOWN_KEY);
        player.getPersistentData().remove(READY_KEY);
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || !fullSet(player)) {
            HISTORY.remove(player.getUUID());
            return;
        }
        remember(player);
        // tickt die Uhr wieder, hört man es (nur einmal, und nur, wenn sie stillstand)
        var data = player.getPersistentData();
        if (data.contains(COOLDOWN_KEY) && !data.getBooleanOr(READY_KEY, false) && rewindReady(player)) {
            data.putBoolean(READY_KEY, true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TIMEWARDEN_READY.get(), SoundSource.PLAYERS,
                    0.8f, 1.0f);
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.timewarden_ready").withColor(0xFFC86A));
        }
    }

    /** Merkt sich, wo der Spieler gerade ist (die letzten drei Sekunden). */
    public static void remember(ServerPlayer player) {
        ArrayDeque<Moment> path = HISTORY.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        path.addLast(new Moment(player.level().dimension(), player.position(), player.getYRot(), player.getXRot(), player.getHealth()));
        while (path.size() > REWIND_TICKS + 1) {
            path.removeFirst();
        }
    }

    // Vor dem Licht der Unsterblichkeit (HIGH): die Rüstung spult zuerst zurück
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || player.isCreative()) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !fullSet(player) || !rewindReady(player)) {
            return;
        }
        event.setCanceled(true);
        rewind(player);
    }

    /** Die Zweite Chance: spult den Spieler drei Sekunden zurück (siehe oben). */
    public static void rewind(ServerPlayer player) {
        ServerLevel level = player.level();
        Moment then = null;
        ArrayDeque<Moment> path = HISTORY.get(player.getUUID());
        if (path != null) {
            for (Moment m : path) {
                if (m.dimension() == level.dimension()) {
                    then = m;
                    break;
                }
            }
        }
        Vec3 from = player.position();
        burst(level, from, false);
        float health = then != null ? then.health() : MIN_HEALTH;
        player.setHealth(Mth.clamp(Math.max(health, MIN_HEALTH), 1.0f, player.getMaxHealth()));
        if (then != null && then.pos().distanceToSqr(from) > 0.01) {
            player.teleportTo(level, then.pos().x, then.pos().y, then.pos().z, Set.of(), then.yRot(), then.xRot(), true);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.needsSync = true;
        player.resetFallDistance();
        player.clearFire();
        List<Holder<MobEffect>> harmful = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                harmful.add(effect.getEffect());
            }
        }
        harmful.forEach(player::removeEffect);
        player.setInvulnerableTime(60);
        player.getPersistentData().putLong(COOLDOWN_KEY, level.getGameTime());
        player.getPersistentData().putBoolean(READY_KEY, false);
        HISTORY.remove(player.getUUID());
        burst(level, player.position(), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.TIMEWARDEN_REWIND.get(), SoundSource.PLAYERS, 1.2f, 1.0f);
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.timewarden_rewind").withStyle(ChatFormatting.GOLD));
    }

    /** Bernsteinstaub: an der alten Stelle zerstiebt er, an der neuen sammelt er sich in einem Ring wie ein Zifferblatt. */
    private static void burst(ServerLevel level, Vec3 at, boolean arrive) {
        if (arrive) {
            for (int i = 0; i < 12; i++) {
                double a = i * Mth.TWO_PI / 12.0;
                level.sendParticles(AMBER, at.x + Math.cos(a) * 1.1, at.y + 1.0, at.z + Math.sin(a) * 1.1, 2, 0.02, 0.02, 0.02, 0.0);
            }
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0, at.z, 16, 0.3, 0.6, 0.3, 0.03);
            level.sendParticles(GOLD, at.x, at.y + 1.0, at.z, 30, 0.4, 0.8, 0.4, 0.0);
        } else {
            level.sendParticles(AMBER, at.x, at.y + 1.0, at.z, 40, 0.4, 0.8, 0.4, 0.0);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, at.x, at.y + 1.0, at.z, 30, 0.3, 0.7, 0.3, 0.05);
        }
    }

    /** Die Zeit stockt: Wer den Träger trifft, wird manchmal kurz langsamer (in Bewegung und Schlag). */
    @SubscribeEvent
    static void onHurt(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || event.getInflictedDamage() <= 0.0f) {
            return;
        }
        int n = pieces(victim);
        if (n == 0 || !(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == victim || !attacker.isAlive()) {
            return;
        }
        if (victim.getRandom().nextFloat() >= SLOW_CHANCE * n) {
            return;
        }
        stall(level, attacker, victim);
    }

    /** Lässt die Zeit um {@code attacker} stocken (auch für Tests). */
    public static void stall(ServerLevel level, LivingEntity attacker, @Nullable LivingEntity by) {
        attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOW_TICKS, 2), by);
        attacker.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, SLOW_TICKS, 1), by);
        for (int i = 0; i < 12; i++) {
            double a = i * Mth.TWO_PI / 12.0;
            level.sendParticles(AMBER, attacker.getX() + Math.cos(a) * 0.8, attacker.getY() + attacker.getBbHeight() * 0.6,
                    attacker.getZ() + Math.sin(a) * 0.8, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), ModSounds.TIMEWARDEN_TICK.get(), SoundSource.PLAYERS, 0.9f,
                0.9f + attacker.getRandom().nextFloat() * 0.2f);
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HISTORY.remove(event.getEntity().getUUID());
    }
}
