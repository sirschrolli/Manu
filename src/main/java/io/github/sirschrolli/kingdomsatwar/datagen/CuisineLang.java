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

        // die Feldfrüchte der Welten (2.0)
        text.add("block.kingdomsatwar.rice", "Rice", "Reis");
        item(text, "rice", "Rice", "Reis");
        lore(text, "rice", "Plant in shallow water over dirt or mud", "In seichtes Wasser über Erde oder Schlamm pflanzen");
        item(text, "rice_pudding", "Rice Pudding", "Milchreis");
        item(text, "sushi", "Sushi", "Sushi");
        text.add("block.kingdomsatwar.sea_grape_plant", "Sea Grapes", "Meertrauben");
        item(text, "sea_grapes", "Sea Grapes", "Meertrauben");
        lore(text, "sea_grapes", "Salty and crunchy. Plant underwater on sand", "Salzig und knackig. Unter Wasser auf Sand pflanzen");
        item(text, "sea_grape_salad", "Sea Grape Salad", "Meertraubensalat");
        lore(text, "sea_grape_salad", "Breathe underwater for a minute", "Eine Minute lang Kiemen");
        text.add("block.kingdomsatwar.float_berry_bush", "Float Berry Bush", "Schwebebeerenstrauch");
        item(text, "float_berries", "Float Berries", "Schwebebeeren");
        lore(text, "float_berries", "Light as air. Plant on end stone", "Leicht wie Luft. Auf Endstein pflanzen");
        item(text, "float_berry_tart", "Float Berry Tart", "Schwebebeerentörtchen");
        lore(text, "float_berry_tart", "Jump higher and land softly", "Höher springen, sanft landen");
        text.add("block.kingdomsatwar.shadow_tubers", "Shadow Tubers", "Schattenknollen");
        item(text, "shadow_tuber", "Shadow Tuber", "Schattenknolle");
        lore(text, "shadow_tuber", "Grows even in darkness. Plant on farmland", "Wächst auch im Dunkeln. Auf Ackerland pflanzen");
        item(text, "roasted_shadow_tuber", "Roasted Shadow Tuber", "Geröstete Schattenknolle");
        item(text, "shadow_tuber_stew", "Tuber Stew", "Knolleneintopf");
        lore(text, "shadow_tuber_stew", "Makes you tough as the Umbrer", "Macht zäh wie die Umbrer");
        text.add("block.kingdomsatwar.starfruit_bush", "Starfruit Bush", "Sternfruchtstrauch");
        item(text, "starfruit", "Starfruit", "Sternfrucht");
        lore(text, "starfruit", "Glows from within. Plant on void moss or stardust", "Leuchtet von innen. Auf Leerenmoos oder Sternenstaub pflanzen");
        item(text, "starfruit_juice", "Starfruit Juice", "Sternfruchtsaft");
        lore(text, "starfruit_juice", "See in the dark – and be swift", "Sehen im Dunkeln – und flink sein");
        text.add("block.kingdomsatwar.chaos_bean_bush", "Chaos Bean Bush", "Chaosbohnenstrauch");
        item(text, "chaos_beans", "Chaos Beans", "Chaosbohnen");
        lore(text, "chaos_beans", "Every bean works differently – not always well", "Jede Bohne wirkt anders – nicht immer gut");
        item(text, "chaos_bean_stew", "Chaos Bean Stew", "Chaosbohneneintopf");
        lore(text, "chaos_bean_stew", "Different every time – but always good", "Jedes Mal anders – aber immer gut");
        text.add("block.kingdomsatwar.fae_berry_bush", "Fae Berry Bush", "Feenbeerenstrauch");
        item(text, "fae_berries", "Fae Berries", "Feenbeeren");
        lore(text, "fae_berries", "Sweet and healing. Plant on fae grass", "Süß und heilsam. Auf Feengras pflanzen");
        item(text, "fae_pancakes", "Fae Pancakes", "Feenpfannkuchen");
        lore(text, "fae_pancakes", "Heal for a while", "Heilen eine Weile");
    }
}
