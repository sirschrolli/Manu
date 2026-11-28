package io.github.sirschrolli.kingdomsatwar.entity.nether;

import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist;
import io.github.sirschrolli.kingdomsatwar.registry.FlameGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Splitter aus Feueropal, den Ignara im Opalregen aus der Höhe auf ihre Gegner stürzen lässt: Er zieht eine Spur aus
 * Flammen hinter sich her und zerspringt beim Aufschlag in einem kleinen Feuerball – wer im Umkreis von {@link #RADIUS}
 * Blöcken steht, brennt. Blöcke beschädigt er nicht.
 */
public class OpalShardEntity extends ThrowableItemProjectile {
    public static final double RADIUS = 1.8;
    private float damage = 7.0f;

    public OpalShardEntity(EntityType<? extends OpalShardEntity> type, Level level) {
        super(type, level);
    }

    public OpalShardEntity(Level level, LivingEntity owner) {
        super(ModEntities.OPAL_SHARD.get(), owner, level, new ItemStack(FlameGear.FIRE_OPAL.get()));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return FlameGear.FIRE_OPAL.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof IgnaraEntity) && !(entity instanceof Glutgeist);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            for (int i = 0; i < 3; i++) {
                double f = i * 0.35;
                level.addParticle(ParticleTypes.FLAME, this.getX() - v.x * f + (this.random.nextDouble() - 0.5) * 0.15,
                        this.getY() + 0.15 - v.y * f, this.getZ() - v.z * f + (this.random.nextDouble() - 0.5) * 0.15, 0.0, 0.01, 0.0);
            }
            if (this.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.SMALL_FLAME, this.getX(), this.getY() + 0.2, this.getZ(), 0.0, 0.03, 0.0);
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

    /** Zerspringt: Splitter und Flammen spritzen, wer nahe steht, brennt. */
    private void burst(ServerLevel level) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        level.playSound(null, x, y, z, ModSounds.IGNARA_SHARD.get(), SoundSource.HOSTILE, 1.2f, 1.3f + this.random.nextFloat() * 0.3f);
        level.sendParticles(ParticleTypes.FLAME, x, y + 0.2, z, 24, 0.6, 0.2, 0.6, 0.06);
        level.sendParticles(ParticleTypes.LAVA, x, y + 0.2, z, 4, 0.4, 0.1, 0.4, 0.0);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFF8A2A), x, y + 0.3, z, 1, 0.0, 0.0, 0.0, 0.0);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS),
                e -> e.isAlive() && e != owner && !(e instanceof IgnaraEntity) && !(e instanceof Glutgeist) && !(e instanceof ArmorStand)
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (victim.distanceToSqr(x, y, z) > (RADIUS + 0.6) * (RADIUS + 0.6)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().thrown(this, owner), this.damage)) {
                victim.igniteForSeconds(3.0f);
            }
        }
    }
}
