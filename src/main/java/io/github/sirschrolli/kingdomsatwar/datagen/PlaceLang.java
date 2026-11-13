package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.command.KawPlaces;
import java.util.LinkedHashMap;
import java.util.Map;

/** Texte von {@code /kaw boss}, {@code /kaw structure} und {@code /kaw dim} (seit 1.1.2): Namen aller Orte und Meldungen. */
final class PlaceLang {
    private PlaceLang() {}

    static void register(HeroLang.Text text) {
        // die Dimensionen, die noch fehlten
        text.add("command.kingdomsatwar.dim.name.abgrund", "in the Abyss", "im Abgrund");
        text.add("command.kingdomsatwar.dim.name.urmeer", "in the Primordial Sea", "im Urmeer");
        text.add("command.kingdomsatwar.dim.name.feenreich", "in the Fae Realm", "im Feenreich");

        // Suchen und Reisen
        text.add("command.kingdomsatwar.locate.where", "%s %s", "%s %s");
        text.add("command.kingdomsatwar.locate.site", "%s: %s", "%s: %s");
        text.add("command.kingdomsatwar.locate.boss", "%s – %s: %s", "%s – %s: %s");
        text.add("command.kingdomsatwar.locate.distance", " (%s blocks away)", " (%s Blöcke entfernt)");
        text.add("command.kingdomsatwar.locate.none", "Nothing found: %s %s.", "Nichts gefunden: %s %s.");
        text.add("command.kingdomsatwar.locate.click", "Click to travel there: %s %s", "Klicken, um hinzureisen: %s %s");
        text.add("command.kingdomsatwar.locate.arrived", "Arrived: %s %s (%s, %s, %s).", "Angekommen: %s %s (%s, %s, %s).");
        text.add("command.kingdomsatwar.locate.denied", "Travelling by click needs cheats (operator rights).",
                "Zum Hinreisen per Klick brauchst du Cheats (Operator-Rechte).");

        // Bosse wecken und zurücksetzen
        text.add("command.kingdomsatwar.boss.awakened", "%s awakens!", "%s erwacht!");
        text.add("command.kingdomsatwar.boss.already_awake", "%s is already awake.", "%s ist schon wach.");
        text.add("command.kingdomsatwar.boss.reset", "%s rests again – the lair is reset.", "%s ruht wieder – der Hort ist zurückgesetzt.");
        text.add("command.kingdomsatwar.boss.no_lair", "%1$s: the heart of the lair is missing (%2$s).",
                "%1$s: Der Kern des Horts fehlt (%2$s).");
        text.add("command.kingdomsatwar.boss.cannot_awaken", "%s cannot be awakened right now.", "%s lässt sich gerade nicht wecken.");

        Map<String, String[]> names = names();
        for (KawPlaces.Site site : KawPlaces.sites()) {
            String[] name = names.remove(site.id());
            if (name == null) {
                throw new IllegalStateException("Kein Name für den Ort " + site.id());
            }
            text.add("place.kingdomsatwar." + site.id(), name[0], name[1]);
        }
        if (!names.isEmpty()) {
            throw new IllegalStateException("Namen ohne Ort: " + names.keySet());
        }
    }

    /** Name jedes Orts: englisch, deutsch. */
    private static Map<String, String[]> names() {
        Map<String, String[]> map = new LinkedHashMap<>();
        // Oberwelt
        map.put("sternensturz", new String[] {"Fallen Star", "Sternensturz"});
        map.put("wasserstollen", new String[] {"Aqueduct of the Ancients", "Wasserstollen der Alten"});
        map.put("gezeitenschrein", new String[] {"Tide Shrine", "Gezeitenschrein"});
        map.put("seedrachengebeine", new String[] {"Sea Dragon Bones", "Gebeine eines Seedrachen"});
        map.put("karawanserei", new String[] {"Caravanserai", "Karawanserei"});
        map.put("schlangentempel", new String[] {"Serpent Temple", "Schlangentempel"});
        map.put("hexenbaum", new String[] {"Witch Tree", "Hexenbaum"});
        map.put("frostgrotte", new String[] {"Frost Grotto", "Frostgrotte"});
        map.put("raeuberlager", new String[] {"Bandit Camp", "Räuberlager"});
        map.put("koehlerei", new String[] {"Charcoal Burners' Camp", "Köhlerei"});
        map.put("glutpforte", new String[] {"Ember Gate", "Glutpforte"});
        map.put("muschelfeld", new String[] {"Clam Field", "Muschelfeld"});
        map.put("korallenpalast", new String[] {"Coral Palace", "Korallenpalast"});
        map.put("verschuettetes_kloster", new String[] {"Buried Abbey", "Verschüttetes Kloster"});
        map.put("stille_kathedrale", new String[] {"Silent Cathedral", "Stille Kathedrale"});
        map.put("pruefungsarena", new String[] {"Arena of Trials", "Arena der Prüfungen"});
        map.put("raubritterburg", new String[] {"Robber Baron's Castle", "Raubritterburg"});
        map.put("fehlerzone", new String[] {"Error Zone", "Fehlerzone"});
        map.put("versunkener_tempel", new String[] {"Sunken Temple", "Versunkener Tempel"});
        map.put("atoll", new String[] {"Sunken Atoll", "Versunkenes Atoll"});
        map.put("vulkankrater", new String[] {"Pyrrhon's Volcanic Crater", "Pyrrhons Vulkankrater"});
        map.put("gletscherkessel", new String[] {"Glaciara's Glacier Basin", "Glaciaras Gletscherkessel"});
        map.put("sturmgipfel", new String[] {"Voltarion's Storm Peaks", "Voltarions Sturmgipfel"});
        map.put("uralter_hain", new String[] {"Verdantis' Ancient Grove", "Verdantis' Uralter Hain"});
        map.put("felsschlucht", new String[] {"Terragon's Rift", "Terragons Felsschlucht"});
        map.put("versunkene_grotte", new String[] {"Thalassa's Sunken Grotto", "Thalassas Versunkene Grotte"});
        map.put("magierturm_feuer", new String[] {"Mage Tower of Fire", "Magierturm des Feuers"});
        map.put("magierturm_eis", new String[] {"Mage Tower of Ice", "Magierturm des Eises"});
        map.put("magierturm_blitz", new String[] {"Mage Tower of Lightning", "Magierturm des Blitzes"});
        map.put("magierturm_natur", new String[] {"Mage Tower of Nature", "Magierturm der Natur"});
        map.put("magierturm_erde", new String[] {"Mage Tower of Earth", "Magierturm der Erde"});
        map.put("magierturm_wasser", new String[] {"Mage Tower of Water", "Magierturm des Wassers"});
        map.put("fachwerkdorf", new String[] {"Timber-Frame Village", "Fachwerkdorf"});
        map.put("nordisches_dorf", new String[] {"Nordic Village", "Nordisches Dorf"});
        map.put("lehmdorf", new String[] {"Clay Village", "Lehmdorf"});
        map.put("pfahldorf", new String[] {"Stilt Village", "Pfahldorf"});
        map.put("stammburg", new String[] {"Realm Castle", "Stammburg"});
        map.put("siegelkreis", new String[] {"Seal Circle", "Siegelkreis"});
        // Nether
        map.put("aschenesse", new String[] {"Ash Forge", "Aschenesse"});
        map.put("flammenheiligtum", new String[] {"Flame Sanctuary", "Flammenheiligtum"});
        map.put("phasenheiligtum", new String[] {"Phase Sanctuary", "Phasenheiligtum"});
        map.put("hortfeste", new String[] {"Hoard Fortress", "Hortfeste"});
        map.put("riesengebeine", new String[] {"Giant Bones", "Riesengebeine"});
        map.put("verlorene_pforte", new String[] {"Lost Gate", "Verlorene Pforte"});
        // End
        map.put("sternwarte", new String[] {"Starwatch", "Sternwarte"});
        // Schattenwelt
        map.put("zitadelle", new String[] {"Shadow Citadel", "Schattenzitadelle"});
        map.put("seelenpferch", new String[] {"Soul Pen", "Seelenpferch"});
        map.put("umbrerdorf", new String[] {"Umbrer Village", "Dorf der Umbrer"});
        map.put("mondtempel", new String[] {"Moon Temple", "Mondtempel"});
        map.put("kristallmine", new String[] {"Crystal Mine", "Kristallmine"});
        map.put("leuchtturm", new String[] {"Lighthouse", "Leuchtturm"});
        map.put("ruine_der_alten_welt", new String[] {"Ruin of the Old World", "Ruine der alten Welt"});
        // Leere
        map.put("thron", new String[] {"Throne of the Ancients", "Thron der Alten"});
        map.put("sternennetz", new String[] {"Star Web", "Sternennetz"});
        // Abgrund
        map.put("schlund_der_welten", new String[] {"Maw of Worlds", "Schlund der Welten"});
        map.put("uhrturm", new String[] {"Clock Tower", "Uhrturm"});
        // Urmeer
        map.put("krakenschlund", new String[] {"Kraken's Maw", "Krakenschlund"});
        map.put("meeresdorf", new String[] {"Sea Village", "Meeresdorf"});
        map.put("hexengrotte", new String[] {"Witch Grotto", "Hexengrotte"});
        // Feenreich
        map.put("mondhain", new String[] {"Moon Grove", "Mondhain"});
        map.put("zauberbuehne", new String[] {"Magic Stage", "Zauberbühne"});
        map.put("mondsteinbruch", new String[] {"Moonstone Quarry", "Mondsteinbruch"});
        return map;
    }
}
