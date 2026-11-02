package io.github.sirschrolli.kingdomsatwar.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Einstellungen, die nur das eigene Spiel betreffen (Mods → Kingdoms at War → Konfiguration,
 * Datei {@code config/kingdomsatwar-client.toml}).
 */
public final class ModClientConfig {
    public static final ModConfigSpec SPEC;
    /** Erzähl-Intro beim ersten Betreten einer Welt zeigen. */
    public static final ModConfigSpec.BooleanValue SHOW_INTRO;
    /** Die Stimme des Erzählers zu den Kapiteln der Geschichte abspielen. */
    public static final ModConfigSpec.BooleanValue NARRATOR_VOICE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SHOW_INTRO = builder
                .comment("Zeigt beim ersten Betreten einer Welt das Erzähl-Intro (jederzeit wieder mit /kaw intro).")
                .translation("kingdomsatwar.configuration.showIntro")
                .define("showIntro", true);
        NARRATOR_VOICE = builder
                .comment("Der Erzähler spricht zu jedem Kapitel der Geschichte (sonst erscheint nur der Text).")
                .translation("kingdomsatwar.configuration.narratorVoice")
                .define("narratorVoice", true);
        SPEC = builder.build();
    }

    private ModClientConfig() {}
}
