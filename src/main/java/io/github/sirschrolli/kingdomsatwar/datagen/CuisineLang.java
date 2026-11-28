package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte der Küche der Welten (seit 1.16): Speisen, Getränke, der Käselaib und die Glutschote. */
final class CuisineLang {
    private CuisineLang() {}

    private static void item(HeroLang.Text text, String id, String english, String deutsch) {
        text.add("item.kingdomsatwar." + id, english, deutsch);
    }

    private static void lore(HeroLang.Text text, String id, String english, String deutsch) {
        text.add("lore.kingdomsatwar." + id + ".1", english, deutsch);
    }

    static void register(HeroLang.Text text) {
        item(text, "flour", "Flour", "Mehl");
        // Sennhütte
        item(text, "cheese", "Cheese", "Bergkäse");
        text.add("block.kingdomsatwar.cheese_wheel", "Cheese Wheel", "Käselaib");
        lore(text, "cheese_wheel", "Place it on the table – four slices", "Auf den Tisch stellen – vier Stücke");
        item(text, "cheese_spaetzle", "Cheese Spaetzle", "Käsespätzle");
        item(text, "pretzel", "Pretzel", "Brezel");
        item(text, "raw_sausage", "Raw Sausage", "Rohe Bratwurst");
        item(text, "sausage", "Grilled Sausage", "Bratwurst");
        // Schmugglerhöhle, Galeone
        item(text, "rum", "Rum", "Rum");
        lore(text, "rum", "Tougher for a while – too much makes your head spin", "Eine Weile zäher – zu viel dreht einem den Kopf");
        // Unterwelt
        text.add("block.kingdomsatwar.ember_peppers", "Ember Peppers", "Glutschoten");
        item(text, "ember_pepper", "Ember Pepper", "Glutschote");
        lore(text, "ember_pepper", "Burning hot: briefly fire resistant. Grows on soul soil", "Brennend scharf: kurz feuerfest. Wächst auf Seelenerde");
        item(text, "ember_chili", "Ember Chili", "Glutchili");
        lore(text, "ember_chili", "Lava can't hurt you for a minute", "Eine Minute lang macht einem Lava nichts aus");
        item(text, "fungus_skewer", "Roasted Fungus Skewer", "Gerösteter Pilzspieß");
        // End
        item(text, "hardtack", "Hardtack", "Schiffszwieback");
        item(text, "chorus_jam", "Chorus Jam", "Chorusmarmelade");
        lore(text, "chorus_jam", "Sweet and light: you drift down like a feather", "Süß und leicht: man sinkt danach wie eine Feder");
        // Schattenwelt
        item(text, "dusk_porridge", "Dusk Porridge", "Dämmerbrei");
        lore(text, "dusk_porridge", "The monks' breakfast: see in the dark", "Die Morgenspeise der Mönche: sehen im Dunkeln");
        item(text, "soul_berry_pie", "Soul Berry Pie", "Seelenbeerkuchen");
        lore(text, "soul_berry_pie", "Glowing berries: see in the dark", "Leuchtende Beeren: sehen im Dunkeln");
        // Leere
        item(text, "star_candy", "Star Candy", "Sternenkandis");
        lore(text, "star_candy", "Crackles on the tongue – swift as a star", "Knistert auf der Zunge – flink wie ein Stern");
        // Abgrund
        item(text, "upside_down_cake", "Upside-Down Cake", "Sturzkuchen");
        lore(text, "upside_down_cake", "Turned out of the tin – you fall a little slower", "Gestürzt gebacken – man fällt danach langsamer");
        item(text, "pilgrim_ration", "Pilgrim's Ration", "Pilgerration");
        // Urmeer
        item(text, "fish_soup", "Fish Soup", "Fischsuppe");
        lore(text, "fish_soup", "Warms you through – hold your breath longer", "Wärmt durch – man hält länger die Luft an");
        item(text, "fish_roll", "Fish Roll", "Fischbrötchen");
        // Feenreich
        item(text, "gingerbread", "Gingerbread", "Lebkuchen");
        item(text, "fae_tea", "Fae Tea", "Feentee");
        lore(text, "fae_tea", "Moonbell tea: refreshes and heals a little", "Mondglöckchentee: belebt und heilt ein wenig");
        item(text, "glimmer_cupcake", "Glimmer Cupcake", "Glimmerküchlein");
        lore(text, "glimmer_cupcake", "Baked with fae dust – jump as if on clouds", "Mit Feenstaub gebacken – springen wie auf Wolken");
    }
}
