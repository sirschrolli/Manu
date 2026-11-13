package io.github.sirschrolli.kingdomsatwar.item.monster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Krallenklinge: drei Werwolfsklauen an einem Faustgriff. Schnell; nachts trifft sie um {@link #NIGHT_BONUS} härter, und
 * wer mit ihr ein Wesen besiegt, gerät in einen Blutrausch (Schnelligkeit und Stärke, siehe {@link MonsterGearEvents}).
 */
public class ClawBladeItem extends Item {
    public static final float NIGHT_BONUS = 2.0f;
    public static final int FRENZY_TICKS = 160;

    public ClawBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        return target.level().isDarkOutside() ? NIGHT_BONUS : 0.0f;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (target.level() instanceof ServerLevel level) {
            // drei Kratzer
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.6), target.getZ(), 6, 0.25, 0.3, 0.25, 0.15);
        }
    }

    /** Blutrausch nach einem Sieg. */
    public static void frenzy(LivingEntity killer) {
        killer.addEffect(new MobEffectInstance(MobEffects.SPEED, FRENZY_TICKS, 0, false, true, true));
        killer.addEffect(new MobEffectInstance(MobEffects.STRENGTH, FRENZY_TICKS, 0, false, true, true));
        if (killer.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, killer.getX(), killer.getEyeY() + 0.3, killer.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
