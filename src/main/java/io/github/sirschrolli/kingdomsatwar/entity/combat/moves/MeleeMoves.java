package io.github.sirschrolli.kingdomsatwar.entity.combat.moves;

import io.github.sirschrolli.kingdomsatwar.entity.combat.Family;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Fight;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Move;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildCaveSpider;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Die Nahkampf-Angriffe der Monster. */
public final class MeleeMoves {
    private MeleeMoves() {}

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Staub vom Boden unter einem Punkt (oder Rauch, wenn dort nichts ist). */
    static void groundDust(Fight f, Vec3 at, int count, double spread, double speed) {
        ServerLevel level = f.level();
        BlockState below = level.getBlockState(BlockPos.containing(at.x, at.y - 0.2, at.z));
        if (below.isAir()) {
            level.sendParticles(ParticleTypes.POOF, at.x, at.y + 0.1, at.z, Math.max(1, count / 3), spread, 0.05, spread, speed);
            return;
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below), at.x, at.y + 0.1, at.z, count, spread, 0.05, spread, speed);
    }

    /** Ein Ring aus Teilchen am Boden. */
    static void ring(Fight f, Vec3 center, double radius, int count, ParticleOptions particle) {
        for (int i = 0; i < count; i++) {
            double a = Mth.TWO_PI * i / count;
            f.level().sendParticles(particle, center.x + Math.cos(a) * radius, center.y + 0.08, center.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Bricht einen erhobenen Schild (für eine Weile unbrauchbar). */
    static void breakShield(Fight f, LivingEntity victim, float seconds) {
        if (!victim.isBlocking()) {
            return;
        }
        ItemStack stack = victim.getItemBlockingWith();
        if (stack == null) {
            return;
        }
        BlocksAttacks blocks = stack.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks != null) {
            blocks.disable(f.level(), victim, seconds, stack);
        }
    }

    /**
     * Sprunggeschwindigkeit, um in etwa {@code ticks} Ticks {@code h} Blöcke weit zu kommen: Den ersten Tick bremst noch
     * der Boden (Reibung des Blocks darunter), danach nur die Luft (0,91).
     */
    static double leap(Mob mob, double h, int ticks) {
        BlockPos below = mob.getBlockPosBelowThatAffectsMyMovement();
        double ground = mob.onGround() ? mob.level().getBlockState(below).getFriction(mob.level(), below, mob) * 0.91 : 0.91;
        double air = (1.0 - Math.pow(0.91, ticks - 1)) / (1.0 - 0.91);
        return h / (1.0 + ground * air);
    }

    /** Abstand in der Waagerechten. */
    static double flat(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    static boolean sees(Fight f, LivingEntity target) {
        return f.mob.getSensing().hasLineOfSight(target);
    }

    // =====================================================================================

    /** Ansprung: duckt sich, die Füße scharren – dann fliegt es auf das Ziel zu und reißt es um. */
    public static final class Lunge extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            LivingEntity t = f.target;
            return dist >= 3.0 && dist <= 8.5 && f.mob.onGround() && Math.abs(t.getY() - f.mob.getY()) < 2.5 && sees(f, t);
        }

        @Override
        public int windup(Fight f) {
            return f.family == Family.SPINNE || f.family == Family.RISSJAEGER || f.family == Family.KRISTALLKRABBLER ? 10 : 13;
        }

        @Override
        public float weight(Fight f, double dist) {
            return dist > 4.0 && dist < 7.0 ? 1.6f : 1.0f;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < this.windup(f) - 4;
        }

        @Override
        public void begin(Fight f) {
            f.mob.playAmbientSound();
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 0) {
                groundDust(f, f.mob.position(), 6, f.mob.getBbWidth() * 0.5, 0.05);
            }
            if (t == this.windup(f) - 4) {
                f.particles(f.dust(1.0f), f.mob.position().add(0.0, f.mob.getBbHeight() * 0.5, 0.0), 8, 0.3, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (t == 0) {
                Vec3 to = f.target.position();
                double h = Math.max(0.5, flat(mob.position(), to) - 0.8);
                double speed = Math.min(1.25, leap(mob, h, 12));
                Vec3 dir = f.forward();
                mob.setDeltaMovement(dir.x * speed, 0.46, dir.z * speed);
                mob.needsSync = true;
                f.sound(SoundEvents.GOAT_LONG_JUMP, 0.8f, 1.2f + f.random().nextFloat() * 0.2f);
                groundDust(f, mob.position(), 14, 0.4, 0.15);
                return true;
            }
            if (f.hit.isEmpty()) {
                for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(0.45))) {
                    if (f.strike(victim, 1.15f, 0.9, 3.0f)) {
                        f.hit.add(victim.getId());
                        f.sound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.8f);
                        f.particles(ParticleTypes.CRIT, victim.position().add(0.0, victim.getBbHeight() * 0.6, 0.0), 10, 0.3, 0.2);
                        break;
                    }
                }
            }
            return !(mob.onGround() && t > 3) && t < 26;
        }

        @Override
        public int recover(Fight f) {
            return 12;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 150 + random.nextInt(100);
        }
    }

    /**
     * Hammerschlag: hebt die Arme (Hufe, Hauer) hoch über sich, ein Ring am Boden zeigt, wo es einschlägt – dann kracht es
     * nieder. Wer im Ring steht, wird getroffen und hochgeschleudert; ein erhobener Schild bricht.
     */
    public static final class Slam extends Move {
        public static final double RADIUS = 2.6;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 3.4 && f.mob.onGround();
        }

        @Override
        public int windup(Fight f) {
            return 16;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < 11;
        }

        @Override
        public void begin(Fight f) {
            f.mob.playAmbientSound();
            f.sound(SoundEvents.RAVAGER_ATTACK, 0.5f, 1.4f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t >= 11) {
                Vec3 at = impact(f);
                ring(f, at, RADIUS, 22, f.dust(1.0f));
                if (t == 11) {
                    f.a = at;
                }
            } else if (t % 2 == 0) {
                f.particles(f.dust(0.8f), f.mob.position().add(0.0, f.mob.getBbHeight() + 0.2, 0.0), 2, 0.25, 0.0);
            }
        }

        static Vec3 impact(Fight f) {
            Vec3 fwd = f.forward();
            Vec3 p = f.mob.position().add(fwd.scale(f.mob.getBbWidth() * 0.5 + 0.9));
            return new Vec3(p.x, f.mob.getY(), p.z);
        }

        @Override
        public boolean act(Fight f, int t) {
            Vec3 at = f.a.equals(Vec3.ZERO) ? impact(f) : f.a;
            ServerLevel level = f.level();
            for (LivingEntity victim : f.victims(new AABB(at, at).inflate(RADIUS, 1.5, RADIUS).move(0.0, 0.5, 0.0))) {
                if (flat(victim.position(), at) > RADIUS + victim.getBbWidth() * 0.5) {
                    continue;
                }
                breakShield(f, victim, 5.0f);
                if (f.strike(victim, 1.35f, 0.55, 4.0f)) {
                    f.knock(victim, 0.55, 0.32);
                }
            }
            groundDust(f, at, 46, RADIUS * 0.45, 0.25);
            level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.3, at.z, 1, 0.0, 0.0, 0.0, 0.0);
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.05, 0.0), null, f.family.color(), 9, 0.5f).param(0.3f).spawn();
            f.sound(SoundEvents.MACE_SMASH_GROUND_HEAVY, 1.0f, 0.75f + f.random().nextFloat() * 0.15f);
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 16;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 170 + random.nextInt(100);
        }
    }

    /** Wirbelhieb: dreht sich weit zurück – dann einmal ganz herum; trifft alle ringsum und wirft sie zurück. */
    public static final class Sweep extends Move {
        public static final double RADIUS = 3.0;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 3.0;
        }

        @Override
        public int windup(Fight f) {
            return 11;
        }

        @Override
        public float weight(Fight f, double dist) {
            // umzingelt: lieber wirbeln
            int around = f.victims(f.mob.getBoundingBox().inflate(RADIUS)).size();
            return around >= 2 ? 2.5f : 1.0f;
        }

        @Override
        public void begin(Fight f) {
            f.mob.playAmbientSound();
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 1) {
                ring(f, f.mob.position(), RADIUS, 14, f.dust(0.7f));
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (t == 0) {
                f.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0f, 0.7f);
            }
            if (t <= 5) {
                // die Klinge (oder Kralle) fährt einmal ringsum
                double a = Math.toRadians(f.yaw + 90.0) + Mth.TWO_PI * t / 6.0;
                Vec3 c = mob.position();
                f.level().sendParticles(ParticleTypes.SWEEP_ATTACK, c.x + Math.cos(a) * 1.8, c.y + mob.getBbHeight() * 0.55,
                        c.z + Math.sin(a) * 1.8, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (t == 2) {
                for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(RADIUS, 0.6, RADIUS))) {
                    if (flat(victim.position(), mob.position()) <= RADIUS + victim.getBbWidth() * 0.5) {
                        f.strike(victim, 0.95f, 0.9, 3.0f);
                    }
                }
            }
            return t < 7;
        }

        @Override
        public int recover(Fight f) {
            return 10;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 130 + random.nextInt(90);
        }
    }

    /**
     * Sturmangriff: scharrt, bebt und senkt den Kopf – dann rennt es geradeaus los (es lenkt kaum nach). Wen es erwischt,
     * den schleudert es weit; prallt es gegen eine Wand, steht es benommen da.
     */
    public static final class Charge extends Move {
        public static final int RUN = 26;

        @Override
        public boolean ready(Fight f, double dist) {
            LivingEntity t = f.target;
            return dist >= 4.5 && dist <= 15.0 && f.mob.onGround() && Math.abs(t.getY() - f.mob.getY()) < 2.0 && sees(f, t);
        }

        @Override
        public int windup(Fight f) {
            return 18;
        }

        @Override
        public float weight(Fight f, double dist) {
            return dist > 7.0 ? 1.8f : 1.0f;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < 14;
        }

        @Override
        public void begin(Fight f) {
            f.mob.playAmbientSound();
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 4 == 0) {
                Vec3 back = f.mob.position().subtract(f.forward().scale(f.mob.getBbWidth() * 0.6));
                groundDust(f, back, 10, 0.25, 0.12);
                f.sound(SoundEvents.RAVAGER_STEP, 0.7f, 1.3f);
            }
            if (t == 14) {
                f.sound(SoundEvents.HOGLIN_ANGRY, 0.7f, 1.2f);
                f.particles(f.dust(1.2f), f.mob.getEyePosition(), 10, 0.25, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (f.target != null && f.target.isAlive()) {
                // lenkt nur ein klein wenig nach
                f.yaw = Mth.approachDegrees(f.yaw, Fight.yawTo(mob.position(), f.target.position()), 2.5f);
            }
            Vec3 dir = f.forward();
            double speed = 0.62;
            Vec3 v = mob.getDeltaMovement();
            mob.setDeltaMovement(dir.x * speed, v.y, dir.z * speed);
            mob.setYRot(f.yaw);
            mob.setYBodyRot(f.yaw);
            mob.setYHeadRot(f.yaw);
            if (t % 2 == 0) {
                groundDust(f, mob.position(), 6, 0.3, 0.08);
            }
            if (t % 5 == 0) {
                f.sound(SoundEvents.RAVAGER_STEP, 0.8f, 1.1f);
            }
            for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(0.55))) {
                if (f.strike(victim, 1.4f, 1.5, 4.0f)) {
                    f.knock(victim, 1.5, 0.35);
                    f.sound(SoundEvents.GOAT_RAM_IMPACT, 1.0f, 0.8f);
                    f.particles(ParticleTypes.CRIT, victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0), 14, 0.3, 0.3);
                    return false;
                }
            }
            if (t > 2 && mob.horizontalCollision) {
                // gegen die Wand: benommen
                f.n = 1;
                f.sound(SoundEvents.RAVAGER_STUNNED, 0.9f, 1.3f);
                f.sound(SoundEvents.ANVIL_LAND, 0.3f, 0.6f);
                mob.setDeltaMovement(-dir.x * 0.3, 0.2, -dir.z * 0.3);
                return false;
            }
            return t < RUN;
        }

        @Override
        public int recover(Fight f) {
            return f.n == 1 ? 46 : 10;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 190 + random.nextInt(110);
        }
    }

    /**
     * Umklammern: greift zu – wen es packt, der kommt kaum vom Fleck und kann nichts abbauen, und es drückt zu. Wer es
     * kräftig trifft, reißt sich los.
     */
    public static final class Grab extends Move {
        public static final int HOLD = 50;
        /** So viel Schaden (Anteil seines Lebens) muss es nehmen, damit es loslässt. */
        public static final float BREAK_FREE = 0.12f;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 2.1 && f.mob.onGround() && !f.target.hasEffect(MobEffects.MINING_FATIGUE);
        }

        @Override
        public int windup(Fight f) {
            return 9;
        }

        @Override
        public void begin(Fight f) {
            f.mob.playAmbientSound();
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 0) {
                f.particles(f.dust(0.8f), f.mob.position().add(f.forward().scale(0.8)).add(0.0, f.mob.getBbHeight() * 0.6, 0.0), 3, 0.2, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity victim = f.target;
            if (t == 0) {
                if (victim == null || mob.distanceTo(victim) > 2.7) {
                    f.sound(SoundEvents.PLAYER_ATTACK_NODAMAGE, 0.8f, 0.8f);
                    return false;
                }
                f.held = victim;
                f.sound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.6f);
            }
            boolean broke = f.taken >= mob.getMaxHealth() * BREAK_FREE;
            if (!(f.held instanceof LivingEntity held) || !held.isAlive() || mob.distanceTo(held) > 3.4 || broke) {
                if (broke) {
                    f.sound(SoundEvents.PLAYER_ATTACK_STRONG, 0.8f, 1.2f);
                }
                return false;
            }
            if (t % 10 == 0) {
                held.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 25, 3, false, true), mob);
                held.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 25, 1, false, true), mob);
            }
            if (t > 0 && t % 20 == 0) {
                held.hurtServer(f.level(), mob.damageSources().mobAttack(mob), 1.5f);
            }
            // hält fest: zieht heran, wer sich entfernt
            double dist = mob.distanceTo(held);
            if (dist > 1.4) {
                Vec3 pull = mob.position().subtract(held.position()).multiply(1.0, 0.0, 1.0).normalize().scale(0.12);
                held.setDeltaMovement(held.getDeltaMovement().multiply(0.4, 1.0, 0.4).add(pull));
                held.needsSync = true;
            }
            if (t % 4 == 0) {
                f.level().sendParticles(f.dust(0.9f), held.getX(), held.getY() + held.getBbHeight() * 0.5, held.getZ(), 3,
                        held.getBbWidth() * 0.4, held.getBbHeight() * 0.3, held.getBbWidth() * 0.4, 0.0);
            }
            return t < HOLD;
        }

        @Override
        public void end(Fight f) {
            if (f.held instanceof LivingEntity held && held.isAlive()) {
                held.removeEffect(MobEffects.MINING_FATIGUE);
            }
        }

        @Override
        public int recover(Fight f) {
            return 14;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 220 + random.nextInt(110);
        }
    }

    /** Giftbiss: ein kurzes Zusammenziehen – dann schnellt es vor und beißt; das Gift wirkt nach. */
    public static final class Bite extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 2.4;
        }

        @Override
        public int windup(Fight f) {
            return 7;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.SPIDER_AMBIENT, 0.9f, 1.5f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 2 == 0) {
                f.particles(new DustParticleOptions(0x58C030, 0.8f), f.mob.getEyePosition(), 2, 0.15, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity victim = f.target;
            Vec3 dir = f.forward();
            mob.setDeltaMovement(dir.x * 0.35, mob.getDeltaMovement().y, dir.z * 0.35);
            if (victim != null && mob.distanceTo(victim) <= 2.8 && f.strike(victim, 1.0f, 0.2, 3.0f)) {
                if (f.family == Family.KRISTALLKRABBLER) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1), mob);
                } else {
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 110, mob instanceof WildCaveSpider ? 1 : 0), mob);
                }
                f.level().sendParticles(new DustParticleOptions(0x58C030, 1.0f), victim.getX(), victim.getY() + victim.getBbHeight() * 0.5,
                        victim.getZ(), 8, 0.25, 0.25, 0.25, 0.0);
                f.sound(SoundEvents.SPIDER_HURT, 0.6f, 1.6f);
            }
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 8;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 120 + random.nextInt(80);
        }
    }

    /**
     * Umkreisen (die Lauernden): hält Abstand und schleicht seitwärts um das Ziel herum, bis es eine Lücke sieht – dann
     * stößt es aus der Flanke zu.
     */
    public static final class Circle extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 2.5 && dist <= 7.5 && f.mob.onGround();
        }

        @Override
        public int windup(Fight f) {
            return 2;
        }

        @Override
        public void begin(Fight f) {
            f.n = 28 + f.random().nextInt(26);
            f.d = f.random().nextBoolean() ? 1.0 : -1.0;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null || !target.isAlive()) {
                return false;
            }
            double dist = mob.distanceTo(target);
            mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
            f.yaw = Fight.yawTo(mob.position(), target.position());
            mob.setYRot(f.yaw);
            mob.setYBodyRot(f.yaw);
            if (t < f.n) {
                float forward = (float) Mth.clamp((dist - 3.6) * 0.45, -0.6, 0.6);
                mob.getMoveControl().strafe(forward, (float) (0.75 * f.d));
                if (mob.horizontalCollision) {
                    f.d = -f.d;
                }
                return true;
            }
            if (t == f.n) {
                // Ausfall aus der Flanke
                Vec3 dir = target.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0).normalize();
                mob.setDeltaMovement(dir.x * 0.72, 0.22, dir.z * 0.72);
                mob.needsSync = true;
                f.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.4f, 1.6f);
            }
            if (dist <= 2.0 && f.hit.isEmpty() && f.strike(target, 1.05f, 0.4, 3.0f)) {
                f.hit.add(target.getId());
                return false;
            }
            return t < f.n + 12;
        }

        @Override
        public void end(Fight f) {
            f.mob.getMoveControl().strafe(0.0f, 0.0f);
            f.mob.setZza(0.0f);
            f.mob.setXxa(0.0f);
        }

        @Override
        public int recover(Fight f) {
            return 6;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 150 + random.nextInt(110);
        }
    }

    /**
     * Deckung (die Zähen): Arme vor dem Gesicht, festen Stand – von vorn kommt kaum etwas durch, und wer in die Deckung
     * schlägt, bekommt sofort einen Gegenschlag. Von der Seite oder von hinten bricht sie.
     */
    public static final class Guard extends Move {
        public static final int HOLD = 44;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 4.5;
        }

        @Override
        public float weight(Fight f, double dist) {
            return f.mob.hurtTime > 0 ? 3.0f : 0.8f;
        }

        @Override
        public int windup(Fight f) {
            return 5;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.ARMOR_EQUIP_IRON, 0.8f, 0.8f);
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (f.target != null) {
                mob.getLookControl().setLookAt(f.target, 30.0f, 30.0f);
                f.yaw = Mth.approachDegrees(f.yaw, Fight.yawTo(mob.position(), f.target.position()), 12.0f);
                mob.setYRot(f.yaw);
                mob.setYBodyRot(f.yaw);
                mob.setYHeadRot(f.yaw);
            }
            if (t % 6 == 0) {
                Vec3 front = mob.position().add(f.forward().scale(mob.getBbWidth() * 0.6)).add(0.0, mob.getBbHeight() * 0.65, 0.0);
                f.level().sendParticles(new DustParticleOptions(0xC8D0E0, 0.7f), front.x, front.y, front.z, 3, 0.2, 0.25, 0.2, 0.0);
            }
            if (f.n == -1) {
                // von hinten durchbrochen: taumelt
                f.sound(SoundEvents.ITEM_BREAK, 0.6f, 1.3f);
                return false;
            }
            if (f.parried) {
                LivingEntity target = f.target;
                if (target != null && mob.distanceTo(target) <= 3.4) {
                    f.sound(SoundEvents.PLAYER_ATTACK_STRONG, 1.0f, 0.8f);
                    f.strike(target, 1.2f, 1.0, 3.5f);
                }
                return false;
            }
            return t < HOLD;
        }

        @Override
        public int recover(Fight f) {
            return f.n == -1 ? 24 : 6;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 130 + random.nextInt(90);
        }
    }

    /** Bogenstoß (Gerippe): ein harter Stoß mit dem Bogen, der Luft schafft – dann ein Satz zurück. */
    public static final class Bash extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 2.4;
        }

        @Override
        public float weight(Fight f, double dist) {
            return 2.0f;
        }

        @Override
        public int windup(Fight f) {
            return 6;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target != null && mob.distanceTo(target) <= 2.9) {
                f.strike(target, 0.8f, 1.25, 2.0f);
                f.knock(target, 1.2, 0.15);
                f.sound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.2f);
            }
            Vec3 back = f.forward().scale(-0.5);
            mob.setDeltaMovement(back.x, 0.25, back.z);
            mob.needsSync = true;
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 6;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 110 + random.nextInt(70);
        }
    }

    /** Schmetterlandung (Schleime): zieht sich zusammen, springt hoch hinauf und landet mit aller Wucht. */
    public static final class Burst extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 1.5 && dist <= 9.0 && f.mob.onGround() && Family.big(f.mob);
        }

        @Override
        public int windup(Fight f) {
            return 14;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < 11;
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 0) {
                f.particles(f.mob instanceof MagmaCube ? ParticleTypes.LAVA : ParticleTypes.ITEM_SLIME, f.mob.position().add(0.0, 0.2, 0.0), 4,
                        f.mob.getBbWidth() * 0.4, 0.05);
            }
        }

        static double radius(Mob mob) {
            int size = mob instanceof AbstractCubeMob cube ? cube.getSize() : 2;
            return 1.4 + size * 0.55;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (t == 0) {
                double h = Math.max(0.0, flat(mob.position(), f.target.position()) - 0.5);
                double speed = Math.min(1.0, leap(mob, h, 22));
                Vec3 dir = f.forward();
                mob.setDeltaMovement(dir.x * speed, 0.92, dir.z * speed);
                mob.needsSync = true;
                f.sound(mob instanceof MagmaCube ? SoundEvents.MAGMA_CUBE_JUMP : SoundEvents.SLIME_JUMP, 1.2f, 0.6f);
                return true;
            }
            if (t > 3 && mob.onGround()) {
                double r = radius(mob);
                Vec3 at = mob.position();
                for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(r, 1.0, r))) {
                    if (flat(victim.position(), at) <= r + victim.getBbWidth() * 0.5 && f.strike(victim, 1.25f, 0.8, 4.0f)) {
                        f.knock(victim, 0.8, 0.3);
                    }
                }
                boolean magma = mob instanceof MagmaCube;
                f.level().sendParticles(magma ? ParticleTypes.LAVA : ParticleTypes.ITEM_SLIME, at.x, at.y + 0.2, at.z, 40, r * 0.5, 0.1, r * 0.5, 0.2);
                groundDust(f, at, 30, r * 0.4, 0.2);
                SpellFxEntity.create(f.level(), SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.05, 0.0), null, magma ? 0xFF6A00 : 0x7ACC5A, 8,
                        0.4f).param((float) (r / 8.0)).spawn();
                f.sound(magma ? SoundEvents.MAGMA_CUBE_SQUISH : SoundEvents.SLIME_SQUISH, 1.6f, 0.5f);
                f.sound(SoundEvents.MACE_SMASH_GROUND, 0.8f, 0.9f);
                return false;
            }
            return t < 40;
        }

        @Override
        public int recover(Fight f) {
            return 14;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 160 + random.nextInt(100);
        }
    }

    /** Sturzflug: legt die Flügel an und stößt geradewegs auf das Ziel herab. */
    public static final class Dive extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 4.0 && dist <= 16.0 && sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return 12;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null || !target.isAlive()) {
                return false;
            }
            Vec3 dir = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(mob.position()).normalize();
            mob.setDeltaMovement(dir.scale(0.8));
            mob.needsSync = true;
            for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(0.4))) {
                if (f.strike(victim, 1.2f, 0.6, 3.0f)) {
                    return false;
                }
            }
            return t < 24;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 160 + random.nextInt(100);
        }
    }
}
