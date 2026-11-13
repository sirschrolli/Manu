package io.github.sirschrolli.kingdomsatwar.entity.arena;

import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Eine Windhose (seit 1.9): Ventor ruft sie (drei, im entfesselten Sturm vier); sie sieht aus wie der Wirbel unter seinem
 * Leib, nur größer, und zieht {@value #LIFETIME} Ticks lang durch die Arena – langsam auf den Nächsten zu, ein wenig hin und
 * her. Wen sie erfasst, den zieht sie hinein, reißt ihn im Kreis nach oben und schleudert ihn oben hinaus (der Sturz tut weh);
 * dazu schmerzt der Sand. Ist ihr Herr fort, fällt sie in sich zusammen.
 */
public class WindVortexEntity extends Entity {
    public static final int LIFETIME = 200;
    /** So weit (waagerecht) erfasst sie, und so hoch reißt sie. */
    public static final double RADIUS = 2.2;
    public static final double HEIGHT = 3.2;
    public static final float DAMAGE = 2.0f;
    private @Nullable UUID owner;
    /** Wen sie gerade hinausgeschleudert hat (und wann): den fängt sie nicht sofort wieder. */
    private final Map<UUID, Integer> thrown = new HashMap<>();
    private double phase;

    public WindVortexEntity(EntityType<? extends WindVortexEntity> type, Level level) {
        super(type, level);
        this.phase = this.random.nextDouble() * Math.PI * 2.0;
    }

    public WindVortexEntity(Level level, VentorEntity owner, Vec3 at) {
        this(ModEntities.WIND_VORTEX.get(), level);
        this.owner = owner.getUUID();
        this.setPos(at.x, at.y, at.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.clientParticles();
            return;
        }
        ServerLevel level = (ServerLevel) this.level();
        Entity master = this.owner == null ? null : level.getEntity(this.owner);
        if (this.tickCount > LIFETIME || !(master instanceof VentorEntity ventor) || !ventor.isAlive()) {
            this.collapse(level);
            return;
        }
        // langsam auf den Nächsten zu, ein wenig hin und her; sie bleibt am Boden
        Player near = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 18.0,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator() && p.isAlive());
        Vec3 v = this.getDeltaMovement();
        double wobble = Math.sin(this.tickCount * 0.09 + this.phase);
        Vec3 steer = Vec3.ZERO;
        if (near != null) {
            Vec3 to = near.position().subtract(this.position()).multiply(1.0, 0.0, 1.0);
            if (to.lengthSqr() > 1.0e-3) {
                to = to.normalize();
                steer = to.scale(0.075).add(new Vec3(-to.z, 0.0, to.x).scale(wobble * 0.05));
            }
        }
        v = new Vec3(v.x * 0.6 + steer.x, v.y - 0.04, v.z * 0.6 + steer.z);
        this.move(MoverType.SELF, v);
        if (this.onGround()) {
            v = new Vec3(v.x, 0.0, v.z);
        }
        this.setDeltaMovement(v);
        this.whirl(level, ventor);
        if (this.tickCount % 30 == 1) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.WIND_VORTEX_HOWL.get(), SoundSource.HOSTILE, 1.4f,
                    0.9f + this.random.nextFloat() * 0.2f);
        }
    }

    /** Wen sie erfasst: hinein, im Kreis nach oben – und oben hinaus. */
    private void whirl(ServerLevel level, VentorEntity ventor) {
        this.thrown.values().removeIf(t -> this.tickCount - t > 30);
        AABB area = new AABB(this.getX() - RADIUS, this.getY() - 0.5, this.getZ() - RADIUS, this.getX() + RADIUS, this.getY() + HEIGHT + 1.0,
                this.getZ() + RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && !(e instanceof VentorEntity)
                && !(e instanceof ArmorStand) && !(e instanceof Player p && (p.isCreative() || p.isSpectator())) && ventor.canStrike(e))) {
            if (this.thrown.containsKey(victim.getUUID())) {
                continue;
            }
            Vec3 to = this.position().subtract(victim.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            if (dist > RADIUS) {
                continue;
            }
            Vec3 in = dist < 1.0e-3 ? Vec3.ZERO : to.normalize();
            Vec3 around = new Vec3(-in.z, 0.0, in.x);
            double rise = victim.getY() - this.getY();
            if (rise > HEIGHT) {
                // oben hinaus
                Vec3 out = in.scale(-1.0).add(around.scale(0.6)).normalize();
                victim.setDeltaMovement(out.x * 1.0, 0.45, out.z * 1.0);
                victim.needsSync = true;
                this.thrown.put(victim.getUUID(), this.tickCount);
                continue;
            }
            Vec3 v = victim.getDeltaMovement();
            victim.setDeltaMovement(v.x * 0.7 + in.x * 0.12 + around.x * 0.18, Math.max(v.y, 0.32), v.z * 0.7 + in.z * 0.12 + around.z * 0.18);
            victim.needsSync = true;
            victim.resetFallDistance();
            if ((this.tickCount + victim.getId()) % 20 == 0) {
                victim.hurtServer(level, this.damageSources().mobAttack(ventor), DAMAGE);
            }
        }
    }

    /** Sie fällt in sich zusammen: ein letzter Staubring. */
    private void collapse(ServerLevel level) {
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.6, 1.0, 0.6, 0.03);
        BlockState ground = level.getBlockState(BlockPos.containing(this.getX(), this.getY() - 0.5, this.getZ()));
        if (!ground.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.2, this.getZ(), 20, 0.8, 0.1,
                    0.8, 0.1);
        }
        this.discard();
    }

    /** Auf dem Client: Staub und Sand am Fuß, Windfetzen am Trichter. */
    private void clientParticles() {
        Level level = this.level();
        double t = this.tickCount;
        for (int k = 0; k < 2; k++) {
            double a = t * 0.55 + k * Math.PI + this.phase;
            double h = this.random.nextDouble() * HEIGHT;
            double r = 0.4 + h * 0.4;
            level.addParticle(ParticleTypes.CLOUD, this.getX() + Math.cos(a) * r, this.getY() + h, this.getZ() + Math.sin(a) * r,
                    -Math.sin(a) * 0.12, 0.04, Math.cos(a) * 0.12);
        }
        if (this.tickCount % 2 == 0) {
            BlockState ground = level.getBlockState(BlockPos.containing(this.getX(), this.getY() - 0.5, this.getZ()));
            if (!ground.isAir()) {
                double a = this.random.nextDouble() * Math.PI * 2.0;
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX() + Math.cos(a) * 0.8, this.getY() + 0.1,
                        this.getZ() + Math.sin(a) * 0.8, -Math.sin(a) * 0.3, 0.2, Math.cos(a) * 0.3);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 96.0 * 96.0;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (this.owner != null) {
            output.store("Owner", UUIDUtil.CODEC, this.owner);
        }
    }
}
