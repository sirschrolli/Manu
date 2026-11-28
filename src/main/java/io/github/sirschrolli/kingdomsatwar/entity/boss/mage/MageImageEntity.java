package io.github.sirschrolli.kingdomsatwar.entity.boss.mage;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Spiegelbild eines Erzmagiers: sieht aus wie er (durchscheinend und flimmernd), schwebt an seinem Platz und wirft
 * schwache Geschosse. Ein einziger Treffer lässt es zerplatzen; nach fünfzehn Sekunden (oder wenn sein Herr fällt)
 * verblasst es von selbst.
 */
public class MageImageEntity extends Monster implements MageMinion, MageLike {
    private static final EntityDataAccessor<Integer> DATA_ELEMENT = SynchedEntityData.defineId(MageImageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_GESTURE = SynchedEntityData.defineId(MageImageEntity.class, EntityDataSerializers.BYTE);
    private static final int LIFE = 300;

    private @Nullable UUID master;
    private int life;
    private int shootTimer = 40;
    private int gestureTimer;
    public final AnimationState gestureAnimation = new AnimationState();

    public MageImageEntity(EntityType<? extends MageImageEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    /** Erzeugt ein Spiegelbild des Magiers an einer Stelle. */
    public static @Nullable MageImageEntity spawn(ServerLevel level, MageBossEntity master, Vec3 at) {
        MageImageEntity image = ModEntities.MAGE_IMAGE.get().create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
        if (image == null) {
            return null;
        }
        image.master = master.getUUID();
        image.entityData.set(DATA_ELEMENT, master.element().ordinal());
        image.snapTo(at.x, at.y, at.z, master.getYRot(), 0.0f);
        image.shootTimer = 25 + image.random.nextInt(30);
        level.addFreshEntity(image);
        BossFx.impact(level, new DustParticleOptions(master.element().color(), 1.5f), at.add(0, 1.2, 0), 30, 0.4, 0.9, 0.4, 0.04);
        return image;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ELEMENT, 0);
        builder.define(DATA_GESTURE, MageBossEntity.ANIM_NONE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_GESTURE.equals(key) && this.level().isClientSide()) {
            if (this.gesture() == MageBossEntity.ANIM_NONE) {
                this.gestureAnimation.stop();
            } else {
                this.gestureAnimation.start(this.tickCount);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Element", this.entityData.get(DATA_ELEMENT));
        if (this.master != null) {
            output.store("Master", UUIDUtil.CODEC, this.master);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_ELEMENT, input.getIntOr("Element", 0));
        this.master = input.read("Master", UUIDUtil.CODEC).orElse(null);
        this.life = LIFE;   // nach dem Laden verblasst es sofort
    }

    @Override
    public DragonType element() {
        return DragonType.values()[Mth.clamp(this.entityData.get(DATA_ELEMENT), 0, DragonType.values().length - 1)];
    }

    @Override
    public byte gesture() {
        return this.entityData.get(DATA_GESTURE);
    }

    @Override
    public AnimationState gestureAnimation() {
        return this.gestureAnimation;
    }

    @Override
    public float visibility(float partialTicks) {
        float fadeIn = Mth.clamp((this.tickCount + partialTicks) / 10.0f, 0.0f, 1.0f);
        return fadeIn * (0.55f + 0.12f * Mth.sin((this.tickCount + partialTicks) * 0.4f));
    }

    @Override
    public float shield(float partialTicks) {
        return 0.0f;
    }

    @Override
    public boolean dormant() {
        return false;
    }

    @Override
    public int dyingTicks() {
        return 0;
    }

    @Override
    public boolean isImage() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        MageBossEntity boss = this.master(level);
        if (boss == null || ++this.life > LIFE) {
            this.vanish(level);
            return;
        }
        LivingEntity target = boss.target();
        // schwebt sanft auf und ab und blickt zum Ziel
        Vec3 v = this.getDeltaMovement();
        double floor = boss.getY();
        double want = floor + Mth.sin((this.tickCount + this.getId()) * 0.08f) * 0.12;
        this.setDeltaMovement(v.x * 0.5, Mth.clamp((want - this.getY()) * 0.15, -0.2, 0.2), v.z * 0.5);
        if (target != null) {
            Vec3 d = target.getEyePosition().subtract(this.getEyePosition());
            float yaw = (float) (Mth.atan2(d.z, d.x) * Mth.RAD_TO_DEG) - 90.0f;
            this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 10.0f));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            if (--this.shootTimer <= 0) {
                this.shootTimer = 45 + this.random.nextInt(35);
                this.entityData.set(DATA_GESTURE, MageBossEntity.ANIM_MISSILES);
                this.gestureTimer = 14;
                Vec3 from = this.position().add(0, 2.4, 0);
                MagicMissile.shoot(level, this, target, from, target.getBoundingBox().getCenter().subtract(from), 0.45, 2.0f);
                level.playSound(null, from.x, from.y, from.z, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 0.8f, 1.6f);
            }
        }
        if (this.gestureTimer > 0 && --this.gestureTimer == 0) {
            this.entityData.set(DATA_GESTURE, MageBossEntity.ANIM_NONE);
        }
    }

    /** Der Magier, dessen Abbild es ist (falls er noch lebt). */
    public @Nullable MageBossEntity master(ServerLevel level) {
        return this.master != null && level.getEntity(this.master) instanceof MageBossEntity boss && boss.isAlive() ? boss : null;
    }

    /** Zerplatzt in Funken. */
    public void vanish(ServerLevel level) {
        BossFx.impact(level, new DustParticleOptions(this.element().color(), 1.4f), new Vec3(this.getX(), this.getY(1.0), this.getZ()), 30, 0.4,
                0.9, 0.4, 0.05);
        BossFx.impact(level, ParticleTypes.POOF, new Vec3(this.getX(), this.getY(0.6), this.getZ()), 10, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1.0f, 1.6f);
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (attacker instanceof MageBossEntity || attacker instanceof MageMinion || source.getDirectEntity() instanceof MagicMissile missile
                && missile.getOwner() instanceof MageMinion) {
            return false;
        }
        this.vanish(level);
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void knockback(double power, double xd, double zd, DamageSource source, float damage, boolean comesFromEffect) {
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }
}
