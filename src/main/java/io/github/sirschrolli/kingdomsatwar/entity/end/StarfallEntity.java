package io.github.sirschrolli.kingdomsatwar.entity.end;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein fallender Stern: Asteria ruft ihn beim Sternenregen auf einen leuchtenden Ring herab (und die Sternenlanze bei jedem
 * dritten Treffer einen kleinen auf ihr Ziel). Er zieht einen Schweif aus Licht hinter sich her und zerspringt beim
 * Aufschlag in Sternenlicht: Wer im Umkreis steht, wird getroffen und fortgestoßen. Blöcke beschädigt er nicht.
 */
public class StarfallEntity extends ThrowableItemProjectile {
    /** Halbmesser des Einschlags (ein großer Stern; ein kleiner trifft nur halb so weit). */
    public static final double RADIUS = 2.4;
    public static final double SPEED = 1.4;
    private static final EntityDataAccessor<Boolean> DATA_SMALL = SynchedEntityData.defineId(StarfallEntity.class, EntityDataSerializers.BOOLEAN);
    private float damage = 10.0f;

    public StarfallEntity(EntityType<? extends StarfallEntity> type, Level level) {
        super(type, level);
    }

    public StarfallEntity(Level level, LivingEntity owner) {
        super(ModEntities.STARFALL.get(), owner, level, new ItemStack(StarBlocks.STAR_SHARD.get()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SMALL, false);
    }

    /** Ein kleiner Stern (von der Sternenlanze): halber Einschlag, halbe Größe. */
    public void setSmall(boolean small) {
        this.entityData.set(DATA_SMALL, small);
    }

    public boolean isSmall() {
        return this.entityData.get(DATA_SMALL);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    private double radius() {
        return this.isSmall() ? RADIUS * 0.6 : RADIUS;
    }

    @Override
    protected Item getDefaultItem() {
        return StarBlocks.STAR_SHARD.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    /** Lässt ihn aus {@code height} Blöcken Höhe (leicht schräg) genau auf {@code mark} fallen. */
    public void dropOnto(Vec3 mark, double height) {
        double a = this.random.nextDouble() * Math.PI * 2.0;
        double off = height * 0.18;
        Vec3 from = mark.add(Math.cos(a) * off, height, Math.sin(a) * off);
        this.setPos(from.x, from.y, from.z);
        this.setDeltaMovement(mark.subtract(from).normalize().scale(SPEED));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof AsteriaEntity) && entity != this.getOwner();
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            int n = this.isSmall() ? 1 : 3;
            for (int i = 0; i < n; i++) {
                double f = i * 0.35;
                level.addParticle(ParticleTypes.END_ROD, this.getX() - v.x * f + (this.random.nextDouble() - 0.5) * 0.2,
                        this.getY() - v.y * f + (this.random.nextDouble() - 0.5) * 0.2, this.getZ() - v.z * f + (this.random.nextDouble() - 0.5) * 0.2,
                        -v.x * 0.05, -v.y * 0.05, -v.z * 0.05);
            }
            if (this.random.nextInt(this.isSmall() ? 4 : 2) == 0) {
                level.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (this.tickCount > 120) {
            this.discard();
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

    /** Zerspringt in Sternenlicht: wer nahe steht, wird getroffen und fortgestoßen. */
    private void burst(ServerLevel level) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        boolean small = this.isSmall();
        double radius = this.radius();
        level.playSound(null, x, y, z, ModSounds.ASTERIA_STAR_IMPACT.get(), small ? SoundSource.PLAYERS : SoundSource.HOSTILE,
                small ? 1.0f : 2.2f, (small ? 1.4f : 0.9f) + this.random.nextFloat() * 0.25f);
        level.sendParticles(ParticleTypes.END_ROD, x, y + 0.3, z, small ? 14 : 36, radius * 0.3, 0.2, radius * 0.3, small ? 0.08 : 0.16);
        level.sendParticles(ParticleTypes.FIREWORK, x, y + 0.3, z, small ? 6 : 16, 0.3, 0.2, 0.3, 0.1);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFB8E2FF), x, y + 0.4, z, 1, 0.0, 0.0, 0.0, 0.0);
        Entity owner = this.getOwner();
        boolean byPlayer = owner instanceof Player;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius + 0.5),
                e -> e.isAlive() && e != owner && !(e instanceof AsteriaEntity) && !(e instanceof ArmorStand)
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator() || byPlayer))
                        && !(!byPlayer && e instanceof Enemy)
                        && !(byPlayer && e instanceof net.minecraft.world.entity.OwnableEntity pet && pet.getOwner() == owner))) {
            Vec3 to = victim.position().subtract(x, y, z);
            if (to.lengthSqr() > (radius + 0.6) * (radius + 0.6)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().indirectMagic(this, owner), this.damage)) {
                Vec3 away = new Vec3(to.x, 0.0, to.z);
                away = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize();
                double k = small ? 0.3 : 0.6;
                victim.push(away.x * k, small ? 0.2 : 0.45, away.z * k);
                victim.needsSync = true;
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Small", this.isSmall());
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSmall(input.getBooleanOr("Small", false));
        this.damage = input.getFloatOr("Damage", 10.0f);
    }
}
