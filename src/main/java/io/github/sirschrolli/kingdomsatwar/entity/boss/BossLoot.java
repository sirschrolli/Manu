package io.github.sirschrolli.kingdomsatwar.entity.boss;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Beute der Bosse, die mehrfach kommen (seit 1.1.3): Wer einen solchen Boss zum ersten Mal selbst besiegt, bekommt sein
 * Sonderstück (Waffe, Schwingen, Zylinder …) sicher, dazu Herz- oder Manakristall; bei jedem weiteren Sieg nur noch mit
 * {@link #SIGNATURE_CHANCE}, Kristalle gar nicht mehr. So lohnt sich der erste Kampf, ohne dass sich Waffen und Kristalle
 * beliebig ernten lassen. Die Beutetabellen prüfen dasselbe über die Statistik „getötet“ des Spielers, der den Boss zuletzt
 * getroffen hat; hier steht es für Beute, die der Boss selbst fallen lässt (etwa der Gezeiten-Speer aus Karkinos' Kuppel).
 */
public final class BossLoot {
    private BossLoot() {}

    /** Wie oft das Sonderstück nach dem ersten eigenen Sieg noch fällt. */
    public static final float SIGNATURE_CHANCE = 0.2f;

    /**
     * Ist das der erste eigene Sieg dieses Spielers über diesen Boss? Die Statistik zählt den Sieg schon mit, sobald der Boss
     * fällt (nur wenn der Spieler den letzten Treffer selbst setzt – trifft zuletzt ein Soldat, ist es kein eigener Sieg).
     */
    public static boolean firstVictory(@Nullable Player player, EntityType<?> boss) {
        return player instanceof ServerPlayer server && server.getStats().getValue(Stats.ENTITY_KILLED.get(boss)) == 1;
    }

    /** Fällt das Sonderstück? Beim ersten eigenen Sieg sicher, sonst mit {@link #SIGNATURE_CHANCE}. */
    public static boolean signatureDrops(LivingEntity boss) {
        return firstVictory(boss.getLastHurtByPlayer(), boss.getType()) || boss.getRandom().nextFloat() < SIGNATURE_CHANCE;
    }
}
