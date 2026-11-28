package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
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
 * Morvan, der Gruftkönig (seit 1.15): ein uralter König, der am Ende der Gruft der Vergessenen vor seinem Thron wacht –
 * Knochen mit dunkler Patina, eine Goldkrone, ein zerschlissener Königsmantel, eisblau glühende Augen und ein Schwert
 * aus Nachtstahl. Er kennt vier Künste:
 * <ul>
 *     <li><b>Totenruf</b> – Seelenflammen züngeln an zwei, drei Stellen aus dem Boden, dann erheben sich dort
 *     Gruftwächter (Gerippe mit Schwert);</li>
 *     <li><b>Seelenschnitt</b> – seine Klinge glimmt eisblau, dann ein weiter Hieb nach vorn, der verdorren lässt;</li>
 *     <li><b>Gruftkälte</b> – ein Warnkreis um ihn, dann bricht eisige Seelenkälte aus dem Boden;</li>
 *     <li><b>Knochenpfad</b> (nur in Rage) – ein Riss läuft auf sein Ziel zu, dann brechen Knochen aus ihm hervor.</li>
 * </ul>
 */
public class CryptKingEntity extends AbstractSkeleton implements MiniBoss {
    public static final float MAX_HEALTH = 170.0f;
    public static final float SCALE = 1.3f;
    public static final int MAX_GUARDS = 4;
    public static final float CLEAVE_DAMAGE = 12.0f;
    public static final double CLEAVE_RANGE = 4.5;
    /** Halber Öffnungswinkel des Seelenschnitts (Grad). */
    public static final double CLEAVE_ANGLE = 55.0;
    public static final float CHILL_DAMAGE = 7.0f;
    public static final double CHILL_RADIUS = 5.5;
    public static final int CHILL_WINDUP = 20;
    public static final float BONE_DAMAGE = 8.0f;
    public static final int SOUL = 0x7FE8FF;
    private static final EntityDataAccessor<Boolean> DATA_AWAKE = SynchedEntityData.defineId(CryptKingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED = SynchedEntityData.defineId(CryptKingEntity.class, EntityDataSerializers.BOOLEAN);

    private final MiniBossCore core;

    public CryptKingEntity(EntityType<? extends CryptKingEntity> type, Level level) {
        super(type, level);
        // die Größe (SCALE) steht schon in den Grundwerten – der Rahmen wächst aber erst, wenn man ihn neu bemisst
        this.refreshDimensions();
        this.xpReward = 120;
        this.setPersistenceRequired();
        this.core = new MiniBossCore(this, BossEvent.BossBarColor.BLUE)
                .special(new RaiseDead())
                .special(new SoulCleave())
                .special(new CryptChill())
                .special(new BonePath());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.SCALE, SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, true) {
            @Override
            public boolean canUse() {
                return CryptKingEntity.this.isAwake() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
            @Override
            public boolean canUse() {
                return CryptKingEntity.this.isAwake() && super.canUse();
            }
        });
    }

    /** Kein Bogen: er kämpft immer mit seiner Klinge (die eigene Nahkampf-Absicht steht in {@link #registerGoals}). */
    @Override
    public void reassessWeaponGoal() {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AWAKE, false);
        builder.define(DATA_ENRAGED, false);
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

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ShadowItems.NIGHTSTEEL_SWORD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.12f);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData data) {
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.setCanPickUpLoot(false);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.isAwake() && this.random.nextInt(this.isEnraged() ? 2 : 5) == 0) {
            // Seelenlicht umweht ihn
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getRandomX(0.6), this.getY(0.2 + this.random.nextDouble() * 0.6),
                    this.getRandomZ(0.6), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.core.tick(level);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && this.isEnraged() && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), this);
        }
        return hit;
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
    protected SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isAwake() ? SoundEvents.SKELETON_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.55f + this.random.nextFloat() * 0.08f;
    }

    @Override
    public void onWake(ServerLevel level, @Nullable LivingEntity cause) {
        this.core.sound(SoundEvents.SKELETON_AMBIENT, 2.5f, 0.4f);
        this.core.sound(SoundEvents.SOUL_ESCAPE.value(), 2.0f, 0.6f);
        level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY(0.6), this.getZ(), 30, 0.6, 1.0, 0.6, 0.04);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(0.3), this.getZ(), 40, 1.2, 0.2, 1.2, 0.02);
    }

    @Override
    public void onEnrage(ServerLevel level) {
        this.core.sound(SoundEvents.WITHER_AMBIENT, 1.6f, 0.7f);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(0.5), this.getZ(), 60, 1.0, 1.0, 1.0, 0.06);
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.31);
        }
    }

    @Override
    public void onSleep(ServerLevel level) {
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.27);
        }
    }

    /** Die Spitze seiner Klinge (vor ihm, in Hüfthöhe). */
    Vec3 blade() {
        Vec3 look = Vec3.directionFromRotation(0.0f, this.yBodyRot);
        Vec3 side = look.yRot((float) Math.toRadians(-90.0));
        return this.position().add(look.scale(0.9)).add(side.scale(0.45)).add(0.0, this.getBbHeight() * 0.55, 0.0);
    }

    // =====================================================================================
    // Künste
    // =====================================================================================

    /** Totenruf: Seelenflammen an zwei, drei Stellen, dann erheben sich dort Gruftwächter. */
    final class RaiseDead extends MiniSpecial {
        private final List<Vec3> spots = new ArrayList<>();

        RaiseDead() {
            super("raise", 40, 3);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return CryptKingEntity.this.level() instanceof ServerLevel level && CryptKingEntity.this.core.minionCount(level) <= MAX_GUARDS - 2;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            CryptKingEntity king = CryptKingEntity.this;
            king.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            king.core.sound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.6f, 0.55f);
            king.core.sound(SoundEvents.SKELETON_AMBIENT, 2.0f, 0.4f);
            this.spots.clear();
            int n = Math.min(king.isEnraged() ? 3 : 2, MAX_GUARDS - king.core.minionCount(level));
            RandomSource random = king.getRandom();
            for (int i = 0; i < n * 5 && this.spots.size() < n; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = 2.5 + random.nextDouble() * 2.5;
                Vec3 at = floor(level, new Vec3(king.getX() + Math.cos(a) * r, king.getY() + 1.0, king.getZ() + Math.sin(a) * r));
                if (at != null) {
                    this.spots.add(at);
                }
            }
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            CryptKingEntity king = CryptKingEntity.this;
            if (t < 28) {
                if (t % 2 == 0) {
                    for (Vec3 s : this.spots) {
                        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, s.x, s.y + 0.1, s.z, 3, 0.25, 0.05, 0.25, 0.03);
                        level.sendParticles(ParticleTypes.SOUL, s.x, s.y + 0.3, s.z, 1, 0.2, 0.2, 0.2, 0.02);
                        BlockState under = level.getBlockState(BlockPos.containing(s).below());
                        if (!under.isAir() && t % 6 == 0) {
                            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, under), s.x, s.y + 0.1, s.z, 6, 0.3, 0.05, 0.3, 0.08);
                        }
                    }
                }
                return;
            }
            if (t == 28) {
                for (Vec3 s : this.spots) {
                    Boneman guard = ModEntities.SKELETON.get().create(level, EntitySpawnReason.MOB_SUMMONED);
                    if (guard == null) {
                        continue;
                    }
                    guard.snapTo(s.x, s.y, s.z, king.getRandom().nextFloat() * 360.0f, 0.0f);
                    guard.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(s)), EntitySpawnReason.MOB_SUMMONED, null);
                    guard.setLook(Boneman.Look.RITTER);
                    guard.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    guard.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
                    guard.setCanPickUpLoot(false);
                    guard.reassessWeaponGoal();
                    guard.setTarget(target);
                    level.addFreshEntity(guard);
                    king.core.addMinion(guard);
                    level.sendParticles(ParticleTypes.SOUL, s.x, s.y + 0.8, s.z, 14, 0.3, 0.6, 0.3, 0.05);
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()), s.x, s.y + 0.2, s.z, 20, 0.35,
                            0.2, 0.35, 0.1);
                }
                king.core.sound(SoundEvents.ZOMBIE_VILLAGER_CURE, 1.0f, 0.5f);
                king.core.sound(SoundEvents.SKELETON_STEP, 2.0f, 0.6f);
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 380 + random.nextInt(80);
        }
    }

    /** Seelenschnitt: die Klinge glimmt, dann ein weiter Hieb nach vorn. */
    final class SoulCleave extends MiniSpecial {
        static final int WINDUP = 14;

        SoulCleave() {
            super("cleave", 26, 5);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < CLEAVE_RANGE;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            CryptKingEntity.this.core.sound(SoundEvents.SOUL_ESCAPE.value(), 2.0f, 1.2f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            CryptKingEntity king = CryptKingEntity.this;
            if (t < WINDUP) {
                king.getLookControl().setLookAt(target, 30.0f, 30.0f);
                king.setYBodyRot(king.getYHeadRot());
                Vec3 b = king.blade();
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, b.x, b.y, b.z, 2, 0.15, 0.25, 0.15, 0.01);
                if (t % 3 == 0) {
                    // der Bogen am Boden zeigt, wohin der Hieb geht
                    float yaw = king.yBodyRot;
                    for (int i = -3; i <= 3; i++) {
                        double a = Math.toRadians(yaw + i * CLEAVE_ANGLE / 3.0);
                        double r = CLEAVE_RANGE - 0.5;
                        level.sendParticles(new DustParticleOptions(SOUL, 1.1f), king.getX() - Math.sin(a) * r, king.getY() + 0.1, king.getZ() + Math.cos(a) * r,
                                1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                return;
            }
            if (t == WINDUP) {
                king.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
                king.core.sound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.6f);
                king.core.sound(SoundEvents.SOUL_ESCAPE.value(), 2.0f, 0.5f);
                Vec3 look = Vec3.directionFromRotation(0.0f, king.yBodyRot);
                for (int i = -2; i <= 2; i++) {
                    Vec3 d = look.yRot((float) Math.toRadians(i * 25.0)).scale(2.0);
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, king.getX() + d.x, king.getY(0.45), king.getZ() + d.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
                for (LivingEntity victim : king.core.foesAround(king.position(), CLEAVE_RANGE + king.getBbWidth() * 0.5, 1.0, 3.5)) {
                    Vec3 to = victim.position().subtract(king.position()).multiply(1.0, 0.0, 1.0);
                    double angle = to.lengthSqr() < 1.0e-4 ? 0.0 : Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(look), -1.0, 1.0)));
                    if (angle > CLEAVE_ANGLE) {
                        continue;
                    }
                    if (king.core.hurt(level, victim, CLEAVE_DAMAGE)) {
                        victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0), king);
                        MiniBossCore.knock(victim, king.position(), 0.8, 0.3);
                    }
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 70 + random.nextInt(30);
        }
    }

    /** Gruftkälte: ein Warnkreis um ihn, dann bricht eisige Seelenkälte aus dem Boden. */
    final class CryptChill extends MiniSpecial {
        CryptChill() {
            super("chill", 30, 4);
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance < CHILL_RADIUS + 0.5;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            CryptKingEntity king = CryptKingEntity.this;
            SpellFxEntity.create(level, SpellFxEntity.Kind.CIRCLE, king.position().add(0.0, 0.03, 0.0), null, SOUL, CHILL_WINDUP, (float) CHILL_RADIUS)
                    .spawn();
            king.core.sound(SoundEvents.SOUL_ESCAPE.value(), 2.5f, 0.7f);
            king.core.sound(SoundEvents.POWDER_SNOW_STEP, 2.0f, 0.5f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            CryptKingEntity king = CryptKingEntity.this;
            if (t < CHILL_WINDUP) {
                if (t % 2 == 0) {
                    for (int i = 0; i < 5; i++) {
                        double a = king.getRandom().nextDouble() * Math.PI * 2.0;
                        double r = king.getRandom().nextDouble() * CHILL_RADIUS;
                        level.sendParticles(ParticleTypes.SNOWFLAKE, king.getX() + Math.cos(a) * r, king.getY() + 0.2, king.getZ() + Math.sin(a) * r, 1,
                                0.0, 0.05, 0.0, 0.0);
                    }
                }
                return;
            }
            if (t == CHILL_WINDUP) {
                Vec3 c = king.position();
                SpellFxEntity.create(level, SpellFxEntity.Kind.SHOCKWAVE, c.add(0.0, 0.05, 0.0), null, SOUL, 12, 0.8f).param(0.42f).spawn();
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y + 0.3, c.z, 80, CHILL_RADIUS * 0.5, 0.3, CHILL_RADIUS * 0.5, 0.04);
                level.sendParticles(ParticleTypes.SNOWFLAKE, c.x, c.y + 0.6, c.z, 60, CHILL_RADIUS * 0.5, 0.6, CHILL_RADIUS * 0.5, 0.05);
                king.core.sound(SoundEvents.GLASS_BREAK, 2.0f, 0.5f);
                king.core.sound(SoundEvents.SOUL_ESCAPE.value(), 2.5f, 0.4f);
                for (LivingEntity victim : king.core.foesAround(c, CHILL_RADIUS, 1.5, 3.0)) {
                    if (king.core.hurt(level, victim, CHILL_DAMAGE)) {
                        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1), king);
                        victim.setTicksFrozen(Math.min(victim.getTicksRequiredToFreeze() + 60, victim.getTicksFrozen() + 140));
                    }
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 120 + random.nextInt(40);
        }
    }

    /** Knochenpfad (nur in Rage): ein Riss läuft auf das Ziel zu, dann brechen Knochen aus ihm hervor. */
    final class BonePath extends MiniSpecial {
        static final int OPEN = 14;
        private Vec3 dir = Vec3.ZERO;
        private Vec3 from = Vec3.ZERO;
        private final IntSet struck = new IntOpenHashSet();

        BonePath() {
            super("bones", 36, 4);
        }

        @Override
        public boolean enragedOnly() {
            return true;
        }

        @Override
        public boolean ready(LivingEntity target, double distance) {
            return distance > 3.0 && distance < 16.0;
        }

        @Override
        public void start(ServerLevel level, LivingEntity target) {
            CryptKingEntity king = CryptKingEntity.this;
            Vec3 to = target.position().subtract(king.position()).multiply(1.0, 0.0, 1.0);
            this.dir = to.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0.0f, king.yBodyRot) : to.normalize();
            this.from = king.position();
            this.struck.clear();
            king.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
            SpellFxEntity.create(level, SpellFxEntity.Kind.FISSURE, this.from.add(this.dir.scale(1.2)).add(0.0, 0.03, 0.0), null, SOUL, 34, 0.7f)
                    .param(3.0f).end(this.from.add(this.dir.scale(17.0))).spawn();
            king.core.sound(SoundEvents.WARDEN_DIG, 1.6f, 0.9f);
        }

        @Override
        public void tick(ServerLevel level, LivingEntity target, int t) {
            CryptKingEntity king = CryptKingEntity.this;
            int k = (t - OPEN) / 2;
            if (t < OPEN || (t - OPEN) % 2 != 0 || k > 7) {
                return;
            }
            Vec3 p = this.from.add(this.dir.scale(2.0 + 2.0 * k));
            Vec3 g = floor(level, p.add(0.0, 2.0, 0.0));
            if (g == null) {
                return;
            }
            SpellFxEntity.create(level, SpellFxEntity.Kind.SPIKE, g, null, 0xE8E0C8, 18, 0.9f).spawn();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()), g.x, g.y + 0.4, g.z, 10, 0.4, 0.3, 0.4,
                    0.1);
            if (k % 2 == 0) {
                king.core.sound(SoundEvents.SKELETON_HURT, 1.4f, 0.6f);
            }
            for (LivingEntity victim : king.core.foesAround(g, 1.4, 1.5, 3.0)) {
                if (this.struck.add(victim.getId()) && king.core.hurt(level, victim, BONE_DAMAGE)) {
                    victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 0.6, 0.0));
                    victim.needsSync = true;
                }
            }
        }

        @Override
        public int recharge(RandomSource random) {
            return 160 + random.nextInt(40);
        }
    }

    /** Der Boden unter einem Punkt (höchstens vier Blöcke tiefer), oder null. */
    static @Nullable Vec3 floor(ServerLevel level, Vec3 at) {
        BlockPos p = BlockPos.containing(at);
        for (int i = 0; i < 6; i++) {
            BlockPos below = p.below();
            if (level.getBlockState(p).getCollisionShape(level, p).isEmpty() && !level.getBlockState(below).getCollisionShape(level, below).isEmpty()) {
                return new Vec3(at.x, p.getY(), at.z);
            }
            p = below;
        }
        return null;
    }
}
