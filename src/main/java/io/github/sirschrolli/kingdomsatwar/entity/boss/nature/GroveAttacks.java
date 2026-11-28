package io.github.sirschrolli.kingdomsatwar.entity.boss.nature;

import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossEffects;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Verdantis' eigene große Angriffe: Dornenwellen, über die man springen muss, und der Sonnenstrahl. */
public final class GroveAttacks {
    private GroveAttacks() {}

    /** Dunkles Dornengrün. */
    private static final DustParticleOptions THORN = new DustParticleOptions(0x2F5A1C, 1.6f);
    /** Junges Grün an den Spitzen. */
    private static final DustParticleOptions SPROUT = new DustParticleOptions(0x7FCB3A, 1.1f);
    /** Sonnengold. */
    private static final DustParticleOptions SUN = new DustParticleOptions(0xFFE36A, 1.8f);
    /** Weißer Kern des Sonnenstrahls. */
    private static final DustParticleOptions SUN_CORE = new DustParticleOptions(0xFFFFF4, 2.2f);
    /** Warnlinie des Sonnenstrahls (blasses Gold). */
    private static final DustParticleOptions SUN_HINT = new DustParticleOptions(0xFFF2B0, 1.0f);

    // =====================================================================================
    // Dornenwelle
    // =====================================================================================

    /**
     * Dornenwelle: Verdantis stampft – aus dem Boden schießt ein Ring aus Dornenranken, der sich rasch nach außen
     * ausbreitet, gefolgt von einem zweiten (in der letzten Phase einem dritten). Die Ranken sind kniehoch: Wer im rechten
     * Moment springt, kommt darüber; wer stehen bleibt, wird zerkratzt, vergiftet und gebremst.
     */
    public static final class ThornWave extends BossAttack {
        private static final int FIRST = 14;
        private static final int GAP = 18;
        private static final double SPEED = 0.55;
        private static final double MAX = 26.0;
        private final float damage;
        private final List<Set<UUID>> hit = new ArrayList<>();

        public ThornWave(float damage) {
            super(BossAnims.STOMP, "thorn_wave");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return FIRST + 2 * GAP + (int) (MAX / SPEED) + 4;
        }

        @Override
        public int cooldown() {
            return 280;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance < MAX ? 6 : 1;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.hit.clear();
            int rings = boss.getPhase() >= 3 ? 3 : 2;
            for (int i = 0; i < rings; i++) {
                this.hit.add(new HashSet<>());
            }
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 c = BossFx.ground(boss, boss.getX(), boss.getZ());
            for (int ring = 0; ring < this.hit.size(); ring++) {
                int born = FIRST + ring * GAP;
                if (tick == born) {
                    BossFx.sound(level, c, SoundEvents.ROOTED_DIRT_BREAK, 4.0f, 0.5f);
                    BossFx.sound(level, c, SoundEvents.SWEET_BERRY_BUSH_BREAK, 4.0f, 0.6f);
                    boss.shake(level, 40, 0.35f, 12);
                    BossFx.groundDust(level, c, 5.0, 50);
                }
                if (tick < born) {
                    continue;
                }
                double r = 3.5 + (tick - born) * SPEED;
                if (r > MAX) {
                    continue;
                }
                // die Ranken: ein dichter Ring, unten dunkel, oben frisches Grün, dazwischen Blätter und Erde
                int count = (int) (r * 4.5);
                for (int i = 0; i < count; i++) {
                    double a = (i + boss.getRandom().nextDouble() * 0.5) * Mth.TWO_PI / count;
                    double x = c.x + Math.cos(a) * r;
                    double z = c.z + Math.sin(a) * r;
                    double y = boss.groundY(x, z);
                    level.sendParticles(THORN, x, y + 0.2, z, 1, 0.08, 0.1, 0.08, 0.0);
                    level.sendParticles(THORN, x, y + 0.55, z, 1, 0.06, 0.1, 0.06, 0.0);
                    level.sendParticles(THORN, x, y + 0.9, z, 1, 0.04, 0.08, 0.04, 0.0);
                    if (i % 2 == 0) {
                        level.sendParticles(SPROUT, x, y + 1.1, z, 1, 0.05, 0.05, 0.05, 0.0);
                    }
                    if (i % 5 == 0) {
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState()), x, y + 0.6, z,
                                1, 0.1, 0.2, 0.1, 0.0);
                    }
                }
                if ((tick - born) % 6 == 0) {
                    BossFx.sound(level, c, SoundEvents.AZALEA_LEAVES_BREAK, 3.0f, 0.6f);
                }
                // wer am Boden steht, wo die Welle gerade ist
                for (LivingEntity e : boss.enemiesAround(c, r + 1.0, 8.0)) {
                    double d = e.position().subtract(c).horizontalDistance();
                    double above = e.getY() - boss.groundY(e.getX(), e.getZ());
                    if (Math.abs(d - r) > 0.8 || above > 1.0 || !this.hit.get(ring).add(e.getUUID())) {
                        continue;
                    }
                    boss.hurtTarget(level, e, this.damage);
                    BossEffects.poison(e, 3);
                    e.addEffect(BossEffects.debuff(MobEffects.SLOWNESS, 60, 1), boss);
                    BossFx.burst(level, THORN, e.position().add(0, 0.6, 0), 10, 0.3, 0.0);
                }
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.hit.clear();
        }
    }

    // =====================================================================================
    // Sonnenstrahl
    // =====================================================================================

    /**
     * Sonnenstrahl: Verdantis hebt den Kopf und trinkt das Licht – goldene Funken strömen in ihr Maul, auf dem Boden zeigt
     * eine blasse Linie, wohin sie zielt. Dann bricht ein gleißender Strahl hervor, gerade wie eine Lanze: Wer in ihm
     * steht, verbrennt, und jeder Treffer gibt Verdantis etwas Kraft zurück.
     */
    public static final class SolarBeam extends BossAttack {
        private static final int CHARGE = 30;
        private static final int LOCK = 22;
        private static final int BEAM = 22;
        private static final double RANGE = 30.0;
        private final float damage;
        private final Set<UUID> hit = new HashSet<>();
        private Vec3 aim = Vec3.ZERO;

        public SolarBeam(float damage) {
            super(BossAnims.BREATH, "solar_beam");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return CHARGE + BEAM + 16;
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
        public int turnUntil() {
            return LOCK;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 8.0 && distance < RANGE ? 6 : 1;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.hit.clear();
            this.aim = target.position();
        }

        /** Der Endpunkt des Strahls: vom Maul über das (beim Zielen gemerkte) Ziel hinaus, bis zum Boden. */
        private Vec3 end(DragonBossEntity boss, Vec3 mouth) {
            Vec3 d = this.aim.add(0.0, 0.8, 0.0).subtract(mouth);
            if (d.lengthSqr() < 1.0e-4) {
                d = boss.forward();
            }
            Vec3 n = d.normalize();
            Vec3 p = mouth;
            for (double t = 0.0; t < RANGE; t += 0.5) {
                p = mouth.add(n.scale(t));
                if (p.y < boss.groundY(p.x, p.z)) {
                    break;
                }
            }
            return p;
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            Vec3 mouth = boss.mouthPosition();
            if (tick < LOCK) {
                this.aim = target.position();
            }
            if (tick < CHARGE) {
                // Licht sammelt sich
                for (int i = 0; i < 5; i++) {
                    double a = boss.getRandom().nextDouble() * Mth.TWO_PI;
                    Vec3 from = mouth.add(Math.cos(a) * 4.0, 2.5 + boss.getRandom().nextDouble() * 2.0, Math.sin(a) * 4.0);
                    Vec3 v = mouth.subtract(from);
                    level.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 0, v.x, v.y, v.z, 0.12);
                }
                level.sendParticles(SUN, mouth.x, mouth.y, mouth.z, 3, 0.25, 0.25, 0.25, 0.0);
                if (tick % 8 == 0) {
                    BossFx.sound(level, mouth, SoundEvents.BEACON_POWER_SELECT, 3.0f, 0.6f + tick * 0.02f);
                }
                // die Warnlinie am Boden
                if (tick >= 10 && tick % 2 == 0) {
                    Vec3 end = this.end(boss, mouth);
                    Vec3 from = BossFx.ground(boss, mouth.x, mouth.z);
                    BossFx.line(level, SUN_HINT, from.add(0, 0.15, 0), new Vec3(end.x, boss.groundY(end.x, end.z) + 0.15, end.z), 0.8, 0.05);
                }
                return;
            }
            int t = tick - CHARGE;
            if (t >= BEAM) {
                return;
            }
            Vec3 end = this.end(boss, mouth);
            if (t == 0) {
                BossFx.sound(level, mouth, SoundEvents.BEACON_ACTIVATE, 5.0f, 0.8f);
                BossFx.sound(level, mouth, ModSounds.DRAGON_ROAR.get(), 4.0f, 1.1f);
            }
            float width = t < 4 ? (t + 1) / 4.0f : t > BEAM - 5 ? (BEAM - t) / 5.0f : 1.0f;
            // ein dicker, gleißender Strahl: heller Kern, goldener Mantel, Funken drumherum
            BossFx.line(level, SUN_CORE, mouth, end, 0.25, 0.05);
            BossFx.line(level, SUN, mouth, end, 0.3, 0.3 * width);
            BossFx.line(level, SUN, mouth, end, 0.45, 0.55 * width);
            BossFx.line(level, ParticleTypes.END_ROD, mouth, end, 0.7, 0.4 * width);
            if (t % 2 == 0) {
                BossFx.ring(level, SUN, new Vec3(end.x, boss.groundY(end.x, end.z), end.z), 1.6 * width + 0.4, 14, 0.15);
            }
            level.sendParticles(ParticleTypes.END_ROD, end.x, end.y + 0.3, end.z, 4, 0.3, 0.2, 0.3, 0.05);
            level.sendParticles(ModParticles.BOSS_SPORE.get(), end.x, end.y + 0.3, end.z, 6, 0.6, 0.3, 0.6, 0.05);
            if (t % 4 == 0) {
                BossFx.sound(level, end, SoundEvents.FIRE_AMBIENT, 3.0f, 1.4f);
            }
            for (LivingEntity e : boss.enemiesAround(boss.position(), RANGE + 4.0, 30.0)) {
                Vec3 body = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0);
                if (BossFx.distanceToSegment(body, mouth, end) > 1.6 || !this.hit.add(e.getUUID())) {
                    continue;
                }
                boss.hurtTarget(level, e, this.damage);
                BossEffects.burn(e, 4);
                e.addEffect(BossEffects.debuff(MobEffects.BLINDNESS, 40, 0), boss);
                boss.heal(boss.getMaxHealth() * 0.01f);
                BossFx.burst(level, SUN, body, 12, 0.4, 0.0);
            }
        }

        @Override
        public void stop(ServerLevel level, DragonBossEntity boss) {
            this.hit.clear();
        }
    }

    // =====================================================================================
    // Rankenpeitschen (seit 1.13)
    // =====================================================================================

    /**
     * Rankenpeitschen: Verdantis stampft auf, und in einem weiten Bogen vor ihr brechen riesige Dornenranken aus dem Boden –
     * eine nach der anderen, von links nach rechts (ab der zweiten Phase hin und wieder zurück). Jede peitscht zur nächsten
     * hinüber: Wer im Bogen steht, wird getroffen, vergiftet und zur Seite geschleudert; wer weiter innen oder außen steht,
     * bleibt verschont.
     */
    public static final class VineLash extends BossAttack {
        static final int FIRST = 12;
        static final int STEP = 3;
        static final int VINES = 7;
        static final int LASH = 11;
        static final int LIFE = 30;
        static final double REACH = 2.2;
        static final double LENGTH = 3.6;
        private final float damage;
        private final List<Vine> vines = new ArrayList<>();
        private double radius;
        private double aim;

        /** Eine Ranke: wo, wohin sie peitscht, wann; wen sie traf. */
        private record Vine(Vec3 pos, Vec3 lash, int born, Set<UUID> hit) {}

        public VineLash(float damage) {
            super(BossAnims.STOMP, "vine_lash");
            this.damage = damage;
        }

        @Override
        public int duration() {
            return FIRST + 2 * VINES * STEP + LASH + 6;
        }

        @Override
        public int cooldown() {
            return 280;
        }

        @Override
        public int turnUntil() {
            return FIRST;
        }

        @Override
        public float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle) {
            return distance > 4.0 && distance < 18.0 ? 7 : 1;
        }

        @Override
        public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
            this.vines.clear();
            this.radius = Mth.clamp(target.position().subtract(boss.position()).horizontalDistance(), 6.0, 14.0);
            this.aim = Math.atan2(target.getZ() - boss.getZ(), target.getX() - boss.getX());
        }

        @Override
        public void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick) {
            if (tick == FIRST - 2) {
                BossFx.sound(level, boss.position(), SoundEvents.ROOTED_DIRT_BREAK, 4.0f, 0.5f);
                BossFx.groundDust(level, boss.position(), 5.0, 50);
            }
            int rounds = boss.getPhase() >= 2 ? 2 : 1;
            int k = tick - FIRST;
            if (k >= 0 && k % STEP == 0 && k / STEP < VINES * rounds) {
                int n = k / STEP;
                // erst von links nach rechts, dann zurück
                int i = n < VINES ? n : 2 * VINES - 1 - n;
                double spread = 120.0 * Mth.DEG_TO_RAD;
                double a = this.aim - spread / 2.0 + spread * i / (VINES - 1.0);
                double dirSign = n < VINES ? 1.0 : -1.0;
                Vec3 pos = BossFx.ground(boss, boss.getX() + Math.cos(a) * this.radius, boss.getZ() + Math.sin(a) * this.radius);
                // peitscht den Bogen entlang zur nächsten Ranke
                Vec3 lash = new Vec3(-Math.sin(a) * dirSign, 0.0, Math.cos(a) * dirSign);
                float yaw = (float) (Mth.atan2(lash.z, lash.x) * Mth.RAD_TO_DEG) - 90.0f;
                SpellFxEntity.create(level, SpellFxEntity.Kind.VINE, pos, DragonType.NATURE, LIFE, 2.1f)
                        .param(LASH / (float) LIFE).facing(yaw).spawn();
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), pos.x, pos.y + 0.2,
                        pos.z, 20, 0.6, 0.2, 0.6, 0.1);
                level.sendParticles(THORN, pos.x, pos.y + 0.4, pos.z, 8, 0.5, 0.5, 0.5, 0.0);
                BossFx.sound(level, pos, SoundEvents.ROOTED_DIRT_BREAK, 2.5f, 0.7f);
                this.vines.add(new Vine(pos, lash, tick, new HashSet<>()));
            }
            for (Vine vine : this.vines) {
                if (tick - vine.born() != LASH) {
                    continue;
                }
                BossFx.sound(level, vine.pos(), SoundEvents.SWEET_BERRY_BUSH_PLACE, 3.0f, 0.5f);
                BossFx.sound(level, vine.pos(), SoundEvents.PLAYER_ATTACK_SWEEP, 2.5f, 0.6f);
                Vec3 end = vine.pos().add(vine.lash().scale(LENGTH));
                BossFx.line(level, SPROUT, vine.pos().add(0.0, 1.0, 0.0), end.add(0.0, 1.0, 0.0), 0.5, 0.2);
                for (LivingEntity e : boss.enemiesAround(vine.pos(), LENGTH + REACH + 1.0, 5.0)) {
                    Vec3 at = new Vec3(e.getX(), vine.pos().y, e.getZ());
                    if (vine.hit().contains(e.getUUID()) || BossFx.distanceToSegment(at, vine.pos(), end) > REACH + e.getBbWidth() * 0.5
                            || e.getY() - vine.pos().y > 4.0) {
                        continue;
                    }
                    vine.hit().add(e.getUUID());
                    boss.hurtTarget(level, e, this.damage);
                    BossEffects.poison(e, 4);
                    e.push(vine.lash().x * 0.9, 0.45, vine.lash().z * 0.9);
                    e.needsSync = true;
                }
            }
        }
    }
}
