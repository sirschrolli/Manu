package io.github.sirschrolli.kingdomsatwar.item.monster;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

/**
 * Moorkeule: ein knorriger Wurzelknüppel mit leuchtenden Pilzen. Schwer; jeder dritte Treffer (im Mittel) lässt Wurzeln
 * zupacken ({@link #GRASP_TICKS} Ticks fast festgehalten). In Regen und Wasser wächst sie langsam wieder zusammen.
 */
public class BogClubItem extends Item {
    public static final float GRASP_CHANCE = 0.35f;
    public static final int GRASP_TICKS = 30;

    public BogClubItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.getRandom().nextFloat() < GRASP_CHANCE) {
            grasp(target, attacker);
        }
    }

    /** Wurzeln packen das Ziel. */
    public static void grasp(LivingEntity target, @Nullable LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, GRASP_TICKS, 4, false, true), attacker);
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MANGROVE_ROOTS.defaultBlockState()), target.getX(),
                    target.getY(0.2), target.getZ(), 16, 0.35, 0.2, 0.35, 0.05);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), target.getX(), target.getY(0.1),
                    target.getZ(), 10, 0.3, 0.1, 0.3, 0.05);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.MIRE_SHAMBLER_GRASP.get(), SoundSource.PLAYERS, 0.8f,
                    1.1f + level.getRandom().nextFloat() * 0.2f);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        // in Regen und Wasser wächst die Wurzel wieder zusammen
        if (stack.isDamaged() && entity.tickCount % 100 == 0 && entity.isInWaterOrRain()) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
}
