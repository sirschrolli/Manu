package io.github.sirschrolli.kingdomsatwar.entity.boss.water;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Thalassas eigene Angriffe: Flutwelle, Strudel, Blasenkäfig und der Sprung aus der Tiefe.
 */
public final class WaterAttacks {
    private WaterAttacks() {}

    /** Wirkung von Wasserstrahl und Wasserblasen: durchnässt (langsamer), Feuer erlischt. */
    public static void drench(DragonBossEntity boss, LivingEntity e) {
        BossEffects.soak(e, 3);
        e.clearFire();
        DragonBossEntity.pushAway(e, boss.position(), 0.35, 0.1);
    }

    private static ThalassaEntity thalassa(DragonBossEntity boss) {
        return (ThalassaEntity) boss;
    }

    /**
     * Flutwelle: Thalassa bäumt sich hoch auf und schlägt mit dem Vorderleib aufs Wasser – eine Welle rollt als Ring
     * über den See und das Ufer hinaus und reißt alles mit, was ihr im Weg steht (nach außen, ins Wasser zurück oder
     * gegen die Wand).
     */
    public static final class TidalWave extends BossAttack {
        private final float damage;
        private final double maxRadius;
        private final Set<UUID> hit = new HashSet<>();
        private Vec3 center = Vec3.ZERO;

        public TidalWave(float damage, double maxRadius) {
            super(BossAnims.RING, "tidal_wave");
            this.damage = damage;
            this.maxRadius = maxRadius;
        }

        @Override
        public int duration() {
            return 60;
        }

        @Override
        public int cooldown() {
            return 220;
        }

        @Override
        public Where where() {
            return Where.WATER;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            int near = boss.enemiesAround(boss.position(), this.maxRadius, 8).size();
            return near >= 2 ? 7 : distance < this.maxRadius ? 3 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.hit.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            ThalassaEntity t = thalassa(boss);
            if (tick < 20) {
                if (tick == 4) {
                    BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 5.0f, 0.6f);
                }
                level.sendParticles(ParticleTypes.FALLING_WATER, boss.getX(), boss.getY() + 5, boss.getZ(), 10, 2.0, 2.0, 2.0, 0.0);
                return;
            }
            if (tick == 20) {
                this.center = new Vec3(boss.getX(), t.waterTop() + 1.0, boss.getZ());
                BossFx.sound(level, this.center, SoundEvents.GENERIC_SPLASH, 6.0f, 0.4f);
                BossFx.sound(level, this.center, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 6.0f, 0.4f);
                boss.shake(level, 50, 0.8f, 18);
                level.sendParticles(ParticleTypes.SPLASH, this.center.x, this.center.y + 1, this.center.z, 150, 3.0, 1.0, 3.0, 0.6);
            }
            double r = 2.0 + (tick - 20) * 0.75;
            if (r > this.maxRadius) {
                return;
            }
            // die Welle: ein Ring aus Gischt und Wasser, außen hoch, innen fallend
            int count = (int) (r * 7);
            for (int k = 0; k < count; k++) {
                double a = k * Mth.TWO_PI / count;
                double x = this.center.x + Math.cos(a) * r;
                double z = this.center.z + Math.sin(a) * r;
                double ground = Math.max(this.center.y - 1, boss.groundY(x, z));
                level.sendParticles(ParticleTypes.SPLASH, x, ground + 0.4, z, 2, 0.25, 0.3, 0.25, 0.25);
                if (k % 3 == 0) {
                    level.sendParticles(ModParticles.BOSS_WATER.get(), x, ground + 1.2, z, 1, 0.2, 0.5, 0.2, 0.02);
                }
            }
            if (tick % 4 == 0) {
                BossFx.sound(level, this.center, SoundEvents.AMBIENT_UNDERWATER_EXIT, 2.5f, 0.5f);
            }
            for (LivingEntity e : boss.enemiesAround(this.center, r + 1.2, 6)) {
                double d = e.position().subtract(this.center).horizontalDistance();
                if (d > r - 1.6 && this.hit.add(e.getUUID())) {
                    boss.hurtTarget(level, e, this.damage);
                    DragonBossEntity.pushAway(e, this.center, 1.6, 0.55);
                    e.clearFire();
                }
            }
        }
    }

    /**
     * Strudel: Thalassa kreist immer schneller um die Mitte des Sees – das Wasser dreht sich mit, zieht alle Gegner im
     * Umkreis zur Mitte und unter die Oberfläche; wer drin ist, wird herumgewirbelt und nimmt Schaden. Vom Ufer aus
     * zieht der Sog nur, wer nah am Wasser steht.
     */
    public static final class Whirlpool extends BossAttack {
        private final float damage;
        private float angle;

        public Whirlpool(float damage) {
            super(BossAnims.ERUPT, "whirlpool");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 120;
        }

        @Override
        public int cooldown() {
            return 360;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public Where where() {
            return Where.WATER;
        }

        @Override
        public boolean stopsMovement() {
            return false;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 5;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            Vec3 c = thalassa(boss).lakeCenter();
            this.angle = (float) Math.atan2(boss.getZ() - c.z, boss.getX() - c.x);
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            ThalassaEntity t = thalassa(boss);
            Vec3 c = t.lakeCenter();
            double lake = t.lakeRadius();
            float power = Mth.clamp(tick / 30.0f, 0.0f, 1.0f) * (tick > 100 ? (120 - tick) / 20.0f : 1.0f);
            // sie kreist um die Mitte (immer schneller)
            double radius = Math.max(4.0, lake * 0.45);
            this.angle += (float) (0.03 + 0.07 * power);
            Vec3 goal = new Vec3(c.x + Math.cos(this.angle) * radius, c.y - 2.5, c.z + Math.sin(this.angle) * radius);
            t.swimTowards(goal, 0.5 + 0.4 * power, 0.12);
            if (tick == 6) {
                BossFx.sound(level, c, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 5.0f, 0.5f);
                BossFx.sound(level, c, ModSounds.DRAGON_ROAR.get(), 5.0f, 0.65f);
            }
            if (tick % 10 == 0) {
                BossFx.sound(level, c, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 4.0f, 0.6f);
            }
            // der Strudel: Spiralen aus Blasen und Gischt an der Oberfläche
            for (int arm = 0; arm < 4; arm++) {
                for (int s = 0; s < 6; s++) {
                    double rr = 1.5 + s * lake / 6.5;
                    double a = -tick * 0.25 * power + arm * Mth.HALF_PI + rr * 0.35;
                    level.sendParticles(ParticleTypes.SPLASH, c.x + Math.cos(a) * rr, c.y + 1.0, c.z + Math.sin(a) * rr, 1, 0.2, 0.0, 0.2, 0.05);
                    if (s % 2 == 0) {
                        level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, c.x + Math.cos(a) * rr, c.y - 1.5, c.z + Math.sin(a) * rr, 1, 0.3,
                                0.5, 0.3, 0.0);
                    }
                }
            }
            if (power < 0.2) {
                return;
            }
            for (LivingEntity e : boss.enemiesAround(c, lake + 4.0, 12)) {
                Vec3 d = new Vec3(c.x - e.getX(), 0, c.z - e.getZ());
                double dist = d.length();
                boolean inWater = e.isInWater();
                if (!inWater && dist > lake + 2.0) {
                    continue;   // am Ufer zieht der Sog nur nah am Wasser
                }
                Vec3 pull = d.normalize().scale((inWater ? 0.09 : 0.06) * power);
                Vec3 swirl = new Vec3(-d.z, 0, d.x).normalize().scale((inWater ? 0.12 : 0.04) * power);
                e.push(pull.x + swirl.x, inWater ? -0.05 * power : 0.0, pull.z + swirl.z);
                e.needsSync = true;
                if (inWater && tick % 20 == 0) {
                    boss.hurtTarget(level, e, this.damage);
                    e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 40, 1), boss);
                }
            }
        }
    }

    /**
     * Blasenkäfig: Mit gespreiztem Kragen faucht Thalassa – um bis zu zwei Gegner schließt sich eine schimmernde
     * Wasserblase, die sie hilflos in die Höhe hebt. Nach drei Sekunden platzt die Blase: Schaden, und wer hoch
     * genug war, stürzt ab (oder ins Wasser).
     */
    public static final class BubblePrison extends BossAttack {
        private final float damage;
        private final List<LivingEntity> victims = new ArrayList<>();

        public BubblePrison(float damage) {
            super(BossAnims.SPECIAL_1, "bubble_prison");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 90;
        }

        @Override
        public int cooldown() {
            return 300;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public int turnUntil() {
            return 20;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 30 ? 5 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.victims.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 4) {
                BossFx.sound(level, boss.mouthPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT, 4.0f, 0.7f);
            }
            if (tick == 22) {
                this.victims.add(target);
                LivingEntity second = null;
                double best = Double.MAX_VALUE;
                for (LivingEntity e : boss.enemiesAround(target.position(), 18, 10)) {
                    if (e != target && e.distanceToSqr(target) < best) {
                        best = e.distanceToSqr(target);
                        second = e;
                    }
                }
                if (second != null && boss.getPhase() >= 3) {
                    this.victims.add(second);
                }
                for (LivingEntity e : this.victims) {
                    e.addEffect(BossEffects.debuff(MobEffects.LEVITATION, 60, 1), boss);
                    e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 70, 3), boss);
                    BossFx.sound(level, e.position(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 3.0f, 0.8f);
                }
            }
            if (tick > 22 && tick < 82) {
                for (LivingEntity e : this.victims) {
                    if (!e.isAlive()) {
                        continue;
                    }
                    // die schimmernde Blase rund um das Opfer
                    Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
                    double r = Math.max(1.2, e.getBbHeight() * 0.75);
                    for (int k = 0; k < 10; k++) {
                        double u = boss.getRandom().nextDouble() * 2 - 1;
                        double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                        double s = Math.sqrt(1 - u * u);
                        level.sendParticles(ParticleTypes.BUBBLE_POP, c.x + Math.cos(a) * s * r, c.y + u * r, c.z + Math.sin(a) * s * r, 1, 0, 0, 0, 0);
                    }
                    level.sendParticles(ModParticles.BOSS_WATER.get(), c.x, c.y, c.z, 2, r * 0.5, r * 0.5, r * 0.5, 0.0);
                    e.clearFire();
                }
            }
            if (tick == 82) {
                for (LivingEntity e : this.victims) {
                    if (!e.isAlive()) {
                        continue;
                    }
                    Vec3 c = e.position().add(0, e.getBbHeight() * 0.5, 0);
                    level.sendParticles(ParticleTypes.SPLASH, c.x, c.y, c.z, 60, 1.0, 1.0, 1.0, 0.4);
                    BossFx.sound(level, c, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, 4.0f, 0.5f);
                    BossFx.sound(level, c, SoundEvents.GENERIC_SPLASH, 3.0f, 0.8f);
                    e.removeEffect(MobEffects.LEVITATION);
                    boss.hurtTarget(level, e, this.damage);
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            for (LivingEntity e : this.victims) {
                e.removeEffect(MobEffects.LEVITATION);
            }
            this.victims.clear();
        }
    }

    /**
     * Sprung aus der Tiefe: Thalassa taucht ab bis auf den Grund, nimmt Schwung und schießt in hohem Bogen aus dem
     * Wasser – quer über den See dorthin, wo ihr Ziel am Ufer steht (sie landet aber immer im Wasser). Beim
     * Eintauchen schlägt eine Gischtwelle über das Ufer.
     */
    public static final class DepthLeap extends BossAttack {
        private final float damage;
        private Vec3 from = Vec3.ZERO;
        private Vec3 to = Vec3.ZERO;
        private boolean landed;

        public DepthLeap(float damage) {
            super(BossAnims.SPECIAL_2, "depth_leap");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 84;
        }

        @Override
        public int cooldown() {
            return 260;
        }

        @Override
        public Where where() {
            return Where.WATER;
        }

        @Override
        public boolean stopsMovement() {
            return false;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 8 ? 4 : 1;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.landed = false;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            ThalassaEntity t = thalassa(boss);
            Vec3 c = t.lakeCenter();
            if (tick < 24) {
                // abtauchen und auf der anderen Seite des Ziels Schwung holen
                Vec3 away = c.subtract(target.position()).multiply(1, 0, 1);
                away = away.lengthSqr() < 0.01 ? boss.forward().scale(-1) : away.normalize();
                Vec3 deep = t.clampToLake(c.add(away.scale(t.lakeRadius() * 0.6)).add(0, -10, 0), 3.0);
                t.swimTowards(deep, 0.6, 0.15);
                if (tick % 3 == 0) {
                    level.sendParticles(ParticleTypes.BUBBLE, boss.getX(), boss.getY() + 1.5, boss.getZ(), 8, 1.5, 0.5, 1.5, 0.1);
                }
                if (tick == 23) {
                    this.from = boss.position();
                    this.to = t.clampToLake(new Vec3(target.getX(), c.y - 1.5, target.getZ()), 4.0);
                    BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 5.0f, 0.7f);
                }
                return;
            }
            int jumpTicks = 40;
            int k = tick - 24;
            if (k <= jumpTicks) {
                double u = k / (double) jumpTicks;
                double peak = c.y + 9.0;
                double x = Mth.lerp(u, this.from.x, this.to.x);
                double z = Mth.lerp(u, this.from.z, this.to.z);
                double base = Mth.lerp(u, this.from.y, this.to.y);
                double y = base + (peak - Math.max(this.from.y, this.to.y)) * 4.0 * u * (1 - u);
                Vec3 next = new Vec3(x, y, z);
                Vec3 vel = next.subtract(boss.position());
                boss.setDeltaMovement(vel);
                boss.turnTowards(this.to, 12.0f);
                if (Math.abs(boss.getY() - c.y) < 1.5) {
                    level.sendParticles(ParticleTypes.SPLASH, boss.getX(), c.y + 1.0, boss.getZ(), 30, 2.0, 0.3, 2.0, 0.4);
                }
                if (boss.getY() > c.y + 1.0 && k % 2 == 0) {
                    level.sendParticles(ParticleTypes.FALLING_WATER, boss.getX(), boss.getY() + 1.5, boss.getZ(), 20, 2.5, 1.0, 2.5, 0.0);
                }
                if (k == 6) {
                    BossFx.sound(level, boss.position(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 6.0f, 0.5f);
                }
                // was unter ihrem Flug steht, wird umgerissen
                if (boss.getY() > c.y + 2.0) {
                    for (LivingEntity e : boss.enemiesAround(boss.position(), 3.5, 6)) {
                        DragonBossEntity.pushAway(e, boss.position(), 1.0, 0.4);
                    }
                }
                return;
            }
            if (!this.landed) {
                this.landed = true;
                boss.setDeltaMovement(0, -0.2, 0);
                Vec3 at = new Vec3(boss.getX(), c.y + 1.0, boss.getZ());
                BossFx.sound(level, at, SoundEvents.GENERIC_SPLASH, 7.0f, 0.35f);
                BossFx.sound(level, at, SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.6f);
                boss.shake(level, 60, 1.0f, 20);
                level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 1, at.z, 200, 4.0, 1.5, 4.0, 0.7);
                BossFx.burst(level, ModParticles.BOSS_WATER.get(), at.add(0, 2, 0), 70, 4.0, 0.3);
                for (LivingEntity e : boss.enemiesAround(at, 8.0, 6)) {
                    boss.hurtTarget(level, e, this.damage);
                    DragonBossEntity.pushAway(e, at, 1.4, 0.6);
                }
            }
            t.swimTowards(t.clampToLake(boss.position().add(0, -2, 0), 3.0), 0.2, 0.1);
        }
    }
}
