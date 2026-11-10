package io.github.sirschrolli.kingdomsatwar.entity.monster;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Geschosse der neuen Monster: der Schlammklumpen des Moorschlurfers (fliegt im Bogen, verlangsamt und spritzt in die
 * Augen), der Magmabatzen des Glutwurms (fliegt im Bogen, setzt in Brand und entzündet, wo er aufschlägt) und das
 * Seelenfeuer des Aschengeists (fliegt gerade, eine Spur aus blauen Flammen, setzt in Brand und lähmt).
 */
public class MonsterShot extends ThrowableItemProjectile {
    public enum Kind {
        MUD, MAGMA, SOUL
    }

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(MonsterShot.class, EntityDataSerializers.BYTE);
    private float damage = 4.0f;
    private int age;

    public MonsterShot(EntityType<? extends MonsterShot> type, Level level) {
        super(type, level);
    }

    /** Wirft ein Geschoss von {@code from} mit der Geschwindigkeit {@code velocity}. */
    public static MonsterShot shoot(ServerLevel level, LivingEntity owner, Kind kind, Vec3 from, Vec3 velocity, float damage) {
        MonsterShot shot = new MonsterShot(ModEntities.MONSTER_SHOT.get(), level);
        shot.entityData.set(DATA_KIND, (byte) kind.ordinal());
        shot.setItem(switch (kind) {
            case MUD -> new ItemStack(Items.MUD);
            case MAGMA -> new ItemStack(Items.MAGMA_CREAM);
            case SOUL -> ItemStack.EMPTY;
        });
        shot.damage = damage;
        shot.setOwner(owner);
        shot.setPos(from.x, from.y, from.z);
        shot.setDeltaMovement(velocity);
        level.addFreshEntity(shot);
        return shot;
    }

    /**
     * Geschwindigkeit, mit der ein Wurf im Bogen (Schwerkraft {@code gravity}) nach {@code ticks} Ticks bei {@code to} ankommt –
     * mit der Luftreibung der Geschosse (je Tick erst Schwerkraft, dann ×0,99, dann die Bewegung).
     */
    public static Vec3 lob(Vec3 from, Vec3 to, int ticks, double gravity) {
        Vec3 d = to.subtract(from);
        double drag = 0.99;
        double sum = drag * (1.0 - Math.pow(drag, ticks)) / (1.0 - drag);
        double fall = gravity * drag / (1.0 - drag) * (ticks - sum);
        return new Vec3(d.x / sum, (d.y + fall) / sum, d.z / sum);
    }

    public Kind kind() {
        return Kind.values()[Mth.clamp(this.entityData.get(DATA_KIND), 0, Kind.values().length - 1)];
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_KIND, (byte) 0);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MUD;
    }

    @Override
    protected double getDefaultGravity() {
        return this.kind() == Kind.SOUL ? 0.0 : 0.04;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            switch (this.kind()) {
                case MUD -> {
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.MUD.defaultBlockState()), this.getX(), this.getY(),
                                this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case MAGMA -> {
                    level.addParticle(ParticleTypes.FLAME, this.getX() - v.x * 0.5, this.getY() - v.y * 0.5, this.getZ() - v.z * 0.5, 0.0, 0.01, 0.0);
                    if (this.random.nextInt(3) == 0) {
                        level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.02, 0.0);
                    }
                    if (this.random.nextInt(4) == 0) {
                        level.addParticle(ParticleTypes.DRIPPING_LAVA, this.getX(), this.getY() - 0.1, this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                case SOUL -> {
                    // ein Ball aus blauen Flammen mit einem Schweif
                    for (int i = 0; i < 3; i++) {
                        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + (this.random.nextDouble() - 0.5) * 0.25,
                                this.getY() + (this.random.nextDouble() - 0.5) * 0.25, this.getZ() + (this.random.nextDouble() - 0.5) * 0.25,
                                -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
                    }
                    if (this.random.nextInt(2) == 0) {
                        level.addParticle(ParticleTypes.SOUL, this.getX(), this.getY(), this.getZ(), -v.x * 0.05, 0.02, -v.z * 0.05);
                    }
                }
            }
        } else if (++this.age > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Entity target = hit.getEntity();
        Entity owner = this.getOwner();
        if (target == owner) {
            return;
        }
        if (!target.hurtServer(level, this.damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null), this.damage)) {
            return;
        }
        if (target instanceof LivingEntity living) {
            switch (this.kind()) {
                case MUD -> {
                    // Schlamm in den Augen und an den Beinen
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1), owner);
                    living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 24, 0), owner);
                }
                case MAGMA -> living.igniteForSeconds(5.0f);
                case SOUL -> {
                    living.igniteForSeconds(3.0f);
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, 0), owner);
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), owner);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (this.kind() == Kind.MAGMA && this.level() instanceof ServerLevel level) {
            // der Batzen entzündet, wo er aufschlägt
            BlockPos at = hit.getBlockPos().relative(hit.getDirection());
            if (level.isEmptyBlock(at) && BaseFireBlock.canBePlacedAt(level, at, hit.getDirection())) {
                level.setBlockAndUpdate(at, BaseFireBlock.getState(level, at));
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 p = this.position();
        switch (this.kind()) {
            case MUD -> {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), p.x, p.y, p.z, 24, 0.3, 0.3, 0.3, 0.15);
                level.playSound(null, p.x, p.y, p.z, ModSounds.MIRE_SHAMBLER_SPLAT.get(), SoundSource.HOSTILE, 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
            }
            case MAGMA -> {
                level.sendParticles(ParticleTypes.LAVA, p.x, p.y, p.z, 8, 0.3, 0.2, 0.3, 0.0);
                level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 16, 0.3, 0.3, 0.3, 0.06);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0f, 0.8f + this.random.nextFloat() * 0.2f);
            }
            case SOUL -> {
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 24, 0.25, 0.25, 0.25, 0.08);
                level.sendParticles(ParticleTypes.SOUL, p.x, p.y, p.z, 6, 0.2, 0.2, 0.2, 0.03);
                level.playSound(null, p.x, p.y, p.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.2f, 0.9f + this.random.nextFloat() * 0.2f);
            }
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Kind", this.kind().ordinal());
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_KIND, (byte) Mth.clamp(input.getIntOr("Kind", 0), 0, Kind.values().length - 1));
        this.damage = input.getFloatOr("Damage", 4.0f);
    }
}
