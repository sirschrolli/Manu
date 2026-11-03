package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/** Texte der Belagerungswaffen: Katapult, Tribok, Balliste, Rammbock, ihre Munition, Bedienung und Mannschaft. */
final class SiegeLang {
    @FunctionalInterface
    interface Text {
        void add(String key, String english, String deutsch);
    }

    private SiegeLang() {}

    static void register(Text text) {
        // --- Waffen (Lebewesen und Bausatz heißen gleich: der Bausatz stellt die Waffe auf) ---
        weapon(text, "catapult", "Catapult", "Katapult");
        weapon(text, "trebuchet", "Trebuchet", "Tribok");
        weapon(text, "ballista", "Ballista", "Balliste");
        weapon(text, "battering_ram", "Battering Ram", "Rammbock");
        text.add("entity.kingdomsatwar.siege_projectile", "Siege Projectile", "Belagerungsgeschoss");

        String place = "Place it on the ground, right-click: man it";
        String placeDe = "Auf den Boden setzen, Rechtsklick: bedienen";
        String dismantle = "Sneak + empty hand: dismantle (planks repair it)";
        String dismantleDe = "Schleichen + leere Hand: zerlegen (Bretter reparieren)";
        lore(text, "catapult", place, placeDe,
                "Hurls stone balls in a high arc – breaks walls", "Wirft Steinkugeln im hohen Bogen – zerschlägt Mauern",
                dismantle, dismantleDe);
        lore(text, "trebuchet", place, placeDe,
                "Hurls heavy boulders very far – smashes even castle walls", "Schleudert schwere Brocken sehr weit – zerschmettert selbst Burgmauern",
                dismantle, dismantleDe);
        lore(text, "ballista", place, placeDe,
                "Shoots heavy bolts fast and straight – pierces armour", "Schießt schwere Bolzen schnell und gerade – durchschlägt Rüstungen",
                dismantle, dismantleDe);
        lore(text, "battering_ram", "Place it on the ground, right-click: push it", "Auf den Boden setzen, Rechtsklick: schieben",
                "Breaks gates, doors and iron bars", "Bricht Tore, Türen und Eisengitter auf",
                dismantle, dismantleDe);

        // --- Munition ---
        text.add("item.kingdomsatwar.siege_stone", "Siege Stone", "Steinkugel");
        text.add(ModItems.loreKey("siege_stone", 1), "Ammunition for catapult and trebuchet", "Munition für Katapult und Tribok");
        text.add("item.kingdomsatwar.ballista_bolt", "Ballista Bolt", "Ballistenbolzen");
        text.add(ModItems.loreKey("ballista_bolt", 1), "Ammunition for the ballista – pierces up to three foes",
                "Munition für die Balliste – durchschlägt bis zu drei Gegner");
        text.add("tooltip.kingdomsatwar.siege.condition", "Condition: %s%%", "Zustand: %s %%");

        // --- Bedienung ---
        text.add("siege.kingdomsatwar.status", "%1$s · %2$s× %3$s · %4$s · range %5$s blocks · %6$s",
                "%1$s · %2$s× %3$s · %4$s · Wurfweite %5$s Blöcke · %6$s");
        text.add("siege.kingdomsatwar.status.ballista", "%1$s · %2$s× %3$s · %4$s · elevation %5$s° · %6$s",
                "%1$s · %2$s× %3$s · %4$s · Neigung %5$s° · %6$s");
        text.add("siege.kingdomsatwar.status.ram", "%1$s · %2$s · %3$s", "%1$s · %2$s · %3$s");
        text.add("siege.kingdomsatwar.ready", "Ready – Space", "Bereit – Leertaste");
        text.add("siege.kingdomsatwar.reloading", "Reloading %s%%", "Lädt nach %s %%");
        text.add("siege.kingdomsatwar.empty", "No ammunition", "Keine Munition");
        text.add("siege.kingdomsatwar.health", "Condition %s%%", "Zustand %s %%");
        text.add("siege.kingdomsatwar.no_ammo", "No ammunition – right-click it with %s", "Keine Munition – Rechtsklick mit %s lädt nach");
        text.add("siege.kingdomsatwar.loaded", "Loaded: %1$s/%2$s %3$s", "Geladen: %1$s/%2$s %3$s");
        text.add("siege.kingdomsatwar.full", "The store is full (%s)", "Der Vorrat ist voll (%s)");
        text.add("siege.kingdomsatwar.repaired", "Repaired – condition %s%%", "Repariert – Zustand %s %%");
        text.add("siege.kingdomsatwar.placed", "%s set up – right-click to man it", "%s aufgestellt – Rechtsklick zum Bedienen");
        text.add("siege.kingdomsatwar.no_room", "No room for %s here", "Hier ist kein Platz für %s");
        text.add("siege.kingdomsatwar.not_yours", "This siege weapon belongs to someone else", "Diese Belagerungswaffe gehört jemand anderem");
        text.add("siege.kingdomsatwar.controls.catapult", "Look: aim · W/S: drive · Space: throw · Sneak: step back",
                "Blick: zielen · W/S: fahren · Leertaste: werfen · Schleichen: zurücktreten");
        text.add("siege.kingdomsatwar.controls.trebuchet", "Look: aim · W/S: drive · Space: hurl · Sneak: step back",
                "Blick: zielen · W/S: fahren · Leertaste: schleudern · Schleichen: zurücktreten");
        text.add("siege.kingdomsatwar.controls.ballista", "Look: aim · W/S: move · Space: shoot · Sneak: step back",
                "Blick: zielen · W/S: schieben · Leertaste: schießen · Schleichen: zurücktreten");
        text.add("siege.kingdomsatwar.controls.ram", "W/S: push · Space: strike · Sneak: step back",
                "W/S: schieben · Leertaste: zustoßen · Schleichen: zurücktreten");

        // --- Mannschaft (Kommandostab) ---
        text.add("siege.kingdomsatwar.crew.assigned", "%2$s: %1$s takes the crew post", "%2$s: %1$s übernimmt die Bedienung");
        text.add("siege.kingdomsatwar.crew.dismissed", "%2$s: %1$s stands down", "%2$s: %1$s tritt ab");
        text.add("siege.kingdomsatwar.crew.selected", "%s awaits orders – point at a target (sneak + click the weapon: withdraw the crew)",
                "%s wartet auf Befehle – zeige auf ein Ziel (Schleichen + Klick auf die Waffe: Mannschaft abziehen)");
        text.add("siege.kingdomsatwar.crew.occupied", "%s is already manned", "%s ist schon besetzt");
        text.add("siege.kingdomsatwar.crew.nobody", "No free soldier of yours nearby to man it", "Kein freier Soldat in der Nähe, der sie bedienen kann");
        text.add("siege.kingdomsatwar.crew.attack", "%1$s: attack %2$s!", "%1$s: Angriff auf %2$s!");
        text.add("siege.kingdomsatwar.crew.bombard", "%s: bombard that position!", "%s: beschießt diese Stelle!");
        text.add("siege.kingdomsatwar.crew.ram", "%s: forward to the gate!", "%s: vorwärts zum Tor!");
        text.add("siege.kingdomsatwar.crew.free", "%s: fire at will", "%s: Feuer frei");
        text.add("siege.kingdomsatwar.crew.out_of_range", "%s: out of range", "%s: außer Reichweite");

        // --- Tod durch Belagerungswaffen ---
        death(text, "siege_stone", "%1$s was crushed by a stone from %2$s", "%1$s wurde von einer Steinkugel von %2$s zerschmettert",
                "%1$s was crushed by a stone ball while fighting %2$s", "%1$s wurde im Kampf gegen %2$s von einer Steinkugel zerschmettert");
        death(text, "ballista_bolt", "%1$s was skewered by %2$s", "%1$s wurde von %2$s aufgespießt",
                "%1$s was skewered by a ballista bolt while fighting %2$s", "%1$s wurde im Kampf gegen %2$s von einem Ballistenbolzen aufgespießt");
        death(text, "battering_ram", "%1$s was rammed flat by %2$s", "%1$s wurde von %2$s umgerammt",
                "%1$s was rammed flat while fighting %2$s", "%1$s wurde im Kampf gegen %2$s umgerammt");

        // --- Einstellung ---
        text.add("kingdomsatwar.configuration.siegeBlockDamage", "Siege weapons break blocks", "Belagerungswaffen zerstören Blöcke");
        text.add("kingdomsatwar.configuration.siegeBlockDamage.tooltip",
                "Catapults, trebuchets and battering rams smash walls, gates and doors (never inside settlements, at the seal circle or at a royal standard).",
                "Katapulte, Triboke und Rammböcke zerschlagen Mauern, Tore und Türen (nie in Siedlungen, am Siegelkreis oder am Reichsbanner).");
    }

    private static void weapon(Text text, String id, String english, String deutsch) {
        text.add("entity.kingdomsatwar." + id, english, deutsch);
        text.add("item.kingdomsatwar." + id, english, deutsch);
    }

    private static void lore(Text text, String id, String en1, String de1, String en2, String de2, String en3, String de3) {
        text.add(ModItems.loreKey(id, 1), en1, de1);
        text.add(ModItems.loreKey(id, 2), en2, de2);
        text.add(ModItems.loreKey(id, 3), en3, de3);
    }

    /** Todesmeldung durch den Schützen (bzw. die Waffe), mit benanntem Gegenstand in seiner Hand und ohne Verursacher. */
    private static void death(Text text, String id, String byEn, String byDe, String playerEn, String playerDe) {
        String key = "death.attack.kingdomsatwar." + id;
        text.add(key, byEn, byDe);
        text.add(key + ".item", byEn + " using %3$s", byDe.replace(" von %2$s", " von %2$s mit %3$s"));
        text.add(key + ".player", playerEn, playerDe);
    }
}
