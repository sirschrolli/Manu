package io.github.sirschrolli.kingdomsatwar.siege;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ballistik der Belagerungswaffen: Wurfweite für ein Ziel finden, Flugbahn vorausberechnen und dem Schützen als
 * Leuchtspur zeigen (nur er sieht sie). Gerechnet wird genau wie im Flug: erst Schwerkraft, dann Luftwiderstand.
 */
public final class SiegeAim {
    private static final int MAX_STEPS = 400;

    private SiegeAim() {}

    /** Abwurfwinkel der Wurfwaffen (Bogenmaß): das Katapult wirft flacher, der Tribok steiler. */
    public static double launchAngle(SiegeType siege) {
        return Math.toRadians(siege == SiegeType.TREBUCHET ? 50.0 : 42.0);
    }

    /**
     * Die Abschussgeschwindigkeit, mit der ein Wurf von {@code from} auf der Höhe von {@code target} in dessen
     * waagerechter Entfernung landet (begrenzt auf das, was die Waffe kann).
     */
    public static double solve(SiegeType siege, Vec3 from, Vec3 target) {
        double dist = target.subtract(from).horizontalDistance();
        double dy = target.y - from.y;
        double lo = siege.minVelocity();
        double hi = siege.maxVelocity();
        if (reach(siege, hi, dy) < dist) {
            return hi;
        }
        if (reach(siege, lo, dy) > dist) {
            return lo;
        }
        for (int i = 0; i < 18; i++) {
            double mid = (lo + hi) * 0.5;
            if (reach(siege, mid, dy) < dist) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return (lo + hi) * 0.5;
    }

    /** Waagerechte Weite eines Wurfs mit Geschwindigkeit {@code v}, bis er wieder auf Höhe {@code dy} fällt. */
    static double reach(SiegeType siege, double v, double dy) {
        double angle = launchAngle(siege);
        double vx = Math.cos(angle) * v;
        double vy = Math.sin(angle) * v;
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < MAX_STEPS; i++) {
            vy -= siege.gravity();
            vx *= siege.drag();
            vy *= siege.drag();
            double nx = x + vx;
            double ny = y + vy;
            if (vy < 0.0 && ny <= dy) {
                double f = (y - dy) / Math.max(1.0e-6, y - ny);
                return x + (nx - x) * f;
            }
            x = nx;
            y = ny;
        }
        return x;
    }

    /** Waagerechte Weite eines Schusses mit diesem Geschwindigkeitsvektor bis zurück auf Abschusshöhe. */
    public static double range(SiegeType siege, Vec3 velocity) {
        double vx = velocity.horizontalDistance();
        double vy = velocity.y;
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < MAX_STEPS; i++) {
            vy -= siege.gravity();
            vx *= siege.drag();
            vy *= siege.drag();
            x += vx;
            y += vy;
            if (vy < 0.0 && y <= 0.0) {
                return x;
            }
        }
        return x;
    }

    /**
     * Leuchtspur der Flugbahn bis zum Einschlag (alle paar Ticks neu, nur für den Schützen), am Einschlag ein Kreis in der
     * Größe der Wirkung.
     */
    static void preview(ServerLevel level, SiegeWeaponEntity weapon, ServerPlayer player) {
        SiegeType siege = weapon.siege();
        Vec3 pos = weapon.launchPoint();
        Vec3 v = weapon.launchVelocity();
        DustParticleOptions dust = SiegeWeaponEntity.previewDust(siege);
        Vec3 impact = null;
        for (int i = 0; i < MAX_STEPS; i++) {
            v = new Vec3(v.x, v.y - siege.gravity(), v.z).scale(siege.drag());
            Vec3 next = pos.add(v);
            BlockHitResult hit = level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, weapon));
            if (hit.getType() != HitResult.Type.MISS) {
                impact = hit.getLocation();
                break;
            }
            if (i % 2 == 0 && i > 1) {
                level.sendParticles(player, dust, true, true, next.x, next.y, next.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            pos = next;
            if (pos.y < level.getMinY()) {
                break;
            }
        }
        if (impact != null) {
            double radius = Math.max(1.0, siege.blastRadius() * 0.6);
            for (int k = 0; k < 14; k++) {
                double a = k * Mth.TWO_PI / 14;
                level.sendParticles(player, new DustParticleOptions(0xFF4030, 1.2f), true, true, impact.x + Math.cos(a) * radius, impact.y + 0.15,
                        impact.z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }
}
