package io.github.sirschrolli.kingdomsatwar.entity.voidrealm;

import io.github.sirschrolli.kingdomsatwar.block.voidrealm.StarWebBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Ballen Sternennetz (seit 0.99): Die Sternenweberin spuckt ihn im Bogen. Wo er aufschlägt, spannt sich Sternennetz
 * (eine Weile, dann zerfällt es); wer getroffen wird, nimmt Schaden und ist eine Weile langsam.
 */
public class StarWebShotEntity extends ThrowableItemProjectile {
    public static final float DAMAGE = 5.0f;
    /** So weit spannt sich das Netz um den Aufschlag (Halbmesser in Blöcken). */
    public static final int SPREAD = 1;

    public StarWebShotEntity(EntityType<? extends StarWebShotEntity> type, Level level) {
        super(type, level);
    }

    public StarWebShotEntity(Level level, LivingEntity owner) {
        super(ModEntities.STAR_WEB_SHOT.get(), owner, level, new ItemStack(StarWeaver.STAR_WEB.get()));
    }

    @Override
    protected Item getDefaultItem() {
        return StarWeaver.STAR_WEB.get().asItem();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof StarWeaverEntity) && !(entity instanceof StarSpiderlingEntity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            this.level().addParticle(ParticleTypes.END_ROD, this.getX() - v.x * 0.4, this.getY() - v.y * 0.4, this.getZ() - v.z * 0.4, 0.0, 0.0, 0.0);
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(new DustParticleOptions(0xE4F0FF, 1.0f), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (this.tickCount > 100) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel level) || this.isRemoved()) {
            return;
        }
        BlockPos at = switch (result) {
            case BlockHitResult block -> block.getBlockPos().relative(block.getDirection());
            case EntityHitResult entity -> entity.getEntity().blockPosition();
            default -> this.blockPosition();
        };
        this.spin(level, at);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.8), e -> e.isAlive() && e != owner
                && !(e instanceof StarWeaverEntity) && !(e instanceof StarSpiderlingEntity) && !(e instanceof ArmorStand)
                && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (victim.hurtServer(level, this.damageSources().thrown(this, owner), DAMAGE)) {
                victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2), owner);
            }
        }
        level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.STAR_WEAVER_WEB.get(), SoundSource.HOSTILE, 1.4f, 1.4f);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 16, 0.6, 0.3, 0.6, 0.05);
        level.sendParticles(new DustParticleOptions(0xE4F0FF, 1.4f), this.getX(), this.getY(), this.getZ(), 20, 0.8, 0.3, 0.8, 0.0);
        this.discard();
    }

    /** Spannt um die Stelle Sternennetz in die Luft (nur wo nichts ist; es zerfällt nach einer Weile). */
    private void spin(ServerLevel level, BlockPos at) {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-SPREAD, 0, -SPREAD), at.offset(SPREAD, 1, SPREAD))) {
            boolean corner = Math.abs(p.getX() - at.getX()) + Math.abs(p.getZ() - at.getZ()) == 2;
            if ((corner && this.random.nextInt(2) == 0) || (p.getY() > at.getY() && this.random.nextInt(3) > 0)) {
                continue;
            }
            if (level.getBlockState(p).isAir()) {
                level.setBlock(p, StarWeaver.STAR_WEB.get().defaultBlockState().setValue(StarWebBlock.TEMPORARY, true), 3);
            }
        }
    }
}
