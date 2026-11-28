package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
 * Ein Pixel aus dem Pixelregen: ein leuchtender Würfel (rot, grün, blau, magenta, cyan oder gelb), der vom Himmel stürzt
 * und genau nach {@link #FALL_TICKS} Ticks auf der markierten Stelle aufschlägt – in einem Knall aus kleinen Pixeln, der
 * trifft, wer dort steht.
 */
public class PixelCubeEntity extends Entity {
    public static final int[] COLORS = {0xFF3040, 0x40FF70, 0x4080FF, 0xF040C8, 0x30E0F0, 0xFFD400};
    public static final int FALL_TICKS = 16;
    private static final double V0 = 0.25;
    private static final double G = 0.045;
    /** So hoch über dem Boden beginnt der Fall (damit er genau nach FALL_TICKS aufschlägt). */
    public static final double HEIGHT = V0 * FALL_TICKS + G * FALL_TICKS * (FALL_TICKS + 1);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(PixelCubeEntity.class, EntityDataSerializers.INT);
    private @Nullable UUID ownerId;
    private Vec3 ground = Vec3.ZERO;
    private float damage = 7.0f;

    public PixelCubeEntity(EntityType<? extends PixelCubeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public PixelCubeEntity(Level level, AnniTrollEntity owner, Vec3 ground, int color, float damage) {
        this(ModEntities.PIXEL_CUBE.get(), level);
        this.ownerId = owner.getUUID();
        this.ground = ground;
        this.damage = damage;
        this.entityData.set(DATA_COLOR, Math.floorMod(color, COLORS.length));
        this.setPos(ground.x, ground.y + HEIGHT, ground.z);
        this.setYRot(level.getRandom().nextFloat() * 360.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COLOR, 0);
    }

    public int colorIndex() {
        return this.entityData.get(DATA_COLOR);
    }

    public int color() {
        return COLORS[Math.floorMod(this.colorIndex(), COLORS.length)];
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ModParticles.GLITCH_PIXEL.get(), this.getX() + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getY() + 0.6, this.getZ() + (this.random.nextDouble() - 0.5) * 0.4, 0.0, 0.05, 0.0);
            }
            return;
        }
        int t = this.tickCount;
        double fallen = V0 * t + G * t * (t + 1);
        double y = this.ground.y + HEIGHT - fallen;
        Vec3 before = this.position();
        this.setPos(this.getX(), Math.max(this.ground.y, y), this.getZ());
        this.setDeltaMovement(this.position().subtract(before));
        if (t >= FALL_TICKS || y <= this.ground.y) {
            this.impact((ServerLevel) this.level());
        }
        if (t > FALL_TICKS + 20) {
            this.discard();
        }
    }

    private void impact(ServerLevel level) {
        Vec3 c = this.ground.add(0.0, 0.3, 0.0);
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), c.x, c.y, c.z, 26, 0.5, 0.3, 0.5, 0.16);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | this.color()), c.x, c.y + 0.2, c.z, 1, 0.0, 0.0, 0.0,
                0.0);
        level.playSound(null, c.x, c.y, c.z, ModSounds.ANNI_PIXEL.get(), SoundSource.HOSTILE, 1.4f, 0.8f + this.random.nextFloat() * 0.5f);
        AnniTrollEntity owner = this.ownerId != null && level.getEntity(this.ownerId) instanceof AnniTrollEntity a ? a : null;
        if (owner != null) {
            DamageSource source = this.damageSources().indirectMagic(this, owner);
            for (LivingEntity victim : owner.victims(level, new AABB(c, c).inflate(1.5, 1.6, 1.5))) {
                if (victim.distanceToSqr(c.x, victim.getY(), c.z) < 1.6 * 1.6) {
                    victim.hurtServer(level, source, this.damage);
                }
            }
        }
        this.discard();
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
