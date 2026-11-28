package io.github.sirschrolli.kingdomsatwar.entity.miniboss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Ein besonderer Angriff eines Mini-Bosses: Er kündigt sich an (Haltung, Klang, Warnkreis oder Linie), wirkt und braucht
 * danach eine Weile, bis er wieder bereit ist. Läuft {@link #duration()} Ticks lang; {@code t} zählt ab 0.
 */
public abstract class MiniSpecial {
    private final String id;
    private final int duration;
    private final int weight;
    int cooldown;

    protected MiniSpecial(String id, int duration, int weight) {
        this.id = id;
        this.duration = duration;
        this.weight = weight;
    }

    /** Der Name (für Tests und Vorführungen). */
    public String id() {
        return this.id;
    }

    public int duration() {
        return this.duration;
    }

    public int weight() {
        return this.weight;
    }

    /** Kann er ihn jetzt gegen dieses Ziel einsetzen (in diesem Abstand)? */
    public abstract boolean ready(LivingEntity target, double distance);

    /** Ein Tick des Angriffs. */
    public abstract void tick(ServerLevel level, LivingEntity target, int t);

    /** Wartezeit danach (Ticks), ehe er diesen Angriff wieder einsetzt. */
    public abstract int recharge(RandomSource random);

    public void start(ServerLevel level, LivingEntity target) {}

    /** Aufräumen (auch wenn der Angriff abbricht). */
    public void stop(ServerLevel level) {}

    /** Nur in Rage? */
    public boolean enragedOnly() {
        return false;
    }

    /** Steht er dabei still? */
    public boolean roots() {
        return true;
    }
}
