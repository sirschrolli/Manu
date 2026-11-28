package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Brutmutter (seit 1.15): eine riesige Spinne, die in der Brutgrotte über ihrem Nest wacht. Sie beißt giftig und
 * kennt vier Künste:
 * <ul>
 *     <li><b>Netzsalve</b> – sie bäumt sich auf und speit drei Netze (in Rage fünf), die festhalten;</li>
 *     <li><b>Brut</b> – die Eiersäcke um sie herum zittern, dann schlüpfen Höhlenspinnen (höchstens {@value #MAX_BROOD});</li>
 *     <li><b>Sprung</b> – sie duckt sich und springt auf ihr Ziel; wer beim Aufprall nah steht, wird getroffen und vergiftet;</li>
 *     <li><b>Giftnebel</b> (nur in Rage) – grüner Dunst steigt auf, dann umhüllt sie eine Giftwolke.</li>
 * </ul>
 * Spinnweben halten sie nicht auf. Stirbt sie, vergeht auch ihre Brut.
 */
public class BroodMotherEntity extends Spider implements MiniBoss {
    public static final float MAX_HEALTH = 150.0f;
    public static final float SCALE = 2.3f;
    public static final int MAX_BROOD = 6;
    public static final float WEB_DAMAGE = 4.0f;
    public static final float POUNCE_DAMAGE = 9.0f;
    public static final double POUNCE_RADIUS = 3.2;
    public static final int VENOM_WINDUP = 20;
    public static final int COLOR = 0x5A2A6E;
    private static final EntityDataAccessor<Boolean> DATA_AWAKE = SynchedEntityData.defineId(BroodMotherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(BroodMotherEntity.class, EntityDataSerializers.BOOLEAN);
    /** Bäumt sie sich auf (Netzsalve, Brut; für das Bild)? */
    private static final EntityDataAccessor<Boolean> DATA_REARING = SynchedEntityData.defineId(BroodMotherEntity.class, EntityDataSerializers.BOOLEAN);

    private final MiniBossCore core;
    /** Im Client: wie weit sie sich aufbäumt (0..1, weich). */
    public float rear;
    public float rearO;

    public BroodMotherEntity(EntityType<? extends BroodMotherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.core = new MiniBossCore(this, BossEvent.BossBarColor.PURPLE)
                .special(new WebVolley())
                .special(new Brood())
                .special(new Pounce())
                .special(new Venom());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return BroodMotherEntity.this.isAwake() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return BroodMotherEntity.this.isAwake() && super.canUse();
            }
        });
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AWAKE, false);
        builder.define(DATA_ENRAGED, false);
        builder.define(DATA_REARING, false);
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
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(enraged ? 0.37 : 0.32);
        }
    }

    public boolean isRearing() {
        return this.entityData.get(DATA_REARING);
    }

    void setRearing(boolean rearing) {
        this.entityData.set(DATA_REARING, rearing);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.rearO = this.rear;
            this.rear += ((this.isRearing() ? 1.0f : 0.0f) - this.rear) * 0.25f;
            if (!this.isAwake() && this.random.nextInt(40) == 0) {
                // sie schläft: ab und zu zuckt ein Bein, Staub rieselt
                this.level().addParticle(ParticleTypes.WHITE_ASH, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 0.0, -0.02, 0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.core.tick(level);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), this);
        }
        return hit;
    }

    /** Keine Reiter, keine zufälligen Tränke (wie bei gewöhnlichen Spinnen). */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        return data;
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
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isAwake() ? SoundEvents.SPIDER_AMBIENT : null;
    }

    @Override
    public float getVoicePitch() {
        return 0.5f + this.random.nextFloat() * 0.08f;
    }

    @Override
    protected float getSoundVolume() {
        return 1.8f;
    }

    @Override
    public void onWake(ServerLevel level, @Nullable LivingEntity cause) {
        this.core.sound(SoundEvents.SPIDER_AMBIENT, 3.0f, 0.35f);
        this.core.sound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.2f, 0.6f);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.COBWEB), this.getX(), this.getY(0.8), this.getZ(), 40,
                this.getBbWidth() * 0.5, 0.6, this.getBbWidth() * 0.5, 0.15);
    }

    @Override
    public void onEnrage(ServerLevel level) {
        this.core.sound(SoundEvents.SPIDER_AMBIENT, 3.0f, 0.3f);
        level.sendParticles(new DustParticleOptions(0x7FD04F, 1.6f), this.getX(), this.getY(0.6), this.getZ(), 40, 1.4, 0.6, 1.4, 0.0);
    }

    /** Wo das Maul ist (vorn am Kopf, in Blickrichtung). */
    Vec3 mouth() {
        Vec3 look = Vec3.directionFromRotation(0.0f, this.yBodyRot);
        return this.position().add(look.scale(this.getBbWidth() * 0.55)).add(0.0, this.getBbHeight() * (this.isRearing() ? 0.75 : 0.5), 0.0);
    }

    // =====================================================================================
    // Künste
    // =====================================================================================

    /** Netzsalve: aufbäumen, dann drei (in Rage fünf) Netze im Fächer. */
    final class WebVolley extends MiniSpecial {
        WebVolley() {
            super("web", 30, 4);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance > 4.0 && distance < 22.0 && BroodMotherEntity.this.hasLineOfSight(target);
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            BroodMotherEntity.this.setRearing(true);
            BroodMotherEntity.this.core.sound(SoundEvents.SPIDER_AMBIENT, 2.0f, 0.45f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            BroodMotherEntity mother = BroodMotherEntity.this;
            mother.getLookControl().setLookAt(target, 30.0f, 30.0f);
            if (t < 16) {
                if (t % 3 == 0) {
                    Vec3 m = mother.mouth();
                    level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.COBWEB), m.x, m.y, m.z, 4, 0.3, 0.2, 0.3, 0.02);
                }
                return;
            }
            if (t == 16) {
                Vec3 m = mother.mouth();
                Vec3 aim = target.getEyePosition().subtract(m);
                int n = mother.isEnraged() ? 5 : 3;
                float spread = mother.isEnraged() ? 13.0f : 11.0f;
                for (int i = 0; i < n; i++) {
                    float yaw = (i - (n - 1) / 2.0f) * spread;
                    Vec3 dir = aim.yRot((float) Math.toRadians(yaw)).normalize();
                    // ein wenig höher gezielt: das Netz fällt im Bogen
                    Vec3 v = dir.scale(1.25).add(0.0, 0.04 * Math.min(aim.length(), 18.0) / 6.0, 0.0);
                    MonsterShot.shoot(level, mother, MonsterShot.Kind.WEB, m, v, WEB_DAMAGE);
                }
                mother.core.sound(SoundEvents.LLAMA_SPIT, 2.0f, 0.6f);
            }
            if (t == 22) {
                mother.setRearing(false);
            }
        }

        @Override
        public void stop(ServerLevel level) {
            BroodMotherEntity.this.setRearing(false);
        }

        @Override
        public int recharge(RandomSource random) {
            return 90 + random.nextInt(40);
        }
    }

    /** Brut: die Eiersäcke rings um sie zittern, dann schlüpfen Höhlenspinnen. */
    final class Brood extends MiniSpecial {
        private final List<Vec3> spots = new ArrayList<>();

        Brood() {
            super("brood", 40, 3);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return BroodMotherEntity.this.level() instanceof ServerLevel level && BroodMotherEntity.this.core.minionCount(level) <= MAX_BROOD - 3;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            BroodMotherEntity mother = BroodMotherEntity.this;
            mother.setRearing(true);
            mother.core.sound(SoundEvents.SPIDER_AMBIENT, 3.0f, 0.3f);
            mother.core.sound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0f, 0.8f);
            this.spots.clear();
            int n = Math.min(mother.isEnraged() ? 4 : 3, MAX_BROOD - mother.core.minionCount(level));
            RandomSource random = mother.getRandom();
            for (int i = 0; i < n * 4 && this.spots.size() < n; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = 2.5 + random.nextDouble() * 3.0;
                BlockPos p = BlockPos.containing(mother.getX() + Math.cos(a) * r, mother.getY() + 1.0, mother.getZ() + Math.sin(a) * r);
                for (int k = 0; k < 4 && level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty(); k++) {
                    p = p.below();
                }
                if (level.getBlockState(p).getCollisionShape(level, p).isEmpty() && !level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty()) {
                    this.spots.add(Vec3.atBottomCenterOf(p));
                }
            }
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            BroodMotherEntity mother = BroodMotherEntity.this;
            if (t < 25) {
                if (t % 2 == 0) {
                    for (Vec3 s : this.spots) {
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUSHROOM_STEM.defaultBlockState()), s.x, s.y + 0.3, s.z, 3,
                                0.25, 0.15, 0.25, 0.05);
                        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.COBWEB), s.x, s.y + 0.4, s.z, 1, 0.2, 0.2, 0.2,
                                0.02);
                    }
                }
                if (t == 12) {
                    mother.core.sound(SoundEvents.TURTLE_EGG_CRACK, 1.5f, 0.6f);
                }
                return;
            }
            if (t == 25) {
                for (Vec3 s : this.spots) {
                    Mob brood = ModEntities.CAVE_SPIDER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (brood == null) {
                        continue;
                    }
                    brood.snapTo(s.x, s.y, s.z, mother.getRandom().nextFloat() * 360.0f, 0.0f);
                    brood.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(s)), EntitySpawnReason.MOB_SUMMONED, null);
                    brood.setTarget(target);
                    level.addFreshEntity(brood);
                    mother.core.addMinion(brood);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUSHROOM_STEM.defaultBlockState()), s.x, s.y + 0.4, s.z, 16,
                            0.3, 0.3, 0.3, 0.12);
                }
                mother.core.sound(SoundEvents.TURTLE_EGG_HATCH, 2.0f, 0.7f);
                mother.core.sound(SoundEvents.SPIDER_STEP, 2.0f, 1.6f);
            }
            if (t == 30) {
                mother.setRearing(false);
            }
        }

        @Override
        public void stop(ServerLevel level) {
            BroodMotherEntity.this.setRearing(false);
        }

        @Override
        public int recharge(RandomSource random) {
            return 320 + random.nextInt(100);
        }
    }

    /** Sprung: ducken, dann auf das Ziel springen; der Aufprall trifft alle in der Nähe. */
    final class Pounce extends MiniSpecial {
        static final int WINDUP = 12;
        static final int FLIGHT = 14;
        private Vec3 landing = Vec3.ZERO;
        private boolean impact;

        Pounce() {
            super("pounce", 34, 4);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance > 4.0 && distance < 14.0 && BroodMotherEntity.this.onGround() && Math.abs(target.getY() - BroodMotherEntity.this.getY()) < 5.0;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            this.impact = false;
            BroodMotherEntity.this.core.sound(SoundEvents.SPIDER_AMBIENT, 2.0f, 0.7f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            BroodMotherEntity mother = BroodMotherEntity.this;
            if (t < WINDUP) {
                mother.getLookControl().setLookAt(target, 30.0f, 30.0f);
                BlockState ground = level.getBlockState(mother.getBlockPosBelowThatAffectsMyMovement());
                if (!ground.isAir() && t % 2 == 0) {
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), mother.getX(), mother.getY() + 0.1, mother.getZ(), 6,
                            mother.getBbWidth() * 0.4, 0.05, mother.getBbWidth() * 0.4, 0.05);
                }
                if (t == WINDUP - 1) {
                    this.landing = target.position();
                }
                return;
            }
            if (t == WINDUP) {
                Vec3 to = this.landing.subtract(mother.position()).multiply(1.0, 0.0, 1.0);
                double h = Math.max(0.0, to.length() - 1.0);
                Vec3 dir = to.lengthSqr() < 1.0e-4 ? Vec3.ZERO : to.normalize();
                double speed = Math.min(1.6, MiniBossCore.leap(mother, h, FLIGHT));
                mother.setDeltaMovement(dir.x * speed, 0.78, dir.z * speed);
                mother.needsSync = true;
                mother.core.sound(SoundEvents.SPIDER_AMBIENT, 2.0f, 0.9f);
                return;
            }
            if (!this.impact && (t > WINDUP + 3 && mother.onGround() || t >= this.duration() - 2)) {
                this.impact = true;
                this.land(level);
            }
        }

        private void land(ServerLevel level) {
            BroodMotherEntity mother = BroodMotherEntity.this;
            Vec3 c = mother.position();
            BlockState ground = level.getBlockState(mother.getBlockPosBelowThatAffectsMyMovement());
            if (!ground.isAir()) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), c.x, c.y + 0.1, c.z, 50, 1.6, 0.1, 1.6, 0.2);
            }
            SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.05, 0.0), null, COLOR, 10, 0.8f).param(0.28f).spawn();
            mother.core.sound(SoundEvents.RAVAGER_STEP, 2.5f, 0.6f);
            mother.core.sound(SoundEvents.SPIDER_HURT, 1.5f, 0.5f);
            for (LivingEntity victim : mother.core.foesAround(c, POUNCE_RADIUS, 1.5, 3.0)) {
                if (mother.core.hurt(level, victim, POUNCE_DAMAGE)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1), mother);
                    MiniBossCore.knock(victim, c, 0.9, 0.45);
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 110 + random.nextInt(40);
        }
    }

    /** Giftnebel (nur in Rage): grüner Dunst steigt auf, dann eine Giftwolke um sie. */
    final class Venom extends MiniSpecial {
        Venom() {
            super("venom", 30, 3);
        }

        @Override
        public boolean enragedOnly() {
            return true;
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < 9.0;
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            BroodMotherEntity mother = BroodMotherEntity.this;
            if (t < VENOM_WINDUP) {
                if (t % 2 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double a = mother.getRandom().nextDouble() * Math.PI * 2.0;
                        double r = 1.0 + mother.getRandom().nextDouble() * 3.5;
                        level.sendParticles(new DustParticleOptions(0x7FD04F, 1.3f), mother.getX() + Math.cos(a) * r, mother.getY() + 0.2,
                                mother.getZ() + Math.sin(a) * r, 1, 0.0, 0.08, 0.0, 0.0);
                    }
                }
                if (t == 0) {
                    mother.core.sound(SoundEvents.PUFFER_FISH_BLOW_UP, 2.0f, 0.5f);
                }
                return;
            }
            if (t == VENOM_WINDUP) {
                AreaEffectCloud cloud = new AreaEffectCloud(level, mother.getX(), mother.getY(), mother.getZ());
                cloud.setOwner(mother);
                cloud.setRadius(4.5f);
                cloud.setDuration(120);
                cloud.setWaitTime(0);
                cloud.setRadiusPerTick(-0.015f);
                cloud.setCustomParticle(new DustParticleOptions(0x7FD04F, 1.0f));
                cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                level.addFreshEntity(cloud);
                mother.core.sound(SoundEvents.PUFFER_FISH_BLOW_OUT, 2.5f, 0.5f);
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 240 + random.nextInt(60);
        }
    }

    /** Für das Bild: wie weit sie sich gerade aufbäumt. */
    public float rear(float partialTick) {
        return Mth.lerp(partialTick, this.rearO, this.rear);
    }
}
