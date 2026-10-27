package io.github.sirschrolli.kingdomsatwar.datagen;

/**
 * Übersetzungen der Dörfer: Dorfbewohner, ihre Berufe, was sie sich erzählen, Wachen und Karten – Englisch und
 * Deutsch.
 */
final class VillageLang {
    private VillageLang() {}

    static void register(EconomyLang.Text t) {
        t.add("entity.kingdomsatwar.villager", "Villager", "Dorfbewohner");
        t.add("item.kingdomsatwar.villager_spawn_egg", "Villager Spawn Egg", "Dorfbewohner-Spawn-Ei");
        professions(t);
        messages(t);
        directions(t);
        rumors(t);
    }

    private static void profession(EconomyLang.Text t, String id, String en, String de, String enFemale, String deFemale) {
        t.add("villager.kingdomsatwar.profession." + id, en, de);
        t.add("villager.kingdomsatwar.profession." + id + ".female", enFemale, deFemale);
    }

    private static void professions(EconomyLang.Text t) {
        profession(t, "peasant", "Day Labourer", "Tagelöhner", "Day Labourer", "Tagelöhnerin");
        profession(t, "farmer", "Farmer", "Bauer", "Farmer", "Bäuerin");
        profession(t, "fisher", "Fisher", "Fischer", "Fisher", "Fischerin");
        profession(t, "shepherd", "Shepherd", "Schäfer", "Shepherdess", "Schäferin");
        profession(t, "fletcher", "Bowyer", "Bogner", "Bowyer", "Bognerin");
        profession(t, "scribe", "Scribe", "Schreiber", "Scribe", "Schreiberin");
        profession(t, "cartographer", "Cartographer", "Kartograf", "Cartographer", "Kartografin");
        profession(t, "herbalist", "Herbalist", "Kräuterkundiger", "Herbalist", "Kräuterkundige");
        profession(t, "armorer", "Armorer", "Rüstungsschmied", "Armorer", "Rüstungsschmiedin");
        profession(t, "weaponsmith", "Weaponsmith", "Waffenschmied", "Weaponsmith", "Waffenschmiedin");
        profession(t, "toolsmith", "Toolsmith", "Werkzeugschmied", "Toolsmith", "Werkzeugschmiedin");
        profession(t, "butcher", "Butcher", "Metzger", "Butcher", "Metzgerin");
        profession(t, "tanner", "Tanner", "Gerber", "Tanner", "Gerberin");
        profession(t, "mason", "Stonemason", "Steinmetz", "Stonemason", "Steinmetzin");
        profession(t, "baker", "Baker", "Bäcker", "Baker", "Bäckerin");
        profession(t, "innkeeper", "Innkeeper", "Wirt", "Innkeeper", "Wirtin");
        profession(t, "merchant", "Merchant", "Händler", "Merchant", "Händlerin");
    }

    private static void messages(EconomyLang.Text t) {
        t.add("message.kingdomsatwar.villager.sleeping", "%s is asleep", "%s schläft");
        t.add("message.kingdomsatwar.villager.who", "%s, %s from %s", "%s, %s aus %s");
        t.add("message.kingdomsatwar.villager.who_alone", "%s, %s", "%s, %s");
        t.add("message.kingdomsatwar.villager.says", "%s: “%s”", "%s: »%s«");
        t.add("message.kingdomsatwar.villager.murdered", "You killed %s – the villagers will not forget it (−%s fame)",
                "Du hast %s getötet – das vergessen die Dörfler nicht (−%s Ansehen)");
        t.add("message.kingdomsatwar.soldier.village_guard", "%s guards %s", "%s bewacht %s");
        t.add("item.kingdomsatwar.map.village", "Map to a Village", "Karte zu einem Dorf");
        t.add("item.kingdomsatwar.map.mage_tower", "Map to a Mage Tower", "Karte zu einem Magierturm");
    }

    private static void directions(EconomyLang.Text t) {
        t.add("direction.kingdomsatwar.north", "north", "Norden");
        t.add("direction.kingdomsatwar.northeast", "northeast", "Nordosten");
        t.add("direction.kingdomsatwar.east", "east", "Osten");
        t.add("direction.kingdomsatwar.southeast", "southeast", "Südosten");
        t.add("direction.kingdomsatwar.south", "south", "Süden");
        t.add("direction.kingdomsatwar.southwest", "southwest", "Südwesten");
        t.add("direction.kingdomsatwar.west", "west", "Westen");
        t.add("direction.kingdomsatwar.northwest", "northwest", "Nordwesten");
    }

    private static void rumor(EconomyLang.Text t, String key, String en, String de) {
        t.add("villager.kingdomsatwar.rumor." + key, en, de);
    }

    private static void rumors(EconomyLang.Text t) {
        rumor(t, "nest", "They say there's a dragon's nest to the %s, not %s paces from here. The wise give it a wide berth.",
                "Man erzählt sich, im %s liege ein Drachennest – keine %s Schritte von hier. Wer klug ist, macht einen Bogen darum.");
        rumor(t, "seal", "The old folk speak of a ring of stones to the %s, a good %s paces away. Something was sealed there once that had better stay asleep.",
                "Die Alten reden von einem Kreis aus Steinen im %s, gut %s Schritte entfernt. Dort wurde einst etwas gebannt, das besser weiterschläft.");
        rumor(t, "village", "To the %s lies %s, a village much like ours – maybe %s paces. Give the folk there my regards!",
                "Im %s liegt %s, ein Dorf wie unseres – vielleicht %s Schritte. Grüß mir die Leute dort!");
        rumor(t, "rain", "Not fit weather for a dog. Come, get out of the rain!", "Bei dem Wetter schickt man keinen Hund vor die Tür. Komm, stell dich unter!");

        rumor(t, "peasant.1", "I help wherever I'm needed – hay today, the bakery tomorrow. There's always work.",
                "Ich helfe, wo man mich braucht – heute beim Heu, morgen beim Bäcker. Arbeit gibt es immer.");
        rumor(t, "peasant.2", "Since the dragons woke, everyone looks at the sky more often. Me too.",
                "Seit die Drachen wach sind, schaut jeder öfter zum Himmel. Ich auch.");
        rumor(t, "farmer.1", "Good weather, good soil – that's all wheat needs. That, and a farmer who gets up early.",
                "Gutes Wetter, guter Boden – mehr braucht der Weizen nicht. Und einen Bauern, der früh aufsteht.");
        rumor(t, "farmer.2", "Bring me pumpkins or melons, I pay in honest coin.", "Bring mir Kürbisse oder Melonen, ich zahle in guter Münze.");
        rumor(t, "fisher.1", "Fish bite best just at dusk. Every child knows that.", "Die Fische beißen am besten, wenn es eben dämmert. Das weiß jedes Kind.");
        rumor(t, "fisher.2", "Once I had something huge on the line. It nearly pulled me into the lake. Nobody believes me.",
                "Einmal hatte ich etwas Riesiges am Haken. Es hätte mich fast in den See gezogen. Keiner glaubt mir.");
        rumor(t, "shepherd.1", "My sheep know every stone around here. And I know every sheep by name.",
                "Meine Schafe kennen jeden Stein hier. Und ich kenne jedes Schaf beim Namen.");
        rumor(t, "shepherd.2", "Wool is the best money there is – it simply grows back.", "Wolle ist das beste Geld – sie wächst einfach nach.");
        rumor(t, "fletcher.1", "A good arrow needs straight spruce, sharp flint and goose feathers. And patience.",
                "Ein guter Pfeil braucht gerade Fichte, scharfen Feuerstein und Gänsefedern. Und Geduld.");
        rumor(t, "fletcher.2", "Ordinary arrows won't pierce dragon scales. But they'll do for everything else.",
                "Gegen Drachenschuppen hilft kein gewöhnlicher Pfeil. Gegen alles andere schon.");
        rumor(t, "scribe.1", "My books hold more knowledge than one person could gather in a lifetime. Some of it is even enchanted.",
                "In meinen Büchern steht mehr Wissen, als einer in seinem Leben sammeln kann. Manches davon ist sogar verzaubert.");
        rumor(t, "scribe.2", "Ink, parchment, steady hands – that's all I need. Well, and candles.",
                "Tinte, Pergament, ruhige Hände – mehr brauche ich nicht. Na gut, und Kerzen.");
        rumor(t, "cartographer.1", "Every map I draw was once a blank spot. I like blank spots.",
                "Jede Karte, die ich zeichne, war einmal ein weißer Fleck. Ich mag weiße Flecken.");
        rumor(t, "cartographer.2", "I know where the dragons nest. If you're brave enough – or foolish enough – I'll sell you a map.",
                "Ich weiß, wo die Drachen nisten. Wenn du mutig genug bist – oder dumm genug –, verkaufe ich dir eine Karte.");
        rumor(t, "herbalist.1", "There's a herb for almost everything. Not for dragonfire, sadly.",
                "Gegen fast alles ist ein Kraut gewachsen. Gegen Drachenfeuer leider nicht.");
        rumor(t, "herbalist.2", "Bring me nether wart and I'll brew you something that lets you see through the night.",
                "Bring mir Netherwarzen, und ich braue dir etwas, mit dem du durch die Nacht sehen kannst.");
        rumor(t, "armorer.1", "A chain shirt stops more than you'd think. Just not a dragon.",
                "Ein Kettenhemd hält mehr ab, als man denkt. Nur keinen Drachen.");
        rumor(t, "armorer.2", "Good armour is like a good friend: you only learn its worth when it's too late to get one.",
                "Gute Rüstung ist wie ein guter Freund: Was sie wert ist, merkt man erst, wenn es zu spät ist, eine zu holen.");
        rumor(t, "weaponsmith.1", "My steel has met wolves, bandits and a troll. The troll cost me an eye.",
                "Mein Stahl hat schon Wölfe, Räuber und einen Troll gesehen. Der Troll hat mich ein Auge gekostet.");
        rumor(t, "weaponsmith.2", "A blade is only as good as the hand that holds it. But a bad blade makes any hand bad.",
                "Eine Klinge ist nur so gut wie die Hand, die sie führt. Aber eine schlechte Klinge macht jede Hand schlecht.");
        rumor(t, "toolsmith.1", "With a good pickaxe you'll reach ores others only dream of.",
                "Mit einer guten Spitzhacke kommst du an Erze, von denen andere nur träumen.");
        rumor(t, "toolsmith.2", "Tools need care. Oil, a whetstone, and never leave them out in the rain!",
                "Werkzeug will gepflegt sein. Öl, Schleifstein, und nie im Regen liegen lassen!");
        rumor(t, "butcher.1", "Game from the forest tastes best. Bring me venison and I'll roast it for you.",
                "Wild aus dem Wald schmeckt am besten. Bring mir Hirsch, und ich mache dir einen Braten draus.");
        rumor(t, "butcher.2", "A knife, a chopping block and a fire – that's my whole art.",
                "Ein Messer, ein Hackklotz und ein Feuer – das ist meine ganze Kunst.");
        rumor(t, "tanner.1", "Smells strong, I know. But good leather wants good work.", "Riecht streng, ich weiß. Aber gutes Leder will gute Arbeit.");
        rumor(t, "tanner.2", "A saddle from my workshop outlasts the horse beneath it.",
                "Ein Sattel aus meiner Werkstatt hält länger als das Pferd darunter.");
        rumor(t, "mason.1", "Every stone has a face that wants to be seen. You just have to find it.",
                "Jeder Stein hat eine Seite, die gesehen werden will. Man muss sie nur finden.");
        rumor(t, "mason.2", "These walls have stood since my grandfather's day. They'll stand long after I'm gone.",
                "Die Mauern hier stehen seit meinem Großvater. Und sie werden noch stehen, wenn ich längst nicht mehr bin.");
        rumor(t, "baker.1", "Fresh bread every morning, before the rooster crows. Fancy a roll?",
                "Frisches Brot jeden Morgen, noch bevor der Hahn kräht. Magst du ein Brötchen?");
        rumor(t, "baker.2", "Flour everywhere – in my hair, my shoes, my bed. You get used to it.",
                "Mehl überall – in den Haaren, in den Schuhen, im Bett. Man gewöhnt sich dran.");
        rumor(t, "innkeeper.1", "A mug of ale, a warm seat by the fire and the latest tales – you'll find it all here.",
                "Ein Krug Bier, ein warmer Platz am Feuer und die neuesten Geschichten – das alles gibt es hier.");
        rumor(t, "innkeeper.2", "A knight was in yesterday swearing he'd seen a dragon. After his fourth mead.",
                "Gestern war ein Ritter hier, der schwor, er hätte einen Drachen gesehen. Nach dem vierten Met.");
        rumor(t, "merchant.1", "Goods from distant lands! If you can't find it here, it doesn't exist – or it's coming next week.",
                "Waren aus fernen Ländern! Was es hier nicht gibt, gibt es nicht – oder erst nächste Woche.");
        rumor(t, "merchant.2", "Haggle? With me? Fine, but only because you have such an honest face.",
                "Feilschen? Mit mir? Na schön, aber nur, weil du so ein ehrliches Gesicht hast.");

        String[][] generic = {
                {"Lovely day, isn't it?", "Schöner Tag heute, findest du nicht?"},
                {"The guards mean well, but against a dragon? I don't know.", "Die Wachen meinen es gut, aber gegen einen Drachen? Ich weiß nicht."},
                {"In the afternoon we all meet in the square. Why don't you join us?",
                        "Am Nachmittag treffen wir uns alle auf dem Platz. Komm doch dazu!"},
                {"My cousin made his fortune in the town. I'd rather stay here.", "Mein Vetter hat in der Stadt sein Glück gemacht. Ich bleibe lieber hier."},
                {"Some nights you can hear something big flying over the roofs.", "Nachts hört man manchmal etwas Großes über die Dächer fliegen."},
                {"Whoever trades honestly is always welcome here.", "Wer ehrlich handelt, ist hier immer willkommen."},
                {"Have you seen the mill? Our pride – well, the miller's pride.", "Hast du schon die Mühle gesehen? Unser Stolz – na ja, der Stolz des Müllers."},
                {"There used to be nothing but forest here. Our grandparents felled every tree themselves.",
                        "Früher war hier nur Wald. Unsere Großeltern haben jeden Baum selbst gefällt."},
                {"The tavern has the best mead far and wide.", "In der Schenke gibt es den besten Met weit und breit."},
                {"Stranger, you look like you've seen a thing or two.", "Fremder, du siehst aus, als hättest du schon einiges erlebt."},
                {"Tell me, is it true that dragons hoard gold?", "Sag mal, stimmt es, dass Drachen Gold horten?"},
                {"When the bell rings, everyone goes indoors. Remember that.", "Wenn die Glocke läutet, gehen alle in die Häuser. Merk dir das."},
                {"I've heard there are towers somewhere where mages live. Creepy.", "Ich habe gehört, irgendwo stehen Türme, in denen Magier hausen. Unheimlich."},
                {"You can only sleep soundly here when the guards are at their posts.",
                        "Ruhig schlafen kann man hier nur, wenn die Wachen auf ihren Posten sind."},
        };
        for (int i = 0; i < generic.length; i++) {
            rumor(t, "generic." + (i + 1), generic[i][0], generic[i][1]);
        }
    }
}
