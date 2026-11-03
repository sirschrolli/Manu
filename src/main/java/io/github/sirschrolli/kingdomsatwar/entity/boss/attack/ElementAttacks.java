package io.github.sirschrolli.kingdomsatwar.entity.boss.attack;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Element-Angriffe, die mehrere Bosse mit eigenem Aussehen ({@link ElementFx}) benutzen: Odem am Boden, Odem im
 * Sturzflug, Geschoss-Salve, Ring um den Boss und Ausbrüche aus dem Boden.
 */
public final class ElementAttacks {
    private ElementAttacks() {}

    /** Odem am Boden: Ansetzen (Brust glüht), dann schwenkt der Kopf mit dem Odem über das Ziel. */
    public static final class Breath extends BossAttack {
        private final ElementFx fx;
        private final float damage;
        private final double range;
        private float sweep;

        public Breath(ElementFx fx, float damage, double range) {
            super(BossAnims.BREATH, "breath");
            this.fx = fx;
            this.damage = damage;
            this.range = range;
        }

        @Override
        public int duration() {
            return 74;
        }

        @Override
        public int cooldown() {
            return 150;
        }

        @Override
        public int turnUntil() {
            return 18;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 5 && distance < this.range && angle < 70 ? 6 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.sweep = boss.getRandom().nextBoolean() ? 1 : -1;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 mouth = boss.mouthPosition();
            if (tick == 4) {
                BossFx.sound(level, mouth, SoundEvents.FIRECHARGE_USE, 2.0f, 0.5f);
            }
            if (tick < 18) {
                BossFx.burst(level, this.fx.secondary(), mouth, 2, 0.4, 0.01);
                return;
            }
            if (tick > 62) {
                return;
            }
            if (tick == 18) {
                BossFx.sound(level, mouth, this.fx.breathSound(), 4.0f, 0.6f);
            }
            float t = (tick - 18) / 44.0f;
            float yawOffset = Mth.sin((t - 0.5f) * Mth.PI) * 32.0f * this.sweep;
            Vec3 aim = target.position().add(0, target.getBbHeight() * 0.4, 0).subtract(mouth).normalize();
            Vec3 dir = aim.yRot(yawOffset * Mth.DEG_TO_RAD);
            BossFx.stream(level, this.fx.main(), mouth, dir, this.range * 0.55, 2.2, 2, 1.7);
            if (tick % 3 == 0) {
                BossFx.stream(level, this.fx.secondary(), mouth.add(dir.scale(4)), dir, this.range * 0.6, 3.2, 1, 0.5);
            }
            if (tick % 4 == 0) {
                BossFx.cone(boss, mouth, dir, this.range, 16, e -> {
                    boss.hurtTarget(level, e, this.damage);
                    this.fx.onHit().accept(boss, e);
                });
            }
        }
    }

    /** Odem im Sturzflug: Der Boss stößt herab, fliegt tief über das Ziel und speit schräg nach unten. */
    public static final class AirBreath extends BossAttack {
        private final ElementFx fx;
        private final float damage;
        private Vec3 dir = Vec3.ZERO;
        private Vec3 over = Vec3.ZERO;

        public AirBreath(ElementFx fx, float damage) {
            super(BossAnims.AIR_BREATH, "air_breath");
            this.fx = fx;
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 95;
        }

        @Override
        public int cooldown() {
            return 160;
        }

        @Override
        public Where where() {
            return Where.AIR;
        }

        @Override
        public boolean stopsMovement() {
            return false;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 12 ? 6 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            Vec3 d = target.position().subtract(boss.position()).multiply(1, 0, 1);
            this.dir = d.lengthSqr() < 1 ? boss.forward() : d.normalize();
            this.over = target.position();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            double ground = boss.groundY(this.over.x, this.over.z);
            if (tick < 30) {
                Vec3 approach = this.over.subtract(this.dir.scale(14));
                boss.flyTowards(new Vec3(approach.x, ground + 9, approach.z), 0.8, 0.12);
            } else if (tick < 70) {
                Vec3 ahead = boss.position().add(this.dir.scale(4));
                boss.flyTowards(new Vec3(ahead.x, ground + 8, ahead.z), 0.85, 0.15);
                Vec3 mouth = boss.mouthPosition();
                Vec3 breath = this.dir.add(0, -0.75, 0).normalize();
                if (tick == 30) {
                    BossFx.sound(level, mouth, this.fx.breathSound(), 4.0f, 0.6f);
                }
                BossFx.stream(level, this.fx.main(), mouth, breath, 8, 2.0, 2, 1.4);
                if (tick % 3 == 0) {
                    BossFx.cone(boss, mouth, breath, 15, 22, e -> {
                        boss.hurtTarget(level, e, this.damage);
                        this.fx.onHit().accept(boss, e);
                    });
                }
            } else {
                Vec3 up = boss.position().add(this.dir.scale(6));
                boss.flyTowards(new Vec3(up.x, ground + 18, up.z), 0.7, 0.08);
            }
        }
    }

    /** Salve: mehrere Geschosse nacheinander aus dem Maul auf das Ziel (mit Vorhalt). */
    public static final class Volley extends BossAttack {
        private final BossProjectile.Kind kind;
        private final int shots;
        private final float damage;

        public Volley(BossProjectile.Kind kind, int shots, float damage) {
            super(BossAnims.VOLLEY, "volley");
            this.kind = kind;
            this.shots = shots;
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 18 + this.shots * 8;
        }

        @Override
        public int cooldown() {
            return 110;
        }

        @Override
        public int turnUntil() {
            return this.duration();
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            if (boss.isFlying()) {
                return 5;
            }
            return distance > 10 ? 5 : 1;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick >= 14 && (tick - 14) % 8 == 0 && (tick - 14) / 8 < this.shots) {
                Vec3 mouth = boss.mouthPosition();
                Vec3 lead = target.position().add(target.getDeltaMovement().multiply(10, 0, 10)).add(0, target.getBbHeight() * 0.5, 0);
                Vec3 dir = lead.subtract(mouth).normalize();
                float spread = 0.06f;
                dir = dir.add((boss.getRandom().nextFloat() - 0.5f) * spread, (boss.getRandom().nextFloat() - 0.5f) * spread,
                        (boss.getRandom().nextFloat() - 0.5f) * spread).normalize();
                BossProjectile.shoot(level, boss, this.kind, mouth, dir, 1.4, this.damage);
                BossFx.sound(level, mouth, SoundEvents.BLAZE_SHOOT, 3.0f, 0.5f);
            }
        }
    }

    /** Ring: Der Boss bäumt sich auf und entlädt eine Welle, die sich ringförmig ausbreitet (darüber springen!). */
    public static final class NovaRing extends BossAttack {
        private final ElementFx fx;
        private final float damage;
        private final double maxRadius;
        private final Set<UUID> hit = new HashSet<>();
        private Vec3 center = Vec3.ZERO;

        public NovaRing(ElementFx fx, float damage, double maxRadius) {
            super(BossAnims.RING, "nova_ring");
            this.fx = fx;
            this.damage = damage;
            this.maxRadius = maxRadius;
        }

        @Override
        public int duration() {
            return 48;
        }

        @Override
        public int cooldown() {
            return 240;
        }

        @Override
        public int minPhase() {
            return 3;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < this.maxRadius ? 7 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.hit.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick < 20) {
                BossFx.burst(level, this.fx.main(), boss.position().add(0, 3, 0), 6, 2.5, 0.05);
                if (tick == 2) {
                    BossFx.sound(level, boss.position(), SoundEvents.BEACON_POWER_SELECT, 3.0f, 0.5f);
                }
                return;
            }
            if (tick == 20) {
                this.center = new Vec3(boss.getX(), boss.getY(), boss.getZ());
                BossFx.sound(level, this.center, SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.6f);
                boss.shake(level, 50, 0.6f, 15);
            }
            double radius = 3 + (tick - 20) * (this.maxRadius - 3) / 24.0;
            if (radius > this.maxRadius) {
                return;
            }
            BossFx.ring(level, this.fx.main(), this.center, radius, (int) (radius * 7), 0.3);
            BossFx.ring(level, this.fx.secondary(), this.center, radius, (int) (radius * 3), 1.0);
            for (LivingEntity e : boss.enemiesAround(this.center, radius + 1.0, 3.0)) {
                double d = e.position().subtract(this.center).horizontalDistance();
                boolean inBand = d > radius - 1.6;
                boolean low = e.getY() - this.center.y < 1.2;
                if (inBand && low && this.hit.add(e.getUUID())) {
                    boss.hurtTarget(level, e, this.damage);
                    this.fx.onHit().accept(boss, e);
                    DragonBossEntity.pushAway(e, this.center, 1.4, 0.5);
                }
            }
        }
    }

    /** Ausbruch: Der Boss schlägt auf den Boden; unter den Gegnern brodelt es kurz – dann bricht es empor. */
    public static final class Eruption extends BossAttack {
        private final ElementFx fx;
        private final float damage;
        private final int waves;
        private final List<Vec3> spots = new ArrayList<>();

        public Eruption(ElementFx fx, float damage, int waves) {
            super(BossAnims.ERUPT, "eruption");
            this.fx = fx;
            this.damage = damage;
            this.waves = waves;
        }

        @Override
        public int duration() {
            return 30 + this.waves * 22;
        }

        @Override
        public int cooldown() {
            return 200;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public int turnUntil() {
            return 10;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 36 ? 4 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 12) {
                BossFx.sound(level, boss.position(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.4f);
                boss.shake(level, 50, 0.5f, 12);
                BossFx.groundDust(level, boss.bodyPoint(0, 0, 4), 4, 40);
            }
            int local = tick - 12;
            if (local < 0) {
                return;
            }
            int wave = local / 22;
            int inWave = local % 22;
            if (wave >= this.waves) {
                return;
            }
            if (inWave == 0) {
                this.spots.clear();
                List<LivingEntity> enemies = boss.enemiesAround(boss.position(), 36, 10);
                for (LivingEntity e : enemies) {
                    this.spots.add(new Vec3(e.getX(), boss.groundY(e.getX(), e.getZ()), e.getZ()));
                }
                for (int i = 0; i < 3; i++) {
                    double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                    double r = 5 + boss.getRandom().nextDouble() * 18;
                    double x = boss.getX() + Math.cos(a) * r;
                    double z = boss.getZ() + Math.sin(a) * r;
                    this.spots.add(new Vec3(x, boss.groundY(x, z), z));
                }
            }
            if (inWave < 16) {
                for (Vec3 spot : this.spots) {
                    BossFx.warnDisc(level, this.fx.secondary(), spot, 2.2, 3);
                    if (inWave % 5 == 0) {
                        BossFx.sound(level, spot, SoundEvents.LAVA_POP, 1.2f, 0.7f);
                    }
                }
            } else if (inWave == 16) {
                for (Vec3 spot : this.spots) {
                    for (int y = 0; y < 7; y++) {
                        BossFx.burst(level, this.fx.main(), spot.add(0, y * 0.9, 0), 8, 0.5, 0.08);
                    }
                    BossFx.sound(level, spot, SoundEvents.FIRECHARGE_USE, 2.0f, 0.6f);
                    for (LivingEntity e : boss.enemiesAround(spot, 2.4, 3.0)) {
                        boss.hurtTarget(level, e, this.damage);
                        this.fx.onHit().accept(boss, e);
                        e.push(0, 1.1, 0);
                        e.needsSync = true;
                    }
                }
            }
        }
    }
}
