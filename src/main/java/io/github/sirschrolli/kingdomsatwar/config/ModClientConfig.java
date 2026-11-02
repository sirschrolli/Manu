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
    /** Wie Spinnen aussehen: detailliert, klassisch (Minecraft) oder – im Arachnophobie-Modus – als Mooslinge. */
    public static final ModConfigSpec.EnumValue<SpiderLook> SPIDER_LOOK;

    /** Aussehen der Spinnen. */
    public enum SpiderLook implements net.neoforged.neoforge.common.TranslatableEnum {
        /** Das eigene, lebensechte Modell der Mod. */
        DETAILED,
        /** Das schlichte Standard-Aussehen von Minecraft. */
        CLASSIC,
        /** Arachnophobie-Modus: statt Spinnen runde, freundliche Moosbälle ohne Beine. */
        MOOSLING;

        @Override
        public net.minecraft.network.chat.Component getTranslatedName() {
            return net.minecraft.network.chat.Component.translatable("kingdomsatwar.configuration.spiderLook." + this.name().toLowerCase(java.util.Locale.ROOT));
        }
    }

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
        SPIDER_LOOK = builder
                .comment("Aussehen der Spinnen: DETAILED (eigenes Modell), CLASSIC (Standard von Minecraft) oder MOOSLING"
                        + " (Arachnophobie-Modus: runde Moosbälle ohne Beine statt Spinnen).")
                .translation("kingdomsatwar.configuration.spiderLook")
                .defineEnum("spiderLook", SpiderLook.DETAILED);
        SPEC = builder.build();
    }

    private ModClientConfig() {}
}
