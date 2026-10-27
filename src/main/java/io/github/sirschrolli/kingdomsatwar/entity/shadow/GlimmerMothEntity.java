package io.github.sirschrolli.kingdomsatwar.entity.shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Glimmerfalter: ein handgroßer Nachtfalter, dessen Flügelflecken silberblau glimmen. Er flattert dicht über dem Boden
 * von Blüte zu Blüte und hinterlässt eine feine Leuchtspur. Harmlos – und ein guter Wegweiser in der Dunkelheit.
 */
public class GlimmerMothEntity extends AmbientCreature {
    private static final net.minecraft.core.particles.DustParticleOptions GLIMMER_DUST =
            new net.minecraft.core.particles.DustParticleOptions(0x8ADCFF, 0.55f);
    private @Nullable BlockPos targetPosition;

    public GlimmerMothEntity(EntityType<? extends GlimmerMothEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        if (this.level().isClientSide() && this.random.nextInt(6) == 0) {
            // feiner, silberblauer Glimmerstaub
            this.level().addParticle(GLIMMER_DUST, this.getX(), this.getY(0.3), this.getZ(), 0.0, -0.02, 0.0);
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.targetPosition != null && !level.isEmptyBlock(this.targetPosition)) {
            this.targetPosition = null;
        }
        if (this.targetPosition == null || this.random.nextInt(40) == 0 || this.targetPosition.closerToCenterThan(this.position(), 1.5)) {
            int x = Mth.floor(this.getX()) + this.random.nextInt(9) - 4;
            int z = Mth.floor(this.getZ()) + this.random.nextInt(9) - 4;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            // dicht über dem Boden bleiben (eins bis vier Blöcke), aber nicht weit unter die eigene Höhe
            int y = Math.max(ground + 1 + this.random.nextInt(4), Mth.floor(this.getY()) - 2);
            this.targetPosition = new BlockPos(x, Math.min(y, ground + 5), z);
        }
        double dx = this.targetPosition.getX() + 0.5 - this.getX();
        double dy = this.targetPosition.getY() + 0.3 - this.getY();
        double dz = this.targetPosition.getZ() + 0.5 - this.getZ();
        Vec3 movement = this.getDeltaMovement();
        Vec3 next = movement.add((Math.signum(dx) * 0.3 - movement.x) * 0.08, (Math.signum(dy) * 0.45 - movement.y) * 0.1,
                (Math.signum(dz) * 0.3 - movement.z) * 0.08);
        this.setDeltaMovement(next);
        float yaw = (float) (Mth.atan2(next.z, next.x) * Mth.RAD_TO_DEG) - 90.0f;
        this.zza = 0.4f;
        this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** Über Land, nicht unter der Erde. */
    public static boolean checkMothSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
            RandomSource random) {
        return EntitySpawnReason.isSpawner(reason) || level.canSeeSky(pos) && pos.getY() > 55;
    }
}
