package io.github.sirschrolli.kingdomsatwar.entity.sea;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Eine Kugel aus Hexentinte (seit 0.97) – die Meerhexe schleudert sie aus ihrer Stabkugel, der Hexenstab ebenso. Sie fliegt
 * unter Wasser so schnell wie an der Luft, folgt ihrem Ziel ein wenig und zerplatzt beim Aufprall: Schaden und Blindheit.
 */
public class InkBoltEntity extends AbstractHurtingProjectile {
    /** So lange (Ticks) fliegt eine Kugel höchstens. */
    public static final int LIFE = 70;
    /** So stark lenkt sie je Tick auf ihr Ziel. */
    private static final double HOMING = 0.07;

    private float damage = 6.0f;
    private int blindness = 40;
    private double speed = 0.6;
    private @Nullable UUID targetId;

    public InkBoltEntity(EntityType<? extends InkBoltEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    /** Eine Kugel von {@code owner} in Richtung {@code dir}; mit {@code target} folgt sie ihm ein wenig. */
    public InkBoltEntity(Level level, LivingEntity owner, Vec3 dir, @Nullable Entity target, float damage, double speed) {
        super(ModEntities.INK_BOLT.get(), owner, dir, level);
        this.accelerationPower = 0.0;
        this.damage = damage;
        this.speed = speed;
        this.targetId = target != null ? target.getUUID() : null;
        this.setDeltaMovement(dir.normalize().scale(speed));
    }

    public InkBoltEntity blindness(int ticks) {
        this.blindness = ticks;
        return this;
    }

    public float damage() {
        return this.damage;
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level) {
            if (this.tickCount > LIFE) {
                this.burst(level);
                this.discard();
                return;
            }
            Entity target = this.targetId != null ? level.getEntity(this.targetId) : null;
            if (target != null && target.isAlive() && this.tickCount > 3) {
                Vec3 now = this.getDeltaMovement();
                Vec3 want = target.getBoundingBox().getCenter().subtract(this.position());
                if (now.lengthSqr() > 1.0e-6 && want.lengthSqr() > 1.0e-6) {
                    Vec3 dir = now.normalize().add(want.normalize().subtract(now.normalize()).scale(HOMING)).normalize();
                    this.setDeltaMovement(dir.scale(this.speed));
                    this.needsSync = true;
                }
            }
        }
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 p = this.position();
            Vec3 v = this.getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                double k = this.random.nextDouble();
                this.level().addParticle(ParticleTypes.SQUID_INK, p.x - v.x * k, p.y + 0.15 - v.y * k, p.z - v.z * k, 0.0, 0.0, 0.0);
            }
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.GLOW, p.x + (this.random.nextDouble() - 0.5) * 0.3, p.y + 0.15,
                        p.z + (this.random.nextDouble() - 0.5) * 0.3, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity instanceof InkBoltEntity) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner instanceof SeaWitchEntity witch) {
            return !(entity instanceof LivingEntity living) || witch.canStrike(living);
        }
        if (owner instanceof Player player) {
            return entity != player && !(entity instanceof ArmorStand) && !(entity instanceof OwnableEntity pet && player.equals(pet.getOwner()));
        }
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel level) || !(result.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().indirectMagic(this, owner instanceof LivingEntity living ? living : this);
        if (victim.hurtServer(level, source, this.damage) && this.blindness > 0) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, this.blindness, 0), owner);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            this.burst(level);
            this.discard();
        }
    }

    /** Die Kugel zerplatzt zu einer Wolke aus Tinte. */
    private void burst(ServerLevel level) {
        level.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 0.15, this.getZ(), 14, 0.3, 0.3, 0.3, 0.06);
        level.sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY() + 0.15, this.getZ(), 5, 0.25, 0.25, 0.25, 0.04);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SEA_WITCH_INK_HIT.get(), SoundSource.HOSTILE, 1.0f,
                0.9f + this.random.nextFloat() * 0.3f);
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
    protected float getInertia() {
        return 1.0f;
    }

    @Override
    protected float getLiquidInertia() {
        return 1.0f;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("InkDamage", this.damage);
        output.putInt("Blindness", this.blindness);
        output.putDouble("InkSpeed", this.speed);
        if (this.targetId != null) {
            output.store("InkTarget", UUIDUtil.CODEC, this.targetId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("InkDamage", 6.0f);
        this.blindness = input.getIntOr("Blindness", 40);
        this.speed = input.getDoubleOr("InkSpeed", 0.6);
        this.targetId = input.read("InkTarget", UUIDUtil.CODEC).orElse(null);
        this.accelerationPower = 0.0;
    }
}
