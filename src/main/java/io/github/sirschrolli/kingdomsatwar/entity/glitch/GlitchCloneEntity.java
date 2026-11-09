package io.github.sirschrolli.kingdomsatwar.entity.glitch;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ein Fehler-Klon Annis: eine Kopie von ihm in nur einer Farbe (rot, grün, blau, magenta oder cyan), etwas kleiner,
 * durchscheinend und flackernd. Er rennt auf sein Ziel zu und „stürzt ab“, sobald er jemanden erreicht (oder nach
 * dreieinhalb Sekunden): ein Knall aus Pixeln. Ein einziger Treffer lässt ihn zerplatzen, bevor er abstürzt. Keine Beute,
 * keine Erfahrung; wird nicht gespeichert.
 */
public class GlitchCloneEntity extends Monster {
    public static final int[] COLORS = {0xFF3040, 0x40FF70, 0x4080FF, 0xF040C8, 0x30E0F0};
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(GlitchCloneEntity.class, EntityDataSerializers.INT);
    private static final int LIFE = 70;
    private @Nullable UUID ownerId;
    private @Nullable LivingEntity target;
    private Vec3 spread = Vec3.ZERO;

    public GlitchCloneEntity(EntityType<? extends GlitchCloneEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public GlitchCloneEntity(Level level, AnniTrollEntity owner) {
        this(ModEntities.GLITCH_CLONE.get(), level);
        this.ownerId = owner.getUUID();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.STEP_HEIGHT, 1.2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_COLOR, 0);
    }

    /** Wen er jagt, welche Farbe er hat und wohin er zuerst ausschwärmt. */
    public void setup(LivingEntity target, int color, Vec3 spread) {
        this.target = target;
        this.entityData.set(DATA_COLOR, Math.floorMod(color, COLORS.length));
        this.spread = spread;
        this.setDeltaMovement(spread.add(0.0, 0.2, 0.0));
    }

    public int colorIndex() {
        return this.entityData.get(DATA_COLOR);
    }

    public boolean isCloneOf(AnniTrollEntity owner) {
        return owner.getUUID().equals(this.ownerId);
    }

    private @Nullable AnniTrollEntity owner(ServerLevel level) {
        return this.ownerId != null && level.getEntity(this.ownerId) instanceof AnniTrollEntity a && a.isAlive() ? a : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.random.nextInt(2) == 0) {
            this.level().addParticle(ModParticles.GLITCH_PIXEL.get(), this.getRandomX(0.8), this.getY() + this.random.nextDouble() * 3.0,
                    this.getRandomZ(0.8), 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        AnniTrollEntity owner = this.owner(level);
        if (owner == null || this.target == null || !this.target.isAlive()) {
            this.pop(level, false);
            return;
        }
        if (this.tickCount < 8) {
            // erst ausschwärmen
            this.getNavigation().stop();
            this.setDeltaMovement(this.spread.add(0.0, this.getDeltaMovement().y, 0.0));
        } else if (this.tickCount % 5 == 0) {
            this.getNavigation().moveTo(this.target, 1.6);
        }
        this.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        boolean near = !owner.victims(level, this.getBoundingBox().inflate(1.4, 0.5, 1.4)).isEmpty();
        if (near || this.tickCount >= LIFE) {
            this.crash(level, owner);
        }
    }

    /** Er stürzt ab: ein Knall aus Pixeln, der alles ringsum trifft. */
    private void crash(ServerLevel level, AnniTrollEntity owner) {
        Vec3 c = this.position().add(0.0, 1.2, 0.0);
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), c.x, c.y, c.z, 60, 1.0, 0.8, 1.0, 0.18);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | COLORS[this.colorIndex()]), c.x, c.y, c.z, 1, 0.0, 0.0,
                0.0, 0.0);
        level.playSound(null, c.x, c.y, c.z, ModSounds.ANNI_CLONE_CRASH.get(), SoundSource.HOSTILE, 2.0f, 0.9f + this.random.nextFloat() * 0.2f);
        for (LivingEntity victim : owner.victims(level, new AABB(c, c).inflate(2.6, 2.0, 2.6))) {
            if (victim.distanceToSqr(c) < 2.8 * 2.8 && victim.hurtServer(level, this.damageSources().indirectMagic(this, owner), owner.scaled(10.0f))) {
                AnniTrollEntity.push(victim, victim.position().subtract(c).multiply(1.0, 0.0, 1.0).normalize().scale(0.8).add(0.0, 0.4, 0.0));
            }
        }
        this.discard();
    }

    /** Getroffen: er zerplatzt (ohne Knall). */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isRemoved()) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof AnniTrollEntity || attacker instanceof GlitchCloneEntity) {
            return false;
        }
        this.pop(level, true);
        return true;
    }

    private void pop(ServerLevel level, boolean loud) {
        if (this.isRemoved()) {
            return;
        }
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), this.getX(), this.getY(0.5), this.getZ(), loud ? 40 : 16, 0.6, 1.0, 0.6, 0.1);
        if (loud) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.ANNI_CLONE_POP.get(), SoundSource.HOSTILE, 1.5f, 1.2f);
        }
        this.discard();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return null;
    }
}
