package io.github.sirschrolli.kingdomsatwar.entity.creator;

import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Farben, Lichtwirkungen und kleine Rechenhilfen für Schrolli den Schöpfer, seine Boten und seine Geschosse. */
public final class DivineFx {
    /** Gold des Lichts, weißes Licht, warmes Heiligenlicht, Bernstein. */
    public static final int GOLD = 0xFFD24A;
    public static final int WHITE = 0xFFF6D8;
    public static final int HOLY = 0xFFE89A;
    public static final int AMBER = 0xFFA838;
    /** Die Farben der Wahren Gestalt: Violett des Alls, Türkis, Rosa. */
    public static final int COSMIC = 0xB070FF;
    public static final int CYAN = 0x80E8FF;
    public static final int ROSE = 0xFF80C8;
    public static final int EMBER = 0xFF5A20;
    public static final int FROST = 0xA8E8FF;

    public static final ParticleOptions GOLD_DUST = new DustParticleOptions(GOLD, 1.6f);
    public static final ParticleOptions WHITE_DUST = new DustParticleOptions(WHITE, 1.3f);
    public static final ParticleOptions VIOLET_DUST = new DustParticleOptions(COSMIC, 1.6f);
    public static final ParticleOptions CYAN_DUST = new DustParticleOptions(CYAN, 1.4f);

    private DivineFx() {}

    /** Eine sichtbare Wirkung (Warnkreis, Säule, Strahl, Ring …) – noch nicht in der Welt, erst {@code spawn()}. */
    public static SpellFxEntity fx(ServerLevel level, SpellFxEntity.Kind kind, Vec3 pos, int color, int life, float size) {
        return SpellFxEntity.create(level, kind, pos, null, color, life, size);
    }

    /** Ein Lichtblitz (die Farbe als RGB). */
    public static void flash(ServerLevel level, Vec3 at, int rgb) {
        level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFF000000 | rgb), at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    /** Goldene Funken, die aufleuchten und verlöschen. */
    public static void sparks(ServerLevel level, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(ModParticles.DIVINE_SPARK.get(), at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** Ein Kranz aus Teilchen. */
    public static void ring(ServerLevel level, ParticleOptions particle, Vec3 c, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2.0 / count;
            level.sendParticles(particle, c.x + Math.cos(a) * radius, c.y, c.z + Math.sin(a) * radius, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    /** Eine Linie aus Teilchen. */
    public static void line(ServerLevel level, ParticleOptions particle, Vec3 a, Vec3 b, double step) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 1.0e-3) {
            return;
        }
        for (double s = 0.0; s <= len; s += step) {
            Vec3 p = a.add(d.scale(s / len));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
    }

    /** Licht strömt von allen Seiten auf eine Stelle zu (beim Sammeln der Kraft). */
    public static void gather(ServerLevel level, Vec3 c, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double a = level.getRandom().nextDouble() * Math.PI * 2.0;
            double y = (level.getRandom().nextDouble() - 0.5) * radius;
            Vec3 from = c.add(Math.cos(a) * radius, y, Math.sin(a) * radius);
            Vec3 v = c.subtract(from).scale(0.08);
            level.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 0, v.x, v.y, v.z, 1.0);
        }
    }

    public static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, soft(pitch));
    }

    /**
     * Hohe Tonlagen werden gestaucht (seit 0.76): Klänge des Schöpfers klingen höchstens ein wenig heller, nie schrill –
     * 1,6 wird zu 1,21, 1,4 zu 1,14; tiefere Lagen bleiben, wie sie sind.
     */
    public static float soft(float pitch) {
        return pitch > 1.0f ? 1.0f + (pitch - 1.0f) * 0.35f : pitch;
    }

    /** Abstand eines Punkts von einer Strecke. */
    public static double distanceToSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double len2 = ab.lengthSqr();
        if (len2 < 1.0e-6) {
            return p.distanceTo(a);
        }
        double t = Math.max(0.0, Math.min(1.0, p.subtract(a).dot(ab) / len2));
        return p.distanceTo(a.add(ab.scale(t)));
    }

    /** Waagrechter Abstand zweier Punkte. */
    public static double flat(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Waagrechte Richtung zum Winkel (Grad, 0 = Süden wie die Blickrichtung der Wesen). */
    public static Vec3 dir(double yawDeg) {
        double r = Math.toRadians(yawDeg);
        return new Vec3(-Math.sin(r), 0.0, Math.cos(r));
    }

    /** Der Gierwinkel von a nach b. */
    public static double yaw(Vec3 a, Vec3 b) {
        return Math.toDegrees(Math.atan2(b.z - a.z, b.x - a.x)) - 90.0;
    }
}
