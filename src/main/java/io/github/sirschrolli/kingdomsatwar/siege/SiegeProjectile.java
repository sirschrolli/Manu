package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

/**
 * Geschosse der Belagerungswaffen: Steinkugel (Katapult), schwerer Brocken (Tribok) und Ballistenbolzen. Kugeln und
 * Brocken zerschlagen Mauerwerk um den Einschlag und schleudern Wesen fort; ein Bolzen durchschlägt Glas, trifft hart
 * und bleibt in der Wand stecken.
 */
public class SiegeProjectile extends Projectile {
    private static final EntityDataAccessor<Byte> DATA_SIEGE = SynchedEntityData.defineId(SiegeProjectile.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_STUCK = SynchedEntityData.defineId(SiegeProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final int STUCK_LIFE = 600;
    /** So viele Wesen durchschlägt ein Bolzen höchstens. */
    private static final int PIERCE = 3;

    private int life;
    private int stuckTicks;
    private final java.util.Set<Integer> pierced = new java.util.HashSet<>();
    /** Darf ein Spieler den steckenden Bolzen wieder aufheben (nur, wenn ein Spieler ihn verschossen hat)? */
    private boolean pickup;

    public SiegeProjectile(EntityType<? extends SiegeProjectile> type, Level level) {
        super(type, level);
    }

    /** Die Waffe schießt: Geschoss an ihrem Abschusspunkt mit ihrer Abschussgeschwindigkeit. */
    static void launch(ServerLevel level, SiegeWeaponEntity weapon, @Nullable LivingEntity shooter) {
        SiegeType siege = weapon.siege();
        SiegeProjectile p = new SiegeProjectile(ModEntities.SIEGE_PROJECTILE.get(), level);
        p.entityData.set(DATA_SIEGE, (byte) siege.ordinal());
        p.setOwner(shooter != null ? shooter : weapon);
        p.pickup = shooter instanceof net.minecraft.world.entity.player.Player player && !player.hasInfiniteMaterials();
        Vec3 from = weapon.launchPoint();
        // der Abschusspunkt ist die Mitte des Geschosses
        p.setPos(from.x, from.y - p.getBbHeight() / 2.0, from.z);
        Vec3 v = weapon.launchVelocity();
        p.setDeltaMovement(v);
        p.setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        p.setXRot((float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG));
        p.yRotO = p.getYRot();
        p.xRotO = p.getXRot();
        level.addFreshEntity(p);
        SiegeImpacts.launchEffects(level, weapon, from);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIEGE, (byte) 0);
        builder.define(DATA_STUCK, false);
    }

    public SiegeType siege() {
        return SiegeType.values()[Mth.clamp(this.entityData.get(DATA_SIEGE), 0, SiegeType.values().length - 1)];
    }

    public boolean stuck() {
        return this.entityData.get(DATA_STUCK);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Siege", this.siege().ordinal());
        output.putBoolean("Stuck", this.stuck());
        output.putInt("StuckTicks", this.stuckTicks);
        output.putBoolean("Pickup", this.pickup);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_SIEGE, (byte) input.getIntOr("Siege", 0));
        this.entityData.set(DATA_STUCK, input.getBooleanOr("Stuck", false));
        this.stuckTicks = input.getIntOr("StuckTicks", 0);
        this.pickup = input.getBooleanOr("Pickup", false);
    }

    @Override
    public void tick() {
        if (this.stuck()) {
            super.tick();
            if (!this.level().isClientSide() && ++this.stuckTicks > STUCK_LIFE) {
                this.discard();
            }
            return;
        }
        SiegeType siege = this.siege();
        Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(new Vec3(v.x, v.y - siege.gravity(), v.z).scale(siege.drag()));
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        boolean impact = hit.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hit);
        this.setPos(impact ? hit.getLocation() : this.position().add(this.getDeltaMovement()));
        this.updateRotation();
        super.tick();
        if (impact && this.isAlive()) {
            this.hitTargetOrDeflectSelf(hit);
        }
        if (this.level().isClientSide()) {
            if (siege != SiegeType.BALLISTA && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (++this.life > 600 || this.getY() < this.level().getMinY() - 16) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity instanceof SiegeProjectile || this.pierced.contains(entity.getId())) {
            return false;
        }
        Entity owner = this.getOwner();
        // die eigene Waffe und ihr Schütze werden nicht getroffen
        return entity != owner && !(owner != null && owner.getVehicle() == entity) && !(entity.getVehicle() != null && entity.getVehicle() == owner);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.siege() != SiegeType.BALLISTA) {
            SiegeImpacts.stone(level, this, hit.getLocation(), hit.getEntity());
            this.discard();
            return;
        }
        // ein Bolzen durchschlägt bis zu drei Wesen und verliert dabei an Wucht
        this.pierced.add(hit.getEntity().getId());
        if (SiegeImpacts.bolt(level, this, hit.getEntity())) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.7));
            if (this.pierced.size() >= PIERCE || hit.getEntity() instanceof SiegeWeaponEntity) {
                this.discard();
            }
        }
    }

    /** Ein steckender Bolzen lässt sich aufheben, wenn ihn ein Spieler verschossen hat. */
    @Override
    public void playerTouch(net.minecraft.world.entity.player.Player player) {
        if (this.level().isClientSide() || !this.stuck() || !this.pickup || this.stuckTicks < 10) {
            return;
        }
        if (player.getInventory().add(new net.minecraft.world.item.ItemStack(io.github.sirschrolli.kingdomsatwar.registry.ModItems.BALLISTA_BOLT.get()))) {
            player.take(this, 1);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.siege() != SiegeType.BALLISTA) {
            SiegeImpacts.stone(level, this, hit.getLocation(), null);
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (SiegeImpacts.shatters(state)) {
            // Glas zerspringt, der Bolzen fliegt weiter
            level.destroyBlock(pos, false, this);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
            return;
        }
        // stecken bleiben
        Vec3 at = hit.getLocation().subtract(this.getDeltaMovement().normalize().scale(0.6));
        this.setPos(at.x, at.y, at.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(DATA_STUCK, true);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.TRIDENT_HIT, SoundSource.NEUTRAL, 1.2f, 0.6f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state.isAir() ? Blocks.STONE.defaultBlockState() : state), at.x, at.y, at.z,
                14, 0.2, 0.2, 0.2, 0.1);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide() && !this.stuck() && this.siege() != SiegeType.BALLISTA && hit.getType() == HitResult.Type.BLOCK) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 192.0 * 192.0;
    }
}
