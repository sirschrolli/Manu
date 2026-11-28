package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Runenwächter (seit 1.15): ein Koloss aus dunklem Stein und Eisen, den die Zwerge einst in ihre Halle stellten. Er
 * steht reglos wie eine Statue am Ende der Halle – seine Runen sind erloschen –, bis jemand naht; dann glühen sie eisblau
 * auf. Er kennt vier Künste:
 * <ul>
 *     <li><b>Runenbeben</b> – er reißt beide Fäuste über den Kopf (ein Warnkreis am Boden), dann schmettert er sie in den
 *     Boden, und eine Welle schleudert alle im Umkreis hoch;</li>
 *     <li><b>Runenblick</b> – seine Augen laden sich auf (eine feine Linie zeigt, wohin er blickt), dann hält er den Blick
 *     einen Herzschlag lang fest und feuert einen Strahl – wer jetzt noch dort steht, wird getroffen;</li>
 *     <li><b>Runenfallen</b> – er zeichnet leuchtende Runenkreise um sein Ziel in den Boden, die kurz darauf
 *     hochgehen;</li>
 *     <li><b>Runenwall</b> (nur in Rage) – er kreuzt die Arme, Runen kreisen um ihn: Schläge prallen fast wirkungslos ab und
 *     wer zuschlägt, bekommt einen Schlag zurück; am Ende entlädt sich der Wall in einer Welle.</li>
 * </ul>
 */
public class RuneWardenEntity extends Monster implements MiniBoss {
    public static final float MAX_HEALTH = 200.0f;
    public static final float SCALE = 1.2f;
    public static final int RUNE = 0x6FF4FF;
    public static final float SLAM_DAMAGE = 11.0f;
    public static final double SLAM_RADIUS = 5.0;
    public static final int SLAM_WINDUP = 18;
    public static final float BEAM_DAMAGE = 12.0f;
    public static final double BEAM_RANGE = 24.0;
    public static final int BEAM_LOCK = 24;
    public static final int BEAM_FIRE = 32;
    public static final float MINE_DAMAGE = 9.0f;
    public static final double MINE_RADIUS = 1.6;
    public static final int MINE_DELAY = 30;
    public static final float WARD_FACTOR = 0.25f;
    public static final float WARD_ZAP = 4.0f;
    public static final float WARD_BURST = 8.0f;
    public static final int WARD_TIME = 80;
    /** Haltung (für das Aussehen): nichts, Fäuste über dem Kopf, Fäuste unten (nach dem Beben), Blick, Zeichnen, Wall. */
    public static final byte POSE_NONE = 0, POSE_RAISE = 1, POSE_SLAM = 2, POSE_GAZE = 3, POSE_INSCRIBE = 4, POSE_WARD = 5;
    public static final byte EVENT_ATTACK = 4;
    private static final EntityDataAccessor<Boolean> DATA_AWAKE = SynchedEntityData.defineId(RuneWardenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(RuneWardenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_POSE = SynchedEntityData.defineId(RuneWardenEntity.class, EntityDataSerializers.BYTE);

    private final MiniBossCore core;
    private int attackAnimationTick;
    /** Im Client: wie weit die Haltung eingenommen ist (0..1), und welche zuletzt. */
    private float poseAmount;
    private float poseAmountO;
    private byte lastPose;
    private boolean warding;

    public RuneWardenEntity(EntityType<? extends RuneWardenEntity> type, Level level) {
        super(type, level);
        // die Größe (SCALE) steht schon in den Grundwerten – der Rahmen wächst aber erst, wenn man ihn neu bemisst
        this.refreshDimensions();
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.core = new MiniBossCore(this, BossEvent.BossBarColor.WHITE)
                .special(new Quake())
                .special(new Gaze())
                .special(new RuneMines())
                .special(new RuneWard());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.0)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return RuneWardenEntity.this.isAwake() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f) {
            @Override
            public boolean canUse() {
                return RuneWardenEntity.this.isAwake() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return RuneWardenEntity.this.isAwake() && super.canUse();
            }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AWAKE, false);
        builder.define(DATA_ENRAGED, false);
        builder.define(DATA_POSE, POSE_NONE);
    }

    @Override
    public MiniBossCore miniBoss() {
        return this.core;
    }

    @Override
    public boolean isAwake() {
        return this.entityData.get(DATA_AWAKE);
    }

    @Override
    public void setAwake(boolean awake) {
        this.entityData.set(DATA_AWAKE, awake);
    }

    @Override
    public boolean isEnraged() {
        return this.entityData.get(DATA_ENRAGED);
    }

    @Override
    public void setEnraged(boolean enraged) {
        this.entityData.set(DATA_ENRAGED, enraged);
    }

    public byte wardenPose() {
        return this.entityData.get(DATA_POSE);
    }

    void setWardenPose(byte pose) {
        this.entityData.set(DATA_POSE, pose);
    }

    /** Im Client: die Haltung, die gerade eingenommen wird oder abklingt. */
    public byte shownPose() {
        return this.lastPose;
    }

    /** Im Client: wie weit die Haltung eingenommen ist (0..1). */
    public float poseAmount(float partialTick) {
        return Mth.lerp(partialTick, this.poseAmountO, this.poseAmount);
    }

    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    /** Hält gerade den Runenwall. */
    public boolean isWarding() {
        return this.warding;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackAnimationTick > 0) {
            this.attackAnimationTick--;
        }
        if (this.level().isClientSide()) {
            byte pose = this.wardenPose();
            this.poseAmountO = this.poseAmount;
            if (pose != POSE_NONE && pose != this.lastPose) {
                this.lastPose = pose;
                this.poseAmountO = this.poseAmount = Math.min(this.poseAmount, 0.35f);
            }
            float goal = pose == POSE_NONE ? 0.0f : 1.0f;
            float step = pose == POSE_SLAM ? 0.6f : 0.2f;
            this.poseAmount = goal > this.poseAmount ? Math.min(goal, this.poseAmount + step) : Math.max(goal, this.poseAmount - 0.15f);
            if (this.isAwake() && this.random.nextInt(this.isEnraged() ? 3 : 6) == 0) {
                this.level().addParticle(new DustParticleOptions(RUNE, 0.8f), this.getRandomX(0.7), this.getY(0.2 + this.random.nextDouble() * 0.7),
                        this.getRandomZ(0.7), 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.core.tick(level);
        if (this.core.current() == null && this.wardenPose() != POSE_NONE) {
            this.setWardenPose(POSE_NONE);
            this.warding = false;
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.attackAnimationTick = 10;
        level.broadcastEntityEvent(this, EVENT_ATTACK);
        boolean hit = super.doHurtTarget(level, target);
        if (hit) {
            // wie der Eisengolem: der Schlag hebt das Ziel hoch
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.45, 0.0));
            target.needsSync = true;
        }
        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.2f, 0.6f);
        return hit;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimationTick = 10;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.2f, 0.6f);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.warding && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= WARD_FACTOR;
            if (source.getEntity() instanceof LivingEntity attacker && attacker != this && this.core.foe(attacker) && attacker.distanceTo(this) < 6.0) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.ARC, this.position().add(0.0, this.getBbHeight() * 0.55, 0.0), null, RUNE, 6, 0.25f)
                        .end(attacker.position().add(0.0, attacker.getBbHeight() * 0.5, 0.0)).spawn();
                if (this.core.hurt(level, attacker, WARD_ZAP)) {
                    MiniBossCore.knock(attacker, this.position(), 0.9, 0.35);
                }
                this.core.sound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.6f, 1.6f);
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.core.seenBy(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.core.unseenBy(player);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            this.core.gone(level);
            level.sendParticles(new DustParticleOptions(RUNE, 1.4f), this.getX(), this.getY(0.5), this.getZ(), 60, 0.8, 1.2, 0.8, 0.0);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel level) {
            this.core.gone(level);
        }
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.core.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.core.load(input);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.2f, 0.6f);
    }

    @Override
    public float getVoicePitch() {
        return 0.5f + this.random.nextFloat() * 0.08f;
    }

    @Override
    public void onWake(ServerLevel level, @Nullable LivingEntity cause) {
        this.core.sound(SoundEvents.BEACON_ACTIVATE, 2.5f, 0.6f);
        this.core.sound(SoundEvents.IRON_GOLEM_REPAIR, 2.0f, 0.5f);
        level.sendParticles(new DustParticleOptions(RUNE, 1.2f), this.getX(), this.getY(0.5), this.getZ(), 50, 0.7, 1.2, 0.7, 0.0);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), this.getX(), this.getY(0.6), this.getZ(),
                40, 0.6, 1.0, 0.6, 0.1);
    }

    @Override
    public void onEnrage(ServerLevel level) {
        this.core.sound(SoundEvents.BEACON_POWER_SELECT, 2.5f, 0.5f);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(0.6), this.getZ(), 50, 0.8, 1.0, 0.8, 0.2);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.28);
        }
    }

    @Override
    public void onSleep(ServerLevel level) {
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.24);
        }
        this.setWardenPose(POSE_NONE);
        this.warding = false;
    }

    Vec3 eyes() {
        return this.getEyePosition();
    }

    // =====================================================================================
    // Künste
    // =====================================================================================

    /** Runenbeben: beide Fäuste über den Kopf, dann in den Boden – eine Welle schleudert alle im Umkreis hoch. */
    final class Quake extends MiniSpecial {
        Quake() {
            super("slam", 34, 5);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < SLAM_RADIUS + 0.5;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            RuneWardenEntity w = RuneWardenEntity.this;
            w.setWardenPose(POSE_RAISE);
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, w.position().add(0.0, 0.03, 0.0), null, RUNE, SLAM_WINDUP, (float) SLAM_RADIUS).spawn();
            w.core.sound(SoundEvents.IRON_GOLEM_REPAIR, 2.0f, 0.5f);
            w.core.sound(SoundEvents.BEACON_POWER_SELECT, 1.4f, 1.4f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            RuneWardenEntity w = RuneWardenEntity.this;
            if (t < SLAM_WINDUP) {
                if (t % 3 == 0) {
                    level.sendParticles(new DustParticleOptions(RUNE, 1.0f), w.getX(), w.getY() + w.getBbHeight() + 0.3, w.getZ(), 3, 0.6, 0.2, 0.6, 0.0);
                }
                return;
            }
            if (t == SLAM_WINDUP) {
                w.setWardenPose(POSE_SLAM);
                Vec3 c = w.position();
                SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.05, 0.0), null, RUNE, 12, 1.0f).param(0.36f).spawn();
                BlockState ground = level.getBlockState(w.blockPosition().below());
                if (!ground.isAir()) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), c.x, c.y + 0.2, c.z, 70, SLAM_RADIUS * 0.45, 0.2,
                            SLAM_RADIUS * 0.45, 0.2);
                }
                level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 2, 0.6, 0.1, 0.6, 0.0);
                w.core.sound(SoundEvents.GENERIC_EXPLODE.value(), 1.6f, 0.5f);
                w.core.sound(SoundEvents.IRON_GOLEM_DAMAGE, 2.0f, 0.5f);
                for (LivingEntity victim : w.core.foesAround(c, SLAM_RADIUS, 1.0, 2.5)) {
                    if (w.core.hurt(level, victim, SLAM_DAMAGE)) {
                        MiniBossCore.knock(victim, c, 0.6, 0.75);
                    }
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 100 + random.nextInt(40);
        }
    }

    /** Runenblick: die Augen laden sich auf, der Blick wird festgehalten, dann ein Strahl. */
    final class Gaze extends MiniSpecial {
        private Vec3 aim = Vec3.ZERO;

        Gaze() {
            super("beam", 44, 4);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance > 4.5 && distance < BEAM_RANGE - 4.0 && RuneWardenEntity.this.hasLineOfSight(target);
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            RuneWardenEntity w = RuneWardenEntity.this;
            w.setWardenPose(POSE_GAZE);
            this.aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
            w.core.sound(SoundEvents.BEACON_AMBIENT, 2.5f, 1.4f);
            w.core.sound(SoundEvents.GUARDIAN_ATTACK, 1.6f, 0.6f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            RuneWardenEntity w = RuneWardenEntity.this;
            if (t < BEAM_LOCK) {
                this.aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
            }
            w.getLookControl().setLookAt(this.aim.x, this.aim.y, this.aim.z, 30.0f, 30.0f);
            Vec3 from = w.eyes();
            Vec3 dir = this.aim.subtract(from);
            if (dir.lengthSqr() < 1.0e-4) {
                return;
            }
            dir = dir.normalize();
            if (t < BEAM_FIRE) {
                // die feine Linie: wohin er blickt (festgehalten ab BEAM_LOCK – dann dichter und heller)
                boolean locked = t >= BEAM_LOCK;
                if (t % (locked ? 1 : 2) == 0) {
                    double len = Math.min(BEAM_RANGE, this.aim.distanceTo(from) + 3.0);
                    for (double d = 1.0; d < len; d += locked ? 0.7 : 1.3) {
                        Vec3 p = from.add(dir.scale(d));
                        level.sendParticles(new DustParticleOptions(locked ? 0xFFFFFF : RUNE, locked ? 0.9f : 0.6f), p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                if (t == BEAM_LOCK) {
                    w.core.sound(SoundEvents.BEACON_POWER_SELECT, 2.0f, 1.8f);
                }
                return;
            }
            if (t == BEAM_FIRE) {
                Vec3 far = from.add(dir.scale(BEAM_RANGE));
                Vec3 end = level.clip(new ClipContext(from, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, w)).getLocation();
                SpellFxEntity.create(level, SpellFxEntity.Kind.BEAM, from, null, RUNE, 12, 0.45f).end(end).spawn();
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 24, 0.3, 0.3, 0.3, 0.3);
                w.core.sound(SoundEvents.BEACON_DEACTIVATE, 2.5f, 1.6f);
                w.core.sound(SoundEvents.TRIDENT_THUNDER.value(), 0.8f, 1.8f);
                AABB span = new AABB(from, end).inflate(1.0);
                for (LivingEntity victim : w.core.foes(span)) {
                    AABB box = victim.getBoundingBox().inflate(0.45);
                    if ((box.contains(from) || box.clip(from, end).isPresent()) && w.core.hurt(level, victim, BEAM_DAMAGE)) {
                        MiniBossCore.knock(victim, from, 0.7, 0.3);
                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1), w);
                    }
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 140 + random.nextInt(40);
        }
    }

    /** Runenfallen: leuchtende Kreise um das Ziel, die kurz darauf hochgehen. */
    final class RuneMines extends MiniSpecial {
        private final List<Vec3> spots = new ArrayList<>();
        private final IntSet struck = new IntOpenHashSet();

        RuneMines() {
            super("mines", MINE_DELAY + 12, 4);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < 18.0;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            RuneWardenEntity w = RuneWardenEntity.this;
            w.setWardenPose(POSE_INSCRIBE);
            this.spots.clear();
            this.struck.clear();
            RandomSource random = w.getRandom();
            int n = w.isEnraged() ? 7 : 5;
            Vec3 under = CryptKingEntity.floor(level, target.position().add(0.0, 0.5, 0.0));
            if (under != null) {
                this.spots.add(under);
            }
            for (int i = 0; i < n * 6 && this.spots.size() < n; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = 1.8 + random.nextDouble() * 3.0;
                Vec3 at = CryptKingEntity.floor(level, target.position().add(Math.cos(a) * r, 1.5, Math.sin(a) * r));
                if (at == null) {
                    continue;
                }
                boolean free = true;
                for (Vec3 s : this.spots) {
                    if (s.distanceToSqr(at) < 2.6 * 2.6) {
                        free = false;
                        break;
                    }
                }
                if (free) {
                    this.spots.add(at);
                }
            }
            for (Vec3 s : this.spots) {
                SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, s.add(0.0, 0.03, 0.0), null, RUNE, MINE_DELAY, (float) MINE_RADIUS).spawn();
            }
            w.core.sound(SoundEvents.ENCHANTMENT_TABLE_USE, 2.0f, 0.6f);
            w.core.sound(SoundEvents.BEACON_POWER_SELECT, 1.2f, 1.2f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            RuneWardenEntity w = RuneWardenEntity.this;
            if (t < MINE_DELAY) {
                if (t % 4 == 0) {
                    for (Vec3 s : this.spots) {
                        level.sendParticles(ParticleTypes.ENCHANT, s.x, s.y + 0.4, s.z, 3, 0.4, 0.2, 0.4, 0.4);
                    }
                }
                return;
            }
            if (t == MINE_DELAY) {
                w.core.sound(SoundEvents.GENERIC_EXPLODE.value(), 1.2f, 1.3f);
                w.core.sound(SoundEvents.AMETHYST_BLOCK_BREAK, 2.0f, 0.6f);
                for (Vec3 s : this.spots) {
                    SpellFxEntity.create(level, SpellFxEntity.Kind.PILLAR, s, null, RUNE, 14, (float) MINE_RADIUS * 0.7f).param(3.5f).spawn();
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, s.x, s.y + 0.5, s.z, 14, 0.4, 0.6, 0.4, 0.2);
                    for (LivingEntity victim : w.core.foesAround(s, MINE_RADIUS, 0.5, 3.0)) {
                        if (this.struck.add(victim.getId()) && w.core.hurt(level, victim, MINE_DAMAGE)) {
                            victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.3, 0.0, 0.3).add(0.0, 0.85, 0.0));
                            victim.needsSync = true;
                        }
                    }
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 160 + random.nextInt(40);
        }
    }

    /** Runenwall (nur in Rage): Schläge prallen ab und schlagen zurück, am Ende entlädt sich der Wall. */
    final class RuneWard extends MiniSpecial {
        RuneWard() {
            super("ward", WARD_TIME + 4, 3);
        }

        @Override
        public boolean enragedOnly() {
            return true;
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < 10.0;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            RuneWardenEntity w = RuneWardenEntity.this;
            w.setWardenPose(POSE_WARD);
            w.warding = true;
            w.core.sound(SoundEvents.BEACON_ACTIVATE, 2.0f, 1.5f);
            w.core.sound(SoundEvents.ANVIL_PLACE, 1.4f, 0.5f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            RuneWardenEntity w = RuneWardenEntity.this;
            if (t < WARD_TIME) {
                if (t % 2 == 0) {
                    // Runen kreisen um ihn (drei Bänder in verschiedenen Höhen)
                    for (int band = 0; band < 3; band++) {
                        double a = (t * 0.22 + band * 2.09) * (band % 2 == 0 ? 1 : -1);
                        double r = w.getBbWidth() * 0.5 + 0.9;
                        double y = w.getY() + w.getBbHeight() * (0.25 + band * 0.28);
                        for (int k = 0; k < 2; k++) {
                            double b = a + k * Math.PI;
                            level.sendParticles(new DustParticleOptions(RUNE, 1.1f), w.getX() + Math.cos(b) * r, y, w.getZ() + Math.sin(b) * r, 1, 0.0,
                                    0.0, 0.0, 0.0);
                        }
                    }
                }
                if (t % 20 == 10) {
                    w.core.sound(SoundEvents.BEACON_AMBIENT, 1.6f, 1.8f);
                }
                return;
            }
            if (t == WARD_TIME) {
                w.warding = false;
                w.setWardenPose(POSE_SLAM);
                Vec3 c = w.position();
                SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.05, 0.0), null, RUNE, 14, 1.0f).param(0.45f).spawn();
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 1.0, c.z, 60, 1.2, 1.0, 1.2, 0.3);
                w.core.sound(SoundEvents.BEACON_DEACTIVATE, 2.5f, 0.8f);
                w.core.sound(SoundEvents.GENERIC_EXPLODE.value(), 1.4f, 0.8f);
                for (LivingEntity victim : w.core.foesAround(c, 6.5, 1.0, 3.0)) {
                    if (w.core.hurt(level, victim, WARD_BURST)) {
                        MiniBossCore.knock(victim, c, 1.3, 0.5);
                    }
                }
            }
        }

        @Override
        public void stop(ServerLevel level) {
            RuneWardenEntity.this.warding = false;
        }

        @Override
        public int recharge(RandomSource random) {
            return 260 + random.nextInt(60);
        }
    }
}
