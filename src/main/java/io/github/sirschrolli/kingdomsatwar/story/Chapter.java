package io.github.sirschrolli.kingdomsatwar.story;

/**
 * Die Kapitel der Geschichte „Das Siegel“. Die Kapitel III bis VIII sind die sechs Drachen (in beliebiger
 * Reihenfolge): Kapitel III beginnt, wenn der Siegelkreis gefunden ist, mit jedem besiegten Drachen folgt das nächste.
 * In der Chronik stehen sie gemeinsam als „Die sechs Drachen“.
 */
public enum Chapter {
    PROLOG("prolog", "Prolog"),
    KINGDOM("kingdom", "I"),
    CHRONICLE("chronicle", "II"),
    DRAGON_1("dragon_1", "III"),
    DRAGON_2("dragon_2", "IV"),
    DRAGON_3("dragon_3", "V"),
    DRAGON_4("dragon_4", "VI"),
    DRAGON_5("dragon_5", "VII"),
    DRAGON_6("dragon_6", "VIII"),
    SEAL("seal", "IX"),
    CROWN("crown", "Epilog");

    private final String id;
    private final String numeral;

    Chapter(String id, String numeral) {
        this.id = id;
        this.numeral = numeral;
    }

    public String id() {
        return this.id;
    }

    /** Römische Ziffer (oder „Prolog“/„Epilog“). */
    public String numeral() {
        return this.numeral;
    }

    public boolean isDragon() {
        return this.ordinal() >= DRAGON_1.ordinal() && this.ordinal() <= DRAGON_6.ordinal();
    }

    /** Für die Drachen-Kapitel: wie viele Drachen beim Beginn des Kapitels schon besiegt sind (0..5). */
    public int dragonIndex() {
        return this.ordinal() - DRAGON_1.ordinal();
    }

    public static Chapter dragon(int defeated) {
        return values()[DRAGON_1.ordinal() + Math.max(0, Math.min(5, defeated))];
    }

    public static Chapter byIndex(int index) {
        Chapter[] all = values();
        return all[Math.max(0, Math.min(all.length - 1, index))];
    }

    public String titleKey() {
        return "chapter.kingdomsatwar." + this.id;
    }
}
