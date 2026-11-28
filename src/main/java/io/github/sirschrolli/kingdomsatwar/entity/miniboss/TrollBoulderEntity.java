package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import io.github.sirschrolli.kingdomsatwar.entity.creature.CaveTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Felsbrocken des Trollvaters (seit 1.15): ein Klotz aus dem Höhlenboden, den Grumbold über den Kopf stemmt und im
 * Bogen schleudert. Er überschlägt sich im Flug und zerschellt beim Aufschlag – wer im Umkreis von {@link #RADIUS} Blöcken
 * steht, nimmt Schaden und fliegt; in Rage zerspringt er in Splitter, die weiterfliegen. Blöcke beschädigt er nicht.
 */
public class TrollBoulderEntity extends ThrowableProjectile {
    public static final double RADIUS = 2.4;
    public static final double GRAVITY = 0.05;
    private static final EntityDataAccessor<BlockState> DATA_BLOCK = SynchedEntityData.defineId(TrollBoulderEntity.class,
            EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> DATA_HELD = SynchedEntityData.defineId(TrollBoulderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SMALL = SynchedEntityData.defineId(TrollBoulderEntity.class, EntityDataSerializers.BOOLEAN);
    /** Halbmesser eines kleinen Brockens, der von der Decke fällt. */
    public static final double SMALL_RADIUS = 1.5;
    private float damage = 10.0f;
    private boolean split;
    private int flight;

    public TrollBoulderEntity(EntityType<? extends TrollBoulderEntity> type, Level level) {
        super(type, level);
    }

    /** Ein Brocken, den {@code owner} über dem Kopf hält (bis {@link #lob}). */
    public TrollBoulderEntity(Level level, LivingEntity owner, BlockState block) {
        super(ModEntities.TROLL_BOULDER.get(), level);
        this.setOwner(owner);
        this.entityData.set(DATA_BLOCK, block);
        this.entityData.set(DATA_HELD, true);
        Vec3 at = held(owner);
        this.setPos(at.x, at.y, at.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        builder.define(DATA_HELD, false);
        builder.define(DATA_SMALL, false);
    }

    /** Ein kleiner Brocken, der bei {@code at} von der Decke fällt (das Gebrüll des Trollvaters lässt die Höhle beben). */
    public static TrollBoulderEntity drop(ServerLevel level, LivingEntity owner, Vec3 at, BlockState block, float damage) {
        TrollBoulderEntity rock = new TrollBoulderEntity(ModEntities.TROLL_BOULDER.get(), level);
        rock.setOwner(owner);
        rock.entityData.set(DATA_BLOCK, block);
        rock.entityData.set(DATA_SMALL, true);
        rock.damage = damage;
        rock.setPos(at.x, at.y, at.z);
        rock.setDeltaMovement(0.0, -0.1, 0.0);
        level.addFreshEntity(rock);
        return rock;
    }

    /** Ein kleiner Brocken von der Decke? */
    public boolean isSmall() {
        return this.entityData.get(DATA_SMALL);
    }

    public BlockState blockState() {
        return this.entityData.get(DATA_BLOCK);
    }

    public boolean isHeld() {
        return this.entityData.get(DATA_HELD);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    /** In Rage: zerspringt beim Aufschlag in Splitter. */
    public void setSplit(boolean split) {
        this.split = split;
    }

    /** Wo er ihn hält: über dem Kopf, ein wenig vor dem Gesicht. */
    public static Vec3 held(Entity owner) {
        Vec3 look = Vec3.directionFromRotation(0.0f, owner instanceof LivingEntity l ? l.yBodyRot : owner.getYRot());
        return owner.position().add(look.scale(0.2)).add(0.0, owner.getBbHeight() * 1.08, 0.0);
    }

    @Override
    protected double getDefaultGravity() {
        return this.isHeld() ? 0.0 : GRAVITY;
    }

    /**
     * Wirft im Bogen nach {@code to}: der flachere der beiden Wurfwinkel, der bei {@code speed} Blöcken je Tick dort ankommt;
     * reicht die Kraft nicht, im Winkel der größten Weite.
     */
    public void lob(Vec3 to, double speed) {
        this.entityData.set(DATA_HELD, false);
        this.flight = 0;
        Vec3 from = this.position();
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double dy = to.y - from.y;
        double flat = Math.sqrt(dx * dx + dz * dz);
        if (flat < 1.0e-3) {
            this.setDeltaMovement(0.0, -0.2, 0.0);
            return;
        }
        double d = flat * 1.04;
        double v2 = speed * speed;
        double disc = v2 * v2 - GRAVITY * (GRAVITY * d * d + 2.0 * dy * v2);
        double angle = disc < 0.0 ? Math.PI / 4.0 : Math.atan((v2 - Math.sqrt(disc)) / (GRAVITY * d));
        double horizontal = Math.cos(angle) * speed;
        this.setDeltaMovement(dx / flat * horizontal, Math.sin(angle) * speed, dz / flat * horizontal);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof CaveTrollEntity) && !this.isHeld();
    }

    /** Drehung im Flug (im Client). */
    public float spin(float partialTicks) {
        return this.isHeld() ? 0.0f : (this.flight + partialTicks) * 13.0f;
    }

    @Override
    public void tick() {
        Level level = this.level();
        if (this.isHeld()) {
            this.baseTick();
            if (this.getOwner() instanceof LivingEntity owner && owner.isAlive()) {
                Vec3 at = held(owner);
                this.setDeltaMovement(at.subtract(this.position()));
                this.setPos(at);
                if (level instanceof ServerLevel server && this.tickCount > 120) {
                    this.shatter(server);
                }
            } else if (level instanceof ServerLevel server) {
                this.shatter(server);
            }
            return;
        }
        super.tick();
        this.flight++;
        if (level.isClientSide()) {
            if (this.random.nextInt(2) == 0) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, this.blockState()), this.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getY() + 0.2, this.getZ() + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.0, 0.0);
            }
        } else if (this.flight > 140) {
            this.shatter((ServerLevel) level);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved() && !this.isHeld()) {
            this.burst(level);
            this.discard();
        }
    }

    /** Zerschellt: Geröll fliegt, wer nahe steht, wird getroffen; in Rage fliegen Splitter weiter. */
    private void burst(ServerLevel level) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        level.playSound(null, x, y, z, ModSounds.TROLL_SMASH.get(), SoundSource.HOSTILE, this.isSmall() ? 1.4f : 2.4f,
                (this.isSmall() ? 0.9f : 0.6f) + this.random.nextFloat() * 0.15f);
        level.playSound(null, x, y, z, SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 2.0f, 0.5f);
        boolean small = this.isSmall();
        double radius = small ? SMALL_RADIUS : RADIUS;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState()), x, y + 0.4, z, small ? 30 : 70, radius * 0.38, 0.5,
                radius * 0.38, 0.25);
        if (!small) {
            level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.3, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.3, z, 6, 0.6, 0.2, 0.6, 0.01);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius), e -> e.isAlive() && e != owner
                && (owner instanceof MiniBoss boss ? boss.miniBoss().foe(e) : !(e instanceof CaveTrollEntity)))) {
            if (victim.distanceToSqr(x, y, z) > (radius + 0.7) * (radius + 0.7)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().thrown(this, owner), this.damage)) {
                MiniBossCore.knock(victim, new Vec3(x, y, z), small ? 0.3 : 0.8, small ? 0.2 : 0.45);
            }
        }
        if (this.split && owner instanceof LivingEntity thrower) {
            for (int i = 0; i < 5; i++) {
                double a = i * Math.PI * 2.0 / 5.0 + this.random.nextDouble() * 0.5;
                Vec3 v = new Vec3(Math.cos(a) * 0.38, 0.42 + this.random.nextDouble() * 0.12, Math.sin(a) * 0.38);
                MonsterShot.shoot(level, thrower, MonsterShot.Kind.ROCK, new Vec3(x, y + 0.6, z), v, 4.0f);
            }
        }
    }

    /** Zerfällt (der Wurf ist abgebrochen oder der Troll fort): ein wenig Staub, kein Schaden. */
    public void shatter(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState()), this.getX(), this.getY() + 0.4, this.getZ(), 30, 0.4,
                0.4, 0.4, 0.1);
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("BlockState", BlockState.CODEC, this.blockState());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_BLOCK, input.read("BlockState", BlockState.CODEC).orElse(Blocks.MOSSY_COBBLESTONE.defaultBlockState()));
        this.entityData.set(DATA_HELD, false);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
