package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;

/**
 * Texte der Belagerungswaffen und Fahrzeuge: Katapult, Tribok, Balliste, Rammbock, Belagerungsturm, Planwagen, ihre
 * Munition, Bedienung, Leine und Mannschaft.
 */
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
        weapon(text, "siege_tower", "Siege Tower", "Belagerungsturm");
        weapon(text, "wagon", "Covered Wagon", "Planwagen");
        text.add("entity.kingdomsatwar.siege_projectile", "Siege Projectile", "Belagerungsgeschoss");

        String place = "Place it on the ground, right-click: man it";
        String placeDe = "Auf den Boden setzen, Rechtsklick: bedienen";
        String dismantle = "Sneak + empty hand: dismantle (planks repair it)";
        String dismantleDe = "Schleichen + leere Hand: zerlegen (Bretter reparieren)";
        String tow = "On a lead a horse pulls it (lead on it, then sneak-click the horse)";
        String towDe = "An der Leine zieht sie ein Pferd (Leine dran, dann schleichend aufs Pferd klicken)";
        lore(text, "catapult", place, placeDe,
                "Hurls stone balls in a high arc – breaks walls", "Wirft Steinkugeln im hohen Bogen – zerschlägt Mauern",
                dismantle, dismantleDe, tow, towDe);
        lore(text, "trebuchet", place, placeDe,
                "Hurls heavy boulders very far – smashes even castle walls", "Schleudert schwere Brocken sehr weit – zerschmettert selbst Burgmauern",
                dismantle, dismantleDe, tow, towDe);
        lore(text, "ballista", place, placeDe,
                "Shoots heavy bolts fast and straight – pierces armour", "Schießt schwere Bolzen schnell und gerade – durchschlägt Rüstungen",
                dismantle, dismantleDe, tow, towDe);
        lore(text, "battering_ram", "Place it on the ground, right-click: push it", "Auf den Boden setzen, Rechtsklick: schieben",
                "Breaks gates, doors and iron bars", "Bricht Tore, Türen und Eisengitter auf",
                dismantle, dismantleDe, tow, towDe);
        lore(text, "siege_tower", "Place it on the ground, right-click: push it", "Auf den Boden setzen, Rechtsklick: schieben",
                "Rolls up to the wall and drops its bridge – eight warriors storm across",
                "Rollt an die Mauer und lässt die Brücke fallen – acht Krieger stürmen hinüber",
                "Right-click with 3 ladders: a storey higher (7, 10, 13 blocks) · sneak + axe: lower",
                "Rechtsklick mit 3 Leitern: ein Stockwerk höher (7, 10, 13 Blöcke) · Schleichen + Axt: niedriger",
                "Sneak + empty hand: dismantle · on a lead a horse pulls it (slowly)",
                "Schleichen + leere Hand: zerlegen · an der Leine zieht ihn ein Pferd (langsam)");
        lore(text, "wagon", "Place it on the ground, right-click: get on", "Auf den Boden setzen, Rechtsklick: aufsteigen",
                "Six rows of cargo and four passengers", "Sechs Reihen Laderaum und vier Mitfahrer",
                "Sneak + empty hand: cargo hold · sneak + axe: dismantle", "Schleichen + leere Hand: Laderaum · Schleichen + Axt: zerlegen",
                "Hitch a horse: lead on the wagon, then sneak-click the horse",
                "Pferd vorspannen: Leine an den Wagen, dann schleichend aufs Pferd klicken");

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
        text.add("siege.kingdomsatwar.controls.siege_tower", "Look: steer · W/S: push · Space: drop/raise the bridge · Sneak: step back",
                "Blick: lenken · W/S: schieben · Leertaste: Brücke herablassen/hochziehen · Schleichen: zurücktreten");
        text.add("siege.kingdomsatwar.controls.siege_tower.rider", "On the platform – once the bridge is down, sneak to storm across onto the wall",
                "Oben auf dem Turm – liegt die Brücke auf, geht es mit Schleichen hinüber auf die Mauer");
        text.add("siege.kingdomsatwar.controls.wagon", "On the coachbox – a horse pulls the wagon on a lead · Inventory key: cargo hold · Sneak: get off",
                "Auf dem Kutschbock – ein Pferd zieht den Wagen an der Leine · Inventartaste: Laderaum · Schleichen: absteigen");
        text.add("siege.kingdomsatwar.controls.wagon.rider", "In the wagon · Inventory key: cargo hold · Sneak: get off",
                "Im Wagen · Inventartaste: Laderaum · Schleichen: absteigen");

        // --- Belagerungsturm und Planwagen ---
        text.add("siege.kingdomsatwar.status.siege_tower", "%1$s · bridge %2$s · %3$s aboard · %4$s", "%1$s · Brücke %2$s · %3$s an Bord · %4$s");
        text.add("siege.kingdomsatwar.status.wagon", "%1$s · cargo %2$s/%3$s · %4$s aboard · %5$s", "%1$s · Laderaum %2$s/%3$s · %4$s an Bord · %5$s");
        text.add("siege.kingdomsatwar.bridge.up", "raised", "oben");
        text.add("siege.kingdomsatwar.bridge.down", "down – storm the wall!", "liegt auf – stürmt die Mauer!");
        text.add("siege.kingdomsatwar.bridge.moving", "moving", "in Bewegung");
        text.add("siege.kingdomsatwar.bridge.no_wall",
                "Nothing within reach of the bridge – push the tower right up to the wall, or match its height (ladders: higher, sneak + axe: lower)",
                "Nichts in Reichweite der Brücke – schiebe den Turm ganz an die Mauer oder passe seine Höhe an (Leitern: höher, Schleichen + Axt: niedriger)");
        text.add("siege.kingdomsatwar.bridge.too_high", "This wall is too high for the bridge – build the tower higher with ladders",
                "Diese Mauer ist zu hoch für die Brücke – baue den Turm mit Leitern höher");
        text.add("siege.kingdomsatwar.tower.height", "%1$s: platform %2$s blocks high", "%1$s: Plattform %2$s Blöcke hoch");
        text.add("siege.kingdomsatwar.tower.highest", "The tower cannot be built any higher (13 blocks)", "Höher lässt sich der Turm nicht bauen (13 Blöcke)");
        text.add("siege.kingdomsatwar.tower.lowest", "The tower cannot be any lower (7 blocks)", "Niedriger geht es nicht (7 Blöcke)");
        text.add("siege.kingdomsatwar.tower.ladders", "A storey takes %s ladders", "Ein Stockwerk braucht %s Leitern");
        text.add("siege.kingdomsatwar.tower.no_room", "No room above the tower for another storey", "Über dem Turm ist kein Platz für ein weiteres Stockwerk");
        text.add("siege.kingdomsatwar.bridge.no_room", "No room to stand at the end of the bridge", "Am Ende der Brücke ist kein Platz zum Stehen");
        text.add("siege.kingdomsatwar.bridge.raise_first", "Raise the bridge first (Space)", "Erst die Brücke hochziehen (Leertaste)");
        text.add("siege.kingdomsatwar.bridge.cross", "The bridge is down – sneak to storm across onto the wall!",
                "Die Brücke liegt auf – Schleichen: hinüber auf die Mauer!");
        text.add("siege.kingdomsatwar.hitched", "%1$s is hitched to the %2$s", "%1$s ist vor %2$s gespannt");

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
        text.add("siege.kingdomsatwar.crew.boarding", "%2$s: %1$s soldiers climb aboard", "%2$s: %1$s Krieger steigen auf");
        text.add("siege.kingdomsatwar.crew.boarding.siege_tower", "%2$s: %1$s soldiers climb up – now point at the wall to storm",
                "%2$s: %1$s Krieger steigen hinauf – zeige jetzt auf die Mauer, die gestürmt wird");
        text.add("siege.kingdomsatwar.crew.unboard", "%2$s: %1$s soldiers dismount and follow you", "%2$s: %1$s Krieger sitzen ab und folgen dir");
        text.add("siege.kingdomsatwar.crew.empty", "%2$s carries no soldiers", "%2$s trägt keine Krieger");
        text.add("siege.kingdomsatwar.crew.full", "%s is fully manned", "%s ist voll besetzt");
        text.add("siege.kingdomsatwar.crew.selected.siege_tower", "%s awaits orders – point at the wall to storm (sneak + click the tower: everyone dismounts)",
                "%s wartet auf Befehle – zeige auf die Mauer, die gestürmt wird (Schleichen + Klick auf den Turm: alle sitzen ab)");
        text.add("siege.kingdomsatwar.crew.siege_tower", "%s: forward to the wall – drop the bridge!", "%s: vorwärts an die Mauer – Brücke runter!");
        text.add("siege.kingdomsatwar.crew.halt", "%s: halt!", "%s: halt!");

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

    private static void lore(Text text, String id, String en1, String de1, String en2, String de2, String en3, String de3, String en4,
            String de4) {
        text.add(ModItems.loreKey(id, 1), en1, de1);
        text.add(ModItems.loreKey(id, 2), en2, de2);
        text.add(ModItems.loreKey(id, 3), en3, de3);
        text.add(ModItems.loreKey(id, 4), en4, de4);
    }

    /** Todesmeldung durch den Schützen (bzw. die Waffe), mit benanntem Gegenstand in seiner Hand und ohne Verursacher. */
    private static void death(Text text, String id, String byEn, String byDe, String playerEn, String playerDe) {
        String key = "death.attack.kingdomsatwar." + id;
        text.add(key, byEn, byDe);
        text.add(key + ".item", byEn + " using %3$s", byDe.replace(" von %2$s", " von %2$s mit %3$s"));
        text.add(key + ".player", playerEn, playerDe);
    }
}
