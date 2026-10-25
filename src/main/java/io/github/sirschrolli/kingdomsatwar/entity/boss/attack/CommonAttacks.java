package io.github.sirschrolli.kingdomsatwar.entity.boss.attack;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossPart;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Angriffe, die alle Drachen-Bosse beherrschen: Biss, Prankenhieb, Schwanzhieb, Flügelschlag, Brüllen, Stampfen,
 * Abheben und Landen. */
public final class CommonAttacks {
    private CommonAttacks() {}

    /** Biss: Der Kopf schnellt vor und schnappt zu. */
    public static final class Bite extends BossAttack {
        private final float damage;
        private final double reach;

        public Bite(float damage, double reach) {
            super(BossAnims.BITE, "bite");
            this.damage = damage;
            this.reach = reach;
        }

        @Override
        public int duration() {
            return 24;
        }

        @Override
        public int cooldown() {
            return 30;
        }

        @Override
        public int turnUntil() {
            return 9;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < this.reach && angle < 45 ? 7 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 6) {
                BossFx.sound(level, boss.mouthPosition(), ModSounds.DRAGON_ROAR.get(), 1.6f, 0.8f);
            }
            if (tick == 12) {
                Vec3 mouth = boss.mouthPosition();
                BossFx.sound(level, mouth, ModSounds.DRAGON_BITE.get(), 3.0f, 0.5f);
                for (LivingEntity e : boss.enemiesAround(mouth, 3.6, 4.0)) {
                    boss.hurtTarget(level, e, this.damage);
                    DragonBossEntity.pushAway(e, boss.position(), 0.8, 0.3);
                }
            }
        }
    }

    /** Prankenhieb: ein Vorderbein fegt durch den Bereich vor dem Boss. */
    public static final class Claw extends BossAttack {
        private final float damage;

        public Claw(float damage) {
            super(BossAnims.CLAW, "claw");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 26;
        }

        @Override
        public int cooldown() {
            return 40;
        }

        @Override
        public int turnUntil() {
            return 8;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 9.5 && angle < 55 ? 5 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 13) {
                Vec3 center = boss.bodyPoint(0, 1.2, 5.2);
                BossFx.sound(level, center, SoundEvents.PLAYER_ATTACK_SWEEP, 2.5f, 0.4f);
                BossFx.groundDust(level, center, 3.0, 25);
                for (LivingEntity e : boss.enemiesAround(center, 4.2, 3.5)) {
                    boss.hurtTarget(level, e, this.damage);
                    DragonBossEntity.pushAway(e, boss.position(), 1.3, 0.35);
                }
            }
        }
    }

    /** Schwanzhieb: Der Boss dreht sich schwungvoll, der Schwanz fegt alles um ihn herum weg. */
    public static final class TailSweep extends BossAttack {
        private final float damage;
        private final Set<UUID> hit = new HashSet<>();
        private float spin;

        public TailSweep(float damage) {
            super(BossAnims.TAIL_SWEEP, "tail_sweep");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 36;
        }

        @Override
        public int cooldown() {
            return 70;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            if (distance > 15) {
                return 0;
            }
            if (angle > 100) {
                return 9;
            }
            int around = boss.enemiesAround(boss.position(), 12, 4).size();
            return around >= 3 ? 4 : 0.5f;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.hit.clear();
            this.spin = boss.getRandom().nextBoolean() ? 1 : -1;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick >= 10 && tick < 24) {
                float step = 15.0f * this.spin;
                boss.setYRot(boss.getYRot() + step);
                boss.yBodyRot += step;
                if (tick == 11) {
                    BossFx.sound(level, boss.position(), SoundEvents.PLAYER_ATTACK_SWEEP, 3.0f, 0.3f);
                }
                for (BossPart part : boss.bossParts()) {
                    if (!part.name.startsWith("tail")) {
                        continue;
                    }
                    for (LivingEntity e : boss.enemiesAround(part.position(), 2.6, 3.0)) {
                        if (this.hit.add(e.getUUID())) {
                            boss.hurtTarget(level, e, this.damage);
                            DragonBossEntity.pushAway(e, boss.position(), 2.2, 0.6);
                        }
                    }
                }
            }
        }
    }

    /** Flügelschlag: eine Sturmböe nach vorn, die Gegner zurückwirft. */
    public static final class WingBuffet extends BossAttack {
        private final float damage;

        public WingBuffet(float damage) {
            super(BossAnims.WING_BUFFET, "wing_buffet");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 32;
        }

        @Override
        public int cooldown() {
            return 120;
        }

        @Override
        public int turnUntil() {
            return 12;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            int front = boss.enemiesAround(boss.bodyPoint(0, 0, 7), 7, 4).size();
            return distance < 16 && angle < 50 ? (front >= 2 ? 6 : 2) : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 16) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_FLAP.get(), 4.0f, 0.4f);
                Vec3 origin = boss.bodyPoint(0, 1.0, 2.0);
                BossFx.cone(boss, origin, boss.forward(), 18, 50, e -> {
                    boss.hurtTarget(level, e, this.damage);
                    DragonBossEntity.pushAway(e, origin, 2.6, 0.7);
                });
                for (int i = 0; i < 4; i++) {
                    BossFx.stream(level, ParticleTypes.CLOUD, origin.add(0, i * 0.8, 0), boss.forward(), 14, 2.5, 2, 0.6);
                }
            }
        }
    }

    /** Brüllen: betäubt kurz (Langsamkeit, Schwäche), lässt den Boden beben. */
    public static final class Roar extends BossAttack {
        private final double radius;

        public Roar(double radius) {
            super(BossAnims.ROAR, "roar");
            this.radius = radius;
        }

        @Override
        public int duration() {
            return 44;
        }

        @Override
        public int cooldown() {
            return 420;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 0.6f;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 12) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.45f);
                BossFx.sound(level, boss.position(), SoundEvents.ENDER_DRAGON_GROWL, 4.0f, 0.6f);
                boss.shake(level, 50, 0.45f, 25);
                for (LivingEntity e : boss.enemiesAround(boss.position(), this.radius, 10)) {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), boss);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), boss);
                }
                net.minecraft.world.phys.Vec3 feet = boss.position();
                for (int r = 4; r <= 12; r += 4) {
                    BossFx.ring(level, ParticleTypes.CLOUD, feet, r, r * 6, 0.3);
                }
                BossFx.groundDust(level, feet, 6, 40);
            }
        }
    }

    /** Stampfen: Der Boss bäumt sich auf und kracht mit den Vorderbeinen auf den Boden – Stoßwelle. */
    public static final class Stomp extends BossAttack {
        private final float damage;

        public Stomp(float damage) {
            super(BossAnims.STOMP, "stomp");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 40;
        }

        @Override
        public int cooldown() {
            return 140;
        }

        @Override
        public int minPhase() {
            return 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            int close = boss.enemiesAround(boss.position(), 10, 4).size();
            return close >= 2 ? 6 : distance < 8 ? 2 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 20) {
                Vec3 center = boss.bodyPoint(0, 0, 3);
                center = new Vec3(center.x, boss.getY(), center.z);
                BossFx.sound(level, center, SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.5f);
                BossFx.sound(level, center, ModSounds.DRAGON_STEP.get(), 4.0f, 0.4f);
                boss.shake(level, 40, 0.8f, 18);
                BossFx.shockwave(level, boss, center, 10, this.damage, 0.9);
            }
        }
    }

    /** Abheben: in die Hocke, Sprung, kräftige Flügelschläge. */
    public static final class TakeOff extends BossAttack {
        public TakeOff() {
            super(BossAnims.TAKE_OFF, "take_off");
        }

        @Override
        public int duration() {
            return 32;
        }

        @Override
        public int cooldown() {
            return 0;
        }

        @Override
        public boolean stopsMovement() {
            return false;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick < 10) {
                boss.stopMoving();
            } else if (tick == 10) {
                boss.setMoveMode(DragonBossEntity.MoveMode.AIR);
                boss.setDeltaMovement(boss.forward().scale(0.25).add(0, 1.1, 0));
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_FLAP.get(), 4.0f, 0.45f);
                BossFx.groundDust(level, boss.position(), 6, 80);
                boss.shake(level, 30, 0.4f, 10);
            } else if (tick > 10) {
                boss.setDeltaMovement(boss.getDeltaMovement().multiply(0.95, 0.9, 0.95).add(0, 0.05, 0));
            }
        }
    }

    /** Landen: Sinkflug zu einem Punkt nahe dem Ziel, Aufprall mit kleiner Stoßwelle. */
    public static final class Land extends BossAttack {
        private Vec3 spot = Vec3.ZERO;

        public Land() {
            super(BossAnims.LAND, "land");
        }

        @Override
        public int duration() {
            return 90;
        }

        @Override
        public int cooldown() {
            return 0;
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
            return 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            Vec3 away = boss.position().subtract(target.position()).multiply(1, 0, 1);
            away = away.lengthSqr() < 1 ? new Vec3(1, 0, 0) : away.normalize();
            Vec3 p = target.position().add(away.scale(10));
            this.spot = new Vec3(p.x, boss.groundY(p.x, p.z), p.z);
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (!boss.isFlying()) {
                return;
            }
            double dist = boss.position().subtract(this.spot).horizontalDistance();
            Vec3 aim = dist > 8 ? this.spot.add(0, Math.min(10, dist * 0.5), 0) : this.spot.add(0, -2, 0);
            boss.flyTowards(aim, dist > 8 ? 0.7 : 0.45, 0.12);
            if (dist < 8) {
                boss.turnTowards(target.position(), 4.0f);
            }
            boolean low = boss.getY() - boss.groundY(boss.getX(), boss.getZ()) < 1.0;
            if ((dist < 4 && low) || boss.onGround() || tick >= this.duration() - 2) {
                boss.setMoveMode(DragonBossEntity.MoveMode.GROUND);
                boss.setDeltaMovement(Vec3.ZERO);
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 2.5f, 0.5f);
                boss.shake(level, 40, 0.6f, 15);
                BossFx.shockwave(level, boss, boss.position(), 8, 8, 0.6);
                boss.finishAttack(level);
            }
        }
    }

    /** Liefert Gegner in einer Liste, die sich nicht wiederholen sollen (z. B. pro Welle einmal). */
    public static List<LivingEntity> once(List<LivingEntity> list, Set<UUID> done) {
        list.removeIf(e -> !done.add(e.getUUID()));
        return list;
    }

    public static float lerp(float t, float a, float b) {
        return Mth.lerp(Mth.clamp(t, 0, 1), a, b);
    }
}
