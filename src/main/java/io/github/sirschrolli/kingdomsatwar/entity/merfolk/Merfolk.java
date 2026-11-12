package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Was Meerjungfrauen und Tritonwachen gemeinsam haben (seit 0.93): Schwimmen, die Haltung im Wasser, der Ruf der Wachen. */
public final class Merfolk {
    /** So weit (Blöcke) hören die Wachen, wenn jemand aus dem Dorf angegriffen wird. */
    public static final double ALERT_RANGE = 32.0;

    private Merfolk() {}

    /** Schwimmen ohne Schwerkraft: Schub in Blickrichtung, das Wasser bremst. */
    public static void swim(Mob mob, Vec3 input) {
        mob.moveRelative(mob.getSpeed(), input);
        mob.move(MoverType.SELF, mob.getDeltaMovement());
        mob.setDeltaMovement(mob.getDeltaMovement().scale(0.88));
    }

    /** Ruft die Tritonwachen in der Nähe gegen jemanden, der das Meervolk angegriffen hat (nicht gegen Spieler im Kreativmodus). */
    public static void alert(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        if (attacker instanceof MerfolkLike || attacker instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        List<TritonEntity> guards = level.getEntitiesOfClass(TritonEntity.class, new AABB(victim.blockPosition()).inflate(ALERT_RANGE));
        for (TritonEntity guard : guards) {
            guard.defend(attacker);
        }
    }

    /**
     * Nur auf dem Client: Haltung und Schwanzschlag. Die Haltung folgt der Geschwindigkeit weich (aufrecht wassertretend bis
     * waagrecht schwimmend), der Schwanz schlägt ruhig, beim Schwimmen schneller und kräftiger.
     */
    public static final class SwimMotion {
        public float pose;
        public float poseO;
        public float phase;
        public float phaseO;

        public void tick(Mob mob) {
            this.poseO = this.pose;
            this.phaseO = this.phase;
            // auf dem Client zählt, wie weit sie sich seit dem letzten Tick bewegt hat (die Bewegung anderer Wesen kommt
            // dort als Position an, nicht immer als Geschwindigkeit)
            double dx = mob.getX() - mob.xo;
            double dy = mob.getY() - mob.yo;
            double dz = mob.getZ() - mob.zo;
            double speed = Math.sqrt(dx * dx + dz * dz + dy * dy * 0.3);
            float target = mob.isInWater() ? Mth.clamp((float) ((speed - 0.025) / 0.07), 0.0f, 1.0f) : 0.0f;
            this.pose += (target - this.pose) * (target > this.pose ? 0.12f : 0.06f);
            this.phase += 0.09f + (float) Math.min(0.45, speed * 2.2);
        }

        public float pose(float partialTicks) {
            return Mth.lerp(partialTicks, this.poseO, this.pose);
        }

        public float phase(float partialTicks) {
            return Mth.lerp(partialTicks, this.phaseO, this.phase);
        }
    }
}
