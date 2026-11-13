package io.github.sirschrolli.kingdomsatwar.entity.boss.sky;

import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.registry.ModDamageTypes;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Bannpfeil der Himmelswacht. Erst lädt er als Siegel über dem Boss (eine Ziellinie zeigt, wen er meint), dann jagt er
 * dem Flieger nach: Er hält vor, wendet aber nur begrenzt scharf – wer im letzten Moment einen Haken schlägt, lässt ihn
 * vorbeischießen. Landet das Ziel, verliert er es und verpufft; an Blöcken zerschellt er. Trifft er, endet das Gleiten,
 * und der {@linkplain FlightBan Flugbann} hält den Getroffenen am Boden – dazu kommt, was zum Boss passt (Brand, Frost,
 * ein Ruck zum Boss, bei Anni der Rückruck auf den Boden …).
 */
public class SkyBoltEntity extends Entity {
    /** Lädt als Siegel über dem Boss. */
    public static final int CHARGE = 0;
    /** Verfolgt sein Ziel. */
    public static final int HOMING = 1;
    /** Hat sein Ziel verloren und fliegt geradeaus, bis er verpufft. */
    public static final int FREE = 2;
    /** So lange verfolgt er höchstens (Ticks). */
    public static final int LIFE = 120;
    /** So viele Ticks muss das Ziel am Boden (oder jedenfalls nicht im Gleitflug) sein, damit er es verliert. */
    public static final int LOSE_TICKS = 8;
    /** So lange fliegt er ohne Ziel noch geradeaus. */
    public static final int FREE_TICKS = 15;
    /** Länge des Schweifs (Punkte), den die Darstellung zieht. */
    public static final int TRAIL = 16;

    private static final EntityDataAccessor<Byte> DATA_THEME = SynchedEntityData.defineId(SkyBoltEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(SkyBoltEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_TARGET = SynchedEntityData.defineId(SkyBoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(SkyBoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE = SynchedEntityData.defineId(SkyBoltEntity.class, EntityDataSerializers.INT);

    private @Nullable LivingEntity owner;
    private @Nullable LivingEntity target;
    private int stateTicks;
    private int life;
    private int notGliding;
    private double speed;
    private @Nullable Vec3 lastTargetPos;
    private Vec3 targetVelocity = Vec3.ZERO;
    /** Wie die Jagd ausging – für die Himmelswacht (Abklingzeit) und die Tests. */
    private Outcome outcome = Outcome.PENDING;
    /** Vorführung: das Siegel bleibt stehen. */
    private boolean held;

    // nur auf dem Client: die letzten Orte (für den Schweif) und wie lange er schon lädt
    private final Vec3[] trail = new Vec3[TRAIL];
    private int trailSize;
    private int clientStateTicks;
    private int clientState = -1;

    /** Wie die Jagd eines Bannpfeils ausging. */
    public enum Outcome { PENDING, HIT, LOST, BLOCKED, EXPIRED, CANCELLED }

    public SkyBoltEntity(EntityType<? extends SkyBoltEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * Lässt über {@code owner} ein Siegel entstehen, das {@code chargeTicks} lang lädt und dann {@code target} nachjagt.
     */
    public static SkyBoltEntity charge(ServerLevel level, LivingEntity owner, LivingEntity target, SkyTheme theme, int chargeTicks) {
        SkyBoltEntity bolt = new SkyBoltEntity(ModEntities.SKY_BOLT.get(), level);
        bolt.owner = owner;
        bolt.target = target;
        bolt.entityData.set(DATA_THEME, (byte) theme.ordinal());
        bolt.entityData.set(DATA_OWNER, owner.getId());
        bolt.entityData.set(DATA_TARGET, target.getId());
        bolt.entityData.set(DATA_CHARGE, Math.max(1, chargeTicks));
        Vec3 anchor = anchor(owner);
        bolt.setPos(anchor.x, anchor.y, anchor.z);
        level.addFreshEntity(bolt);
        level.playSound(null, anchor.x, anchor.y, anchor.z, ModSounds.SKY_WARD_CHARGE.get(), SoundSource.HOSTILE, 4.0f, 30.0f / Math.max(18, chargeTicks));
        return bolt;
    }

    /** Vorführung: ein fertig geladenes Siegel, das über {@code owner} stehen bleibt – ohne Ziel und ohne Schuss. */
    public static SkyBoltEntity display(ServerLevel level, LivingEntity owner, SkyTheme theme) {
        SkyBoltEntity bolt = new SkyBoltEntity(ModEntities.SKY_BOLT.get(), level);
        bolt.owner = owner;
        bolt.held = true;
        bolt.entityData.set(DATA_THEME, (byte) theme.ordinal());
        bolt.entityData.set(DATA_OWNER, owner.getId());
        bolt.entityData.set(DATA_CHARGE, 24);
        Vec3 anchor = anchor(owner);
        bolt.setPos(anchor.x, anchor.y, anchor.z);
        level.addFreshEntity(bolt);
        return bolt;
    }

    /** Wo das Siegel schwebt: ein gutes Stück über dem Kopf des Bosses – unter einer niedrigen Decke rückt es tiefer. */
    public static Vec3 anchor(Entity owner) {
        double top = owner.getBoundingBox().maxY;
        double y = top + 1.5 + Math.min(2.5, owner.getBbWidth() * 0.25);
        Level level = owner.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while (y > top + 0.4 && !level.getBlockState(pos.set(owner.getX(), y + 0.3, owner.getZ())).getCollisionShape(level, pos).isEmpty()) {
            y -= 0.5;
        }
        return new Vec3(owner.getX(), y, owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_THEME, (byte) 0);
        builder.define(DATA_STATE, (byte) CHARGE);
        builder.define(DATA_TARGET, -1);
        builder.define(DATA_OWNER, -1);
        builder.define(DATA_CHARGE, 30);
    }

    public SkyTheme theme() {
        return SkyTheme.byIndex(this.entityData.get(DATA_THEME));
    }

    public int state() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(int state) {
        this.entityData.set(DATA_STATE, (byte) state);
        this.stateTicks = 0;
    }

    /** So lange lädt das Siegel insgesamt (Ticks). */
    public int chargeTicks() {
        return this.entityData.get(DATA_CHARGE);
    }

    public Outcome outcome() {
        return this.outcome;
    }

    /** Das Ziel (auf dem Client über die Kennung gesucht). */
    public @Nullable Entity targetEntity() {
        if (!this.level().isClientSide()) {
            return this.target;
        }
        int id = this.entityData.get(DATA_TARGET);
        return id < 0 ? null : this.level().getEntity(id);
    }

    /** Der Boss, über dem das Siegel schwebt (auf dem Client über die Kennung gesucht). */
    public @Nullable Entity ownerEntity() {
        if (!this.level().isClientSide()) {
            return this.owner;
        }
        int id = this.entityData.get(DATA_OWNER);
        return id < 0 ? null : this.level().getEntity(id);
    }

    // ------------------------------------------------------------------ Ablauf

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            this.stateTicks++;
            switch (this.state()) {
                case CHARGE -> this.tickCharge(level);
                case HOMING -> this.tickHoming(level);
                default -> this.tickFree(level);
            }
        } else {
            this.tickClient();
        }
    }

    private void tickCharge(ServerLevel level) {
        LivingEntity owner = this.owner;
        LivingEntity target = this.target;
        if (this.held) {
            if (owner == null || !owner.isAlive()) {
                this.discard();
            } else {
                Vec3 at = anchor(owner);
                this.setPos(at.x, at.y, at.z);
            }
            return;
        }
        if (owner == null || !owner.isAlive() || owner.level() != level || !valid(target, level)) {
            this.end(level, Outcome.CANCELLED, false);
            return;
        }
        Vec3 anchor = anchor(owner);
        this.setPos(anchor.x, anchor.y, anchor.z);
        this.trackTarget(target);
        // wer sofort landet, dem geschieht nichts: das Siegel erlischt
        if (this.notGliding >= LOSE_TICKS + 2) {
            this.end(level, Outcome.CANCELLED, true);
            return;
        }
        SkyTheme theme = this.theme();
        if (this.stateTicks % 2 == 0) {
            // Funken strömen zum Siegel
            double a = this.random.nextDouble() * Math.PI * 2.0;
            double r = 2.2 + this.random.nextDouble() * 1.2;
            level.sendParticles(theme.particle(), anchor.x + Math.cos(a) * r, anchor.y + 0.3 + this.random.nextDouble() * 0.6,
                    anchor.z + Math.sin(a) * r, 0, -Math.cos(a) * 0.5, 0.0, -Math.sin(a) * 0.5, 0.35);
        }
        if (this.stateTicks >= this.chargeTicks()) {
            this.launch(level, target);
        }
    }

    private void launch(ServerLevel level, LivingEntity target) {
        SkyTheme theme = this.theme();
        this.speed = 0.9;
        Vec3 dir = this.aimPoint(target).subtract(this.position());
        dir = dir.lengthSqr() < 1.0e-6 ? new Vec3(0.0, 1.0, 0.0) : dir.normalize();
        this.setDeltaMovement(dir.scale(this.speed));
        this.lookAlong(dir);
        this.setState(HOMING);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SKY_WARD_LAUNCH.get(), SoundSource.HOSTILE, 4.0f,
                0.95f + this.random.nextFloat() * 0.1f);
        level.sendParticles(theme.particle(), this.getX(), this.getY() + 0.3, this.getZ(), 10, 0.5, 0.2, 0.5, 0.1);
    }

    private void tickHoming(ServerLevel level) {
        LivingEntity target = this.target;
        if (++this.life > LIFE) {
            this.end(level, Outcome.EXPIRED, true);
            return;
        }
        if (!valid(target, level)) {
            this.outcome = Outcome.LOST;
            this.setState(FREE);
            return;
        }
        this.trackTarget(target);
        if (this.notGliding >= LOSE_TICKS) {
            // gelandet: der Pfeil findet ihn nicht mehr
            this.outcome = Outcome.LOST;
            this.setState(FREE);
            return;
        }
        SkyTheme theme = this.theme();
        this.speed = Math.min(theme.speed(), this.speed + 0.12);
        Vec3 dir = this.getDeltaMovement();
        dir = dir.lengthSqr() < 1.0e-6 ? new Vec3(0.0, 1.0, 0.0) : dir.normalize();
        Vec3 want = this.aimPoint(target).subtract(this.position());
        if (want.lengthSqr() > 1.0e-6) {
            dir = turnToward(dir, want.normalize(), theme.turn());
        }
        if (this.fly(level, dir, target)) {
            return;
        }
        if (this.tickCount % 2 == 0) {
            level.sendParticles(theme.particle(), this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.08, 0.08, 0.08, 0.01);
        }
    }

    private void tickFree(ServerLevel level) {
        if (this.stateTicks > FREE_TICKS || ++this.life > LIFE + FREE_TICKS) {
            this.end(level, this.outcome == Outcome.PENDING ? Outcome.EXPIRED : this.outcome, true);
            return;
        }
        Vec3 dir = this.getDeltaMovement();
        if (dir.lengthSqr() < 1.0e-6) {
            this.end(level, this.outcome, true);
            return;
        }
        this.fly(level, dir.normalize(), null);
    }

    /**
     * Fliegt ein Stück in Richtung {@code dir}.
     *
     * @return ob der Pfeil dabei sein Ende gefunden hat (Treffer oder Block)
     */
    private boolean fly(ServerLevel level, Vec3 dir, @Nullable LivingEntity target) {
        Vec3 from = this.position();
        Vec3 to = from.add(dir.scale(this.speed));
        Vec3 eye = new Vec3(0.0, 0.3, 0.0);
        BlockHitResult block = level.clip(new ClipContext(from.add(eye), to.add(eye), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        boolean blocked = block.getType() == HitResult.Type.BLOCK;
        Vec3 reach = blocked ? block.getLocation().subtract(eye) : to;
        if (target != null && target.isFallFlying()) {
            var box = target.getBoundingBox().inflate(0.75);
            if (box.contains(from.add(eye)) || box.clip(from.add(eye), reach.add(eye)).isPresent()) {
                this.setPos(target.getX(), target.getY(0.5) - 0.3, target.getZ());
                this.strike(level, target, dir);
                return true;
            }
        }
        if (blocked) {
            this.setPos(reach.x, reach.y, reach.z);
            this.end(level, Outcome.BLOCKED, true);
            return true;
        }
        this.setPos(to.x, to.y, to.z);
        this.setDeltaMovement(dir.scale(this.speed));
        this.lookAlong(dir);
        return false;
    }

    /**
     * Merkt sich, wie schnell das Ziel fliegt und ob es noch gleitet. Bei Spielern gilt die Bewegung, die ihr Client
     * zuletzt gemeldet hat (ihre Orte kommen beim Server unregelmäßig an); sonst zählt der Weg seit dem letzten Tick.
     */
    private void trackTarget(LivingEntity target) {
        Vec3 pos = target.position();
        Vec3 known = target instanceof ServerPlayer player ? player.getKnownMovement() : Vec3.ZERO;
        if (known.lengthSqr() > 1.0e-6) {
            this.targetVelocity = known;
        } else if (this.lastTargetPos != null) {
            Vec3 step = pos.subtract(this.lastTargetPos);
            // ein Sprung (Teleport) ist keine Geschwindigkeit
            this.targetVelocity = step.lengthSqr() > 36.0 ? Vec3.ZERO : this.targetVelocity.scale(0.4).add(step.scale(0.6));
        }
        this.lastTargetPos = pos;
        this.notGliding = target.isFallFlying() ? 0 : this.notGliding + 1;
    }

    /**
     * Wohin der Pfeil zielt: auf den Treffpunkt – dorthin, wo das Ziel sein wird, wenn er es bei gleichbleibendem Kurs
     * erreicht (höchstens 30 Ticks voraus). Wer geradeaus fliegt, wird so beim ersten Anflug getroffen.
     */
    private Vec3 aimPoint(LivingEntity target) {
        Vec3 center = new Vec3(target.getX(), target.getY(0.5) - 0.3, target.getZ());
        double pace = Math.max(this.speed, this.theme().speed() * 0.9);
        return center.add(this.targetVelocity.scale(interceptTime(center.subtract(this.position()), this.targetVelocity, pace)));
    }

    /**
     * Nach wie vielen Ticks ein Geschoss mit Tempo {@code pace} ein Ziel einholt, das von {@code offset} aus mit
     * {@code velocity} weiterfliegt (0 bis 30; ist es nicht einzuholen, wird auf seinen jetzigen Kurs vorgehalten).
     */
    public static double interceptTime(Vec3 offset, Vec3 velocity, double pace) {
        double a = velocity.lengthSqr() - pace * pace;
        double b = 2.0 * offset.dot(velocity);
        double c = offset.lengthSqr();
        double time;
        if (Math.abs(a) < 1.0e-6) {
            time = b < -1.0e-6 ? -c / b : 30.0;
        } else {
            double disc = b * b - 4.0 * a * c;
            if (disc < 0.0) {
                time = 30.0;
            } else {
                double root = Math.sqrt(disc);
                double t1 = (-b - root) / (2.0 * a);
                double t2 = (-b + root) / (2.0 * a);
                time = t1 > 0.0 && t2 > 0.0 ? Math.min(t1, t2) : Math.max(t1, t2);
                if (time < 0.0) {
                    time = 30.0;
                }
            }
        }
        return Mth.clamp(time, 0.0, 30.0);
    }

    /** Dreht {@code dir} höchstens um {@code maxAngle} (Bogenmaß) auf {@code want} zu; beide sind Einheitsvektoren. */
    public static Vec3 turnToward(Vec3 dir, Vec3 want, double maxAngle) {
        double dot = Mth.clamp(dir.dot(want), -1.0, 1.0);
        double angle = Math.acos(dot);
        if (angle <= maxAngle || angle < 1.0e-4) {
            return want;
        }
        double sin = Math.sin(angle);
        if (sin < 1.0e-4) {
            // genau entgegengesetzt: irgendwohin seitlich ausbrechen
            Vec3 side = Math.abs(dir.y) < 0.9 ? dir.cross(new Vec3(0.0, 1.0, 0.0)) : dir.cross(new Vec3(1.0, 0.0, 0.0));
            return dir.scale(Math.cos(maxAngle)).add(side.normalize().scale(Math.sin(maxAngle))).normalize();
        }
        double t = maxAngle / angle;
        return dir.scale(Math.sin((1.0 - t) * angle) / sin).add(want.scale(Math.sin(t * angle) / sin)).normalize();
    }

    private static boolean valid(@Nullable LivingEntity target, ServerLevel level) {
        return target != null && target.isAlive() && target.level() == level
                && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private void lookAlong(Vec3 v) {
        float yaw = (float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    // ------------------------------------------------------------------ Treffer und Ende

    /** Der Pfeil trifft: Schaden, das Gleiten endet, Flugbann – und was zum Boss passt. */
    private void strike(ServerLevel level, LivingEntity victim, Vec3 dir) {
        SkyTheme theme = this.theme();
        LivingEntity owner = this.owner;
        float damage = theme.damage() * (owner instanceof ElementalBoss boss ? boss.damageMultiplier() : 1.0f);
        DamageSource source = level.damageSources().source(ModDamageTypes.SKY_BOLT, this, owner != null ? owner : this);
        victim.stopFallFlying();
        victim.hurtServer(level, source, damage);
        int ban = FlightBan.apply(victim, theme.banTicks(), owner);
        // der Schwung bricht, es geht abwärts
        Vec3 fall = dir.scale(0.3).add(0.0, theme == SkyTheme.EARTH ? -1.0 : -0.55, 0.0);
        victim.setDeltaMovement(fall);
        this.rider(level, theme, victim, owner);
        victim.needsSync = true;
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.SKY_WARD_HIT.get(), SoundSource.HOSTILE, 2.5f,
                0.95f + this.random.nextFloat() * 0.1f);
        level.sendParticles(theme.particle(), victim.getX(), victim.getY(0.5), victim.getZ(), 22, 0.4, 0.5, 0.4, 0.16);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT, victim.getX(), victim.getY(0.5), victim.getZ(), 18, 0.4, 0.5, 0.4, 0.3);
        if (victim instanceof ServerPlayer player && theme != SkyTheme.GLITCH) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sky_ward.banned", (ban + 19) / 20).withColor(theme.color));
        }
        this.outcome = Outcome.HIT;
        this.discard();
    }

    /** Was der Treffer außer dem Flugbann anrichtet. */
    private void rider(ServerLevel level, SkyTheme theme, LivingEntity victim, @Nullable LivingEntity owner) {
        switch (theme) {
            case FIRE -> victim.igniteForSeconds(5.0f);
            case ICE -> {
                victim.setTicksFrozen(Math.max(victim.getTicksFrozen(), victim.getTicksRequiredToFreeze() + 100));
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), owner);
            }
            case LIGHTNING -> {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), owner);
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0), owner);
            }
            case NATURE -> victim.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), owner);
            case EARTH -> victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), owner);
            case WATER -> {
                victim.clearFire();
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 0), owner);
            }
            case SHADOW -> victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0), owner);
            case REEF -> {
                victim.clearFire();
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), owner);
            }
            case MOTH -> victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3), owner);
            case KEEPER -> {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), owner);
                victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 1), owner);
            }
            case ASH -> victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0), owner);
            case MYSTIK -> {
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0), owner);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, victim.getX(), victim.getY(0.5), victim.getZ(), 30, 0.6, 0.6, 0.6,
                        0.02);
            }
            case FORGE -> {
                victim.igniteForSeconds(4.0f);
                pull(victim, owner, 0.9);
            }
            case PRIEST -> {
                victim.igniteForSeconds(6.0f);
                pull(victim, owner, 1.0);
            }
            case PHASE -> {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2), owner);
                pull(victim, owner, 1.1);
            }
            case ECHO -> {
                victim.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0), owner);
                pull(victim, owner, 1.0);
            }
            case RAVEN -> {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 1), owner);
                victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 120, 0), owner);
                pull(victim, owner, 0.8);
            }
            case STAR -> {
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0), owner);
                pull(victim, owner, 0.8);
            }
            case GLITCH -> this.rubberBand(level, victim, owner);
            case VOID -> victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1), owner);
            case MAW -> pull(victim, owner, 1.3);
            case CREATOR -> {
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1), owner);
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0), owner);
            }
            case TIME -> {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3), owner);
                victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80, 1), owner);
                pull(victim, owner, 0.8);
            }
        }
    }

    /** Ein Ruck zum Boss hin (die Kette des Schmieds, die Schwere des Sterns, der Sog des Schlunds). */
    private static void pull(LivingEntity victim, @Nullable LivingEntity owner, double strength) {
        if (owner == null) {
            return;
        }
        Vec3 to = owner.position().add(0.0, owner.getBbHeight() * 0.5, 0.0).subtract(victim.position());
        if (to.lengthSqr() < 9.0) {
            return;
        }
        Vec3 v = to.normalize().scale(strength);
        victim.setDeltaMovement(v.x, Math.min(v.y, 0.1) - 0.25, v.z);
    }

    /**
     * Anni: „Fliegen ist auf diesem Server nicht erlaubt“ – der Flieger steht mit einem Ruck wieder auf dem Boden unter
     * sich (aber nicht im Boss: wer über ihm flog, landet ein paar Schritte vor ihm).
     */
    private void rubberBand(ServerLevel level, LivingEntity victim, @Nullable LivingEntity owner) {
        double x = victim.getX();
        double z = victim.getZ();
        if (owner != null) {
            double dx = x - owner.getX();
            double dz = z - owner.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double room = owner.getBbWidth() * 0.5 + 6.0;
            if (distance < room) {
                if (distance < 0.01) {
                    Vec3 front = Vec3.directionFromRotation(0.0f, owner.getYRot());
                    dx = front.x;
                    dz = front.z;
                    distance = 1.0;
                }
                double aside = owner.getX() + dx / distance * room;
                double ahead = owner.getZ() + dz / distance * room;
                if (floorBelow(level, aside, victim.getY(), ahead) != null) {
                    x = aside;
                    z = ahead;
                }
            }
        }
        Double floor = floorBelow(level, x, victim.getY(), z);
        if (floor != null) {
            level.sendParticles(SkyTheme.GLITCH.particle(), victim.getX(), victim.getY(0.5), victim.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
            victim.teleportTo(x, floor, z);
            victim.setDeltaMovement(Vec3.ZERO);
            victim.fallDistance = 0.0;
            level.sendParticles(SkyTheme.GLITCH.particle(), victim.getX(), victim.getY(0.5), victim.getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        }
        if (victim instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.sky_ward.no_fly").withColor(0xFF5050));
        }
    }

    /** Die Höhe des Bodens unter diesem Punkt (höchstens 96 Blöcke tiefer) – oder {@code null}, wenn dort keiner ist. */
    private static @Nullable Double floorBelow(ServerLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y, z).mutable();
        int lowest = Math.max(level.getMinY(), pos.getY() - 96);
        while (pos.getY() > lowest && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            pos.move(0, -1, 0);
        }
        return pos.getY() > lowest ? pos.getY() + 1.0 : null;
    }

    /** Der Pfeil endet, ohne zu treffen. */
    private void end(ServerLevel level, Outcome outcome, boolean visible) {
        this.outcome = outcome == Outcome.PENDING ? Outcome.EXPIRED : outcome;
        if (visible) {
            level.sendParticles(this.theme().particle(), this.getX(), this.getY() + 0.3, this.getZ(), 14, 0.25, 0.25, 0.25, 0.08);
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SKY_WARD_FIZZLE.get(), SoundSource.HOSTILE, 1.6f,
                    0.9f + this.random.nextFloat() * 0.2f);
        }
        this.discard();
    }

    // ------------------------------------------------------------------ Client

    private void tickClient() {
        int state = this.state();
        if (state != this.clientState) {
            this.clientState = state;
            this.clientStateTicks = 0;
            this.trailSize = 0;
        }
        this.clientStateTicks++;
        if (state != CHARGE) {
            System.arraycopy(this.trail, 0, this.trail, 1, TRAIL - 1);
            this.trail[0] = this.position();
            this.trailSize = Math.min(TRAIL, this.trailSize + 1);
        }
    }

    /** Client: wie viele Ticks der Pfeil schon in seinem jetzigen Zustand ist. */
    public int clientStateTicks() {
        return this.clientStateTicks;
    }

    /** Client: die letzten Orte des Pfeils, der jüngste zuerst. */
    public int trailSize() {
        return this.trailSize;
    }

    public Vec3 trailPoint(int index) {
        return this.trail[index];
    }

    // ------------------------------------------------------------------ Entity-Pflichten

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
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
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
    }
}
