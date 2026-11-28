package io.github.sirschrolli.kingdomsatwar.enchant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

/**
 * Donnerschlag (seit 0.87): Mit etwas Glück fährt nach dem Treffer ein knisternder Funkenschlag von oben in den
 * Getroffenen. Der Schlag selbst hat ihn eben erst getroffen – damit der Funkenschlag nicht in dessen kurzer Schonzeit
 * verpufft, zählt er für sich.
 */
public record ThunderstrikeEffect(LevelBasedValue chance, LevelBasedValue damage) implements EnchantmentEntityEffect {
    public static final MapCodec<ThunderstrikeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("chance").forGetter(ThunderstrikeEffect::chance),
            LevelBasedValue.CODEC.fieldOf("damage").forGetter(ThunderstrikeEffect::damage)
    ).apply(instance, ThunderstrikeEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        if (!(entity instanceof LivingEntity victim) || !victim.isAlive() || level.getRandom().nextFloat() >= this.chance.calculate(enchantmentLevel)) {
            return;
        }
        strike(level, victim, item.owner(), this.damage.calculate(enchantmentLevel));
    }

    /** Der Funkenschlag selbst: ein zackiger Strahl von oben, ein Funkenregen im Getroffenen, Knistern – und Schaden. */
    public static void strike(ServerLevel level, LivingEntity victim, Entity attacker, float amount) {
        double x = victim.getX();
        double z = victim.getZ();
        double top = victim.getY() + victim.getBbHeight() + 3.2;
        double mid = victim.getY() + victim.getBbHeight() * 0.55;
        var random = level.getRandom();
        // der Strahl: vier, fünf Zacken von oben in das Ziel, dicht mit hellblauem Glimmen gezeichnet, ein weißer Kern
        var glow = new net.minecraft.core.particles.DustParticleOptions(0x8FD4FF, 1.15f);
        var core = new net.minecraft.core.particles.DustParticleOptions(0xF4FBFF, 0.7f);
        double px = x;
        double py = top;
        double pz = z;
        int bends = 5;
        for (int i = 1; i <= bends; i++) {
            double t = i / (double) bends;
            double nx = i == bends ? x : x + (random.nextDouble() - 0.5) * 0.9 * (1.0 - t * 0.6);
            double nz = i == bends ? z : z + (random.nextDouble() - 0.5) * 0.9 * (1.0 - t * 0.6);
            double ny = top + (mid - top) * t;
            int dots = 9;
            for (int d = 0; d < dots; d++) {
                double u = d / (double) dots;
                double ex = px + (nx - px) * u;
                double ey = py + (ny - py) * u;
                double ez = pz + (nz - pz) * u;
                level.sendParticles(glow, ex, ey, ez, 1, 0.02, 0.02, 0.02, 0.0);
                level.sendParticles(core, ex, ey, ez, 1, 0.0, 0.0, 0.0, 0.0);
            }
            px = nx;
            py = ny;
            pz = nz;
        }
        // im Getroffenen: ein Funkenregen
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, mid, z, 26, victim.getBbWidth() * 0.45, victim.getBbHeight() * 0.35,
                victim.getBbWidth() * 0.45, 0.4);
        level.sendParticles(ParticleTypes.SCRAPE, x, mid, z, 10, victim.getBbWidth() * 0.5, victim.getBbHeight() * 0.4, victim.getBbWidth() * 0.5, 0.6);
        level.playSound(null, x, mid, z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.7f, 1.45f + random.nextFloat() * 0.3f);
        var source = attacker != null
                ? level.damageSources().source(DamageTypes.LIGHTNING_BOLT, attacker)
                : level.damageSources().source(DamageTypes.LIGHTNING_BOLT);
        // die Schonzeit nach dem Hieb gilt nicht für den Funkenschlag (danach beginnt sie neu)
        victim.damageCooldownTime = 0;
        victim.hurtServer(level, source, amount);
    }

    @Override
    public MapCodec<ThunderstrikeEffect> codec() {
        return CODEC;
    }
}
