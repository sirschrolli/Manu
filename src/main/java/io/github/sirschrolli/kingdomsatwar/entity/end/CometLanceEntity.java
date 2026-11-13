package io.github.sirschrolli.kingdomsatwar.entity.end;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Asterias Komet: ihre Sternenlanze, als Komet geschleudert – sie fliegt Spitze voran mit einem langen Schweif aus Licht und
 * zerplatzt beim Aufschlag in Sternenlicht (wer nahe steht, wird getroffen, fortgestoßen und leuchtet; Blöcke bleiben
 * heil). Danach fliegt ihr Licht zurück in Asterias Hand.
 */
public class CometLanceEntity extends ThrowableItemProjectile {
    public static final double SPEED = 1.7;
    public static final double RADIUS = 3.2;
    private static final double GRAVITY = 0.012;
    private float damage = 12.0f;

    public CometLanceEntity(EntityType<? extends CometLanceEntity> type, Level level) {
        super(type, level);
    }

    public CometLanceEntity(Level level, LivingEntity owner) {
        super(ModEntities.COMET_LANCE.get(), owner, level, new ItemStack(StarBlocks.STAR_LANCE.get()));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected Item getDefaultItem() {
        return StarBlocks.STAR_LANCE.get();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    /** Wirft sie von {@code from} auf {@code to} (mit einem kleinen Vorhalt gegen das Absinken). */
    public void launch(Vec3 from, Vec3 to) {
        this.setPos(from.x, from.y, from.z);
        Vec3 dir = to.subtract(from);
        double ticks = dir.length() / SPEED;
        Vec3 v = dir.normalize().scale(SPEED).add(0.0, GRAVITY * ticks * 0.5, 0.0);
        this.setDeltaMovement(v);
        double flat = v.horizontalDistance();
        this.setYRot((float) (net.minecraft.util.Mth.atan2(v.x, v.z) * net.minecraft.util.Mth.RAD_TO_DEG));
        this.setXRot((float) (net.minecraft.util.Mth.atan2(v.y, flat) * net.minecraft.util.Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof AsteriaEntity);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            for (int i = 0; i < 4; i++) {
                double f = 0.4 + i * 0.45;
                level.addParticle(ParticleTypes.END_ROD, this.getX() - v.x * f + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() - v.y * f + (this.random.nextDouble() - 0.5) * 0.3, this.getZ() - v.z * f + (this.random.nextDouble() - 0.5) * 0.3,
                        -v.x * 0.04, -v.y * 0.04, -v.z * 0.04);
            }
            level.addParticle(ParticleTypes.FIREWORK, this.getX() - v.x, this.getY() - v.y, this.getZ() - v.z, 0.0, 0.0, 0.0);
        } else if (this.tickCount > 100) {
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

    private void burst(ServerLevel level) {
        Vec3 c = this.position();
        level.playSound(null, c.x, c.y, c.z, ModSounds.ASTERIA_COMET_IMPACT.get(), SoundSource.HOSTILE, 3.0f, 0.95f + this.random.nextFloat() * 0.1f);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFDCF0FF), c.x, c.y + 0.5, c.z, 2, 0.3, 0.3, 0.3, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 0.5, c.z, 70, 0.6, 0.4, 0.6, 0.3);
        level.sendParticles(ParticleTypes.FIREWORK, c.x, c.y + 0.5, c.z, 30, 0.5, 0.3, 0.5, 0.2);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS + 0.5),
                e -> e.isAlive() && e != owner && !(e instanceof AsteriaEntity) && !(e instanceof ArmorStand) && !(e instanceof Enemy)
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            Vec3 to = victim.position().subtract(c);
            if (to.lengthSqr() > (RADIUS + 0.6) * (RADIUS + 0.6)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().indirectMagic(this, owner), this.damage)) {
                Vec3 away = new Vec3(to.x, 0.0, to.z);
                away = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize();
                victim.push(away.x * 1.0, 0.5, away.z * 1.0);
                victim.needsSync = true;
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0), owner);
            }
        }
        // das Licht der Lanze fliegt zurück in ihre Hand
        if (owner != null && owner.isAlive()) {
            Vec3 hand = owner.position().add(0.0, 2.6, 0.0);
            double len = hand.distanceTo(c);
            for (double s = 0.0; s < len; s += 0.6) {
                Vec3 p = c.add(hand.subtract(c).scale(s / len));
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
    }
}
