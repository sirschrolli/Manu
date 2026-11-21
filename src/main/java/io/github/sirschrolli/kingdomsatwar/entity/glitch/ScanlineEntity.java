package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
 * Ein Zeilenfehler: eine leuchtende Zeile, die einmal ganz um Anni herumfegt (vierzehn Blöcke lang). Die rote liegt dicht
 * über dem Boden – wer springt, entgeht ihr –, die blaue in Kopfhöhe – wer sich duckt, entgeht ihr. Wen sie streift, den
 * trifft sie (einmal je Zeile). Ihr Winkel ergibt sich aus Anfang, Geschwindigkeit und Alter (im Spiel ohne Ruckeln).
 */
public class ScanlineEntity extends Entity {
    public static final float RADIUS = 14.0f;
    private static final EntityDataAccessor<Float> DATA_HEIGHT = SynchedEntityData.defineId(ScanlineEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_LOW = SynchedEntityData.defineId(ScanlineEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_START = SynchedEntityData.defineId(ScanlineEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPEED = SynchedEntityData.defineId(ScanlineEntity.class, EntityDataSerializers.FLOAT);
    /** So lange erscheint die Zeile erst blass (Vorwarnung), bevor sie fegt. */
    public static final int WARMUP = 8;
    private @Nullable UUID ownerId;
    private float damage = 12.0f;
    private final Set<UUID> struck = new HashSet<>();

    public ScanlineEntity(EntityType<? extends ScanlineEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public ScanlineEntity(Level level, AnniTrollEntity owner, Vec3 center, float height, boolean low, float startDeg, float speedDeg,
            float damage) {
        this(ModEntities.SCANLINE.get(), level);
        this.ownerId = owner.getUUID();
        this.damage = damage;
        this.setPos(center.x, center.y, center.z);
        this.entityData.set(DATA_HEIGHT, height);
        this.entityData.set(DATA_LOW, low);
        this.entityData.set(DATA_START, startDeg);
        this.entityData.set(DATA_SPEED, speedDeg);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HEIGHT, 0.45f);
        builder.define(DATA_LOW, true);
        builder.define(DATA_START, 0.0f);
        builder.define(DATA_SPEED, 8.0f);
    }

    public float height() {
        return this.entityData.get(DATA_HEIGHT);
    }

    /** Rot und tief (springen) oder blau und hoch (ducken). */
    public boolean isLow() {
        return this.entityData.get(DATA_LOW);
    }

    public float speed() {
        return this.entityData.get(DATA_SPEED);
    }

    /** Wie lange sie fegt (eine ganze Umdrehung). */
    public int sweepTicks() {
        return Mth.ceil(360.0f / Math.max(1.0f, Math.abs(this.speed())));
    }

    /** Winkel der Zeile (Grad, wie atan2(z, x)) zum Zeitpunkt {@code age}. */
    public float angleAt(float age) {
        return this.entityData.get(DATA_START) + this.speed() * Math.max(0.0f, age - WARMUP);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        AnniTrollEntity owner = this.ownerId != null && level.getEntity(this.ownerId) instanceof AnniTrollEntity a ? a : null;
        if (owner == null || this.tickCount > WARMUP + this.sweepTicks() + 4) {
            this.discard();
            return;
        }
        if (this.tickCount <= WARMUP || this.tickCount > WARMUP + this.sweepTicks()) {
            return;
        }
        float a0 = this.angleAt(this.tickCount - 1);
        float a1 = this.angleAt(this.tickCount);
        double y = this.getY() + this.height();
        DamageSource source = this.damageSources().indirectMagic(this, owner);
        AABB area = new AABB(this.getX() - RADIUS, y - 3.0, this.getZ() - RADIUS, this.getX() + RADIUS, y + 3.0, this.getZ() + RADIUS);
        for (LivingEntity victim : owner.victims(level, area)) {
            AABB box = victim.getBoundingBox();
            if (y < box.minY || y > box.maxY || this.struck.contains(victim.getUUID())) {
                continue;
            }
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > RADIUS + victim.getBbWidth() * 0.5 || d < 0.5) {
                continue;
            }
            float at = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG);
            float halfWidth = (float) (Math.atan2(victim.getBbWidth() * 0.5 + 0.15, d) * Mth.RAD_TO_DEG);
            if (swept(a0, a1, at, halfWidth)) {
                this.struck.add(victim.getUUID());
                if (victim.hurtServer(level, source, this.damage)) {
                    float tangent = (a1 > a0 ? 90.0f : -90.0f) + at;
                    victim.push(Math.cos(tangent * Mth.DEG_TO_RAD) * 0.5, 0.25, Math.sin(tangent * Mth.DEG_TO_RAD) * 0.5);
                    victim.needsSync = true;
                    victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 1), owner);
                }
            }
        }
    }

    /** Hat die Zeile zwischen a0 und a1 den Winkel {@code at} (± halfWidth) überstrichen? */
    static boolean swept(float a0, float a1, float at, float halfWidth) {
        float lo = Math.min(a0, a1) - halfWidth;
        float hi = Math.max(a0, a1) + halfWidth;
        float rel = Mth.wrapDegrees(at - lo);
        if (rel < 0.0f) {
            rel += 360.0f;
        }
        return rel <= hi - lo;
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
