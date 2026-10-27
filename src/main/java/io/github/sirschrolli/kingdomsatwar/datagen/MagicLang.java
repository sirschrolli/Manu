package io.github.sirschrolli.kingdomsatwar.datagen;

/**
 * Übersetzungen der Magie (Aetherit-Stab, Zauber, Zaubertafeln, Zauberkreis, Magietisch, Erzmagier und ihre Türme) –
 * Englisch und Deutsch.
 */
final class MagicLang {
    private MagicLang() {}

    static void register(EconomyLang.Text t) {
        staff(t);
        spells(t);
        tablets(t);
        wheel(t);
        table(t);
        mages(t);
        books(t);
    }

    private static void staff(EconomyLang.Text t) {
        t.add("item.kingdomsatwar.aetherit_staff", "Aetherit Staff", "Aetherit-Stab");
        t.add("lore.kingdomsatwar.aetherit_staff.1", "Right-click: cast the chosen spell", "Rechtsklick: gewählten Zauber wirken");
        t.add("lore.kingdomsatwar.aetherit_staff.2", "Learns new spells at the Magic Table", "Lernt neue Zauber am Magietisch");
        t.add("tooltip.kingdomsatwar.staff.spells", "Spells:", "Zauber:");
        t.add("tooltip.kingdomsatwar.staff.wheel", "Hold %s: spell circle", "%s halten: Zauberkreis");
        t.add("lore.kingdomsatwar.old_rune.1", "Ancient – inscribe it at the Magic Table like a spell tablet",
                "Uralt – am Magietisch wie eine Zaubertafel einschreiben");
        t.add("item.kingdomsatwar.fire_rune", "Fire Rune", "Feuerrune");
        t.add("item.kingdomsatwar.ice_rune", "Ice Rune", "Eisrune");
        t.add("item.kingdomsatwar.lightning_rune", "Lightning Rune", "Blitzrune");
        t.add("item.kingdomsatwar.nature_rune", "Nature Rune", "Naturrune");
        t.add("item.kingdomsatwar.earth_rune", "Earth Rune", "Erdrune");
        t.add("item.kingdomsatwar.water_rune", "Water Rune", "Wasserrune");
    }

    private static void spell(EconomyLang.Text t, String id, String nameEn, String nameDe, String descEn, String descDe,
            String level2En, String level2De, String level3En, String level3De) {
        String key = "spell.kingdomsatwar." + id;
        t.add(key, nameEn, nameDe);
        t.add(key + ".description", descEn, descDe);
        t.add(key + ".level2", level2En, level2De);
        t.add(key + ".level3", level3En, level3De);
    }

    private static void spells(EconomyLang.Text t) {
        spell(t, "aether_bolt", "Aether Bolt", "Aether-Blitz",
                "A beam of pure magic strikes the target you aim at.", "Ein Strahl reiner Magie trifft das anvisierte Ziel.",
                "More damage, greater reach", "Mehr Schaden, größere Reichweite",
                "Leaps to a second enemy", "Springt auf einen zweiten Feind über");
        spell(t, "fire", "Fireball", "Feuerball",
                "Hurls a fireball that sets enemies ablaze – but never your castle.",
                "Schleudert einen Feuerball, der Feinde in Brand setzt – aber nie die eigene Burg.",
                "Three fireballs in a fan", "Drei Feuerbälle im Fächer",
                "They burst into a cloud of flame", "Sie zerbersten in einer Feuerwolke");
        spell(t, "ice", "Frost Breath", "Frosthauch",
                "A breath of frost slows and chills enemies and turns water to ice.",
                "Ein Hauch aus Frost verlangsamt und unterkühlt Feinde und friert Wasser zu Eis.",
                "Colder and farther", "Kälter und weiter",
                "Freezes enemies solid", "Friert Feinde fest");
        spell(t, "lightning", "Lightning Strike", "Blitzschlag",
                "Calls down lightning where you aim.", "Ruft einen Blitz an die anvisierte Stelle.",
                "Leaps to two more enemies", "Springt auf zwei weitere Feinde über",
                "Chain lightning: up to four enemies", "Kettenblitz: bis zu vier weitere Feinde");
        spell(t, "nature", "Healing Bloom", "Heilende Blüte",
                "Heals you and your allies nearby and makes plants sprout.",
                "Heilt dich und Verbündete in der Nähe und lässt Pflanzen sprießen.",
                "Adds a protective barrier", "Dazu ein schützender Schild",
                "Roots seize nearby enemies", "Wurzeln packen Feinde in der Nähe");
        spell(t, "earth", "Earthquake", "Erdstoß",
                "The ground bursts open and hurls nearby enemies away.",
                "Der Boden bricht auf und schleudert Feinde in der Nähe von dir.",
                "Stronger and wider", "Stärker und weiter",
                "Rock spikes make enemies stagger", "Felsdornen lassen Feinde taumeln");
        spell(t, "water", "Tidal Wave", "Woge",
                "A wave pushes everything back, puts out fires and lets you breathe underwater.",
                "Eine Woge drängt alles zurück, löscht Feuer und lässt dich unter Wasser atmen.",
                "Also heals you", "Heilt dich dabei",
                "Water shield: you take less damage", "Wasserschild: du nimmst weniger Schaden");
    }

    private static void tablets(EconomyLang.Text t) {
        t.add("item.kingdomsatwar.fire_spell_tablet", "Fire Spell Tablet", "Zaubertafel des Feuers");
        t.add("item.kingdomsatwar.ice_spell_tablet", "Ice Spell Tablet", "Zaubertafel des Eises");
        t.add("item.kingdomsatwar.lightning_spell_tablet", "Lightning Spell Tablet", "Zaubertafel des Blitzes");
        t.add("item.kingdomsatwar.nature_spell_tablet", "Nature Spell Tablet", "Zaubertafel der Natur");
        t.add("item.kingdomsatwar.earth_spell_tablet", "Earth Spell Tablet", "Zaubertafel der Erde");
        t.add("item.kingdomsatwar.water_spell_tablet", "Water Spell Tablet", "Zaubertafel des Wassers");
        t.add("tooltip.kingdomsatwar.spell_tablet.use", "Inscribe it into an Aetherit Staff at the Magic Table",
                "Am Magietisch in einen Aetherit-Stab einschreiben");
    }

    private static void wheel(EconomyLang.Text t) {
        t.add("key.category.kingdomsatwar.magic", "Kingdoms at War: Magic", "Kingdoms at War: Magie");
        t.add("key.kingdomsatwar.spell_wheel", "Spell Circle (hold)", "Zauberkreis (halten)");
        t.add("gui.kingdomsatwar.spell_wheel", "Spell Circle", "Zauberkreis");
        t.add("gui.kingdomsatwar.spell_wheel.hint_hold", "Release to choose · 1–7 · Right-click: cancel",
                "Loslassen: wählen · 1–7 · Rechtsklick: abbrechen");
        t.add("gui.kingdomsatwar.spell_wheel.hint_click", "Click a spell · Esc: cancel", "Zauber anklicken · Esc: abbrechen");
        t.add("gui.kingdomsatwar.spell_wheel.level", "Level %s", "Stufe %s");
        t.add("gui.kingdomsatwar.spell_wheel.locked", "Not learned yet", "Noch nicht gelernt");
        t.add("gui.kingdomsatwar.spell_wheel.learn",
                "Defeat the archmage of this element in his tower and inscribe his spell tablet at the Magic Table.",
                "Besiege den Erzmagier dieses Elements in seinem Turm und schreibe seine Zaubertafel am Magietisch ein.");
        t.add("gui.kingdomsatwar.spell_wheel.cooldown", "Recharging: %s s", "Lädt: %s s");
        t.add("gui.kingdomsatwar.spell_wheel.ready", "Ready", "Bereit");
    }

    private static void table(EconomyLang.Text t) {
        t.add("block.kingdomsatwar.magic_table", "Magic Table", "Magietisch");
        t.add("lore.kingdomsatwar.magic_table.1", "Inscribes spell tablets into an Aetherit Staff",
                "Schreibt Zaubertafeln in einen Aetherit-Stab ein");
        t.add("lore.kingdomsatwar.magic_table.2", "Without a tablet: strengthens the Aether Bolt", "Ohne Tafel: stärkt den Aether-Blitz");
        t.add("container.kingdomsatwar.magic_table", "Magic Table", "Magietisch");
        t.add("gui.kingdomsatwar.magic_table.tablet", "Spell tablet", "Zaubertafel");
        t.add("gui.kingdomsatwar.magic_table.catalyst", "Aetherit", "Aetherit");
        t.add("gui.kingdomsatwar.magic_table.inscribe", "Inscribe", "Einschreiben");
        t.add("gui.kingdomsatwar.magic_table.learn", "The staff learns this spell", "Der Stab lernt diesen Zauber");
        t.add("gui.kingdomsatwar.magic_table.status.no_staff", "Place an Aetherit Staff in the centre of the circle.",
                "Lege einen Aetherit-Stab in die Mitte des Kreises.");
        t.add("gui.kingdomsatwar.magic_table.status.max_level", "This spell has already reached its highest level.",
                "Dieser Zauber hat schon die höchste Stufe.");
        t.add("gui.kingdomsatwar.magic_table.status.need_aetherit", "Not enough Aetherit", "Zu wenig Aetherit");
        t.add("gui.kingdomsatwar.magic_table.status.need_experience", "Not enough experience levels", "Zu wenige Erfahrungsstufen");
        t.add("gui.kingdomsatwar.magic_table.status.ready", "Ready", "Bereit");
    }

    private static void mage(EconomyLang.Text t, String element, String name, String titleEn, String titleDe, String phase2En,
            String phase2De, String phase3En, String phase3De) {
        t.add("entity.kingdomsatwar." + element + "_mage", name, name);
        t.add("item.kingdomsatwar." + element + "_mage_spawn_egg", name + " Spawn Egg", name + "-Spawn-Ei");
        t.add("boss.kingdomsatwar.mage_" + element, name + ", " + titleEn, name + ", " + titleDe);
        t.add("boss.kingdomsatwar.mage_" + element + ".title", titleEn, titleDe);
        t.add("boss.kingdomsatwar.mage_" + element + ".phase2", phase2En, phase2De);
        t.add("boss.kingdomsatwar.mage_" + element + ".phase3", phase3En, phase3De);
    }

    private static void mages(EconomyLang.Text t) {
        mage(t, "fire", "Ignatius", "Archmage of Flames", "Erzmagier der Flammen",
                "»You will burn!«", "»Ihr werdet brennen!«", "»Feel the wrath of the flames!«", "»Spürt den Zorn der Flammen!«");
        mage(t, "ice", "Isolde", "Archmage of Frost", "Erzmagierin des Frosts",
                "»Your blood shall freeze.«", "»Euer Blut soll gefrieren.«", "»The eternal winter awakens!«", "»Der ewige Winter erwacht!«");
        mage(t, "lightning", "Fulgor", "Archmage of Storms", "Erzmagier der Stürme",
                "»Hear the thunder!«", "»Hört den Donner!«", "»The storm obeys me!«", "»Der Sturm gehorcht mir!«");
        mage(t, "nature", "Sylvana", "Archmage of the Wilds", "Erzmagierin der Wildnis",
                "»The forest does not forget.«", "»Der Wald vergisst nicht.«", "»Root and thorn, take them!«", "»Wurzel und Dorn, holt sie!«");
        mage(t, "earth", "Gorvan", "Archmage of Stone", "Erzmagier des Gesteins",
                "»The mountain does not yield.«", "»Der Berg weicht nicht.«", "»Let the earth tremble!«", "»Die Erde soll beben!«");
        mage(t, "water", "Marena", "Archmage of the Tides", "Erzmagierin der Gezeiten",
                "»The tide is turning.«", "»Die Flut wendet sich.«", "»Sink into the deep!«", "»Versinkt in der Tiefe!«");
        t.add("entity.kingdomsatwar.mage_image", "Mirror Image", "Spiegelbild");
        t.add("entity.kingdomsatwar.magic_missile", "Magic Missile", "Magisches Geschoss");
        t.add("boss.kingdomsatwar.mage.defeated", "%s has been defeated. The spell tablet is yours.",
                "%s ist besiegt. Die Zaubertafel gehört dir.");
    }

    private static void book(EconomyLang.Text t, String element, String titleEn, String titleDe, String[] en, String[] de) {
        String key = "book.kingdomsatwar.mage_notes." + element;
        t.add(key + ".title", titleEn, titleDe);
        for (int i = 0; i < en.length; i++) {
            t.add(key + ".page" + (i + 1), en[i], de[i]);
        }
    }

    /** Die Aufzeichnungen der Erzmagier auf den Lesepulten ihrer Bibliotheken – mit Hinweisen für den Kampf. */
    private static void books(EconomyLang.Text t) {
        String hintEn = "My missiles follow you, yet a bold strike sends them back. My rune shield breaks after a few blows, my mirror "
                + "images after a single one.";
        String hintDe = "Meine Geschosse folgen dir, doch ein beherzter Schlag wirft sie zurück. Mein Runenschild bricht nach einigen "
                + "Hieben, meine Spiegelbilder schon nach einem.";
        book(t, "fire", "Notes of Ignatius", "Aufzeichnungen des Ignatius", new String[] {
                "Notes of Ignatius, Archmage of Flames.\n\nI raised this tower on black stone, far from forest and grass – fire tolerates "
                        + "no neighbours.",
                "The fire spell tablet rests in my sanctum. Whoever writes it into an Aetherit staff hurls fireballs that set foes "
                        + "ablaze – but never one's own roof.",
                hintEn,
                "When the sky glows, I call down meteors. Watch for the rings of smoke on the ground and never stand still.\n\nFire "
                        + "does not burn me."}, new String[] {
                "Aufzeichnungen des Ignatius, Erzmagier der Flammen.\n\nDiesen Turm habe ich auf schwarzem Stein errichtet, fern von Wald "
                        + "und Gras – das Feuer duldet keine Nachbarn.",
                "Die Zaubertafel des Feuers ruht in meinem Sanktum. Wer sie in einen Aetherit-Stab schreibt, schleudert Feuerbälle, die "
                        + "Feinde entzünden – doch nie das eigene Dach.",
                hintDe,
                "Glüht der Himmel, rufe ich Meteore herab. Achte auf die Kreise aus Rauch am Boden und bleib nie stehen.\n\nFeuer "
                        + "brennt mich nicht."});
        book(t, "ice", "Notes of Isolde", "Aufzeichnungen der Isolde", new String[] {
                "Notes of Isolde, Archmage of Frost.\n\nIn the eternal snow the air is clear and still. Here I can hear the ice grow.",
                "The frost spell tablet teaches Frost Breath: it slows foes, freezes them solid and turns water into ice. I keep it "
                        + "in my sanctum.",
                hintEn,
                "When I call the blizzard, icicles fall from above. A ring on the ground shows where they strike. Keep moving, or you "
                        + "will freeze.\n\nCold does not harm me."}, new String[] {
                "Aufzeichnungen der Isolde, Erzmagierin des Frosts.\n\nIm ewigen Schnee ist die Luft klar und still. Hier höre ich "
                        + "das Eis wachsen.",
                "Die Zaubertafel des Frosts lehrt den Frosthauch: Er verlangsamt Feinde, lässt sie erstarren und legt Wasser in Eis. "
                        + "Ich hüte sie in meinem Sanktum.",
                hintDe,
                "Rufe ich den Blizzard, fallen Eiszapfen aus der Höhe. Ein Kreis am Boden zeigt, wo sie treffen. Bleib in Bewegung, "
                        + "sonst gefrierst du.\n\nKälte schadet mir nicht."});
        book(t, "lightning", "Notes of Fulgor", "Aufzeichnungen des Fulgor", new String[] {
                "Notes of Fulgor, Archmage of Storms.\n\nOn the windy heights lightning strikes most gladly. My tower wears copper so "
                        + "that it listens to me.",
                "The storm spell tablet calls lightning to any spot you aim at. At higher levels the bolt leaps from foe to foe.",
                hintEn,
                "In my thunderstorm every strike is heralded by crackling sparks. Where they circle, it is about to crash.\n\n"
                        + "Lightning does not hurt me."}, new String[] {
                "Aufzeichnungen des Fulgor, Erzmagier der Stürme.\n\nAuf den windigen Höhen schlägt der Blitz am liebsten ein. Mein "
                        + "Turm trägt Kupfer, damit er mir zuhört.",
                "Die Zaubertafel der Stürme ruft Blitze an jede Stelle, die du anvisierst. In höheren Stufen springt der Blitz von "
                        + "Feind zu Feind.",
                hintDe,
                "Im Gewittersturm kündigt sich jeder Einschlag mit knisternden Funken an. Wo sie kreisen, kracht es gleich.\n\n"
                        + "Blitze treffen mich nicht."});
        book(t, "nature", "Notes of Sylvana", "Aufzeichnungen der Sylvana", new String[] {
                "Notes of Sylvana, Archmage of the Wilds.\n\nThe trees around my tower are older than any kingdom. They whisper to me "
                        + "who is coming.",
                "The wild spell tablet makes a healing bloom open: it heals you and your companions and lets plants sprout.",
                hintEn,
                "In my overgrowth vinelings burst from the ground and thorns hold you fast while I heal. Strike the vinelings "
                        + "quickly, or they will surround you."}, new String[] {
                "Aufzeichnungen der Sylvana, Erzmagierin der Wildnis.\n\nDie Bäume um meinen Turm sind älter als jedes Königreich. "
                        + "Sie flüstern mir zu, wer kommt.",
                "Die Zaubertafel der Wildnis lässt eine heilende Blüte erblühen: Sie heilt dich und deine Gefährten und lässt "
                        + "Pflanzen sprießen.",
                hintDe,
                "Im Wildwuchs brechen Ranklinge aus dem Boden, und Dornen halten dich fest, während ich heile. Schlag die Ranklinge "
                        + "rasch, sonst umringen sie dich."});
        book(t, "earth", "Notes of Gorvan", "Aufzeichnungen des Gorvan", new String[] {
                "Notes of Gorvan, Archmage of Stone.\n\nStone is patient. My tower of tuff will still stand when the castles of kings "
                        + "have crumbled to dust.",
                "The stone spell tablet teaches Earthquake: the ground bursts open and hurls foes away from you. I keep it in my "
                        + "sanctum.",
                hintEn,
                "In my rockquake I hurl boulders at you, and at the end the whole hall shakes. Keep your distance when rings of dust "
                        + "gather around me."}, new String[] {
                "Aufzeichnungen des Gorvan, Erzmagier des Gesteins.\n\nStein ist geduldig. Mein Turm aus Tuff steht noch, wenn die "
                        + "Burgen der Könige längst zu Staub zerfallen sind.",
                "Die Zaubertafel des Gesteins lehrt den Erdstoß: Der Boden bricht auf und schleudert Feinde von dir. Ich verwahre sie "
                        + "in meinem Sanktum.",
                hintDe,
                "Im Felsbeben werfe ich Brocken nach dir, und zum Schluss bebt der ganze Saal. Halte Abstand, wenn sich Staubringe um "
                        + "mich ziehen."});
        book(t, "water", "Notes of Marena", "Aufzeichnungen der Marena", new String[] {
                "Notes of Marena, Archmage of the Tides.\n\nOn the shore I hear the sea breathe. It comes and goes – and takes what "
                        + "it will.",
                "The tide spell tablet calls a wave: it pushes everything back, puts out fires and lets you breathe underwater.",
                hintEn,
                "In my maelstrom the whirl drags you towards me. Fight the pull and get away before the fountain bursts up in the "
                        + "middle."}, new String[] {
                "Aufzeichnungen der Marena, Erzmagierin der Gezeiten.\n\nAm Ufer höre ich das Meer atmen. Es kommt und geht – und "
                        + "nimmt mit, was es will.",
                "Die Zaubertafel der Gezeiten ruft eine Woge: Sie drängt alles zurück, löscht Feuer und lässt dich unter Wasser atmen.",
                hintDe,
                "Im Mahlstrom zieht dich der Wirbel zu mir. Stemm dich gegen den Sog und weiche, ehe in der Mitte die Fontäne "
                        + "emporschießt."});
    }
}
