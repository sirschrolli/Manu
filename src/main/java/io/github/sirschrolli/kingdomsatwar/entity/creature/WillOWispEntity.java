package io.github.sirschrolli.kingdomsatwar.entity.creature;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Irrlicht: ein schwebendes, leuchtendes Licht, das nachts über Sümpfen und in dunklen Wäldern tanzt.
 * Weicht Spielern aus, verschwindet bei Sonnenaufgang und hinterlässt Irrlichtstaub – eine Zutat für Magie.
 */
public class WillOWispEntity extends PathfinderMob {
    private int vanishTicks;

    public WillOWispEntity(EntityType<? extends WillOWispEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.FLYING_SPEED, 0.35)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 5.0f, 1.0, 1.4));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, this.getSpeed());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.GLOW, this.getRandomX(0.6), this.getY(0.5) + (this.random.nextDouble() - 0.5) * 0.4,
                        this.getRandomZ(0.6), 0.0, 0.01, 0.0);
            }
            if (this.random.nextInt(12) == 0) {
                level.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(),
                        (this.random.nextDouble() - 0.5) * 0.02, 0.02, (this.random.nextDouble() - 0.5) * 0.02);
            }
            return;
        }
        // Bei Tageslicht verblasst das Irrlicht und verschwindet
        if (level.isBrightOutside() && level.canSeeSky(this.blockPosition()) && !this.isPersistenceRequired()) {
            if (++this.vanishTicks > 40 + this.random.nextInt(60)) {
                this.vanish((ServerLevel) level);
            }
        } else {
            this.vanishTicks = 0;
        }
    }

    private void vanish(ServerLevel level) {
        level.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY(0.5), this.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
        this.playSound(ModSounds.WISP_VANISH.get(), 0.8f, 1.2f);
        this.discard();
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return true;
    }

    /** Nur nachts und im Dunkeln (kein Fackellicht in der Nähe). */
    public static boolean checkWispSpawnRules(EntityType<WillOWispEntity> type, ServerLevelAccessor level, EntitySpawnReason reason,
            BlockPos pos, RandomSource random) {
        if (EntitySpawnReason.isSpawner(reason)) {
            return true;
        }
        return level.getLevel().isDarkOutside() && level.getBrightness(LightLayer.BLOCK, pos) < 5 && random.nextInt(3) == 0
                && Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    // ------------------------------------------------------------------ Geräusche

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.WISP_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WISP_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WISP_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }
}
