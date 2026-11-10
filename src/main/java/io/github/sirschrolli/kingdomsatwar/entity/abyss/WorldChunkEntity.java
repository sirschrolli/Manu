package io.github.sirschrolli.kingdomsatwar.entity.abyss;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Brocken einer verschlungenen Welt, den der Weltenfresser ausspeit. Er zerschellt, wo er aufschlägt, und trifft
 * alle ringsum – je nach Welt mit eigener Wirkung: Glut setzt in Brand, Frost lähmt, End lässt schweben, Schatten blendet.
 */
public class WorldChunkEntity extends ThrowableProjectile {
    public enum Kind {
        GLUT, FROST, END, SHADOW
    }

    private static final EntityDataAccessor<Byte> DATA_KIND = SynchedEntityData.defineId(WorldChunkEntity.class, EntityDataSerializers.BYTE);
    private float damage = 9.0f;

    public WorldChunkEntity(EntityType<? extends WorldChunkEntity> type, Level level) {
        super(type, level);
    }

    /** Speit einen Brocken der Welt {@code kind} von {@code from} mit {@code velocity}. */
    public static WorldChunkEntity spit(ServerLevel level, LivingEntity owner, Kind kind, Vec3 from, Vec3 velocity, float damage) {
        WorldChunkEntity chunk = new WorldChunkEntity(ModEntities.WORLD_CHUNK.get(), level);
        chunk.entityData.set(DATA_KIND, (byte) kind.ordinal());
        chunk.damage = damage;
        chunk.setOwner(owner);
        chunk.setPos(from.x, from.y, from.z);
        chunk.setDeltaMovement(velocity);
        level.addFreshEntity(chunk);
        return chunk;
    }

    public Kind kind() {
        byte k = this.entityData.get(DATA_KIND);
        return Kind.values()[Math.floorMod(k, Kind.values().length)];
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_KIND, (byte) 0);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof WorldEaterEntity) && !(entity instanceof WorldEaterPart) && !(entity instanceof WorldChunkEntity)
                && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            ParticleOptions trail = switch (this.kind()) {
                case GLUT -> ParticleTypes.FLAME;
                case FROST -> ParticleTypes.SNOWFLAKE;
                case END -> ParticleTypes.REVERSE_PORTAL;
                case SHADOW -> ParticleTypes.SOUL_FIRE_FLAME;
            };
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(trail, this.getX() + (this.random.nextDouble() - 0.5) * 0.8, this.getY() + 0.4 + (this.random.nextDouble() - 0.5) * 0.8,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.0, 0.0);
            }
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 0.0, 0.01, 0.0);
            }
        } else if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            this.burst(level, result.getLocation());
            this.discard();
        }
    }

    private BlockState rock() {
        return switch (this.kind()) {
            case GLUT -> Blocks.NETHERRACK.defaultBlockState();
            case FROST -> Blocks.PACKED_ICE.defaultBlockState();
            case END -> Blocks.END_STONE.defaultBlockState();
            case SHADOW -> Blocks.BLACKSTONE.defaultBlockState();
        };
    }

    /** Zerschellen: Splitter, ein Ring aus Staub und die Wirkung seiner Welt auf alle ringsum. */
    private void burst(ServerLevel level, Vec3 at) {
        Kind kind = this.kind();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.rock()), at.x, at.y + 0.3, at.z, 50, 0.8, 0.4, 0.8, 0.2);
        level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.3, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        int color = switch (kind) {
            case GLUT -> 0xFF6A1A;
            case FROST -> 0x9ADCFF;
            case END -> 0xC08CFF;
            case SHADOW -> 0x5AE0E8;
        };
        for (int k = 0; k < 20; k++) {
            double a = k * Math.PI * 2.0 / 20.0;
            level.sendParticles(new DustParticleOptions(color, 1.8f), at.x + Math.cos(a) * 2.4, at.y + 0.2, at.z + Math.sin(a) * 2.4, 1, 0.0, 0.0, 0.0,
                    0.0);
        }
        level.playSound(null, BlockPos.containing(at), ModSounds.WORLD_EATER_CHUNK.get(), SoundSource.HOSTILE, 2.5f,
                kind == Kind.FROST ? 1.25f : kind == Kind.END ? 1.1f : 0.9f);
        Entity owner = this.getOwner();
        // ein Brocken des Weltenspeers trifft weder seinen Träger noch dessen Verbündete und Tiere
        boolean fromPlayer = owner instanceof net.minecraft.world.entity.player.Player;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(2.8),
                e -> e.isAlive() && !(e instanceof WorldEaterEntity) && (!fromPlayer || e != owner && !e.isAlliedTo(owner)
                        && !(e instanceof net.minecraft.world.entity.OwnableEntity pet && pet.getOwner() == owner)))) {
            double d = e.position().add(0.0, e.getBbHeight() * 0.5, 0.0).distanceTo(at);
            if (d > 3.2) {
                continue;
            }
            e.hurtServer(level, this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null),
                    this.damage * (float) (1.0 - d / 6.4));
            switch (kind) {
                case GLUT -> e.igniteForSeconds(5.0f);
                case FROST -> {
                    e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 2));
                    e.setTicksFrozen(Math.max(e.getTicksFrozen(), 160));
                }
                case END -> e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));
                case SHADOW -> e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Kind", this.entityData.get(DATA_KIND));
        output.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_KIND, input.getByteOr("Kind", (byte) 0));
        this.damage = input.getFloatOr("Damage", 9.0f);
    }
}
