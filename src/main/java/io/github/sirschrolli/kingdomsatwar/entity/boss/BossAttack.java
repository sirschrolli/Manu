package io.github.sirschrolli.kingdomsatwar.entity.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Ein Angriff eines Drachen-Bosses mit Ablauf in Ticks: Ansetzen (Vorwarnung), Ausführen, Erholen.
 *
 * <p>Die Nummer ({@link #id()}) wird an die Clients geschickt, damit das Modell die passende Animation abspielt.
 * Wann ein Angriff in Frage kommt, sagt {@link #weight}; der Boss wählt unter allen möglichen Angriffen
 * gewichtet zufällig.
 */
public abstract class BossAttack {
    /** Wo der Boss sein muss, damit der Angriff möglich ist. */
    public enum Where { GROUND, AIR, WATER, ANY }

    private final int id;
    private final String name;
    protected int cooldownLeft;

    protected BossAttack(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    /** Gesamtdauer in Ticks. */
    public abstract int duration();

    /** Pause in Ticks, bevor derselbe Angriff wieder gewählt werden kann. */
    public abstract int cooldown();

    /** Ab welcher Kampfphase (1–3) der Angriff möglich ist. */
    public int minPhase() {
        return 1;
    }

    public Where where() {
        return Where.GROUND;
    }

    /** Hält der Boss während des Angriffs an? Sonst steuert der Angriff die Bewegung selbst
     * (z. B. Landen, Sturzflug; für normales Weiterbewegen {@link DragonBossEntity#moveInFight} aufrufen). */
    public boolean stopsMovement() {
        return true;
    }

    /** Dreht sich der Boss während des Ansetzens zum Ziel? (bis zu diesem Tick) */
    public int turnUntil() {
        return 0;
    }

    /**
     * Wie gern der Angriff jetzt gewählt wird (0 = gar nicht). {@code distance} = waagerechter Abstand zum Ziel,
     * {@code angle} = Winkel zwischen Blickrichtung und Ziel (0 = genau voraus, 180 = dahinter).
     */
    public abstract float weight(DragonBossEntity boss, LivingEntity target, double distance, float angle);

    public void start(ServerLevel level, DragonBossEntity boss, LivingEntity target) {
    }

    /** Wird jeden Tick aufgerufen ({@code tick} zählt ab 0). */
    public abstract void tick(ServerLevel level, DragonBossEntity boss, LivingEntity target, int tick);

    public void stop(ServerLevel level, DragonBossEntity boss) {
    }

    boolean ready() {
        return cooldownLeft <= 0;
    }

    void coolDown() {
        if (cooldownLeft > 0) {
            cooldownLeft--;
        }
    }

    void resetCooldown(float factor) {
        cooldownLeft = Math.round(cooldown() * factor);
    }
}
