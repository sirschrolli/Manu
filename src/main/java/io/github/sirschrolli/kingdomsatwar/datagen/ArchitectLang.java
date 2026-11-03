package io.github.sirschrolli.kingdomsatwar.datagen;

/** Übersetzungen des Dragon Architect (Vermessungsstab, eigene Baupläne, Bauweise) – Englisch und Deutsch. */
final class ArchitectLang {
    private ArchitectLang() {}

    static void register(EconomyLang.Text t) {
        rod(t);
        screen(t);
        blueprint(t);
        town(t);
        messages(t);
    }

    private static void rod(EconomyLang.Text t) {
        t.add("item.kingdomsatwar.surveyors_rod", "Surveyor's Rod", "Vermessungsstab");
        t.add("item.kingdomsatwar.surveyors_rod.lore1", "Use on two corners of a building", "An zwei Ecken eines Gebäudes benutzen");
        t.add("item.kingdomsatwar.surveyors_rod.lore2", "Sneak + use: draw it as your own blueprint", "Schleichen + Rechtsklick: als eigenen Bauplan zeichnen");
        t.add("item.kingdomsatwar.surveyors_rod.lore3", "Your builders build it again – block by block or in sections",
                "Deine Baumeister bauen es nach – Block für Block oder abschnittsweise");
        t.add("item.kingdomsatwar.surveyors_rod.area", "Area: %s × %s × %s", "Bereich: %s × %s × %s");
        t.add("item.kingdomsatwar.surveyors_rod.one", "First corner set", "Erste Ecke gesetzt");
        t.add("gui.kingdomsatwar.architect.corner1", "Corner 1: use", "Ecke 1: Rechtsklick");
        t.add("gui.kingdomsatwar.architect.corner2", "Corner 2: use", "Ecke 2: Rechtsklick");
        t.add("gui.kingdomsatwar.architect.too_large", "Too large (at most %s wide, %s high)", "Zu groß (höchstens %s breit, %s hoch)");
        t.add("gui.kingdomsatwar.architect.front", "Front", "Vorderseite");
        t.add("gui.kingdomsatwar.architect.front_door", "Front (door)", "Vorderseite (Tür)");
        t.add("gui.kingdomsatwar.architect.save_hint", "Sneak + use: draw blueprint", "Schleichen + Rechtsklick: Bauplan zeichnen");
    }

    private static void screen(EconomyLang.Text t) {
        t.add("gui.kingdomsatwar.architect.title", "Draw Blueprint", "Bauplan zeichnen");
        t.add("gui.kingdomsatwar.architect.default_name", "My building", "Mein Bauwerk");
        t.add("gui.kingdomsatwar.architect.name", "Name of the blueprint", "Name des Bauplans");
        t.add("gui.kingdomsatwar.architect.front_label", "Front", "Vorderseite");
        t.add("gui.kingdomsatwar.architect.side.north", "North", "Norden");
        t.add("gui.kingdomsatwar.architect.side.east", "East", "Osten");
        t.add("gui.kingdomsatwar.architect.side.south", "South", "Süden");
        t.add("gui.kingdomsatwar.architect.side.west", "West", "Westen");
        t.add("gui.kingdomsatwar.architect.front_door_hint", "The side with the main door – here the entrance will face you when you place the blueprint.",
                "Die Seite mit der Haupttür – beim Aufstellen zeigt der Eingang hier zu dir.");
        t.add("gui.kingdomsatwar.architect.front_hint", "The side the entrance faces when you place the blueprint.",
                "Die Seite, die beim Aufstellen des Bauplans zu dir zeigt.");
        t.add("gui.kingdomsatwar.architect.ground_label", "Ground line", "Bodenlinie");
        t.add("gui.kingdomsatwar.architect.ground_short", "ground", "Boden");
        t.add("gui.kingdomsatwar.architect.layer", "%s layer", "%s Lage");
        t.add("gui.kingdomsatwar.architect.layers", "%s layers", "%s Lagen");
        t.add("gui.kingdomsatwar.architect.ground_hint",
                "Layers of terrain below the building. Below the ground line, earth and rock stay as they are at the new site; cellars are dug out.",
                "Lagen Gelände unter dem Gebäude. Unter der Bodenlinie bleiben Erde und Fels am neuen Bauplatz, wie sie sind; Keller werden ausgehoben.");
        t.add("gui.kingdomsatwar.architect.blocks", "%s blocks", "%s Blöcke");
        t.add("gui.kingdomsatwar.architect.materials_hint",
                "Building materials your builder fetches from the storage: wood, stone and metal units.",
                "Baustoffe, die dein Baumeister aus dem Lager holt: Einheiten Holz, Stein und Metall.");
        t.add("gui.kingdomsatwar.architect.no_precious", "No precious blocks", "Keine kostbaren Blöcke");
        t.add("gui.kingdomsatwar.architect.precious_title", "Precious blocks", "Kostbare Blöcke");
        t.add("gui.kingdomsatwar.architect.precious_hint", "These must lie in the storage exactly like this.",
                "Diese müssen genau so im Lager liegen.");
        t.add("gui.kingdomsatwar.architect.skipped", "Not copied: %s", "Nicht übernommen: %s");
        t.add("gui.kingdomsatwar.architect.price", "Building costs about", "Bau kostet etwa");
        t.add("gui.kingdomsatwar.architect.empty", "There is nothing to draw here.", "Hier gibt es nichts zu zeichnen.");
        t.add("gui.kingdomsatwar.architect.cancel", "Cancel", "Abbrechen");
        t.add("gui.kingdomsatwar.architect.save", "Draw", "Zeichnen");
        t.add("gui.kingdomsatwar.architect.paper", "Uses one sheet of paper", "Braucht ein Blatt Papier");
        t.add("gui.kingdomsatwar.architect.paper_free", "Creative mode: no paper needed", "Kreativmodus: ohne Papier");
        t.add("gui.kingdomsatwar.plan.front", "Front view", "Ansicht");
        t.add("gui.kingdomsatwar.plan.top", "Ground plan", "Grundriss");
    }

    private static void blueprint(EconomyLang.Text t) {
        t.add("item.kingdomsatwar.blueprint.custom.by", "Drawn by %s", "Gezeichnet von %s");
        t.add("item.kingdomsatwar.blueprint.custom.size", "%s × %s × %s, %s blocks", "%s × %s × %s, %s Blöcke");
        t.add("item.kingdomsatwar.blueprint.custom.precious", "Precious: %s× %s", "Kostbar: %s× %s");
        t.add("item.kingdomsatwar.blueprint.custom.more_precious", "… and %s more precious blocks", "… und %s weitere kostbare Blöcke");
        t.add("building.kingdomsatwar.custom", "Own blueprint", "Eigener Bauplan");
        t.add("building.kingdomsatwar.custom.description", "A building you measured with the surveyor's rod.",
                "Ein Gebäude, das du mit dem Vermessungsstab vermessen hast.");
        t.add("building.kingdomsatwar.custom.design.0", "Own blueprint", "Eigener Bauplan");
    }

    private static void town(EconomyLang.Text t) {
        t.add("gui.kingdomsatwar.town.plans", "Own blueprints", "Eigene Pläne");
        t.add("gui.kingdomsatwar.town.plans.scan", "Measure!", "Vermessen!");
        t.add("gui.kingdomsatwar.town.plans.count", "%s plans", "%s Pläne");
        t.add("gui.kingdomsatwar.town.plans.description",
                "Buildings you measured with the surveyor's rod. Take a copy of a blueprint, place it in your settlement and your builders build it – block by block or in sections.",
                "Gebäude, die du mit dem Vermessungsstab vermessen hast. Nimm eine Abschrift eines Bauplans, stelle ihn in deiner Siedlung auf und deine Baumeister bauen ihn nach – Block für Block oder abschnittsweise.");
        t.add("gui.kingdomsatwar.town.plans.back", "◀ Back", "◀ Zurück");
        t.add("gui.kingdomsatwar.town.plans.title", "Own blueprints (%s)", "Eigene Baupläne (%s)");
        t.add("gui.kingdomsatwar.town.plans.rod", "Surveyor's rod", "Vermessungsstab");
        t.add("gui.kingdomsatwar.town.plans.rod.hint", "Take a surveyor's rod to measure buildings.",
                "Einen Vermessungsstab nehmen, um Gebäude zu vermessen.");
        t.add("gui.kingdomsatwar.town.plans.rod_taken", "Surveyor's rod taken", "Vermessungsstab genommen");
        t.add("gui.kingdomsatwar.town.plans.empty",
                "No own blueprints yet. Take the surveyor's rod, use it on two opposite corners of a building and then sneak + use to draw it.",
                "Noch keine eigenen Baupläne. Nimm den Vermessungsstab, tippe zwei gegenüberliegende Ecken eines Gebäudes an und zeichne es dann mit Schleichen + Rechtsklick.");
        t.add("gui.kingdomsatwar.town.plans.size", "%s×%s×%s · %s blocks", "%s×%s×%s · %s Blöcke");
        t.add("gui.kingdomsatwar.town.plans.take", "Copy", "Abschrift");
        t.add("gui.kingdomsatwar.town.plans.take.hint", "Take a copy of the blueprint (uses one sheet of paper)",
                "Eine Abschrift des Bauplans nehmen (braucht ein Blatt Papier)");
        t.add("gui.kingdomsatwar.town.plans.take.free", "Take a copy of the blueprint", "Eine Abschrift des Bauplans nehmen");
        t.add("gui.kingdomsatwar.town.plans.no_paper", "You need a sheet of paper for a copy", "Für eine Abschrift brauchst du ein Blatt Papier");
        t.add("gui.kingdomsatwar.town.plans.delete", "Delete %s", "%s löschen");
        t.add("gui.kingdomsatwar.town.plans.delete_confirm", "Click again to delete %s (buildings stay)",
                "Noch einmal klicken, um %s zu löschen (Gebäude bleiben stehen)");
        t.add("gui.kingdomsatwar.town.plans.deleted", "Blueprint deleted: %s", "Bauplan gelöscht: %s");
        t.add("gui.kingdomsatwar.town.mode.slow", "Block by block", "Block für Block");
        t.add("gui.kingdomsatwar.town.mode.fast", "In sections", "Abschnittsweise");
        t.add("gui.kingdomsatwar.town.mode.slow.hint", "The builder sets every block by hand – a true building site.",
                "Der Baumeister setzt jeden Block einzeln – eine echte Baustelle.");
        t.add("gui.kingdomsatwar.town.mode.fast.hint", "The builder raises whole sections at once – much faster, ideal for castles and walls.",
                "Der Baumeister zieht ganze Abschnitte auf einmal hoch – viel schneller, ideal für Burgen und Mauern.");
        t.add("gui.kingdomsatwar.town.mode.switch", "Click to switch", "Klicken zum Umschalten");
        t.add("gui.kingdomsatwar.town.notice.fast", "%s is now built in sections", "%s wird jetzt abschnittsweise gebaut");
        t.add("gui.kingdomsatwar.town.notice.slow", "%s is now built block by block", "%s wird jetzt Block für Block gebaut");
        t.add("gui.kingdomsatwar.town.waiting.precious",
                "The builder needs %s exactly like this – put it into a chest of the storage.",
                "Der Baumeister braucht %s genau so – lege es in eine Truhe des Lagers.");
        t.add("message.kingdomsatwar.town.precious", "%s× %s (%s in storage)", "%s× %s (%s im Lager)");
        t.add("gui.kingdomsatwar.town.import", "Read schematic", "Schematik einlesen");
        t.add("gui.kingdomsatwar.town.import.hint",
                "Read a .litematic, .schem or .nbt file from the schematics folder as your own blueprint.",
                "Eine .litematic-, .schem- oder .nbt-Datei aus dem Ordner »schematics« als eigenen Bauplan einlesen.");
        t.add("gui.kingdomsatwar.town.import.title", "Schematics (%s)", "Schematiken (%s)");
        t.add("gui.kingdomsatwar.town.import.refresh", "Look again", "Neu nachsehen");
        t.add("gui.kingdomsatwar.town.import.loading", "Looking into the folders …", "Sehe in den Ordnern nach …");
        t.add("gui.kingdomsatwar.town.import.empty",
                "No schematics found. Put .litematic, .schem or .nbt files into the folder »schematics« of your Minecraft folder (where Litematica keeps them). Schematics in config/worldedit/schematics and structures saved with a structure block are found too.",
                "Keine Schematiken gefunden. Lege .litematic-, .schem- oder .nbt-Dateien in den Ordner »schematics« deines Minecraft-Ordners (dort legt sie auch Litematica ab). Gefunden werden auch Schematiken in config/worldedit/schematics und Strukturen, die mit dem Strukturblock gespeichert wurden.");
        t.add("gui.kingdomsatwar.town.import.read", "Read", "Einlesen");
        t.add("gui.kingdomsatwar.town.import.read.hint", "Read %s as your own blueprint", "%s als eigenen Bauplan einlesen");
        t.add("gui.kingdomsatwar.town.import.done", "Read: %s", "Eingelesen: %s");
        t.add("gui.kingdomsatwar.town.import.done_unknown", "Read: %s (%s unknown blocks became air)",
                "Eingelesen: %s (%s unbekannte Blöcke wurden Luft)");
        t.add("gui.kingdomsatwar.town.import.error.missing", "The file is gone", "Die Datei gibt es nicht mehr");
        t.add("gui.kingdomsatwar.town.import.error.too_many", "Too many blueprints – delete old ones first", "Zu viele Baupläne – lösche erst alte");
        t.add("gui.kingdomsatwar.town.import.error.too_large", "Too large (at most %s wide and %s high)", "Zu groß (höchstens %s breit und %s hoch)");
        t.add("gui.kingdomsatwar.town.import.error.empty", "There is nothing in this file", "In dieser Datei ist nichts");
        t.add("gui.kingdomsatwar.town.import.error.broken", "This file cannot be read", "Diese Datei lässt sich nicht lesen");
        t.add("gui.kingdomsatwar.town.export", "Save as schematic", "Als Schematik ablegen");
        t.add("gui.kingdomsatwar.town.export.hint",
                "Saves the blueprint as .litematic, .schem and .nbt in schematics/kingdomsatwar – for Litematica, WorldEdit or the structure block.",
                "Legt den Bauplan als .litematic, .schem und .nbt in schematics/kingdomsatwar ab – für Litematica, WorldEdit oder den Strukturblock.");
        t.add("gui.kingdomsatwar.town.export.done", "Saved as schematic: %s", "Als Schematik abgelegt: %s");
        t.add("gui.kingdomsatwar.town.export.failed", "Could not save %s", "%s ließ sich nicht ablegen");
        t.add("message.kingdomsatwar.architect.exported", "Saved: %s", "Abgelegt: %s");
        t.add("message.kingdomsatwar.town.place.unknown", "This blueprint is unknown in this world", "Diesen Bauplan kennt diese Welt nicht");
    }

    private static void messages(EconomyLang.Text t) {
        t.add("message.kingdomsatwar.architect.first", "Corner 1 set (%s, %s, %s) – now the opposite corner",
                "Ecke 1 gesetzt (%s, %s, %s) – jetzt die gegenüberliegende Ecke");
        t.add("message.kingdomsatwar.architect.second", "Area %s × %s × %s – sneak + use to draw the blueprint",
                "Bereich %s × %s × %s – Schleichen + Rechtsklick zeichnet den Bauplan");
        t.add("message.kingdomsatwar.architect.too_large", "Too large – at most %s blocks wide and %s high",
                "Zu groß – höchstens %s Blöcke breit und %s hoch");
        t.add("message.kingdomsatwar.architect.too_far", "Step closer to the building", "Geh näher an das Gebäude heran");
        t.add("message.kingdomsatwar.architect.not_loaded", "Part of the area is not loaded", "Ein Teil des Bereichs ist nicht geladen");
        t.add("message.kingdomsatwar.architect.too_many", "You already have %s blueprints – delete old ones in the town window",
                "Du hast schon %s Baupläne – lösche alte im Stadtfenster");
        t.add("message.kingdomsatwar.architect.no_paper", "You need a sheet of paper", "Du brauchst ein Blatt Papier");
        t.add("message.kingdomsatwar.architect.empty", "There is nothing to draw in this area", "In diesem Bereich gibt es nichts zu zeichnen");
        t.add("message.kingdomsatwar.architect.failed", "The blueprint could not be saved", "Der Bauplan ließ sich nicht speichern");
        t.add("message.kingdomsatwar.architect.cleared", "Area cleared", "Bereich verworfen");
        t.add("message.kingdomsatwar.architect.how", "Use the rod on two corners of a building", "Tippe mit dem Stab zwei Ecken eines Gebäudes an");
        t.add("message.kingdomsatwar.architect.saved", "Blueprint \"%s\" drawn: %s × %s × %s, %s blocks",
                "Bauplan „%s“ gezeichnet: %s × %s × %s, %s Blöcke");
        t.add("message.kingdomsatwar.architect.skipped", "Not copied: %s", "Nicht übernommen: %s");
        t.add("message.kingdomsatwar.architect.count", "%s× %s", "%s× %s");
    }
}
