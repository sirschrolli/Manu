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
                "%s: The castle guards attack anyone who sets foot inside their walls. Defeat the ruler (%s) and seize the royal standard behind the throne to conquer the castle.",
                "%s: Die Burgwachen greifen jeden an, der ihre Mauern betritt. Besiege den Herrscher (%s) und ergreife das Reichsbanner hinter dem Thron, um die Burg zu erobern.");
        text.add("realm.kingdomsatwar.alarm", "Alarm! The guards of %s have spotted you!", "Alarm! Die Wachen von %s haben dich entdeckt!");
        text.add("realm.kingdomsatwar.lord_fallen", "%s has fallen – %s is without a ruler!", "%s ist gefallen – %s ist ohne Herrscher!");
        text.add("realm.kingdomsatwar.lord_fallen.hint", "Seize the royal standard behind the throne to conquer the castle.",
                "Ergreife das Reichsbanner hinter dem Thron, um die Burg zu erobern.");
        // Eroberung
        text.add("realm.kingdomsatwar.conquer.lord_alive", "%s still lives – defeat the ruler first!", "%s lebt noch – besiege zuerst den Herrscher!");
        text.add("realm.kingdomsatwar.conquer.defended", "The guards of %s still defend the standard (%s left)!",
                "Die Wachen von %s verteidigen noch das Reichsbanner (noch %s)!");
        text.add("realm.kingdomsatwar.conquer.yours", "This castle already belongs to you.", "Diese Burg gehört bereits dir.");
        text.add("realm.kingdomsatwar.conquer.taken", "The castle of %s has already been conquered.", "Die Stammburg von %s ist bereits erobert.");
        text.add("realm.kingdomsatwar.conquer.title", "Castle conquered!", "Burg erobert!");
        text.add("realm.kingdomsatwar.conquer.subtitle", "%s bows to your banner", "%s beugt sich deinem Banner");
        text.add("realm.kingdomsatwar.conquer.announce", "%s has conquered the castle of %s!", "%s hat die Stammburg von %s erobert!");
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
