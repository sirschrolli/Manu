package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Ein Schwert der Schwerterkiste: Es erscheint schimmernd neben der Kiste aus Licht, die Spitze auf ihre Mitte gerichtet,
 * schwebt und zittert – und stößt im festgelegten Tick zu (den Schaden richtet MissMystik aus). Danach verblasst es. Nur
 * zum Ansehen; wird nicht gespeichert.
 */
public class MagicSwordEntity extends Entity {
    /** So lange (Ticks) dauert der Stoß, so lange bleibt es danach stecken. */
    public static final int THRUST_TICKS = 3;
    public static final int LINGER = 12;
    private static final EntityDataAccessor<Vector3fc> DATA_CENTER = SynchedEntityData.defineId(MagicSwordEntity.class,
            EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> DATA_STAB = SynchedEntityData.defineId(MagicSwordEntity.class, EntityDataSerializers.INT);

    public MagicSwordEntity(EntityType<? extends MagicSwordEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public MagicSwordEntity(Level level, Vec3 start, Vec3 center, int stab) {
        this(ModEntities.MAGIC_SWORD.get(), level);
        this.setPos(start.x, start.y, start.z);
        this.entityData.set(DATA_CENTER, new Vector3f((float) center.x, (float) center.y, (float) center.z));
        this.entityData.set(DATA_STAB, stab);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CENTER, new Vector3f());
        builder.define(DATA_STAB, 20);
    }

    /** Wohin die Spitze zeigt (die Mitte der Kiste). */
    public Vec3 center() {
        Vector3fc c = this.entityData.get(DATA_CENTER);
        return new Vec3(c.x(), c.y(), c.z());
    }

    /** In diesem Tick (seit es erschien) stößt es zu. */
    public int stabTick() {
        return this.entityData.get(DATA_STAB);
    }

    /** Wie weit es zugestoßen hat (0 = schwebt, 1 = ganz hinein). */
    public float thrust(float partialTicks) {
        float t = this.tickCount + partialTicks - this.stabTick();
        return Math.max(0.0f, Math.min(1.0f, t / THRUST_TICKS));
    }

    /** Sichtbarkeit: blendet ein, steht, verblasst nach dem Stoß. */
    public float alpha(float partialTicks) {
        float t = this.tickCount + partialTicks;
        float in = Math.min(1.0f, t / 6.0f);
        float out = 1.0f - Math.max(0.0f, Math.min(1.0f, (t - this.stabTick() - THRUST_TICKS - LINGER + 6.0f) / 6.0f));
        return Math.max(0.0f, Math.min(in, out));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.tickCount < this.stabTick() && this.random.nextInt(3) == 0) {
                this.level().addParticle(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (this.tickCount > this.stabTick() + THRUST_TICKS + LINGER) {
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
