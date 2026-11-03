package io.github.sirschrolli.kingdomsatwar.siege;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

/**
 * Die vier Belagerungswaffen und ihre Werte.
 *
 * <ul>
 *     <li><b>Katapult</b>: wirft Steinkugeln im Bogen (16–70 Blöcke weit), reißt kleine Löcher in Mauern</li>
 *     <li><b>Tribok</b>: die große Wurfmaschine – weiter (35–130 Blöcke) und verheerender, aber langsam nachzuladen und schwerfällig</li>
 *     <li><b>Balliste</b>: schießt schwere Bolzen fast gerade, wohin man blickt – gegen Krieger, Reiter und andere Belagerungswaffen</li>
 *     <li><b>Rammbock</b>: fahrbares Schutzdach mit Widder – bricht Tore, Türen, Fallgitter und Gitter</li>
 * </ul>
 */
public enum SiegeType implements StringRepresentable {
    CATAPULT(120.0f, 0.055, 2.2f, 100, Ammo.STONE, 1.0, 2.2, 0.05, 0.99, 14.0f, 1.5, 3.0, 2.2f, 2.35f, new Vec3(-1.35, 0.0, -0.25)),
    TREBUCHET(220.0f, 0.03, 1.0f, 220, Ammo.STONE, 1.6, 3.2, 0.05, 0.99, 24.0f, 2.3, 4.0, 2.7f, 5.0f, new Vec3(-1.85, 0.0, -0.9)),
    BALLISTA(90.0f, 0.075, 3.0f, 60, Ammo.BOLT, 3.6, 3.6, 0.02, 0.99, 18.0f, 0.0, 0.0, 1.8f, 1.5f, new Vec3(0.0, 0.0, -1.3)),
    RAM(260.0f, 0.065, 2.0f, 34, Ammo.NONE, 0.0, 0.0, 0.0, 1.0, 9.0f, 0.0, 0.0, 2.4f, 2.6f, new Vec3(-0.6, 0.0, -0.3));

    /** Welche Munition eine Waffe verschießt. */
    public enum Ammo { STONE, BOLT, NONE }

    private final float health;
    private final double speed;
    private final float turnRate;
    private final int reload;
    private final Ammo ammo;
    private final double minVelocity;
    private final double maxVelocity;
    private final double gravity;
    private final double drag;
    private final float damage;
    private final double blockRadius;
    private final double blastRadius;
    private final float width;
    private final float height;
    private final Vec3 seat;

    SiegeType(float health, double speed, float turnRate, int reload, Ammo ammo, double minVelocity, double maxVelocity, double gravity,
            double drag, float damage, double blockRadius, double blastRadius, float width, float height, Vec3 seat) {
        this.health = health;
        this.speed = speed;
        this.turnRate = turnRate;
        this.reload = reload;
        this.ammo = ammo;
        this.minVelocity = minVelocity;
        this.maxVelocity = maxVelocity;
        this.gravity = gravity;
        this.drag = drag;
        this.damage = damage;
        this.blockRadius = blockRadius;
        this.blastRadius = blastRadius;
        this.width = width;
        this.height = height;
        this.seat = seat;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    /** Wie viel sie aushält. */
    public float health() {
        return this.health;
    }

    /** Fahrgeschwindigkeit (Blöcke je Tick). */
    public double speed() {
        return this.speed;
    }

    /** So viele Grad je Tick dreht sie sich zur Blickrichtung des Schützen. */
    public float turnRate() {
        return this.turnRate;
    }

    /** Nachladezeit in Ticks (beim Rammbock: ein Stoß). */
    public int reload() {
        return this.reload;
    }

    public Ammo ammo() {
        return this.ammo;
    }

    /** Abschussgeschwindigkeit bei flachstem Blick (Wurfwaffen) bzw. immer (Balliste). */
    public double minVelocity() {
        return this.minVelocity;
    }

    /** Abschussgeschwindigkeit bei steilstem Blick. */
    public double maxVelocity() {
        return this.maxVelocity;
    }

    public double gravity() {
        return this.gravity;
    }

    public double drag() {
        return this.drag;
    }

    /** Schaden am Einschlag (Mitte) bzw. je Bolzen. */
    public float damage() {
        return this.damage;
    }

    /** Halbmesser, in dem ein Einschlag Blöcke zerschlägt (0: keine). */
    public double blockRadius() {
        return this.blockRadius;
    }

    /** Halbmesser, in dem ein Einschlag Wesen trifft. */
    public double blastRadius() {
        return this.blastRadius;
    }

    public float width() {
        return this.width;
    }

    public float height() {
        return this.height;
    }

    /**
     * Platz des Schützen relativ zur Waffe (x: links +/rechts −, Höhe, z: vorn +/hinten −), wo er steht: bei Katapult und
     * Tribok rechts neben dem Gestell an der Winde (von dort hat er freie Sicht nach vorn), hinter der Balliste, im Rammbock.
     */
    public Vec3 seat() {
        return this.seat;
    }

    /** Wirft sie im Bogen (Wurfweite stellt sich auf das anvisierte Ziel ein)? */
    public boolean lobs() {
        return this == CATAPULT || this == TREBUCHET;
    }

    /** Sitzt der Schütze? (Alle stehen: neben dem Gestell, hinter der Balliste, im Rammbock.) */
    public boolean seated() {
        return false;
    }
}
