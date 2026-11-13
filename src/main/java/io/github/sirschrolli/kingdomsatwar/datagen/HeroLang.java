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
        text.add(ModItems.loreKey("heart_crystal", 1), "Right-click: half a heart more, for good", "Rechtsklick: ein halbes Herz mehr, für immer");
        text.add(ModItems.loreKey("heart_crystal", 2), "Grows rarely in caves – at most 50", "Wächst selten in Höhlen – höchstens 50");
        text.add("item.kingdomsatwar.mana_crystal", "Mana Crystal", "Manakristall");
        text.add(ModItems.loreKey("mana_crystal", 1), "Right-click: 10 more mana, for good", "Rechtsklick: 10 Mana mehr, für immer");
        text.add(ModItems.loreKey("mana_crystal", 2), "Grows rarely deep in caves – at most 50", "Wächst selten tief in Höhlen – höchstens 50");
        text.add("block.kingdomsatwar.heart_crystal_formation", "Heart Crystal", "Herzkristall");
        text.add("block.kingdomsatwar.mana_crystal_formation", "Mana Crystal", "Manakristall");
        text.add("message.kingdomsatwar.crystal.heart.absorbed", "❤ Half a heart more! Now %1$s hearts (%2$s/%3$s heart crystals)",
                "❤ Ein halbes Herz mehr! Jetzt %1$s Herzen (%2$s/%3$s Herzkristalle)");
        text.add("message.kingdomsatwar.crystal.mana.absorbed", "◆ More mana! Now up to %1$s mana (%2$s/%3$s mana crystals)",
                "◆ Mehr Mana! Jetzt bis zu %1$s Mana (%2$s/%3$s Manakristalle)");
        text.add("message.kingdomsatwar.crystal.heart.full", "You have already absorbed all %s heart crystals",
                "Du hast schon alle %s Herzkristalle aufgenommen");
        text.add("message.kingdomsatwar.crystal.mana.full", "You have already absorbed all %s mana crystals",
                "Du hast schon alle %s Manakristalle aufgenommen");
        text.add("tooltip.kingdomsatwar.crystal.count", "Absorbed: %s/%s", "Aufgenommen: %s/%s");

        // --- Heldenstufe und Talente ---
        text.add("key.category.kingdomsatwar.hero", "Kingdoms at War: Hero", "Kingdoms at War: Held");
        text.add("key.kingdomsatwar.character", "Hero's path & character", "Heldenweg & Charakter");
        text.add("key.kingdomsatwar.pat", "Pat (Legend reward)", "Streicheln (Belohnung der Legende)");
        text.add("key.kingdomsatwar.chronicle", "Open the Chronicle of the Seal", "Chronik des Siegels aufschlagen");
        text.add("gui.kingdomsatwar.character.title", "Character", "Charakter");
        text.add("gui.kingdomsatwar.character.level", "Hero Level %s", "Heldenstufe %s");
        text.add("gui.kingdomsatwar.character.experience", "%s / %s", "%s / %s");
        text.add("gui.kingdomsatwar.character.experience.max", "%s – highest level", "%s – höchste Stufe");
        text.add("gui.kingdomsatwar.character.next", "%s more for hero level %s", "Noch %s bis Heldenstufe %s");
        text.add("gui.kingdomsatwar.character.spent", "%s of %s spent", "%s von %s verteilt");
        text.add("gui.kingdomsatwar.character.rank.from", "From hero level %s (%s hero experience)", "Ab Heldenstufe %s (%s Heldenerfahrung)");
        text.add("gui.kingdomsatwar.character.rank.bonus", "+%s talent points on reaching it", "+%s Talentpunkte, wenn du ihn erreichst");
        text.add("gui.kingdomsatwar.character.rank.reward", "Reward: pat any creature (Sneak + right-click or %s)",
                "Belohnung: jedes Wesen streicheln (Schleichen + Rechtsklick oder %s)");
        text.add("gui.kingdomsatwar.character.rank.reward.immortal", "Reward: once every %s minutes the light saves you from death",
                "Belohnung: Einmal alle %s Minuten bewahrt dich das Licht vor dem Tod");
        text.add("gui.kingdomsatwar.character.rank.reached", "Reached", "Erreicht");
        text.add("gui.kingdomsatwar.character.rank.ahead", "Still ahead of you", "Liegt noch vor dir");
        text.add("gui.kingdomsatwar.character.talent.max", "At %s points: %s", "Mit %s Punkten: %s");
        text.add("gui.kingdomsatwar.character.talent.mastery", "Mastery: points %s to %s glow golden", "Meisterschaft: die Punkte %s bis %s leuchten golden");
        text.add("gui.kingdomsatwar.character.might", "Hero's might", "Heldenkraft");
        text.add("gui.kingdomsatwar.character.might.hint", "Grows by itself along the hero's path up to level 500 – no talent points needed.",
                "Wächst auf dem Heldenweg bis Stufe 500 von selbst – ohne Talentpunkte.");
        text.add("gui.kingdomsatwar.character.might.health", "+%s hearts (one about every 25 levels)", "+%s Herzen (eins etwa alle 25 Stufen)");
        text.add("gui.kingdomsatwar.character.might.damage", "+%s damage", "+%s Schaden");
        text.add("gui.kingdomsatwar.character.might.mana", "+%s mana · recovers %s %% faster", "+%s Mana · erholt sich %s %% schneller");
        text.add("gui.kingdomsatwar.character.might.toughness", "+%s armour toughness (one about every 63 levels)",
                "+%s Rüstungshärte (eins etwa alle 63 Stufen)");
        text.add("gui.kingdomsatwar.character.might.next", "Next heart at hero level %s", "Nächstes Herz auf Heldenstufe %s");
        text.add("gui.kingdomsatwar.character.immortal.ready", "Light of immortality: ready", "Licht der Unsterblichkeit: bereit");
        text.add("gui.kingdomsatwar.character.immortal.rest", "Light of immortality: rests for %s more minutes",
                "Licht der Unsterblichkeit: ruht noch %s Minuten");
        text.add("gui.kingdomsatwar.character.sources.title", "Hero experience", "Heldenerfahrung");
        text.add("gui.kingdomsatwar.character.sources",
                "You gain it for defeated monsters and bosses (a quarter also for those your soldiers and dragons defeat), for ore you "
                        + "mine, for advancements, for every biome you set foot in for the first time and for every castle you conquer. "
                        + "About every two and a half levels bring a talent point, every new rank two more, and every level makes you a little "
                        + "stronger by itself (hero's might). Hero level 100 – the Legend – takes about 1.17 million hero experience, hero "
                        + "level 200 – the Immortal – about 4.3 million, the end of the path at level 500 about 20.9 million. Minecraft's own "
                        + "experience stays exactly as it is.",
                "Es gibt sie für besiegte Monster und Bosse (ein Viertel auch für die, die deine Soldaten und Drachen besiegen), für Erz, "
                        + "das du abbaust, für Fortschritte, für jedes Biom, das du zum ersten Mal betrittst, und für jede eroberte Burg. "
                        + "Etwa alle zweieinhalb Stufen gibt es einen Talentpunkt, jeder neue Rang bringt zwei dazu, und jede Stufe macht von "
                        + "selbst ein wenig stärker (Heldenkraft). Heldenstufe 100 – die Legende – braucht rund 1,17 Millionen Heldenerfahrung, "
                        + "Heldenstufe 200 – der Unsterbliche – rund 4,3 Millionen, das Ende des Weges auf Stufe 500 rund 20,9 Millionen. Die "
                        + "Erfahrung von Minecraft bleibt genau, wie sie ist.");
        text.add("gui.kingdomsatwar.character.points", "Talent points: %s", "Talentpunkte: %s");
        text.add("gui.kingdomsatwar.character.points.none", "All talent points spent", "Alle Talentpunkte verteilt");
        text.add("gui.kingdomsatwar.character.talent.spend", "Click: spend a point", "Klick: einen Punkt verteilen");
        text.add("gui.kingdomsatwar.character.talent.no_points", "No talent point left – the hero's path brings one about every two and a half "
                + "levels", "Kein Talentpunkt frei – der Heldenweg bringt etwa alle zweieinhalb Stufen einen");
        text.add("gui.kingdomsatwar.character.talent.full", "This talent is mastered (%s points)", "Dieses Talent ist gemeistert (%s Punkte)");
        text.add("gui.kingdomsatwar.character.reset", "Redistribute", "Neu verteilen");
        text.add("gui.kingdomsatwar.character.reset.hint", "Take back all points – costs %s", "Alle Punkte zurücknehmen – kostet %s");
        text.add("gui.kingdomsatwar.character.reset.none", "No points spent yet", "Noch keine Punkte verteilt");
        text.add("gui.kingdomsatwar.character.done", "Done", "Fertig");
        text.add("gui.kingdomsatwar.character.path", "Path", "Zum Weg");
        text.add("gui.kingdomsatwar.path.title", "The Hero's Path", "Der Heldenweg");
        text.add("gui.kingdomsatwar.path.talents", "Talents & values", "Talente & Werte");
        text.add("gui.kingdomsatwar.path.total", "%s %% of the path · %s / %s hero experience", "%s %% des Weges · %s / %s Heldenerfahrung");
        text.add("gui.kingdomsatwar.path.reward.points", "+%s talent points", "+%s Talentpunkte");
        text.add("gui.kingdomsatwar.path.reward.pat", "Reward: patting", "Belohnung: Streicheln");
        text.add("gui.kingdomsatwar.path.reward.immortal", "Reward: immortality", "Belohnung: Unsterblichkeit");
        text.add("gui.kingdomsatwar.path.hint", "Mouse wheel, drag or arrow keys: along the path · click here: jump",
                "Mausrad, Ziehen oder Pfeiltasten: den Weg entlang · hier klicken: dorthin springen");
        text.add("gui.kingdomsatwar.path.needs", "From %s hero experience", "Ab %s Heldenerfahrung");
        text.add("gui.kingdomsatwar.path.missing", "%s hero experience to go", "Noch %s Heldenerfahrung");
        text.add("gui.kingdomsatwar.path.point", "+1 talent point", "+1 Talentpunkt");
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
        text.add("hero.kingdomsatwar.level_up.reward", "Reward unlocked: patting – Sneak + right-click or %s",
                "Belohnung freigeschaltet: Streicheln – Schleichen + Rechtsklick oder %s");
        text.add("hero.kingdomsatwar.level_up.reward.immortal", "Reward unlocked: the light of immortality",
                "Belohnung freigeschaltet: das Licht der Unsterblichkeit");
        text.add("hero.kingdomsatwar.level_up.might", "Hero's might: %s", "Heldenkraft: %s");
        text.add("hero.kingdomsatwar.might.heart", "+%s heart", "+%s Herz");
        text.add("hero.kingdomsatwar.might.hearts", "+%s hearts", "+%s Herzen");
        text.add("hero.kingdomsatwar.might.damage", "+%s damage", "+%s Schaden");
        text.add("hero.kingdomsatwar.might.mana", "+%s mana", "+%s Mana");
        text.add("hero.kingdomsatwar.might.toughness", "+%s toughness", "+%s Härte");
        text.add("hero.kingdomsatwar.reward.immortal", "You are Immortal! Once every %s minutes the light saves you from death – "
                + "like a totem, when no totem helps any more.", "Du bist unsterblich! Einmal alle %s Minuten bewahrt dich das Licht vor dem "
                + "Tod – wie ein Totem, wenn kein Totem mehr hilft.");
        text.add("hero.kingdomsatwar.immortal.title", "The Light of Immortality", "Das Licht der Unsterblichkeit");
        text.add("hero.kingdomsatwar.immortal.subtitle", "You rise again – now the light rests for %s minutes",
                "Du stehst wieder auf – nun ruht das Licht %s Minuten");
        text.add("hero.kingdomsatwar.immortal.kept", "✦ The light of immortality keeps everything you carried – it awaits you when you rise again",
                "✦ Das Licht der Unsterblichkeit bewahrt dein Hab und Gut – es wartet auf dich, wenn du wieder aufstehst");
        text.add("hero.kingdomsatwar.immortal.saved", "✦ The light of immortality saves you – now it rests for %s minutes",
                "✦ Das Licht der Unsterblichkeit bewahrt dich – nun ruht es %s Minuten");
        text.add("hero.kingdomsatwar.rank_up", "New rank: %s – %s extra talent points!", "Neuer Rang: %s – %s Talentpunkte extra!");
        text.add("hero.kingdomsatwar.reward.pat",
                "You are a Legend! Sneak and right-click any creature with an empty hand (or press %s) to pat it – hold to keep patting. "
                        + "Your own soldiers and animals get half a heart back.",
                "Du bist eine Legende! Schleiche und klicke ein Wesen mit leerer Hand rechts an (oder drücke %s), um es zu streicheln – "
                        + "gedrückt halten streichelt weiter. Deine eigenen Soldaten und Tiere bekommen dabei ein halbes Herz zurück.");
        text.add("subtitles.kingdomsatwar.hero.pat", "Pat", "Streicheln");
        text.add("hero.kingdomsatwar.pat.locked", "Patting is the reward of the Legend (hero level %s)",
                "Streicheln ist die Belohnung der Legende (Heldenstufe %s)");
        text.add("hero.kingdomsatwar.stretched", "The hero's path now leads up to level %s – your might stays the same, you are now "
                + "hero level %s.", "Der Heldenweg führt jetzt bis Stufe %s – deine Kraft bleibt, wie sie war: Du bist jetzt Heldenstufe %s.");
        text.add("hero.kingdomsatwar.settled", "The hero levels have changed – you are now hero level %s and get all talent points back to "
                + "spend anew (free of charge).", "Die Heldenstufen haben sich geändert – du bist jetzt Heldenstufe %s und bekommst alle "
                + "Talentpunkte zurück, um sie neu zu verteilen (kostenlos).");
        text.add("hero.kingdomsatwar.rank.squire", "Squire", "Knappe");
        text.add("hero.kingdomsatwar.rank.warrior", "Warrior", "Recke");
        text.add("hero.kingdomsatwar.rank.knight", "Knight", "Ritter");
        text.add("hero.kingdomsatwar.rank.hero", "Hero", "Held");
        text.add("hero.kingdomsatwar.rank.warlord", "Warlord", "Kriegsherr");
        text.add("hero.kingdomsatwar.rank.legend", "Legend", "Legende");
        text.add("hero.kingdomsatwar.rank.myth", "Myth", "Mythos");
        text.add("hero.kingdomsatwar.rank.demigod", "Demigod", "Halbgott");
        text.add("hero.kingdomsatwar.rank.titan", "Titan", "Titan");
        text.add("hero.kingdomsatwar.rank.starwalker", "Starwalker", "Sternenwanderer");
        text.add("hero.kingdomsatwar.rank.immortal", "Immortal", "Unsterblicher");
        text.add("hero.kingdomsatwar.discovered", "New biome discovered: %s", "Neues Biom entdeckt: %s");
        text.add("hud.kingdomsatwar.hero.gained", "+%s hero experience", "+%s Heldenerfahrung");
        text.add("hud.kingdomsatwar.hero.plus", "+%s", "+%s");
        text.add("commands.kingdomsatwar.hero.status", "%s: hero level %s (%s) · %s / %s hero experience · %s talent points free",
                "%s: Heldenstufe %s (%s) · %s / %s Heldenerfahrung · %s Talentpunkte frei");
        text.add("commands.kingdomsatwar.army.xp", "%s combat experience for each of your %s soldiers nearby",
                "%s Kampferfahrung für jeden deiner %s Soldaten in der Nähe");
        talent(text, "strength", "Strength", "Stärke", "+%s damage", "+%s Schaden",
                "+0.3 damage per point with every blow", "+0,3 Schaden je Punkt bei jedem Schlag");
        talent(text, "vitality", "Vitality", "Lebenskraft", "+%s health", "+%s Leben",
                "+1 health per point – a heart for every two", "+1 Leben je Punkt – ein Herz für je zwei");
        talent(text, "agility", "Agility", "Gewandtheit", "+%s %% speed", "+%s %% Tempo",
                "+1.25 % walking speed per point, +0.5 % in mastery", "+1,25 % Lauftempo je Punkt, in der Meisterschaft +0,5 %");
        talent(text, "toughness", "Toughness", "Zähigkeit", "+%s armour", "+%s Rüstung",
                "+0.4 armour per point, in mastery also +0.25 armour toughness", "+0,4 Rüstung je Punkt, in der Meisterschaft dazu +0,25 Rüstungshärte");
        text.add("hero.kingdomsatwar.talent.toughness.effect_mastery", "+%s armour · +%s toughness", "+%s Rüstung · +%s Härte");
        text.add("hero.kingdomsatwar.talent.toughness.effect_hard", "+%s toughness", "+%s Rüstungshärte");
        talent(text, "arcana", "Arcana", "Zauberkunst", "+%s mana", "+%s Mana",
                "+8 mana and 4 % faster mana recovery per point", "+8 Mana und 4 % schnellere Erholung des Manas je Punkt");
        talent(text, "fortune", "Fortune", "Glück", "+%s luck", "+%s Glück",
                "+0.2 luck per point – better loot in chests", "+0,2 Glück je Punkt – bessere Beute in Truhen");
        talent(text, "fishing", "Angling", "Angelkunst", "+%s %% bite", "+%s %% Biss",
                "Fish bite 1.5 % sooner and the catch zone in the fight grows by 1 % per point; in mastery 1 % sooner, 1 % more zone and "
                        + "0.1 luck for fishing per point (rarer fish, less junk, more chests)",
                "Die Fische beißen je Punkt 1,5 % schneller, und die Fangzone im Drill wird 1 % größer; in der Meisterschaft 1 % schneller, "
                        + "1 % mehr Zone und 0,1 Glück beim Angeln je Punkt (seltenere Fische, weniger Kram, öfter eine Truhe)");
        text.add("hero.kingdomsatwar.talent.fishing.effect_mastery", "+%s %% bite · +%s luck", "+%s %% Biss · +%s Glück");
    }

    private static void talent(Text text, String id, String english, String deutsch, String effectEn, String effectDe, String descEn,
            String descDe) {
        String key = "hero.kingdomsatwar.talent." + id;
        text.add(key, english, deutsch);
        text.add(key + ".effect", effectEn, effectDe);
        text.add(key + ".desc", descEn, descDe);
    }
}
