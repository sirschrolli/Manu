package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Die Mondsilberklinge: eine leichte, schnelle Klinge, die Schattenwesen und Untote besonders hart trifft – getroffene
 * leuchten kurz auf, sodass sie sich im Zwielicht nicht mehr verbergen können.
 */
public class MoonsilverBladeItem extends Item {
    /** Zusätzlicher Schaden gegen Schattenwesen und Untote. */
    public static final float BONUS_DAMAGE = 6.0f;

    public MoonsilverBladeItem(Properties properties) {
        super(properties);
    }

    /** Trifft die Klinge dieses Wesen besonders hart? */
    public static boolean isBane(Entity target) {
        return target.is(ModTags.EntityTypes.SHADOW_CREATURES) || target.is(EntityTypeTags.UNDEAD);
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource damageSource) {
        return isBane(victim) ? BONUS_DAMAGE : 0.0f;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (isBane(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0), attacker);
            if (target.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY(0.6), target.getZ(), 10, 0.3, 0.4, 0.3, 0.03);
            }
        }
    }
}
