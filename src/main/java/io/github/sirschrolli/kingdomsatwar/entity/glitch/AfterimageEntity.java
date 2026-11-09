package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Ein Nachbild: Wo Anni eben noch stand (beim Springen), bleibt für einen Augenblick sein erstarrtes, farbverschobenes
 * Abbild stehen und zerfällt. Beim Rückruck markiert ein flimmerndes Abbild die Stelle, an die es jemanden gleich
 * zurückreißt (eine Säule in Größe des Wesens, mit Zeilen und Farbsäumen).
 */
public class AfterimageEntity extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_TROLL = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_TIME = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WIDTH = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HEIGHT = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_LIFE = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_GLITCH = SynchedEntityData.defineId(AfterimageEntity.class, EntityDataSerializers.BOOLEAN);

    public AfterimageEntity(EntityType<? extends AfterimageEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /** Annis Nachbild (in der Haltung, die er gerade hat). */
    public static AfterimageEntity ofTroll(ServerLevel level, AnniTrollEntity troll, int life) {
        AfterimageEntity image = new AfterimageEntity(ModEntities.AFTERIMAGE.get(), level);
        image.snapTo(troll.getX(), troll.getY(), troll.getZ(), troll.getYRot(), 0.0f);
        image.entityData.set(DATA_TROLL, true);
        image.entityData.set(DATA_ATTACK, troll.currentAttack().ordinal());
        image.entityData.set(DATA_TIME, troll.attackTime(0.0f));
        image.entityData.set(DATA_LIFE, life);
        image.entityData.set(DATA_GLITCH, troll.isGlitchMode());
        return image;
    }

    /** Das flimmernde Abbild eines Wesens (die Stelle, an die der Rückruck es zurückreißt). */
    public static AfterimageEntity ofEntity(ServerLevel level, LivingEntity entity, int life) {
        AfterimageEntity image = new AfterimageEntity(ModEntities.AFTERIMAGE.get(), level);
        image.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0f);
        image.entityData.set(DATA_TROLL, false);
        image.entityData.set(DATA_WIDTH, entity.getBbWidth());
        image.entityData.set(DATA_HEIGHT, entity.getBbHeight());
        image.entityData.set(DATA_LIFE, life);
        return image;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TROLL, true);
        builder.define(DATA_ATTACK, 0);
        builder.define(DATA_TIME, 0.0f);
        builder.define(DATA_WIDTH, 0.6f);
        builder.define(DATA_HEIGHT, 1.8f);
        builder.define(DATA_LIFE, 14);
        builder.define(DATA_GLITCH, false);
    }

    public boolean isTroll() {
        return this.entityData.get(DATA_TROLL);
    }

    public int attack() {
        return this.entityData.get(DATA_ATTACK);
    }

    public float attackTime() {
        return this.entityData.get(DATA_TIME);
    }

    public float width() {
        return this.entityData.get(DATA_WIDTH);
    }

    public float height() {
        return this.entityData.get(DATA_HEIGHT);
    }

    public int life() {
        return this.entityData.get(DATA_LIFE);
    }

    public boolean isGlitchMode() {
        return this.entityData.get(DATA_GLITCH);
    }

    /** 1 am Anfang, 0 am Ende. */
    public float fade(float partialTicks) {
        return Math.max(0.0f, 1.0f - (this.tickCount + partialTicks) / Math.max(1, this.life()));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount >= this.life()) {
            this.discard();
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
