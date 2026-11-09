package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Mondfalter: ein handgroßer, fliederfarbener Falter, den die Mottenkönigin herbeiruft. Er flattert auf sein Ziel zu,
 * beißt und weicht zurück – und verglimmt nach einer halben Minute (oder wenn seine Königin fällt).
 */
public class MoonMothEntity extends Monster {
    private static final int LIFETIME = 600;
    private int retreat;

    public MoonMothEntity(EntityType<? extends MoonMothEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 2.5)
                .add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void travel(Vec3 input) {
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.88));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount > LIFETIME) {
            this.vanish(level);
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || target instanceof Player p && (p.isCreative() || p.isSpectator())) {
            target = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 24.0,
                    e -> e instanceof Player p && !p.isCreative() && !p.isSpectator());
            this.setTarget(target);
        }
        if (target == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, Math.sin(this.tickCount * 0.2) * 0.01, 0.0));
            return;
        }
        Vec3 want = target.getEyePosition().subtract(0.0, 0.3, 0.0);
        if (this.retreat > 0) {
            this.retreat--;
            want = want.add(this.position().subtract(want).normalize().scale(5.0)).add(0.0, 2.5, 0.0);
        }
        Vec3 to = want.subtract(this.position());
        Vec3 wobble = new Vec3(Math.sin(this.tickCount * 0.5) * 0.06, Math.cos(this.tickCount * 0.37) * 0.05, Math.cos(this.tickCount * 0.43) * 0.06);
        this.setDeltaMovement(this.getDeltaMovement().add(to.normalize().scale(0.07)).add(wobble));
        this.lookAt(target, 30.0f, 30.0f);
        if (this.retreat == 0 && this.distanceTo(target) < 1.4) {
            this.doHurtTarget(level, target);
            this.retreat = 20 + this.random.nextInt(15);
        }
    }

    /** In einem Funkenregen verglimmen. */
    public void vanish(ServerLevel level) {
        level.sendParticles(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.5), this.getZ(), 8, 0.2, 0.2, 0.2, 0.03);
        this.discard();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.random.nextInt(3) == 0) {
            this.level().addParticle(ModParticles.FAE_SPARK.get(), this.getX(), this.getY(0.4), this.getZ(), 0.0, -0.02, 0.0);
        }
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return true;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 1.5f + this.random.nextFloat() * 0.2f;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }
}
