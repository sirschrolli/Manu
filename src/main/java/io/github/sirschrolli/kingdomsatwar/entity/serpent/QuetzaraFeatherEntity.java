package io.github.sirschrolli.kingdomsatwar.entity.serpent;

import io.github.sirschrolli.kingdomsatwar.item.serpent.SerpentFangItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Eine Feder aus Quetzaras Krause (seit 1.10): smaragdgrün mit goldenem Kiel, die Spitze in Gift getaucht. Sie fliegt wie ein
 * Pfeil, wer getroffen wird, ist vergiftet; im Boden bleibt sie kurz stecken, dann zerfällt sie. Aufheben kann man sie nicht.
 */
public class QuetzaraFeatherEntity extends AbstractArrow {
    public static final float DAMAGE = 5.0f;
    private static final int LIFETIME = 120;

    public QuetzaraFeatherEntity(EntityType<? extends QuetzaraFeatherEntity> type, Level level) {
        super(type, level);
    }

    public QuetzaraFeatherEntity(Level level, LivingEntity owner, Vec3 from) {
        super(ModEntities.QUETZARA_FEATHER.get(), owner, level, new ItemStack(Items.FEATHER), null);
        this.setPos(from.x, from.y, from.z);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGroundTime > 30 || this.tickCount > LIFETIME) {
            this.discard();
            return;
        }
        if (this.level().isClientSide() && !this.isInGround() && this.tickCount % 2 == 0) {
            Vec3 v = this.getDeltaMovement();
            this.level().addParticle(new DustParticleOptions(QuetzaraEntity.JADE, 0.7f), this.getX() - v.x * 0.5, this.getY() - v.y * 0.5,
                    this.getZ() - v.z * 0.5, 0.0, 0.0, 0.0);
        }
    }

    /** Steckt die Feder (im Boden oder in einer Wand)? */
    public boolean stuck() {
        return this.isInGround();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.025;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof QuetzaraEntity) && !(entity instanceof QuetzaraPart)
                && !(entity instanceof ShedSkinEntity) && !(entity instanceof QuetzaraFeatherEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner = this.getOwner();
        if (this.level() instanceof ServerLevel level) {
            DamageSource source = this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null);
            if (entity.hurtServer(level, source, DAMAGE) && entity instanceof LivingEntity victim) {
                SerpentFangItem.poison(victim, owner, 0, 70);
                Vec3 v = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
                if (v.lengthSqr() > 1.0e-4) {
                    v = v.normalize().scale(0.25);
                    victim.push(v.x, 0.05, v.z);
                }
            }
            this.playSound(ModSounds.QUETZARA_FEATHER_HIT.get(), 1.0f, 1.1f + this.random.nextFloat() * 0.2f);
        }
        this.discard();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.FEATHER);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return ModSounds.QUETZARA_FEATHER_HIT.get();
    }
}
