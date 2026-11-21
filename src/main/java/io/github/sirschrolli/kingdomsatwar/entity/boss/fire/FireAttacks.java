package io.github.sirschrolli.kingdomsatwar.entity.boss.fire;

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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Pyrrhons eigene große Angriffe: Feuerwirbel, die über das Nest wandern, und Magmaspalten, die sternförmig aufreißen.
 */
public final class FireAttacks {
    private FireAttacks() {}

    /** Glut in den Spalten (orange, groß). */
    private static final DustParticleOptions EMBER = new DustParticleOptions(0xFF7A1A, 1.7f);
    /** Kleine Glutfunken im Feuerwirbel. */
    private static final DustParticleOptions EMBER_SMALL = new DustParticleOptions(0xFFB13A, 0.9f);
    /** Dunkle Kruste am Rand der Spalten. */
    private static final DustParticleOptions CRUST = new DustParticleOptions(0x3A1A10, 1.4f);

    // =====================================================================================
    // Feuerwirbel
    // =====================================================================================

    /**
     * Feuerwirbel: Pyrrhon reißt brüllend den Kopf hoch – vor ihm drehen sich zwei (in der letzten Phase drei) Wirbel aus
     * Flammen in die Höhe, neun Blöcke hoch, und wandern langsam auf die Gegner zu (wer rennt, entkommt ihnen). Wer in
     * einen Wirbel gerät, wird hineingezogen, angehoben und brennt.
     */
    public static final class FireTornado extends BossAttack {
        private static final double HEIGHT = 9.0;
        private static final double REACH = 2.6;
        private static final double SPEED = 0.13;
        private final float damage;
        private final List<Tornado> tornadoes = new ArrayList<>();

        private static final class Tornado {
            Vec3 pos;
            @Nullable UUID target;
            final double phase;

            Tornado(Vec3 pos, double phase) {
                this.pos = pos;
                this.phase = phase;
            }
        }

        public FireTornado(float damage) {
            super(BossAnims.ROAR, "fire_tornado");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 160;
        }

        @Override
        public int cooldown() {
            return 420;
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
            return distance > 8.0 ? 6 : 3;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.tornadoes.clear();
            int count = boss.getPhase() >= 3 ? 3 : 2;
            for (int i = 0; i < count; i++) {
                double side = count == 2 ? (i == 0 ? -7.0 : 7.0) : (i - 1) * 8.0;
                Vec3 p = boss.bodyPoint(side, 0.0, 9.0 - Math.abs(side) * 0.3);
                p = BossFx.keepInArena(boss, p, DragonBossEntity.ARENA_RADIUS - 4.0);
                this.tornadoes.add(new Tornado(BossFx.ground(boss, p.x, p.z), i * 2.1));
            }
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 4) {
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 6.0f, 0.55f);
                boss.shake(level, 50, 0.25f, 20);
            }
            float grow = Mth.clamp(tick / 24.0f, 0.0f, 1.0f);
            float fade = tick > 145 ? (160 - tick) / 15.0f : 1.0f;
            float power = grow * fade;
            List<LivingEntity> enemies = boss.enemiesAround(BossFx.arena(boss), DragonBossEntity.ARENA_RADIUS + 6.0, 40);
            for (Tornado t : this.tornadoes) {
                if (tick == 2) {
                    BossFx.sound(level, t.pos, SoundEvents.FIRECHARGE_USE, 3.0f, 0.5f);
                    BossFx.burst(level, ParticleTypes.LAVA, t.pos.add(0, 0.5, 0), 12, 0.6, 0.2);
                }
                // wandern: alle Sekunde neu auf den nächsten Gegner zu
                if (tick >= 24 && tick <= 145) {
                    if (tick % 20 == 4 || t.target == null) {
                        LivingEntity nearest = null;
                        double best = Double.MAX_VALUE;
                        for (LivingEntity e : enemies) {
                            double d = e.position().distanceToSqr(t.pos);
                            if (d < best) {
                                best = d;
                                nearest = e;
                            }
                        }
                        t.target = nearest == null ? null : nearest.getUUID();
                    }
                    LivingEntity chase = null;
                    for (LivingEntity e : enemies) {
                        if (e.getUUID().equals(t.target)) {
                            chase = e;
                        }
                    }
                    if (chase != null) {
                        Vec3 d = new Vec3(chase.getX() - t.pos.x, 0.0, chase.getZ() - t.pos.z);
                        if (d.lengthSqr() > 0.04) {
                            Vec3 step = d.normalize().scale(SPEED);
                            // ein Wirbel schlingert ein wenig
                            double wobble = Math.sin((tick + t.phase * 10.0) * 0.21) * 0.05;
                            Vec3 next = t.pos.add(step.x - step.z * wobble * 8.0, 0.0, step.z + step.x * wobble * 8.0);
                            next = BossFx.keepInArena(boss, next, DragonBossEntity.ARENA_RADIUS - 2.0);
                            t.pos = BossFx.ground(boss, next.x, next.z);
                        }
                    }
                }
                if (power <= 0.02f) {
                    continue;
                }
                // Aussehen: drei Flammenarme, oben breiter, Rauch an der Spitze, Glut am Fuß
                {
                    // ein schlanker Wirbel: drei Arme aus Flammen, oben breiter, dazwischen Glut, oben Rauch, unten Lava
                    double height = HEIGHT * power;
                    double spin = tick * 0.45 + t.phase;
                    BossFx.helix(level, ParticleTypes.FLAME, t.pos, height, 0.45, 2.4 * power + 0.3, 3, 1.7, spin, 16, 0.16);
                    BossFx.helix(level, EMBER_SMALL, t.pos, height * 0.95, 0.35, 2.0 * power + 0.2, 3, 1.7, spin + 0.5, 8, 0.0);
                    if (tick % 2 == 0) {
                        BossFx.helix(level, ParticleTypes.SMALL_FLAME, t.pos, height * 0.8, 0.2, 1.2 * power, 2, 2.4, -spin * 1.3, 7, 0.1);
                        level.sendParticles(ParticleTypes.LARGE_SMOKE, t.pos.x, t.pos.y + height, t.pos.z, 2, 1.2 * power, 0.3, 1.2 * power, 0.02);
                        BossFx.ring(level, ParticleTypes.FLAME, t.pos, 1.4 * power + 0.6, 12, 0.1);
                    }
                    if (tick % 5 == 0) {
                        level.sendParticles(ParticleTypes.LAVA, t.pos.x, t.pos.y + 0.3, t.pos.z, 2, 0.5, 0.1, 0.5, 0.0);
                        level.sendParticles(ModParticles.BOSS_FLAME.get(), t.pos.x, t.pos.y + 0.4, t.pos.z, 1, 0.3, 0.1, 0.3, 0.0);
                    }
                }
                if (tick % 15 == 0) {
                    BossFx.sound(level, t.pos, SoundEvents.BLAZE_BURN, 2.5f, 0.55f + 0.1f * (float) t.phase);
                }
                // Sog: hineinziehen, herumwirbeln, anheben – und brennen
                for (LivingEntity e : enemies) {
                    Vec3 d = new Vec3(t.pos.x - e.getX(), 0.0, t.pos.z - e.getZ());
                    double dist = d.length();
                    double dy = e.getY() - t.pos.y;
                    if (dist > REACH + 1.5 || dy < -1.0 || dy > HEIGHT) {
                        continue;
                    }
                    Vec3 in = dist < 1.0e-3 ? Vec3.ZERO : d.scale(1.0 / dist);
                    double pull = (dist > REACH ? 0.05 : 0.08) * power;
                    e.push(in.x * pull - in.z * 0.09 * power, dist < REACH ? 0.07 * power : 0.0, in.z * pull + in.x * 0.09 * power);
                    e.needsSync = true;
                    if (dist < REACH && tick % 10 == 0) {
                        boss.hurtTarget(level, e, this.damage * power);
                        BossEffects.burn(e, 4);
                    }
                }
            }
            if (tick == 150) {
                for (Tornado t : this.tornadoes) {
                    BossFx.burst(level, ParticleTypes.LARGE_SMOKE, t.pos.add(0, 3, 0), 30, 1.2, 0.05);
                    BossFx.sound(level, t.pos, SoundEvents.FIRE_EXTINGUISH, 2.0f, 0.6f);
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.tornadoes.clear();
        }

        /** Für Tests: wo die Wirbel gerade stehen. */
        public List<Vec3> positions() {
            return this.tornadoes.stream().map(t -> t.pos).toList();
        }
    }

    // =====================================================================================
    // Magmaspalten
    // =====================================================================================

    /**
     * Magmaspalten: Pyrrhon bäumt sich auf und stampft – von seinen Pranken reißen glühende Spalten sternförmig über den
     * Boden (eine genau auf das Ziel zu), einen Block je Tick. Wer sie kommen sieht, tritt zur Seite: Einen Herzschlag
     * später bricht aus jeder Spalte eine Wand aus Flammen und Lava, die verbrennt und hochschleudert.
     */
    public static final class MagmaFissures extends BossAttack {
        private static final int IMPACT = 16;
        private static final int LENGTH = 24;
        private static final int ERUPT = IMPACT + LENGTH + 6;
        private final float damage;
        private final List<List<Vec3>> cracks = new ArrayList<>();
        private final Set<UUID> hit = new HashSet<>();
        private double aim;

        public MagmaFissures(float damage) {
            super(BossAnims.STOMP, "magma_fissures");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return ERUPT + 26;
        }

        @Override
        public int cooldown() {
            return 300;
        }

        @Override
        public int turnUntil() {
            return 12;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 26.0 ? 6 : 2;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.cracks.clear();
            this.hit.clear();
            this.aim = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX());
        }

        /** Wie viele Spalten (mehr in späteren Phasen). */
        private static int count(DragonBossEntity boss) {
            return switch (boss.getPhase()) {
                case 1 -> 5;
                case 2 -> 6;
                default -> 8;
            };
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == IMPACT) {
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 4.0f, 0.5f);
                BossFx.sound(level, boss.position(), ModSounds.DRAGON_ROAR.get(), 5.0f, 0.7f);
                boss.shake(level, 60, 0.6f, 25);
                BossFx.groundDust(level, boss.position(), 6.0, 80);
                int n = count(boss);
                for (int i = 0; i < n; i++) {
                    this.cracks.add(new ArrayList<>());
                }
            }
            if (tick > IMPACT && tick <= IMPACT + LENGTH && !this.cracks.isEmpty()) {
                // die Spalten wachsen: ein Block je Tick, leicht im Zickzack
                int step = tick - IMPACT;
                for (int i = 0; i < this.cracks.size(); i++) {
                    double a = this.aim + i * Mth.TWO_PI / this.cracks.size() + Math.sin(step * 0.7 + i) * 0.06;
                    double r = 4.0 + step;
                    Vec3 p = BossFx.ground(boss, boss.getX() + Math.cos(a) * r, boss.getZ() + Math.sin(a) * r);
                    if (BossFx.arena(boss).distanceTo(p) > DragonBossEntity.ARENA_RADIUS) {
                        continue;
                    }
                    this.cracks.get(i).add(p);
                    level.sendParticles(EMBER, p.x, p.y + 0.1, p.z, 4, 0.25, 0.02, 0.25, 0.0);
                    level.sendParticles(CRUST, p.x, p.y + 0.1, p.z, 3, 0.45, 0.02, 0.45, 0.0);
                    level.sendParticles(ParticleTypes.SMOKE, p.x, p.y + 0.2, p.z, 1, 0.2, 0.1, 0.2, 0.02);
                    if (step % 2 == 0) {
                        level.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.2, p.z, 1, 0.1, 0.0, 0.1, 0.0);
                    }
                }
                if (step % 4 == 0) {
                    BossFx.sound(level, boss.position(), SoundEvents.LAVA_POP, 3.0f, 0.6f);
                }
            }
            // die Glut pulsiert, bis die Spalten aufbrechen (das ist die Warnung)
            if (tick > IMPACT + LENGTH && tick < ERUPT && tick % 2 == 0) {
                for (List<Vec3> crack : this.cracks) {
                    for (int k = 0; k < crack.size(); k += 2) {
                        Vec3 p = crack.get(k);
                        level.sendParticles(EMBER, p.x, p.y + 0.15, p.z, 1, 0.2, 0.02, 0.2, 0.0);
                    }
                }
            }
            if (tick == ERUPT) {
                BossFx.sound(level, boss.position(), SoundEvents.FIRECHARGE_USE, 5.0f, 0.4f);
                BossFx.sound(level, boss.position(), SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.8f);
            }
            if (tick >= ERUPT && tick < ERUPT + 10) {
                float rise = (tick - ERUPT + 1) / 10.0f;
                for (List<Vec3> crack : this.cracks) {
                    for (int k = 0; k < crack.size(); k++) {
                        Vec3 p = crack.get(k);
                        // eine Wand aufsteigender Flammen, Glut und Lavaspritzer
                        level.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.15, p.z, 0, 0.0, 1.0, 0.0, 0.18 + 0.12 * rise);
                        level.sendParticles(ParticleTypes.FLAME, p.x + 0.2, p.y + 0.4 + rise, p.z - 0.2, 0, 0.0, 1.0, 0.0, 0.12);
                        level.sendParticles(EMBER, p.x, p.y + 0.4 + rise * 1.6, p.z, 1, 0.15, 0.4, 0.15, 0.0);
                        if (k % 3 == tick % 3) {
                            level.sendParticles(ParticleTypes.SMALL_FLAME, p.x, p.y + 1.0 + rise * 2.0, p.z, 2, 0.2, 0.5, 0.2, 0.02);
                        }
                        if (k % 4 == 0 && (tick == ERUPT || tick == ERUPT + 4)) {
                            level.sendParticles(ParticleTypes.LAVA, p.x, p.y + 0.4, p.z, 2, 0.2, 0.3, 0.2, 0.0);
                        }
                        if (k % 6 == 0 && tick == ERUPT + 1) {
                            level.sendParticles(ModParticles.BOSS_FLAME.get(), p.x, p.y + 0.3, p.z, 0, 0.0, 1.0, 0.0, 0.25);
                        }
                    }
                }
                for (LivingEntity e : boss.enemiesAround(boss.position(), 4.0 + LENGTH + 2.0, 6.0)) {
                    if (this.hit.contains(e.getUUID())) {
                        continue;
                    }
                    for (List<Vec3> crack : this.cracks) {
                        if (this.near(crack, e.position(), 1.4)) {
                            this.hit.add(e.getUUID());
                            boss.hurtTarget(level, e, this.damage);
                            BossEffects.burn(e, 6);
                            e.push(0.0, 0.85, 0.0);
                            e.needsSync = true;
                            break;
                        }
                    }
                }
            }
            if (tick >= ERUPT + 10 && tick % 4 == 0) {
                for (List<Vec3> crack : this.cracks) {
                    for (int k = 0; k < crack.size(); k += 3) {
                        Vec3 p = crack.get(k);
                        level.sendParticles(ParticleTypes.SMOKE, p.x, p.y + 0.3, p.z, 1, 0.2, 0.2, 0.2, 0.01);
                    }
                }
            }
        }

        private boolean near(List<Vec3> crack, Vec3 pos, double radius) {
            for (Vec3 p : crack) {
                double dx = p.x - pos.x;
                double dz = p.z - pos.z;
                if (dx * dx + dz * dz < radius * radius && Math.abs(pos.y - p.y) < 2.0) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.cracks.clear();
            this.hit.clear();
        }

        /** Für Tests: die Punkte der Spalten. */
        public List<List<Vec3>> cracks() {
            return this.cracks;
        }
    }
}
