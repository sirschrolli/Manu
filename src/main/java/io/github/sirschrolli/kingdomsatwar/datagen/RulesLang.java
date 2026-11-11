package io.github.sirschrolli.kingdomsatwar.datagen;

/** Texte zu den Regeln (Härte, Himmelsrichtungen statt Koordinaten) und zu den Verzauberungen der Mod. */
final class RulesLang {
    private RulesLang() {}

    static void register(WarLang.Text text) {
        // Verzauberungen auf den Waffen der Mod (seit 0.79): was Schärfe, Bann und Nemesis bringen
        text.add("tooltip.kingdomsatwar.enchant.sharpness", "+%s damage from Sharpness", "+%s Schaden durch Schärfe");
        text.add("tooltip.kingdomsatwar.enchant.smite", "+%s damage against the undead", "+%s Schaden gegen Untote");
        text.add("tooltip.kingdomsatwar.enchant.bane", "+%s damage against arthropods", "+%s Schaden gegen Gliederfüßer");
        text.add("tooltip.kingdomsatwar.enchant.ability", " …and its abilities hit %s%% harder", " … und ihre Fähigkeiten treffen %s %% härter");
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
        // Die Kampfdimensionen der großen Bosse
        text.add("message.kingdomsatwar.boss_arena.forbidden", "This world does not allow it.", "Diese Welt lässt es nicht zu.");
        text.add("message.kingdomsatwar.boss_arena.rescued", "The world hurls you back into the fight!",
                "Die Welt schleudert dich zurück in den Kampf!");
        text.add("message.kingdomsatwar.boss_arena.lost_everything", "Everything you carried is lost in this world.",
                "Alles, was du bei dir trugst, ist in dieser Welt verloren.");
        text.add("block.kingdomsatwar.bed.boss_arena", "There is no rest here – not while the fight rages.",
                "Hier gibt es keine Ruhe – nicht, solange der Kampf tobt.");
        bossArena(text, "moon_stage", "Moon Stage", "Mondbühne",
                "MissMystik pulls you onto her Moon Stage – the show begins!",
                "MissMystik holt dich auf ihre Mondbühne – die Vorstellung beginnt!",
                "The curtain falls – MissMystik is defeated. The Moon Stage releases you, together with her treasure.",
                "Der Vorhang fällt – MissMystik ist besiegt. Die Mondbühne entlässt euch samt ihrem Schatz.",
                "The show is over – MissMystik vanishes into her hat and waits for a new audience.",
                "Die Vorstellung ist vorbei – MissMystik verschwindet in ihrem Zylinder und wartet auf neues Publikum.");
        bossArena(text, "ember_forge", "Ember Forge", "Glutesse",
                "Malleus drags you into his Ember Forge!",
                "Malleus reißt dich in seine Glutesse!",
                "The forge grows cold – Malleus has fallen. You return with his treasure.",
                "Die Esse erkaltet – Malleus ist gefallen. Ihr kehrt mit seiner Beute zurück.",
                "The Ember Forge dies down – Malleus returns to his anvil.",
                "Die Glutesse erlischt – Malleus kehrt an seinen Amboss zurück.");
        bossArena(text, "star_canopy", "Star Canopy", "Sternenzelt",
                "Asteria pulls you up into the Star Canopy!",
                "Asteria zieht dich hinauf ins Sternenzelt!",
                "Asteria burns out among the stars – the Star Canopy releases you, together with her treasure.",
                "Asteria verglüht zwischen den Sternen – das Sternenzelt entlässt euch samt ihrem Schatz.",
                "The Star Canopy closes – Asteria returns to the sky.",
                "Das Sternenzelt schließt sich – Asteria kehrt in den Himmel zurück.");
        bossArena(text, "error_space", "Error Space", "Fehlerraum",
                "Anni loads you into the Error Space… ERROR 404: exit not found.",
                "Anni lädt dich in den Fehlerraum … ERROR 404: Ausgang nicht gefunden.",
                "Anni has crashed. The Error Space closes – you return with the loot.",
                "Anni ist abgestürzt. Der Fehlerraum schließt sich – ihr kehrt mit der Beute zurück.",
                "Connection lost. Anni resets himself.",
                "Verbindung getrennt. Anni setzt sich zurück.");
        bossArena(text, "throne_realm", "Throne Realm", "Thronwelt",
                "The Shattered King summons you before his throne in the Throne Realm!",
                "Der Zerbrochene König ruft dich vor seinen Thron in der Thronwelt!",
                "The Shattered King has fallen – the Throne Realm crumbles, and you return with his treasure.",
                "Der Zerbrochene König ist gefallen – die Thronwelt zerfällt, und ihr kehrt mit seiner Beute zurück.",
                "The Throne Realm closes – the king returns to his throne.",
                "Die Thronwelt schließt sich – der König kehrt auf seinen Thron zurück.");
        bossArena(text, "world_maw", "World Maw", "Weltenschlund",
                "The World-Eater swallows you – you fall into the World Maw!",
                "Der Weltenfresser verschlingt dich – du stürzt in den Weltenschlund!",
                "The World-Eater has crumbled – the World Maw spits you back out, together with his hoard.",
                "Der Weltenfresser ist zerfallen – der Weltenschlund speit euch samt seinem Hort zurück.",
                "The World Maw closes – the World-Eater sinks back into his hoard.",
                "Der Weltenschlund schließt sich – der Weltenfresser sinkt zurück in seinen Hort.");
        bossArena(text, "creation", "Creation", "Die Schöpfung",
                "A pillar of light seizes you – you are lifted into Creation, the court of Schrolli the Creator!",
                "Eine Säule aus Licht ergreift dich – du wirst in die Schöpfung gehoben, den Hof Schrollis des Schöpfers!",
                "Schrolli the Creator has been defeated – Creation releases you, and his treasure with you.",
                "Schrolli der Schöpfer ist bezwungen – die Schöpfung entlässt euch, und mit euch seinen Schatz.",
                "Creation fades – the Creator rests again, and the spark still glows in the altar.",
                "Die Schöpfung verblasst – der Schöpfer ruht wieder, und der Funke glüht noch im Altar.");
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

    /** Name einer Kampfdimension und was man beim Hineinziehen, beim Sieg und bei der Niederlage liest. */
    private static void bossArena(WarLang.Text text, String id, String nameEn, String nameDe, String pulledEn, String pulledDe,
            String victoryEn, String victoryDe, String defeatEn, String defeatDe) {
        text.add("dimension.kingdomsatwar." + id, nameEn, nameDe);
        text.add("message.kingdomsatwar.boss_arena." + id + ".pulled", pulledEn, pulledDe);
        text.add("message.kingdomsatwar.boss_arena." + id + ".victory", victoryEn, victoryDe);
        text.add("message.kingdomsatwar.boss_arena." + id + ".defeat", defeatEn, defeatDe);
    }

    private static void enchantment(WarLang.Text text, String id, String nameEn, String nameDe, String descEn, String descDe) {
        text.add("enchantment.kingdomsatwar." + id, nameEn, nameDe);
        text.add("enchantment.kingdomsatwar." + id + ".desc", descEn, descDe);
    }
}
