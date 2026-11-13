package io.github.sirschrolli.kingdomsatwar.entity.arena;

import io.github.sirschrolli.kingdomsatwar.block.arena.GladiatorNetBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * Ein geworfenes Wurfnetz (seit 1.9): Es fliegt im Bogen, ausgebreitet und sich drehend. Trifft es ein Wesen, legt es sich
 * um dessen Füße (und, bei Großen, den Leib) – es steckt fest, bis das Netz zerrissen ist oder sich löst ({@link
 * GladiatorNetBlock}), und ist eine Weile langsam. Geht es daneben, bleibt es als Falle liegen. Ventor wirft es (und stößt
 * gleich darauf zu); das Wurfnetz aus seiner Beute ebenso.
 */
public class GladiatorNetEntity extends ThrowableItemProjectile {
    public static final double SPEED = 1.15;
    private static final double GRAVITY = 0.035;
    /** So lange hält ein Netz, das jemanden traf, und eines, das als Falle am Boden liegt. */
    public static final int SNARE_TICKS = 70;
    public static final int TRAP_TICKS = 200;
    private float damage = 2.0f;

    public GladiatorNetEntity(EntityType<? extends GladiatorNetEntity> type, Level level) {
        super(type, level);
    }

    public GladiatorNetEntity(Level level, LivingEntity owner) {
        super(ModEntities.GLADIATOR_NET.get(), owner, level, new ItemStack(ArenaGear.GLADIATOR_NET.get()));
    }

    @Override
    protected Item getDefaultItem() {
        return ArenaGear.GLADIATOR_NET.get();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    /** Wirft es von {@code from} auf {@code to} (im Bogen, mit Vorhalt gegen das Absinken). */
    public void launch(Vec3 from, Vec3 to) {
        this.setPos(from.x, from.y, from.z);
        Vec3 dir = to.subtract(from);
        double ticks = dir.length() / SPEED;
        Vec3 v = dir.normalize().scale(SPEED).add(0.0, GRAVITY * ticks * 0.5, 0.0);
        this.setDeltaMovement(v);
        this.setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > 100) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof WindVortexEntity)
                && !(this.getOwner() instanceof VentorEntity && entity instanceof VentorEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel level && result.getEntity() instanceof LivingEntity victim
                && !(victim instanceof Player p && (p.isCreative() || p.isSpectator()))) {
            victim.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), this.damage);
            snare(level, victim, SNARE_TICKS);
            if (this.getOwner() instanceof VentorEntity ventor) {
                ventor.onNetCaught(victim);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel level) {
            BlockPos at = result.getBlockPos().relative(result.getDirection());
            if (GladiatorNetBlock.place(level, at, TRAP_TICKS)) {
                level.playSound(null, at, ModSounds.GLADIATOR_NET_SNARE.get(), SoundSource.NEUTRAL, 1.2f, 1.2f);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ArenaGear.GLADIATOR_NET.get()), this.getX(), this.getY(), this.getZ(), 10, 0.3, 0.3,
                    0.3, 0.05);
            this.discard();
        }
    }

    /** Legt das Netz um {@code victim}: um die Füße (bei Großen auch um den Leib); dazu ist es eine Weile langsam. */
    public static void snare(ServerLevel level, LivingEntity victim, int ticks) {
        BlockPos feet = victim.blockPosition();
        boolean held = GladiatorNetBlock.place(level, feet, ticks);
        if (victim.getBbHeight() > 1.2) {
            held |= GladiatorNetBlock.place(level, feet.above(), ticks);
        }
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, held ? 1 : 3));
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.GLADIATOR_NET_SNARE.get(), SoundSource.HOSTILE, 1.8f, 1.0f);
        level.sendParticles(ParticleTypes.SMALL_GUST, victim.getX(), victim.getY(0.5), victim.getZ(), 4, 0.3, 0.4, 0.3, 0.0);
    }
}
