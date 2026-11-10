package io.github.sirschrolli.kingdomsatwar.entity.creator;

import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModDamageTypes;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Geschosse Schrollis des Schöpfers (und des Zepters des Schöpfers):
 * <ul>
 *     <li>{@link Kind#SPEAR} Lichtspeer – schwebt erst, auf sein Ziel gerichtet, und fliegt dann von selbst los;</li>
 *     <li>{@link Kind#FEATHER} goldene Feder, {@link Kind#BOLT} Lichtpfeil der Seraphlinge – fliegen gerade;</li>
 *     <li>{@link Kind#STAR} fallender Stern – zerspringt beim Aufschlag in Licht;</li>
 *     <li>{@link Kind#HALO} geworfener Heiligenschein – saust hinaus und kehrt zurück (oder kreist in einer Spirale), geht
 *     durch alles hindurch;</li>
 *     <li>{@link Kind#ORB} die Weltkugel – hält an ihrem Ziel an, wird zum Sog und zerbirst;</li>
 *     <li>{@link Kind#PLANET} eine Welt – kreist über ihm, bis er sie im Bogen schleudert (Glut, Eis, Erde oder Leere);</li>
 *     <li>{@link Kind#SUN} die Sonne der Supernova und {@link Kind#FIXED_STAR} die Sterne des Sternbilds – nur Licht.</li>
 * </ul>
 * Blöcke beschädigen sie nicht. Sie werden nicht gespeichert.
 */
public class DivineShotEntity extends AbstractHurtingProjectile {
    public enum Kind {
        SPEAR(1.0f), FEATHER(0.5f), BOLT(0.4f), STAR(0.9f), HALO(1.8f), ORB(1.0f), PLANET(2.6f), SUN(2.0f), FIXED_STAR(0.6f);

        public final float size;

        Kind(float size) {
            this.size = size;
        }
    }

    public static final int PLANET_EARTH = 0;
    public static final int PLANET_LAVA = 1;
    public static final int PLANET_ICE = 2;
    public static final int PLANET_VOID = 3;
    public static final int PLANET_SUN = 4;
    public static final int PLANET_GLOBE = 5;
    public static final String[] PLANET_NAMES = {"earth", "lava", "ice", "void", "sun", "globe"};

    public static final int HOLD = 0;
    public static final int FLY = 1;
    public static final int RETURN = 2;
    public static final int VORTEX = 3;
    public static final int ORBIT = 4;

    /** Weltkugel: so lange saugt sie, so weit reicht der Sog, so groß ist der Knall. */
    public static final int ORB_PULL_TICKS = 46;
    public static final double ORB_PULL_RADIUS = 13.0;
    public static final double ORB_BLAST_RADIUS = 5.5;

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(DivineShotEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(DivineShotEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(DivineShotEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(DivineShotEntity.class, EntityDataSerializers.FLOAT);

    private float damage = 8.0f;
    private double radius;
    private double speed = 1.5;
    private int launchAt = -1;
    private int life = 140;
    private @Nullable UUID aimTarget;
    private Vec3 goal = Vec3.ZERO;
    private Vec3 orbitCenter = Vec3.ZERO;
    private float orbitPhase;
    private int slot;
    private int stateTick;
    private final Set<UUID> hit = new HashSet<>();
    private final Map<UUID, Integer> hitCooldown = new HashMap<>();

    public DivineShotEntity(EntityType<? extends DivineShotEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    private static DivineShotEntity make(ServerLevel level, LivingEntity owner, Kind kind, Vec3 at, float damage) {
        DivineShotEntity shot = new DivineShotEntity(ModEntities.DIVINE_SHOT.get(), level);
        shot.entityData.set(DATA_KIND, (byte) kind.ordinal());
        shot.setOwner(owner);
        shot.setPos(at.x, at.y, at.z);
        shot.damage = damage;
        shot.setNoGravity(true);
        return shot;
    }

    /** Ein Lichtspeer, der bei {@code at} schwebt, auf sein Ziel gerichtet, und nach {@code delay} Ticks losfliegt. */
    public static DivineShotEntity spear(ServerLevel level, LivingEntity owner, Vec3 at, @Nullable LivingEntity target, int delay, float damage,
            double speed) {
        DivineShotEntity shot = make(level, owner, Kind.SPEAR, at, damage);
        shot.setState(HOLD);
        shot.launchAt = Math.max(1, delay);
        shot.speed = speed;
        shot.aimTarget = target != null ? target.getUUID() : null;
        shot.life = delay + 100;
        if (target != null) {
            shot.lookAlong(target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(at));
        }
        level.addFreshEntity(shot);
        return shot;
    }

    /**
     * Ein Speer des Klingenkranzes (seit 0.78): schwebt bei {@code at}, die Spitze auf {@code mark}, stößt nach
     * {@code delay} Ticks dorthin und vergeht gleich dahinter (so bleibt die Lücke im Kranz ein sicherer Weg hinaus).
     */
    public static DivineShotEntity ringSpear(ServerLevel level, LivingEntity owner, Vec3 at, Vec3 mark, int delay, float damage, double speed) {
        DivineShotEntity shot = make(level, owner, Kind.SPEAR, at, damage);
        shot.setState(HOLD);
        shot.launchAt = Math.max(1, delay);
        shot.speed = speed;
        shot.goal = mark;
        shot.life = shot.launchAt + (int) Math.ceil(at.distanceTo(mark) / speed) + 2;
        shot.lookAlong(mark.subtract(at));
        level.addFreshEntity(shot);
        return shot;
    }

    /** Ein Lichtspeer, der sofort fliegt (das Zepter des Schöpfers). */
    public static DivineShotEntity thrownSpear(ServerLevel level, LivingEntity owner, Vec3 at, Vec3 dir, double speed, float damage) {
        DivineShotEntity shot = make(level, owner, Kind.SPEAR, at, damage);
        shot.fly(dir, speed);
        shot.life = 80;
        level.addFreshEntity(shot);
        return shot;
    }

    public static DivineShotEntity feather(ServerLevel level, LivingEntity owner, Vec3 at, Vec3 dir, double speed, float damage) {
        DivineShotEntity shot = make(level, owner, Kind.FEATHER, at, damage);
        shot.fly(dir, speed);
        shot.life = 70;
        level.addFreshEntity(shot);
        return shot;
    }

    public static DivineShotEntity bolt(ServerLevel level, LivingEntity owner, Vec3 at, Vec3 dir, double speed, float damage) {
        DivineShotEntity shot = make(level, owner, Kind.BOLT, at, damage);
        shot.fly(dir, speed);
        shot.life = 60;
        level.addFreshEntity(shot);
        return shot;
    }

    /** Ein Stern, der aus {@code height} Blöcken Höhe genau auf {@code mark} fällt. */
    public static DivineShotEntity star(ServerLevel level, LivingEntity owner, Vec3 mark, double height, double speed, float damage, double radius) {
        double a = level.getRandom().nextDouble() * Math.PI * 2.0;
        double off = height * 0.15;
        Vec3 from = mark.add(Math.cos(a) * off, height, Math.sin(a) * off);
        DivineShotEntity shot = make(level, owner, Kind.STAR, from, damage);
        shot.radius = radius;
        shot.fly(mark.subtract(from), speed);
        shot.life = 100;
        level.addFreshEntity(shot);
        return shot;
    }

    /**
     * Eine kleine Sonne, die wie ein Stern auf {@code mark} stürzt (Sonnensturz des Allmächtigen, seit 0.78): Sie zerbirst
     * in Glut und lässt glühende Erde zurück.
     */
    public static DivineShotEntity fallingSun(ServerLevel level, LivingEntity owner, Vec3 mark, double height, double speed, float damage,
            double radius) {
        DivineShotEntity shot = star(level, owner, mark, height, speed, damage, radius);
        shot.entityData.set(DATA_VARIANT, (byte) PLANET_SUN);
        return shot;
    }

    /** Ist dies eine stürzende Sonne (und kein Stern)? */
    public boolean isFallingSun() {
        return this.kind() == Kind.STAR && this.variant() == PLANET_SUN;
    }

    /** Ein Heiligenschein, der nach {@code to} saust und zu seinem Werfer zurückkehrt. */
    public static DivineShotEntity halo(ServerLevel level, LivingEntity owner, Vec3 from, Vec3 to, double speed, float damage) {
        DivineShotEntity shot = make(level, owner, Kind.HALO, from, damage);
        shot.goal = to;
        shot.speed = speed;
        shot.setState(FLY);
        shot.noPhysics = true;
        shot.life = 140;
        level.addFreshEntity(shot);
        return shot;
    }

    /** Ein Heiligenschein, der in einer Spirale um {@code center} kreist (hinaus und wieder herein). */
    public static DivineShotEntity orbitHalo(ServerLevel level, LivingEntity owner, Vec3 center, float phase, float damage, int life) {
        DivineShotEntity shot = make(level, owner, Kind.HALO, center, damage);
        shot.orbitCenter = center;
        shot.orbitPhase = phase;
        shot.setState(ORBIT);
        shot.noPhysics = true;
        shot.life = life;
        level.addFreshEntity(shot);
        return shot;
    }

    /** Die Weltkugel fliegt nach {@code mark}, wird dort zum Sog und zerbirst. */
    public static DivineShotEntity orb(ServerLevel level, LivingEntity owner, Vec3 from, Vec3 mark, float damage) {
        DivineShotEntity shot = make(level, owner, Kind.ORB, from, damage);
        shot.entityData.set(DATA_VARIANT, (byte) PLANET_GLOBE);
        shot.goal = mark;
        shot.speed = 1.1;
        shot.setState(FLY);
        shot.noPhysics = true;
        shot.radius = ORB_BLAST_RADIUS;
        shot.life = 200;
        level.addFreshEntity(shot);
        return shot;
    }

    /** Eine Welt, die über ihrem Schöpfer kreist (Platz 0–2), bis er sie schleudert. */
    public static DivineShotEntity planet(ServerLevel level, SchrolliEntity owner, int variant, int slot) {
        DivineShotEntity shot = make(level, owner, Kind.PLANET, owner.position().add(0.0, 10.0, 0.0), 24.0f * owner.power());
        shot.entityData.set(DATA_VARIANT, (byte) variant);
        shot.entityData.set(DATA_SCALE, 0.15f);
        shot.slot = slot;
        shot.setState(HOLD);
        shot.noPhysics = true;
        shot.radius = 5.5;
        shot.life = 260;
        level.addFreshEntity(shot);
        return shot;
    }

    /** Eine Sonne (nur Licht; ihr Schöpfer bewegt und vergrößert sie). */
    public static DivineShotEntity sun(ServerLevel level, LivingEntity owner, Vec3 at, float scale) {
        DivineShotEntity shot = make(level, owner, Kind.SUN, at, 0.0f);
        shot.entityData.set(DATA_VARIANT, (byte) PLANET_SUN);
        shot.entityData.set(DATA_SCALE, scale);
        shot.setState(HOLD);
        shot.noPhysics = true;
        shot.life = 400;
        level.addFreshEntity(shot);
        return shot;
    }

    /** Ein Stern des Sternbilds (nur Licht). */
    public static DivineShotEntity fixedStar(ServerLevel level, LivingEntity owner, Vec3 at, int life) {
        DivineShotEntity shot = make(level, owner, Kind.FIXED_STAR, at, 0.0f);
        shot.entityData.set(DATA_VARIANT, (byte) PLANET_SUN);
        shot.entityData.set(DATA_SCALE, 0.3f);
        shot.setState(HOLD);
        shot.noPhysics = true;
        shot.life = life;
        level.addFreshEntity(shot);
        return shot;
    }

    private void fly(Vec3 dir, double speed) {
        this.speed = speed;
        this.setState(FLY);
        Vec3 v = dir.lengthSqr() < 1.0e-6 ? new Vec3(0.0, -1.0, 0.0) : dir.normalize().scale(speed);
        this.setDeltaMovement(v);
        this.lookAlong(v);
    }

    /** Richtet das Geschoss aus (wie Pfeile: Gier = atan2(x, z)). */
    private void lookAlong(Vec3 v) {
        double h = v.horizontalDistance();
        float yaw = (float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(v.y, h) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KIND, (byte) 0);
        builder.define(DATA_STATE, (byte) FLY);
        builder.define(DATA_VARIANT, (byte) 0);
        builder.define(DATA_SCALE, 1.0f);
    }

    public Kind kind() {
        return Kind.values()[Mth.clamp(this.entityData.get(DATA_KIND), 0, Kind.values().length - 1)];
    }

    public int state() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(int state) {
        this.entityData.set(DATA_STATE, (byte) state);
        this.stateTick = 0;
    }

    public int variant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public float visualScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public void setVisualScale(float scale) {
        if (Math.abs(scale - this.visualScale()) > 1.0e-3f) {
            this.entityData.set(DATA_SCALE, scale);
        }
    }

    /** Schleudert die Welt im Bogen auf {@code mark}. */
    public void hurl(Vec3 mark) {
        Vec3 to = mark.subtract(this.position());
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        int ticks = Math.max(14, (int) (flat / 1.1));
        double g = 0.04;
        this.setDeltaMovement(to.x / ticks, (to.y + 0.5 * g * ticks * ticks) / ticks, to.z / ticks);
        this.setState(FLY);
        this.goal = mark;
        if (this.level() instanceof ServerLevel level) {
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, mark.add(0.0, 0.06, 0.0), this.variant() == PLANET_LAVA ? DivineFx.EMBER
                    : this.variant() == PLANET_ICE ? DivineFx.FROST : this.variant() == PLANET_VOID ? DivineFx.COSMIC : DivineFx.GOLD, ticks,
                    (float) this.radius).spawn();
        }
    }

    // ------------------------------------------------------------------ Ablauf

    @Override
    public void tick() {
        Kind kind = this.kind();
        int state = this.state();
        this.stateTick++;
        if (!this.level().isClientSide()) {
            Entity owner = this.getOwner();
            if (this.tickCount > this.life || owner == null && kind != Kind.SPEAR || owner != null && owner.isRemoved() && kind != Kind.SPEAR) {
                this.discard();
                return;
            }
        }
        if (this.level().isClientSide()) {
            this.trail(kind, state);
        }
        switch (kind) {
            case SPEAR -> {
                if (state == HOLD) {
                    this.holdSpear();
                    return;
                }
            }
            case HALO -> {
                this.tickHalo(state);
                return;
            }
            case ORB -> {
                this.tickOrb(state);
                return;
            }
            case PLANET -> {
                this.tickPlanet(state);
                return;
            }
            case SUN, FIXED_STAR -> {
                this.setDeltaMovement(Vec3.ZERO);
                return;
            }
            default -> {
            }
        }
        super.tick();
    }

    /** Schwebt und zielt; zur rechten Zeit fliegt er los (auf das Ziel, mit Vorhalt). */
    private void holdSpear() {
        this.setDeltaMovement(Vec3.ZERO);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = this.aimTarget != null ? level.getEntity(this.aimTarget) : null;
        Vec3 aim = target != null ? target.position().add(0.0, target.getBbHeight() * 0.5, 0.0) : null;
        if (aim != null && this.tickCount < this.launchAt) {
            this.lookAlong(aim.subtract(this.position()));
        }
        if (this.tickCount >= this.launchAt) {
            Vec3 dir;
            if (aim != null) {
                double travel = aim.distanceTo(this.position()) / this.speed;
                Vec3 v = target.getDeltaMovement();
                dir = aim.add(v.x * travel * 0.7, 0.0, v.z * travel * 0.7).subtract(this.position());
            } else if (this.goal.distanceToSqr(this.position()) > 1.0e-4 && !this.goal.equals(Vec3.ZERO)) {
                dir = this.goal.subtract(this.position());
            } else {
                // die Richtung, in die er zeigt (Gier wie bei Pfeilen: atan2(x, z))
                float yaw = this.getYRot() * Mth.DEG_TO_RAD;
                float pitch = this.getXRot() * Mth.DEG_TO_RAD;
                dir = new Vec3(Mth.sin(yaw) * Mth.cos(pitch), Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch));
            }
            this.fly(dir, this.speed);
            DivineFx.sound(level, this.position(), ModSounds.SCHROLLI_SPEAR_FLY.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.3f);
        }
    }

    /** Der Heiligenschein: hinaus, zurück – oder in der Spirale; er geht durch alles und trifft jeden höchstens einmal je Weg. */
    private void tickHalo(int state) {
        this.setYRot(this.getYRot() + 31.0f);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Entity owner = this.getOwner();
        Vec3 next;
        if (state == ORBIT) {
            float k = Mth.clamp(this.tickCount / (float) this.life, 0.0f, 1.0f);
            double r = 3.0 + 18.0 * Math.sin(Math.PI * k);
            double a = Math.toRadians(this.orbitPhase + this.tickCount * 7.0);
            next = this.orbitCenter.add(Math.cos(a) * r, 0.0, Math.sin(a) * r);
            if (this.tickCount % 12 == 0) {
                DivineFx.sound(level, next, ModSounds.SCHROLLI_HALO.get(), 1.2f, 1.4f);
            }
        } else if (state == FLY) {
            Vec3 to = this.goal.subtract(this.position());
            if (to.length() < this.speed + 0.2 || this.stateTick > 44) {
                this.setState(RETURN);
                this.hit.clear();
                next = this.position();
            } else {
                next = this.position().add(to.normalize().scale(this.speed));
            }
        } else {
            Vec3 home = owner != null ? owner.position().add(0.0, owner.getBbHeight() * 0.85, 0.0) : this.position();
            Vec3 to = home.subtract(this.position());
            if (to.length() < 2.0 || this.stateTick > 80 || owner == null) {
                this.discard();
                return;
            }
            next = this.position().add(to.normalize().scale(Math.min(to.length(), this.speed * 1.15)));
        }
        this.setDeltaMovement(next.subtract(this.position()));
        this.setPos(next);
        AABB box = this.getBoundingBox().inflate(0.9, 0.5, 0.9);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, this::canHurt)) {
            boolean fresh = state == ORBIT ? !this.hitCooldown.containsKey(e.getUUID()) : this.hit.add(e.getUUID());
            if (fresh) {
                if (state == ORBIT) {
                    this.hitCooldown.put(e.getUUID(), 14);
                }
                if (e.hurtServer(level, this.divineSource(level), this.damage)) {
                    SchrolliEntity.knock(e, this.position(), 0.9, 0.35);
                    DivineFx.sparks(level, e.position().add(0.0, 1.0, 0.0), 10, 0.3, 0.1);
                }
            }
        }
        this.hitCooldown.replaceAll((id, t) -> t - 1);
        this.hitCooldown.values().removeIf(t -> t <= 0);
    }

    /** Die Weltkugel: fliegt zu ihrem Ziel, wird zum Sog (zieht Gegner und Geschosse an) und zerbirst in Licht. */
    private void tickOrb(int state) {
        this.setYRot(this.getYRot() + 6.0f);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (state == FLY) {
            Vec3 to = this.goal.subtract(this.position());
            boolean blocked = level.clip(new ClipContext(this.position(), this.position().add(to.normalize().scale(Math.min(to.length(), this.speed))),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.BLOCK;
            if (to.length() < this.speed + 0.1 || blocked || this.stateTick > 60) {
                this.setDeltaMovement(Vec3.ZERO);
                this.setState(VORTEX);
                DivineFx.fx(level, SpellFxEntity.Kind.VORTEX, this.position(), DivineFx.COSMIC, ORB_PULL_TICKS + 4, 4.4f).spawn();
                DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, this.groundBelow(level).add(0.0, 0.06, 0.0), DivineFx.AMBER, ORB_PULL_TICKS,
                        (float) this.radius).spawn();
                DivineFx.sound(level, this.position(), ModSounds.SCHROLLI_ORB.get(), 3.5f, 0.7f);
                return;
            }
            Vec3 v = to.normalize().scale(this.speed);
            this.setDeltaMovement(v);
            this.setPos(this.position().add(v));
            return;
        }
        // Sog
        this.setDeltaMovement(Vec3.ZERO);
        Vec3 c = this.position();
        float k = this.stateTick / (float) ORB_PULL_TICKS;
        double pull = 0.03 + 0.07 * k;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(ORB_PULL_RADIUS), this::canHurt)) {
            Vec3 to = c.subtract(e.position().add(0.0, e.getBbHeight() * 0.5, 0.0));
            double d = to.length();
            if (d > ORB_PULL_RADIUS || d < 0.5) {
                continue;
            }
            Vec3 dir = to.scale(1.0 / d);
            double f = pull * (0.4 + 0.6 * (1.0 - d / ORB_PULL_RADIUS));
            e.push(dir.x * f, dir.y * f * 0.5, dir.z * f);
            e.needsSync = true;
        }
        if (this.stateTick % 2 == 0) {
            DivineFx.gather(level, c, 7.0, 5);
        }
        if (this.stateTick >= ORB_PULL_TICKS) {
            this.burst(level, c, this.radius, DivineFx.WHITE);
            this.discard();
        }
    }

    /** Eine Welt: wächst über ihrem Schöpfer, kreist – geschleudert fliegt sie im Bogen und schlägt ein. */
    private void tickPlanet(int state) {
        this.setYRot(this.getYRot() + 3.0f);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (state == HOLD) {
            Entity owner = this.getOwner();
            if (owner == null) {
                this.discard();
                return;
            }
            this.setVisualScale(Math.min(1.0f, 0.15f + this.tickCount / 14.0f * 0.85f));
            double a = Math.toRadians(this.slot * 120.0 + this.tickCount * 3.0);
            double s = owner instanceof LivingEntity l ? l.getScale() : 1.0;
            Vec3 at = owner.position().add(Math.cos(a) * 4.4 * s, 6.6 * s + 1.5, Math.sin(a) * 4.4 * s);
            this.setDeltaMovement(at.subtract(this.position()));
            this.setPos(at);
            return;
        }
        Vec3 v = this.getDeltaMovement().add(0.0, -0.04, 0.0);
        Vec3 from = this.position();
        Vec3 to = from.add(v);
        BlockHitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean impact = block.getType() == HitResult.Type.BLOCK || this.stateTick > 120;
        if (!impact) {
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().expandTowards(v).inflate(0.6), this::canHurt)) {
                impact = true;
                break;
            }
        }
        if (impact) {
            Vec3 at = block.getType() == HitResult.Type.BLOCK ? block.getLocation() : to;
            this.setPos(at);
            this.planetImpact(level, at);
            this.discard();
            return;
        }
        this.setDeltaMovement(v);
        this.setPos(to);
        if (this.stateTick % 2 == 0) {
            ParticleOptions p = switch (this.variant()) {
                case PLANET_LAVA -> ParticleTypes.FLAME;
                case PLANET_ICE -> ParticleTypes.SNOWFLAKE;
                case PLANET_VOID -> ParticleTypes.REVERSE_PORTAL;
                default -> ParticleTypes.END_ROD;
            };
            level.sendParticles(p, this.getX(), this.getY(), this.getZ(), 6, 1.0, 1.0, 1.0, 0.02);
        }
    }

    /** Der Einschlag einer Welt: Licht, ein Ring – und je nach Welt Glut, Frost, ein gewaltiger Stoß oder ein Sog. */
    private void planetImpact(ServerLevel level, Vec3 at) {
        int variant = this.variant();
        int color = variant == PLANET_LAVA ? DivineFx.EMBER : variant == PLANET_ICE ? DivineFx.FROST : variant == PLANET_VOID ? DivineFx.COSMIC
                : DivineFx.GOLD;
        DivineFx.sound(level, at, ModSounds.SCHROLLI_IMPACT.get(), 6.0f, 0.7f + this.random.nextFloat() * 0.2f);
        DivineFx.flash(level, at.add(0.0, 1.0, 0.0), color);
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 1.0, at.z, 4, 1.6, 0.6, 1.6, 0.0);
        DivineFx.sparks(level, at.add(0.0, 1.0, 0.0), 80, 2.0, 0.3);
        DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.1, 0.0), color, 24, 2.0f).param(0.6f).spawn();
        Entity owner = this.getOwner();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(this.radius + 1.0), this::canHurt)) {
            double d = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).distanceTo(at);
            if (d > this.radius + 0.6) {
                continue;
            }
            float f = (float) Math.max(0.35, 1.0 - d / (this.radius + 0.6));
            if (!e.hurtServer(level, this.divineSource(level), this.damage * f)) {
                continue;
            }
            switch (variant) {
                case PLANET_LAVA -> e.igniteForSeconds(6.0f);
                case PLANET_ICE -> {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), owner);
                    e.setTicksFrozen(Math.max(e.getTicksFrozen(), 200));
                }
                case PLANET_VOID -> e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1), owner);
                default -> SchrolliEntity.knock(e, at, 2.0 * f, 0.9);
            }
            if (variant != PLANET_EARTH) {
                SchrolliEntity.knock(e, at, 0.8 * f, 0.4);
            }
        }
        if (variant == PLANET_LAVA) {
            // glühende Erde: wer darin steht, brennt
            SpellFxEntity scorch = DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, at.add(0.0, 0.06, 0.0), DivineFx.EMBER, 80, 4.0f);
            scorch.onTick(fx -> {
                if (fx.tickCount % 10 == 0) {
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(4.0, 2.0, 4.0), this::canHurt)) {
                        if (DivineFx.flat(e.position(), at) <= 4.0) {
                            e.hurtServer(level, this.divineSource(level), 3.0f);
                            e.igniteForSeconds(2.0f);
                        }
                    }
                    level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 8, 2.5, 0.1, 2.5, 0.01);
                }
            }).spawn();
        }
    }

    /** Glühende Erde nach dem Sturz einer Sonne: wer darin steht, brennt (drei Sekunden lang). */
    private void scorch(ServerLevel level, Vec3 at) {
        Vec3 ground = at.add(0.0, 0.06, 0.0);
        DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, at.add(0.0, 0.1, 0.0), DivineFx.EMBER, 16, 1.5f).param(0.45f).spawn();
        SpellFxEntity glow = DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, ground, DivineFx.AMBER, 60, 3.2f);
        glow.onTick(fx -> {
            if (fx.tickCount % 10 == 0) {
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(3.2, 2.0, 3.2), this::canHurt)) {
                    if (DivineFx.flat(e.position(), at) <= 3.2) {
                        e.hurtServer(level, this.divineSource(level), 3.0f);
                        e.igniteForSeconds(2.0f);
                    }
                }
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.2, at.z, 6, 1.8, 0.1, 1.8, 0.01);
                level.sendParticles(ParticleTypes.LAVA, at.x, at.y + 0.2, at.z, 2, 1.2, 0.1, 1.2, 0.0);
            }
        }).spawn();
    }

    /** Zerspringt in Licht: wer im Umkreis steht, wird getroffen und fortgestoßen. */
    private void burst(ServerLevel level, Vec3 at, double radius, int color) {
        DivineFx.flash(level, at, color);
        DivineFx.sparks(level, at, (int) (20 + radius * 10), radius * 0.35, 0.2);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, (int) (10 + radius * 6), radius * 0.3, radius * 0.3, radius * 0.3, 0.15);
        DivineFx.sound(level, at, this.kind() == Kind.STAR ? ModSounds.SCHROLLI_STAR.get() : ModSounds.SCHROLLI_BLAST.get(),
                radius > 4.0 ? 4.0f : 2.2f, 0.9f + this.random.nextFloat() * 0.3f);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(radius + 1.0), this::canHurt)) {
            double d = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).distanceTo(at);
            if (d > radius + 0.6) {
                continue;
            }
            float f = (float) Math.max(0.4, 1.0 - d / (radius + 0.6));
            if (e.hurtServer(level, this.divineSource(level), this.damage * f)) {
                SchrolliEntity.knock(e, at, 0.8 * f, 0.4 * f);
            }
        }
    }

    private Vec3 groundBelow(ServerLevel level) {
        BlockHitResult hit = level.clip(new ClipContext(this.position(), this.position().add(0.0, -24.0, 0.0), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : this.position().add(0.0, -2.0, 0.0);
    }

    private DamageSource divineSource(ServerLevel level) {
        Entity owner = this.getOwner();
        return level.damageSources().source(ModDamageTypes.DIVINE, this, owner != null ? owner : this);
    }

    /** Wen das Geschoss treffen darf: die Gegner seines Werfers (beim Zepter: alles außer dem Spieler und seinen Begleitern). */
    boolean canHurt(LivingEntity e) {
        Entity owner = this.getOwner();
        if (!e.isAlive() || e == owner || e instanceof ArmorStand) {
            return false;
        }
        if (owner instanceof SchrolliEntity boss) {
            return boss.canStrike(e);
        }
        if (owner instanceof SeraphlingEntity seraph) {
            return seraph.canStrike(e);
        }
        if (owner instanceof Player player) {
            if (e instanceof Player other) {
                return player.canHarmPlayer(other);
            }
            return !(e instanceof OwnableEntity pet && pet.getOwner() == player)
                    && !(e instanceof io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity soldier
                    && player.getUUID().equals(soldier.getOwnerId()));
        }
        return !(e instanceof Enemy);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof LivingEntity living && this.canHurt(living) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(this.level() instanceof ServerLevel level) || !(result.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        Kind kind = this.kind();
        if (kind == Kind.STAR) {
            return;                                 // der Stern zerspringt in onHit
        }
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null);
        if (victim.hurtServer(level, source, this.damage)) {
            Vec3 v = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
            if (v.lengthSqr() > 1.0e-4) {
                v = v.normalize();
                victim.push(v.x * (kind == Kind.SPEAR ? 0.5 : 0.2), 0.15, v.z * (kind == Kind.SPEAR ? 0.5 : 0.2));
                victim.needsSync = true;
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            Vec3 at = result.getLocation();
            if (this.isFallingSun()) {
                this.burst(level, at, this.radius, DivineFx.EMBER);
                this.scorch(level, at);
            } else if (this.kind() == Kind.STAR) {
                this.burst(level, at, this.radius, DivineFx.GOLD);
            } else {
                DivineFx.sparks(level, at, this.kind() == Kind.SPEAR ? 14 : 5, 0.2, 0.08);
                level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, this.kind() == Kind.SPEAR ? 8 : 3, 0.1, 0.1, 0.1, 0.08);
                if (this.kind() == Kind.SPEAR) {
                    DivineFx.sound(level, at, ModSounds.SCHROLLI_STAR.get(), 1.0f, 1.6f);
                }
            }
            this.discard();
        }
    }

    /** Spuren aus Licht (nur auf dem Bildschirm). */
    private void trail(Kind kind, int state) {
        Level level = this.level();
        if (state == HOLD && kind == Kind.SPEAR) {
            if (this.random.nextInt(3) == 0) {
                level.addParticle(ModParticles.DIVINE_SPARK.get(), this.getRandomX(0.4), this.getRandomY(), this.getRandomZ(0.4), 0.0, 0.0, 0.0);
            }
            return;
        }
        Vec3 v = this.getDeltaMovement();
        switch (kind) {
            case SPEAR, STAR -> {
                if (kind == Kind.STAR && this.variant() == PLANET_SUN) {
                    level.addParticle(ParticleTypes.FLAME, this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), -v.x * 0.05, -v.y * 0.05,
                            -v.z * 0.05);
                }
                for (int i = 0; i < (kind == Kind.STAR ? 3 : 2); i++) {
                    double f = i * 0.4;
                    level.addParticle(ParticleTypes.END_ROD, this.getX() - v.x * f, this.getY() - v.y * f, this.getZ() - v.z * f, -v.x * 0.03,
                            -v.y * 0.03, -v.z * 0.03);
                }
                if (this.random.nextInt(2) == 0) {
                    level.addParticle(ModParticles.DIVINE_SPARK.get(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                }
            }
            case FEATHER, BOLT -> {
                if (this.random.nextInt(2) == 0) {
                    level.addParticle(ModParticles.DIVINE_SPARK.get(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                }
            }
            case HALO -> {
                for (int i = 0; i < 2; i++) {
                    double a = this.random.nextDouble() * Math.PI * 2.0;
                    level.addParticle(ParticleTypes.END_ROD, this.getX() + Math.cos(a) * 1.4, this.getY(), this.getZ() + Math.sin(a) * 1.4, 0.0, 0.0,
                            0.0);
                }
            }
            case ORB -> {
                if (state == VORTEX) {
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, this.getRandomX(2.0), this.getRandomY(), this.getRandomZ(2.0), 0.0, 0.0, 0.0);
                } else {
                    level.addParticle(ModParticles.DIVINE_SPARK.get(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                }
            }
            case SUN -> {
                float s = this.visualScale();
                for (int i = 0; i < 1 + (int) s; i++) {
                    double a = this.random.nextDouble() * Math.PI * 2.0;
                    double b = (this.random.nextDouble() - 0.5) * Math.PI;
                    double r = s * 0.9;
                    level.addParticle(ParticleTypes.END_ROD, this.getX() + Math.cos(a) * Math.cos(b) * r, this.getY() + Math.sin(b) * r,
                            this.getZ() + Math.sin(a) * Math.cos(b) * r, 0.0, 0.0, 0.0);
                }
            }
            case FIXED_STAR -> {
                if (this.random.nextInt(3) == 0) {
                    level.addParticle(ModParticles.DIVINE_SPARK.get(), this.getRandomX(0.6), this.getRandomY(), this.getRandomZ(0.6), 0.0, 0.0, 0.0);
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel level && this.getOwner() instanceof SchrolliEntity boss) {
            Kind kind = this.kind();
            if (kind == Kind.HALO || kind == Kind.ORB) {
                boolean others = !level.getEntitiesOfClass(DivineShotEntity.class, boss.getBoundingBox().inflate(SchrolliEntity.ARENA_RADIUS),
                        s -> s != this && s.kind() == kind && s.getOwner() == boss && !s.isRemoved()).isEmpty();
                if (!others) {
                    boss.setFlag(kind == Kind.HALO ? SchrolliEntity.FLAG_HALO : SchrolliEntity.FLAG_ORB, false);
                    DivineFx.flash(level, kind == Kind.HALO ? boss.local(0.0, 5.6, -0.7) : boss.orbHand(), DivineFx.HOLY);
                }
            }
        }
        super.remove(reason);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }

    @Override
    protected float getLiquidInertia() {
        return 1.0f;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }
}
