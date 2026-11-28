package io.github.sirschrolli.kingdomsatwar.entity.combat.moves;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Family;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Fight;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Move;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Temper;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Moderkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.RottenBristling;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.rules.Hardness;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Die besonderen Angriffe einzelner Familien: Kampfschrei, die Kniffe der Kriecher, Flüche, Fangzähne, Feuer, Rauch. */
public final class SpecialMoves {
    private SpecialMoves() {}

    /** Die Oberkante des Bodens unter (oder bei) einem Punkt, höchstens {@code depth} Blöcke tiefer (sonst null). */
    static @Nullable Vec3 ground(ServerLevel level, Vec3 at, int depth) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(at.x, at.y + 1.0, at.z).mutable();
        for (int i = 0; i < depth + 2; i++) {
            BlockPos below = pos.below();
            BlockState under = level.getBlockState(below);
            VoxelShape shape = under.getCollisionShape(level, below);
            if (!shape.isEmpty() && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return new Vec3(at.x, below.getY() + shape.max(Direction.Axis.Y), at.z);
            }
            pos.move(0, -1, 0);
        }
        return null;
    }

    static Vec3 groundOr(ServerLevel level, Vec3 at) {
        Vec3 g = ground(level, at, 6);
        return g != null ? g : at;
    }

    // =====================================================================================

    /**
     * Kampfschrei: reckt den Kopf und brüllt – Artgenossen ringsum werden schneller und stärker und stürzen sich auf
     * dasselbe Ziel. Anführer schreien gleich zu Beginn eines Kampfes.
     */
    public static final class Roar extends Move {
        public static final double RANGE = 16.0;
        public static final int BUFF_TICKS = 160;

        static List<Mob> allies(Fight f) {
            return f.level().getEntitiesOfClass(Mob.class, f.mob.getBoundingBox().inflate(RANGE, 6.0, RANGE),
                    m -> m != f.mob && m.isAlive() && Family.of(m) == f.family && Hardness.isEnemy(m));
        }

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 20.0 && (f.style().temper() == Temper.ANFUEHRER || !allies(f).isEmpty());
        }

        @Override
        public float weight(Fight f, double dist) {
            return f.style().temper() == Temper.ANFUEHRER ? 4.0f : 0.8f;
        }

        @Override
        public int windup(Fight f) {
            return 14;
        }

        @Override
        public void begin(Fight f) {
            roar(f);
        }

        static void roar(Fight f) {
            Mob mob = f.mob;
            SoundEvent sound;
            float pitch = 0.8f;
            switch (f.family) {
                case UNTOTE -> {
                    sound = mob instanceof Mummy ? SoundEvents.HUSK_AMBIENT : mob instanceof DrownedCorpse ? SoundEvents.DROWNED_AMBIENT
                            : mob instanceof RottenBristling ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIE_AMBIENT;
                    pitch = 0.5f;
                }
                case DIENER -> sound = SoundEvents.VINDICATOR_CELEBRATE;
                case VERWUESTER -> {
                    sound = SoundEvents.RAVAGER_ROAR;
                    pitch = 1.0f;
                }
                case BORSTLING -> sound = SoundEvents.PIGLIN_ANGRY;
                case HUENE -> sound = SoundEvents.PIGLIN_BRUTE_ANGRY;
                case KEILER -> sound = mob instanceof Moderkeiler ? SoundEvents.ZOGLIN_ANGRY : SoundEvents.HOGLIN_ANGRY;
                case BANDIT -> sound = SoundEvents.PILLAGER_CELEBRATE;
                default -> {
                    mob.playAmbientSound();
                    sound = SoundEvents.RAVAGER_ROAR;
                    pitch = 1.5f;
                }
            }
            f.sound(sound, 2.2f, pitch);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 0) {
                f.particles(f.dust(1.1f), f.mob.getEyePosition(), 4, 0.3, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            ServerLevel level = f.level();
            SpellFxEntity.create(level, SpellFxEntity.Kind.ECHO, mob.position().add(0.0, 0.05, 0.0), null, f.family.color(), 18, 0.6f)
                    .param(0.55f).spawn();
            level.sendParticles(f.dust(1.4f), mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 24, 0.6, 0.4, 0.6, 0.0);
            mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 0), mob);
            for (Mob ally : allies(f)) {
                ally.addEffect(new MobEffectInstance(MobEffects.SPEED, BUFF_TICKS, 0), mob);
                ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, BUFF_TICKS, 0), mob);
                if (ally.getTarget() == null && f.target != null && f.target.isAlive()) {
                    ally.setTarget(f.target);
                }
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, ally.getX(), ally.getY() + ally.getBbHeight() + 0.3, ally.getZ(), 2, 0.2, 0.1,
                        0.2, 0.0);
                level.sendParticles(f.dust(1.0f), ally.getX(), ally.getY() + ally.getBbHeight() * 0.5, ally.getZ(), 8, 0.3, 0.4, 0.3, 0.0);
            }
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 8;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 480 + random.nextInt(240);
        }
    }

    // ---- Kriecher

    static boolean calm(Mob mob) {
        return mob instanceof Creeper creeper && !creeper.isIgnited() && creeper.getSwelling(1.0f) < 0.05f && creeper.getSwellDir() <= 0;
    }

    static void swell(Mob mob, int dir) {
        if (mob instanceof Creeper creeper) {
            creeper.setSwellDir(dir);
        }
    }

    /** Finte: zischt und bläht sich auf wie vor dem Knall – und weicht dann doch zurück. Beim nächsten Mal vielleicht nicht. */
    public static final class Feint extends Move {
        public static final int SWELL = 14;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 2.0 && dist <= 6.5 && f.mob.onGround() && calm(f.mob);
        }

        @Override
        public int windup(Fight f) {
            return 1;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (t == 0) {
                swell(mob, 1);
                f.sound(SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
            }
            if (t == SWELL) {
                swell(mob, -1);
                f.sound(SoundEvents.FIRE_EXTINGUISH, 0.5f, 1.8f);
                f.particles(ParticleTypes.POOF, mob.position().add(0.0, mob.getBbHeight() * 0.6, 0.0), 8, 0.3, 0.02);
            }
            if (t > SWELL && f.target != null && mob.onGround()) {
                Vec3 away = mob.position().subtract(f.target.position()).multiply(1.0, 0.0, 1.0);
                if (away.lengthSqr() > 1.0e-4) {
                    away = away.normalize().scale(0.22);
                    mob.setDeltaMovement(away.x, mob.getDeltaMovement().y, away.z);
                }
            }
            return t < SWELL + 12;
        }

        @Override
        public void end(Fight f) {
            swell(f.mob, -1);
        }

        @Override
        public int recover(Fight f) {
            return 4;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 220 + random.nextInt(100);
        }
    }

    /** Sprungzünder: duckt sich – und springt das Ziel mit brennender Lunte an. */
    public static final class FuseLeap extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            LivingEntity t = f.target;
            return dist >= 3.5 && dist <= 7.5 && f.mob.onGround() && calm(f.mob) && Math.abs(t.getY() - f.mob.getY()) < 2.0 && MeleeMoves.sees(f, t);
        }

        @Override
        public int windup(Fight f) {
            return 11;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < 8;
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 0) {
                MeleeMoves.groundDust(f, f.mob.position(), 6, 0.3, 0.05);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (t == 0) {
                double h = Math.max(0.5, MeleeMoves.flat(mob.position(), f.target.position()) - 1.0);
                double speed = Math.min(1.0, MeleeMoves.airSpeed(h, 12));
                Vec3 dir = f.forward();
                mob.setDeltaMovement(dir.x * speed, 0.48, dir.z * speed);
                mob.needsSync = true;
                swell(mob, 1);
                f.sound(SoundEvents.CREEPER_PRIMED, 1.0f, 1.0f);
                MeleeMoves.groundDust(f, mob.position(), 12, 0.4, 0.12);
                return true;
            }
            return !(mob.onGround() && t > 3) && t < 24;
        }

        @Override
        public void end(Fight f) {
            if (f.target == null || !f.target.isAlive() || f.mob.distanceTo(f.target) > 3.2) {
                swell(f.mob, -1);
            }
        }

        @Override
        public int recover(Fight f) {
            return 1;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 220 + random.nextInt(100);
        }
    }

    /** Sporenstoß: bläht sich ein wenig auf – dann pufft ihm eine Wolke Sporen entgegen (übel, langsam). */
    public static final class Spores extends Move {
        public static final double RANGE = 5.5;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 5.0 && calm(f.mob) && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return 12;
        }

        @Override
        public void begin(Fight f) {
            swell(f.mob, 1);
            f.sound(SoundEvents.PUFFER_FISH_BLOW_UP, 1.0f, 0.7f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t == 8) {
                swell(f.mob, 0);
            }
            if (t % 2 == 0) {
                f.particles(new DustParticleOptions(0x8FD04F, 0.9f), f.mob.position().add(0.0, f.mob.getBbHeight() + 0.1, 0.0), 3, 0.25, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            swell(mob, -1);
            ServerLevel level = f.level();
            Vec3 origin = mob.position().add(0.0, mob.getBbHeight() * 0.6, 0.0);
            Vec3 dir = f.forward();
            for (double s = 0.6; s <= RANGE; s += 0.5) {
                double spread = 0.25 + s * 0.3;
                Vec3 p = origin.add(dir.scale(s));
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, p.x, p.y, p.z, 4, spread, spread * 0.6, spread, 0.0);
                level.sendParticles(new DustParticleOptions(0x8FD04F, 1.2f), p.x, p.y, p.z, 3, spread, spread * 0.6, spread, 0.0);
            }
            level.sendParticles(ParticleTypes.COMPOSTER, origin.x, origin.y, origin.z, 14, 0.4, 0.3, 0.4, 0.05);
            f.sound(SoundEvents.PUFFER_FISH_BLOW_OUT, 1.2f, 0.7f);
            for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(RANGE, 2.0, RANGE))) {
                Vec3 to = victim.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0);
                double len = to.length();
                if (len > RANGE + 0.5 || len > 0.5 && to.normalize().dot(dir) < Math.cos(Math.toRadians(50.0))) {
                    continue;
                }
                victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 140, 0), mob);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), mob);
                f.strike(victim, 1.0f, 0.0, 2.0f);
            }
            return false;
        }

        @Override
        public void end(Fight f) {
            swell(f.mob, -1);
        }

        @Override
        public int recover(Fight f) {
            return 8;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 160 + random.nextInt(90);
        }
    }

    /** Rankenzug: eine Ranke tastet sich zum Ziel – dann reißt sie es heran, und die Lunte brennt. */
    public static final class Vine extends Move {
        public static final int WINDUP = 16;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 4.0 && dist <= 9.5 && calm(f.mob) && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return WINDUP;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < WINDUP - 3;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.CAVE_VINES_PLACE, 1.2f, 0.6f);
        }

        @Override
        public void warn(Fight f, int t) {
            LivingEntity target = f.target;
            if (target == null) {
                return;
            }
            Vec3 from = f.mob.position().add(0.0, f.mob.getBbHeight() * 0.5, 0.0);
            Vec3 to = target.position().add(0.0, target.getBbHeight() * 0.4, 0.0);
            double len = from.distanceTo(to) * Math.min(1.0, (t + 1.0) / (WINDUP - 3.0));
            Vec3 dir = to.subtract(from).normalize();
            for (double s = 0.5; s < len; s += 0.45) {
                Vec3 p = from.add(dir.scale(s)).add(0.0, Math.sin(s * 2.2 + t * 0.6) * 0.12, 0.0);
                f.level().sendParticles(new DustParticleOptions(0x3F8F2F, 0.8f), p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (t % 4 == 0) {
                f.sound(SoundEvents.VINE_PLACE, 0.6f, 0.8f);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null || mob.distanceTo(target) > 11.0 || !MeleeMoves.sees(f, target)) {
                return false;
            }
            Vec3 pull = mob.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            double dist = pull.length();
            pull = pull.normalize();
            double speed = Math.min(1.9, 0.17 * dist + 0.3) * Math.max(0.0, 1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) * 0.5);
            target.setDeltaMovement(pull.x * speed, 0.34, pull.z * speed);
            target.needsSync = true;
            Vec3 from = mob.position().add(0.0, mob.getBbHeight() * 0.5, 0.0);
            Vec3 to = target.position().add(0.0, target.getBbHeight() * 0.4, 0.0);
            Vec3 dir = to.subtract(from).normalize();
            for (double s = 0.4; s < from.distanceTo(to); s += 0.25) {
                Vec3 p = from.add(dir.scale(s));
                f.level().sendParticles(ParticleTypes.COMPOSTER, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
            }
            f.sound(SoundEvents.FISHING_BOBBER_RETRIEVE, 1.2f, 0.6f);
            swell(mob, 1);
            f.sound(SoundEvents.CREEPER_PRIMED, 1.0f, 1.0f);
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 2;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 280 + random.nextInt(100);
        }
    }

    // ---- Hexe, Leerwandler, Beschwörer

    /** Fluchkreis (Hexe): ein violetter Kreis unter dem Ziel – wer nicht hinaustritt, wird langsam und schwach. */
    public static final class Curse extends Move {
        public static final double RADIUS = 2.4;
        public static final int DELAY = 24;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 3.0 && dist <= 14.0 && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return 4;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.WITCH_AMBIENT, 1.0f, 0.7f);
        }

        @Override
        public boolean act(Fight f, int t) {
            ServerLevel level = f.level();
            if (t == 0) {
                if (f.target == null) {
                    return false;
                }
                f.a = groundOr(level, f.target.position());
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, f.a.add(0.0, 0.02, 0.0), null, 0x9B4FC8, DELAY + 2, (float) RADIUS).spawn();
                f.sound(SoundEvents.EVOKER_PREPARE_WOLOLO, 0.8f, 1.4f);
            }
            if (t % 2 == 0) {
                level.sendParticles(ParticleTypes.WITCH, f.a.x, f.a.y + 0.2, f.a.z, 3, RADIUS * 0.5, 0.05, RADIUS * 0.5, 0.0);
            }
            if (t == DELAY) {
                for (LivingEntity victim : f.victims(new AABB(f.a, f.a).inflate(RADIUS, 2.0, RADIUS))) {
                    if (MeleeMoves.flat(victim.position(), f.a) > RADIUS + 0.3) {
                        continue;
                    }
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), f.mob);
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), f.mob);
                    victim.hurtServer(level, f.mob.damageSources().indirectMagic(f.mob, f.mob), 3.0f);
                }
                level.sendParticles(ParticleTypes.WITCH, f.a.x, f.a.y + 0.4, f.a.z, 40, RADIUS * 0.5, 0.4, RADIUS * 0.5, 0.1);
                level.sendParticles(new DustParticleOptions(0x9B4FC8, 1.5f), f.a.x, f.a.y + 0.3, f.a.z, 30, RADIUS * 0.5, 0.2, RADIUS * 0.5, 0.0);
                f.sound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.2f);
                return false;
            }
            return true;
        }

        @Override
        public int recover(Fight f) {
            return 0;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 170 + random.nextInt(80);
        }
    }

    /** Hinterhalt (Leerwandler): Funken hinter dem Ziel verraten, wo – dann steht er dort und schlägt zu. */
    public static final class Ambush extends Move {
        public static final int WINDUP = 16;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 3.0 && dist <= 16.0 && !f.mob.isInWater();
        }

        @Override
        public int windup(Fight f) {
            return WINDUP;
        }

        static Vec3 behind(LivingEntity target) {
            Vec3 back = Vec3.directionFromRotation(0.0f, target.getYRot()).scale(-2.2);
            return target.position().add(back);
        }

        @Override
        public void begin(Fight f) {
            f.a = behind(f.target);
            f.sound(SoundEvents.ENDERMAN_STARE, 0.5f, 1.4f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t < WINDUP - 6 && f.target != null) {
                f.a = behind(f.target);
            }
            ServerLevel level = f.level();
            int count = 1 + t / 3;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, f.a.x, f.a.y + 1.2, f.a.z, count, 0.25, 0.7, 0.25, 0.02);
            level.sendParticles(ParticleTypes.PORTAL, f.mob.getX(), f.mob.getY() + 1.4, f.mob.getZ(), 2, 0.3, 0.8, 0.3, 0.3);
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            if (t == 0) {
                Vec3 old = mob.position();
                if (!RangedMoves.teleport(mob, f.a)) {
                    return false;
                }
                ServerLevel level = f.level();
                level.sendParticles(ParticleTypes.PORTAL, old.x, old.y + 1.4, old.z, 30, 0.3, 0.9, 0.3, 0.3);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + 1.4, mob.getZ(), 30, 0.3, 0.9, 0.3, 0.1);
                f.sound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 0.8f);
                f.yaw = Fight.yawTo(mob.position(), target.position());
                mob.setYRot(f.yaw);
                mob.setYBodyRot(f.yaw);
                mob.setYHeadRot(f.yaw);
            }
            if (t == 4 && mob.distanceTo(target) <= 3.4) {
                f.strike(target, 1.25f, 0.6, 4.0f);
            }
            return t < 5;
        }

        @Override
        public int recover(Fight f) {
            return 8;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 200 + random.nextInt(100);
        }
    }

    /** Fangkreis (Beschwörer): ein violetter Ring unter dem Ziel – dann brechen Fangzähne im ganzen Kreis hervor. */
    public static final class Fangs extends Move {
        public static final int WINDUP = 18;
        public static final double RADIUS = 2.45;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 3.0 && dist <= 14.0 && MeleeMoves.sees(f, f.target)
                    && !(f.mob instanceof Conjurer conjurer && conjurer.isCastingSpell());
        }

        @Override
        public int windup(Fight f) {
            return WINDUP;
        }

        @Override
        public void begin(Fight f) {
            if (f.mob instanceof Conjurer conjurer) {
                conjurer.showFangCasting(true);
            }
            f.sound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.0f, 0.9f);
            f.a = groundOr(f.level(), f.target.position());
        }

        @Override
        public void warn(Fight f, int t) {
            if (t < 10 && f.target != null) {
                f.a = groundOr(f.level(), f.target.position());
            }
            if (t >= 6 && t % 2 == 0) {
                MeleeMoves.ring(f, f.a, RADIUS, 20, new DustParticleOptions(0x8A5AE0, 1.0f));
                MeleeMoves.ring(f, f.a, RADIUS * 0.5, 10, new DustParticleOptions(0x8A5AE0, 0.8f));
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            ServerLevel level = f.level();
            Mob mob = f.mob;
            Vec3 c = f.a;
            spawnFang(level, mob, c, 0.0f, 0);
            for (int i = 0; i < 6; i++) {
                double a = Mth.TWO_PI * i / 6.0;
                spawnFang(level, mob, c.add(Math.cos(a) * RADIUS * 0.5, 0.0, Math.sin(a) * RADIUS * 0.5), (float) a, 2);
            }
            for (int i = 0; i < 10; i++) {
                double a = Mth.TWO_PI * (i + 0.5) / 10.0;
                spawnFang(level, mob, c.add(Math.cos(a) * RADIUS, 0.0, Math.sin(a) * RADIUS), (float) a, 4);
            }
            f.sound(SoundEvents.EVOKER_CAST_SPELL, 1.0f, 1.0f);
            return false;
        }

        static void spawnFang(ServerLevel level, Mob owner, Vec3 at, float rot, int delay) {
            Vec3 g = groundOr(level, at);
            level.addFreshEntity(new EvokerFangs(level, g.x, g.y, g.z, rot, delay, owner));
        }

        @Override
        public void end(Fight f) {
            if (f.mob instanceof Conjurer conjurer) {
                conjurer.showFangCasting(false);
            }
        }

        @Override
        public int recover(Fight f) {
            return 0;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 200 + random.nextInt(100);
        }
    }

    // ---- Nether und Nacht

    /** Feuerring (Glutgeist): die Glutklingen lodern auf – dann breitet sich ein Feuerring am Boden aus; darüber springen! */
    public static final class FireRing extends Move {
        public static final int LIFE = 24;
        public static final float START = 0.8f;
        public static final float GROWTH = 0.34f;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 7.5 && ground(f.level(), f.mob.position(), 7) != null;
        }

        @Override
        public int windup(Fight f) {
            return 14;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.BLAZE_BURN, 2.0f, 0.6f);
        }

        @Override
        public void warn(Fight f, int t) {
            Mob mob = f.mob;
            for (int i = 0; i < 2; i++) {
                double a = t * 0.9 + i * Math.PI;
                f.level().sendParticles(ParticleTypes.FLAME, mob.getX() + Math.cos(a) * 1.1, mob.getY() + 0.4 + t * 0.03, mob.getZ() + Math.sin(a) * 1.1,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            ServerLevel level = f.level();
            if (t == 0) {
                Vec3 g = ground(level, f.mob.position(), 7);
                if (g == null) {
                    return false;
                }
                f.a = g;
                SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, 0.05, 0.0), DragonType.FIRE, 0xFF7A1A, LIFE, START).param(GROWTH)
                        .spawn();
                f.sound(SoundEvents.FIRECHARGE_USE, 1.4f, 0.6f);
            }
            double r = START + GROWTH * t;
            for (LivingEntity victim : f.victims(new AABB(f.a, f.a).inflate(r + 1.0, 2.0, r + 1.0))) {
                double d = MeleeMoves.flat(victim.position(), f.a);
                if (Math.abs(d - r) > 0.6 + victim.getBbWidth() * 0.5 || victim.getY() - f.a.y > SpellFxEntity.SHOCKWAVE_HEIGHT
                        || !f.hit.add(victim.getId())) {
                    continue;
                }
                victim.hurtServer(level, f.mob.damageSources().mobAttack(f.mob), 4.0f);
                victim.igniteForSeconds(3.0f);
            }
            return t < LIFE;
        }

        @Override
        public int recover(Fight f) {
            return 0;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 180 + random.nextInt(80);
        }
    }

    /** Flammensäule (Glutgeist): ein Glutkreis unter dem Ziel – wer stehen bleibt, steht gleich in einer Feuersäule. */
    public static final class Pillar extends Move {
        public static final int DELAY = 24;
        public static final double RADIUS = 1.5;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 4.0 && dist <= 18.0 && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return 3;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.BLAZE_AMBIENT, 1.0f, 0.6f);
        }

        @Override
        public boolean act(Fight f, int t) {
            ServerLevel level = f.level();
            if (t == 0) {
                if (f.target == null) {
                    return false;
                }
                f.a = groundOr(level, f.target.position());
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, f.a.add(0.0, 0.02, 0.0), DragonType.FIRE, 0xFF7A1A, DELAY + 2, (float) RADIUS)
                        .spawn();
            }
            if (t % 3 == 0 && t < DELAY) {
                MeleeMoves.ring(f, f.a, RADIUS, 10, ParticleTypes.FLAME);
            }
            if (t == DELAY) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, f.a, DragonType.FIRE, 0xFF7A1A, 22, (float) RADIUS * 0.85f).param(5.0f).spawn();
                for (LivingEntity victim : f.victims(new AABB(f.a, f.a).inflate(RADIUS, 0.0, RADIUS).expandTowards(0.0, 5.0, 0.0).move(0.0, -1.0, 0.0))) {
                    if (MeleeMoves.flat(victim.position(), f.a) <= RADIUS + victim.getBbWidth() * 0.5) {
                        victim.hurtServer(level, f.mob.damageSources().mobAttack(f.mob), 5.0f);
                        victim.igniteForSeconds(4.0f);
                        victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 0.45, 0.0));
                        victim.needsSync = true;
                    }
                }
                f.sound(SoundEvents.FIRECHARGE_USE, 1.6f, 0.7f);
                f.sound(SoundEvents.BLAZE_SHOOT, 1.2f, 0.6f);
                return false;
            }
            return true;
        }

        @Override
        public int recover(Fight f) {
            return 0;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 160 + random.nextInt(80);
        }
    }

    /** Schreckensschrei (Nachtmahr): ein gellender Schrei – schwach, langsam, und die Nacht wird noch dunkler. */
    public static final class Screech extends Move {
        public static final double RANGE = 9.0;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 14.0;
        }

        @Override
        public int windup(Fight f) {
            return 10;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.PHANTOM_AMBIENT, 2.5f, 0.5f);
        }

        @Override
        public void warn(Fight f, int t) {
            f.particles(new DustParticleOptions(0x6A5ACD, 1.0f), f.mob.position(), 3, 0.5, 0.0);
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            ServerLevel level = f.level();
            level.sendParticles(ParticleTypes.SONIC_BOOM, mob.getX(), mob.getY() + 0.3, mob.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            for (LivingEntity victim : f.victims(mob.getBoundingBox().inflate(RANGE))) {
                if (victim.distanceTo(mob) > RANGE) {
                    continue;
                }
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), mob);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), mob);
                victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0), mob);
                SpellFxEntity.create(level, SpellFxEntity.Kind.ECHO, victim.position().add(0.0, 0.05, 0.0), null, 0x6A5ACD, 14, 0.4f).param(0.25f)
                        .spawn();
            }
            f.sound(SoundEvents.PHANTOM_SWOOP, 2.0f, 0.5f);
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 0;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 300 + random.nextInt(120);
        }
    }

    /** Rauchschwall (Rauchqualle): ein Schwall aus schwarzem Qualm – wo er sich legt, sieht man nichts mehr. */
    public static final class Smoke extends Move {
        public static final float RADIUS = 3.2f;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 6.0 && dist <= 32.0 && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return 18;
        }

        @Override
        public void begin(Fight f) {
            if (f.mob instanceof Ghast ghast) {
                ghast.setCharging(true);
            }
            f.sound(SoundEvents.GHAST_WARN, 2.5f, 0.6f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 2 == 0) {
                f.particles(ParticleTypes.LARGE_SMOKE, RangedMoves.mouth(f.mob), 3, 0.5, 0.02);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            ServerLevel level = f.level();
            Mob mob = f.mob;
            if (t == 0) {
                if (f.target == null) {
                    return false;
                }
                f.a = groundOr(level, f.target.position());
                f.b = RangedMoves.mouth(mob);
                f.sound(SoundEvents.GHAST_SHOOT, 2.0f, 0.5f);
                if (mob instanceof Ghast ghast) {
                    ghast.setCharging(false);
                }
            }
            // der Schwall wälzt sich zum Ziel
            double s0 = t / 6.0;
            double s1 = (t + 1) / 6.0;
            Vec3 d = f.a.add(0.0, 1.0, 0.0).subtract(f.b);
            for (double s = s0; s < s1; s += 0.04) {
                Vec3 p = f.b.add(d.scale(s));
                level.sendParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y, p.z, 2, 0.3, 0.3, 0.3, 0.01);
            }
            if (t == 6) {
                AreaEffectCloud cloud = new AreaEffectCloud(level, f.a.x, f.a.y, f.a.z);
                cloud.setOwner(mob);
                cloud.setRadius(RADIUS);
                cloud.setDuration(90);
                cloud.setWaitTime(0);
                cloud.setRadiusPerTick(0.0f);
                cloud.setCustomParticle(ParticleTypes.LARGE_SMOKE);
                cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0));
                level.addFreshEntity(cloud);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, f.a.x, f.a.y + 0.5, f.a.z, 16, RADIUS * 0.5, 0.3, RADIUS * 0.5, 0.02);
                f.sound(SoundEvents.FIRE_EXTINGUISH, 1.2f, 0.5f);
                return false;
            }
            return true;
        }

        @Override
        public void end(Fight f) {
            if (f.mob instanceof Ghast ghast) {
                ghast.setCharging(false);
            }
        }

        @Override
        public int recover(Fight f) {
            return 0;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 220 + random.nextInt(100);
        }
    }
}
