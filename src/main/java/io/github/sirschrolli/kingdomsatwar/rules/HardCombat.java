package io.github.sirschrolli.kingdomsatwar.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Härte im Kampf (seit 1.3, wie {@link Hardness} nicht abschaltbar und nicht auf dem GameTest-Server):
 * <ul>
 *     <li><b>Bosse heilen sich:</b> Wer vor einem Boss flieht – niemand mehr in {@value #BOSS_LEASH} Blöcken, zehn
 *     Sekunden kein Treffer –, findet ihn wieder heil vor: Er heilt {@value #BOSS_REGENERATION_PERCENT} % seiner
 *     Lebenspunkte je Sekunde.</li>
 *     <li><b>Monster heilen sich:</b> Fünf Sekunden ohne Treffer (und ohne zu brennen), dann ein halbes Herz alle
 *     zwei Sekunden.</li>
 *     <li><b>Giftige Treffer:</b> Zombies machen hungrig, Spinnen und andere Krabbler vergiften, Skelettpfeile lähmen,
 *     andere Monster schwächen – nicht jeder Treffer, aber oft genug.</li>
 *     <li><b>Höherer Fallschaden:</b> anderthalbmal so viel.</li>
 *     <li><b>Pfeile zerbrechen:</b> Jeder zweite Pfeil, der in einen Block fährt, zersplittert.</li>
 * </ul>
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HardCombat {
    /** So viel ihrer Lebenspunkte heilen Bosse je Sekunde, wenn niemand mehr kämpft. */
    public static final float BOSS_REGENERATION = 0.02f;
    static final int BOSS_REGENERATION_PERCENT = 2;
    /** So lange darf ein Boss keinen Treffer abbekommen haben (Ticks), bevor er heilt. */
    public static final int BOSS_CALM_TICKS = 200;
    /** In diesem Umkreis darf kein Spieler mehr sein, damit ein Boss heilt. */
    public static final double BOSS_LEASH = 32.0;
    /** So viele Lebenspunkte heilen Monster je Sekunde (ein halbes Herz alle zwei Sekunden). */
    public static final float MONSTER_REGENERATION = 0.5f;
    /** So lange darf ein Monster keinen Schaden genommen haben (Ticks), bevor es heilt. */
    public static final int MONSTER_CALM_TICKS = 100;
    /** Fallschaden der Spieler mal so viel. */
    public static final float FALL_DAMAGE = 1.5f;
    /** So oft zersplittert ein Pfeil, der in einen Block fährt. */
    public static final float ARROW_BREAK = 0.5f;

    /** Pfeile, die in diesem Tick in einen Block gefahren sind und zersplittern (nur im Server-Thread). */
    private static final List<AbstractArrow> BREAKING = new ArrayList<>();

    private HardCombat() {}

    // =====================================================================================
    // Heilung der Bosse und Monster
    // =====================================================================================

    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || (mob.tickCount + mob.getId()) % 20 != 0
                || !(mob.level() instanceof ServerLevel level) || !Hardness.active(level.getServer())) {
            return;
        }
        regenerate(level, mob);
    }

    /**
     * Ein Boss oder Monster, das in Ruhe gelassen wird, heilt (einmal je Sekunde aufgerufen). Gibt zurück, wie viel es
     * geheilt hat.
     */
    public static float regenerate(ServerLevel level, Mob mob) {
        if (!mob.isAlive() || mob.getHealth() >= mob.getMaxHealth() || mob.isOnFire()) {
            return 0.0f;
        }
        float amount;
        if (Hardness.isBoss(mob)) {
            if (mob.getLastDamageSource(BOSS_CALM_TICKS) != null || fighterNear(level, mob)) {
                return 0.0f;
            }
            amount = mob.getMaxHealth() * BOSS_REGENERATION;
        } else if (Hardness.isEnemy(mob)) {
            if (mob.getLastDamageSource(MONSTER_CALM_TICKS) != null) {
                return 0.0f;
            }
            amount = MONSTER_REGENERATION;
        } else {
            return 0.0f;
        }
        float before = mob.getHealth();
        mob.heal(amount);
        return mob.getHealth() - before;
    }

    /** Ist noch jemand in der Nähe, der kämpfen könnte (ein Spieler im Überlebens- oder Abenteuermodus)? */
    private static boolean fighterNear(ServerLevel level, Mob mob) {
        double leash = BOSS_LEASH * BOSS_LEASH;
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative() && player.distanceToSqr(mob) < leash) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Giftige Treffer
    // =====================================================================================

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getInflictedDamage() <= 0.0f || player.isCreative()
                || player.isSpectator() || !Hardness.active(player.level().getServer())) {
            return;
        }
        DamageSource source = event.getSource();
        MobEffectInstance venom = venom(source.getEntity(), source.getDirectEntity(), player.getRandom());
        if (venom != null) {
            player.addEffect(venom, source.getEntity());
        }
    }

    /**
     * Was ein Treffer dieses Monsters dem Spieler mitgibt (oder {@code null}): Zombies Hunger (30 %, zehn Sekunden),
     * Krabbler Gift (30 %, fünf Sekunden), Pfeile der Skelette Langsamkeit (25 %, drei Sekunden), andere Monster im
     * Nahkampf Schwäche (15 %, vier Sekunden). Bosse haben ihre eigenen Künste.
     */
    public static @Nullable MobEffectInstance venom(@Nullable Entity attacker, @Nullable Entity direct, RandomSource random) {
        if (attacker == null || !Hardness.isEnemy(attacker) || Hardness.isBoss(attacker)) {
            return null;
        }
        boolean melee = direct == attacker;
        if (melee && attacker.is(EntityTypeTags.ZOMBIES)) {
            return random.nextFloat() < 0.3f ? new MobEffectInstance(MobEffects.HUNGER, 200, 0) : null;
        }
        if (melee && attacker.is(EntityTypeTags.ARTHROPOD)) {
            return random.nextFloat() < 0.3f ? new MobEffectInstance(MobEffects.POISON, 100, 0) : null;
        }
        if (!melee && direct instanceof AbstractArrow && attacker.is(EntityTypeTags.SKELETONS)) {
            return random.nextFloat() < 0.25f ? new MobEffectInstance(MobEffects.SLOWNESS, 60, 0) : null;
        }
        if (melee) {
            return random.nextFloat() < 0.15f ? new MobEffectInstance(MobEffects.WEAKNESS, 80, 0) : null;
        }
        return null;
    }

    // =====================================================================================
    // Fallschaden
    // =====================================================================================

    @SubscribeEvent(priority = EventPriority.LOW)
    static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && player.level() instanceof ServerLevel level && Hardness.active(level.getServer())) {
            event.setDamageMultiplier(event.getDamageMultiplier() * FALL_DAMAGE);
        }
    }

    // =====================================================================================
    // Pfeile zerbrechen
    // =====================================================================================

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onImpact(ProjectileImpactEvent event) {
        if (event.isCanceled() || event.getRayTraceResult().getType() != HitResult.Type.BLOCK
                || !(event.getProjectile() instanceof AbstractArrow arrow) || !(arrow.level() instanceof ServerLevel level)
                || !Hardness.active(level.getServer()) || !breaks(arrow, level.getRandom())) {
            return;
        }
        BREAKING.add(arrow);
    }

    /** Zersplittert dieser Pfeil am Block? Nur gewöhnliche Pfeile von Spielern, die man wieder aufheben könnte. */
    public static boolean breaks(AbstractArrow arrow, RandomSource random) {
        return (arrow instanceof Arrow || arrow instanceof SpectralArrow) && arrow.getOwner() instanceof Player
                && arrow.pickup == AbstractArrow.Pickup.ALLOWED && random.nextFloat() < ARROW_BREAK;
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (BREAKING.isEmpty()) {
            return;
        }
        for (AbstractArrow arrow : BREAKING) {
            if (arrow.isAlive() && arrow.level() instanceof ServerLevel level) {
                shatter(level, arrow);
            }
        }
        BREAKING.clear();
    }

    /** Der Pfeil zersplittert: ein paar Splitter, ein trockenes Knacken, weg ist er. */
    public static void shatter(ServerLevel level, AbstractArrow arrow) {
        ItemStack look = arrow.getPickupItemStackOrigin();
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, look.isEmpty() ? net.minecraft.world.item.Items.ARROW : look.getItem()),
                arrow.getX(), arrow.getY(), arrow.getZ(), 6, 0.06, 0.06, 0.06, 0.06);
        level.playSound(null, arrow.getX(), arrow.getY(), arrow.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.35f,
                1.5f + level.getRandom().nextFloat() * 0.3f);
        arrow.discard();
    }
}
