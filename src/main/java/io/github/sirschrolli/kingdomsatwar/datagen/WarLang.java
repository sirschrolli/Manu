package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.war.Realm;
import net.minecraft.world.item.DyeColor;

/** Texte zum Krieg der Königreiche: die vier Reiche, ihre Herrscher und Wappen. */
final class WarLang {
    @FunctionalInterface
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private WarLang() {}

    static void register(Text text) {
        realm(text, Realm.EISENMARK, "Kingdom of Eisenmark", "Königreich Eisenmark", "Eisenmark", "King Harald the Grey",
                "König Harald der Graue");
        realm(text, Realm.RABENSTEIN, "Duchy of Rabenstein", "Herzogtum Rabenstein", "Rabenstein", "Duchess Morwen", "Herzogin Morwen");
        realm(text, Realm.DORNFELS, "Principality of Dornfels", "Fürstentum Dornfels", "Dornfels", "Prince Kasimir", "Fürst Kasimir");
        realm(text, Realm.SILBERAU, "County of Silberau", "Grafschaft Silberau", "Silberau", "Count Ansgar", "Graf Ansgar");
        // die Wappenzeichen als Bannermuster ("Weißer Turm", "Gelbe Sonne" …)
        emblem(text, "tower", "Tower", "Turm", true);
        emblem(text, "raven", "Raven", "Rabe", true);
        emblem(text, "sun", "Sun", "Sonne", false);
        emblem(text, "swan", "Swan", "Schwan", true);
        // Grenze, Alarm, Fall des Herrschers
        text.add("realm.kingdomsatwar.discover.ruler", "Ruled by %s", "Es herrscht %s");
        text.add("realm.kingdomsatwar.discover.leaderless", "The realm mourns its fallen ruler", "Das Reich trauert um seinen gefallenen Herrscher");
        text.add("realm.kingdomsatwar.discover.conquered", "Conquered land – your banner flies here", "Erobertes Land – hier weht dein Banner");
        text.add("realm.kingdomsatwar.discover.hint",
                "%s: The castle guards attack anyone who sets foot inside their walls. Defeat the ruler (%s) and seize the royal standard behind the throne (right-click it) to conquer the castle.",
                "%s: Die Burgwachen greifen jeden an, der ihre Mauern betritt. Besiege den Herrscher (%s) und ergreife das Reichsbanner hinter dem Thron (Rechtsklick darauf), um die Burg zu erobern.");
        // Heerhaufen gegen die eigene Siedlung
        text.add("raid.kingdomsatwar.title", "War band!", "Heerhaufen!");
        text.add("raid.kingdomsatwar.subtitle", "%s marches with %s warriors", "%s zieht mit %s Kriegern heran");
        text.add("raid.kingdomsatwar.start", "A war band of %s marches on %s – %s warriors under a captain!",
                "Ein Heerhaufen von %s zieht gegen %s – %s Krieger unter einem Hauptmann!");
        text.add("raid.kingdomsatwar.hint", "Defend your town banner: if nobody guards it, they plunder your storehouse.",
                "Verteidige dein Stadtbanner: Bewacht es niemand mehr, plündern sie dein Lager.");
        text.add("raid.kingdomsatwar.bar", "War band of %s (%s of %s)", "Heerhaufen von %s (%s von %s)");
        text.add("raid.kingdomsatwar.bar.retreat", "War band of %s retreats (%s of %s)", "Heerhaufen von %s zieht ab (%s von %s)");
        text.add("raid.kingdomsatwar.victory", "The war band of %s is beaten! Its war chest (%s silver coins and more) lies in your storehouse.",
                "Der Heerhaufen von %s ist geschlagen! Seine Kriegskasse (%s Silbermünzen und mehr) liegt in deinem Lager.");
        text.add("raid.kingdomsatwar.plunder", "The war band of %s plunders the storehouse of %s (%s items)!",
                "Der Heerhaufen von %s plündert das Lager von %s (%s Gegenstände)!");
        text.add("raid.kingdomsatwar.retreat", "The war band of %s retreats.", "Der Heerhaufen von %s zieht ab.");
        text.add("raid.kingdomsatwar.gone", "The war band of %s has left your lands.", "Der Heerhaufen von %s hat dein Land verlassen.");
        text.add("command.kingdomsatwar.raid.stopped", "All war bands have been called off", "Alle Heerhaufen abgezogen");
        text.add("command.kingdomsatwar.raid.no_town", "You have no town with a banner in the overworld", "Du hast keine Siedlung mit Banner in der Oberwelt");
        text.add("command.kingdomsatwar.raid.running", "A war band is already marching on your town", "Gegen deine Siedlung zieht schon ein Heerhaufen");
        text.add("command.kingdomsatwar.raid.conquered", "That realm is conquered", "Dieses Reich ist erobert");
        text.add("command.kingdomsatwar.raid.no_room", "No room for a war band to gather near your town", "Kein Platz für einen Heerhaufen bei deiner Siedlung");
        text.add("kingdomsatwar.configuration.soldierVoices", "Troops speak", "Truppen sprechen");
        text.add("kingdomsatwar.configuration.soldierVoices.tooltip",
                "Your troops speak when recruited, commanded, attacking and victorious. Off since 0.79: they salute and fight in silence.",
                "Deine Truppen sprechen beim Anwerben, bei Befehlen, im Angriff und nach dem Sieg. Seit 0.79 aus: Sie salutieren und kämpfen stumm.");
        text.add("kingdomsatwar.configuration.realmRaids", "War bands of the realms", "Heerhaufen der Reiche");
        text.add("kingdomsatwar.configuration.realmRaids.tooltip",
                "Realms you are at war with send war bands against your town (only while you are there).",
                "Reiche, mit denen du im Krieg liegst, schicken Heerhaufen gegen deine Siedlung (nur, wenn du dort bist).");
        text.add("kingdomsatwar.configuration.raidIntervalDays", "Days between war bands", "Tage zwischen zwei Heerhaufen");
        text.add("kingdomsatwar.configuration.raidIntervalDays.tooltip", "At least this many in-game days pass between two war bands of the same realm.",
                "So viele Spieltage vergehen mindestens zwischen zwei Heerhaufen desselben Reichs.");
        text.add("realm.kingdomsatwar.alarm", "Alarm! The guards of %s have spotted you!", "Alarm! Die Wachen von %s haben dich entdeckt!");
        text.add("realm.kingdomsatwar.lord_fallen", "%s has fallen – %s is without a ruler!", "%s ist gefallen – %s ist ohne Herrscher!");
        text.add("realm.kingdomsatwar.lord_fallen.hint",
                "Seize the shimmering royal standard behind the throne (right-click it) to conquer the castle – once no guard is left in the throne hall.",
                "Ergreife das schimmernde Reichsbanner hinter dem Thron (Rechtsklick darauf), um die Burg zu erobern – sobald keine Wache mehr im Thronsaal steht.");
        // Eroberung
        text.add("realm.kingdomsatwar.conquer.lord_alive", "%s still lives – defeat the ruler first (he glows now)!",
                "%s lebt noch – besiege zuerst den Herrscher (er leuchtet jetzt auf)!");
        text.add("realm.kingdomsatwar.conquer.defended", "The guards of %s still defend the standard (%s left – they glow now)!",
                "Die Wachen von %s verteidigen noch das Reichsbanner (noch %s – sie leuchten jetzt auf)!");
        text.add("realm.kingdomsatwar.conquer.yours", "This castle already belongs to you.", "Diese Burg gehört bereits dir.");
        text.add("realm.kingdomsatwar.conquer.taken", "The castle of %s has already been conquered.", "Die Stammburg von %s ist bereits erobert.");
        text.add("realm.kingdomsatwar.conquer.title", "Castle conquered!", "Burg erobert!");
        text.add("realm.kingdomsatwar.conquer.subtitle", "%s bows to your banner", "%s beugt sich deinem Banner");
        text.add("realm.kingdomsatwar.conquer.announce", "%s has conquered the castle of %s!", "%s hat die Stammburg von %s erobert!");
        text.add("realm.kingdomsatwar.conquer.trophy", "You take the royal standard of %s as a trophy – your own banner now hangs behind the throne.",
                "Du nimmst das Reichsbanner von %s als Trophäe an dich – hinter dem Thron hängt jetzt dein eigenes Banner.");
        text.add("realm.kingdomsatwar.conquer.tribute",
                "From now on, the realm pays tribute every day into the treasury chest in the ruler's chamber.",
                "Von nun an zahlt das Reich jeden Tag Tribut in die Schatztruhe in der Kammer des Herrschers.");
        // Karten
        text.add("item.kingdomsatwar.map.realm", "Map to a Royal Castle", "Karte zu einer Stammburg");
        text.add("item.kingdomsatwar.map.realm_castle", "Map to the Castle of %s", "Karte zur Stammburg von %s");
        // Befehl
        text.add("command.kingdomsatwar.realms.header", "The four realms:", "Die vier Reiche:");
        text.add("command.kingdomsatwar.realms.entry", "%s at %s – %s (%s fallen warriors)", "%s bei %s – %s (%s gefallene Krieger)");
        text.add("command.kingdomsatwar.realms.ruled", "ruled by %s", "es herrscht %s");
        text.add("command.kingdomsatwar.realms.leaderless", "ruler fallen", "Herrscher gefallen");
        text.add("command.kingdomsatwar.realms.conquered", "conquered", "erobert");
        text.add("command.kingdomsatwar.realms.reset", "All realms have been reset.", "Alle Reiche wurden zurückgesetzt.");
        text.add("command.kingdomsatwar.realms.check.found", "%s: castle at %s %s %s (facing %s)", "%s: Burg bei %s %s %s (Drehung %s)");
        text.add("command.kingdomsatwar.realms.check.missing", "%s: no castle in the chunk at %s %s", "%s: keine Burg im Chunk bei %s %s");
        text.add("command.kingdomsatwar.realms.check.none", "%s: no place for a castle found", "%s: kein Platz für eine Burg gefunden");
    }

    private static void realm(Text text, Realm realm, String nameEn, String nameDe, String shortName, String rulerEn, String rulerDe) {
        String key = "realm.kingdomsatwar." + realm.id();
        text.add(key, nameEn, nameDe);
        text.add(key + ".short", shortName, shortName);
        text.add(key + ".ruler", rulerEn, rulerDe);
    }

    private static void emblem(Text text, String id, String nounEn, String nounDe, boolean masculine) {
        for (DyeColor color : DyeColor.values()) {
            text.add("block.kingdomsatwar.banner." + id + "." + color.getName(), colorEn(color) + " " + nounEn,
                    colorDe(color) + (masculine ? "er " : "e ") + nounDe);
        }
    }

    private static String colorEn(DyeColor color) {
        return switch (color) {
            case WHITE -> "White";
            case ORANGE -> "Orange";
            case MAGENTA -> "Magenta";
            case LIGHT_BLUE -> "Light Blue";
            case YELLOW -> "Yellow";
            case LIME -> "Lime";
            case PINK -> "Pink";
            case GRAY -> "Gray";
            case LIGHT_GRAY -> "Light Gray";
            case CYAN -> "Cyan";
            case PURPLE -> "Purple";
            case BLUE -> "Blue";
            case BROWN -> "Brown";
            case GREEN -> "Green";
            case RED -> "Red";
            case BLACK -> "Black";
        };
    }

    /** Wortstamm der Farbe für die gebeugte Form ("Weiß" + "er Turm"). */
    private static String colorDe(DyeColor color) {
        return switch (color) {
            case WHITE -> "Weiß";
            case ORANGE -> "Orangefarben";
            case MAGENTA -> "Magentafarben";
            case LIGHT_BLUE -> "Hellblau";
            case YELLOW -> "Gelb";
            case LIME -> "Hellgrün";
            case PINK -> "Rosafarben";
            case GRAY -> "Grau";
            case LIGHT_GRAY -> "Hellgrau";
            case CYAN -> "Türkisfarben";
            case PURPLE -> "Violett";
            case BLUE -> "Blau";
            case BROWN -> "Braun";
            case GREEN -> "Grün";
            case RED -> "Rot";
            case BLACK -> "Schwarz";
        };
    }
}
