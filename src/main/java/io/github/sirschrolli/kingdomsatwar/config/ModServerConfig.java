package io.github.sirschrolli.kingdomsatwar.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Einstellungen der Welt bzw. des Servers (im Weltordner unter {@code serverconfig}); der Server gibt sie an alle Spieler weiter.
 */
public final class ModServerConfig {
    public static final ModConfigSpec SPEC;
    /** Wie viele Soldaten ein Spieler höchstens anwerben kann. */
    public static final ModConfigSpec.IntValue ARMY_LIMIT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ARMY_LIMIT = builder
                .comment("Wie viele Soldaten ein Spieler höchstens gleichzeitig anwerben kann.")
                .translation("kingdomsatwar.configuration.armyLimit")
                .defineInRange("armyLimit", 32, 1, 256);
        SPEC = builder.build();
    }

    private ModServerConfig() {}
}
