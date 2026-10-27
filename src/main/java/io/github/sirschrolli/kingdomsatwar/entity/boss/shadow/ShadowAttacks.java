package io.github.sirschrolli.kingdomsatwar.entity.boss.shadow;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Nyxars eigene Angriffe: Schattenschrei, Schattensprung, Schattenklauen, Finsternis, Seelenraub und Schattenregen.
 * Alles, was Schatten berührt, zehrt (Verdorren) und trübt die Sicht – aber nie so, dass man nichts mehr sieht.
 */
public final class ShadowAttacks {
    private ShadowAttacks() {}

    /** Wirkung des Schattenodems: zehrt und verdunkelt kurz die Sicht. */
    public static void wither(DragonBossEntity boss, LivingEntity e) {
        BossEffects.wither(e, 2);
    }

    private static Vec3 center(DragonBossEntity boss) {
        return boss.getHome() != null ? Vec3.atCenterOf(boss.getHome()) : boss.position();
    }

    private static boolean immune(LivingEntity e) {
        return e instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    /** Eine dünne Linie aus Seelen zwischen zwei Punkten (Seelenraub, Finsternis). */
    static void soulLine(ServerLevel level, Vec3 from, Vec3 to, double phase) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 0.2) {
            return;
        }
        int steps = Math.max(3, (int) (len * 1.6));
        for (int i = 0; i < steps; i++) {
            double t = (i + phase % 1.0) / steps;
            double wobble = Math.sin(t * Math.PI) * 0.6;
            Vec3 p = from.add(d.scale(t)).add(Math.sin(t * 9 + phase * 3) * wobble, Math.cos(t * 7 + phase * 2) * wobble * 0.5, 0);
            level.sendParticles(i % 3 == 0 ? ParticleTypes.SCULK_SOUL : ModParticles.BOSS_SHADOW.get(), p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    // =====================================================================================
    // Schattenschrei
    // =====================================================================================

    /**
     * Schattenschrei: Nyxar reckt den Hals und kreischt. Ein Ring aus Dunkelheit fegt über die Arena – wer zu nah ist,
     * wird zurückgeworfen, sieht kurz kaum etwas und ist geschwächt.
     */
    public static final class ShadowShriek extends BossAttack {
        private final double radius;

        public ShadowShriek(double radius) {
            super(BossAnims.ROAR, "shadow_shriek");
            this.radius = radius;
        }

        @Override
        public int duration() {
            return 46;
        }

        @Override
        public int cooldown() {
            return 280;
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
            Vec3 mouth = boss.mouthPosition();
            if (tick < 14) {
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, mouth, 2, 0.6, 0.02);
                return;
            }
            if (tick == 14) {
                BossFx.sound(level, mouth, ModSounds.NYXAR_SHRIEK.get(), 8.0f, 0.6f);
                BossFx.sound(level, mouth, SoundEvents.WARDEN_SONIC_BOOM, 5.0f, 0.5f);
                boss.shake(level, 70, 0.8f, 25);
                level.sendParticles(ParticleTypes.SONIC_BOOM, mouth.x, mouth.y, mouth.z, 1, 0, 0, 0, 0);
                for (LivingEntity e : boss.enemiesAround(boss.position(), this.radius, 10)) {
                    if (immune(e)) {
                        continue;
                    }
                    DragonBossEntity.pushAway(e, boss.position(), 1.2, 0.45);
                    e.addEffect(BossEffects.debuff(MobEffects.DARKNESS, 140, 0), boss);
                    e.addEffect(BossEffects.debuff(MobEffects.WEAKNESS, 120, 0), boss);
                    e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 40, 1), boss);
                }
            }
            // der Ring wandert nach außen
            int t = tick - 14;
            if (t >= 0 && t < 14) {
                double r = 2 + t * this.radius / 13.0;
                BossFx.ring(level, ModParticles.BOSS_SHADOW.get(), boss.position(), r, (int) (r * 5), 0.4);
                BossFx.ring(level, ParticleTypes.SCULK_SOUL, boss.position(), r, (int) (r * 2), 1.4);
            }
        }
    }

    // =====================================================================================
    // Schattensprung
    // =====================================================================================

    /**
     * Schattensprung: Nyxar zerfließt zu Schatten, gleitet unsichtbar über den Boden und taucht hinter seinem Ziel
     * wieder auf – mit einem Prankenhieb aus der Dunkelheit. Eine Seelenspur am Boden verrät, wohin er gleitet.
     */
    public static final class ShadowStep extends BossAttack {
        private final float damage;
        private Vec3 from = Vec3.ZERO;
        private Vec3 to = Vec3.ZERO;

        public ShadowStep(float damage) {
            super(BossAnims.SPECIAL_1, "shadow_step");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return 58;
        }

        @Override
        public int cooldown() {
            return 240;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 12 ? 6 : angle > 100 ? 4 : 1.5f;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.from = boss.position();
            this.to = this.from;
        }

        /** Ein Platz hinter dem Ziel (vom Boss aus gesehen), der in der Arena und nicht zu hoch oder tief liegt. */
        private Vec3 destination(DragonBossEntity boss, LivingEntity target) {
            Vec3 away = target.position().subtract(boss.position()).multiply(1, 0, 1);
            away = away.lengthSqr() < 1.0e-3 ? boss.forward() : away.normalize();
            Vec3 home = center(boss);
            for (double dist : new double[] {9.0, 7.0, 5.0}) {
                for (double side : new double[] {0.0, 0.6, -0.6}) {
                    Vec3 dir = away.yRot((float) side);
                    double x = target.getX() + dir.x * dist;
                    double z = target.getZ() + dir.z * dist;
                    int y = boss.groundY(x, z);
                    if (Math.abs(y - target.getY()) <= 4 && new Vec3(x, 0, z).distanceTo(new Vec3(home.x, 0, home.z)) < DragonBossEntity.ARENA_RADIUS - 6) {
                        return new Vec3(x, y, z);
                    }
                }
            }
            return boss.position();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick < 12) {
                // er zerfließt
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, boss.position().add(0, 2.5, 0), 6, 3.0, 0.03);
                BossFx.burst(level, ModParticles.BOSS_SHADOW.get(), boss.position().add(0, 2.0, 0), 4, 3.0, 0.02);
                if (tick == 2) {
                    BossFx.sound(level, boss.position(), SoundEvents.WARDEN_DIG, 4.0f, 0.8f);
                    BossFx.sound(level, boss.position(), SoundEvents.SCULK_CATALYST_BLOOM, 3.0f, 0.5f);
                }
                return;
            }
            if (tick == 12) {
                boss.setInvisible(true);
                this.from = boss.position();
                this.to = this.destination(boss, target);
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, this.from.add(0, 2, 0), 40, 3.0, 0.15);
                BossFx.sound(level, this.from, SoundEvents.SOUL_ESCAPE.value(), 4.0f, 0.5f);
            }
            if (tick >= 12 && tick < 30) {
                // die Spur gleitet am Boden zum Ziel
                double k = (tick - 12) / 18.0;
                Vec3 p = this.from.lerp(this.to, k);
                Vec3 ground = new Vec3(p.x, boss.groundY(p.x, p.z) + 0.2, p.z);
                level.sendParticles(ParticleTypes.SCULK_SOUL, ground.x, ground.y, ground.z, 6, 0.8, 0.1, 0.8, 0.01);
                level.sendParticles(ModParticles.BOSS_SHADOW.get(), ground.x, ground.y + 0.3, ground.z, 4, 1.0, 0.2, 1.0, 0.0);
                if (tick % 6 == 0) {
                    BossFx.sound(level, ground, SoundEvents.SCULK_BLOCK_SPREAD, 2.0f, 0.5f);
                }
                return;
            }
            if (tick == 30) {
                float yaw = (float) (Mth.atan2(target.getZ() - this.to.z, target.getX() - this.to.x) * Mth.RAD_TO_DEG) - 90.0f;
                boss.teleportTo(this.to.x, this.to.y, this.to.z);
                boss.setYRot(yaw);
                boss.setYBodyRot(yaw);
                boss.setYHeadRot(yaw);
                boss.setInvisible(false);
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, this.to.add(0, 3, 0), 80, 4.0, 0.2);
                BossFx.burst(level, ModParticles.BOSS_SHADOW.get(), this.to.add(0, 2, 0), 30, 4.0, 0.05);
                BossFx.sound(level, this.to, SoundEvents.WARDEN_EMERGE, 4.0f, 1.2f);
                BossFx.sound(level, this.to, ModSounds.DRAGON_ROAR.get(), 5.0f, 0.75f);
                boss.shake(level, 30, 0.4f, 10);
                return;
            }
            if (tick == 38) {
                // Prankenhieb aus der Dunkelheit
                Vec3 front = boss.bodyPoint(0, 1.5, 5.0);
                BossFx.sound(level, front, SoundEvents.PLAYER_ATTACK_SWEEP, 3.0f, 0.5f);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, front.x, front.y, front.z, 3, 1.6, 0.3, 1.6, 0);
                for (LivingEntity e : boss.enemiesAround(front, 6.0, 4.0)) {
                    if (boss.hurtTarget(level, e, this.damage)) {
                        BossEffects.wither(e, 3);
                        DragonBossEntity.pushAway(e, boss.position(), 0.9, 0.35);
                    }
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.setInvisible(false);
        }
    }

    // =====================================================================================
    // Schattenklauen
    // =====================================================================================

    /**
     * Schattenklauen: Nyxar schlägt die Pranken auf, und unter jedem Gegner sammelt sich ein Fleck aus Dunkelheit. Kurz
     * darauf greifen Klauen aus Schatten daraus empor – wer noch darin steht, wird hochgerissen und zehrt.
     */
    public static final class ShadowClaws extends BossAttack {
        private final float damage;
        private final int minPhase;
        private final List<Vec3> spots = new ArrayList<>();

        public ShadowClaws(float damage, int minPhase) {
            super(BossAnims.ERUPT, "shadow_claws");
            this.damage = damage;
            this.minPhase = minPhase;
        }

        @Override
        public int duration() {
            return 12 + 3 * 24;
        }

        @Override
        public int cooldown() {
            return 210;
        }

        @Override
        public int minPhase() {
            return this.minPhase;
        }

        @Override
        public int turnUntil() {
            return 10;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 36 ? 5 : 0;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 12) {
                BossFx.sound(level, boss.position(), SoundEvents.WARDEN_ATTACK_IMPACT, 4.0f, 0.5f);
                boss.shake(level, 50, 0.5f, 12);
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, boss.bodyPoint(0, 0.5, 4), 40, 3.0, 0.1);
            }
            int local = tick - 12;
            if (local < 0) {
                return;
            }
            int inWave = local % 24;
            if (inWave == 0) {
                this.spots.clear();
                for (LivingEntity e : boss.enemiesAround(boss.position(), 36, 10)) {
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
                    BossFx.warnDisc(level, ParticleTypes.SCULK_SOUL, spot.add(0, 0.1, 0), 1.6 + inWave * 0.02, 3);
                    level.sendParticles(ModParticles.BOSS_SHADOW.get(), spot.x, spot.y + 0.2, spot.z, 2, 0.7, 0.05, 0.7, 0.0);
                    if (inWave % 6 == 0) {
                        BossFx.sound(level, spot, SoundEvents.SCULK_BLOCK_SPREAD, 1.6f, 0.6f);
                    }
                }
            } else if (inWave == 16) {
                for (Vec3 spot : this.spots) {
                    claw(level, boss, spot);
                    for (LivingEntity e : boss.enemiesAround(spot, 1.9, 3.0)) {
                        if (boss.hurtTarget(level, e, this.damage)) {
                            BossEffects.wither(e, 3);
                            e.push(0, 0.9, 0);
                            e.needsSync = true;
                        }
                    }
                }
            }
        }

        /** Fünf Schattenkrallen schnellen aus dem Boden und krümmen sich nach innen. */
        private static void claw(ServerLevel level, DragonBossEntity boss, Vec3 spot) {
            BossFx.sound(level, spot, SoundEvents.WARDEN_ATTACK_IMPACT, 2.0f, 0.8f);
            BossFx.sound(level, spot, SoundEvents.SOUL_ESCAPE.value(), 2.0f, 0.6f);
            for (int f = 0; f < 5; f++) {
                double a = f * Mth.TWO_PI / 5 + boss.getRandom().nextDouble() * 0.3;
                for (int s = 0; s < 9; s++) {
                    double h = s * 0.42;
                    double r = 1.4 - s * 0.14;
                    double x = spot.x + Math.cos(a) * r;
                    double z = spot.z + Math.sin(a) * r;
                    level.sendParticles(ModParticles.BOSS_SHADOW.get(), x, spot.y + h, z, 1, 0.04, 0.04, 0.04, 0.0);
                    if (s % 2 == 0) {
                        level.sendParticles(ParticleTypes.SCULK_SOUL, x, spot.y + h, z, 1, 0.05, 0.05, 0.05, 0.01);
                    }
                }
            }
            level.sendParticles(ParticleTypes.SOUL, spot.x, spot.y + 0.5, spot.z, 10, 0.6, 0.4, 0.6, 0.05);
        }
    }

    // =====================================================================================
    // Finsternis
    // =====================================================================================

    /**
     * Finsternis: Nyxar breitet die Schwingen aus und löscht das Licht – die ganze Arena versinkt im Dunkel. Aus seinem
     * Leib lösen sich Irrlichter, die die Gegner verfolgen. Wer läuft, entkommt ihnen; wer stehen bleibt, wird
     * getroffen. (Sein Zeichenangriff in Phase 1 und 2.)
     */
    public static final class Eclipse extends BossAttack {
        private static final double ORB_SPEED = 0.34;
        private final int minPhase;
        private final List<Orb> orbs = new ArrayList<>();

        private static final class Orb {
            Vec3 pos;
            final LivingEntity target;
            final int delay;
            int life;

            Orb(Vec3 pos, LivingEntity target, int delay) {
                this.pos = pos;
                this.target = target;
                this.delay = delay;
            }
        }

        public Eclipse(int minPhase) {
            super(BossAnims.SPECIAL_2, "eclipse");
            this.minPhase = minPhase;
        }

        @Override
        public int duration() {
            return 170;
        }

        @Override
        public int cooldown() {
            return 560;
        }

        @Override
        public int minPhase() {
            return this.minPhase;
        }

        @Override
        public Where where() {
            return Where.ANY;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return 4;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.orbs.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 body = boss.position().add(0, 4, 0);
            if (tick < 24) {
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, body, 8, 4.0, 0.04);
                if (tick == 4) {
                    BossFx.sound(level, body, SoundEvents.WARDEN_ROAR, 6.0f, 0.5f);
                    boss.setEmpowered(true);
                }
                return;
            }
            if (tick == 24) {
                boss.setEmpowered(false);
                BossFx.sound(level, body, SoundEvents.WARDEN_SONIC_BOOM, 6.0f, 0.4f);
                BossFx.sound(level, body, SoundEvents.BEACON_DEACTIVATE, 6.0f, 0.5f);
                boss.shake(level, 80, 0.7f, 30);
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF4A2A7A), body.x, body.y, body.z, 2, 0.5, 0.5, 0.5, 0);
                BossFx.burst(level, ModParticles.BOSS_SHADOW.get(), body, 120, 10.0, 0.2);
                Vec3 c = center(boss);
                List<LivingEntity> enemies = boss.enemiesAround(c, DragonBossEntity.ARENA_RADIUS, 40);
                for (LivingEntity e : enemies) {
                    if (immune(e)) {
                        continue;
                    }
                    e.addEffect(BossEffects.debuff(MobEffects.DARKNESS, 260, 0), boss);
                }
                // je Gegner zwei Irrlichter (höchstens zwölf), nacheinander losgelassen
                int n = 0;
                for (LivingEntity e : enemies) {
                    for (int k = 0; k < 2 && n < 12; k++, n++) {
                        double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                        this.orbs.add(new Orb(body.add(Math.cos(a) * 3, boss.getRandom().nextDouble() * 2, Math.sin(a) * 3), e, n * 6));
                    }
                }
            }
            for (Orb orb : this.orbs) {
                orb.life++;
                if (orb.life < orb.delay) {
                    // kreist noch um Nyxar
                    double a = (tick + orb.delay * 7) * 0.2;
                    orb.pos = body.add(Math.cos(a) * 4, Math.sin(a * 0.7) * 1.5, Math.sin(a) * 4);
                } else if (orb.target.isAlive()) {
                    Vec3 goal = orb.target.position().add(0, orb.target.getBbHeight() * 0.5, 0);
                    Vec3 d = goal.subtract(orb.pos);
                    double len = d.length();
                    if (len < 1.3) {
                        burst(level, boss, orb);
                        orb.life = Integer.MIN_VALUE / 2;
                        continue;
                    }
                    orb.pos = orb.pos.add(d.scale(Math.min(ORB_SPEED * (1.0 + 0.004 * orb.life), len) / len));
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, orb.pos.x, orb.pos.y, orb.pos.z, 1, 0.08, 0.08, 0.08, 0.0);
                level.sendParticles(ModParticles.BOSS_SHADOW.get(), orb.pos.x, orb.pos.y, orb.pos.z, 1, 0.1, 0.1, 0.1, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, orb.pos.x, orb.pos.y, orb.pos.z, 1, 0.02, 0.02, 0.02, 0.0);
                if ((tick + orb.delay) % 16 == 0) {
                    BossFx.sound(level, orb.pos, SoundEvents.SOUL_ESCAPE.value(), 0.8f, 1.4f);
                }
            }
            this.orbs.removeIf(o -> o.life < 0 || !o.target.isAlive());
        }

        private static void burst(ServerLevel level, DragonBossEntity boss, Orb orb) {
            BossFx.burst(level, ParticleTypes.SCULK_SOUL, orb.pos, 20, 0.8, 0.08);
            BossFx.sound(level, orb.pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, 1.0f, 1.6f);
            for (LivingEntity e : boss.enemiesAround(orb.pos, 2.0, 2.0)) {
                if (e.hurtServer(level, boss.damageSources().indirectMagic(boss, boss), 6.0f * boss.damageMultiplier())) {
                    BossEffects.wither(e, 2);
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            boss.setEmpowered(false);
            this.orbs.clear();
        }
    }

    // =====================================================================================
    // Seelenraub
    // =====================================================================================

    /**
     * Seelenraub: Nyxar richtet sich auf, und von bis zu drei Gegnern fließen Seelenfäden in sein Maul. Solange die
     * Fäden halten, nehmen sie Schaden und er heilt sich daran. Wer sich weit genug entfernt, zerreißt den Faden.
     */
    public static final class SoulDrain extends BossAttack {
        private static final double BREAK_DISTANCE = 22.0;
        private final float damage;
        private final int minPhase;
        private final List<LivingEntity> victims = new ArrayList<>();

        public SoulDrain(float damage, int minPhase) {
            super(BossAnims.BREATH, "soul_drain");
            this.damage = damage;
            this.minPhase = minPhase;
        }

        @Override
        public int duration() {
            return 82;
        }

        @Override
        public int cooldown() {
            return 320;
        }

        @Override
        public int minPhase() {
            return this.minPhase;
        }

        @Override
        public int turnUntil() {
            return 14;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < 18 ? 4 : 0;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.victims.clear();
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 mouth = boss.mouthPosition();
            if (tick < 16) {
                BossFx.burst(level, ParticleTypes.SCULK_SOUL, mouth, 3, 0.8, 0.02);
                if (tick == 2) {
                    BossFx.sound(level, mouth, SoundEvents.SCULK_SHRIEKER_SHRIEK, 3.0f, 0.5f);
                }
                return;
            }
            if (tick == 16) {
                List<LivingEntity> near = boss.enemiesAround(boss.position(), 18, 8);
                near.sort((a, b) -> Double.compare(a.distanceToSqr(boss), b.distanceToSqr(boss)));
                for (LivingEntity e : near) {
                    if (!immune(e) && this.victims.size() < 3) {
                        this.victims.add(e);
                    }
                }
                BossFx.sound(level, mouth, ModSounds.NYXAR_BREATH.get(), 4.0f, 0.6f);
            }
            if (tick > 74) {
                return;
            }
            this.victims.removeIf(e -> !e.isAlive() || e.distanceTo(boss) > BREAK_DISTANCE);
            for (LivingEntity e : this.victims) {
                Vec3 chest = e.position().add(0, e.getBbHeight() * 0.6, 0);
                soulLine(level, chest, mouth, tick * 0.15);
                if (tick % 12 == 4) {
                    float dealt = this.damage * 0.25f;
                    if (e.hurtServer(level, boss.damageSources().indirectMagic(boss, boss), dealt * boss.damageMultiplier())) {
                        e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 30, 1), boss);
                        boss.heal(Math.min(dealt * 2.0f, boss.getMaxHealth() * 0.01f));
                        level.sendParticles(ParticleTypes.SOUL, chest.x, chest.y, chest.z, 6, 0.3, 0.3, 0.3, 0.04);
                        BossFx.sound(level, chest, SoundEvents.SOUL_ESCAPE.value(), 1.2f, 0.8f);
                    }
                }
            }
            if (tick % 20 == 0 && !this.victims.isEmpty()) {
                BossFx.sound(level, mouth, SoundEvents.WARDEN_HEARTBEAT, 3.0f, 0.7f);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.victims.clear();
        }
    }

    // =====================================================================================
    // Schattenregen
    // =====================================================================================

    /**
     * Schattenregen (Phase 3): Nyxar steigt hoch über die Arena, und aus dem violetten Himmel stürzen Seelenpfeile –
     * zuerst gezielt auf die Gegner, dann überall. Dunkle Kreise am Boden zeigen, wo sie einschlagen.
     */
    public static final class ShadowRain extends BossAttack {
        private final float damage;
        private final int minPhase;

        public ShadowRain(float damage, int minPhase) {
            super(BossAnims.RAIN, "shadow_rain");
            this.damage = damage;
            this.minPhase = minPhase;
        }

        @Override
        public int duration() {
            return 120;
        }

        @Override
        public int cooldown() {
            return 320;
        }

        @Override
        public int minPhase() {
            return this.minPhase;
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
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == 8) {
                BossFx.sound(level, boss.position(), ModSounds.NYXAR_SHRIEK.get(), 7.0f, 0.5f);
                BossFx.sound(level, boss.position(), SoundEvents.WARDEN_ROAR, 5.0f, 0.6f);
                boss.shake(level, 70, 0.35f, 25);
            }
            if (tick < 20 || tick > 106 || tick % 3 != 0) {
                return;
            }
            Vec3 c = center(boss);
            List<LivingEntity> enemies = boss.enemiesAround(c, DragonBossEntity.ARENA_RADIUS, 40);
            Vec3 spot;
            if (!enemies.isEmpty() && boss.getRandom().nextFloat() < (tick < 60 ? 0.8f : 0.5f)) {
                LivingEntity victim = enemies.get(boss.getRandom().nextInt(enemies.size()));
                Vec3 lead = victim.getDeltaMovement().multiply(14, 0, 14);
                spot = victim.position().add(lead).add((boss.getRandom().nextDouble() - 0.5) * 5, 0, (boss.getRandom().nextDouble() - 0.5) * 5);
            } else {
                double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                double r = 4 + boss.getRandom().nextDouble() * 26;
                spot = c.add(Math.cos(a) * r, 0, Math.sin(a) * r);
            }
            Vec3 impact = new Vec3(spot.x, boss.groundY(spot.x, spot.z), spot.z);
            BossFx.warnDisc(level, ParticleTypes.SCULK_SOUL, impact.add(0, 0.1, 0), 2.2, 18);
            BossFx.ring(level, ModParticles.BOSS_SHADOW.get(), impact, 2.6, 14, 0.2);
            Vec3 from = impact.add(2 * (boss.getRandom().nextDouble() - 0.5), 28, 2 * (boss.getRandom().nextDouble() - 0.5));
            BossProjectile.shoot(level, boss, BossProjectile.Kind.SOUL_BOLT, from, impact.subtract(from).normalize(), 1.05, this.damage);
        }
    }
}
