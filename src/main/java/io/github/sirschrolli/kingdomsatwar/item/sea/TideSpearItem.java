package io.github.sirschrolli.kingdomsatwar.item.sea;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Der Gezeiten-Speer: die Waffe des letzten Gezeitenkönigs des Meeresvolks, die Karkinos jahrhundertelang in seiner
 * Kuppel trug. Ein Speer wie die Speere von Minecraft (Stoß und Ansturm), mit einer Klinge aus leuchtendem Aquamarin.
 * <ul>
 *     <li><b>Gezeitenkraft:</b> im Wasser oder im Regen trifft er um ein Drittel härter.</li>
 *     <li><b>Brandungsstoß:</b> jeder Treffer im Wasser oder im Regen lässt eine Welle aufschäumen, die Gegner ringsum
 *     zurückwirft.</li>
 *     <li><b>Gnade der Gezeiten:</b> wer ihn in der Hand hält, schwimmt wie ein Delfin.</li>
 * </ul>
 */
public class TideSpearItem extends Item {
    public TideSpearItem(Properties properties) {
        super(properties);
    }

    /** Im Wasser oder im Regen? */
    public static boolean wet(Entity entity) {
        return entity.isInWaterOrRain();
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker && wet(attacker)) {
            return damage / 3.0f;
        }
        return 0.0f;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        super.postHurtEnemy(stack, mob, attacker);
        if (!(attacker.level() instanceof ServerLevel level) || !wet(attacker)) {
            return;
        }
        // Brandungsstoß: eine Welle schäumt um das Ziel auf und wirft die Gegner ringsum zurück
        level.sendParticles(ParticleTypes.SPLASH, mob.getX(), mob.getY() + 0.3, mob.getZ(), 40, 1.2, 0.2, 1.2, 0.3);
        level.sendParticles(ParticleTypes.BUBBLE_POP, mob.getX(), mob.getY() + 0.5, mob.getZ(), 20, 1.0, 0.4, 1.0, 0.1);
        level.playSound(null, mob.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1.value(), SoundSource.PLAYERS, 0.6f, 1.3f);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(2.5),
                e -> e != attacker && e.isAlive() && !e.isAlliedTo(attacker))) {
            double dx = other.getX() - attacker.getX();
            double dz = other.getZ() - attacker.getZ();
            double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            other.push(dx / len * 0.6, 0.25, dz / len * 0.6);
            other.needsSync = true;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (slot == EquipmentSlot.MAINHAND && owner instanceof LivingEntity living && living.isInWater() && level.getGameTime() % 20 == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false, true));
        }
    }
}
