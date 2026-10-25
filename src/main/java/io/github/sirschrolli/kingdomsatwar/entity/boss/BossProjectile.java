package io.github.sirschrolli.kingdomsatwar.entity.boss;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Geschosse der Drachen-Bosse: Feuerbälle, Meteore usw. Sie explodieren beim Aufprall, ohne Blöcke zu zerstören,
 * und treffen nur Gegner des Bosses.
 */
public class BossProjectile extends AbstractHurtingProjectile {
    public enum Kind {
        FIREBALL(1.0f, 3.0, 4, false),
        METEOR(2.2f, 4.2, 5, true);

        public final float size;
        public final double radius;
        public final int fireSeconds;
        public final boolean falls;

        Kind(float size, double radius, int fireSeconds, boolean falls) {
            this.size = size;
            this.radius = radius;
            this.fireSeconds = fireSeconds;
            this.falls = falls;
        }
    }

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(BossProjectile.class, EntityDataSerializers.BYTE);
    private float damage = 8.0f;
    private int life;

    public BossProjectile(EntityType<? extends BossProjectile> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.0;
    }

    /** Schießt ein Geschoss aus {@code from} in Richtung {@code dir}. */
    public static BossProjectile shoot(ServerLevel level, DragonBossEntity boss, Kind kind, Vec3 from, Vec3 dir, double speed, float damage) {
        BossProjectile p = new BossProjectile(ModEntities.BOSS_PROJECTILE.get(), level);
        p.setKind(kind);
        p.damage = damage;
        p.setOwner(boss);
        p.setPos(from.x, from.y, from.z);
        p.setDeltaMovement(dir.normalize().scale(speed));
        p.accelerationPower = 0.02;
        level.addFreshEntity(p);
        return p;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KIND, (byte) 0);
    }

    public Kind getKind() {
        return Kind.values()[Mth.clamp(this.entityData.get(DATA_KIND), 0, Kind.values().length - 1)];
    }

    public void setKind(Kind kind) {
        this.entityData.set(DATA_KIND, (byte) kind.ordinal());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Kind", this.getKind().ordinal());
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setKind(Kind.values()[Mth.clamp(input.getIntOr("Kind", 0), 0, Kind.values().length - 1)]);
        this.damage = input.getFloatOr("Damage", 8.0f);
    }

    @Override
    public void tick() {
        Kind kind = this.getKind();
        if (kind.falls && !this.level().isClientSide()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
        }
        super.tick();
        if (this.level().isClientSide()) {
            ParticleOptions trail = kind == Kind.METEOR ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE;
            for (int i = 0; i < (kind == Kind.METEOR ? 4 : 2); i++) {
                this.level().addParticle(ParticleTypes.FLAME, this.getX() + (this.random.nextDouble() - 0.5) * kind.size,
                        this.getY() + (this.random.nextDouble() - 0.5) * kind.size, this.getZ() + (this.random.nextDouble() - 0.5) * kind.size,
                        0, 0, 0);
            }
            this.level().addParticle(trail, this.getX(), this.getY(), this.getZ(), 0, 0.02, 0);
        } else if (++this.life > 200) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof DragonBossEntity || entity instanceof BossPart || entity instanceof BossProjectile) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level) {
            this.explode(level);
            this.discard();
        }
    }

    private void explode(ServerLevel level) {
        Kind kind = this.getKind();
        Vec3 c = this.position();
        level.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z, kind == Kind.METEOR ? 4 : 1, kind.radius * 0.3, 0.3, kind.radius * 0.3, 0);
        level.sendParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 40, kind.radius * 0.4, 0.4, kind.radius * 0.4, 0.08);
        level.sendParticles(ParticleTypes.LAVA, c.x, c.y, c.z, 12, kind.radius * 0.3, 0.2, kind.radius * 0.3, 0.1);
        level.playSound(null, c.x, c.y, c.z, SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.HOSTILE,
                kind == Kind.METEOR ? 3.0f : 1.6f, 0.7f + this.random.nextFloat() * 0.2f);
        DragonBossEntity boss = this.getOwner() instanceof DragonBossEntity b ? b : null;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(kind.radius))) {
            if (boss != null ? !boss.isBossEnemy(e) : e instanceof DragonBossEntity) {
                continue;
            }
            double d = e.position().add(0, e.getBbHeight() * 0.5, 0).distanceTo(c);
            if (d > kind.radius + 0.5) {
                continue;
            }
            float f = (float) Math.max(0.35, 1.0 - d / (kind.radius + 0.5));
            DamageSource source = boss != null ? this.damageSources().mobProjectile(this, boss) : this.damageSources().explosion(this, null);
            e.hurtServer(level, source, this.damage * f);
            if (kind.fireSeconds > 0) {
                e.igniteForSeconds(kind.fireSeconds);
            }
            DragonBossEntity.pushAway(e, c, 0.7 * f, 0.4 * f);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
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
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected float getInertia() {
        return 1.0f;
    }
}
