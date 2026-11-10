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
    /** Die Klangwelten der Biome (Wind, Laub, Vögel, Brandung …) abspielen. */
    public static final ModConfigSpec.BooleanValue BIOME_AMBIENCE;
    /** Wie laut die Klangwelten der Biome sind (Prozent). */
    public static final ModConfigSpec.IntValue BIOME_AMBIENCE_VOLUME;
    /** Ab 21 Herzen die Lebensleiste als eine Reihe in farbigen Schichten zeigen (sonst stapelt Minecraft die Reihen). */
    public static final ModConfigSpec.BooleanValue HERO_HEARTS;

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
        BIOME_AMBIENCE = builder
                .comment("Spielt die Klangwelten der Biome: Wind, rauschendes Laub, Vögel, Brandung, Grollen … – nie dauerhaft,"
                        + " sondern ab und zu, mit langen ruhigen Pausen.")
                .translation("kingdomsatwar.configuration.biomeAmbience")
                .define("biomeAmbience", true);
        BIOME_AMBIENCE_VOLUME = builder
                .comment("Lautstärke der Biom-Klänge in Prozent (0 = stumm). Zusätzlich gilt der Regler „Umgebung“ in den Musik- und"
                        + " Toneinstellungen von Minecraft.")
                .translation("kingdomsatwar.configuration.biomeAmbienceVolume")
                .defineInRange("biomeAmbienceVolume", 60, 0, 100);
        HERO_HEARTS = builder
                .comment("Ab 21 Herzen (oder 21 Rüstungspunkten) zeigt die Leiste eine einzige Reihe in farbigen Schichten (je zehn"
                        + " Herzen bzw. zwanzig Punkte eine Farbe) und daneben die Zahl, statt die Herzen zu einer Wand aus Reihen zu stapeln.")
                .translation("kingdomsatwar.configuration.heroHearts")
                .define("heroHearts", true);
        SPEC = builder.build();
    }

    private ModClientConfig() {}
}
