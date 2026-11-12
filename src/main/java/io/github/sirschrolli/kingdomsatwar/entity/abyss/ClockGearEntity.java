package io.github.sirschrolli.kingdomsatwar.entity.abyss;

import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Zahnrad des Stundenwächters (seit 1.01): Er schleudert es, und es rollt auf der Kante über das Zifferblatt – an Wänden
 * und am Rand des Zifferblatts prallt es ab. Wen es überrollt, den trifft es ({@link #damage}) und stößt ihn mit. Nach
 * {@link #LIFE} Ticks (oder wenn sein Herr fällt) zerspringt es. Wird nicht gespeichert.
 */
public class ClockGearEntity extends Entity {
    public static final int LIFE = 90;
    /** Halbmesser des Zahnrads (Blöcke) – so schnell dreht es sich beim Rollen. */
    public static final float RADIUS = 0.65f;
    private @Nullable HourWardenEntity owner;
    private @Nullable BlockPos arena;
    private float damage = HourWardenEntity.GEAR_DAMAGE;
    private Vec3 velocity = Vec3.ZERO;
    private final Map<UUID, Integer> hits = new HashMap<>();

    public ClockGearEntity(EntityType<? extends ClockGearEntity> type, Level level) {
        super(type, level);
    }

    /** Schleudert das Zahnrad von {@code from} mit {@code velocity} (waagerecht); {@code arena}: die Nabe seines Zifferblatts. */
    public void launch(HourWardenEntity owner, Vec3 from, Vec3 velocity, @Nullable BlockPos arena, float damage) {
        this.owner = owner;
        this.arena = arena;
        this.damage = damage;
        this.velocity = new Vec3(velocity.x, 0.0, velocity.z);
        this.setPos(from.x, from.y, from.z);
        this.setDeltaMovement(this.velocity);
        this.face();
    }

    public @Nullable HourWardenEntity owner() {
        return this.owner;
    }

    private void face() {
        if (this.velocity.horizontalDistanceSqr() > 1.0e-6) {
            float yaw = (float) (Mth.atan2(-this.velocity.x, this.velocity.z) * Mth.RAD_TO_DEG);
            this.setYRot(yaw);
            this.yRotO = yaw;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.WAX_ON, this.getX(), this.getY() + 0.1, this.getZ(), 0.0, 0.05, 0.0);
            }
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(TimewardenArmor.AMBER, this.getRandomX(0.5), this.getY() + 0.6, this.getRandomZ(0.5), 0.0, 0.0, 0.0);
            }
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        if (this.owner == null || !this.owner.isAlive() || this.tickCount > LIFE) {
            this.shatter(level);
            return;
        }
        Vec3 wanted = new Vec3(this.velocity.x, this.getDeltaMovement().y - 0.08, this.velocity.z);
        Vec3 before = this.position();
        this.move(MoverType.SELF, wanted);
        Vec3 moved = this.position().subtract(before);
        double vx = this.velocity.x;
        double vz = this.velocity.z;
        // an Wänden prallt es ab
        if (this.horizontalCollision) {
            if (Math.abs(moved.x) < Math.abs(wanted.x) * 0.5) {
                vx = -vx;
            }
            if (Math.abs(moved.z) < Math.abs(wanted.z) * 0.5) {
                vz = -vz;
            }
            this.clang(level, 0.8f);
        }
        // am Rand des Zifferblatts kehrt es um (wie an einer Wand)
        if (this.arena != null) {
            double dx = this.getX() - (this.arena.getX() + 0.5);
            double dz = this.getZ() - (this.arena.getZ() + 0.5);
            double d = Math.hypot(dx, dz);
            if (d > HourWardenEntity.ARENA - 1.2 && d > 1.0e-3 && (dx * vx + dz * vz) > 0.0) {
                double nx = dx / d;
                double nz = dz / d;
                double dot = vx * nx + vz * nz;
                vx -= 2.0 * dot * nx;
                vz -= 2.0 * dot * nz;
                this.clang(level, 0.6f);
            }
        }
        this.velocity = new Vec3(vx, 0.0, vz);
        double vy = this.onGround() ? 0.0 : this.getDeltaMovement().y;
        this.setDeltaMovement(vx, vy, vz);
        this.face();
        if (this.getY() < level.getMinY() - 8) {
            this.discard();
            return;
        }
        if (this.tickCount % 7 == 0) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.HOUR_WARDEN_GEARS.get(), SoundSource.HOSTILE, 0.6f, 1.6f);
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.25), this.owner::canStrike)) {
            Integer last = this.hits.get(victim.getUUID());
            if (last != null && this.tickCount - last < 15) {
                continue;
            }
            this.hits.put(victim.getUUID(), this.tickCount);
            if (victim.hurtServer(level, this.damageSources().mobProjectile(this, this.owner), this.damage)) {
                Vec3 push = this.velocity.normalize();
                victim.push(push.x * 0.8, 0.35, push.z * 0.8);
                victim.needsSync = true;
                this.clang(level, 1.0f);
            }
        }
    }

    private void clang(ServerLevel level, float volume) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.HOUR_WARDEN_GEAR_HIT.get(), SoundSource.HOSTILE, volume,
                0.9f + this.random.nextFloat() * 0.3f);
        level.sendParticles(ParticleTypes.WAX_ON, this.getX(), this.getY() + 0.6, this.getZ(), 5, 0.3, 0.3, 0.3, 0.3);
    }

    /** Es zerspringt in Bronzesplitter und Funken. */
    public void shatter(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ClockTower.CLOCK_BRONZE.base().get().defaultBlockState()), this.getX(),
                this.getY() + 0.6, this.getZ(), 24, 0.4, 0.4, 0.4, 0.15);
        level.sendParticles(TimewardenArmor.AMBER, this.getX(), this.getY() + 0.6, this.getZ(), 12, 0.4, 0.4, 0.4, 0.0);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.HOUR_WARDEN_GEAR_HIT.get(), SoundSource.HOSTILE, 1.0f, 0.7f);
        this.discard();
    }

    /** Wie weit es sich beim Rollen gedreht hat (Grad, für die Darstellung). */
    public float roll(float partialTicks) {
        return (this.tickCount + partialTicks) * 0.55f / RADIUS * Mth.RAD_TO_DEG;
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
