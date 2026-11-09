package io.github.sirschrolli.kingdomsatwar.item.end;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.end.StarfallEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
 * Die Sternenlanze: Asterias Lanze – ein silberner Schaft mit goldener Wendel, eine Brechscheibe und eine Spitze aus einem
 * leuchtenden Sternkristall. Ein Speer wie die Speere von Minecraft (Stoß und Ansturm), dazu:
 * <ul>
 *     <li><b>Sternenkraft:</b> im End – oder nachts unter freiem Himmel – trifft sie um ein Drittel härter;</li>
 *     <li><b>Sternenmal:</b> wen sie trifft, der leuchtet; jeder dritte Treffer ruft einen kleinen Stern herab, der auf
 *     das Ziel stürzt;</li>
 *     <li><b>Sternenfall:</b> wer sie in der Hand hält, sinkt sanft wie eine Sternschnuppe (kein Fallschaden).</li>
 * </ul>
 */
public class StarLanceItem extends Item {
    private static final String HITS = KingdomsAtWar.MODID + ":star_lance_hits";

    public StarLanceItem(Properties properties) {
        super(properties);
    }

    /** Im End – oder nachts unter freiem Himmel? */
    public static boolean starlit(Entity entity) {
        Level level = entity.level();
        return level.dimension() == Level.END || level.isDarkOutside() && level.canSeeSky(entity.blockPosition().above());
    }

    @Override
    public float getAttackDamageBonus(Entity victim, float damage, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker && starlit(attacker)) {
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
        // Sternenmal: das Ziel leuchtet
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0), attacker);
        level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY(0.6), mob.getZ(), 8, 0.3, 0.4, 0.3, 0.05);
        CompoundTag data = attacker.getPersistentData();
        int hits = data.getIntOr(HITS, 0) + 1;
        if (hits < 3) {
            data.putInt(HITS, hits);
            return;
        }
        data.putInt(HITS, 0);
        // jeder dritte Treffer: ein kleiner Stern stürzt auf das Ziel
        StarfallEntity star = new StarfallEntity(level, attacker);
        star.setSmall(true);
        star.setDamage(starlit(attacker) ? 8.0f : 6.0f);
        star.dropOnto(mob.position().add(0.0, mob.getBbHeight() * 0.5, 0.0), 12.0);
        level.addFreshEntity(star);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if ((slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) && owner instanceof LivingEntity living
                && !living.isFallFlying() && living.fallDistance > 2.5 && level.getGameTime() % 5 == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false, true));
        }
    }
}
