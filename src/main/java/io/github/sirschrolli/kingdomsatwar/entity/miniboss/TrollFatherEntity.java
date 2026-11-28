package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import io.github.sirschrolli.kingdomsatwar.entity.creature.CaveTrollEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Grumbold, der Trollvater (seit 1.15): der älteste Troll unter der Erde – blaugrau wie alter Fels, Flechten im Pelz,
 * weißes Haar, die Keule mit Goldbändern beschlagen. Er schläft in der Trollhöhle auf seinem Knochenhaufen, bis jemand
 * naht. Er kennt vier Künste:
 * <ul>
 *     <li><b>Felswurf</b> – er reißt einen Brocken aus dem Boden, stemmt ihn über den Kopf und schleudert ihn im Bogen;</li>
 *     <li><b>Erdstoß</b> – ein Warnkreis um ihn, dann schmettert er die Keule in den Boden: Risse laufen nach außen, wer
 *     im Umkreis steht, fliegt;</li>
 *     <li><b>Trollgebrüll</b> – er brüllt, dass die Höhle bebt: wer nahe steht, wird schwach und langsam, und Brocken fallen
 *     von der Decke (ihre Schatten zeigen, wo);</li>
 *     <li><b>Keulenwirbel</b> (nur in Rage) – er wirbelt mit ausgestreckter Keule um sich selbst und stapft dabei auf sein
 *     Ziel zu.</li>
 * </ul>
 * Wie jeder Troll erstarrt er im Sonnenlicht.
 */
public class TrollFatherEntity extends CaveTrollEntity implements MiniBoss {
    public static final float MAX_HEALTH = 220.0f;
    public static final float SCALE = 1.45f;
    public static final float BOULDER_DAMAGE = 11.0f;
    public static final int LIFT = 22;
    public static final float STOMP_DAMAGE = 10.0f;
    public static final double STOMP_RADIUS = 6.0;
    public static final int STOMP_WINDUP = 18;
    public static final float ROCK_DAMAGE = 6.0f;
    public static final float SPIN_DAMAGE = 7.0f;
    public static final double SPIN_RADIUS = 3.6;
    public static final int SPIN_TIME = 50;
    public static final int COLOR = 0xB8B44A;
    /** Haltung (für das Aussehen): nichts, Brocken über dem Kopf, Wurf, Gebrüll, Wirbel. */
    public static final byte POSE_NONE = 0, POSE_LIFT = 1, POSE_THROW = 2, POSE_ROAR = 3, POSE_SPIN = 4;
    private static final EntityDataAccessor<Boolean> DATA_AWAKE = SynchedEntityData.defineId(TrollFatherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(TrollFatherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_POSE = SynchedEntityData.defineId(TrollFatherEntity.class, EntityDataSerializers.BYTE);

    private final MiniBossCore core;
    /** Im Client: Haltung (eingenommen 0..1) und Drehung im Wirbel (Grad). */
    private float poseAmount;
    private float poseAmountO;
    private byte lastPose;
    private float spin;
    private float spinO;

    public TrollFatherEntity(EntityType<? extends TrollFatherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.core = new MiniBossCore(this, BossEvent.BossBarColor.GREEN)
                .special(new Boulder())
                .special(new Stomp())
                .special(new Roar())
                .special(new Spin());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return CaveTrollEntity.createAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return TrollFatherEntity.this.isAwake() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f) {
            @Override
            public boolean canUse() {
                return TrollFatherEntity.this.isAwake() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return TrollFatherEntity.this.isAwake() && super.canUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return TrollFatherEntity.this.isAwake() && super.canUse();
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

    public byte trollPose() {
        return this.entityData.get(DATA_POSE);
    }

    void setTrollPose(byte pose) {
        this.entityData.set(DATA_POSE, pose);
    }

    /** Im Client: die Haltung, die gerade eingenommen wird oder abklingt. */
    public byte shownPose() {
        return this.lastPose;
    }

    public float poseAmount(float partialTick) {
        return Mth.lerp(partialTick, this.poseAmountO, this.poseAmount);
    }

    /** Im Client: Drehung im Keulenwirbel (Grad). */
    public float spin(float partialTick) {
        return Mth.lerp(partialTick, this.spinO, this.spin);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            byte pose = this.trollPose();
            this.poseAmountO = this.poseAmount;
            if (pose != POSE_NONE && pose != this.lastPose) {
                this.lastPose = pose;
                this.poseAmountO = this.poseAmount = pose == POSE_THROW ? this.poseAmount : Math.min(this.poseAmount, 0.3f);
            }
            float goal = pose == POSE_NONE ? 0.0f : 1.0f;
            float step = pose == POSE_THROW ? 0.5f : 0.16f;
            this.poseAmount = goal > this.poseAmount ? Math.min(goal, this.poseAmount + step) : Math.max(goal, this.poseAmount - 0.12f);
            this.spinO = this.spin;
            if (pose == POSE_SPIN) {
                this.spin += 38.0f;
            } else if (this.spin != 0.0f) {
                // auslaufen bis zur nächsten vollen Drehung
                float rest = 360.0f - Mth.positiveModulo(this.spin, 360.0f);
                float step2 = Math.min(rest, 24.0f);
                this.spin += step2;
                if (rest <= 24.0f) {
                    this.spin = 0.0f;
                    this.spinO = 0.0f;
                }
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.core.tick(level);
        if (this.core.current() == null && this.trollPose() != POSE_NONE) {
            this.setTrollPose(POSE_NONE);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.trollPose() == POSE_SPIN || this.trollPose() == POSE_LIFT) {
            return false;                                       // im Wirbel oder mit dem Brocken über dem Kopf schlägt er nicht zu
        }
        return super.doHurtTarget(level, target);
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
    protected net.minecraft.sounds.@Nullable SoundEvent getAmbientSound() {
        return this.isAwake() ? super.getAmbientSound() : SoundEvents.FOX_SLEEP;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f + (this.random.nextFloat() - 0.5f) * 0.08f;
    }

    @Override
    public void onWake(ServerLevel level, @Nullable LivingEntity cause) {
        this.core.sound(SoundEvents.RAVAGER_ROAR, 2.5f, 0.5f);
        this.core.sound(ModSounds.TROLL_AMBIENT.get(), 2.5f, 0.55f);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(0.9), this.getZ(), 10, 0.4, 0.2, 0.4, 0.01);
    }

    @Override
    public void onEnrage(ServerLevel level) {
        this.core.sound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.4f);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY(1.0), this.getZ(), 6, 0.6, 0.3, 0.6, 0.0);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.3);
        }
    }

    @Override
    public void onSleep(ServerLevel level) {
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.26);
        }
        this.setTrollPose(POSE_NONE);
    }

    /** Welcher Block als Brocken aus dem Boden gerissen wird: Stein bleibt Stein, sonst bemooster Bruchstein. */
    BlockState rockFrom(ServerLevel level) {
        BlockState floor = level.getBlockState(this.blockPosition().below());
        if (floor.is(BlockTags.BASE_STONE_OVERWORLD) || floor.is(Blocks.COBBLESTONE) || floor.is(Blocks.MOSSY_COBBLESTONE)) {
            return floor.getBlock().defaultBlockState();
        }
        return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    }

    // =====================================================================================
    // Künste
    // =====================================================================================

    /** Felswurf: einen Brocken aus dem Boden reißen, über den Kopf stemmen und schleudern. */
    final class Boulder extends MiniSpecial {
        private @Nullable TrollBoulderEntity rock;

        Boulder() {
            super("boulder", LIFT + 14, 5);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance > 5.5 && distance < 24.0 && TrollFatherEntity.this.hasLineOfSight(target);
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            troll.setTrollPose(POSE_LIFT);
            BlockState block = troll.rockFrom(level);
            this.rock = new TrollBoulderEntity(level, troll, block);
            this.rock.setDamage(BOULDER_DAMAGE);
            this.rock.setSplit(troll.isEnraged());
            level.addFreshEntity(this.rock);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, block), troll.getX(), troll.getY() + 0.2, troll.getZ(), 40, 0.9, 0.2, 0.9,
                    0.2);
            troll.core.sound(SoundEvents.STONE_BREAK, 2.0f, 0.5f);
            troll.core.sound(ModSounds.TROLL_AMBIENT.get(), 2.0f, 0.6f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            troll.getLookControl().setLookAt(target, 30.0f, 30.0f);
            troll.setYBodyRot(troll.getYHeadRot());
            if (t == LIFT - 4) {
                troll.setTrollPose(POSE_THROW);
            }
            if (t == LIFT && this.rock != null && this.rock.isAlive()) {
                // dorthin, wo das Ziel gleich sein wird
                Vec3 aim = target.position().add(target.getDeltaMovement().multiply(8.0, 0.0, 8.0));
                this.rock.lob(aim, 1.15);
                troll.core.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.4f);
                troll.core.sound(ModSounds.TROLL_SMASH.get(), 1.4f, 0.5f);
                this.rock = null;
            }
        }

        @Override
        public void stop(ServerLevel level) {
            if (this.rock != null && this.rock.isAlive() && this.rock.isHeld()) {
                this.rock.shatter(level);
            }
            this.rock = null;
        }

        @Override
        public int recharge(RandomSource random) {
            return 110 + random.nextInt(40);
        }
    }

    /** Erdstoß: ein Warnkreis, dann schmettert er die Keule in den Boden – Risse laufen nach außen. */
    final class Stomp extends MiniSpecial {
        Stomp() {
            super("stomp", STOMP_WINDUP + 14, 5);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < STOMP_RADIUS + 0.5;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, troll.position().add(0.0, 0.03, 0.0), null, COLOR, STOMP_WINDUP, (float) STOMP_RADIUS)
                    .spawn();
            troll.core.sound(ModSounds.TROLL_AMBIENT.get(), 2.0f, 0.5f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            if (t == STOMP_WINDUP - 8) {
                // die Keule fällt in 0,38 s (siehe CaveTrollModel) – genau mit dem Ende des Warnkreises
                level.broadcastEntityEvent(troll, EVENT_SMASH);
            }
            if (t != STOMP_WINDUP) {
                return;
            }
            Vec3 c = troll.position();
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.05, 0.0), null, COLOR, 14, 1.0f).param(0.42f).spawn();
            for (int i = 0; i < 4; i++) {
                float yaw = troll.yBodyRot + i * 90.0f + 45.0f;
                Vec3 dir = Vec3.directionFromRotation(0.0f, yaw);
                SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, c.add(dir.scale(1.2)).add(0.0, 0.03, 0.0), null, 0xFF9A3A, 30, 0.6f).param(0.0f)
                        .end(c.add(dir.scale(STOMP_RADIUS + 1.0))).spawn();
            }
            BlockState ground = level.getBlockState(troll.blockPosition().below());
            if (!ground.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), c.x, c.y + 0.2, c.z, 90, STOMP_RADIUS * 0.45, 0.2,
                        STOMP_RADIUS * 0.45, 0.25);
            }
            troll.core.sound(SoundEvents.GENERIC_EXPLODE.value(), 1.8f, 0.45f);
            troll.core.sound(ModSounds.TROLL_SMASH.get(), 2.5f, 0.5f);
            dust(level, c, 6);
            for (LivingEntity victim : troll.core.foesAround(c, STOMP_RADIUS, 1.0, 2.5)) {
                if (troll.core.hurt(level, victim, STOMP_DAMAGE)) {
                    MiniBossCore.knock(victim, c, 0.7, 0.7);
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 1), troll);
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 90 + random.nextInt(40);
        }
    }

    /** Trollgebrüll: schwach und langsam, und Brocken fallen von der Decke. */
    final class Roar extends MiniSpecial {
        static final int SHOUT = 10;
        static final int FALL = 22;
        private final List<Vec3> spots = new ArrayList<>();

        Roar() {
            super("roar", 40, 3);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < 14.0;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            troll.setTrollPose(POSE_ROAR);
            this.spots.clear();
            troll.core.sound(ModSounds.TROLL_AMBIENT.get(), 2.0f, 0.7f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            if (t == SHOUT) {
                troll.core.sound(SoundEvents.RAVAGER_ROAR, 3.0f, 0.45f);
                Vec3 c = troll.position();
                SpellFxEntity.create(level, SpellFxEntity.Kind.ECHO, c.add(0.0, 0.1, 0.0), null, 0xD8D0B0, 18, 1.2f).param(0.55f).spawn();
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, troll.getX(), troll.getEyeY(), troll.getZ(), 8, 0.3, 0.1, 0.3, 0.03);
                dust(level, c, 9);
                for (LivingEntity victim : troll.core.foesAround(c, 9.0, 2.0, 4.0)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1), troll);
                    victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), troll);
                    MiniBossCore.knock(victim, c, 0.45, 0.15);
                }
                // die Brocken: über jedem Gegner und ringsum
                RandomSource random = troll.getRandom();
                int n = troll.isEnraged() ? 7 : 5;
                for (LivingEntity foe : troll.core.foesAround(c, 16.0, 3.0, 6.0)) {
                    this.spots.add(foe.position());
                    if (this.spots.size() >= 3) {
                        break;
                    }
                }
                for (int i = 0; i < n * 4 && this.spots.size() < n; i++) {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    double r = 2.5 + random.nextDouble() * 7.0;
                    Vec3 at = CryptKingEntity.floor(level, c.add(Math.cos(a) * r, 1.5, Math.sin(a) * r));
                    if (at != null) {
                        this.spots.add(at);
                    }
                }
                for (Vec3 s : this.spots) {
                    Vec3 g = CryptKingEntity.floor(level, s.add(0.0, 0.5, 0.0));
                    Vec3 mark = g == null ? s : g;
                    SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, mark.add(0.0, 0.03, 0.0), null, 0x2A2420, FALL - SHOUT + 16,
                            (float) TrollBoulderEntity.SMALL_RADIUS).spawn();
                }
                return;
            }
            if (t == FALL) {
                for (Vec3 s : this.spots) {
                    Vec3 top = ceiling(level, s);
                    TrollBoulderEntity.drop(level, troll, top, Blocks.COBBLESTONE.defaultBlockState(), ROCK_DAMAGE);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), top.x, top.y + 0.6, top.z, 8,
                            0.4, 0.1, 0.4, 0.0);
                }
                troll.core.sound(SoundEvents.GRAVEL_BREAK, 2.0f, 0.5f);
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 220 + random.nextInt(60);
        }
    }

    /** Keulenwirbel (nur in Rage): dreht sich mit ausgestreckter Keule und stapft auf sein Ziel zu. */
    final class Spin extends MiniSpecial {
        private final IntSet hit = new IntOpenHashSet();

        Spin() {
            super("spin", SPIN_TIME + 12, 4);
        }

        @Override
        public boolean enragedOnly() {
            return true;
        }

        @Override
        public boolean roots() {
            return false;
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < 8.0;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            TrollFatherEntity.this.setTrollPose(POSE_SPIN);
            TrollFatherEntity.this.core.sound(ModSounds.TROLL_AMBIENT.get(), 2.0f, 0.8f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            TrollFatherEntity troll = TrollFatherEntity.this;
            if (t >= SPIN_TIME) {
                // danach ist ihm kurz schwindlig
                troll.setTrollPose(POSE_NONE);
                troll.getNavigation().stop();
                if (t == SPIN_TIME) {
                    level.sendParticles(ParticleTypes.CRIT, troll.getX(), troll.getY(1.05), troll.getZ(), 10, 0.5, 0.1, 0.5, 0.05);
                }
                return;
            }
            if (t % 10 == 0) {
                troll.getNavigation().moveTo(target, 0.75);
            }
            if (t % 6 == 0) {
                this.hit.clear();
                troll.core.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.8f, 0.5f);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, troll.getX(), troll.getY(0.45), troll.getZ(), 3, SPIN_RADIUS * 0.4, 0.2, SPIN_RADIUS * 0.4,
                        0.0);
            }
            for (LivingEntity victim : troll.core.foesAround(troll.position(), SPIN_RADIUS, 0.5, 3.5)) {
                if (this.hit.add(victim.getId()) && troll.core.hurt(level, victim, SPIN_DAMAGE)) {
                    MiniBossCore.knock(victim, troll.position(), 1.0, 0.35);
                }
            }
        }

        @Override
        public void stop(ServerLevel level) {
            TrollFatherEntity.this.setTrollPose(POSE_NONE);
        }

        @Override
        public int recharge(RandomSource random) {
            return 200 + random.nextInt(60);
        }
    }

    /** Staub und Kiesel rieseln von der Decke (die Höhle bebt). */
    static void dust(ServerLevel level, Vec3 c, int radius) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 14; i++) {
            Vec3 s = c.add((random.nextDouble() - 0.5) * radius * 2.0, 0.0, (random.nextDouble() - 0.5) * radius * 2.0);
            Vec3 top = ceiling(level, s);
            level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.GRAVEL.defaultBlockState()), top.x, top.y + 0.6, top.z, 2, 0.3,
                    0.0, 0.3, 0.0);
        }
    }

    /** Knapp unter der Decke über einem Punkt (höchstens acht Blöcke hoch). */
    static Vec3 ceiling(ServerLevel level, Vec3 at) {
        BlockPos p = BlockPos.containing(at.x, at.y + 1.0, at.z);
        for (int i = 0; i < 9; i++) {
            BlockPos up = p.above();
            if (!level.getBlockState(up).getCollisionShape(level, up).isEmpty()) {
                return new Vec3(at.x, p.getY() - 0.2, at.z);
            }
            p = up;
        }
        return new Vec3(at.x, p.getY(), at.z);
    }
}
