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
        text.add("key.kingdomsatwar.pat", "Pat (Legend reward)", "Streicheln (Belohnung der Legende)");
        text.add("gui.kingdomsatwar.character.title", "Character", "Charakter");
        text.add("gui.kingdomsatwar.character.level", "Hero Level %s", "Heldenstufe %s");
        text.add("gui.kingdomsatwar.character.experience", "%s / %s", "%s / %s");
        text.add("gui.kingdomsatwar.character.experience.max", "%s – highest level", "%s – höchste Stufe");
        text.add("gui.kingdomsatwar.character.next", "%s more for hero level %s", "Noch %s bis Heldenstufe %s");
        text.add("gui.kingdomsatwar.character.spent", "%s of %s spent", "%s von %s verteilt");
        text.add("gui.kingdomsatwar.character.rank.from", "From hero level %s (%s hero experience)", "Ab Heldenstufe %s (%s Heldenerfahrung)");
        text.add("gui.kingdomsatwar.character.rank.bonus", "+%s talent points on reaching it", "+%s Talentpunkte, wenn du ihn erreichst");
        text.add("gui.kingdomsatwar.character.rank.reward", "Reward: you may pat any creature (%s)", "Belohnung: Du darfst jedes Wesen streicheln (%s)");
        text.add("gui.kingdomsatwar.character.rank.reached", "Reached", "Erreicht");
        text.add("gui.kingdomsatwar.character.rank.ahead", "Still ahead of you", "Liegt noch vor dir");
        text.add("gui.kingdomsatwar.character.talent.max", "At %s points: %s", "Mit %s Punkten: %s");
        text.add("gui.kingdomsatwar.character.sources.title", "Hero experience", "Heldenerfahrung");
        text.add("gui.kingdomsatwar.character.sources",
                "You gain it for defeated monsters and bosses (a quarter also for those your soldiers and dragons defeat), for ore you "
                        + "mine, for advancements, for every biome you set foot in for the first time and for every castle you conquer. "
                        + "Every level brings a talent point, every new rank two more. Hero level 100 – the Legend – takes 392,040 hero "
                        + "experience. Minecraft's own experience stays exactly as it is.",
                "Es gibt sie für besiegte Monster und Bosse (ein Viertel auch für die, die deine Soldaten und Drachen besiegen), für Erz, "
                        + "das du abbaust, für Fortschritte, für jedes Biom, das du zum ersten Mal betrittst, und für jede eroberte Burg. "
                        + "Jede Stufe bringt einen Talentpunkt, jeder neue Rang zwei dazu. Heldenstufe 100 – die Legende – braucht 392.040 "
                        + "Heldenerfahrung. Die Erfahrung von Minecraft bleibt genau, wie sie ist.");
        text.add("gui.kingdomsatwar.character.points", "Talent points: %s", "Talentpunkte: %s");
        text.add("gui.kingdomsatwar.character.points.none", "All talent points spent", "Alle Talentpunkte verteilt");
        text.add("gui.kingdomsatwar.character.talent.spend", "Click: spend a point", "Klick: einen Punkt verteilen");
        text.add("gui.kingdomsatwar.character.talent.no_points", "No talent point left – the next hero level brings one",
                "Kein Talentpunkt frei – die nächste Heldenstufe bringt einen");
        text.add("gui.kingdomsatwar.character.talent.full", "This talent is mastered (%s points)", "Dieses Talent ist gemeistert (%s Punkte)");
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
        text.add("hero.kingdomsatwar.level_up.rank", "New rank: %s", "Neuer Rang: %s");
        text.add("hero.kingdomsatwar.level_up.point", "+%s talent point – press %s", "+%s Talentpunkt – drücke %s");
        text.add("hero.kingdomsatwar.level_up.points", "+%s talent points – press %s", "+%s Talentpunkte – drücke %s");
        text.add("hero.kingdomsatwar.level_up.reward", "Reward unlocked: patting – press %s", "Belohnung freigeschaltet: Streicheln – drücke %s");
        text.add("hero.kingdomsatwar.rank_up", "New rank: %s – %s extra talent points!", "Neuer Rang: %s – %s Talentpunkte extra!");
        text.add("hero.kingdomsatwar.reward.pat",
                "You are a Legend! Look at any creature and press %s to pat it – your own soldiers and animals get half a heart back.",
                "Du bist eine Legende! Schau ein Wesen an und drücke %s, um es zu streicheln – deine eigenen Soldaten und Tiere bekommen "
                        + "dabei ein halbes Herz zurück.");
        text.add("hero.kingdomsatwar.pat.locked", "Patting is the reward of the Legend (hero level %s)",
                "Streicheln ist die Belohnung der Legende (Heldenstufe %s)");
        text.add("hero.kingdomsatwar.settled", "The hero levels have changed – you are now hero level %s and get all talent points back to "
                + "spend anew (free of charge).", "Die Heldenstufen haben sich geändert – du bist jetzt Heldenstufe %s und bekommst alle "
                + "Talentpunkte zurück, um sie neu zu verteilen (kostenlos).");
        text.add("hero.kingdomsatwar.rank.squire", "Squire", "Knappe");
        text.add("hero.kingdomsatwar.rank.warrior", "Warrior", "Recke");
        text.add("hero.kingdomsatwar.rank.knight", "Knight", "Ritter");
        text.add("hero.kingdomsatwar.rank.hero", "Hero", "Held");
        text.add("hero.kingdomsatwar.rank.warlord", "Warlord", "Kriegsherr");
        text.add("hero.kingdomsatwar.rank.legend", "Legend", "Legende");
        text.add("hero.kingdomsatwar.discovered", "New biome discovered: %s", "Neues Biom entdeckt: %s");
        text.add("hud.kingdomsatwar.hero.gained", "+%s hero experience", "+%s Heldenerfahrung");
        text.add("hud.kingdomsatwar.hero.plus", "+%s", "+%s");
        talent(text, "strength", "Strength", "Stärke", "+%s damage", "+%s Schaden",
                "+0.3 damage per point with every blow", "+0,3 Schaden je Punkt bei jedem Schlag");
        talent(text, "vitality", "Vitality", "Lebenskraft", "+%s health", "+%s Leben",
                "+1 health per point (twenty points: ten hearts)", "+1 Leben je Punkt (zwanzig Punkte: zehn Herzen)");
        talent(text, "agility", "Agility", "Gewandtheit", "+%s %% speed", "+%s %% Tempo",
                "+1.25 % walking speed per point", "+1,25 % Lauftempo je Punkt");
        talent(text, "toughness", "Toughness", "Zähigkeit", "+%s armour", "+%s Rüstung",
                "+0.4 armour per point", "+0,4 Rüstung je Punkt");
        talent(text, "arcana", "Arcana", "Zauberkunst", "+%s mana", "+%s Mana",
                "+8 mana and 4 % faster mana recovery per point", "+8 Mana und 4 % schnellere Erholung des Manas je Punkt");
        talent(text, "fortune", "Fortune", "Glück", "+%s luck", "+%s Glück",
                "+0.2 luck per point – better loot in chests", "+0,2 Glück je Punkt – bessere Beute in Truhen");
    }

    private static void talent(Text text, String id, String english, String deutsch, String effectEn, String effectDe, String descEn,
            String descDe) {
        String key = "hero.kingdomsatwar.talent." + id;
        text.add(key, english, deutsch);
        text.add(key + ".effect", effectEn, effectDe);
        text.add(key + ".desc", descEn, descDe);
    }
}
