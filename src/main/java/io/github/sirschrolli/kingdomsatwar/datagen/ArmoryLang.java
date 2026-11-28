package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/** Texte der Waffenkammer (0.84): Namen und Beschreibungen der Waffen, Spitzhacken und Schilde. */
final class ArmoryLang {
    private ArmoryLang() {}

    private static void gear(HeroLang.Text text, String id, String nameEn, String nameDe, String loreEn, String loreDe, String abilityEn,
            String abilityDe) {
        text.add("item.kingdomsatwar." + id, nameEn, nameDe);
        text.add(ModItems.loreKey(id, 1), loreEn, loreDe);
        text.add(ModItems.loreKey(id, 2), abilityEn, abilityDe);
    }

    static void register(HeroLang.Text text) {
        // --- Waffen ---
        gear(text, "steel_halberd", "Steel Halberd", "Stahl-Hellebarde",
                "A long pole with axe blade, hook and spike – reaches 4.5 blocks",
                "Eine lange Stange mit Axtblatt, Haken und Stoßklinge – reicht 4,5 Blöcke",
                "A full swing pierces up to two foes behind the target",
                "Ein voller Schlag durchbohrt bis zu zwei Gegner hinter dem Ziel");
        gear(text, "draconium_warhammer", "Draconium Warhammer", "Draconium-Kriegshammer",
                "Heavy and slow, with a glowing rune in its head",
                "Schwer und langsam, mit einer glühenden Rune im Kopf",
                "Hold use, then release: Earthshock hurls nearby foes away and sets them ablaze",
                "Benutzen halten und loslassen: Der Erdstoß schleudert Gegner ringsum fort und setzt sie in Brand");
        gear(text, "nightsteel_dagger", "Nightsteel Dagger", "Nachtstahl-Dolch",
                "Short and very fast, with a violet glowing fuller",
                "Kurz und sehr schnell, mit einer violett glühenden Hohlkehle",
                "Strikes from behind deal double damage",
                "Stiche von hinten machen doppelten Schaden");
        gear(text, "abyssite_greatsword", "Abyssite Greatsword", "Abyssit-Großschwert",
                "Long, heavy and slow – crystals of the abyss in its guard",
                "Lang, schwer und langsam – Kristalle des Abgrunds in der Parierstange",
                "A full swing cleaves everything in a half circle in front of you",
                "Ein voller Schlag spaltet alles im Halbkreis vor dir");
        gear(text, "steel_double_axe", "Steel Double Axe", "Stahl-Doppelaxt",
                "Two blades, twice the weight – it fells trees and foes",
                "Zwei Blätter, doppeltes Gewicht – sie fällt Bäume und Gegner",
                "A blocked strike disables the shield for ten seconds",
                "Ein geblockter Hieb legt den Schild zehn Sekunden lahm");
        // --- Spitzhacken ---
        gear(text, "miner_hammer", "Miner's Hammer", "Bergmannshammer",
                "A heavy steel block bound with brass",
                "Ein schwerer Stahlblock mit Messingbändern",
                "Mines three by three blocks – sneak to mine a single one",
                "Baut drei mal drei Blöcke ab – schleichend nur einen");
        gear(text, "ember_pickaxe", "Ember Pickaxe", "Glutspitzhacke",
                "A draconium pick with a glowing vein and an ember stone",
                "Eine Draconium-Picke mit glühender Ader und einem Glutstein",
                "Ores and raw materials come out smelted",
                "Erze und Rohstoffe kommen geschmolzen heraus");
        gear(text, "vein_breaker", "Vein Breaker", "Adernbrecher",
                "A long nightsteel pick with an ore crystal in its socket",
                "Eine lange Nachtstahl-Picke mit einem Erzkristall in der Fassung",
                "Breaks a whole ore vein at once – sneak to mine a single block",
                "Bricht eine ganze Erzader auf einmal – schleichend nur einen Block");
        gear(text, "ore_seeker", "Ore Seeker", "Erzspürer",
                "An abyssite pick with an eye that sees through stone",
                "Eine Abyssit-Picke mit einem Auge, das durch Stein sieht",
                "Use: ores within twelve blocks glow through the walls for eight seconds",
                "Benutzen: Erze im Umkreis von zwölf Blöcken leuchten acht Sekunden durch die Wände");
        // --- Schilde ---
        gear(text, "steel_buckler", "Steel Buckler", "Stahl-Rundschild",
                "Small, round and quick to raise",
                "Klein, rund und schnell gehoben",
                "Block right as you raise it to parry: the attacker is thrown back and weakened",
                "Gleich beim Heben geblockt ist pariert: Der Angreifer wird zurückgestoßen und geschwächt");
        gear(text, "dragon_shield", "Dragon Shield", "Drachenschild",
                "A draconium heater shield with a golden dragon mask",
                "Ein Wappenschild aus Draconium mit goldener Drachenmaske",
                "Whoever strikes it catches fire",
                "Wer gegen ihn schlägt, fängt Feuer");
        gear(text, "tower_shield", "Tower Shield", "Turmschild",
                "Tall and heavy – nightsteel banded with moonsilver",
                "Groß und schwer – Nachtstahl mit Bändern aus Mondsilber",
                "Covers your flanks too and holds firm, but slows you down",
                "Deckt auch die Flanken und steht fest, macht aber langsamer");
        gear(text, "mirror_shield", "Mirror Shield", "Spiegelschild",
                "Polished abyssite in a rim of glowing runes",
                "Poliertes Abyssit in einem Rand aus glühenden Runen",
                "Projectiles bounce back wherever you look",
                "Geschosse prallen zurück – dorthin, wohin du schaust");
        text.add("entity.kingdomsatwar.ore_glow", "Ore Glow", "Erzleuchten");
    }
}
