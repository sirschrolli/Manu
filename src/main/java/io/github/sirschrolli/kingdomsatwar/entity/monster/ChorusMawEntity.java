package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Chorusschlund: eine fleischfressende Choruspflanze der äußeren End-Inseln. Sie wurzelt (läuft nicht) und sieht
 * geschlossen aus wie jede andere Chorusblüte – kommt Beute heran, klappt der Schlund auf. Aus der Nähe schnappt sie zu,
 * bis gut vier Blöcke weit peitscht sie mit ihren Ranken und zieht ihr Opfer heran; weiter weg spuckt sie Chorussamen:
 * Wer getroffen wird, verschwindet wie nach einer Chorusfrucht an eine zufällige Stelle in der Nähe (und ihm wird übel).
 * Schwer verwundet verpflanzt sie sich selbst ein Stück weiter.
 */
public class ChorusMawEntity extends Monster {
    public static final byte EVENT_LASH_LEFT = 94;
    public static final byte EVENT_LASH_RIGHT = 95;
    public static final byte EVENT_BITE = 96;
    public static final byte EVENT_SPIT = 97;
    public static final int LASH_TICKS = 14;
    public static final int LASH_HIT = 6;
    public static final double LASH_REACH = 4.8;
    public static final int BITE_TICKS = 12;
    public static final int BITE_HIT = 5;
    public static final double BITE_REACH = 2.6;
    public static final int SPIT_TICKS = 16;
    public static final int SPIT_RELEASE = 8;
    /** Ab dieser Nähe öffnet sie den Schlund. */
    public static final double WAKE_RANGE = 14.0;
    private static final EntityDataAccessor<Boolean> DATA_OPEN = SynchedEntityData.defineId(ChorusMawEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState lashLeftAnimation = new AnimationState();
    public final AnimationState lashRightAnimation = new AnimationState();
    public final AnimationState biteAnimation = new AnimationState();
    public final AnimationState spitAnimation = new AnimationState();
    private int lashTicks;
    private int biteTicks;
    private int spitTicks;
    private int lashCooldown = 20;
    private int biteCooldown = 10;
    private int spitCooldown = 50;
    private int replantCooldown = 100;
    private boolean leftNext;

    public ChorusMawEntity(EntityType<? extends ChorusMawEntity> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 18.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OPEN, false);
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target.distanceToSqr(this) < WAKE_RANGE * WAKE_RANGE));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SoldierEntity.class, 10, true, false,
                (target, level) -> target.distanceToSqr(this) < WAKE_RANGE * WAKE_RANGE));
    }

    /** Ist der Schlund offen? */
    public boolean isOpen() {
        return this.entityData.get(DATA_OPEN);
    }

    /** Für die Vorführung: den Schlund öffnen oder schließen. */
    public void forceOpen(boolean open) {
        this.entityData.set(DATA_OPEN, open);
    }

    /** Für Tests und die Vorführung: sofort mit einer Ranke peitschen. */
    public void forceLash() {
        this.startLash();
    }

    /** Für Tests und die Vorführung: sofort zuschnappen. */
    public void forceBite() {
        this.startBite();
    }

    /** Für Tests und die Vorführung: sofort einen Chorussamen spucken. */
    public void forceSpit() {
        this.startSpit();
    }

    /** Für Tests: sich sofort verpflanzen. Gibt zurück, ob sie einen Platz gefunden hat. */
    public boolean forceReplant() {
        return this.level() instanceof ServerLevel level && this.replant(level);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
        // sie wurzelt
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.lashCooldown > 0) {
            this.lashCooldown--;
        }
        if (this.biteCooldown > 0) {
            this.biteCooldown--;
        }
        if (this.spitCooldown > 0) {
            this.spitCooldown--;
        }
        if (this.replantCooldown > 0) {
            this.replantCooldown--;
        }
        LivingEntity target = this.getTarget();
        boolean awake = target != null && target.isAlive() && this.distanceToSqr(target) < WAKE_RANGE * WAKE_RANGE;
        if (!this.isNoAi() && awake != this.isOpen()) {
            this.entityData.set(DATA_OPEN, awake);
            this.playSound(ModSounds.CHORUS_MAW_OPEN.get(), 1.4f, awake ? 1.0f : 0.7f);
        }
        if (target != null && awake) {
            this.turnTo(target);
        }
        if (this.lashTicks > 0) {
            this.lashTicks--;
            if (this.lashTicks == LASH_TICKS - LASH_HIT) {
                this.lash(level, target);
            }
            return;
        }
        if (this.biteTicks > 0) {
            this.biteTicks--;
            if (this.biteTicks == BITE_TICKS - BITE_HIT) {
                this.bite(level, target);
            }
            return;
        }
        if (this.spitTicks > 0) {
            this.spitTicks--;
            if (this.spitTicks == SPIT_TICKS - SPIT_RELEASE) {
                this.spit(level, target);
            }
            return;
        }
        if (!awake || this.isNoAi()) {
            return;
        }
        double d = this.distanceTo(target);
        if (d < BITE_REACH && this.biteCooldown == 0) {
            this.startBite();
        } else if (d < LASH_REACH && this.lashCooldown == 0) {
            this.startLash();
        } else if (d >= 4.5 && d <= 16.0 && this.spitCooldown == 0 && this.hasLineOfSight(target)) {
            this.startSpit();
        }
    }

    private void turnTo(Entity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 12.0f));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
    }

    private void startLash() {
        this.lashTicks = LASH_TICKS;
        this.lashCooldown = 24 + this.random.nextInt(12);
        this.leftNext = !this.leftNext;
        this.level().broadcastEntityEvent(this, this.leftNext ? EVENT_LASH_LEFT : EVENT_LASH_RIGHT);
    }

    /** Die Ranke peitscht: Schaden, und das Opfer wird ein Stück herangezogen. */
    private void lash(ServerLevel level, @Nullable LivingEntity target) {
        this.playSound(ModSounds.CHORUS_MAW_LASH.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        if (target == null || !target.isAlive() || this.distanceTo(target) > LASH_REACH + 0.4) {
            return;
        }
        if (target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {
            Vec3 pull = this.position().subtract(target.position()).multiply(1.0, 0.0, 1.0);
            pull = pull.lengthSqr() < 1.0e-4 ? Vec3.ZERO : pull.normalize().scale(0.9);
            target.push(pull.x, 0.22, pull.z);
            target.needsSync = true;
            level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY(0.5), target.getZ(), 12, 0.3, 0.4, 0.3, 0.3);
        }
    }

    private void startBite() {
        this.biteTicks = BITE_TICKS;
        this.biteCooldown = 22 + this.random.nextInt(10);
        this.level().broadcastEntityEvent(this, EVENT_BITE);
    }

    private void bite(ServerLevel level, @Nullable LivingEntity target) {
        this.playSound(ModSounds.CHORUS_MAW_BITE.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
        if (target != null && target.isAlive() && this.distanceTo(target) <= BITE_REACH + 0.5) {
            target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + 2.0f);
        }
    }

    private void startSpit() {
        this.spitTicks = SPIT_TICKS;
        this.spitCooldown = 70 + this.random.nextInt(40);
        this.level().broadcastEntityEvent(this, EVENT_SPIT);
    }

    /** Ein Chorussame im Bogen: wer getroffen wird, verschwindet an eine zufällige Stelle in der Nähe. */
    private void spit(ServerLevel level, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }
        Vec3 from = this.position().add(0.0, 2.0, 0.0).add(this.getViewVector(1.0f).multiply(1.0, 0.0, 1.0).normalize().scale(0.6));
        Vec3 to = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).add(target.getDeltaMovement().scale(6.0));
        int ticks = (int) Mth.clamp(from.distanceTo(to) * 1.6, 8.0, 26.0);
        MonsterShot.shoot(level, this, MonsterShot.Kind.CHORUS, from, MonsterShot.lob(from, to, ticks, 0.04), 3.0f);
        this.playSound(ModSounds.CHORUS_MAW_SPIT.get(), 1.4f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    /**
     * Teleportiert wie nach einer Chorusfrucht an eine zufällige, sichere Stelle (sechzehn Versuche im Würfel mit der
     * Kantenlänge {@code diameter}). Gibt zurück, ob es geklappt hat.
     */
    public static boolean chorusTeleport(ServerLevel level, LivingEntity e, double diameter) {
        Vec3 from = e.position();
        for (int i = 0; i < 16; i++) {
            double x = e.getX() + (e.getRandom().nextDouble() - 0.5) * diameter;
            double y = Mth.clamp(e.getY() + (e.getRandom().nextDouble() - 0.5) * diameter, level.getMinY(),
                    level.getMinY() + level.getLogicalHeight() - 1);
            double z = e.getZ() + (e.getRandom().nextDouble() - 0.5) * diameter;
            if (e.isPassenger()) {
                e.stopRiding();
            }
            if (e.randomTeleport(x, y, z, true, BlockTags.CONSUMABLE_DOES_NOT_TELEPORT_TO)) {
                level.gameEvent(GameEvent.TELEPORT, from, GameEvent.Context.of(e));
                level.playSound(null, from.x, from.y, from.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.0f);
                e.resetFallDistance();
                return true;
            }
        }
        return false;
    }

    /**
     * Verpflanzt sich selbst: sucht eine Stelle mit festem Boden bis zu sechs Blöcke weiter (von etwas oberhalb sinkt sie
     * auf den Boden – so findet sie auch Hänge, die höher liegen).
     */
    private boolean replant(ServerLevel level) {
        Vec3 from = this.position();
        for (int i = 0; i < 16; i++) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 12.0;
            double y = this.getY() + this.random.nextInt(7);
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 12.0;
            if (this.randomTeleport(x, y, z, false, BlockTags.CONSUMABLE_DOES_NOT_TELEPORT_TO)) {
                this.replantCooldown = 300;
                this.playSound(ModSounds.CHORUS_MAW_TELEPORT.get(), 1.6f, 1.0f);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, from.x, from.y + 1.0, from.z, 40, 0.5, 0.8, 0.5, 0.2);
                level.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.5, 0.8, 0.5, 0.4);
                level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, Blocks.CHORUS_PLANT.defaultBlockState()),
                        from.x, from.y + 0.3, from.z, 16, 0.4, 0.2, 0.4, 0.1);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.isAlive() && this.getHealth() < this.getMaxHealth() * 0.5f && this.replantCooldown == 0 && this.random.nextInt(2) == 0) {
            this.replant(level);
        }
        return hurt;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_LASH_LEFT -> this.lashLeftAnimation.start(this.tickCount);
            case EVENT_LASH_RIGHT -> this.lashRightAnimation.start(this.tickCount);
            case EVENT_BITE -> this.biteAnimation.start(this.tickCount);
            case EVENT_SPIT -> this.spitAnimation.start(this.tickCount);
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level.isClientSide() && this.isOpen() && this.random.nextInt(4) == 0) {
            // aus dem offenen Schlund steigen violette Funken
            level.addParticle(ParticleTypes.PORTAL, this.getRandomX(0.4), this.getY() + 2.0, this.getRandomZ(0.4), 0.0, 0.1, 0.0);
        }
    }

    /** Auf den äußeren End-Inseln zwischen den Chorusbäumen; nie mehr als drei auf dreißig Blöcke. */
    public static boolean checkMawSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL || !checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return level.getBlockState(pos.below()).is(Blocks.END_STONE)
                && level.getEntitiesOfClass(ChorusMawEntity.class, new AABB(pos).inflate(30.0)).size() < 3;
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.CHORUS_MAW_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CHORUS_MAW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CHORUS_MAW_DEATH.get();
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
        // wurzelt fest
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ReplantCooldown", this.replantCooldown);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        this.replantCooldown = input.getIntOr("ReplantCooldown", 0);
    }

    @Override
    protected void doPush(Entity entity) {
        // schiebt nicht und wird nicht geschoben
    }

    /** Für die Effekte: Übelkeit nach dem Chorussprung. */
    static void dizzy(LivingEntity e, @Nullable Entity source) {
        e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0), source);
    }
}
