package io.github.sirschrolli.kingdomsatwar.enchant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/** Verzauberungswirkung „heilen“: das betroffene Wesen erhält Leben zurück (für Lebensraub: der Angreifer). */
public record HealEffect(LevelBasedValue amount) implements EnchantmentEntityEffect {
    public static final MapCodec<HealEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(HealEffect::amount)
    ).apply(instance, HealEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        if (entity instanceof LivingEntity living && living.isAlive() && living.getHealth() < living.getMaxHealth()) {
            living.heal(this.amount.calculate(enchantmentLevel));
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, living.getX(), living.getY() + living.getBbHeight() * 0.6, living.getZ(), 2,
                    0.25, 0.2, 0.25, 0.0);
        }
    }

    @Override
    public MapCodec<HealEffect> codec() {
        return CODEC;
    }
}
