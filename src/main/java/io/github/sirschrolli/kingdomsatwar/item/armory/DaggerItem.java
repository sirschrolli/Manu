package io.github.sirschrolli.kingdomsatwar.item.armory;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Nachtstahl-Dolch: kurz und sehr schnell, mit einer violett glühenden Hohlkehle. Wer von <b>hinten</b> zusticht (das Ziel
 * schaut weg, mehr als {@link #BACK_DEGREES}° neben der Richtung zum Angreifer), macht doppelten Schaden.
 */
public class DaggerItem extends Item {
    public static final double BACK_DEGREES = 120.0;
    private static final double BACK_COS = Math.cos(Math.toRadians(BACK_DEGREES));

    public DaggerItem(Properties properties) {
        super(properties);
    }

    /** Steht der Angreifer hinter dem Ziel (waagrecht betrachtet, gemessen an der Blickrichtung des Ziels)? */
    public static boolean isBehind(Entity attacker, LivingEntity target) {
        Vec3 to = attacker.position().subtract(target.position()).multiply(1, 0, 1);
        if (to.lengthSqr() < 1.0e-4) {
            return false;
        }
        float yaw = target.getYHeadRot() * Mth.DEG_TO_RAD;
        Vec3 facing = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
        return facing.dot(to.normalize()) <= BACK_COS;
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (source.getEntity() != null && victim instanceof LivingEntity target && isBehind(source.getEntity(), target)) {
            return damage;
        }
        return 0.0f;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel level && isBehind(attacker, target)) {
            Vec3 c = target.getBoundingBox().getCenter();
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, c.x, c.y, c.z, 14, 0.25, 0.3, 0.25, 0.25);
            level.sendParticles(ParticleTypes.WITCH, c.x, c.y + 0.2, c.z, 6, 0.2, 0.2, 0.2, 0.02);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.6f);
        }
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        return ability != ItemAbilities.SWORD_SWEEP && super.canPerformAction(stack, ability);
    }
}
