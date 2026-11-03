package io.github.sirschrolli.kingdomsatwar.entity.boss;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Hilfen für Boss-Angriffe: Partikel, Geräusche, Kegel- und Flächentreffer, Vorwarnungen. */
public final class BossFx {
    private BossFx() {}

    public static void sound(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.HOSTILE, volume, pitch);
    }

    /** Teilchenwolke, nach allen Seiten gleich weit gestreut (mit freier Sicht für den, der mittendrin steht – siehe {@link #impact}). */
    public static void burst(ServerLevel level, ParticleOptions particle, Vec3 pos, int count, double spread, double speed) {
        impact(level, particle, pos, count, spread, spread, spread, speed);
    }

    /** So weit vor den Augen eines Spielers entstehen keine Teilchen einer Wolke, in der er selbst steht. */
    public static final double CLEAR_SIGHT = 1.7;

    /**
     * Teilchenwolke an einer Stelle, an der auch ein Spieler stehen kann (Einschlag, Explosion, Zauber am Gegner).
     * Zuschauer sehen sie wie gewohnt. Wer aber mittendrin steht – meist der Getroffene selbst –, bekäme die Teilchen
     * direkt vor die Augen: bildschirmfüllende bunte Klötze, hinter denen man nichts mehr sieht. Für ihn werden die
     * Teilchen einzeln verteilt: die, die näher als {@link #CLEAR_SIGHT} vor seinen Augen entstünden, entfallen, und
     * keines fliegt auf ihn zu.
     */
    public static void impact(ServerLevel level, ParticleOptions particle, Vec3 c, int count, double dx, double dy, double dz, double speed) {
        if (count <= 0) {
            level.sendParticles(particle, c.x, c.y, c.z, count, dx, dy, dz, speed);
            return;
        }
        double spread = Math.max(dx, Math.max(dy, dz));
        double clear = clearance(particle);
        // eine weite Wolke ist dünn – da landet kaum ein Teilchen vor den Augen
        double near = spread > 2.5 ? 0.0 : clear + 2.5 * spread + 8.0 * speed;
        var random = level.getRandom();
        for (var player : level.players()) {
            Vec3 eye = player.getEyePosition();
            if (eye.distanceToSqr(c) >= near * near) {
                level.sendParticles(player, particle, false, false, c.x, c.y, c.z, count, dx, dy, dz, speed);
                continue;
            }
            for (int i = 0; i < Math.min(count, 160); i++) {
                double x = c.x + random.nextGaussian() * dx;
                double y = c.y + random.nextGaussian() * dy;
                double z = c.z + random.nextGaussian() * dz;
                double ex = x - eye.x;
                double ey = y - eye.y;
                double ez = z - eye.z;
                if (ex * ex + ey * ey + ez * ez < clear * clear) {
                    continue;
                }
                // Flugrichtung zufällig wie sonst – aber nie auf die Augen zu (Anzahl 0: dx/dy/dz sind die Geschwindigkeit)
                double vx = random.nextGaussian() * speed;
                double vy = random.nextGaussian() * speed;
                double vz = random.nextGaussian() * speed;
                if (vx * ex + vy * ey + vz * ez < 0.0) {
                    vx = -vx;
                    vy = -vy;
                    vz = -vz;
                }
                level.sendParticles(player, particle, false, false, x, y, z, 0, vx, vy, vz, 1.0);
            }
        }
    }

    /**
     * Wie weit eine Teilchenart vor den Augen bleiben muss: Explosionswolken sind einige Blöcke groß (der Auslöser
     * verteilt sie zudem ringsum) – die übrigen reichen mit {@link #CLEAR_SIGHT}.
     */
    static double clearance(ParticleOptions particle) {
        if (particle.getType() == ParticleTypes.EXPLOSION_EMITTER) {
            return 6.5;
        }
        return particle.getType() == ParticleTypes.EXPLOSION ? 4.5 : CLEAR_SIGHT;
    }

    /** Ist die Stelle so nah vor den Augen eines Spielers, dass Teilchen dort sein Bild verdecken würden? */
    public static boolean nearEyes(Level level, Vec3 pos) {
        for (Player player : level.players()) {
            if (player.getEyePosition().distanceToSqr(pos) < CLEAR_SIGHT * CLEAR_SIGHT) {
                return true;
            }
        }
        return false;
    }

    /** Partikelring auf dem Boden (Vorwarnung oder Welle). */
    public static void ring(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int count, double yOffset) {
        for (int i = 0; i < count; i++) {
            double a = i * Mth.TWO_PI / count;
            double x = center.x + Math.cos(a) * radius;
            double z = center.z + Math.sin(a) * radius;
            level.sendParticles(particle, x, center.y + yOffset, z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    /**
     * Gefüllter Warnkreis (z. B. wo gleich etwas einschlägt). Wer im Kreis steht, sieht ihn um sich herum – nur direkt
     * unter seinen Füßen nicht: was dort aufsteigt (Rauch, Funken), stiege ihm sonst ins Gesicht.
     */
    public static void warnDisc(ServerLevel level, ParticleOptions particle, Vec3 center, double radius, int count) {
        RandomPoints rnd = new RandomPoints(level);
        for (int i = 0; i < count; i++) {
            double r = Math.sqrt(rnd.next()) * radius;
            double a = rnd.next() * Mth.TWO_PI;
            double x = center.x + Math.cos(a) * r;
            double y = center.y + 0.15;
            double z = center.z + Math.sin(a) * r;
            for (var player : level.players()) {
                double dx = player.getX() - x;
                double dz = player.getZ() - z;
                if (dx * dx + dz * dz < 1.0 && Math.abs(player.getY() - y) < 2.5) {
                    continue;
                }
                level.sendParticles(player, particle, false, false, x, y, z, 1, 0.0, 0.02, 0.0, 0.0);
            }
        }
    }

    /** Blockbrocken-Partikel des Bodens (Stampfen, Aufprall). */
    public static void groundDust(ServerLevel level, Vec3 center, double radius, int count) {
        BlockPos below = BlockPos.containing(center.x, center.y - 0.5, center.z);
        BlockState state = level.getBlockState(below);
        if (state.isAir()) {
            state = level.getBlockState(below.below());
        }
        if (!state.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), center.x, center.y + 0.3, center.z, count,
                    radius * 0.5, 0.3, radius * 0.5, 0.15);
        }
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.3, center.z, count / 4, radius * 0.4, 0.2, radius * 0.4, 0.05);
    }

    /** Alle Gegner in einem Kegel vom Punkt {@code origin} in Richtung {@code dir}. */
    public static void cone(ElementalBoss boss, Vec3 origin, Vec3 dir, double range, double halfAngleDeg, Consumer<LivingEntity> hit) {
        Vec3 n = dir.normalize();
        double cos = Math.cos(Math.toRadians(halfAngleDeg));
        AABB box = new AABB(origin, origin.add(n.scale(range))).inflate(range * Math.sin(Math.toRadians(halfAngleDeg)) + 2.0);
        for (LivingEntity e : boss.enemiesIn(box)) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(origin);
            double d = to.length();
            if (d <= range && d > 0.01 && to.scale(1.0 / d).dot(n) >= cos) {
                hit.accept(e);
            }
        }
    }

    /** Stoßwelle am Boden: Schaden und Hochschleudern, nach außen schwächer. */
    public static void shockwave(ServerLevel level, ElementalBoss boss, Vec3 center, double radius, float damage, double launch) {
        List<LivingEntity> hits = boss.enemiesAround(center, radius, 4.0);
        for (LivingEntity e : hits) {
            double d = e.position().subtract(center).horizontalDistance();
            float f = (float) (1.0 - d / radius * 0.5);
            if (e.onGround() || e.getY() - center.y < 1.5) {
                boss.hurtTarget(level, e, damage * f);
                DragonBossEntity.pushAway(e, center, 1.2 * f, launch * f);
            }
        }
        groundDust(level, center, radius * 0.6, 60);
        for (int r = 2; r <= radius; r += 2) {
            ring(level, ParticleTypes.CLOUD, center, r, (int) (r * 5), 0.2);
        }
    }

    /** Linie von Partikeln (z. B. Odem-Strahl), jitter = seitliche Streuung je Block. */
    public static void stream(ServerLevel level, ParticleOptions particle, Vec3 from, Vec3 dir, double length, double jitter,
            int perBlock, double speed) {
        Vec3 n = dir.normalize();
        RandomPoints rnd = new RandomPoints(level);
        for (double t = 0.5; t < length; t += 1.0 / perBlock) {
            double spread = jitter * t * 0.18 + 0.1;
            Vec3 p = from.add(n.scale(t));
            // Anzahl 0: dx/dy/dz sind dann die Flugrichtung, "speed" ihr Betrag
            level.sendParticles(particle, p.x + (rnd.next() - 0.5) * spread, p.y + (rnd.next() - 0.5) * spread,
                    p.z + (rnd.next() - 0.5) * spread, 0, n.x, n.y, n.z, speed);
        }
    }

    /** Kleiner Zufallsgenerator auf Basis des Level-Zufalls (für Partikel). */
    static final class RandomPoints {
        private final ServerLevel level;

        RandomPoints(ServerLevel level) {
            this.level = level;
        }

        double next() {
            return this.level.getRandom().nextDouble();
        }
    }
}
