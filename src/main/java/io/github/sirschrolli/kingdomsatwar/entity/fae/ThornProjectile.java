package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Ein Dorn, den der Dornwicht schleudert: sticht und vergiftet kurz; bleibt nicht liegen und lässt sich nicht aufheben. */
public class ThornProjectile extends AbstractArrow {
    public ThornProjectile(EntityType<? extends ThornProjectile> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public ThornProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.THORN.get(), shooter, level, new ItemStack(Items.STICK), null);
        this.pickup = Pickup.DISALLOWED;
        this.setBaseDamage(2.5);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.STICK);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0), this.getEffectSource());
    }

    @Override
    public void tick() {
        super.tick();
        // Dornen, die stecken bleiben, zerfallen bald
        if (this.isInGround() && this.inGroundTime > 40) {
            this.discard();
        }
    }
}
