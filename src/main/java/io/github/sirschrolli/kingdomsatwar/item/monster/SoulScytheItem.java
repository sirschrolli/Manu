package io.github.sirschrolli.kingdomsatwar.item.monster;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Seelensense: Schaft aus Aschenholz, die Klinge aus Seelenfeuer. Jeder Treffer schwächt und setzt in Brand. Mit der
 * rechten Taste <b>verweht</b> ihr Träger wie ein Aschengeist: ein Sprung bis {@link #DASH} Blöcke nach vorn (nur wohin er
 * passt), dabei kurz unsichtbar und unverwundbar; danach ruht die Sense {@link #COOLDOWN} Ticks.
 */
public class SoulScytheItem extends Item {
    public static final double DASH = 7.0;
    public static final int COOLDOWN = 140;
    public static final int FADE_TICKS = 16;

    public SoulScytheItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), attacker);
        if (!target.fireImmune()) {
            target.igniteForSeconds(3.0f);
        }
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.6), target.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
            level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.8), target.getZ(), 2, 0.2, 0.2, 0.2, 0.02);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        Vec3 to = dashTarget(server, player);
        if (to == null) {
            return InteractionResult.FAIL;
        }
        fade(server, player, to);
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        stack.hurtAndBreak(2, player, hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Wohin der Sprung führt: so weit nach vorn (waagrecht), wie der Träger frei steht – mindestens zwei Blöcke. */
    public static Vec3 dashTarget(ServerLevel level, LivingEntity entity) {
        Vec3 look = entity.getLookAngle().multiply(1.0, 0.0, 1.0);
        if (look.lengthSqr() < 1.0e-4) {
            return null;
        }
        look = look.normalize();
        Vec3 from = entity.position();
        Vec3 best = null;
        for (double d = 0.5; d <= DASH; d += 0.5) {
            Vec3 at = from.add(look.scale(d));
            AABB box = entity.getBoundingBox().move(at.subtract(from));
            if (!level.noCollision(entity, box)) {
                break;
            }
            best = at;
        }
        return best != null && best.distanceToSqr(from) >= 4.0 ? best : null;
    }

    /** Verwehen: Asche am Start und am Ziel, kurz unsichtbar und unverwundbar. */
    public static void fade(ServerLevel level, LivingEntity entity, Vec3 to) {
        Vec3 from = entity.position();
        level.sendParticles(ParticleTypes.WHITE_ASH, from.x, from.y + 1.0, from.z, 60, 0.35, 0.7, 0.35, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y + 0.9, from.z, 12, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, from.x, from.y, from.z, ModSounds.ASH_WRAITH_FADE.get(), SoundSource.PLAYERS, 1.0f, 1.2f);
        entity.teleportTo(to.x, to.y, to.z);
        entity.resetFallDistance();
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, FADE_TICKS, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, FADE_TICKS, 4, false, false, false));
        level.sendParticles(ParticleTypes.WHITE_ASH, to.x, to.y + 1.0, to.z, 40, 0.35, 0.7, 0.35, 0.02);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, to.x, to.y + 1.0, to.z, 10, 0.3, 0.5, 0.3, 0.02);
    }
}
