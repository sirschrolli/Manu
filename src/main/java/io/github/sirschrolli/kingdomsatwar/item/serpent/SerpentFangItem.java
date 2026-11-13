package io.github.sirschrolli.kingdomsatwar.item.serpent;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

/**
 * Der Schlangenzahn (seit 1.10): ein Dolch aus einem Giftzahn Quetzaras, der Gefiederten Schlange – die Klinge elfenbeinweiß
 * mit einer grün schimmernden Giftrinne, das Heft aus Jade mit Gold, am Knauf zwei Quetzalfedern. Schnell und scharf; jeder
 * Treffer <b>vergiftet</b> (Gift II, {@link #POISON_TICKS} Ticks), und wer schon vergiftet ist, den trifft er um
 * {@link #POISONED_BONUS} härter.
 * Rechtsklick: der <b>Schlangenstoß</b> – ein Satz nach vorn wie die Schlange beim Zubeißen; wer bis {@link #LUNGE_RANGE}
 * Blöcke vor dir steht, den beißt der Zahn ({@link #LUNGE_FACTOR}-facher Schaden, schweres Gift). Danach ruht er
 * {@link #COOLDOWN} Ticks.
 */
public class SerpentFangItem extends Item {
    /** Schaden (mit der Faust des Spielers) und Schläge je Sekunde. */
    public static final double DAMAGE = 7.0;
    public static final double SPEED = 2.0;
    public static final int POISON_TICKS = 80;
    public static final float POISONED_BONUS = 0.25f;
    public static final int COOLDOWN = 120;
    public static final double LUNGE_RANGE = 5.5;
    public static final float LUNGE_FACTOR = 1.5f;
    /** Das Grün des Gifts in der Rinne. */
    public static final int VENOM = 0x7CFF4A;

    public SerpentFangItem(Properties properties) {
        super(properties);
    }

    /** Wer schon vergiftet ist, den trifft der Zahn härter. */
    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (victim instanceof LivingEntity target && target.hasEffect(MobEffects.POISON)) {
            return damage * POISONED_BONUS;
        }
        return 0.0f;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        poison(target, attacker, 1, POISON_TICKS);
    }

    /** Vergiftet (verlängert ein schwächeres Gift, verkürzt kein stärkeres). */
    public static void poison(LivingEntity target, @Nullable Entity source, int amplifier, int ticks) {
        MobEffectInstance current = target.getEffect(MobEffects.POISON);
        if (current == null || current.getAmplifier() < amplifier || current.getDuration() < ticks) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, ticks, Math.max(amplifier, current == null ? 0 : current.getAmplifier())), source);
        }
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(new DustParticleOptions(VENOM, 0.9f), target.getX(), target.getY(0.6), target.getZ(), 6, 0.25, 0.3, 0.25, 0.0);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 look = player.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
        if (look.lengthSqr() < 1.0e-4) {
            return InteractionResult.PASS;
        }
        look = look.normalize();
        // der Satz nach vorn (auf beiden Seiten: der Spieler bewegt sich selbst)
        player.push(look.x * 1.15, player.onGround() ? 0.28 : 0.08, look.z * 1.15);
        if (level instanceof ServerLevel server) {
            LivingEntity victim = lungeTarget(server, player, look);
            if (victim != null) {
                float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * LUNGE_FACTOR;
                if (victim.hurtServer(server, player.damageSources().playerAttack(player), damage)) {
                    poison(victim, player, 2, POISON_TICKS);
                    victim.push(look.x * 0.4, 0.15, look.z * 0.4);
                    server.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(0.6), victim.getZ(), 12, 0.3, 0.3, 0.3, 0.3);
                }
            }
            Vec3 p = player.position().add(0.0, 0.9, 0.0);
            for (int i = 0; i < 6; i++) {
                Vec3 q = p.add(look.scale(i * 0.6));
                server.sendParticles(new DustParticleOptions(VENOM, 1.1f), q.x, q.y, q.z, 2, 0.15, 0.2, 0.15, 0.0);
            }
            server.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.QUETZARA_STRIKE.get(), SoundSource.PLAYERS, 1.1f,
                    1.25f + server.getRandom().nextFloat() * 0.1f);
            stack.hurtAndBreak(2, player, hand);
            player.getCooldowns().addCooldown(stack, COOLDOWN);
        }
        return InteractionResult.SUCCESS;
    }

    /** Wen der Schlangenstoß trifft: das nächste Wesen bis {@link #LUNGE_RANGE} Blöcke vor dem Spieler (höchstens 30° daneben). */
    public static @Nullable LivingEntity lungeTarget(ServerLevel level, Player player, Vec3 look) {
        Vec3 eye = player.getEyePosition();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        AABB area = player.getBoundingBox().expandTowards(look.scale(LUNGE_RANGE)).inflate(1.5);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area, e -> canHit(player, e))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double dist = to.length();
            if (dist > LUNGE_RANGE + e.getBbWidth() * 0.5 || dist < 1.0e-3) {
                continue;
            }
            Vec3 flat = to.multiply(1.0, 0.0, 1.0);
            if (flat.lengthSqr() > 0.04 && flat.normalize().dot(look) < Math.cos(Math.toRadians(30.0))) {
                continue;
            }
            if (dist < bestDist && player.hasLineOfSight(e)) {
                best = e;
                bestDist = dist;
            }
        }
        return best;
    }

    private static boolean canHit(Player player, Entity e) {
        return e != player && e.isAlive() && !(e instanceof ArmorStand) && !(e instanceof OwnableEntity pet && player.equals(pet.getOwner()))
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator() || !player.canHarmPlayer(p)));
    }

    /** Ein Dolch: kein Schwungangriff. */
    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return ability != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, ability);
    }
}
