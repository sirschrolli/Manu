package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Feld fehlender Textur: ein Quadrat aus Magenta und Schwarz auf dem Boden (Kantenlänge 2 × Halbmesser + 1). Eine
 * halbe Sekunde blinkt nur sein Rand (Zeit zu fliehen), dann ist es scharf: Wer darin steht, hängt fest (stark verlangsamt)
 * und nimmt jede halbe Sekunde Schaden. Nach sieben Sekunden lädt die Welt die richtige Textur nach, und es ist fort.
 */
public class MissingZoneEntity extends Entity {
    public static final int WARMUP = 12;
    public static final int LIFE = 150;
    private static final EntityDataAccessor<Integer> DATA_HALF = SynchedEntityData.defineId(MissingZoneEntity.class, EntityDataSerializers.INT);
    private @Nullable UUID ownerId;
    private float damage = 4.0f;

    public MissingZoneEntity(EntityType<? extends MissingZoneEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public MissingZoneEntity(Level level, AnniTrollEntity owner, Vec3 ground, int half, float damage) {
        this(ModEntities.MISSING_ZONE.get(), level);
        this.ownerId = owner.getUUID();
        this.damage = damage;
        this.entityData.set(DATA_HALF, half);
        this.setPos(Math.floor(ground.x) + 0.5, ground.y, Math.floor(ground.z) + 0.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HALF, 4);
    }

    /** Halbe Kantenlänge (in Blöcken, ohne den mittleren Block). */
    public int half() {
        return this.entityData.get(DATA_HALF);
    }

    public boolean isArmed() {
        return this.tickCount >= WARMUP;
    }

    @Override
    public void tick() {
        super.tick();
        int half = this.half();
        if (this.level().isClientSide()) {
            // an den Rändern flimmern Pixel auf
            if (this.random.nextInt(this.isArmed() ? 1 : 3) == 0) {
                double edge = half + 0.5;
                double along = (this.random.nextDouble() * 2.0 - 1.0) * edge;
                boolean xSide = this.random.nextBoolean();
                double sign = this.random.nextBoolean() ? 1.0 : -1.0;
                this.level().addParticle(ModParticles.GLITCH_PIXEL.get(), this.getX() + (xSide ? edge * sign : along), this.getY() + 0.1,
                        this.getZ() + (xSide ? along : edge * sign), 0.0, 0.04, 0.0);
            }
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        AnniTrollEntity owner = this.ownerId != null && level.getEntity(this.ownerId) instanceof AnniTrollEntity a ? a : null;
        if (owner == null || this.tickCount >= LIFE) {
            level.sendParticles(ModParticles.GLITCH_PIXEL.get(), this.getX(), this.getY() + 0.2, this.getZ(), 30, half * 0.5, 0.1, half * 0.5, 0.05);
            this.discard();
            return;
        }
        if (this.tickCount == WARMUP) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ANNI_MISSING.get(), SoundSource.HOSTILE, 1.6f, 1.3f);
        }
        if (this.isArmed() && (this.tickCount - WARMUP) % 10 == 0) {
            double h = half + 0.5;
            AABB area = new AABB(this.getX() - h, this.getY() - 1.0, this.getZ() - h, this.getX() + h, this.getY() + 2.5, this.getZ() + h);
            DamageSource source = this.damageSources().indirectMagic(this, owner);
            for (LivingEntity victim : owner.victims(level, area)) {
                victim.hurtServer(level, source, this.damage);
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 2), owner);
                victim.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 30, 1), owner);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
