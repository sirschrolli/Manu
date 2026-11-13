package io.github.sirschrolli.kingdomsatwar.entity.boss.lightning;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Voltarions eigene Angriffe: Kettenblitz, Blitzfeld, Sturzflug-Ansturm, Gewitter und Donnerschrei.
 */
public final class LightningAttacks {
    private LightningAttacks() {}

    /** Ein Blitz, der nur leuchtet und kracht (setzt nichts in Brand – den Schaden verteilt der Angriff selbst). */
    public static void visualBolt(ServerLevel level, Vec3 pos) {
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.snapTo(pos.x, pos.y, pos.z);
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
    }

    /** Ein knisternder Lichtbogen zwischen zwei Punkten (Zickzack aus Funken). */
    public static void arc(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 0.1) {
            return;
        }
        int steps = Math.max(4, (int) (len * 2.5));
        Vec3 prev = from;
        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 p = from.add(d.scale(t));
            if (i < steps) {
                double jitter = 0.6 * Math.sin(t * Math.PI);
                p = p.add((level.getRandom().nextDouble() - 0.5) * jitter, (level.getRandom().nextDouble() - 0.5) * jitter,
                        (level.getRandom().nextDouble() - 0.5) * jitter);
            }
            Vec3 mid = prev.add(p).scale(0.5);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0);
            level.sendParticles(ModParticles.BOSS_SPARK.get(), mid.x, mid.y, mid.z, 1, 0.0, 0.0, 0.0, 0.0);
            prev = p;
        }
    }

    /**
     * Kettenblitz: Voltarion lädt sich auf (die Adern gleißen), dann fährt ein Blitz aus seinem Maul ins Ziel und springt
     * von dort auf bis zu vier weitere Gegner über (jeder Sprung etwas schwächer).
     */
    public static final class ChainLightning extends BossAttack {
        private final float damage;

        public ChainLightning(float damage) {
            super(BossAnims.SPECIAL_1, "chain_lightning");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 46;
        }

        @Override
        public int cooldown() {
            return 160;
        }

        @Override
        public int turnUntil() {
            return 22;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 34 ? 6 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 mouth = boss.mouthPosition();
            if (tick < 22) {
                BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, mouth, 4, 0.8, 0.1);
                if (tick % 6 == 0) {
                    BossFx.sound(level, mouth, SoundEvents.BEACON_AMBIENT, 2.0f, 1.6f);
                }
                if (tick == 2) {
                    boss.setEmpowered(true);
                }
                return;
            }
            if (tick == 22) {
                boss.setEmpowered(false);
                BossFx.sound(level, mouth, SoundEvents.LIGHTNING_BOLT_THUNDER, 4.0f, 1.3f);
                BossFx.sound(level, mouth, SoundEvents.LIGHTNING_BOLT_IMPACT, 3.0f, 1.0f);
                List<LivingEntity> chain = new ArrayList<>();
                chain.add(target);
                LivingEntity last = target;
                for (int i = 0; i < 4; i++) {
                    LivingEntity next = null;
                    double best = 12 * 12;
                    for (LivingEntity e : boss.enemiesAround(last.position(), 12, 6)) {
                        if (chain.contains(e)) {
                            continue;
                        }
                        double d = e.distanceToSqr(last);
                        if (d < best) {
                            best = d;
                            next = e;
                        }
                    }
                    if (next == null) {
                        break;
                    }
                    chain.add(next);
                    last = next;
                }
                Vec3 from = mouth;
                float dmg = this.damage;
                for (LivingEntity e : chain) {
                    Vec3 to = e.position().add(0, e.getBbHeight() * 0.6, 0);
                    arc(level, from, to);
                    level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFD8F4FF), to.x, to.y, to.z, 1, 0, 0, 0, 0);
                    boss.hurtTarget(level, e, dmg);
                    BossEffects.stun(e, 2);
                    from = to;
                    dmg *= 0.8f;
                }
            } else if (tick < 30) {
                BossFx.burst(level, ModParticles.BOSS_SPARK.get(), mouth, 3, 0.6, 0.05);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.setEmpowered(false);
        }
    }

    /**
     * Blitzfeld: Voltarion breitet die Flügel aus und lädt die Luft um sich auf – sechs Sekunden lang zucken Blitze durch
     * alles, was ihm zu nahe ist (alle halbe Sekunde Schaden und Betäubung). Weglaufen!
     */
    public static final class LightningField extends BossAttack {
        private final float damage;
        private final double radius;

        public LightningField(float damage, double radius) {
            super(BossAnims.RING, "lightning_field");
            this.damage = damage;
            this.radius = radius;
        }

        @Override
        public int duration() {
            return 150;
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
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            int close = boss.enemiesAround(boss.position(), this.radius, 5).size();
            return close >= 2 ? 7 : distance < this.radius ? 4 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 c = boss.position().add(0, 0.3, 0);
            if (tick < 24) {
                // Warnung: Funken sammeln sich am Boden im Feld
                BossFx.ring(level, ParticleTypes.ELECTRIC_SPARK, c, this.radius, 40, 0.2);
                BossFx.warnDisc(level, ModParticles.BOSS_SPARK.get(), c, this.radius, 8);
                if (tick == 2) {
                    BossFx.sound(level, c, SoundEvents.BEACON_POWER_SELECT, 3.0f, 1.4f);
                    boss.setEmpowered(true);
                }
                return;
            }
            if (tick > 140) {
                boss.setEmpowered(false);
                return;
            }
            BossFx.ring(level, ModParticles.BOSS_SPARK.get(), c, this.radius, 30, 0.4 + (tick % 10) * 0.25);
            if (tick % 3 == 0) {
                double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                double r = boss.getRandom().nextDouble() * this.radius;
                Vec3 p = c.add(Math.cos(a) * r, boss.getRandom().nextDouble() * 3, Math.sin(a) * r);
                arc(level, boss.bodyPoint(0, 4, 0), p);
            }
            if (tick % 10 == 0) {
                BossFx.sound(level, c, SoundEvents.LIGHTNING_BOLT_IMPACT, 1.5f, 1.6f);
                for (LivingEntity e : boss.enemiesAround(c, this.radius, 5)) {
                    arc(level, boss.bodyPoint(0, 4, 0), e.position().add(0, 1, 0));
                    boss.hurtTarget(level, e, this.damage);
                    BossEffects.stun(e, 1);
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.setEmpowered(false);
        }
    }

    /**
     * Sturzflug-Ansturm: Voltarion steigt hoch, legt die Flügel an und stürzt sich wie ein Blitz auf sein Ziel – beim
     * Aufprall schlägt ein echter Blitz ein und eine Stoßwelle fegt über den Boden.
     */
    public static final class DiveCharge extends BossAttack {
        private final float damage;
        private Vec3 spot = Vec3.ZERO;
        private Vec3 start = Vec3.ZERO;
        private boolean impacted;

        public DiveCharge(float damage) {
            super(BossAnims.AIR_BREATH, "dive_charge");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 90;
        }

        @Override
        public int cooldown() {
            return 220;
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
            return 6;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.spot = target.position();
            this.impacted = false;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            double ground = boss.groundY(this.spot.x, this.spot.z);
            if (tick < 30) {
                // aufsteigen und ausrichten, das Ziel wird markiert
                Vec3 back = this.spot.add(boss.position().subtract(this.spot).multiply(1, 0, 1).normalize().scale(20));
                boss.flyTowards(new Vec3(back.x, ground + 22, back.z), 0.9, 0.12);
                this.spot = target.position();
                BossFx.warnDisc(level, ModParticles.BOSS_SPARK.get(), new Vec3(this.spot.x, ground, this.spot.z), 4.0, 6);
                if (tick == 28) {
                    this.start = boss.position();
                    BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.8f);
                    boss.setEmpowered(true);
                }
                return;
            }
            if (!this.impacted) {
                Vec3 to = new Vec3(this.spot.x, ground + 0.5, this.spot.z).subtract(boss.position());
                double dist = to.length();
                Vec3 vel = to.scale(Math.min(2.2, dist) / Math.max(dist, 0.01));
                boss.setDeltaMovement(vel);
                boss.turnTowards(this.spot, 20.0f);
                BossFx.burst(level, ModParticles.BOSS_SPARK.get(), boss.position().add(0, 2, 0), 6, 1.5, 0.02);
                BossFx.burst(level, ParticleTypes.ELECTRIC_SPARK, boss.position().add(0, 2, 0), 8, 2.0, 0.2);
                if (dist < 3.0 || tick > 70) {
                    this.impacted = true;
                    Vec3 hit = new Vec3(boss.getX(), ground, boss.getZ());
                    visualBolt(level, hit);
                    BossFx.sound(level, hit, SoundEvents.LIGHTNING_BOLT_THUNDER, 5.0f, 0.9f);
                    BossFx.sound(level, hit, SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.7f);
                    boss.shake(level, 60, 1.0f, 20);
                    BossFx.shockwave(level, boss, hit, 9, this.damage, 0.9);
                    for (LivingEntity e : boss.enemiesAround(hit, 9, 4)) {
                        BossEffects.stun(e, 2);
                    }
                    boss.setEmpowered(false);
                    boss.setDeltaMovement(0, 0.6, 0);
                }
                return;
            }
            // wieder aufsteigen
            Vec3 up = boss.position().add(boss.forward().scale(6));
            boss.flyTowards(new Vec3(up.x, ground + 16, up.z), 0.6, 0.08);
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.setEmpowered(false);
        }
    }

    /**
     * Gewitter: Voltarion schwebt hoch über dem Gipfel und ruft den Sturm – Blitze schlagen nacheinander bei seinen
     * Gegnern ein (Funkenkreise am Boden warnen eine Sekunde vorher).
     */
    public static final class Thunderstorm extends BossAttack {
        private final float damage;
        private final List<Vec3> pending = new ArrayList<>();
        private final List<Integer> times = new ArrayList<>();

        public Thunderstorm(float damage) {
            super(BossAnims.RAIN, "thunderstorm");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 120;
        }

        @Override
        public int cooldown() {
            return 280;
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
            return 6;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.pending.clear();
            this.times.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 6) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.75f);
                BossFx.sound(level, boss.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 4.0f, 0.6f);
                boss.setEmpowered(true);
                level.getServer().setWeatherParameters(0, 900, true, true);   // aus dem Regen wird ein Gewitter
            }
            if (tick >= 16 && tick <= 96 && tick % 6 == 0) {
                Vec3 center = boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : boss.position();
                List<LivingEntity> enemies = boss.enemiesAround(center, DragonBossEntity.ARENA_RADIUS, 40);
                Vec3 spot;
                if (!enemies.isEmpty() && boss.getRandom().nextFloat() < 0.8f) {
                    LivingEntity victim = enemies.get(boss.getRandom().nextInt(enemies.size()));
                    spot = victim.position().add((boss.getRandom().nextDouble() - 0.5) * 3, 0, (boss.getRandom().nextDouble() - 0.5) * 3);
                } else {
                    double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                    double r = 4 + boss.getRandom().nextDouble() * 24;
                    spot = center.add(Math.cos(a) * r, 0, Math.sin(a) * r);
                }
                this.pending.add(new Vec3(spot.x, boss.groundY(spot.x, spot.z), spot.z));
                this.times.add(tick + 20);
            }
            for (int i = this.pending.size() - 1; i >= 0; i--) {
                Vec3 p = this.pending.get(i);
                if (tick < this.times.get(i)) {
                    BossFx.ring(level, ParticleTypes.ELECTRIC_SPARK, p, 2.2, 10, 0.1);
                    continue;
                }
                visualBolt(level, p);
                for (LivingEntity e : boss.enemiesAround(p, 2.6, 4)) {
                    boss.hurtTarget(level, e, this.damage);
                    BossEffects.stun(e, 2);
                }
                this.pending.remove(i);
                this.times.remove(i);
            }
            if (tick == 110) {
                boss.setEmpowered(false);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.setEmpowered(false);
            this.pending.clear();
            this.times.clear();
        }
    }

    /**
     * Donnerschrei: Voltarion bäumt sich auf und brüllt mit der Wucht eines Donnerschlags – wer in der Nähe ist, wird
     * zurückgeworfen und kurz betäubt. (Auch bei jedem Phasenwechsel.)
     */
    public static final class ThunderRoar extends BossAttack {
        private final double radius;

        public ThunderRoar(double radius) {
            super(BossAnims.ROAR, "thunder_roar");
            this.radius = radius;
        }

        @Override
        public int duration() {
            return 44;
        }

        @Override
        public int cooldown() {
            return 260;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < this.radius ? 3 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 14) {
                Vec3 mouth = boss.mouthPosition();
                BossFx.sound(level, mouth, ModSounds.DRAGON_ROAR.get(), 8.0f, 0.7f);
                BossFx.sound(level, mouth, SoundEvents.LIGHTNING_BOLT_THUNDER, 6.0f, 0.7f);
                boss.shake(level, 70, 0.9f, 25);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFE8F8FF), mouth.x, mouth.y, mouth.z, 1, 0, 0, 0, 0);
                for (int r = 3; r <= this.radius; r += 3) {
                    BossFx.ring(level, ParticleTypes.ELECTRIC_SPARK, boss.position().add(0, 0.5, 0), r, r * 5, 0.5);
                }
                for (LivingEntity e : boss.enemiesAround(boss.position(), this.radius, 8)) {
                    if (e instanceof Player player && (player.isCreative() || player.isSpectator())) {
                        continue;
                    }
                    DragonBossEntity.pushAway(e, boss.position(), 1.3, 0.5);
                    BossEffects.stun(e, 3);
                }
            }
        }
    }

    /** Wirkung des Blitzodems: kurz betäubt. */
    public static void shock(DragonBossEntity boss, LivingEntity e) {
        BossEffects.stun(e, 1);
    }
}
