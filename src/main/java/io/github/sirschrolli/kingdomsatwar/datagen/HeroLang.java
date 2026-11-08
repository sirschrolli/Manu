package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/** Texte zum eigenen Helden: Herz- und Manakristalle, Heldenstufe, Talente und Charakterfenster. */
final class HeroLang {
    @FunctionalInterface
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private HeroLang() {}

    static void register(Text text) {
        // --- Herz- und Manakristalle ---
        text.add("item.kingdomsatwar.heart_crystal", "Heart Crystal", "Herzkristall");
        text.add(ModItems.loreKey("heart_crystal", 1), "Right-click: one more heart, for good", "Rechtsklick: ein Herz mehr, für immer");
        text.add(ModItems.loreKey("heart_crystal", 2), "Grows rarely in caves – at most 10", "Wächst selten in Höhlen – höchstens 10");
        text.add("item.kingdomsatwar.mana_crystal", "Mana Crystal", "Manakristall");
        text.add(ModItems.loreKey("mana_crystal", 1), "Right-click: 20 more mana, for good", "Rechtsklick: 20 Mana mehr, für immer");
        text.add(ModItems.loreKey("mana_crystal", 2), "Grows rarely deep in caves – at most 10", "Wächst selten tief in Höhlen – höchstens 10");
        text.add("block.kingdomsatwar.heart_crystal_formation", "Heart Crystal", "Herzkristall");
        text.add("block.kingdomsatwar.mana_crystal_formation", "Mana Crystal", "Manakristall");
        text.add("message.kingdomsatwar.crystal.heart.absorbed", "❤ One more heart! Now %1$s hearts (%2$s/%3$s heart crystals)",
                "❤ Ein Herz mehr! Jetzt %1$s Herzen (%2$s/%3$s Herzkristalle)");
        text.add("message.kingdomsatwar.crystal.mana.absorbed", "◆ More mana! Now up to %1$s mana (%2$s/%3$s mana crystals)",
                "◆ Mehr Mana! Jetzt bis zu %1$s Mana (%2$s/%3$s Manakristalle)");
        text.add("message.kingdomsatwar.crystal.heart.full", "You have already absorbed all %s heart crystals",
                "Du hast schon alle %s Herzkristalle aufgenommen");
        text.add("message.kingdomsatwar.crystal.mana.full", "You have already absorbed all %s mana crystals",
                "Du hast schon alle %s Manakristalle aufgenommen");
        text.add("tooltip.kingdomsatwar.crystal.count", "Absorbed: %s/%s", "Aufgenommen: %s/%s");

        // --- Heldenstufe und Talente ---
        text.add("key.category.kingdomsatwar.hero", "Kingdoms at War: Hero", "Kingdoms at War: Held");
        text.add("key.kingdomsatwar.character", "Character", "Charakter");
        text.add("gui.kingdomsatwar.character.title", "Character", "Charakter");
        text.add("gui.kingdomsatwar.character.level", "Hero Level %s", "Heldenstufe %s");
        text.add("gui.kingdomsatwar.character.experience", "%s / %s hero experience", "%s / %s Heldenerfahrung");
        text.add("gui.kingdomsatwar.character.experience.max", "%s hero experience – highest level", "%s Heldenerfahrung – höchste Stufe");
        text.add("gui.kingdomsatwar.character.sources.title", "Hero experience", "Heldenerfahrung");
        text.add("gui.kingdomsatwar.character.sources",
                "You gain it for defeated monsters and bosses (a quarter also for those your soldiers and dragons defeat), for ore you "
                        + "mine, for advancements, for every biome you set foot in for the first time and for every castle you conquer. "
                        + "Every level brings a talent point. Minecraft's own experience stays exactly as it is.",
                "Es gibt sie für besiegte Monster und Bosse (ein Viertel auch für die, die deine Soldaten und Drachen besiegen), für Erz, "
                        + "das du abbaust, für Fortschritte, für jedes Biom, das du zum ersten Mal betrittst, und für jede eroberte Burg. "
                        + "Jede Stufe bringt einen Talentpunkt. Die Erfahrung von Minecraft bleibt genau, wie sie ist.");
        text.add("gui.kingdomsatwar.character.points", "Talent points: %s", "Talentpunkte: %s");
        text.add("gui.kingdomsatwar.character.points.none", "All talent points spent", "Alle Talentpunkte verteilt");
        text.add("gui.kingdomsatwar.character.talent.spend", "Click: spend a point", "Klick: einen Punkt verteilen");
        text.add("gui.kingdomsatwar.character.talent.no_points", "No talent point left – the next hero level brings one",
                "Kein Talentpunkt frei – die nächste Heldenstufe bringt einen");
        text.add("gui.kingdomsatwar.character.talent.full", "This talent is mastered (10 points)", "Dieses Talent ist gemeistert (10 Punkte)");
        text.add("gui.kingdomsatwar.character.reset", "Redistribute", "Neu verteilen");
        text.add("gui.kingdomsatwar.character.reset.hint", "Take back all points – costs %s", "Alle Punkte zurücknehmen – kostet %s");
        text.add("gui.kingdomsatwar.character.reset.none", "No points spent yet", "Noch keine Punkte verteilt");
        text.add("gui.kingdomsatwar.character.done", "Done", "Fertig");
        text.add("gui.kingdomsatwar.character.stats", "Your values", "Deine Werte");
        text.add("gui.kingdomsatwar.character.stats.hint", "Hearts, mana, armour and damage – with crystals, talents and what you wear and hold.",
                "Herzen, Mana, Rüstung und Schaden – mit Kristallen, Talenten und dem, was du trägst und hältst.");
        text.add("gui.kingdomsatwar.character.hearts", "Heart crystals %s/%s", "Herzkristalle %s/%s");
        text.add("gui.kingdomsatwar.character.mana", "Mana crystals %s/%s", "Manakristalle %s/%s");
        text.add("gui.kingdomsatwar.character.biomes", "Biomes found: %s", "Entdeckte Biome: %s");
        text.add("hero.kingdomsatwar.level_up.title", "Hero Level %s", "Heldenstufe %s");
        text.add("hero.kingdomsatwar.level_up.subtitle", "A new talent point – press %s", "Ein neuer Talentpunkt – drücke %s");
        text.add("hero.kingdomsatwar.discovered", "New biome discovered: %s", "Neues Biom entdeckt: %s");
        text.add("hud.kingdomsatwar.hero.gained", "+%s hero experience", "+%s Heldenerfahrung");
        talent(text, "strength", "Strength", "Stärke", "+%s damage", "+%s Schaden",
                "+0.5 damage per point with every blow", "+0,5 Schaden je Punkt bei jedem Schlag");
        talent(text, "vitality", "Vitality", "Lebenskraft", "+%s health", "+%s Leben",
                "+1 health per point (ten points: five hearts)", "+1 Leben je Punkt (zehn Punkte: fünf Herzen)");
        talent(text, "agility", "Agility", "Gewandtheit", "+%s %% speed", "+%s %% Tempo",
                "+2 % walking speed per point", "+2 % Lauftempo je Punkt");
        talent(text, "toughness", "Toughness", "Zähigkeit", "+%s armour", "+%s Rüstung",
                "+0.5 armour per point", "+0,5 Rüstung je Punkt");
        talent(text, "arcana", "Arcana", "Zauberkunst", "+%s mana", "+%s Mana",
                "+10 mana and 5 % faster mana recovery per point", "+10 Mana und 5 % schnellere Erholung des Manas je Punkt");
        talent(text, "fortune", "Fortune", "Glück", "+%s luck", "+%s Glück",
                "+0.25 luck per point – better loot in chests", "+0,25 Glück je Punkt – bessere Beute in Truhen");
    }

    private static void talent(Text text, String id, String english, String deutsch, String effectEn, String effectDe, String descEn,
            String descDe) {
        String key = "hero.kingdomsatwar.talent." + id;
        text.add(key, english, deutsch);
        text.add(key + ".effect", effectEn, effectDe);
        text.add(key + ".desc", descEn, descDe);
    }
}
