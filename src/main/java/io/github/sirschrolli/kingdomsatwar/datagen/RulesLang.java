package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte zu den Regeln (Härte, Himmelsrichtungen statt Koordinaten) und zu den Verzauberungen der Mod. */
final class RulesLang {
    private RulesLang() {}

    static void register(WarLang.Text text) {
        // Härte
        text.add("message.kingdomsatwar.hardness.weakened", "Death still lingers in your bones – your blows are weaker for a while.",
                "Der Tod steckt dir noch in den Knochen – deine Schläge sind eine Weile schwächer.");
        // Die Arkane Sphäre
        text.add("message.kingdomsatwar.arena.pulled", "%s pulls you into the Arcane Sphere!", "%s zieht dich in die Arkane Sphäre!");
        text.add("message.kingdomsatwar.arena.rules",
                "Nothing can be built or broken here – and whoever dies here loses everything they carry.",
                "Hier lässt sich nichts bauen und nichts abbauen – und wer hier stirbt, verliert alles, was er bei sich trägt.");
        text.add("message.kingdomsatwar.arena.forbidden", "The Sphere does not allow it.", "Die Sphäre lässt es nicht zu.");
        text.add("message.kingdomsatwar.arena.rescued", "The Sphere hurls you back into the arena!", "Die Sphäre schleudert dich zurück in die Arena!");
        text.add("message.kingdomsatwar.arena.lost_everything", "Everything you carried is lost in the Sphere.",
                "Alles, was du bei dir trugst, ist in der Sphäre verloren.");
        text.add("message.kingdomsatwar.arena.victory", "The archmage has fallen – the Sphere releases you, together with his treasure.",
                "Der Erzmagier ist gefallen – die Sphäre entlässt euch samt seinem Schatz.");
        text.add("message.kingdomsatwar.arena.defeat", "The Sphere closes – the archmage returns to his tower.",
                "Die Sphäre schließt sich – der Erzmagier kehrt in seinen Turm zurück.");
        text.add("block.kingdomsatwar.bed.arcane_sphere", "There is no rest in the Arcane Sphere.", "In der Arkanen Sphäre gibt es keine Ruhe.");
        text.add("dimension.kingdomsatwar.arcane_sphere", "Arcane Sphere", "Arkane Sphäre");
        // Verzauberungen: Name und Erklärung (im Tooltip verzauberter Bücher)
        enchantment(text, "dragonslayer", "Dragonslayer", "Drachentöter",
                "Deals much more damage to dragons, dragon lords and lindworms.",
                "Viel mehr Schaden gegen Drachen, Drachenfürsten und Lindwürmer.");
        enchantment(text, "frostbite", "Frostbite", "Frostklinge",
                "Hits chill the target to the bone and slow it down.",
                "Treffer lassen den Gegner bis ins Mark erstarren und verlangsamen ihn.");
        enchantment(text, "lifesteal", "Lifesteal", "Lebensraub",
                "Every hit heals the wielder a little.",
                "Jeder Treffer heilt den Träger ein wenig.");
        enchantment(text, "armor_breaker", "Armor Breaker", "Rüstungsbrecher",
                "Axe blows cut through part of the target's armor.",
                "Axthiebe durchschlagen einen Teil der gegnerischen Rüstung.");
        enchantment(text, "volley", "Volley", "Pfeilhagel",
                "The bow looses three arrows at once.",
                "Der Bogen schießt drei Pfeile auf einmal.");
        enchantment(text, "dragonscale", "Dragonscale", "Drachenschuppe",
                "Protects against everything dragons do: claws, bites and breath.",
                "Schützt vor allem, was Drachen anrichten: Klauen, Bisse und Atem.");
        enchantment(text, "windstride", "Windstride", "Windschritt",
                "You walk and run faster.",
                "Du gehst und läufst schneller.");
        enchantment(text, "weight_curse", "Curse of Weight", "Fluch der Schwere",
                "This armor drags you down: you move more slowly.",
                "Diese Rüstung zieht dich nieder: Du bewegst dich langsamer.");
    }

    private static void enchantment(WarLang.Text text, String id, String nameEn, String nameDe, String descEn, String descDe) {
        text.add("enchantment.kingdomsatwar." + id, nameEn, nameDe);
        text.add("enchantment.kingdomsatwar." + id + ".desc", descEn, descDe);
    }
}
