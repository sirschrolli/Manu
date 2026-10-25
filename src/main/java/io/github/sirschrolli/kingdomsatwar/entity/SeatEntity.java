package io.github.sirschrolli.kingdomsatwar.entity;

import io.github.sirschrolli.kingdomsatwar.block.SeatBlock;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Unsichtbarer Sitz auf Stühlen, Bänken und dem Thron. Wer sich setzt, reitet auf diesem Sitz; steht er auf
 * oder wird das Möbelstück abgebaut, verschwindet der Sitz wieder.
 */
public class SeatEntity extends Entity {
    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * Setzt {@code sitter} auf das Möbelstück bei {@code pos}: Sitzfläche {@code height} Blöcke über dem Boden,
     * Blick in Richtung {@code facing}.
     *
     * @return ob er jetzt sitzt (auf dem Client immer {@code true})
     */
    public static boolean sit(Level level, BlockPos pos, double height, Direction facing, LivingEntity sitter) {
        if (!(level instanceof ServerLevel server)) {
            return true;
        }
        if (sitter.isPassenger() || isOccupied(level, pos)) {
            return false;
        }
        SeatEntity seat = ModEntities.SEAT.get().create(server, EntitySpawnReason.TRIGGERED);
        if (seat == null) {
            return false;
        }
        float yaw = facing.toYRot();
        seat.snapTo(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5, yaw, 0.0f);
        server.addFreshEntity(seat);
        if (!sitter.startRiding(seat)) {
            seat.discard();
            return false;
        }
        sitter.setYRot(yaw);
        sitter.setYBodyRot(yaw);
        sitter.setYHeadRot(yaw);
        seat.positionRider(sitter);
        return true;
    }

    /** Sitzt dort schon jemand? */
    public static boolean isOccupied(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(SeatEntity.class, new AABB(pos), Entity::isVehicle).isEmpty();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            boolean seatGone = !(level.getBlockState(this.blockPosition()).getBlock() instanceof SeatBlock);
            if (seatGone || (!this.isVehicle() && this.tickCount > 2)) {
                this.ejectPassengers();
                this.discard();
            }
        }
    }

    // ------------------------------------------------------------------ Sitzhaltung

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        this.clampRotation(passenger);
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        this.clampRotation(passenger);
    }

    /** Der Körper bleibt zur Lehne ausgerichtet, den Kopf kann man wie im Boot drehen. */
    private void clampRotation(Entity passenger) {
        float bodyYaw = this.getYRot();
        passenger.setYBodyRot(bodyYaw);
        if (passenger instanceof Mob mob) {
            mob.setYRot(bodyYaw);
            mob.clampHeadRotationToBody();
            return;
        }
        float delta = Mth.wrapDegrees(passenger.getYRot() - bodyYaw);
        float clamped = Mth.clamp(delta, -105.0f, 105.0f);
        passenger.yRotO += clamped - delta;
        passenger.setYRot(passenger.getYRot() + clamped - delta);
        passenger.setYHeadRot(passenger.getYRot());
    }

    /** Aufstehen: bevorzugt vor das Möbelstück, sonst seitlich, nach hinten oder obendrauf. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos pos = this.blockPosition();
        Direction front = Direction.fromYRot(this.getYRot());
        for (Direction side : new Direction[] {front, front.getClockWise(), front.getCounterClockWise(), front.getOpposite()}) {
            Vec3 target = DismountHelper.findSafeDismountLocation(passenger.getType(), this.level(), pos.relative(side), false);
            if (target != null) {
                return target;
            }
        }
        Vec3 above = DismountHelper.findSafeDismountLocation(passenger.getType(), this.level(), pos.above(), false);
        return above != null ? above : super.getDismountLocationForPassenger(passenger);
    }

    // ------------------------------------------------------------------ unzerstörbar und unsichtbar

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE_ENTITY;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
