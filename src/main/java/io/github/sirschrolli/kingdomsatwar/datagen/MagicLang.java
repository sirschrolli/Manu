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
}
