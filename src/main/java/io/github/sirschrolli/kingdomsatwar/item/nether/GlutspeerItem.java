package io.github.sirschrolli.kingdomsatwar.item.nether;

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
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Der Glutspeer: das Meisterstück des Glutschmieds Malleus – ein Schaft aus geschwärztem Eisen mit goldenen Ringen und
 * eine breite Klinge, die nie erkaltet. Ein Speer wie die Speere von Minecraft (Stoß und Ansturm):
 * <ul>
 *     <li><b>Glutkraft:</b> im Nether – oder wenn sein Träger selbst brennt – trifft er um ein Drittel härter;</li>
 *     <li><b>Glutstoß:</b> jeder Treffer setzt das Ziel in Brand; im Nether (oder brennend) lodert zudem ein
 *     Flammenring auf, der die Gegner ringsum entzündet;</li>
 *     <li><b>Hitzeschutz:</b> wer ihn im Nether in der Hand hält, ist vor Feuer und Lava geschützt.</li>
 * </ul>
 */
public class GlutspeerItem extends Item {
    public GlutspeerItem(Properties properties) {
        super(properties);
    }

    /** Im Nether (oder in der Glutesse des Schmieds) – oder brennt der Träger? */
    public static boolean blazing(Entity entity) {
        return io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.counts(entity.level(), Level.NETHER) || entity.isOnFire();
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker && blazing(attacker)) {
            return damage / 3.0f;
        }
        return 0.0f;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        super.postHurtEnemy(stack, mob, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        mob.igniteForSeconds(5.0f);
        level.sendParticles(ParticleTypes.FLAME, mob.getX(), mob.getY(0.5), mob.getZ(), 12, 0.3, 0.4, 0.3, 0.03);
        if (!blazing(attacker)) {
            return;
        }
        // Glutstoß: ein Flammenring lodert um das Ziel auf und entzündet die Gegner ringsum
        for (int i = 0; i < 28; i++) {
            double a = i * Math.PI * 2.0 / 28;
            level.sendParticles(ParticleTypes.FLAME, mob.getX() + Math.cos(a) * 2.2, mob.getY() + 0.15, mob.getZ() + Math.sin(a) * 2.2, 1, 0.0,
                    0.12, 0.0, 0.02);
        }
        level.sendParticles(ParticleTypes.LAVA, mob.getX(), mob.getY() + 0.3, mob.getZ(), 6, 0.6, 0.1, 0.6, 0.0);
        level.playSound(null, mob.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 0.8f);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(2.5),
                e -> e != attacker && e != mob && e.isAlive() && !e.isAlliedTo(attacker))) {
            other.igniteForSeconds(4.0f);
            double dx = other.getX() - mob.getX();
            double dz = other.getZ() - mob.getZ();
            double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
            other.push(dx / len * 0.4, 0.2, dz / len * 0.4);
            other.needsSync = true;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if ((slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) && owner instanceof LivingEntity living
                && io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.counts(level, Level.NETHER) && level.getGameTime() % 20 == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
        }
    }
}
