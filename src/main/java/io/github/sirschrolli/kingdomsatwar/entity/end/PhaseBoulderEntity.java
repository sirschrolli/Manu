package io.github.sirschrolli.kingdomsatwar.entity.end;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Phasenbrocken (seit 1.5): ein Block aus Endstein, Purpur oder Obsidian, den Phasmor mit der Kraft der Leere aus dem
 * Heiligtum reißt. Zuerst kreist er mit seinen Brüdern um das Haupt des Königs ({@link #isHeld()}), dann fliegt er auf sein
 * Ziel und zerschellt – wer im Umkreis von {@link #RADIUS} Blöcken steht, nimmt Schaden. Blöcke beschädigt er nicht (der
 * Brocken ist ein Abbild aus der Leere, das Heiligtum bleibt heil).
 */
public class PhaseBoulderEntity extends ThrowableProjectile {
    public static final double RADIUS = 1.7;
    /** So schnell fliegt er (Blöcke je Tick). */
    public static final double SPEED = 1.25;
    private static final EntityDataAccessor<BlockState> DATA_BLOCK = SynchedEntityData.defineId(PhaseBoulderEntity.class,
            EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> DATA_HELD = SynchedEntityData.defineId(PhaseBoulderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SLOT = SynchedEntityData.defineId(PhaseBoulderEntity.class, EntityDataSerializers.INT);
    private float damage = 8.0f;
    private int flight;

    public PhaseBoulderEntity(EntityType<? extends PhaseBoulderEntity> type, Level level) {
        super(type, level);
    }

    /** Ein Brocken, der um {@code king} kreist (Platz {@code slot} von {@code count}). */
    public PhaseBoulderEntity(Level level, PhasmorEntity king, BlockState block, int slot, int count) {
        super(ModEntities.PHASE_BOULDER.get(), level);
        this.setOwner(king);
        this.entityData.set(DATA_BLOCK, block);
        this.entityData.set(DATA_HELD, true);
        this.entityData.set(DATA_SLOT, slot * 360 / Math.max(1, count));
        Vec3 at = orbit(king, slot * 360 / Math.max(1, count), level.getGameTime(), 0.0f);
        this.setPos(at.x, king.getY() + 0.2, at.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK, Blocks.END_STONE_BRICKS.defaultBlockState());
        builder.define(DATA_HELD, false);
        builder.define(DATA_SLOT, 0);
    }

    public BlockState blockState() {
        return this.entityData.get(DATA_BLOCK);
    }

    /** Kreist er noch um den König? */
    public boolean isHeld() {
        return this.entityData.get(DATA_HELD);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected double getDefaultGravity() {
        return this.isHeld() ? 0.0 : 0.02;
    }

    /** Wo der Brocken kreist: in drei Blöcken Abstand über den Schultern des Königs, langsam um ihn herum, sacht auf und ab. */
    public static Vec3 orbit(Entity king, int slotDegrees, long gameTime, float partialTicks) {
        double t = gameTime + partialTicks;
        double a = Math.toRadians(slotDegrees) + t * 0.12;
        double bob = Math.sin(t * 0.2 + slotDegrees) * 0.25;
        return new Vec3(king.getX() + Math.cos(a) * 2.6, king.getY() + king.getBbHeight() * 0.92 + bob, king.getZ() + Math.sin(a) * 2.6);
    }

    /** Wirft ihn auf sein Ziel (dorthin, wo es gleich sein wird; der Bogen ist flach). */
    public void launchAt(LivingEntity target) {
        Vec3 aim = target.position().add(target.getDeltaMovement().multiply(6.0, 0.0, 6.0)).add(0.0, target.getBbHeight() * 0.5, 0.0);
        Vec3 dir = aim.subtract(this.position());
        double distance = dir.length();
        double ticks = distance / SPEED;
        // die Schwerkraft ausgleichen: etwas höher zielen
        dir = dir.add(0.0, 0.5 * 0.02 * ticks * ticks, 0.0).normalize().scale(SPEED);
        this.entityData.set(DATA_HELD, false);
        this.setDeltaMovement(dir);
        this.flight = 0;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof PhasmorEntity) && !(entity instanceof PhasmorIllusion);
    }

    @Override
    public void tick() {
        Level level = this.level();
        if (this.isHeld()) {
            this.baseTick();
            if (this.getOwner() instanceof PhasmorEntity king && king.isAlive()) {
                Vec3 at = orbit(king, this.entityData.get(DATA_SLOT), level.getGameTime(), 0.0f);
                Vec3 now = this.position();
                // erst aus dem Boden aufsteigen, dann kreisen
                Vec3 next = this.tickCount < 12 ? now.lerp(at, 0.25) : at;
                this.setDeltaMovement(next.subtract(now));
                this.setPos(next);
                if (level.isClientSide() && this.random.nextInt(2) == 0) {
                    level.addParticle(new DustParticleOptions(PhasmorEntity.PHASIT, 0.9f), this.getX() + (this.random.nextDouble() - 0.5) * 0.9,
                            this.getY() + this.random.nextDouble() * 0.9, this.getZ() + (this.random.nextDouble() - 0.5) * 0.9, 0.0, -0.02, 0.0);
                }
                if (!level.isClientSide() && this.tickCount > 160) {
                    this.shatter((ServerLevel) level);
                }
            } else if (level instanceof ServerLevel server) {
                this.shatter(server);
            }
            return;
        }
        super.tick();
        if (level.isClientSide()) {
            level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.4, this.getZ(), 0.0, 0.0, 0.0);
            if (this.random.nextInt(2) == 0) {
                level.addParticle(new DustParticleOptions(PhasmorEntity.PHASIT, 1.0f), this.getX(), this.getY() + 0.4, this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (++this.flight > 100) {
            this.shatter((ServerLevel) level);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            this.burst(level);
            this.discard();
        }
    }

    /** Zerschellt im Flug: Splitter fliegen, wer nahe steht, wird getroffen. */
    private void burst(ServerLevel level) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        level.playSound(null, x, y, z, ModSounds.PHASMOR_IMPACT.get(), SoundSource.HOSTILE, 2.6f, 0.85f + this.random.nextFloat() * 0.3f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState()), x, y + 0.4, z, 60, 0.7, 0.5, 0.7, 0.25);
        level.sendParticles(new DustParticleOptions(PhasmorEntity.PHASIT, 1.4f), x, y + 0.5, z, 24, 0.6, 0.5, 0.6, 0.0);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 0.5, z, 20, 0.5, 0.5, 0.5, 0.1);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS),
                e -> e.isAlive() && e != owner && !(e instanceof PhasmorEntity) && !(e instanceof PhasmorIllusion) && !(e instanceof ArmorStand)
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (victim.distanceToSqr(x, y, z) > (RADIUS + 0.7) * (RADIUS + 0.7)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().thrown(this, owner), this.damage)) {
                Vec3 out = victim.position().subtract(x, victim.getY(), z);
                out = out.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : out.normalize();
                victim.push(out.x * 0.6, 0.35, out.z * 0.6);
                victim.needsSync = true;
            }
        }
    }

    /** Zerfällt (der Wurf ist abgebrochen oder der König fort): ein wenig Staub, kein Schaden. */
    public void shatter(ServerLevel level) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.blockState()), this.getX(), this.getY() + 0.4, this.getZ(), 30, 0.4,
                0.4, 0.4, 0.1);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 0.4, this.getZ(), 16, 0.4, 0.4, 0.4, 0.05);
        this.discard();
    }

    /** Drehung im Flug (im Client): je nach Platz ein wenig anders. */
    public float spin(float partialTicks) {
        return (this.tickCount + partialTicks) * (this.isHeld() ? 4.0f : 14.0f) + this.entityData.get(DATA_SLOT);
    }

    /** Wie weit er schon aus dem Boden gestiegen ist (0–1, im Client: er wächst beim Aufsteigen). */
    public float rise(float partialTicks) {
        return Mth.clamp((this.tickCount + partialTicks) / 10.0f, 0.2f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("BlockState", BlockState.CODEC, this.blockState());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_BLOCK, input.read("BlockState", BlockState.CODEC).orElse(Blocks.END_STONE_BRICKS.defaultBlockState()));
        this.entityData.set(DATA_HELD, false);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
