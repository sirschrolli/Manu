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
import org.jspecify.annotations.Nullable;

/**
 * Ein Klumpen Gift, den Quetzara spuckt (seit 1.10): Er fliegt im Bogen und zerplatzt beim Aufschlag zu einer Giftwolke; wen
 * er unmittelbar trifft, den trifft er hart und vergiftet ihn. Die Tropfen ihres Giftregens (seit 1.13) treffen schwächer und
 * hinterlassen kleinere, kürzere Wolken.
 */
public class VenomGlobEntity extends ThrowableProjectile {
    public static final float DAMAGE = 7.0f;
    public static final float CLOUD_RADIUS = 2.6f;
    public static final int CLOUD_TICKS = 120;
    public static final float DROP_DAMAGE = 5.0f;
    public static final float DROP_CLOUD_RADIUS = 1.6f;
    public static final int DROP_CLOUD_TICKS = 70;
    /** Schwerkraft und Luftwiderstand je Tick (so fliegt er; der Giftregen zielt damit). */
    public static final double GRAVITY = 0.035;
    public static final double DRAG = 0.99;

    /** Ein Tropfen des Giftregens (nur auf dem Server bekannt; er sieht aus wie ein Klumpen). */
    private boolean drop;

    public VenomGlobEntity(EntityType<? extends VenomGlobEntity> type, Level level) {
        super(type, level);
    }

    public VenomGlobEntity(Level level, LivingEntity owner) {
        super(ModEntities.VENOM_GLOB.get(), level);
        this.setOwner(owner);
    }

    /** Macht ihn zu einem Tropfen des Giftregens. */
    public VenomGlobEntity asDrop() {
        this.drop = true;
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
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
            if (result.getEntity().hurtServer(level, this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null),
                    this.drop ? DROP_DAMAGE : DAMAGE)
                    && result.getEntity() instanceof LivingEntity victim) {
                SerpentFangItem.poison(victim, owner, 1, 100);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity living ? living : null;
            if (this.drop) {
                splash(level, result.getLocation(), owner, DROP_CLOUD_RADIUS, DROP_CLOUD_TICKS);
            } else {
                splash(level, result.getLocation(), owner);
            }
            this.discard();
        }
    }

    /** Die Giftwolke: Wer darin steht, wird vergiftet; sie schrumpft und verweht nach sechs Sekunden. */
    public static void splash(ServerLevel level, Vec3 at, @Nullable LivingEntity owner) {
        splash(level, at, owner, CLOUD_RADIUS, CLOUD_TICKS);
    }

    /** Eine Giftwolke dieser Größe und Dauer (sie schrumpft auf die Hälfte). */
    public static void splash(ServerLevel level, Vec3 at, @Nullable LivingEntity owner, float radius, int ticks) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
        if (owner != null) {
            cloud.setOwner(owner);
        }
        cloud.setRadius(radius);
        cloud.setRadiusOnUse(0.0f);
        cloud.setWaitTime(0);
        cloud.setDuration(ticks);
        cloud.setRadiusPerTick(-radius * 0.5f / ticks);
        cloud.setCustomParticle(new DustParticleOptions(QuetzaraEntity.VENOM, 1.2f));
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
        level.addFreshEntity(cloud);
        level.playSound(null, at.x, at.y, at.z, ModSounds.QUETZARA_SPLASH.get(), SoundSource.HOSTILE, 1.6f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        level.sendParticles(new DustParticleOptions(QuetzaraEntity.VENOM, 1.4f), at.x, at.y + 0.3, at.z, 30, 1.0, 0.3, 1.0, 0.0);
    }
}
