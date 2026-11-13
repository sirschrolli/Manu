package io.github.sirschrolli.kingdomsatwar.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Unsichtbarer Träger der Lichteffekte beim Siegelbruch (Lichtsäulen, Strahlen zur Mitte, Lichtkugel, Druckwelle,
 * Schattensäule). Er steht über der Mitte des Siegelkreises; der Server meldet jeden Tick den Fortschritt, gezeichnet
 * wird auf dem eigenen Bildschirm ({@code SealBreakRenderer}). Wird nicht gespeichert.
 */
public class SealBreakEntity extends Entity {
    private static final EntityDataAccessor<Integer> TICKS = SynchedEntityData.defineId(SealBreakEntity.class, EntityDataSerializers.INT);
    /** Notbremse: so lange lebt der Träger höchstens. */
    private static final int MAX_AGE = 600;

    public SealBreakEntity(EntityType<? extends SealBreakEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TICKS, 0);
    }

    /** Fortschritt des Siegelbruchs in Ticks. */
    public int breakTicks() {
        return this.entityData.get(TICKS);
    }

    public void setBreakTicks(int ticks) {
        this.entityData.set(TICKS, ticks);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > MAX_AGE) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
