package io.github.sirschrolli.kingdomsatwar.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Einstellungen der Welt bzw. des Servers (im Weltordner unter {@code serverconfig}); der Server gibt sie an alle Spieler weiter.
 */
public final class ModServerConfig {
    public static final ModConfigSpec SPEC;
    /** Wie viele Soldaten ein Spieler höchstens anwerben kann. */
    public static final ModConfigSpec.IntValue ARMY_LIMIT;
    /** Wie oft man mit einem Bürger handeln kann, bevor sein Laden Pause macht. */
    public static final ModConfigSpec.IntValue TRADES_BEFORE_PAUSE;
    /** Wie lange die Pause dauert (Minuten Echtzeit). */
    public static final ModConfigSpec.IntValue TRADE_PAUSE_MINUTES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ARMY_LIMIT = builder
                .comment("Wie viele Soldaten ein Spieler höchstens gleichzeitig anwerben kann.")
                .translation("kingdomsatwar.configuration.armyLimit")
                .defineInRange("armyLimit", 32, 1, 256);
        TRADES_BEFORE_PAUSE = builder
                .comment("Wie oft man mit einem Bürger handeln kann, bevor sein Laden eine Pause macht.")
                .translation("kingdomsatwar.configuration.tradesBeforePause")
                .defineInRange("tradesBeforePause", 16, 1, 1000);
        TRADE_PAUSE_MINUTES = builder
                .comment("Wie lange (Minuten Echtzeit) ein Laden nach Erreichen der Handelsgrenze geschlossen bleibt.")
                .translation("kingdomsatwar.configuration.tradePauseMinutes")
                .defineInRange("tradePauseMinutes", 180, 0, 10_080);
        SPEC = builder.build();
    }

    private ModServerConfig() {}
}
