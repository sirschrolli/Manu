package io.github.sirschrolli.kingdomsatwar.entity.serpent;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Locale;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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
 * Quetzaras abgestreifte Haut (seit 1.10): Sie liegt, wo die Schlange sich häutete – blass, papieren, fast durchscheinend, mit
 * jeder Schuppe und Feder, mit aufgerissenem Kopf. Eine <b>Täuschung</b>: Sie zischt dann und wann, als lebte sie, und wer sie
 * trifft, dem platzt sie als Giftwolke entgegen. Nach einer Weile zerfällt sie.
 */
public class ShedSkinEntity extends Entity {
    public static final int LIFETIME = 900;
    private static final int CRUMBLE_TICKS = 24;
    private static final EntityDataAccessor<String> DATA_POSE = SynchedEntityData.defineId(ShedSkinEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_BURST = SynchedEntityData.defineId(ShedSkinEntity.class, EntityDataSerializers.BOOLEAN);

    /** Der Nacken und die Glieder (Index SEGMENTS = Schwanz), bezogen auf die Lage der Haut. */
    private final Vec3[] points = new Vec3[QuetzaraEntity.SEGMENTS + 1];
    private Vec3 joint = Vec3.ZERO;
    private float headYaw;
    private float headPitch;
    private float ruff;
    private boolean posed;
    private int life;
    private int crumble;
    private @Nullable LivingEntity owner;

    public ShedSkinEntity(EntityType<? extends ShedSkinEntity> type, Level level) {
        super(type, level);
        for (int i = 0; i < this.points.length; i++) {
            this.points[i] = Vec3.ZERO;
        }
    }

    /** Die Haut, wie sie gerade um die Schlange liegt. */
    public ShedSkinEntity(Level level, QuetzaraEntity serpent) {
        this(ModEntities.SHED_SKIN.get(), level);
        Vec3 o = serpent.position();
        this.setPos(o.x, o.y, o.z);
        this.owner = serpent;
        StringBuilder pose = new StringBuilder();
        Vec3 j = serpent.joint(1.0f).subtract(o);
        append(pose, j);
        for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
            append(pose, serpent.bodyPoint(i, 1.0f).subtract(o));
        }
        pose.append(String.format(Locale.ROOT, "%.1f;%.1f;%.2f", serpent.getYHeadRot(), serpent.getXRot(), serpent.ruff(1.0f)));
        this.entityData.set(DATA_POSE, pose.toString());
        this.decode(pose.toString());
    }

    private static void append(StringBuilder b, Vec3 v) {
        b.append(String.format(Locale.ROOT, "%.2f,%.2f,%.2f;", v.x, v.y, v.z));
    }

    private void decode(String pose) {
        String[] parts = pose.split(";");
        if (parts.length < QuetzaraEntity.SEGMENTS + 5) {
            return;
        }
        try {
            this.joint = vec(parts[0]);
            for (int i = 0; i <= QuetzaraEntity.SEGMENTS; i++) {
                this.points[i] = vec(parts[i + 1]);
            }
            int k = QuetzaraEntity.SEGMENTS + 2;
            this.headYaw = Float.parseFloat(parts[k]);
            this.headPitch = Float.parseFloat(parts[k + 1]);
            this.ruff = Float.parseFloat(parts[k + 2]);
            this.posed = true;
            this.setBoundingBox(this.makeBoundingBox());
        } catch (NumberFormatException ignored) {
            this.posed = false;
        }
    }

    private static Vec3 vec(String s) {
        String[] c = s.split(",");
        return new Vec3(Double.parseDouble(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2]));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_POSE, "");
        builder.define(DATA_BURST, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_POSE.equals(key)) {
            this.decode(this.entityData.get(DATA_POSE));
        }
    }

    /** Die Haut reicht so weit wie der Leib, der in ihr lag. */
    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        if (!this.posed) {
            return super.makeBoundingBox(pos);
        }
        AABB box = new AABB(this.joint, this.joint);
        for (Vec3 p : this.points) {
            box = box.minmax(new AABB(p, p));
        }
        return box.inflate(0.6, 0.5, 0.6).move(pos);
    }

    public boolean isPosed() {
        return this.posed;
    }

    public Vec3 jointOffset() {
        return this.joint;
    }

    public Vec3 point(int i) {
        return this.points[i];
    }

    public float headYaw() {
        return this.headYaw;
    }

    public float headPitch() {
        return this.headPitch;
    }

    public float ruff() {
        return this.ruff;
    }

    /** Zerfällt sie gerade (0 … 1)? */
    public float crumble(float partialTicks) {
        return this.entityData.get(DATA_BURST) ? Mth.clamp((this.crumble + partialTicks) / CRUMBLE_TICKS, 0.0f, 1.0f) : 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        this.life++;
        if (this.entityData.get(DATA_BURST)) {
            this.crumble++;
            if (this.level() instanceof ServerLevel level && this.crumble % 3 == 0) {
                int i = Mth.clamp(QuetzaraEntity.SEGMENTS - this.crumble / 2, 0, QuetzaraEntity.SEGMENTS);
                Vec3 p = this.position().add(this.points[i]);
                level.sendParticles(new DustParticleOptions(QuetzaraEntity.SKIN, 1.2f), p.x, p.y, p.z, 6, 0.4, 0.3, 0.4, 0.0);
            }
            if (this.crumble >= CRUMBLE_TICKS && !this.level().isClientSide()) {
                this.discard();
            }
            return;
        }
        if (this.level() instanceof ServerLevel level) {
            // dann und wann zischt sie, als lebte sie noch
            if (this.life % 70 == 35) {
                Vec3 j = this.position().add(this.joint);
                level.playSound(null, j.x, j.y, j.z, ModSounds.QUETZARA_HISS.get(), SoundSource.HOSTILE, 1.4f, 1.1f + this.random.nextFloat() * 0.15f);
            }
            if (this.life >= LIFETIME) {
                this.entityData.set(DATA_BURST, true);
            }
        }
    }

    @Override
    public boolean isPickable() {
        return !this.entityData.get(DATA_BURST);
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    /** Wer sie trifft, dem platzt sie als Giftwolke entgegen. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.entityData.get(DATA_BURST) || this.isRemoved()) {
            return false;
        }
        this.burst(level);
        return true;
    }

    public void burst(ServerLevel level) {
        this.entityData.set(DATA_BURST, true);
        Vec3 mid = this.position().add(this.points[QuetzaraEntity.SEGMENTS / 3]);
        VenomGlobEntity.splash(level, mid, this.owner != null && this.owner.isAlive() ? this.owner : null);
        level.playSound(null, mid.x, mid.y, mid.z, ModSounds.QUETZARA_SKIN_BURST.get(), SoundSource.HOSTILE, 2.0f, 1.0f);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.life = input.getIntOr("Life", 0);
        String pose = input.getStringOr("Pose", "");
        this.entityData.set(DATA_POSE, pose);
        this.decode(pose);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Life", this.life);
        output.putString("Pose", this.entityData.get(DATA_POSE));
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 96.0 * 96.0;
    }
}
