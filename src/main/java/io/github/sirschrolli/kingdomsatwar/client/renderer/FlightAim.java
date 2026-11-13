package io.github.sirschrolli.kingdomsatwar.client.renderer;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Richtet fliegende Speere, Lanzen, Bolzen und Dreizacke genau in ihre Flugrichtung aus – aus der Bewegung selbst
 * berechnet, nicht aus der gespeicherten Drehung des Geschosses. Die hinkt bei Minecraft hinterher (sie folgt der
 * Bahn nur zu einem Fünftel je Tick), kommt beim Wurf aus dem Laufen schräg an, und Geschosse, die von
 * {@code AbstractHurtingProjectile} erben, drehen sie sogar in eine andere Richtung als Pfeile.
 *
 * <p>Ergebnis wie bei Pfeilen: Gier = atan2(x, z), Neigung nach oben positiv (Grad). Zwischen zwei Ticks wird zwischen
 * dem letzten Schritt und dem nächsten überblendet, damit die Spitze ruhig der Bahn folgt; plötzliche Wendungen (ein
 * Abprall) dreht sie in ein paar Ticks nach. Steht das Geschoss still (steckt es), bleibt die letzte Flugrichtung.
 */
public final class FlightAim {
    /** Größte Drehung je Tick bei plötzlichen Richtungswechseln (Grad). */
    private static final float MAX_TURN = 40.0f;
    private static final double STILL = 1.0e-7;

    private static final Map<Entity, float[]> LAST = new WeakHashMap<>();

    private FlightAim() {}

    /**
     * Gier und Neigung (Grad) in {@code out[0]} und {@code out[1]}.
     *
     * @param backwards rückwärts ausrichten (der heimkehrende Dreizack fliegt mit dem Griff voran)
     * @param stillUsesRotation steht das Geschoss still, gilt seine eigene Drehung (etwa ein schwebender Lichtspeer, der
     *         zielt); sonst die zuletzt geflogene Richtung (ein steckender Dreizack)
     */
    public static void aim(Entity entity, float partialTicks, boolean backwards, boolean stillUsesRotation, float[] out) {
        Vec3 next = entity.getDeltaMovement();
        Vec3 last = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        Vec3 v;
        if (next.lengthSqr() > STILL && last.lengthSqr() > STILL && last.dot(next) > 0.0) {
            // vom letzten Schritt zum nächsten überblenden (beide auf gleiche Länge gebracht)
            v = last.normalize().lerp(next.normalize(), partialTicks);
        } else if (next.lengthSqr() > STILL) {
            v = next;
        } else {
            v = last;
        }
        float time = entity.tickCount + partialTicks;
        float[] memo = LAST.get(entity);
        if (v.lengthSqr() <= STILL) {
            if (stillUsesRotation || memo == null) {
                out[0] = entity.getYRot(partialTicks);
                out[1] = entity.getXRot(partialTicks);
                LAST.put(entity, new float[] {out[0], out[1], time});
            } else {
                out[0] = memo[0];
                out[1] = memo[1];
            }
            return;
        }
        if (backwards) {
            v = v.scale(-1.0);
        }
        float yaw = (float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG);
        if (memo != null && time >= memo[2] && time - memo[2] < 10.0f) {
            float step = MAX_TURN * Math.max(time - memo[2], 0.05f);
            yaw = Mth.approachDegrees(memo[0], yaw, step);
            pitch = Mth.approach(memo[1], pitch, step);
        }
        out[0] = yaw;
        out[1] = pitch;
        if (memo == null) {
            LAST.put(entity, new float[] {yaw, pitch, time});
        } else {
            memo[0] = yaw;
            memo[1] = pitch;
            memo[2] = time;
        }
    }
}
