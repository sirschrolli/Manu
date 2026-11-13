package io.github.sirschrolli.kingdomsatwar.entity.serpent;

import io.github.sirschrolli.kingdomsatwar.item.serpent.SerpentFangItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Klumpen Gift, den Quetzara spuckt (seit 1.10): Er fliegt im Bogen und zerplatzt beim Aufschlag zu einer Giftwolke; wen
 * er unmittelbar trifft, den trifft er hart und vergiftet ihn.
 */
public class VenomGlobEntity extends ThrowableProjectile {
    public static final float DAMAGE = 7.0f;
    public static final float CLOUD_RADIUS = 2.6f;
    public static final int CLOUD_TICKS = 120;

    public VenomGlobEntity(EntityType<? extends VenomGlobEntity> type, Level level) {
        super(type, level);
    }

    public VenomGlobEntity(Level level, LivingEntity owner) {
        super(ModEntities.VENOM_GLOB.get(), level);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.035;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 120) {
            this.discard();
            return;
        }
        if (this.level().isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            for (int k = 0; k < 2; k++) {
                this.level().addParticle(new DustParticleOptions(QuetzaraEntity.VENOM, 1.0f), this.getX() - v.x * k * 0.5, this.getY() - v.y * k * 0.5,
                        this.getZ() - v.z * k * 0.5, 0.0, -0.03, 0.0);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof QuetzaraEntity) && !(entity instanceof QuetzaraPart)
                && !(entity instanceof ShedSkinEntity) && !(entity instanceof VenomGlobEntity) && !(entity instanceof QuetzaraFeatherEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel level) {
            Entity owner = this.getOwner();
            if (result.getEntity().hurtServer(level, this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null), DAMAGE)
                    && result.getEntity() instanceof LivingEntity victim) {
                SerpentFangItem.poison(victim, owner, 1, 100);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            splash(level, result.getLocation(), this.getOwner() instanceof LivingEntity living ? living : null);
            this.discard();
        }
    }

    /** Die Giftwolke: Wer darin steht, wird vergiftet; sie schrumpft und verweht nach sechs Sekunden. */
    public static void splash(ServerLevel level, Vec3 at, LivingEntity owner) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
        if (owner != null) {
            cloud.setOwner(owner);
        }
        cloud.setRadius(CLOUD_RADIUS);
        cloud.setRadiusOnUse(0.0f);
        cloud.setWaitTime(0);
        cloud.setDuration(CLOUD_TICKS);
        cloud.setRadiusPerTick(-CLOUD_RADIUS * 0.5f / CLOUD_TICKS);
        cloud.setCustomParticle(new DustParticleOptions(QuetzaraEntity.VENOM, 1.2f));
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        level.addFreshEntity(cloud);
        level.playSound(null, at.x, at.y, at.z, ModSounds.QUETZARA_SPLASH.get(), SoundSource.HOSTILE, 1.6f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        level.sendParticles(new DustParticleOptions(QuetzaraEntity.VENOM, 1.4f), at.x, at.y + 0.3, at.z, 30, 1.0, 0.3, 1.0, 0.0);
    }
}
