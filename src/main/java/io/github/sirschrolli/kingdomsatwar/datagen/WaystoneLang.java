package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte von Wegkompass und Wegsteinen (seit 1.12). */
final class WaystoneLang {
    private WaystoneLang() {}

    static void register(HeroLang.Text t) {
        t.add("item.kingdomsatwar.waystone_compass", "Waystone Compass", "Wegkompass");
        t.add("item.kingdomsatwar.waystone_compass.count", "Waystones: %s/%s", "Wegsteine: %s/%s");
        t.add("item.kingdomsatwar.waystone_compass.target", "Target: %s", "Ziel: %s");
        t.add("item.kingdomsatwar.waystone_compass.spawn", "No target – points to spawn", "Kein Ziel – zeigt zum Spawn");
        t.add("item.kingdomsatwar.waystone_compass.hint", "Use on a waystone: remember it · Right-click: choose a target",
                "An einem Wegstein: merken · Rechtsklick: Ziel wählen");

        t.add("block.kingdomsatwar.waystone", "Waystone", "Wegstein");
        t.add("block.kingdomsatwar.waystone.named", "Waystone (%s)", "Wegstein (%s)");
        t.add("lore.kingdomsatwar.waystone.1", "A Waystone Compass can remember it", "Ein Wegkompass kann ihn sich merken");
        t.add("lore.kingdomsatwar.waystone.2", "A name tag gives it a name", "Ein Namensschild gibt ihm einen Namen");

        t.add("message.kingdomsatwar.waystone.saved", "Remembered %s (%s/%s)", "%s gemerkt (%s/%s)");
        t.add("message.kingdomsatwar.waystone.full", "The compass already knows %s waystones – forget one first.",
                "Der Kompass kennt schon %s Wegsteine – vergiss erst einen.");
        t.add("message.kingdomsatwar.waystone.target", "The needle points to %s", "Die Nadel zeigt zu %s");
        t.add("message.kingdomsatwar.waystone.spawn", "No target – the needle points to spawn", "Kein Ziel – die Nadel zeigt zum Spawn");
        t.add("message.kingdomsatwar.waystone.forgotten", "Forgot %s", "%s vergessen");
        t.add("message.kingdomsatwar.waystone.gone", "%s is gone – the compass forgot it.", "%s steht nicht mehr – der Kompass hat ihn vergessen.");
        t.add("message.kingdomsatwar.waystone.renamed", "The waystone is now called %s", "Der Wegstein heißt jetzt %s");
        t.add("message.kingdomsatwar.waystone.info", "%s – use a Waystone Compass on it to remember it",
                "%s – mit einem Wegkompass anklicken, um ihn zu merken");

        String g = "gui.kingdomsatwar.waystone_compass.";
        t.add(g + "title", "Waystone Compass", "Wegkompass");
        t.add(g + "count", "%s/%s waystones", "%s/%s Wegsteine");
        t.add(g + "north", "N", "N");
        t.add(g + "east", "E", "O");
        t.add(g + "south", "S", "S");
        t.add(g + "west", "W", "W");
        t.add(g + "free", "Empty slot", "Freier Platz");
        t.add(g + "free_hint", "Use the compass on a waystone to remember it here", "Klicke mit dem Kompass einen Wegstein an, um ihn hier zu merken");
        t.add(g + "here", "You are here!", "Du bist da!");
        t.add(g + "distance", "about %s blocks %s", "etwa %s Blöcke Richtung %s");
        t.add(g + "distance_short", "%s bl. · %s", "%s Bl. · %s");
        t.add(g + "dimension.overworld", "in the Overworld", "in der Oberwelt");
        t.add(g + "dimension.nether", "in the Nether", "im Nether");
        t.add(g + "dimension.end", "in the End", "im Ende");
        t.add(g + "dimension.other", "in another world", "in einer anderen Welt");
        t.add(g + "click_select", "Click: set as target", "Klick: als Ziel wählen");
        t.add(g + "click_clear", "Click: clear target", "Klick: Ziel aufheben");
        t.add(g + "click_forget", "Right-click twice: forget", "Zweimal Rechtsklick: vergessen");
        t.add(g + "forget_confirm", "Right-click again: forget", "Nochmal Rechtsklick: vergessen");
        t.add(g + "clear", "Clear target", "Ziel aufheben");
        t.add(g + "clear_hint", "The needle points to spawn again", "Die Nadel zeigt wieder zum Spawn");
        t.add(g + "no_target", "No target", "Kein Ziel");
        t.add(g + "to_spawn", "The needle points to the world spawn", "Die Nadel zeigt zum Weltspawn");
        t.add(g + "spinning", "The needle spins – this world has no spawn", "Die Nadel kreist – hier gibt es keinen Spawn");
        t.add(g + "hint1", "Use the compass on a waystone to remember it (up to 8)", "Mit dem Kompass einen Wegstein anklicken: merken (bis zu 8)");
        t.add(g + "hint2", "Click: target · Right-click: forget · Keys 1–8", "Klick: Ziel · Rechtsklick: vergessen · Tasten 1–8");

        String[][] shortDirections = {{"north", "N", "N"}, {"northeast", "NE", "NO"}, {"east", "E", "O"}, {"southeast", "SE", "SO"},
                {"south", "S", "S"}, {"southwest", "SW", "SW"}, {"west", "W", "W"}, {"northwest", "NW", "NW"}};
        for (String[] d : shortDirections) {
            t.add("direction.kingdomsatwar.short." + d[0], d[1], d[2]);
        }
    }
}
