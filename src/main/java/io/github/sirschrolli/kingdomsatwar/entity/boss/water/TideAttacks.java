package io.github.sirschrolli.kingdomsatwar.entity.boss.water;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Thalassas eigene große Angriffe: das Geysirfeld, die Wasserschlangen und die Hydroklinge (seit 1.13). */
public final class TideAttacks {
    private TideAttacks() {}

    /** Tiefes Meeresblau. */
    private static final DustParticleOptions DEEP = new DustParticleOptions(0x1E6FB8, 1.5f);
    /** Helle Gischt. */
    private static final DustParticleOptions FOAM = new DustParticleOptions(0xD6F4FF, 1.2f);

    // =====================================================================================
    // Geysirfeld
    // =====================================================================================

    /**
     * Geysirfeld: Thalassa taucht ab und schlägt mit dem Schwanz – unter jedem Gegner beginnt es zu brodeln (ein Kreis aus
     * Blasen und Gischt), und einen Herzschlag später schießt dort ein Geysir zehn Blöcke hoch, der jeden in die Luft
     * schleudert. Drei Wellen nacheinander; wer das Brodeln sieht und zur Seite springt, bleibt am Boden.
     */
    public static final class GeyserField extends BossAttack {
        private static final int WARN = 16;
        private static final int SPOUT = 12;
        private static final int[] WAVES = {10, 40, 70};
        private final float damage;
        private final List<Geyser> geysers = new ArrayList<>();

        private static final class Geyser {
            final Vec3 pos;
            final int born;
            final Set<UUID> hit = new HashSet<>();

            Geyser(Vec3 pos, int born) {
                this.pos = pos;
                this.born = born;
            }
        }

        public GeyserField(float damage) {
            super(BossAnims.ERUPT, "geyser_field");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return WAVES[WAVES.length - 1] + WARN + SPOUT + 6;
        }

        @Override
        public int cooldown() {
            return 300;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 6;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.geysers.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            int waves = boss.getPhase() >= 2 ? 3 : 2;
            for (int w = 0; w < waves; w++) {
                if (tick != WAVES[w]) {
                    continue;
                }
                int i = 0;
                for (LivingEntity e : boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40)) {
                    if (i++ >= 4) {
                        break;
                    }
                    // etwas vor den Laufweg (wer stehen bleibt, steht mittendrin)
                    Vec3 lead = e.getDeltaMovement().multiply(1.0, 0.0, 1.0).scale(10.0);
                    Vec3 spot = BossFx.ground(boss, e.getX() + lead.x, e.getZ() + lead.z);
                    this.geysers.add(new Geyser(spot, tick));
                }
                BossFx.sound(level, boss.position(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 5.0f, 0.6f);
            }
            for (Geyser g : this.geysers) {
                int age = tick - g.born;
                if (age < WARN) {
                    // es brodelt: Blasen, ein Gischtkreis, der sich zusammenzieht
                    double r = 1.9 - age * 0.06;
                    BossFx.ring(level, ParticleTypes.SPLASH, g.pos, r, 14, 0.15);
                    level.sendParticles(ParticleTypes.BUBBLE_POP, g.pos.x, g.pos.y + 0.2, g.pos.z, 4, 0.7, 0.05, 0.7, 0.02);
                    level.sendParticles(FOAM, g.pos.x, g.pos.y + 0.1, g.pos.z, 3, 0.8, 0.02, 0.8, 0.0);
                    if (age % 5 == 0) {
                        BossFx.sound(level, g.pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 2.0f, 0.8f);
                    }
                } else if (age < WARN + SPOUT) {
                    int t = age - WARN;
                    if (t == 0) {
                        BossFx.sound(level, g.pos, SoundEvents.GENERIC_SPLASH, 4.0f, 0.5f);
                        BossFx.sound(level, g.pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 4.0f, 0.6f);
                    }
                    double height = Math.min(10.0, 3.0 + t * 1.6);
                    // ein Strahl aus Gischt: innen weiß, außen blau, oben eine Krone aus Spritzern
                    for (double h = 0.0; h < height; h += 0.5) {
                        double w = 0.35 + h * 0.035;
                        level.sendParticles(FOAM, g.pos.x, g.pos.y + h, g.pos.z, 2, w * 0.6, 0.15, w * 0.6, 0.0);
                        level.sendParticles(DEEP, g.pos.x, g.pos.y + h, g.pos.z, 1, w, 0.2, w, 0.0);
                        level.sendParticles(ParticleTypes.SPLASH, g.pos.x, g.pos.y + h, g.pos.z, 2, w, 0.2, w, 0.1);
                    }
                    level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, g.pos.x, g.pos.y + 0.5, g.pos.z, 6, 0.4, 0.6, 0.4, 0.2);
                    level.sendParticles(ParticleTypes.SPLASH, g.pos.x, g.pos.y + height, g.pos.z, 14, 1.0, 0.3, 1.0, 0.3);
                    level.sendParticles(ParticleTypes.FALLING_WATER, g.pos.x, g.pos.y + height, g.pos.z, 6, 1.2, 0.2, 1.2, 0.0);
                    if (t % 4 == 0) {
                        level.sendParticles(ModParticles.BOSS_WATER.get(), g.pos.x, g.pos.y + height, g.pos.z, 2, 0.6, 0.2, 0.6, 0.0);
                    }
                    for (LivingEntity e : boss.enemiesAround(g.pos, 1.7, height)) {
                        if (e.getY() >= g.pos.y - 0.5 && g.hit.add(e.getUUID())) {
                            boss.hurtTarget(level, e, this.damage);
                            e.setDeltaMovement(e.getDeltaMovement().x * 0.3, 1.15, e.getDeltaMovement().z * 0.3);
                            e.needsSync = true;
                            BossEffects.soak(e, 3);
                        }
                    }
                } else if (age == WARN + SPOUT) {
                    BossFx.burst(level, ParticleTypes.SPLASH, g.pos.add(0, 1, 0), 30, 1.2, 0.3);
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.geysers.clear();
        }
    }

    // =====================================================================================
    // Wasserschlangen
    // =====================================================================================

    /**
     * Wasserschlangen: Thalassa reißt das Maul auf – aus dem See springen Schlangen aus Wasser, drei (in der letzten
     * Phase fünf), jede in einem hohen Bogen auf einen Gegner zu, mit langem, schäumendem Leib. Wo sie aufschlagen,
     * spritzt das Wasser auseinander: Schaden, Rückstoß und nasse, schwere Glieder.
     */
    public static final class WaterSerpents extends BossAttack {
        private static final int FLIGHT = 26;
        private static final double ARC = 8.0;
        private final float damage;
        private final List<Serpent> serpents = new ArrayList<>();

        private static final class Serpent {
            final Vec3 from;
            Vec3 to;
            final int born;
            final @Nullable UUID target;
            final Deque<Vec3> trail = new ArrayDeque<>();
            boolean landed;

            Serpent(Vec3 from, Vec3 to, int born, @Nullable UUID target) {
                this.from = from;
                this.to = to;
                this.born = born;
                this.target = target;
            }
        }

        public WaterSerpents(float damage) {
            super(BossAnims.VOLLEY, "water_serpents");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 12 + 5 * 8 + FLIGHT + 8;
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
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 6.0 ? 6 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.serpents.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            int count = boss.getPhase() >= 3 ? 5 : 3;
            if (tick >= 12 && (tick - 12) % 8 == 0 && (tick - 12) / 8 < count) {
                int i = (tick - 12) / 8;
                List<LivingEntity> enemies = boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40);
                LivingEntity e = enemies.isEmpty() ? target : enemies.get(i % enemies.size());
                // aus dem Wasser neben Thalassa
                double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                Vec3 from = BossFx.ground(boss, boss.getX() + Math.cos(a) * 5.0, boss.getZ() + Math.sin(a) * 5.0);
                from = new Vec3(from.x, Math.max(from.y, boss.getY()), from.z);
                Vec3 to = BossFx.ground(boss, e.getX(), e.getZ());
                this.serpents.add(new Serpent(from, to, tick, e.getUUID()));
                BossFx.sound(level, from, SoundEvents.GENERIC_SPLASH, 3.0f, 0.7f);
                BossFx.sound(level, from, ModSounds.DRAGON_BREATH.get(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.WATER).get(), 2.5f, 1.2f);
                BossFx.burst(level, ParticleTypes.SPLASH, from.add(0, 0.5, 0), 20, 0.6, 0.3);
            }
            for (Serpent s : this.serpents) {
                if (s.landed) {
                    continue;
                }
                int age = tick - s.born;
                if (age <= FLIGHT / 2) {
                    // im Steigen folgt sie dem Ziel noch ein wenig
                    for (LivingEntity e : boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40)) {
                        if (e.getUUID().equals(s.target)) {
                            Vec3 want = BossFx.ground(boss, e.getX(), e.getZ());
                            s.to = s.to.add(want.subtract(s.to).scale(0.15));
                        }
                    }
                }
                double t = Math.min(1.0, age / (double) FLIGHT);
                Vec3 p = s.from.add(s.to.subtract(s.from).scale(t)).add(0.0, Math.sin(t * Math.PI) * ARC + 0.6, 0.0);
                s.trail.addFirst(p);
                while (s.trail.size() > 9) {
                    s.trail.removeLast();
                }
                // der Kopf schäumt, der Leib zieht nach und wird dünner
                level.sendParticles(DEEP, p.x, p.y, p.z, 4, 0.25, 0.25, 0.25, 0.0);
                level.sendParticles(FOAM, p.x, p.y + 0.2, p.z, 2, 0.2, 0.2, 0.2, 0.0);
                int k = 0;
                for (Vec3 seg : s.trail) {
                    double w = 0.35 * (1.0 - k / 10.0);
                    level.sendParticles(ModParticles.BOSS_WATER.get(), seg.x, seg.y, seg.z, 1, w, w, w, 0.0);
                    if (k % 2 == 0) {
                        level.sendParticles(ParticleTypes.DRIPPING_WATER, seg.x, seg.y - 0.2, seg.z, 1, w, 0.0, w, 0.0);
                    }
                    k++;
                }
                if (age >= FLIGHT) {
                    s.landed = true;
                    Vec3 hit = s.to.add(0.0, 0.5, 0.0);
                    BossFx.burst(level, ParticleTypes.SPLASH, hit, 50, 1.5, 0.4);
                    BossFx.burst(level, ModParticles.BOSS_WATER.get(), hit, 16, 1.2, 0.1);
                    BossFx.ring(level, FOAM, s.to, 2.4, 24, 0.2);
                    BossFx.sound(level, hit, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 4.0f, 0.7f);
                    for (LivingEntity e : boss.enemiesAround(s.to, 2.4, 3.0)) {
                        boss.hurtTarget(level, e, this.damage);
                        DragonBossEntity.pushAway(e, s.to, 0.9, 0.35);
                        BossEffects.soak(e, 3);
                    }
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.serpents.clear();
        }
    }

    // =====================================================================================
    // Hydroklinge (seit 1.13)
    // =====================================================================================

    /**
     * Hydroklinge: Thalassa hebt den Kopf und schneidet mit einem dünnen Strahl Wasser unter höchstem Druck einmal quer über
     * den Grund vor sich – ein Bogen von 150 Grad, knapp über dem Boden (ab der zweiten Phase hin und zurück). Wer im rechten
     * Moment springt, lässt die Klinge unter sich durch; wen sie erwischt, den reißt sie mit und durchnässt ihn.
     */
    public static final class HydroBlade extends BossAttack {
        static final int WINDUP = 16;
        static final int SWEEP = 30;
        static final double NEAR = 3.0;
        static final double RANGE = 18.0;
        static final double REACH = 0.9;
        static final double ARC = 150.0;
        private final float damage;
        private final java.util.Map<UUID, Integer> lastHit = new java.util.HashMap<>();
        private double aim;
        private int sign;

        public HydroBlade(float damage) {
            super(BossAnims.BREATH, "hydro_blade");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return WINDUP + 2 * SWEEP + 10;
        }

        @Override
        public int cooldown() {
            return 280;
        }

        @Override
        public int turnUntil() {
            return WINDUP - 2;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 4.0 && distance < RANGE ? 7 : 1;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.lastHit.clear();
            this.aim = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX());
            this.sign = boss.getRandom().nextBoolean() ? 1 : -1;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 mouth = boss.mouthPosition();
            if (tick < WINDUP) {
                // sie saugt Wasser an: Blasen und Gischt strömen zum Maul
                level.sendParticles(ParticleTypes.BUBBLE_POP, mouth.x, mouth.y, mouth.z, 4, 0.8, 0.6, 0.8, 0.02);
                level.sendParticles(FOAM, mouth.x, mouth.y, mouth.z, 3, 0.6, 0.6, 0.6, 0.0);
                if (tick == WINDUP - 4) {
                    this.aim = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX());
                }
                return;
            }
            int rounds = boss.getPhase() >= 2 ? 2 : 1;
            int k = tick - WINDUP;
            if (k >= SWEEP * rounds) {
                return;
            }
            if (k % SWEEP == 0) {
                BossFx.sound(level, mouth, ModSounds.DRAGON_BREATH.get(io.github.sirschrolli.kingdomsatwar.dragon.DragonType.WATER).get(), 5.0f, 1.5f);
                BossFx.sound(level, mouth, SoundEvents.TRIDENT_RIPTIDE_3.value(), 4.0f, 0.7f);
            }
            // hin, und (ab Phase 2) zurück
            double u = (k % SWEEP + 1) / (double) SWEEP;
            if (k >= SWEEP) {
                u = 1.0 - u;
            }
            double a = this.aim + this.sign * (u - 0.5) * ARC * Mth.DEG_TO_RAD;
            Vec3 dir = new Vec3(Math.cos(a), 0.0, Math.sin(a));
            Vec3 near = BossFx.ground(boss, boss.getX() + dir.x * NEAR, boss.getZ() + dir.z * NEAR).add(0.0, 0.5, 0.0);
            Vec3 far = BossFx.ground(boss, boss.getX() + dir.x * RANGE, boss.getZ() + dir.z * RANGE).add(0.0, 0.5, 0.0);
            // der Strahl: vom Maul hinab, dann flach über den Grund
            BossFx.line(level, FOAM, mouth, near, 0.6, 0.06);
            BossFx.line(level, FOAM, near, far, 0.7, 0.05);
            level.sendParticles(ParticleTypes.SPLASH, far.x, far.y, far.z, 10, 0.5, 0.2, 0.5, 0.2);
            level.sendParticles(DEEP, far.x, far.y, far.z, 4, 0.4, 0.2, 0.4, 0.0);
            Vec3 mid = near.add(far).scale(0.5);
            level.sendParticles(ParticleTypes.SPLASH, mid.x, mid.y, mid.z, 6, 3.0, 0.1, 3.0, 0.1);
            if (k % 6 == 0) {
                BossFx.sound(level, mid, SoundEvents.GENERIC_SPLASH, 2.5f, 1.4f);
            }
            Vec3 sweep = new Vec3(-dir.z, 0.0, dir.x).scale(this.sign * (k >= SWEEP ? -1.0 : 1.0));
            for (LivingEntity e : boss.enemiesAround(boss.position(), RANGE + 2.0, 6.0)) {
                Vec3 at = new Vec3(e.getX(), near.y, e.getZ());
                double ground = boss.groundY(e.getX(), e.getZ());
                if (BossFx.distanceToSegment(at, near, far) > REACH + e.getBbWidth() * 0.5 || e.getY() - ground > 0.9) {
                    continue;
                }
                Integer last = this.lastHit.get(e.getUUID());
                if (last != null && tick - last < 15) {
                    continue;
                }
                this.lastHit.put(e.getUUID(), tick);
                boss.hurtTarget(level, e, this.damage);
                BossEffects.soak(e, 4);
                e.push(sweep.x * 1.1, 0.3, sweep.z * 1.1);
                e.needsSync = true;
                level.sendParticles(ParticleTypes.SPLASH, e.getX(), e.getY(0.5), e.getZ(), 20, 0.4, 0.4, 0.4, 0.3);
            }
        }
    }
}
