package io.github.sirschrolli.kingdomsatwar.entity.boss;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Beute der Bosse, die mehrfach kommen (seit 1.1.5): Ihr Sonderstück (Waffe, Schwingen, Zylinder …) fällt bei jedem Sieg nur
 * mit {@link #SIGNATURE_CHANCE} – auch beim ersten. Der Manakristall kommt beim ersten eigenen Sieg sicher, danach nie
 * wieder; der Herzkristall (seit 1.2) bei jedem Sieg nur mit {@link #HEART_CHANCE}; Münzen und Schätze sind maßvoll. So lassen sich Waffen und Kristalle nicht beliebig ernten. Die
 * Beutetabellen prüfen den ersten Sieg über die Statistik „getötet“ des Spielers, der den Boss zuletzt getroffen hat; hier
 * steht die Wahrscheinlichkeit für Beute, die der Boss selbst fallen lässt (etwa der Gezeiten-Speer aus Karkinos' Kuppel).
 */
public final class BossLoot {
    private BossLoot() {}

    /** Wie oft das Sonderstück bei einem Sieg fällt (auch beim ersten). */
    public static final float SIGNATURE_CHANCE = 0.2f;

    /**
     * Seit 1.2 ist auch der Herzkristall nie sicher (die Mod soll schwer sein): Bosse, die mehrfach kommen, lassen ihn bei
     * jedem Sieg eines Spielers nur mit dieser Wahrscheinlichkeit fallen (je Kristall) – auch beim ersten.
     */
    public static final float HEART_CHANCE = 0.25f;

    /** … Bosse, die nur einmal kommen (Drachen, Nyxar, der Zerbrochene König, der Weltenfresser), je Kristall mit dieser. */
    public static final float HEART_CHANCE_ONCE = 0.5f;

    /**
     * Ist das der erste eigene Sieg dieses Spielers über diesen Boss? Die Statistik zählt den Sieg schon mit, sobald der Boss
     * fällt (nur wenn der Spieler den letzten Treffer selbst setzt – trifft zuletzt ein Soldat, ist es kein eigener Sieg).
     */
    public static boolean firstVictory(@Nullable Player player, EntityType<?> boss) {
        return player instanceof ServerPlayer server && server.getStats().getValue(Stats.ENTITY_KILLED.get(boss)) == 1;
    }

    /** Fällt das Sonderstück? Bei jedem Sieg mit {@link #SIGNATURE_CHANCE}. */
    public static boolean signatureDrops(LivingEntity boss) {
        return boss.getRandom().nextFloat() < SIGNATURE_CHANCE;
    }
}
