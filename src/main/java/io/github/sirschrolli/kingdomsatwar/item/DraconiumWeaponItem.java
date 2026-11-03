package io.github.sirschrolli.kingdomsatwar.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Draconium-Waffen tragen die Glut der Drachen in sich: Jeder Treffer setzt das Ziel in Brand. */
public class DraconiumWeaponItem extends Item {
    private final int burnSeconds;

    public DraconiumWeaponItem(Properties properties, int burnSeconds) {
        super(properties);
        this.burnSeconds = burnSeconds;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (!target.fireImmune()) {
            target.igniteForSeconds(this.burnSeconds);
        }
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.6), target.getZ(), 8, 0.25, 0.35, 0.25, 0.02);
        }
    }
}
