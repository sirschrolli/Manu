package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ein Fahrzeug der Belagerung – der {@link SiegeTowerEntity Belagerungsturm} und der {@link SiegeWagonEntity Planwagen}. Es
 * schießt nicht, trägt aber mehrere Krieger: Der erste schiebt (Turm) bzw. sitzt auf dem Kutschbock (Wagen), die anderen
 * fahren mit. Soldaten steigen auf Befehl des Kommandostabs auf und wieder ab (siehe {@link SiegeCrew}); wer mitfährt,
 * hört solange nicht auf die gewöhnlichen Befehle.
 */
public abstract class SiegeVehicleEntity extends SiegeWeaponEntity {
    protected SiegeVehicleEntity(EntityType<? extends SiegeWeaponEntity> type, Level level, SiegeType siege) {
        super(type, level, siege);
    }

    /** Platz Nummer {@code index} (0: wer schiebt bzw. auf dem Bock sitzt) relativ zum Fahrzeug (x links +, Höhe, z vorn +). */
    protected abstract Vec3 seat(int index);

    /** Zeile über der Schnellleiste für Mitfahrer (nicht den, der lenkt). */
    protected abstract Component riderStatus();

    // =====================================================================================
    // Plätze
    // =====================================================================================

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        int index = this.getPassengers().indexOf(passenger);
        Vec3 seat = this.seat(index < 0 ? this.getPassengers().size() : index);
        // Füße: im Stehen auf der Plattform bzw. dem Boden, im Sitzen 0,6 unter der Sitzfläche
        double y = seat.y + passenger.getVehicleAttachmentPoint(this).y - (this.siege().seated() ? 0.6 : 0.0);
        return new Vec3(seat.x, y, seat.z).yRot(-this.getYRot() * Mth.DEG_TO_RAD);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        if (this.siege().seated()) {
            this.clampRotation(passenger);
        }
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        if (this.siege().seated()) {
            this.clampRotation(passenger);
        }
    }

    /** Wer sitzt, sitzt in Fahrtrichtung – den Kopf dreht er höchstens gut zur Seite (wie im Boot). */
    private void clampRotation(Entity passenger) {
        passenger.setYBodyRot(this.getYRot());
        float diff = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
        float clamped = Mth.clamp(diff, -105.0f, 105.0f);
        passenger.yRotO += clamped - diff;
        passenger.setYRot(passenger.getYRot() + clamped - diff);
        passenger.setYHeadRot(passenger.getYRot());
    }

    /** Wie viele Soldaten an Bord sind. */
    public int soldiersAboard() {
        int n = 0;
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof SoldierEntity) {
                n++;
            }
        }
        return n;
    }

    /** Alle Soldaten sitzen ab und folgen wieder ihrem Herrn. @return wie viele abgesessen sind */
    public int dismountSoldiers() {
        int n = 0;
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            if (passenger instanceof SoldierEntity soldier) {
                soldier.stopRiding();
                soldier.setMode(SoldierEntity.MODE_FOLLOW, null);
                n++;
            }
        }
        return n;
    }

    /** Bricht das Fahrzeug auseinander oder wird zerlegt, halten die Krieger dort Stellung, wo sie abgesprungen sind. */
    @Override
    protected void unload(ServerLevel level) {
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            passenger.stopRiding();
            if (passenger instanceof SoldierEntity soldier) {
                soldier.setMode(SoldierEntity.MODE_HOLD, soldier.blockPosition());
            }
        }
    }

    // =====================================================================================
    // Lenken
    // =====================================================================================

    @Override
    protected double control(ServerLevel level, boolean towed) {
        List<Entity> passengers = this.getPassengers();
        Entity driver = passengers.isEmpty() ? null : passengers.getFirst();
        double drive = 0.0;
        if (driver instanceof ServerPlayer player) {
            Input input = player.getLastClientInput();
            if (this.siege().speed() > 0.0) {
                drive = input.forward() ? 1.0 : input.backward() ? -0.6 : 0.0;
                this.turnToward(player.getYRot(), this.siege().turnRate() * (drive != 0.0 ? 0.6f : 1.0f));
            }
            if (input.jump() && !this.lastJump) {
                this.jump(level, player);
            }
            this.lastJump = input.jump();
        } else if (driver instanceof SoldierEntity soldier) {
            drive = SiegeCrew.tick(level, this, soldier);
            this.lastJump = false;
            if (soldier.getSoldierType() == SoldierType.ENGINEER) {
                this.engineerWork(level, soldier);
            }
        } else {
            this.lastJump = false;
        }
        if (--this.overlayTimer <= 0) {
            this.overlayTimer = 10;
            for (int i = 0; i < passengers.size(); i++) {
                if (passengers.get(i) instanceof ServerPlayer player) {
                    player.sendOverlayMessage(i == 0 ? this.status() : this.riderStatus());
                }
            }
        }
        return drive;
    }

    /** Fahrzeuge schießen nicht: Die Leertaste tut, was das Fahrzeug kann (der Turm lässt seine Brücke fallen). */
    @Override
    protected void jump(ServerLevel level, ServerPlayer player) {
    }

    @Override
    protected void boarded(Player player) {
        boolean driver = this.getFirstPassenger() == player;
        player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.controls." + this.siege().id() + (driver ? "" : ".rider"))
                .withStyle(ChatFormatting.GRAY));
        this.overlayTimer = 50;
    }
}
