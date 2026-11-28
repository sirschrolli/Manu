package io.github.sirschrolli.kingdomsatwar.item.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Glutzahn: eine gebogene Klinge aus dem Fangzahn des Glutwurms, mit glühenden Adern. Setzt jedes Ziel in Brand
 * ({@link #BURN_SECONDS} Sekunden) und trifft, was schon brennt, um {@link #BURNING_BONUS} härter.
 */
public class EmberFangItem extends Item {
    public static final float BURNING_BONUS = 4.0f;
    public static final float BURN_SECONDS = 5.0f;

    public EmberFangItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        return target.isOnFire() && !target.fireImmune() ? BURNING_BONUS : 0.0f;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!target.fireImmune()) {
            target.igniteForSeconds(BURN_SECONDS);
        }
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.6), target.getZ(), 10, 0.25, 0.35, 0.25, 0.03);
            level.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY(0.6), target.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        }
    }
}
