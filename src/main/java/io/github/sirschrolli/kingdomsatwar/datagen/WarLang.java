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
            case ORANGE -> "Orange";
            case MAGENTA -> "Magentafarben";
            case LIGHT_BLUE -> "Hellblau";
            case YELLOW -> "Gelb";
            case LIME -> "Hellgrün";
            case PINK -> "Rosafarben";
            case GRAY -> "Grau";
            case LIGHT_GRAY -> "Hellgrau";
            case CYAN -> "Türkis";
            case PURPLE -> "Violett";
            case BLUE -> "Blau";
            case BROWN -> "Braun";
            case GREEN -> "Grün";
            case RED -> "Rot";
            case BLACK -> "Schwarz";
        };
    }
}
