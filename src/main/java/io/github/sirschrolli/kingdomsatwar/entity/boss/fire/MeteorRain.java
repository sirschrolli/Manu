package io.github.sirschrolli.kingdomsatwar.entity.boss.fire;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Meteorregen: Pyrrhon steigt auf, brüllt in den Himmel – und glühende Felsbrocken stürzen ins Nest. Wo ein
 * Meteor einschlagen wird, steigt vorher Rauch aus dem Boden.
 */
public final class MeteorRain extends BossAttack {
    private final float damage;

    public MeteorRain(float damage) {
        super(BossAnims.RAIN, "meteor_rain");
        this.damage = damage;
    }

    @Override
    public int duration() {
        return 110;
    }

    @Override
    public int cooldown() {
        return 320;
    }

    @Override
    public int minPhase() {
        return 2;
    }

    @Override
    public Where where() {
        return Where.AIR;
    }

    @Override
    public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
        return 5;
    }

    @Override
    public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
        if (tick == 8) {
            BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.4f);
            BossFx.sound(level, boss.position(), SoundEvents.ENDER_DRAGON_GROWL, 4.0f, 0.5f);
            boss.shake(level, 70, 0.4f, 30);
        }
        if (tick < 18 || tick > 96 || tick % 5 != 0) {
            return;
        }
        List<LivingEntity> enemies = boss.enemiesAround(boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : boss.position(),
                DragonBossEntity.ARENA_RADIUS, 40);
        Vec3 spot;
        if (!enemies.isEmpty() && boss.getRandom().nextFloat() < 0.7f) {
            LivingEntity victim = enemies.get(boss.getRandom().nextInt(enemies.size()));
            spot = victim.position().add((boss.getRandom().nextDouble() - 0.5) * 8, 0, (boss.getRandom().nextDouble() - 0.5) * 8);
        } else {
            double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
            double r = 4 + boss.getRandom().nextDouble() * 26;
            Vec3 c = boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : target.position();
            spot = c.add(Math.cos(a) * r, 0, Math.sin(a) * r);
        }
        double ground = boss.groundY(spot.x, spot.z);
        Vec3 impact = new Vec3(spot.x, ground, spot.z);
        BossFx.warnDisc(level, ParticleTypes.LARGE_SMOKE, impact, 2.5, 14);
        BossFx.ring(level, ParticleTypes.FLAME, impact, 3.0, 18, 0.2);
        // von schräg oben herabstürzen: trifft etwa 22 Ticke später
        Vec3 from = impact.add(8 * (boss.getRandom().nextDouble() - 0.5), 34, 8 * (boss.getRandom().nextDouble() - 0.5));
        Vec3 dir = impact.subtract(from).normalize();
        BossProjectile.shoot(level, boss, BossProjectile.Kind.METEOR, from, dir, 1.2, this.damage);
    }
}
