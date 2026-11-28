package io.github.sirschrolli.kingdomsatwar.entity.fae;

import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Felsbrocken des Steinhüters (seit 0.96): ein Klotz aus Feenstein mit Erde und Mondsteinkristallen, den er aus dem
 * Boden reißt und im hohen Bogen schleudert. Er überschlägt sich im Flug und zerschellt beim Aufschlag – wer im Umkreis von
 * knapp drei Blöcken steht, nimmt Schaden und fliegt; im Zorn brechen dort Mondsteinkristalle aus dem Boden. Blöcke
 * beschädigt er nicht.
 */
public class KeeperBoulderEntity extends ThrowableProjectile {
    public static final double RADIUS = 2.8;
    private static final double GRAVITY = 0.05;
    private static final EntityDataAccessor<Integer> DATA_SEED = SynchedEntityData.defineId(KeeperBoulderEntity.class, EntityDataSerializers.INT);
    private float damage = 10.0f;
    private boolean split;

    public KeeperBoulderEntity(EntityType<? extends KeeperBoulderEntity> type, Level level) {
        super(type, level);
    }

    public KeeperBoulderEntity(Level level, LivingEntity owner) {
        super(ModEntities.KEEPER_BOULDER.get(), level);
        this.setOwner(owner);
        this.entityData.set(DATA_SEED, level.getRandom().nextInt(1 << 16));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SEED, 0);
    }

    /** Zufall für die Drehung im Flug (im Client). */
    public int seed() {
        return this.entityData.get(DATA_SEED);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    /** Im Zorn: beim Aufschlag brechen Mondsteinkristalle aus dem Boden. */
    public void setSplit(boolean split) {
        this.split = split;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    /**
     * Wirft im Bogen von {@code from} nach {@code to}: der flachere der beiden Wurfwinkel, der bei {@code speed} Blöcken je
     * Tick dort ankommt; reicht die Kraft nicht, im Winkel der größten Weite.
     */
    public void lob(Vec3 from, Vec3 to, double speed) {
        this.setPos(from.x, from.y, from.z);
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double dy = to.y - from.y;
        double flat = Math.sqrt(dx * dx + dz * dz);
        if (flat < 1.0e-3) {
            this.setDeltaMovement(0.0, -0.2, 0.0);
            return;
        }
        double d = flat * 1.06;
        double v2 = speed * speed;
        double disc = v2 * v2 - GRAVITY * (GRAVITY * d * d + 2.0 * dy * v2);
        double angle = disc < 0.0 ? Math.PI / 4.0 : Math.atan((v2 - Math.sqrt(disc)) / (GRAVITY * d));
        double horizontal = Math.cos(angle) * speed;
        this.setDeltaMovement(dx / flat * horizontal, Math.sin(angle) * speed, dz / flat * horizontal);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof StoneKeeperEntity);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.isClientSide()) {
            if (this.random.nextInt(2) == 0) {
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.FAESTONE.base().get().defaultBlockState()),
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.8, this.getY() + 0.3, this.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                        0.0, 0.0, 0.0);
            }
            if (this.random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.4, this.getZ(), 0.0, 0.0, 0.0);
            }
        } else if (this.tickCount > 200) {
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

    /** Zerschellt: Geröll und Mondsteinsplitter fliegen, wer nahe steht, wird getroffen. */
    private void burst(ServerLevel level) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        level.playSound(null, x, y, z, ModSounds.STONE_KEEPER_BOULDER.get(), SoundSource.HOSTILE, 3.0f, 0.85f + this.random.nextFloat() * 0.3f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.FAESTONE.base().get().defaultBlockState()), x, y + 0.4, z, 80, 1.0,
                0.5, 1.0, 0.25);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.MOONSTONE_BLOCK.get().defaultBlockState()), x, y + 0.5, z, 24, 0.8,
                0.4, 0.8, 0.2);
        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.4, z, 2, 0.4, 0.2, 0.4, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, x, y + 0.6, z, 14, 0.8, 0.6, 0.8, 0.08);
        Entity owner = this.getOwner();
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(RADIUS),
                e -> e.isAlive() && e != owner && !(e instanceof StoneKeeperEntity) && !(e instanceof ArmorStand)
                        && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (victim.distanceToSqr(x, y, z) > (RADIUS + 0.7) * (RADIUS + 0.7)) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().thrown(this, owner), this.damage)) {
                Vec3 out = victim.position().subtract(x, victim.getY(), z);
                out = out.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : out.normalize();
                victim.push(out.x * 0.8, 0.45, out.z * 0.8);
                victim.needsSync = true;
            }
        }
        if (this.split) {
            for (int i = 0; i < 4; i++) {
                double a = i * Math.PI / 2.0 + this.random.nextDouble() * 0.6;
                Vec3 at = new Vec3(x + Math.cos(a) * 1.6, Math.floor(y + 0.2), z + Math.sin(a) * 1.6);
                SpellFxEntity.create(level, SpellFxEntity.Kind.CRYSTAL, at, null, 0x8EC8FF, 22, 0.55f).spawn();
            }
        }
    }
}
