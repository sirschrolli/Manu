package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte der Totems und der Äpfel der Macht (seit 0.80). */
final class RelicLang {
    private RelicLang() {}

    static void register(HeroLang.Text text) {
        text.add("itemGroup.kingdomsatwar.relics", "Kingdoms at War: Totems & Apples", "Kingdoms at War: Totems & Äpfel");
        text.add("totem.kingdomsatwar.offhand", "In the off hand:", "In der Zweithand:");
        text.add("totem.kingdomsatwar.death", "When death comes:", "Wenn der Tod kommt:");
        text.add("totem.kingdomsatwar.charges", "Charges: %1$s / %2$s", "Ladungen: %1$s / %2$s");

        text.add("item.kingdomsatwar.totem_ember", "Totem of Embers", "Totem der Glut");
        text.add("totem.kingdomsatwar.ember.passive.1", "Fire and lava cannot burn you", "Feuer und Lava verbrennen dich nicht");
        text.add("totem.kingdomsatwar.ember.passive.2", "Whoever strikes you catches fire", "Wer dich schlägt, fängt Feuer");
        text.add("totem.kingdomsatwar.ember.revive.1", "You rise with 8 hearts, fire-proof and shielded",
                "Du stehst mit 8 Herzen wieder auf, feuerfest und geschützt");
        text.add("totem.kingdomsatwar.ember.revive.2", "Fire nova: foes within 6 blocks burn", "Feuernova: Feinde im Umkreis von 6 Blöcken brennen");

        text.add("item.kingdomsatwar.totem_frost", "Totem of Frost", "Totem des Frosts");
        text.add("totem.kingdomsatwar.frost.passive.1", "You never freeze", "Du erfrierst nicht");
        text.add("totem.kingdomsatwar.frost.passive.2", "Whoever strikes you is slowed by frost", "Wer dich schlägt, den lähmt der Frost");
        text.add("totem.kingdomsatwar.frost.revive.1", "You rise with 8 hearts and Resistance II", "Du stehst mit 8 Herzen und Resistenz II wieder auf");
        text.add("totem.kingdomsatwar.frost.revive.2", "Frost nova: foes within 6 blocks freeze", "Frostnova: Feinde im Umkreis von 6 Blöcken erstarren");

        text.add("item.kingdomsatwar.totem_storm", "Totem of the Storm", "Totem des Sturms");
        text.add("totem.kingdomsatwar.storm.revive.1", "You rise with 8 hearts, swift and hasty", "Du stehst mit 8 Herzen wieder auf, schnell und flink");
        text.add("totem.kingdomsatwar.storm.revive.2", "Lightning strikes up to six foes around you", "Blitze fahren in bis zu sechs Feinde ringsum");

        text.add("item.kingdomsatwar.totem_stone", "Totem of Stone", "Totem des Felsens");
        text.add("totem.kingdomsatwar.stone.revive.1", "You rise with 10 hearts – and ten extra hearts on top",
                "Du stehst mit 10 Herzen wieder auf – und zehn Extraherzen dazu");
        text.add("totem.kingdomsatwar.stone.revive.2", "Earthshock: foes within 6 blocks are hurled upwards",
                "Erdstoß: Feinde im Umkreis von 6 Blöcken werden emporgeschleudert");

        text.add("item.kingdomsatwar.totem_grove", "Totem of the Grove", "Totem des Hains");
        text.add("totem.kingdomsatwar.grove.passive.1", "Heals half a heart every 5 seconds", "Heilt alle 5 Sekunden ein halbes Herz");
        text.add("totem.kingdomsatwar.grove.passive.2", "Poison does not cling to you", "Gift haftet nicht an dir");
        text.add("totem.kingdomsatwar.grove.revive.1", "You rise with 12 hearts, sated, with Regeneration III",
                "Du stehst mit 12 Herzen wieder auf, satt und mit Regeneration III");
        text.add("totem.kingdomsatwar.grove.revive.2", "Your animals and troops nearby are healed", "Deine Tiere und Truppen in der Nähe werden geheilt");

        text.add("item.kingdomsatwar.totem_tide", "Totem of Tides", "Totem der Gezeiten");
        text.add("totem.kingdomsatwar.tide.passive.1", "Conduit Power: breathe, see and dig under water",
                "Meereskraft: atmen, sehen und graben unter Wasser");
        text.add("totem.kingdomsatwar.tide.revive.1", "You rise with 8 hearts; fire goes out, the sea carries you",
                "Du stehst mit 8 Herzen wieder auf; Feuer erlischt, das Meer trägt dich");
        text.add("totem.kingdomsatwar.tide.revive.2", "Tidal wave: foes within 7 blocks are swept away",
                "Flutwelle: Feinde im Umkreis von 7 Blöcken werden fortgespült");

        text.add("item.kingdomsatwar.totem_shadow", "Totem of Shadows", "Totem der Schatten");
        text.add("totem.kingdomsatwar.shadow.passive.1", "Night Vision", "Nachtsicht");
        text.add("totem.kingdomsatwar.shadow.passive.2", "Foes notice you only from half the distance",
                "Feinde bemerken dich erst aus halber Entfernung");
        text.add("totem.kingdomsatwar.shadow.revive.1", "You rise with 8 hearts – unseen and swift", "Du stehst mit 8 Herzen wieder auf – ungesehen und schnell");
        text.add("totem.kingdomsatwar.shadow.revive.2", "Foes within 10 blocks lose sight of you and weaken",
                "Feinde im Umkreis von 10 Blöcken verlieren dich aus den Augen und erlahmen");

        text.add("item.kingdomsatwar.totem_void", "Totem of the Void", "Totem der Leere");
        text.add("totem.kingdomsatwar.void.revive.1", "You rise with 8 hearts, a few steps away from danger",
                "Du stehst mit 8 Herzen wieder auf – ein Stück fort von der Gefahr");
        text.add("totem.kingdomsatwar.void.revive.2", "Saves you from the void, too: back to the last solid ground",
                "Rettet auch aus dem Nichts: zurück auf den letzten festen Boden");

        text.add("item.kingdomsatwar.totem_creator", "Totem of the Creator", "Totem des Schöpfers");
        text.add("totem.kingdomsatwar.creator.revive.1", "You rise with all your hearts, shielded and healing",
                "Du stehst mit allen Herzen wieder auf, geschützt und heilend");
        text.add("totem.kingdomsatwar.creator.revive.2", "Nova of light: foes within 8 blocks are struck and thrown back",
                "Lichtnova: Feinde im Umkreis von 8 Blöcken werden getroffen und fortgeschleudert");
        text.add("totem.kingdomsatwar.creator.revive.3", "Returns to your hand until it has held three times",
                "Kehrt in deine Hand zurück, bis es dreimal gehalten hat");

        text.add("item.kingdomsatwar.dragon_apple", "Dragon Apple", "Drachenapfel");
        text.add("item.kingdomsatwar.star_apple", "Star Apple", "Sternenapfel");
        text.add("item.kingdomsatwar.shadow_apple", "Shadow Apple", "Schattenapfel");
        text.add("item.kingdomsatwar.immortal_apple", "Apple of Immortality", "Apfel der Unsterblichkeit");
        text.add("apple.kingdomsatwar.eaten", "When eaten:", "Gegessen:");
        text.add("apple.kingdomsatwar.second_life", "Second Life: once, death cannot take you", "Zweites Leben: Einmal kann dich der Tod nicht holen");
        text.add("effect.kingdomsatwar.second_life", "Second Life", "Zweites Leben");
        text.add("message.kingdomsatwar.second_life", "Your Second Life has saved you.", "Dein Zweites Leben hat dich gerettet.");
    }
}
