package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Kristallsplitter: das Geschoss des Splitterwächters. Fliegt schnell und gerade, zerspringt beim Aufprall und lässt
 * den Getroffenen kurz taumeln. Wie ein Feuerball des Ghasts lässt er sich mit einem gut gezielten Schlag zurückschlagen.
 */
public class WardenShardEntity extends AbstractHurtingProjectile {
    private float damage = 6.0f;
    private int life;

    public WardenShardEntity(EntityType<? extends WardenShardEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    /** Schießt einen Splitter von {@code from} in Richtung {@code dir}. */
    public static WardenShardEntity shoot(ServerLevel level, LivingEntity owner, Vec3 from, Vec3 dir, double speed, float damage) {
        WardenShardEntity shard = new WardenShardEntity(ModEntities.WARDEN_SHARD.get(), level);
        shard.setOwner(owner);
        shard.damage = damage;
        shard.setPos(from.x, from.y, from.z);
        Vec3 v = dir.normalize();
        shard.setDeltaMovement(v.scale(speed));
        shard.accelerationPower = 0.01;
        shard.setYRot((float) (-Math.atan2(v.x, v.z) * 180.0 / Math.PI));
        shard.setXRot((float) (-Math.atan2(v.y, Math.hypot(v.x, v.z)) * 180.0 / Math.PI));
        level.addFreshEntity(shard);
        return shard;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("Damage", 6.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && ++this.life > 120) {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return ParticleTypes.END_ROD;
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof ShardWardenEntity) && !(entity instanceof WardenShardEntity) && super.canHitEntity(entity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel level && result.getEntity() instanceof LivingEntity target) {
            Entity owner = this.getOwner();
            var source = owner instanceof LivingEntity living ? this.damageSources().mobProjectile(this, living)
                    : this.damageSources().magic();
            if (target.hurtServer(level, source, this.damage)) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1), owner);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            Vec3 c = this.position();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, VoidBlocks.VOID_CRYSTAL.get().defaultBlockState()), c.x, c.y, c.z,
                    18, 0.2, 0.2, 0.2, 0.15);
            level.sendParticles(ParticleTypes.END_ROD, c.x, c.y, c.z, 6, 0.1, 0.1, 0.1, 0.08);
            level.playSound(null, c.x, c.y, c.z, ModSounds.WARDEN_SHARD_HIT.get(), SoundSource.HOSTILE, 1.0f,
                    0.9f + this.random.nextFloat() * 0.3f);
            this.discard();
        }
    }
}
