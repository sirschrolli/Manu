package io.github.sirschrolli.kingdomsatwar.entity.creator;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Seraphling: ein kleiner Lichtbote, den Schrolli der Schöpfer aus Licht formt. Er umschwirrt die Gegner seines Herrn und
 * schießt Lichtpfeile in kurzen Salven; nach einer Minute (oder wenn sein Herr fällt) vergeht er in Licht. Er geht durch
 * Wände und kennt weder Fall noch Feuer.
 */
public class SeraphlingEntity extends Monster {
    public static final int LIFETIME = 1200;
    private @Nullable UUID master;
    private int shootCooldown = 40;
    private int burst;
    private float orbit;
    public float flapO;
    public float flap;

    public SeraphlingEntity(EntityType<? extends SeraphlingEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setNoGravity(true);
        this.noPhysics = true;
        this.orbit = this.random.nextFloat() * Mth.TWO_PI;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 36.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.GRAVITY, 0.0);
    }

    /** Formt einen Seraphling bei {@code at}, im Dienst des Schöpfers. */
    public static @Nullable SeraphlingEntity summon(ServerLevel level, SchrolliEntity master, Vec3 at) {
        SeraphlingEntity s = ModEntities.SERAPHLING.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (s == null) {
            return null;
        }
        s.snapTo(at.x, at.y, at.z, master.getYRot(), 0.0f);
        s.master = master.getUUID();
        if (master.isTrueForm()) {
            s.getAttribute(Attributes.MAX_HEALTH).setBaseValue(48.0);
            s.setHealth(48.0f);
        }
        level.addFreshEntity(s);
        DivineFx.sparks(level, at, 24, 0.5, 0.1);
        level.playSound(null, at.x, at.y, at.z, ModSounds.SERAPHLING_AMBIENT.get(), s.getSoundSource(), 1.5f, 1.2f);
        return s;
    }

    private @Nullable SchrolliEntity master(ServerLevel level) {
        return this.master != null && level.getEntity(this.master) instanceof SchrolliEntity boss && boss.isAlive() ? boss : null;
    }

    boolean canStrike(LivingEntity e) {
        if (this.level() instanceof ServerLevel level) {
            SchrolliEntity boss = this.master(level);
            if (boss != null) {
                return boss.canStrike(e);
            }
        }
        return e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive() || false;
    }

    @Override
    public void travel(Vec3 input) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.88));
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        this.setNoGravity(true);
        super.tick();
        if (this.level().isClientSide()) {
            this.flapO = this.flap;
            this.flap += 0.55f;
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(ModParticles.DIVINE_SPARK.get(), this.getRandomX(0.6), this.getY(0.4), this.getRandomZ(0.6), 0.0, -0.01,
                        0.0);
            }
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        SchrolliEntity boss = this.master(level);
        if (this.tickCount > LIFETIME || this.master != null && boss == null) {
            this.vanish(level);
            return;
        }
        LivingEntity target = boss != null ? boss.getTarget() : level.getNearestPlayer(this, 32.0);
        if (target != null && !this.canStrike(target)) {
            target = null;
        }
        if (target == null) {
            Vec3 home = boss != null ? boss.position().add(0.0, 4.0, 0.0) : this.position();
            this.steer(home.add(Math.cos(this.orbit) * 4.0, 0.0, Math.sin(this.orbit) * 4.0), 0.4);
            this.orbit += 0.05f;
            return;
        }
        this.setTarget(target);
        this.orbit += 0.045f;
        double r = 6.0 + Math.sin(this.tickCount * 0.05) * 1.5;
        Vec3 want = target.position().add(Math.cos(this.orbit) * r, 3.2 + Math.sin(this.tickCount * 0.11) * 0.8, Math.sin(this.orbit) * r);
        this.steer(want, 0.55);
        float yaw = (float) DivineFx.yaw(this.position(), target.position());
        this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, 20.0f));
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        if (this.burst > 0) {
            if (this.tickCount % 5 == 0) {
                this.burst--;
                Vec3 from = this.position().add(0.0, 0.8, 0.0);
                Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
                Vec3 dir = aim.subtract(from).add((this.random.nextDouble() - 0.5) * 0.6, (this.random.nextDouble() - 0.5) * 0.4,
                        (this.random.nextDouble() - 0.5) * 0.6);
                float damage = boss != null ? 5.0f * boss.power() : 5.0f;
                DivineShotEntity.bolt(level, this, from, dir, 1.1, damage);
                level.playSound(null, from.x, from.y, from.z, ModSounds.SERAPHLING_SHOOT.get(), this.getSoundSource(), 1.2f,
                        1.0f + this.random.nextFloat() * 0.3f);
            }
        } else if (--this.shootCooldown <= 0) {
            this.burst = 3;
            this.shootCooldown = boss != null && boss.isTrueForm() ? 36 + this.random.nextInt(14) : 46 + this.random.nextInt(20);
        }
    }

    private void steer(Vec3 want, double max) {
        Vec3 to = want.subtract(this.position());
        Vec3 v = this.getDeltaMovement().add(to.normalize().scale(Math.min(to.length(), 3.0) * 0.03));
        if (v.length() > max) {
            v = v.normalize().scale(max);
        }
        this.setDeltaMovement(v);
    }

    /** Vergeht in Licht (ohne Beute). */
    public void vanish(ServerLevel level) {
        DivineFx.sparks(level, this.position().add(0.0, 0.6, 0.0), 30, 0.4, 0.15);
        level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 12, 0.3, 0.3, 0.3, 0.08);
        this.discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)
                || source.getEntity() instanceof SchrolliEntity || source.getEntity() instanceof SeraphlingEntity) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel level) {
            DivineFx.flash(level, this.position().add(0.0, 0.6, 0.0), DivineFx.HOLY);
            DivineFx.sparks(level, this.position().add(0.0, 0.6, 0.0), 40, 0.5, 0.2);
        }
    }

    @Override
    protected void tickDeath() {
        if (++this.deathTime >= 6 && !this.isRemoved()) {
            this.remove(Entity.RemovalReason.KILLED);
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
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean isFlyingVehicle() {
        return true;
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return ModSounds.SERAPHLING_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SERAPHLING_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SERAPHLING_DEATH.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.master != null) {
            output.store("Master", net.minecraft.core.UUIDUtil.CODEC, this.master);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.master = input.read("Master", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
    }
}
