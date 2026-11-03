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
    /** Zerschlagen Belagerungswaffen Blöcke (Mauern, Tore)? */
    public static final ModConfigSpec.BooleanValue SIEGE_BLOCK_DAMAGE;
    /** Schicken Reiche, mit denen man im Krieg liegt, Heerhaufen gegen die eigene Siedlung? */
    public static final ModConfigSpec.BooleanValue REALM_RAIDS;
    /** So viele Spieltage vergehen mindestens zwischen zwei Heerhaufen eines Reichs. */
    public static final ModConfigSpec.IntValue RAID_INTERVAL_DAYS;

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
        SIEGE_BLOCK_DAMAGE = builder
                .comment("Belagerungswaffen zerschlagen Mauern, Tore und Türen (nie in Siedlungen, am Siegelkreis oder am Reichsbanner).")
                .translation("kingdomsatwar.configuration.siegeBlockDamage")
                .define("siegeBlockDamage", true);
        REALM_RAIDS = builder
                .comment("Reiche, mit denen ein Spieler im Krieg liegt, schicken Heerhaufen gegen seine Siedlung (nur, wenn er dort ist).")
                .translation("kingdomsatwar.configuration.realmRaids")
                .define("realmRaids", true);
        RAID_INTERVAL_DAYS = builder
                .comment("So viele Spieltage vergehen mindestens zwischen zwei Heerhaufen desselben Reichs.")
                .translation("kingdomsatwar.configuration.raidIntervalDays")
                .defineInRange("raidIntervalDays", 3, 1, 30);
        SPEC = builder.build();
    }

    private ModServerConfig() {}
}
