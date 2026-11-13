package io.github.sirschrolli.kingdomsatwar.entity.shadow;

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
 * Ein Irrlicht (seit 0.98) – der Seelenhirte lässt sie aus seiner Laterne frei, sein Hirtenstab ebenso. Ein Irrlicht steigt
 * erst ein Stück auf und schwebt einen Augenblick (es „wittert"), dann jagt es seinem Ziel nach, immer schneller, und lenkt
 * dabei nach. Beim Aufprall zerstiebt es: Schaden, und der Getroffene verdorrt ein wenig.
 */
public class SoulWispEntity extends AbstractHurtingProjectile {
    /** So lange (Ticks) lebt ein Irrlicht höchstens. */
    public static final int LIFE = 90;
    /** So lange schwebt es, bevor es jagt. */
    public static final int GATHER = 10;
    /** So stark lenkt es je Tick auf sein Ziel. */
    private static final double HOMING = 0.11;
    private static final double TOP_SPEED = 0.62;

    private float damage = 6.0f;
    private @Nullable UUID targetId;

    public SoulWispEntity(EntityType<? extends SoulWispEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    /** Ein Irrlicht von {@code owner}, das zuerst in Richtung {@code start} steigt und dann {@code target} jagt. */
    public SoulWispEntity(Level level, LivingEntity owner, Vec3 start, @Nullable Entity target, float damage) {
        super(ModEntities.SOUL_WISP.get(), owner, start, level);
        this.accelerationPower = 0.0;
        this.damage = damage;
        this.targetId = target != null ? target.getUUID() : null;
        this.setDeltaMovement(start);
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
            Vec3 now = this.getDeltaMovement();
            if (this.tickCount < GATHER) {
                // es schwebt und bremst, ein wenig zitternd
                this.setDeltaMovement(now.scale(0.82).add((this.random.nextDouble() - 0.5) * 0.02, 0.004, (this.random.nextDouble() - 0.5) * 0.02));
                this.needsSync = true;
            } else {
                Entity target = this.targetId != null ? level.getEntity(this.targetId) : null;
                double speed = Math.min(TOP_SPEED, 0.2 + (this.tickCount - GATHER) * 0.04);
                Vec3 dir = now.lengthSqr() > 1.0e-6 ? now.normalize() : new Vec3(0.0, 1.0, 0.0);
                if (target != null && target.isAlive()) {
                    Vec3 want = target.getBoundingBox().getCenter().subtract(this.position());
                    if (want.lengthSqr() > 1.0e-6) {
                        double k = this.tickCount < GATHER + 4 ? 0.5 : HOMING;
                        dir = dir.add(want.normalize().subtract(dir).scale(k)).normalize();
                    }
                }
                this.setDeltaMovement(dir.scale(speed));
                this.needsSync = true;
            }
        }
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 p = this.position();
            Vec3 v = this.getDeltaMovement();
            double k = this.random.nextDouble();
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, p.x - v.x * k, p.y + 0.15 - v.y * k, p.z - v.z * k, 0.0, 0.01, 0.0);
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SOUL, p.x, p.y + 0.15, p.z, 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity instanceof SoulWispEntity) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner instanceof SoulShepherdEntity shepherd) {
            return !(entity instanceof LivingEntity living) || shepherd.canStrike(living);
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
        if (victim.hurtServer(level, source, this.damage)) {
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), owner);
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

    /** Das Irrlicht zerstiebt in Seelenfunken. */
    private void burst(ServerLevel level) {
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.15, this.getZ(), 10, 0.25, 0.25, 0.25, 0.05);
        level.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 0.15, this.getZ(), 4, 0.2, 0.2, 0.2, 0.03);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SOUL_SHEPHERD_WISP_HIT.get(), SoundSource.HOSTILE, 1.0f,
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
        output.putFloat("WispDamage", this.damage);
        if (this.targetId != null) {
            output.store("WispTarget", UUIDUtil.CODEC, this.targetId);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("WispDamage", 6.0f);
        this.targetId = input.read("WispTarget", UUIDUtil.CODEC).orElse(null);
        this.accelerationPower = 0.0;
    }
}
