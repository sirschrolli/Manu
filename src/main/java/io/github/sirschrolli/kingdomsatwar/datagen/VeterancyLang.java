package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte zur Kampferfahrung der Truppen: Stufen, die neuen Ränge Meister und Legende, Aufstieg und Anzeige. */
final class VeterancyLang {
    @FunctionalInterface
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private VeterancyLang() {}

    static void register(Text text) {
        text.add("soldier.kingdomsatwar.level", "Level %s", "Stufe %s");
        text.add("gui.kingdomsatwar.soldier.experience", "Combat experience: %s / %s", "Kampferfahrung: %s / %s");
        text.add("gui.kingdomsatwar.soldier.experience.max", "Combat experience: %s – highest level", "Kampferfahrung: %s – höchste Stufe");
        text.add("gui.kingdomsatwar.soldier.experience.hint",
                "Soldiers learn in battle: every hit and every victory gives combat experience, strong foes and bosses a lot. "
                        + "Each level brings more health, damage and armour.",
                "Soldaten lernen im Kampf: Jeder Treffer und jeder Sieg gibt Kampferfahrung, starke Gegner und Bosse besonders viel. "
                        + "Jede Stufe bringt mehr Leben, Schaden und Rüstung.");

        // Aufstieg
        text.add("message.kingdomsatwar.soldier.level_up", "%s has reached level %s!", "%s hat Stufe %s erreicht!");
        text.add("message.kingdomsatwar.soldier.rank_ready", "%s has gained enough combat experience – the next rank awaits at your commander's table: %s",
                "%s hat genug Kampferfahrung gesammelt – am Feldherrentisch wartet der nächste Rang: %s");
        text.add("message.kingdomsatwar.soldier.second_wind", "%s rallies with a second wind!", "%s rafft sich mit letzter Kraft wieder auf!");

        // Feldherrentisch
        text.add("message.kingdomsatwar.recruit.needs_level", "This soldier needs more combat experience for the next rank.",
                "Für den nächsten Rang fehlt diesem Soldaten noch Kampferfahrung.");
        text.add("gui.kingdomsatwar.recruitment.result.needs_level", "More experience needed", "Mehr Erfahrung nötig");
        text.add("gui.kingdomsatwar.recruitment.rank_up.needs", "Promotion to %1$s from level %2$s – %3$s combat experience to go",
                "Befördern zum Rang %1$s ab Stufe %2$s – noch %3$s Kampferfahrung");
        text.add("gui.kingdomsatwar.recruitment.rank_requirement", "From level %s:", "Ab Stufe %s:");
        text.add("gui.kingdomsatwar.recruitment.rank_up.perk.3", "Master: recovers twice as fast outside battle",
                "Meister: erholt sich außerhalb des Kampfes doppelt so schnell");
        text.add("gui.kingdomsatwar.recruitment.rank_up.perk.4", "Legend: a second wind once per battle, comrades nearby learn faster",
                "Legende: einmal im Kampf ein zweiter Atem, Kameraden in der Nähe lernen schneller");
    }
}
