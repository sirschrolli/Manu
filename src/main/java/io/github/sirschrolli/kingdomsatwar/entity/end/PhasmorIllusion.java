package io.github.sirschrolli.kingdomsatwar.entity.end;

import io.github.sirschrolli.kingdomsatwar.item.end.VoidwalkerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Trugbild Phasmors (seit 1.5): ein Abbild des Leerwandlerkönigs, das er aus der Leere ruft, um sich unter seinesgleichen
 * zu verbergen. Es sieht aus wie er – nur ein wenig durchscheinend – und schlägt mit der Klinge nach seinen Gegnern, aber
 * schwach. Beim ersten Treffer, im Wasser und nach {@link #LIFE} Ticks zerfällt es zu türkisem Staub; es lässt nichts
 * zurück. Stirbt der König, vergehen auch seine Trugbilder.
 */
public class PhasmorIllusion extends Monster implements PhasmorLike {
    /** So lange (Ticks) hält ein Trugbild. */
    public static final int LIFE = 300;
    public static final float SLASH_DAMAGE = 4.0f;
    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(PhasmorIllusion.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CROWNED = SynchedEntityData.defineId(PhasmorIllusion.class, EntityDataSerializers.BOOLEAN);

    private @Nullable UUID kingId;
    private int life;
    private int attackTick;
    private int cooldown = 20;
    private int clientAttackStart;

    public PhasmorIllusion(EntityType<? extends PhasmorIllusion> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.ATTACK_DAMAGE, SLASH_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACK, PhasmorEntity.Attack.NONE.ordinal());
        builder.define(DATA_CROWNED, false);
    }

    /** Gehört zu diesem König (gekrönt sieht es gekrönt aus). */
    public void bind(PhasmorEntity king, boolean crowned) {
        this.kingId = king.getUUID();
        this.entityData.set(DATA_CROWNED, crowned);
    }

    /** Der König, zu dem es gehört (oder null). */
    public @Nullable PhasmorEntity king() {
        if (this.kingId != null && this.level() instanceof ServerLevel level && level.getEntity(this.kingId) instanceof PhasmorEntity king) {
            return king;
        }
        return null;
    }

    @Override
    public int attackOrdinal() {
        return this.entityData.get(DATA_ATTACK);
    }

    @Override
    public float attackTime(float partialTicks) {
        return this.tickCount - this.clientAttackStart + partialTicks;
    }

    @Override
    public boolean isCrowned() {
        return this.entityData.get(DATA_CROWNED);
    }

    @Override
    public boolean isIllusion() {
        return true;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ATTACK.equals(key)) {
            this.clientAttackStart = this.tickCount;
        }
    }

    // ------------------------------------------------------------------ Grundverhalten

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    /** Trugbilder überdauern kein Neuladen der Welt. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return true;
    }

    /** Jeder Treffer (und Wasser) lässt es zerfallen. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(level, source, amount);
        }
        this.dispel();
        return true;
    }

    /** Zerfällt zu türkisem Staub (mit einem leisen Klang). */
    public void dispel() {
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }
        level.sendParticles(new DustParticleOptions(PhasmorEntity.PHASIT, 1.4f), this.getX(), this.getY(0.5), this.getZ(), 60, 0.4, 1.4, 0.4,
                0.0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY(0.5), this.getZ(), 30, 0.4, 1.4, 0.4, 0.05);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.PHASMOR_ILLUSION.get(), SoundSource.HOSTILE, 1.4f,
                1.5f + this.random.nextFloat() * 0.2f);
        this.discard();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        PhasmorEntity king = this.king();
        if (++this.life > LIFE || king == null || !king.isAlive() || this.isInWater()) {
            this.dispel();
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !king.canStrike(target) || target.distanceToSqr(this) > 40.0 * 40.0) {
            target = king.getTarget();
            this.setTarget(target);
        }
        if (target == null) {
            this.getNavigation().stop();
            return;
        }
        this.getLookControl().setLookAt(target, 30.0f, 30.0f);
        PhasmorEntity.Attack attack = PhasmorEntity.Attack.values()[this.attackOrdinal()];
        if (attack == PhasmorEntity.Attack.NONE) {
            if (this.distanceTo(target) > 3.2) {
                this.getNavigation().moveTo(target, 1.0);
            } else {
                this.getNavigation().stop();
            }
            if (--this.cooldown <= 0 && this.distanceTo(target) < 4.0) {
                this.attackTick = 0;
                this.entityData.set(DATA_ATTACK, PhasmorEntity.Attack.SLASH.ordinal());
                this.getNavigation().stop();
            }
            return;
        }
        this.attackTick++;
        this.lookAt(target, 40.0f, 40.0f);
        if (this.attackTick == PhasmorEntity.SLASH_HIT) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.PHASMOR_SWING.get(), SoundSource.HOSTILE, 1.2f, 1.2f);
            Vec3 to = target.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.length() < 4.2 && king.canStrike(target)) {
                target.hurtServer(level, this.damageSources().mobAttack(this), SLASH_DAMAGE);
            }
        }
        if (this.attackTick >= PhasmorEntity.Attack.SLASH.duration) {
            this.entityData.set(DATA_ATTACK, PhasmorEntity.Attack.NONE.ordinal());
            this.cooldown = 30 + this.random.nextInt(25);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
            this.level().addParticle(new DustParticleOptions(PhasmorEntity.PHASIT, 0.6f), this.getRandomX(0.6), this.getRandomY(),
                    this.getRandomZ(0.6), 0.0, 0.01, 0.0);
        }
    }

    /** Seine Trugbilder greifen ihn nicht an (und er sie nicht). */
    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) && !(target instanceof PhasmorEntity) && !(target instanceof PhasmorIllusion) && !(target instanceof ArmorStand)
                && !(target instanceof Player p && (p.isCreative() || p.isSpectator()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PHASMOR_ILLUSION.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return ModSounds.PHASMOR_ILLUSION.get();
    }

    /** Für die Vorführung: ein Trugbild an diesem Platz (Verschnaufpause beim Leben). */
    public void freshen() {
        this.life = 0;
        VoidwalkerArmor.forget(this);
    }
}
