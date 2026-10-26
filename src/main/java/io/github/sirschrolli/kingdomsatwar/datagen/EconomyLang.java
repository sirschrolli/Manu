package io.github.sirschrolli.kingdomsatwar.datagen;

/**
 * Übersetzungen der Wirtschaft (Münzen, Stadt, Bürger, Handel, Ansehen, Krönung) – Englisch und Deutsch.
 */
final class EconomyLang {
    /** Ein Eintrag: Schlüssel, Englisch, Deutsch. */
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private EconomyLang() {}

    static void register(Text t) {
        coins(t);
    }

    private static void coins(Text t) {
        t.add("item.kingdomsatwar.bronze_coin", "Bronze Coin", "Bronzemünze");
        t.add("item.kingdomsatwar.silver_coin", "Silver Coin", "Silbermünze");
        t.add("item.kingdomsatwar.gold_coin", "Gold Coin", "Goldmünze");
        t.add("coin.kingdomsatwar.bronze", "%s Bronze", "%s Bronze");
        t.add("coin.kingdomsatwar.silver", "%s Silver", "%s Silber");
        t.add("coin.kingdomsatwar.gold", "%s Gold", "%s Gold");
        t.add("item.kingdomsatwar.coin.value", "Worth %s", "Wert: %s");
        t.add("item.kingdomsatwar.coin.stack_value", "Stack worth %s", "Stapel: %s");
        t.add("item.kingdomsatwar.coin.combine", "Use: change 10 into one %s", "Rechtsklick: 10 in eine %s wechseln");
        t.add("item.kingdomsatwar.coin.split", "Sneak + use: change one into ten smaller coins",
                "Schleichen + Rechtsklick: eine in zehn kleinere wechseln");
        t.add("message.kingdomsatwar.coin.need_ten", "You need ten of them to exchange", "Zum Wechseln brauchst du zehn Stück");
        t.add("gui.kingdomsatwar.recruitment.pay", "Pay: %s", "Sold: %s");
        t.add("gui.kingdomsatwar.recruitment.purse", "Your purse: %s", "Deine Börse: %s");
    }
}
