package io.github.sirschrolli.kingdomsatwar.entity.arena;

import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der geworfene Sturmdreizack (seit 1.9): Er fliegt mit den Zinken voran, ein Windfaden hinter sich; wo er aufschlägt (in
 * einem Wesen oder im Boden), entfesselt er eine Böe – wer nah steht, nimmt Schaden und fliegt. Danach kehrt er von selbst in
 * die Hand zurück, die ihn warf: zu Ventor, der ihn gleich wieder schleudert, oder zum Spieler (wie mit Treue).
 */
public class StormTridentEntity extends AbstractArrow {
    /** So schnell fliegt er (Blöcke je Tick). */
    public static final double SPEED = 2.2;
    public static final float PLAYER_DAMAGE = 9.0f;
    public static final double BURST_RADIUS = 2.6;
    public static final float BURST_DAMAGE = 4.0f;
    private static final EntityDataAccessor<Boolean> DATA_FOIL = SynchedEntityData.defineId(StormTridentEntity.class, EntityDataSerializers.BOOLEAN);

    private boolean dealtDamage;
    private boolean burst;
    private float damage = PLAYER_DAMAGE;
    public int clientReturnTicks;

    public StormTridentEntity(EntityType<? extends StormTridentEntity> type, Level level) {
        super(type, level);
    }

    /** Geworfen von {@code owner} (der Sturmdreizack fliegt mit allem, was an ihm ist). */
    public StormTridentEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(ModEntities.STORM_TRIDENT.get(), owner, level, stack, null);
        this.entityData.set(DATA_FOIL, stack.hasFoil());
    }

    /** Von Ventor geworfen: aus {@code from} auf {@code to} (mit Vorhalt gegen das Absinken); aufheben kann ihn niemand. */
    public static StormTridentEntity thrownBy(ServerLevel level, LivingEntity owner, Vec3 from, Vec3 to, float damage) {
        StormTridentEntity trident = new StormTridentEntity(level, owner, new ItemStack(ArenaGear.STORM_TRIDENT.get()));
        trident.setPos(from.x, from.y, from.z);
        Vec3 dir = to.subtract(from);
        double ticks = dir.length() / SPEED;
        Vec3 v = dir.normalize().scale(SPEED).add(0.0, trident.getDefaultGravity() * ticks * 0.5, 0.0);
        trident.setDeltaMovement(v);
        trident.setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        trident.setXRot((float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG));
        trident.yRotO = trident.getYRot();
        trident.xRotO = trident.getXRot();
        trident.pickup = AbstractArrow.Pickup.DISALLOWED;
        trident.damage = damage;
        return trident;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FOIL, false);
    }

    public boolean isFoil() {
        return this.entityData.get(DATA_FOIL);
    }

    /** Fliegt er gerade zurück (mit dem Griff voran)? */
    public boolean isReturning() {
        return this.isNoPhysics();
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }
        Entity owner = this.getOwner();
        if ((this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.acceptableOwner(owner)) {
                if (this.level() instanceof ServerLevel level && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(level, this.getPickupItem(), 0.1f);
                }
                this.discard();
                return;
            }
            if (owner instanceof VentorEntity ventor && this.position().distanceTo(owner.getEyePosition().subtract(0.0, 0.8, 0.0)) < 2.0) {
                if (!this.level().isClientSide()) {
                    ventor.catchTrident();
                    this.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.HOSTILE, 1.6f, 0.9f);
                }
                this.discard();
                return;
            }
            this.setNoPhysics(true);
            Vec3 to = owner.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + to.y * 0.045, this.getZ());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(to.normalize().scale(0.15)));
            if (this.clientReturnTicks == 0) {
                this.playSound(SoundEvents.TRIDENT_RETURN, 10.0f, 1.0f);
            }
            this.clientReturnTicks++;
        }
        super.tick();
        if (this.level().isClientSide() && !this.isInGround()) {
            Vec3 v = this.getDeltaMovement();
            this.level().addParticle(ParticleTypes.CLOUD, this.getX() - v.x * 0.5, this.getY() - v.y * 0.5, this.getZ() - v.z * 0.5,
                    -v.x * 0.03, -v.y * 0.03, -v.z * 0.03);
        }
    }

    private boolean acceptableOwner(Entity owner) {
        return owner.isAlive() && !(owner instanceof ServerPlayer player && player.isSpectator());
    }

    @Override
    protected @Nullable EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return this.dealtDamage ? null : super.findHitEntity(from, to);
    }

    @Override
    protected Collection<EntityHitResult> findHitEntities(Vec3 from, Vec3 to) {
        EntityHitResult hit = this.findHitEntity(from, to);
        return hit != null ? List.of(hit) : List.of();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof WindVortexEntity)
                && !(this.getOwner() instanceof VentorEntity && entity instanceof VentorEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().trident(this, owner == null ? this : owner);
        float damage = this.damage;
        if (this.level() instanceof ServerLevel level) {
            damage = EnchantmentHelper.modifyDamage(level, this.getWeaponItem(), entity, source, damage);
        }
        this.dealtDamage = true;
        boolean hurt = entity.hurtOrSimulate(source, damage);
        if (hurt && entity instanceof LivingEntity victim) {
            if (this.level() instanceof ServerLevel level) {
                EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(level, victim, source, this.getWeaponItem(), weapon -> this.kill(level));
            }
            this.doKnockback(victim, source);
            this.doPostHurtEffects(victim);
        }
        if (this.level() instanceof ServerLevel level) {
            this.burst(level, this.position());
        }
        if (entity.projectileReceivesSideEffectsOnHit(hurt)) {
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0f, 1.0f);
            this.deflect(ProjectileDeflection.REVERSE, entity, this.owner, false, new Vec3(0.02, 0.2, 0.02));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level) {
            this.burst(level, result.getLocation());
        }
    }

    /** Die Böe beim Aufschlag: Wer nah steht (außer dem, der warf, und seinen Leuten), nimmt Schaden und fliegt. */
    private void burst(ServerLevel level, Vec3 at) {
        if (this.burst) {
            return;
        }
        this.burst = true;
        Entity owner = this.getOwner();
        boolean byPlayer = owner instanceof Player;
        level.playSound(null, at.x, at.y, at.z, ModSounds.STORM_TRIDENT_BURST.get(), byPlayer ? SoundSource.PLAYERS : SoundSource.HOSTILE, 2.2f,
                0.9f + this.random.nextFloat() * 0.2f);
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, at.x, at.y + 0.3, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.SMALL_GUST, at.x, at.y + 0.4, at.z, 10, 0.8, 0.4, 0.8, 0.0);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(BURST_RADIUS + 0.5),
                e -> e.isAlive() && e != owner && !(e instanceof ArmorStand) && !(e instanceof Player p && (p.isCreative() || p.isSpectator()))
                        && !(byPlayer && e instanceof Player) && !(owner instanceof VentorEntity && e instanceof VentorEntity))) {
            Vec3 to = victim.position().subtract(at).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            if (dist > BURST_RADIUS + victim.getBbWidth() * 0.5 || Math.abs(victim.getY() - at.y) > 3.0) {
                continue;
            }
            victim.hurtServer(level, this.damageSources().trident(this, owner == null ? this : owner), BURST_DAMAGE);
            Vec3 dir = dist < 1.0e-3 ? Vec3.ZERO : to.normalize();
            victim.push(dir.x * 0.8, 0.55, dir.z * 0.8);
        }
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getPickupItemStackOrigin();
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ArenaGear.STORM_TRIDENT.get());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.dealtDamage = input.getBooleanOr("DealtDamage", false);
        this.burst = input.getBooleanOr("Burst", false);
        this.damage = input.getFloatOr("Damage", PLAYER_DAMAGE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("DealtDamage", this.dealtDamage);
        output.putBoolean("Burst", this.burst);
        output.putFloat("Damage", this.damage);
    }

    @Override
    public void tickDespawn() {
        if (this.pickup != AbstractArrow.Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99f;
    }

    @Override
    public boolean shouldRender(double camX, double camY, double camZ) {
        return true;
    }
}
