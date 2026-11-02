package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte zu den Regeln (Härte, Himmelsrichtungen statt Koordinaten) und zu den Verzauberungen der Mod. */
final class RulesLang {
    private RulesLang() {}

    static void register(WarLang.Text text) {
        // Härte
        text.add("message.kingdomsatwar.hardness.weakened", "Death has left its mark: you are weakened for two minutes.",
                "Der Tod hat Spuren hinterlassen: Du bist zwei Minuten lang geschwächt.");
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
